import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

# Charger les données
df = pd.read_csv("jade-project/data_log240.txt", sep=";")

# Définir les groupes
groups = {
    "NSC": ["NSC_G", "NSC_PC"],
    "Liver": ["Liver_res", "Liver_G", "Liver_PC", "Liver_res2"],
    "Pancreas": ["Pancreas_res", "Pancreas_G", "Pancreas_PC"],
    "BetaCells": ["Beta_res", "Beta_res2", "Beta_G", "Beta_PC"],
    "AlphaCells": ["Alpha_res", "Alpha_G", "Alpha_PC"],
    "Surrenal": ["Surrenal_res", "Surrenal_G", "Surrenal_PC"],
    "Metabolic": ["Glucose", "Glycogene", "AcidesGras", "AcidesAmines", "AcetylCoA", "Pyruvate"],
    "Metabolism": ["Glucose", "Glycogene", "AcidesGras", "AcetylCoA", "Pyruvate"],
    "ATP": ["ATP"],
    "AMPK": ["AMPK"],
    "Environment": ["Cortisol", "Insulin", "Glucagon"],
    "Liver_HC": ["Liver_G", "Liver_PC"],
    "Pancreas_HC": ["Pancreas_G", "Pancreas_PC"],
    "BetaCells_HC": ["Beta_G", "Beta_PC"],
    "AlphaCells_HC": ["Alpha_G", "Alpha_PC"],
    "Surrenal_HC": ["Surrenal_G", "Surrenal_PC"]
}

# Limiter la fenêtre temporelle
t_start, t_end = 0, 240
window = df[(df["time"] >= t_start) & (df["time"] <= t_end)]

# Fonction pour tracer un graphique simple
def plot_time_series(group_name, variables):
    fig, ax = plt.subplots(figsize=(10,6))
    time = window["time"]

    for var in variables:
        ax.plot(time, window[var], marker="o", label=var)

    # Ajouter les lignes verticales pour minuit (time % 24 == 0)
    for t in range(int(time.min()), int(time.max())+1):
        if t % 24 == 0:
            ax.axvline(x=t, color="red", linestyle="--", alpha=0.7, label="Minuit" if t == 0 else "")

    ax.set_xlabel("Temps (h)")
    ax.set_ylabel("Valeur")
    plt.title(f"Évolution temporelle - {group_name} (t={t_start}h à {t_end}h)")
    plt.legend()
    plt.tight_layout()
    plt.show()

# Générer un graph par groupe
for name, vars in groups.items():
    plot_time_series(name, vars)
