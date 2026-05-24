package moe.vitamin.campuslink.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import moe.vitamin.campuslink.CampusLink;
import moe.vitamin.campuslink.config.impl.CertificationConfig;
import moe.vitamin.campuslink.config.impl.DatabaseConfig;
import moe.vitamin.campuslink.config.impl.EmailConfig;
import moe.vitamin.campuslink.config.impl.SoraConfig;
import moe.vitamin.campuslink.config.yaml.YamlConfig;

import java.io.*;

@Getter
@Slf4j
public class ConfigManager {

    private SoraConfig soraConfig;
    private EmailConfig emailConfig;
    private CertificationConfig certificationConfig;

    public SoraConfig loadSoraConfig() throws YamlConfigLoadException {
        return loadConfig(getSoraConfigFile(), SoraConfig.class);
    }

    public EmailConfig loadEmailConfig() throws YamlConfigLoadException {
        return loadConfig(getEmailConfigFile(), EmailConfig.class);
    }

    public CertificationConfig loadCertificationConfig() throws YamlConfigLoadException {
        return loadConfig(getCertificationConfigFile(), CertificationConfig.class);
    }

    public DatabaseConfig loadDatabaseConfig() throws YamlConfigLoadException {
        return loadConfig(getDatabaseConfigFile(), DatabaseConfig.class);
    }

    public void reload() throws YamlConfigLoadException {
        this.soraConfig = loadSoraConfig();
        this.emailConfig = loadEmailConfig();
        this.certificationConfig = loadCertificationConfig();
    }

    private <T extends YamlConfig> T loadConfig(File file, Class<T> configClass) throws YamlConfigLoadException {
        try {
            File configFile = createResourceIfNotExists(file);
            T config = configClass.getConstructor(File.class)
                    .newInstance(configFile);
            config.load();
            return config;
        } catch (Exception e) {
            throw new YamlConfigLoadException(file, e);
        }
    }

    public File getSoraConfigFile() {
        return new File(getConfigFolder(), "sora.yaml");
    }

    public File getEmailConfigFile() {
        return new File(getConfigFolder(), "email.yaml");
    }

    public File getCertificationConfigFile() {
        return new File(getConfigFolder(), "certification.yaml");
    }

    public File getDatabaseConfigFile() {
        return new File(getConfigFolder(), "database.yaml");
    }

    public File getConfigFolder() {
        return new File(CampusLink.getDataFolder(), "config");
    }

    public static File createResourceIfNotExists(File file, String resourcePath) {
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

    public static File createResourceIfNotExists(File file) {
        return createResourceIfNotExists(file, file.getName());
    }

}
