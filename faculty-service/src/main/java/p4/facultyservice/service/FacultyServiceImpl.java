package p4.facultyservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import p4.facultyservice.dto.FacultyDTO;
import p4.facultyservice.model.Faculty;
import p4.facultyservice.repository.FacultyRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FacultyServiceImpl implements FacultyService {

    private final FacultyRepository facultyRepository;

    @Override
    public FacultyDTO createFaculty(FacultyDTO facultyDTO) {
        if (facultyRepository.existsByEmail(facultyDTO.getEmail())) {
            throw new IllegalArgumentException("Faculty member with email " + facultyDTO.getEmail() + " already exists");
        }
        Faculty faculty = convertToEntity(facultyDTO);
        Faculty savedFaculty = facultyRepository.save(faculty);
        return convertToDTO(savedFaculty);
    }

    @Override
    public FacultyDTO updateFaculty(Long id, FacultyDTO facultyDTO) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Faculty not found with id: " + id));

        // Check if email is being changed and if new email already exists
        if (!faculty.getEmail().equals(facultyDTO.getEmail()) &&
            facultyRepository.existsByEmail(facultyDTO.getEmail())) {
            throw new IllegalArgumentException("Faculty member with email " + facultyDTO.getEmail() + " already exists");
        }

        updateFacultyFromDTO(faculty, facultyDTO);
        Faculty updatedFaculty = facultyRepository.save(faculty);
        return convertToDTO(updatedFaculty);
    }

    @Override
    @Transactional(readOnly = true)
    public FacultyDTO getFacultyById(Long id) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Faculty not found with id: " + id));
        return convertToDTO(faculty);
    }

    @Override
    @Transactional(readOnly = true)
    public FacultyDTO getFacultyByEmail(String email) {
        Faculty faculty = facultyRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Faculty not found with email: " + email));
        return convertToDTO(faculty);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacultyDTO> getFacultyByDepartment(String department) {
        return facultyRepository.findByDepartment(department).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacultyDTO> getFacultyByTitle(String title) {
        return facultyRepository.findByTitle(title).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacultyDTO> getAllFaculty() {
        return facultyRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteFaculty(Long id) {
        if (!facultyRepository.existsById(id)) {
            throw new EntityNotFoundException("Faculty not found with id: " + id);
        }
        facultyRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return facultyRepository.existsByEmail(email);
    }

    private Faculty convertToEntity(FacultyDTO facultyDTO) {
        Faculty faculty = new Faculty();
        updateFacultyFromDTO(faculty, facultyDTO);
        return faculty;
    }

    private void updateFacultyFromDTO(Faculty faculty, FacultyDTO facultyDTO) {
        faculty.setName(facultyDTO.getName());
        faculty.setEmail(facultyDTO.getEmail());
        faculty.setDepartment(facultyDTO.getDepartment());
        faculty.setTitle(facultyDTO.getTitle());
        faculty.setOfficeLocation(facultyDTO.getOfficeLocation());
        faculty.setOfficeHours(facultyDTO.getOfficeHours());
        faculty.setResearchInterests(facultyDTO.getResearchInterests());
    }

    private FacultyDTO convertToDTO(Faculty faculty) {
        FacultyDTO dto = new FacultyDTO();
        dto.setId(faculty.getId());
        dto.setName(faculty.getName());
        dto.setEmail(faculty.getEmail());
        dto.setDepartment(faculty.getDepartment());
        dto.setTitle(faculty.getTitle());
        dto.setOfficeLocation(faculty.getOfficeLocation());
        dto.setOfficeHours(faculty.getOfficeHours());
        dto.setResearchInterests(faculty.getResearchInterests());
        return dto;
    }
}
