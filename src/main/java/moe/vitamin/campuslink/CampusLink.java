package moe.vitamin.campuslink;

import lombok.Getter;
import moe.vitamin.campuslink.discord.Sora;

import java.io.File;

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
        throw new UnsupportedOperationException("Not implemented yet");
    }
}