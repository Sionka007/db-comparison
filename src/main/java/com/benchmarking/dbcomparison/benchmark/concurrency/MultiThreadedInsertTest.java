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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class MultiThreadedInsertTest {

    private static final String METRIC_NAME = "customer_multithreaded_insert";
    private final DataGenerator dataGenerator;
    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private DatabaseMetrics databaseMetrics;
    @Autowired
    private BenchmarkConfig benchmarkConfig;

    public MultiThreadedInsertTest() {
        this.dataGenerator = new DataGenerator();
    }


    void testMultiThreadedInsert() throws InterruptedException {
        int threadCount = benchmarkConfig.getThreads();
        int totalRecords = benchmarkConfig.getRecordCount();
        log.info("Rozpoczynam test wielowątkowego INSERT ({} wątków)", threadCount);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCounter = new AtomicInteger();
        AtomicInteger errorCounter = new AtomicInteger();

        List<Long> threadDurations = new ArrayList<>();

        long startTime = System.nanoTime();
        int recordsPerThread = totalRecords / threadCount;

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                Timer.Sample timer = databaseMetrics.startTimer();
                long threadStart = System.nanoTime();
                try {
                    List<Customer> customers = new ArrayList<>();
                    for (int j = 0; j < recordsPerThread; j++) {
                        customers.add(dataGenerator.generateCustomer());
                    }
                    customerRepository.saveAll(customers);
                    databaseMetrics.incrementDatabaseOperations(METRIC_NAME, activeProfile);
                    successCounter.addAndGet(customers.size());
                } catch (Exception e) {
                    log.error("Błąd w wątku INSERT", e);
                    databaseMetrics.incrementDatabaseErrors(METRIC_NAME, activeProfile);
                    errorCounter.incrementAndGet();
                    databaseMetrics.incrementFailedQueries();
                } finally {
                    long threadDuration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - threadStart);
                    synchronized (threadDurations) {
                        threadDurations.add(threadDuration);
                    }
                    databaseMetrics.stopTimer(timer, METRIC_NAME, activeProfile);
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        long totalDuration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        double avgTime = threadDurations.stream().mapToLong(Long::longValue).average().orElse(0);
        double opsPerSecond = totalDuration > 0 ? successCounter.get() / (totalDuration / 1000.0) : 0;

        log.info("Zakończono test wielowątkowy: {} rekordów w {} ms", successCounter.get(), totalDuration);
        logPerformance(totalDuration, avgTime, successCounter.get(), errorCounter.get(), threadDurations.size(), opsPerSecond);
    }

    private void logPerformance(long totalDuration, double avgThreadTime, int totalSuccess, int totalErrors, int threads, double opsPerSecond) {
        File csvFile = new File("performance-multithread-insert.csv");
        boolean writeHeader = !csvFile.exists() || csvFile.length() == 0;

        try (FileWriter writer = new FileWriter(csvFile, true)) {
            if (writeHeader) {
                writer.write("Operacja,Czas całkowity[ms],Śr. czas wątku[ms],Wątki,Sukcesy,Błędy,Operacji/s,Profil,db.operations,db.errors,db.queries.failed,db.operation.time\n");
            }

            double operations = databaseMetrics.getOperationsCount(METRIC_NAME, activeProfile);
            double errors = databaseMetrics.getErrorsCount(METRIC_NAME, activeProfile);
            double failedQueries = databaseMetrics.getFailedQueriesCount();
            double totalTime = databaseMetrics.getTotalOperationTimeMillis(METRIC_NAME, activeProfile);

            writer.write(String.format("Wielowątkowy INSERT,%d,%.0f,%d,%d,%d,%.2f,%s,%.0f,%.0f,%.0f,%.0f\n",
                    totalDuration, avgThreadTime, threads, totalSuccess, totalErrors, opsPerSecond,
                    activeProfile, operations, errors, failedQueries, totalTime));
        } catch (IOException e) {
            log.error("Błąd zapisu wyników wielowątkowych do CSV", e);
        }
    }

    //runAllTests
    public void runAllTests() throws InterruptedException {
        log.info("Rozpoczynam testy wielowątkowe INSERT");
        testMultiThreadedInsert();
        log.info("Zakończono testy wielowątkowe INSERT");
    }
}
