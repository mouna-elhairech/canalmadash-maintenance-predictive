package com.canal.canalmadash.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class RoleApplication {

    /*
     * Role fonctionnel de l'application.
     * Il sert a decrire ce qu'un profil utilisateur peut faire ou voir dans
     * l'interface, par association avec des fonctionnalites.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 60)
    private String code;

    @Column(nullable = false)
    private String libelle;

    @Column(length = 1000)
    private String objectif;

    private boolean actif = true;

    // Relation many-to-many: un role donne acces a plusieurs fonctionnalites.
    @ManyToMany
    @JoinTable(
            name = "role_application_fonctionnalite",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "fonctionnalite_id")
    )
    private List<FonctionnaliteApplication> fonctionnalites = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getObjectif() {
        return objectif;
    }

    public void setObjectif(String objectif) {
        this.objectif = objectif;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public List<FonctionnaliteApplication> getFonctionnalites() {
        return fonctionnalites;
    }

    public void setFonctionnalites(List<FonctionnaliteApplication> fonctionnalites) {
        this.fonctionnalites = fonctionnalites;
    }
}
