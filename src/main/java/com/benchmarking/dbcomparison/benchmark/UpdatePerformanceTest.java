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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
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

    void beforeEach() {
        log.info("----> START {}", "UpdatePerformanceTest");
        testStartTime = System.nanoTime();
    }


    void afterEach() {
        long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - testStartTime);
        log.info("<---- KONIEC {} ({} ms)", "UpdatePerformanceTest", duration);
    }


    void testUpdateProductPrices() {
        final String label = "Aktualizacja cen produktów";
        final String metricName = "update_products";
        final String operation = "UPDATE";

        log.info("Start: {}", label);
        Timer.Sample timer = databaseMetrics.startTimer();
        long startTime = System.nanoTime();

        int totalSize = 0;
        try {
            List<Product> allProducts = productRepository.findAll();
            int recordsToProcess = Math.min(1000, allProducts.size());
            List<Product> productsToUpdate = new ArrayList<>();

            // Przygotuj 1000 produktów do aktualizacji
            for (int i = 0; i < recordsToProcess; i++) {
                Product product = allProducts.get(i % allProducts.size());
                product.setPrice(product.getPrice().multiply(java.math.BigDecimal.valueOf(1.1)));
                productsToUpdate.add(product);
                totalSize++;
            }

            // Aktualizuj wszystkie produkty jedną operacją
            productRepository.saveAll(productsToUpdate);

            databaseMetrics.incrementDatabaseOperations(metricName, activeProfile);
            databaseMetrics.incrementDatabaseOperations(operation, activeProfile);
            databaseMetrics.recordDataSize(metricName, activeProfile, totalSize);
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            databaseMetrics.recordLockWaitTime("products_table", activeProfile, durationMs);
            databaseMetrics.recordTransactionTime(durationMs);
        } catch (Exception e) {
            databaseMetrics.incrementDatabaseErrors(metricName, activeProfile);
            throw e;
        } finally {
            databaseMetrics.stopTimer(timer, operation, activeProfile);
        }

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        logPerformance(label, metricName, durationMs, totalSize);
    }


    void testUpdateCustomerEmails() {
        final String label = "Aktualizacja emaili klientów";
        final String metricName = "update_customers";
        final String operation = "UPDATE";

        log.info("Start: {}", label);
        Timer.Sample timer = databaseMetrics.startTimer();
        long startTime = System.nanoTime();

        int totalSize = 0;
        try {
            List<Customer> allCustomers = customerRepository.findAll();
            int recordsToProcess = Math.min(1000, allCustomers.size());
            List<Customer> customersToUpdate = new ArrayList<>();

            // Przygotuj 1000 klientów do aktualizacji
            for (int i = 0; i < recordsToProcess; i++) {
                Customer customer = allCustomers.get(i % allCustomers.size());
                customer.setEmail("updated+" + customer.getId() + "_" + i + "@mail.com");
                customersToUpdate.add(customer);
                totalSize++;
            }

            // Aktualizuj wszystkich klientów jedną operacją
            customerRepository.saveAll(customersToUpdate);

            databaseMetrics.incrementDatabaseOperations(metricName, activeProfile);
            databaseMetrics.incrementDatabaseOperations(operation, activeProfile);
            databaseMetrics.recordDataSize(metricName, activeProfile, totalSize);
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            databaseMetrics.recordLockWaitTime("customers_table", activeProfile, durationMs);
            databaseMetrics.recordTransactionTime(durationMs);
        } catch (Exception e) {
            databaseMetrics.incrementDatabaseErrors(metricName, activeProfile);
            throw e;
        } finally {
            databaseMetrics.stopTimer(timer, operation, activeProfile);
        }

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        logPerformance(label, metricName, durationMs, totalSize);
    }


    void testUpdateOrderStatus() {
        final String label = "Aktualizacja statusów zamówień";
        final String metricName = "update_orders";
        final String operation = "UPDATE";

        log.info("Start: {}", label);
        Timer.Sample timer = databaseMetrics.startTimer();
        long startTime = System.nanoTime();

        int totalSize = 0;
        try {
            List<Order> allOrders = orderRepository.findAll();
            int recordsToProcess = Math.min(1000, allOrders.size());
            List<Order> ordersToUpdate = new ArrayList<>();

            // Przygotuj 1000 zamówień do aktualizacji
            for (int i = 0; i < recordsToProcess; i++) {
                Order order = allOrders.get(i % allOrders.size());
                order.setStatus("UPDATED_" + i);
                ordersToUpdate.add(order);
                totalSize++;
            }

            // Aktualizuj wszystkie zamówienia jedną operacją
            orderRepository.saveAll(ordersToUpdate);

            databaseMetrics.incrementDatabaseOperations(metricName, activeProfile);
            databaseMetrics.incrementDatabaseOperations(operation, activeProfile);
            databaseMetrics.recordDataSize(metricName, activeProfile, totalSize);
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            databaseMetrics.recordLockWaitTime("orders_table", activeProfile, durationMs);
            databaseMetrics.recordTransactionTime(durationMs);
        } catch (Exception e) {
            databaseMetrics.incrementDatabaseErrors(metricName, activeProfile);
            throw e;
        } finally {
            databaseMetrics.stopTimer(timer, operation, activeProfile);
        }

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        logPerformance(label, metricName, durationMs, totalSize);
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

    //runUpdateTests
    public void runAll() {
        beforeEach();
        try {
            testUpdateProductPrices();
            testUpdateCustomerEmails();
            testUpdateOrderStatus();
        } catch (Exception e) {
            log.error("Błąd podczas wykonywania testów aktualizacji: {}", e.getMessage(), e);
        } finally {
            afterEach();
        }
    }
}
