package com.canal.canalmadash.config;

import com.canal.canalmadash.entity.TypeAction;
import com.canal.canalmadash.repository.TypeActionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DefaultTypeActionData implements CommandLineRunner {

    /*
     * Initialisation des etats machine indispensables au demarrage.
     * La methode ensure evite les doublons si les donnees existent deja.
     */
    private final TypeActionRepository typeActionRepository;

    public DefaultTypeActionData(TypeActionRepository typeActionRepository) {
        this.typeActionRepository = typeActionRepository;
    }

    @Override
    public void run(String... args) {
        ensure("MARCHE", "En marche", "#16a34a");
        ensure("PANNE", "Panne", "#dc2626");
        ensure("MAINTENANCE", "Maintenance", "#2563eb");
        ensure("ARRET", "Arret", "#f97316");
        ensure("PRESTATION", "Prestation", "#7c3aed");
    }

    private void ensure(String code, String libelle, String color) {
        // Cree le type seulement s'il n'existe pas encore.
        typeActionRepository.findByCodeIgnoreCase(code).orElseGet(() -> {
            TypeAction typeAction = new TypeAction();
            typeAction.setCode(code);
            typeAction.setLibelle(libelle);
            typeAction.setColor(color);
            typeAction.setActif(true);
            return typeActionRepository.save(typeAction);
        });
    }
}
