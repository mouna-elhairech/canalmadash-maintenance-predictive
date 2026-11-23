package com.canal.canalmadash.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Machine extends AuditableEntity {

    /*
     * Entity principale du projet.
     * Une machine represente un equipement industriel suivi par l'application:
     * etat actuel, caracteristiques techniques, couts, seuils d'alerte,
     * historique des actions, maintenances et demandes.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nom;

    private String ref;

    @ManyToOne(optional = false)
    private TypeAction typeAction;   // état actuel

    // Date de debut de l'etat actuel, utilisee pour calculer la duree en cours.
    private LocalDateTime dateDebutAction;

    // Chemin relatif de l'image stockee par ImageStorageService.
    private String imagePath;

    private String emplacement;
    private String fabricant;
    private String modele;
    private String numeroSerie;
    private LocalDate dateInstallation;
    private String criticite;
    private Double heuresMarche;
    private Double coutAchat;
    private Double coutInstallation;
    private Double coutTransport;
    private Double coutFormation;
    private Double coutMiseEnService;

    /*
     * Seuils personnalisables par machine.
     * S'ils ne sont pas renseignes, les getters retournent des valeurs par defaut.
     */
    private Double seuilDisponibiliteAttentionPercent;
    private Double seuilDisponibiliteCritiquePercent;
    private Long seuilTempsArretAttentionMinutes;
    private Long seuilTempsArretCritiqueMinutes;
    private Long seuilPannesAttention;
    private Long seuilPannesCritique;
    private Double seuilTempsReparationAttentionHours;
    private Double seuilTempsReparationCritiqueHours;
    private Double seuilRemplacementSurveillancePercent;
    private Double seuilRemplacementAttentionPercent;
    private Double seuilRemplacementCritiquePercent;

    @Column(length = 1000)
    private String commentaireTechnique;

    // Historique des etats de la machine. Ignore en JSON pour eviter les boucles Machine -> Action -> Machine.
    @OneToMany(
            mappedBy = "machine",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    private List<Action> actions=new ArrayList<Action>();

    // Interventions deja realisees sur cette machine.
    @OneToMany(
            mappedBy = "machine",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    private List<Maintenance> maintenances=new ArrayList<Maintenance>();


    // Demandes de maintenance ouvertes ou terminees pour cette machine.
    @OneToMany(
            mappedBy = "machine",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    private List<DemandeMaintenance> demandeMaintenances=new ArrayList<DemandeMaintenance>();

    public Machine() {
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public void setTypeAction(TypeAction typeAction) {
        this.typeAction = typeAction;
    }

    public void setDateDebutAction(LocalDateTime dateDebutAction) {
        this.dateDebutAction = dateDebutAction;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setEmplacement(String emplacement) {
        this.emplacement = emplacement;
    }

    public void setFabricant(String fabricant) {
        this.fabricant = fabricant;
    }

    public void setModele(String modele) {
        this.modele = modele;
    }

    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    public void setDateInstallation(LocalDate dateInstallation) {
        this.dateInstallation = dateInstallation;
    }

    public void setCriticite(String criticite) {
        this.criticite = criticite;
    }

    public void setHeuresMarche(Double heuresMarche) {
        this.heuresMarche = heuresMarche;
    }

    public void setCoutAchat(Double coutAchat) {
        this.coutAchat = coutAchat;
    }

    public void setCoutInstallation(Double coutInstallation) {
        this.coutInstallation = coutInstallation;
    }

    public void setCoutTransport(Double coutTransport) {
        this.coutTransport = coutTransport;
    }

    public void setCoutFormation(Double coutFormation) {
        this.coutFormation = coutFormation;
    }

    public void setCoutMiseEnService(Double coutMiseEnService) {
        this.coutMiseEnService = coutMiseEnService;
    }

    public void setSeuilDisponibiliteAttentionPercent(Double seuilDisponibiliteAttentionPercent) {
        this.seuilDisponibiliteAttentionPercent = seuilDisponibiliteAttentionPercent;
    }

    public void setSeuilDisponibiliteCritiquePercent(Double seuilDisponibiliteCritiquePercent) {
        this.seuilDisponibiliteCritiquePercent = seuilDisponibiliteCritiquePercent;
    }

    public void setSeuilTempsArretAttentionMinutes(Long seuilTempsArretAttentionMinutes) {
        this.seuilTempsArretAttentionMinutes = seuilTempsArretAttentionMinutes;
    }

    public void setSeuilTempsArretCritiqueMinutes(Long seuilTempsArretCritiqueMinutes) {
        this.seuilTempsArretCritiqueMinutes = seuilTempsArretCritiqueMinutes;
    }

    public void setSeuilPannesAttention(Long seuilPannesAttention) {
        this.seuilPannesAttention = seuilPannesAttention;
    }

    public void setSeuilPannesCritique(Long seuilPannesCritique) {
        this.seuilPannesCritique = seuilPannesCritique;
    }

    public void setSeuilTempsReparationAttentionHours(Double seuilTempsReparationAttentionHours) {
        this.seuilTempsReparationAttentionHours = seuilTempsReparationAttentionHours;
    }

    public void setSeuilTempsReparationCritiqueHours(Double seuilTempsReparationCritiqueHours) {
        this.seuilTempsReparationCritiqueHours = seuilTempsReparationCritiqueHours;
    }

    public void setSeuilRemplacementSurveillancePercent(Double seuilRemplacementSurveillancePercent) {
        this.seuilRemplacementSurveillancePercent = seuilRemplacementSurveillancePercent;
    }

    public void setSeuilRemplacementAttentionPercent(Double seuilRemplacementAttentionPercent) {
        this.seuilRemplacementAttentionPercent = seuilRemplacementAttentionPercent;
    }

    public void setSeuilRemplacementCritiquePercent(Double seuilRemplacementCritiquePercent) {
        this.seuilRemplacementCritiquePercent = seuilRemplacementCritiquePercent;
    }

    public void setCommentaireTechnique(String commentaireTechnique) {
        this.commentaireTechnique = commentaireTechnique;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public void setMaintenances(List<Maintenance> maintenances) {
        this.maintenances = maintenances;
    }

    public void setDemandeMaintenances(List<DemandeMaintenance> demandeMaintenances) {
        this.demandeMaintenances = demandeMaintenances;
    }

    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getRef() {
        return ref;
    }

    public TypeAction getTypeAction() {
        return typeAction;
    }

    public LocalDateTime getDateDebutAction() {
        return dateDebutAction;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getEmplacement() {
        return emplacement;
    }

    public String getFabricant() {
        return fabricant;
    }

    public String getModele() {
        return modele;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public LocalDate getDateInstallation() {
        return dateInstallation;
    }

    public String getCriticite() {
        return criticite;
    }

    public Double getHeuresMarche() {
        return heuresMarche;
    }

    public Double getCoutAchat() {
        return coutAchat;
    }

    public Double getCoutInstallation() {
        return coutInstallation;
    }

    public Double getCoutTransport() {
        return coutTransport;
    }

    public Double getCoutFormation() {
        return coutFormation;
    }

    public Double getCoutMiseEnService() {
        return coutMiseEnService;
    }

    public Double getSeuilDisponibiliteAttentionPercent() {
        // Valeur par defaut si l'utilisateur n'a pas personnalise le seuil.
        return seuilDisponibiliteAttentionPercent == null ? 85 : seuilDisponibiliteAttentionPercent;
    }

    public Double getSeuilDisponibiliteCritiquePercent() {
        return seuilDisponibiliteCritiquePercent == null ? 70 : seuilDisponibiliteCritiquePercent;
    }

    public Long getSeuilTempsArretAttentionMinutes() {
        return seuilTempsArretAttentionMinutes == null ? 120 : seuilTempsArretAttentionMinutes;
    }

    public Long getSeuilTempsArretCritiqueMinutes() {
        return seuilTempsArretCritiqueMinutes == null ? 480 : seuilTempsArretCritiqueMinutes;
    }

    public Long getSeuilPannesAttention() {
        return seuilPannesAttention == null ? 3 : seuilPannesAttention;
    }

    public Long getSeuilPannesCritique() {
        return seuilPannesCritique == null ? 6 : seuilPannesCritique;
    }

    public Double getSeuilTempsReparationAttentionHours() {
        return seuilTempsReparationAttentionHours == null ? 4 : seuilTempsReparationAttentionHours;
    }

    public Double getSeuilTempsReparationCritiqueHours() {
        return seuilTempsReparationCritiqueHours == null ? 8 : seuilTempsReparationCritiqueHours;
    }

    public Double getSeuilRemplacementSurveillancePercent() {
        return seuilRemplacementSurveillancePercent == null ? 40 : seuilRemplacementSurveillancePercent;
    }

    public Double getSeuilRemplacementAttentionPercent() {
        return seuilRemplacementAttentionPercent == null ? 70 : seuilRemplacementAttentionPercent;
    }

    public Double getSeuilRemplacementCritiquePercent() {
        return seuilRemplacementCritiquePercent == null ? 100 : seuilRemplacementCritiquePercent;
    }

    public double getCoutTotalInvestissement() {
        // Cout global d'acquisition et de mise en service de la machine.
        return safeCost(coutAchat)
                + safeCost(coutInstallation)
                + safeCost(coutTransport)
                + safeCost(coutFormation)
                + safeCost(coutMiseEnService);
    }

    private double safeCost(Double value) {
        // Evite les NullPointerException dans les calculs de cout.
        return value == null ? 0 : value;
    }

    public String getCommentaireTechnique() {
        return commentaireTechnique;
    }

    public List<Action> getActions() {
        return actions;
    }

    public List<Maintenance> getMaintenances() {
        return maintenances;
    }

    public List<DemandeMaintenance> getDemandeMaintenances() {
        return demandeMaintenances;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Machine other)) {
            return false;
        }
        return id != 0 && id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "Machine{id=" + id + ", nom='" + nom + "', ref='" + ref + "'}";
    }
}
