package com.canal.canalmadash.dto;

import com.canal.canalmadash.Enum.CategorieFonctionnalite;

public class FonctionnaliteApplicationRequest {

    /*
     * DTO utilise pour creer ou modifier une fonctionnalite.
     * Une fonctionnalite peut etre une page du dashboard ou une action precise.
     */
    private String code;
    private String libelle;
    private String description;

    // Route Angular concernee quand la fonctionnalite represente une page.
    private String route;

    // Par defaut, une fonctionnalite est consideree comme une action.
    private CategorieFonctionnalite categorie = CategorieFonctionnalite.ACTION;
    private boolean actif = true;

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
}
