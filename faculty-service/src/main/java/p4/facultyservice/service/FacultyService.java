package p4.facultyservice.service;

import p4.facultyservice.dto.FacultyDTO;
import java.util.List;

public interface FacultyService {
    FacultyDTO createFaculty(FacultyDTO facultyDTO);
    FacultyDTO updateFaculty(Long id, FacultyDTO facultyDTO);
    FacultyDTO getFacultyById(Long id);
    FacultyDTO getFacultyByEmail(String email);
    List<FacultyDTO> getFacultyByDepartment(String department);
    List<FacultyDTO> getFacultyByTitle(String title);
    List<FacultyDTO> getAllFaculty();
    void deleteFaculty(Long id);
    boolean existsByEmail(String email);
}
