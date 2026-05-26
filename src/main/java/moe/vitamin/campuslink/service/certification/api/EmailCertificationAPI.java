package moe.vitamin.campuslink.service.certification.api;

import moe.vitamin.campuslink.CampusLink;

import java.util.concurrent.CompletableFuture;

public final class EmailCertificationAPI {

    public EmailCertificationAPI() {
        throw new UnsupportedOperationException("EmailCertificationAPI is a utility class and cannot be instantiated");
    }

    public static CompletableFuture<Boolean> isEmailCertified(long discordUserId, long guildId) {
        return CampusLink.getInstance()
                .getEmailCertificationManager()
                .isCertified(discordUserId, guildId);
    }

}
