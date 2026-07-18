package com.hivehub.app.colmenas;

import com.hivehub.app.domain.Inventario;
import com.hivehub.app.domain.TipoDeInventario;
import com.hivehub.app.repository.InventarioRepository;
import com.hivehub.app.repository.TipoDeInventarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.hivehub.app.colmenas.IColmenaRepository;

import java.util.List;

public class ColmenaService implements IColmenaService {

    @Autowired
    private IColmenaRepository colmenaRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private TipoDeInventarioRepository tipoInventarioRepository;

    @Override
    public List<Colmena> findAll() {
        return colmenaRepository.findAll();
    }

    @Override
    public Colmena findById(Long id) {
        return colmenaRepository.findById(id).orElse(null);
    }

    @Override
    public Colmena save(Colmena colmena) {
        return colmenaRepository.save(colmena);
    }

    @Override
    public Colmena update(long id, Colmena updatedColmena) {
        return null; // Implementar si lo necesitas
    }

    @Override
    public Colmena saveDTO(ColmenaDTO colmena) {
        return null; // Implementar si lo necesitas
    }

    @Override
    public void delete(long id) {
        colmenaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Colmena updateDTO(long id, ColmenaDTO updatedColmena) {
        Colmena colmena = colmenaRepository.findById(id);
        
        if (colmena == null) {
            throw new RuntimeException("Colmena no encontrada");
        }

        colmena.setName(updatedColmena.getName());

        sincronizarInventario(colmena, "Colmena", null, updatedColmena.getCamaras() != null ? updatedColmena.getCamaras() : 0);
        sincronizarInventario(colmena, "Alza", updatedColmena.getMarcosAlza(), updatedColmena.getAlzas() != null ? updatedColmena.getAlzas() : 0);
        sincronizarInventario(colmena, "Núcleo", null, updatedColmena.getNucleos() != null ? updatedColmena.getNucleos() : 0);

        return colmenaRepository.save(colmena);
    }

    private void sincronizarInventario(Colmena colmena, String tipoNombre, Integer marcos, int cantidadDeseada) {
        TipoDeInventario tipoObjetivo;
        if (marcos != null) {
            tipoObjetivo = tipoInventarioRepository.findByNombreIgnoreCaseAndCantidadMarcos(tipoNombre, marcos)
                    .orElseThrow(() -> new RuntimeException("Tipo de inventario no encontrado: " + tipoNombre + " con " + marcos + " marcos"));
        } else {
            tipoObjetivo = tipoInventarioRepository.findByNombreIgnoreCase(tipoNombre)
                    .orElseThrow(() -> new RuntimeException("Tipo de inventario no encontrado: " + tipoNombre));
        }

        List<Inventario> inventariosActuales = inventarioRepository.findByColmenaIdAndTipoInventarioNombreIgnoreCase(colmena.getId(), tipoNombre);
        int cantidadActual = inventariosActuales.size();

        if (cantidadDeseada < cantidadActual) {
            int elementosAEliminar = cantidadActual - cantidadDeseada;
            for (int i = 0; i < elementosAEliminar; i++) {
                Inventario inv = inventariosActuales.get(i);
                inventarioRepository.delete(inv);
                colmena.getInventarios().remove(inv);
            }
        }

        if (cantidadDeseada > cantidadActual) {
            int elementosACrear = cantidadDeseada - cantidadActual;
            for (int i = 0; i < elementosACrear; i++) {
                Inventario nuevoInv = Inventario.builder()
                        .colmena(colmena)
                        .tipoInventario(tipoObjetivo)
                        .build();
                inventarioRepository.save(nuevoInv);
                colmena.getInventarios().add(nuevoInv);
            }
        }

        if (cantidadDeseada > 0) {
            List<Inventario> inventariosRestantes = inventarioRepository.findByColmenaIdAndTipoInventarioNombreIgnoreCase(colmena.getId(), tipoNombre);
            for (Inventario inv : inventariosRestantes) {
                if (!inv.getTipoInventario().getId().equals(tipoObjetivo.getId())) {
                    inv.setTipoInventario(tipoObjetivo);
                    inventarioRepository.save(inv);
                }
            }
        }
    }
}