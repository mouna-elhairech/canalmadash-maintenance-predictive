package com.canal.canalmadash.repository;

import com.canal.canalmadash.entity.TypeMaintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository des types de maintenance.
 * Ces types sont utilises par les demandes, interventions et regles preventives.
 */
@Repository
public interface TypeMaintenanceRepository extends JpaRepository<TypeMaintenance, Integer> {

    // Recherche insensible a la casse pour eviter les doublons de codes.
    Optional<TypeMaintenance> findByCodeIgnoreCase(String code);

    // Liste des types utilisables par les utilisateurs.
    List<TypeMaintenance> findByActifTrue();
}
