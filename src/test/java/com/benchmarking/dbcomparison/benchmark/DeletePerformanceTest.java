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
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DeletePerformanceTest {

    @Autowired private DatabaseMetrics databaseMetrics;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductReviewRepository reviewRepository;
    @Autowired private InventoryMovementRepository movementRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private BrandRepository brandRepository;
    @Autowired private ProductCategoryRepository categoryRepository;

    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;

    private long testStartTime;

    @BeforeEach
    void beforeEach() {
        testStartTime = System.nanoTime();
    }

    @AfterEach
    void afterEach(TestInfo testInfo) {
        long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - testStartTime);
        log.info("Test {} zakończony w {} ms", testInfo.getDisplayName(), duration);
    }

    @Test @Order(1)
    void testDeleteInventoryMovements() {
        performDeleteTest("Usuwanie ruchów magazynowych", "delete_inventory_movements",
                movementRepository.findAll(),
                movementRepository::delete,
                "inventory_movements");
    }

    @Test @Order(2)
    void testDeleteProductReviews() {
        performDeleteTest("Usuwanie opinii produktów", "delete_product_reviews",
                reviewRepository.findAll(),
                reviewRepository::delete,
                "product_reviews");
    }

    @Test @Order(3)
    void testDeleteOrderItems() {
        performDeleteTest("Usuwanie pozycji zamówień", "delete_order_items",
                orderItemRepository.findAll(),
                orderItemRepository::delete,
                "order_items");
    }

    @Test @Order(4)
    void testDeleteOrders() {
        performDeleteTest("Usuwanie zamówień", "delete_orders",
                orderRepository.findAll(),
                orderRepository::delete,
                "orders");
    }

    @Test @Order(5)
    void testDeleteProducts() {
        reviewRepository.deleteAll();
        movementRepository.deleteAll();
        performDeleteTest("Usuwanie produktów", "delete_products",
                productRepository.findAll(),
                productRepository::delete,
                "products");
    }

    @Test @Order(6)
    void testDeleteCategories() {
        performDeleteTest("Usuwanie kategorii", "delete_categories",
                categoryRepository.findAll(),
                categoryRepository::delete,
                "product_categories");
    }

    @Test @Order(7)
    void testDeleteBrands() {
        performDeleteTest("Usuwanie marek", "delete_brands",
                brandRepository.findAll(),
                brandRepository::delete,
                "brands");
    }

    @Test @Order(8)
    void testDeleteCustomers() {
        performDeleteTest("Usuwanie klientów", "delete_customers",
                customerRepository.findAll(),
                customerRepository::delete,
                "customers");
    }

    // Metoda pomocnicza do wykonywania testów usuwania
    private <T> void performDeleteTest(String label, String metricName, List<T> entities, java.util.function.Consumer<T> deleteFunction, String lockWaitLabel) {
        log.info("Start: {}", label);
        Timer.Sample timer = databaseMetrics.startTimer();
        long startTime = System.nanoTime();

        int count = entities.size();

        try {
            entities.forEach(deleteFunction);

            databaseMetrics.incrementDatabaseOperations(metricName, activeProfile);
            databaseMetrics.recordDataSize(metricName, activeProfile, count);

            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            databaseMetrics.recordLockWaitTime(lockWaitLabel, activeProfile, durationMs);
            databaseMetrics.recordTransactionTime(durationMs);

            logPerformance(label, metricName, durationMs, count);
        } catch (Exception e) {
            databaseMetrics.incrementDatabaseErrors(metricName, activeProfile);
            throw e;
        } finally {
            databaseMetrics.stopTimer(timer, metricName, activeProfile);
        }
    }

    private void logPerformance(String label, String metricName, long durationMs, int recordCount) {
        double opsCount = databaseMetrics.getOperationsCount(metricName, activeProfile);
        double errorsCount = databaseMetrics.getErrorsCount(metricName, activeProfile);
        double failedQueries = databaseMetrics.getFailedQueriesCount();
        double totalOpTime = databaseMetrics.getTotalOperationTimeMillis(metricName, activeProfile);
        double opsPerSecond = durationMs > 0 ? recordCount / (durationMs / 1000.0) : 0;

        File csvFile = new File("performance-delete-results.csv");
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
