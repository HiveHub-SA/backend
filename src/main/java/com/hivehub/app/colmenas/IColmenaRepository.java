package com.hivehub.app.colmenas;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IColmenaRepository extends JpaRepository<Colmena, Long> {

    List<Colmena> findAll();
    Colmena findById(long id);
}
