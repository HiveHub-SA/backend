/**
 * Spring Data JPA repository for {@link User} entities.
 * Provides CRUD operations and a custom finder by username.
 */
package com.hivehub.app.repository;

import com.hivehub.app.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
