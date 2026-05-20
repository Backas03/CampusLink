package moe.vitamin.campuslink;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import moe.vitamin.campuslink.config.impl.DatabaseConfig;
import moe.vitamin.campuslink.config.impl.SoraConfig;
import moe.vitamin.campuslink.database.HikariPoolManager;
import moe.vitamin.campuslink.discord.Sora;
import moe.vitamin.campuslink.service.certification.EmailCertificationManager;

import java.io.*;
import java.net.URISyntaxException;

@Slf4j
public class CampusLink {

    @Getter
    private static CampusLink instance;

    public static void main(String[] args) {
        Sora sora = Sora.builder()
                .setConfig(loadSoraConfig())
                .build();

        instance = new CampusLink(sora);
    }

    @Getter
    private final Sora sora;
    @Getter
    private final HikariPoolManager hikariPoolManager;
    @Getter
    private final EmailCertificationManager emailCertificationManager;

    private CampusLink(Sora sora) {
        this.sora = sora;
        this.hikariPoolManager = new HikariPoolManager(loadDatabaseConfig());

        emailCertificationManager = EmailCertificationManager.init();
    }

    public File getDatabaseConfigFile() {
        return new File(getDataFolder(), "database.yaml");
    }

    private DatabaseConfig loadDatabaseConfig() {
        File configFile = createResourceIfNotExists(getDatabaseConfigFile(), "database.yaml");

        var config = new DatabaseConfig(configFile);
        config.load();

        return config;
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

    private static SoraConfig loadSoraConfig() {
        File file = createResourceIfNotExists(new File(getDataFolder(), "sora.yaml"), "sora.yaml");

        var config = new SoraConfig(file);
        config.load();
        return config;
    }


    private static File createResourceIfNotExists(File file, String resourcePath) {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                log.error("Failed to create file. file={}", file, e);
                return file;
            }
            try (InputStream in = CampusLink.class.getClassLoader().getResourceAsStream(resourcePath);
                 OutputStream out = new FileOutputStream(file)) {
                if (in == null) {
                    log.error("Resource not found: " + resourcePath);
                    return file;
                }
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                log.error("Failed to create resource file: {}", file.getAbsolutePath(), e);
            }
        }
        return file;
    }
}