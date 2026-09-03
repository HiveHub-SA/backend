package com.hivehub.app.login.security;

import com.hivehub.app.login.user.User;
import com.hivehub.app.login.user.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Spring security llama a este metodo "loadUserByUsername" por contrato de UserDetailsService,
        // pero en nuestro caso, el "username" es el email del usuario.
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail()) // Usamos el email como nombre de usuario para Spring Security
                .password(user.getPassword())
                .authorities(Collections.emptyList()) // Necesario para Spring Security, aunque no usemos roles
                .build();
    }
}
