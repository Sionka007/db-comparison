package com.benchmarking.dbcomparison.benchmark.concurrency;

import com.benchmarking.dbcomparison.config.DatabaseMetrics;
import com.benchmarking.dbcomparison.model.Customer;
import com.benchmarking.dbcomparison.repository.CustomerRepository;
import com.benchmarking.dbcomparison.util.DataGenerator;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@SpringBootTest
public class MultiThreadedDeleteTest {

    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private DatabaseMetrics databaseMetrics;

    private static final int TOTAL_RECORDS = 1000;
    private static final int THREAD_COUNT = 10;
    private static final String METRIC_NAME = "customer_multithreaded_delete";

    @BeforeAll
    static void setupGenerator() {
        new DataGenerator(); // jeśli generator będzie później używany
    }

    @Test
    void testMultiThreadedDelete() throws InterruptedException {
        log.info("Rozpoczynam test wielowątkowego DELETE ({} wątków)", THREAD_COUNT);

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

        log.info("Zakończono test DELETE: {} rekordów w {} ms", successCounter.get(), totalDuration);

        logPerformance(totalDuration, avgThreadTime, successCounter.get(), errorCounter.get(), threadDurations.size());
    }

    private void logPerformance(long totalDuration, double avgThreadTime, int totalSuccess, int totalErrors, int threads) {
        File file = new File("performance-multithread-delete.csv");
        boolean newFile = !file.exists();

        try (FileWriter writer = new FileWriter(file, true)) {
            if (newFile) {
                writer.write("Operacja,Czas całkowity[ms],Śr. czas wątku[ms],Wątki,Sukcesy,Błędy,Profil\n");
            }
            writer.write(String.format(
                    "Wielowątkowy DELETE,%d,%d,%d,%d,%d,%s\n",
                    totalDuration, (int) avgThreadTime, threads, totalSuccess, totalErrors, activeProfile));
        } catch (IOException e) {
            log.error("Błąd zapisu wyników DELETE do CSV", e);
        }
    }
}
