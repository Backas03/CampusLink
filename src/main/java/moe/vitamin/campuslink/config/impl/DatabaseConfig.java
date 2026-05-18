package moe.vitamin.campuslink.config.impl;

import lombok.Getter;
import moe.vitamin.campuslink.config.YamlConfig;
import moe.vitamin.campuslink.config.YamlNode;

import java.io.File;

@Getter
public class DatabaseConfig extends YamlConfig {

    private String driverClassName;
    private String jdbcUrl;
    private String username;
    private String password;
    private int maximumPoolSize;
    private int minimumIdle;
    private long maximumLifetime;
    private long connectionTimeout;
    private long idleTimeout;
    private String poolName;

    public DatabaseConfig(File file) {
        super(file);
    }

    @Override
    public void load() {
        super.load();

        YamlNode databaseNode = getNode("database");
        this.driverClassName = databaseNode.getString("driver-class-name");
        this.jdbcUrl = databaseNode.getString("url");
        this.username = databaseNode.getString("username");
        this.password = databaseNode.getString("password");
        this.maximumPoolSize = databaseNode.getIntOrDefault("maximum-pool-size", 10);
        this.minimumIdle = databaseNode.getIntOrDefault("minimum-idle", 2);
        this.maximumLifetime = databaseNode.getLongOrDefault("maximum-lifetime", 1800000L);
        this.connectionTimeout = databaseNode.getLongOrDefault("connection-timeout", 30000L);
        this.idleTimeout = databaseNode.getLongOrDefault("idle-timeout", 600000L);
        this.poolName = databaseNode.getStringOrDefault("pool-name", "CampusLink-HikariPool");
    }
}
