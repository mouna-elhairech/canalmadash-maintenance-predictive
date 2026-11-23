package com.canal.canalmadash.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class TypeMaintenance extends AbstractTypeParametre {

    /*
     * Type d'intervention ou de demande maintenance.
     * Exemple: corrective, preventive, controle periodique ou nettoyage.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
