package com.benchmarking.dbcomparison.benchmark.concurrency;

import com.benchmarking.dbcomparison.config.DatabaseMetrics;
import com.benchmarking.dbcomparison.model.Customer;
import com.benchmarking.dbcomparison.model.Order;
import com.benchmarking.dbcomparison.model.OrderItem;
import com.benchmarking.dbcomparison.model.ProductReview;
import com.benchmarking.dbcomparison.repository.CustomerRepository;
import com.benchmarking.dbcomparison.repository.OrderItemRepository;
import com.benchmarking.dbcomparison.repository.OrderRepository;
import com.benchmarking.dbcomparison.repository.ProductReviewRepository;
import com.benchmarking.dbcomparison.util.DataGenerator;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class MultiThreadedDeleteTest {

    private static final int TOTAL_RECORDS = 1000;
    private static final int THREAD_COUNT = 10;
    private static final String METRIC_NAME = "customer_multithreaded_delete";
    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private ProductReviewRepository productReviewRepository;
    @Autowired
    private DatabaseMetrics databaseMetrics;

    static void setupGenerator() {
        new DataGenerator(); // opcjonalnie, jeśli potrzebne
    }

    //runAllTests
    public void runAllTests() throws InterruptedException {
        log.info("Rozpoczynam test wielowątkowego DELETE ({} wątków)", THREAD_COUNT);

        // Najpierw usuwamy powiązane rekordy
        deleteRelatedRecords();

        List<Customer> allCustomers = customerRepository.findAll();
        if (allCustomers.size() < TOTAL_RECORDS) {
            log.warn("Brak wystarczającej liczby rekordów do testu DELETE, znaleziono: {}", allCustomers.size());
            return;
        }

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        AtomicInteger successCounter = new AtomicInteger();
        AtomicInteger errorCounter = new AtomicInteger();

        List<Long> threadDurations = new CopyOnWriteArrayList<>();

        long startTime = System.nanoTime();
        int recordsPerThread = TOTAL_RECORDS / THREAD_COUNT;

        for (int i = 0; i < THREAD_COUNT; i++) {
            int startIdx = i * recordsPerThread;
            int endIdx = startIdx + recordsPerThread;
            List<Customer> customersSlice = allCustomers.subList(startIdx, endIdx);

            executor.submit(() -> {
                Timer.Sample timer = databaseMetrics.startTimer();
                long threadStart = System.nanoTime();

                try {
                    customerRepository.deleteAll(customersSlice);

                    databaseMetrics.incrementDatabaseOperations(METRIC_NAME, activeProfile);
                    databaseMetrics.recordDataSize(METRIC_NAME, activeProfile, customersSlice.size());

                    successCounter.addAndGet(customersSlice.size());
                } catch (Exception e) {
                    log.error("Błąd w wątku DELETE", e);
                    databaseMetrics.incrementDatabaseErrors(METRIC_NAME, activeProfile);
                    databaseMetrics.incrementFailedQueries();
                    errorCounter.incrementAndGet();
                } finally {
                    long threadDuration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - threadStart);
                    threadDurations.add(threadDuration);
                    databaseMetrics.stopTimer(timer, METRIC_NAME, activeProfile);
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        long totalDuration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        double avgThreadTime = threadDurations.stream().mapToLong(Long::longValue).average().orElse(0);
        double opsPerSecond = totalDuration > 0 ? successCounter.get() / (totalDuration / 1000.0) : 0;

        log.info("Zakończono test DELETE: {} rekordów w {} ms", successCounter.get(), totalDuration);
        logPerformance(totalDuration, avgThreadTime, successCounter.get(), errorCounter.get(), threadDurations.size(), opsPerSecond);
    }

    private void deleteRelatedRecords() throws InterruptedException {
        // Usuwanie elementów zamówień
        List<OrderItem> orderItems = orderItemRepository.findAll();
        deleteInBatches(orderItems, orderItemRepository, "orderitem_delete");

        // Usuwanie recenzji produktów
        List<ProductReview> reviews = productReviewRepository.findAll();
        deleteInBatches(reviews, productReviewRepository, "productreview_delete");

        // Usuwanie zamówień
        List<Order> orders = orderRepository.findAll();
        deleteInBatches(orders, orderRepository, "order_delete");
    }

    private <T> void deleteInBatches(List<T> entities, JpaRepository<T, ?> repository, String metricName) throws InterruptedException {
        if (entities.isEmpty()) {
            return;
        }

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        int batchSize = Math.max(1, entities.size() / THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            int startIdx = i * batchSize;
            int endIdx = Math.min(startIdx + batchSize, entities.size());
            if (startIdx >= entities.size()) {
                latch.countDown();
                continue;
            }

            List<T> batch = entities.subList(startIdx, endIdx);
            executor.submit(() -> {
                Timer.Sample timer = databaseMetrics.startTimer();
                try {
                    repository.deleteAll(batch);
                    databaseMetrics.incrementDatabaseOperations(metricName, activeProfile);
                } catch (Exception e) {
                    log.error("Błąd podczas usuwania danych: {}", e.getMessage());
                    databaseMetrics.incrementDatabaseErrors(metricName, activeProfile);
                    databaseMetrics.incrementFailedQueries();
                } finally {
                    databaseMetrics.stopTimer(timer, metricName, activeProfile);
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
    }

    private void logPerformance(long totalDuration, double avgThreadTime, int totalSuccess, int totalErrors, int threads, double opsPerSecond) {
        File file = new File("performance-multithread-delete.csv");
        boolean writeHeader = !file.exists() || file.length() == 0;

        try (FileWriter writer = new FileWriter(file, true)) {
            if (writeHeader) {
                writer.write("Operacja,Czas całkowity[ms],Śr. czas wątku[ms],Wątki,Sukcesy,Błędy,Operacji/s,Profil,db.operations,db.errors,db.queries.failed,db.operation.time\n");
            }

            double operations = databaseMetrics.getOperationsCount(METRIC_NAME, activeProfile);
            double errors = databaseMetrics.getErrorsCount(METRIC_NAME, activeProfile);
            double failedQueries = databaseMetrics.getFailedQueriesCount();
            double totalTime = databaseMetrics.getTotalOperationTimeMillis(METRIC_NAME, activeProfile);

            writer.write(String.format("Wielowątkowy DELETE,%d,%.0f,%d,%d,%d,%.2f,%s,%.0f,%.0f,%.0f,%.0f\n",
                    totalDuration, avgThreadTime, threads, totalSuccess, totalErrors, opsPerSecond,
                    activeProfile, operations, errors, failedQueries, totalTime));
        } catch (IOException e) {
            log.error("Błąd zapisu wyników wielowątkowego DELETE do CSV", e);
        }
    }


}
