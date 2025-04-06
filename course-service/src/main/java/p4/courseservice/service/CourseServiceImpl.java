package p4.courseservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import p4.courseservice.dto.CourseDTO;
import p4.courseservice.model.Course;
import p4.courseservice.repository.CourseRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseServiceImpl implements CourseService {
    
    private final CourseRepository courseRepository;

    @Override
    public CourseDTO createCourse(CourseDTO courseDTO) {
        if (courseRepository.findByCourseCode(courseDTO.getCourseCode()).isPresent()) {
            throw new IllegalArgumentException("Course with code " + courseDTO.getCourseCode() + " already exists");
        }
        Course course = convertToEntity(courseDTO);
        Course savedCourse = courseRepository.save(course);
        return convertToDTO(savedCourse);
    }

    @Override
    public CourseDTO updateCourse(Long id, CourseDTO courseDTO) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + id));
        
        // Check if course code is being changed and if new code already exists
        if (!course.getCourseCode().equals(courseDTO.getCourseCode()) &&
            courseRepository.findByCourseCode(courseDTO.getCourseCode()).isPresent()) {
            throw new IllegalArgumentException("Course with code " + courseDTO.getCourseCode() + " already exists");
        }

        updateCourseFromDTO(course, courseDTO);
        Course updatedCourse = courseRepository.save(course);
        return convertToDTO(updatedCourse);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDTO getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + id));
        return convertToDTO(course);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDTO getCourseByCourseCode(String courseCode) {
        Course course = courseRepository.findByCourseCode(courseCode)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with code: " + courseCode));
        return convertToDTO(course);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDTO> getCoursesByDepartment(String department) {
        return courseRepository.findByDepartment(department).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDTO> getCoursesByInstructor(Long instructorId) {
        return courseRepository.findByInstructorId(instructorId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new EntityNotFoundException("Course not found with id: " + id);
        }
        courseRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCourseCode(String courseCode) {
        return courseRepository.findByCourseCode(courseCode).isPresent();
    }

    private Course convertToEntity(CourseDTO courseDTO) {
        Course course = new Course();
        updateCourseFromDTO(course, courseDTO);
        return course;
    }

    private void updateCourseFromDTO(Course course, CourseDTO courseDTO) {
        course.setCourseName(courseDTO.getCourseName());
        course.setCourseCode(courseDTO.getCourseCode());
        course.setInstructorId(courseDTO.getInstructorId());
        course.setDepartment(courseDTO.getDepartment());
        course.setCredits(courseDTO.getCredits());
    }

    private CourseDTO convertToDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setCourseName(course.getCourseName());
        dto.setCourseCode(course.getCourseCode());
        dto.setInstructorId(course.getInstructorId());
        dto.setDepartment(course.getDepartment());
        dto.setCredits(course.getCredits());
        return dto;
    }
}
