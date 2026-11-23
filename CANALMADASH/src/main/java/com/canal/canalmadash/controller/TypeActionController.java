package com.canal.canalmadash.controller;

import com.canal.canalmadash.entity.TypeAction;
import com.canal.canalmadash.service.TypeActionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/typeAction")
@CrossOrigin
public class TypeActionController {

    /*
     * Controller des types d'action machine.
     * Ces endpoints alimentent les listes de choix pour l'etat courant d'une
     * machine: marche, panne, maintenance, arret, etc.
     */
    private final TypeActionService typeActionService;

    public TypeActionController(TypeActionService typeActionService) {
        this.typeActionService = typeActionService;
    }

    @GetMapping
    public List<TypeAction> findAll() {
        return typeActionService.findAll();
    }

    @GetMapping("/actifs")
    public List<TypeAction> findActifs() {
        // Liste utilisee dans les formulaires pour masquer les types desactives.
        return this.typeActionService.findActifs();
    }

    @GetMapping("/{id}")
    public TypeAction findById(@PathVariable int id) {
        return this.typeActionService.findById(id);
    }

    @PostMapping
    public TypeAction create(@RequestBody TypeAction type) {
        return this.typeActionService.create(type);
    }

    @PutMapping("/{id}")
    public TypeAction update(@PathVariable int id,
                             @RequestBody TypeAction update) {
        return this.typeActionService.update(id, update);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        this.typeActionService.delete(id);
    }

    @PostMapping("/{id}/disable")
    public TypeAction disable(@PathVariable int id) {
        return this.typeActionService.disable(id);
    }

    @PostMapping("/{id}/enable")
    public TypeAction enable(@PathVariable int id) {
        return this.typeActionService.enable(id);
    }
}
