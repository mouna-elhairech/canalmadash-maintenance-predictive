package com.canal.canalmadash.entity;

import com.canal.canalmadash.Enum.TypeReglePreventive;
import com.canal.canalmadash.Enum.UniteControlePeriodique;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class RegleMaintenancePreventive {

    /*
     * Regle qui sert a anticiper la maintenance.
     * Selon son type, elle peut generer ou recommander une demande apres une
     * duree, un nombre d'heures, un nombre de pannes ou un controle periodique.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Machine machine;

    // Type de maintenance a creer/recommander quand la regle se declenche.
    @ManyToOne(optional = false)
    private TypeMaintenance typeMaintenance;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private TypeReglePreventive typeRegle;

    // Valeur metier de la regle: jours, heures de marche ou nombre de pannes selon typeRegle.
    private Integer valeur;

    // Frequence utilisee pour les controles periodiques.
    private Integer frequence;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private UniteControlePeriodique unite;

    private LocalDateTime dateDebut;

    // Prochaine date calculee pour declencher ou afficher la maintenance preventive.
    private LocalDateTime prochaineDate;
    private boolean actif = true;

    // Evite de generer plusieurs fois la meme demande preventive.
    private LocalDateTime derniereGeneration;

    @Column(length = 500)
    private String description;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Machine getMachine() { return machine; }
    public void setMachine(Machine machine) { this.machine = machine; }
    public TypeMaintenance getTypeMaintenance() { return typeMaintenance; }
    public void setTypeMaintenance(TypeMaintenance typeMaintenance) { this.typeMaintenance = typeMaintenance; }
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
    public LocalDateTime getProchaineDate() { return prochaineDate; }
    public void setProchaineDate(LocalDateTime prochaineDate) { this.prochaineDate = prochaineDate; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    public LocalDateTime getDerniereGeneration() { return derniereGeneration; }
    public void setDerniereGeneration(LocalDateTime derniereGeneration) { this.derniereGeneration = derniereGeneration; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
