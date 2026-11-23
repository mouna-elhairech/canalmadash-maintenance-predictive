package com.canal.canalmadash.repository;

import com.canal.canalmadash.entity.DemandeInterventionExterne;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository des demandes d'intervention externe.
 */
public interface DemandeInterventionExterneRepository extends JpaRepository<DemandeInterventionExterne, Long> {
    // Demandes fournisseur rattachees a une machine, de la plus recente a la plus ancienne.
    List<DemandeInterventionExterne> findByMachineIdOrderByDateCreationDesc(int machineId);
}
