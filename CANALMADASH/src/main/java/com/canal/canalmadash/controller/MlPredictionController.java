package com.canal.canalmadash.controller;

import com.canal.canalmadash.service.MlPredictionService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ml")
@CrossOrigin(origins = "*")
public class MlPredictionController {

    /*
     * Controller REST qui expose les endpoints ML prediction.
     * Angular appelle ces endpoints pour afficher les predictions.
     */

    private final MlPredictionService mlPredictionService;

    public MlPredictionController(MlPredictionService mlPredictionService) {
        this.mlPredictionService = mlPredictionService;
    }

    // ─── CHECK IF FLASK IS AVAILABLE ──────────────────
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        boolean available = mlPredictionService.isFlaskAvailable();
        return ResponseEntity.ok(
                java.util.Map.of(
                        "flask_available", available,
                        "status", available ? "ok" : "Flask API not running"
                )
        );
    }

    // ─── PREDICT FOR ONE MACHINE ──────────────────────
    @GetMapping("/predict/{machineId}")
    public ResponseEntity<?> predict(@PathVariable int machineId) {
        MlPredictionService.MlPredictionResult result =
                mlPredictionService.predictMachine(machineId);
        return ResponseEntity.ok(result);
    }

    // ─── PREDICT FOR ALL MACHINES ─────────────────────
    @GetMapping("/predict/all")
    public ResponseEntity<JsonNode> predictAll() {
        JsonNode result = mlPredictionService.predictAll();
        return ResponseEntity.ok(result);
    }
    // ─── HISTORIQUE DU SCORE ──────────────────────────
    @GetMapping("/history/{machineId}")
    public ResponseEntity<JsonNode> history(@PathVariable int machineId) {
        return ResponseEntity.ok(mlPredictionService.history(machineId));
    }
    // ─── RAPPORT REDIGE PAR OLLAMA ────────────────────
    @GetMapping("/report/{machineId}")
    public ResponseEntity<JsonNode> report(@PathVariable int machineId) {
        return ResponseEntity.ok(mlPredictionService.report(machineId));
    }
}