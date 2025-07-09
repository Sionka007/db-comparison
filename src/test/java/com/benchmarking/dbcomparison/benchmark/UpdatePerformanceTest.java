package com.benchmarking.dbcomparison.benchmark;

import com.benchmarking.dbcomparison.config.DatabaseMetrics;
import com.benchmarking.dbcomparison.model.Customer;
import com.benchmarking.dbcomparison.model.Order;
import com.benchmarking.dbcomparison.model.Product;
import com.benchmarking.dbcomparison.repository.CustomerRepository;
import com.benchmarking.dbcomparison.repository.OrderRepository;
import com.benchmarking.dbcomparison.repository.ProductRepository;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UpdatePerformanceTest {

    @Autowired
    private DatabaseMetrics databaseMetrics;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;

    private long testStartTime;

    @BeforeEach
    void beforeEach(TestInfo testInfo) {
        log.info("----> START {}", testInfo.getDisplayName());
        testStartTime = System.nanoTime();
    }

    @AfterEach
    void afterEach(TestInfo testInfo) {
        long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - testStartTime);
        log.info("<---- KONIEC {} ({} ms)", testInfo.getDisplayName(), duration);
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    void testUpdateProductPrices() {
        final String label = "Aktualizacja cen produktów";
        final String metricName = "update_products";

        log.info("Start: {}", label);
        Timer.Sample timer = databaseMetrics.startTimer();
        long startTime = System.nanoTime();

        int size = 0;
        try {
            List<Product> products = productRepository.findAll();
            for (Product product : products) {
                product.setPrice(product.getPrice().multiply(java.math.BigDecimal.valueOf(1.1)));
            }
            List<Product> updated = productRepository.saveAll(products);
            size = updated.size();

            databaseMetrics.incrementDatabaseOperations(metricName, activeProfile);
            databaseMetrics.recordDataSize(metricName, activeProfile, size);
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            databaseMetrics.recordLockWaitTime("products_table", activeProfile, durationMs);
            databaseMetrics.recordTransactionTime(durationMs);
        } catch (Exception e) {
            databaseMetrics.incrementDatabaseErrors(metricName, activeProfile);
            throw e;
        } finally {
            databaseMetrics.stopTimer(timer, metricName, activeProfile);
        }

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        logPerformance(label, metricName, durationMs, size);
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    void testUpdateCustomerEmails() {
        final String label = "Aktualizacja emaili klientów";
        final String metricName = "update_customers";

        log.info("Start: {}", label);
        Timer.Sample timer = databaseMetrics.startTimer();
        long startTime = System.nanoTime();

        int size = 0;
        try {
            List<Customer> customers = customerRepository.findAll();
            for (Customer customer : customers) {
                customer.setEmail("updated+" + customer.getId() + "@mail.com");
            }
            List<Customer> updated = customerRepository.saveAll(customers);
            size = updated.size();

            databaseMetrics.incrementDatabaseOperations(metricName, activeProfile);
            databaseMetrics.recordDataSize(metricName, activeProfile, size);
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            databaseMetrics.recordLockWaitTime("customers_table", activeProfile, durationMs);
            databaseMetrics.recordTransactionTime(durationMs);
        } catch (Exception e) {
            databaseMetrics.incrementDatabaseErrors(metricName, activeProfile);
            throw e;
        } finally {
            databaseMetrics.stopTimer(timer, metricName, activeProfile);
        }

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        logPerformance(label, metricName, durationMs, size);
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    void testUpdateOrderStatus() {
        final String label = "Aktualizacja statusów zamówień";
        final String metricName = "update_orders";

        log.info("Start: {}", label);
        Timer.Sample timer = databaseMetrics.startTimer();
        long startTime = System.nanoTime();

        int size = 0;
        try {
            List<Order> orders = orderRepository.findAll();
            for (Order order : orders) {
                order.setStatus("UPDATED");
            }
            List<Order> updated = orderRepository.saveAll(orders);
            size = updated.size();

            databaseMetrics.incrementDatabaseOperations(metricName, activeProfile);
            databaseMetrics.recordDataSize(metricName, activeProfile, size);
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            databaseMetrics.recordLockWaitTime("orders_table", activeProfile, durationMs);
            databaseMetrics.recordTransactionTime(durationMs);
        } catch (Exception e) {
            databaseMetrics.incrementDatabaseErrors(metricName, activeProfile);
            throw e;
        } finally {
            databaseMetrics.stopTimer(timer, metricName, activeProfile);
        }

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        logPerformance(label, metricName, durationMs, size);
    }

    private void logPerformance(String label, String metricName, long durationMs, int recordCount) {
        double opsCount = databaseMetrics.getOperationsCount(metricName, activeProfile);
        double errorsCount = databaseMetrics.getErrorsCount(metricName, activeProfile);
        double failedQueries = databaseMetrics.getFailedQueriesCount();
        double totalOpTime = databaseMetrics.getTotalOperationTimeMillis(metricName, activeProfile);
        double opsPerSecond = durationMs > 0 ? recordCount / (durationMs / 1000.0) : 0;

        File csvFile = new File("performance-update-results.csv");
        boolean writeHeader = !csvFile.exists() || csvFile.length() == 0;

        try (FileWriter writer = new FileWriter(csvFile, true)) {
            if (writeHeader) {
                writer.write("Operacja,Czas[ms],Liczba rekordów,Operacji/s,Profil,DB_operacje,DB_błędy,DB_failed_queries,DB_czas_timer_ms\n");
            }
            writer.write(String.format("%s,%d,%d,%.2f,%s,%.0f,%.0f,%.0f,%.2f\n",
                    label, durationMs, recordCount, opsPerSecond, activeProfile,
                    opsCount, errorsCount, failedQueries, totalOpTime));
        } catch (IOException e) {
            log.error("Bd podczas zapisu do pliku CSV", e);
        }
    }
}
