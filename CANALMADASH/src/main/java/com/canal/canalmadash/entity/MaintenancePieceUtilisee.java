package com.canal.canalmadash.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class MaintenancePieceUtilisee {

    /*
     * Ligne de piece consommee dans une intervention.
     * Elle garde la quantite sortie du stock, le cout unitaire au moment de
     * l'intervention et les retours eventuels.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Intervention qui a consomme la piece. Ignoree en JSON pour eviter les cycles.
    @JsonIgnore
    @ManyToOne(optional = false)
    private Maintenance maintenance;

    @ManyToOne(optional = false)
    private PieceRechange piece;

    private int quantite;

    // Quantite deja retournee au stock apres l'intervention.
    private int quantiteRetournee;

    // Cout fige au moment de la sortie de stock, meme si le cout moyen change plus tard.
    private Double coutUnitaire;
    private String causeRetour;
    private LocalDateTime dateRetour;

    public Long getId() {
        return id;
    }

    public Maintenance getMaintenance() {
        return maintenance;
    }

    public void setMaintenance(Maintenance maintenance) {
        this.maintenance = maintenance;
    }

    public PieceRechange getPiece() {
        return piece;
    }

    public void setPiece(PieceRechange piece) {
        this.piece = piece;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public int getQuantiteRetournee() {
        return quantiteRetournee;
    }

    public void setQuantiteRetournee(int quantiteRetournee) {
        this.quantiteRetournee = quantiteRetournee;
    }

    public int getQuantiteDisponibleRetour() {
        // Quantite encore disponible pour un retour au stock.
        return Math.max(quantite - quantiteRetournee, 0);
    }

    public Double getCoutUnitaire() {
        return coutUnitaire;
    }

    public void setCoutUnitaire(Double coutUnitaire) {
        this.coutUnitaire = coutUnitaire;
    }

    public String getCauseRetour() {
        return causeRetour;
    }

    public void setCauseRetour(String causeRetour) {
        this.causeRetour = causeRetour;
    }

    public LocalDateTime getDateRetour() {
        return dateRetour;
    }

    public void setDateRetour(LocalDateTime dateRetour) {
        this.dateRetour = dateRetour;
    }

    public double getCoutTotal() {
        // Cout brut de la ligne avant deduction des retours.
        return quantite * (coutUnitaire == null ? 0 : coutUnitaire);
    }

    public double getCoutNet() {
        // Cout reel apres retour des pieces non utilisees.
        return getQuantiteDisponibleRetour() * (coutUnitaire == null ? 0 : coutUnitaire);
    }
}
