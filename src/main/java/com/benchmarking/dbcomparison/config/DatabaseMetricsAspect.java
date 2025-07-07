package com.benchmarking.dbcomparison.config;

import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
            databaseMetrics.incrementDatabaseErrors("transaction", activeProfile);
            databaseMetrics.incrementFailedQueries();
            throw e;
        }
    }

    @Around("execution(* com.benchmarking.dbcomparison.repository.*.*(..))")
    public Object measureQueryTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String operationType = getOperationType(methodName);
        Timer.Sample timer = databaseMetrics.startTimer();

        try {
            Object result = joinPoint.proceed();
            databaseMetrics.stopTimer(timer, operationType, activeProfile);
            databaseMetrics.incrementDatabaseOperations(operationType, activeProfile);

            // Dodatkowe metryki dla określonych typów operacji
            if (result instanceof Iterable<?> && methodName.startsWith("find")) {
                int count = countResults((Iterable<?>) result);
                databaseMetrics.recordDataSize(getRepositoryName(joinPoint), activeProfile, count);
            }

            return result;
        } catch (Exception e) {
            databaseMetrics.incrementDatabaseErrors(operationType, activeProfile);
            throw e;
        }
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
