package com.canal.canalmadash.repository;

import com.canal.canalmadash.entity.PieceRechange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository des pieces de rechange.
 * Il contient la recherche des pieces compatibles avec une machine precise.
 */
public interface PieceRechangeRepository extends JpaRepository<PieceRechange, Long> {
    // Pieces sous un seuil donne, triees pour afficher les plus critiques en premier.
    List<PieceRechange> findByQuantiteLessThanEqualOrderByQuantiteAsc(int seuilMinimum);

    @Query("""
            select distinct p
            from PieceRechange p
            left join p.machinesCompatibles m
            where p.standard = true or m.id = :machineId
            order by p.reference asc
            """)
    // Une piece standard est compatible partout; sinon elle doit etre liee a la machine.
    List<PieceRechange> findCompatiblesForMachine(@Param("machineId") int machineId);
}
