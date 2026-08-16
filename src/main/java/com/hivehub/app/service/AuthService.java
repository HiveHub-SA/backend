package com.hivehub.app.service;

import com.hivehub.app.domain.Sesion;
import com.hivehub.app.domain.User;
import com.hivehub.app.repository.SesionRepository;
import com.hivehub.app.repository.UserRepository;
import com.hivehub.app.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

/**
 * Servicio de autenticación que gestiona login y logout.
 * El login genera un token JWT y registra una sesión activa.
 * El logout elimina la sesión, invalidando el token.
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final SesionRepository sesionRepository;
    private final UserRepository userRepository;
    private final long expirationMs;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       SesionRepository sesionRepository,
                       UserRepository userRepository,
                       @Value("${app.security.jwt.expiration-ms:86400000}") long expirationMs) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.sesionRepository = sesionRepository;
        this.userRepository = userRepository;
        this.expirationMs = expirationMs;
    }

    /**
     * Autentica al usuario y crea una sesión activa con el token JWT generado.
     *
     * @param username nombre de usuario
     * @param password contraseña
     * @return token JWT válido por 24 horas
     */
    @Transactional
    public String login(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al registrar sesión"));

        Sesion sesion = new Sesion();
        sesion.setTokenJWT(token);
        sesion.setTiempoExpiracion(Instant.now().plusMillis(expirationMs));
        sesion.setUser(user);
        sesionRepository.save(sesion);

        return token;
    }

    /**
     * Cierra la sesión eliminando el registro del token de la tabla de sesiones.
     * Tras esto, el token JWT deja de ser válido aunque no haya expirado.
     *
     * @param token el token JWT a invalidar
     */
    @Transactional
    public void logout(String token) {
        sesionRepository.deleteByTokenJWT(token);
    }
}
