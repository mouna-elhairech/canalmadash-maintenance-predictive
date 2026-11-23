package com.canal.canalmadash.service;

import com.canal.canalmadash.entity.ScoreCriticiteParametre;
import com.canal.canalmadash.repository.ScoreCriticiteParametreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScoreCriticiteParametreService {

    /*
     * Service de configuration du score de criticite.
     * L'application garde une seule ligne de parametres, identifiee par id=1.
     */
    private static final long SINGLETON_ID = 1L;
    private final ScoreCriticiteParametreRepository repository;

    public ScoreCriticiteParametreService(ScoreCriticiteParametreRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ScoreCriticiteParametre getOrCreate() {
        // Cree les parametres par defaut si la table est encore vide.
        return repository.findById(SINGLETON_ID).orElseGet(() -> repository.save(new ScoreCriticiteParametre()));
    }

    @Transactional
    public ScoreCriticiteParametre update(ScoreCriticiteParametre request) {
        // Normalise les valeurs pour eviter les poids negatifs ou pourcentages hors limites.
        ScoreCriticiteParametre existing = getOrCreate();
        existing.setPoidsCoutMaintenance(nonNegative(request.getPoidsCoutMaintenance()));
        existing.setPoidsNombrePannes(nonNegative(request.getPoidsNombrePannes()));
        existing.setPoidsTempsArret(nonNegative(request.getPoidsTempsArret()));
        existing.setPoidsDisponibilite(nonNegative(request.getPoidsDisponibilite()));
        existing.setPoidsTempsReparation(nonNegative(request.getPoidsTempsReparation()));
        existing.setFacteurSurveillance(percent(request.getFacteurSurveillance()));
        existing.setFacteurAttention(percent(request.getFacteurAttention()));
        existing.setFacteurCritique(percent(request.getFacteurCritique()));
        existing.setSeuilNormalMax(nonNegative(request.getSeuilNormalMax()));
        existing.setSeuilSurveillanceMax(Math.max(existing.getSeuilNormalMax(), request.getSeuilSurveillanceMax()));
        return repository.save(existing);
    }

    @Transactional
    public ScoreCriticiteParametre resetDefaults() {
        // Remet les valeurs par defaut definies dans l'entity.
        ScoreCriticiteParametre defaults = new ScoreCriticiteParametre();
        defaults.setId(SINGLETON_ID);
        return repository.save(defaults);
    }

    private double nonNegative(double value) {
        return Math.max(0, value);
    }

    private double percent(double value) {
        return Math.max(0, Math.min(100, value));
    }
}
