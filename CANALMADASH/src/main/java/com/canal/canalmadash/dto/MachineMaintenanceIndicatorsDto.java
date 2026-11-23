package com.canal.canalmadash.dto;

public class MachineMaintenanceIndicatorsDto {

    /*
     * DTO des indicateurs calcules pour une machine.
     * Il resume les performances de maintenance: disponibilite, pannes,
     * temps d'arret, temps de reparation, couts et recommandations.
     */
    private int machineId;
    private String nom;
    private String ref;
    private long actionCount;
    private long failureCount;
    private long uptimeMinutes;
    private long downtimeMinutes;

    // Pourcentage de temps disponible sur la periode analysee.
    private double availabilityPercent;

    // MTBF: temps moyen entre pannes, en heures.
    private double mtbfHours;

    // MTTR: temps moyen de reparation, en heures.
    private double mttrHours;
    private double investmentCost;
    private double maintenanceCost;

    // Rapport cout maintenance / cout investissement.
    private double replacementRatioPercent;
    private String replacementRecommendation;
    private String replacementRecommendationLevel;
    private String availabilityRecommendation;
    private String availabilityRecommendationLevel;
    private String downtimeRecommendation;
    private String downtimeRecommendationLevel;
    private String failureRecommendation;
    private String failureRecommendationLevel;
    private String repairTimeRecommendation;
    private String repairTimeRecommendationLevel;

    // Seuils utilises pour expliquer pourquoi une recommandation est affichee.
    private double seuilDisponibiliteAttentionPercent;
    private double seuilDisponibiliteCritiquePercent;
    private long seuilTempsArretAttentionMinutes;
    private long seuilTempsArretCritiqueMinutes;
    private long seuilPannesAttention;
    private long seuilPannesCritique;
    private double seuilTempsReparationAttentionHours;
    private double seuilTempsReparationCritiqueHours;
    private double seuilRemplacementSurveillancePercent;
    private double seuilRemplacementAttentionPercent;
    private double seuilRemplacementCritiquePercent;

    public int getMachineId() {
        return machineId;
    }

    public void setMachineId(int machineId) {
        this.machineId = machineId;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public long getActionCount() {
        return actionCount;
    }

    public void setActionCount(long actionCount) {
        this.actionCount = actionCount;
    }

    public long getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(long failureCount) {
        this.failureCount = failureCount;
    }

    public long getUptimeMinutes() {
        return uptimeMinutes;
    }

    public void setUptimeMinutes(long uptimeMinutes) {
        this.uptimeMinutes = uptimeMinutes;
    }

    public long getDowntimeMinutes() {
        return downtimeMinutes;
    }

    public void setDowntimeMinutes(long downtimeMinutes) {
        this.downtimeMinutes = downtimeMinutes;
    }

    public double getAvailabilityPercent() {
        return availabilityPercent;
    }

    public void setAvailabilityPercent(double availabilityPercent) {
        this.availabilityPercent = availabilityPercent;
    }

    public double getMtbfHours() {
        return mtbfHours;
    }

    public void setMtbfHours(double mtbfHours) {
        this.mtbfHours = mtbfHours;
    }

    public double getMttrHours() {
        return mttrHours;
    }

    public void setMttrHours(double mttrHours) {
        this.mttrHours = mttrHours;
    }

    public double getInvestmentCost() {
        return investmentCost;
    }

    public void setInvestmentCost(double investmentCost) {
        this.investmentCost = investmentCost;
    }

    public double getMaintenanceCost() {
        return maintenanceCost;
    }

    public void setMaintenanceCost(double maintenanceCost) {
        this.maintenanceCost = maintenanceCost;
    }

    public double getReplacementRatioPercent() {
        return replacementRatioPercent;
    }

    public void setReplacementRatioPercent(double replacementRatioPercent) {
        this.replacementRatioPercent = replacementRatioPercent;
    }

    public String getReplacementRecommendation() {
        return replacementRecommendation;
    }

    public void setReplacementRecommendation(String replacementRecommendation) {
        this.replacementRecommendation = replacementRecommendation;
    }

    public String getReplacementRecommendationLevel() {
        return replacementRecommendationLevel;
    }

    public void setReplacementRecommendationLevel(String replacementRecommendationLevel) {
        this.replacementRecommendationLevel = replacementRecommendationLevel;
    }

    public String getAvailabilityRecommendation() {
        return availabilityRecommendation;
    }

    public void setAvailabilityRecommendation(String availabilityRecommendation) {
        this.availabilityRecommendation = availabilityRecommendation;
    }

    public String getAvailabilityRecommendationLevel() {
        return availabilityRecommendationLevel;
    }

    public void setAvailabilityRecommendationLevel(String availabilityRecommendationLevel) {
        this.availabilityRecommendationLevel = availabilityRecommendationLevel;
    }

    public String getDowntimeRecommendation() {
        return downtimeRecommendation;
    }

    public void setDowntimeRecommendation(String downtimeRecommendation) {
        this.downtimeRecommendation = downtimeRecommendation;
    }

    public String getDowntimeRecommendationLevel() {
        return downtimeRecommendationLevel;
    }

    public void setDowntimeRecommendationLevel(String downtimeRecommendationLevel) {
        this.downtimeRecommendationLevel = downtimeRecommendationLevel;
    }

    public String getFailureRecommendation() {
        return failureRecommendation;
    }

    public void setFailureRecommendation(String failureRecommendation) {
        this.failureRecommendation = failureRecommendation;
    }

    public String getFailureRecommendationLevel() {
        return failureRecommendationLevel;
    }

    public void setFailureRecommendationLevel(String failureRecommendationLevel) {
        this.failureRecommendationLevel = failureRecommendationLevel;
    }

    public String getRepairTimeRecommendation() {
        return repairTimeRecommendation;
    }

    public void setRepairTimeRecommendation(String repairTimeRecommendation) {
        this.repairTimeRecommendation = repairTimeRecommendation;
    }

    public String getRepairTimeRecommendationLevel() {
        return repairTimeRecommendationLevel;
    }

    public void setRepairTimeRecommendationLevel(String repairTimeRecommendationLevel) {
        this.repairTimeRecommendationLevel = repairTimeRecommendationLevel;
    }

    public double getSeuilDisponibiliteAttentionPercent() {
        return seuilDisponibiliteAttentionPercent;
    }

    public void setSeuilDisponibiliteAttentionPercent(double seuilDisponibiliteAttentionPercent) {
        this.seuilDisponibiliteAttentionPercent = seuilDisponibiliteAttentionPercent;
    }

    public double getSeuilDisponibiliteCritiquePercent() {
        return seuilDisponibiliteCritiquePercent;
    }

    public void setSeuilDisponibiliteCritiquePercent(double seuilDisponibiliteCritiquePercent) {
        this.seuilDisponibiliteCritiquePercent = seuilDisponibiliteCritiquePercent;
    }

    public long getSeuilTempsArretAttentionMinutes() {
        return seuilTempsArretAttentionMinutes;
    }

    public void setSeuilTempsArretAttentionMinutes(long seuilTempsArretAttentionMinutes) {
        this.seuilTempsArretAttentionMinutes = seuilTempsArretAttentionMinutes;
    }

    public long getSeuilTempsArretCritiqueMinutes() {
        return seuilTempsArretCritiqueMinutes;
    }

    public void setSeuilTempsArretCritiqueMinutes(long seuilTempsArretCritiqueMinutes) {
        this.seuilTempsArretCritiqueMinutes = seuilTempsArretCritiqueMinutes;
    }

    public long getSeuilPannesAttention() {
        return seuilPannesAttention;
    }

    public void setSeuilPannesAttention(long seuilPannesAttention) {
        this.seuilPannesAttention = seuilPannesAttention;
    }

    public long getSeuilPannesCritique() {
        return seuilPannesCritique;
    }

    public void setSeuilPannesCritique(long seuilPannesCritique) {
        this.seuilPannesCritique = seuilPannesCritique;
    }

    public double getSeuilTempsReparationAttentionHours() {
        return seuilTempsReparationAttentionHours;
    }

    public void setSeuilTempsReparationAttentionHours(double seuilTempsReparationAttentionHours) {
        this.seuilTempsReparationAttentionHours = seuilTempsReparationAttentionHours;
    }

    public double getSeuilTempsReparationCritiqueHours() {
        return seuilTempsReparationCritiqueHours;
    }

    public void setSeuilTempsReparationCritiqueHours(double seuilTempsReparationCritiqueHours) {
        this.seuilTempsReparationCritiqueHours = seuilTempsReparationCritiqueHours;
    }

    public double getSeuilRemplacementSurveillancePercent() {
        return seuilRemplacementSurveillancePercent;
    }

    public void setSeuilRemplacementSurveillancePercent(double seuilRemplacementSurveillancePercent) {
        this.seuilRemplacementSurveillancePercent = seuilRemplacementSurveillancePercent;
    }

    public double getSeuilRemplacementAttentionPercent() {
        return seuilRemplacementAttentionPercent;
    }

    public void setSeuilRemplacementAttentionPercent(double seuilRemplacementAttentionPercent) {
        this.seuilRemplacementAttentionPercent = seuilRemplacementAttentionPercent;
    }

    public double getSeuilRemplacementCritiquePercent() {
        return seuilRemplacementCritiquePercent;
    }

    public void setSeuilRemplacementCritiquePercent(double seuilRemplacementCritiquePercent) {
        this.seuilRemplacementCritiquePercent = seuilRemplacementCritiquePercent;
    }
}
