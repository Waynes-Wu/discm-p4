package p4.gradesservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import p4.gradesservice.dto.GradeDTO;
import p4.gradesservice.exception.ResourceNotFoundException;
import p4.gradesservice.model.Grade;
import p4.gradesservice.model.Course;
import p4.gradesservice.repository.CourseRepository;
import p4.gradesservice.repository.GradeRepository;
import p4.gradesservice.service.GradeService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;
    private final CourseRepository courseRepository;

    @Override
    @Transactional
    public GradeDTO submitGrade(GradeDTO gradeDTO) {
        // Verify course exists
        if (!courseRepository.existsByCourseCode(gradeDTO.getCourseCode())) {
            throw new ResourceNotFoundException("Course not found with code: " + gradeDTO.getCourseCode());
        }

        Grade grade = new Grade();
        grade.setStudentId(gradeDTO.getStudentId());
        grade.setCourseCode(gradeDTO.getCourseCode());
        grade.setGrade(gradeDTO.getGrade());
        grade.setComments(gradeDTO.getComments());

        Grade saved = gradeRepository.save(grade);
        return convertToDTO(saved);
    }

    @Override
    @Transactional
    public GradeDTO updateGrade(Long id, GradeDTO gradeDTO) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + id));

        grade.setGrade(gradeDTO.getGrade());
        grade.setComments(gradeDTO.getComments());

        Grade updated = gradeRepository.save(grade);
        return convertToDTO(updated);
    }

    @Override
    public GradeDTO getGradeById(Long id) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + id));
        return convertToDTO(grade);
    }

    @Override
    public GradeDTO getGradeByStudentAndCourse(Long studentId, String courseCode) {
        Grade grade = gradeRepository.findByStudentIdAndCourseCode(studentId, courseCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Grade not found for student: " + studentId + " and course: " + courseCode));
        return convertToDTO(grade);
    }

    @Override
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
    @Transactional
    public void deleteGrade(Long id) {
        if (!gradeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Grade not found with id: " + id);
        }
        gradeRepository.deleteById(id);
    }

    @Override
    public double calculateCourseAverage(String courseCode) {
        List<Grade> grades = gradeRepository.findByCourseCode(courseCode);
        if (grades.isEmpty()) {
            return 0.0;
        }
        return grades.stream()
                .mapToDouble(Grade::getGrade)
                .average()
                .orElse(0.0);
    }

    @Override
    public double calculateStudentGPA(Long studentId) {
        List<Grade> grades = gradeRepository.findByStudentId(studentId);
        if (grades.isEmpty()) {
            return 0.0;
        }

        // Get total credits and weighted grade points
        double totalCredits = 0.0;
        double totalGradePoints = 0.0;

        for (Grade grade : grades) {
            Course course = courseRepository.findByCourseCode(grade.getCourseCode());
            if (course == null) {
                throw new ResourceNotFoundException("Course not found: " + grade.getCourseCode());
            }

            totalCredits += course.getCredits();
            totalGradePoints += grade.getGrade() * course.getCredits();
        }

        return totalCredits > 0 ? totalGradePoints / totalCredits : 0.0;
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