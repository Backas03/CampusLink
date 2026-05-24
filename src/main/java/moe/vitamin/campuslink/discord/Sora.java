package moe.vitamin.campuslink.discord;

import lombok.AccessLevel;
import lombok.Getter;
import moe.vitamin.campuslink.command.CommandManager;
import moe.vitamin.campuslink.command.impl.HelpSlashCommand;
import moe.vitamin.campuslink.command.impl.ReloadConfigChatCommand;
import moe.vitamin.campuslink.service.certification.command.EmailCertificationInfoSlashCommand;
import moe.vitamin.campuslink.service.certification.command.EmailCertificationSlashCommand;
import net.dv8tion.jda.api.JDA;

@Getter
public class Sora {

    public static SoraBuilder builder() {
        return new SoraBuilder();
    }

    @Getter(AccessLevel.NONE)
    private final JDA jda;
    private final CommandManager commandManager;

    protected Sora(JDA jda) {
        this.jda = jda;

        this.commandManager = new CommandManager(this);
        this.commandManager.init();
        registerCommands();
    }

    public JDA getJDA() {
        return jda;
    }

    private void registerCommands() {
        commandManager.registerSlashCommand(new HelpSlashCommand());
        commandManager.registerChatCommand("reload", new ReloadConfigChatCommand());

        commandManager.registerSlashCommand(new EmailCertificationSlashCommand());
        commandManager.registerSlashCommand(new EmailCertificationInfoSlashCommand());
    }

}
