package p4.facultyservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import p4.facultyservice.model.Faculty;
import java.util.List;
import java.util.Optional;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    Optional<Faculty> findByEmail(String email);
    List<Faculty> findByDepartment(String department);
    List<Faculty> findByTitle(String title);
    boolean existsByEmail(String email);
}
