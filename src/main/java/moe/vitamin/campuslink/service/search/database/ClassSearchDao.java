package moe.vitamin.campuslink.service.search;

import lombok.extern.slf4j.Slf4j;
import moe.vitamin.campuslink.CampusLink;
import org.jetbrains.annotations.Nullable;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalTime;

@Slf4j
public class ClassSearchDao {

    public static final String TABLE_NAME = "class_search";

    public static final class Fields {
        // 학년 (예: 1, 2, 3, 4학년 -> Integer)
        public static final Field<Integer> GRADE = DSL.field("grade", Integer.class);

        // 구분 (예: 전필, 전선, 교양 -> String)
        public static final Field<String> CLASSIFICATION = DSL.field("classification", String.class);

        // 수강학과
        public static final Field<String> DEPARTMENT = DSL.field("department", String.class);

        // 수강번호 (보통 학수번호나 강좌코드는 String이나 Long을 사용합니다)
        public static final Field<String> COURSE_NUMBER = DSL.field("course_number", String.class);

        // 교과목명
        public static final Field<String> COURSE_NAME = DSL.field("course_name", String.class);

        // 학점 (예: 3학점, 2학점 -> Integer)
        public static final Field<Integer> CREDITS = DSL.field("credits", Integer.class);

        // 담당교수
        public static final Field<String> PROFESSOR = DSL.field("professor", String.class);

        // 강의 시작 시간 (시/분 포함, 예: 09:30:00)
        public static final Field<LocalTime> START_TIME = DSL.field("start_time", LocalTime.class);

        // 강의 종료 시간 (시/분 포함, 예: 11:15:00)
        public static final Field<LocalTime> END_TIME = DSL.field("end_time", LocalTime.class);

        // 소요 시간 (예: 75분 -> Integer로 분 단위 저장)
        public static final Field<Integer> DURATION_MINUTES = DSL.field("duration_minutes", Integer.class);

        // 강의실
        public static final Field<String> CLASSROOM = DSL.field("classroom", String.class);

        // 비고
        public static final Field<String> REMARKS = DSL.field("remarks", String.class);
    }

    public static void init() {
        try (Connection connection = CampusLink.getInstance()
                .getHikariPoolManager()
                .getConnection()) {
            DSLContext context = DSL.using(connection);
            context.createTableIfNotExists(TABLE_NAME)
                    .column(Fields.GRADE)
                    .column(Fields.CLASSIFICATION)
                    .column(Fields.DEPARTMENT)
                    .column(Fields.COURSE_NUMBER)
                    .column(Fields.COURSE_NAME)
                    .column(Fields.CREDITS)
                    .column(Fields.PROFESSOR)
                    .column(Fields.START_TIME)
                    .column(Fields.END_TIME)
                    .column(Fields.DURATION_MINUTES)
                    .column(Fields.CLASSROOM)
                    .column(Fields.REMARKS)
                    .primaryKey(Fields.COURSE_NUMBER)
                    .execute();

        } catch (SQLException e) {
            log.error("Error while initializing HikariPoolManager", e);
        }
    }

    @Nullable
    public static ClassDataDto findByCourseNumber(String courseNumber) {
        try (Connection connection = CampusLink.getInstance().getHikariPoolManager().getConnection()) {
            DSLContext context = DSL.using(connection);
            return context.selectFrom(DSL.table(TABLE_NAME))
                    .where(Fields.COURSE_NUMBER.eq(courseNumber))
                    .fetchOptional()
                    .map(record -> ClassDataDto.builder()
                            .grade(record.get(Fields.GRADE))
                            .classification(record.get(Fields.CLASSIFICATION))
                            .department(record.get(Fields.DEPARTMENT))
                            .courseNumber(record.get(Fields.COURSE_NUMBER))
                            .courseName(record.get(Fields.COURSE_NAME))
                            .credits(record.get(Fields.CREDITS))
                            .professor(record.get(Fields.PROFESSOR))
                            .startTime(record.get(Fields.START_TIME))
                            .endTime(record.get(Fields.END_TIME))
                            .durationMinutes(record.get(Fields.DURATION_MINUTES))
                            .classroom(record.get(Fields.CLASSROOM))
                            .remarks(record.get(Fields.REMARKS))
                            .build())
                    .orElse(null);
        } catch (SQLException e) {
            log.error("Failed to query course number: {}", courseNumber, e);
            return null;
        }
    }

}