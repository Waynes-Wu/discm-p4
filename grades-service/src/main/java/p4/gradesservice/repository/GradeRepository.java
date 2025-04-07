package p4.gradesservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import p4.gradesservice.model.Grade;
import java.util.List;
import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findByStudentId(Long studentId);

    List<Grade> findByCourseCode(String courseCode);

    Optional<Grade> findByStudentIdAndCourseCode(Long studentId, String courseCode);

    boolean existsByStudentIdAndCourseCode(Long studentId, String courseCode);
}
