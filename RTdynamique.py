import numpy as np
import matplotlib.pyplot as plt

# Définition des célérités selon l'état (G, PC)
celerities = {
    (0, 0, 0): (0.18, -0.18),
    (0, 1, 0): (-0.18, -0.18),
    (1, 0, 0): (0.25, 0.13),
    (1, 1, 0): (-0.18, 0.25),
    # avec cortisol
    (0, 0, 1): (1/7.5, 1/5.5),
    (0, 1, 1): (-1/5.5, 1/7.5),
    (1, 0, 1): (1/4, -1/5.5),
    (1, 1, 1): (1/4, 1/4),
}


# Initialisation des états
G, PC, C= 0, 0, 0 # État initial
x, y = 0.0, 1.0  # Position initiale correcte
epsilon = 0.05  # Seuil de tolérance
dt = 1  # Pas de temps
max_time = 20  # Temps maximal simulé en heures
max_iterations = 5000  # Sécurité pour éviter boucle infinie

# Stockage des données pour affichage
time_vals = []
G_vals = []
PC_vals = []
C_vals = []
x_vals = []
y_vals = []
state_vals = []
#state_trajectories = {(0, 0): ([], []), (0, 1): ([], []), (1, 0): ([], []), (1, 1): ([], [])}
state_trajectories = {(0, 0): ([], [], []), (0, 1): ([], [], []), (1, 0): ([], [], []), (1, 1): ([], [], [])}

# Boucle de simulation
t = 0  # Temps initial
while t * dt < max_time and t < max_iterations:
        # Déclencher un pic de cortisol à t = 8h
    if t * dt == 25:
        C =  1 # Activation du cortisol
        print(f"🔥 Pic de cortisol déclenché à t={t*dt}h")

    # Désactiver le cortisol après 4 heures d'action (t=12h)
    if t * dt == 30:
        C = 0  # Désactivation du cortisol
        print(f"🛑 Cortisol désactivé à t={t*dt}h")

    time_vals.append(t * dt)
    G_vals.append(G)
    PC_vals.append(PC)
    C_vals.append(C)
    x_vals.append(x)
    y_vals.append(y)
    state_vals.append((G, PC))
    state_trajectories[(G, PC)][0].append(x)
    state_trajectories[(G, PC)][1].append(y)
    state_trajectories[(G, PC)][2].append(t * dt)

    # Récupérer les célérités de l'état actuel
    c_x, c_y = celerities[(G, PC, C)]

    # Mettre à jour les positions x et y
    x += c_x * dt
    y += c_y * dt

    # Affichage de l'avancement toutes les 10 itérations
    print(f"[t={t*dt:.1f}h] G={G}, PC={PC}, x={x:.3f}, y={y:.3f}")

    # Gestion des transitions avec objectifs propres à chaque état
    if G == 1 and PC == 1: # (1,1) -> (0,1)
        if x <= epsilon: #donc x cherche à etre à 0 # (1,1) -> (0,1)
            G, PC = 0, 1
            x = 1 # transition depuis 1,1 vers 0,1 donc passe le mur et passe de 0 à 1
        elif y >= 1 - epsilon: #glissement
            G, PC = 0, 1
            y = 1
            x = 1
    elif G == 0 and PC == 1: # (0,1) -> (0,0)
        if y <= epsilon:  # (0,1) -> (0,0)
            G, PC = 0, 0
            y = 1 # transition depuis 0,1 vers 0,0 donc passe le mur et passe de 0 à 1
        elif x <= epsilon: #glissement
            G, PC = 0, 0
            y = 1
            x = 0   
    elif G == 0 and PC == 0:
        if x >= 1 - epsilon:  # (0,0) -> (1,0)
            G, PC = 1, 0
            x = 0 # transition depuis 0,0 vers 1,0 donc passe le mur et passe de 1 à 0
        elif y <= epsilon: #glissement
            G, PC = 1, 0
            x = 0
            y = 0
    elif G == 1 and PC == 0:
        if y >= 1 - epsilon:  # (1,0) -> (1,1)
            G, PC = 1, 1
            y = 0 # transition depuis 1,0 vers 1,1 donc passe le mur et passe de 1 à 0
        elif x >= 1 - epsilon: #glissement
            G, PC = 1, 1
            x = 1
            y = 0


    print(f"✅ Nouveau état: (G={G}, PC={PC}) à t={t*dt:.1f}h, x={x:.3f}, y={y:.3f}\n")
    
    t += 1  # Incrémentation du temps

# Vérification si la simulation s'est bien arrêtée
if t >= max_iterations:
    print("⛔ Attention : simulation arrêtée après 5000 itérations (sécurité active)")


# Affichage des graphiques pour chaque état
fig, axes = plt.subplots(2, 2, figsize=(10, 10))
state_positions = {(1, 1): (0, 1), (0, 1): (0, 0), (1, 0): (1, 1), (0, 0): (1, 0)}

for (state, ax_pos) in state_positions.items():
    ax = axes[ax_pos[0], ax_pos[1]]
    times = np.array(state_trajectories[state][2])
    colors = ['blue' if t < 24 else 'red' for t in times]
    ax.scatter(state_trajectories[state][0], state_trajectories[state][1], c=colors, label=f"État (G={state[0]}, PC={state[1]})")
    ax.set_xlim(0, 1)
    ax.set_ylim(0, 1)
    ax.set_title(f"État (G={state[0]}, PC={state[1]})")
    ax.set_xlabel("x (Position interne)")
    ax.set_ylabel("y (Position interne)")
    ax.grid()
    ax.legend()

plt.suptitle("Trajectoire du système dans les états de René Thomas")
plt.tight_layout(rect=[0, 0, 1, 0.96])
plt.show()

# Affichage des graphiques pour chaque état
fig, axes = plt.subplots(2, 2, figsize=(10, 10))
state_positions = {(1, 1): (0, 1), (0, 1): (0, 0), (1, 0): (1, 1), (0, 0): (1, 0)}

for (state, ax_pos) in state_positions.items():
    ax = axes[ax_pos[0], ax_pos[1]]
    ax.plot(state_trajectories[state][0], state_trajectories[state][1], marker='o', linestyle='-')
    ax.set_xlim(0, 1)
    ax.set_ylim(0, 1)
    ax.set_title(f"État (G={state[0]}, PC={state[1]})")
    ax.set_xlabel("x (Position interne)")
    ax.set_ylabel("y (Position interne)")
    ax.grid()

plt.suptitle("Trajectoire du système dans les états de René Thomas")
plt.tight_layout(rect=[0, 0, 1, 0.96])
plt.show()

# Affichage du graphique des transitions temporelles
plt.figure(figsize=(10, 5))
plt.plot(time_vals, G_vals, label="G (PER/CRY libre)", linestyle='-', marker='o')
plt.plot(time_vals, PC_vals, label="PC (Complexe PER/CRY)", linestyle='-', marker='s')
plt.plot(time_vals, C_vals, label="Cortisol", linestyle='-', marker='x')
plt.xlabel("Temps simulé (h)")
plt.ylabel("État")
plt.title("Dynamique du réseau René Thomas")
plt.legend()
plt.grid()
plt.show()
