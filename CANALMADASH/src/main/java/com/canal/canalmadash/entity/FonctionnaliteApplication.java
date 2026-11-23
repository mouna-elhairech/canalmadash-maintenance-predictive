package com.canal.canalmadash.entity;

import com.canal.canalmadash.Enum.CategorieFonctionnalite;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class FonctionnaliteApplication {

    /*
     * Fonctionnalite visible ou utilisable dans l'application.
     * Elle peut representer une page, une action ou une permission metier.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 80)
    private String code;

    @Column(nullable = false)
    private String libelle;

    @Column(length = 1000)
    private String description;

    // Route Angular associee quand la fonctionnalite correspond a une page.
    private String route;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private CategorieFonctionnalite categorie = CategorieFonctionnalite.ACTION;

    private boolean actif = true;

    // Roles qui possedent cette fonctionnalite. Ignore en JSON pour eviter les cycles.
    @JsonIgnore
    @ManyToMany(mappedBy = "fonctionnalites")
    private List<RoleApplication> roles = new ArrayList<>();

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public CategorieFonctionnalite getCategorie() {
        return categorie;
    }

    public void setCategorie(CategorieFonctionnalite categorie) {
        this.categorie = categorie;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public List<RoleApplication> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleApplication> roles) {
        this.roles = roles;
    }
}
