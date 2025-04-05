package p4.gradesservice.service;

import p4.gradesservice.dto.GradeDTO;
import java.util.List;

public interface GradeService {
    GradeDTO submitGrade(GradeDTO gradeDTO);
    GradeDTO updateGrade(Long id, GradeDTO gradeDTO);
    GradeDTO getGradeById(Long id);
    GradeDTO getGradeByStudentAndCourse(Long studentId, Long courseId);
    List<GradeDTO> getGradesByStudent(Long studentId);
    List<GradeDTO> getGradesByCourse(Long courseId);
    void deleteGrade(Long id);
    double calculateCourseAverage(Long courseId);
    double calculateStudentGPA(Long studentId);
}
