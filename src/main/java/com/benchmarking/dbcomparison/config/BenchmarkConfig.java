package com.benchmarking.dbcomparison.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "benchmark")
public class BenchmarkConfig {
    private int recordCount = 1000; // domyślna wartość
    private int threads = 10;       // domyślna liczba wątków

    public int getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }
}
