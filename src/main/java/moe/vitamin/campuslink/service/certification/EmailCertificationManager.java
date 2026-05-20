package moe.vitamin.campuslink.service.certification;

import moe.vitamin.campuslink.service.certification.database.EmailCertificationDao;
import moe.vitamin.campuslink.service.certification.result.EmailCertificationRequestResult;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class EmailCertificationManager {

    public static EmailCertificationManager init() {
        EmailCertificationDao.init();

        return new EmailCertificationManager();
    }

    private final Pattern emailPattern;

    private EmailCertificationManager() {
        this.emailPattern = Pattern.compile("^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@daegu\\.ac\\.kr");
    }


    public CompletableFuture<EmailCertificationRequestResult> requestCertification(
            String email,
            User user
    ) {
        if (!isValidEmail(email)) {
            return CompletableFuture.completedFuture(EmailCertificationRequestResult.INVALID_EMAIL);
        }

        return CompletableFuture.supplyAsync(() -> {

            return null;
        });

    }

    public boolean isValidEmail(String email) {
        return emailPattern.matcher(email).matches();
    }

}
