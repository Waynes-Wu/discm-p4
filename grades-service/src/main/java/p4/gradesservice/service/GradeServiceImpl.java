package p4.gradesservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import p4.gradesservice.dto.GradeDTO;
import p4.gradesservice.model.Grade;
import p4.gradesservice.repository.GradeRepository;
import p4.gradesservice.exception.GradeNotFoundException;
import p4.gradesservice.exception.DuplicateGradeException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;

    @Override
    public GradeDTO submitGrade(GradeDTO gradeDTO) {
        if (gradeRepository.existsByStudentIdAndCourseCode(gradeDTO.getStudentId(), gradeDTO.getCourseCode())) {
            throw new DuplicateGradeException("Grade already exists for this student and course");
        }

        Grade grade = new Grade();
        grade.setStudentId(gradeDTO.getStudentId());
        grade.setCourseCode(gradeDTO.getCourseCode());
        grade.setGrade(gradeDTO.getGrade());
        grade.setComments(gradeDTO.getComments());

        Grade savedGrade = gradeRepository.save(grade);
        return convertToDTO(savedGrade);
    }

    @Override
    public GradeDTO updateGrade(Long id, GradeDTO gradeDTO) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new GradeNotFoundException("Grade not found with id: " + id));

        grade.setGrade(gradeDTO.getGrade());
        grade.setComments(gradeDTO.getComments());

        Grade updatedGrade = gradeRepository.save(grade);
        return convertToDTO(updatedGrade);
    }

    @Override
    @Transactional(readOnly = true)
    public GradeDTO getGradeById(Long id) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new GradeNotFoundException("Grade not found with id: " + id));
        return convertToDTO(grade);
    }

    @Override
    public GradeDTO getGradeByStudentAndCourse(Long studentId, String courseCode) {
        Grade grade = gradeRepository.findByStudentIdAndCourseCode(studentId, courseCode)
                .orElseThrow(() -> new GradeNotFoundException(
                        "Grade not found for studentId: " + studentId + " and courseCode: " + courseCode));
        return convertToDTO(grade);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradeDTO> getGradesByStudent(Long studentId) {
        return gradeRepository.findByStudentId(studentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<GradeDTO> getGradesByCourse(String courseCode) {
        return gradeRepository.findByCourseCode(courseCode).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteGrade(Long id) {
        if (!gradeRepository.existsById(id)) {
            throw new GradeNotFoundException("Grade not found with id: " + id);
        }
        gradeRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public double calculateCourseAverage(String courseCode) {
        List<Grade> grades = gradeRepository.findByCourseCode(courseCode);
        if (grades.isEmpty()) {
            throw new EntityNotFoundException("No grades found for course: " + courseCode);
        }
        return grades.stream()
                .mapToDouble(Grade::getGrade)
                .average()
                .orElse(0.0);
    }

    @Override
    @Transactional(readOnly = true)
    public double calculateStudentGPA(Long studentId) {
        List<Grade> grades = gradeRepository.findByStudentId(studentId);
        if (grades.isEmpty()) {
            throw new EntityNotFoundException("No grades found for student: " + studentId);
        }
        return grades.stream()
                .mapToDouble(Grade::getGrade)
                .average()
                .orElse(0.0);
    }

    private GradeDTO convertToDTO(Grade grade) {
        GradeDTO dto = new GradeDTO();
        dto.setId(grade.getId());
        dto.setStudentId(grade.getStudentId());
        dto.setCourseCode(grade.getCourseCode());
        dto.setGrade(grade.getGrade());
        dto.setComments(grade.getComments());
        dto.setSubmittedAt(grade.getSubmittedAt());
        dto.setLastModifiedAt(grade.getLastModifiedAt());
        return dto;
    }
}
