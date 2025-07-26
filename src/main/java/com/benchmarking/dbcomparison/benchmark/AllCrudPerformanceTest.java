package com.benchmarking.dbcomparison.benchmark;

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
public class AllCrudPerformanceTest {

    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;

    private String timestamp;


    @Autowired
    private InsertPerformanceTest insertTests;

    @Autowired
    private ReadPerformanceTest readTests;

    @Autowired
    private UpdatePerformanceTest updateTests;

    @Autowired
    private DeletePerformanceTest deleteTests;


    void setUp() {
        timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        log.info("Start testów wydajnościowych CRUD dla profilu: {}", activeProfile);
        createBackupFiles();
    }


    void runInsertTests() {
        log.info("Uruchamiam testy INSERT...");
        insertTests.cleanDatabaseAfterAll();
        insertTests.setUp();
        insertTests.testCustomerInsertPerformance();
        insertTests.testBrandAndCategoryInsertPerformance();
        insertTests.testProductInsertPerformance();
        insertTests.testOrderInsertPerformance();
        insertTests.testProductReviewInsertPerformance();
        insertTests.testInventoryMovementInsertPerformance();
        log.info("Testy INSERT zakończone");
    }


    void runReadTests() {
        log.info("Uruchamiam testy READ...");
        readTests.testReadAllCustomers();
        readTests.testReadAllProducts();
        readTests.testReadAllOrdersWithJoins();
        readTests.testReadTopRatedProducts();
        log.info("Testy READ zakończone");
    }


    void runUpdateTests() {
        log.info("Uruchamiam testy UPDATE...");
        updateTests.testUpdateProductPrices();
        updateTests.testUpdateCustomerEmails();
        updateTests.testUpdateOrderStatus();
        log.info("Testy UPDATE zakończone");
    }


    void runDeleteTests() {
        log.info("Uruchamiam testy DELETE...");
        deleteTests.testDeleteOrderItems();
        deleteTests.testDeleteOrders();
        deleteTests.testDeleteProducts();
        deleteTests.testDeleteCustomers();
        log.info("Testy DELETE zakończone");
    }


    void tearDown() {
        log.info("Wszystkie testy CRUD zakończone dla profilu: {}", activeProfile);
        archiveResultFiles();
    }

    private void createBackupFiles() {
        try {
            backupIfExists("performance-insert-results.csv");
            backupIfExists("performance-read-results.csv");
            backupIfExists("performance-update-results.csv");
            backupIfExists("performance-delete-results.csv");
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
            String resultDir = String.format("results_%s_%s", activeProfile, timestamp);
            new File(resultDir).mkdirs();

            moveIfExists("performance-insert-results.csv", resultDir);
            moveIfExists("performance-read-results.csv", resultDir);
            moveIfExists("performance-update-results.csv", resultDir);
            moveIfExists("performance-delete-results.csv", resultDir);

            log.info("Wyniki testów zostały zarchiwizowane w katalogu: {}", resultDir);
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

    //run the tests in sequence
    public void runAllTests() {
        setUp();
        runInsertTests();
        runReadTests();
        runUpdateTests();
        runDeleteTests();
        tearDown();
        log.info("Wszystkie testy CRUD zakończone pomyślnie dla profilu: {}", activeProfile);
        log.info("Wyniki testów zapisane w plikach CSV i zarchiwizowane.");
        archiveResultFiles();
        log.info("Archiwizacja wyników zakończona pomyślnie.");
        log.info("Testy wydajnościowe CRUD zakończone pomyślnie dla profilu: {}", activeProfile);
        log.info("Czas wykonania testów: {}", timestamp);
        log.info("Wszystkie testy CRUD zakończone pomyślnie dla profilu: {}", activeProfile);

    }
}
