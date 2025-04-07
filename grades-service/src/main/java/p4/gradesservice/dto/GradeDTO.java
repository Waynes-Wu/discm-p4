package p4.gradesservice.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GradeDTO {
    private Long id;

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Course code is required")
    private String courseCode;

    @NotNull(message = "Grade is required")
    @DecimalMin(value = "0.0", message = "Grade cannot be less than 0.0")
    @DecimalMax(value = "4.0", message = "Grade cannot be more than 4.0")
    private Double grade;

    private String comments;
    private LocalDateTime submittedAt;
    private LocalDateTime lastModifiedAt;
}
