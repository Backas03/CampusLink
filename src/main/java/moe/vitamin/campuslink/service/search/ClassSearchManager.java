package moe.vitamin.campuslink.service.search;

import lombok.extern.slf4j.Slf4j;
import moe.vitamin.campuslink.service.search.database.ClassSearchDao;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ClassSearchManager {

    private static final Pattern TIME_PATTERN = Pattern.compile("([월화수목금토일])\\((\\d{2}:\\d{2})~(\\d{2}:\\d{2})\\)");

    public static void importCsv(File file) {
        List<ClassDataDto> classList = parseCsv(file);
        if (classList == null || classList.isEmpty()) {
            log.warn("No classes parsed or CSV file is invalid.");
            return;
        }

        log.info("Parsed {} classes. Inserting to DB...", classList.size());
        ClassSearchDao.batchSaveClassData(classList);
        log.info("Database insertion completed successfully.");
    }

    public static List<ClassDataDto> parseCsv(File file) {
        if (file == null || !file.exists()) {
            log.error("CSV file does not exist or is null.");
            return Collections.emptyList();
        }

        List<ClassDataDto> classList = new ArrayList<>();
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            Charset charset = isUtf8(bytes) ? StandardCharsets.UTF_8 : Charset.forName("MS949");
            log.info("Detected encoding for {}: {}", file.getName(), charset.name());

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes), charset))) {
                String headerLine = reader.readLine();
                if (headerLine == null) {
                    log.warn("Empty CSV file.");
                    return Collections.emptyList();
                }

                // Check header to find column indices
                List<String> headers = parseCsvLine(headerLine);
                if (!headers.isEmpty()) {
                    String firstHeader = headers.get(0);
                    if (firstHeader.startsWith("\uFEFF")) {
                        headers.set(0, firstHeader.substring(1));
                    }
                }

                int idxClassification = headers.indexOf("구분");
                int idxGrade = headers.indexOf("학년");
                int idxDepartment = headers.indexOf("수강학과");
                int idxCourseNumber = headers.indexOf("수강번호");
                int idxCourseName = headers.indexOf("교과목명");
                int idxCredits = headers.indexOf("학점");
                int idxProfessor = headers.indexOf("담당교수");
                int idxClassTime = headers.indexOf("강의시간");
                int idxClassroom = headers.indexOf("강의실");
                int idxSubField = headers.indexOf("소영역");
                int idxRemarks = headers.indexOf("비고");

                log.debug("CSV Header Indices - Classification: {}, Grade: {}, Department: {}, CourseNumber: {}, CourseName: {}, Credits: {}, Professor: {}, ClassTime: {}, Classroom: {}, SubField: {}, Remarks: {}",
                        idxClassification, idxGrade, idxDepartment, idxCourseNumber, idxCourseName, idxCredits, idxProfessor, idxClassTime, idxClassroom, idxSubField, idxRemarks);

                if (idxCourseNumber == -1 || idxCourseName == -1) {
                    log.error("Invalid CSV format. Header must contain '수강번호' and '교과목명'. Found headers: {}", headers);
                    return Collections.emptyList();
                }

                String lastClassification = "";
                Integer lastGrade = 1;

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    List<String> cells = parseCsvLine(line);
                    // Ensure the list is padded to match the headers size
                    while (cells.size() < headers.size()) {
                        cells.add("");
                    }

                    // Get or forward fill Classification
                    String classification = getValue(cells, idxClassification);
                    if (classification.isEmpty()) {
                        classification = lastClassification;
                    } else {
                        lastClassification = classification;
                    }

                    // Get or forward fill Grade
                    String gradeStr = getValue(cells, idxGrade);
                    Integer grade;
                    if (gradeStr.isEmpty()) {
                        grade = lastGrade;
                    } else {
                        try {
                            grade = Integer.parseInt(gradeStr.replaceAll("[^0-9]", ""));
                            lastGrade = grade;
                        } catch (NumberFormatException e) {
                            grade = lastGrade;
                        }
                    }

                    String department = getValue(cells, idxDepartment);
                    String courseNumber = getValue(cells, idxCourseNumber);
                    if (courseNumber.isEmpty()) {
                        // Course number is primary key and must not be empty
                        continue;
                    }

                    String courseName = getValue(cells, idxCourseName);

                    String creditsStr = getValue(cells, idxCredits);
                    Integer credits = 0;
                    if (!creditsStr.isEmpty()) {
                        try {
                            credits = Integer.parseInt(creditsStr.replaceAll("[^0-9]", ""));
                        } catch (NumberFormatException ignored) {}
                    }

                    String professor = getValue(cells, idxProfessor);
                    String classTimeRaw = getValue(cells, idxClassTime);
                    String classroom = getValue(cells, idxClassroom);

                    // Parse Day, Start/End Time and Duration
                    String dayOfWeek = null;
                    LocalTime startTime = null;
                    LocalTime endTime = null;
                    Integer durationMinutes = 0;

                    if (!classTimeRaw.isEmpty()) {
                        Matcher matcher = TIME_PATTERN.matcher(classTimeRaw);
                        if (matcher.find()) {
                            dayOfWeek = matcher.group(1);
                            try {
                                startTime = LocalTime.parse(matcher.group(2));
                                endTime = LocalTime.parse(matcher.group(3));
                                durationMinutes = (int) ChronoUnit.MINUTES.between(startTime, endTime);
                            } catch (Exception e) {
                                log.warn("Failed to parse time slots for string: {}", classTimeRaw, e);
                            }
                        }
                    }

                    // Combine subfield (소영역) and remarks (비고) to avoid losing data
                    String subField = getValue(cells, idxSubField);
                    String rawRemarks = getValue(cells, idxRemarks);
                    StringBuilder remarksBuilder = new StringBuilder();
                    if (!subField.isEmpty()) {
                        remarksBuilder.append("[소영역] ").append(subField);
                    }
                    if (!rawRemarks.isEmpty()) {
                        if (remarksBuilder.length() > 0) {
                            remarksBuilder.append(" | ");
                        }
                        remarksBuilder.append("[비고] ").append(rawRemarks);
                    }
                    String remarks = remarksBuilder.toString();

                    ClassDataDto dto = ClassDataDto.builder()
                            .grade(grade)
                            .classification(classification)
                            .department(department)
                            .courseNumber(courseNumber)
                            .courseName(courseName)
                            .credits(credits)
                            .professor(professor)
                            .startTime(startTime)
                            .endTime(endTime)
                            .durationMinutes(durationMinutes)
                            .classroom(classroom)
                            .remarks(remarks)
                            .dayOfWeek(dayOfWeek)
                            .build();

                    classList.add(dto);
                }
            }
        } catch (IOException e) {
            log.error("Failed to read CSV file: {}", file.getName(), e);
        }
        return classList;
    }

    private static String getValue(List<String> cells, int index) {
        if (index >= 0 && index < cells.size()) {
            return cells.get(index).trim();
        }
        return "";
    }

    private static boolean isUtf8(byte[] bytes) {
        try {
            CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
            decoder.onMalformedInput(CodingErrorAction.REPORT);
            decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
            decoder.decode(ByteBuffer.wrap(bytes));
            return true;
        } catch (CharacterCodingException e) {
            return false;
        }
    }

    private static List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        values.add(sb.toString().trim());
        return values;
    }
}