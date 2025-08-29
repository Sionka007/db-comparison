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

    @Autowired private InsertPerformanceTest insertTests;
    @Autowired private ReadPerformanceTest readTests;
    @Autowired private UpdatePerformanceTest updateTests;
    @Autowired private DeletePerformanceTest deleteTests;

    private void backupIfExists(String filename) {
        try {
            File f = new File(filename);
            if (f.exists()) {
                String ts = timestamp != null ? timestamp : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String backup = filename.replace(".csv", "_" + ts + ".csv");
                Files.copy(f.toPath(), Path.of(backup));
                f.delete();
            }
        } catch (Exception e) {
            log.warn("Backup failed for {}", filename, e);
        }
    }

    private void createBackups() {
        backupIfExists("performance-insert-results.csv");
        backupIfExists("performance-read-results.csv");
        backupIfExists("performance-update-results.csv");
        backupIfExists("performance-delete-results.csv");
    }

    private void archiveAll() {
        try {
            String dir = "results_" + activeProfile + "_" + timestamp;
            new File(dir).mkdirs();
            for (String name : new String[]{
                    "performance-insert-results.csv",
                    "performance-read-results.csv",
                    "performance-update-results.csv",
                    "performance-delete-results.csv"
            }) {
                File f = new File(name);
                if (f.exists()) {
                    Files.move(f.toPath(), Path.of(dir, name));
                }
            }
            log.info("Results archived in {}", dir);
        } catch (Exception e) {
            log.error("Archiving error", e);
        }
    }

    public void runAllTests() {
        timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        log.info("CRUD perf tests start, profile={}", activeProfile);
        createBackups();

        // INSERT przygotowuje dane do późniejszych testów
        insertTests.setUp();
        insertTests.testCustomerInsertPerformance();
        insertTests.testBrandAndCategoryInsertPerformance();
        insertTests.testProductInsertPerformance();
        insertTests.testOrderInsertPerformance();
        insertTests.testProductReviewInsertPerformance();
        insertTests.testInventoryMovementInsertPerformance();

        // READ/UPDATE/DELETE na już istniejących danych
        readTests.runAll();
        updateTests.runAll();
        deleteTests.runAll();

        archiveAll();
        log.info("CRUD perf tests finished, profile={}", activeProfile);
    }
}
