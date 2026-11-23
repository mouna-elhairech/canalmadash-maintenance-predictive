package com.canal.canalmadash.repository;

import com.canal.canalmadash.entity.RegleMaintenancePreventive;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository des regles de maintenance preventive.
 */
public interface RegleMaintenancePreventiveRepository extends JpaRepository<RegleMaintenancePreventive, Long> {
    // Regles actives affichees dans le dashboard, triees par nom de machine.
    List<RegleMaintenancePreventive> findByActifTrueOrderByMachineNomAsc();

    // Regles d'une machine pour sa fiche detaillee.
    List<RegleMaintenancePreventive> findByMachineIdOrderByIdDesc(int machineId);
}
