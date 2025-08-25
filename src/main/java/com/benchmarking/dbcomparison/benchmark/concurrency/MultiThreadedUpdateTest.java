package com.benchmarking.dbcomparison.benchmark.concurrency;

import com.benchmarking.dbcomparison.config.BenchmarkConfig;
import com.benchmarking.dbcomparison.config.DatabaseMetrics;
import com.benchmarking.dbcomparison.model.Customer;
import com.benchmarking.dbcomparison.repository.CustomerRepository;
import com.benchmarking.dbcomparison.util.DataGenerator;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class MultiThreadedUpdateTest {

    private static final String METRIC_NAME = "customer_multithreaded_update";
    private final DataGenerator dataGenerator;
    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private DatabaseMetrics databaseMetrics;
    @Autowired
    private BenchmarkConfig benchmarkConfig;

    public MultiThreadedUpdateTest() {
        this.dataGenerator = new DataGenerator();
    }


    void testMultiThreadedUpdate() throws InterruptedException {
        int threadCount = benchmarkConfig.getThreads();
        int totalRecords = benchmarkConfig.getRecordCount();
        log.info("Rozpoczynam test wielowątkowej AKTUALIZACJI ({} wątków)", threadCount);

        List<Customer> allCustomers = customerRepository.findAll();
        if (allCustomers.size() < totalRecords) {
            log.warn("Brak wystarczającej liczby rekordów do testu, znaleziono: {}", allCustomers.size());
            return;
        }

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCounter = new AtomicInteger();
        AtomicInteger errorCounter = new AtomicInteger();

        List<Long> threadDurations = new CopyOnWriteArrayList<>();

        long startTime = System.nanoTime();
        int recordsPerThread = totalRecords / threadCount;

        for (int i = 0; i < threadCount; i++) {
            int startIdx = i * recordsPerThread;
            int endIdx = startIdx + recordsPerThread;
            List<Customer> customersSlice = allCustomers.subList(startIdx, endIdx);

            executor.submit(() -> {
                Timer.Sample timer = databaseMetrics.startTimer();
                long threadStart = System.nanoTime();
                try {
                    customersSlice.forEach(c -> c.setEmail("updated+" + c.getId() + "@mail.com"));
                    customerRepository.saveAll(customersSlice);

                    databaseMetrics.incrementDatabaseOperations(METRIC_NAME, activeProfile);
                    databaseMetrics.recordDataSize(METRIC_NAME, activeProfile, customersSlice.size());

                    successCounter.addAndGet(customersSlice.size());
                } catch (Exception e) {
                    log.error("Błąd w wątku UPDATE", e);
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

        log.info("Zakończono test wielowątkowy UPDATE: {} rekordów w {} ms", successCounter.get(), totalDuration);
        logPerformance(totalDuration, avgThreadTime, successCounter.get(), errorCounter.get(), threadDurations.size(), opsPerSecond);
    }

    private void logPerformance(long totalDuration, double avgThreadTime, int totalSuccess, int totalErrors, int threads, double opsPerSecond) {
        File file = new File("performance-multithread-update.csv");
        boolean writeHeader = !file.exists() || file.length() == 0;

        try (FileWriter writer = new FileWriter(file, true)) {
            if (writeHeader) {
                writer.write("Operacja,Czas całkowity[ms],Śr. czas wątku[ms],Wątki,Sukcesy,Błędy,Operacji/s,Profil,db.operations,db.errors,db.queries.failed,db.operation.time\n");
            }

            double operations = databaseMetrics.getOperationsCount(METRIC_NAME, activeProfile);
            double errors = databaseMetrics.getErrorsCount(METRIC_NAME, activeProfile);
            double failedQueries = databaseMetrics.getFailedQueriesCount();
            double totalTime = databaseMetrics.getTotalOperationTimeMillis(METRIC_NAME, activeProfile);

            writer.write(String.format("Wielowątkowy UPDATE,%d,%.0f,%d,%d,%d,%.2f,%s,%.0f,%.0f,%.0f,%.0f\n",
                    totalDuration, avgThreadTime, threads, totalSuccess, totalErrors, opsPerSecond,
                    activeProfile, operations, errors, failedQueries, totalTime));
        } catch (IOException e) {
            log.error("Błąd zapisu wyników wielowątkowego UPDATE do CSV", e);
        }
    }

    //runAllTests method to run the test
    public void runAllTests() {
        try {
            testMultiThreadedUpdate();
        } catch (InterruptedException e) {
            log.error("Błąd podczas wykonywania testu wielowątkowego UPDATE", e);
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
    }

}
