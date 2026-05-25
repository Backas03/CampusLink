package moe.vitamin.campuslink.service.search.database;

import lombok.extern.slf4j.Slf4j;
import moe.vitamin.campuslink.CampusLink;
import moe.vitamin.campuslink.service.search.ClassDataDto;
import org.jetbrains.annotations.Nullable;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;

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
        public static final Field<String> COURSE_NUMBER = DSL.field(
                DSL.name("course_number"),
                SQLDataType.VARCHAR(255).notNull()
        );

        // 교과목명
        public static final Field<String> COURSE_NAME = DSL.field("course_name", String.class);

        // 학점 (예: 3학점, 2학점 -> Integer)
        public static final Field<Integer> CREDITS = DSL.field("credits", Integer.class);

        // 담당교수
        public static final Field<String> PROFESSOR = DSL.field("professor", String.class);

        // 강의 시작 시간 (시/분 포함, 예: 09:30:00)
        public static final Field<LocalTime> START_TIME = DSL.field(
                DSL.name("start_time"),
                SQLDataType.LOCALTIME
        );

        // 강의 종료 시간 (시/분 포함, 예: 11:15:00)
        public static final Field<LocalTime> END_TIME = DSL.field(
                DSL.name("end_time"),
                SQLDataType.LOCALTIME
        );

        // 소요 시간 (예: 75분 -> Integer로 분 단위 저장)
        public static final Field<Integer> DURATION_MINUTES = DSL.field("duration_minutes", Integer.class);

        // 강의실
        public static final Field<String> CLASSROOM = DSL.field("classroom", String.class);

        // 비고
        public static final Field<String> REMARKS = DSL.field("remarks", String.class);

        // 요일 (예: 월, 화, 수, 목, 금, 토, 일 -> String)
        public static final Field<String> DAY_OF_WEEK = DSL.field("day_of_week", String.class);
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
                    .column(Fields.DAY_OF_WEEK)
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
            return context.select(
                            Fields.GRADE, Fields.CLASSIFICATION, Fields.DEPARTMENT,
                            Fields.COURSE_NUMBER, Fields.COURSE_NAME, Fields.CREDITS,
                            Fields.PROFESSOR, Fields.START_TIME, Fields.END_TIME,
                            Fields.DURATION_MINUTES, Fields.CLASSROOM, Fields.REMARKS, Fields.DAY_OF_WEEK
                    )
                    .from(DSL.table(TABLE_NAME))
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
                            .dayOfWeek(record.get(Fields.DAY_OF_WEEK))
                            .build())
                    .orElse(null);
        } catch (SQLException e) {
            log.error("Failed to query course number: {}", courseNumber, e);
            return null;
        }
    }

    public static void saveClassData(ClassDataDto data) {
        try (Connection connection = CampusLink.getInstance().getHikariPoolManager().getConnection()) {
            DSLContext context = DSL.using(connection);
            context.insertInto(DSL.table(TABLE_NAME),
                            Fields.GRADE, Fields.CLASSIFICATION, Fields.DEPARTMENT,
                            Fields.COURSE_NUMBER, Fields.COURSE_NAME, Fields.CREDITS,
                            Fields.PROFESSOR, Fields.START_TIME, Fields.END_TIME,
                            Fields.DURATION_MINUTES, Fields.CLASSROOM, Fields.REMARKS, Fields.DAY_OF_WEEK)
                    .values(data.getGrade(), data.getClassification(), data.getDepartment(),
                            data.getCourseNumber(), data.getCourseName(), data.getCredits(),
                            data.getProfessor(), data.getStartTime(), data.getEndTime(),
                            data.getDurationMinutes(), data.getClassroom(), data.getRemarks(), data.getDayOfWeek())
                    .onDuplicateKeyUpdate()
                    .set(Fields.GRADE, data.getGrade())
                    .set(Fields.CLASSIFICATION, data.getClassification())
                    .set(Fields.DEPARTMENT, data.getDepartment())
                    .set(Fields.COURSE_NAME, data.getCourseName())
                    .set(Fields.CREDITS, data.getCredits())
                    .set(Fields.PROFESSOR, data.getProfessor())
                    .set(Fields.START_TIME, data.getStartTime())
                    .set(Fields.END_TIME, data.getEndTime())
                    .set(Fields.DURATION_MINUTES, data.getDurationMinutes())
                    .set(Fields.CLASSROOM, data.getClassroom())
                    .set(Fields.REMARKS, data.getRemarks())
                    .set(Fields.DAY_OF_WEEK, data.getDayOfWeek())
                    .execute();
        } catch (SQLException e) {
            log.error("Failed to save class data for course number: {}", data.getCourseNumber(), e);
        }
    }

    public static void batchSaveClassData(List<ClassDataDto> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return;
        }
        try (Connection connection = CampusLink.getInstance().getHikariPoolManager().getConnection()) {
            DSLContext context = DSL.using(connection);
            var queries = dataList.stream().map(data ->
                    context.insertInto(DSL.table(TABLE_NAME),
                                    Fields.GRADE, Fields.CLASSIFICATION, Fields.DEPARTMENT,
                                    Fields.COURSE_NUMBER, Fields.COURSE_NAME, Fields.CREDITS,
                                    Fields.PROFESSOR, Fields.START_TIME, Fields.END_TIME,
                                    Fields.DURATION_MINUTES, Fields.CLASSROOM, Fields.REMARKS, Fields.DAY_OF_WEEK)
                            .values(data.getGrade(), data.getClassification(), data.getDepartment(),
                                    data.getCourseNumber(), data.getCourseName(), data.getCredits(),
                                    data.getProfessor(), data.getStartTime(), data.getEndTime(),
                                    data.getDurationMinutes(), data.getClassroom(), data.getRemarks(), data.getDayOfWeek())
                            .onDuplicateKeyUpdate()
                            .set(Fields.GRADE, data.getGrade())
                            .set(Fields.CLASSIFICATION, data.getClassification())
                            .set(Fields.DEPARTMENT, data.getDepartment())
                            .set(Fields.COURSE_NAME, data.getCourseName())
                            .set(Fields.CREDITS, data.getCredits())
                            .set(Fields.PROFESSOR, data.getProfessor())
                            .set(Fields.START_TIME, data.getStartTime())
                            .set(Fields.END_TIME, data.getEndTime())
                            .set(Fields.DURATION_MINUTES, data.getDurationMinutes())
                            .set(Fields.CLASSROOM, data.getClassroom())
                            .set(Fields.REMARKS, data.getRemarks())
                            .set(Fields.DAY_OF_WEEK, data.getDayOfWeek())
            ).toList();

            context.batch(queries).execute();
            log.info("Successfully batch saved {} class records.", dataList.size());
        } catch (SQLException e) {
            log.error("Failed to batch save class data", e);
        }
    }

    public static List<String> getUniqueClassifications() {
        try (Connection connection = CampusLink.getInstance().getHikariPoolManager().getConnection()) {
            DSLContext context = DSL.using(connection);
            return context.selectDistinct(Fields.CLASSIFICATION)
                    .from(DSL.table(TABLE_NAME))
                    .where(Fields.CLASSIFICATION.isNotNull())
                    .orderBy(Fields.CLASSIFICATION.asc())
                    .fetchInto(String.class);
        } catch (SQLException e) {
            log.error("Failed to fetch unique classifications", e);
            return List.of();
        }
    }

    public static List<String> getUniqueDepartments() {
        try (Connection connection = CampusLink.getInstance().getHikariPoolManager().getConnection()) {
            DSLContext context = DSL.using(connection);
            return context.selectDistinct(Fields.DEPARTMENT)
                    .from(DSL.table(TABLE_NAME))
                    .where(Fields.DEPARTMENT.isNotNull())
                    .orderBy(Fields.DEPARTMENT.asc())
                    .fetchInto(String.class);
        } catch (SQLException e) {
            log.error("Failed to fetch unique departments", e);
            return List.of();
        }
    }

    public static List<ClassDataDto> searchClasses(
            @Nullable String classification,
            @Nullable String department,
            @Nullable String courseNumber,
            @Nullable String courseName,
            @Nullable Integer credits,
            @Nullable String professor,
            @Nullable String dayOfWeek
    ) {
        try (Connection connection = CampusLink.getInstance().getHikariPoolManager().getConnection()) {
            DSLContext context = DSL.using(connection);

            var query = context.select(
                    Fields.GRADE, Fields.CLASSIFICATION, Fields.DEPARTMENT,
                    Fields.COURSE_NUMBER, Fields.COURSE_NAME, Fields.CREDITS,
                    Fields.PROFESSOR, Fields.START_TIME, Fields.END_TIME,
                    Fields.DURATION_MINUTES, Fields.CLASSROOM, Fields.REMARKS, Fields.DAY_OF_WEEK
            ).from(DSL.table(TABLE_NAME));

            Condition conditions = DSL.noCondition();

            if (classification != null && !classification.trim().isEmpty()) {
                conditions = conditions.and(Fields.CLASSIFICATION.eq(classification));
            }
            if (department != null && !department.trim().isEmpty()) {
                conditions = conditions.and(Fields.DEPARTMENT.eq(department));
            }
            if (courseNumber != null && !courseNumber.trim().isEmpty()) {
                conditions = conditions.and(Fields.COURSE_NUMBER.contains(courseNumber));
            }
            if (courseName != null && !courseName.trim().isEmpty()) {
                conditions = conditions.and(Fields.COURSE_NAME.contains(courseName));
            }
            if (credits != null) {
                conditions = conditions.and(Fields.CREDITS.eq(credits));
            }
            if (professor != null && !professor.trim().isEmpty()) {
                conditions = conditions.and(Fields.PROFESSOR.contains(professor));
            }
            if (dayOfWeek != null && !dayOfWeek.trim().isEmpty()) {
                conditions = conditions.and(Fields.DAY_OF_WEEK.eq(dayOfWeek));
            }

            return query.where(conditions)
                    .fetch()
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
                            .dayOfWeek(record.get(Fields.DAY_OF_WEEK))
                            .build());
        } catch (SQLException e) {
            log.error("Failed to search classes", e);
            return List.of();
        }
    }
}