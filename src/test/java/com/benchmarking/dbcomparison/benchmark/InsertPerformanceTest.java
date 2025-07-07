package com.benchmarking.dbcomparison.benchmark;

import com.benchmarking.dbcomparison.config.DatabaseMetrics;
import com.benchmarking.dbcomparison.model.*;
import com.benchmarking.dbcomparison.repository.*;
import com.benchmarking.dbcomparison.util.DataGenerator;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Slf4j
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class InsertPerformanceTest {

    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;

    @Autowired private DatabaseMetrics databaseMetrics;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private BrandRepository brandRepository;
    @Autowired private ProductCategoryRepository categoryRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductReviewRepository reviewRepository;
    @Autowired private InventoryMovementRepository movementRepository;

    private DataGenerator dataGenerator;
    private List<Customer> customers;
    private List<Brand> brands;
    private List<ProductCategory> categories;
    private List<Product> products;
    private long testStartTime;

    @BeforeAll
    void setUp() {
        dataGenerator = new DataGenerator();
        customers = new ArrayList<>();
        brands = new ArrayList<>();
        categories = new ArrayList<>();
        products = new ArrayList<>();

        // Wyczyść tabele
        movementRepository.deleteAll();
        reviewRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        brandRepository.deleteAll();
        customerRepository.deleteAll();
    }

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
    void testCustomerInsertPerformance() {
        final String label = "Dodawanie klientów";
        final String metricName = "customer_insert";

        log.info("Start: {}", label);
        Timer.Sample timer = databaseMetrics.startTimer();
        long startTime = System.nanoTime();

        try {
            for (int i = 0; i < 1000; i++) {
                customers.add(dataGenerator.generateCustomer());
            }
            customers = customerRepository.saveAll(customers);

            databaseMetrics.incrementDatabaseOperations(metricName, activeProfile);
            databaseMetrics.recordDataSize("customers", activeProfile, customers.size());
        } catch (Exception e) {
            databaseMetrics.incrementDatabaseErrors(metricName, activeProfile);
            throw e;
        } finally {
            databaseMetrics.stopTimer(timer, metricName, activeProfile);
        }

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        logPerformance(label, metricName, durationMs, customers.size());
    }

    @Test @Order(2)
    void testBrandAndCategoryInsertPerformance() {
        final String label = "Dodawanie marek i kategorii";
        final String metricName = "brand_category_insert";

        log.info("Start: {}", label);
        Timer.Sample timer = databaseMetrics.startTimer();
        long startTime = System.nanoTime();

        try {
            for (int i = 0; i < 50; i++) {
                brands.add(dataGenerator.generateBrand());
            }
            brands = brandRepository.saveAll(brands);
            databaseMetrics.incrementDatabaseOperations("brand_insert", activeProfile);
            databaseMetrics.recordDataSize("brands", activeProfile, brands.size());

            for (int i = 0; i < 5; i++) {
                ProductCategory main = categoryRepository.save(dataGenerator.generateCategory(null));
                categories.add(main);
                for (int j = 0; j < 3; j++) {
                    ProductCategory sub = categoryRepository.save(dataGenerator.generateCategory(main));
                    categories.add(sub);
                }
            }
            databaseMetrics.incrementDatabaseOperations("category_insert", activeProfile);
            databaseMetrics.recordDataSize("categories", activeProfile, categories.size());

        } catch (Exception e) {
            databaseMetrics.incrementDatabaseErrors(metricName, activeProfile);
            throw e;
        } finally {
            databaseMetrics.stopTimer(timer, metricName, activeProfile);
        }

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        logPerformance(label, metricName, durationMs, brands.size() + categories.size());
    }

    @Test @Order(3)
    void testProductInsertPerformance() {
        final String label = "Dodawanie produktów";
        final String metricName = "product_insert";

        log.info("Start: {}", label);
        Timer.Sample timer = databaseMetrics.startTimer();
        long startTime = System.nanoTime();

        try {
            assumeTrue(!brands.isEmpty() && !categories.isEmpty(), "Brak marek lub kategorii – pomiń test");

            products = new ArrayList<>();
            for (int i = 0; i < 500; i++) {
                Brand brand = brands.get(i % brands.size());
                ProductCategory category = categories.get(i % categories.size());
                products.add(dataGenerator.generateProduct(brand, category));
            }
            products = productRepository.saveAll(products);

            databaseMetrics.incrementDatabaseOperations(metricName, activeProfile);
            databaseMetrics.recordDataSize("products", activeProfile, products.size());
        } catch (Exception e) {
            databaseMetrics.incrementDatabaseErrors(metricName, activeProfile);
            throw e;
        } finally {
            databaseMetrics.stopTimer(timer, metricName, activeProfile);
        }

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        logPerformance(label, metricName, durationMs, products.size());
    }

    @Test @Order(4)
    void testOrderInsertPerformance() {
        final String label = "Dodawanie zamówień";
        final String metricName = "order_insert";

        log.info("Start: {}", label);
        Timer.Sample timer = databaseMetrics.startTimer();
        long startTime = System.nanoTime();

        List<com.benchmarking.dbcomparison.model.Order> orders = new ArrayList<>();
        try {
            assumeTrue(!products.isEmpty() && !customers.isEmpty(), "Brak klientów lub produktów – pomiń test");

            for (int i = 0; i < 200; i++) {
                Customer customer = customers.get(i % customers.size());
                orders.add(dataGenerator.generateOrder(customer, products));
            }
            orders = orderRepository.saveAll(orders);

            databaseMetrics.incrementDatabaseOperations(metricName, activeProfile);
            databaseMetrics.recordDataSize("orders", activeProfile, orders.size());
        } catch (Exception e) {
            databaseMetrics.incrementDatabaseErrors(metricName, activeProfile);
            throw e;
        } finally {
            databaseMetrics.stopTimer(timer, metricName, activeProfile);
        }

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        logPerformance(label, metricName, durationMs, orders.size());
    }

    @Test @Order(5)
    void testProductReviewInsertPerformance() {
        final String label = "Dodawanie opinii o produktach";
        final String metricName = "product_review_insert";

        log.info("Start: {}", label);
        Timer.Sample timer = databaseMetrics.startTimer();
        long startTime = System.nanoTime();

        List<ProductReview> reviews = new ArrayList<>();
        try {
            assumeTrue(!products.isEmpty() && !customers.isEmpty(), "Brak klientów lub produktów – pomiń test");

            for (int i = 0; i < 300; i++) {
                Customer customer = customers.get(i % customers.size());
                Product product = products.get(i % products.size());
                reviews.add(dataGenerator.generateReview(product, customer));
            }
            reviews = reviewRepository.saveAll(reviews);

            databaseMetrics.incrementDatabaseOperations(metricName, activeProfile);
            databaseMetrics.recordDataSize("product_reviews", activeProfile, reviews.size());
        } catch (Exception e) {
            databaseMetrics.incrementDatabaseErrors(metricName, activeProfile);
            throw e;
        } finally {
            databaseMetrics.stopTimer(timer, metricName, activeProfile);
        }

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        logPerformance(label, metricName, durationMs, reviews.size());
    }

    @Test @Order(6)
    void testInventoryMovementInsertPerformance() {
        final String label = "Dodawanie ruchów magazynowych";
        final String metricName = "inventory_movement_insert";

        log.info("Start: {}", label);
        Timer.Sample timer = databaseMetrics.startTimer();
        long startTime = System.nanoTime();

        List<InventoryMovement> movements = new ArrayList<>();
        try {
            assumeTrue(!products.isEmpty(), "Brak produktów – pomiń test");

            for (int i = 0; i < 400; i++) {
                Product product = products.get(i % products.size());
                movements.add(dataGenerator.generateInventoryMovement(product));
            }
            movements = movementRepository.saveAll(movements);

            databaseMetrics.incrementDatabaseOperations(metricName, activeProfile);
            databaseMetrics.recordDataSize("inventory_movements", activeProfile, movements.size());
        } catch (Exception e) {
            databaseMetrics.incrementDatabaseErrors(metricName, activeProfile);
            throw e;
        } finally {
            databaseMetrics.stopTimer(timer, metricName, activeProfile);
        }

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        logPerformance(label, metricName, durationMs, movements.size());
    }

    private void logPerformance(String label, String metricName, long durationMs, int recordCount) {
        double opsCount = databaseMetrics.getOperationsCount(metricName, activeProfile);
        double errorsCount = databaseMetrics.getErrorsCount(metricName, activeProfile);
        double failedQueries = databaseMetrics.getFailedQueriesCount();
        double totalOpTime = databaseMetrics.getTotalOperationTimeMillis(metricName, activeProfile);
        double opsPerSecond = durationMs > 0 ? recordCount / (durationMs / 1000.0) : 0;

        File csvFile = new File("performance-insert-results.csv");
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
