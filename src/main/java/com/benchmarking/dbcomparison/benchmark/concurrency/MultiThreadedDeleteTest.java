package com.benchmarking.dbcomparison.benchmark.concurrency;

import com.benchmarking.dbcomparison.config.BenchmarkConfig;
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

    private static final String METRIC_NAME = "customer_multithreaded_delete";

    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;

    @Autowired private CustomerRepository customerRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private ProductReviewRepository productReviewRepository;
    @Autowired private DatabaseMetrics databaseMetrics;
    @Autowired private BenchmarkConfig benchmarkConfig;

    static void setupGenerator() {
        new DataGenerator(); // opcjonalnie, jeśli potrzebne
    }

    //runAllTests
    public void runAllTests() throws InterruptedException {
        int configuredThreads = Math.max(1, benchmarkConfig.getThreads());
        int totalRecords = Math.max(1, benchmarkConfig.getRecordCount());

        // Najpierw usuwamy powiązane rekordy
        deleteRelatedRecords(Math.min(configuredThreads, totalRecords));

        List<Customer> allCustomers = customerRepository.findAll();
        if (allCustomers.size() < totalRecords) {
            log.warn("Brak wystarczającej liczby rekordów do testu DELETE, znaleziono: {}", allCustomers.size());
            return;
        }

        int threadCount = Math.min(configuredThreads, totalRecords);
        log.info("Rozpoczynam test wielowątkowego DELETE ({} wątków)", threadCount);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        java.util.List<Long> threadDurations = new CopyOnWriteArrayList<>();

        long startTime = System.nanoTime();
        int base = totalRecords / threadCount;
        int remainder = totalRecords % threadCount;

        final AtomicInteger successCounter = new AtomicInteger();
        final AtomicInteger errorCounter = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            int startIdx = i * base + Math.min(i, remainder);
            int endIdx = startIdx + base + (i < remainder ? 1 : 0);
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
        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
            log.warn("DELETE executor timeout – forcing shutdownNow()");
            executor.shutdownNow();
        }

        long totalDuration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        double avgThreadTime = threadDurations.stream().mapToLong(Long::longValue).average().orElse(0);
        double opsPerSecond = totalDuration > 0 ? successCounter.get() / (totalDuration / 1000.0) : 0;

        log.info("Zakończono test DELETE: {} rekordów w {} ms", successCounter.get(), totalDuration);
        logPerformance(totalDuration, avgThreadTime, successCounter.get(), errorCounter.get(), threadDurations.size(), opsPerSecond);
    }

    private void deleteRelatedRecords(int threadCount) throws InterruptedException {
        // Usuwanie elementów zamówień
        List<OrderItem> orderItems = orderItemRepository.findAll();
        deleteInBatches(orderItems, orderItemRepository, "orderitem_delete", threadCount);

        // Usuwanie recenzji produktów
        List<ProductReview> reviews = productReviewRepository.findAll();
        deleteInBatches(reviews, productReviewRepository, "productreview_delete", threadCount);

        // Usuwanie zamówień
        List<Order> orders = orderRepository.findAll();
        deleteInBatches(orders, orderRepository, "order_delete", threadCount);
    }

    private <T> void deleteInBatches(List<T> entities, JpaRepository<T, ?> repository, String metricName, int threadCount) throws InterruptedException {
        if (entities.isEmpty()) return;

        int actualThreads = Math.min(threadCount, entities.size());
        ExecutorService executor = Executors.newFixedThreadPool(actualThreads);
        CountDownLatch latch = new CountDownLatch(actualThreads);
        int base = entities.size() / actualThreads;
        int remainder = entities.size() % actualThreads;

        for (int i = 0; i < actualThreads; i++) {
            int startIdx = i * base + Math.min(i, remainder);
            int endIdx = startIdx + base + (i < remainder ? 1 : 0);

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
        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
            log.warn("DELETE-batches executor timeout – forcing shutdownNow()");
            executor.shutdownNow();
        }
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
