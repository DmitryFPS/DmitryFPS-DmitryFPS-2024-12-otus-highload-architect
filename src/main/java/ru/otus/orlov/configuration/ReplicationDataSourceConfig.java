package ru.otus.orlov.configuration;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Configuration
@EnableScheduling
public class ReplicationDataSourceConfig {
    // Мапа для хранения задержек каждого слейва
    private final Map<String, Long> latencyMap = new ConcurrentHashMap<>();

    @Primary
    @Bean
    @DependsOn({"writeDataSource", "readDataSource1", "readDataSource2", "routingDataSource"})
    public DataSource dataSource() {
        return new LazyConnectionDataSourceProxy(routingDataSource());
    }

    @Bean
    public DataSource routingDataSource() {
        final AbstractRoutingDataSource routingDataSource = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
                    log.info("Транзакция на чтение, передаем в slave");
                    return determineBestReadDataSource();
                }
                log.info("Транзакция на запись, передаем в master");
                return "write";
            }
        };

        // Настройка источников данных
        final Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put("write", writeDataSource());
        dataSourceMap.put("read1", readDataSource1());
        dataSourceMap.put("read2", readDataSource2());

        routingDataSource.setDefaultTargetDataSource(writeDataSource());
        routingDataSource.setTargetDataSources(dataSourceMap);

        return routingDataSource;
    }

    @Bean
    public SpringLiquibase liquibase(final DataSource dataSource) {
        final SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog/db.changelog-master.yaml");
        return liquibase;
    }

    /** Выбирает слейв с наименьшей задержкой. */
    private String determineBestReadDataSource() {
        long minLatency = Long.MAX_VALUE;
        String bestDataSource = "read1";

        for (final Map.Entry<String, Long> entry : latencyMap.entrySet()) {
            if (entry.getValue() < minLatency) {
                minLatency = entry.getValue();
                bestDataSource = entry.getKey();
            }
        }

        return bestDataSource;
    }

    /** Периодически обновляет задержки для каждого слейва. */
    @Scheduled(fixedRate = 5000) // Обновляем каждые 5 секунд
    public void updateLatencies() {
        latencyMap.put("read1", measureLatency(readDataSource1()));
        latencyMap.put("read2", measureLatency(readDataSource2()));
        log.info("Updated latencies: {}", latencyMap);
    }

    /** Измеряет задержку для указанного источника данных. */
    private long measureLatency(final DataSource dataSource) {
        long startTime = System.currentTimeMillis();
        try (final Connection connection = dataSource.getConnection();
             final Statement statement = connection.createStatement()) {
            statement.execute("SELECT 1");
        } catch (final SQLException e) {
            // В случае ошибки возвращаем максимальную задержку
            return Long.MAX_VALUE;
        }
        return System.currentTimeMillis() - startTime;
    }

    /** Мастер-источник данных для операций записи. */
    @Bean
    public DataSource writeDataSource() {
        final HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:postgresql://pgmaster:5432/postgres");
        dataSource.setUsername("postgres");
        dataSource.setPassword("pass");
        dataSource.setMaximumPoolSize(1000);
        dataSource.setIdleTimeout(30000);
        dataSource.setMaxLifetime(1800000);
        dataSource.setLeakDetectionThreshold(5000);
        return dataSource;
    }

    /** Первый слейв-источник данных для операций чтения. */
    @Bean
    public DataSource readDataSource1() {
        final HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:postgresql://pgslave:5432/postgres");
        dataSource.setUsername("postgres");
        dataSource.setPassword("pass");
        dataSource.setMaximumPoolSize(1000);
        dataSource.setIdleTimeout(30000);
        dataSource.setMaxLifetime(1800000);
        dataSource.setLeakDetectionThreshold(5000);
        return dataSource;
    }

    /** Второй слейв-источник данных для операций чтения */
    @Bean
    public DataSource readDataSource2() {
        final HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:postgresql://pgasyncslave:5432/postgres");
        dataSource.setUsername("postgres");
        dataSource.setPassword("pass");
        dataSource.setMaximumPoolSize(1000);
        dataSource.setIdleTimeout(30000);
        dataSource.setMaxLifetime(1800000);
        dataSource.setLeakDetectionThreshold(5000);
        return dataSource;
    }

    @Scheduled(fixedRate = 60000)
    public void logConnectionPoolStats() {
        log.info("Write DataSource stats: {}", ((HikariDataSource) writeDataSource()).getHikariPoolMXBean());
        log.info("Read DataSource 1 stats: {}", ((HikariDataSource) readDataSource1()).getHikariPoolMXBean());
        log.info("Read DataSource 2 stats: {}", ((HikariDataSource) readDataSource2()).getHikariPoolMXBean());
    }

    @PreDestroy
    public void closeDataSources() {
        ((HikariDataSource) writeDataSource()).close();
        ((HikariDataSource) readDataSource1()).close();
        ((HikariDataSource) readDataSource2()).close();
    }
}
