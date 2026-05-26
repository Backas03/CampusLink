package moe.vitamin.campuslink.service.certification.database;

import lombok.extern.slf4j.Slf4j;
import moe.vitamin.campuslink.CampusLink;
import org.jetbrains.annotations.Nullable;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Slf4j
public class EmailCertificationDao {

    public static final String TABLE_NAME = "email_certification";

    public static final class Fields {

        public static final Field<String> EMAIL = DSL.field(
                DSL.name("email"),
                SQLDataType.VARCHAR(255).notNull()
        );

        public static final Field<Long> DISCORD_USER_ID = DSL.field(
                DSL.name("discord_user_id"),
                SQLDataType.BIGINT.notNull()
        );

        public static final Field<Long> CERTIFIED_GUILD_AT = DSL.field(
                DSL.name("guild_id"),
                SQLDataType.BIGINT.notNull()
        );

        public static final Field<LocalDateTime> CERTIFIED_AT = DSL.field(
                DSL.name("certified_at"),
                SQLDataType.LOCALDATETIME.notNull()
        );


        private Fields() {
            throw new UnsupportedOperationException();
        }
    }

    public static void init() {
        try (Connection connection = CampusLink.getInstance().getHikariPoolManager().getConnection()) {
            DSLContext context = DSL.using(connection);
            context.createTableIfNotExists(TABLE_NAME)
                    .column(Fields.EMAIL)
                    .column(Fields.DISCORD_USER_ID)
                    .column(Fields.CERTIFIED_GUILD_AT)
                    .column(Fields.CERTIFIED_AT)
                    .primaryKey(Fields.EMAIL, Fields.DISCORD_USER_ID)
                    .execute();
        } catch (SQLException e) {
            log.error("Failed to initialize email certification table", e);
        }
    }

    @Nullable
    public static EmailCertificationData loadCertificationData(long discordUserId, long guildId) {
        try (Connection connection = CampusLink.getInstance().getHikariPoolManager().getConnection()) {
            DSLContext context = DSL.using(connection);
            var record = context.select(Fields.EMAIL, Fields.DISCORD_USER_ID, Fields.CERTIFIED_GUILD_AT, Fields.CERTIFIED_AT)
                    .from(TABLE_NAME)
                    .where(Fields.DISCORD_USER_ID.eq(discordUserId).and(Fields.CERTIFIED_GUILD_AT.eq(guildId)))
                    .fetch();
            if (record.isEmpty()) {
                return null;
            }
            if (record.size() > 1) {
                log.warn("Multiple certification data found for discord user: {}", discordUserId);
            }
            var result = record.getFirst();
            return new EmailCertificationData(
                    result.value1(),
                    result.value2(),
                    result.value3(),
                    result.value4()
            );
        } catch (SQLException e) {
            log.error("Failed to load email certification data for discord user id: {}", discordUserId, e);
            return null;
        }
    }

    public static EmailCertificationData loadCertificationDataFromEmail(String email) {
        try (Connection connection = CampusLink.getInstance().getHikariPoolManager().getConnection()) {
            DSLContext context = DSL.using(connection);
            var record = context.select(Fields.EMAIL, Fields.DISCORD_USER_ID, Fields.CERTIFIED_GUILD_AT, Fields.CERTIFIED_AT)
                    .from(TABLE_NAME)
                    .where(Fields.EMAIL.eq(email))
                    .fetch();
            if (record.isEmpty()) {
                return null;
            }
            if (record.size() > 1) {
                log.warn("Multiple certification data found for email: {}", email);
            }
            var result = record.getFirst();
            return new EmailCertificationData(
                    result.value1(),
                    result.value2(),
                    result.value3(),
                    result.value4()
            );
        } catch (SQLException e) {
            log.error("Failed to load email certification data for email: {}", email, e);
            return null;
        }
    }

    public static void saveEmailCertificationData(EmailCertificationData data) {
        try (Connection connection = CampusLink.getInstance().getHikariPoolManager().getConnection()) {
            DSLContext context = DSL.using(connection);
            context.insertInto(DSL.table(TABLE_NAME),
                            Fields.EMAIL, Fields.DISCORD_USER_ID, Fields.CERTIFIED_GUILD_AT, Fields.CERTIFIED_AT)
                    .values(data.getEmail(), data.getDiscordUserId(), data.getGuildId(), data.getCertifiedAt())
                    .execute();
        } catch (SQLException e) {
            log.error("Failed to save email certification data for discord user id: {}", data.getDiscordUserId(), e);
        }
    }

    public static void insertEmailCertification(String email, long discordUserId, long guildId, LocalDateTime certifiedAt) {
        saveEmailCertificationData(new EmailCertificationData(email, discordUserId, guildId, certifiedAt));
    }

    public static void removeEmailCertificationData(long discordUserid, long guildId) {
        try (Connection connection = CampusLink.getInstance().getHikariPoolManager().getConnection()) {
            DSLContext context = DSL.using(connection);
            context.delete(DSL.table(TABLE_NAME))
                    .where(Fields.DISCORD_USER_ID.eq(discordUserid)
                            .and(Fields.CERTIFIED_GUILD_AT.eq(guildId)))
                    .execute();
            log.info("Removed email certification data for discord user id: {} and guild id: {}", discordUserid, guildId);
        } catch (SQLException e) {
            log.error("Failed to remove email certification data for discord user id: {} and guild id: {}", discordUserid, guildId, e);
        }
    }
}
