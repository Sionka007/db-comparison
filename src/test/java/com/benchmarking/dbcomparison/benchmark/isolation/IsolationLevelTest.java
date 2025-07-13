package com.benchmarking.dbcomparison.benchmark.isolation;

import com.benchmarking.dbcomparison.config.DatabaseMetrics;
import com.benchmarking.dbcomparison.model.Customer;
import com.benchmarking.dbcomparison.repository.CustomerRepository;
import com.benchmarking.dbcomparison.util.DataGenerator;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@SpringBootTest
public class IsolationLevelTest {

    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;

    @Autowired private CustomerRepository customerRepository;
    @Autowired private DatabaseMetrics databaseMetrics;
    @Autowired private PlatformTransactionManager transactionManager;
    private final DataGenerator dataGenerator = new DataGenerator();

    private static final String CSV_FILE = "isolation-test-results.csv";
    private static final int TIMEOUT_SECONDS = 10;

    private TransactionTemplate repeatableReadTx;
    private TransactionTemplate readCommittedTx;
    private TransactionTemplate serializableTx;

    @BeforeEach
    void setUp() {
        repeatableReadTx = new TransactionTemplate(transactionManager);
        repeatableReadTx.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);

        readCommittedTx = new TransactionTemplate(transactionManager);
        readCommittedTx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);

        serializableTx = new TransactionTemplate(transactionManager);
        serializableTx.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
    }

    @Test
    void testPhantomReadRepeatableRead() throws InterruptedException {
        String testName = "phantom_read_repeatable_read";
        long startTime = System.nanoTime();
        Timer.Sample timer = databaseMetrics.startTimer();
        AtomicBoolean phantomDetected = new AtomicBoolean(false);
        AtomicReference<String> resultDescription = new AtomicReference<>("");

        repeatableReadTx.execute(status -> {
            try {
                List<Customer> initialCustomers = customerRepository.findAll();
                int initialCount = initialCustomers.size();

                CountDownLatch insertLatch = new CountDownLatch(1);
                Future<Customer> insertFuture = Executors.newSingleThreadExecutor().submit(() -> {
                    try {
                        return readCommittedTx.execute(innerStatus -> {
                            Customer newCustomer = dataGenerator.generateCustomer();
                            newCustomer = customerRepository.save(newCustomer);
                            insertLatch.countDown();
                            return newCustomer;
                        });
                    } catch (Exception e) {
                        log.error("Błąd podczas dodawania klienta", e);
                        insertLatch.countDown();
                        return null;
                    }
                });

                if (!insertLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    resultDescription.set("Insert operation timed out");
                    return null;
                }

                List<Customer> finalCustomers = customerRepository.findAll();
                int finalCount = finalCustomers.size();

                phantomDetected.set(finalCount != initialCount);
                resultDescription.set(String.format("Initial count: %d, Final count: %d", initialCount, finalCount));

                return null;
            } catch (Exception e) {
                resultDescription.set("Error: " + e.getMessage());
                return null;
            }
        });

        long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        logResults(testName, duration, "REPEATABLE_READ", phantomDetected.get(), resultDescription.get());
    }

    @Test
    void testPhantomReadReadCommitted() throws InterruptedException {
        String testName = "phantom_read_read_committed";
        long startTime = System.nanoTime();
        Timer.Sample timer = databaseMetrics.startTimer();
        AtomicBoolean phantomDetected = new AtomicBoolean(false);
        AtomicReference<String> resultDescription = new AtomicReference<>("");

        readCommittedTx.execute(status -> {
            try {
                List<Customer> initialCustomers = customerRepository.findAll();
                int initialCount = initialCustomers.size();

                CountDownLatch insertLatch = new CountDownLatch(1);
                Future<Customer> insertFuture = Executors.newSingleThreadExecutor().submit(() -> {
                    try {
                        return readCommittedTx.execute(innerStatus -> {
                            Customer newCustomer = dataGenerator.generateCustomer();
                            newCustomer = customerRepository.save(newCustomer);
                            insertLatch.countDown();
                            return newCustomer;
                        });
                    } catch (Exception e) {
                        log.error("Błąd podczas dodawania klienta", e);
                        insertLatch.countDown();
                        return null;
                    }
                });

                if (!insertLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    resultDescription.set("Insert operation timed out");
                    return null;
                }

                List<Customer> finalCustomers = customerRepository.findAll();
                int finalCount = finalCustomers.size();

                phantomDetected.set(finalCount != initialCount);
                resultDescription.set(String.format("Initial count: %d, Final count: %d", initialCount, finalCount));

                return null;
            } catch (Exception e) {
                resultDescription.set("Error: " + e.getMessage());
                return null;
            }
        });

        long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        logResults(testName, duration, "READ_COMMITTED", phantomDetected.get(), resultDescription.get());
    }

    @Test
    void testPhantomReadSerializable() throws InterruptedException {
        String testName = "phantom_read_serializable";
        long startTime = System.nanoTime();
        Timer.Sample timer = databaseMetrics.startTimer();
        AtomicBoolean phantomDetected = new AtomicBoolean(false);
        AtomicReference<String> resultDescription = new AtomicReference<>("");

        serializableTx.execute(status -> {
            try {
                List<Customer> initialCustomers = customerRepository.findAll();
                int initialCount = initialCustomers.size();

                CountDownLatch insertLatch = new CountDownLatch(1);
                Future<Customer> insertFuture = Executors.newSingleThreadExecutor().submit(() -> {
                    try {
                        return serializableTx.execute(innerStatus -> {
                            Customer newCustomer = dataGenerator.generateCustomer();
                            newCustomer = customerRepository.save(newCustomer);
                            insertLatch.countDown();
                            return newCustomer;
                        });
                    } catch (Exception e) {
                        log.error("Błąd podczas dodawania klienta w SERIALIZABLE", e);
                        insertLatch.countDown();
                        return null;
                    }
                });

                if (!insertLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    resultDescription.set("Insert operation timed out");
                    return null;
                }

                List<Customer> finalCustomers = customerRepository.findAll();
                int finalCount = finalCustomers.size();

                phantomDetected.set(finalCount != initialCount);
                resultDescription.set(String.format("Initial count: %d, Final count: %d", initialCount, finalCount));

                return null;
            } catch (Exception e) {
                resultDescription.set("Error: " + e.getMessage());
                return null;
            }
        });

        long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        logResults(testName, duration, "SERIALIZABLE", phantomDetected.get(), resultDescription.get());
    }

    private void logResults(String testName, long duration, String isolationLevel,
                            boolean phantomDetected, String resultDescription) {
        File csvFile = new File(CSV_FILE);
        boolean writeHeader = !csvFile.exists();

        try (FileWriter writer = new FileWriter(csvFile, true)) {
            if (writeHeader) {
                writer.write("Test,Duration[ms],IsolationLevel,PhantomDetected,ResultDescription," +
                        "Profile,db.operations,db.errors\n");
            }

            double operations = databaseMetrics.getOperationsCount(testName, activeProfile);
            double errors = databaseMetrics.getErrorsCount(testName, activeProfile);

            writer.write(String.format("%s,%d,%s,%b,\"%s\",%s,%.0f,%.0f\n",
                    testName, duration, isolationLevel, phantomDetected,
                    resultDescription, activeProfile, operations, errors));

        } catch (IOException e) {
            log.error("Błąd podczas zapisu wyników do CSV", e);
        }
    }
}