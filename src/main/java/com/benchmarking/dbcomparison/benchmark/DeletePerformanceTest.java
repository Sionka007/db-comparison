package com.benchmarking.dbcomparison.benchmark;

import com.benchmarking.dbcomparison.config.BenchmarkConfig;
import com.benchmarking.dbcomparison.config.DatabaseMetrics;
import com.benchmarking.dbcomparison.repository.*;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Component
public class DeletePerformanceTest {

    private final DatabaseMetrics databaseMetrics;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductReviewRepository reviewRepository;
    private final InventoryMovementRepository movementRepository;
    private final CustomerRepository customerRepository;
    private final BrandRepository brandRepository;
    private final ProductCategoryRepository categoryRepository;
    private final BenchmarkConfig benchmarkConfig;
    private final DeletePerformanceTest self;

    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;

    public DeletePerformanceTest(DatabaseMetrics databaseMetrics,
                                 OrderItemRepository orderItemRepository,
                                 OrderRepository orderRepository,
                                 ProductRepository productRepository,
                                 ProductReviewRepository reviewRepository,
                                 InventoryMovementRepository movementRepository,
                                 CustomerRepository customerRepository,
                                 BrandRepository brandRepository,
                                 ProductCategoryRepository categoryRepository,
                                 BenchmarkConfig benchmarkConfig,
                                 @Lazy DeletePerformanceTest self) {
        this.databaseMetrics = databaseMetrics;
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
        this.movementRepository = movementRepository;
        this.customerRepository = customerRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.benchmarkConfig = benchmarkConfig;
        this.self = self;
    }

    private boolean isMySql()    { return activeProfile != null && activeProfile.toLowerCase().contains("mysql"); }
    private boolean isPostgres() { return activeProfile != null && activeProfile.toLowerCase().contains("postgres"); }

    private int limit() { return Math.max(1, benchmarkConfig.getRecordCount()); }
    private int chunkSize() { return Math.max(2_000, Math.min(limit(), benchmarkConfig.getBatchSize())); }

    private void writeCsv(String metric, String label, long durMs, int count) {
        File csv = new File("performance-delete-results.csv");
        boolean header = !csv.exists() || csv.length() == 0;
        double opsPerSec = durMs > 0 ? (count * 1000.0) / durMs : 0.0;
        double p95 = databaseMetrics.getP95Millis(metric, activeProfile);

        try (FileWriter w = new FileWriter(csv, true)) {
            if (header) {
                // identyczny układ i nazwy kolumn jak w przykładzie:
                w.write("Operacja;Czas[ms];Liczba rekordów;Operacji/s;Profil;p95_ms;DB_operacje;DB_failed_queries;DB_czas_timer_ms\n");
            }
            String line = String.join(";",
                    label,
                    String.valueOf(durMs),
                    String.valueOf(count),
                    String.valueOf(opsPerSec),
                    activeProfile,
                    String.valueOf(Double.isNaN(p95) ? 0 : p95),
                    String.valueOf(databaseMetrics.getOperationsCount(metric, activeProfile)),
                    String.valueOf(databaseMetrics.getFailedQueriesCount()),
                    String.valueOf(databaseMetrics.getTotalOperationTimeMillis(metric, activeProfile))
            );
            w.write(line + "\n");
        } catch (IOException e) {
            log.warn("CSV write error for {}: {}", metric, e.getMessage());
        }
    }


    /** Jeden chunk w osobnej transakcji (krótko trzymanej). Wołaj przez self.* żeby zadziałał proxy. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected int doBulk(String label, String metric, Supplier<Integer> action) {
        log.info("Start: {}", label);
        Timer.Sample t = databaseMetrics.startTimer();
        long start = System.nanoTime();
        try {
            int affected = action.get();
            databaseMetrics.incrementDatabaseOperations(metric, activeProfile);
            databaseMetrics.recordDataSize(metric, activeProfile, affected);
            long dur = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            databaseMetrics.recordTransactionTime(dur);
            writeCsv(metric, label, dur, affected);
            return affected;
        } catch (Exception e) {
            databaseMetrics.incrementDatabaseErrors(metric, activeProfile);
            databaseMetrics.incrementFailedQueries();
            throw e;
        } finally {
            databaseMetrics.stopTimer(t, metric, activeProfile);
        }
    }

    /* --- dispatch do metod repo, zgodnie z profilem --- */
    private int delOrderItemsTopN(int lim) {
        if (isMySql())    return orderItemRepository.deleteTopNMySql(lim);
        if (isPostgres()) return orderItemRepository.deleteTopNPostgres(lim);
        return orderItemRepository.deleteTopNPostgres(lim);
    }
    private int delOrderItemsForSameOrders(int lim) {
        if (isMySql())    return orderItemRepository.deleteTopNMySql(lim);
        if (isPostgres()) return orderItemRepository.deleteTopNPostgres(lim);
        return 0;
    }
    private int delOrdersTopN(int lim) {
        if (isMySql())    return orderRepository.deleteTopNMySql(lim);
        if (isPostgres()) return orderRepository.deleteTopNPostgres(lim);
        return orderRepository.deleteTopNPostgres(lim);
    }
    private int delProductReviewsTopN(int lim) {
        return isMySql() ? reviewRepository.deleteTopNMySql(lim)
                : reviewRepository.deleteTopNPostgres(lim);
    }
    private int delInventoryMovementsTopN(int lim) {
        return isMySql() ? movementRepository.deleteTopNMySql(lim)
                : movementRepository.deleteTopNPostgres(lim);
    }
    private int delProductsTopN(int lim) {
        return isMySql() ? productRepository.deleteTopNMySql(lim)
                : productRepository.deleteTopNPostgres(lim);
    }
    private int delCustomersTopN(int lim) {
        return isMySql() ? customerRepository.deleteTopNMySql(lim)
                : customerRepository.deleteTopNPostgres(lim);
    }
    private int delBrandsTopN(int lim) {
        return isMySql() ? brandRepository.deleteTopNMySql(lim)
                : brandRepository.deleteTopNPostgres(lim);
    }
    private int delCategoriesTopN(int lim) {
        return isMySql() ? categoryRepository.deleteTopNMySql(lim)
                : categoryRepository.deleteTopNPostgres(lim);
    }

    /* --- testy: bez @Transactional na metodach pętli, transakcje tylko na chunk --- */
    public void testDeleteOrderItems() {
        final int target = limit(), bite = chunkSize();
        int left = target, total = 0;
        while (left > 0) {
            final int currentLimit = Math.min(bite, left);
            int n = self.doBulk("Usuwanie pozycji zamówień (chunk " + currentLimit + ")", "delete_order_items",
                    () -> delOrderItemsTopN(currentLimit));
            if (n == 0) break; total += n; left -= n;
        }
        log.info("Usunięto łącznie {} pozycji zamówień", total);
    }

    public void testDeleteOrders() {
        final int target = limit(), bite = chunkSize();
        int left = target, total = 0;
        while (left > 0) {
            final int currentLimit = Math.min(bite, left);
            self.doBulk("Usuwanie pozycji tych zamówień (chunk " + currentLimit + ")",
                    "delete_order_items_for_orders",
                    () -> delOrderItemsForSameOrders(currentLimit));
            int nOrders = self.doBulk("Usuwanie zamówień (chunk " + currentLimit + ")",
                    "delete_orders",
                    () -> delOrdersTopN(currentLimit));
            if (nOrders == 0) break; total += nOrders; left -= nOrders;
        }
        log.info("Usunięto łącznie {} zamówień", total);
    }

    public void testDeleteProductReviews() {
        final int target = limit(), bite = chunkSize();
        int left = target, total = 0;
        while (left > 0) {
            final int currentLimit = Math.min(bite, left);
            int n = self.doBulk("Usuwanie opinii produktów (chunk " + currentLimit + ")", "delete_product_reviews",
                    () -> delProductReviewsTopN(currentLimit));
            if (n == 0) break; total += n; left -= n;
        }
        log.info("Usunięto łącznie {} opinii produktów", total);
    }

    public void testDeleteInventoryMovements() {
        final int target = limit(), bite = chunkSize();
        int left = target, total = 0;
        while (left > 0) {
            final int currentLimit = Math.min(bite, left);
            int n = self.doBulk("Usuwanie ruchów magazynowych (chunk " + currentLimit + ")", "delete_inventory_movements",
                    () -> delInventoryMovementsTopN(currentLimit));
            if (n == 0) break; total += n; left -= n;
        }
        log.info("Usunięto łącznie {} ruchów magazynowych", total);
    }

    public void testDeleteProducts() {
        final int target = limit(), bite = chunkSize();
        int left = target, total = 0;
        while (left > 0) {
            final int currentLimit = Math.min(bite, left);
            self.doBulk("Usuwanie opinii (pod produkty) – chunk " + currentLimit, "delete_product_reviews",
                    () -> delProductReviewsTopN(currentLimit));
            self.doBulk("Usuwanie ruchów magazynowych (pod produkty) – chunk " + currentLimit, "delete_inventory_movements",
                    () -> delInventoryMovementsTopN(currentLimit));
            int n = self.doBulk("Usuwanie produktów (chunk " + currentLimit + ")", "delete_products",
                    () -> delProductsTopN(currentLimit));
            if (n == 0) break; total += n; left -= n;
        }
        log.info("Usunięto łącznie {} produktów", total);
    }

    public void testDeleteCustomers() {
        final int target = limit(), bite = chunkSize();
        int left = target, total = 0;
        while (left > 0) {
            final int currentLimit = Math.min(bite, left);
            int n = self.doBulk("Usuwanie klientów (chunk " + currentLimit + ")", "delete_customers",
                    () -> delCustomersTopN(currentLimit));
            if (n == 0) break; total += n; left -= n;
        }
        log.info("Usunięto łącznie {} klientów", total);
    }

    public void testDeleteBrands() {
        final int currentLimit = chunkSize();
        self.doBulk("Usuwanie marek (chunk " + currentLimit + ")", "delete_brands",
                () -> delBrandsTopN(currentLimit));
    }

    public void testDeleteCategories() {
        final int currentLimit = chunkSize();
        self.doBulk("Usuwanie kategorii (chunk " + currentLimit + ")", "delete_categories",
                () -> delCategoriesTopN(currentLimit));
    }

    public void runAll() {
        self.testDeleteOrderItems();
//        self.testDeleteOrders();
        self.testDeleteProductReviews();
        self.testDeleteInventoryMovements();
//        self.testDeleteProducts();
//        self.testDeleteCustomers();
        // opcjonalnie:
        // self.testDeleteBrands();
        // self.testDeleteCategories();
    }
}
