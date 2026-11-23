package com.canal.canalmadash.service;

import com.canal.canalmadash.entity.TypeAction;
import com.canal.canalmadash.repository.TypeActionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TypeActionService {

    /*
     * Service de parametrage des etats machine.
     * Les types d'action alimentent l'etat courant des machines et les
     * historiques Action.
     */
    private final TypeActionRepository typeActionRepository;

    public TypeActionService(TypeActionRepository typeActionRepository) {
        this.typeActionRepository = typeActionRepository;
    }

    public List<TypeAction> findAll() {
        return typeActionRepository.findAll();
    }

    public List<TypeAction> findActifs() {
        return typeActionRepository.findByActifTrue();
    }

    public TypeAction findById(int id) {
        return typeActionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("TypeAction introuvable id=" + id));
    }

    public TypeAction create(TypeAction type) {
        // Force une creation et applique une couleur par defaut si elle manque.
        type.setId(0);
        if (type.getColor() == null || type.getColor().isBlank()) {
            type.setColor("#64748b");
        }
        return typeActionRepository.save(type);
    }

    public TypeAction update(int id, TypeAction update) {
        TypeAction existing = findById(id);
        existing.setCode(update.getCode());
        existing.setLibelle(update.getLibelle());
        existing.setActif(update.isActif());
        existing.setColor(update.getColor());
        return typeActionRepository.save(existing);
    }

    public void delete(int id) {
        typeActionRepository.deleteById(id);
    }

    public TypeAction disable(int id) {
        // Desactivation logique pour conserver les anciens historiques.
        TypeAction typeAction = this.findById(id);
        typeAction.setActif(false);
        return typeActionRepository.save(typeAction);
    }

    public TypeAction enable(int id) {
        TypeAction typeAction = this.findById(id);
        typeAction.setActif(true);
        return typeActionRepository.save(typeAction);
    }
}
