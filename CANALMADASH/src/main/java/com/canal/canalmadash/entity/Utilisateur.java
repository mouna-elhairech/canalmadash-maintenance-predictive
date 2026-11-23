package com.canal.canalmadash.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "utilisateur")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "mot_de_passe_hash", nullable = false, length = 60)
    private String motDePasseHash;

    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id")
    private RoleApplication role;

    private boolean actif = true;

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public String getEmail() { return email; }
    public String getMotDePasseHash() { return motDePasseHash; }
    public RoleApplication getRole() { return role; }
    public boolean isActif() { return actif; }
}