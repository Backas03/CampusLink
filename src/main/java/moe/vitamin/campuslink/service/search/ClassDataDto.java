package moe.vitamin.campuslink.service.search;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalTime;

@Getter
@Builder
@ToString
public class ClassDataDto {
    private final Integer grade;
    private final String classification;
    private final String department;
    private final String courseNumber;
    private final String courseName;
    private final Integer credits;
    private final String professor;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final Integer durationMinutes;
    private final String classroom;
    private final String remarks;
}
