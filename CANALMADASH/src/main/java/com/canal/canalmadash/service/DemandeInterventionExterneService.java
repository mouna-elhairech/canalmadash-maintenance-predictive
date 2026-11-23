package com.canal.canalmadash.service;

import com.canal.canalmadash.Enum.StatutDemandeInterventionExterne;
import com.canal.canalmadash.dto.DemandeInterventionExterneRequest;
import com.canal.canalmadash.entity.DemandeInterventionExterne;
import com.canal.canalmadash.entity.DemandeMaintenance;
import com.canal.canalmadash.Enum.StatutDemandeMaintenance;
import com.canal.canalmadash.repository.DemandeInterventionExterneRepository;
import com.canal.canalmadash.repository.DemandeMaintenanceRepository;
import com.canal.canalmadash.repository.MachineRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DemandeInterventionExterneService {

    /*
     * Service des demandes vers prestataire externe.
     * Il verifie la coherence avec la machine et la demande interne avant de
     * creer le brouillon puis controle les transitions de statut.
     */
    private final DemandeInterventionExterneRepository repository;
    private final MachineRepository machineRepository;
    private final DemandeMaintenanceRepository demandeMaintenanceRepository;

    public DemandeInterventionExterneService(
            DemandeInterventionExterneRepository repository,
            MachineRepository machineRepository,
            DemandeMaintenanceRepository demandeMaintenanceRepository
    ) {
        this.repository = repository;
        this.machineRepository = machineRepository;
        this.demandeMaintenanceRepository = demandeMaintenanceRepository;
    }

    public List<DemandeInterventionExterne> findByMachine(int machineId) {
        return repository.findByMachineIdOrderByDateCreationDesc(machineId);
    }

    public DemandeInterventionExterne create(DemandeInterventionExterneRequest request) {
        // Creation d'un brouillon externe a partir des informations fournisseur.
        validateRequest(request);
        DemandeInterventionExterne demande = new DemandeInterventionExterne();
        demande.setMachine(machineRepository.findById(request.getMachineId())
                .orElseThrow(() -> new IllegalArgumentException("Machine introuvable id=" + request.getMachineId())));
        if (request.getDemandeMaintenanceId() != null) {
            // Si une demande interne est liee, elle doit appartenir a la meme machine.
            DemandeMaintenance demandeMaintenance = demandeMaintenanceRepository.findById(request.getDemandeMaintenanceId())
                    .orElseThrow(() -> new IllegalArgumentException("Demande maintenance introuvable id=" + request.getDemandeMaintenanceId()));
            if (demandeMaintenance.getMachine() != null
                    && demandeMaintenance.getMachine().getId() != request.getMachineId()) {
                throw new IllegalArgumentException("La demande externe ne correspond pas a la machine");
            }
            if (demandeMaintenance.getStatut() == StatutDemandeMaintenance.TERMINEE
                    || demandeMaintenance.getStatut() == StatutDemandeMaintenance.ANNULEE) {
                throw new IllegalArgumentException("Impossible de preparer une demande externe pour une demande cloturee");
            }
            demande.setDemandeMaintenance(demandeMaintenance);
        }
        demande.setFournisseurNom(request.getFournisseurNom());
        demande.setFournisseurContact(request.getFournisseurContact());
        demande.setFournisseurTelephone(request.getFournisseurTelephone());
        demande.setFournisseurEmail(request.getFournisseurEmail());
        demande.setFournisseurAdresse(request.getFournisseurAdresse());
        demande.setObjet(request.getObjet());
        demande.setDescription(request.getDescription());
        demande.setDateSouhaitee(request.getDateSouhaitee());
        demande.setDateCreation(LocalDateTime.now());
        demande.setStatut(StatutDemandeInterventionExterne.BROUILLON);
        return repository.save(demande);
    }

    public DemandeInterventionExterne updateStatut(Long id, StatutDemandeInterventionExterne statut) {
        // Passage controle entre brouillon, envoyee, acceptee, planifiee et terminee.
        DemandeInterventionExterne demande = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande externe introuvable id=" + id));
        validateTransition(demande.getStatut(), statut);
        demande.setStatut(statut);
        return repository.save(demande);
    }

    private void validateRequest(DemandeInterventionExterneRequest request) {
        if (request.getFournisseurNom() == null || request.getFournisseurNom().isBlank()) {
            throw new IllegalArgumentException("Le fournisseur est obligatoire");
        }
        if (request.getObjet() == null || request.getObjet().isBlank()) {
            throw new IllegalArgumentException("L'objet de la demande externe est obligatoire");
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new IllegalArgumentException("La description de la demande externe est obligatoire");
        }
    }

    private void validateTransition(StatutDemandeInterventionExterne current, StatutDemandeInterventionExterne next) {
        // Empêche de rouvrir une demande externe deja terminee ou refusee.
        if (current == next) {
            return;
        }
        if (current == StatutDemandeInterventionExterne.TERMINEE
                || current == StatutDemandeInterventionExterne.REFUSEE) {
            throw new IllegalArgumentException("Une demande externe cloturee ne peut pas changer de statut");
        }
        boolean allowed = switch (current) {
            case BROUILLON -> next == StatutDemandeInterventionExterne.ENVOYEE;
            case ENVOYEE -> next == StatutDemandeInterventionExterne.ACCEPTEE
                    || next == StatutDemandeInterventionExterne.REFUSEE;
            case ACCEPTEE -> next == StatutDemandeInterventionExterne.PLANIFIEE
                    || next == StatutDemandeInterventionExterne.TERMINEE;
            case PLANIFIEE -> next == StatutDemandeInterventionExterne.TERMINEE;
            case REFUSEE, TERMINEE -> false;
        };
        if (!allowed) {
            throw new IllegalArgumentException("Transition de statut externe invalide: " + current + " -> " + next);
        }
    }
}
