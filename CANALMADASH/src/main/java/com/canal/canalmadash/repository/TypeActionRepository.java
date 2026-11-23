package com.canal.canalmadash.repository;

import com.canal.canalmadash.entity.TypeAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository des types d'action machine.
 * Ces types servent aux etats courants et a l'historique des machines.
 */
@Repository
public interface TypeActionRepository extends JpaRepository<TypeAction, Integer> {

    // Recherche insensible a la casse pour eviter les doublons de codes.
    Optional<TypeAction> findByCodeIgnoreCase(String code);

    // Liste des types disponibles dans les formulaires.
    List<TypeAction> findByActifTrue();
}

