package com.canal.canalmadash.service;

import com.canal.canalmadash.Enum.StatutDemandePiece;
import com.canal.canalmadash.Enum.TypeMouvementStockPiece;
import com.canal.canalmadash.dto.DemandePieceRequest;
import com.canal.canalmadash.dto.EntreeStockPieceRequest;
import com.canal.canalmadash.entity.DemandePieceRechange;
import com.canal.canalmadash.entity.Machine;
import com.canal.canalmadash.entity.Maintenance;
import com.canal.canalmadash.entity.MaintenancePieceUtilisee;
import com.canal.canalmadash.entity.MouvementStockPiece;
import com.canal.canalmadash.entity.PieceRechange;
import com.canal.canalmadash.repository.DemandePieceRechangeRepository;
import com.canal.canalmadash.repository.MachineRepository;
import com.canal.canalmadash.repository.MouvementStockPieceRepository;
import com.canal.canalmadash.repository.PieceRechangeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class PieceRechangeService {

    /*
     * Service de stock des pieces de rechange.
     * Il centralise les entrees, sorties, retours, demandes d'achat et le calcul
     * du cout moyen unitaire. C'est important pour garder des couts fiables.
     */
    private final PieceRechangeRepository repository;
    private final DemandePieceRechangeRepository demandeRepository;
    private final MachineRepository machineRepository;
    private final MouvementStockPieceRepository mouvementRepository;

    public PieceRechangeService(
            PieceRechangeRepository repository,
            DemandePieceRechangeRepository demandeRepository,
            MachineRepository machineRepository,
            MouvementStockPieceRepository mouvementRepository
    ) {
        this.repository = repository;
        this.demandeRepository = demandeRepository;
        this.machineRepository = machineRepository;
        this.mouvementRepository = mouvementRepository;
    }

    @Transactional(readOnly = true)
    public List<PieceRechange> findAll() {
        return repository.findAll().stream()
                .peek(this::hydrateMachineIds)
                .sorted(Comparator.comparing(PieceRechange::getReference, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PieceRechange> findCompatiblesForMachine(int machineId) {
        return repository.findCompatiblesForMachine(machineId).stream()
                .peek(this::hydrateMachineIds)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PieceRechange> findAlertes() {
        return findAll().stream()
                .filter(PieceRechange::isAlerteStock)
                .toList();
    }

    @Transactional
    public PieceRechange save(PieceRechange piece) {
        /*
         * A la creation, le stock initial passe par un mouvement d'entree.
         * Comme ca, l'historique de stock reste complet des le premier jour.
         */
        int initialQuantity = Math.max(0, piece.getQuantite());
        double initialValue = safeMoney(piece.getValeurStock());
        normalizePiece(piece);
        piece.setQuantite(0);
        piece.setValeurStock(0.0);
        piece.setCoutUnitaire(0.0);
        applyCompatibleMachines(piece);
        PieceRechange saved = repository.save(piece);
        if (initialQuantity > 0) {
            saved = enregistrerEntreeStock(saved.getId(), initialQuantity, initialValue, "Stock initial");
        }
        createAutomaticDemandIfNeeded(saved);
        hydrateMachineIds(saved);
        return saved;
    }

    @Transactional
    public PieceRechange update(Long id, PieceRechange payload) {
        PieceRechange piece = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Piece introuvable id=" + id));
        piece.setReference(payload.getReference());
        piece.setLibelle(payload.getLibelle());
        piece.setSeuilMinimum(payload.getSeuilMinimum());
        piece.setStandard(payload.isStandard());
        piece.setMachineIds(payload.getMachineIds());
        normalizePiece(piece);
        applyCompatibleMachines(piece);
        PieceRechange saved = repository.save(piece);
        createAutomaticDemandIfNeeded(saved);
        hydrateMachineIds(saved);
        return saved;
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<DemandePieceRechange> findDemandes() {
        return demandeRepository.findAllByOrderByDateDemandeDesc();
    }

    @Transactional(readOnly = true)
    public List<MouvementStockPiece> findMouvements(Long pieceId) {
        return mouvementRepository.findByPieceIdOrderByDateMouvementDesc(pieceId);
    }

    @Transactional
    public PieceRechange ajouterStock(Long pieceId, EntreeStockPieceRequest request) {
        // Controle simple avant de creer une entree de stock valorisee.
        if (request.getQuantite() <= 0) {
            throw new IllegalArgumentException("La quantite d'entree doit etre superieure a zero");
        }
        if (request.getPrixTotal() == null || request.getPrixTotal() < 0) {
            throw new IllegalArgumentException("Le prix total de l'entree est obligatoire");
        }
        return enregistrerEntreeStock(pieceId, request.getQuantite(), request.getPrixTotal(), request.getCommentaire());
    }

    @Transactional
    public DemandePieceRechange createDemande(DemandePieceRequest request) {
        PieceRechange piece = repository.findById(request.getPieceId())
                .orElseThrow(() -> new IllegalArgumentException("Piece introuvable id=" + request.getPieceId()));

        DemandePieceRechange demande = new DemandePieceRechange();
        demande.setPiece(piece);
        demande.setQuantiteDemandee(Math.max(1, request.getQuantiteDemandee()));
        demande.setMotif(request.getMotif());
        demande.setDateDemande(LocalDateTime.now());
        demande.setStatut(StatutDemandePiece.EN_ATTENTE);
        return demandeRepository.save(demande);
    }

    @Transactional
    public DemandePieceRechange updateDemandeStatut(Long id, StatutDemandePiece statut, Double prixTotalReception) {
        DemandePieceRechange demande = demandeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande piece introuvable id=" + id));
        validateDemandePieceTransition(demande, statut);
        demande.setStatut(statut);

        if (statut == StatutDemandePiece.RECUE && demande.getDateReception() == null) {
            demande.setDateReception(LocalDateTime.now());
            PieceRechange piece = demande.getPiece();
            double prixTotal = prixTotalReception == null
                    ? demande.getQuantiteDemandee() * safeMoney(piece.getCoutUnitaire())
                    : Math.max(0, prixTotalReception);
            demande.setPrixTotalReception(prixTotal);
            enregistrerEntreeStock(piece.getId(), demande.getQuantiteDemandee(), prixTotal, "Reception demande piece #" + demande.getId());
        }

        return demandeRepository.save(demande);
    }

    @Transactional
    public double consommerPourIntervention(PieceRechange piece, int quantite, Maintenance maintenance, MaintenancePieceUtilisee used) {
        /*
         * Sortie de stock pendant une intervention.
         * Le cout est pris au cout unitaire moyen au moment de la sortie.
         */
        if (quantite <= 0) {
            return 0;
        }
        if (piece.getQuantite() < quantite) {
            throw new IllegalArgumentException("Stock insuffisant pour la piece " + piece.getReference());
        }
        double coutUnitaireSortie = safeMoney(piece.getCoutUnitaire());
        double coutTotal = roundMoney(coutUnitaireSortie * quantite);

        piece.setQuantite(piece.getQuantite() - quantite);
        piece.setValeurStock(Math.max(0, roundMoney(safeMoney(piece.getValeurStock()) - coutTotal)));
        refreshAverageCost(piece);
        repository.save(piece);

        enregistrerMouvement(
                piece,
                TypeMouvementStockPiece.SORTIE_INTERVENTION,
                quantite,
                coutUnitaireSortie,
                coutTotal,
                maintenance,
                used,
                "Sortie vers intervention"
        );
        createAutomaticDemandIfNeeded(piece);
        return coutTotal;
    }

    @Transactional
    public void retournerDepuisIntervention(PieceRechange piece, int quantite, Maintenance maintenance, MaintenancePieceUtilisee used, String cause) {
        // Retour d'une piece non utilisee ou remontee: le stock et sa valeur remontent.
        double coutUnitaireRetour = safeMoney(used.getCoutUnitaire());
        double coutTotal = roundMoney(coutUnitaireRetour * quantite);

        piece.setQuantite(piece.getQuantite() + quantite);
        piece.setValeurStock(roundMoney(safeMoney(piece.getValeurStock()) + coutTotal));
        refreshAverageCost(piece);
        repository.save(piece);

        enregistrerMouvement(
                piece,
                TypeMouvementStockPiece.RETOUR_INTERVENTION,
                quantite,
                coutUnitaireRetour,
                coutTotal,
                maintenance,
                used,
                cause
        );
    }

    private void normalizePiece(PieceRechange piece) {
        if (piece.getReference() == null || piece.getReference().isBlank()) {
            throw new IllegalArgumentException("La reference de la piece est obligatoire");
        }
        if (piece.getLibelle() == null || piece.getLibelle().isBlank()) {
            throw new IllegalArgumentException("Le libelle de la piece est obligatoire");
        }
        piece.setReference(piece.getReference().trim());
        piece.setLibelle(piece.getLibelle().trim());
        piece.setQuantite(Math.max(0, piece.getQuantite()));
        piece.setSeuilMinimum(Math.max(0, piece.getSeuilMinimum()));
        piece.setValeurStock(Math.max(0, safeMoney(piece.getValeurStock())));
        refreshAverageCost(piece);
    }

    private PieceRechange enregistrerEntreeStock(Long pieceId, int quantite, double prixTotal, String commentaire) {
        // Une entree augmente la quantite et la valeur globale du stock.
        PieceRechange piece = repository.findById(pieceId)
                .orElseThrow(() -> new IllegalArgumentException("Piece introuvable id=" + pieceId));
        double normalizedPrixTotal = roundMoney(Math.max(0, prixTotal));
        piece.setQuantite(piece.getQuantite() + quantite);
        piece.setValeurStock(roundMoney(safeMoney(piece.getValeurStock()) + normalizedPrixTotal));
        refreshAverageCost(piece);
        piece = repository.save(piece);

        enregistrerMouvement(
                piece,
                TypeMouvementStockPiece.ENTREE,
                quantite,
                quantite == 0 ? 0 : roundMoney(normalizedPrixTotal / quantite),
                normalizedPrixTotal,
                null,
                null,
                commentaire == null || commentaire.isBlank() ? "Entree stock" : commentaire.trim()
        );
        return piece;
    }

    private void enregistrerMouvement(
            PieceRechange piece,
            TypeMouvementStockPiece type,
            int quantite,
            double coutUnitaire,
            double coutTotal,
            Maintenance maintenance,
            MaintenancePieceUtilisee used,
            String commentaire
    ) {
        MouvementStockPiece mouvement = new MouvementStockPiece();
        mouvement.setPiece(piece);
        mouvement.setMaintenance(maintenance);
        mouvement.setPieceUtilisee(used);
        mouvement.setType(type);
        mouvement.setQuantite(quantite);
        mouvement.setCoutUnitaire(roundMoney(coutUnitaire));
        mouvement.setCoutTotal(roundMoney(coutTotal));
        mouvement.setDateMouvement(LocalDateTime.now());
        mouvement.setCommentaire(commentaire);
        mouvementRepository.save(mouvement);
    }

    private void refreshAverageCost(PieceRechange piece) {
        // Le cout unitaire est recalcule a partir de la valeur totale du stock.
        if (piece.getQuantite() <= 0) {
            piece.setValeurStock(0.0);
            piece.setCoutUnitaire(0.0);
            return;
        }
        piece.setCoutUnitaire(roundMoney(safeMoney(piece.getValeurStock()) / piece.getQuantite()));
    }

    private double safeMoney(Double value) {
        return value == null ? 0 : Math.max(0, value);
    }

    private double roundMoney(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private void validateDemandePieceTransition(DemandePieceRechange demande, StatutDemandePiece next) {
        if (demande.getStatut() == StatutDemandePiece.RECUE && next != StatutDemandePiece.RECUE) {
            throw new IllegalArgumentException("Une demande recue ne peut pas changer de statut");
        }
        if (demande.getStatut() == StatutDemandePiece.ANNULEE && next != StatutDemandePiece.ANNULEE) {
            throw new IllegalArgumentException("Une demande annulee ne peut pas changer de statut");
        }
        if (next == StatutDemandePiece.RECUE
                && demande.getStatut() != StatutDemandePiece.EN_ATTENTE
                && demande.getStatut() != StatutDemandePiece.COMMANDEE) {
            throw new IllegalArgumentException("Seule une demande ouverte peut etre recue");
        }
    }

    public void createAutomaticDemandIfNeeded(PieceRechange piece) {
        // Si le stock passe sous le seuil minimum, une demande d'achat est creee une seule fois.
        if (!piece.isAlerteStock() || piece.getId() == null) {
            return;
        }
        boolean hasOpenDemand = demandeRepository.existsByPieceIdAndStatutIn(
                piece.getId(),
                List.of(StatutDemandePiece.EN_ATTENTE, StatutDemandePiece.COMMANDEE)
        );
        if (hasOpenDemand) {
            return;
        }

        DemandePieceRequest request = new DemandePieceRequest();
        request.setPieceId(piece.getId());
        request.setQuantiteDemandee(Math.max(piece.getSeuilMinimum() * 2, 1));
        request.setMotif(piece.getQuantite() <= 0 ? "Stock termine" : "Stock sous seuil minimum");
        createDemande(request);
    }

    private void applyCompatibleMachines(PieceRechange piece) {
        if (piece.isStandard()) {
            piece.getMachinesCompatibles().clear();
            return;
        }
        List<Integer> ids = piece.getMachineIds() == null ? List.of() : piece.getMachineIds();
        List<Machine> machines = ids.stream()
                .distinct()
                .map(id -> machineRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Machine introuvable id=" + id)))
                .toList();
        piece.setMachinesCompatibles(machines);
    }

    private void hydrateMachineIds(PieceRechange piece) {
        piece.setMachineIds(piece.getMachinesCompatibles().stream().map(Machine::getId).toList());
    }
}
