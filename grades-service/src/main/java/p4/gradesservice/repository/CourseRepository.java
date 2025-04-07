package p4.gradesservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import p4.gradesservice.model.Course;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByInstructorId(Long instructorId);

    boolean existsByCourseCode(String courseCode);

    Course findByCourseCode(String courseCode);
}