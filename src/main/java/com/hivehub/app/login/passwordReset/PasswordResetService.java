package com.hivehub.app.login.passwordReset;

import com.hivehub.app.login.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Random;

@Service
public class PasswordResetService {

    private final PasswordResetCodeRepository codeRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(PasswordResetCodeRepository codeRepository,
                                UserRepository userRepository,
                                JavaMailSender mailSender,
                                PasswordEncoder passwordEncoder) {
        this.codeRepository = codeRepository;
        this.userRepository = userRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void sendCode(String email) {
        // Si el email no existe no avisamos, por seguridad
        if (!userRepository.existsByEmail(email)) {
            return;
        }

        // Borra códigos anteriores del mismo email
        codeRepository.deleteAllByEmail(email);

        String code = String.format("%06d", new Random().nextInt(999999));

        PasswordResetCode reset = new PasswordResetCode();
        reset.setEmail(email);
        reset.setCode(code);
        reset.setExpiresAt(Instant.now().plusSeconds(30 * 60)); // 30 minutos
        codeRepository.save(reset);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Código de recuperación de contraseña");
        message.setText("Tu código de recuperación es: " + code +
                "\n\nEste código expira en 30 minutos." +
                "\nSi no solicitaste esto, ignorá este mensaje.");
        mailSender.send(message);
    }

    @Transactional(readOnly = true)
    public void verifyCode(String email, String code) {
        PasswordResetCode reset = codeRepository
                .findTopByEmailAndUsedFalseOrderByExpiresAtDesc(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido"));

        if (reset.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El código expiró");
        }

        if (!reset.getCode().equals(code)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código incorrecto");
        }
    }

    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        PasswordResetCode reset = codeRepository
                .findTopByEmailAndUsedFalseOrderByExpiresAtDesc(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido"));

        if (reset.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El código expiró");
        }

        if (!reset.getCode().equals(code)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código incorrecto");
        }

        // Marca como usado antes de cambiar la contraseña
        reset.setUsed(true);
        codeRepository.save(reset);

        userRepository.findByEmail(email).ifPresent(user -> {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        });
    }
}
