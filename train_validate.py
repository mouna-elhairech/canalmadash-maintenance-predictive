#!/usr/bin/env python3
"""
train_validate.py
Entraine et VALIDE le modele de prediction de panne a 7 jours.

Protocole (a presenter tel quel au jury) :
  1. Decoupage temporel : test = 6 derniers mois, jamais utilise pour regler quoi que ce soit.
     Marge de 7 jours entre entrainement et test (les etiquettes ne se chevauchent pas).
  2. Reglage du seuil d'alerte par validation temporelle INTERNE a l'entrainement
     (fenetre qui s'agrandit, 4 plis), en maximisant le F2 (le rappel compte 2x plus que la precision :
     une panne manquee coute plus cher qu'une inspection inutile).
  3. Comparaison : A jamais de panne | B regle simple | C historique | D + contexte | E + capteurs,
     avec Random Forest et Gradient Boosting.
  4. Incertitude : intervalle de confiance a 95 % par bootstrap par semaines.

Usage : python3 train_validate.py        (necessite dataset_panne_7j.csv et dataset_meta.json)
Sorties : resultats_validation.csv, model_v2.pkl, model_v2_meta.json  (l'ancien model.pkl n'est pas touche)
"""
import json
import pickle
import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestClassifier, HistGradientBoostingClassifier
from sklearn.metrics import (average_precision_score, precision_recall_curve, precision_score,
                             recall_score, f1_score, fbeta_score, confusion_matrix)

SEED = 42
TEST_DAYS = 182      # 6 mois de test
GAP = 7              # marge = horizon de prediction
BETA = 2             # F2 pour choisir le seuil
N_FOLDS = 4
N_BOOT = 400

df = pd.read_csv("dataset_panne_7j.csv", parse_dates=["date"])
meta = json.load(open("dataset_meta.json"))
HIST, CTX, SENS = meta["historique"], meta["contexte"], meta["capteurs"]
SETS = {"C historique": HIST, "D +contexte": HIST + CTX, "E +capteurs": HIST + CTX + SENS}


def make(kind):
    if kind == "RF":
        return RandomForestClassifier(n_estimators=300, min_samples_leaf=5, class_weight="balanced_subsample",
                                      n_jobs=-1, random_state=SEED)
    return HistGradientBoostingClassifier(max_iter=200, learning_rate=0.05, max_depth=4,
                                          class_weight="balanced", random_state=SEED)


def best_threshold(y, s):
    p, r, t = precision_recall_curve(y, s)
    f = (1 + BETA ** 2) * p[:-1] * r[:-1] / np.maximum(BETA ** 2 * p[:-1] + r[:-1], 1e-12)
    return float(t[int(np.argmax(f))])


def oof_scores(X, y, dates, kind):
    """Validation temporelle interne : fenetre d'entrainement qui s'agrandit, marge de 7 jours."""
    days = np.sort(dates.unique())
    blocks = np.array_split(days, N_FOLDS + 1)
    oof_y, oof_s = [], []
    for k in range(1, N_FOLDS + 1):
        v_start = blocks[k][0]
        tr = dates <= v_start - np.timedelta64(GAP + 1, "D")
        va = dates.isin(blocks[k])
        m = make(kind).fit(X[tr], y[tr])
        oof_s.append(m.predict_proba(X[va])[:, 1])
        oof_y.append(y[va])
    return np.concatenate(oof_y), np.concatenate(oof_s)


def week_ci(y, s, weeks, n=N_BOOT, seed=SEED):
    rng = np.random.default_rng(seed)
    uw = np.unique(weeks)
    idx = {w: np.where(weeks == w)[0] for w in uw}
    vals = []
    for _ in range(n):
        pick = np.concatenate([idx[w] for w in rng.choice(uw, len(uw))])
        if y[pick].sum() > 0:
            vals.append(average_precision_score(y[pick], s[pick]))
    return np.percentile(vals, [2.5, 97.5])


def at_threshold(y, s, thr, n_weeks):
    pred = (s >= thr).astype(int)
    tn, fp, fn, tp = confusion_matrix(y, pred, labels=[0, 1]).ravel()
    return dict(precision=precision_score(y, pred, zero_division=0), recall=recall_score(y, pred),
                f1=f1_score(y, pred), tp=tp, fp=fp, fn=fn, tn=tn, alertes_par_semaine=pred.sum() / n_weeks)


# ---------------------------------------------------------------- decoupage
last = df["date"].max()
test_start = last - pd.Timedelta(days=TEST_DAYS - 1)
train = df[df["date"] <= test_start - pd.Timedelta(days=GAP + 1)].reset_index(drop=True)
test = df[df["date"] >= test_start].reset_index(drop=True)
y_tr, y_te = train["panne_7j"].values, test["panne_7j"].values
weeks_te = test["date"].dt.isocalendar().week.values + 100 * test["date"].dt.isocalendar().year.values
n_weeks = test["date"].nunique() / 7
print(f"ENTRAINEMENT : {train['date'].min().date()} -> {train['date'].max().date()}  "
      f"({len(train)} lignes, {y_tr.sum()} positifs, {100 * y_tr.mean():.1f} %)")
print(f"TEST         : {test['date'].min().date()} -> {test['date'].max().date()}  "
      f"({len(test)} lignes, {y_te.sum()} positifs, {100 * y_te.mean():.1f} %)")
print(f"(marge de {GAP} jours entre les deux)\n")

rows = []
# ---------------------------------------------------------------- A et B : references
chance = y_te.mean()
rows.append(dict(modele="A jamais de panne", pr_auc=chance, ic_bas=np.nan, ic_haut=np.nan, seuil=np.nan,
                 precision=0.0, recall=0.0, f1=0.0, tp=0, fp=0, fn=int(y_te.sum()), tn=int((1 - y_te).sum()),
                 alertes_par_semaine=0.0))
sB = test["pannes_30j"].values.astype(float)
lo, hi = week_ci(y_te, sB, weeks_te)
rows.append(dict(modele="B regle : >=2 pannes sur 30 j", pr_auc=average_precision_score(y_te, sB), ic_bas=lo, ic_haut=hi,
                 seuil=2, **at_threshold(y_te, sB, 2, n_weeks)))

# ---------------------------------------------------------------- C, D, E
cv_ap, fitted = {}, {}
for set_name, cols in SETS.items():
    X_tr, X_te = train[cols].values, test[cols].values
    for kind in ("RF", "HGB"):
        yo, so = oof_scores(X_tr, y_tr, train["date"], kind)       # reglage du seuil SANS toucher au test
        thr = best_threshold(yo, so)
        cv_ap[(set_name, kind)] = average_precision_score(yo, so)
        model = make(kind).fit(X_tr, y_tr)
        s = model.predict_proba(X_te)[:, 1]
        lo, hi = week_ci(y_te, s, weeks_te)
        rows.append(dict(modele=f"{set_name} ({kind})", pr_auc=average_precision_score(y_te, s), ic_bas=lo, ic_haut=hi,
                         seuil=thr, **at_threshold(y_te, s, thr, n_weeks)))
        fitted[(set_name, kind)] = (model, thr, cols)

res = pd.DataFrame(rows)
pd.set_option("display.width", 200)
show = res[["modele", "pr_auc", "ic_bas", "ic_haut", "precision", "recall", "f1", "alertes_par_semaine"]].round(3)
print("RESULTATS SUR LE TEST (6 derniers mois)")
print(f"Niveau du hasard pour la PR-AUC = taux de positifs = {chance:.3f}\n")
print(show.to_string(index=False))
res.round(4).to_csv("resultats_validation.csv", index=False)

# ---------------------------------------------------------------- modele retenu (choisi SANS le test)
cands = {k: v for k, v in cv_ap.items() if k[0] == "E +capteurs"}
best = max(cands, key=cands.get)
model, thr, cols = fitted[best]
print(f"\nModele retenu (meilleure PR-AUC en validation interne) : {best[0]} {best[1]}  seuil = {thr:.3f}")
if best[1] == "RF":
    imp = sorted(zip(cols, model.feature_importances_), key=lambda x: -x[1])[:8]
    print("Variables les plus utilisees :", ", ".join(f"{c} ({100 * v:.0f} %)" for c, v in imp))
final = res[res["modele"] == f"{best[0]} ({best[1]})"].iloc[0]
pickle.dump(dict(model=model, features=cols, threshold=thr), open("model_v2.pkl", "wb"))
json.dump(dict(modele=best[1], jeu_de_variables=best[0], seuil=thr, features=cols,
               train_jusqu_au=str(train["date"].max().date()), test_depuis=str(test["date"].min().date()),
               pr_auc_test=round(float(final["pr_auc"]), 3), f1_test=round(float(final["f1"]), 3),
               precision_test=round(float(final["precision"]), 3), recall_test=round(float(final["recall"]), 3)),
          open("model_v2_meta.json", "w"), indent=2)
print("Fichiers ecrits : resultats_validation.csv, model_v2.pkl, model_v2_meta.json")
