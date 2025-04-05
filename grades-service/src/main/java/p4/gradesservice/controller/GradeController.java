package p4.gradesservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import p4.gradesservice.dto.ApiResponse;
import p4.gradesservice.dto.GradeDTO;
import p4.gradesservice.service.GradeService;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @PostMapping
    public ResponseEntity<ApiResponse<GradeDTO>> submitGrade(@Valid @RequestBody GradeDTO gradeDTO) {
        GradeDTO submitted = gradeService.submitGrade(gradeDTO);
        return ResponseEntity.ok(ApiResponse.success(submitted, "Grade submitted successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GradeDTO>> updateGrade(
            @PathVariable Long id,
            @Valid @RequestBody GradeDTO gradeDTO) {
        GradeDTO updated = gradeService.updateGrade(id, gradeDTO);
        return ResponseEntity.ok(ApiResponse.success(updated, "Grade updated successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GradeDTO>> getGradeById(@PathVariable Long id) {
        GradeDTO grade = gradeService.getGradeById(id);
        return ResponseEntity.ok(ApiResponse.success(grade));
    }

    @GetMapping("/student/{studentId}/course/{courseId}")
    public ResponseEntity<ApiResponse<GradeDTO>> getGradeByStudentAndCourse(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        GradeDTO grade = gradeService.getGradeByStudentAndCourse(studentId, courseId);
        return ResponseEntity.ok(ApiResponse.success(grade));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<List<GradeDTO>>> getGradesByStudent(@PathVariable Long studentId) {
        List<GradeDTO> grades = gradeService.getGradesByStudent(studentId);
        return ResponseEntity.ok(ApiResponse.success(grades));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<List<GradeDTO>>> getGradesByCourse(@PathVariable Long courseId) {
        List<GradeDTO> grades = gradeService.getGradesByCourse(courseId);
        return ResponseEntity.ok(ApiResponse.success(grades));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGrade(@PathVariable Long id) {
        gradeService.deleteGrade(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Grade deleted successfully"));
    }

    @GetMapping("/course/{courseId}/average")
    public ResponseEntity<ApiResponse<Double>> calculateCourseAverage(@PathVariable Long courseId) {
        double average = gradeService.calculateCourseAverage(courseId);
        return ResponseEntity.ok(ApiResponse.success(average));
    }

    @GetMapping("/student/{studentId}/gpa")
    public ResponseEntity<ApiResponse<Double>> calculateStudentGPA(@PathVariable Long studentId) {
        double gpa = gradeService.calculateStudentGPA(studentId);
        return ResponseEntity.ok(ApiResponse.success(gpa));
    }
}
