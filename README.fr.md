# CanalMaDash : plateforme de maintenance prédictive

Plateforme web de gestion de la maintenance industrielle (machines, demandes de maintenance, interventions, pièces de rechange, tableaux de bord) avec un **module de machine learning qui estime le risque de panne dans les 7 prochains jours**, et une **authentification par rôles**.

> Réalisé pendant un stage chez **Canal Technology Group**. La plateforme de base (Spring Boot + Angular + MySQL, application mobile) a été développée par mon encadrant de stage. **Mes contributions :** le module de maintenance prédictive (simulation, jeu de données, modèle, validation, API, intégration dans l'interface) et la couche d'authentification et de droits par rôle. Voir [Ce que j'ai réalisé](#ce-que-jai-réalisé).

[English version](README.md)

## Fonctionnalités

- Supervision du parc de machines : état en direct, temps passé dans l'état actuel, historique
- Demandes de maintenance avec tableau Kanban, interventions, contrôles périodiques
- Stock de pièces de rechange avec alertes et mouvements
- Tableau de bord : disponibilité, MTBF, MTTR, coûts, analyse de remplacement, exports PDF et CSV
- Assistant IA local (Ollama) pour le diagnostic et un chatbot
- **Prédiction du risque de panne (7 jours)** avec explication des facteurs principaux (SHAP)
- **Authentification et droits par rôle** (JWT + BCrypt)

## Architecture

| Partie | Technologie | Port |
|---|---|---|
| API backend | Spring Boot 3.5, Java 17, Spring Security, MySQL | 8080 |
| Frontend | Angular 17, Tailwind CSS | 4200 |
| API ML | Python 3.12, Flask, scikit-learn, SHAP | 5000 |
| LLM local | Ollama (`gemma3:4b`) | 11434 |
| Application mobile | Ionic / Capacitor (`canal-ma-mobile`) | - |

```
Angular  ->  Spring Boot (JWT)  ->  MySQL
                  |-> API Flask ML (score de risque + SHAP)
                  |-> Ollama (diagnostic, chatbot, rapport rédigé)
```

## Démarrage

### Prérequis

Java 17+, Node.js 20+, MySQL 8, Python 3.12, et éventuellement [Ollama](https://ollama.com) avec `ollama pull gemma3:4b`.

### 1. Backend

La base `CanalMaDashDB` est créée automatiquement. Les tables sont créées au démarrage, et les 6 rôles par défaut avec leurs droits sont insérés au premier lancement.

```bash
export DB_PASSWORD='mot-de-passe-root-mysql'
export JWT_SECRET='un-secret-aleatoire-d-au-moins-32-caracteres'
cd CANALMADASH
bash mvnw spring-boot:run
```

Aucun secret n'est stocké dans le code : `DB_PASSWORD` et `JWT_SECRET` sont lus depuis des variables d'environnement. Utilise un `JWT_SECRET` différent dans chaque environnement.

### 2. Créer le premier compte

Les mots de passe sont stockés sous forme de hash BCrypt. Génère un hash, puis insère le compte dans MySQL :

```bash
pip install bcrypt
python3 -c "import bcrypt; print(bcrypt.hashpw(b'VOTRE_MOT_DE_PASSE', bcrypt.gensalt(10)).decode())"
```

```sql
INSERT INTO utilisateur (nom, email, mot_de_passe_hash, role_id)
VALUES ('Votre Nom', 'vous@exemple.com', '<HASH_BCRYPT>', 1);
```

Identifiants de rôle : `1` ADMIN, `2` RESPONSABLE_MAINTENANCE, `3` TECHNICIEN_MAINTENANCE, `4` OPERATEUR, `5` MAGASINIER, `6` CONSULTATION.

### 3. Frontend

```bash
cd CANAL_MA_DASH_UI
npm install
npm start
```

Ouvre http://localhost:4200 et connecte-toi.

### 4. API ML (optionnel)

```bash
pip install -r requirements.txt
python3 flask_api.py        # demande le mot de passe MySQL, ou lit DB_PASSWORD
```

Les versions figées dans `requirements.txt` comptent : `model_v2.pkl` ne peut être chargé qu'avec la même version de scikit-learn que celle de l'entraînement.

## Authentification et rôles

- La connexion renvoie un **JWT** signé (HS512, 8 h) contenant le rôle de l'utilisateur et les codes de ses droits.
- Les mots de passe sont hashés avec **BCrypt**. L'API est **sans état** (stateless).
- Les droits viennent de la base de données (`role_application`, `fonctionnalite_application`) : chaque rôle est relié à des *pages* et des *actions*.
- **Frontend :** les gardes de routes, les entrées de menu et les boutons sont affichés ou cachés selon les droits. Une URL saisie à la main sans droit est redirigée.
- **Backend :** chaque endpoint exige le droit correspondant (par exemple changer l'état d'une machine, piloter les demandes, gérer le stock). Tout ce qui n'est pas explicitement autorisé renvoie `403`.

| Rôle | Entrées de menu |
|---|---|
| ADMIN | toutes les pages, dont les paramètres et la gestion des rôles |
| RESPONSABLE_MAINTENANCE | Dashboard, Kanban, Pièces, Aide |
| TECHNICIEN_MAINTENANCE | Machines, Kanban, Pièces |
| OPERATEUR | Machines |
| MAGASINIER | Pièces |
| CONSULTATION | Dashboard, Machines, Aide |

## Maintenance prédictive (ML)

**Objectif :** pour chaque machine et chaque jour, estimer la probabilité d'une panne dans les 7 jours suivants.

**Chaîne de traitement**

```
generate_cafe_data.py  ->  build_dataset.py  ->  train_validate.py  ->  model_v2.pkl + model_v2_meta.json
(données simulées)         (variables + cible)   (Random Forest, validation)
                                                                         |
                                                       flask_api.py (score + SHAP)  ->  Spring Boot  ->  Angular
```

- **Données :** 24 mois de données **simulées** pour 12 machines d'un café-restaurant (capteurs, pannes, maintenance).
- **Variables (30) :** historique de maintenance (âge de la machine, jours depuis la dernière maintenance, pannes sur 30 / 90 jours...), contexte (ambiance, intensité, dureté sur 7 jours) et signaux capteurs (température, vibration, pression, cycles : z-scores et pentes).
- **Validation :** séparation temporelle stricte (entraînement jusqu'au 2026-03-31, test à partir du 2026-04-08), horizon de 7 jours.

| Métrique (jeu de test) | Valeur |
|---|---|
| Modèle | Random Forest |
| PR-AUC | 0,753 |
| F1 | 0,723 |
| Précision | 0,767 |
| Rappel | 0,685 |
| Seuil de décision | 0,38 |

**Limites, dites clairement**

- Les données sont **simulées** : c'est une preuve de concept à valider sur des données réelles, pas un modèle de production.
- Le modèle contient des variables propres aux équipements (`eq_cafe`, `eq_moulin`, ...), il est donc **spécifique à ce parc**. Un autre site a besoin de ses propres données et d'un réentraînement avec la même chaîne (adapter les variables d'équipement, puis relancer `build_dataset.py` et `train_validate.py`).
- `generate_cafe_data.py` écrit des lignes simulées dans la base MySQL : à lancer uniquement sur une base de développement.

## Ce que j'ai réalisé

- Module de maintenance prédictive : simulation des données, construction du jeu de données, entraînement du modèle et validation temporelle, API Flask avec explications SHAP, intégration Spring Boot, et écrans de score de risque dans Angular.
- Authentification et droits par rôle : BCrypt + JWT, règles Spring Security par droit, gardes et intercepteur Angular, menus et boutons pilotés par les droits.

## Structure du projet

```
CANALMADASH/          Backend Spring Boot
CANAL_MA_DASH_UI/     Frontend Angular
canal-ma-mobile/      Application mobile Ionic
database/             Scripts SQL
flask_api.py          API ML
generate_cafe_data.py, build_dataset.py, train_validate.py   Chaîne ML
model_v2.pkl, model_v2_meta.json, dataset_*.{csv,json}       Modèle entraîné et données
```