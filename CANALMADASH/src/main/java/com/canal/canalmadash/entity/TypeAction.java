package com.canal.canalmadash.entity;

import jakarta.persistence.*;

@Entity
public class TypeAction extends AbstractTypeParametre{

    /*
     * Type d'etat d'une machine.
     * Exemple metier: EN_MARCHE, EN_PANNE, ARRET, MAINTENANCE.
     * Ces types alimentent l'etat courant de Machine et l'historique Action.
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
