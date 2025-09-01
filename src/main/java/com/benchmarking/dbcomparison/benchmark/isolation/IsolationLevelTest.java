package com.benchmarking.dbcomparison.benchmark.isolation;

import com.benchmarking.dbcomparison.config.DatabaseMetrics;
import com.benchmarking.dbcomparison.util.DataGenerator;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class IsolationLevelTest {

    private static final String CSV_FILE = "isolation-test-results.csv";
    private static final int TIMEOUT_SECONDS = 10;
    private static final int SERIALIZABLE_MAX_RETRIES = 3;

    private final DataGenerator dataGenerator = new DataGenerator();

    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;

    @Autowired private DatabaseMetrics databaseMetrics;
    @Autowired private PlatformTransactionManager txManager;
    @Autowired private JdbcTemplate jdbc;

    private TransactionTemplate txReadCommitted;
    private TransactionTemplate txRepeatableRead;
    private TransactionTemplate txSerializable;

    private boolean isPostgres() { return activeProfile != null && activeProfile.toLowerCase().contains("postgres"); }
    private boolean isMySql()    { return activeProfile != null && activeProfile.toLowerCase().contains("mysql"); }

    private String likeContains(String col, String val) {
        // ILIKE tylko w PG
        return isMySql() ? col + " LIKE '%" + val + "%'" : col + " ILIKE '%" + val + "%'";
    }

    /** PG: gen_random_uuid(); MySQL (CHAR(36)): UUID() */
    private String idSqlForInsert() {
        return isPostgres() ? "gen_random_uuid()" : "UUID_TO_BIN(UUID(), true)";
    }

    /** Upewnij się, że w PG mamy funkcję do UUID (pgcrypto). */
    private void ensurePgUuidExtension() {
        if (isPostgres()) {
            try { jdbc.execute("CREATE EXTENSION IF NOT EXISTS pgcrypto"); } catch (Exception ignored) {}
        }
    }

    void setUp() {
        txReadCommitted = new TransactionTemplate(txManager);
        txReadCommitted.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);

        txRepeatableRead = new TransactionTemplate(txManager);
        txRepeatableRead.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);

        txSerializable = new TransactionTemplate(txManager);
        txSerializable.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);

        ensurePgUuidExtension();

        // proste dane bazowe
        jdbc.update("DELETE FROM customer");
        String idExpr = idSqlForInsert();
        for (int i = 0; i < 50; i++) {
            // nie używamy getFaker(); bierzemy dane z DataGeneratora
            var c = dataGenerator.generateCustomer();
            String first = c.getFirstName() != null ? c.getFirstName() : "Name" + i;
            String last  = (i % 2 == 0) ? "Zorro" : "Alpha";
            String email = c.getEmail() != null ? c.getEmail() : ("user"+i+"@example.com");

            jdbc.update(
                    "INSERT INTO customer (id, first_name, last_name, email) VALUES (" + idExpr + ", ?, ?, ?)",
                    first, last, email
            );
        }
    }

    /** PHANTOM READ pod predykatem (COUNT(*) WHERE last_name LIKE '%Z%') */
    public void testPhantomReadReadCommitted() throws InterruptedException {
        final String test = "phantom_read_read_committed";
        final String where = likeContains("last_name", "Z");

        Timer.Sample t = databaseMetrics.startTimer();
        long startNs = System.nanoTime();
        boolean phantom = false;

        ExecutorService pool = Executors.newSingleThreadExecutor();
        CountDownLatch inserted = new CountDownLatch(1);

        try {
            Integer before = txReadCommitted.execute(status ->
                    jdbc.queryForObject("SELECT COUNT(*) FROM customer WHERE " + where, Integer.class));

            // równoległy INSERT, który spełnia warunek (phantom)
            pool.submit(() -> {
                try {
                    txReadCommitted.execute(s2 -> {
                        jdbc.update("INSERT INTO customer (id, first_name, last_name, email) VALUES (" +
                                        idSqlForInsert() + ", ?, ?, ?)",
                                "Pha", "Zeta",
                                // prosta losowa poczta bez getFaker()
                                "pha-" + UUID.randomUUID() + "@example.com");
                        return null;
                    });
                } catch (Exception e) {
                    databaseMetrics.incrementFailedQueries();
                    log.warn("Insert thread error: {}", e.getMessage());
                } finally {
                    inserted.countDown();
                }
            });

            if (!inserted.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                databaseMetrics.incrementFailedQueries();
                log.warn("Insert timeout");
            }

            Integer after = txReadCommitted.execute(status ->
                    jdbc.queryForObject("SELECT COUNT(*) FROM customer WHERE " + where, Integer.class));

            phantom = !Objects.equals(before, after);
        } finally {
            pool.shutdownNow();
            databaseMetrics.stopTimer(t, test, activeProfile);
        }

        long durMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
        databaseMetrics.recordTransactionTime(durMs);
        databaseMetrics.incrementDatabaseOperations(test, activeProfile);
        writeCsv(test, "READ_COMMITTED", phantom, durMs);
    }

    public void testPhantomReadRepeatableRead() throws InterruptedException {
        final String test = "phantom_read_repeatable_read";
        final String where = likeContains("last_name", "Z");

        Timer.Sample t = databaseMetrics.startTimer();
        long startNs = System.nanoTime();
        boolean phantom = false;

        ExecutorService pool = Executors.newSingleThreadExecutor();
        CountDownLatch inserted = new CountDownLatch(1);

        try {
            Integer before = txRepeatableRead.execute(status ->
                    jdbc.queryForObject("SELECT COUNT(*) FROM customer WHERE " + where, Integer.class));

            pool.submit(() -> {
                try {
                    // INSERT w RC żeby był widoczny „z zewnątrz”
                    txReadCommitted.execute(s2 -> {
                        jdbc.update("INSERT INTO customer (id, first_name, last_name, email) VALUES (" +
                                        idSqlForInsert() + ", ?, ?, ?)",
                                "Pha", "Zeta",
                                "pha-" + UUID.randomUUID() + "@example.com");
                        return null;
                    });
                } catch (Exception e) {
                    databaseMetrics.incrementFailedQueries();
                    log.warn("Insert thread error: {}", e.getMessage());
                } finally {
                    inserted.countDown();
                }
            });

            if (!inserted.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                databaseMetrics.incrementFailedQueries();
                log.warn("Insert timeout");
            }

            Integer after = txRepeatableRead.execute(status ->
                    jdbc.queryForObject("SELECT COUNT(*) FROM customer WHERE " + where, Integer.class));

            // RR (snapshot) zwykle NIE zobaczy phantomu
            phantom = !Objects.equals(before, after);
        } finally {
            pool.shutdownNow();
            databaseMetrics.stopTimer(t, test, activeProfile);
        }

        long durMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
        databaseMetrics.recordTransactionTime(durMs);
        databaseMetrics.incrementDatabaseOperations(test, activeProfile);
        writeCsv(test, "REPEATABLE_READ", phantom, durMs);
    }

    public void testPhantomReadSerializable() {
        final String test = "phantom_read_serializable";
        final String where = likeContains("last_name", "Z");

        int attempts = 0;
        boolean phantom = false;
        long startNs = System.nanoTime();
        Timer.Sample t = databaseMetrics.startTimer();

        try {
            while (true) {
                attempts++;
                try {
                    Integer before = txSerializable.execute(s ->
                            jdbc.queryForObject("SELECT COUNT(*) FROM customer WHERE " + where, Integer.class));

                    // konkurencyjna transakcja (RC) wstawiająca rekord pasujący do predykatu
                    txReadCommitted.execute(s2 -> {
                        jdbc.update("INSERT INTO customer (id, first_name, last_name, email) VALUES (" +
                                        idSqlForInsert() + ", ?, ?, ?)",
                                "Pha", "Zeta",
                                "pha-" + UUID.randomUUID() + "@example.com");
                        return null;
                    });

                    Integer after = txSerializable.execute(s ->
                            jdbc.queryForObject("SELECT COUNT(*) FROM customer WHERE " + where, Integer.class));

                    // w SERIALIZABLE spodziewamy się braku phantomu (lub serialization failure → retry)
                    phantom = !Objects.equals(before, after);
                    break;
                } catch (ConcurrencyFailureException | TransactionSystemException ex) {
                    if (attempts >= SERIALIZABLE_MAX_RETRIES) {
                        databaseMetrics.incrementFailedQueries();
                        log.warn("Serializable retry exhausted: {}", ex.getMessage());
                        break;
                    }
                    log.info("Serializable retry ({}/{}): {}", attempts, SERIALIZABLE_MAX_RETRIES, ex.getMessage());
                    // retry
                }
            }
        } finally {
            databaseMetrics.stopTimer(t, test, activeProfile);
        }

        long durMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
        databaseMetrics.recordTransactionTime(durMs);
        databaseMetrics.incrementDatabaseOperations(test, activeProfile);
        writeCsv(test, "SERIALIZABLE", phantom, durMs);
    }

    private void writeCsv(String testName, String isolationLevel, boolean phantomDetected, long durMs) {
        File csv = new File(CSV_FILE);
        boolean header = !csv.exists() || csv.length() == 0;
        double p95 = databaseMetrics.getP95Millis(testName, activeProfile);

        try (FileWriter w = new FileWriter(csv, true)) {
            if (header) {
                w.write("Test;Duration[ms];IsolationLevel;PhantomDetected;Profile;p95_ms;DB_operations;DB_failed_queries;DB_timer_ms\n");
            }
            String line = String.join(";",
                    testName,
                    String.valueOf(durMs),
                    isolationLevel,
                    String.valueOf(phantomDetected),
                    activeProfile,
                    String.valueOf(Double.isNaN(p95) ? 0 : p95),
                    String.valueOf(databaseMetrics.getOperationsCount(testName, activeProfile)),
                    String.valueOf(databaseMetrics.getFailedQueriesCount()),
                    String.valueOf(databaseMetrics.getTotalOperationTimeMillis(testName, activeProfile))
            );
            w.write(line + "\n");
        } catch (IOException e) {
            log.error("CSV write error: {}", e.getMessage());
        }
    }

    public void runAllTests() throws InterruptedException {
        setUp();
        testPhantomReadReadCommitted();
        testPhantomReadRepeatableRead();
        testPhantomReadSerializable();
    }
}
