package com.canal.canalmadash.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Action {

    /*
     * Une Action represente une periode d'etat pour une machine.
     * Exemple: machine en marche de 08:00 a 12:30, puis en panne a partir de 12:30.
     * Si dateFin est null, cela veut dire que l'etat est encore en cours.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(optional = false)
    private TypeAction typeAction;

    private LocalDateTime dateDebut;

    // Null tant que cette action est l'etat courant de la machine.
    private LocalDateTime dateFin;

    // Relation vers la machine concernee, ignoree en JSON pour eviter une boucle infinie.
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "machine_id")
    private Machine machine;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TypeAction getTypeAction() {
        return typeAction;
    }

    public void setTypeAction(TypeAction typeAction) {
        this.typeAction = typeAction;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }
}
