package com.hivehub.app.apiarios;

import java.util.List;

public interface IApiarioService {
    List<Apiario> findAll();
    Apiario findById(Long id);
    Apiario save(Apiario apiario);
    Apiario update(long id, Apiario updatedApiario);
    void delete(long id);
}
