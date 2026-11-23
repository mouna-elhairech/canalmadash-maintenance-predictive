package com.canal.canalmadash.entity;

import com.canal.canalmadash.Enum.PrioriteDemandeMaintenance;
import com.canal.canalmadash.Enum.StatutDemandeMaintenance;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class DemandeMaintenance {

    /*
     * Demande creee avant une intervention.
     * Elle represente le besoin exprime par un operateur ou par une regle
     * preventive: probleme observe, priorite, statut et dates de suivi.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;


    @JsonIgnore
    @ManyToOne(optional = false)
    private Machine machine;

    // Type de maintenance demande: corrective, preventive, controle, etc.
    @ManyToOne(optional = false)
    private TypeMaintenance typeMaintenance;

    @Column(nullable = false, length = 500)
    private String description;

    private LocalDateTime dateDemande = LocalDateTime.now();

    // Date visee pour planifier l'intervention.
    private LocalDateTime dateSouhaitee;

    // Dates reelles renseignees automatiquement selon les changements de statut.
    private LocalDateTime dateDebutReelle;
    private LocalDateTime dateFinReelle;

    @Enumerated(EnumType.STRING)
    private StatutDemandeMaintenance statut = StatutDemandeMaintenance.EN_ATTENTE;

    @Enumerated(EnumType.STRING)
    private PrioriteDemandeMaintenance priorite = PrioriteDemandeMaintenance.NORMALE;

    private String demandeur;

    private String imagePath;


    // Intervention realisee pour cette demande, si elle existe deja.
    @JsonIgnore
    @OneToOne(mappedBy = "demandeMaintenance")
    private Maintenance maintenance;

    @JsonIgnore
    @ManyToOne
    private RegleMaintenancePreventive reglePreventive;

    public Maintenance getMaintenance() {
        return maintenance;
    }

    public void setMaintenance(Maintenance maintenance) {
        this.maintenance = maintenance;
    }

    public RegleMaintenancePreventive getReglePreventive() {
        return reglePreventive;
    }

    public void setReglePreventive(RegleMaintenancePreventive reglePreventive) {
        this.reglePreventive = reglePreventive;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getDemandeur() {
        return demandeur;
    }

    public void setDemandeur(String demandeur) {
        this.demandeur = demandeur;
    }

    public StatutDemandeMaintenance getStatut() {
        return statut;
    }

    public void setStatut(StatutDemandeMaintenance statut) {
        this.statut = statut;
    }

    public PrioriteDemandeMaintenance getPriorite() {
        return priorite;
    }

    public void setPriorite(PrioriteDemandeMaintenance priorite) {
        this.priorite = priorite;
    }

    public LocalDateTime getDateFinReelle() {
        return dateFinReelle;
    }

    public void setDateFinReelle(LocalDateTime dateFinReelle) {
        this.dateFinReelle = dateFinReelle;
    }

    public LocalDateTime getDateDebutReelle() {
        return dateDebutReelle;
    }

    public void setDateDebutReelle(LocalDateTime dateDebutReelle) {
        this.dateDebutReelle = dateDebutReelle;
    }

    public LocalDateTime getDateSouhaitee() {
        return dateSouhaitee;
    }

    public void setDateSouhaitee(LocalDateTime dateSouhaitee) {
        this.dateSouhaitee = dateSouhaitee;
    }

    public LocalDateTime getDateDemande() {
        return dateDemande;
    }

    public void setDateDemande(LocalDateTime dateDemande) {
        this.dateDemande = dateDemande;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TypeMaintenance getTypeMaintenance() {
        return typeMaintenance;
    }

    public void setTypeMaintenance(TypeMaintenance typeMaintenance) {
        this.typeMaintenance = typeMaintenance;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
