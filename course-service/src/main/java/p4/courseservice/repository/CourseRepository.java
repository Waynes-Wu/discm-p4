package p4.courseservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import p4.courseservice.model.Course;
import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCourseCode(String courseCode);
    List<Course> findByDepartment(String department);
    List<Course> findByInstructorId(Long instructorId);
}
