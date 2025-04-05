package p4.facultyservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FacultyDTO {
    private Long id;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;
    
    @NotBlank(message = "Department is required")
    private String department;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String officeLocation;
    private String officeHours;
    private String researchInterests;
}
