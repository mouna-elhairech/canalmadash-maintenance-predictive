# CanalMaDash: Predictive Maintenance Platform

Web platform for industrial maintenance management (machines, maintenance requests, interventions, spare parts, dashboards) with a **machine-learning module that estimates the risk of a breakdown within the next 7 days**, and **role-based authentication**.

> Built during an internship at **Canal Technology Group**. The base platform (Spring Boot + Angular + MySQL, mobile app) was developed by my internship supervisor. **My contributions:** the predictive-maintenance module (simulation, dataset, model, validation, API, UI integration) and the authentication / role-based access layer. See [What I built](#what-i-built).

[Version française](README.fr.md)

## Features

- Machine fleet supervision: live status, time spent in the current state, history
- Maintenance requests with a Kanban board, interventions, preventive controls
- Spare-parts stock with alerts and movements
- Dashboard: availability, MTBF, MTTR, costs, replacement analysis, PDF and CSV exports
- Local AI assistant (Ollama) for diagnostics and a chatbot
- **Breakdown-risk prediction (7 days)** with explanation of the main factors (SHAP)
- **Authentication and permissions per role** (JWT + BCrypt)

## Architecture

| Part | Technology | Port |
|---|---|---|
| Backend API | Spring Boot 3.5, Java 17, Spring Security, MySQL | 8080 |
| Frontend | Angular 17, Tailwind CSS | 4200 |
| ML API | Python 3.12, Flask, scikit-learn, SHAP | 5000 |
| Local LLM | Ollama (`gemma3:4b`) | 11434 |
| Mobile app | Ionic / Capacitor (`canal-ma-mobile`) | - |

```
Angular  ->  Spring Boot (JWT)  ->  MySQL
                  |-> Flask ML API (risk score + SHAP)
                  |-> Ollama (diagnostic, chatbot, written report)
```

## Getting started

### Prerequisites

Java 17+, Node.js 20+, MySQL 8, Python 3.12, and optionally [Ollama](https://ollama.com) with `ollama pull gemma3:4b`.

### 1. Backend

The database `CanalMaDashDB` is created automatically. Tables are created on startup, and the 6 default roles with their permissions are seeded on first start.

```bash
export DB_PASSWORD='your-mysql-root-password'
export JWT_SECRET='a-random-secret-of-at-least-32-characters'
cd CANALMADASH
bash mvnw spring-boot:run
```

Secrets are never stored in the code: `DB_PASSWORD` and `JWT_SECRET` are read from environment variables. Use a different `JWT_SECRET` in each environment.

### 2. Create the first account

Passwords are stored as BCrypt hashes. Generate a hash, then insert the account in MySQL:

```bash
pip install bcrypt
python3 -c "import bcrypt; print(bcrypt.hashpw(b'YOUR_PASSWORD', bcrypt.gensalt(10)).decode())"
```

```sql
INSERT INTO utilisateur (nom, email, mot_de_passe_hash, role_id)
VALUES ('Your Name', 'you@example.com', '<BCRYPT_HASH>', 1);
```

Role ids: `1` ADMIN, `2` RESPONSABLE_MAINTENANCE, `3` TECHNICIEN_MAINTENANCE, `4` OPERATEUR, `5` MAGASINIER, `6` CONSULTATION.

### 3. Frontend

```bash
cd CANAL_MA_DASH_UI
npm install
npm start
```

Open http://localhost:4200 and sign in.

### 4. ML API (optional)

```bash
pip install -r requirements.txt
python3 flask_api.py        # asks for the MySQL password, or reads DB_PASSWORD
```

The pinned versions in `requirements.txt` matter: `model_v2.pkl` can only be loaded with the same scikit-learn version it was trained with.

## Authentication and roles

- Login returns a signed **JWT** (HS512, 8 h) containing the user's role and permission codes.
- Passwords are hashed with **BCrypt**. The API is **stateless**.
- Permissions come from the database (`role_application`, `fonctionnalite_application`): each role is linked to *pages* and *actions*.
- **Frontend:** route guards, menu entries and buttons are shown or hidden according to the permissions. Unauthorized direct URLs are redirected.
- **Backend:** every endpoint requires the matching permission (for example changing a machine state, piloting requests, managing stock). Anything not explicitly allowed returns `403`.

| Role | Menu entries |
|---|---|
| ADMIN | all pages, including settings and role management |
| RESPONSABLE_MAINTENANCE | Dashboard, Kanban, Spare parts, Help |
| TECHNICIEN_MAINTENANCE | Machines, Kanban, Spare parts |
| OPERATEUR | Machines |
| MAGASINIER | Spare parts |
| CONSULTATION | Dashboard, Machines, Help |

## Predictive maintenance (ML)

**Goal:** for each machine and each day, estimate the probability of a breakdown in the next 7 days.

**Pipeline**

```
generate_cafe_data.py  ->  build_dataset.py  ->  train_validate.py  ->  model_v2.pkl + model_v2_meta.json
(simulated data)           (features + target)   (Random Forest, validation)
                                                                         |
                                                       flask_api.py (score + SHAP)  ->  Spring Boot  ->  Angular
```

- **Data:** 24 months of **simulated** data for 12 café-restaurant machines (sensors, breakdowns, maintenance).
- **Features (30):** maintenance history (machine age, days since last maintenance, breakdowns over 30 / 90 days...), context (ambient, intensity, hardness over 7 days) and sensor signals (temperature, vibration, pressure, cycles: z-scores and slopes).
- **Validation:** strict time split (train until 2026-03-31, test from 2026-04-08), 7-day horizon.

| Metric (test set) | Value |
|---|---|
| Model | Random Forest |
| PR-AUC | 0.753 |
| F1 | 0.723 |
| Precision | 0.767 |
| Recall | 0.685 |
| Decision threshold | 0.38 |

**Limits, stated plainly**

- The data is **simulated**: this is a proof of concept to validate on real data, not a production model.
- The model contains equipment-specific features (`eq_cafe`, `eq_moulin`, ...), so it is **specific to this fleet**. Another site needs its own data and a retraining with the same pipeline (adapt the equipment features and rerun `build_dataset.py` and `train_validate.py`).
- `generate_cafe_data.py` writes simulated rows into the MySQL database: run it on a development database only.

## What I built

- Predictive-maintenance module: data simulation, dataset construction, model training and time-split validation, Flask API with SHAP explanations, Spring Boot integration, and the risk-score screens in Angular.
- Authentication and role-based access: BCrypt + JWT, Spring Security rules per permission, Angular guards / interceptor, and permission-driven menus and buttons.

## Project structure

```
CANALMADASH/          Spring Boot backend
CANAL_MA_DASH_UI/     Angular frontend
canal-ma-mobile/      Ionic mobile app
database/             SQL scripts
flask_api.py          ML API
generate_cafe_data.py, build_dataset.py, train_validate.py   ML pipeline
model_v2.pkl, model_v2_meta.json, dataset_*.{csv,json}       trained model and data
```