package com.canal.canalmadash.repository;

import com.canal.canalmadash.entity.Action;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository de l'historique des actions machine.
 * Une action correspond a une periode pendant laquelle une machine reste dans
 * un etat donne.
 */
@Repository
public interface ActionRepository extends JpaRepository<Action, Integer> {

    // Historique complet d'une machine, dans l'ordre chronologique.
    List<Action> findByMachineIdOrderByDateDebutAsc(int machineId);

    // actions dans une période donnée
    List<Action> findByMachineIdAndDateDebutBetweenOrderByDateDebutAsc(
            int machineId,
            LocalDateTime from,
            LocalDateTime to
    );

    // Derniere action ouverte: dateFin null veut dire que l'etat est encore actif.
    Action findTopByMachineIdAndDateFinIsNullOrderByDateDebutDesc(int machineId);
}
