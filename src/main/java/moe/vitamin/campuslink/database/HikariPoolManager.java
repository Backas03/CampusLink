package moe.vitamin.campuslink.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import moe.vitamin.campuslink.config.impl.DatabaseConfig;

import java.sql.Connection;
import java.sql.SQLException;

public class HikariPoolManager {

    private final HikariDataSource dataSource;

    public HikariPoolManager(DatabaseConfig config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(config.getDriverClassName());
        hikariConfig.setJdbcUrl(config.getJdbcUrl());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.setMinimumIdle(config.getMinimumIdle());
        hikariConfig.setMaximumPoolSize(config.getMaximumPoolSize());
        hikariConfig.setConnectionTimeout(config.getConnectionTimeout());
        hikariConfig.setIdleTimeout(config.getIdleTimeout());
        hikariConfig.setLeakDetectionThreshold(10000L);

        this.dataSource = new HikariDataSource(hikariConfig);
    }

    public Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    public void close() {
        this.dataSource.close();
    }
}
