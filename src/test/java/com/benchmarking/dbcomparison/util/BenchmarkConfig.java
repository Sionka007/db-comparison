package com.benchmarking.dbcomparison.util;

import com.benchmarking.dbcomparison.benchmark.DeletePerformanceTest;
import com.benchmarking.dbcomparison.benchmark.InsertPerformanceTest;
import com.benchmarking.dbcomparison.benchmark.ReadPerformanceTest;
import com.benchmarking.dbcomparison.benchmark.UpdatePerformanceTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BenchmarkConfig {
    @Bean
    public InsertPerformanceTest insertPerformanceTest() {
        return new InsertPerformanceTest();
    }

    @Bean
    public ReadPerformanceTest readPerformanceTest() {
        return new ReadPerformanceTest();
    }

    @Bean
    public UpdatePerformanceTest updatePerformanceTest() {
        return new UpdatePerformanceTest();
    }

    @Bean
    public DeletePerformanceTest deletePerformanceTest() {
        return new DeletePerformanceTest();
    }
}
