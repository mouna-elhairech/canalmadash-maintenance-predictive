package com.canal.canalmadash.dto;

import java.time.LocalDateTime;

public class DemandeInterventionExterneRequest {

    /*
     * DTO envoye quand l'equipe veut preparer une intervention externe.
     * Il contient la machine, le fournisseur et le besoin a transmettre.
     */
    private int machineId;

    // Optionnel: relie la demande externe a une demande de maintenance interne.
    private Integer demandeMaintenanceId;
    private String fournisseurNom;
    private String fournisseurContact;
    private String fournisseurTelephone;
    private String fournisseurEmail;
    private String fournisseurAdresse;
    private String objet;

    // Description longue du probleme ou de la prestation attendue.
    private String description;
    private LocalDateTime dateSouhaitee;

    public int getMachineId() {
        return machineId;
    }

    public void setMachineId(int machineId) {
        this.machineId = machineId;
    }

    public Integer getDemandeMaintenanceId() {
        return demandeMaintenanceId;
    }

    public void setDemandeMaintenanceId(Integer demandeMaintenanceId) {
        this.demandeMaintenanceId = demandeMaintenanceId;
    }

    public String getFournisseurNom() {
        return fournisseurNom;
    }

    public void setFournisseurNom(String fournisseurNom) {
        this.fournisseurNom = fournisseurNom;
    }

    public String getFournisseurContact() {
        return fournisseurContact;
    }

    public void setFournisseurContact(String fournisseurContact) {
        this.fournisseurContact = fournisseurContact;
    }

    public String getFournisseurTelephone() {
        return fournisseurTelephone;
    }

    public void setFournisseurTelephone(String fournisseurTelephone) {
        this.fournisseurTelephone = fournisseurTelephone;
    }

    public String getFournisseurEmail() {
        return fournisseurEmail;
    }

    public void setFournisseurEmail(String fournisseurEmail) {
        this.fournisseurEmail = fournisseurEmail;
    }

    public String getFournisseurAdresse() {
        return fournisseurAdresse;
    }

    public void setFournisseurAdresse(String fournisseurAdresse) {
        this.fournisseurAdresse = fournisseurAdresse;
    }

    public String getObjet() {
        return objet;
    }

    public void setObjet(String objet) {
        this.objet = objet;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDateSouhaitee() {
        return dateSouhaitee;
    }

    public void setDateSouhaitee(LocalDateTime dateSouhaitee) {
        this.dateSouhaitee = dateSouhaitee;
    }
}
