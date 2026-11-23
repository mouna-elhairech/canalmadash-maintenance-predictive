package com.canal.canalmadash.service;

import com.canal.canalmadash.Enum.PrioriteDemandeMaintenance;
import com.canal.canalmadash.Enum.StatutDemandeMaintenance;
import com.canal.canalmadash.Enum.TypeReglePreventive;
import com.canal.canalmadash.Enum.UniteControlePeriodique;
import com.canal.canalmadash.dto.RegleMaintenancePreventiveRequest;
import com.canal.canalmadash.entity.*;
import com.canal.canalmadash.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RegleMaintenancePreventiveService {

    /*
     * Service des controles periodiques.
     * Il permet de creer des regles preventives et de generer automatiquement
     * des demandes de maintenance quand une regle arrive a echeance.
     */
    private final RegleMaintenancePreventiveRepository regleRepository;
    private final MachineRepository machineRepository;
    private final TypeMaintenanceRepository typeMaintenanceRepository;
    private final DemandeMaintenanceRepository demandeRepository;

    public RegleMaintenancePreventiveService(
            RegleMaintenancePreventiveRepository regleRepository,
            MachineRepository machineRepository,
            TypeMaintenanceRepository typeMaintenanceRepository,
            DemandeMaintenanceRepository demandeRepository
    ) {
        this.regleRepository = regleRepository;
        this.machineRepository = machineRepository;
        this.typeMaintenanceRepository = typeMaintenanceRepository;
        this.demandeRepository = demandeRepository;
    }

    public List<RegleMaintenancePreventive> findAll() {
        return regleRepository.findByActifTrueOrderByMachineNomAsc();
    }

    public List<RegleMaintenancePreventive> findByMachine(int machineId) {
        return regleRepository.findByMachineIdOrderByIdDesc(machineId);
    }

    public RegleMaintenancePreventive create(RegleMaintenancePreventiveRequest request) {
        // Pour l'instant, la creation gere les controles periodiques planifies.
        validateControlePeriodiqueRequest(request);
        Machine machine = machineRepository.findById(request.getMachineId())
                .orElseThrow(() -> new IllegalArgumentException("Machine introuvable id=" + request.getMachineId()));
        TypeMaintenance typeMaintenance = typeMaintenanceRepository.findById(request.getTypeMaintenanceId())
                .orElseThrow(() -> new IllegalArgumentException("TypeMaintenance introuvable id=" + request.getTypeMaintenanceId()));
        RegleMaintenancePreventive regle = new RegleMaintenancePreventive();
        regle.setMachine(machine);
        regle.setTypeMaintenance(typeMaintenance);
        regle.setTypeRegle(TypeReglePreventive.CONTROLE_PERIODIQUE);
        regle.setValeur(null);
        regle.setFrequence(request.getFrequence());
        regle.setUnite(request.getUnite());
        regle.setDateDebut(request.getDateDebut());
        regle.setProchaineDate(resolveInitialNextDate(request));
        regle.setDescription(request.getDescription());
        regle.setActif(true);
        return regleRepository.save(regle);
    }

    public void disable(Long id) {
        // On desactive plutot que supprimer pour garder l'historique et eviter les pertes.
        RegleMaintenancePreventive regle = regleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Regle introuvable id=" + id));
        regle.setActif(false);
        regleRepository.save(regle);
    }

    @Transactional
    public int generateDemandes() {
        /*
         * Parcourt les regles actives et cree une demande planifiee si:
         * - la date est arrivee,
         * - aucune demande ouverte n'existe deja pour cette regle.
         */
        int generated = 0;
        LocalDateTime now = LocalDateTime.now();
        for (RegleMaintenancePreventive regle : findAll()) {
            if (shouldGenerate(regle, now) && !hasOpenPreventiveDemand(regle)) {
                DemandeMaintenance demande = new DemandeMaintenance();
                demande.setMachine(regle.getMachine());
                demande.setTypeMaintenance(regle.getTypeMaintenance());
                demande.setDescription(buildDescription(regle));
                demande.setDemandeur("SYSTEME_PREVENTIF");
                demande.setDateDemande(now);
                demande.setDateSouhaitee(resolveDateSouhaitee(regle, now));
                demande.setStatut(StatutDemandeMaintenance.PLANIFIEE);
                demande.setPriorite(PrioriteDemandeMaintenance.NORMALE);
                demande.setReglePreventive(regle);
                demandeRepository.save(demande);
                regle.setDerniereGeneration(now);
                if (regle.getTypeRegle() == TypeReglePreventive.CONTROLE_PERIODIQUE) {
                    regle.setProchaineDate(nextPeriodicDate(resolveDateSouhaitee(regle, now), regle));
                }
                regleRepository.save(regle);
                generated++;
            }
        }
        return generated;
    }

    private boolean shouldGenerate(RegleMaintenancePreventive regle, LocalDateTime now) {
        return regle.getTypeRegle() == TypeReglePreventive.CONTROLE_PERIODIQUE
                && shouldGeneratePeriodic(regle, now);
    }

    private boolean hasOpenPreventiveDemand(RegleMaintenancePreventive regle) {
        // Evite de creer plusieurs demandes pour le meme controle non termine.
        if (regle.getId() != null) {
            return demandeRepository.existsByReglePreventiveIdAndStatutNotIn(
                    regle.getId(),
                    List.of(StatutDemandeMaintenance.TERMINEE, StatutDemandeMaintenance.ANNULEE)
            );
        }
        return false;
    }

    private boolean shouldGeneratePeriodic(RegleMaintenancePreventive regle, LocalDateTime now) {
        if (regle.getFrequence() == null || regle.getFrequence() <= 0 || regle.getUnite() == null) {
            return false;
        }
        LocalDateTime next = regle.getProchaineDate() == null ? resolveFallbackStartDate(regle) : regle.getProchaineDate();
        return !next.isAfter(now);
    }

    private LocalDateTime resolveInitialNextDate(RegleMaintenancePreventiveRequest request) {
        if (request.getTypeRegle() != TypeReglePreventive.CONTROLE_PERIODIQUE) {
            return null;
        }
        LocalDateTime start = request.getDateDebut() == null ? LocalDateTime.now() : request.getDateDebut();
        return start;
    }

    private LocalDateTime resolveFallbackStartDate(RegleMaintenancePreventive regle) {
        return regle.getDateDebut() == null ? LocalDateTime.now() : regle.getDateDebut();
    }

    private LocalDateTime resolveDateSouhaitee(RegleMaintenancePreventive regle, LocalDateTime now) {
        if (regle.getTypeRegle() == TypeReglePreventive.CONTROLE_PERIODIQUE && regle.getProchaineDate() != null) {
            return regle.getProchaineDate();
        }
        return now.plusDays(1);
    }

    private LocalDateTime nextPeriodicDate(LocalDateTime from, RegleMaintenancePreventive regle) {
        // Calcule la prochaine echeance selon l'unite choisie par l'utilisateur.
        int value = Math.max(regle.getFrequence() == null ? 1 : regle.getFrequence(), 1);
        UniteControlePeriodique unit = regle.getUnite() == null ? UniteControlePeriodique.JOUR : regle.getUnite();
        return switch (unit) {
            case JOUR -> from.plusDays(value);
            case SEMAINE -> from.plusWeeks(value);
            case MOIS -> from.plusMonths(value);
            case ANNEE -> from.plusYears(value);
        };
    }

    private String buildDescription(RegleMaintenancePreventive regle) {
        return "Controle periodique genere automatiquement: "
                + (regle.getDescription() == null || regle.getDescription().isBlank()
                ? describeRule(regle)
                : regle.getDescription());
    }

    private String describeRule(RegleMaintenancePreventive regle) {
        return "Controle periodique chaque " + regle.getFrequence() + " " + regle.getUnite();
    }

    private void validateControlePeriodiqueRequest(RegleMaintenancePreventiveRequest request) {
        if (request.getFrequence() == null || request.getFrequence() <= 0) {
            throw new IllegalArgumentException("La frequence du controle periodique est obligatoire");
        }
        if (request.getUnite() == null) {
            throw new IllegalArgumentException("L'unite du controle periodique est obligatoire");
        }
    }
}
