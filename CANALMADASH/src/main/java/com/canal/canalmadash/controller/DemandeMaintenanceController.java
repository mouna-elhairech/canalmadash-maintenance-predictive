package com.canal.canalmadash.controller;

import com.canal.canalmadash.dto.DemandeMaintenanceRequest;
import com.canal.canalmadash.Enum.PrioriteDemandeMaintenance;
import com.canal.canalmadash.Enum.StatutDemandeMaintenance;
import com.canal.canalmadash.entity.DemandeMaintenance;
import com.canal.canalmadash.service.DemandeMaintenanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/demandes-maintenance")
@CrossOrigin
public class DemandeMaintenanceController {

    /*
     * Controller REST des demandes de maintenance.
     * Il expose les actions du cycle de vie: creation, recherche, statut,
     * validation et refus de validation.
     */
    private final DemandeMaintenanceService demandeMaintenanceService;

    public DemandeMaintenanceController(DemandeMaintenanceService demandeMaintenanceService) {
        this.demandeMaintenanceService = demandeMaintenanceService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DemandeMaintenance create(
            @RequestPart("data") DemandeMaintenanceRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        // Creation depuis le web ou le mobile avec une image optionnelle du probleme.
        return demandeMaintenanceService.createDemande(request, image);
    }
    @GetMapping("/machine/{machineId}")
    public List<DemandeMaintenance> getByMachine(
            @PathVariable int machineId,
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return demandeMaintenanceService.findByMachine(machineId, from, to);
    }

    @GetMapping("/search")
    public List<DemandeMaintenance> search(
            @RequestParam(value = "machineId", required = false) Integer machineId,
            @RequestParam(value = "statut", required = false) StatutDemandeMaintenance statut,
            @RequestParam(value = "priorite", required = false) PrioriteDemandeMaintenance priorite,
            @RequestParam(value = "typeMaintenanceId", required = false) Integer typeMaintenanceId,
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(value = "retard", required = false) Boolean retard,
            @RequestParam(value = "coutMin", required = false) Double coutMin
    ) {
        // Recherche utilisee par le kanban, les filtres et les vues de suivi.
        return demandeMaintenanceService.search(machineId, statut, priorite, typeMaintenanceId, from, to, retard, coutMin);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        demandeMaintenanceService.delete(id);
    }

    @PatchMapping("/{id}/statut")
    public DemandeMaintenance updateStatut(
            @PathVariable int id,
            @RequestParam("statut") StatutDemandeMaintenance statut,
            @RequestParam(value = "dateSouhaitee", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateSouhaitee
    ) {
        // Changement controle du statut, avec date souhaitee si la demande est planifiee.
        return demandeMaintenanceService.updateStatut(id, statut, dateSouhaitee);
    }

    @PostMapping("/{id}/validation")
    public DemandeMaintenance validerIntervention(@PathVariable int id) {
        return demandeMaintenanceService.validerIntervention(id);
    }

    @PostMapping("/{id}/refus-validation")
    public DemandeMaintenance refuserIntervention(@PathVariable int id) {
        return demandeMaintenanceService.refuserIntervention(id);
    }

}

