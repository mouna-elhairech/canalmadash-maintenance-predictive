package com.canal.canalmadash.dto;

public class DemandePieceRequest {

    /*
     * DTO de creation d'une demande de piece.
     * Il est utilise par l'utilisateur ou automatiquement quand le stock
     * passe sous le seuil minimum.
     */
    private Long pieceId;
    private int quantiteDemandee;

    // Motif visible dans le suivi des demandes de pieces.
    private String motif;

    public Long getPieceId() {
        return pieceId;
    }

    public void setPieceId(Long pieceId) {
        this.pieceId = pieceId;
    }

    public int getQuantiteDemandee() {
        return quantiteDemandee;
    }

    public void setQuantiteDemandee(int quantiteDemandee) {
        this.quantiteDemandee = quantiteDemandee;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }
}
