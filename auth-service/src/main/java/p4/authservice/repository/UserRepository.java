package p4.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import p4.authservice.model.User;
import p4.authservice.model.UserType;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);

    boolean existsByEmail(String email);

    long countByType(UserType type);

    User findById(long id);
}
