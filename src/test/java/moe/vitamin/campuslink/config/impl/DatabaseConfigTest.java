package moe.vitamin.campuslink.config.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseConfigTest {

    @TempDir
    Path tempDir;

    @Test
    void load_readsDatabaseAndHikariSettingsFromYamlFile() throws IOException {
        Path yamlPath = tempDir.resolve("database.yaml");
        Files.writeString(yamlPath, """
                database:
                  driver-class-name: "org.mariadb.jdbc.Driver"
                  url: "jdbc:mariadb://127.0.0.1:3306/campuslink"
                  username: "test_user"
                  password: "test_pw"
                  hikari-cp:
                    maximum-pool-size: 20
                    minimum-idle: 5
                    max-lifetime: 123456
                    connection-timeout: 15000
                    idle-timeout: 90000
                    pool-name: "TestPool"
                """);

        DatabaseConfig config = new DatabaseConfig(yamlPath.toFile());
        config.load();

        assertEquals("org.mariadb.jdbc.Driver", config.getDriverClassName());
        assertEquals("jdbc:mariadb://127.0.0.1:3306/campuslink", config.getJdbcUrl());
        assertEquals("test_user", config.getUsername());
        assertEquals("test_pw", config.getPassword());
        assertEquals(20, config.getMaximumPoolSize());
        assertEquals(5, config.getMinimumIdle());
        assertEquals(123456L, config.getMaximumLifetime());
        assertEquals(15000L, config.getConnectionTimeout());
        assertEquals(90000L, config.getIdleTimeout());
        assertEquals("TestPool", config.getPoolName());
    }
}
