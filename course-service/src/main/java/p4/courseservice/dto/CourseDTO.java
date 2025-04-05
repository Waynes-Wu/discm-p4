package p4.courseservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CourseDTO {
    private Long id;
    
    @NotBlank(message = "Course name is required")
    private String courseName;
    
    @NotBlank(message = "Course code is required")
    private String courseCode;
    
    private Long instructorId;
    
    @NotBlank(message = "Department is required")
    private String department;
    
    @Positive(message = "Credit hours must be positive")
    private Integer creditHours;
}
