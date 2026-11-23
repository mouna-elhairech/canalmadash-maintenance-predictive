package com.canal.canalmadash.controller;

import com.canal.canalmadash.dto.FonctionnaliteApplicationRequest;
import com.canal.canalmadash.dto.RoleApplicationRequest;
import com.canal.canalmadash.entity.FonctionnaliteApplication;
import com.canal.canalmadash.entity.RoleApplication;
import com.canal.canalmadash.service.RoleApplicationService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles-application")
@CrossOrigin
public class RoleApplicationController {

    /*
     * Controller de parametrage des roles et droits.
     * Il permet de consulter les roles, modifier leurs fonctionnalites et
     * recreer les valeurs par defaut si necessaire.
     */
    private final RoleApplicationService service;

    public RoleApplicationController(RoleApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public List<RoleApplication> findRoles() {
        return service.findRolesWithFonctionnalites();
    }

    @PostMapping
    public RoleApplication createRole(@RequestBody RoleApplicationRequest request) {
        return service.createRole(request);
    }

    @PutMapping("/{id}")
    public RoleApplication updateRole(@PathVariable Long id, @RequestBody RoleApplicationRequest request) {
        return service.updateRole(id, request);
    }

    @GetMapping("/fonctionnalites")
    public List<FonctionnaliteApplication> findFonctionnalites() {
        return service.findFonctionnalites();
    }

    @PostMapping("/fonctionnalites")
    public FonctionnaliteApplication createFonctionnalite(@RequestBody FonctionnaliteApplicationRequest request) {
        return service.createFonctionnalite(request);
    }

    @PutMapping("/fonctionnalites/{id}")
    public FonctionnaliteApplication updateFonctionnalite(
            @PathVariable Long id,
            @RequestBody FonctionnaliteApplicationRequest request
    ) {
        return service.updateFonctionnalite(id, request);
    }

    @PostMapping("/initialiser")
    public void initialiser() {
        // Reinitialise le catalogue par defaut des roles et fonctionnalites.
        service.initializeDefaults();
    }
}
