package com.hivehub.app.login.passwordReset;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, Long> {
    Optional<PasswordResetCode> findTopByEmailAndUsedFalseOrderByExpiresAtDesc(String email);
    void deleteAllByEmail(String email);
}
