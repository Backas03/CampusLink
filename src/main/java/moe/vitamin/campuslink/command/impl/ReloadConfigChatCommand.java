package moe.vitamin.campuslink.command.impl;

import lombok.extern.slf4j.Slf4j;
import moe.vitamin.campuslink.CampusLink;
import moe.vitamin.campuslink.command.api.ChatCommandSource;
import moe.vitamin.campuslink.config.YamlConfigLoadException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class ReloadConfigChatCommand implements ChatCommandSource {

    @Override
    public void onTriggered(MessageReceivedEvent event) {
        event.getMessage().reply("데이터를 리로드 하고 있습니다...").queue(
                message -> {
                    long start = System.currentTimeMillis();
                    CompletableFuture.runAsync(() -> {
                        try {
                            CampusLink.getInstance().getConfigManager().reload();
                            CampusLink.getInstance().getPermissionManager().reload(CampusLink.getInstance().getConfigManager().getPermissionConfig());
                            long duration = System.currentTimeMillis() - start;

                            message.editMessage("데이터 리로드가 완료되었습니다. (소요 시간: " + duration + "ms)").queue();
                        } catch (YamlConfigLoadException e) {
                            log.error("Failed to reload config", e);
                            message.editMessage("데이터 리로드에 실패했습니다.").queue();
                        }
                    });
                }
        );
    }

    @Nullable
    @Override
    public String getDescription() {
        return "모든 설정을 리로드합니다.";
    }

    @Nullable
    @Override
    public String getUsage() {
        return "reload";
    }

    @Nullable
    @Override
    public String getPermissionNode() {
        return "campuslink.command.reload";
    }
}
