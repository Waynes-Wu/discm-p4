package p4.gradesservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GradeDTO {
    private Long id;
    
    @NotNull(message = "Student ID is required")
    private Long studentId;
    
    @NotNull(message = "Course ID is required")
    private Long courseId;
    
    @NotNull(message = "Grade is required")
    @Min(value = 0, message = "Grade cannot be less than 0")
    @Max(value = 100, message = "Grade cannot be more than 100")
    private Double grade;
    
    private String comments;
    private LocalDateTime submittedAt;
    private LocalDateTime lastModifiedAt;
}
