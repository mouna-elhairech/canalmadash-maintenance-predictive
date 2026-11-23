package com.canal.canalmadash.repository;

import com.canal.canalmadash.entity.MouvementStockPiece;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository de l'historique des mouvements de stock.
 */
public interface MouvementStockPieceRepository extends JpaRepository<MouvementStockPiece, Long> {
    // Historique d'une piece, utile pour comprendre les entrees, sorties et retours.
    List<MouvementStockPiece> findByPieceIdOrderByDateMouvementDesc(Long pieceId);
}
