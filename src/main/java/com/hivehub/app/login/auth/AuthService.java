package com.hivehub.app.login.auth;

import com.hivehub.app.login.blacklist.TokenRevocado;
import com.hivehub.app.login.blacklist.TokenRevocadoRepository;
import com.hivehub.app.login.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final TokenRevocadoRepository tokenRevocadoRepository;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       TokenRevocadoRepository tokenRevocadoRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.tokenRevocadoRepository = tokenRevocadoRepository;
    }

    @Transactional
    public String login(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return jwtService.generateToken(userDetails);
    }

    @Transactional
    public void logout(String token) {
        Instant expiracion = jwtService.extractExpiration(token).toInstant();
        if (expiracion.isBefore(Instant.now())) {
            return;
        }
        TokenRevocado revocado = new TokenRevocado();
        revocado.setToken(token);
        revocado.setRevokedAt(Instant.now());
        revocado.setExpiracion(expiracion);
        tokenRevocadoRepository.save(revocado);
    }
}