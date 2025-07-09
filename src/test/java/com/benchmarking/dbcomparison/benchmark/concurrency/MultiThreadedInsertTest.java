package com.benchmarking.dbcomparison.benchmark.concurrency;

import com.benchmarking.dbcomparison.config.DatabaseMetrics;
import com.benchmarking.dbcomparison.model.*;
import com.benchmarking.dbcomparison.repository.CustomerRepository;
import com.benchmarking.dbcomparison.util.DataGenerator;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@SpringBootTest
public class MultiThreadedInsertTest {

    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;

    @Autowired private CustomerRepository customerRepository;
    @Autowired private DatabaseMetrics databaseMetrics;

    private static DataGenerator dataGenerator;
    private static final int TOTAL_RECORDS = 1000;
    private static final int THREAD_COUNT = 10;
    private static final String METRIC_NAME = "customer_multithreaded_insert";

    @BeforeAll
    static void setupGenerator() {
        dataGenerator = new DataGenerator();
    }

    @Test
    void testMultiThreadedInsert() throws InterruptedException {
        log.info("Rozpoczynam test wielowątkowego INSERT ({} wątków)", THREAD_COUNT);

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        AtomicInteger successCounter = new AtomicInteger();
        AtomicInteger errorCounter = new AtomicInteger();

        List<Long> threadDurations = new ArrayList<>();

        long startTime = System.nanoTime();

        int recordsPerThread = TOTAL_RECORDS / THREAD_COUNT;

        for (int i = 0; i < THREAD_COUNT; i++) {
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

        log.info("Zakończono test wielowątkowy: {} rekordów w {} ms", successCounter.get(), totalDuration);
        logPerformance(totalDuration, avgTime, successCounter.get(), errorCounter.get(), threadDurations.size());
    }

    private void logPerformance(long totalDuration, double avgThreadTime, int totalSuccess, int totalErrors, int threads) {
        try (FileWriter writer = new FileWriter("performance-multithread-insert.csv", true)) {
            writer.write("Operacja,Czas całkowity[ms],Śr. czas wątku[ms],Wątki,Sukcesy,Błędy,Profil\n");
            writer.write(String.format("Wielowątkowy INSERT,%d,%.2f,%d,%d,%d,%s\n",
                    totalDuration, avgThreadTime, threads, totalSuccess, totalErrors, activeProfile));
        } catch (IOException e) {
            log.error("Błąd zapisu wyników wielowątkowych do CSV", e);
        }
    }
}
