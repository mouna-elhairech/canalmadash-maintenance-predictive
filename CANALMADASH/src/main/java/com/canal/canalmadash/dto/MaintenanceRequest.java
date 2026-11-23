package com.canal.canalmadash.dto;

import com.canal.canalmadash.Enum.TypeIntervention;
import java.util.ArrayList;
import java.util.List;

public class MaintenanceRequest {
    /*
     * DTO recu quand le frontend cree une intervention.
     * Les donnees texte passent ici; les images sont envoyees a part
     * dans la meme requete multipart.
     */
    private int machineId;

    // Optionnel: permet de transformer une demande existante en intervention.
    private Integer demandeMaintenanceId;

    // Le front envoie soit un type existant, soit un nouveau libelle propose.
    private Integer typeMaintenanceId;
    private String nouveauTypeLibelle;
    private String description;
    private String note;
    private String piecesUtilisees;
    private Double cout;
    private String causePanne;
    private String checklist;
    private Boolean controleEffectue;
    private Boolean nettoyageEffectue;
    private Boolean testFinalEffectue;
    private String validationSignature;
    private TypeIntervention typeIntervention;
    private String fournisseurNom;
    private String fournisseurContact;
    private String fournisseurTelephone;
    private String fournisseurEmail;
    private String fournisseurAdresse;
    private String referenceDevis;
    private String messageDemandeExterne;

    // Liste structuree des pieces consommees pendant l'intervention.
    private List<PieceUtiliseeRequest> pieces = new ArrayList<>();

    // Technicien ou profil qui a declare l'intervention.
    private String user;

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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPiecesUtilisees() {
        return piecesUtilisees;
    }

    public void setPiecesUtilisees(String piecesUtilisees) {
        this.piecesUtilisees = piecesUtilisees;
    }

    public Double getCout() {
        return cout;
    }

    public void setCout(Double cout) {
        this.cout = cout;
    }

    public String getCausePanne() {
        return causePanne;
    }

    public void setCausePanne(String causePanne) {
        this.causePanne = causePanne;
    }

    public String getChecklist() {
        return checklist;
    }

    public void setChecklist(String checklist) {
        this.checklist = checklist;
    }

    public Boolean getControleEffectue() {
        return controleEffectue;
    }

    public void setControleEffectue(Boolean controleEffectue) {
        this.controleEffectue = controleEffectue;
    }

    public Boolean getNettoyageEffectue() {
        return nettoyageEffectue;
    }

    public void setNettoyageEffectue(Boolean nettoyageEffectue) {
        this.nettoyageEffectue = nettoyageEffectue;
    }

    public Boolean getTestFinalEffectue() {
        return testFinalEffectue;
    }

    public void setTestFinalEffectue(Boolean testFinalEffectue) {
        this.testFinalEffectue = testFinalEffectue;
    }

    public String getValidationSignature() {
        return validationSignature;
    }

    public void setValidationSignature(String validationSignature) {
        this.validationSignature = validationSignature;
    }

    public TypeIntervention getTypeIntervention() {
        return typeIntervention;
    }

    public void setTypeIntervention(TypeIntervention typeIntervention) {
        this.typeIntervention = typeIntervention;
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

    public String getReferenceDevis() {
        return referenceDevis;
    }

    public void setReferenceDevis(String referenceDevis) {
        this.referenceDevis = referenceDevis;
    }

    public String getMessageDemandeExterne() {
        return messageDemandeExterne;
    }

    public void setMessageDemandeExterne(String messageDemandeExterne) {
        this.messageDemandeExterne = messageDemandeExterne;
    }

    public List<PieceUtiliseeRequest> getPieces() {
        return pieces;
    }

    public void setPieces(List<PieceUtiliseeRequest> pieces) {
        this.pieces = pieces;
    }
}
