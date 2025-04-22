import numpy as np
import matplotlib.pyplot as plt

# Fonction de régulation discrète façon René Thomas
#def gene_regulation(G, PC):
#    if G > 0.5:
#        PC = 1  # G active PC
#    else:
#        PC = 0
#    
#    if PC > 0.5:
#        G = 0  # PC inhibe G
#    else:
#        G = 1
#    
#    return G, PC

#import numpy as np
import matplotlib.pyplot as plt

# Fonction de régulation douce façon René Thomas avec transition progressive
def gene_regulation(G, PC, t, dt):
    G_new = G + dt * (1 - PC - G)  # Augmente progressivement jusqu'à inhibition
    PC_new = PC + dt * (G - PC)  # Suit un cycle avec un décalage temporel
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
    
    # Mise à jour douce de PerCry
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
plt.figure(figsize=(10, 6))
labels = ["G (PerCry)", "PC (Complexe PerCry)", "Glucose", "Glycogène", "Pyruvate", "Acétyl-CoA", "Acides Gras", "ATP"]
for i in range(8):
    plt.plot(time, results[:, i], label=labels[i])

plt.xlabel("Temps")
plt.ylabel("Concentration relative")
plt.legend()
plt.title("Simulation du métabolisme hépatique et horloge circadienne")
plt.show()
