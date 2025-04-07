package p4.mainservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

@Data
public class GradeForm {
    @NotBlank(message = "Course is required")
    private String courseCode;

    @NotNull(message = "Student is required")
    private Long studentId;

    @NotNull(message = "Grade is required")
    @Min(value = 0, message = "Grade must be at least 0.0")
    @Max(value = 4, message = "Grade must be at most 4.0")
    private Double grade;

    private String comments;
}