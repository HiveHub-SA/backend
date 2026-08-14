package com.hivehub.app.apiarios;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IApiarioRepository extends JpaRepository<Apiario, Long> {

    List<Apiario> findAll();
    Apiario findById(long id);
    List<Apiario> findByIdIn(List<Long> ids);
}
