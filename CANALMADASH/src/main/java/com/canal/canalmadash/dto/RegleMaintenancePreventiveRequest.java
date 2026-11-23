package com.canal.canalmadash.dto;

import com.canal.canalmadash.Enum.TypeReglePreventive;
import com.canal.canalmadash.Enum.UniteControlePeriodique;

import java.time.LocalDateTime;

public class RegleMaintenancePreventiveRequest {

    /*
     * DTO de creation/modification d'une regle preventive.
     * Il indique sur quelle machine appliquer la regle, quel type de maintenance
     * generer et selon quel rythme ou seuil.
     */
    private int machineId;
    private int typeMaintenanceId;
    private TypeReglePreventive typeRegle;

    // Valeur interpretee selon typeRegle: jours, heures de marche ou nombre de pannes.
    private Integer valeur;

    // Frequence et unite utilisees pour les controles periodiques.
    private Integer frequence;
    private UniteControlePeriodique unite;

    // Date a partir de laquelle la regle commence a compter.
    private LocalDateTime dateDebut;
    private String description;

    public int getMachineId() { return machineId; }
    public void setMachineId(int machineId) { this.machineId = machineId; }
    public int getTypeMaintenanceId() { return typeMaintenanceId; }
    public void setTypeMaintenanceId(int typeMaintenanceId) { this.typeMaintenanceId = typeMaintenanceId; }
    public TypeReglePreventive getTypeRegle() { return typeRegle; }
    public void setTypeRegle(TypeReglePreventive typeRegle) { this.typeRegle = typeRegle; }
    public Integer getValeur() { return valeur; }
    public void setValeur(Integer valeur) { this.valeur = valeur; }
    public Integer getFrequence() { return frequence; }
    public void setFrequence(Integer frequence) { this.frequence = frequence; }
    public UniteControlePeriodique getUnite() { return unite; }
    public void setUnite(UniteControlePeriodique unite) { this.unite = unite; }
    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
