package p4.gradesservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "grades", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "student_id", "course_code" })
})
public class Grade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Student ID is required")
    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @NotNull(message = "Course code is required")
    @Column(name = "course_code", nullable = false)
    private String courseCode;

    @DecimalMin(value = "0.0", message = "Grade cannot be less than 0.0")
    @DecimalMax(value = "4.0", message = "Grade cannot be more than 4.0")
    @Column(nullable = false)
    private Double grade;

    @Column(length = 500)
    private String comments;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    @Column
    private LocalDateTime lastModifiedAt;

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
        lastModifiedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedAt = LocalDateTime.now();
    }
}
