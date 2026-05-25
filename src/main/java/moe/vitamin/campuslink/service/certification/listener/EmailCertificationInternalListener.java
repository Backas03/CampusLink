package moe.vitamin.campuslink.service.certification.listener;

import moe.vitamin.campuslink.service.certification.database.EmailCertificationDao;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class EmailCertificationInternalListener extends ListenerAdapter {


    // TODO: register & remove guild certification data
    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        CompletableFuture.runAsync(() -> {
            // EmailCertificationDao.removeEmailCertificationData();
        });
    }



}
