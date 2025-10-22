import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

# Charger les données
df = pd.read_csv("jade-project/data_log24bis.txt", sep=";")

# Définir les groupes
groups = {
    "NSC": ["NSC_res", "NSC_G", "NSC_PC"],
    "Liver": ["Liver_res", "Liver_G", "Liver_PC", "Liver_res2"],
    "Pancreas": ["Pancreas_res", "Pancreas_G", "Pancreas_PC"],
    "BetaCells": ["Beta_res", "Beta_res2", "Beta_G", "Beta_PC"],
    "AlphaCells": ["Alpha_res", "Alpha_G", "Alpha_PC"],
    "Surrenal": ["Surrenal_res", "Surrenal_G", "Surrenal_PC"],
    "Metabolic": ["Glucose", "Glycogene", "AcidesGras", "AcidesAmines", "AcetylCoA", "Pyruvate"],
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
t_start, t_end = 0, 24
window = df[(df["time"] >= t_start) & (df["time"] <= t_end)]

# Fonction pour tracer un groupe
def plot_clock_chart(group_name, variables):
    fig, ax = plt.subplots(subplot_kw={'polar': True}, figsize=(7,7))
    time = window["time"]

    for var in variables:
        values = window[var]
        angles = (time % 24) / 24 * 2 * np.pi
        ax.plot(angles, values, marker="o", label=var)

    ax.set_theta_direction(-1)
    ax.set_theta_offset(np.pi/2)
    ax.set_xticks(np.linspace(0, 2*np.pi, 24, endpoint=False))
    ax.set_xticklabels([f"{h}h" for h in range(24)])
    plt.title(f"Clock chart - {group_name} (t={t_start}h à {t_end}h)")
    plt.legend()
    plt.show()

# Générer un chart par groupe
for name, vars in groups.items():
    plot_clock_chart(name, vars)
