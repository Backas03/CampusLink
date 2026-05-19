package moe.vitamin.campuslink;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import moe.vitamin.campuslink.config.impl.SoraConfig;
import moe.vitamin.campuslink.discord.Sora;

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

    private CampusLink(Sora sora) {
        this.sora = sora;
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
            file.mkdirs();
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