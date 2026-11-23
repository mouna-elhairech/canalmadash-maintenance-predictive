package com.canal.canalmadash.dto;

import java.util.ArrayList;
import java.util.List;

public class RoleApplicationRequest {

    /*
     * DTO utilise pour creer ou modifier un role applicatif.
     * Il contient les informations visibles du role et la liste des
     * fonctionnalites rattachees par leurs ids.
     */
    private String code;
    private String libelle;
    private String objectif;
    private boolean actif = true;

    // Le service transforme ces ids en entities FonctionnaliteApplication.
    private List<Long> fonctionnaliteIds = new ArrayList<>();

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

    public List<Long> getFonctionnaliteIds() {
        return fonctionnaliteIds;
    }

    public void setFonctionnaliteIds(List<Long> fonctionnaliteIds) {
        this.fonctionnaliteIds = fonctionnaliteIds;
    }
}
