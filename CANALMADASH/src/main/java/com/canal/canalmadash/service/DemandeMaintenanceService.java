package com.canal.canalmadash.service;

import com.canal.canalmadash.Enum.StatutDemandeMaintenance;
import com.canal.canalmadash.Enum.PrioriteDemandeMaintenance;
import com.canal.canalmadash.dto.DemandeMaintenanceRequest;
import com.canal.canalmadash.entity.DemandeMaintenance;
import com.canal.canalmadash.entity.Machine;
import com.canal.canalmadash.entity.TypeMaintenance;
import com.canal.canalmadash.repository.DemandeMaintenanceRepository;
import com.canal.canalmadash.repository.MachineRepository;
import com.canal.canalmadash.repository.TypeMaintenanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DemandeMaintenanceService {

    /*
     * Service des demandes de maintenance.
     * Il gere le cycle de vie complet d'une demande: creation par operateur,
     * planification, intervention, validation finale ou annulation.
     */
    private final MachineRepository machineRepository;
    private final TypeMaintenanceRepository typeMaintenanceRepository;
    private final DemandeMaintenanceRepository demandeMaintenanceRepository;
    private final ImageStorageService imageStorageService;


    public DemandeMaintenanceService(MachineRepository machineRepository, TypeMaintenanceRepository typeMaintenanceRepository, DemandeMaintenanceRepository demandeMaintenanceRepository, ImageStorageService imageStorageService) {
        this.machineRepository = machineRepository;
        this.typeMaintenanceRepository = typeMaintenanceRepository;
        this.demandeMaintenanceRepository = demandeMaintenanceRepository;
        this.imageStorageService = imageStorageService;
    }

    public List<DemandeMaintenance> findByMachine(int machineId, LocalDateTime from, LocalDateTime to) {
        // Si une periode est fournie, on limite l'historique affiche dans le detail machine.
        if (from != null && to != null) {
            return demandeMaintenanceRepository
                    .findByMachineIdAndDateDemandeBetweenOrderByDateDemandeDesc(machineId, from, to);
        }
        return demandeMaintenanceRepository.findByMachineIdOrderByDateDemandeDesc(machineId);
    }

    public DemandeMaintenance findById(int id) {
        return demandeMaintenanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("DemandeMaintenance introuvable id=" + id));
    }

    @Transactional(readOnly = true)
    public List<DemandeMaintenance> search(
            Integer machineId,
            StatutDemandeMaintenance statut,
            PrioriteDemandeMaintenance priorite,
            Integer typeMaintenanceId,
            LocalDateTime from,
            LocalDateTime to,
            Boolean retard,
            Double coutMin
    ) {
        /*
         * Recherche multi-criteres pour le kanban et les vues de pilotage.
         * Le filtrage reste ici cote service pour garder une API simple.
         */
        LocalDateTime now = LocalDateTime.now();
        return demandeMaintenanceRepository.findAll().stream()
                .filter(d -> machineId == null || d.getMachine() != null && d.getMachine().getId() == machineId)
                .filter(d -> statut == null || d.getStatut() == statut)
                .filter(d -> priorite == null || d.getPriorite() == priorite)
                .filter(d -> typeMaintenanceId == null || d.getTypeMaintenance() != null && d.getTypeMaintenance().getId() == typeMaintenanceId)
                .filter(d -> from == null || d.getDateDemande() != null && !d.getDateDemande().isBefore(from))
                .filter(d -> to == null || d.getDateDemande() != null && !d.getDateDemande().isAfter(to))
                .filter(d -> retard == null || retard == isLate(d, now))
                .filter(d -> coutMin == null || d.getMaintenance() != null && d.getMaintenance().getCout() != null && d.getMaintenance().getCout() >= coutMin)
                .sorted((a, b) -> b.getDateDemande().compareTo(a.getDateDemande()))
                .toList();
    }

    private boolean isLate(DemandeMaintenance demande, LocalDateTime now) {
        // Une demande est en retard si la date souhaitee est depassee et qu'elle n'est pas cloturee.
        return demande.getDateSouhaitee() != null
                && demande.getDateSouhaitee().isBefore(now)
                && demande.getStatut() != StatutDemandeMaintenance.TERMINEE
                && demande.getStatut() != StatutDemandeMaintenance.ANNULEE;
    }
    public void delete(int id) {
        DemandeMaintenance dm = findById(id);
        if (dm.getImagePath() != null) {
            imageStorageService.delete(dm.getImagePath());
        }
        demandeMaintenanceRepository.deleteById(id);
    }

    @Transactional
    public DemandeMaintenance updateStatut(int id, StatutDemandeMaintenance statut, LocalDateTime dateSouhaitee) {
        /*
         * Chaque statut met a jour les dates metier:
         * debut reel quand le travail commence, fin reelle quand il attend
         * validation ou quand il est cloture.
         */
        DemandeMaintenance demande = findById(id);
        validateStatusTransition(demande, statut, dateSouhaitee);
        demande.setStatut(statut);

        LocalDateTime now = LocalDateTime.now();
        switch (statut) {
            case EN_ATTENTE -> {
                demande.setDateDebutReelle(null);
                demande.setDateFinReelle(null);
                demande.setDateSouhaitee(null);
            }
            case PLANIFIEE -> {
                if (dateSouhaitee != null) {
                    demande.setDateSouhaitee(dateSouhaitee);
                }
                demande.setDateFinReelle(null);
            }
            case EN_COURS -> {
                if (demande.getDateDebutReelle() == null) {
                    demande.setDateDebutReelle(now);
                }
                demande.setDateFinReelle(null);
            }
            case EN_ATTENTE_VALIDATION -> {
                if (demande.getDateDebutReelle() == null) {
                    demande.setDateDebutReelle(now);
                }
                if (demande.getDateFinReelle() == null) {
                    demande.setDateFinReelle(now);
                }
            }
            case TERMINEE, ANNULEE -> {
                if (demande.getDateDebutReelle() == null) {
                    demande.setDateDebutReelle(now);
                }
                demande.setDateFinReelle(now);
            }
        }

        return demandeMaintenanceRepository.save(demande);
    }

    @Transactional
    public DemandeMaintenance validerIntervention(int id) {
        DemandeMaintenance demande = findById(id);
        if (demande.getStatut() != StatutDemandeMaintenance.EN_ATTENTE_VALIDATION) {
            throw new IllegalArgumentException("Seule une demande en attente de validation peut etre validee");
        }
        demande.setStatut(StatutDemandeMaintenance.TERMINEE);
        if (demande.getDateFinReelle() == null) {
            demande.setDateFinReelle(LocalDateTime.now());
        }
        return demandeMaintenanceRepository.save(demande);
    }

    @Transactional
    public DemandeMaintenance refuserIntervention(int id) {
        DemandeMaintenance demande = findById(id);
        if (demande.getStatut() != StatutDemandeMaintenance.EN_ATTENTE_VALIDATION) {
            throw new IllegalArgumentException("Seule une demande en attente de validation peut etre refusee");
        }
        demande.setStatut(StatutDemandeMaintenance.EN_COURS);
        demande.setDateFinReelle(null);
        return demandeMaintenanceRepository.save(demande);
    }

    private void validateStatusTransition(
            DemandeMaintenance demande,
            StatutDemandeMaintenance next,
            LocalDateTime dateSouhaitee
    ) {
        // Garde-fous pour eviter les retours incoherents dans le cycle de vie.
        if (demande.getMaintenance() != null
                && next != StatutDemandeMaintenance.EN_ATTENTE_VALIDATION
                && next != StatutDemandeMaintenance.TERMINEE) {
            throw new IllegalArgumentException("Une demande avec intervention doit etre en validation ou terminee");
        }
        if (demande.getStatut() == StatutDemandeMaintenance.TERMINEE && next != StatutDemandeMaintenance.TERMINEE) {
            throw new IllegalArgumentException("Une demande terminee ne peut pas etre rouverte");
        }
        if (demande.getStatut() == StatutDemandeMaintenance.ANNULEE && next != StatutDemandeMaintenance.ANNULEE) {
            throw new IllegalArgumentException("Une demande annulee ne peut pas etre rouverte");
        }
        if (next == StatutDemandeMaintenance.PLANIFIEE
                && dateSouhaitee == null
                && demande.getDateSouhaitee() == null) {
            throw new IllegalArgumentException("Une date de planification est obligatoire");
        }
    }

    public DemandeMaintenance createDemande(DemandeMaintenanceRequest dto, MultipartFile image) {
        // 1) On rattache la demande a une machine existante.
        Machine machine = machineRepository.findById(dto.getMachineId())
                .orElseThrow(() -> new IllegalArgumentException("Machine introuvable id=" + dto.getMachineId()));

        // 2) On utilise un type existant ou on cree une proposition inactive.
        TypeMaintenance typeMaintenance;
        if (dto.getTypeMaintenanceId() != null) {
            typeMaintenance = typeMaintenanceRepository.findById(dto.getTypeMaintenanceId())
                    .orElseThrow(() -> new IllegalArgumentException("TypeMaintenance introuvable id=" + dto.getTypeMaintenanceId()));
        } else if (dto.getNouveauTypeLibelle() != null && !dto.getNouveauTypeLibelle().isBlank()) {
            String libelle = dto.getNouveauTypeLibelle().trim();
            String code = slugify(libelle);

            TypeMaintenance tm = new TypeMaintenance();
            tm.setCode(code);
            tm.setLibelle(libelle);
            tm.setActif(false);   // Le responsable pourra valider ce nouveau type plus tard.
            typeMaintenance = typeMaintenanceRepository.save(tm);
        } else {
            throw new IllegalArgumentException("Il faut soit typeMaintenanceId, soit nouveauTypeLibelle");
        }

        // 3) La demande commence toujours en attente, avec une priorite par defaut.
        DemandeMaintenance demande = new DemandeMaintenance();
        demande.setMachine(machine);
        demande.setTypeMaintenance(typeMaintenance);
        demande.setDescription(dto.getDescription());
        demande.setDemandeur(dto.getDemandeur());
        demande.setDateDemande(LocalDateTime.now());
        demande.setStatut(StatutDemandeMaintenance.EN_ATTENTE);
        demande.setPriorite(dto.getPriorite() == null ? PrioriteDemandeMaintenance.NORMALE : dto.getPriorite());

        // Premier save obligatoire pour obtenir l'id utilise dans le nom du fichier image.
        demande = demandeMaintenanceRepository.save(demande);

        // 4) Image optionnelle ajoutee par l'operateur pour illustrer le probleme.
        if (image != null && !image.isEmpty()) {
            String path = imageStorageService.storeDemandeImage(demande.getId(), image);
            demande.setImagePath(path);
            demande = demandeMaintenanceRepository.save(demande);
        }

        return demande;
    }

    private String slugify(String label) {
        String code = label.toUpperCase()
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        if (code.length() > 40) {
            code = code.substring(0, 40);
        }
        return code;
    }
}
