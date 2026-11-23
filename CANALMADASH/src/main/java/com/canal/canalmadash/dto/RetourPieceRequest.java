package com.canal.canalmadash.dto;

public class RetourPieceRequest {

    /*
     * DTO utilise pour retourner au stock une partie des pieces sorties
     * pendant une intervention.
     */
    private int quantite;

    // Cause obligatoire pour garder une trace claire du retour.
    private String cause;

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }
}
