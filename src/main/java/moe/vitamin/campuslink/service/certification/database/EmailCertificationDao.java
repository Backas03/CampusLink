package moe.vitamin.campuslink.service.certification.database;

import lombok.extern.slf4j.Slf4j;
import moe.vitamin.campuslink.CampusLink;
import org.jetbrains.annotations.Nullable;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Slf4j
public class EmailCertificationDao {

    public static final String TABLE_NAME = "email_certification";

    public static final class Fields {

        public static final Field<String> EMAIL = DSL.field("email", String.class);
        public static final Field<Long> DISCORD_USER_ID = DSL.field("discord_user_id", Long.class);
        public static final Field<LocalDateTime> CERTIFIED_AT = DSL.field("certified_at", LocalDateTime.class);

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
                    .column(Fields.CERTIFIED_AT)
                    .primaryKey(Fields.EMAIL, Fields.DISCORD_USER_ID)
                    .execute();
        } catch (SQLException e) {
            log.error("Failed to initialize email certification table", e);
        }
    }

    @Nullable
    public static EmailCertificationData loadCertificationData(long discordUserId) {
        try (Connection connection = CampusLink.getInstance().getHikariPoolManager().getConnection()) {
            DSLContext context = DSL.using(connection);
            var record = context.select(Fields.EMAIL, Fields.DISCORD_USER_ID, Fields.CERTIFIED_AT)
                    .from(TABLE_NAME)
                    .where(Fields.DISCORD_USER_ID.eq(discordUserId))
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
                    result.value3()
            );
        } catch (SQLException e) {
            log.error("Failed to load email certification data for discord user id: {}", discordUserId, e);
            return null;
        }
    }

    public static EmailCertificationData loadCertificationData(String email) {
        try (Connection connection = CampusLink.getInstance().getHikariPoolManager().getConnection()) {
            DSLContext context = DSL.using(connection);
            var record = context.select(Fields.EMAIL, Fields.DISCORD_USER_ID, Fields.CERTIFIED_AT)
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
                    result.value3()
            );
        } catch (SQLException e) {
            log.error("Failed to load email certification data for email: {}", email, e);
            return null;
        }
    }
}
