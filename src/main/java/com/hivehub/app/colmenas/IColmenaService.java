package com.hivehub.app.colmenas;

import java.util.List;

public interface IColmenaService{
    List<Colmena> findAll();
    Colmena findById(Long id);
    Colmena save(Colmena colmena   );
    Colmena update(long id, Colmena updatedColmena);
    Colmena saveDTO(ColmenaDTO colmena);
    Colmena updateDTO(long id, ColmenaDTO updatedColmena);
    void delete(long id);
}
