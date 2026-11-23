package com.canal.canalmadash.repository;

import com.canal.canalmadash.entity.RoleApplication;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository des roles applicatifs.
 * Les EntityGraph chargent les fonctionnalites avec les roles pour afficher
 * les droits sans requetes supplementaires.
 */
@Repository
public interface RoleApplicationRepository extends JpaRepository<RoleApplication, Long> {
    // Recherche par code stable, utilisee pour initialiser ou modifier un role precis.
    Optional<RoleApplication> findByCode(String code);

    // Roles actifs avec leurs fonctionnalites deja chargees.
    @EntityGraph(attributePaths = "fonctionnalites")
    List<RoleApplication> findByActifTrueOrderByLibelleAsc();

    // Liste complete affichee dans l'ecran de parametrage.
    @EntityGraph(attributePaths = "fonctionnalites")
    List<RoleApplication> findAllByOrderByLibelleAsc();

    // Charge un role avec ses droits avant modification.
    @EntityGraph(attributePaths = "fonctionnalites")
    Optional<RoleApplication> findWithFonctionnalitesById(Long id);
}
