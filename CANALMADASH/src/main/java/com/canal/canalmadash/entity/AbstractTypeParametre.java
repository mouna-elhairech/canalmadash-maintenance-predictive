package com.canal.canalmadash.entity;

import jakarta.persistence.*;

@MappedSuperclass
public abstract class AbstractTypeParametre {

    /*
     * Classe de base pour les tables de parametrage simples.
     * Exemple: TypeAction et TypeMaintenance partagent tous un code, un libelle,
     * un etat actif/inactif et une couleur d'affichage.
     */
    @Column(unique = true, nullable = false, length = 50)
    protected String code;

    @Column(nullable = false, length = 150)
    protected String libelle;

    protected boolean actif = true;

    // Couleur utilisee par le dashboard pour afficher le type dans les badges et graphiques.
    protected String color;



    public String getCode() {
        return code;
    }

    public String getLibelle() {
        return libelle;
    }

    public boolean isActif() {
        return actif;
    }



    public void setCode(String code) {
        this.code = code;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}

