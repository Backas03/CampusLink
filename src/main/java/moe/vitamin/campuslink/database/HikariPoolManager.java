package moe.vitamin.campuslink.database;

import com.zaxxer.hikari.HikariDataSource;
import moe.vitamin.campuslink.config.DatabaseConfig;

public class HikariPoolManager {

    private final HikariDataSource dataSource;

    public HikariPoolManager(DatabaseConfig config) {
        this.dataSource = null; // TODO: impl
    }
}
