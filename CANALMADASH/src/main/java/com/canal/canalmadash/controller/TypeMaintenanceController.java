package com.canal.canalmadash.controller;

import com.canal.canalmadash.entity.TypeMaintenance;
import com.canal.canalmadash.service.TypeMaintenanceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/typeMaintenance")
@CrossOrigin
public class TypeMaintenanceController {

    private final TypeMaintenanceService typeMaintenanceService;

    public TypeMaintenanceController(TypeMaintenanceService typeMaintenanceService) {
        this.typeMaintenanceService = typeMaintenanceService;
    }

    // GET /api/typeMaintenance
    @GetMapping
    public List<TypeMaintenance> findAll() {
        return this.typeMaintenanceService.findAll();
    }

    // GET /api/typeMaintenance/actifs
    @GetMapping("/actifs")
    public List<TypeMaintenance> findActifs() {
        return this.typeMaintenanceService.findActifs();
    }

    // GET /api/typeMaintenance/{id}
    @GetMapping("/{id}")
    public TypeMaintenance findById(@PathVariable int id) {
        return this.typeMaintenanceService.findById(id);
    }

    // POST /api/typeMaintenance
    @PostMapping
    public TypeMaintenance create(@RequestBody TypeMaintenance type) {
        return this.typeMaintenanceService.create(type);
    }

    // PUT /api/typeMaintenance/{id}
    @PutMapping("/{id}")
    public TypeMaintenance update(@PathVariable int id,
                                  @RequestBody TypeMaintenance update) {
        return this.typeMaintenanceService.update(id, update);
    }

    // DELETE /api/typeMaintenance/{id}
    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        this.typeMaintenanceService.delete(id);
    }

    // POST /api/typeMaintenance/{id}/disable
    @PostMapping("/{id}/disable")
    public TypeMaintenance disable(@PathVariable int id) {
        return this.typeMaintenanceService.disable(id);
    }

    // POST /api/typeMaintenance/{id}/enable
    @PostMapping("/{id}/enable")
    public TypeMaintenance enable(@PathVariable int id) {
        return this.typeMaintenanceService.enable(id);
    }
}
