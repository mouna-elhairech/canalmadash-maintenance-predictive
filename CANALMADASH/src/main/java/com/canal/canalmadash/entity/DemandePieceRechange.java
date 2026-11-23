package com.canal.canalmadash.entity;

import com.canal.canalmadash.Enum.StatutDemandePiece;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class DemandePieceRechange {

    /*
     * Demande d'achat ou de reception d'une piece de rechange.
     * Elle peut etre creee manuellement ou automatiquement quand le stock passe
     * sous le seuil minimum.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private PieceRechange piece;

    private int quantiteDemandee;

    // Prix total reel saisi a la reception, utilise pour valoriser l'entree de stock.
    private Double prixTotalReception;
    private String motif;
    private LocalDateTime dateDemande = LocalDateTime.now();

    // Renseignee uniquement lorsque la demande passe au statut RECUE.
    private LocalDateTime dateReception;

    @Enumerated(EnumType.STRING)
    private StatutDemandePiece statut = StatutDemandePiece.EN_ATTENTE;

    public Long getId() {
        return id;
    }

    public PieceRechange getPiece() {
        return piece;
    }

    public void setPiece(PieceRechange piece) {
        this.piece = piece;
    }

    public int getQuantiteDemandee() {
        return quantiteDemandee;
    }

    public void setQuantiteDemandee(int quantiteDemandee) {
        this.quantiteDemandee = quantiteDemandee;
    }

    public Double getPrixTotalReception() {
        return prixTotalReception;
    }

    public void setPrixTotalReception(Double prixTotalReception) {
        this.prixTotalReception = prixTotalReception;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public LocalDateTime getDateDemande() {
        return dateDemande;
    }

    public void setDateDemande(LocalDateTime dateDemande) {
        this.dateDemande = dateDemande;
    }

    public LocalDateTime getDateReception() {
        return dateReception;
    }

    public void setDateReception(LocalDateTime dateReception) {
        this.dateReception = dateReception;
    }

    public StatutDemandePiece getStatut() {
        return statut;
    }

    public void setStatut(StatutDemandePiece statut) {
        this.statut = statut;
    }
}
