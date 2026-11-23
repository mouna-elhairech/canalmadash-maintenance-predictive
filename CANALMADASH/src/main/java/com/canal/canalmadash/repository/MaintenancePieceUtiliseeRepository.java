package com.canal.canalmadash.repository;

import com.canal.canalmadash.entity.MaintenancePieceUtilisee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository des pieces consommees pendant une intervention.
 */
@Repository
public interface MaintenancePieceUtiliseeRepository extends JpaRepository<MaintenancePieceUtilisee, Long> {
}
