package com.benchmarking.dbcomparison.config;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.distribution.ValueAtPercentile;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class DatabaseMetrics {
    private final MeterRegistry meterRegistry;
    private final Counter failedQueriesCounter;
    private final Timer transactionTimer;

    private static final String APPLICATION_TAG = "db-comparison";

    public DatabaseMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.failedQueriesCounter = Counter.builder("db_queries_failed_total")
            .description("Number of failed database queries")
            .register(meterRegistry);
        this.transactionTimer = Timer.builder("db_transaction_duration_seconds")
            .description("Database transaction duration")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);
    }

    public void recordTransactionTime(long timeInMillis) {
        transactionTimer.record(timeInMillis, TimeUnit.MILLISECONDS);
    }

    public void incrementFailedQueries() {
        failedQueriesCounter.increment();
    }

    public void incrementDatabaseOperations(String operation, String database) {
        meterRegistry.counter("db_operations_total",
            "operation", operation,
            "database", database,
            "application", APPLICATION_TAG
        ).increment();
    }

    public void recordDataSize(String tableName, String database, long size) {
        meterRegistry.gauge("db_table_size",
            Tags.of(
                Tag.of("table", tableName),
                Tag.of("database", database),
                Tag.of("application", APPLICATION_TAG)
            ),
            size);
    }

    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    private Timer getOrCreateOperationTimer(String operation, String database) {
        // Ten timer będzie używany przez stopTimer() i do odczytu p95
        return Timer.builder("db_operation_time_seconds")
                .description("DB operation duration")
                .publishPercentiles(0.5, 0.95, 0.99)          // percentyle
                .publishPercentileHistogram(true)              // histogram (lepsza precyzja)
                .tags("operation", operation,
                        "database", database,
                        "application", APPLICATION_TAG)
                .register(meterRegistry);
    }

    public void stopTimer(Timer.Sample sample, String operation, String database) {
        Timer t = getOrCreateOperationTimer(operation, database);
        sample.stop(t);
    }

    public double getP95Millis(String operation, String database) {
        Timer t = meterRegistry.find("db_operation_time_seconds")
                .tag("operation", operation)
                .tag("database", database)
                .timer();
        if (t == null) return Double.NaN;

        var snap = t.takeSnapshot();
        for (ValueAtPercentile v : snap.percentileValues()) {
            if (Math.abs(v.percentile() - 0.95) < 1e-9) {
                return v.value(TimeUnit.MILLISECONDS);
            }
        }
        return Double.NaN;
    }

    // Cache hit ratio
    public void recordCacheHit(String operation, String database, boolean isHit) {
        meterRegistry.counter("db_cache_hits_total",
            "operation", operation,
            "database", database,
            "result", isHit ? "hit" : "miss",
            "application", APPLICATION_TAG
        ).increment();
    }

    // Aktywne połączenia
    public void recordActiveConnections(String database, long connectionCount) {
        meterRegistry.gauge("db_connections_active",
            Tags.of(
                Tag.of("database", database),
                Tag.of("application", APPLICATION_TAG)
            ),
            connectionCount);
    }

    // Aktywne wątki
    public void recordThreadCount(String operation, String database, int threadCount) {
        meterRegistry.gauge("db_threads_active",
            Tags.of(
                Tag.of("operation", operation),
                Tag.of("database", database),
                Tag.of("application", APPLICATION_TAG)
            ),
            threadCount);
    }

    // Czas oczekiwania na blokady
    private Timer createLockWaitTimer(String lockType, String database) {
        return Timer.builder("db_lock_wait_seconds")
            .tags("lock_type", lockType,
                  "database", database,
                  "application", APPLICATION_TAG)
            .register(meterRegistry);
    }

    public void recordLockWaitTime(String lockType, String database, long waitTimeMillis) {
        Timer timer = createLockWaitTimer(lockType, database);
        timer.record(waitTimeMillis, TimeUnit.MILLISECONDS);
    }

    public double getOperationsCount(String operation, String database) {
        Counter counter = meterRegistry.find("db_operations_total")
                .tag("operation", operation)
                .tag("database", database)
                .counter();
        return counter != null ? counter.count() : 0;
    }

    public double getErrorsCount(String operation, String database) {
        Counter counter = meterRegistry.find("db_errors_total")
                .tag("operation", operation)
                .tag("database", database)
                .counter();
        return counter != null ? counter.count() : 0;
    }

    public double getFailedQueriesCount() {
        Counter counter = meterRegistry.find("db_queries_failed_total").counter();
        return counter != null ? counter.count() : 0;
    }

    public double getTotalOperationTimeMillis(String operation, String database) {
        Timer timer = meterRegistry.find("db_operation_time_seconds")
                .tag("operation", operation)
                .tag("database", database)
                .timer();
        return timer != null ? timer.totalTime(TimeUnit.MILLISECONDS) : 0;
    }

    public void recordIndexUsage(String indexName, String database) {
        meterRegistry.counter("db_index_usage_total",
            "index", indexName,
            "database", database,
            "application", APPLICATION_TAG
        ).increment();
    }

    public void incrementDatabaseErrors(String operation, String database) {
        meterRegistry.counter("db_errors_total",
            "operation", operation,
            "database", database,
            "application", APPLICATION_TAG
        ).increment();
    }

    public void recordJoinQueryMetrics(String database, int joinCount, long durationMs) {
        meterRegistry.timer("db_join_query_seconds",
            "database", database,
            "join_count", String.valueOf(joinCount),
            "application", APPLICATION_TAG
        ).record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordCacheMetrics(String operation, String database, boolean isHit) {
        meterRegistry.counter("db_cache_hits_total",
                "operation", operation,
                "database", database,
                "result", isHit ? "hit" : "miss",
                "application", APPLICATION_TAG
        ).increment();
    }
}
