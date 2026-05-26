package moe.vitamin.campuslink.service.certification.listener;

import lombok.extern.slf4j.Slf4j;
import moe.vitamin.campuslink.service.certification.database.EmailCertificationDao;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class EmailCertificationInternalListener extends ListenerAdapter {
    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        CompletableFuture.runAsync(() -> {
            long start = System.currentTimeMillis();
            EmailCertificationDao.removeEmailCertificationData(event.getUser().getIdLong(), event.getGuild().getIdLong());
            log.info("Guild member quited, removed email certification data for user {} in guild {}, took {} ms",
                    event.getUser().getIdLong(),
                    event.getGuild().getIdLong(),
                    System.currentTimeMillis() - start
            );
        });
    }
}
