package com.canal.canalmadash.repository;

import com.canal.canalmadash.Enum.StatutDemandeMaintenance;
import com.canal.canalmadash.entity.DemandeMaintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository des demandes de maintenance.
 * Il fournit les recherches necessaires au detail machine, au kanban et aux
 * regles preventives.
 */
@Repository
public interface DemandeMaintenanceRepository extends JpaRepository<DemandeMaintenance, Integer> {

    // Historique complet des demandes d'une machine, de la plus recente a la plus ancienne.
    List<DemandeMaintenance> findByMachineIdOrderByDateDemandeDesc(int machineId);

    // Meme historique, mais limite a une periode pour les filtres du detail machine.
    List<DemandeMaintenance> findByMachineIdAndDateDemandeBetweenOrderByDateDemandeDesc(
            int machineId,
            LocalDateTime from,
            LocalDateTime to
    );

    // Recherche des demandes d'une machine dans un statut precis.
    List<DemandeMaintenance> findByMachineIdAndStatutOrderByDateDemandeDesc(
            int machineId,
            StatutDemandeMaintenance statut
    );

    // Utilise par les regles preventives pour eviter les doublons de demandes ouvertes.
    boolean existsByReglePreventiveIdAndStatutNotIn(Long reglePreventiveId, List<StatutDemandeMaintenance> statuts);

}
