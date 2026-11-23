package com.canal.canalmadash.service;

import com.canal.canalmadash.Enum.CategorieFonctionnalite;
import com.canal.canalmadash.dto.FonctionnaliteApplicationRequest;
import com.canal.canalmadash.dto.RoleApplicationRequest;
import com.canal.canalmadash.entity.FonctionnaliteApplication;
import com.canal.canalmadash.entity.RoleApplication;
import com.canal.canalmadash.repository.FonctionnaliteApplicationRepository;
import com.canal.canalmadash.repository.RoleApplicationRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoleApplicationService {

    /*
     * Service de parametrage des roles et fonctionnalites.
     * Il initialise des profils par defaut au demarrage, puis permet au
     * dashboard de modifier les droits applicatifs.
     */
    private final RoleApplicationRepository roleRepository;
    private final FonctionnaliteApplicationRepository fonctionnaliteRepository;

    public RoleApplicationService(
            RoleApplicationRepository roleRepository,
            FonctionnaliteApplicationRepository fonctionnaliteRepository
    ) {
        this.roleRepository = roleRepository;
        this.fonctionnaliteRepository = fonctionnaliteRepository;
    }

    @PostConstruct
    @Transactional
    public void initializeDefaultsOnStartup() {
        // Au demarrage, on cree ce qui manque sans ecraser les personnalisations existantes.
        seedDefaults(false);
    }

    @Transactional
    public void initializeDefaults() {
        // Action manuelle de reinitialisation: cette fois on peut ecraser les valeurs par defaut.
        seedDefaults(true);
    }

    private void seedDefaults(boolean overwriteExisting) {
        /*
         * Catalogue fonctionnel de l'application.
         * Les codes restent stables pour pouvoir les rattacher aux roles.
         */
        Map<String, FonctionnaliteApplication> fonctionnalites = new LinkedHashMap<>();
        addFeature(fonctionnalites, "PAGE_DASHBOARD", "Dashboard maintenance", "Vue globale des indicateurs maintenance.", "/dashboard", CategorieFonctionnalite.PAGE, overwriteExisting);
        addFeature(fonctionnalites, "PAGE_MACHINES", "Machines", "Liste et supervision des machines.", "/machines", CategorieFonctionnalite.PAGE, overwriteExisting);
        addFeature(fonctionnalites, "PAGE_DETAIL_MACHINE", "Detail machine", "Fiche machine, historique, demandes et interventions.", "/machinesD/:id", CategorieFonctionnalite.PAGE, overwriteExisting);
        addFeature(fonctionnalites, "PAGE_KANBAN", "Kanban maintenance", "Pilotage des demandes par statut.", "/maintenance/kanban", CategorieFonctionnalite.PAGE, overwriteExisting);
        addFeature(fonctionnalites, "PAGE_PIECES", "Pieces de rechange", "Gestion du stock des pieces.", "/maintenance/pieces", CategorieFonctionnalite.PAGE, overwriteExisting);
        addFeature(fonctionnalites, "PAGE_PARAM_TYPES", "Parametres types", "Configuration des types d actions et maintenances.", "/parametres/types", CategorieFonctionnalite.PAGE, overwriteExisting);
        addFeature(fonctionnalites, "PAGE_PARAM_SCORE", "Parametres score criticite", "Configuration des poids, facteurs et seuils.", "/parametres/score", CategorieFonctionnalite.PAGE, overwriteExisting);
        addFeature(fonctionnalites, "PAGE_PARAM_ROLES", "Roles et droits", "Configuration des roles et fonctionnalites.", "/parametres/roles", CategorieFonctionnalite.PAGE, overwriteExisting);
        addFeature(fonctionnalites, "PAGE_AIDE", "Aide application", "Documentation de l application.", "/aide", CategorieFonctionnalite.PAGE, overwriteExisting);

        addFeature(fonctionnalites, "MACHINE_CREER_MODIFIER_SUPPRIMER", "Creer, modifier et supprimer les machines", "Gestion complete du parc machines.", null, CategorieFonctionnalite.ACTION, overwriteExisting);
        addFeature(fonctionnalites, "MACHINE_CHANGER_ETAT", "Changer l etat machine", "Changer l etat courant d une machine.", null, CategorieFonctionnalite.ACTION, overwriteExisting);
        addFeature(fonctionnalites, "DEMANDE_CREER", "Creer une demande maintenance", "Declarer un besoin de maintenance.", null, CategorieFonctionnalite.ACTION, overwriteExisting);
        addFeature(fonctionnalites, "DEMANDE_PILOTER", "Piloter les demandes", "Planifier, demarrer, terminer, annuler ou valider une demande.", null, CategorieFonctionnalite.ACTION, overwriteExisting);
        addFeature(fonctionnalites, "INTERVENTION_CREER", "Creer une intervention", "Saisir une intervention interne ou externe.", null, CategorieFonctionnalite.ACTION, overwriteExisting);
        addFeature(fonctionnalites, "PIECE_GERER_STOCK", "Gerer le stock pieces", "Creer les pieces, saisir les entrees, sorties et retours.", null, CategorieFonctionnalite.ACTION, overwriteExisting);
        addFeature(fonctionnalites, "RAPPORT_CONSULTER", "Consulter rapports et exports", "Ouvrir les PDF, exports et historiques.", null, CategorieFonctionnalite.ACTION, overwriteExisting);
        addFeature(fonctionnalites, "CONTROLE_PERIODIQUE_GERER", "Gerer controles periodiques", "Creer et desactiver les regles de controle periodique.", null, CategorieFonctionnalite.ACTION, overwriteExisting);
        addFeature(fonctionnalites, "SCORE_PARAMETRER", "Parametrer score criticite", "Modifier les points, facteurs et seuils du score.", null, CategorieFonctionnalite.ACTION, overwriteExisting);
        addFeature(fonctionnalites, "ROLE_PARAMETRER", "Parametrer roles et droits", "Creer les roles et affecter les fonctionnalites.", null, CategorieFonctionnalite.ACTION, overwriteExisting);

        createOrUpdateRole("ADMIN", "Administrateur", "Configurer l application et garder la vision complete sur le parc.", fonctionnalites,
                overwriteExisting,
                "PAGE_DASHBOARD", "PAGE_MACHINES", "PAGE_DETAIL_MACHINE", "PAGE_KANBAN", "PAGE_PIECES", "PAGE_PARAM_TYPES", "PAGE_PARAM_SCORE", "PAGE_PARAM_ROLES", "PAGE_AIDE",
                "MACHINE_CREER_MODIFIER_SUPPRIMER", "MACHINE_CHANGER_ETAT", "DEMANDE_CREER", "DEMANDE_PILOTER", "INTERVENTION_CREER", "PIECE_GERER_STOCK",
                "RAPPORT_CONSULTER", "CONTROLE_PERIODIQUE_GERER", "SCORE_PARAMETRER", "ROLE_PARAMETRER");
        createOrUpdateRole("RESPONSABLE_MAINTENANCE", "Responsable maintenance", "Piloter les demandes, planifier les interventions et analyser les indicateurs.", fonctionnalites,
                overwriteExisting,
                "PAGE_DASHBOARD", "PAGE_DETAIL_MACHINE", "PAGE_KANBAN", "PAGE_PIECES", "PAGE_AIDE",
                "MACHINE_CHANGER_ETAT", "DEMANDE_CREER", "DEMANDE_PILOTER", "INTERVENTION_CREER", "PIECE_GERER_STOCK", "RAPPORT_CONSULTER", "CONTROLE_PERIODIQUE_GERER");
        createOrUpdateRole("TECHNICIEN_MAINTENANCE", "Technicien maintenance", "Executer les interventions et renseigner les informations terrain.", fonctionnalites,
                overwriteExisting,
                "PAGE_MACHINES", "PAGE_DETAIL_MACHINE", "PAGE_KANBAN", "PAGE_PIECES",
                "MACHINE_CHANGER_ETAT", "DEMANDE_CREER", "INTERVENTION_CREER", "PIECE_GERER_STOCK");
        createOrUpdateRole("OPERATEUR", "Operateur", "Declarer les problemes et suivre l etat des machines.", fonctionnalites,
                overwriteExisting,
                "PAGE_MACHINES", "PAGE_DETAIL_MACHINE",
                "MACHINE_CHANGER_ETAT", "DEMANDE_CREER");
        createOrUpdateRole("MAGASINIER", "Magasinier", "Gerer le stock des pieces de rechange.", fonctionnalites,
                overwriteExisting,
                "PAGE_PIECES", "PAGE_DETAIL_MACHINE",
                "PIECE_GERER_STOCK", "RAPPORT_CONSULTER");
        createOrUpdateRole("CONSULTATION", "Consultation", "Lire les donnees sans modifier les informations.", fonctionnalites,
                overwriteExisting,
                "PAGE_DASHBOARD", "PAGE_MACHINES", "PAGE_DETAIL_MACHINE", "PAGE_AIDE",
                "RAPPORT_CONSULTER");
    }

    @Transactional(readOnly = true)
    public List<RoleApplication> findRolesWithFonctionnalites() {
        return roleRepository.findAllByOrderByLibelleAsc();
    }

    @Transactional(readOnly = true)
    public List<FonctionnaliteApplication> findFonctionnalites() {
        return fonctionnaliteRepository.findAll();
    }

    @Transactional
    public RoleApplication createRole(RoleApplicationRequest request) {
        RoleApplication role = new RoleApplication();
        applyRoleRequest(role, request);
        return roleRepository.save(role);
    }

    @Transactional
    public RoleApplication updateRole(Long id, RoleApplicationRequest request) {
        RoleApplication role = roleRepository.findWithFonctionnalitesById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role introuvable id=" + id));
        applyRoleRequest(role, request);
        return roleRepository.save(role);
    }

    @Transactional
    public FonctionnaliteApplication createFonctionnalite(FonctionnaliteApplicationRequest request) {
        FonctionnaliteApplication fonctionnalite = new FonctionnaliteApplication();
        applyFonctionnaliteRequest(fonctionnalite, request);
        return fonctionnaliteRepository.save(fonctionnalite);
    }

    @Transactional
    public FonctionnaliteApplication updateFonctionnalite(Long id, FonctionnaliteApplicationRequest request) {
        FonctionnaliteApplication fonctionnalite = fonctionnaliteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fonctionnalite introuvable id=" + id));
        applyFonctionnaliteRequest(fonctionnalite, request);
        return fonctionnaliteRepository.save(fonctionnalite);
    }

    private void applyRoleRequest(RoleApplication role, RoleApplicationRequest request) {
        // Normalise le code et rattache les fonctionnalites demandees par le front.
        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new IllegalArgumentException("Le code du role est obligatoire");
        }
        if (request.getLibelle() == null || request.getLibelle().isBlank()) {
            throw new IllegalArgumentException("Le libelle du role est obligatoire");
        }
        role.setCode(request.getCode().trim().toUpperCase());
        role.setLibelle(request.getLibelle().trim());
        role.setObjectif(request.getObjectif());
        role.setActif(request.isActif());
        List<FonctionnaliteApplication> fonctionnalites = fonctionnaliteRepository.findAllById(request.getFonctionnaliteIds());
        role.setFonctionnalites(fonctionnalites);
    }

    private void applyFonctionnaliteRequest(FonctionnaliteApplication fonctionnalite, FonctionnaliteApplicationRequest request) {
        // Normalise la fonctionnalite avant sauvegarde pour eviter les codes incoherents.
        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new IllegalArgumentException("Le code de la fonctionnalite est obligatoire");
        }
        if (request.getLibelle() == null || request.getLibelle().isBlank()) {
            throw new IllegalArgumentException("Le libelle de la fonctionnalite est obligatoire");
        }
        fonctionnalite.setCode(request.getCode().trim().toUpperCase());
        fonctionnalite.setLibelle(request.getLibelle().trim());
        fonctionnalite.setDescription(request.getDescription());
        fonctionnalite.setRoute(request.getRoute());
        fonctionnalite.setCategorie(request.getCategorie() == null ? CategorieFonctionnalite.ACTION : request.getCategorie());
        fonctionnalite.setActif(request.isActif());
    }

    private void addFeature(
            Map<String, FonctionnaliteApplication> map,
            String code,
            String libelle,
            String description,
            String route,
            CategorieFonctionnalite categorie,
            boolean overwriteExisting
    ) {
        // Cree ou met a jour une fonctionnalite par defaut et la place dans la map locale.
        FonctionnaliteApplication fonctionnalite = fonctionnaliteRepository.findByCode(code)
                .orElseGet(FonctionnaliteApplication::new);
        if (fonctionnalite.getId() == null || overwriteExisting) {
            fonctionnalite.setCode(code);
            fonctionnalite.setLibelle(libelle);
            fonctionnalite.setDescription(description);
            fonctionnalite.setRoute(route);
            fonctionnalite.setCategorie(categorie);
            fonctionnalite.setActif(true);
        }
        map.put(code, fonctionnaliteRepository.save(fonctionnalite));
    }

    private void createOrUpdateRole(
            String code,
            String libelle,
            String objectif,
            Map<String, FonctionnaliteApplication> fonctionnalites,
            boolean overwriteExisting,
            String... codes
    ) {
        // Cree ou met a jour un role par defaut avec sa liste de fonctionnalites.
        RoleApplication role = roleRepository.findByCode(code).orElseGet(RoleApplication::new);
        if (role.getId() == null || overwriteExisting) {
            role.setCode(code);
            role.setLibelle(libelle);
            role.setObjectif(objectif);
            role.setActif(true);
            List<FonctionnaliteApplication> assigned = new ArrayList<>();
            Arrays.stream(codes)
                    .map(fonctionnalites::get)
                    .filter(item -> item != null)
                    .forEach(assigned::add);
            role.setFonctionnalites(assigned);
        }
        roleRepository.save(role);
    }
}
