package com.benchmarking.dbcomparison.benchmark;

import com.benchmarking.dbcomparison.config.BenchmarkConfig;
import com.benchmarking.dbcomparison.config.DatabaseMetrics;
import com.benchmarking.dbcomparison.model.*;
import com.benchmarking.dbcomparison.repository.*;
import com.benchmarking.dbcomparison.util.CsvFormatter;
import com.benchmarking.dbcomparison.util.DataGenerator;
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
    @Autowired
    private BenchmarkConfig benchmarkConfig;

    private DataGenerator dataGenerator;
    private List<Customer> customers;
    private List<Brand> brands;
    private List<ProductCategory> categories;
    private List<Product> products;
    private long testStartTime;

    // Zmiana ze stałych na właściwości konfigurowane
    private int getMaxRecords() {
        return benchmarkConfig.getRecordCount();
    }

    private final static int MAX_CATEGORIES = 200;
    private final static int SUBCATEGORIES_PER_MAIN = 4;

    public void setUp() {
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

    void beforeEach() {
        testStartTime = System.nanoTime();
    }

    void afterEach() {
        long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - testStartTime);
        log.info("Test zakończony w {} ms", duration);
    }

    public void testCustomerInsertPerformance() {
        final String label = "Dodawanie klientów";
        final String metricName = "customer_insert";

        log.info("Start: {}", label);
        Timer.Sample timer = databaseMetrics.startTimer();
        long startTime = System.nanoTime();

        try {
            for (int i = 0; i < getMaxRecords(); i++) {
                customers.add(dataGenerator.generateCustomer());
            }
            customers = customerRepository.saveAll(customers);
            databaseMetrics.incrementDatabaseOperations(metricName, activeProfile);
            databaseMetrics.recordDataSize("customers", activeProfile, customers.size());
        } catch (Exception e) {
            log.error("Błąd podczas {}: {}", label, e.getMessage());
            databaseMetrics.incrementFailedQueries();
            throw e;
        } finally {
            databaseMetrics.stopTimer(timer, metricName, activeProfile);
        }

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        logPerformance(label, metricName, durationMs, customers.size());
    }

    public void testBrandAndCategoryInsertPerformance() {
        final String label = "Dodawanie marek i kategorii";
        final String metricName = "brand_category_insert";

        log.info("Start: {}", label);
        Timer.Sample timer = databaseMetrics.startTimer();
        long startTime = System.nanoTime();

        try {
            for (int i = 0; i < getMaxRecords(); i++) {
                brands.add(dataGenerator.generateBrand());
            }
            brands = brandRepository.saveAll(brands);
            databaseMetrics.incrementDatabaseOperations("brand_insert", activeProfile);
            databaseMetrics.recordDataSize("brands", activeProfile, brands.size());

            // Zmiana: generowanie 1000 kategorii zamiast 20
            for (int i = 0; i < MAX_CATEGORIES; i++) { // 200 kategorii głównych
                ProductCategory main = categoryRepository.save(dataGenerator.generateCategory(null));
                categories.add(main);
                for (int j = 0; j < SUBCATEGORIES_PER_MAIN; j++) { // 4 podkategorie dla każdej = 1000 łącznie
                    ProductCategory sub = categoryRepository.save(dataGenerator.generateCategory(main));
                    categories.add(sub);
                }
            }
            databaseMetrics.incrementDatabaseOperations("category_insert", activeProfile);
            databaseMetrics.recordDataSize("categories", activeProfile, categories.size());
        } catch (Exception e) {
            log.error("Błąd podczas {}: {}", label, e.getMessage());
            databaseMetrics.incrementFailedQueries();
            throw e;
        } finally {
            databaseMetrics.stopTimer(timer, metricName, activeProfile);
        }

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        logPerformance(label, metricName, durationMs, brands.size() + categories.size());
    }

    public void testProductInsertPerformance() {
        final String label = "Dodawanie produktów";
        final String metricName = "product_insert";

        log.info("Start: {}", label);
        Timer.Sample timer = databaseMetrics.startTimer();
        long startTime = System.nanoTime();

        try {
            products = new ArrayList<>();
            for (int i = 0; i < getMaxRecords(); i++) {
                Brand brand = brands.get(i % brands.size());
                ProductCategory category = categories.get(i % categories.size());
                products.add(dataGenerator.generateProduct(brand, category));
            }
            products = productRepository.saveAll(products);
            databaseMetrics.incrementDatabaseOperations(metricName, activeProfile);
            databaseMetrics.recordDataSize("products", activeProfile, products.size());
        } catch (Exception e) {
            log.error("Błąd podczas {}: {}", label, e.getMessage());
            databaseMetrics.incrementFailedQueries();
            throw e;
        } finally {
            databaseMetrics.stopTimer(timer, metricName, activeProfile);
        }

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        logPerformance(label, metricName, durationMs, products.size());
    }

    public void testOrderInsertPerformance() {
        final String label = "Dodawanie zamówień";
        final String metricName = "order_insert";

        log.info("Start: {}", label);
        Timer.Sample timer = databaseMetrics.startTimer();
        long startTime = System.nanoTime();

        List<Order> orders = new ArrayList<>();
        try {
            for (int i = 0; i < getMaxRecords(); i++) {
                // Mierzenie cache hit ratio podczas pobierania customera
                Customer customer = customers.get(i % customers.size());
                databaseMetrics.recordCacheHit("SELECT", activeProfile, customer != null);

                // Mierzenie cache hit ratio podczas pobierania produktów
                List<Product> selectedProducts = products.subList(0, Math.min(5, products.size()));
                databaseMetrics.recordCacheHit("SELECT", activeProfile, !selectedProducts.isEmpty());

                orders.add(dataGenerator.generateOrder(customer, selectedProducts));
            }
            orders = orderRepository.saveAll(orders);
            databaseMetrics.incrementDatabaseOperations(metricName, activeProfile);
            databaseMetrics.recordDataSize("orders", activeProfile, orders.size());
        } catch (Exception e) {
            log.error("Błąd podczas {}: {}", label, e.getMessage());
            databaseMetrics.incrementFailedQueries();
            throw e;
        } finally {
            databaseMetrics.stopTimer(timer, metricName, activeProfile);
        }

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        logPerformance(label, metricName, durationMs, orders.size());
    }

    public void testProductReviewInsertPerformance() {
        final String label = "Dodawanie opinii o produktach";
        final String metricName = "product_review_insert";

        log.info("Start: {}", label);
        Timer.Sample timer = databaseMetrics.startTimer();
        long startTime = System.nanoTime();

        List<ProductReview> reviews = new ArrayList<>();
        try {
            for (int i = 0; i < getMaxRecords(); i++) {
                // Mierzenie cache hit ratio podczas pobierania customera i produktu
                Customer customer = customers.get(i % customers.size());
                databaseMetrics.recordCacheHit("SELECT", activeProfile, customer != null);

                Product product = products.get(i % products.size());
                databaseMetrics.recordCacheHit("SELECT", activeProfile, product != null);

                reviews.add(dataGenerator.generateReview(product, customer));
            }
            reviews = reviewRepository.saveAll(reviews);
            databaseMetrics.incrementDatabaseOperations(metricName, activeProfile);
            databaseMetrics.recordDataSize("product_reviews", activeProfile, reviews.size());
        } catch (Exception e) {
            log.error("Błąd podczas {}: {}", label, e.getMessage());
            databaseMetrics.incrementFailedQueries();
            throw e;
        } finally {
            databaseMetrics.stopTimer(timer, metricName, activeProfile);
        }

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        logPerformance(label, metricName, durationMs, reviews.size());
    }

    public void testInventoryMovementInsertPerformance() {
        final String label = "Dodawanie ruchów magazynowych";
        final String metricName = "inventory_movement_insert";

        log.info("Start: {}", label);
        Timer.Sample timer = databaseMetrics.startTimer();
        long startTime = System.nanoTime();

        List<InventoryMovement> movements = new ArrayList<>();
        try {
            for (int i = 0; i < getMaxRecords(); i++) {
                Product product = products.get(i % products.size());
                movements.add(dataGenerator.generateInventoryMovement(product));
            }
            movements = movementRepository.saveAll(movements);
            databaseMetrics.incrementDatabaseOperations(metricName, activeProfile);
            databaseMetrics.recordDataSize("inventory_movements", activeProfile, movements.size());
        } catch (Exception e) {
            log.error("Błąd podczas {}: {}", label, e.getMessage());
            databaseMetrics.incrementFailedQueries();
            throw e;
        } finally {
            databaseMetrics.stopTimer(timer, metricName, activeProfile);
        }

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        logPerformance(label, metricName, durationMs, movements.size());
    }

    private void logPerformance(String label, String metricName, long durationMs, int recordCount) {
        double opsCount = databaseMetrics.getOperationsCount(metricName, activeProfile);
        double failedQueries = databaseMetrics.getFailedQueriesCount();
        double totalOpTime = databaseMetrics.getTotalOperationTimeMillis(metricName, activeProfile);
        double opsPerSecond = durationMs > 0 ? recordCount / (durationMs / 1000.0) : 0;

        saveToCsv(label, durationMs, recordCount, activeProfile);
    }

    private void saveToCsv(String label, long durationMs, int recordCount, String profile) {
        File csvFile = new File("performance-insert-results.csv");
        boolean writeHeader = !csvFile.exists() || csvFile.length() == 0;

        try (FileWriter writer = new FileWriter(csvFile, true)) {
            if (writeHeader) {
                writer.write("Operacja;Czas[ms];Liczba rekordów;Operacji/s;Profil;DB_operacje;DB_failed_queries;DB_czas_timer_ms\n");
            }
            double operationsPerSecond = (recordCount * 1000.0) / durationMs;
            writer.write(CsvFormatter.formatCsvLine(
                label,
                durationMs,
                recordCount,
                operationsPerSecond,
                profile,
                databaseMetrics.getOperationsCount(label, profile),
                databaseMetrics.getFailedQueriesCount(),
                databaseMetrics.getTotalOperationTimeMillis(label, profile)
            ));
        } catch (IOException e) {
            log.error("Błąd podczas zapisywania wyników do CSV", e);
        }
    }

    public void cleanDatabaseAfterAll() {
        movementRepository.deleteAll();
        reviewRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        brandRepository.deleteAll();
        customerRepository.deleteAll();

        log.info("Baza danych została wyczyszczona po zakończeniu testów InsertPerformanceTest.");
    }

    public void runAll() {
        setUp();
        testCustomerInsertPerformance();
        testBrandAndCategoryInsertPerformance();
        testProductInsertPerformance();
        testOrderInsertPerformance();
        testProductReviewInsertPerformance();
        testInventoryMovementInsertPerformance();
        cleanDatabaseAfterAll();
    }
}
