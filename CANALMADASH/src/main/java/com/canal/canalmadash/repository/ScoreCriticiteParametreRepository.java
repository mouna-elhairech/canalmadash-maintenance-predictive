package com.canal.canalmadash.repository;

import com.canal.canalmadash.entity.ScoreCriticiteParametre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository de la configuration globale du score de criticite.
 * La table contient normalement une seule ligne avec id=1.
 */
@Repository
public interface ScoreCriticiteParametreRepository extends JpaRepository<ScoreCriticiteParametre, Long> {
}
