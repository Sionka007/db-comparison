package com.benchmarking.dbcomparison.benchmark;

import com.benchmarking.dbcomparison.config.BenchmarkConfig;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ReadPerformanceTest {

    @Autowired private DatabaseMetrics databaseMetrics;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private BenchmarkConfig benchmarkConfig;

    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;

    private void writeCsv(String metric, String label, long durMs, int count) {
        File csv = new File("performance-read-results.csv");
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

    private int pageSize() {
        return Math.min(10_000, Math.max(500, benchmarkConfig.getRecordCount()));
    }

    @Transactional(readOnly = true)
    public void testReadAllCustomers() {
        final String label = "Odczyt klientów", metric = "read_customers";
        log.info("Start: {}", label);
        customerRepository.findAll(PageRequest.of(0, Math.min(100, pageSize()))); // warm-up
        Timer.Sample t = databaseMetrics.startTimer();
        long start = System.nanoTime();
        int total = 0;
        try {
            int page = 0, size = pageSize();
            while (true) {
                Page<Customer> p = customerRepository.findAll(PageRequest.of(page, size));
                total += p.getNumberOfElements();
                databaseMetrics.incrementDatabaseOperations(metric, activeProfile);
                if (!p.hasNext()) break;
                page++;
            }
            databaseMetrics.recordDataSize(metric, activeProfile, total);
        } finally {
            databaseMetrics.stopTimer(t, metric, activeProfile);
        }
        writeCsv(metric, label, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start), total);
    }

    @Transactional(readOnly = true)
    public void testReadAllProducts() {
        final String label = "Odczyt produktów", metric = "read_products";
        log.info("Start: {}", label);
        productRepository.findAll(PageRequest.of(0, Math.min(100, pageSize()))); // warm-up
        Timer.Sample t = databaseMetrics.startTimer();
        long start = System.nanoTime();
        int total = 0;
        try {
            int page = 0, size = pageSize();
            while (true) {
                Page<Product> p = productRepository.findAll(PageRequest.of(page, size));
                total += p.getNumberOfElements();
                databaseMetrics.incrementDatabaseOperations(metric, activeProfile);
                if (!p.hasNext()) break;
                page++;
            }
            databaseMetrics.recordDataSize(metric, activeProfile, total);
        } finally {
            databaseMetrics.stopTimer(t, metric, activeProfile);
        }
        writeCsv(metric, label, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start), total);
    }

    @Transactional(readOnly = true)
    public void testReadAllOrdersWithJoins() {
        final String label = "Odczyt zamówień (z JOIN)", metric = "read_orders_with_joins";
        log.info("Start: {}", label);
        Timer.Sample t = databaseMetrics.startTimer();
        long start = System.nanoTime();
        int total = 0;
        try {
            int page = 0, size = Math.min(2000, pageSize());
            while (true) {
                List<UUID> ids = orderRepository.findOrderIdsPage(PageRequest.of(page, size));
                if (ids.isEmpty()) break;
                List<Order> chunk = orderRepository.findAllWithCustomerAndItemsByIdIn(ids);
                total += chunk.size();
                databaseMetrics.incrementDatabaseOperations(metric, activeProfile);
                databaseMetrics.recordDataSize(metric, activeProfile, chunk.size());
                page++;
            }
            long durMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            databaseMetrics.recordJoinQueryMetrics(activeProfile, 3, durMs);
        } finally {
            databaseMetrics.stopTimer(t, metric, activeProfile);
        }
        writeCsv(metric, label, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start), total);
    }

    @Transactional(readOnly = true)
    public void testReadTopRatedProducts() {
        final String label = "Odczyt TOP produktów", metric = "read_top_products";
        log.info("Start: {}", label);
        productRepository.findTop100ByOrderByRatingDesc(); // warm-up
        Timer.Sample t = databaseMetrics.startTimer();
        long start = System.nanoTime();
        int size;
        try {
            size = productRepository.findTop100ByOrderByRatingDesc().size();
            databaseMetrics.incrementDatabaseOperations(metric, activeProfile);
            databaseMetrics.recordDataSize(metric, activeProfile, size);
        } finally {
            databaseMetrics.stopTimer(t, metric, activeProfile);
        }
        writeCsv(metric, label, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start), size);
    }

    public void runAll() {
        testReadAllCustomers();
        testReadAllProducts();
        testReadAllOrdersWithJoins();
        testReadTopRatedProducts();
    }
}
