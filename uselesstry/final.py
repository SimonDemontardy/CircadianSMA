import numpy as np
import matplotlib.pyplot as plt
from scipy.integrate import odeint

# Modèle René Thomas : Régulation discrète de PerCry
def gene_regulation(G, PC):
    if G > 0.5:
        PC = 1  # G active PC
    else:
        PC = 0
    
    if PC > 0.5:
        G = 0  # PC inhibe G
    else:
        G = 1
    
    return G, PC

# Modèle continu : Métabolisme hépatique
def metabolic_system(y, t, PC, cortisol, insuline, glucagon, ampk):
    Glucose, Glycogene, Pyruvate, Acetyl_CoA, Acides_Gras, ATP = y
    
    # Activation du cycle de Krebs influencée par PC
    krebs_activation = 1 + 0.5 * PC  # PC booste légèrement le cycle
    
    # Métabolisme
    dGlucose = -Pyruvate - insuline * Glucose + glucagon * Glycogene
    dGlycogene = insuline * Glucose - glucagon * Glycogene
    dPyruvate = (Glucose - Acetyl_CoA) * krebs_activation  # Influence de PC sur Krebs
    dAcetyl_CoA = Pyruvate - Acides_Gras + ampk * Acides_Gras
    dAcides_Gras = Acetyl_CoA - ampk * Acides_Gras
    dATP = Acetyl_CoA - (1 / (1 + ampk)) * ATP
    
    return [dGlucose, dGlycogene, dPyruvate, dAcetyl_CoA, dAcides_Gras, dATP]

# Paramètres
dt = 1  # Pas de mise à jour du modèle René Thomas
Tmax = 50  # Temps total de simulation
n_steps = int(Tmax / dt)

# Conditions initiales
G, PC = 1, 0  # État initial du cycle circadien
metabolites_0 = [5, 10, 2, 1, 3, 2]
cortisol, insuline, glucagon, ampk = 0.5, 0.3, 0.2, 0.4

# Stockage des résultats
time = np.arange(0, Tmax, dt)
results_percry = np.zeros((n_steps, 2))
results_metabolism = np.zeros((n_steps, 6))

# Simulation itérative
t = 0
for i in range(n_steps):
    cortisol = max(0, cortisol - 0.0002 * t)  # Dégradation lente du cortisol
    
    # Mise à jour de PerCry (discrète)
    G, PC = gene_regulation(G, PC)
    results_percry[i] = [G, PC]
    
    # Simulation du métabolisme (continu, ODE)
    t_span = np.linspace(t, t + dt, 10)
    metabolites = odeint(metabolic_system, metabolites_0, t_span, args=(PC, cortisol, insuline, glucagon, ampk))
    metabolites_0 = metabolites[-1]  # Mise à jour pour la prochaine itération
    results_metabolism[i] = metabolites_0
    
    t += dt

# Affichage des résultats
plt.figure(figsize=(12, 6))

# Premier graphique : G et PC (Régulation circadienne)
plt.subplot(2, 1, 1)
plt.step(time, results_percry[:, 0], label="G (PerCry)", where='post')
plt.step(time, results_percry[:, 1], label="PC (Complexe PerCry)", where='post')
plt.xlabel("Temps")
plt.ylabel("État binaire")
plt.legend()
plt.title("Oscillation du cycle PerCry (Modèle René Thomas)")

# Second graphique : Métabolisme
plt.subplot(2, 1, 2)
labels = ["Glucose", "Glycogène", "Pyruvate", "Acétyl-CoA", "Acides Gras", "ATP"]
for i in range(6):
    plt.plot(time, results_metabolism[:, i], label=labels[i])

plt.xlabel("Temps")
plt.ylabel("Concentration relative")
plt.legend()
plt.title("Simulation du métabolisme hépatique (Modèle ODE)")

plt.tight_layout()
plt.show()
