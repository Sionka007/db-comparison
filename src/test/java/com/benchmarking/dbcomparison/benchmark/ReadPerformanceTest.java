package com.benchmarking.dbcomparison.benchmark;

import com.benchmarking.dbcomparison.config.DatabaseMetrics;
import com.benchmarking.dbcomparison.model.*;
import com.benchmarking.dbcomparison.repository.*;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReadPerformanceTest {

    @Autowired
    private DatabaseMetrics databaseMetrics;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BrandRepository brandRepository;

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

    @Test @Order(1)
    void testReadAllCustomers() {
        final String label = "Odczyt klientów";
        final String metricName = "read_customers";

        log.info("Start: {}", label);
        Timer.Sample timer = databaseMetrics.startTimer();
        long startTime = System.nanoTime();

        int size = 0;
        try {
            List<Customer> customers = customerRepository.findAll();
            size = customers.size();
            databaseMetrics.incrementDatabaseOperations(metricName, activeProfile);
            databaseMetrics.recordDataSize(metricName, activeProfile, size);
        } catch (Exception e) {
            databaseMetrics.incrementDatabaseErrors(metricName, activeProfile);
            throw e;
        } finally {
            databaseMetrics.stopTimer(timer, metricName, activeProfile);
        }

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        logPerformance(label, metricName, durationMs, size);
    }

    @Test @Order(2)
    void testReadAllProducts() {
        final String label = "Odczyt produktów";
        final String metricName = "read_products";

        log.info("Start: {}", label);
        Timer.Sample timer = databaseMetrics.startTimer();
        long startTime = System.nanoTime();

        int size = 0;
        try {
            List<Product> products = productRepository.findAll();
            size = products.size();
            databaseMetrics.incrementDatabaseOperations(metricName, activeProfile);
            databaseMetrics.recordDataSize(metricName, activeProfile, size);
        } catch (Exception e) {
            databaseMetrics.incrementDatabaseErrors(metricName, activeProfile);
            throw e;
        } finally {
            databaseMetrics.stopTimer(timer, metricName, activeProfile);
        }

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        logPerformance(label, metricName, durationMs, size);
    }

    @Test @Order(3)
    void testReadAllOrdersWithJoins() {
        final String label = "Odczyt zamówień (z JOIN)";
        final String metricName = "read_orders_with_joins";

        log.info("Start: {}", label);
        Timer.Sample timer = databaseMetrics.startTimer();
        long startTime = System.nanoTime();

        int size = 0;
        try {
            List<com.benchmarking.dbcomparison.model.Order> orders = orderRepository.findAllWithCustomerAndItems();
            size = orders.size();
            databaseMetrics.incrementDatabaseOperations(metricName, activeProfile);
            databaseMetrics.recordDataSize(metricName, activeProfile, size);
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            databaseMetrics.recordJoinQueryMetrics(activeProfile, 3, durationMs);
        } catch (Exception e) {
            databaseMetrics.incrementDatabaseErrors(metricName, activeProfile);
            throw e;
        } finally {
            databaseMetrics.stopTimer(timer, metricName, activeProfile);
        }

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        logPerformance(label, metricName, durationMs, size);
    }

    @Test @Order(4)
    void testReadTopRatedProducts() {
        final String label = "Odczyt TOP produktów";
        final String metricName = "read_top_products";

        log.info("Start: {}", label);
        Timer.Sample timer = databaseMetrics.startTimer();
        long startTime = System.nanoTime();

        int size = 0;
        try {
            List<Product> topProducts = productRepository.findTop100ByOrderByRatingDesc();
            size = topProducts.size();
            databaseMetrics.incrementDatabaseOperations(metricName, activeProfile);
            databaseMetrics.recordDataSize(metricName, activeProfile, size);
            databaseMetrics.recordCacheMetrics(metricName, activeProfile, true);
        } catch (Exception e) {
            databaseMetrics.incrementDatabaseErrors(metricName, activeProfile);
            databaseMetrics.recordCacheMetrics(metricName, activeProfile, false);
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

        File csvFile = new File("performance-read-results.csv");
        boolean writeHeader = !csvFile.exists() || csvFile.length() == 0;

        try (FileWriter writer = new FileWriter(csvFile, true)) {
            if (writeHeader) {
                writer.write("Operacja,Czas[ms],Liczba rekordów,Operacji/s,Profil,DB_operacje,DB_błędy,DB_failed_queries,DB_czas_timer_ms\n");
            }
            writer.write(String.format("%s,%d,%d,%.2f,%s,%.0f,%.0f,%.0f,%.2f\n",
                    label, durationMs, recordCount, opsPerSecond, activeProfile,
                    opsCount, errorsCount, failedQueries, totalOpTime));
        } catch (IOException e) {
            log.error("Błąd podczas zapisu do pliku CSV", e);
        }
    }
}
