import numpy as np
import matplotlib.pyplot as plt

# Fonction de régulation douce avec amortissement pour stabiliser l'oscillation
def gene_regulation(G, PC, t, dt):
    G_new = G + dt * (1 - PC - G) * np.exp(-0.01 * t)  # Amortissement exponentiel
    PC_new = PC + dt * (G - PC) * np.exp(-0.01 * t)  # Stabilisation progressive
    return G_new, PC_new

# Paramètres de simulation
dt = 0.1  # Pas de temps
Tmax = 50  # Temps total de simulation
n_steps = int(Tmax / dt)

# Conditions initiales
G, PC = 1, 0  # État initial du cycle circadien
Glucose, Glycogene, Pyruvate, Acetyl_CoA, Acides_Gras, ATP = 5, 10, 2, 1, 3, 2
cortisol, insuline, glucagon, ampk = 0.5, 0.3, 0.2, 0.4

# Stockage des résultats
time = np.arange(0, Tmax, dt)
results = np.zeros((n_steps, 8))

# Simulation itérative
t = 0
for i in range(n_steps):
    cortisol = max(0, cortisol - 0.0002 * t)  # Dégradation lente du cortisol
    
    # Mise à jour douce et stabilisée de PerCry
    G, PC = gene_regulation(G, PC, t, dt)
    
    # Mise à jour des métabolites
    dGlucose = -Pyruvate - insuline * Glucose + glucagon * Glycogene
    dGlycogene = insuline * Glucose - glucagon * Glycogene
    dPyruvate = Glucose - Acetyl_CoA
    dAcetyl_CoA = Pyruvate - Acides_Gras + ampk * Acides_Gras
    dAcides_Gras = Acetyl_CoA - ampk * Acides_Gras
    dATP = Acetyl_CoA - (1 / (1 + ampk)) * ATP
    
    # Intégration explicite (Euler)
    Glucose += dGlucose * dt
    Glycogene += dGlycogene * dt
    Pyruvate += dPyruvate * dt
    Acetyl_CoA += dAcetyl_CoA * dt
    Acides_Gras += dAcides_Gras * dt
    ATP += dATP * dt
    
    # Stockage des valeurs
    results[i] = [G, PC, Glucose, Glycogene, Pyruvate, Acetyl_CoA, Acides_Gras, ATP]
    t += dt

# Affichage des résultats
plt.figure(figsize=(12, 6))

# Premier graphique : G et PC
plt.subplot(2, 1, 1)
plt.plot(time, results[:, 0], label="G (PerCry)")
plt.plot(time, results[:, 1], label="PC (Complexe PerCry)")
plt.xlabel("Temps")
plt.ylabel("Concentration relative")
plt.legend()
plt.title("Oscillation du cycle PerCry")

# Second graphique : Métabolites
plt.subplot(2, 1, 2)
labels = ["Glucose", "Glycogène", "Pyruvate", "Acétyl-CoA", "Acides Gras", "ATP"]
for i in range(2, 8):
    plt.plot(time, results[:, i], label=labels[i-2])

plt.xlabel("Temps")
plt.ylabel("Concentration relative")
plt.legend()
plt.title("Simulation du métabolisme hépatique")

plt.tight_layout()
plt.show()
