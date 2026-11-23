package com.canal.canalmadash.dto;

public class PieceUtiliseeRequest {
    /*
     * Ligne de piece utilisee dans une intervention.
     * Le service s'en sert pour verifier le stock, consommer la quantite
     * et ajouter le cout de la piece au cout final de maintenance.
     */
    private Long pieceId;
    private int quantite;

    public Long getPieceId() {
        return pieceId;
    }

    public void setPieceId(Long pieceId) {
        this.pieceId = pieceId;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }
}
