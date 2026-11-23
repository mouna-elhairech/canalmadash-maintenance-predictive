package com.canal.canalmadash.entity;

import com.canal.canalmadash.Enum.TypeMouvementStockPiece;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class MouvementStockPiece {

    /*
     * Trace comptable du stock de pieces.
     * Chaque entree, sortie vers intervention ou retour cree un mouvement pour
     * pouvoir expliquer l'evolution de la quantite et de la valeur du stock.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private PieceRechange piece;

    // Renseigne quand le mouvement vient d'une intervention.
    @JsonIgnore
    @ManyToOne
    private Maintenance maintenance;

    @JsonIgnore
    @ManyToOne
    private MaintenancePieceUtilisee pieceUtilisee;

    @Enumerated(EnumType.STRING)
    private TypeMouvementStockPiece type;

    private int quantite;

    // Cout unitaire et cout total au moment exact du mouvement.
    private Double coutUnitaire;
    private Double coutTotal;
    private LocalDateTime dateMouvement;

    @Column(length = 1000)
    private String commentaire;

    public Long getId() {
        return id;
    }

    public PieceRechange getPiece() {
        return piece;
    }

    public void setPiece(PieceRechange piece) {
        this.piece = piece;
    }

    public Maintenance getMaintenance() {
        return maintenance;
    }

    public void setMaintenance(Maintenance maintenance) {
        this.maintenance = maintenance;
    }

    public MaintenancePieceUtilisee getPieceUtilisee() {
        return pieceUtilisee;
    }

    public void setPieceUtilisee(MaintenancePieceUtilisee pieceUtilisee) {
        this.pieceUtilisee = pieceUtilisee;
    }

    public TypeMouvementStockPiece getType() {
        return type;
    }

    public void setType(TypeMouvementStockPiece type) {
        this.type = type;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public Double getCoutUnitaire() {
        return coutUnitaire;
    }

    public void setCoutUnitaire(Double coutUnitaire) {
        this.coutUnitaire = coutUnitaire;
    }

    public Double getCoutTotal() {
        return coutTotal;
    }

    public void setCoutTotal(Double coutTotal) {
        this.coutTotal = coutTotal;
    }

    public LocalDateTime getDateMouvement() {
        return dateMouvement;
    }

    public void setDateMouvement(LocalDateTime dateMouvement) {
        this.dateMouvement = dateMouvement;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }
}
