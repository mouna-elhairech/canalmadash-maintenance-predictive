package com.canal.canalmadash.service.ai;

import com.canal.canalmadash.dto.AiDiagnosticResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "disabled", matchIfMissing = true)
public class DisabledMaintenanceAiService implements MaintenanceAiService {

    /*
     * Implementation de secours quand l'IA n'est pas configuree.
     * Elle permet aux endpoints IA de repondre proprement au lieu de provoquer
     * une erreur Spring au demarrage.
     */
    @Override
    public AiDiagnosticResponse testModel() {
        AiDiagnosticResponse response = new AiDiagnosticResponse();
        response.setProvider("disabled");
        response.setStatus("DISABLED");
        response.setDiagnostic("Le service IA est desactive. Active app.ai.provider=ollama pour tester Ollama.");
        return response;
    }

    @Override
    public AiDiagnosticResponse diagnoseMachine(int machineId) {
        AiDiagnosticResponse response = new AiDiagnosticResponse();
        response.setProvider("disabled");
        response.setMachineId(machineId);
        response.setStatus("DISABLED");
        response.setDiagnostic("Le service IA est desactive. Active app.ai.provider=ollama pour utiliser Ollama.");
        return response;
    }

    @Override
    public AiDiagnosticResponse chat(Integer machineId, String message) {
        AiDiagnosticResponse response = new AiDiagnosticResponse();
        response.setProvider("disabled");
        response.setMachineId(machineId == null ? 0 : machineId);
        response.setStatus("DISABLED");
        response.setDiagnostic("Le service IA est desactive. Active app.ai.provider=ollama pour utiliser le chatbot.");
        return response;
    }
}
