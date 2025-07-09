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

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@SpringBootTest
public class MultiThreadedUpdateTest {

    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private DatabaseMetrics databaseMetrics;

    private static DataGenerator dataGenerator;
    private static final int TOTAL_RECORDS = 1000;
    private static final int THREAD_COUNT = 10;
    private static final String METRIC_NAME = "customer_multithreaded_update";

    @BeforeAll
    static void setupGenerator() {
        dataGenerator = new DataGenerator();
    }

    @Test
    void testMultiThreadedUpdate() throws InterruptedException {
        log.info("Rozpoczynam test wielowątkowej AKTUALIZACJI ({} wątków)", THREAD_COUNT);

        // Wczytaj wszystkich klientów - załóżmy, że mamy co najmniej TOTAL_RECORDS
        List<Customer> allCustomers = customerRepository.findAll();
        if (allCustomers.size() < TOTAL_RECORDS) {
            log.warn("Brak wystarczającej liczby rekordów do testu, znaleziono: {}", allCustomers.size());
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
                    // Aktualizuj emaile klientów w tej części listy
                    customersSlice.forEach(c -> c.setEmail("updated+" + c.getId() + "@mail.com"));

                    // Zapisz zmiany
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

        log.info("Zakończono test wielowątkowy UPDATE: {} rekordów w {} ms", successCounter.get(), totalDuration);

        logPerformance(totalDuration, avgThreadTime, successCounter.get(), errorCounter.get(), threadDurations.size());
    }

    private void logPerformance(long totalDuration, double avgThreadTime, int totalSuccess, int totalErrors, int threads) {
        try (FileWriter writer = new FileWriter("performance-multithread-update.csv", true)) {
            // Dopisz nagłówek jeśli plik jest nowy
            writer.write("Operacja,Czas całkowity[ms],Śr. czas wątku[ms],Wątki,Sukcesy,Błędy,Profil\n");
            writer.write(String.format("Wielowątkowy UPDATE,%d,%.2f,%d,%d,%d,%s\n",
                    totalDuration, avgThreadTime, threads, totalSuccess, totalErrors, activeProfile));
        } catch (IOException e) {
            log.error("Błąd zapisu wyników wielowątkowego UPDATE do CSV", e);
        }
    }
}
