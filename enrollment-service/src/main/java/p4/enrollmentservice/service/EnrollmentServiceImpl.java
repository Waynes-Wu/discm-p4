package p4.enrollmentservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import p4.enrollmentservice.dto.ApiResponse;
import p4.enrollmentservice.dto.EnrollmentDTO;
import p4.enrollmentservice.model.Enrollment;
import p4.enrollmentservice.repository.EnrollmentRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final RestTemplate restTemplate;

    @Override
    public EnrollmentDTO createEnrollment(EnrollmentDTO enrollmentDTO) {
        System.out.println("DEBUG: Creating enrollment for user " + enrollmentDTO.getUserId() + " in course "
                + enrollmentDTO.getCourseCode());

        // First check if user is already enrolled
        if (enrollmentRepository.existsByUserIdAndCourseCode(
                enrollmentDTO.getUserId(), enrollmentDTO.getCourseCode())) {
            System.out.println("DEBUG: User " + enrollmentDTO.getUserId() + " is already enrolled in course "
                    + enrollmentDTO.getCourseCode());
            throw new IllegalStateException("User is already enrolled in this course");
        }

        System.out.println("DEBUG: Checking if course exists: " + enrollmentDTO.getCourseCode());
        System.out.println("DEBUG: Calling course service URL: http://course-service:8082/api/courses/code/"
                + enrollmentDTO.getCourseCode() + "/name");

        // Then check if course exists
        try {
            ResponseEntity<ApiResponse<String>> response = restTemplate.exchange(
                    "http://course-service:8082/api/courses/code/" + enrollmentDTO.getCourseCode() + "/name",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<String>>() {
                    });

            System.out.println("DEBUG: Course service response status: " + response.getStatusCode());
            System.out.println("DEBUG: Course service response body: " + response.getBody());

            if (response.getBody() == null || !response.getBody().isSuccess()) {
                System.out.println("DEBUG: Course " + enrollmentDTO.getCourseCode() + " does not exist");
                throw new IllegalStateException("Course does not exist");
            }

            System.out.println("DEBUG: Course exists, proceeding with enrollment");
        } catch (Exception e) {
            System.out.println("DEBUG: Course service error: " + e.getMessage());
            System.out.println("DEBUG: Course " + enrollmentDTO.getCourseCode() + " does not exist");
            throw new IllegalStateException("Course does not exist");
        }

        Enrollment enrollment = convertToEntity(enrollmentDTO);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        System.out.println("DEBUG: Successfully created enrollment with ID: " + savedEnrollment.getId());
        return convertToDTO(savedEnrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public EnrollmentDTO getEnrollmentById(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found with id: " + id));
        return convertToDTO(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getEnrollmentsByUserId(Long userId) {
        System.out.println("DEBUG: Getting enrollments for user ID: " + userId);
        List<Enrollment> enrollments = enrollmentRepository.findByUserId(userId);
        System.out.println("DEBUG: Found " + enrollments.size() + " enrollments");
        enrollments.forEach(e -> System.out
                .println("DEBUG: Found enrollment: courseCode=" + e.getCourseCode() + ", userId=" + e.getUserId()));
        return enrollments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getEnrollmentsByCourseCode(String courseCode) {
        return enrollmentRepository.findByCourseCode(courseCode).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteEnrollment(Long id) {
        if (!enrollmentRepository.existsById(id)) {
            throw new EntityNotFoundException("Enrollment not found with id: " + id);
        }
        enrollmentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserEnrolledInCourse(Long userId, String courseCode) {
        System.out.println("DEBUG: Checking if user " + userId + " is enrolled in course " + courseCode);
        boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseCode(userId, courseCode);
        System.out.println("DEBUG: User " + userId + " enrolled in " + courseCode + ": " + isEnrolled);
        return isEnrolled;
    }

    private Enrollment convertToEntity(EnrollmentDTO dto) {
        Enrollment enrollment = new Enrollment();
        enrollment.setUserId(dto.getUserId());
        enrollment.setCourseCode(dto.getCourseCode());
        return enrollment;
    }

    private EnrollmentDTO convertToDTO(Enrollment enrollment) {
        EnrollmentDTO dto = new EnrollmentDTO();
        dto.setId(enrollment.getId());
        dto.setUserId(enrollment.getUserId());
        dto.setCourseCode(enrollment.getCourseCode());
        dto.setCreatedAt(enrollment.getCreatedAt());
        return dto;
    }
}
