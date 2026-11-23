package com.canal.canalmadash.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ScoreCriticiteParametre {

    /*
     * Parametrage global du score de criticite des machines.
     * Le backend conserve une seule ligne, avec id = 1, pour que le dashboard
     * puisse modifier les poids sans changer le code.
     */
    @Id
    private Long id = 1L;

    // Poids des criteres utilises dans le calcul du score.
    private double poidsCoutMaintenance = 20;
    private double poidsNombrePannes = 25;
    private double poidsTempsArret = 25;
    private double poidsDisponibilite = 20;
    private double poidsTempsReparation = 10;

    // Facteurs appliques selon le niveau d'alerte de chaque critere.
    private double facteurSurveillance = 45;
    private double facteurAttention = 70;
    private double facteurCritique = 100;

    // Bornes de classement final: normal, surveillance, attention/critique.
    private double seuilNormalMax = 39;
    private double seuilSurveillanceMax = 69;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getPoidsCoutMaintenance() {
        return poidsCoutMaintenance;
    }

    public void setPoidsCoutMaintenance(double poidsCoutMaintenance) {
        this.poidsCoutMaintenance = poidsCoutMaintenance;
    }

    public double getPoidsNombrePannes() {
        return poidsNombrePannes;
    }

    public void setPoidsNombrePannes(double poidsNombrePannes) {
        this.poidsNombrePannes = poidsNombrePannes;
    }

    public double getPoidsTempsArret() {
        return poidsTempsArret;
    }

    public void setPoidsTempsArret(double poidsTempsArret) {
        this.poidsTempsArret = poidsTempsArret;
    }

    public double getPoidsDisponibilite() {
        return poidsDisponibilite;
    }

    public void setPoidsDisponibilite(double poidsDisponibilite) {
        this.poidsDisponibilite = poidsDisponibilite;
    }

    public double getPoidsTempsReparation() {
        return poidsTempsReparation;
    }

    public void setPoidsTempsReparation(double poidsTempsReparation) {
        this.poidsTempsReparation = poidsTempsReparation;
    }

    public double getFacteurSurveillance() {
        return facteurSurveillance;
    }

    public void setFacteurSurveillance(double facteurSurveillance) {
        this.facteurSurveillance = facteurSurveillance;
    }

    public double getFacteurAttention() {
        return facteurAttention;
    }

    public void setFacteurAttention(double facteurAttention) {
        this.facteurAttention = facteurAttention;
    }

    public double getFacteurCritique() {
        return facteurCritique;
    }

    public void setFacteurCritique(double facteurCritique) {
        this.facteurCritique = facteurCritique;
    }

    public double getSeuilNormalMax() {
        return seuilNormalMax;
    }

    public void setSeuilNormalMax(double seuilNormalMax) {
        this.seuilNormalMax = seuilNormalMax;
    }

    public double getSeuilSurveillanceMax() {
        return seuilSurveillanceMax;
    }

    public void setSeuilSurveillanceMax(double seuilSurveillanceMax) {
        this.seuilSurveillanceMax = seuilSurveillanceMax;
    }
}
