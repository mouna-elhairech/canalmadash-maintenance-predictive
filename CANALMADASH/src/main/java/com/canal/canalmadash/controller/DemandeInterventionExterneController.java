package com.canal.canalmadash.controller;

import com.canal.canalmadash.Enum.StatutDemandeInterventionExterne;
import com.canal.canalmadash.dto.DemandeInterventionExterneRequest;
import com.canal.canalmadash.entity.DemandeInterventionExterne;
import com.canal.canalmadash.service.DemandeInterventionExterneService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interventions-externes")
@CrossOrigin
public class DemandeInterventionExterneController {

    /*
     * Controller des interventions externes.
     * Il permet de preparer une demande fournisseur et de suivre son statut.
     */
    private final DemandeInterventionExterneService service;

    public DemandeInterventionExterneController(DemandeInterventionExterneService service) {
        this.service = service;
    }

    @GetMapping("/machine/{machineId}")
    public List<DemandeInterventionExterne> findByMachine(@PathVariable int machineId) {
        return service.findByMachine(machineId);
    }

    @PostMapping
    public DemandeInterventionExterne create(@RequestBody DemandeInterventionExterneRequest request) {
        return service.create(request);
    }

    @PatchMapping("/{id}/statut")
    public DemandeInterventionExterne updateStatut(
            @PathVariable Long id,
            @RequestParam("statut") StatutDemandeInterventionExterne statut
    ) {
        // Transition controlee par le service pour respecter le cycle externe.
        return service.updateStatut(id, statut);
    }
}
