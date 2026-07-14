package com.hivehub.app.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad JPA que representa a un usuario de la aplicación.
 * Contiene identificador, nombre de usuario y contraseña.
 */
@Entity
@Table(name = "app_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
 * Obtiene la contraseña cifrada del usuario.
 */
public String getPassword() {
    return password;
}

    /**
 * Asigna la contraseña cifrada al usuario.
 * @param password contraseña ya codificada
 */
public void setPassword(String password) {
    this.password = password;
}
}
