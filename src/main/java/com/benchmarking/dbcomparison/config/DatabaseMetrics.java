package com.benchmarking.dbcomparison.config;

import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class DatabaseMetrics {
    private final MeterRegistry meterRegistry;
    private final Counter failedQueriesCounter;
    private final Timer transactionTimer;

    public DatabaseMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.failedQueriesCounter = Counter.builder("db.queries.failed")
            .description("Number of failed database queries")
            .register(meterRegistry);
        this.transactionTimer = Timer.builder("db.transaction.duration")
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

    // Metryki dla operacji bazodanowych
    public void incrementDatabaseOperations(String operation, String database) {
        meterRegistry.counter("db.operations",
            "operation", operation,
            "database", database
        ).increment();
    }

    // Metryki rozmiaru danych
    public void recordDataSize(String tableName, String database, long size) {
        meterRegistry.gauge("db.table.size",
            Tags.of("table", tableName, "database", database),
            size);
    }

    // Timer dla operacji
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopTimer(Timer.Sample sample, String operation, String database) {
        sample.stop(meterRegistry.timer("db.operation.time",
            "operation", operation,
            "database", database
        ));
    }

    // Metryki błędów
    public void incrementDatabaseErrors(String operation, String database) {
        meterRegistry.counter("db.errors",
            "operation", operation,
            "database", database
        ).increment();
    }

    // Metryki wykorzystania indeksów
    public void recordIndexUsage(String indexName, String database, long usageCount) {
        meterRegistry.gauge("db.index.usage",
            Tags.of("index", indexName, "database", database),
            usageCount);
    }

    // Metryki cache
    public void recordCacheMetrics(String operation, String database, boolean cacheHit) {
        meterRegistry.counter("db.cache.hits",
            "operation", operation,
            "database", database,
            "result", cacheHit ? "hit" : "miss"
        ).increment();
    }

    // Metryki blokad
    public void recordLockWaitTime(String lockType, String database, long waitTimeMillis) {
        meterRegistry.timer("db.lock.wait",
            "lock_type", lockType,
            "database", database
        ).record(waitTimeMillis, TimeUnit.MILLISECONDS);
    }

    // Metryki połączeń
    public void recordActiveConnections(String database, long connectionCount) {
        meterRegistry.gauge("db.connections.active",
            Tags.of("database", database),
            connectionCount);
    }

    // Metryki zapytań złożonych
    public void recordJoinQueryMetrics(String database, int tableCount, long duration) {
        meterRegistry.timer("db.join.duration",
            "database", database,
            "tables", String.valueOf(tableCount)
        ).record(duration, TimeUnit.MILLISECONDS);
    }

    // Metryki wielooperacyjności
    public void recordThreadCount(String operation, String database, int threadCount) {
        meterRegistry.gauge("db.threads.active",
                Tags.of("operation", operation, "database", database),
                threadCount);
    }

    public double getOperationsCount(String operation, String database) {
        Counter counter = meterRegistry.find("db.operations")
                .tag("operation", operation)
                .tag("database", database)
                .counter();
        return counter != null ? counter.count() : 0;
    }

    public double getErrorsCount(String operation, String database) {
        Counter counter = meterRegistry.find("db.errors")
                .tag("operation", operation)
                .tag("database", database)
                .counter();
        return counter != null ? counter.count() : 0;
    }

    public double getFailedQueriesCount() {
        Counter counter = meterRegistry.find("db.queries.failed").counter();
        return counter != null ? counter.count() : 0;
    }

    public double getTotalOperationTimeMillis(String operation, String database) {
        Timer timer = meterRegistry.find("db.operation.time")
                .tag("operation", operation)
                .tag("database", database)
                .timer();
        return timer != null ? timer.totalTime(TimeUnit.MILLISECONDS) : 0;
    }

}
