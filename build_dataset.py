#!/usr/bin/env python3
"""
build_dataset.py
Construit le jeu de donnees du modele : UNE LIGNE = UN EQUIPEMENT UN JOUR DONNE.

Cible  : panne_7j = 1 si une panne DEMARRE entre demain et dans 7 jours.
Regle d'or : chaque variable n'utilise que des informations connues a la fin du jour J
(aucune information du futur). Un test anti-fuite le verifie a la fin du script.

Usage : python3 build_dataset.py
Sortie : dataset_panne_7j.csv  +  dataset_meta.json  (familles de variables)
Necessite : generate_cafe_data.py dans le meme dossier (liste des equipements).
"""
import json
import random
import numpy as np
import pandas as pd
from generate_cafe_data import EQUIP

WARMUP = 90          # jours ignores au debut (les fenetres de 90 j ne sont pas pleines)
HORIZON = 7          # on predit une panne dans les 7 jours
SENSORS = ["temperature_interne", "vibration", "pression", "temps_cycle"]
SHORT = {"temperature_interne": "temp", "vibration": "vib", "pression": "pres", "temps_cycle": "cycle"}
BY_REF = {e["ref"]: e for e in EQUIP}


def load_from_mysql():
    import getpass
    import mysql.connector
    conn = mysql.connector.connect(host="localhost", user="root",
                                   password=getpass.getpass("Mot de passe MySQL (root) : "),
                                   database="CanalMaDashDB")
    cur = conn.cursor()

    def q(sql, cols):
        cur.execute(sql)
        return pd.DataFrame(cur.fetchall(), columns=cols)

    machines = q("SELECT id, ref, date_installation FROM machine", ["machine_id", "ref", "date_installation"])
    actions = q("SELECT a.machine_id, t.code, a.date_debut, a.date_fin FROM action a "
                "JOIN type_action t ON t.id=a.type_action_id", ["machine_id", "code", "date_debut", "date_fin"])
    maint = q("SELECT m.machine_id, t.code, m.date_debut FROM maintenance m "
              "JOIN type_maintenance t ON t.id=m.type_id", ["machine_id", "code", "date_debut"])
    mes = q("SELECT date_mesure, machine_id, temperature_interne, vibration, pression, temps_cycle, "
            "temperature_ambiante, durete_eau, intensite_utilisation, bruit_signale FROM mesures_capteurs "
            "ORDER BY machine_id, date_mesure",
            ["date", "machine_id", *SENSORS, "amb", "durete", "intensite", "bruit"])
    conn.close()
    return machines, actions, maint, mes


def build(machines, actions, maint, mes, cutoff=None):
    """cutoff=None : jeu complet avec cible. cutoff=date : donnees tronquees a cette date, sans cible
    (sert au test anti-fuite)."""
    mes = mes.copy()
    mes["date"] = pd.to_datetime(mes["date"])
    actions = actions.copy()
    maint = maint.copy()
    actions["date_debut"] = pd.to_datetime(actions["date_debut"])
    actions["date_fin"] = pd.to_datetime(actions["date_fin"])
    maint["date_debut"] = pd.to_datetime(maint["date_debut"])
    if cutoff is not None:
        cutoff = pd.Timestamp(cutoff)
        mes = mes[mes["date"] <= cutoff]
        actions = actions[actions["date_debut"].dt.normalize() <= cutoff]
        maint = maint[maint["date_debut"].dt.normalize() <= cutoff]
    last_day = mes["date"].max()
    out = []
    for _, m in machines.iterrows():
        mid, ref = m["machine_id"], m["ref"]
        eq = BY_REF[ref]
        d = mes[mes["machine_id"] == mid].sort_values("date").set_index("date")
        days = d.index
        # --- historique ---
        pan = actions[(actions["machine_id"] == mid) & (actions["code"] == "PANNE")]
        fail_days = pan["date_debut"].dt.normalize().sort_values().tolist()
        mdays = maint[maint["machine_id"] == mid]["date_debut"].dt.normalize().sort_values().tolist()
        f = pd.DataFrame(index=days)
        f["machine_id"], f["ref"], f["equip"] = mid, ref, eq["key"]
        inst = pd.Timestamp(m["date_installation"])
        f["age_mois"] = ((days - inst).days / 30.4).round(1)
        last_m = pd.Series(pd.NaT, index=days, dtype="datetime64[ns]")
        for t in mdays:
            last_m[days >= t] = t
        f["jours_depuis_maintenance"] = (pd.Series(days, index=days) - last_m).dt.days
        f["retard_maintenance_ratio"] = (f["jours_depuis_maintenance"] / eq["interval"]).round(3)
        fd = pd.Series(fail_days, dtype="datetime64[ns]")
        f["pannes_30j"] = [int(((fd > t - pd.Timedelta(days=30)) & (fd <= t)).sum()) for t in days]
        f["pannes_90j"] = [int(((fd > t - pd.Timedelta(days=90)) & (fd <= t)).sum()) for t in days]
        lastf = pd.Series(pd.NaT, index=days, dtype="datetime64[ns]")
        for t in fail_days:
            lastf[days >= t] = t
        f["jours_depuis_derniere_panne"] = (pd.Series(days, index=days) - lastf).dt.days.fillna(180).clip(upper=180)
        # --- contexte (moyennes sur 7 jours) ---
        f["amb_7j"] = d["amb"].rolling(7).mean().round(2)
        f["intensite_7j"] = d["intensite"].rolling(7).mean().round(2)
        f["durete_7j"] = d["durete"].rolling(7).mean().round(2)
        # --- capteurs : ecart a la normale de l'equipement et pente sur 7 jours ---
        w = np.arange(7) - 3.0
        for s in SENSORS:
            k = SHORT[s]
            if d[s].notna().any():
                base = d[s].iloc[:60]
                med, sd = base.median(), base.std() or 1.0
                f[f"z_{k}"] = ((d[s].rolling(7).mean() - med) / sd).round(3)
                f[f"pente_{k}"] = (d[s].rolling(7).apply(lambda v: np.dot(w, v) / 28.0, raw=True) / sd).round(3)
            else:
                f[f"z_{k}"] = 0.0
                f[f"pente_{k}"] = 0.0
        f["bruit_7j"] = d["bruit"].rolling(7).sum()
        # --- jours ou l'equipement est en panne : pas de prediction possible ---
        down = pd.Series(False, index=days)
        for _, a in pan.iterrows():
            end = a["date_fin"] if pd.notna(a["date_fin"]) else last_day + pd.Timedelta(days=1)
            down |= (days + pd.Timedelta(days=1) > a["date_debut"]) & (days <= end)
        f["en_panne"] = down
        # --- cible ---
        if cutoff is None:
            fa = np.array(fail_days, dtype="datetime64[ns]")
            f["panne_7j"] = [int(((fa > t.to_datetime64()) & (fa <= (t + pd.Timedelta(days=HORIZON)).to_datetime64())).any())
                             for t in days]
        f["jour_index"] = np.arange(len(days))
        out.append(f.reset_index(names="date"))
    df = pd.concat(out, ignore_index=True)
    if cutoff is None:
        df = df[(df["jour_index"] >= WARMUP) & (df["date"] <= last_day - pd.Timedelta(days=HORIZON))]
        df = df[~df["en_panne"]]
    return df.drop(columns=["jour_index"]).reset_index(drop=True)


def families():
    eqcols = [f"eq_{e['key']}" for e in EQUIP]
    hist = ["age_mois", "jours_depuis_maintenance", "retard_maintenance_ratio",
            "pannes_30j", "pannes_90j", "jours_depuis_derniere_panne"] + eqcols
    ctx = ["amb_7j", "intensite_7j", "durete_7j"]
    sens = [f"z_{SHORT[s]}" for s in SENSORS] + [f"pente_{SHORT[s]}" for s in SENSORS] + ["bruit_7j"]
    return hist, ctx, sens


def finalize(df):
    for e in EQUIP:
        df[f"eq_{e['key']}"] = (df["equip"] == e["key"]).astype(int)
    return df


def leak_check(machines, actions, maint, mes, full, n=6):
    """Pour quelques lignes au hasard : on refait le calcul avec des donnees COUPEES au jour J.
    Si les variables sont identiques, aucune information du futur n'est utilisee."""
    hist, ctx, sens = families()
    cols = hist + ctx + sens
    rnd = random.Random(0)
    bad = 0
    for idx in rnd.sample(range(len(full)), n):
        row = full.iloc[idx]
        cut = build(machines, actions, maint, mes, cutoff=row["date"])
        cut = finalize(cut)
        r = cut[(cut["machine_id"] == row["machine_id"]) & (cut["date"] == row["date"])]
        if r.empty:
            continue
        a, b = r.iloc[0][cols].astype(float).values, row[cols].astype(float).values
        if not np.allclose(a, b, atol=1e-6, equal_nan=True):
            bad += 1
            print("  DIFFERENCE :", row["ref"], row["date"].date(),
                  [c for c, x, y in zip(cols, a, b) if not np.isclose(x, y, atol=1e-6, equal_nan=True)])
    return bad


def run(machines, actions, maint, mes):
    df = finalize(build(machines, actions, maint, mes))
    hist, ctx, sens = families()
    print(f"Lignes : {len(df)}   Periode : {df['date'].min().date()} -> {df['date'].max().date()}")
    print(f"Taux de jours positifs : {100 * df['panne_7j'].mean():.1f} %  "
          f"({int(df['panne_7j'].sum())} positifs)")
    print("\nPositifs par equipement :")
    print(df.groupby("equip")["panne_7j"].agg(["sum", "count"]).rename(columns={"sum": "positifs", "count": "lignes"}).to_string())
    nan = df[hist + ctx + sens].isna().sum()
    print("\nValeurs manquantes :", "aucune" if nan.sum() == 0 else nan[nan > 0].to_dict())
    print("\nTest anti-fuite (6 lignes recalculees avec des donnees coupees au jour J) ...")
    bad = leak_check(machines, actions, maint, mes, df)
    print("  OK : aucune information du futur utilisee" if bad == 0 else f"  ATTENTION : {bad} ligne(s) differente(s)")
    df.to_csv("dataset_panne_7j.csv", index=False)
    json.dump({"historique": hist, "contexte": ctx, "capteurs": sens, "cible": "panne_7j",
               "warmup_jours": WARMUP, "horizon_jours": HORIZON}, open("dataset_meta.json", "w"), indent=2)
    print("\nFichiers ecrits : dataset_panne_7j.csv, dataset_meta.json")
    return df


if __name__ == "__main__":
    run(*load_from_mysql())
