package com.canal.canalmadash.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class PieceRechange {

    /*
     * Piece de rechange geree en stock.
     * Le stock est valorise: quantite, valeur totale et cout moyen unitaire.
     * Une piece peut etre standard ou compatible avec une liste precise de machines.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reference;
    private String libelle;
    private int quantite;
    private int seuilMinimum;
    private Double coutUnitaire;
    private Double valeurStock;
    // true: compatible avec toutes les machines. false: compatibilite controlee par machinesCompatibles.
    private boolean standard = true;

    // Relation many-to-many pour les pieces non standards.
    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "piece_rechange_machine",
            joinColumns = @JoinColumn(name = "piece_id"),
            inverseJoinColumns = @JoinColumn(name = "machine_id")
    )
    private List<Machine> machinesCompatibles = new ArrayList<>();

    @Transient
    private List<Integer> machineIds = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public int getSeuilMinimum() {
        return seuilMinimum;
    }

    public void setSeuilMinimum(int seuilMinimum) {
        this.seuilMinimum = seuilMinimum;
    }

    public Double getCoutUnitaire() {
        return coutUnitaire;
    }

    public void setCoutUnitaire(Double coutUnitaire) {
        this.coutUnitaire = coutUnitaire;
    }

    public Double getValeurStock() {
        // Compatibilite avec les anciennes donnees: recalcule si la valeur n'est pas encore stockee.
        if (valeurStock == null) {
            return quantite * (coutUnitaire == null ? 0 : coutUnitaire);
        }
        return valeurStock;
    }

    public void setValeurStock(Double valeurStock) {
        this.valeurStock = valeurStock;
    }

    public boolean isStandard() {
        return standard;
    }

    public void setStandard(boolean standard) {
        this.standard = standard;
    }

    public List<Machine> getMachinesCompatibles() {
        return machinesCompatibles;
    }

    public void setMachinesCompatibles(List<Machine> machinesCompatibles) {
        this.machinesCompatibles = machinesCompatibles;
    }

    public List<Integer> getMachineIds() {
        return machineIds;
    }

    public void setMachineIds(List<Integer> machineIds) {
        this.machineIds = machineIds;
    }

    public boolean isAlerteStock() {
        // Utilise par le dashboard et par la creation automatique de demande d'achat.
        return quantite <= seuilMinimum;
    }
}
