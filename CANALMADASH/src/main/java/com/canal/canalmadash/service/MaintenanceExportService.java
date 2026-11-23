package com.canal.canalmadash.service;

import com.canal.canalmadash.dto.DashboardMaintenanceDto;
import com.canal.canalmadash.entity.Action;
import com.canal.canalmadash.entity.DemandeMaintenance;
import com.canal.canalmadash.entity.Machine;
import com.canal.canalmadash.entity.Maintenance;
import com.canal.canalmadash.repository.ActionRepository;
import com.canal.canalmadash.repository.DemandeMaintenanceRepository;
import com.canal.canalmadash.repository.MachineRepository;
import com.canal.canalmadash.repository.MaintenanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MaintenanceExportService {

    /*
     * Service d'export CSV.
     * Il prepare des fichiers simples a ouvrir dans Excel: dashboard,
     * demandes en retard et historique complet d'une machine.
     */
    private final DashboardMaintenanceService dashboardMaintenanceService;
    private final MachineRepository machineRepository;
    private final ActionRepository actionRepository;
    private final DemandeMaintenanceRepository demandeMaintenanceRepository;
    private final MaintenanceRepository maintenanceRepository;

    public MaintenanceExportService(
            DashboardMaintenanceService dashboardMaintenanceService,
            MachineRepository machineRepository,
            ActionRepository actionRepository,
            DemandeMaintenanceRepository demandeMaintenanceRepository,
            MaintenanceRepository maintenanceRepository
    ) {
        this.dashboardMaintenanceService = dashboardMaintenanceService;
        this.machineRepository = machineRepository;
        this.actionRepository = actionRepository;
        this.demandeMaintenanceRepository = demandeMaintenanceRepository;
        this.maintenanceRepository = maintenanceRepository;
    }

    @Transactional(readOnly = true)
    public byte[] dashboardCsv() {
        // Export synthetique des indicateurs principaux du dashboard.
        DashboardMaintenanceDto dashboard = dashboardMaintenanceService.getDashboard();
        StringBuilder csv = new StringBuilder();
        csv.append("Indicateur;Valeur\n");
        row(csv, "Machines", dashboard.getTotalMachines());
        row(csv, "Disponibilite", dashboard.getAvailabilityPercent() + "%");
        row(csv, "Temps d'arret minutes", dashboard.getTotalDowntimeMinutes());
        row(csv, "Temps moyen entre pannes", dashboard.getMtbfHours() + " h");
        row(csv, "Temps moyen de reparation", dashboard.getMttrHours() + " h");
        row(csv, "Demandes en retard", dashboard.getOverdueDemandesCount());
        row(csv, "Planifiees cette semaine", dashboard.getUpcomingDemandesCount());
        row(csv, "Urgentes critiques", dashboard.getHighPriorityDemandesCount());
        row(csv, "Cout total", dashboard.getTotalMaintenanceCost());
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] overdueDemandesCsv() {
        // Export des demandes dont la date souhaitee est depassee et qui ne sont pas cloturees.
        List<DemandeMaintenance> demandes = demandeMaintenanceRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        StringBuilder csv = new StringBuilder();
        csv.append("ID;Machine;Reference;Type;Priorite;Statut;Date demande;Date planifiee;Description\n");
        demandes.stream()
                .filter(demande -> demande.getDateSouhaitee() != null)
                .filter(demande -> demande.getDateSouhaitee().isBefore(now))
                .filter(demande -> demande.getStatut() != com.canal.canalmadash.Enum.StatutDemandeMaintenance.TERMINEE)
                .filter(demande -> demande.getStatut() != com.canal.canalmadash.Enum.StatutDemandeMaintenance.ANNULEE)
                .forEach(demande -> csv.append(escape(demande.getId())).append(';')
                        .append(escape(demande.getMachine() == null ? "" : demande.getMachine().getNom())).append(';')
                        .append(escape(demande.getMachine() == null ? "" : demande.getMachine().getRef())).append(';')
                        .append(escape(demande.getTypeMaintenance() == null ? "" : demande.getTypeMaintenance().getLibelle())).append(';')
                        .append(escape(demande.getPriorite())).append(';')
                        .append(escape(demande.getStatut())).append(';')
                        .append(escape(demande.getDateDemande())).append(';')
                        .append(escape(demande.getDateSouhaitee())).append(';')
                        .append(escape(demande.getDescription())).append('\n'));
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] machineHistoryCsv(int machineId) {
        /*
         * Export complet d'une machine:
         * informations machine, historique des etats, demandes et interventions.
         */
        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new IllegalArgumentException("Machine introuvable id=" + machineId));
        List<Action> actions = actionRepository.findByMachineIdOrderByDateDebutAsc(machineId);
        List<DemandeMaintenance> demandes = demandeMaintenanceRepository.findByMachineIdOrderByDateDemandeDesc(machineId);
        List<Maintenance> maintenances = maintenanceRepository.findByMachineIdOrderByDateDebutDesc(machineId);

        StringBuilder csv = new StringBuilder();
        csv.append("Machine;").append(escape(machine.getNom())).append(";Reference;").append(escape(machine.getRef())).append("\n\n");

        csv.append("ACTIONS\nType;Debut;Fin\n");
        for (Action action : actions) {
            csv.append(escape(action.getTypeAction() == null ? "" : action.getTypeAction().getLibelle())).append(';')
                    .append(escape(action.getDateDebut())).append(';')
                    .append(escape(action.getDateFin())).append('\n');
        }

        csv.append("\nDEMANDES\nID;Type;Priorite;Statut;Date demande;Date planifiee;Description\n");
        for (DemandeMaintenance demande : demandes) {
            csv.append(escape(demande.getId())).append(';')
                    .append(escape(demande.getTypeMaintenance() == null ? "" : demande.getTypeMaintenance().getLibelle())).append(';')
                    .append(escape(demande.getPriorite())).append(';')
                    .append(escape(demande.getStatut())).append(';')
                    .append(escape(demande.getDateDemande())).append(';')
                    .append(escape(demande.getDateSouhaitee())).append(';')
                    .append(escape(demande.getDescription())).append('\n');
        }

        csv.append("\nINTERVENTIONS\nID;Type;Debut;Fin;Pieces;Cout;Description;Note\n");
        for (Maintenance maintenance : maintenances) {
            csv.append(escape(maintenance.getId())).append(';')
                    .append(escape(maintenance.getType() == null ? "" : maintenance.getType().getLibelle())).append(';')
                    .append(escape(maintenance.getDateDebut())).append(';')
                    .append(escape(maintenance.getDateFin())).append(';')
                    .append(escape(maintenance.getPiecesUtilisees())).append(';')
                    .append(escape(maintenance.getCout())).append(';')
                    .append(escape(maintenance.getDescription())).append(';')
                    .append(escape(maintenance.getNote())).append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void row(StringBuilder csv, String label, Object value) {
        csv.append(escape(label)).append(';').append(escape(value)).append('\n');
    }

    private String escape(Object value) {
        // Echappe les guillemets pour produire un CSV lisible par Excel.
        if (value == null) {
            return "";
        }
        return "\"" + value.toString().replace("\"", "\"\"") + "\"";
    }
}
