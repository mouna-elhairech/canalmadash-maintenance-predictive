package com.canal.canalmadash.controller;

import com.canal.canalmadash.Enum.StatutDemandePiece;
import com.canal.canalmadash.dto.DemandePieceRequest;
import com.canal.canalmadash.dto.EntreeStockPieceRequest;
import com.canal.canalmadash.entity.DemandePieceRechange;
import com.canal.canalmadash.entity.MouvementStockPiece;
import com.canal.canalmadash.entity.PieceRechange;
import com.canal.canalmadash.service.PieceRechangeService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pieces-rechange")
@CrossOrigin
public class PieceRechangeController {

    /*
     * Controller REST des pieces de rechange.
     * Il expose le stock, les mouvements et les demandes d'achat.
     */
    private final PieceRechangeService service;

    public PieceRechangeController(PieceRechangeService service) {
        this.service = service;
    }

    @GetMapping
    public List<PieceRechange> findAll() {
        return service.findAll();
    }

    @GetMapping("/alertes")
    public List<PieceRechange> findAlertes() {
        // Pieces dont la quantite est sous le seuil minimum.
        return service.findAlertes();
    }

    @GetMapping("/machine/{machineId}/compatibles")
    public List<PieceRechange> findCompatiblesForMachine(@PathVariable int machineId) {
        return service.findCompatiblesForMachine(machineId);
    }

    @PostMapping
    public PieceRechange create(@RequestBody PieceRechange piece) {
        return service.save(piece);
    }

    @PutMapping("/{id}")
    public PieceRechange update(@PathVariable Long id, @RequestBody PieceRechange piece) {
        return service.update(id, piece);
    }

    @PostMapping("/{id}/entrees-stock")
    public PieceRechange ajouterStock(@PathVariable Long id, @RequestBody EntreeStockPieceRequest request) {
        // Entree de stock valorisee: elle modifie quantite, valeur et cout moyen.
        return service.ajouterStock(id, request);
    }

    @GetMapping("/{id}/mouvements")
    public List<MouvementStockPiece> findMouvements(@PathVariable Long id) {
        return service.findMouvements(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/demandes")
    public List<DemandePieceRechange> findDemandes() {
        return service.findDemandes();
    }

    @PostMapping("/demandes")
    public DemandePieceRechange createDemande(@RequestBody DemandePieceRequest request) {
        return service.createDemande(request);
    }

    @PostMapping("/demandes/{id}/statut")
    public DemandePieceRechange updateDemandeStatut(
            @PathVariable Long id,
            @RequestParam("statut") StatutDemandePiece statut,
            @RequestParam(value = "prixTotalReception", required = false) Double prixTotalReception
    ) {
        // Lorsqu'une demande passe a RECUE, le stock est augmente automatiquement.
        return service.updateDemandeStatut(id, statut, prixTotalReception);
    }
}
