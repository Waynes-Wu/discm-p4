package p4.enrollmentservice.service;

import p4.enrollmentservice.dto.EnrollmentDTO;
import java.util.List;

public interface EnrollmentService {
    EnrollmentDTO createEnrollment(EnrollmentDTO enrollmentDTO);
    EnrollmentDTO getEnrollmentById(Long id);
    List<EnrollmentDTO> getEnrollmentsByUserId(Long userId);
    List<EnrollmentDTO> getEnrollmentsByCourseId(Long courseId);
    void deleteEnrollment(Long id);
    boolean isUserEnrolledInCourse(Long userId, Long courseId);
}
