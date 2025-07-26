package com.benchmarking.dbcomparison.controller;
import com.benchmarking.dbcomparison.benchmark.*;
import com.benchmarking.dbcomparison.benchmark.concurrency.*;
import com.benchmarking.dbcomparison.benchmark.index.IndexPerformanceTest;
import com.benchmarking.dbcomparison.benchmark.isolation.AcidTest;
import com.benchmarking.dbcomparison.benchmark.isolation.IsolationLevelTest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/benchmark")
@RequiredArgsConstructor
public class BenchmarkController {
    private final InsertPerformanceTest insertPerformanceTest;
    private final ReadPerformanceTest readPerformanceTest;
    private final UpdatePerformanceTest updatePerformanceTest;
    private final DeletePerformanceTest deletePerformanceTest;
    private final AllCrudPerformanceTest allCrudPerformanceTest;
    private final IndexPerformanceTest indexPerformanceTest;
    private final AcidTest acidTest;
    private final IsolationLevelTest isolationLevelTest;
    private final MultiThreadedInsertTest multiThreadedInsertTest;
    private final MultiThreadedReadTest multiThreadedReadTest;
    private final MultiThreadedUpdateTest multiThreadedUpdateTest;
    private final MultiThreadedDeleteTest multiThreadedDeleteTest;
    private final MultiThreadedCrudTest multiThreadedCrudTest;


// --------------- CRUD Tests ----------------
    @GetMapping("/insert")
    public String runInsertTest() {
        insertPerformanceTest.runAll();
        return "Test insert zakończony.";
    }

    @GetMapping("/read")
    public String runReadTest() {
        readPerformanceTest.runAll();
        return "Test read zakończony.";
    }

    @GetMapping("/update")
    public String runUpdateTest() {
        updatePerformanceTest.runAll();
        return "Test update zakończony.";
    }

    @GetMapping("/delete")
    public String runDeleteTest() {
        deletePerformanceTest.runAll();
        return "Test delete zakończony.";
    }

    @GetMapping("/allCrudTests")
    public String runAllCrudTests() {
        allCrudPerformanceTest.runAllTests();
        return "Wszystkie testy CRUD zakończone.";
    }

// --------------- Index Tests ----------------
    @GetMapping("/index")
    public String runIndexTest() {
        indexPerformanceTest.runAllTests();
        return "Wszystkie testy indexów zakończone.";
    }

// --------------- Isolation Tests ----------------
    @GetMapping("/acid")
    public String runAcidTest() throws InterruptedException {
        acidTest.runAllTests();
        return "Wszystkie testy ACID zakończone.";
    }

    @GetMapping("/isolation")
    public String runIsolationLevelTest() throws InterruptedException {
        isolationLevelTest.runAllTests();
        return "Wszystkie testy poziomów izolacji zakończone.";
    }

// --------------- Multi-Threaded CRUD Tests ----------------
    @GetMapping("/multiThreadedInsert")
    public String runMultiThreadedInsertTest() throws InterruptedException {
        multiThreadedInsertTest.runAllTests();
        return "Wielowątkowy test INSERT zakończony.";
    }

    @GetMapping("/multiThreadedRead")
    public String runMultiThreadedReadTest() throws InterruptedException {
        multiThreadedReadTest.runAllTests();
        return "Wielowątkowy test READ zakończony.";
    }

    @GetMapping("/multiThreadedUpdate")
    public String runMultiThreadedUpdateTest() throws InterruptedException {
        multiThreadedUpdateTest.runAllTests();
        return "Wielowątkowy test UPDATE zakończony.";
    }

    @GetMapping("/multiThreadedDelete")
    public String runMultiThreadedDeleteTest() throws InterruptedException {
        multiThreadedDeleteTest.runAllTests();
        return "Wielowątkowy test DELETE zakończony.";
    }

    @GetMapping("/multiThreadedCrud")
    public String runMultiThreadedCrudTests() throws InterruptedException {
        multiThreadedCrudTest.runAllTests();
        return "Wszystkie wielowątkowe testy CRUD zakończone.";
    }



}
