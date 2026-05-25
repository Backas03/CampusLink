package moe.vitamin.campuslink.service.certification.database;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class EmailCertificationData {

    private final String email;
    private final long discordUserId;
    private final long guildId;
    private final LocalDateTime certifiedAt;

}
