package moe.vitamin.campuslink.discord;

import moe.vitamin.campuslink.config.impl.SoraConfig;
import net.dv8tion.jda.api.JDA;

public class Sora {

    public static SoraBuilder builder() {
        return new SoraBuilder();
    }

    private final JDA jda;

    protected Sora(JDA jda) {
        this.jda = jda;
    }

    public JDA getJDA() {
        return jda;
    }

}
