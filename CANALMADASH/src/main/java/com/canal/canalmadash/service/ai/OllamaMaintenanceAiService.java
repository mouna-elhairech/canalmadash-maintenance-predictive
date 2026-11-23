package com.canal.canalmadash.service.ai;

import com.canal.canalmadash.dto.AiDiagnosticResponse;
import com.canal.canalmadash.dto.MachineMaintenanceIndicatorsDto;
import com.canal.canalmadash.dto.AiResponseSection;
import com.canal.canalmadash.entity.DemandeMaintenance;
import com.canal.canalmadash.entity.Machine;
import com.canal.canalmadash.entity.Maintenance;
import com.canal.canalmadash.entity.MaintenancePieceUtilisee;
import com.canal.canalmadash.repository.DemandeMaintenanceRepository;
import com.canal.canalmadash.repository.MachineRepository;
import com.canal.canalmadash.repository.MaintenanceRepository;
import com.canal.canalmadash.repository.PieceRechangeRepository;
import com.canal.canalmadash.service.DashboardMaintenanceService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "ollama")
public class OllamaMaintenanceAiService implements MaintenanceAiService {

    /*
     * Implementation IA basee sur Ollama.
     * Elle construit un contexte metier a partir de la base de donnees, appelle
     * l'API locale Ollama, puis transforme la reponse en sections affichables.
     */
    private final MachineRepository machineRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final DemandeMaintenanceRepository demandeRepository;
    private final PieceRechangeRepository pieceRechangeRepository;
    private final DashboardMaintenanceService dashboardMaintenanceService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${app.ai.ollama.base-url:http://localhost:11434}")
    private String baseUrl;

    @Value("${app.ai.ollama.model:llama3.1}")
    private String model;

    @Value("${app.ai.ollama.timeout-seconds:90}")
    private long timeoutSeconds;

    public OllamaMaintenanceAiService(
            MachineRepository machineRepository,
            MaintenanceRepository maintenanceRepository,
            DemandeMaintenanceRepository demandeRepository,
            PieceRechangeRepository pieceRechangeRepository,
            DashboardMaintenanceService dashboardMaintenanceService,
            ObjectMapper objectMapper
    ) {
        this.machineRepository = machineRepository;
        this.maintenanceRepository = maintenanceRepository;
        this.demandeRepository = demandeRepository;
        this.pieceRechangeRepository = pieceRechangeRepository;
        this.dashboardMaintenanceService = dashboardMaintenanceService;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public AiDiagnosticResponse testModel() {
        // Petit appel de verification pour savoir si Ollama et le modele repondent.
        String prompt = """
                Fais un test court.
                Reponds exactement avec:
                - Service IA operationnel
                - Modele pret pour diagnostic maintenance
                - Une phrase simple de conseil maintenance
                """;

        AiDiagnosticResponse response = new AiDiagnosticResponse();
        response.setProvider("ollama");
        response.setModel(model);
        response.setStatus("OK");
        response.setDiagnostic(callOllama(prompt));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public AiDiagnosticResponse diagnoseMachine(int machineId) {
        /*
         * Diagnostic cible sur une machine.

         */
        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new IllegalArgumentException("Machine introuvable id=" + machineId));
        MachineMaintenanceIndicatorsDto indicators = dashboardMaintenanceService.getMachineIndicators(machineId);
        List<Maintenance> maintenances = maintenanceRepository.findByMachineIdOrderByDateDebutDesc(machineId).stream()
                .limit(10)
                .toList();
        List<DemandeMaintenance> demandes = demandeRepository.findByMachineIdOrderByDateDemandeDesc(machineId).stream()
                .limit(10)
                .toList();

        String context = buildContext(machine, indicators, maintenances, demandes);
        String diagnostic = callOllama(context);

        AiDiagnosticResponse response = new AiDiagnosticResponse();
        response.setProvider("ollama");
        response.setModel(model);
        response.setMachineId(machine.getId());
        response.setMachineNom(machine.getNom());
        response.setStatus("OK");
        response.setDiagnostic(diagnostic);
        response.setSections(toSections(diagnostic));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public AiDiagnosticResponse chat(Integer machineId, String message) {
        // Chat global ou contextualise par machine selon la presence de machineId.
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Le message est obligatoire");
        }

        String context = buildGlobalContext();
        Machine machine = null;
        if (machineId != null) {
            machine = machineRepository.findById(machineId)
                    .orElseThrow(() -> new IllegalArgumentException("Machine introuvable id=" + machineId));
            MachineMaintenanceIndicatorsDto indicators = dashboardMaintenanceService.getMachineIndicators(machineId);
            List<Maintenance> maintenances = maintenanceRepository.findByMachineIdOrderByDateDebutDesc(machineId).stream()
                    .limit(10)
                    .toList();
            List<DemandeMaintenance> demandes = demandeRepository.findByMachineIdOrderByDateDemandeDesc(machineId).stream()
                    .limit(10)
                    .toList();
            context = buildContext(machine, indicators, maintenances, demandes);
        }

        String prompt = """
                CONTEXTE APPLICATION ET DONNEES DISPONIBLES
                %s

                QUESTION UTILISATEUR
                %s

                Reponds en francais simple.
                Si la question concerne les donnees, utilise uniquement les donnees du contexte.
                Si une information manque, dis clairement qu elle manque.
                Tu peux aider l utilisateur a comprendre comment utiliser l application.
                Structure la reponse avec 2 a 4 sections courtes.
                """.formatted(context, message);

        String answer = callOllama(prompt);
        AiDiagnosticResponse response = new AiDiagnosticResponse();
        response.setProvider("ollama");
        response.setModel(model);
        response.setMachineId(machineId == null ? 0 : machineId);
        response.setMachineNom(machine == null ? null : machine.getNom());
        response.setStatus("OK");
        response.setDiagnostic(answer);
        response.setSections(toSections(answer));
        return response;
    }

    private String callOllama(String context) {
        // Appel HTTP brut vers l'endpoint chat d'Ollama.
        try {
            Map<String, Object> payload = Map.of(
                    "model", model,
                    "stream", false,
                    "messages", List.of(
                            Map.of(
                                    "role", "system",
                                    "content", """
                                            Tu es un assistant IA specialise en maintenance industrielle.
                                            Tu aides a diagnostiquer les machines a partir des donnees fournies.
                                            Tu ne dois pas inventer des donnees absentes.
                                            Reponds en francais simple.
                                            Utilise des titres de section courts sous cette forme:
                                            Resume:
                                            Risque:
                                            Causes probables:
                                            Actions recommandees:
                                            Pieces a verifier:
                                            Decision conseillee:
                                            """
                            ),
                            Map.of("role", "user", "content", context)
                    )
            );

            // Construction de la requete attendue par l'API chat d'Ollama.
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(normalizedBaseUrl() + "/api/chat"))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return "Ollama a retourne une erreur HTTP " + response.statusCode() + " : " + response.body();
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode content = root.path("message").path("content");
            if (content.isMissingNode() || content.asText().isBlank()) {
                return "Ollama a repondu, mais le contenu du diagnostic est vide.";
            }
            return content.asText();
        } catch (IOException e) {
            return "Impossible de communiquer avec Ollama. Verifie que Ollama est demarre et que le modele est installe. Detail: " + e.getMessage();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "La demande IA a ete interrompue.";
        }
    }

    private List<AiResponseSection> toSections(String text) {
        /*
         * L'IA renvoie du texte libre. Cette methode reconnait les titres les plus
         * probables pour afficher la reponse sous forme de blocs propres dans l'UI.
         */
        List<String> knownTitles = List.of(
                "Resume",
                "Résumé",
                "Risque",
                "Causes probables",
                "Actions recommandees",
                "Actions recommandées",
                "Pieces a verifier",
                "Pièces à vérifier",
                "Decision conseillee",
                "Décision conseillée",
                "Conseil",
                "Recommandation"
        );
        List<AiResponseSection> sections = new java.util.ArrayList<>();
        String currentTitle = "Reponse";
        StringBuilder currentContent = new StringBuilder();

        for (String rawLine : text.split("\\R")) {
            String line = rawLine.trim();
            // On accepte aussi les titres numerotes ou en markdown gras.
            String normalizedLine = line
                    .replaceFirst("^\\d+[.)]\\s*", "")
                    .replace("**", "");
            String title = knownTitles.stream()
                    .filter(item -> normalizedLine.equalsIgnoreCase(item + ":") || normalizedLine.equalsIgnoreCase(item))
                    .findFirst()
                    .orElse(null);
            if (title != null) {
                addSection(sections, currentTitle, currentContent);
                currentTitle = title.replace("Résumé", "Resume")
                        .replace("Actions recommandées", "Actions recommandees")
                        .replace("Pièces à vérifier", "Pieces a verifier")
                        .replace("Décision conseillée", "Decision conseillee");
                currentContent = new StringBuilder();
            } else if (!line.isBlank()) {
                if (!currentContent.isEmpty()) {
                    currentContent.append("\n");
                }
                currentContent.append(rawLine);
            }
        }
        addSection(sections, currentTitle, currentContent);
        if (sections.isEmpty() && text != null && !text.isBlank()) {
            sections.add(new AiResponseSection("Reponse", text));
        }
        return sections;
    }

    private void addSection(List<AiResponseSection> sections, String title, StringBuilder content) {
        if (!content.isEmpty()) {
            sections.add(new AiResponseSection(title, content.toString().trim()));
        }
    }

    private String buildGlobalContext() {
        /*
         * Contexte pour le chatbot general.
         * On donne a l'IA la structure de l'application et un echantillon recent
         * des donnees pour qu'elle reponde sans inventer.
         */
        List<Machine> machines = machineRepository.findAll();
        List<DemandeMaintenance> demandes = demandeRepository.findAll().stream()
                .sorted((a, b) -> b.getDateDemande().compareTo(a.getDateDemande()))
                .limit(15)
                .toList();
        List<Maintenance> maintenances = maintenanceRepository.findAll().stream()
                .sorted((a, b) -> {
                    if (a.getDateDebut() == null && b.getDateDebut() == null) return 0;
                    if (a.getDateDebut() == null) return 1;
                    if (b.getDateDebut() == null) return -1;
                    return b.getDateDebut().compareTo(a.getDateDebut());
                })
                .limit(15)
                .toList();

        return """
                TU ES LE CHATBOT GLOBAL DE L APPLICATION CANALMADASH.

                ROLE DU CHATBOT
                - Expliquer comment utiliser l application.
                - Aider a trouver les machines, demandes, interventions, pieces et indicateurs.
                - Proposer des actions maintenance a partir des donnees disponibles.
                - Ne jamais modifier les donnees directement.

                PAGES PRINCIPALES
                - Dashboard: indicateurs globaux, machines critiques, couts, retards, calendrier.
                - Machine List: liste des machines et acces au detail machine.
                - Detail machine: etat actuel, historique, demandes, interventions, pieces, indicateurs, diagnostic IA.
                - Kanban: demandes par statut avec drag and drop.
                - Pieces: stock pieces de rechange, machines compatibles, entree stock, retour piece.
                - Parametres types: types action et types maintenance.
                - Parametres score: poids, facteurs et seuils du score criticite.
                - Roles droits: roles et fonctionnalites parametrables.
                - Aide: explication de l application.

                RESUME BASE DE DONNEES
                - Machines: %s
                - Demandes maintenance: %s
                - Interventions: %s
                - Pieces de rechange: %s

                MACHINES
                %s

                PIECES DE RECHANGE
                %s

                DERNIERES DEMANDES
                %s

                DERNIERES INTERVENTIONS
                %s
                """.formatted(
                machines.size(),
                demandeRepository.count(),
                maintenanceRepository.count(),
                pieceRechangeRepository.count(),
                formatMachinesGlobal(machines),
                formatPiecesGlobal(),
                formatDemandes(demandes),
                formatMaintenances(maintenances)
        );
    }

    private String formatMachinesGlobal(List<Machine> machines) {
        // Resume compact du parc machine, limite pour garder un prompt raisonnable.
        if (machines.isEmpty()) {
            return "- Aucune machine disponible.";
        }
        return machines.stream()
                .limit(25)
                .map(machine -> {
                    MachineMaintenanceIndicatorsDto indicators = dashboardMaintenanceService.getMachineIndicators(machine.getId());
                    return "- #%s | nom=%s | ref=%s | etat=%s | pannes=%s | disponibilite=%.2f%% | coutMaintenance=%.2f | remplacement=%.2f%%".formatted(
                            machine.getId(),
                            safe(machine.getNom()),
                            safe(machine.getRef()),
                            machine.getTypeAction() == null ? "-" : safe(machine.getTypeAction().getLibelle()),
                            indicators.getFailureCount(),
                            indicators.getAvailabilityPercent(),
                            indicators.getMaintenanceCost(),
                            indicators.getReplacementRatioPercent()
                    );
                })
                .collect(Collectors.joining("\n"));
    }

    private String formatPiecesGlobal() {
        // Les pieces sont ajoutees au contexte pour aider l'IA a proposer quoi verifier.
        List<com.canal.canalmadash.entity.PieceRechange> pieces = pieceRechangeRepository.findAll().stream()
                .limit(25)
                .toList();
        if (pieces.isEmpty()) {
            return "- Aucune piece disponible.";
        }
        return pieces.stream()
                .map(piece -> "- #%s | reference=%s | libelle=%s | quantite=%s | seuil=%s | coutUnitaire=%s | standard=%s | alerteStock=%s".formatted(
                        piece.getId(),
                        safe(piece.getReference()),
                        safe(piece.getLibelle()),
                        piece.getQuantite(),
                        piece.getSeuilMinimum(),
                        piece.getCoutUnitaire(),
                        piece.isStandard(),
                        piece.isAlerteStock()
                ))
                .collect(Collectors.joining("\n"));
    }

    private String buildContext(
            Machine machine,
            MachineMaintenanceIndicatorsDto indicators,
            List<Maintenance> maintenances,
            List<DemandeMaintenance> demandes
    ) {
        /*
         * Contexte dedie a une machine.
         * Il combine les informations techniques, les indicateurs calcules et
         * l'historique recent des demandes/interventions.
         */
        return """
                Analyse cette machine et propose un diagnostic maintenance.

                MACHINE
                - Id: %s
                - Nom: %s
                - Reference: %s
                - Etat actuel: %s
                - Emplacement: %s
                - Fabricant: %s
                - Modele: %s
                - Numero serie: %s
                - Date installation: %s
                - Criticite saisie: %s
                - Heures de marche: %s
                - Cout investissement total: %.2f
                - Commentaire technique: %s

                INDICATEURS
                - Nombre de pannes: %s
                - Temps d arret total minutes: %s
                - Disponibilite: %.2f%%
                - Temps moyen entre pannes heures: %.2f
                - Temps moyen de reparation heures: %.2f
                - Cout maintenance: %.2f
                - Ratio remplacement: %.2f%%
                - Recommandation remplacement: %s
                - Recommandation disponibilite: %s
                - Recommandation temps arret: %s
                - Recommandation pannes: %s
                - Recommandation temps reparation: %s

                DERNIERES DEMANDES
                %s

                DERNIERES INTERVENTIONS
                %s
                """.formatted(
                machine.getId(),
                safe(machine.getNom()),
                safe(machine.getRef()),
                machine.getTypeAction() == null ? "-" : safe(machine.getTypeAction().getLibelle()),
                safe(machine.getEmplacement()),
                safe(machine.getFabricant()),
                safe(machine.getModele()),
                safe(machine.getNumeroSerie()),
                machine.getDateInstallation(),
                safe(machine.getCriticite()),
                machine.getHeuresMarche(),
                machine.getCoutTotalInvestissement(),
                safe(machine.getCommentaireTechnique()),
                indicators.getFailureCount(),
                indicators.getDowntimeMinutes(),
                indicators.getAvailabilityPercent(),
                indicators.getMtbfHours(),
                indicators.getMttrHours(),
                indicators.getMaintenanceCost(),
                indicators.getReplacementRatioPercent(),
                safe(indicators.getReplacementRecommendation()),
                safe(indicators.getAvailabilityRecommendation()),
                safe(indicators.getDowntimeRecommendation()),
                safe(indicators.getFailureRecommendation()),
                safe(indicators.getRepairTimeRecommendation()),
                formatDemandes(demandes),
                formatMaintenances(maintenances)
        );
    }

    private String formatDemandes(List<DemandeMaintenance> demandes) {
        // Transforme les demandes en lignes simples pour que le modele les lise facilement.
        if (demandes.isEmpty()) {
            return "- Aucune demande disponible.";
        }
        return demandes.stream()
                .map(demande -> "- #%s | statut=%s | priorite=%s | type=%s | date=%s | souhaitee=%s | description=%s".formatted(
                        demande.getId(),
                        demande.getStatut(),
                        demande.getPriorite(),
                        demande.getTypeMaintenance() == null ? "-" : safe(demande.getTypeMaintenance().getLibelle()),
                        demande.getDateDemande(),
                        demande.getDateSouhaitee(),
                        safe(demande.getDescription())
                ))
                .collect(Collectors.joining("\n"));
    }

    private String formatMaintenances(List<Maintenance> maintenances) {
        // Meme principe pour les interventions: une ligne par operation avec les champs importants.
        if (maintenances.isEmpty()) {
            return "- Aucune intervention disponible.";
        }
        return maintenances.stream()
                .map(maintenance -> "- #%s | type=%s | intervention=%s | debut=%s | fin=%s | cout=%.2f | cause=%s | description=%s | pieces=%s".formatted(
                        maintenance.getId(),
                        maintenance.getType() == null ? "-" : safe(maintenance.getType().getLibelle()),
                        maintenance.getTypeIntervention(),
                        maintenance.getDateDebut(),
                        maintenance.getDateFin(),
                        maintenance.getCout() == null ? 0 : maintenance.getCout(),
                        safe(maintenance.getCausePanne()),
                        safe(maintenance.getDescription()),
                        formatPieces(maintenance)
                ))
                .collect(Collectors.joining("\n"));
    }

    private String formatPieces(Maintenance maintenance) {
        // Priorite aux pieces structurees; sinon on garde l'ancien champ texte.
        if (maintenance.getPieces() == null || maintenance.getPieces().isEmpty()) {
            return safe(maintenance.getPiecesUtilisees());
        }
        return maintenance.getPieces().stream()
                .map(this::formatPiece)
                .collect(Collectors.joining(", "));
    }

    private String formatPiece(MaintenancePieceUtilisee item) {
        String libelle = item.getPiece() == null ? "piece" : safe(item.getPiece().getLibelle());
        return libelle + " x" + item.getQuantite() + " coutUnitaire=" + (item.getCoutUnitaire() == null ? 0 : item.getCoutUnitaire());
    }

    private String normalizedBaseUrl() {
        // Evite de produire une URL avec deux slashs avant /api/chat.
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
