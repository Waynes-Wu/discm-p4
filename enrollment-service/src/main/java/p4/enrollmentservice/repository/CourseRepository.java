package p4.enrollmentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import p4.enrollmentservice.model.Course;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, String> {
    @Query(value = "SELECT * FROM courses WHERE course_code = :courseCode", nativeQuery = true)
    Optional<Course> findByCourseCode(@Param("courseCode") String courseCode);
}