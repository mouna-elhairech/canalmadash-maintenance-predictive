package com.canal.canalmadash.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;

@MappedSuperclass
public abstract class AuditableEntity {

    /*
     * Classe de base pour les entities qui doivent garder une trace temporelle.
     * createdAt est fixe a la creation; updatedAt change automatiquement avant
     * chaque update JPA.
     */
    @Column(updatable = false)
    protected LocalDateTime createdAt = LocalDateTime.now();

    protected LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        // Callback JPA appele automatiquement avant la sauvegarde d'une modification.
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
