package com.canal.canalmadash.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AiDiagnosticResponse {

    /*
     * Reponse structuree du diagnostic IA.
     * Elle indique quel moteur a repondu, sur quelle machine, puis separe
     * la reponse en texte global et sections lisibles par le frontend.
     */
    private String provider;
    private String model;
    private int machineId;
    private String machineNom;
    private String status;

    // Texte principal du diagnostic.
    private String diagnostic;

    // Sections detaillees: causes possibles, recommandations, prochaines actions, etc.
    private List<AiResponseSection> sections = new ArrayList<>();

    // Date de generation affichee dans l'interface.
    private LocalDateTime generatedAt = LocalDateTime.now();

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getMachineId() {
        return machineId;
    }

    public void setMachineId(int machineId) {
        this.machineId = machineId;
    }

    public String getMachineNom() {
        return machineNom;
    }

    public void setMachineNom(String machineNom) {
        this.machineNom = machineNom;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDiagnostic() {
        return diagnostic;
    }

    public void setDiagnostic(String diagnostic) {
        this.diagnostic = diagnostic;
    }

    public List<AiResponseSection> getSections() {
        return sections;
    }

    public void setSections(List<AiResponseSection> sections) {
        this.sections = sections;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}
