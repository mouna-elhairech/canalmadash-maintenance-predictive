package com.canal.canalmadash.service;

import com.canal.canalmadash.Enum.StatutDemandeMaintenance;
import com.canal.canalmadash.dto.DashboardMaintenanceDto;
import com.canal.canalmadash.entity.Action;
import com.canal.canalmadash.entity.DemandeMaintenance;
import com.canal.canalmadash.entity.Machine;
import com.canal.canalmadash.entity.Maintenance;
import com.canal.canalmadash.repository.ActionRepository;
import com.canal.canalmadash.repository.DemandeMaintenanceRepository;
import com.canal.canalmadash.repository.MachineRepository;
import com.canal.canalmadash.repository.MaintenanceRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class MaintenancePdfReportService {

    /*
     * Service de generation PDF.
     * Il produit des rapports directement en memoire pour que les controllers
     * puissent les renvoyer en telechargement sans creer de fichier temporaire.
     */
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Font TITLE = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(15, 23, 42));
    private static final Font SECTION = new Font(Font.HELVETICA, 13, Font.BOLD, new Color(15, 23, 42));
    private static final Font NORMAL = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.DARK_GRAY);
    private static final Font HEADER = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);

    private final DashboardMaintenanceService dashboardMaintenanceService;
    private final MachineRepository machineRepository;
    private final ActionRepository actionRepository;
    private final DemandeMaintenanceRepository demandeMaintenanceRepository;
    private final MaintenanceRepository maintenanceRepository;

    public MaintenancePdfReportService(
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
    public byte[] dashboardPdf() {
        // Rapport global: synthese, couts, retards, planning et recommandations.
        DashboardMaintenanceDto dashboard = dashboardMaintenanceService.getDashboard();
        return writePdf(document -> {
            addTitle(document, "Rapport maintenance detaille");
            addParagraph(document, "Genere le " + formatDate(LocalDateTime.now()));

            addSection(document, "Synthese generale");
            PdfPTable summary = table(2, 40, 60);
            row(summary, "Machines", dashboard.getTotalMachines());
            row(summary, "Disponibilite", dashboard.getAvailabilityPercent() + " %");
            row(summary, "Temps d'arret total", formatDuration(dashboard.getTotalDowntimeMinutes()));
            row(summary, "Temps moyen entre deux pannes", dashboard.getMtbfHours() + " h");
            row(summary, "Temps moyen de reparation", dashboard.getMttrHours() + " h");
            row(summary, "Demandes en retard", dashboard.getOverdueDemandesCount());
            row(summary, "Planifiees cette semaine", dashboard.getUpcomingDemandesCount());
            row(summary, "Urgentes / critiques", dashboard.getHighPriorityDemandesCount());
            row(summary, "Pannes longues", dashboard.getLongDowntimeMachinesCount());
            row(summary, "Cout total maintenance", money(dashboard.getTotalMaintenanceCost()));
            document.add(summary);

            addCostSection(document, "Cout par machine", dashboard.getCostByMachine());
            addCostSection(document, "Cout par type de maintenance", dashboard.getCostByType());
            addCostSection(document, "Cout par mois", dashboard.getCostByMonth());
            addDemandesSection(document, "Demandes en retard", dashboard.getOverdueDemandes());
            addDemandesSection(document, "Planning des 7 prochains jours", dashboard.getUpcomingDemandes());
            addLongDowntimeSection(document, dashboard.getLongDowntimeMachines());
            addPreventiveSection(document, dashboard.getPreventiveRecommendations());
        });
    }

    @Transactional(readOnly = true)
    public byte[] overdueDemandesPdf() {
        // Rapport cible pour les demandes depassees, utile en reunion de suivi.
        LocalDateTime now = LocalDateTime.now();
        List<DemandeMaintenance> demandes = demandeMaintenanceRepository.findAll().stream()
                .filter(demande -> demande.getDateSouhaitee() != null)
                .filter(demande -> demande.getDateSouhaitee().isBefore(now))
                .filter(demande -> demande.getStatut() != StatutDemandeMaintenance.TERMINEE)
                .filter(demande -> demande.getStatut() != StatutDemandeMaintenance.ANNULEE)
                .toList();

        return writePdf(document -> {
            addTitle(document, "Rapport des demandes en retard");
            addParagraph(document, "Genere le " + formatDate(now));
            PdfPTable table = table(7, 8, 18, 15, 12, 12, 15, 20);
            header(table, "ID", "Machine", "Type", "Priorite", "Statut", "Planifiee", "Description");
            for (DemandeMaintenance demande : demandes) {
                cells(table,
                        demande.getId(),
                        demande.getMachine() == null ? "" : demande.getMachine().getNom(),
                        demande.getTypeMaintenance() == null ? "" : demande.getTypeMaintenance().getLibelle(),
                        demande.getPriorite(),
                        demande.getStatut(),
                        formatDate(demande.getDateSouhaitee()),
                        demande.getDescription());
            }
            document.add(table);
        });
    }

    @Transactional(readOnly = true)
    public byte[] machineHistoryPdf(int machineId) {
        /*
         * Fiche complete d'une machine:
         * etats successifs, demandes de maintenance et interventions realisees.
         */
        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new IllegalArgumentException("Machine introuvable id=" + machineId));
        List<Action> actions = actionRepository.findByMachineIdOrderByDateDebutAsc(machineId);
        List<DemandeMaintenance> demandes = demandeMaintenanceRepository.findByMachineIdOrderByDateDemandeDesc(machineId);
        List<Maintenance> maintenances = maintenanceRepository.findByMachineIdOrderByDateDebutDesc(machineId);

        return writePdf(document -> {
            addTitle(document, "Fiche historique machine");
            addParagraph(document, "Machine: " + machine.getNom() + " | Reference: " + machine.getRef());
            addParagraph(document, "Genere le " + formatDate(LocalDateTime.now()));

            addSection(document, "Actions machine");
            PdfPTable actionTable = table(3, 34, 33, 33);
            header(actionTable, "Action", "Debut", "Fin");
            for (Action action : actions) {
                cells(actionTable,
                        action.getTypeAction() == null ? "" : action.getTypeAction().getLibelle(),
                        formatDate(action.getDateDebut()),
                        formatDate(action.getDateFin()));
            }
            document.add(actionTable);

            addSection(document, "Demandes de maintenance");
            PdfPTable demandeTable = table(6, 8, 18, 14, 14, 18, 28);
            header(demandeTable, "ID", "Type", "Priorite", "Statut", "Planifiee", "Description");
            for (DemandeMaintenance demande : demandes) {
                cells(demandeTable,
                        demande.getId(),
                        demande.getTypeMaintenance() == null ? "" : demande.getTypeMaintenance().getLibelle(),
                        demande.getPriorite(),
                        demande.getStatut(),
                        formatDate(demande.getDateSouhaitee()),
                        demande.getDescription());
            }
            document.add(demandeTable);

            addSection(document, "Interventions / prestations");
            PdfPTable interventionTable = table(7, 8, 15, 15, 15, 17, 10, 20);
            header(interventionTable, "ID", "Type", "Debut", "Fin", "Pieces", "Cout", "Description");
            for (Maintenance maintenance : maintenances) {
                cells(interventionTable,
                        maintenance.getId(),
                        maintenance.getType() == null ? "" : maintenance.getType().getLibelle(),
                        formatDate(maintenance.getDateDebut()),
                        formatDate(maintenance.getDateFin()),
                        maintenance.getPiecesUtilisees(),
                        money(maintenance.getCout()),
                        maintenance.getDescription());
            }
            document.add(interventionTable);
        });
    }

    @Transactional(readOnly = true)
    public byte[] monthlyPdf(int year, int month) {
        /*
         * Rapport mensuel: on isole d'abord la periode demandee, puis on garde
         * uniquement les demandes/interventions rattachees a ce mois.
         */
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime from = ym.atDay(1).atStartOfDay();
        LocalDateTime to = ym.atEndOfMonth().atTime(23, 59, 59);
        List<DemandeMaintenance> demandes = demandeMaintenanceRepository.findAll().stream()
                .filter(d -> d.getDateDemande() != null && !d.getDateDemande().isBefore(from) && !d.getDateDemande().isAfter(to))
                .toList();
        List<Maintenance> maintenances = maintenanceRepository.findAll().stream()
                .filter(m -> m.getDateDebut() != null && !m.getDateDebut().isBefore(from) && !m.getDateDebut().isAfter(to))
                .toList();
        DashboardMaintenanceDto dashboard = dashboardMaintenanceService.getDashboard();
        // Le cout et les retards sont recalcules sur le mois pour garder un rapport autonome.
        double cost = maintenances.stream().filter(m -> m.getCout() != null).mapToDouble(Maintenance::getCout).sum();
        long late = demandes.stream()
                .filter(d -> d.getDateSouhaitee() != null && d.getDateSouhaitee().isBefore(LocalDateTime.now()))
                .filter(d -> d.getStatut() != StatutDemandeMaintenance.TERMINEE && d.getStatut() != StatutDemandeMaintenance.ANNULEE)
                .count();

        return writePdf(document -> {
            addTitle(document, "Rapport mensuel maintenance - " + ym);
            addParagraph(document, "Periode: " + formatDate(from) + " au " + formatDate(to));
            addSection(document, "Synthese du mois");
            PdfPTable summary = table(2, 45, 55);
            row(summary, "Demandes creees", demandes.size());
            row(summary, "Interventions realisees", maintenances.size());
            row(summary, "Demandes en retard", late);
            row(summary, "Cout total du mois", money(cost));
            row(summary, "Disponibilite actuelle du parc", dashboard.getAvailabilityPercent() + " %");
            row(summary, "Machines critiques actuelles", dashboard.getTopProblemMachines().size());
            document.add(summary);

            addSection(document, "Interventions du mois");
            PdfPTable interventions = table(6, 18, 18, 16, 18, 12, 18);
            header(interventions, "Machine", "Type", "Date", "Pieces", "Cout", "Description");
            for (Maintenance maintenance : maintenances) {
                cells(interventions,
                        maintenance.getMachine() == null ? "" : maintenance.getMachine().getNom(),
                        maintenance.getType() == null ? "" : maintenance.getType().getLibelle(),
                        formatDate(maintenance.getDateDebut()),
                        maintenance.getPiecesUtilisees(),
                        money(maintenance.getCout()),
                        maintenance.getDescription());
            }
            document.add(interventions);

            addDemandesSection(document, "Demandes en retard actuelles", dashboard.getOverdueDemandes());
            addPreventiveSection(document, dashboard.getPreventiveRecommendations());
        });
    }

    private byte[] writePdf(PdfBody body) {
        /*
         * Methode commune a tous les rapports.
         * Le PDF est ecrit dans un ByteArrayOutputStream pour etre renvoye
         * directement par l'API REST en tant que fichier telechargeable.
         */
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
            PdfWriter.getInstance(document, out);
            document.open();
            body.write(document);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur generation PDF", e);
        }
    }

    private void addTitle(Document document, String title) throws Exception {
        Paragraph paragraph = new Paragraph(title, TITLE);
        paragraph.setSpacingAfter(8);
        document.add(paragraph);
    }

    private void addSection(Document document, String title) throws Exception {
        Paragraph paragraph = new Paragraph(title, SECTION);
        paragraph.setSpacingBefore(14);
        paragraph.setSpacingAfter(6);
        document.add(paragraph);
    }

    private void addParagraph(Document document, String text) throws Exception {
        Paragraph paragraph = new Paragraph(text, NORMAL);
        paragraph.setSpacingAfter(6);
        document.add(paragraph);
    }

    private void addCostSection(Document document, String title, List<DashboardMaintenanceDto.CostStatDto> items) throws Exception {
        // Section reutilisable pour les graphiques de cout exportes sous forme de tableau.
        addSection(document, title);
        PdfPTable table = table(2, 70, 30);
        header(table, "Element", "Cout");
        for (DashboardMaintenanceDto.CostStatDto item : items) {
            cells(table, item.getLabel(), money(item.getCost()));
        }
        document.add(table);
    }

    private void addDemandesSection(Document document, String title, List<DashboardMaintenanceDto.DemandeSummaryDto> demandes) throws Exception {
        // Meme structure pour les demandes en retard et les demandes planifiees.
        addSection(document, title);
        PdfPTable table = table(6, 20, 18, 12, 12, 18, 20);
        header(table, "Machine", "Type", "Priorite", "Statut", "Date", "Description");
        for (DashboardMaintenanceDto.DemandeSummaryDto demande : demandes) {
            cells(table,
                    demande.getMachineNom(),
                    demande.getTypeMaintenance(),
                    demande.getPriorite(),
                    demande.getStatut(),
                    formatDate(demande.getDateSouhaitee()),
                    demande.getDescription());
        }
        document.add(table);
    }

    private void addLongDowntimeSection(Document document, List<DashboardMaintenanceDto.LongDowntimeMachineDto> machines) throws Exception {
        addSection(document, "Machines trop longtemps en panne");
        PdfPTable table = table(4, 30, 25, 20, 25);
        header(table, "Machine", "Reference", "Etat", "Duree");
        for (DashboardMaintenanceDto.LongDowntimeMachineDto machine : machines) {
            cells(table, machine.getNom(), machine.getRef(), machine.getEtat(), formatDuration(machine.getMinutes()));
        }
        document.add(table);
    }

    private void addPreventiveSection(Document document, List<DashboardMaintenanceDto.PreventiveRecommendationDto> items) throws Exception {
        addSection(document, "Maintenance preventive conseillee");
        PdfPTable table = table(4, 25, 18, 25, 32);
        header(table, "Machine", "Reference", "Raison", "Recommandation");
        for (DashboardMaintenanceDto.PreventiveRecommendationDto item : items) {
            cells(table, item.getNom(), item.getRef(), item.getReason(), item.getRecommendation());
        }
        document.add(table);
    }

    private PdfPTable table(int columns, float... widths) throws Exception {
        // Centralise le style des tableaux pour garder tous les exports homogenes.
        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(100);
        table.setWidths(widths);
        table.setSpacingAfter(8);
        return table;
    }

    private void header(PdfPTable table, String... labels) {
        for (String label : labels) {
            PdfPCell cell = new PdfPCell(new Phrase(label, HEADER));
            cell.setBackgroundColor(new Color(15, 23, 42));
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    private void row(PdfPTable table, Object label, Object value) {
        cells(table, label, value);
    }

    private void cells(PdfPTable table, Object... values) {
        // Convertit les valeurs null en chaine vide pour eviter les "null" visibles dans le PDF.
        for (Object value : values) {
            PdfPCell cell = new PdfPCell(new Phrase(value == null ? "" : value.toString(), NORMAL));
            cell.setPadding(5);
            cell.setVerticalAlignment(Element.ALIGN_TOP);
            table.addCell(cell);
        }
    }

    private String formatDate(LocalDateTime date) {
        return date == null ? "" : date.format(DATE_FORMAT);
    }

    private String formatDuration(long minutes) {
        // Affichage court, plus lisible pour les durees dans les tableaux PDF.
        long days = minutes / 1440;
        long hours = (minutes % 1440) / 60;
        long mins = minutes % 60;
        if (days > 0) {
            return days + " j " + hours + " h";
        }
        if (hours > 0) {
            return hours + " h " + mins + " min";
        }
        return mins + " min";
    }

    private String money(Double value) {
        return money(value == null ? 0 : value.doubleValue());
    }

    private String money(double value) {
        return String.format(java.util.Locale.US, "%.2f DH", value);
    }

    private interface PdfBody {
        void write(Document document) throws Exception;
    }
}
