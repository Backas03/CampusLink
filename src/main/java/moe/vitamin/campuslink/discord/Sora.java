package moe.vitamin.campuslink.discord;

import moe.vitamin.campuslink.config.impl.SoraConfig;
import net.dv8tion.jda.api.JDA;

public class Sora {

    public static SoraBuilder builder() {
        return new SoraBuilder();
    }

    private final JDA jda;
    private final SoraConfig config;

    protected Sora(JDA jda, SoraConfig config) {
        this.jda = jda;
        this.config = config;
    }

    public JDA getJDA() {
        return jda;
    }

}
