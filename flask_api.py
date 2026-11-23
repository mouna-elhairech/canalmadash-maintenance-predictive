#!/usr/bin/env python3
"""
flask_api.py  (version 2 : 12 equipements du cafe-restaurant)

API de prediction appelee par Spring Boot (GET /api/ml/predict/<id>).
Elle reutilise EXACTEMENT le code de build_dataset.py : les variables du modele en service
sont calculees comme a l'entrainement (une seule source de verite).

Lancer : python3 flask_api.py     (mot de passe MySQL demande, ou variable DB_PASSWORD)
Necessite : model_v2.pkl, model_v2_meta.json, build_dataset.py, generate_cafe_data.py
"""
import json
import os
import pickle
import time

import numpy as np
import pandas as pd
import shap
from flask import Flask, jsonify

import build_dataset as bd

CACHE_SECONDS = 60
app = Flask(__name__)

with open("model_v2.pkl", "rb") as fh:
    BUNDLE = pickle.load(fh)
MODEL, FEATURES, THRESHOLD = BUNDLE["model"], BUNDLE["features"], BUNDLE["threshold"]
META = json.load(open("model_v2_meta.json"))
EXPLAINER = shap.TreeExplainer(MODEL)   # explication des scores (valeurs de Shapley)
_cache = {"t": 0.0, "df": None, "names": {}}
_hist = {"t": 0.0, "df": None, "fail": {}}


def connect():
    import mysql.connector
    return mysql.connector.connect(host="localhost", user="root", password=PASSWORD, database="CanalMaDashDB")


def load_tables():
    conn = connect()
    cur = conn.cursor()

    def q(sql, cols):
        cur.execute(sql)
        return pd.DataFrame(cur.fetchall(), columns=cols)

    machines = q("SELECT id, ref, date_installation, nom FROM machine", ["machine_id", "ref", "date_installation", "nom"])
    actions = q("SELECT a.machine_id, t.code, a.date_debut, a.date_fin FROM action a "
                "JOIN type_action t ON t.id=a.type_action_id", ["machine_id", "code", "date_debut", "date_fin"])
    maint = q("SELECT m.machine_id, t.code, m.date_debut FROM maintenance m "
              "JOIN type_maintenance t ON t.id=m.type_id", ["machine_id", "code", "date_debut"])
    mes = q("SELECT date_mesure, machine_id, temperature_interne, vibration, pression, temps_cycle, "
            "temperature_ambiante, durete_eau, intensite_utilisation, bruit_signale FROM mesures_capteurs "
            "ORDER BY machine_id, date_mesure",
            ["date", "machine_id", *bd.SENSORS, "amb", "durete", "intensite", "bruit"])
    conn.close()
    return machines, actions, maint, mes


def load_costs():
    conn = connect()
    cur = conn.cursor()
    cur.execute("SELECT m.machine_id, t.code, m.cout FROM maintenance m "
                "JOIN type_maintenance t ON t.id=m.type_id WHERE m.cout IS NOT NULL")
    costs = pd.DataFrame(cur.fetchall(), columns=["machine_id", "code", "cout"])
    conn.close()
    costs["cout"] = costs["cout"].astype(float)
    return costs


def contributions(X):
    """Contribution de chaque variable au score de la classe 'panne' (valeurs de Shapley)."""
    sv = EXPLAINER.shap_values(X)
    return sv[1] if isinstance(sv, list) else (sv[:, :, 1] if sv.ndim == 3 else sv)


def latest_rows():
    """Variables de chaque equipement pour le DERNIER jour de mesure (recalculees si le cache a plus de 60 s)."""
    if _cache["df"] is not None and time.time() - _cache["t"] < CACHE_SECONDS:
        return _cache["df"], _cache["names"]
    machines, actions, maint, mes = load_tables()
    last = pd.to_datetime(mes["date"]).max()
    df = bd.finalize(bd.build(machines[["machine_id", "ref", "date_installation"]], actions, maint, mes, cutoff=last))
    df = df.sort_values("date").groupby("machine_id").tail(1).set_index("machine_id")
    contrib = pd.DataFrame(contributions(df[FEATURES].astype(float).values), index=df.index, columns=FEATURES)
    _cache.update(t=time.time(), df=df, contrib=contrib, names=dict(zip(machines["machine_id"], machines["nom"])),
                  costs=load_costs().groupby(["machine_id", "code"])["cout"].agg(["mean", "count"]))
    return df, _cache["names"]


def history_rows():
    """Score de chaque equipement pour chaque jour de la PERIODE DE TEST (jamais vue a l'entrainement),
    calcule avec les variables connues ce jour-la, + dates des vraies pannes."""
    if _hist["df"] is not None and time.time() - _hist["t"] < CACHE_SECONDS:
        return _hist["df"], _hist["fail"]
    machines, actions, maint, mes = load_tables()
    last = pd.to_datetime(mes["date"]).max()
    df = bd.finalize(bd.build(machines[["machine_id", "ref", "date_installation"]], actions, maint, mes, cutoff=last))
    df = df[(df["date"] >= pd.Timestamp(META["test_depuis"])) & (~df["en_panne"])].copy()
    df["score"] = MODEL.predict_proba(df[FEATURES].astype(float).values)[:, 1]
    pan = actions[actions["code"] == "PANNE"].copy()
    pan["jour"] = pd.to_datetime(pan["date_debut"]).dt.normalize()
    fail = {int(m): sorted(g["jour"].dt.strftime("%Y-%m-%d")) for m, g in pan.groupby("machine_id")}
    _hist.update(t=time.time(), df=df, fail=fail)
    return df, fail


def level(score):
    if score >= THRESHOLD:
        return "CRITIQUE", "red", "Inspection recommandee dans les 48 h"
    if score >= THRESHOLD / 2:
        return "ATTENTION", "orange", "Planifier une inspection cette semaine"
    return "NORMAL", "green", "Continuer la surveillance habituelle"


def economics(mid):
    """Cout moyen d'une panne (CORRECTIF) vs d'une visite (PREVENTIF) pour l'equipement."""
    costs = _cache["costs"]

    def stat(code):
        return costs.loc[(mid, code)] if (mid, code) in costs.index else None

    panne, visite = stat("CORRECTIF"), stat("PREVENTIF")
    ok = panne is not None and visite is not None and panne["mean"] != 0
    return dict(
        cout_panne_moyen=None if panne is None else int(round(panne["mean"])),
        nb_pannes=0 if panne is None else int(panne["count"]),
        cout_visite_moyen=None if visite is None else int(round(visite["mean"])),
        nb_visites=0 if visite is None else int(visite["count"]),
        seuil_rentabilite_pct=round(100 * float(visite["mean"]) / float(panne["mean"]), 1) if ok else None,
    )


def predict_row(mid, row, name):
    score = float(MODEL.predict_proba(row[FEATURES].astype(float).values.reshape(1, -1))[0][1])
    lvl, color, reco = level(score)
    c = _cache["contrib"].loc[mid]
    ranked = [f for f in c.abs().sort_values(ascending=False).index if not f.startswith("eq_")][:4]
    return dict(
        machine_id=int(mid), nom=name, probability=round(100 * score, 1), risk_level=lvl, risk_color=color,
        recommendation=reco, model_accuracy=round(100 * META["precision_test"], 1),
        features={f: float(row[f]) for f in FEATURES if not f.startswith("eq_")},
        top_factors=[dict(name=f, value=round(float(row[f]), 2), contribution=round(100 * float(c[f]), 1),
                          importance=round(abs(100 * float(c[f])), 1),
                          effect="hausse" if c[f] > 0 else "baisse") for f in ranked],
        as_of=str(row["date"].date()),
        economics=economics(mid),
        model_metrics=dict(pr_auc=META["pr_auc_test"], f1=META["f1_test"], precision=META["precision_test"],
                           recall=META["recall_test"], seuil=round(THRESHOLD, 3)),
    )


@app.route("/health")
def health():
    return jsonify(status="ok", model=f"{META['modele']} ({META['jeu_de_variables']})",
                   accuracy=round(100 * META["precision_test"], 1), pr_auc_test=META["pr_auc_test"])


@app.route("/predict/all")
def predict_all():
    try:
        df, names = latest_rows()
        out = [predict_row(mid, r, names[mid]) for mid, r in df.iterrows()]
        return jsonify(sorted(out, key=lambda x: -x["probability"]))
    except Exception as e:
        return jsonify(error=str(e)), 500


@app.route("/predict/<int:machine_id>")
def predict(machine_id):
    try:
        df, names = latest_rows()
        if machine_id not in df.index:
            return jsonify(error=f"Equipement {machine_id} introuvable"), 404
        return jsonify(predict_row(machine_id, df.loc[machine_id], names[machine_id]))
    except Exception as e:
        return jsonify(error=str(e)), 500


@app.route("/history/<int:machine_id>")
def history(machine_id):
    try:
        df, fail = history_rows()
        d = df[df["machine_id"] == machine_id].sort_values("date")
        if d.empty:
            return jsonify(error=f"Equipement {machine_id} introuvable"), 404
        return jsonify(machine_id=machine_id, seuil=round(THRESHOLD, 3), test_depuis=META["test_depuis"],
                       points=[dict(d=t.strftime("%Y-%m-%d"), s=round(100 * v, 1)) for t, v in zip(d["date"], d["score"])],
                       pannes=fail.get(machine_id, []))
    except Exception as e:
        return jsonify(error=str(e)), 500


if __name__ == "__main__":
    import getpass
    PASSWORD = os.environ.get("DB_PASSWORD") or getpass.getpass("Mot de passe MySQL (root) : ")
    print(f"Modele charge : {META['modele']} ({META['jeu_de_variables']})  PR-AUC test = {META['pr_auc_test']}")
    print("API ML sur http://localhost:5000  (/health, /predict/<id>, /predict/all)")
    app.run(host="0.0.0.0", port=5000, debug=False)
