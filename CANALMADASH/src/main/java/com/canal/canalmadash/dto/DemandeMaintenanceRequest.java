package com.canal.canalmadash.dto;

import com.canal.canalmadash.Enum.PrioriteDemandeMaintenance;

public class DemandeMaintenanceRequest {
    /*
     * DTO envoye par le web ou le mobile pour creer une demande de maintenance.
     * Il decrit le probleme, la machine concernee, le type de maintenance
     * et l'utilisateur qui fait la demande.
     */
    private int machineId;

    // Si le type existe deja, le front envoie son id.
    private Integer typeMaintenanceId;

    // Sinon, l'utilisateur peut proposer un nouveau type, cree inactif cote backend.
    private String nouveauTypeLibelle;
    private String description;
    private String demandeur;

    // Si elle est absente, le service met NORMALE par defaut.
    private PrioriteDemandeMaintenance priorite;

    public int getMachineId() {
        return machineId;
    }

    public void setMachineId(int machineId) {
        this.machineId = machineId;
    }

    public Integer getTypeMaintenanceId() {
        return typeMaintenanceId;
    }

    public void setTypeMaintenanceId(Integer typeMaintenanceId) {
        this.typeMaintenanceId = typeMaintenanceId;
    }

    public String getNouveauTypeLibelle() {
        return nouveauTypeLibelle;
    }

    public void setNouveauTypeLibelle(String nouveauTypeLibelle) {
        this.nouveauTypeLibelle = nouveauTypeLibelle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDemandeur() {
        return demandeur;
    }

    public void setDemandeur(String demandeur) {
        this.demandeur = demandeur;
    }

    public PrioriteDemandeMaintenance getPriorite() {
        return priorite;
    }

    public void setPriorite(PrioriteDemandeMaintenance priorite) {
        this.priorite = priorite;
    }
}
