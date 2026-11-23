package com.canal.canalmadash.service;

import com.canal.canalmadash.entity.Action;
import com.canal.canalmadash.entity.Machine;
import com.canal.canalmadash.entity.TypeAction;
import com.canal.canalmadash.repository.ActionRepository;
import com.canal.canalmadash.repository.MachineRepository;
import com.canal.canalmadash.repository.TypeActionRepository;
import jakarta.transaction.Transactional;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class MachineService {

    /*
     * Service central pour les machines.
     * Il ne fait pas seulement du CRUD: il garde aussi l'historique des etats
     * via les objets Action. A chaque changement d'etat, l'action ouverte est
     * fermee et une nouvelle action est creee pour construire la timeline.
     */
    private final MachineRepository machineRepository;
    private final TypeActionRepository typeActionRepository;
    private final ActionRepository actionRepository;
    private final ImageStorageService imageStorageService;

    public MachineService(
            MachineRepository machineRepository,
            TypeActionRepository typeActionRepository,
            ActionRepository actionRepository,
            ImageStorageService imageStorageService
    ) {
        this.machineRepository = machineRepository;
        this.typeActionRepository = typeActionRepository;
        this.actionRepository = actionRepository;
        this.imageStorageService = imageStorageService;
    }

    public Machine ajouterMachine(Machine machine) {
        // Une machine doit toujours commencer avec un etat/action valide.
        if (machine.getTypeAction() == null || machine.getTypeAction().getId() <= 0) {
            throw new IllegalArgumentException("TypeAction invalide pour la machine");
        }

        TypeAction type = findTypeAction(machine.getTypeAction().getId());
        LocalDateTime now = LocalDateTime.now();

        machine.setTypeAction(type);
        machine.setDateDebutAction(now);

        Machine saved = machineRepository.save(machine);

        // On ouvre la premiere action pour pouvoir mesurer la duree de cet etat.
        createOpenAction(saved, type, now);

        return saved;
    }

    public List<Machine> getToutesLesMachines() {
        return machineRepository.findAll();
    }

    public Machine getMachine(int id) {
        return machineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Machine introuvable id=" + id));
    }

    public Machine modifierMachine(int id, Machine machineDetails) {
        Machine existing = getMachine(id);
        existing.setNom(machineDetails.getNom());
        existing.setRef(machineDetails.getRef());
        existing.setImagePath(machineDetails.getImagePath());
        copyMachineDetails(existing, machineDetails);

        Machine saved = machineRepository.save(existing);
        if (machineDetails.getTypeAction() != null && machineDetails.getTypeAction().getId() > 0) {
            return applyStateIfChanged(saved.getId(), machineDetails.getTypeAction().getId());
        }
        return saved;
    }

    public void supprimerMachine(int id) {
        Machine machine = getMachine(id);
        if (machine.getImagePath() != null) {
            imageStorageService.delete(machine.getImagePath());
        }
        machineRepository.deleteById(id);
    }

    public Machine ajouterMachineAvecImage(
            String nom,
            String ref,
            int typeActionId,
            String emplacement,
            String fabricant,
            String modele,
            String numeroSerie,
            LocalDate dateInstallation,
            String criticite,
            Double heuresMarche,
            Double coutAchat,
            Double coutInstallation,
            Double coutTransport,
            Double coutFormation,
            Double coutMiseEnService,
            Double seuilDisponibiliteAttentionPercent,
            Double seuilDisponibiliteCritiquePercent,
            Long seuilTempsArretAttentionMinutes,
            Long seuilTempsArretCritiqueMinutes,
            Long seuilPannesAttention,
            Long seuilPannesCritique,
            Double seuilTempsReparationAttentionHours,
            Double seuilTempsReparationCritiqueHours,
            Double seuilRemplacementSurveillancePercent,
            Double seuilRemplacementAttentionPercent,
            Double seuilRemplacementCritiquePercent,
            String commentaireTechnique,
            MultipartFile image
    ) {
        /*
         * Creation complete depuis le formulaire Angular:
         * informations techniques, couts, seuils d'alerte et image optionnelle.
         */
        TypeAction type = findTypeAction(typeActionId);
        LocalDateTime now = LocalDateTime.now();

        Machine machine = new Machine();
        machine.setNom(nom);
        machine.setRef(ref);
        machine.setEmplacement(emplacement);
        machine.setFabricant(fabricant);
        machine.setModele(modele);
        machine.setNumeroSerie(numeroSerie);
        machine.setDateInstallation(dateInstallation);
        machine.setCriticite(criticite);
        machine.setHeuresMarche(heuresMarche);
        setMachineCosts(machine, coutAchat, coutInstallation, coutTransport, coutFormation, coutMiseEnService);
        setMachineThresholds(
                machine,
                seuilDisponibiliteAttentionPercent,
                seuilDisponibiliteCritiquePercent,
                seuilTempsArretAttentionMinutes,
                seuilTempsArretCritiqueMinutes,
                seuilPannesAttention,
                seuilPannesCritique,
                seuilTempsReparationAttentionHours,
                seuilTempsReparationCritiqueHours,
                seuilRemplacementSurveillancePercent,
                seuilRemplacementAttentionPercent,
                seuilRemplacementCritiquePercent
        );
        machine.setCommentaireTechnique(commentaireTechnique);
        machine.setTypeAction(type);
        machine.setDateDebutAction(now);

        machine = machineRepository.save(machine);
        if (image != null && !image.isEmpty()) {
            String path = imageStorageService.storeMachineImage(machine.getId(), image);
            machine.setImagePath(path);
            machine = machineRepository.save(machine);
        }

        createOpenAction(machine, type, now);
        return machine;
    }

    public Machine modifierMachineAvecImage(
            int id,
            String nom,
            String ref,
            int typeActionId,
            String emplacement,
            String fabricant,
            String modele,
            String numeroSerie,
            LocalDate dateInstallation,
            String criticite,
            Double heuresMarche,
            Double coutAchat,
            Double coutInstallation,
            Double coutTransport,
            Double coutFormation,
            Double coutMiseEnService,
            Double seuilDisponibiliteAttentionPercent,
            Double seuilDisponibiliteCritiquePercent,
            Long seuilTempsArretAttentionMinutes,
            Long seuilTempsArretCritiqueMinutes,
            Long seuilPannesAttention,
            Long seuilPannesCritique,
            Double seuilTempsReparationAttentionHours,
            Double seuilTempsReparationCritiqueHours,
            Double seuilRemplacementSurveillancePercent,
            Double seuilRemplacementAttentionPercent,
            Double seuilRemplacementCritiquePercent,
            String commentaireTechnique,
            MultipartFile image
    ) {
        Machine machine = getMachine(id);
        machine.setNom(nom);
        machine.setRef(ref);
        machine.setEmplacement(emplacement);
        machine.setFabricant(fabricant);
        machine.setModele(modele);
        machine.setNumeroSerie(numeroSerie);
        machine.setDateInstallation(dateInstallation);
        machine.setCriticite(criticite);
        machine.setHeuresMarche(heuresMarche);
        setMachineCosts(machine, coutAchat, coutInstallation, coutTransport, coutFormation, coutMiseEnService);
        setMachineThresholds(
                machine,
                seuilDisponibiliteAttentionPercent,
                seuilDisponibiliteCritiquePercent,
                seuilTempsArretAttentionMinutes,
                seuilTempsArretCritiqueMinutes,
                seuilPannesAttention,
                seuilPannesCritique,
                seuilTempsReparationAttentionHours,
                seuilTempsReparationCritiqueHours,
                seuilRemplacementSurveillancePercent,
                seuilRemplacementAttentionPercent,
                seuilRemplacementCritiquePercent
        );
        machine.setCommentaireTechnique(commentaireTechnique);

        if (image != null && !image.isEmpty()) {
            if (machine.getImagePath() != null) {
                imageStorageService.delete(machine.getImagePath());
            }
            String path = imageStorageService.storeMachineImage(id, image);
            machine.setImagePath(path);
        }

        machineRepository.save(machine);
        return applyStateIfChanged(id, typeActionId);
    }

    public Action changerEtatMachine(int machineId, int typeActionId) {
        /*
         * Le changement d'etat est volontairement transactionnel:
         * 1. fermer l'action actuelle,
         * 2. ouvrir la nouvelle action,
         * 3. mettre a jour l'etat courant de la machine.
         */
        Machine machine = getMachine(machineId);
        TypeAction nouveauType = findTypeAction(typeActionId);
        LocalDateTime now = LocalDateTime.now();

        Action lastOpen = actionRepository.findTopByMachineIdAndDateFinIsNullOrderByDateDebutDesc(machineId);

        if (isSameState(machine, typeActionId)) {
            if (lastOpen != null && isSameActionType(lastOpen, typeActionId)) {
                return lastOpen;
            }
            LocalDateTime start = machine.getDateDebutAction() == null ? now : machine.getDateDebutAction();
            return createOpenAction(machine, nouveauType, start);
        }

        if (lastOpen != null) {
            lastOpen.setDateFin(now);
            actionRepository.save(lastOpen);
        }

        Action action = createOpenAction(machine, nouveauType, now);
        machine.setTypeAction(nouveauType);
        machine.setDateDebutAction(now);
        machineRepository.save(machine);

        return action;
    }

    public Machine remplacerImage(int id, MultipartFile file) {
        Machine machine = getMachine(id);

        if (machine.getImagePath() != null) {
            imageStorageService.delete(machine.getImagePath());
        }

        String path = imageStorageService.storeMachineImage(id, file);
        machine.setImagePath(path);

        return machineRepository.save(machine);
    }

    public Resource getImageResource(int id) {
        Machine machine = getMachine(id);
        if (machine.getImagePath() == null) {
            return null;
        }
        return imageStorageService.loadAsResource(machine.getImagePath());
    }

    public void supprimerImage(int id) {
        Machine machine = getMachine(id);
        if (machine.getImagePath() != null) {
            imageStorageService.delete(machine.getImagePath());
            machine.setImagePath(null);
            machineRepository.save(machine);
        }
    }

    public List<Action> getActionsForMachine(int machineId, LocalDateTime from, LocalDateTime to) {
        if (from == null && to == null) {
            return actionRepository.findByMachineIdOrderByDateDebutAsc(machineId);
        }

        LocalDateTime start = from == null ? LocalDateTime.MIN : from;
        LocalDateTime end = to == null ? LocalDateTime.now() : to;

        return actionRepository.findByMachineIdAndDateDebutBetweenOrderByDateDebutAsc(machineId, start, end);
    }

    private Machine applyStateIfChanged(int machineId, int typeActionId) {
        // Evite de creer une nouvelle ligne d'historique si l'etat n'a pas change.
        Machine machine = getMachine(machineId);
        if (!isSameState(machine, typeActionId)) {
            changerEtatMachine(machineId, typeActionId);
            return getMachine(machineId);
        }
        return machine;
    }

    private Action createOpenAction(Machine machine, TypeAction typeAction, LocalDateTime dateDebut) {
        // Une action sans dateFin represente l'etat actuellement en cours.
        Action action = new Action();
        action.setMachine(machine);
        action.setTypeAction(typeAction);
        action.setDateDebut(dateDebut);
        return actionRepository.save(action);
    }

    private TypeAction findTypeAction(int typeActionId) {
        return typeActionRepository.findById(typeActionId)
                .orElseThrow(() -> new IllegalArgumentException("TypeAction introuvable id=" + typeActionId));
    }

    private void copyMachineDetails(Machine target, Machine source) {
        target.setEmplacement(source.getEmplacement());
        target.setFabricant(source.getFabricant());
        target.setModele(source.getModele());
        target.setNumeroSerie(source.getNumeroSerie());
        target.setDateInstallation(source.getDateInstallation());
        target.setCriticite(source.getCriticite());
        target.setHeuresMarche(source.getHeuresMarche());
        setMachineCosts(
                target,
                source.getCoutAchat(),
                source.getCoutInstallation(),
                source.getCoutTransport(),
                source.getCoutFormation(),
                source.getCoutMiseEnService()
        );
        setMachineThresholds(
                target,
                source.getSeuilDisponibiliteAttentionPercent(),
                source.getSeuilDisponibiliteCritiquePercent(),
                source.getSeuilTempsArretAttentionMinutes(),
                source.getSeuilTempsArretCritiqueMinutes(),
                source.getSeuilPannesAttention(),
                source.getSeuilPannesCritique(),
                source.getSeuilTempsReparationAttentionHours(),
                source.getSeuilTempsReparationCritiqueHours(),
                source.getSeuilRemplacementSurveillancePercent(),
                source.getSeuilRemplacementAttentionPercent(),
                source.getSeuilRemplacementCritiquePercent()
        );
        target.setCommentaireTechnique(source.getCommentaireTechnique());
    }

    private void setMachineCosts(
            Machine machine,
            Double coutAchat,
            Double coutInstallation,
            Double coutTransport,
            Double coutFormation,
            Double coutMiseEnService
    ) {
        machine.setCoutAchat(normalizeCost(coutAchat));
        machine.setCoutInstallation(normalizeCost(coutInstallation));
        machine.setCoutTransport(normalizeCost(coutTransport));
        machine.setCoutFormation(normalizeCost(coutFormation));
        machine.setCoutMiseEnService(normalizeCost(coutMiseEnService));
    }

    private Double normalizeCost(Double value) {
        if (value == null) {
            return null;
        }
        return Math.max(0, value);
    }

    private void setMachineThresholds(
            Machine machine,
            Double seuilDisponibiliteAttentionPercent,
            Double seuilDisponibiliteCritiquePercent,
            Long seuilTempsArretAttentionMinutes,
            Long seuilTempsArretCritiqueMinutes,
            Long seuilPannesAttention,
            Long seuilPannesCritique,
            Double seuilTempsReparationAttentionHours,
            Double seuilTempsReparationCritiqueHours,
            Double seuilRemplacementSurveillancePercent,
            Double seuilRemplacementAttentionPercent,
            Double seuilRemplacementCritiquePercent
    ) {
        /*
         * Les seuils permettent au dashboard de classer les machines:
         * surveillance, attention ou critique selon disponibilite, arrets,
         * pannes, reparations et cout de remplacement.
         */
        machine.setSeuilDisponibiliteAttentionPercent(normalizePercent(seuilDisponibiliteAttentionPercent));
        machine.setSeuilDisponibiliteCritiquePercent(normalizePercent(seuilDisponibiliteCritiquePercent));
        machine.setSeuilTempsArretAttentionMinutes(normalizeLongThreshold(seuilTempsArretAttentionMinutes));
        machine.setSeuilTempsArretCritiqueMinutes(normalizeLongThreshold(seuilTempsArretCritiqueMinutes));
        machine.setSeuilPannesAttention(normalizeLongThreshold(seuilPannesAttention));
        machine.setSeuilPannesCritique(normalizeLongThreshold(seuilPannesCritique));
        machine.setSeuilTempsReparationAttentionHours(normalizeCost(seuilTempsReparationAttentionHours));
        machine.setSeuilTempsReparationCritiqueHours(normalizeCost(seuilTempsReparationCritiqueHours));
        machine.setSeuilRemplacementSurveillancePercent(normalizeCost(seuilRemplacementSurveillancePercent));
        machine.setSeuilRemplacementAttentionPercent(normalizeCost(seuilRemplacementAttentionPercent));
        machine.setSeuilRemplacementCritiquePercent(normalizeCost(seuilRemplacementCritiquePercent));
    }

    private Double normalizePercent(Double value) {
        if (value == null) {
            return null;
        }
        return Math.min(100, Math.max(0, value));
    }

    private Long normalizeLongThreshold(Long value) {
        if (value == null) {
            return null;
        }
        return Math.max(0, value);
    }

    private boolean isSameState(Machine machine, int typeActionId) {
        return machine.getTypeAction() != null && machine.getTypeAction().getId() == typeActionId;
    }

    private boolean isSameActionType(Action action, int typeActionId) {
        return action.getTypeAction() != null && action.getTypeAction().getId() == typeActionId;
    }
}
