package p4.enrollmentservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "courses")
public class Course {
    @Id
    @Column(unique = true, nullable = false)
    private String courseCode;

    @NotBlank(message = "Course name is required")
    @Column(nullable = false)
    private String courseName;

    @Column(name = "instructor_id")
    private Long instructorId;

    @NotBlank(message = "Department is required")
    @Column(nullable = false)
    private String department;

    @Positive(message = "Credits must be positive")
    @Column(nullable = false)
    private Double credits;
}