import numpy as np
import matplotlib.pyplot as plt

def thomas_dynamics(state, velocities):
    """ Fonction de mise à jour selon la dynamique de René Thomas avec vitesses """
    G, PC = state  # Décomposition de l'état
    
    # Récupération des vitesses associées à l'état actuel
    velocity = velocities[state]
    
    # Règles de transition avec prise en compte de la vitesse
    new_G = 1 if PC == 0 else 0  # PC inhibe G
    new_PC = 1 if G == 1 else 0  # G active PC
    
    return (new_G, new_PC), velocity

def simulate(initial_state, velocities, total_time=24, dt=1):
    """ Simulation de l'évolution du réseau sur 24h avec vitesses """
    time_steps = np.arange(0, total_time + dt, dt)
    states = [initial_state]
    times = [0]
    
    current_time = 0
    while current_time < total_time:
        new_state, velocity = thomas_dynamics(states[-1], velocities)
        current_time += velocity * dt  # Avancement temporel selon la vitesse
        times.append(current_time)
        states.append(new_state)
    
    return times, states

# Définition des vitesses pour chaque état
velocities = {
    (0, 0): 1.2,  # Exemple de vitesse pour chaque état
    (0, 1): 0.8,
    (1, 0): 1.0,
    (1, 1): 5,
}

# Simulation à partir d'un état initial
initial_state = (1, 0)  # G actif, PC inactif
total_time = 24
dt = 1  # Pas de temps
times, trajectory = simulate(initial_state, velocities, total_time, dt)

# Extraction des états individuels
states_G = [state[0] for state in trajectory]
states_PC = [state[1] for state in trajectory]

# Affichage des résultats
plt.figure(figsize=(10, 5))
plt.plot(times, states_G, label="G (PER/CRY libre)", marker='o')
plt.plot(times, states_PC, label="PC (Complexe PER/CRY)", marker='s')
plt.xlabel("Temps (h)")
plt.ylabel("État")
plt.title("Dynamique du réseau de régulation sur 24h")
plt.legend()
plt.grid()
plt.show()
