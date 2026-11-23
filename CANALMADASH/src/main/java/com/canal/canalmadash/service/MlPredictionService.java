package com.canal.canalmadash.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class MlPredictionService {

    /*
     * Service qui appelle l'API Flask ML (port 5000)
     * pour obtenir une prediction de panne pour une machine.
     */

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    // Flask API URL
    private static final String FLASK_URL = "http://localhost:5000";


    // Reglages Ollama (les memes que ceux de application.properties)
    @org.springframework.beans.factory.annotation.Value("${app.ai.ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @org.springframework.beans.factory.annotation.Value("${app.ai.ollama.model:llama3.1}")
    private String ollamaModel;

    @org.springframework.beans.factory.annotation.Value("${app.ai.ollama.timeout-seconds:90}")
    private long ollamaTimeout;

    public MlPredictionService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    // ─── DTO for ML Prediction Result ─────────────────
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class MlPredictionResult {
        @com.fasterxml.jackson.annotation.JsonAlias("machine_id")
        public int machineId;

        public String nom;
        public double probability;

        @com.fasterxml.jackson.annotation.JsonAlias("risk_level")
        public String riskLevel;

        @com.fasterxml.jackson.annotation.JsonAlias("risk_color")
        public String riskColor;

        public String recommendation;

        @com.fasterxml.jackson.annotation.JsonAlias("model_accuracy")
        public double modelAccuracy;

        @com.fasterxml.jackson.annotation.JsonAlias("model_metrics")
        public Object modelMetrics;

        @com.fasterxml.jackson.annotation.JsonAlias("as_of")
        public String asOf;

        public Object features;
        public Object economics;

        @com.fasterxml.jackson.annotation.JsonAlias("top_factors")
        public Object topFactors;

        public String error;
    }
    // ─── PREDICT FOR ONE MACHINE ───────────────────────
    public MlPredictionResult predictMachine(int machineId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(FLASK_URL + "/predict/" + machineId))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 200) {
                return objectMapper.readValue(
                        response.body(),
                        MlPredictionResult.class
                );
            } else {
                MlPredictionResult error = new MlPredictionResult();
                error.error = "Flask API returned status: " + response.statusCode();
                return error;
            }

        } catch (Exception e) {
            MlPredictionResult error = new MlPredictionResult();
            error.error = "Cannot connect to ML API: " + e.getMessage();
            return error;
        }
    }

    // ─── PREDICT FOR ALL MACHINES ─────────────────────
    public JsonNode predictAll() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(FLASK_URL + "/predict/all"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            return objectMapper.readTree(response.body());

        } catch (Exception e) {
            return objectMapper.createObjectNode()
                    .put("error", "Cannot connect to ML API: " + e.getMessage());
        }
    }

    // ─── HISTORIQUE DU SCORE (periode de test) ────────
    public JsonNode history(int machineId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(FLASK_URL + "/history/" + machineId))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            return objectMapper.readTree(response.body());

        } catch (Exception e) {
            return objectMapper.createObjectNode()
                    .put("error", "Cannot connect to ML API: " + e.getMessage());
        }
    }
    public JsonNode report(int machineId) {
        try {
            MlPredictionResult p = predictMachine(machineId);
            if (p.error != null) {
                return objectMapper.createObjectNode().put("error", p.error);
            }

            StringBuilder hausse = new StringBuilder();
            StringBuilder baisse = new StringBuilder();
            JsonNode list = objectMapper.valueToTree(p.topFactors);
            if (list != null && list.isArray()) {
                for (JsonNode f : list) {
                    double c = f.path("contribution").asDouble();
                    String line = "- " + factorLabel(f.path("name").asText())
                            + " (" + Math.abs(c) + " points)\n";
                    if (c > 0) {
                        hausse.append(line);
                    } else {
                        baisse.append(line);
                    }
                }
            }
            String up = hausse.length() == 0 ? "aucun\n" : hausse.toString();
            String down = baisse.length() == 0 ? "aucun\n" : baisse.toString();
            long seuil = Math.round(objectMapper.valueToTree(p.modelMetrics).path("seuil").asDouble() * 100);

            String system = "Tu rediges un court rapport de maintenance en francais simple pour un cafe-restaurant, "
                    + "a partir de resultats deja calcules. Regles : utilise UNIQUEMENT les informations fournies ; "
                    + "n'invente aucune cause, aucun chiffre et aucune action ; ne dis jamais que le score est une "
                    + "probabilite ; si une liste vaut 'aucun', dis qu'aucun facteur n'agit dans ce sens. "
                    + "Format exact, trois titres : "
                    + "'Ce que dit le modele:' (1 phrase : le score, le seuil et le niveau), "
                    + "'Pourquoi:' (2 phrases maximum : cite d'abord les facteurs qui augmentent le risque, "
                    + "puis ceux qui le diminuent), "
                    + "'Action recommandee:' (recopie exactement la recommandation fournie, sans rien ajouter).";

            String user = "Equipement : " + p.nom + "\n"
                    + "Score de risque (0 a 100) : " + p.probability + "\n"
                    + "Seuil d'alerte : " + seuil + "\n"
                    + "Niveau : " + p.riskLevel + "\n"
                    + "Donnees au : " + p.asOf + "\n"
                    + "Facteurs qui AUGMENTENT le risque :\n" + up
                    + "Facteurs qui DIMINUENT le risque :\n" + down
                    + "Recommandation a recopier : " + p.recommendation;

            java.util.Map<String, Object> payload = java.util.Map.of(
                    "model", ollamaModel,
                    "stream", false,
                    "options", java.util.Map.of("temperature", 0, "num_predict", 250),
                    "messages", java.util.List.of(
                            java.util.Map.of("role", "system", "content", system),
                            java.util.Map.of("role", "user", "content", user)
                    )
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ollamaBaseUrl.replaceAll("/+$", "") + "/api/chat"))
                    .timeout(Duration.ofSeconds(ollamaTimeout))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return objectMapper.createObjectNode()
                        .put("error", "Ollama a retourne l'erreur HTTP " + response.statusCode());
            }

            String text = objectMapper.readTree(response.body()).path("message").path("content").asText("");
            com.fasterxml.jackson.databind.node.ObjectNode out = objectMapper.createObjectNode();
            out.put("machineId", machineId);
            out.put("model", ollamaModel);
            out.put("score", p.probability);
            out.put("riskLevel", p.riskLevel);
            out.put("report", text);
            return out;

        } catch (Exception e) {
            return objectMapper.createObjectNode()
                    .put("error", "Impossible de generer le rapport : " + e.getMessage());
        }
    }

    private static String factorLabel(String name) {
        switch (name) {
            case "pente_vib": return "Tendance des vibrations (7 j)";
            case "z_vib": return "Vibrations par rapport a la normale";
            case "pente_temp": return "Tendance de la temperature (7 j)";
            case "z_temp": return "Temperature par rapport a la normale";
            case "pente_pres": return "Tendance de la pression (7 j)";
            case "z_pres": return "Pression par rapport a la normale";
            case "pente_cycle": return "Tendance du temps de cycle (7 j)";
            case "z_cycle": return "Temps de cycle par rapport a la normale";
            case "bruit_7j": return "Bruits anormaux signales (7 j)";
            case "jours_depuis_maintenance": return "Jours depuis la derniere maintenance";
            case "retard_maintenance_ratio": return "Retard de maintenance";
            case "pannes_30j": return "Pannes sur 30 jours";
            case "pannes_90j": return "Pannes sur 90 jours";
            case "jours_depuis_derniere_panne": return "Jours depuis la derniere panne";
            case "age_mois": return "Age de l'equipement (mois)";
            case "amb_7j": return "Temperature ambiante (7 j)";
            case "intensite_7j": return "Intensite d'utilisation (7 j)";
            case "durete_7j": return "Durete de l'eau (7 j)";
            default: return name;
        }
    }


    // ─── CHECK FLASK API HEALTH ───────────────────────
    public boolean isFlaskAvailable() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(FLASK_URL + "/health"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            return response.statusCode() == 200;

        } catch (Exception e) {
            return false;
        }
    }
}