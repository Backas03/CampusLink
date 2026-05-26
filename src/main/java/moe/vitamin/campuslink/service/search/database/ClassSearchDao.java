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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static void syncWithCsvFiles() {
        File dataFolder = CampusLink.getDataFolder();
        if (dataFolder == null) {
            log.warn("Data folder is null, skipping CSV synchronization.");
            return;
        }
        File coursesDir = new File(dataFolder, "courses");
        if (!coursesDir.exists()) {
            if (coursesDir.mkdirs()) {
                log.info("Created courses directory at {}", coursesDir.getAbsolutePath());
            } else {
                log.error("Failed to create courses directory at {}", coursesDir.getAbsolutePath());
                return;
            }
        }

        File[] files = coursesDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
        if (files == null || files.length == 0) {
            log.info("No CSV files found in courses directory: {}", coursesDir.getAbsolutePath());
            return;
        }

        log.info("Found {} CSV files in courses directory. Starting synchronization...", files.length);

        try (Connection connection = CampusLink.getInstance().getHikariPoolManager().getConnection()) {
            log.info("Starting database transaction for CSV course synchronization...");
            connection.setAutoCommit(false);
            try {
                DSLContext context = DSL.using(connection);

                // Empty the class_search table for clean sync
                context.deleteFrom(DSL.table(TABLE_NAME)).execute();
                log.info("Cleared existing records in {}", TABLE_NAME);

                int totalInserted = 0;
                Pattern timePattern = Pattern.compile("([월화수목금토일])\\((\\d{2}:\\d{2})[-~](\\d{2}:\\d{2})\\)");
                List<org.jooq.Query> queries = new ArrayList<>();

                for (File file : files) {
                    log.info("Parsing CSV file: {}", file.getName());
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
                        String headerLine = br.readLine(); // Skip header
                        String line;
                        while ((line = br.readLine()) != null) {
                            if (line.trim().isEmpty()) continue;
                            List<String> columns = parseCsvLine(line);
                            if (columns.size() < 4) { // Needs at least course number at index 3
                                continue;
                            }

                            String classification = columns.size() > 0 ? columns.get(0) : "";
                            String gradeStr = columns.size() > 1 ? columns.get(1) : "";
                            String department = columns.size() > 2 ? columns.get(2) : "";
                            String courseNumber = columns.size() > 3 ? columns.get(3) : "";
                            String courseName = columns.size() > 4 ? columns.get(4) : "";
                            String creditsStr = columns.size() > 5 ? columns.get(5) : "";
                            String hoursStr = columns.size() > 6 ? columns.get(6) : ""; // not stored directly
                            String professor = columns.size() > 7 ? columns.get(7) : "";
                            String classTime = columns.size() > 8 ? columns.get(8) : "";
                            String classroom = columns.size() > 9 ? columns.get(9) : "";
                            String remarks = columns.size() > 11 ? columns.get(11) : "";

                            if (courseNumber == null || courseNumber.trim().isEmpty()) {
                                continue;
                            }

                            Integer grade = null;
                            try {
                                if (gradeStr != null && !gradeStr.trim().isEmpty()) {
                                    grade = Integer.parseInt(gradeStr.trim());
                                }
                            } catch (NumberFormatException ignored) {}

                            Integer credits = null;
                            try {
                                if (creditsStr != null && !creditsStr.trim().isEmpty()) {
                                    credits = Integer.parseInt(creditsStr.trim());
                                }
                            } catch (NumberFormatException ignored) {}

                            // Parse time fields
                            String dayOfWeek = null;
                            LocalTime startTime = null;
                            LocalTime endTime = null;
                            Integer durationMinutes = null;

                            if (classTime != null && !classTime.trim().isEmpty()) {
                                Matcher matcher = timePattern.matcher(classTime);
                                if (matcher.find()) {
                                    dayOfWeek = matcher.group(1);
                                    try {
                                        startTime = LocalTime.parse(matcher.group(2));
                                        endTime = LocalTime.parse(matcher.group(3));
                                        durationMinutes = (int) java.time.temporal.ChronoUnit.MINUTES.between(startTime, endTime);
                                    } catch (Exception ignored) {}
                                }
                            }

                            queries.add(context.insertInto(DSL.table(TABLE_NAME))
                                    .set(Fields.GRADE, grade)
                                    .set(Fields.CLASSIFICATION, classification)
                                    .set(Fields.DEPARTMENT, department)
                                    .set(Fields.COURSE_NUMBER, courseNumber)
                                    .set(Fields.COURSE_NAME, courseName)
                                    .set(Fields.CREDITS, credits)
                                    .set(Fields.PROFESSOR, professor)
                                    .set(Fields.START_TIME, startTime)
                                    .set(Fields.END_TIME, endTime)
                                    .set(Fields.DURATION_MINUTES, durationMinutes)
                                    .set(Fields.CLASSROOM, classroom)
                                    .set(Fields.REMARKS, remarks)
                                    .set(Fields.DAY_OF_WEEK, dayOfWeek)
                                    .onDuplicateKeyIgnore()
                            );
                        }
                    } catch (Exception e) {
                        log.error("Failed to parse CSV file: {}", file.getName(), e);
                    }
                }

                if (!queries.isEmpty()) {
                    int batchSize = 1000;
                    for (int i = 0; i < queries.size(); i += batchSize) {
                        List<org.jooq.Query> subList = queries.subList(i, Math.min(i + batchSize, queries.size()));
                        int[] results = context.batch(subList).execute();
                        for (int res : results) {
                            if (res > 0) totalInserted += res;
                        }
                    }
                }

                log.info("Committing database transaction for course synchronization...");
                connection.commit();
                log.info("Successfully synchronized {} courses with the database.", totalInserted);
            } catch (Exception e) {
                log.error("Error occurred during course synchronization. Initiating transaction rollback...", e);
                connection.rollback();
                log.info("Database transaction successfully rolled back.");
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (Exception e) {
            log.error("Failed to synchronize courses from CSV. Transaction rolled back, previous database records preserved.", e);
        }
    }

    private static List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString().trim());
        return result;
    }
}