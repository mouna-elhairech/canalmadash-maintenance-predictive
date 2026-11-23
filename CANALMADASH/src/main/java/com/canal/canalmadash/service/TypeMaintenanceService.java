package com.canal.canalmadash.service;

import com.canal.canalmadash.entity.TypeMaintenance;
import com.canal.canalmadash.repository.TypeMaintenanceRepository;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
public class TypeMaintenanceService {

    /*
     * Service de parametrage des types de maintenance.
     * Ces types sont utilises dans les demandes, interventions et regles
     * preventives.
     */
    private final TypeMaintenanceRepository typeMaintenanceRepository;

    public TypeMaintenanceService(TypeMaintenanceRepository typeMaintenanceRepository) {
        this.typeMaintenanceRepository = typeMaintenanceRepository;
    }


    public List<TypeMaintenance> findAll() {
        return typeMaintenanceRepository.findAll();
    }


    public List<TypeMaintenance> findActifs() {
        return typeMaintenanceRepository.findByActifTrue();
    }



    public TypeMaintenance findById(int id) {
        return typeMaintenanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("TypeMaintenance introuvable id=" + id));
    }



    public TypeMaintenance create(TypeMaintenance type) {
        // Force une creation et applique une couleur neutre si le front n'envoie rien.
        type.setId(0);
        if (type.getColor() == null || type.getColor().isBlank()) {
            type.setColor("#64748b");
        }
        return typeMaintenanceRepository.save(type);
    }



    public TypeMaintenance update(int id, TypeMaintenance update) {
        TypeMaintenance existing = findById(id);
        existing.setCode(update.getCode());
        existing.setLibelle(update.getLibelle());
        existing.setActif(update.isActif());
        existing.setColor(update.getColor());
        return typeMaintenanceRepository.save(existing);
    }



    public void delete(int id) {
        typeMaintenanceRepository.deleteById(id);
    }



    public TypeMaintenance disable(int id) {
        // Desactivation logique pour ne pas casser les anciennes demandes/interventions.
        TypeMaintenance typeMaintenance = this.findById(id);
        typeMaintenance.setActif(false);
        return typeMaintenanceRepository.save(typeMaintenance);
    }




    public TypeMaintenance enable(int id) {
        TypeMaintenance typeMaintenance = this.findById(id);
        typeMaintenance.setActif(true);
        return typeMaintenanceRepository.save(typeMaintenance);
    }



}
