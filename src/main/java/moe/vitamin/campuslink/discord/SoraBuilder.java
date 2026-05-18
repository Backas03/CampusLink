package moe.vitamin.campuslink.discord;

import moe.vitamin.campuslink.config.impl.SoraConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class SoraBuilder {

    private String token;
    private SoraConfig config;

    public SoraBuilder setToken(String token) {
        this.token = token;
        return this;
    }

    public SoraBuilder setToLoadFromConfig(SoraConfig config) {
        this.config = config;
        return this;
    }

    public Sora build() {
        JDABuilder builder;
        if (config != null) {
            builder = JDABuilder.createDefault(config.getToken());
        } else {
            builder = JDABuilder.createDefault(token);
        }
        JDA jda = builder
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .enableCache(CacheFlag.ROLE_TAGS)
                .build();

        return new Sora(jda);
    }

}
