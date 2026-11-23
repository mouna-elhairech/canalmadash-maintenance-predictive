package com.canal.canalmadash.service;

import com.canal.canalmadash.Enum.PrioriteDemandeMaintenance;
import com.canal.canalmadash.Enum.StatutDemandeMaintenance;
import com.canal.canalmadash.dto.DashboardMaintenanceDto;
import com.canal.canalmadash.dto.MachineMaintenanceIndicatorsDto;
import com.canal.canalmadash.entity.Action;
import com.canal.canalmadash.entity.DemandeMaintenance;
import com.canal.canalmadash.entity.Machine;
import com.canal.canalmadash.entity.Maintenance;
import com.canal.canalmadash.entity.ScoreCriticiteParametre;
import com.canal.canalmadash.entity.TypeAction;
import com.canal.canalmadash.repository.ActionRepository;
import com.canal.canalmadash.repository.DemandeMaintenanceRepository;
import com.canal.canalmadash.repository.MachineRepository;
import com.canal.canalmadash.repository.MaintenanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardMaintenanceService {
    /*
     * Service de calcul du tableau de bord maintenance.
     * Il transforme les donnees brutes (machines, actions, demandes,
     * interventions) en indicateurs lisibles: disponibilite, MTBF, MTTR,
     * couts, retards, alertes, calendrier et criticite.
     */
    private final MachineRepository machineRepository;
    private final ActionRepository actionRepository;
    private final DemandeMaintenanceRepository demandeMaintenanceRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final ScoreCriticiteParametreService scoreParametreService;

    public DashboardMaintenanceService(
            MachineRepository machineRepository,
            ActionRepository actionRepository,
            DemandeMaintenanceRepository demandeMaintenanceRepository,
            MaintenanceRepository maintenanceRepository,
            ScoreCriticiteParametreService scoreParametreService
    ) {
        this.machineRepository = machineRepository;
        this.actionRepository = actionRepository;
        this.demandeMaintenanceRepository = demandeMaintenanceRepository;
        this.maintenanceRepository = maintenanceRepository;
        this.scoreParametreService = scoreParametreService;
    }

    @Transactional
    public DashboardMaintenanceDto getDashboard() {
        /*
         * Point d'entree principal du dashboard.
         */
        List<Machine> machines = machineRepository.findAll();
        List<Action> actions = actionRepository.findAll();
        List<DemandeMaintenance> demandes = demandeMaintenanceRepository.findAll();
        List<Maintenance> maintenances = maintenanceRepository.findAll();
        ScoreCriticiteParametre scoreParametres = scoreParametreService.getOrCreate();
        LocalDateTime now = LocalDateTime.now();

        Map<Integer, MachineProblemAccumulator> problemByMachine = machines.stream()
                .collect(Collectors.toMap(
                        Machine::getId,
                        MachineProblemAccumulator::new,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        Map<Integer, List<Action>> actionsByMachine = actions.stream()
                .filter(action -> action.getMachine() != null)
                .collect(Collectors.groupingBy(action -> action.getMachine().getId()));

        Map<Integer, List<Maintenance>> maintenancesByMachine = maintenances.stream()
                .filter(maintenance -> maintenance.getMachine() != null)
                .collect(Collectors.groupingBy(maintenance -> maintenance.getMachine().getId()));

        List<MachineMaintenanceIndicatorsDto> machineIndicators = machines.stream()
                .map(machine -> calculateMachineIndicators(
                        machine,
                        actionsByMachine.getOrDefault(machine.getId(), List.of()),
                        maintenancesByMachine.getOrDefault(machine.getId(), List.of()),
                        now
                ))
                .toList();

        // Totaux globaux calcules a partir des indicateurs machine.
        long downtimeMinutes = machineIndicators.stream()
                .mapToLong(MachineMaintenanceIndicatorsDto::getDowntimeMinutes)
                .sum();
        long uptimeMinutes = machineIndicators.stream()
                .mapToLong(MachineMaintenanceIndicatorsDto::getUptimeMinutes)
                .sum();
        long failureCount = machineIndicators.stream()
                .mapToLong(MachineMaintenanceIndicatorsDto::getFailureCount)
                .sum();
        long totalMinutes = downtimeMinutes + uptimeMinutes;

        for (Action action : actions) {
            // Alimente le classement des machines qui posent le plus de problemes.
            long minutes = actionDurationMinutes(action, now);
            if (minutes <= 0) {
                continue;
            }

            boolean down = isDowntimeAction(action.getTypeAction());
            boolean failure = isFailureAction(action.getTypeAction());

            if (action.getMachine() != null) {
                MachineProblemAccumulator accumulator = problemByMachine.get(action.getMachine().getId());
                if (accumulator != null) {
                    if (down) {
                        accumulator.downtimeMinutes += minutes;
                    }
                    if (failure) {
                        accumulator.failureCount++;
                    }
                }
            }
        }

        DashboardMaintenanceDto dto = new DashboardMaintenanceDto();
        // Le DTO final regroupe toutes les zones visibles dans le dashboard Angular.
        dto.setTotalMachines(machines.size());//Nb machine
        dto.setTotalDowntimeMinutes(downtimeMinutes); //temps d'arret
        dto.setAvailabilityPercent(totalMinutes == 0 ? 0 : round1((uptimeMinutes * 100.0) / totalMinutes));//Disponibilite
        dto.setMtbfHours(failureCount == 0 ? 0 : round1((uptimeMinutes / 60.0) / failureCount));
        dto.setMttrHours(failureCount == 0 ? 0 : round1((downtimeMinutes / 60.0) / failureCount));
        dto.setOverdueDemandesCount(countOverdueDemandes(demandes, now));//Demandes en retard
        dto.setUpcomingDemandesCount(countUpcomingDemandes(demandes, now));//Planifiees cette semaine
        dto.setHighPriorityDemandesCount(countHighPriorityDemandes(demandes));//Urgentes / critiques
        dto.setTotalMaintenanceCost(totalMaintenanceCost(maintenances));
        dto.setLongDowntimeMachinesCount(buildLongDowntimeMachines(machines, now).size());
        dto.setMachinesByState(buildMachinesByState(machines));
        dto.setDemandesByStatus(buildDemandesByStatus(demandes));
        dto.setTopProblemMachines(buildTopProblemMachines(problemByMachine));
        dto.setOverdueDemandes(buildOverdueDemandes(demandes, now));
        dto.setUpcomingDemandes(buildUpcomingDemandes(demandes, now));
        dto.setCostByMachine(buildCostByMachine(maintenances));
        dto.setCostByType(buildCostByType(maintenances));
        dto.setCostByMonth(buildCostByMonth(maintenances));
        dto.setLongDowntimeMachines(buildLongDowntimeMachines(machines, now));
        dto.setPreventiveRecommendations(buildPreventiveRecommendations(machineIndicators));
        dto.setMachineIndicators(machineIndicators);
        dto.setNotifications(buildNotifications(demandes, machines, now));
        dto.setCalendarItems(buildCalendarItems(demandes, now));
        dto.setCriticalMachines(buildCriticalMachines(machineIndicators, maintenances, scoreParametres));
        dto.setCauseStats(buildCauseStats(maintenances));
        dto.setObjectives(buildObjectives(dto));

        return dto;
    }

    @Transactional(readOnly = true)
    public MachineMaintenanceIndicatorsDto getMachineIndicators(int machineId) {
        // Version detaillee pour une seule machine, utilisee dans la fiche machine.
        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new IllegalArgumentException("Machine introuvable id=" + machineId));
        List<Action> actions = actionRepository.findByMachineIdOrderByDateDebutAsc(machineId);
        List<Maintenance> maintenances = maintenanceRepository.findByMachineIdOrderByDateDebutDesc(machineId);
        return calculateMachineIndicators(machine, actions, maintenances, LocalDateTime.now());
    }

    private MachineMaintenanceIndicatorsDto calculateMachineIndicators(
            Machine machine,
            List<Action> actions,
            List<Maintenance> maintenances,
            LocalDateTime now
    ) {
        /*
         * Calcule les indicateurs de fiabilite d'une machine:
         * uptime, downtime, nombre de pannes, disponibilite, MTBF et MTTR.
         */
        long uptimeMinutes = 0;
        long downtimeMinutes = 0;
        long failureCount = 0;

        for (Action action : actions) {
            long minutes = actionDurationMinutes(action, now);
            if (minutes <= 0) {
                continue;
            }

            if (isDowntimeAction(action.getTypeAction())) {
                downtimeMinutes += minutes;
            } else {
                uptimeMinutes += minutes;
            }

            if (isFailureAction(action.getTypeAction())) {
                failureCount++;
            }
        }

        long totalMinutes = uptimeMinutes + downtimeMinutes;

        MachineMaintenanceIndicatorsDto dto = new MachineMaintenanceIndicatorsDto();
        dto.setMachineId(machine.getId());
        dto.setNom(machine.getNom());
        dto.setRef(machine.getRef());
        dto.setActionCount(actions.size());
        dto.setFailureCount(failureCount);
        dto.setUptimeMinutes(uptimeMinutes);
        dto.setDowntimeMinutes(downtimeMinutes);
        dto.setAvailabilityPercent(totalMinutes == 0 ? 0 : round1((uptimeMinutes * 100.0) / totalMinutes));
        dto.setMtbfHours(failureCount == 0 ? 0 : round1((uptimeMinutes / 60.0) / failureCount));
        dto.setMttrHours(failureCount == 0 ? 0 : round1((downtimeMinutes / 60.0) / failureCount));
        enrichThresholdAnalysis(dto, machine);
        enrichReplacementAnalysis(dto, machine, maintenances);
        return dto;
    }

    private void enrichReplacementAnalysis(
            MachineMaintenanceIndicatorsDto dto,
            Machine machine,
            List<Maintenance> maintenances
    ) {
        // Compare le cout de maintenance cumule au cout d'investissement de la machine.
        double investmentCost = round1(machine.getCoutTotalInvestissement());
        double maintenanceCost = totalMaintenanceCost(maintenances);
        double ratio = investmentCost <= 0 ? 0 : round1((maintenanceCost * 100.0) / investmentCost);

        dto.setInvestmentCost(investmentCost);
        dto.setMaintenanceCost(maintenanceCost);
        dto.setReplacementRatioPercent(ratio);
        dto.setSeuilRemplacementSurveillancePercent(machine.getSeuilRemplacementSurveillancePercent());
        dto.setSeuilRemplacementAttentionPercent(machine.getSeuilRemplacementAttentionPercent());
        dto.setSeuilRemplacementCritiquePercent(machine.getSeuilRemplacementCritiquePercent());

        if (investmentCost <= 0) {
            dto.setReplacementRecommendation("Renseigner les couts machine pour analyser le remplacement");
            dto.setReplacementRecommendationLevel("INFO");
            return;
        }
        if (ratio < machine.getSeuilRemplacementSurveillancePercent()) {
            dto.setReplacementRecommendation("Machine rentable a conserver");
            dto.setReplacementRecommendationLevel("OK");
        } else if (ratio < machine.getSeuilRemplacementAttentionPercent()) {
            dto.setReplacementRecommendation("Machine a surveiller");
            dto.setReplacementRecommendationLevel("SURVEILLANCE");
        } else if (ratio < machine.getSeuilRemplacementCritiquePercent()) {
            dto.setReplacementRecommendation("Planifier une etude de remplacement");
            dto.setReplacementRecommendationLevel("ATTENTION");
        } else {
            dto.setReplacementRecommendation("Remplacement fortement recommande");
            dto.setReplacementRecommendationLevel("CRITIQUE");
        }
    }

    private void enrichThresholdAnalysis(MachineMaintenanceIndicatorsDto dto, Machine machine) {
        /*
         * Compare les indicateurs calcules avec les seuils propres a la machine.
         * Chaque comparaison produit un message et un niveau pour l'interface.
         */
        dto.setSeuilDisponibiliteAttentionPercent(machine.getSeuilDisponibiliteAttentionPercent());
        dto.setSeuilDisponibiliteCritiquePercent(machine.getSeuilDisponibiliteCritiquePercent());
        dto.setSeuilTempsArretAttentionMinutes(machine.getSeuilTempsArretAttentionMinutes());
        dto.setSeuilTempsArretCritiqueMinutes(machine.getSeuilTempsArretCritiqueMinutes());
        dto.setSeuilPannesAttention(machine.getSeuilPannesAttention());
        dto.setSeuilPannesCritique(machine.getSeuilPannesCritique());
        dto.setSeuilTempsReparationAttentionHours(machine.getSeuilTempsReparationAttentionHours());
        dto.setSeuilTempsReparationCritiqueHours(machine.getSeuilTempsReparationCritiqueHours());

        if (dto.getAvailabilityPercent() <= 0) {
            dto.setAvailabilityRecommendation("Disponibilite non calculable");
            dto.setAvailabilityRecommendationLevel("INFO");
        } else if (dto.getAvailabilityPercent() < machine.getSeuilDisponibiliteCritiquePercent()) {
            dto.setAvailabilityRecommendation("Disponibilite critique");
            dto.setAvailabilityRecommendationLevel("CRITIQUE");
        } else if (dto.getAvailabilityPercent() < machine.getSeuilDisponibiliteAttentionPercent()) {
            dto.setAvailabilityRecommendation("Disponibilite a surveiller");
            dto.setAvailabilityRecommendationLevel("ATTENTION");
        } else {
            dto.setAvailabilityRecommendation("Disponibilite correcte");
            dto.setAvailabilityRecommendationLevel("OK");
        }

        if (dto.getDowntimeMinutes() >= machine.getSeuilTempsArretCritiqueMinutes()) {
            dto.setDowntimeRecommendation("Temps d'arret critique");
            dto.setDowntimeRecommendationLevel("CRITIQUE");
        } else if (dto.getDowntimeMinutes() >= machine.getSeuilTempsArretAttentionMinutes()) {
            dto.setDowntimeRecommendation("Temps d'arret a surveiller");
            dto.setDowntimeRecommendationLevel("ATTENTION");
        } else {
            dto.setDowntimeRecommendation("Temps d'arret acceptable");
            dto.setDowntimeRecommendationLevel("OK");
        }

        if (dto.getFailureCount() >= machine.getSeuilPannesCritique()) {
            dto.setFailureRecommendation("Nombre de pannes critique");
            dto.setFailureRecommendationLevel("CRITIQUE");
        } else if (dto.getFailureCount() >= machine.getSeuilPannesAttention()) {
            dto.setFailureRecommendation("Nombre de pannes a surveiller");
            dto.setFailureRecommendationLevel("ATTENTION");
        } else {
            dto.setFailureRecommendation("Nombre de pannes acceptable");
            dto.setFailureRecommendationLevel("OK");
        }

        if (dto.getFailureCount() <= 0 || dto.getMttrHours() <= 0) {
            dto.setRepairTimeRecommendation("Temps de reparation non calculable");
            dto.setRepairTimeRecommendationLevel("INFO");
        } else if (dto.getMttrHours() >= machine.getSeuilTempsReparationCritiqueHours()) {
            dto.setRepairTimeRecommendation("Reparation trop lente");
            dto.setRepairTimeRecommendationLevel("CRITIQUE");
        } else if (dto.getMttrHours() >= machine.getSeuilTempsReparationAttentionHours()) {
            dto.setRepairTimeRecommendation("Temps de reparation a surveiller");
            dto.setRepairTimeRecommendationLevel("ATTENTION");
        } else {
            dto.setRepairTimeRecommendation("Temps de reparation correct");
            dto.setRepairTimeRecommendationLevel("OK");
        }
    }

    private List<DashboardMaintenanceDto.StateStatDto> buildMachinesByState(List<Machine> machines) {
        // Compte les machines par etat actuel pour alimenter le graphique de repartition.
        return machines.stream()
                .collect(Collectors.groupingBy(
                        machine -> stateKey(machine.getTypeAction()),
                        LinkedHashMap::new,
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .map(entry -> {
                    TypeAction type = entry.getKey();
                    return new DashboardMaintenanceDto.StateStatDto(
                            safeCode(type),
                            safeLabel(type),
                            safeColor(type),
                            entry.getValue()
                    );
                })
                .sorted(Comparator.comparing(DashboardMaintenanceDto.StateStatDto::getCount).reversed())
                .toList();
    }

    private Map<String, Long> buildDemandesByStatus(List<DemandeMaintenance> demandes) {
        // Garde tous les statuts dans le resultat, meme ceux qui ont zero demande.
        Map<StatutDemandeMaintenance, Long> counts = demandes.stream()
                .collect(Collectors.groupingBy(
                        DemandeMaintenance::getStatut,
                        () -> new EnumMap<>(StatutDemandeMaintenance.class),
                        Collectors.counting()
                ));

        Map<String, Long> result = new LinkedHashMap<>();
        for (StatutDemandeMaintenance statut : StatutDemandeMaintenance.values()) {
            result.put(statut.name(), counts.getOrDefault(statut, 0L));
        }
        return result;
    }

    private long countOverdueDemandes(List<DemandeMaintenance> demandes, LocalDateTime now) {
        return demandes.stream().filter(demande -> isOverdue(demande, now)).count();
    }

    private long countUpcomingDemandes(List<DemandeMaintenance> demandes, LocalDateTime now) {
        LocalDateTime limit = now.plusDays(7);
        return demandes.stream()
                .filter(demande -> isOpenDemande(demande)
                        && demande.getDateSouhaitee() != null
                        && !demande.getDateSouhaitee().isBefore(now)
                        && !demande.getDateSouhaitee().isAfter(limit))
                .count();
    }

    private long countHighPriorityDemandes(List<DemandeMaintenance> demandes) {
        return demandes.stream()
                .filter(this::isOpenDemande)
                .filter(demande -> demande.getPriorite() == PrioriteDemandeMaintenance.URGENTE
                        || demande.getPriorite() == PrioriteDemandeMaintenance.CRITIQUE)
                .count();
    }

    private double totalMaintenanceCost(List<Maintenance> maintenances) {
        return round1(maintenances.stream()
                .filter(maintenance -> maintenance.getCout() != null)
                .mapToDouble(Maintenance::getCout)
                .sum());
    }

    private List<DashboardMaintenanceDto.CostStatDto> buildCostByMachine(List<Maintenance> maintenances) {
        return maintenances.stream()
                .filter(maintenance -> maintenance.getCout() != null)
                .collect(Collectors.groupingBy(
                        maintenance -> maintenance.getMachine() == null ? "Machine inconnue" : maintenance.getMachine().getNom(),
                        LinkedHashMap::new,
                        Collectors.summingDouble(Maintenance::getCout)
                ))
                .entrySet()
                .stream()
                .map(entry -> new DashboardMaintenanceDto.CostStatDto(entry.getKey(), round1(entry.getValue())))
                .sorted(Comparator.comparing(DashboardMaintenanceDto.CostStatDto::getCost).reversed())
                .limit(8)
                .toList();
    }

    private List<DashboardMaintenanceDto.CostStatDto> buildCostByType(List<Maintenance> maintenances) {
        return maintenances.stream()
                .filter(maintenance -> maintenance.getCout() != null)
                .collect(Collectors.groupingBy(
                        maintenance -> maintenance.getType() == null ? "Type inconnu" : maintenance.getType().getLibelle(),
                        LinkedHashMap::new,
                        Collectors.summingDouble(Maintenance::getCout)
                ))
                .entrySet()
                .stream()
                .map(entry -> new DashboardMaintenanceDto.CostStatDto(entry.getKey(), round1(entry.getValue())))
                .sorted(Comparator.comparing(DashboardMaintenanceDto.CostStatDto::getCost).reversed())
                .limit(8)
                .toList();
    }

    private List<DashboardMaintenanceDto.CostStatDto> buildCostByMonth(List<Maintenance> maintenances) {
        return maintenances.stream()
                .filter(maintenance -> maintenance.getCout() != null)
                .filter(maintenance -> maintenance.getDateDebut() != null)
                .collect(Collectors.groupingBy(
                        maintenance -> YearMonth.from(maintenance.getDateDebut()).toString(),
                        LinkedHashMap::new,
                        Collectors.summingDouble(Maintenance::getCout)
                ))
                .entrySet()
                .stream()
                .map(entry -> new DashboardMaintenanceDto.CostStatDto(entry.getKey(), round1(entry.getValue())))
                .sorted(Comparator.comparing(DashboardMaintenanceDto.CostStatDto::getLabel).reversed())
                .limit(12)
                .toList();
    }

    private List<DashboardMaintenanceDto.LongDowntimeMachineDto> buildLongDowntimeMachines(
            List<Machine> machines,
            LocalDateTime now
    ) {
        // Remonte les machines arretees plus longtemps que leur seuil d'attention.
        return machines.stream()
                .filter(machine -> isDowntimeAction(machine.getTypeAction()))
                .filter(machine -> machine.getDateDebutAction() != null)
                .map(machine -> new DashboardMaintenanceDto.LongDowntimeMachineDto(
                        machine.getId(),
                        machine.getNom(),
                        machine.getRef(),
                        safeLabel(machine.getTypeAction()),
                        Duration.between(machine.getDateDebutAction(), now).toMinutes()
                ))
                .filter(machine -> machines.stream()
                        .filter(source -> source.getId() == machine.getMachineId())
                        .findFirst()
                        .map(source -> machine.getMinutes() >= source.getSeuilTempsArretAttentionMinutes())
                        .orElse(false))
                .sorted(Comparator.comparing(DashboardMaintenanceDto.LongDowntimeMachineDto::getMinutes).reversed())
                .limit(8)
                .toList();
    }

    private List<DashboardMaintenanceDto.PreventiveRecommendationDto> buildPreventiveRecommendations(
            List<MachineMaintenanceIndicatorsDto> indicators
    ) {
        // Recommandations preventives basees sur les signaux faibles deja calcules.
        return indicators.stream()
                .filter(machine -> "ATTENTION".equals(machine.getFailureRecommendationLevel())
                        || "CRITIQUE".equals(machine.getFailureRecommendationLevel())
                        || "ATTENTION".equals(machine.getAvailabilityRecommendationLevel())
                        || "CRITIQUE".equals(machine.getAvailabilityRecommendationLevel())
                        || "ATTENTION".equals(machine.getRepairTimeRecommendationLevel())
                        || "CRITIQUE".equals(machine.getRepairTimeRecommendationLevel()))
                .sorted(Comparator
                        .comparingLong(MachineMaintenanceIndicatorsDto::getFailureCount)
                        .thenComparingDouble(MachineMaintenanceIndicatorsDto::getDowntimeMinutes)
                        .reversed())
                .limit(8)
                .map(machine -> new DashboardMaintenanceDto.PreventiveRecommendationDto(
                        machine.getMachineId(),
                        machine.getNom(),
                        machine.getRef(),
                        preventiveReason(machine),
                        "Planifier une maintenance preventive et verifier les pieces critiques."
                ))
                .toList();
    }

    private List<DashboardMaintenanceDto.NotificationDto> buildNotifications(
            List<DemandeMaintenance> demandes,
            List<Machine> machines,
            LocalDateTime now
    ) {
        /*
         * Flux d'alertes du dashboard.
         * On melange retards, longues pannes, interventions terminees et nouvelles
         * demandes, puis on garde les plus recentes.
         */
        List<DashboardMaintenanceDto.NotificationDto> notifications = new java.util.ArrayList<>();

        demandes.stream()
                .filter(demande -> isOverdue(demande, now))
                .limit(5)
                .forEach(demande -> notifications.add(new DashboardMaintenanceDto.NotificationDto(
                        "danger",
                        "Demande en retard",
                        safeMachineName(demande) + " - " + safeMaintenanceType(demande),
                        demande.getDateSouhaitee()
                )));

        machines.stream()
                .filter(machine -> isDowntimeAction(machine.getTypeAction()))
                .filter(machine -> machine.getDateDebutAction() != null)
                .filter(machine -> Duration.between(machine.getDateDebutAction(), now).toMinutes()
                        >= machine.getSeuilTempsArretAttentionMinutes())
                .map(machine -> new DashboardMaintenanceDto.NotificationDto(
                        "warning",
                        "Machine longtemps en panne",
                        machine.getNom() + " depuis " + formatDuration(Duration.between(machine.getDateDebutAction(), now).toMinutes()),
                        machine.getDateDebutAction()
                ))
                .limit(5)
                .forEach(notifications::add);

        demandes.stream()
                .filter(demande -> demande.getStatut() == StatutDemandeMaintenance.TERMINEE)
                .filter(demande -> demande.getDateFinReelle() != null && demande.getDateFinReelle().isAfter(now.minusDays(2)))
                .limit(5)
                .forEach(demande -> notifications.add(new DashboardMaintenanceDto.NotificationDto(
                        "success",
                        "Intervention terminee",
                        safeMachineName(demande) + " - " + safeMaintenanceType(demande),
                        demande.getDateFinReelle()
                )));

        demandes.stream()
                .filter(demande -> demande.getDateDemande() != null && demande.getDateDemande().isAfter(now.minusDays(2)))
                .limit(5)
                .forEach(demande -> notifications.add(new DashboardMaintenanceDto.NotificationDto(
                        "info",
                        "Nouvelle demande",
                        safeMachineName(demande) + " - " + safeMaintenanceType(demande),
                        demande.getDateDemande()
                )));

        return notifications.stream()
                .sorted(Comparator.comparing(DashboardMaintenanceDto.NotificationDto::getDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(12)
                .toList();
    }

    private List<DashboardMaintenanceDto.CalendarItemDto> buildCalendarItems(
            List<DemandeMaintenance> demandes,
            LocalDateTime now
    ) {
        // Calendrier court terme: demandes ouvertes prevues dans les 31 prochains jours.
        LocalDateTime limit = now.plusDays(31);
        return demandes.stream()
                .filter(this::isOpenDemande)
                .filter(demande -> demande.getDateSouhaitee() != null)
                .filter(demande -> !demande.getDateSouhaitee().isAfter(limit))
                .sorted(Comparator.comparing(DemandeMaintenance::getDateSouhaitee))
                .limit(50)
                .map(demande -> new DashboardMaintenanceDto.CalendarItemDto(
                        demande.getId(),
                        safeMachineName(demande),
                        safeMaintenanceType(demande),
                        demande.getStatut() == null ? "" : demande.getStatut().name(),
                        demande.getPriorite() == null ? PrioriteDemandeMaintenance.NORMALE.name() : demande.getPriorite().name(),
                        demande.getDateSouhaitee(),
                        isOverdue(demande, now)
                ))
                .toList();
    }

    private List<DashboardMaintenanceDto.CriticalMachineDto> buildCriticalMachines(
            List<MachineMaintenanceIndicatorsDto> indicators,
            List<Maintenance> maintenances,
            ScoreCriticiteParametre parametres
    ) {
        /*
         * Score de criticite configurable.
         * Les poids et facteurs viennent des parametres pour laisser le metier
         * ajuster l'importance du cout, des pannes, de l'arret et de la disponibilite.
         */
        Map<Integer, Double> costByMachine = maintenances.stream()
                .filter(maintenance -> maintenance.getMachine() != null)
                .filter(maintenance -> maintenance.getCout() != null)
                .collect(Collectors.groupingBy(
                        maintenance -> maintenance.getMachine().getId(),
                        Collectors.summingDouble(Maintenance::getCout)
                ));

        return indicators.stream()
                .map(machine -> {
                    double costScore = levelScore(machine.getReplacementRecommendationLevel(), parametres.getPoidsCoutMaintenance(), parametres);
                    double failureScore = levelScore(machine.getFailureRecommendationLevel(), parametres.getPoidsNombrePannes(), parametres);
                    double downtimeScore = levelScore(machine.getDowntimeRecommendationLevel(), parametres.getPoidsTempsArret(), parametres);
                    double availabilityScore = levelScore(machine.getAvailabilityRecommendationLevel(), parametres.getPoidsDisponibilite(), parametres);
                    double repairScore = levelScore(machine.getRepairTimeRecommendationLevel(), parametres.getPoidsTempsReparation(), parametres);
                    double score = round1(costScore + failureScore + downtimeScore + availabilityScore);
                    score = round1(score + repairScore);
                    return new DashboardMaintenanceDto.CriticalMachineDto(
                            machine.getMachineId(),
                            machine.getNom(),
                            machine.getRef(),
                            score,
                            criticalLevel(score, parametres),
                            criticalReason(machine, costByMachine.getOrDefault(machine.getMachineId(), 0.0))
                    );
                })
                .filter(machine -> machine.getScore() > 0)
                .sorted(Comparator.comparing(DashboardMaintenanceDto.CriticalMachineDto::getScore).reversed())
                .limit(8)
                .toList();
    }

    private List<DashboardMaintenanceDto.CostStatDto> buildCauseStats(List<Maintenance> maintenances) {
        // Classe les causes de panne les plus frequentes pour orienter les actions correctives.
        return maintenances.stream()
                .filter(maintenance -> maintenance.getCausePanne() != null && !maintenance.getCausePanne().isBlank())
                .collect(Collectors.groupingBy(
                        maintenance -> maintenance.getCausePanne().trim(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .map(entry -> new DashboardMaintenanceDto.CostStatDto(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(DashboardMaintenanceDto.CostStatDto::getCost).reversed())
                .limit(8)
                .toList();
    }

    private List<DashboardMaintenanceDto.ObjectiveDto> buildObjectives(DashboardMaintenanceDto dashboard) {
        // Objectifs rapides affiches comme indicateurs de pilotage.
        return List.of(
                new DashboardMaintenanceDto.ObjectiveDto(
                        "Disponibilite globale",
                        95,
                        dashboard.getAvailabilityPercent(),
                        "%",
                        dashboard.getAvailabilityPercent() >= 95
                ),
                new DashboardMaintenanceDto.ObjectiveDto(
                        "Demandes en retard",
                        0,
                        dashboard.getOverdueDemandesCount(),
                        "",
                        dashboard.getOverdueDemandesCount() == 0
                ),
                new DashboardMaintenanceDto.ObjectiveDto(
                        "Temps moyen de reparation",
                        4,
                        dashboard.getMttrHours(),
                        "h",
                        dashboard.getMttrHours() > 0 && dashboard.getMttrHours() <= 4
                )
        );
    }

    private String preventiveReason(MachineMaintenanceIndicatorsDto machine) {
        if ("ATTENTION".equals(machine.getFailureRecommendationLevel())
                || "CRITIQUE".equals(machine.getFailureRecommendationLevel())) {
            return machine.getFailureCount() + " pannes detectees";
        }
        if ("ATTENTION".equals(machine.getAvailabilityRecommendationLevel())
                || "CRITIQUE".equals(machine.getAvailabilityRecommendationLevel())) {
            return "Disponibilite faible: " + machine.getAvailabilityPercent() + "%";
        }
        return "Temps moyen de reparation eleve: " + machine.getMttrHours() + " h";
    }

    private String criticalLevel(double score, ScoreCriticiteParametre parametres) {
        if (score > parametres.getSeuilSurveillanceMax()) {
            return "Critique";
        }
        if (score > parametres.getSeuilNormalMax()) {
            return "A surveiller";
        }
        return "Normal";
    }

    private String criticalReason(MachineMaintenanceIndicatorsDto machine, double cost) {
        if ("ATTENTION".equals(machine.getFailureRecommendationLevel())
                || "CRITIQUE".equals(machine.getFailureRecommendationLevel())) {
            return machine.getFailureCount() + " pannes";
        }
        if ("ATTENTION".equals(machine.getDowntimeRecommendationLevel())
                || "CRITIQUE".equals(machine.getDowntimeRecommendationLevel())) {
            return formatDuration(machine.getDowntimeMinutes()) + " d'arret";
        }
        if (cost > 0) {
            return "Cout " + round1(cost) + " DH";
        }
        return "Indicateurs faibles";
    }

    private double levelScore(String level, double max, ScoreCriticiteParametre parametres) {
        // Transforme un niveau textuel en points selon le parametrage de criticite.
        if ("CRITIQUE".equals(level)) {
            return max * parametres.getFacteurCritique() / 100.0;
        }
        if ("ATTENTION".equals(level)) {
            return max * parametres.getFacteurAttention() / 100.0;
        }
        if ("SURVEILLANCE".equals(level)) {
            return max * parametres.getFacteurSurveillance() / 100.0;
        }
        return 0;
    }

    private String safeMachineName(DemandeMaintenance demande) {
        return demande.getMachine() == null ? "Machine inconnue" : demande.getMachine().getNom();
    }

    private String safeMaintenanceType(DemandeMaintenance demande) {
        return demande.getTypeMaintenance() == null ? "Maintenance" : demande.getTypeMaintenance().getLibelle();
    }

    private String formatDuration(long minutes) {
        if (minutes < 60) {
            return minutes + " min";
        }
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        if (hours < 24) {
            return hours + " h " + remainingMinutes + " min";
        }
        return (hours / 24) + " j " + (hours % 24) + " h";
    }

    private List<DashboardMaintenanceDto.DemandeSummaryDto> buildOverdueDemandes(
            List<DemandeMaintenance> demandes,
            LocalDateTime now
    ) {
        return demandes.stream()
                .filter(demande -> isOverdue(demande, now))
                .sorted(Comparator.comparing(DemandeMaintenance::getDateSouhaitee))
                .limit(6)
                .map(this::toDemandeSummary)
                .toList();
    }

    private List<DashboardMaintenanceDto.DemandeSummaryDto> buildUpcomingDemandes(
            List<DemandeMaintenance> demandes,
            LocalDateTime now
    ) {
        LocalDateTime limit = now.plusDays(7);
        return demandes.stream()
                .filter(demande -> isOpenDemande(demande)
                        && demande.getDateSouhaitee() != null
                        && !demande.getDateSouhaitee().isBefore(now)
                        && !demande.getDateSouhaitee().isAfter(limit))
                .sorted(Comparator.comparing(DemandeMaintenance::getDateSouhaitee))
                .limit(8)
                .map(this::toDemandeSummary)
                .toList();
    }

    private boolean isOverdue(DemandeMaintenance demande, LocalDateTime now) {
        return isOpenDemande(demande)
                && demande.getDateSouhaitee() != null
                && demande.getDateSouhaitee().isBefore(now);
    }

    private boolean isOpenDemande(DemandeMaintenance demande) {
        return demande.getStatut() != StatutDemandeMaintenance.TERMINEE
                && demande.getStatut() != StatutDemandeMaintenance.ANNULEE;
    }

    private DashboardMaintenanceDto.DemandeSummaryDto toDemandeSummary(DemandeMaintenance demande) {
        return new DashboardMaintenanceDto.DemandeSummaryDto(
                demande.getId(),
                demande.getMachine() == null ? "" : demande.getMachine().getNom(),
                demande.getMachine() == null ? "" : demande.getMachine().getRef(),
                demande.getTypeMaintenance() == null ? "" : demande.getTypeMaintenance().getLibelle(),
                demande.getStatut() == null ? "" : demande.getStatut().name(),
                demande.getPriorite() == null ? PrioriteDemandeMaintenance.NORMALE.name() : demande.getPriorite().name(),
                demande.getDescription(),
                demande.getDateDemande(),
                demande.getDateSouhaitee()
        );
    }

    private List<DashboardMaintenanceDto.ProblemMachineDto> buildTopProblemMachines(
            Map<Integer, MachineProblemAccumulator> problemByMachine
    ) {
        return problemByMachine.values()
                .stream()
                .filter(machine -> machine.failureCount > 0 || machine.downtimeMinutes > 0)
                .sorted(Comparator
                        .comparingLong((MachineProblemAccumulator machine) -> machine.downtimeMinutes)
                        .thenComparingLong(machine -> machine.failureCount)
                        .reversed())
                .limit(5)
                .map(machine -> new DashboardMaintenanceDto.ProblemMachineDto(
                        machine.machine.getId(),
                        machine.machine.getNom(),
                        machine.machine.getRef(),
                        machine.failureCount,
                        machine.downtimeMinutes
                ))
                .toList();
    }

    private long actionDurationMinutes(Action action, LocalDateTime now) {
        // Une action ouverte est consideree active jusqu'a maintenant.
        if (action.getDateDebut() == null) {
            return 0;
        }
        LocalDateTime end = action.getDateFin() == null ? now : action.getDateFin();
        if (end.isBefore(action.getDateDebut())) {
            return 0;
        }
        return Duration.between(action.getDateDebut(), end).toMinutes();
    }

    private boolean isDowntimeAction(TypeAction typeAction) {
        // Detection souple basee sur le code/libelle du type d'action.
        String value = normalizedTypeText(typeAction);
        return value.contains("PANNE")
                || value.contains("ARRET")
                || value.contains("STOP")
                || value.contains("MAINTENANCE")
                || value.contains("PRESTATION");
    }

    private boolean isFailureAction(TypeAction typeAction) {
        return normalizedTypeText(typeAction).contains("PANNE");
    }

    private String normalizedTypeText(TypeAction typeAction) {
        // Normalisation sans accents pour reconnaitre PANNE/ARRET meme si le libelle varie.
        if (typeAction == null) {
            return "";
        }
        return ((typeAction.getCode() == null ? "" : typeAction.getCode()) + " "
                + (typeAction.getLibelle() == null ? "" : typeAction.getLibelle()))
                .toUpperCase(Locale.ROOT)
                .transform(value -> Normalizer.normalize(value, Normalizer.Form.NFD))
                .replaceAll("\\p{M}", "");
    }

    private TypeAction stateKey(TypeAction typeAction) {
        return typeAction;
    }

    private String safeCode(TypeAction typeAction) {
        if (typeAction == null || typeAction.getCode() == null || typeAction.getCode().isBlank()) {
            return "INCONNU";
        }
        return typeAction.getCode();
    }

    private String safeLabel(TypeAction typeAction) {
        if (typeAction == null || typeAction.getLibelle() == null || typeAction.getLibelle().isBlank()) {
            return "Inconnu";
        }
        return typeAction.getLibelle();
    }

    private String safeColor(TypeAction typeAction) {
        if (typeAction == null || typeAction.getColor() == null || typeAction.getColor().isBlank()) {
            return "#64748b";
        }
        return typeAction.getColor();
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private static class MachineProblemAccumulator {
        private final Machine machine;
        private long failureCount;
        private long downtimeMinutes;

        private MachineProblemAccumulator(Machine machine) {
            this.machine = machine;
        }
    }
}
