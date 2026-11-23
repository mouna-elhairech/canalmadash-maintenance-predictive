package com.canal.canalmadash.dto;

public class EntreeStockPieceRequest {

    /*
     * DTO utilise pour enregistrer une entree de stock.
     * Le prixTotal sert a recalculer la valeur du stock et le cout moyen.
     */
    private int quantite;
    private Double prixTotal;

    // Commentaire libre affiche dans l'historique des mouvements.
    private String commentaire;

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public Double getPrixTotal() {
        return prixTotal;
    }

    public void setPrixTotal(Double prixTotal) {
        this.prixTotal = prixTotal;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }
}
