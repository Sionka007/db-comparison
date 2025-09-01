package com.benchmarking.dbcomparison.benchmark.isolation;

import com.benchmarking.dbcomparison.config.DatabaseMetrics;
import com.benchmarking.dbcomparison.model.Customer;
import com.benchmarking.dbcomparison.repository.CustomerRepository;
import com.benchmarking.dbcomparison.util.DataGenerator;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class AcidTest {

    private static final String CSV_FILE = "acid-test-results.csv";
    private static final int TIMEOUT_SECONDS = 10;

    private final DataGenerator dataGenerator = new DataGenerator();

    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;

    @Autowired private CustomerRepository customerRepository;
    @Autowired private DatabaseMetrics databaseMetrics;
    @Autowired private PlatformTransactionManager txManager;

    private void writeCsv(String testName, long durMs, String property, boolean violated, String desc) {
        File csv = new File(CSV_FILE);
        boolean header = !csv.exists() || csv.length() == 0;
        double p95 = databaseMetrics.getP95Millis(testName, activeProfile);
        try (FileWriter w = new FileWriter(csv, true)) {
            if (header) {
                w.write("Test;Duration[ms];PropertyTested;ViolationDetected;ResultDescription;Profile;p95_ms;DB_operations;DB_failed_queries;DB_timer_ms\n");
            }
            String line = String.join(";",
                    testName,
                    String.valueOf(durMs),
                    property,
                    String.valueOf(violated),
                    "\"" + (desc == null ? "" : desc.replace("\"", "'")) + "\"",
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

    public void testAtomicity() {
        final String test = "atomicity_test";
        Timer.Sample t = databaseMetrics.startTimer();
        long startNs = System.nanoTime();
        boolean violated;
        String desc;

        customerRepository.deleteAll();
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);

        try {
            tx.execute(new TransactionCallbackWithoutResult() {
                @Override protected void doInTransactionWithoutResult(TransactionStatus status) {
                    Customer a = dataGenerator.generateCustomer();
                    Customer b = dataGenerator.generateCustomer();
                    b.setEmail(null); // wymuszenie błędu
                    customerRepository.save(a);
                    customerRepository.save(b); // powinien zrzucić wyjątek i cofnąć całą transakcję
                }
            });
            violated = true; // jeśli tu doszliśmy bez wyjątku — naruszenie
            desc = "No exception thrown";
        } catch (Exception e) {
            // po wyjątku transakcja powinna zostać zrolowana; weryfikacja stanu:
            violated = customerRepository.count() != 0;
            desc = "Exception: " + e.getMessage() + ", remaining=" + customerRepository.count();
        } finally {
            databaseMetrics.stopTimer(t, test, activeProfile);
        }

        long durMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
        databaseMetrics.recordTransactionTime(durMs);
        databaseMetrics.incrementDatabaseOperations(test, activeProfile);
        writeCsv(test, durMs, "Atomicity", violated, desc);
    }

    public void testConsistency() throws InterruptedException {
        final String test = "consistency_test";
        Timer.Sample t = databaseMetrics.startTimer();
        long startNs = System.nanoTime();

        // flags bez "effectively final" problemu
        AtomicBoolean violated = new AtomicBoolean(false);
        AtomicReference<String> desc = new AtomicReference<>("");

        customerRepository.deleteAll();
        Customer base = dataGenerator.generateCustomer();
        customerRepository.save(base);

        CountDownLatch latch = new CountDownLatch(1);
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            pool.submit(() -> {
                try {
                    Customer dup = dataGenerator.generateCustomer();
                    dup.setEmail(base.getEmail()); // zakładamy UNIQUE(email)
                    customerRepository.save(dup);
                    violated.set(true);
                    desc.set("Duplicate email inserted");
                } catch (Exception e) {
                    desc.set("Constraint violation: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });

            boolean finished = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                desc.set("Timeout waiting for duplicate insert task");
            }
        } finally {
            pool.shutdownNow();
            databaseMetrics.stopTimer(t, test, activeProfile);
        }

        long durMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
        databaseMetrics.recordTransactionTime(durMs);
        databaseMetrics.incrementDatabaseOperations(test, activeProfile);
        writeCsv(test, durMs, "Consistency", violated.get(), desc.get());
    }


    public void testDurability() {
        final String test = "durability_test";
        Timer.Sample t = databaseMetrics.startTimer();
        long startNs = System.nanoTime();
        boolean violated = false;
        String desc = "";

        Customer c = dataGenerator.generateCustomer();
        customerRepository.save(c);
        UUID id = c.getId();
        String email = c.getEmail();

        try {
            Thread.sleep(500); // „czekamy na fsync”
        } catch (InterruptedException ignored) {}

        Customer re = customerRepository.findById(id).orElse(null);
        if (re == null || !email.equals(re.getEmail())) {
            violated = true;
            desc = "Data not persisted";
        }

        databaseMetrics.stopTimer(t, test, activeProfile);
        long durMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
        databaseMetrics.recordTransactionTime(durMs);
        databaseMetrics.incrementDatabaseOperations(test, activeProfile);
        writeCsv(test, durMs, "Durability", violated, desc);
    }

    /** Non-repeatable read w READ_COMMITTED. */
    public void testNonRepeatableRead() throws InterruptedException {
        final String test = "non_repeatable_read_test";
        Timer.Sample t = databaseMetrics.startTimer();
        long startNs = System.nanoTime();
        boolean detected;
        String desc;

        customerRepository.deleteAll();
        Customer c = dataGenerator.generateCustomer();
        customerRepository.save(c);
        String updatedName = "UpdatedName";

        CountDownLatch writeDone = new CountDownLatch(1);

        TransactionTemplate tx1 = new TransactionTemplate(txManager);
        tx1.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);

        final String[] firstRead = new String[1];
        final String[] secondRead = new String[1];

        tx1.execute(status -> {
            Customer r1 = customerRepository.findById(c.getId()).orElse(null);
            firstRead[0] = r1 != null ? r1.getFirstName() : null;

            // równoległa zmiana
            new Thread(() -> {
                TransactionTemplate tx2 = new TransactionTemplate(txManager);
                tx2.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
                tx2.execute(s2 -> {
                    Customer r2 = customerRepository.findById(c.getId()).orElse(null);
                    if (r2 != null) {
                        r2.setFirstName(updatedName);
                        customerRepository.save(r2);
                    }
                    writeDone.countDown();
                    return null;
                });
            }).start();

            try { writeDone.await(TIMEOUT_SECONDS, TimeUnit.SECONDS); } catch (InterruptedException ignored) {}
            Customer r3 = customerRepository.findById(c.getId()).orElse(null);
            secondRead[0] = r3 != null ? r3.getFirstName() : null;
            return null;
        });

        detected = firstRead[0] != null && secondRead[0] != null && !firstRead[0].equals(secondRead[0]);
        desc = "First=" + firstRead[0] + ", Second=" + secondRead[0];

        databaseMetrics.stopTimer(t, test, activeProfile);
        long durMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
        databaseMetrics.recordTransactionTime(durMs);
        databaseMetrics.incrementDatabaseOperations(test, activeProfile);
        writeCsv(test, durMs, "NonRepeatableRead", detected, desc);
    }

    /** Lost update: dwie transakcje RC nadpisują się (last-writer-wins). */
    public void testLostUpdate() throws InterruptedException {
        final String test = "lost_update_test";
        Timer.Sample t = databaseMetrics.startTimer();
        long startNs = System.nanoTime();
        boolean lost;
        String desc;

        customerRepository.deleteAll();
        Customer c = dataGenerator.generateCustomer();
        c.setFirstName("X");
        customerRepository.save(c);

        CountDownLatch bothDone = new CountDownLatch(2);

        Runnable txA = () -> {
            TransactionTemplate tx = new TransactionTemplate(txManager);
            tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
            tx.execute(s -> {
                Customer r = customerRepository.findById(c.getId()).orElse(null);
                if (r != null) {
                    // read-modify-write bez blokad
                    sleep(300);
                    r.setFirstName("A");
                    customerRepository.save(r);
                }
                bothDone.countDown();
                return null;
            });
        };

        Runnable txB = () -> {
            TransactionTemplate tx = new TransactionTemplate(txManager);
            tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
            tx.execute(s -> {
                Customer r = customerRepository.findById(c.getId()).orElse(null);
                if (r != null) {
                    sleep(300);
                    r.setFirstName("B");
                    customerRepository.save(r);
                }
                bothDone.countDown();
                return null;
            });
        };

        new Thread(txA).start();
        new Thread(txB).start();
        bothDone.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        Customer after = customerRepository.findById(c.getId()).orElse(null);
        String finalName = after != null ? after.getFirstName() : null;

        // Jeśli oba zapisy doszły do skutku, a finalny stan to "A" LUB "B", mamy lost update (nadpisanie).
        lost = "A".equals(finalName) || "B".equals(finalName);
        desc = "FinalName=" + finalName;

        databaseMetrics.stopTimer(t, test, activeProfile);
        long durMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
        databaseMetrics.recordTransactionTime(durMs);
        databaseMetrics.incrementDatabaseOperations(test, activeProfile);
        writeCsv(test, durMs, "LostUpdate", lost, desc);
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    public void runAllTests() throws InterruptedException {
        log.info("Rozpoczynam testy ACID...");
        testAtomicity();
        testConsistency();
        testDurability();
        testNonRepeatableRead();
        testLostUpdate();
        log.info("Testy ACID zakończone.");
    }
}
