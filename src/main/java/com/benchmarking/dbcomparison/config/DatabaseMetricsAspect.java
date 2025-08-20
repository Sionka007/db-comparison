package com.benchmarking.dbcomparison.config;

import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Aspect
@Component
public class DatabaseMetricsAspect {

    private final DatabaseMetrics databaseMetrics;

    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;

    public DatabaseMetricsAspect(DatabaseMetrics databaseMetrics) {
        this.databaseMetrics = databaseMetrics;
    }

    @Around("@annotation(transactional)")
    public Object measureTransactionTime(ProceedingJoinPoint joinPoint, Transactional transactional) throws Throwable {
        Timer.Sample timer = databaseMetrics.startTimer();
        String operationType = joinPoint.getSignature().getName();

        try {
            Object result = joinPoint.proceed();
            databaseMetrics.stopTimer(timer, "transaction", activeProfile);
            databaseMetrics.incrementDatabaseOperations("transaction", activeProfile);
            return result;
        } catch (Exception e) {
            databaseMetrics.incrementFailedQueries();
            throw e;
        }
    }

    @Around("execution(* com.benchmarking.dbcomparison.repository.*.*(..))")
    public Object measureQueryTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String operationType = getOperationType(methodName);
        Timer.Sample timer = databaseMetrics.startTimer();
        String repositoryName = getRepositoryName(joinPoint);

        try {
            // Pomiar aktywnych połączeń i wątków przed wykonaniem operacji
            updateConnectionMetrics();
            databaseMetrics.recordThreadCount(operationType, activeProfile, Thread.activeCount());

            Object result = joinPoint.proceed();
            databaseMetrics.stopTimer(timer, operationType, activeProfile);
            databaseMetrics.incrementDatabaseOperations(operationType, activeProfile);

            // Pomiar rozmiaru danych
            if (result instanceof Iterable<?>) {
                int count = countResults((Iterable<?>) result);
                databaseMetrics.recordDataSize(repositoryName, activeProfile, count);
            }

            // Cache hit ratio dla operacji SELECT
            if (isSelectOperation(operationType)) {
                measureCacheEffectiveness(methodName, result);
            }

            // Czas oczekiwania na blokady dla operacji modyfikujących
            if (isModifyingOperation(operationType)) {
                measureLockWaitTime(operationType);
            }

            return result;
        } catch (Exception e) {
            databaseMetrics.incrementFailedQueries();
            throw e;
        }
    }

    private void updateConnectionMetrics() {
        try {
            // Pobieranie informacji o połączeniach z HikariCP
            long activeConnections = getHikariActiveConnections();
            databaseMetrics.recordActiveConnections(activeProfile, activeConnections);
        } catch (Exception e) {
            // Ignoruj błędy - nie chcemy przerywać głównej operacji
            // log.warn("Nie udało się pobrać statystyk połączeń", e);
        }
    }

    private long getHikariActiveConnections() {
        // Tutaj można dodać integrację z HikariCP metrics
        return 10; // Tymczasowa wartość
    }

    private void measureCacheEffectiveness(String methodName, Object result) {
        boolean isCacheHit = isCacheHit(result);
        databaseMetrics.recordCacheHit(methodName, activeProfile, isCacheHit);
    }

    private boolean isCacheHit(Object result) {
        if (result instanceof Iterable<?>) {
            return countResults((Iterable<?>) result) > 0;
        }
        return result != null;
    }

    private void measureLockWaitTime(String operationType) {
        // Symulacja czasu oczekiwania na blokady
        long waitTime = estimateLockWaitTime();
        databaseMetrics.recordLockWaitTime(operationType, activeProfile, waitTime);
    }

    private long estimateLockWaitTime() {
        // W rzeczywistej implementacji należałoby pobrać te dane z bazy
        return ThreadLocalRandom.current().nextLong(1, 100);
    }

    private boolean isSelectOperation(String operationType) {
        return "SELECT".equals(operationType);
    }

    private boolean isModifyingOperation(String operationType) {
        return Arrays.asList("INSERT", "UPDATE", "DELETE").contains(operationType);
    }

    private String getOperationType(String methodName) {
        if (methodName.startsWith("find") || methodName.startsWith("get")) return "SELECT";
        if (methodName.startsWith("save") || methodName.startsWith("insert")) return "INSERT";
        if (methodName.startsWith("update")) return "UPDATE";
        if (methodName.startsWith("delete") || methodName.startsWith("remove")) return "DELETE";
        if (methodName.contains("Join")) return "JOIN";
        return "OTHER";
    }

    private String getRepositoryName(ProceedingJoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        return className.replace("Repository", "").toLowerCase();
    }

    private int countResults(Iterable<?> results) {
        int count = 0;
        for (Object o : results) count++;
        return count;
    }
}
