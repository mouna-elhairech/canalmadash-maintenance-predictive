package com.canal.canalmadash.repository;

import com.canal.canalmadash.Enum.StatutDemandePiece;
import com.canal.canalmadash.entity.DemandePieceRechange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository des demandes de pieces de rechange.
 */
public interface DemandePieceRechangeRepository extends JpaRepository<DemandePieceRechange, Long> {
    // Verifie s'il existe deja une demande ouverte pour eviter les doublons automatiques.
    boolean existsByPieceIdAndStatutIn(Long pieceId, List<StatutDemandePiece> statuts);

    // Liste de suivi des demandes de pieces, de la plus recente a la plus ancienne.
    List<DemandePieceRechange> findAllByOrderByDateDemandeDesc();
}
