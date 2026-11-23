package com.canal.canalmadash.controller;

import com.canal.canalmadash.dto.MaintenanceRequest;
import com.canal.canalmadash.dto.RetourPieceRequest;
import com.canal.canalmadash.entity.Maintenance;
import com.canal.canalmadash.entity.MaintenancePieceUtilisee;
import com.canal.canalmadash.service.MaintenanceService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/maintenances")
@CrossOrigin
public class MaintenanceController {

    /*
     * Controller REST des interventions.
     * Il recoit les formulaires multipart du front: donnees JSON + images.
     */
    private final MaintenanceService maintenanceService;

    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Maintenance create(
            @RequestPart("data") MaintenanceRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "imageAvant", required = false) MultipartFile imageAvant,
            @RequestPart(value = "imageApres", required = false) MultipartFile imageApres
    ) {
        // Creation d'une intervention avec image principale et/ou images avant/apres.
        return maintenanceService.createMaintenance(request, image, imageAvant, imageApres);
    }

    @GetMapping("/machine/{machineId}")
    public List<Maintenance> getByMachine(@PathVariable int machineId) {
        return maintenanceService.findByMachine(machineId);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> getImage(@PathVariable Long id) {
        return imageResponse(maintenanceService.getMaintenanceImage(id, "principale"));
    }

    @GetMapping("/{id}/image-avant")
    public ResponseEntity<Resource> getImageAvant(@PathVariable Long id) {
        return imageResponse(maintenanceService.getMaintenanceImage(id, "avant"));
    }

    @GetMapping("/{id}/image-apres")
    public ResponseEntity<Resource> getImageApres(@PathVariable Long id) {
        return imageResponse(maintenanceService.getMaintenanceImage(id, "apres"));
    }

    @GetMapping("/demandes/{id}/image")
    public ResponseEntity<Resource> getDemandeImage(@PathVariable int id) {
        return imageResponse(maintenanceService.getDemandeImage(id));
    }

    @PostMapping("/pieces/{pieceUtiliseeId}/retour")
    public MaintenancePieceUtilisee retournerPiece(
            @PathVariable Long pieceUtiliseeId,
            @RequestBody RetourPieceRequest request
    ) {
        return maintenanceService.retournerPiece(pieceUtiliseeId, request);
    }

    private ResponseEntity<Resource> imageResponse(Resource resource) {
        // Reponse commune pour afficher les images stockees par le backend.
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
}
