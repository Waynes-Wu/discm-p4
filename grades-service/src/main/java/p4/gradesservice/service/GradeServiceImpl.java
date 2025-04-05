package p4.gradesservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import p4.gradesservice.dto.GradeDTO;
import p4.gradesservice.model.Grade;
import p4.gradesservice.repository.GradeRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;

    @Override
    public GradeDTO submitGrade(GradeDTO gradeDTO) {
        validateGrade(gradeDTO.getGrade());
        
        if (gradeRepository.existsByStudentIdAndCourseId(
                gradeDTO.getStudentId(), gradeDTO.getCourseId())) {
            throw new IllegalStateException("Grade already exists for this student and course");
        }

        Grade grade = convertToEntity(gradeDTO);
        Grade savedGrade = gradeRepository.save(grade);
        return convertToDTO(savedGrade);
    }

    @Override
    public GradeDTO updateGrade(Long id, GradeDTO gradeDTO) {
        validateGrade(gradeDTO.getGrade());
        
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Grade not found with id: " + id));

        updateGradeFromDTO(grade, gradeDTO);
        Grade updatedGrade = gradeRepository.save(grade);
        return convertToDTO(updatedGrade);
    }

    @Override
    @Transactional(readOnly = true)
    public GradeDTO getGradeById(Long id) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Grade not found with id: " + id));
        return convertToDTO(grade);
    }

    @Override
    @Transactional(readOnly = true)
    public GradeDTO getGradeByStudentAndCourse(Long studentId, Long courseId) {
        Grade grade = gradeRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new EntityNotFoundException(
                    "Grade not found for student " + studentId + " and course " + courseId));
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
    @Transactional(readOnly = true)
    public List<GradeDTO> getGradesByCourse(Long courseId) {
        return gradeRepository.findByCourseId(courseId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteGrade(Long id) {
        if (!gradeRepository.existsById(id)) {
            throw new EntityNotFoundException("Grade not found with id: " + id);
        }
        gradeRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public double calculateCourseAverage(Long courseId) {
        List<Grade> courseGrades = gradeRepository.findByCourseId(courseId);
        if (courseGrades.isEmpty()) {
            throw new IllegalStateException("No grades found for course: " + courseId);
        }
        return courseGrades.stream()
                .mapToDouble(Grade::getGrade)
                .average()
                .orElse(0.0);
    }

    @Override
    @Transactional(readOnly = true)
    public double calculateStudentGPA(Long studentId) {
        List<Grade> studentGrades = gradeRepository.findByStudentId(studentId);
        if (studentGrades.isEmpty()) {
            throw new IllegalStateException("No grades found for student: " + studentId);
        }
        return studentGrades.stream()
                .mapToDouble(this::convertGradeToGPA)
                .average()
                .orElse(0.0);
    }

    private void validateGrade(Double grade) {
        if (grade == null) {
            throw new IllegalArgumentException("Grade cannot be null");
        }
        if (grade < 0 || grade > 100) {
            throw new IllegalArgumentException("Grade must be between 0 and 100");
        }
    }

    private double convertGradeToGPA(Grade grade) {
        double numericGrade = grade.getGrade();
        if (numericGrade >= 93) return 4.0;
        if (numericGrade >= 90) return 3.7;
        if (numericGrade >= 87) return 3.3;
        if (numericGrade >= 83) return 3.0;
        if (numericGrade >= 80) return 2.7;
        if (numericGrade >= 77) return 2.3;
        if (numericGrade >= 73) return 2.0;
        if (numericGrade >= 70) return 1.7;
        if (numericGrade >= 67) return 1.3;
        if (numericGrade >= 63) return 1.0;
        if (numericGrade >= 60) return 0.7;
        return 0.0;
    }

    private Grade convertToEntity(GradeDTO gradeDTO) {
        Grade grade = new Grade();
        updateGradeFromDTO(grade, gradeDTO);
        return grade;
    }

    private void updateGradeFromDTO(Grade grade, GradeDTO gradeDTO) {
        grade.setStudentId(gradeDTO.getStudentId());
        grade.setCourseId(gradeDTO.getCourseId());
        grade.setGrade(gradeDTO.getGrade());
        grade.setComments(gradeDTO.getComments());
    }

    private GradeDTO convertToDTO(Grade grade) {
        GradeDTO dto = new GradeDTO();
        dto.setId(grade.getId());
        dto.setStudentId(grade.getStudentId());
        dto.setCourseId(grade.getCourseId());
        dto.setGrade(grade.getGrade());
        dto.setComments(grade.getComments());
        dto.setSubmittedAt(grade.getSubmittedAt());
        dto.setLastModifiedAt(grade.getLastModifiedAt());
        return dto;
    }
}
