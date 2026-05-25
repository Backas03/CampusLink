package moe.vitamin.campuslink.service.search;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClassSearchManagerTest {

    @TempDir
    Path tempDir;

    @Test
    void parseCsv_successfullyParsesUtf8Csv() throws IOException {
        String csvContent = "구분,학년,수강학과,수강번호,교과목명,학점,시간,담당교수,강의시간,강의실,소영역,비고\n" +
                "공통,1,대학전체,1016,컴퓨팅사고와코딩,1,1-0-0,이건직,,DU~MOOC,AI·SW,DU~MOOC\n" +
                ",,대학전체,1034,DU진로탐색,1,1-0-0,홍길동,화(14:00~14:50),경영대강당,진로·취업,블렌디드러닝\n";

        Path csvPath = tempDir.resolve("test_utf8.csv");
        Files.writeString(csvPath, csvContent);

        List<ClassDataDto> result = ClassSearchManager.parseCsv(csvPath.toFile());

        assertEquals(2, result.size());

        // Test Row 1
        ClassDataDto row1 = result.get(0);
        assertEquals("공통", row1.getClassification());
        assertEquals(1, row1.getGrade());
        assertEquals("대학전체", row1.getDepartment());
        assertEquals("1016", row1.getCourseNumber());
        assertEquals("컴퓨팅사고와코딩", row1.getCourseName());
        assertEquals(1, row1.getCredits());
        assertEquals("이건직", row1.getProfessor());
        assertNull(row1.getDayOfWeek());
        assertNull(row1.getStartTime());
        assertNull(row1.getEndTime());
        assertEquals(0, row1.getDurationMinutes());
        assertEquals("DU~MOOC", row1.getClassroom());
        assertEquals("[소영역] AI·SW | [비고] DU~MOOC", row1.getRemarks());

        // Test Row 2 (Forward Fill Classification & Grade, and Parse Time)
        ClassDataDto row2 = result.get(1);
        assertEquals("공통", row2.getClassification());
        assertEquals(1, row2.getGrade());
        assertEquals("대학전체", row2.getDepartment());
        assertEquals("1034", row2.getCourseNumber());
        assertEquals("DU진로탐색", row2.getCourseName());
        assertEquals(1, row2.getCredits());
        assertEquals("홍길동", row2.getProfessor());
        assertEquals("화", row2.getDayOfWeek());
        assertEquals(LocalTime.of(14, 0), row2.getStartTime());
        assertEquals(LocalTime.of(14, 50), row2.getEndTime());
        assertEquals(50, row2.getDurationMinutes());
        assertEquals("경영대강당", row2.getClassroom());
        assertEquals("[소영역] 진로·취업 | [비고] 블렌디드러닝", row2.getRemarks());
    }

    @Test
    void parseCsv_successfullyParsesMs949Csv() throws IOException {
        String csvContent = "구분,학년,수강학과,수강번호,교과목명,학점,시간,담당교수,강의시간,강의실,소영역,비고\n" +
                "전공선택,2,컴퓨터공학,2001,알고리즘,3,3-0-0,김철수,수(10:00~11:50),공학관,전공,중요과목\n";

        Path csvPath = tempDir.resolve("test_ms949.csv");
        Files.write(csvPath, csvContent.getBytes(Charset.forName("MS949")));

        List<ClassDataDto> result = ClassSearchManager.parseCsv(csvPath.toFile());

        assertEquals(1, result.size());

        ClassDataDto dto = result.get(0);
        assertEquals("전공선택", dto.getClassification());
        assertEquals(2, dto.getGrade());
        assertEquals("컴퓨터공학", dto.getDepartment());
        assertEquals("2001", dto.getCourseNumber());
        assertEquals("알고리즘", dto.getCourseName());
        assertEquals(3, dto.getCredits());
        assertEquals("김철수", dto.getProfessor());
        assertEquals("수", dto.getDayOfWeek());
        assertEquals(LocalTime.of(10, 0), dto.getStartTime());
        assertEquals(LocalTime.of(11, 50), dto.getEndTime());
        assertEquals(110, dto.getDurationMinutes());
        assertEquals("공학관", dto.getClassroom());
        assertEquals("[소영역] 전공 | [비고] 중요과목", dto.getRemarks());
    }
}
