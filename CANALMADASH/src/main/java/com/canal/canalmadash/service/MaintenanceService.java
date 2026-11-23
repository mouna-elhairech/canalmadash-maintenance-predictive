package com.canal.canalmadash.service;

import com.canal.canalmadash.Enum.StatutDemandeMaintenance;
import com.canal.canalmadash.Enum.TypeIntervention;
import com.canal.canalmadash.dto.MaintenanceRequest;
import com.canal.canalmadash.dto.PieceUtiliseeRequest;
import com.canal.canalmadash.dto.RetourPieceRequest;
import com.canal.canalmadash.entity.DemandeMaintenance;
import com.canal.canalmadash.entity.Machine;
import com.canal.canalmadash.entity.Maintenance;
import com.canal.canalmadash.entity.MaintenancePieceUtilisee;
import com.canal.canalmadash.entity.PieceRechange;
import com.canal.canalmadash.entity.TypeMaintenance;
import com.canal.canalmadash.repository.DemandeMaintenanceRepository;
import com.canal.canalmadash.repository.MachineRepository;
import com.canal.canalmadash.repository.MaintenanceRepository;
import com.canal.canalmadash.repository.MaintenancePieceUtiliseeRepository;
import com.canal.canalmadash.repository.PieceRechangeRepository;
import com.canal.canalmadash.repository.TypeMaintenanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class MaintenanceService {
    /*
     * Service des interventions de maintenance.
     * Il transforme une demande en intervention reelle, consomme les pieces,
     * calcule le cout total, stocke les images avant/apres et remet la demande
     * dans l'etat "en attente de validation".
     */
    private final MaintenanceRepository maintenanceRepository;
    private final MaintenancePieceUtiliseeRepository maintenancePieceUtiliseeRepository;
    private final MachineRepository machineRepository;
    private final TypeMaintenanceRepository typeMaintenanceRepository;
    private final DemandeMaintenanceRepository demandeMaintenanceRepository;
    private final PieceRechangeRepository pieceRechangeRepository;
    private final PieceRechangeService pieceRechangeService;
    private final ImageStorageService imageStorageService;

    public MaintenanceService(
            MaintenanceRepository maintenanceRepository,
            MaintenancePieceUtiliseeRepository maintenancePieceUtiliseeRepository,
            MachineRepository machineRepository,
            TypeMaintenanceRepository typeMaintenanceRepository,
            DemandeMaintenanceRepository demandeMaintenanceRepository,
            PieceRechangeRepository pieceRechangeRepository,
            PieceRechangeService pieceRechangeService,
            ImageStorageService imageStorageService
    ) {
        this.maintenanceRepository = maintenanceRepository;
        this.maintenancePieceUtiliseeRepository = maintenancePieceUtiliseeRepository;
        this.machineRepository = machineRepository;
        this.typeMaintenanceRepository = typeMaintenanceRepository;
        this.demandeMaintenanceRepository = demandeMaintenanceRepository;
        this.pieceRechangeRepository = pieceRechangeRepository;
        this.pieceRechangeService = pieceRechangeService;
        this.imageStorageService = imageStorageService;
    }

    @Transactional(readOnly = true)
    public List<Maintenance> findByMachine(int machineId) {
        return maintenanceRepository.findByMachineIdOrderByDateDebutDesc(machineId);
    }

    @Transactional(readOnly = true)
    public Resource getMaintenanceImage(Long id, String kind) {
        Maintenance maintenance = maintenanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Maintenance introuvable id=" + id));
        String path = switch (kind) {
            case "avant" -> maintenance.getImageAvantPath();
            case "apres" -> maintenance.getImageApresPath();
            default -> maintenance.getImagePath();
        };
        return imageStorageService.loadAsResource(path);
    }

    @Transactional(readOnly = true)
    public Resource getDemandeImage(int id) {
        DemandeMaintenance demande = demandeMaintenanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande maintenance introuvable id=" + id));
        return imageStorageService.loadAsResource(demande.getImagePath());
    }

    @Transactional
    public MaintenancePieceUtilisee retournerPiece(Long pieceUtiliseeId, RetourPieceRequest request) {
        if (request.getCause() == null || request.getCause().isBlank()) {
            throw new IllegalArgumentException("La cause du retour est obligatoire");
        }
        if (request.getQuantite() <= 0) {
            throw new IllegalArgumentException("La quantite retournee doit etre superieure a zero");
        }

        MaintenancePieceUtilisee pieceUtilisee = maintenancePieceUtiliseeRepository.findById(pieceUtiliseeId)
                .orElseThrow(() -> new IllegalArgumentException("Piece utilisee introuvable id=" + pieceUtiliseeId));
        int disponibleRetour = pieceUtilisee.getQuantiteDisponibleRetour();
        if (request.getQuantite() > disponibleRetour) {
            throw new IllegalArgumentException("Quantite retournee superieure a la quantite disponible");
        }

        PieceRechange piece = pieceUtilisee.getPiece();
        pieceRechangeService.retournerDepuisIntervention(
                piece,
                request.getQuantite(),
                pieceUtilisee.getMaintenance(),
                pieceUtilisee,
                request.getCause().trim()
        );

        pieceUtilisee.setQuantiteRetournee(pieceUtilisee.getQuantiteRetournee() + request.getQuantite());
        pieceUtilisee.setCauseRetour(request.getCause().trim());
        pieceUtilisee.setDateRetour(LocalDateTime.now());
        MaintenancePieceUtilisee saved = maintenancePieceUtiliseeRepository.save(pieceUtilisee);
        subtractReturnedPieceCost(pieceUtilisee.getMaintenance(), request.getQuantite(), pieceUtilisee.getCoutUnitaire());
        return saved;
    }

    @Transactional
    public Maintenance createMaintenance(
            MaintenanceRequest request,
            MultipartFile image,
            MultipartFile imageAvant,
            MultipartFile imageApres
    ) {
        /*
         * Flux principal d'une intervention:
         * validation -> creation -> consommation pieces -> sauvegarde images
         * -> mise a jour de la demande liee.
         */
        validateMaintenanceRequest(request);

        Machine machine = machineRepository.findById(request.getMachineId())
                .orElseThrow(() -> new IllegalArgumentException("Machine introuvable id=" + request.getMachineId()));

        TypeMaintenance type = resolveTypeMaintenance(request);
        DemandeMaintenance demande = resolveDemandeMaintenance(request, machine);
        LocalDateTime now = LocalDateTime.now();

        Maintenance maintenance = new Maintenance();
        maintenance.setMachine(machine);
        maintenance.setType(type);
        maintenance.setDescription(request.getDescription());
        maintenance.setNote(request.getNote());
        maintenance.setPiecesUtilisees(request.getPiecesUtilisees());
        maintenance.setCout(normalizeCost(request.getCout()));
        maintenance.setCausePanne(request.getCausePanne());
        maintenance.setChecklist(request.getChecklist());
        maintenance.setControleEffectue(Boolean.TRUE.equals(request.getControleEffectue()));
        maintenance.setNettoyageEffectue(Boolean.TRUE.equals(request.getNettoyageEffectue()));
        maintenance.setTestFinalEffectue(Boolean.TRUE.equals(request.getTestFinalEffectue()));
        maintenance.setValidationSignature(request.getValidationSignature());
        maintenance.setTypeIntervention(request.getTypeIntervention() == null ? TypeIntervention.INTERNE : request.getTypeIntervention());
        if (maintenance.getTypeIntervention() == TypeIntervention.EXTERNE) {
            maintenance.setFournisseurNom(request.getFournisseurNom());
            maintenance.setFournisseurContact(request.getFournisseurContact());
            maintenance.setFournisseurTelephone(request.getFournisseurTelephone());
            maintenance.setFournisseurEmail(request.getFournisseurEmail());
            maintenance.setFournisseurAdresse(request.getFournisseurAdresse());
            maintenance.setReferenceDevis(request.getReferenceDevis());
            maintenance.setMessageDemandeExterne(request.getMessageDemandeExterne());
        }
        maintenance.setUser(
                request.getUser() == null || request.getUser().isBlank()
                        ? "MAINTENANCE"
                        : request.getUser().trim()
        );
        maintenance.setDateDebut(now);
        maintenance.setDateFin(now);
        maintenance.setDemandeMaintenance(demande);
        maintenance = maintenanceRepository.save(maintenance);

        double piecesCost = attachAndConsumePieces(maintenance, request, machine);
        maintenance.setCout(roundMoney(normalizeCost(maintenance.getCout()) + piecesCost));
        maintenance = maintenanceRepository.save(maintenance);

        maintenance = storeMaintenanceImages(maintenance, image, imageAvant, imageApres);

        if (demande != null) {
            demande.setMaintenance(maintenance);
            demande.setStatut(StatutDemandeMaintenance.EN_ATTENTE_VALIDATION);
            if (demande.getDateDebutReelle() == null) {
                demande.setDateDebutReelle(now);
            }
            demande.setDateFinReelle(now);
            demandeMaintenanceRepository.save(demande);
        }

        return maintenanceRepository.findWithPiecesById(maintenance.getId()).orElse(maintenance);
    }

    private double attachAndConsumePieces(Maintenance maintenance, MaintenanceRequest request, Machine machine) {
        // Chaque piece utilisee diminue le stock et ajoute son cout a l'intervention.
        if (request.getPieces() == null || request.getPieces().isEmpty()) {
            return 0;
        }

        double piecesCost = 0;
        for (PieceUtiliseeRequest item : request.getPieces()) {
            if (item.getPieceId() == null || item.getQuantite() <= 0) {
                continue;
            }
            PieceRechange piece = pieceRechangeRepository.findById(item.getPieceId())
                    .orElseThrow(() -> new IllegalArgumentException("Piece introuvable id=" + item.getPieceId()));
            if (!isPieceCompatible(piece, machine)) {
                throw new IllegalArgumentException("La piece " + piece.getReference() + " n'est pas compatible avec cette machine");
            }
            if (piece.getQuantite() < item.getQuantite()) {
                throw new IllegalArgumentException("Stock insuffisant pour la piece " + piece.getReference());
            }

            MaintenancePieceUtilisee used = new MaintenancePieceUtilisee();
            used.setMaintenance(maintenance);
            used.setPiece(piece);
            used.setQuantite(item.getQuantite());
            used.setCoutUnitaire(piece.getCoutUnitaire());
            maintenance.getPieces().add(used);
            maintenancePieceUtiliseeRepository.save(used);
            piecesCost += pieceRechangeService.consommerPourIntervention(piece, item.getQuantite(), maintenance, used);
        }
        return roundMoney(piecesCost);
    }

    private boolean isPieceCompatible(PieceRechange piece, Machine machine) {
        // Une piece standard va sur toutes les machines; sinon elle doit etre explicitement compatible.
        return piece.isStandard()
                || piece.getMachinesCompatibles().stream().anyMatch(item -> item.getId() == machine.getId());
    }

    private void validateMaintenanceRequest(MaintenanceRequest request) {
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new IllegalArgumentException("La description de l'intervention est obligatoire");
        }
        if (request.getTypeIntervention() == TypeIntervention.EXTERNE
                && (request.getFournisseurNom() == null || request.getFournisseurNom().isBlank())) {
            throw new IllegalArgumentException("Le fournisseur est obligatoire pour une intervention externe");
        }
    }

    private Maintenance storeMaintenanceImages(
            Maintenance maintenance,
            MultipartFile image,
            MultipartFile imageAvant,
            MultipartFile imageApres
    ) {
        /*
         * Le front peut envoyer trois images:
         * imageAvant, imageApres et image principale. L'image apres devient
         * aussi l'image principale si elle existe.
         */
        if (imageAvant != null && !imageAvant.isEmpty()) {
            String path = imageStorageService.storeMaintenanceAvantImage(maintenance.getId(), imageAvant);
            maintenance.setImageAvantPath(path);
        }
        if (imageApres != null && !imageApres.isEmpty()) {
            String path = imageStorageService.storeMaintenanceApresImage(maintenance.getId(), imageApres);
            maintenance.setImageApresPath(path);
            maintenance.setImagePath(path);
        }
        if (image != null && !image.isEmpty()) {
            String path = imageStorageService.storeMaintenanceImage(maintenance.getId(), image);
            maintenance.setImagePath(path);
            if (maintenance.getImageApresPath() == null) {
                maintenance.setImageApresPath(path);
            }
        }
        return maintenanceRepository.save(maintenance);
    }

    private DemandeMaintenance resolveDemandeMaintenance(MaintenanceRequest request, Machine machine) {
        // Si l'intervention vient d'une demande, on verifie qu'elle n'est pas deja traitee.
        if (request.getDemandeMaintenanceId() == null) {
            return null;
        }

        DemandeMaintenance demande = demandeMaintenanceRepository.findById(request.getDemandeMaintenanceId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "DemandeMaintenance introuvable id=" + request.getDemandeMaintenanceId()
                ));

        if (demande.getMachine() != null && demande.getMachine().getId() != machine.getId()) {
            throw new IllegalArgumentException("La demande ne correspond pas a la machine indiquee");
        }
        if (demande.getMaintenance() != null) {
            throw new IllegalArgumentException("Cette demande possede deja une intervention");
        }
        if (demande.getStatut() == StatutDemandeMaintenance.TERMINEE
                || demande.getStatut() == StatutDemandeMaintenance.ANNULEE) {
            throw new IllegalArgumentException("Impossible d'intervenir sur une demande cloturee");
        }

        return demande;
    }

    private TypeMaintenance resolveTypeMaintenance(MaintenanceRequest request) {
        // Meme logique que les demandes: type existant ou nouveau type propose.
        if (request.getTypeMaintenanceId() != null) {
            return typeMaintenanceRepository.findById(request.getTypeMaintenanceId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "TypeMaintenance introuvable id=" + request.getTypeMaintenanceId()
                    ));
        }

        if (request.getNouveauTypeLibelle() != null && !request.getNouveauTypeLibelle().isBlank()) {
            String libelle = request.getNouveauTypeLibelle().trim();
            TypeMaintenance type = new TypeMaintenance();
            type.setCode(slugify(libelle));
            type.setLibelle(libelle);
            type.setActif(false);
            type.setColor("#64748b");
            return typeMaintenanceRepository.save(type);
        }

        throw new IllegalArgumentException("Il faut soit typeMaintenanceId, soit nouveauTypeLibelle");
    }

    private String slugify(String label) {
        String code = label.toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        if (code.length() > 40) {
            code = code.substring(0, 40);
        }
        return code;
    }

    private void subtractReturnedPieceCost(Maintenance maintenance, int quantite, Double coutUnitaire) {
        // Quand une piece est retournee au stock, son cout sort du cout final de l'intervention.
        if (maintenance == null || maintenance.getId() == null) {
            return;
        }
        double returnedCost = quantite * normalizeCost(coutUnitaire);
        maintenance.setCout(roundMoney(normalizeCost(maintenance.getCout()) - returnedCost));
        maintenanceRepository.save(maintenance);
    }

    private double normalizeCost(Double value) {
        return value == null ? 0 : Math.max(0, value);
    }

    private double roundMoney(double value) {
        return Math.round(Math.max(0, value) * 100.0) / 100.0;
    }
}
