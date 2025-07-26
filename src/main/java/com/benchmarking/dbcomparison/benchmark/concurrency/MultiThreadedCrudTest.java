package com.benchmarking.dbcomparison.benchmark.concurrency;

import com.benchmarking.dbcomparison.benchmark.InsertPerformanceTest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class MultiThreadedCrudTest {

    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;

    @Autowired
    private MultiThreadedInsertTest insertTest;
    @Autowired
    private MultiThreadedReadTest readTest;
    @Autowired
    private MultiThreadedUpdateTest updateTest;
    @Autowired
    private MultiThreadedDeleteTest deleteTest;
    @Autowired
    private InsertPerformanceTest insertPerformanceTest;

    private String timestamp;


    void setUp() {
        timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        log.info("Start testów wielowątkowych CRUD dla profilu: {}", activeProfile);

        // Przygotowanie danych testowych
        log.info("Przygotowywanie danych testowych...");
        insertPerformanceTest.cleanDatabaseAfterAll();
        insertPerformanceTest.setUp();
        insertPerformanceTest.testCustomerInsertPerformance();
        insertPerformanceTest.testBrandAndCategoryInsertPerformance();
        insertPerformanceTest.testProductInsertPerformance();
        insertPerformanceTest.testOrderInsertPerformance();
        insertPerformanceTest.testProductReviewInsertPerformance();
        insertPerformanceTest.testInventoryMovementInsertPerformance();
        log.info("Dane testowe zostały przygotowane");

        createBackupFiles();
    }

    void testMultiThreadedInsert() throws InterruptedException {
        log.info("Uruchamiam wielowątkowy test INSERT...");
        insertTest.testMultiThreadedInsert();
    }


    void testMultiThreadedRead() throws InterruptedException {
        log.info("Uruchamiam wielowątkowy test READ...");
        readTest.testMultiThreadedRead();
    }


    void testMultiThreadedUpdate() throws InterruptedException {
        log.info("Uruchamiam wielowątkowy test UPDATE...");
        updateTest.testMultiThreadedUpdate();
    }

    void testMultiThreadedDelete() throws InterruptedException {
        log.info("Uruchamiam wielowątkowy test DELETE...");
        deleteTest.runAllTests();
    }

    void tearDown() {
        log.info("Wszystkie testy wielowątkowe CRUD zakończone dla profilu: {}", activeProfile);
        archiveResultFiles();
    }

    private void createBackupFiles() {
        try {
            backupIfExists("performance-multithread-insert.csv");
            backupIfExists("performance-multithread-read.csv");
            backupIfExists("performance-multithread-update.csv");
            backupIfExists("performance-multithread-delete.csv");
        } catch (Exception e) {
            log.error("Błąd podczas tworzenia kopii zapasowych plików CSV", e);
        }
    }

    private void backupIfExists(String filename) {
        try {
            File file = new File(filename);
            if (file.exists()) {
                String backupName = filename.replace(".csv", String.format("_%s.csv", timestamp));
                Files.copy(file.toPath(), Path.of(backupName));
                file.delete();
            }
        } catch (Exception e) {
            log.error("Błąd podczas tworzenia kopii zapasowej pliku: {}", filename, e);
        }
    }

    private void archiveResultFiles() {
        try {
            String resultDir = String.format("results_multithread_%s_%s", activeProfile, timestamp);
            new File(resultDir).mkdirs();

            moveIfExists("performance-multithread-insert.csv", resultDir);
            moveIfExists("performance-multithread-read.csv", resultDir);
            moveIfExists("performance-multithread-update.csv", resultDir);
            moveIfExists("performance-multithread-delete.csv", resultDir);

            log.info("Wyniki testów wielowątkowych zostały zarchiwizowane w katalogu: {}", resultDir);
        } catch (Exception e) {
            log.error("Błąd podczas archiwizacji wyników", e);
        }
    }

    private void moveIfExists(String filename, String targetDir) {
        try {
            File file = new File(filename);
            if (file.exists()) {
                Files.move(file.toPath(), Path.of(targetDir, filename));
            }
        } catch (Exception e) {
            log.error("Błąd podczas przenoszenia pliku: {}", filename, e);
        }
    }

    //runAllTests
    public void runAllTests() throws InterruptedException {
        log.info("Rozpoczynam testy wielowątkowe CRUD");
        testMultiThreadedInsert();
        testMultiThreadedRead();
        testMultiThreadedUpdate();
        testMultiThreadedDelete();
        log.info("Zakończono testy wielowątkowe CRUD");
    }
}
