package p4.facultyservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import p4.facultyservice.dto.ApiResponse;
import p4.facultyservice.dto.FacultyDTO;
import p4.facultyservice.service.FacultyService;

import java.util.List;

@RestController
@RequestMapping("/api/faculty")
@RequiredArgsConstructor
public class FacultyController {

    private final FacultyService facultyService;

    @PostMapping
    public ResponseEntity<ApiResponse<FacultyDTO>> createFaculty(@Valid @RequestBody FacultyDTO facultyDTO) {
        FacultyDTO created = facultyService.createFaculty(facultyDTO);
        return ResponseEntity.ok(ApiResponse.success(created, "Faculty member created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FacultyDTO>> updateFaculty(
            @PathVariable Long id,
            @Valid @RequestBody FacultyDTO facultyDTO) {
        FacultyDTO updated = facultyService.updateFaculty(id, facultyDTO);
        return ResponseEntity.ok(ApiResponse.success(updated, "Faculty member updated successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FacultyDTO>> getFacultyById(@PathVariable Long id) {
        FacultyDTO faculty = facultyService.getFacultyById(id);
        return ResponseEntity.ok(ApiResponse.success(faculty));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<FacultyDTO>> getFacultyByEmail(@PathVariable String email) {
        FacultyDTO faculty = facultyService.getFacultyByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(faculty));
    }

    @GetMapping("/department/{department}")
    public ResponseEntity<ApiResponse<List<FacultyDTO>>> getFacultyByDepartment(@PathVariable String department) {
        List<FacultyDTO> faculty = facultyService.getFacultyByDepartment(department);
        return ResponseEntity.ok(ApiResponse.success(faculty));
    }

    @GetMapping("/title/{title}")
    public ResponseEntity<ApiResponse<List<FacultyDTO>>> getFacultyByTitle(@PathVariable String title) {
        List<FacultyDTO> faculty = facultyService.getFacultyByTitle(title);
        return ResponseEntity.ok(ApiResponse.success(faculty));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FacultyDTO>>> getAllFaculty() {
        List<FacultyDTO> faculty = facultyService.getAllFaculty();
        return ResponseEntity.ok(ApiResponse.success(faculty));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFaculty(@PathVariable Long id) {
        facultyService.deleteFaculty(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Faculty member deleted successfully"));
    }
}
