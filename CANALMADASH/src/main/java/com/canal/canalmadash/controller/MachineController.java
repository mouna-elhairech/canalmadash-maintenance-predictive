package com.canal.canalmadash.controller;

import com.canal.canalmadash.entity.Action;
import com.canal.canalmadash.entity.Machine;
import com.canal.canalmadash.service.MachineService;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/machines")
@CrossOrigin
public class MachineController {

    /*
     * Controller REST des machines.
     * Son role est de traduire les requetes HTTP du frontend vers le service
     * metier. La logique importante reste dans MachineService.
     */
    private final MachineService machineService;

    public MachineController(MachineService machineService) {
        this.machineService = machineService;
    }

    @PostMapping
    public Machine ajouterMachine(@RequestBody Machine machine) {
        return machineService.ajouterMachine(machine);
    }

    @GetMapping
    public List<Machine> getToutesLesMachines() {
        return machineService.getToutesLesMachines();
    }

    @GetMapping("/{id}")
    public Machine getMachine(@PathVariable int id) {
        return machineService.getMachine(id);
    }

    @PutMapping("/{id}")
    public Machine modifierMachine(@PathVariable int id,
                                   @RequestBody Machine machineDetails) {
        return machineService.modifierMachine(id, machineDetails);
    }

    @DeleteMapping("/{id}")
    public void supprimerMachine(@PathVariable int id) {
        machineService.supprimerMachine(id);
    }

    @GetMapping("/{id}/actions")
    public List<Action> getActionsForMachine(
            @PathVariable int id,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        // Historique des etats d'une machine, optionnellement limite a une periode.
        return machineService.getActionsForMachine(id, from, to);
    }


    @PostMapping(value = "/avec-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Machine ajouterMachineAvecImage(
            @RequestParam String nom,
            @RequestParam String ref,
            @RequestParam("typeActionId") int typeActionId,
            @RequestParam(value = "emplacement", required = false) String emplacement,
            @RequestParam(value = "fabricant", required = false) String fabricant,
            @RequestParam(value = "modele", required = false) String modele,
            @RequestParam(value = "numeroSerie", required = false) String numeroSerie,
            @RequestParam(value = "dateInstallation", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateInstallation,
            @RequestParam(value = "criticite", required = false) String criticite,
            @RequestParam(value = "heuresMarche", required = false) Double heuresMarche,
            @RequestParam(value = "coutAchat", required = false) Double coutAchat,
            @RequestParam(value = "coutInstallation", required = false) Double coutInstallation,
            @RequestParam(value = "coutTransport", required = false) Double coutTransport,
            @RequestParam(value = "coutFormation", required = false) Double coutFormation,
            @RequestParam(value = "coutMiseEnService", required = false) Double coutMiseEnService,
            @RequestParam(value = "seuilDisponibiliteAttentionPercent", required = false) Double seuilDisponibiliteAttentionPercent,
            @RequestParam(value = "seuilDisponibiliteCritiquePercent", required = false) Double seuilDisponibiliteCritiquePercent,
            @RequestParam(value = "seuilTempsArretAttentionMinutes", required = false) Long seuilTempsArretAttentionMinutes,
            @RequestParam(value = "seuilTempsArretCritiqueMinutes", required = false) Long seuilTempsArretCritiqueMinutes,
            @RequestParam(value = "seuilPannesAttention", required = false) Long seuilPannesAttention,
            @RequestParam(value = "seuilPannesCritique", required = false) Long seuilPannesCritique,
            @RequestParam(value = "seuilTempsReparationAttentionHours", required = false) Double seuilTempsReparationAttentionHours,
            @RequestParam(value = "seuilTempsReparationCritiqueHours", required = false) Double seuilTempsReparationCritiqueHours,
            @RequestParam(value = "seuilRemplacementSurveillancePercent", required = false) Double seuilRemplacementSurveillancePercent,
            @RequestParam(value = "seuilRemplacementAttentionPercent", required = false) Double seuilRemplacementAttentionPercent,
            @RequestParam(value = "seuilRemplacementCritiquePercent", required = false) Double seuilRemplacementCritiquePercent,
            @RequestParam(value = "commentaireTechnique", required = false) String commentaireTechnique,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        // Creation depuis un formulaire multipart: champs texte + image de la machine.
        return machineService.ajouterMachineAvecImage(
                nom, ref, typeActionId, emplacement, fabricant, modele, numeroSerie,
                dateInstallation, criticite, heuresMarche, coutAchat, coutInstallation,
                coutTransport, coutFormation, coutMiseEnService,
                seuilDisponibiliteAttentionPercent, seuilDisponibiliteCritiquePercent,
                seuilTempsArretAttentionMinutes, seuilTempsArretCritiqueMinutes,
                seuilPannesAttention, seuilPannesCritique,
                seuilTempsReparationAttentionHours, seuilTempsReparationCritiqueHours,
                seuilRemplacementSurveillancePercent, seuilRemplacementAttentionPercent,
                seuilRemplacementCritiquePercent, commentaireTechnique, image
        );
    }

    @PutMapping(value = "/{id}/avec-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Machine modifierMachineAvecImage(
            @PathVariable int id,
            @RequestParam String nom,
            @RequestParam String ref,
            @RequestParam("typeActionId") int typeActionId,
            @RequestParam(value = "emplacement", required = false) String emplacement,
            @RequestParam(value = "fabricant", required = false) String fabricant,
            @RequestParam(value = "modele", required = false) String modele,
            @RequestParam(value = "numeroSerie", required = false) String numeroSerie,
            @RequestParam(value = "dateInstallation", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateInstallation,
            @RequestParam(value = "criticite", required = false) String criticite,
            @RequestParam(value = "heuresMarche", required = false) Double heuresMarche,
            @RequestParam(value = "coutAchat", required = false) Double coutAchat,
            @RequestParam(value = "coutInstallation", required = false) Double coutInstallation,
            @RequestParam(value = "coutTransport", required = false) Double coutTransport,
            @RequestParam(value = "coutFormation", required = false) Double coutFormation,
            @RequestParam(value = "coutMiseEnService", required = false) Double coutMiseEnService,
            @RequestParam(value = "seuilDisponibiliteAttentionPercent", required = false) Double seuilDisponibiliteAttentionPercent,
            @RequestParam(value = "seuilDisponibiliteCritiquePercent", required = false) Double seuilDisponibiliteCritiquePercent,
            @RequestParam(value = "seuilTempsArretAttentionMinutes", required = false) Long seuilTempsArretAttentionMinutes,
            @RequestParam(value = "seuilTempsArretCritiqueMinutes", required = false) Long seuilTempsArretCritiqueMinutes,
            @RequestParam(value = "seuilPannesAttention", required = false) Long seuilPannesAttention,
            @RequestParam(value = "seuilPannesCritique", required = false) Long seuilPannesCritique,
            @RequestParam(value = "seuilTempsReparationAttentionHours", required = false) Double seuilTempsReparationAttentionHours,
            @RequestParam(value = "seuilTempsReparationCritiqueHours", required = false) Double seuilTempsReparationCritiqueHours,
            @RequestParam(value = "seuilRemplacementSurveillancePercent", required = false) Double seuilRemplacementSurveillancePercent,
            @RequestParam(value = "seuilRemplacementAttentionPercent", required = false) Double seuilRemplacementAttentionPercent,
            @RequestParam(value = "seuilRemplacementCritiquePercent", required = false) Double seuilRemplacementCritiquePercent,
            @RequestParam(value = "commentaireTechnique", required = false) String commentaireTechnique,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        // Modification multipart: meme logique que la creation, avec remplacement image possible.
        return machineService.modifierMachineAvecImage(
                id, nom, ref, typeActionId, emplacement, fabricant, modele, numeroSerie,
                dateInstallation, criticite, heuresMarche, coutAchat, coutInstallation,
                coutTransport, coutFormation, coutMiseEnService,
                seuilDisponibiliteAttentionPercent, seuilDisponibiliteCritiquePercent,
                seuilTempsArretAttentionMinutes, seuilTempsArretCritiqueMinutes,
                seuilPannesAttention, seuilPannesCritique,
                seuilTempsReparationAttentionHours, seuilTempsReparationCritiqueHours,
                seuilRemplacementSurveillancePercent, seuilRemplacementAttentionPercent,
                seuilRemplacementCritiquePercent, commentaireTechnique, image
        );
    }

    // -------- Changer l'état de la machine --------

    @PostMapping("/{id}/etat")
    public Action changerEtat(@PathVariable int id,
                              @RequestParam("typeActionId") int typeActionId) {
        return machineService.changerEtatMachine(id, typeActionId);
    }


    @PostMapping("/{id}/image")
    public Machine uploadImage(
            @PathVariable int id,
            @RequestParam("file") MultipartFile file
    ) {
        return machineService.remplacerImage(id, file);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> getImage(@PathVariable int id) {
        // L'image est renvoyee inline pour etre affichee directement par le navigateur.
        Resource resource = machineService.getImageResource(id);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }

        String contentType = "image/jpeg";
        try {
            contentType = Files.probeContentType(resource.getFile().toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
        } catch (Exception ignored) {}

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}/image")
    public ResponseEntity<Void> deleteImage(@PathVariable int id) {
        machineService.supprimerImage(id);
        return ResponseEntity.noContent().build();
    }
}
