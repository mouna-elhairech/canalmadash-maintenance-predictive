package com.canal.canalmadash.dto;

public class AiChatRequest {

    /*
     * Requete envoyee au chat IA.
     * machineId est optionnel: s'il est present, l'IA repond avec le contexte
     * de cette machine; sinon elle donne une aide plus generale.
     */
    private Integer machineId;

    // Question saisie par l'utilisateur dans le chat.
    private String message;

    public Integer getMachineId() {
        return machineId;
    }

    public void setMachineId(Integer machineId) {
        this.machineId = machineId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
