package com.canal.canalmadash.repository;

import com.canal.canalmadash.entity.Maintenance;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository des interventions de maintenance.
 * Les EntityGraph chargent le type et les pieces en une seule requete logique
 * pour les rapports, exports et reponses API detaillees.
 */
@Repository
public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {

    // Charge les interventions avec leurs pieces pour eviter les acces lazy cote JSON/rapport.
    @EntityGraph(attributePaths = {"type", "pieces", "pieces.piece"})
    List<Maintenance> findByMachineIdOrderByDateDebutDesc(int machineId);

    // Recharge une intervention complete apres creation, avec les pieces detaillees.
    @EntityGraph(attributePaths = {"type", "pieces", "pieces.piece"})
    Optional<Maintenance> findWithPiecesById(Long id);
}
