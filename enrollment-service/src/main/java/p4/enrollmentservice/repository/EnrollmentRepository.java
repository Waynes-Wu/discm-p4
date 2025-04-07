package p4.enrollmentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import p4.enrollmentservice.model.Enrollment;
import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByUserId(Long userId);

    List<Enrollment> findByCourseCode(String courseCode);

    boolean existsByUserIdAndCourseCode(Long userId, String courseCode);
}
