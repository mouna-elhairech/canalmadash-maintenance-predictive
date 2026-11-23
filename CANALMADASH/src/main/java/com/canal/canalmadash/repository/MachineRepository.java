package com.canal.canalmadash.repository;

import com.canal.canalmadash.entity.Machine;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository des machines.
 * JpaRepository fournit deja les operations CRUD de base.
 */
public interface MachineRepository extends JpaRepository<Machine, Integer> {
}
