package moe.vitamin.campuslink;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import moe.vitamin.campuslink.discord.Sora;

import java.io.File;
import java.net.URISyntaxException;

@Slf4j
public class CampusLink {

    @Getter
    private static CampusLink instance;

    public static void main(String[] args) {
        Sora sora = Sora.builder()
                .setToken(null)
                .build();

        instance = new CampusLink(sora);
    }

    @Getter
    private final Sora sora;

    private CampusLink(Sora sora) {
        this.sora = sora;
    }

    public File getDataFolder() {
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