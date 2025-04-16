import pandas as pd

# 🔹 Initialisation des métabolites avec un état plus dynamique
metabolite_states = {
    "Glucose": 1,      # Réserve initiale
    "Glycogène": 2,    # Stock plein
    "Pyruvate": 1,     # Intermédiaire disponible
    "ATP": 2,          # Énergie initiale élevée
    "AMPK": 0,         # Inactif au départ
}

# 🔹 Définition des flux métaboliques
def glycolyse(glucose, atp):
    return glucose > 0 and atp < 2  # Produit du pyruvate et de l'ATP

def cycle_krebs(pyruvate, atp):
    return pyruvate > 0 and atp < 2  # Produit de l'ATP

def glycogenese(glucose, atp):
    return glucose == 2 and atp == 2  # Stocke le glucose en glycogène

def glycogenolyse(glycogene, glucose, ampk):
    return glycogene > 0 and (glucose == 0 or ampk == 2)  # Libère du glucose

def neoglucogenese(pyruvate, glucose, ampk):
    return pyruvate > 0 and glucose < 2 and ampk == 2  # Produit du glucose si AMPK actif

# 🔹 Simulation du métabolisme hépatique
def simulate_fba(n_steps=10):
    history = []

    for step in range(n_steps):
        active_fluxes = {
            "Glycolyse": glycolyse(metabolite_states["Glucose"], metabolite_states["ATP"]),
            "Cycle de Krebs": cycle_krebs(metabolite_states["Pyruvate"], metabolite_states["ATP"]),
            "Glycogenèse": glycogenese(metabolite_states["Glucose"], metabolite_states["ATP"]),
            "Glycogénolyse": glycogenolyse(metabolite_states["Glycogène"], metabolite_states["Glucose"], metabolite_states["AMPK"]),
            "Néoglucogenèse": neoglucogenese(metabolite_states["Pyruvate"], metabolite_states["Glucose"], metabolite_states["AMPK"]),
        }

        new_states = metabolite_states.copy()

        # 🔹 Métabolisme énergétique
        if active_fluxes["Glycolyse"]:
            new_states["Glucose"] = max(0, new_states["Glucose"] - 1)
            new_states["Pyruvate"] = min(2, new_states["Pyruvate"] + 1)
            new_states["ATP"] = min(2, new_states["ATP"] + 1)

        if active_fluxes["Cycle de Krebs"]:
            new_states["Pyruvate"] = max(0, new_states["Pyruvate"] - 1)
            new_states["ATP"] = min(2, new_states["ATP"] + 1)

        if active_fluxes["Glycogenèse"]:
            new_states["Glucose"] = max(0, new_states["Glucose"] - 1)
            new_states["Glycogène"] = min(2, new_states["Glycogène"] + 1)

        if active_fluxes["Glycogénolyse"]:
            new_states["Glycogène"] = max(0, new_states["Glycogène"] - 1)
            new_states["Glucose"] = min(2, new_states["Glucose"] + 1)

        if active_fluxes["Néoglucogenèse"]:
            new_states["Pyruvate"] = max(0, new_states["Pyruvate"] - 1)
            new_states["Glucose"] = min(2, new_states["Glucose"] + 1)

        # 🔹 Consommation passive d'ATP
        if new_states["ATP"] > 0:
            new_states["ATP"] -= 1  # Simulation de l'utilisation normale de l'ATP

        # 🔹 Activation de l'AMPK si ATP est bas
        if new_states["ATP"] == 0:
            new_states["AMPK"] = 2  # AMPK s'active pour compenser la faible énergie
        elif new_states["ATP"] > 1:
            new_states["AMPK"] = 0  # AMPK s'éteint quand ATP revient à un niveau correct

        metabolite_states.update(new_states)
        history.append(metabolite_states.copy())

    df_history = pd.DataFrame(history)
    df_history.index.name = "Cycle"

    return df_history

# 🔹 Exécution de la simulation
df_results = simulate_fba(n_steps=20)

# 🔹 Affichage des résultats
print(df_results)
