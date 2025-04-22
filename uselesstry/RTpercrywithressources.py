import numpy as np
import matplotlib.pyplot as plt

def thomas_dynamics(state, transition_celerities, current_time):
    """ Fonction de mise à jour selon la dynamique de René Thomas avec impact du cortisol et célérités correctes """
    G, PC, C = state  # Décomposition de l'état
    
    # Célérités spécifiques pour chaque transition selon les états (déjà en h⁻¹ dans la thèse)
    celerity_g, celerity_pc = transition_celerities[state]
    
    # Gestion des célérités négatives : ralentissement des transitions descendantes
    if celerity_g < 0 and G == 1:
        celerity_g = abs(celerity_g)  # Ralentissement de la désactivation de G
    if celerity_pc < 0 and PC == 1:
        celerity_pc = abs(celerity_pc)  # Ralentissement de la désactivation de PC
    
    # Règles de transition avec effet du cortisol
    new_G = 1 if (PC == 0 and C == 0) else 0  # PC et cortisol inhibent G
    new_PC = 1 if G == 1 else 0  # G active PC
    new_C = 1 if 6 <= current_time % 24 <= 12 else 0  # Cortisol suit un cycle naturel
    
    return (new_G, new_PC, new_C), (celerity_g, celerity_pc)

def simulate(initial_state, transition_celerities, total_time=24, dt=1):
    """ Simulation de l'évolution du réseau sur 24h avec impact du cortisol """
    times = [0]
    states = [initial_state]
    
    current_time = 0
    while current_time < total_time:
        new_state, (celerity_g, celerity_pc) = thomas_dynamics(states[-1], transition_celerities, current_time)
        
        # Calcul du pas de temps basé directement sur les célérités en h⁻¹
        current_time += min(1 / abs(celerity_g), 1 / abs(celerity_pc))  
        times.append(current_time)
        states.append(new_state)
    
    return times, states

# Définition des célérités en h⁻¹ (directement issues de la thèse)
transition_celerities = {
    (0, 0, 0): (1/7.5, 1/5.5),  # G et PC impactés séparément
    (0, 1, 0): (-1/5.5, 1/7.5),  # G décroît lentement (célérité négative)
    (1, 0, 0): (1/5.5, -1/4),  # PC décroît lentement
    (1, 1, 0): (1/4, 1/4),
    (0, 0, 1): (1/7.5, 1/5.5),  # G impacté par C
    (0, 1, 1): (-1/5.5, 1/7.5),
    (1, 0, 1): (1/4, -1/5.5),
    (1, 1, 1): (1/4, 1/4),
}

# Simulation à partir d'un état initial
initial_state = (1, 0, 0)  # G actif, PC inactif, Cortisol bas
total_time = 24

dt = 1  # Pas de temps
# Exécution de la simulation avec les bonnes unités
times, trajectory = simulate(initial_state, transition_celerities, total_time, dt)

# Extraction des états individuels
states_G = [state[0] for state in trajectory]
states_PC = [state[1] for state in trajectory]
states_C = [state[2] for state in trajectory]

# Affichage des résultats
plt.figure(figsize=(10, 5))
plt.plot(times, states_G, label="G (PER/CRY libre)", marker='o')
plt.plot(times, states_PC, label="PC (Complexe PER/CRY)", marker='s')
plt.plot(times, states_C, label="Cortisol (C)", linestyle='dashed')
plt.xlabel("Temps (h)")
plt.ylabel("État")
plt.title("Dynamique du réseau avec impact du cortisol et célérités spécifiques")
plt.legend()
plt.grid()
plt.show()
