package com.benchmarking.dbcomparison.benchmark.index;

import com.benchmarking.dbcomparison.config.DatabaseMetrics;
import com.benchmarking.dbcomparison.model.Brand;
import com.benchmarking.dbcomparison.model.Product;
import com.benchmarking.dbcomparison.model.ProductCategory;
import com.benchmarking.dbcomparison.repository.BrandRepository;
import com.benchmarking.dbcomparison.repository.ProductCategoryRepository;
import com.benchmarking.dbcomparison.repository.ProductRepository;
import com.benchmarking.dbcomparison.util.DataGenerator;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IndexPerformanceTest {

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private ProductRepository productRepository;
    @Autowired private BrandRepository brandRepository;
    @Autowired private ProductCategoryRepository categoryRepository;
    @Autowired private DatabaseMetrics databaseMetrics;

    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;

    private final DataGenerator dataGenerator = new DataGenerator();

    @BeforeAll
    void prepareData() {
        log.info("Czyszczenie bazy danych i generowanie danych testowych");
        productRepository.deleteAll();
        brandRepository.deleteAll();
        categoryRepository.deleteAll();

        Brand brand = brandRepository.save(dataGenerator.generateBrand());
        ProductCategory category = categoryRepository.save(dataGenerator.generateCategory(null));

        Set<String> usedSkus = new HashSet<>();

        List<Product> products = new ArrayList<>(dataGenerator.generateMixedProducts(brand, category,
                "Pro", "unique", 10000, 0.3));

        productRepository.saveAll(products);

        // Usuwanie indeksów zależnie od bazy
        if ("postgres".equals(activeProfile)) {
            jdbcTemplate.execute("DROP INDEX IF EXISTS idx_product_name");
            jdbcTemplate.execute("DROP INDEX IF EXISTS idx_product_name_category");
            jdbcTemplate.execute("DROP INDEX IF EXISTS idx_product_partial");
            jdbcTemplate.execute("DROP INDEX IF EXISTS idx_product_description_fulltext");
        } else if ("mysql".equals(activeProfile)) {
            dropIndexIfExists("idx_product_name", "product");
            dropIndexIfExists("idx_product_name_category", "product");
            dropIndexIfExists("idx_product_partial", "product");
            dropIndexIfExists("idx_product_description_fulltext", "product");
        }

        log.info("Dane testowe i baza przygotowane.");
    }


    @Test @Order(1)
    void baselineNoIndex() {
        runQueryAndLog("Brak indeksu",
                formatLikeQuery("SELECT * FROM product WHERE name ILIKE '%Pro%'"),
                "none");
    }

    @Test @Order(2)
    void singleIndexTest() {
        createIndex("idx_product_name",
                "CREATE INDEX idx_product_name ON product(name)",
                "CREATE INDEX idx_product_name ON product(name)");

        runQueryAndLog("Indeks pojedynczy (name)",
                formatLikeQuery("SELECT * FROM product WHERE name ILIKE '%Pro%'"),
                "idx_product_name");
    }

    @Test @Order(3)
    void compoundIndexTest() {
        createIndex("idx_product_name_category",
                "CREATE INDEX idx_product_name_category ON product(name, category_id)",
                "CREATE INDEX idx_product_name_category ON product(name, category_id)");

        runQueryAndLog("Indeks złożony (name, category_id)",
                formatLikeQuery("SELECT * FROM product WHERE name ILIKE '%Pro%' AND category_id IS NOT NULL"),
                "idx_product_name_category");
    }

    @Test @Order(4)
    void partialIndexTest() {
        if ("postgres".equals(activeProfile)) {
            jdbcTemplate.execute("CREATE INDEX idx_product_partial ON product(name) WHERE is_available = true");
        } else if ("mysql".equals(activeProfile)) {
            dropIndexIfExists("idx_product_partial", "product"); // w MySQL ignorujemy tworzenie indeksu częściowego
        }

        runQueryAndLog("Indeks częściowy (is_available = true)",
                formatLikeQuery("SELECT * FROM product WHERE is_available = true AND name ILIKE '%Pro%'"),
                "idx_product_partial");
    }

    @Test @Order(5)
    void fulltextIndexTest() {
        if ("postgres".equals(activeProfile)) {
            jdbcTemplate.execute("CREATE INDEX idx_product_description_fulltext ON product USING GIN (to_tsvector('simple', description))");
            runQueryAndLog("Fulltext (PostgreSQL)",
                    "SELECT * FROM product WHERE to_tsvector('simple', description) @@ plainto_tsquery('simple', 'unique')",
                    "idx_product_description_fulltext");
        } else if ("mysql".equals(activeProfile)) {
            jdbcTemplate.execute("CREATE FULLTEXT INDEX idx_product_description_fulltext ON product(description)");
            runQueryAndLog("Fulltext (MySQL)",
                    "SELECT * FROM product WHERE MATCH(description) AGAINST('unique' IN NATURAL LANGUAGE MODE)",
                    "idx_product_description_fulltext");
        }
    }

    private String formatLikeQuery(String sql) {
        if ("mysql".equals(activeProfile)) {
            return sql.replaceAll("ILIKE", "LIKE");
        }
        return sql;
    }

    private void runQueryAndLog(String label, String query, String indexName) {
        log.info("Wykonywanie testu: {}", label);
        Timer.Sample timer = databaseMetrics.startTimer();

        long start = System.nanoTime();
        List<?> results = jdbcTemplate.queryForList(query);
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        databaseMetrics.stopTimer(timer, label, activeProfile);
        databaseMetrics.recordTransactionTime(durationMs);
        databaseMetrics.incrementDatabaseOperations(label, activeProfile);
        databaseMetrics.recordIndexUsage(indexName, activeProfile, results.size());

        saveToCsv(label, query, durationMs, results.size());
    }


    private void saveToCsv(String label, String query, long durationMs, int resultSize) {
        File csvFile = new File("performance-index-results.csv");
        boolean writeHeader = !csvFile.exists() || csvFile.length() == 0;

        try (FileWriter writer = new FileWriter(csvFile, true)) {
            if (writeHeader) {
                writer.write("Typ indeksu,Czas[ms],Liczba wyników,Zapytanie,Profil,db.operations,db.errors,db.queries.failed,db.operation.time\n");
            }

            // Pobieramy metryki z DatabaseMetrics
            double operations = databaseMetrics.getOperationsCount(label, activeProfile);
            double errors = databaseMetrics.getErrorsCount(label, activeProfile);
            double failedQueries = databaseMetrics.getFailedQueriesCount();
            double operationTime = databaseMetrics.getTotalOperationTimeMillis(label, activeProfile);

            writer.write(String.format("%s,%d,%d,\"%s\",%s,%.0f,%.0f,%.0f,%.0f\n",
                    label,
                    durationMs,
                    resultSize,
                    query.replaceAll("\n", " "),
                    activeProfile,
                    operations,
                    errors,
                    failedQueries,
                    operationTime));
        } catch (IOException e) {
            log.error("Błąd zapisu CSV: {}", e.getMessage());
        }
    }


    private void dropIndexIfExists(String indexName, String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM INFORMATION_SCHEMA.STATISTICS WHERE table_schema = DATABASE() AND table_name = ? AND index_name = ?",
                Integer.class, tableName, indexName);

        if (count != null && count > 0) {
            jdbcTemplate.execute("DROP INDEX " + indexName + " ON " + tableName);
        }
    }

    private void createIndex(String indexName, String sqlPostgres, String sqlMysql) {
        if ("postgres".equals(activeProfile)) {
            jdbcTemplate.execute("DROP INDEX IF EXISTS " + indexName);
            jdbcTemplate.execute(sqlPostgres);
        } else if ("mysql".equals(activeProfile)) {
            dropIndexIfExists(indexName, "product");
            jdbcTemplate.execute(sqlMysql);
        }
    }


}
