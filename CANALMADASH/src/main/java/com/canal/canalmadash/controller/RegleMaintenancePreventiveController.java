package com.canal.canalmadash.controller;

import com.canal.canalmadash.dto.RegleMaintenancePreventiveRequest;
import com.canal.canalmadash.entity.RegleMaintenancePreventive;
import com.canal.canalmadash.service.RegleMaintenancePreventiveService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/regles-preventives")
@CrossOrigin
public class RegleMaintenancePreventiveController {

    /*
     * Controller des regles preventives.
     * Il expose la creation de controles periodiques et la generation
     * automatique des demandes arrivees a echeance.
     */
    private final RegleMaintenancePreventiveService service;

    public RegleMaintenancePreventiveController(RegleMaintenancePreventiveService service) {
        this.service = service;
    }

    @GetMapping
    public List<RegleMaintenancePreventive> findAll() {
        return service.findAll();
    }

    @GetMapping("/machine/{machineId}")
    public List<RegleMaintenancePreventive> findByMachine(@PathVariable int machineId) {
        return service.findByMachine(machineId);
    }

    @PostMapping
    public RegleMaintenancePreventive create(@RequestBody RegleMaintenancePreventiveRequest request) {
        return service.create(request);
    }

    @DeleteMapping("/{id}")
    public void disable(@PathVariable Long id) {
        service.disable(id);
    }

    @PostMapping("/generate")
    public Map<String, Integer> generate() {
        // Declenche manuellement la generation des demandes preventives dues.
        return Map.of("generated", service.generateDemandes());
    }
}
