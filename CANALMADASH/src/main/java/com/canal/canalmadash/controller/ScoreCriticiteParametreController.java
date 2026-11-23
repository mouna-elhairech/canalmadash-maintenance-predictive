package com.canal.canalmadash.controller;

import com.canal.canalmadash.entity.ScoreCriticiteParametre;
import com.canal.canalmadash.service.ScoreCriticiteParametreService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/score-criticite-parametres")
@CrossOrigin
public class ScoreCriticiteParametreController {

    /*
     * Controller de parametrage du score de criticite.
     * Il expose la ligne unique de configuration utilisee par le dashboard.
     */
    private final ScoreCriticiteParametreService service;

    public ScoreCriticiteParametreController(ScoreCriticiteParametreService service) {
        this.service = service;
    }

    @GetMapping
    public ScoreCriticiteParametre get() {
        return service.getOrCreate();
    }

    @PutMapping
    public ScoreCriticiteParametre update(@RequestBody ScoreCriticiteParametre request) {
        return service.update(request);
    }

    @PostMapping("/reset")
    public ScoreCriticiteParametre reset() {
        // Revient aux poids et seuils par defaut.
        return service.resetDefaults();
    }
}
