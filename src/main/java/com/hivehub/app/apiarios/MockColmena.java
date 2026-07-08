package com.hivehub.app.apiarios;

import jakarta.persistence.*;

//This class should disappear once the colmena original class is coded.

@Embeddable
public class MockColmena {

    private String mockName;

    // How it should look like on the real colmena class:

    //@ManyToOne
    //@JoinColumn(name = "fk_apiario", referencedColumnName = "id", nullable = true)
    //private Apiario apiario;
}
