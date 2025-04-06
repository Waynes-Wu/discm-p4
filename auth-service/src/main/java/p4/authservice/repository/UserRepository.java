package p4.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import p4.authservice.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    User findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    User findById(long id);
}
