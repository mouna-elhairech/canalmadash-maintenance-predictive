package com.canal.canalmadash.service.ai;

import com.canal.canalmadash.dto.AiDiagnosticResponse;

public interface MaintenanceAiService {
    /*
     * Contrat commun pour l'assistant IA.
     * Les controllers utilisent cette interface sans savoir si l'implementation
     * active est Ollama ou le mode desactive.
     */
    AiDiagnosticResponse testModel();

    AiDiagnosticResponse diagnoseMachine(int machineId);

    AiDiagnosticResponse chat(Integer machineId, String message);
}
