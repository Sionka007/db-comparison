package com.benchmarking.dbcomparison.util;

import com.benchmarking.dbcomparison.benchmark.DeletePerformanceTest;
import com.benchmarking.dbcomparison.benchmark.InsertPerformanceTest;
import com.benchmarking.dbcomparison.benchmark.ReadPerformanceTest;
import com.benchmarking.dbcomparison.benchmark.UpdatePerformanceTest;
import com.benchmarking.dbcomparison.benchmark.concurrency.MultiThreadedDeleteTest;
import com.benchmarking.dbcomparison.benchmark.concurrency.MultiThreadedInsertTest;
import com.benchmarking.dbcomparison.benchmark.concurrency.MultiThreadedReadTest;
import com.benchmarking.dbcomparison.benchmark.concurrency.MultiThreadedUpdateTest;
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

    @Bean
    public MultiThreadedInsertTest multiThreadedInsertTest() {
        return new MultiThreadedInsertTest();
    }

    @Bean
    public MultiThreadedReadTest multiThreadedReadTest() {
        return new MultiThreadedReadTest();
    }

    @Bean
    public MultiThreadedUpdateTest multiThreadedUpdateTest() {
        return new MultiThreadedUpdateTest();
    }

    @Bean
    public MultiThreadedDeleteTest multiThreadedDeleteTest() {
        return new MultiThreadedDeleteTest();
    }
}
