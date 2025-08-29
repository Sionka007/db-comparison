package com.benchmarking.dbcomparison.benchmark;

import com.benchmarking.dbcomparison.config.BenchmarkConfig;
import com.benchmarking.dbcomparison.config.DatabaseMetrics;
import com.benchmarking.dbcomparison.model.*;
import com.benchmarking.dbcomparison.repository.*;
import com.benchmarking.dbcomparison.util.DataGenerator;
import io.micrometer.core.instrument.Timer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class InsertPerformanceTest {

    private final DatabaseMetrics databaseMetrics;
    private final CustomerRepository customerRepository;
    private final BrandRepository brandRepository;
    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ProductReviewRepository reviewRepository;
    private final InventoryMovementRepository movementRepository;
    private final OrderItemRepository orderItemRepository;
    private final BenchmarkConfig benchmarkConfig;

    @PersistenceContext
    private EntityManager em;

    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;

    private DataGenerator dataGenerator = new DataGenerator();

    private List<Customer> customers = new ArrayList<>();
    private List<Brand> brands = new ArrayList<>();
    private List<ProductCategory> categories = new ArrayList<>();
    private List<Product> products = new ArrayList<>();

    private static final int MAX_CATEGORIES = 200;
    private static final int SUBCATEGORIES_PER_MAIN = 4;

    public InsertPerformanceTest(DatabaseMetrics databaseMetrics,
                                 CustomerRepository customerRepository,
                                 BrandRepository brandRepository,
                                 ProductCategoryRepository categoryRepository,
                                 ProductRepository productRepository,
                                 OrderRepository orderRepository,
                                 ProductReviewRepository reviewRepository,
                                 InventoryMovementRepository movementRepository,
                                 OrderItemRepository orderItemRepository,
                                 BenchmarkConfig benchmarkConfig) {
        this.databaseMetrics = databaseMetrics;
        this.customerRepository = customerRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
        this.movementRepository = movementRepository;
        this.orderItemRepository = orderItemRepository;
        this.benchmarkConfig = benchmarkConfig;
    }

    private boolean isMySql()    { return activeProfile != null && activeProfile.toLowerCase().contains("mysql"); }
    private boolean isPostgres() { return activeProfile != null && activeProfile.toLowerCase().contains("postgres"); }

    private int maxRecords() { return Math.max(1, benchmarkConfig.getRecordCount()); }
    private int batchSize()  { return Math.max(100, benchmarkConfig.getBatchSize()); }

    private void initIfNeeded() {
        if (dataGenerator == null) dataGenerator = new DataGenerator();
        if (customers == null) customers = new ArrayList<>();
        if (brands == null) brands = new ArrayList<>();
        if (categories == null) categories = new ArrayList<>();
        if (products == null) products = new ArrayList<>();
    }

    /** Szybkie czyszczenie danych. */
    private void truncateAll() {
        String[] tables = new String[] {
                "order_item",
                "orders",
                "product_review",
                "inventory_movement",
                "product",
                "product_category",
                "brand",
                "customer"
        };
        if (isPostgres()) {
            em.createNativeQuery("TRUNCATE TABLE " + String.join(", ", tables) + " RESTART IDENTITY CASCADE")
                    .executeUpdate();
            return;
        }
        if (isMySql()) {
            em.createNativeQuery("SET FOREIGN_KEY_CHECKS=0").executeUpdate();
            for (String t : tables) em.createNativeQuery("TRUNCATE TABLE " + t).executeUpdate();
            em.createNativeQuery("SET FOREIGN_KEY_CHECKS=1").executeUpdate();
            return;
        }
        for (String t : tables) em.createNativeQuery("DELETE FROM " + t).executeUpdate();
    }

    private void clearLocalCaches() {
        em.flush(); em.clear();
        customers.clear(); brands.clear(); categories.clear(); products.clear();
    }

    private void writeCsv(String label, long durationMs, int recordCount, String metricName) {
        File csv = new File("performance-insert-results.csv");
        boolean header = !csv.exists() || csv.length() == 0;
        double opsPerSec = durationMs > 0 ? (recordCount * 1000.0) / durationMs : 0.0;
        double p95 = databaseMetrics.getP95Millis(metricName, activeProfile);
        try (FileWriter w = new FileWriter(csv, true)) {
            if (header) {
                w.write("Operacja;Czas[ms];Liczba rekordów;Operacji/s;Profil;p95_ms;DB_operacje;DB_failed_queries;DB_czas_timer_ms\n");
            }
            String line = String.join(";",
                    label, String.valueOf(durationMs), String.valueOf(recordCount),
                    String.valueOf(opsPerSec), activeProfile,
                    String.valueOf(Double.isNaN(p95) ? 0 : p95),
                    String.valueOf(databaseMetrics.getOperationsCount(metricName, activeProfile)),
                    String.valueOf(databaseMetrics.getFailedQueriesCount()),
                    String.valueOf(databaseMetrics.getTotalOperationTimeMillis(metricName, activeProfile)));
            w.write(line + "\n");
        } catch (IOException e) {
            log.error("CSV write error", e);
        }
    }

    private <T> int saveBatch(List<T> buf, java.util.function.Function<List<T>, List<T>> saver) {
        if (buf.isEmpty()) return 0;
        List<T> saved = saver.apply(buf);
        em.flush(); em.clear();
        int n = saved.size();
        buf.clear();
        return n;
    }

    private <T> List<T> sample(Class<T> type, int limit) {
        return em.createQuery("select e from " + type.getSimpleName() + " e", type)
                .setMaxResults(limit)
                .getResultList();
    }

    @Transactional
    public void setUp() {
        initIfNeeded();
        log.info("setUp: TRUNCATE...");
        truncateAll();
        clearLocalCaches();
        log.info("setUp: done.");
    }

    @Transactional
    public void cleanDatabaseAfterAll() {
        log.info("cleanDatabaseAfterAll: TRUNCATE...");
        truncateAll();
        clearLocalCaches();
    }

    @Transactional
    public void testCustomerInsertPerformance() {
        initIfNeeded();
        final String label = "Dodawanie klientów", metric = "customer_insert";
        Timer.Sample timer = databaseMetrics.startTimer();
        long start = System.nanoTime();
        int total = 0;
        try {
            customers.clear();
            List<Customer> buf = new ArrayList<>(batchSize());
            int target = maxRecords(), bs = batchSize();
            for (int i = 0; i < target; i++) {
                buf.add(dataGenerator.generateCustomer());
                if (buf.size() == bs) total += saveBatch(buf, customerRepository::saveAll);
            }
            total += saveBatch(buf, customerRepository::saveAll);
            customers = sample(Customer.class, Math.min(10_000, total));
            databaseMetrics.incrementDatabaseOperations(metric, activeProfile);
            databaseMetrics.recordDataSize("customers", activeProfile, total);
        } catch (Exception e) {
            databaseMetrics.incrementFailedQueries(); throw e;
        } finally {
            databaseMetrics.stopTimer(timer, metric, activeProfile);
        }
        writeCsv(label, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start), total, metric);
    }

    @Transactional
    public void testBrandAndCategoryInsertPerformance() {
        initIfNeeded();
        final String label = "Dodawanie marek i kategorii", metric = "brand_category_insert";
        Timer.Sample timer = databaseMetrics.startTimer();
        long start = System.nanoTime();
        int total = 0;
        try {
            // Brand
            brands.clear();
            List<Brand> bbuf = new ArrayList<>(batchSize());
            int targetBrands = maxRecords(), bs = batchSize();
            for (int i = 0; i < targetBrands; i++) {
                bbuf.add(dataGenerator.generateBrand());
                if (bbuf.size() == bs) total += saveBatch(bbuf, brandRepository::saveAll);
            }
            total += saveBatch(bbuf, brandRepository::saveAll);
            brands = sample(Brand.class, Math.min(10_000, (int) brandRepository.count()));
            databaseMetrics.incrementDatabaseOperations("brand_insert", activeProfile);
            databaseMetrics.recordDataSize("brands", activeProfile, brands.size());

            // Category
            categories.clear();
            List<ProductCategory> cbuf = new ArrayList<>(batchSize());
            for (int i = 0; i < MAX_CATEGORIES; i++) {
                ProductCategory main = dataGenerator.generateCategory(null);
                cbuf.add(main);
                for (int j = 0; j < SUBCATEGORIES_PER_MAIN; j++) cbuf.add(dataGenerator.generateCategory(main));
                if (cbuf.size() >= bs) total += saveBatch(cbuf, categoryRepository::saveAll);
            }
            total += saveBatch(cbuf, categoryRepository::saveAll);
            categories = sample(ProductCategory.class, Math.min(20_000, (int) categoryRepository.count()));
            databaseMetrics.incrementDatabaseOperations("category_insert", activeProfile);
            databaseMetrics.recordDataSize("categories", activeProfile, categories.size());
        } catch (Exception e) {
            databaseMetrics.incrementFailedQueries(); throw e;
        } finally {
            databaseMetrics.stopTimer(timer, metric, activeProfile);
        }
        writeCsv(label, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start), total, metric);
    }

    @Transactional
    public void testProductInsertPerformance() {
        initIfNeeded();
        final String label = "Dodawanie produktów", metric = "product_insert";
        Timer.Sample timer = databaseMetrics.startTimer();
        long start = System.nanoTime();
        int total = 0;
        try {
            List<Product> pbuf = new ArrayList<>(batchSize());
            int target = maxRecords(), bs = batchSize();
            int bsz = Math.max(1, brands.size()), csz = Math.max(1, categories.size());
            for (int i = 0; i < target; i++) {
                Brand brand = brands.get(i % bsz);
                ProductCategory category = categories.get(i % csz);
                pbuf.add(dataGenerator.generateProduct(brand, category));
                if (pbuf.size() == bs) total += saveBatch(pbuf, productRepository::saveAll);
            }
            total += saveBatch(pbuf, productRepository::saveAll);
            products = sample(Product.class, Math.min(20_000, (int) productRepository.count()));
            databaseMetrics.incrementDatabaseOperations(metric, activeProfile);
            databaseMetrics.recordDataSize("products", activeProfile, total);
        } catch (Exception e) {
            databaseMetrics.incrementFailedQueries(); throw e;
        } finally {
            databaseMetrics.stopTimer(timer, metric, activeProfile);
        }
        writeCsv(label, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start), total, metric);
    }

    @Transactional
    public void testOrderInsertPerformance() {
        initIfNeeded();
        final String label = "Dodawanie zamówień", metric = "order_insert";
        Timer.Sample timer = databaseMetrics.startTimer();
        long start = System.nanoTime();
        int total = 0;
        try {
            List<Product> pick = products.isEmpty() ? new ArrayList<>() : products.subList(0, Math.min(5, products.size()));
            List<Order> obuf = new ArrayList<>(batchSize());
            int target = maxRecords(), bs = batchSize(), csz = Math.max(1, customers.size());
            for (int i = 0; i < target; i++) {
                Customer customer = customers.get(i % csz);
                obuf.add(dataGenerator.generateOrder(customer, pick.isEmpty() ? products : pick));
                if (obuf.size() == bs) total += saveBatch(obuf, orderRepository::saveAll);
            }
            total += saveBatch(obuf, orderRepository::saveAll);
            databaseMetrics.incrementDatabaseOperations(metric, activeProfile);
            databaseMetrics.recordDataSize("orders", activeProfile, total);
        } catch (Exception e) {
            databaseMetrics.incrementFailedQueries(); throw e;
        } finally {
            databaseMetrics.stopTimer(timer, metric, activeProfile);
        }
        writeCsv(label, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start), total, metric);
    }

    @Transactional
    public void testProductReviewInsertPerformance() {
        initIfNeeded();
        final String label = "Dodawanie opinii o produktach", metric = "product_review_insert";
        Timer.Sample timer = databaseMetrics.startTimer();
        long start = System.nanoTime();
        int total = 0;
        try {
            List<ProductReview> rbuf = new ArrayList<>(batchSize());
            int target = maxRecords(), bs = batchSize();
            int csz = Math.max(1, customers.size()), psz = Math.max(1, products.size());
            for (int i = 0; i < target; i++) {
                Customer customer = customers.get(i % csz);
                Product product = products.get(i % psz);
                rbuf.add(dataGenerator.generateReview(product, customer));
                if (rbuf.size() == bs) total += saveBatch(rbuf, reviewRepository::saveAll);
            }
            total += saveBatch(rbuf, reviewRepository::saveAll);
            databaseMetrics.incrementDatabaseOperations(metric, activeProfile);
            databaseMetrics.recordDataSize("product_reviews", activeProfile, total);
        } catch (Exception e) {
            databaseMetrics.incrementFailedQueries(); throw e;
        } finally {
            databaseMetrics.stopTimer(timer, metric, activeProfile);
        }
        writeCsv(label, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start), total, metric);
    }

    @Transactional
    public void testInventoryMovementInsertPerformance() {
        initIfNeeded();
        final String label = "Dodawanie ruchów magazynowych", metric = "inventory_movement_insert";
        Timer.Sample timer = databaseMetrics.startTimer();
        long start = System.nanoTime();
        int total = 0;
        try {
            List<InventoryMovement> mbuf = new ArrayList<>(batchSize());
            int target = maxRecords(), bs = batchSize(), psz = Math.max(1, products.size());
            for (int i = 0; i < target; i++) {
                Product product = products.get(i % psz);
                mbuf.add(dataGenerator.generateInventoryMovement(product));
                if (mbuf.size() == bs) total += saveBatch(mbuf, movementRepository::saveAll);
            }
            total += saveBatch(mbuf, movementRepository::saveAll); // <- upewnij się, że to jest mbuf (literówka!)
            databaseMetrics.incrementDatabaseOperations(metric, activeProfile);
            databaseMetrics.recordDataSize("inventory_movements", activeProfile, total);
        } catch (Exception e) {
            databaseMetrics.incrementFailedQueries(); throw e;
        } finally {
            databaseMetrics.stopTimer(timer, metric, activeProfile);
        }
        writeCsv(label, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start), total, metric);
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
