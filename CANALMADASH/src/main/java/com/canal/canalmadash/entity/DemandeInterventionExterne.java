package com.canal.canalmadash.entity;

import com.canal.canalmadash.Enum.StatutDemandeInterventionExterne;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class DemandeInterventionExterne {

    /*
     * Demande envoyee a un fournisseur externe.
     * Elle peut etre creee depuis une machine seule ou depuis une demande de
     * maintenance qui necessite un prestataire.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Machine machine;

    // Optionnel: demande interne qui a declenche le besoin d'intervention externe.
    @ManyToOne
    private DemandeMaintenance demandeMaintenance;

    private String fournisseurNom;
    private String fournisseurContact;
    private String fournisseurTelephone;
    private String fournisseurEmail;
    private String fournisseurAdresse;
    private String objet;

    // Description longue du probleme ou de la prestation attendue.
    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    private StatutDemandeInterventionExterne statut = StatutDemandeInterventionExterne.BROUILLON;

    // Date de creation du brouillon ou de la demande.
    private LocalDateTime dateCreation = LocalDateTime.now();
    private LocalDateTime dateSouhaitee;

    public Long getId() {
        return id;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    public DemandeMaintenance getDemandeMaintenance() {
        return demandeMaintenance;
    }

    public void setDemandeMaintenance(DemandeMaintenance demandeMaintenance) {
        this.demandeMaintenance = demandeMaintenance;
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

    public StatutDemandeInterventionExterne getStatut() {
        return statut;
    }

    public void setStatut(StatutDemandeInterventionExterne statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateSouhaitee() {
        return dateSouhaitee;
    }

    public void setDateSouhaitee(LocalDateTime dateSouhaitee) {
        this.dateSouhaitee = dateSouhaitee;
    }
}
