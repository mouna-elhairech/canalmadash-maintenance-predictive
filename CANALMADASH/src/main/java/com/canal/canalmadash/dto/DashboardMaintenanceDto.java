package com.canal.canalmadash.dto;

import java.util.List;
import java.util.Map;

public class DashboardMaintenanceDto {

    /*
     * DTO de synthese du tableau de bord maintenance.
     * Il regroupe les indicateurs globaux, les listes d'alertes,
     * les statistiques de cout et les donnees du calendrier.
     */
    private long totalMachines;
    private double availabilityPercent;
    private long totalDowntimeMinutes;
    private double mtbfHours;
    private double mttrHours;
    private long overdueDemandesCount;
    private long upcomingDemandesCount;
    private long highPriorityDemandesCount;
    private double totalMaintenanceCost;
    private long longDowntimeMachinesCount;
    private List<StateStatDto> machinesByState;
    private Map<String, Long> demandesByStatus;
    private List<ProblemMachineDto> topProblemMachines;
    private List<DemandeSummaryDto> overdueDemandes;
    private List<DemandeSummaryDto> upcomingDemandes;
    private List<CostStatDto> costByMachine;
    private List<CostStatDto> costByType;
    private List<CostStatDto> costByMonth;
    private List<LongDowntimeMachineDto> longDowntimeMachines;
    private List<PreventiveRecommendationDto> preventiveRecommendations;
    private List<MachineMaintenanceIndicatorsDto> machineIndicators;
    private List<NotificationDto> notifications;
    private List<CalendarItemDto> calendarItems;
    private List<CriticalMachineDto> criticalMachines;
    private List<CostStatDto> causeStats;
    private List<ObjectiveDto> objectives;

    public long getTotalMachines() {
        return totalMachines;
    }

    public void setTotalMachines(long totalMachines) {
        this.totalMachines = totalMachines;
    }

    public double getAvailabilityPercent() {
        return availabilityPercent;
    }

    public void setAvailabilityPercent(double availabilityPercent) {
        this.availabilityPercent = availabilityPercent;
    }

    public long getTotalDowntimeMinutes() {
        return totalDowntimeMinutes;
    }

    public void setTotalDowntimeMinutes(long totalDowntimeMinutes) {
        this.totalDowntimeMinutes = totalDowntimeMinutes;
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

    public long getOverdueDemandesCount() {
        return overdueDemandesCount;
    }

    public void setOverdueDemandesCount(long overdueDemandesCount) {
        this.overdueDemandesCount = overdueDemandesCount;
    }

    public long getUpcomingDemandesCount() {
        return upcomingDemandesCount;
    }

    public void setUpcomingDemandesCount(long upcomingDemandesCount) {
        this.upcomingDemandesCount = upcomingDemandesCount;
    }

    public long getHighPriorityDemandesCount() {
        return highPriorityDemandesCount;
    }

    public void setHighPriorityDemandesCount(long highPriorityDemandesCount) {
        this.highPriorityDemandesCount = highPriorityDemandesCount;
    }

    public double getTotalMaintenanceCost() {
        return totalMaintenanceCost;
    }

    public void setTotalMaintenanceCost(double totalMaintenanceCost) {
        this.totalMaintenanceCost = totalMaintenanceCost;
    }

    public long getLongDowntimeMachinesCount() {
        return longDowntimeMachinesCount;
    }

    public void setLongDowntimeMachinesCount(long longDowntimeMachinesCount) {
        this.longDowntimeMachinesCount = longDowntimeMachinesCount;
    }

    public List<StateStatDto> getMachinesByState() {
        return machinesByState;
    }

    public void setMachinesByState(List<StateStatDto> machinesByState) {
        this.machinesByState = machinesByState;
    }

    public Map<String, Long> getDemandesByStatus() {
        return demandesByStatus;
    }

    public void setDemandesByStatus(Map<String, Long> demandesByStatus) {
        this.demandesByStatus = demandesByStatus;
    }

    public List<ProblemMachineDto> getTopProblemMachines() {
        return topProblemMachines;
    }

    public void setTopProblemMachines(List<ProblemMachineDto> topProblemMachines) {
        this.topProblemMachines = topProblemMachines;
    }

    public List<DemandeSummaryDto> getOverdueDemandes() {
        return overdueDemandes;
    }

    public void setOverdueDemandes(List<DemandeSummaryDto> overdueDemandes) {
        this.overdueDemandes = overdueDemandes;
    }

    public List<DemandeSummaryDto> getUpcomingDemandes() {
        return upcomingDemandes;
    }

    public void setUpcomingDemandes(List<DemandeSummaryDto> upcomingDemandes) {
        this.upcomingDemandes = upcomingDemandes;
    }

    public List<CostStatDto> getCostByMachine() {
        return costByMachine;
    }

    public void setCostByMachine(List<CostStatDto> costByMachine) {
        this.costByMachine = costByMachine;
    }

    public List<CostStatDto> getCostByType() {
        return costByType;
    }

    public void setCostByType(List<CostStatDto> costByType) {
        this.costByType = costByType;
    }

    public List<CostStatDto> getCostByMonth() {
        return costByMonth;
    }

    public void setCostByMonth(List<CostStatDto> costByMonth) {
        this.costByMonth = costByMonth;
    }

    public List<LongDowntimeMachineDto> getLongDowntimeMachines() {
        return longDowntimeMachines;
    }

    public void setLongDowntimeMachines(List<LongDowntimeMachineDto> longDowntimeMachines) {
        this.longDowntimeMachines = longDowntimeMachines;
    }

    public List<PreventiveRecommendationDto> getPreventiveRecommendations() {
        return preventiveRecommendations;
    }

    public void setPreventiveRecommendations(List<PreventiveRecommendationDto> preventiveRecommendations) {
        this.preventiveRecommendations = preventiveRecommendations;
    }

    public List<MachineMaintenanceIndicatorsDto> getMachineIndicators() {
        return machineIndicators;
    }

    public void setMachineIndicators(List<MachineMaintenanceIndicatorsDto> machineIndicators) {
        this.machineIndicators = machineIndicators;
    }

    public List<NotificationDto> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<NotificationDto> notifications) {
        this.notifications = notifications;
    }

    public List<CalendarItemDto> getCalendarItems() {
        return calendarItems;
    }

    public void setCalendarItems(List<CalendarItemDto> calendarItems) {
        this.calendarItems = calendarItems;
    }

    public List<CriticalMachineDto> getCriticalMachines() {
        return criticalMachines;
    }

    public void setCriticalMachines(List<CriticalMachineDto> criticalMachines) {
        this.criticalMachines = criticalMachines;
    }

    public List<CostStatDto> getCauseStats() {
        return causeStats;
    }

    public void setCauseStats(List<CostStatDto> causeStats) {
        this.causeStats = causeStats;
    }

    public List<ObjectiveDto> getObjectives() {
        return objectives;
    }

    public void setObjectives(List<ObjectiveDto> objectives) {
        this.objectives = objectives;
    }

    public static class StateStatDto {
        private String code;
        private String label;
        private String color;
        private long count;

        public StateStatDto(String code, String label, String color, long count) {
            this.code = code;
            this.label = label;
            this.color = color;
            this.count = count;
        }

        public String getCode() {
            return code;
        }

        public String getLabel() {
            return label;
        }

        public String getColor() {
            return color;
        }

        public long getCount() {
            return count;
        }
    }

    public static class ProblemMachineDto {
        private int id;
        private String nom;
        private String ref;
        private long failureCount;
        private long downtimeMinutes;

        public ProblemMachineDto(int id, String nom, String ref, long failureCount, long downtimeMinutes) {
            this.id = id;
            this.nom = nom;
            this.ref = ref;
            this.failureCount = failureCount;
            this.downtimeMinutes = downtimeMinutes;
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

        public long getFailureCount() {
            return failureCount;
        }

        public long getDowntimeMinutes() {
            return downtimeMinutes;
        }
    }

    public static class DemandeSummaryDto {
        private int id;
        private String machineNom;
        private String machineRef;
        private String typeMaintenance;
        private String statut;
        private String priorite;
        private String description;
        private java.time.LocalDateTime dateDemande;
        private java.time.LocalDateTime dateSouhaitee;

        public DemandeSummaryDto(
                int id,
                String machineNom,
                String machineRef,
                String typeMaintenance,
                String statut,
                String priorite,
                String description,
                java.time.LocalDateTime dateDemande,
                java.time.LocalDateTime dateSouhaitee
        ) {
            this.id = id;
            this.machineNom = machineNom;
            this.machineRef = machineRef;
            this.typeMaintenance = typeMaintenance;
            this.statut = statut;
            this.priorite = priorite;
            this.description = description;
            this.dateDemande = dateDemande;
            this.dateSouhaitee = dateSouhaitee;
        }

        public int getId() {
            return id;
        }

        public String getMachineNom() {
            return machineNom;
        }

        public String getMachineRef() {
            return machineRef;
        }

        public String getTypeMaintenance() {
            return typeMaintenance;
        }

        public String getStatut() {
            return statut;
        }

        public String getPriorite() {
            return priorite;
        }

        public String getDescription() {
            return description;
        }

        public java.time.LocalDateTime getDateDemande() {
            return dateDemande;
        }

        public java.time.LocalDateTime getDateSouhaitee() {
            return dateSouhaitee;
        }
    }

    public static class CostStatDto {
        private String label;
        private double cost;

        public CostStatDto(String label, double cost) {
            this.label = label;
            this.cost = cost;
        }

        public String getLabel() {
            return label;
        }

        public double getCost() {
            return cost;
        }
    }

    public static class LongDowntimeMachineDto {
        private int machineId;
        private String nom;
        private String ref;
        private String etat;
        private long minutes;

        public LongDowntimeMachineDto(int machineId, String nom, String ref, String etat, long minutes) {
            this.machineId = machineId;
            this.nom = nom;
            this.ref = ref;
            this.etat = etat;
            this.minutes = minutes;
        }

        public int getMachineId() {
            return machineId;
        }

        public String getNom() {
            return nom;
        }

        public String getRef() {
            return ref;
        }

        public String getEtat() {
            return etat;
        }

        public long getMinutes() {
            return minutes;
        }
    }

    public static class PreventiveRecommendationDto {
        private int machineId;
        private String nom;
        private String ref;
        private String reason;
        private String recommendation;

        public PreventiveRecommendationDto(int machineId, String nom, String ref, String reason, String recommendation) {
            this.machineId = machineId;
            this.nom = nom;
            this.ref = ref;
            this.reason = reason;
            this.recommendation = recommendation;
        }

        public int getMachineId() {
            return machineId;
        }

        public String getNom() {
            return nom;
        }

        public String getRef() {
            return ref;
        }

        public String getReason() {
            return reason;
        }

        public String getRecommendation() {
            return recommendation;
        }
    }

    public static class NotificationDto {
        private String level;
        private String title;
        private String message;
        private java.time.LocalDateTime date;

        public NotificationDto(String level, String title, String message, java.time.LocalDateTime date) {
            this.level = level;
            this.title = title;
            this.message = message;
            this.date = date;
        }

        public String getLevel() {
            return level;
        }

        public String getTitle() {
            return title;
        }

        public String getMessage() {
            return message;
        }

        public java.time.LocalDateTime getDate() {
            return date;
        }
    }

    public static class CalendarItemDto {
        private int demandeId;
        private String machineNom;
        private String typeMaintenance;
        private String statut;
        private String priorite;
        private java.time.LocalDateTime dateSouhaitee;
        private boolean retard;

        public CalendarItemDto(
                int demandeId,
                String machineNom,
                String typeMaintenance,
                String statut,
                String priorite,
                java.time.LocalDateTime dateSouhaitee,
                boolean retard
        ) {
            this.demandeId = demandeId;
            this.machineNom = machineNom;
            this.typeMaintenance = typeMaintenance;
            this.statut = statut;
            this.priorite = priorite;
            this.dateSouhaitee = dateSouhaitee;
            this.retard = retard;
        }

        public int getDemandeId() {
            return demandeId;
        }

        public String getMachineNom() {
            return machineNom;
        }

        public String getTypeMaintenance() {
            return typeMaintenance;
        }

        public String getStatut() {
            return statut;
        }

        public String getPriorite() {
            return priorite;
        }

        public java.time.LocalDateTime getDateSouhaitee() {
            return dateSouhaitee;
        }

        public boolean isRetard() {
            return retard;
        }
    }

    public static class CriticalMachineDto {
        private int machineId;
        private String nom;
        private String ref;
        private double score;
        private String niveau;
        private String raison;

        public CriticalMachineDto(int machineId, String nom, String ref, double score, String niveau, String raison) {
            this.machineId = machineId;
            this.nom = nom;
            this.ref = ref;
            this.score = score;
            this.niveau = niveau;
            this.raison = raison;
        }

        public int getMachineId() {
            return machineId;
        }

        public String getNom() {
            return nom;
        }

        public String getRef() {
            return ref;
        }

        public double getScore() {
            return score;
        }

        public String getNiveau() {
            return niveau;
        }

        public String getRaison() {
            return raison;
        }
    }

    public static class ObjectiveDto {
        private String label;
        private double target;
        private double value;
        private String unit;
        private boolean achieved;

        public ObjectiveDto(String label, double target, double value, String unit, boolean achieved) {
            this.label = label;
            this.target = target;
            this.value = value;
            this.unit = unit;
            this.achieved = achieved;
        }

        public String getLabel() {
            return label;
        }

        public double getTarget() {
            return target;
        }

        public double getValue() {
            return value;
        }

        public String getUnit() {
            return unit;
        }

        public boolean isAchieved() {
            return achieved;
        }
    }
}
