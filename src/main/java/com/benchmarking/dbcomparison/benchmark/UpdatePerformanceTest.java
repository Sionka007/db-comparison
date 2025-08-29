package com.benchmarking.dbcomparison.benchmark;

import com.benchmarking.dbcomparison.config.BenchmarkConfig;
import com.benchmarking.dbcomparison.config.DatabaseMetrics;
import com.benchmarking.dbcomparison.repository.CustomerRepository;
import com.benchmarking.dbcomparison.repository.OrderRepository;
import com.benchmarking.dbcomparison.repository.ProductRepository;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class UpdatePerformanceTest {

    private final DatabaseMetrics databaseMetrics;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final BenchmarkConfig benchmarkConfig;
    private final UpdatePerformanceTest self;

    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;

    public UpdatePerformanceTest(DatabaseMetrics databaseMetrics,
                                 ProductRepository productRepository,
                                 CustomerRepository customerRepository,
                                 OrderRepository orderRepository,
                                 BenchmarkConfig benchmarkConfig,
                                 @Lazy UpdatePerformanceTest self) {
        this.databaseMetrics = databaseMetrics;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.benchmarkConfig = benchmarkConfig;
        this.self = self;
    }

    private boolean isMySql() { return activeProfile != null && activeProfile.toLowerCase().contains("mysql"); }
    private int limit() { return Math.max(1, benchmarkConfig.getRecordCount()); }

    private void writeCsv(String metric, String label, long durMs, int count) {
        File csv = new File("performance-update-results.csv");
        boolean header = !csv.exists() || csv.length() == 0;
        double opsPerSec = durMs > 0 ? (count * 1000.0) / durMs : 0.0;
        double p95 = databaseMetrics.getP95Millis(metric, activeProfile);
        try (FileWriter w = new FileWriter(csv, true)) {
            if (header) {
                w.write("Operacja;Czas[ms];Liczba rekordów;Operacji/s;Profil;p95_ms;DB_operacje;DB_błędy;DB_failed_queries;DB_czas_timer_ms\n");
            }
            String line = String.join(";",
                    label, String.valueOf(durMs), String.valueOf(count),
                    String.valueOf(opsPerSec), activeProfile,
                    String.valueOf(Double.isNaN(p95) ? 0 : p95),
                    String.valueOf(databaseMetrics.getOperationsCount(metric, activeProfile)),
                    String.valueOf(databaseMetrics.getErrorsCount(metric, activeProfile)),
                    String.valueOf(databaseMetrics.getFailedQueriesCount()),
                    String.valueOf(databaseMetrics.getTotalOperationTimeMillis(metric, activeProfile)));
            w.write(line + "\n");
        } catch (IOException e) {
            log.warn("CSV write error: {}", e.getMessage());
        }
    }

    @Transactional
    public void testUpdateProductPrices() {
        final String label = "Aktualizacja cen produktów", metric = "update_products";
        Timer.Sample t = databaseMetrics.startTimer();
        long start = System.nanoTime();
        try {
            int updated = isMySql()
                    ? productRepository.bulkIncreasePriceByPercentMySql(limit(), 10.0)
                    : productRepository.bulkIncreasePriceByPercentPostgres(limit(), 10.0);
            databaseMetrics.incrementDatabaseOperations(metric, activeProfile);
            databaseMetrics.recordDataSize(metric, activeProfile, updated);
            long dur = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            databaseMetrics.recordTransactionTime(dur);
            writeCsv(metric, label, dur, updated);
        } catch (Exception e) {
            databaseMetrics.incrementDatabaseErrors(metric, activeProfile);
            throw e;
        } finally {
            databaseMetrics.stopTimer(t, metric, activeProfile);
        }
    }

    @Transactional
    public void testUpdateCustomerEmails() {
        final String label = "Aktualizacja emaili klientów", metric = "update_customers";
        Timer.Sample t = databaseMetrics.startTimer();
        long start = System.nanoTime();
        try {
            int updated = isMySql()
                    ? customerRepository.bulkUpdateEmailsMySql(limit(), "updated")
                    : customerRepository.bulkUpdateEmailsPostgres(limit(), "updated");
            databaseMetrics.incrementDatabaseOperations(metric, activeProfile);
            databaseMetrics.recordDataSize(metric, activeProfile, updated);
            long dur = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            databaseMetrics.recordTransactionTime(dur);
            writeCsv(metric, label, dur, updated);
        } catch (Exception e) {
            databaseMetrics.incrementDatabaseErrors(metric, activeProfile);
            throw e;
        } finally {
            databaseMetrics.stopTimer(t, metric, activeProfile);
        }
    }

    @Transactional
    public void testUpdateOrderStatus() {
        final String label = "Aktualizacja statusów zamówień", metric = "update_orders";
        Timer.Sample t = databaseMetrics.startTimer();
        long start = System.nanoTime();
        try {
            int updated = isMySql()
                    ? orderRepository.bulkUpdateStatusMySql(limit(), "UPDATED")
                    : orderRepository.bulkUpdateStatusPostgres(limit(), "UPDATED");
            databaseMetrics.incrementDatabaseOperations(metric, activeProfile);
            databaseMetrics.recordDataSize(metric, activeProfile, updated);
            long dur = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            databaseMetrics.recordTransactionTime(dur);
            writeCsv(metric, label, dur, updated);
        } catch (Exception e) {
            databaseMetrics.incrementDatabaseErrors(metric, activeProfile);
            throw e;
        } finally {
            databaseMetrics.stopTimer(t, metric, activeProfile);
        }
    }

    public void runAll() {
        self.testUpdateProductPrices();
        self.testUpdateCustomerEmails();
        self.testUpdateOrderStatus();
    }
}
