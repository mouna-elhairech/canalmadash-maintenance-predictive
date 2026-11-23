package com.canal.canalmadash.controller;

import com.canal.canalmadash.dto.AiChatRequest;
import com.canal.canalmadash.dto.AiDiagnosticResponse;
import com.canal.canalmadash.service.ai.MaintenanceAiService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin
public class AiMaintenanceController {

    /*
     * Controller de l'assistant IA maintenance.
     * Les endpoints restent simples: tester le modele, diagnostiquer une
     * machine, ou discuter avec un contexte optionnel.
     */
    private final MaintenanceAiService maintenanceAiService;

    public AiMaintenanceController(MaintenanceAiService maintenanceAiService) {
        this.maintenanceAiService = maintenanceAiService;
    }

    @GetMapping("/test")
    public AiDiagnosticResponse testModel() {
        return maintenanceAiService.testModel();
    }

    @GetMapping("/machines/{machineId}/diagnostic")
    public AiDiagnosticResponse diagnoseMachine(@PathVariable int machineId) {
        return maintenanceAiService.diagnoseMachine(machineId);
    }

    @PostMapping("/chat")
    public AiDiagnosticResponse chat(@RequestBody AiChatRequest request) {
        // machineId peut etre null: dans ce cas le chat repond avec un contexte global.
        return maintenanceAiService.chat(request.getMachineId(), request.getMessage());
    }
}
