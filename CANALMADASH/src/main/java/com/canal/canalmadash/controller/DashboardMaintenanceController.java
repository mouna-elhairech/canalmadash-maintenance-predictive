package com.canal.canalmadash.controller;

import com.canal.canalmadash.dto.DashboardMaintenanceDto;
import com.canal.canalmadash.dto.MachineMaintenanceIndicatorsDto;
import com.canal.canalmadash.service.DashboardMaintenanceService;
import com.canal.canalmadash.service.MaintenanceExportService;
import com.canal.canalmadash.service.MaintenancePdfReportService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin
public class DashboardMaintenanceController {

    /*
     * Controller du tableau de bord maintenance.
     * Il expose les indicateurs JSON et les exports CSV/PDF consommes par
     * l'interface Angular.
     */
    private final DashboardMaintenanceService dashboardMaintenanceService;
    private final MaintenanceExportService maintenanceExportService;
    private final MaintenancePdfReportService maintenancePdfReportService;

    public DashboardMaintenanceController(
            DashboardMaintenanceService dashboardMaintenanceService,
            MaintenanceExportService maintenanceExportService,
            MaintenancePdfReportService maintenancePdfReportService
    ) {
        this.dashboardMaintenanceService = dashboardMaintenanceService;
        this.maintenanceExportService = maintenanceExportService;
        this.maintenancePdfReportService = maintenancePdfReportService;
    }

    @GetMapping("/maintenance")
    public DashboardMaintenanceDto getMaintenanceDashboard() {
        // Donnees completes du dashboard: cartes, graphiques, alertes et calendrier.
        return dashboardMaintenanceService.getDashboard();
    }

    @GetMapping("/maintenance/machines/{machineId}")
    public MachineMaintenanceIndicatorsDto getMachineMaintenanceIndicators(@PathVariable int machineId) {
        return dashboardMaintenanceService.getMachineIndicators(machineId);
    }

    @GetMapping("/maintenance/export")
    public ResponseEntity<byte[]> exportDashboard() {
        // Export CSV de la synthese globale.
        return csv("dashboard-maintenance.csv", maintenanceExportService.dashboardCsv());
    }

    @GetMapping("/maintenance/retards/export")
    public ResponseEntity<byte[]> exportOverdueDemandes() {
        return csv("demandes-retard.csv", maintenanceExportService.overdueDemandesCsv());
    }

    @GetMapping("/maintenance/machines/{machineId}/historique/export")
    public ResponseEntity<byte[]> exportMachineHistory(@PathVariable int machineId) {
        return csv("historique-machine-" + machineId + ".csv", maintenanceExportService.machineHistoryCsv(machineId));
    }

    @GetMapping("/maintenance/report")
    public ResponseEntity<byte[]> dashboardReportPdf() {
        return pdf("rapport-maintenance.pdf", maintenancePdfReportService.dashboardPdf());
    }

    @GetMapping("/maintenance/retards/report")
    public ResponseEntity<byte[]> overdueDemandesReportPdf() {
        return pdf("rapport-demandes-retard.pdf", maintenancePdfReportService.overdueDemandesPdf());
    }

    @GetMapping("/maintenance/machines/{machineId}/historique/report")
    public ResponseEntity<byte[]> machineHistoryReportPdf(@PathVariable int machineId) {
        return pdf("rapport-historique-machine-" + machineId + ".pdf", maintenancePdfReportService.machineHistoryPdf(machineId));
    }

    @GetMapping("/maintenance/monthly-report")
    public ResponseEntity<byte[]> monthlyReportPdf(
            @org.springframework.web.bind.annotation.RequestParam("year") int year,
            @org.springframework.web.bind.annotation.RequestParam("month") int month
    ) {
        return pdf("rapport-maintenance-" + year + "-" + month + ".pdf", maintenancePdfReportService.monthlyPdf(year, month));
    }

    private ResponseEntity<byte[]> csv(String filename, byte[] data) {
        // Reponse telechargeable au format CSV.
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv"));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return ResponseEntity.ok().headers(headers).body(data);
    }

    private ResponseEntity<byte[]> pdf(String filename, byte[] data) {
        // Reponse telechargeable au format PDF.
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return ResponseEntity.ok().headers(headers).body(data);
    }
}
