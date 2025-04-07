package p4.gradesservice.service;

import p4.gradesservice.dto.GradeDTO;
import java.util.List;

public interface GradeService {
    GradeDTO submitGrade(GradeDTO gradeDTO);

    GradeDTO updateGrade(Long id, GradeDTO gradeDTO);

    GradeDTO getGradeById(Long id);

    GradeDTO getGradeByStudentAndCourse(Long studentId, String courseCode);

    List<GradeDTO> getGradesByStudent(Long studentId);

    List<GradeDTO> getGradesByCourse(String courseCode);

    void deleteGrade(Long id);

    double calculateCourseAverage(String courseCode);

    double calculateStudentGPA(Long studentId);
}
