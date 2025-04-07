package p4.enrollmentservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import p4.enrollmentservice.dto.ApiResponse;
import p4.enrollmentservice.dto.EnrollmentDTO;
import p4.enrollmentservice.service.EnrollmentService;
import p4.enrollmentservice.util.JwtUtil;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<ApiResponse<EnrollmentDTO>> createEnrollment(
            @Valid @RequestBody EnrollmentDTO enrollmentDTO,
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtUtil.extractTokenFromHeader(authHeader);
        if (token == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid authorization header"));
        }

        Long userId = jwtUtil.extractUserId(token);
        if (userId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("User ID not found in token"));
        }

        // Set the user ID from the token
        enrollmentDTO.setUserId(userId);

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

    @GetMapping("/course/{courseCode}")
    public ResponseEntity<ApiResponse<List<EnrollmentDTO>>> getEnrollmentsByCourseCode(
            @PathVariable String courseCode) {
        List<EnrollmentDTO> enrollments = enrollmentService.getEnrollmentsByCourseCode(courseCode);
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
            @RequestParam String courseCode) {
        boolean isEnrolled = enrollmentService.isUserEnrolledInCourse(userId, courseCode);
        return ResponseEntity.ok(ApiResponse.success(isEnrolled));
    }
}
