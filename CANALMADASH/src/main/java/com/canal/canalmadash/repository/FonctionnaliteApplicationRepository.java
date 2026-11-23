package com.canal.canalmadash.repository;

import com.canal.canalmadash.entity.FonctionnaliteApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository des fonctionnalites applicatives rattachees aux roles.
 */
@Repository
public interface FonctionnaliteApplicationRepository extends JpaRepository<FonctionnaliteApplication, Long> {
    // Recherche par code stable lors de l'initialisation des droits.
    Optional<FonctionnaliteApplication> findByCode(String code);
}
