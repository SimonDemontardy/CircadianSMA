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
max_time = 24  # Temps maximal simulé en heures
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
t = 0
while t * dt < max_time and t < max_iterations:
    if t * dt == 25:
        C = 1
        print(f"🔥 Pic de cortisol déclenché à t={t*dt}h")
    if t * dt == 30:
        C = 0
        print(f"🛑 Cortisol désactivé à t={t*dt}h")

    # Sauvegarde
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

    # Mise à jour des positions
    c_x, c_y = celerities[(G, PC, C)]
    x += c_x * dt
    y += c_y * dt

    # Saturation des bords (pour ne pas sortir du carré [0,1]x[0,1])
    x = max(0, min(x, 1))
    y = max(0, min(y, 1))

    print(f"[t={t*dt:.1f}h] G={G}, PC={PC}, x={x:.3f}, y={y:.3f}")

    # Transitions d'état
    if G == 1 and PC == 1:
        if x <= epsilon:
            G, PC = 0, 1
            x = 1
        elif y >= 1 - epsilon:
            y = 1  # On longe le bord

    elif G == 0 and PC == 1:
        if y <= epsilon:
            G, PC = 0, 0
            y = 1
        elif x <= epsilon:
            x = 0  # On longe le bord

    elif G == 0 and PC == 0:
        if x >= 1 - epsilon:
            G, PC = 1, 0
            x = 0
        elif y <= epsilon:
            y = 0  # On longe le bord

    elif G == 1 and PC == 0:
        if y >= 1 - epsilon:
            G, PC = 1, 1
            y = 0
        elif x >= 1 - epsilon:
            x = 1  # On longe le bord

    print(f"✅ Nouveau état: (G={G}, PC={PC}) à t={t*dt:.1f}h, x={x:.3f}, y={y:.3f}\n")
    t += 1

if t >= max_iterations:
    print("⛔ Attention : simulation arrêtée après 5000 itérations (sécurité active)")

# Affichage : scatter
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

plt.suptitle("Trajectoire du système dans les états de René Thomas (scatter)")
plt.tight_layout(rect=[0, 0, 1, 0.96])
plt.show()

# Affichage : courbes continues
fig, axes = plt.subplots(2, 2, figsize=(10, 10))

for (state, ax_pos) in state_positions.items():
    ax = axes[ax_pos[0], ax_pos[1]]
    ax.plot(state_trajectories[state][0], state_trajectories[state][1], marker='o', linestyle='-')
    ax.set_xlim(0, 1)
    ax.set_ylim(0, 1)
    ax.set_title(f"État (G={state[0]}, PC={state[1]})")
    ax.set_xlabel("x (Position interne)")
    ax.set_ylabel("y (Position interne)")
    ax.grid()

plt.suptitle("Trajectoire du système dans les états de René Thomas (courbes)")
plt.tight_layout(rect=[0, 0, 1, 0.96])
plt.show()

# Affichage : dynamique temporelle
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

# Affichage : courbes continues avec changement de couleur à chaque transition et point rouge initial
import matplotlib.cm as cm

fig, axes = plt.subplots(2, 2, figsize=(10, 10))
state_positions = {(1, 1): (0, 1), (0, 1): (0, 0), (1, 0): (1, 1), (0, 0): (1, 0)}

# Couleurs cycliques par transitions dans un état
color_cycle = cm.get_cmap('tab20')

for (state, ax_pos) in state_positions.items():
    ax = axes[ax_pos[0], ax_pos[1]]
    x_list = state_trajectories[state][0]
    y_list = state_trajectories[state][1]

    if len(x_list) == 0:
        continue

    # Index des points où l'état change (interruption de la trajectoire)
    segments = []
    current_segment = [[x_list[0]], [y_list[0]]]

    for i in range(1, len(x_list)):
        if (x_list[i] - x_list[i - 1])**2 + (y_list[i] - y_list[i - 1])**2 > 0.1:
            # Saut significatif → nouvelle couleur
            segments.append(current_segment)
            current_segment = [[x_list[i]], [y_list[i]]]
        else:
            current_segment[0].append(x_list[i])
            current_segment[1].append(y_list[i])
    segments.append(current_segment)

    for j, segment in enumerate(segments):
        color = color_cycle(j % 20)
        ax.plot(segment[0], segment[1], marker='o', linestyle='-', color=color)

    # Ajouter le gros point rouge à l’état initial (G=0, PC=0, x=0, y=1)
    if state == (0, 0):
        ax.plot(x_vals[0], y_vals[0], 'ro', markersize=12, label='Départ')

    ax.set_xlim(0, 1)
    ax.set_ylim(0, 1)
    ax.set_title(f"État (G={state[0]}, PC={state[1]})")
    ax.set_xlabel("x (Position interne)")
    ax.set_ylabel("y (Position interne)")
    ax.grid()
    ax.legend()

plt.suptitle("Trajectoire du système dans les états de René Thomas (couleur = phase)")
plt.tight_layout(rect=[0, 0, 1, 0.96])
plt.show()
