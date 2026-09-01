package com.hivehub.app.login.security;

import com.hivehub.app.login.user.User;
import com.hivehub.app.login.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/*
Del framework spring security, es la interfaz que Spring Security usa para cargar los detalles
del usuario durante la autenticación. Implementamos esta interfaz para que Spring Security pueda
obtener los detalles del usuario desde nuestra base de datos.
*/

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(Collections.emptyList()) // Necesario para Spring Security, aunque no usemos roles
                .build();
    }
}
