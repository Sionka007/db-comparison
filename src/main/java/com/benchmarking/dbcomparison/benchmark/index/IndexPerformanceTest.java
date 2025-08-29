package com.benchmarking.dbcomparison.benchmark.index;

import com.benchmarking.dbcomparison.config.BenchmarkConfig;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class IndexPerformanceTest {

    private final DataGenerator dataGenerator = new DataGenerator();

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private ProductRepository productRepository;
    @Autowired private BrandRepository brandRepository;
    @Autowired private ProductCategoryRepository categoryRepository;
    @Autowired private DatabaseMetrics databaseMetrics;
    @Autowired private BenchmarkConfig benchmarkConfig;

    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;

    /* ------------ helpers ------------ */

    private boolean isMySql() { return activeProfile != null && activeProfile.toLowerCase().contains("mysql"); }
    private boolean isPostgres() { return activeProfile != null && activeProfile.toLowerCase().contains("postgres"); }

    private String likeDialect(String sql) {
        // ILIKE tylko w PG
        return isMySql() ? sql.replace("ILIKE", "LIKE") : sql;
    }

    /* ------------ data setup ------------ */

    void prepareData() {
        log.info("Czyszczenie bazy danych i generowanie danych testowych...");
        productRepository.deleteAll();
        brandRepository.deleteAll();
        categoryRepository.deleteAll();

        Brand brand = brandRepository.save(dataGenerator.generateBrand());
        ProductCategory category = categoryRepository.save(dataGenerator.generateCategory(null));

        List<Product> products = new ArrayList<>(dataGenerator.generateMixedProducts(
                brand, category, "Pro", "unique", benchmarkConfig.getRecordCount(), 0.3
        ));
        productRepository.saveAll(products);

        // DROP indeksów
        if (isPostgres()) {
            jdbcTemplate.execute("DROP INDEX IF EXISTS idx_product_name");
            jdbcTemplate.execute("DROP INDEX IF EXISTS idx_product_name_category");
            jdbcTemplate.execute("DROP INDEX IF EXISTS idx_product_partial");
            jdbcTemplate.execute("DROP INDEX IF EXISTS idx_product_description_fulltext");
            jdbcTemplate.execute("DROP INDEX IF EXISTS idx_product_name_trgm");
        } else if (isMySql()) {
            dropIndexIfExists("idx_product_name", "product");
            dropIndexIfExists("idx_product_name_category", "product");
            dropIndexIfExists("idx_product_partial", "product");
            dropIndexIfExists("idx_product_description_fulltext", "product");
        }

        log.info("Dane testowe i baza przygotowane.");
    }

    /* ------------ tests ------------ */

    void baselineNoIndex() {
        runQueryAndLog(
                "Brak indeksu",
                likeDialect("SELECT * FROM product WHERE name ILIKE '%Pro%'"),
                "baseline_no_index"
        );
    }

    void singleIndexTest() {
        createIndex("idx_product_name",
                "CREATE INDEX idx_product_name ON product(name)",
                "CREATE INDEX idx_product_name ON product(name)");

        runQueryAndLog(
                "Indeks pojedynczy (name)",
                likeDialect("SELECT * FROM product WHERE name ILIKE '%Pro%'"),
                "single_idx_name"
        );
    }

    void compoundIndexTest() {
        createIndex("idx_product_name_category",
                "CREATE INDEX idx_product_name_category ON product(name, category_id)",
                "CREATE INDEX idx_product_name_category ON product(name, category_id)");

        runQueryAndLog(
                "Indeks złożony (name, category_id)",
                likeDialect("SELECT * FROM product WHERE name ILIKE '%Pro%' AND category_id IS NOT NULL"),
                "compound_idx_name_category"
        );
    }

    void partialIndexTest() {
        if (isPostgres()) {
            jdbcTemplate.execute("CREATE INDEX idx_product_partial ON product(name) WHERE is_available = true");
        } else if (isMySql()) {
            // brak partial w MySQL – pomijamy tworzenie
            dropIndexIfExists("idx_product_partial", "product");
        }

        runQueryAndLog(
                "Indeks częściowy (is_available = true)",
                likeDialect("SELECT * FROM product WHERE is_available = true AND name ILIKE '%Pro%'"),
                "partial_idx_available_name"
        );
    }

    void fulltextIndexTest() {
        if (isPostgres()) {
            jdbcTemplate.execute("CREATE INDEX idx_product_description_fulltext " +
                    "ON product USING GIN (to_tsvector('simple', coalesce(description,'')))");
            runQueryAndLog(
                    "Fulltext (PostgreSQL)",
                    "SELECT * FROM product WHERE to_tsvector('simple', coalesce(description,'')) @@ plainto_tsquery('simple', 'unique')",
                    "fulltext_pg_description"
            );
        } else if (isMySql()) {
            jdbcTemplate.execute("CREATE FULLTEXT INDEX idx_product_description_fulltext ON product(description)");
            runQueryAndLog(
                    "Fulltext (MySQL)",
                    "SELECT * FROM product WHERE MATCH(description) AGAINST('unique' IN NATURAL LANGUAGE MODE)",
                    "fulltext_mysql_description"
            );
        }
    }

    /** LIKE '%...%' w PG — preferowany GIN (pg_trgm); fallback na GiST, jeśli gin_trgm_ops nie jest dostępny. */
    void trigramLikeContainsTestIfPg() {
        if (!isPostgres()) return;

        // 1) upewnij się, że rozszerzenie jest, próbuj w public
        try {
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS pg_trgm");
        } catch (Exception e) {
            log.warn("CREATE EXTENSION pg_trgm (bez schematu) nie powiodło się: {}", e.getMessage());
        }
        try {
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS pg_trgm WITH SCHEMA public");
        } catch (Exception e) {
            log.warn("CREATE EXTENSION pg_trgm WITH SCHEMA public nie powiodło się: {}", e.getMessage());
        }

        // 2) czy istnieje klasa operatorów GIN?
        boolean hasGinTrgm = hasOpClass("gin_trgm_ops", "gin");
        boolean hasGistTrgm = hasOpClass("gist_trgm_ops", "gist");

        // 3) drop i twórz odpowiedni indeks
        jdbcTemplate.execute("DROP INDEX IF EXISTS idx_product_name_trgm");
        try {
            if (hasGinTrgm) {
                jdbcTemplate.execute("CREATE INDEX idx_product_name_trgm ON product USING GIN (name gin_trgm_ops)");
                log.info("Utworzono GIN trigram index (gin_trgm_ops).");
            } else if (hasGistTrgm) {
                jdbcTemplate.execute("CREATE INDEX idx_product_name_trgm ON product USING GIST (name gist_trgm_ops)");
                log.info("Utworzono GiST trigram index (gist_trgm_ops) – fallback.");
            } else {
                log.warn("Brak dostępnych klas operatorów trgm (GIN/GiST). Pomijam test trigram.");
                return;
            }
        } catch (Exception e) {
            databaseMetrics.incrementFailedQueries();
            log.error("Nie udało się utworzyć indeksu trigram: {}", e.getMessage());
            return;
        }

        runQueryAndLog(
                "Trigram (LIKE '%...%')",
                "SELECT * FROM product WHERE name ILIKE '%Pro%'",
                "trgm_idx_name_like_contains"
        );
    }

    /* ------------ execution + CSV ------------ */

    private void runQueryAndLog(String label, String sql, String metricKey) {
        log.info("Wykonywanie testu: {} | SQL: {}", label, sql);

        // Warm-up (bez pomiaru)
        try {
            jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            databaseMetrics.incrementFailedQueries();
            log.warn("Warm-up error ({}): {}", label, e.getMessage());
        }

        Timer.Sample t = databaseMetrics.startTimer();
        long startNs = System.nanoTime();

        int size = 0;
        try {
            List<?> results = jdbcTemplate.queryForList(sql);
            size = results.size();
            databaseMetrics.incrementDatabaseOperations(metricKey, activeProfile);
        } catch (Exception e) {
            databaseMetrics.incrementFailedQueries();
            log.error("Błąd zapytania [{}]: {}", label, e.getMessage());
        } finally {
            databaseMetrics.stopTimer(t, metricKey, activeProfile);
        }

        long durMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
        databaseMetrics.recordTransactionTime(durMs);

        writeCsv(label, sql, metricKey, durMs, size);
    }

    private void writeCsv(String label, String sql, String metricKey, long durMs, int resultCount) {
        File csv = new File("performance-index-results.csv");
        boolean header = !csv.exists() || csv.length() == 0;

        // 1 zapytanie = 1 operacja
        double opsPerSec = durMs > 0 ? (1000.0 / durMs) : 0.0;
        double p95 = databaseMetrics.getP95Millis(metricKey, activeProfile);

        try (FileWriter w = new FileWriter(csv, true)) {
            if (header) {
                w.write("Operacja;Czas[ms];Liczba rekordów;Operacji/s;Profil;p95_ms;DB_operacje;DB_failed_queries;DB_czas_timer_ms;Zapytanie\n");
            }
            String line = String.join(";",
                    label,
                    String.valueOf(durMs),
                    String.valueOf(resultCount),
                    String.valueOf(opsPerSec),
                    activeProfile,
                    String.valueOf(Double.isNaN(p95) ? 0 : p95),
                    String.valueOf(databaseMetrics.getOperationsCount(metricKey, activeProfile)),
                    String.valueOf(databaseMetrics.getFailedQueriesCount()),
                    String.valueOf(databaseMetrics.getTotalOperationTimeMillis(metricKey, activeProfile)),
                    "\"" + sql.replace("\n", " ").replace("\"", "'") + "\""
            );
            w.write(line + "\n");
        } catch (IOException e) {
            log.error("Błąd zapisu CSV: {}", e.getMessage());
        }
    }

    /* ------------ DDL helpers ------------ */

    private void createIndex(String indexName, String sqlPostgres, String sqlMysql) {
        if (isPostgres()) {
            jdbcTemplate.execute("DROP INDEX IF EXISTS " + indexName);
            jdbcTemplate.execute(sqlPostgres);
        } else if (isMySql()) {
            dropIndexIfExists(indexName, "product");
            jdbcTemplate.execute(sqlMysql);
        }
    }

    private void dropIndexIfExists(String indexName, String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM INFORMATION_SCHEMA.STATISTICS " +
                        "WHERE table_schema = DATABASE() AND table_name = ? AND index_name = ?",
                Integer.class, tableName, indexName);
        if (count != null && count > 0) {
            jdbcTemplate.execute("DROP INDEX " + indexName + " ON " + tableName);
        }
    }

    /** Sprawdza dostępność klasy operatorów dla metody dostępu (gin/gist). */
    private boolean hasOpClass(String opcName, String amName) {
        try {
            Integer cnt = jdbcTemplate.queryForObject(
                    "SELECT COUNT(1) FROM pg_opclass c JOIN pg_am a ON a.oid = c.opcmethod " +
                            "WHERE c.opcname = ? AND a.amname = ?",
                    Integer.class, opcName, amName
            );
            return cnt != null && cnt > 0;
        } catch (Exception e) {
            log.warn("Nie można sprawdzić opclass {} dla {}: {}", opcName, amName, e.getMessage());
            return false;
        }
    }

    /* ------------ runner ------------ */

    public void runAllTests() {
        log.info("Rozpoczynam testy wydajności indeksów");
        prepareData();

        baselineNoIndex();
        singleIndexTest();
        compoundIndexTest();
        partialIndexTest();
        fulltextIndexTest();
        trigramLikeContainsTestIfPg(); // w PG tworzy GIN (lub GiST jako fallback)

        log.info("Zakończono testy wydajności indeksów");
    }
}
