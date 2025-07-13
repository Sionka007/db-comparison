package com.benchmarking.dbcomparison.benchmark.isolation;

import com.benchmarking.dbcomparison.config.DatabaseMetrics;
import com.benchmarking.dbcomparison.model.Customer;
import com.benchmarking.dbcomparison.repository.CustomerRepository;
import com.benchmarking.dbcomparison.util.DataGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@SpringBootTest
public class AcidTest {

    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;

    @Autowired private CustomerRepository customerRepository;
    @Autowired private DatabaseMetrics databaseMetrics;
    @Autowired private PlatformTransactionManager transactionManager;
    private final DataGenerator dataGenerator = new DataGenerator();

    private static final String CSV_FILE = "acid-test-results.csv";
    private static final int TIMEOUT_SECONDS = 10;

    @Test
    void testAtomicity() throws InterruptedException {
        String testName = "atomicity_test";
        long startTime = System.nanoTime();
        boolean atomicityViolated = false;
        String resultDescription = "";

        customerRepository.deleteAll();
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        try {
            txTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    Customer customer1 = dataGenerator.generateCustomer();
                    Customer customer2 = dataGenerator.generateCustomer();
                    customer2.setEmail(null); // Wymuszamy błąd walidacji
                    customerRepository.save(customer1);
                    customerRepository.save(customer2);
                }
            });
        } catch (Exception e) {
            resultDescription = "Exception: " + e.getMessage();
        }
        // Sprawdź stan bazy PO rollbacku, już poza transakcją
        List<Customer> remainingCustomers = customerRepository.findAll();
        atomicityViolated = !remainingCustomers.isEmpty();
        resultDescription += ", Remaining records: " + remainingCustomers.size();

        long duration = System.nanoTime() - startTime;
        logResults(testName, duration / 1_000_000, "Atomicity", atomicityViolated, resultDescription);
    }

    @Test
    void testConsistency() throws InterruptedException {
        String testName = "consistency_test";
        long startTime = System.nanoTime();
        AtomicBoolean consistencyViolated = new AtomicBoolean(false);
        AtomicReference<String> resultDescription = new AtomicReference<>("");

        Customer customer = dataGenerator.generateCustomer();
        customerRepository.save(customer);

        CountDownLatch latch = new CountDownLatch(1);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                Customer duplicateEmail = dataGenerator.generateCustomer();
                duplicateEmail.setEmail(customer.getEmail());
                customerRepository.save(duplicateEmail);
                consistencyViolated.set(true);
                resultDescription.set("Duplicate email was saved successfully");
            } catch (Exception e) {
                log.info("Expected constraint violation caught: {}", e.getMessage());
            }
            latch.countDown();
        });

        latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        executor.shutdown();

        long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        logResults(testName, duration, "Consistency", consistencyViolated.get(), resultDescription.get());
    }


    @Test
    void testDurability() throws InterruptedException {
        String testName = "durability_test";
        long startTime = System.nanoTime();
        boolean durabilityViolated = false;
        String resultDescription = "";

        Customer customer = dataGenerator.generateCustomer();
        customerRepository.save(customer);
        String savedId = customer.getId().toString();
        String savedEmail = customer.getEmail();

        Thread.sleep(1000);

        Customer retrieved = customerRepository.findById(UUID.fromString(savedId)).orElse(null);
        if (retrieved == null || !retrieved.getEmail().equals(savedEmail)) {
            durabilityViolated = true;
            resultDescription = "Data not persisted correctly";
        }

        long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        logResults(testName, duration, "Durability", durabilityViolated, resultDescription);
    }

    @Test
    void testNonRepeatableRead() throws InterruptedException {
        String testName = "non_repeatable_read_test";
        long startTime = System.nanoTime();
        AtomicBoolean nonRepeatableReadDetected = new AtomicBoolean(false);
        AtomicReference<String> resultDescription = new AtomicReference<>("");

        customerRepository.deleteAll();
        Customer customer = dataGenerator.generateCustomer();
        customerRepository.save(customer);
        String originalName = customer.getFirstName();
        String updatedName = "UpdatedName";

        CountDownLatch latch = new CountDownLatch(1);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);

        txTemplate.execute(status -> {
            // Pierwszy odczyt
            Customer c1 = customerRepository.findById(customer.getId()).orElse(null);
            String name1 = c1 != null ? c1.getFirstName() : null;

            // Współbieżna transakcja zmienia dane
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> {
                TransactionTemplate tx2 = new TransactionTemplate(transactionManager);
                tx2.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
                tx2.execute(s2 -> {
                    Customer c2 = customerRepository.findById(customer.getId()).orElse(null);
                    if (c2 != null) {
                        c2.setFirstName(updatedName);
                        customerRepository.save(c2);
                    }
                    latch.countDown();
                    return null;
                });
            });
            try { latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS); } catch (InterruptedException ignored) {}
            executor.shutdown();

            // Drugi odczyt
            Customer c3 = customerRepository.findById(customer.getId()).orElse(null);
            String name2 = c3 != null ? c3.getFirstName() : null;

            nonRepeatableReadDetected.set(name1 != null && name2 != null && !name1.equals(name2));
            resultDescription.set(String.format("First read: %s, Second read: %s", name1, name2));
            return null;
        });

        long duration = System.nanoTime() - startTime;
        logResults(testName, duration / 1_000_000, "NonRepeatableRead", nonRepeatableReadDetected.get(), resultDescription.get());
    }

    @Test
    void testLostUpdate() throws InterruptedException {
        String testName = "lost_update_test";
        long startTime = System.nanoTime();
        boolean lostUpdateDetected = false;
        String resultDescription = "";

        customerRepository.deleteAll();
        Customer customer = dataGenerator.generateCustomer();
        customerRepository.save(customer);
        String id = customer.getId().toString();

        CountDownLatch latch = new CountDownLatch(2);
        AtomicReference<String> nameA = new AtomicReference<>();
        AtomicReference<String> nameB = new AtomicReference<>();

        Runnable txA = () -> {
            TransactionTemplate tx = new TransactionTemplate(transactionManager);
            tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
            tx.execute(status -> {
                Customer c = customerRepository.findById(customer.getId()).orElse(null);
                if (c != null) {
                    c.setFirstName("A");
                    try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                    customerRepository.save(c);
                    nameA.set("A");
                }
                latch.countDown();
                return null;
            });
        };
        Runnable txB = () -> {
            TransactionTemplate tx = new TransactionTemplate(transactionManager);
            tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
            tx.execute(status -> {
                Customer c = customerRepository.findById(customer.getId()).orElse(null);
                if (c != null) {
                    c.setFirstName("B");
                    try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                    customerRepository.save(c);
                    nameB.set("B");
                }
                latch.countDown();
                return null;
            });
        };
        Thread tA = new Thread(txA);
        Thread tB = new Thread(txB);
        tA.start();
        tB.start();
        latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        Customer after = customerRepository.findById(customer.getId()).orElse(null);
        if (after != null) {
            String finalName = after.getFirstName();
            // Jeśli tylko jedna zmiana się zachowała, mamy lost update
            lostUpdateDetected = !("A".equals(finalName) && "B".equals(finalName));
            resultDescription = "Final name: " + finalName;
        }

        long duration = System.nanoTime() - startTime;
        logResults(testName, duration / 1_000_000, "LostUpdate", lostUpdateDetected, resultDescription);
    }

    private void logResults(String testName, long duration, String propertyTested,
                            boolean violationDetected, String resultDescription) {
        File csvFile = new File(CSV_FILE);
        boolean writeHeader = !csvFile.exists();

        try (FileWriter writer = new FileWriter(csvFile, true)) {
            if (writeHeader) {
                writer.write("Test,Duration[ms],PropertyTested,ViolationDetected,ResultDescription," +
                        "Profile,db.operations,db.errors\n");
            }

            double operations = databaseMetrics.getOperationsCount(testName, activeProfile);
            double errors = databaseMetrics.getErrorsCount(testName, activeProfile);

            writer.write(String.format("%s,%d,%s,%b,\"%s\",%s,%.0f,%.0f\n",
                    testName, duration, propertyTested, violationDetected,
                    resultDescription, activeProfile, operations, errors));

        } catch (IOException e) {
            log.error("Błąd podczas zapisu wyników do CSV", e);
        }
    }
}

