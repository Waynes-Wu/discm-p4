package p4.courseservice.service;

import p4.courseservice.dto.CourseDTO;
import java.util.List;

public interface CourseService {
    CourseDTO createCourse(CourseDTO courseDTO);
    CourseDTO updateCourse(Long id, CourseDTO courseDTO);
    CourseDTO getCourseById(Long id);
    CourseDTO getCourseByCourseCode(String courseCode);
    List<CourseDTO> getCoursesByDepartment(String department);
    List<CourseDTO> getCoursesByInstructor(Long instructorId);
    List<CourseDTO> getAllCourses();
    void deleteCourse(Long id);
    boolean existsByCourseCode(String courseCode);
}
