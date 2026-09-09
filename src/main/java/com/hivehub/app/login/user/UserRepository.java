/**
 * Spring Data JPA repository for {@link User} entities.
 * Provides CRUD operations and a custom finder by username.
 */
package com.hivehub.app.login.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String username);
    Optional<User> findByEmail(String email);
}
