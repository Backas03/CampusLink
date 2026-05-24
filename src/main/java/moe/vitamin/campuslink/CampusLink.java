package moe.vitamin.campuslink;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import moe.vitamin.campuslink.config.ConfigManager;
import moe.vitamin.campuslink.config.YamlConfigLoadException;
import moe.vitamin.campuslink.database.HikariPoolManager;
import moe.vitamin.campuslink.discord.Sora;
import moe.vitamin.campuslink.service.certification.EmailCertificationManager;

import java.io.*;
import java.net.URISyntaxException;

@Getter
@Slf4j
public class CampusLink {

    @Getter
    private static CampusLink instance;

    public static void main(String[] args) {
        try {
            instance = new CampusLink();
            instance.loadServices();
        } catch (YamlConfigLoadException e) {
            log.error("Failed to load config file: {}. Please check your file and try load manually again.", e.getFile(), e);
        } finally {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (instance != null) {
                    instance.close();
                }
            }));
        }
    }

    private final ConfigManager configManager;
    private final HikariPoolManager hikariPoolManager;

    private Sora sora;
    private EmailCertificationManager emailCertificationManager;

    private CampusLink() throws YamlConfigLoadException {
        this.configManager = new ConfigManager();
        this.configManager.reload();

        this.hikariPoolManager = new HikariPoolManager(configManager.loadDatabaseConfig());
    }

    private void loadServices() {
        this.sora = Sora.builder()
                .setConfig(configManager.getSoraConfig())
                .build();
        this.emailCertificationManager = EmailCertificationManager.init();
    }

    public void close() {
        if (hikariPoolManager != null) hikariPoolManager.close();
    }

    public static File getDataFolder() {
        try {
            File jarFile = new File(CampusLink.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());
            return jarFile.getParentFile();
        } catch (URISyntaxException e) {
            log.error("Failed to get data folder", e);
        }
        return null;
    }
}