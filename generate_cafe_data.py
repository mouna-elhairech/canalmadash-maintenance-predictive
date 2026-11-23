#!/usr/bin/env python3
"""
generate_cafe_data.py
Simule 24 mois d'exploitation d'un cafe-restaurant (12 equipements) et ecrit
les resultats dans la base CanalMaDashDB :
  - tables de l'application : machine, action, maintenance, demande_maintenance
  - table a part : mesures_capteurs (mesures journalieres, utilisee par le ML)

Usage :
  python3 generate_cafe_data.py --dry   -> statistiques seulement, rien n'est ecrit
  python3 generate_cafe_data.py         -> ecrit dans MySQL (apres confirmation)
"""
import sys
import math
import random
from datetime import datetime, timedelta

SEED = 42
N_DAYS = 730              # 24 mois
NEGLECT_P = 0.0022        # proba par jour de demarrer une periode de negligence
NEGLECT_LEN = (30, 80)    # duree (jours) d'une periode de negligence
DB_NAME = "CanalMaDashDB"

# Chaque equipement : taux de panne de base (pannes/an), intervalle de maintenance
# preventive (jours), groupe de regle, causes (la 1ere = cause liee a la regle),
# capteurs : nom -> (moyenne, ecart-type, derive avant panne).
EQUIP = [
 dict(key="cafe", nom="Machine a cafe espresso", ref="CAFE-01", lieu="Bar", crit="CRITIQUE",
      base=4, interval=30, grp="scale", age=(6, 48), achat=45000, repair=(4, 24), cost=(800, 2500),
      causes=[("Entartrage (calcaire)", 5), ("Fuite de pression", 3), ("Usure des joints de groupe", 3)],
      prev="Detartrage de la chaudiere", prev_cost=(150, 400), prev_hours=(1, 2.5),
      sensors=dict(temperature_interne=(93, 0.8, -5), pression=(9, 0.25, -2.5),
                   temps_cycle=(27, 1.2, 8), vibration=(1.0, 0.12, 0.9))),
 dict(key="moulin", nom="Moulin a cafe", ref="MOUL-01", lieu="Bar", crit="HAUTE",
      base=2, interval=30, grp="wear", age=(6, 48), achat=12000, repair=(2, 8), cost=(400, 1200),
      causes=[("Usure des meules", 4), ("Surchauffe du moteur", 3)],
      prev="Nettoyage et reglage des meules", prev_cost=(50, 200), prev_hours=(0.5, 1.5),
      sensors=dict(temperature_interne=(45, 3, 22), vibration=(2.0, 0.25, 3.0), temps_cycle=(12, 0.8, 5))),
 dict(key="chambre", nom="Chambre froide", ref="CHFR-01", lieu="Reserve", crit="CRITIQUE",
      base=1.5, interval=30, grp="heat", age=(12, 60), achat=60000, repair=(6, 48), cost=(1500, 6000),
      causes=[("Condenseur encrasse", 5), ("Fuite de gaz frigorigene", 2), ("Joints de porte uses", 2)],
      prev="Nettoyage de la grille du condenseur", prev_cost=(100, 300), prev_hours=(1, 2),
      sensors=dict(temperature_interne=(3, 0.4, 4), vibration=(1.2, 0.15, 1.8), temps_cycle=(45, 3, 28))),
 dict(key="vitrine", nom="Vitrine refrigeree", ref="VITR-01", lieu="Salle", crit="HAUTE",
      base=3, interval=14, grp="heat", age=(6, 48), achat=25000, repair=(3, 24), cost=(500, 2000),
      causes=[("Grille de condenseur encrassee", 4), ("Givre excessif", 3), ("Ventilateur en panne", 2),
              ("Sonde de temperature HS", 2)],
      prev="Degivrage et nettoyage des filtres", prev_cost=(50, 150), prev_hours=(0.5, 1.5),
      sensors=dict(temperature_interne=(4, 0.5, 4), vibration=(0.8, 0.1, 1.2), temps_cycle=(50, 3, 22))),
 dict(key="glacons", nom="Machine a glacons", ref="GLAC-01", lieu="Cuisine", crit="NORMALE",
      base=3, interval=90, grp="scale", age=(6, 36), achat=20000, repair=(4, 24), cost=(600, 2000),
      causes=[("Filtre bouche (calcaire)", 5), ("Panne du cycle de demoulage", 2)],
      prev="Desinfection et changement du filtre", prev_cost=(150, 450), prev_hours=(1, 2),
      sensors=dict(temperature_interne=(2, 0.4, 4), pression=(3, 0.15, -1.1), temps_cycle=(22, 1.2, 9))),
 dict(key="lavevaisselle", nom="Lave-vaisselle pro", ref="LAVE-01", lieu="Plonge", crit="HAUTE",
      base=4, interval=30, grp="scale", age=(6, 48), achat=35000, repair=(4, 24), cost=(700, 2500),
      causes=[("Resistance entartree", 5), ("Filtre bouche", 2), ("Fuite", 2)],
      prev="Detartrage du lave-vaisselle", prev_cost=(100, 300), prev_hours=(1, 2),
      sensors=dict(temperature_interne=(62, 0.8, -8), pression=(2.5, 0.12, -0.8), temps_cycle=(120, 5, 35))),
 dict(key="four", nom="Four mixte", ref="FOUR-01", lieu="Cuisine", crit="HAUTE",
      base=2, interval=30, grp=None, age=(12, 60), achat=70000, repair=(4, 24), cost=(800, 3000),
      causes=[("Thermostat defectueux", 4), ("Moteur du ventilateur grille", 3)],
      prev="Nettoyage profond et recalibrage", prev_cost=(100, 350), prev_hours=(1, 3),
      sensors=dict(temperature_interne=(200, 2.5, 14), vibration=(1.5, 0.15, 2.2), temps_cycle=(12, 0.8, 5))),
 dict(key="friteuse", nom="Friteuse electrique", ref="FRIT-01", lieu="Cuisine", crit="NORMALE",
      base=3, interval=90, grp=None, age=(6, 48), achat=8000, repair=(2, 12), cost=(400, 1500),
      causes=[("Thermostat de securite HS", 4), ("Resistances encrassees", 3)],
      prev="Revision et vidange complete", prev_cost=(100, 300), prev_hours=(1, 2),
      sensors=dict(temperature_interne=(175, 2, 18), temps_cycle=(9, 0.6, 4.5))),
 dict(key="clim", nom="Climatisation", ref="CLIM-01", lieu="Salle", crit="NORMALE",
      base=1.5, interval=30, grp="heat", age=(12, 72), achat=30000, repair=(6, 48), cost=(1000, 4000),
      causes=[("Filtres satures", 5), ("Fuite de fluide frigorigene", 2)],
      prev="Nettoyage des filtres", prev_cost=(50, 150), prev_hours=(0.5, 1.5),
      sensors=dict(temperature_interne=(22, 0.7, 4.5), vibration=(1.5, 0.15, 2.0), pression=(18, 0.5, -3.5))),
 dict(key="adoucisseur", nom="Adoucisseur d'eau", ref="ADOU-01", lieu="Local technique", crit="NORMALE",
      base=1, interval=14, grp=None, age=(6, 48), achat=9000, repair=(1, 8), cost=(200, 1000),
      causes=[("Manque de sel / resine saturee", 5), ("Vanne de regeneration bloquee", 1)],
      prev="Rechargement en sel et test de durete", prev_cost=(40, 120), prev_hours=(0.3, 1),
      sensors=dict(pression=(3.5, 0.15, -1.0), temps_cycle=(90, 4, 25))),
 dict(key="jus", nom="Machine a jus / presse-agrumes", ref="JUS-01", lieu="Bar", crit="NORMALE",
      base=3.5, interval=90, grp="wear", age=(6, 36), achat=7000, repair=(2, 12), cost=(400, 1500),
      causes=[("Usure des roulements", 4), ("Moteur en surchauffe", 3), ("Joints uses", 2)],
      prev="Controle des joints et lubrification", prev_cost=(50, 150), prev_hours=(0.5, 1.5),
      sensors=dict(temperature_interne=(40, 2.5, 18), vibration=(1.8, 0.2, 2.8))),
 dict(key="blender", nom="Blender professionnel", ref="BLEN-01", lieu="Bar", crit="NORMALE",
      base=3.5, interval=60, grp="wear", age=(6, 36), achat=6000, repair=(2, 8), cost=(300, 1200),
      causes=[("Infiltration de liquide dans le moteur", 3), ("Roulements grippes", 4), ("Lames usees", 3)],
      prev="Inspection des joints du bol et des lames", prev_cost=(50, 200), prev_hours=(0.3, 1),
      sensors=dict(temperature_interne=(45, 3, 22), vibration=(2.2, 0.25, 3.2))),
]
SENSOR_COLS = ["temperature_interne", "vibration", "pression", "temps_cycle"]
FOURNISSEURS = ["Frigoriste Atlas", "Technicien Cafe Maroc", "SAV Equipements Pro", "Depannage Casa Froid"]


def simulate():
    rnd = random.Random(SEED)
    today = datetime.now().replace(hour=0, minute=0, second=0, microsecond=0)
    start = today - timedelta(days=N_DAYS)
    st = [dict(days_since=rnd.randint(0, e["interval"]), neglect_until=-1, down_until=start,
               age0=rnd.randint(*e["age"]), events=[]) for e in EQUIP]
    ad = next(i for i, e in enumerate(EQUIP) if e["key"] == "adoucisseur")
    order = [ad] + [i for i in range(len(EQUIP)) if i != ad]   # l'adoucisseur d'abord (effet domino)
    ambs, intens, dureté = [], [], []
    hard = False
    for d in range(N_DAYS):
        day = start + timedelta(days=d)
        doy = day.timetuple().tm_yday
        amb = 27 + 7 * math.sin(2 * math.pi * (doy - 110) / 365) + rnd.gauss(0, 1.2)
        weekend = day.weekday() >= 5
        summer = amb > 30
        w = [max(0.05, 0.35 - 0.2 * weekend - 0.1 * summer), 0.4, 0.25 + 0.3 * weekend + 0.15 * summer]
        inten = rnd.choices([1, 2, 3], weights=w)[0]
        ambs.append(amb)
        intens.append(inten)
        for i in order:
            e, s = EQUIP[i], st[i]
            s["days_since"] += 1
            if d >= s["neglect_until"] and rnd.random() < NEGLECT_P:
                s["neglect_until"] = d + rnd.randint(*NEGLECT_LEN)
            neglect = d < s["neglect_until"]
            # --- maintenance preventive ---
            if s["days_since"] >= e["interval"] and not neglect and rnd.random() < 0.35:
                t0 = day + timedelta(hours=6, minutes=rnd.randint(0, 30))
                if t0 >= s["down_until"]:
                    t1 = t0 + timedelta(hours=rnd.uniform(*e["prev_hours"]))
                    s["events"].append(dict(kind="PREV", start=t0, end=t1, desc=e["prev"],
                                            cost=round(rnd.uniform(*e["prev_cost"]), 2), cause=None,
                                            ext=False, day=d, prec=None))
                    s["down_until"] = t1
                    s["days_since"] = 0
            if i == ad:
                hard = s["days_since"] > 28
                dureté.append(8 + 0.8 * max(0, s["days_since"] - 14) + rnd.gauss(0, 0.8))
            # --- pannes ---
            if d <= N_DAYS - 3:
                ds, g = s["days_since"], e["grp"]
                r = ds / e["interval"]
                mult, flag = 1.0, False
                if g == "scale":
                    if ds > 1.5 * e["interval"]: mult *= 3; flag = True   # 45 j pour un intervalle de 30 j
                    if hard: mult *= 2; flag = True
                elif g == "heat":
                    if amb > 30 and ds > 2 * e["interval"]: mult *= 5; flag = True   # 60 j pour un intervalle de 30 j
                elif g == "wear":
                    if inten == 3 and r > 1: mult *= 3; flag = True
                if r > 1:
                    mult *= math.exp(1.2 * (r - 1))
                mult *= 1 + (s["age0"] + d / 30.4) / 120
                mult = min(mult, 8.0)
                if rnd.random() < e["base"] / 365 * mult:
                    t0 = day + timedelta(hours=rnd.randint(7, 21), minutes=rnd.randint(0, 59))
                    if t0 >= s["down_until"]:
                        if flag and rnd.random() < 0.7:
                            cause = e["causes"][0][0]
                        else:
                            cause = rnd.choices([c[0] for c in e["causes"]], [c[1] for c in e["causes"]])[0]
                        lo, hi = e["repair"]
                        t1 = t0 + timedelta(hours=rnd.triangular(lo, hi, lo + 0.25 * (hi - lo)))
                        cost = round(rnd.uniform(*e["cost"]), 2)
                        prec = (rnd.randint(7, 14), rnd.uniform(0.7, 1.3)) if rnd.random() < 0.8 else None
                        s["events"].append(dict(kind="PANNE", start=t0, end=t1, cause=cause, cost=cost,
                                                desc=f"{e['nom']} : {cause}",
                                                ext=cost > 1800 and rnd.random() < 0.6, day=d, prec=prec))
                        s["down_until"] = t1
                        s["days_since"] = 0
    return dict(start=start, today=today, st=st, amb=ambs, inten=intens, durete=dureté, rnd=rnd)


def sensor_rows(sim):
    """Une ligne par equipement et par jour, avec derive avant panne (80 % des pannes)."""
    rnd = random.Random(SEED + 1)
    rows = []
    for i, e in enumerate(EQUIP):
        prog = {}
        for ev in sim["st"][i]["events"]:
            if ev["kind"] == "PANNE" and ev["prec"]:
                L, sc = ev["prec"]
                f = ev["day"]
                for d in range(max(0, f - L), f + 1):
                    p = (((d - (f - L)) / L) ** 1.5) * sc
                    prog[d] = max(prog.get(d, 0), p)
        for d in range(N_DAYS):
            day = (sim["start"] + timedelta(days=d)).date()
            p = prog.get(d, 0.0)
            vals = {}
            for name, (mu, sd, drift) in e["sensors"].items():
                v = mu + rnd.gauss(0, sd) + drift * p
                if name == "temperature_interne" and e["key"] in ("chambre", "vitrine", "glacons"):
                    v += 0.08 * (sim["amb"][d] - 27)
                if name == "vibration":
                    v += 0.15 * sd * (sim["inten"][d] - 2)
                vals[name] = v
            if vals and rnd.random() < 0.012:                        # fausse alerte isolee
                k = rnd.choice(list(vals))
                vals[k] += rnd.choice([-1, 1]) * rnd.uniform(3, 5) * e["sensors"][k][1]
            bruit = 1 if rnd.random() < 0.02 + 0.6 * min(p, 1.0) else 0
            rows.append((day, i, *[round(vals[c], 2) if c in vals else None for c in SENSOR_COLS],
                         round(sim["amb"][d], 1), round(sim["durete"][d], 1), sim["inten"][d], bruit))
    return rows


def actions_for(sim, i):
    segs, cur = [], sim["start"]
    for ev in sorted(sim["st"][i]["events"], key=lambda x: x["start"]):
        if ev["start"] > cur:
            segs.append(("MARCHE", cur, ev["start"]))
        segs.append(("PANNE" if ev["kind"] == "PANNE" else "MAINTENANCE", ev["start"], ev["end"]))
        cur = ev["end"]
    segs.append(("MARCHE", cur, None))
    return segs


def print_stats(sim):
    tot, pos, n = 0, 0, 0
    print(f"\n{'Equipement':34s} {'Pannes':>6s} {'Prev.':>6s} {'Dispo %':>8s}")
    causes = {}
    for i, e in enumerate(EQUIP):
        evs = sim["st"][i]["events"]
        pan = [x for x in evs if x["kind"] == "PANNE"]
        prv = [x for x in evs if x["kind"] == "PREV"]
        down = sum((x["end"] - x["start"]).total_seconds() for x in evs)
        disp = 100 * (1 - down / (N_DAYS * 86400))
        print(f"{e['nom']:34s} {len(pan):6d} {len(prv):6d} {disp:8.1f}")
        tot += len(pan)
        for x in pan:
            causes[x["cause"]] = causes.get(x["cause"], 0) + 1
        fdays = sorted(x["day"] for x in pan)
        for d in range(N_DAYS - 7):
            n += 1
            if any(d < f <= d + 7 for f in fdays):
                pos += 1
    print(f"\nTotal pannes : {tot}  (~{tot / 2:.0f} par an)")
    print(f"Jours positifs (panne dans les 7 jours) : {100 * pos / n:.1f} %")
    print("Causes les plus frequentes :")
    for c, k in sorted(causes.items(), key=lambda x: -x[1])[:6]:
        print(f"  {c:40s} {k}")


def write_db(sim):
    import getpass
    import mysql.connector
    pwd = getpass.getpass("Mot de passe MySQL (root) : ")
    conn = mysql.connector.connect(host="localhost", user="root", password=pwd, database=DB_NAME)
    cur = conn.cursor()

    # 1) tables a vider : machine + toutes les tables qui en dependent (directement ou non)
    todo, seen = ["machine"], []
    while todo:
        t = todo.pop()
        if t in seen:
            continue
        seen.append(t)
        cur.execute("SELECT TABLE_NAME FROM information_schema.KEY_COLUMN_USAGE "
                    "WHERE TABLE_SCHEMA=%s AND REFERENCED_TABLE_NAME=%s", (DB_NAME, t))
        todo += [r[0] for r in cur.fetchall()]
    print("\nTables qui seront VIDEES :")
    for t in seen:
        cur.execute(f"SELECT COUNT(*) FROM `{t}`")
        print(f"  {t:35s} {cur.fetchone()[0]} lignes")
    if input("\nTape OUI pour continuer (sauvegarde deja faite ?) : ").strip() != "OUI":
        print("Annule, rien n'a ete modifie.")
        return
    cur.execute("SET FOREIGN_KEY_CHECKS=0")
    for t in seen:
        cur.execute(f"TRUNCATE TABLE `{t}`")
    cur.execute("SET FOREIGN_KEY_CHECKS=1")

    # 2) types
    cur.execute("SELECT code, id FROM type_action")
    ta = dict(cur.fetchall())
    for need in ("MARCHE", "PANNE", "MAINTENANCE"):
        if need not in ta:
            raise SystemExit(f"type_action '{need}' introuvable : lance d'abord l'application une fois.")
    cur.execute("SELECT code, id FROM type_maintenance")
    tm = dict(cur.fetchall())
    for code, color, lib in (("PREVENTIF", "#16a34a", "Maintenance preventive"),
                             ("CORRECTIF", "#dc2626", "Maintenance corrective")):
        if code not in tm:
            cur.execute("INSERT INTO type_maintenance (actif, code, color, libelle) VALUES (%s,%s,%s,%s)",
                        (1, code, color, lib))
            tm[code] = cur.lastrowid

    # 3) machines + etats + interventions + demandes
    now = datetime.now()
    mids = []
    for i, e in enumerate(EQUIP):
        s = sim["st"][i]
        age = s["age0"] + N_DAYS / 30.4
        acts = actions_for(sim, i)
        last_start = acts[-1][1]
        cur.execute(
            "INSERT INTO machine (nom, ref, emplacement, criticite, heures_marche, cout_achat, "
            "date_installation, numero_serie, type_action_id, date_debut_action, created_at, updated_at) "
            "VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)",
            (e["nom"], e["ref"], e["lieu"], e["crit"], round(age * 30 * 14, 1), e["achat"],
             (sim["today"] - timedelta(days=int(age * 30.4))).date(), f"SN-{e['ref']}-{2000 + i}",
             ta["MARCHE"], last_start, sim["start"], now))
        mid = cur.lastrowid
        mids.append(mid)
        cur.executemany("INSERT INTO action (machine_id, type_action_id, date_debut, date_fin) VALUES (%s,%s,%s,%s)",
                        [(mid, ta[k], a, b) for k, a, b in acts])
        rnd = random.Random(SEED + 10 + i)
        for ev in s["events"]:
            ext = ev["ext"]
            if ev["kind"] == "PANNE":
                prio = "CRITIQUE" if e["crit"] == "CRITIQUE" and rnd.random() < 0.5 else (
                    "URGENTE" if e["crit"] != "NORMALE" or rnd.random() < 0.3 else "NORMALE")
                cur.execute(
                    "INSERT INTO demande_maintenance (date_demande, date_souhaitee, date_debut_reelle, "
                    "date_fin_reelle, demandeur, description, priorite, statut, machine_id, type_maintenance_id) "
                    "VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)",
                    (ev["start"], None, ev["start"] + timedelta(minutes=20), ev["end"], "OPERATEUR",
                     ev["desc"][:480], prio, "TERMINEE", mid, tm["CORRECTIF"]))
                did, type_id = cur.lastrowid, tm["CORRECTIF"]
            else:
                did, type_id = None, tm["PREVENTIF"]
            cur.execute(
                "INSERT INTO maintenance (cause_panne, cout, date_debut, date_fin, description, type_intervention, "
                "user, fournisseur_nom, controle_effectue, nettoyage_effectue, test_final_effectue, "
                "machine_id, type_id, demande_maintenance_id) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)",
                (ev["cause"], ev["cost"], ev["start"], ev["end"], ev["desc"][:250],
                 "EXTERNE" if ext else "INTERNE", "TECHNICIEN",
                 rnd.choice(FOURNISSEURS) if ext else None, 1, 1 if ev["kind"] == "PREV" else 0, 1,
                 mid, type_id, did))

    # 4) quelques demandes ouvertes pour la demo (Kanban / Dashboard)
    def open_demande(key, desc, statut, prio, wished):
        i = next(k for k, e in enumerate(EQUIP) if e["key"] == key)
        cur.execute(
            "INSERT INTO demande_maintenance (date_demande, date_souhaitee, demandeur, description, priorite, "
            "statut, machine_id, type_maintenance_id) VALUES (%s,%s,%s,%s,%s,%s,%s,%s)",
            (now - timedelta(days=1), wished, "OPERATEUR", desc, prio, statut, mids[i], tm["PREVENTIF"]))
    open_demande("cafe", "Detartrage a planifier", "EN_ATTENTE", "NORMALE", None)
    open_demande("lavevaisselle", "Detartrage planifie", "PLANIFIEE", "NORMALE", now + timedelta(days=2))
    open_demande("chambre", "Nettoyage du condenseur (en retard)", "PLANIFIEE", "URGENTE", now - timedelta(days=1))

    # 5) table des capteurs (hors application)
    cur.execute("DROP TABLE IF EXISTS mesures_capteurs")
    cur.execute(
        "CREATE TABLE mesures_capteurs (id BIGINT AUTO_INCREMENT PRIMARY KEY, date_mesure DATE NOT NULL, "
        "machine_id INT NOT NULL, temperature_interne FLOAT NULL, vibration FLOAT NULL, pression FLOAT NULL, "
        "temps_cycle FLOAT NULL, temperature_ambiante FLOAT, durete_eau FLOAT, intensite_utilisation TINYINT, "
        "bruit_signale TINYINT, UNIQUE KEY uq_mesure (date_mesure, machine_id), INDEX idx_machine (machine_id))")
    rows = [(r[0], mids[r[1]], *r[2:]) for r in sensor_rows(sim)]
    cur.executemany(
        "INSERT INTO mesures_capteurs (date_mesure, machine_id, temperature_interne, vibration, pression, "
        "temps_cycle, temperature_ambiante, durete_eau, intensite_utilisation, bruit_signale) "
        "VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)", rows)
    conn.commit()
    for t in ("machine", "action", "maintenance", "demande_maintenance", "mesures_capteurs"):
        cur.execute(f"SELECT COUNT(*) FROM `{t}`")
        print(f"  {t:22s} {cur.fetchone()[0]} lignes")
    cur.close()
    conn.close()
    print("\nTermine.")


if __name__ == "__main__":
    sim = simulate()
    print_stats(sim)
    if "--dry" not in sys.argv:
        write_db(sim)
