package com.hivehub.app.autocompletado;

public interface AIFormFillerService {
    FormularioIADTO completarFormulario(String texto) throws IAServiceException;       
}
