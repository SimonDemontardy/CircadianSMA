import numpy as np
import matplotlib.pyplot as plt
from scipy.integrate import odeint

# Définition des équations du modèle
def system(y, t, cortisol, insuline, glucagon, ampk):
    G, PC, Glucose, Glycogene, Pyruvate, Acetyl_CoA, Acides_Gras, ATP = y
    
    # Dégradation progressive du cortisol
    cortisol = max(0, cortisol - 0.01 * t)  # Le cortisol diminue lentement
    
    # Horloge circadienne (Boucle PerCry)
    dG = -PC + (1 - cortisol)  # Cortisol inhibe G
    dPC = G - PC  # PC est activé par G
    
    # Couplage PerCry et Métabolisme
    alpha, beta, gamma = 0.1, 0.05, 0.1  # Coefficients d'influence de PerCry
    dGlucose = -Pyruvate - insuline * Glucose + glucagon * Glycogene  # Glycolyse + Stockage/libération
    dGlycogene = insuline * Glucose - glucagon * Glycogene  # Stockage et libération du glucose
    dPyruvate = Glucose + alpha * PC * Glucose - Acetyl_CoA  # PC favorise la glycolyse
    dAcetyl_CoA = Pyruvate - Acides_Gras + ampk * Acides_Gras - beta * PC * Acetyl_CoA  # PC inhibe la lipogenèse
    dAcides_Gras = Acetyl_CoA - ampk * Acides_Gras  # Stockage et oxydation
    dATP = Acetyl_CoA - (1 / (1 + ampk)) * ATP  # ATP produit par catabolisme, consommé en fonction de l’énergie dispo
    
    # Influence de G sur AMPK
    ampk += gamma * G  # G active l'AMPK
    
    return [dG, dPC, dGlucose, dGlycogene, dPyruvate, dAcetyl_CoA, dAcides_Gras, dATP]

# Conditions initiales
y0 = [1, 0, 5, 10, 2, 1, 3, 2]  # État initial des variables

# Temps de simulation
t = np.linspace(0, 50, 500)

# Simulation avec différents niveaux d'hormones
cortisol = 0.5  # Niveau initial de stress
insuline = 0.3  # Taux moyen d'insuline
glucagon = 0.2  # Taux moyen de glucagon
ampk = 0.4  # Activation moyenne de l'AMPK

solution = odeint(system, y0, t, args=(cortisol, insuline, glucagon, ampk))

# Affichage des résultats
plt.figure(figsize=(10, 6))
labels = ["G (PerCry)", "PC (Complexe PerCry)", "Glucose", "Glycogène", "Pyruvate", "Acétyl-CoA", "Acides Gras", "ATP"]
for i in range(8):
    plt.plot(t, solution[:, i], label=labels[i])

plt.xlabel("Temps")
plt.ylabel("Concentration relative")
plt.legend()
plt.title("Simulation du métabolisme hépatique et horloge circadienne")
plt.show()
