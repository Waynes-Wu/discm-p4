package p4.enrollmentservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import p4.enrollmentservice.dto.ApiResponse;
import p4.enrollmentservice.dto.EnrollmentDTO;
import p4.enrollmentservice.service.EnrollmentService;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<EnrollmentDTO>> createEnrollment(@Valid @RequestBody EnrollmentDTO enrollmentDTO) {
        EnrollmentDTO created = enrollmentService.createEnrollment(enrollmentDTO);
        return ResponseEntity.ok(ApiResponse.success(created, "Enrollment created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EnrollmentDTO>> getEnrollmentById(@PathVariable Long id) {
        EnrollmentDTO enrollment = enrollmentService.getEnrollmentById(id);
        return ResponseEntity.ok(ApiResponse.success(enrollment));
    }

    @GetMapping("/student/{userId}")
    public ResponseEntity<ApiResponse<List<EnrollmentDTO>>> getEnrollmentsByUserId(@PathVariable Long userId) {
        List<EnrollmentDTO> enrollments = enrollmentService.getEnrollmentsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<List<EnrollmentDTO>>> getEnrollmentsByCourseId(@PathVariable Long courseId) {
        List<EnrollmentDTO> enrollments = enrollmentService.getEnrollmentsByCourseId(courseId);
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEnrollment(@PathVariable Long id) {
        enrollmentService.deleteEnrollment(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Enrollment deleted successfully"));
    }

    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Boolean>> isUserEnrolledInCourse(
            @RequestParam Long userId,
            @RequestParam Long courseId) {
        boolean isEnrolled = enrollmentService.isUserEnrolledInCourse(userId, courseId);
        return ResponseEntity.ok(ApiResponse.success(isEnrolled));
    }
}
