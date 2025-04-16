import numpy as np
import matplotlib.pyplot as plt
from scipy.integrate import odeint
import pandas as pd
from scipy.optimize import linprog

# 🔹 Paramètres des taux métaboliques (ajuster selon le modèle)
k1, k2, k3, k4 = 0.5, 0.3, 0.2, 0.4  # Glycolyse et Cycle de Krebs
k5, k6, k7 = 0.2, 0.3, 0.1  # Glycogenèse, Glycogénolyse, Néoglucogenèse
k8 = 0.2  # Consommation d'ATP

# 🔹 Modèle des équations différentielles
def metabolic_model(y, t):
    Glucose, Glycogène, Pyruvate, ATP, AMPK = y

    # 🔹 Activation de l'AMPK si ATP est bas
    if ATP < 1.5:
        AMPK = 1
    else:
        AMPK = 0

    # 🔹 Flux métaboliques
    dGlucose_dt = -k1 * Glucose + k6 * Glycogène + (k7 * Pyruvate if AMPK else 0)
    dGlycogène_dt = k5 * Glucose - k6 * Glycogène
    dPyruvate_dt = k2 * Glucose - k3 * Pyruvate
    dATP_dt = k4 * Pyruvate - k8 * ATP  # ATP produit par le cycle de Krebs, consommé passivement

    return [dGlucose_dt, dGlycogène_dt, dPyruvate_dt, dATP_dt, AMPK]

# 🔹 Conditions initiales (Glucose, Glycogène, Pyruvate, ATP, AMPK)
y0 = [5.0, 8.0, 2.0, 3.0, 0]

# 🔹 Simulation sur 100 unités de temps
t = np.linspace(0, 100, 500)
solution = odeint(metabolic_model, y0, t)

# 🔹 Extraction des solutions
Glucose_sol, Glycogène_sol, Pyruvate_sol, ATP_sol, AMPK_sol = solution.T

# 🔹 Affichage des résultats
plt.figure(figsize=(10, 6))
plt.plot(t, Glucose_sol, label="Glucose", linewidth=2)
plt.plot(t, Glycogène_sol, label="Glycogène", linewidth=2)
plt.plot(t, Pyruvate_sol, label="Pyruvate", linewidth=2)
plt.plot(t, ATP_sol, label="ATP", linewidth=2)
plt.legend()
plt.xlabel("Temps")
plt.ylabel("Concentration")
plt.title("Évolution des métabolites en continu")
plt.grid()
plt.show()

# 🔹 Définition de la matrice stœchiométrique
S = np.array([
    [-1,  0, -1, +1, +1,  0],  # Glucose
    [ 0,  0, +1, -1,  0,  0],  # Glycogène
    [+1, -1,  0,  0, -1,  0],  # Pyruvate
    [+1, +1,  0,  0,  0, -1]   # ATP
])

# 🔹 Définition des noms des réactions
reactions = ["Glycolyse", "Cycle Krebs", "Glycogenèse", "Glycogénolyse", "Néoglucogenèse", "Consommation ATP"]

# 🔹 Fonction objectif : maximiser la production d'ATP
c = np.array([-0, -0, -0, -0, -0, -1])  # On maximise ATP, donc -1 car linprog fait une min

# 🔹 Contraintes : Équilibre des métabolites (S @ v = 0)
A_eq = S
b_eq = np.zeros(S.shape[0])

# 🔹 Définition des bornes biologiques ajustées
v_max = [8, 8, 5, 5, 3, 8]  # Ajustement des flux max
bounds = [(0, v_max[i]) for i in range(len(reactions))]

# 🔹 Ajout de nouvelles contraintes biologiques
A_ub = np.array([
    [1, 0, 0, 0, 1, 0],  # Glycolyse + Néoglucogenèse <= 5
    [-0.5, 1, 0, 0, 0, 0],  # Cycle Krebs >= 0.5 * Glycolyse
    [1, 1, 0, 0, 0, -1]   # ATP produit >= ATP consommé
])
b_ub = np.array([5, 0, 0])

# 🔹 Résolution de l'optimisation linéaire
result = linprog(c, A_eq=A_eq, b_eq=b_eq, A_ub=A_ub, b_ub=b_ub, bounds=bounds, method='highs')

# 🔹 Affichage des résultats
if result.success:
    flux_values = result.x
    flux_df = pd.DataFrame({'Réaction': reactions, 'Flux optimal': flux_values})
    print(flux_df)
else:
    print("Problème d'optimisation :", result.message)