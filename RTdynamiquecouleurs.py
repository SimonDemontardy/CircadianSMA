import numpy as np
import matplotlib.pyplot as plt
import matplotlib.cm as cm

# === Définition des célérités selon l'état (G, PC, C) ===
celerities = {
    (0, 0, 0): (0.20, -0.18), # vkg/pc/C, vkpc
    (0, 1, 0): (-0.16, -0.18), #  vkg/C, vkpc
    (1, 0, 0): (0.25, 0.13), # vkg/pc/C, vkpc/g
    (1, 1, 0): (-0.16, 0.25), # vkg/C, vkpc/g
    # avec cortisol donc pas ressource car inib
    (0, 0, 1): (0.18, -0.18), # vkg/pc, vkpc
    (0, 1, 1): (-0.18, -0.18), #  vkg, vkpc
    (1, 0, 1): (0.25, 0.13), # vkg/pc, vkpc/g
    (1, 1, 1): (-0.18, 0.25), # vkg, vkpc/g
}

# === Paramètres de simulation ===
G, PC, C = 0, 0, 0  # État initial
x, y = 0.0, 1.0     # Position initiale
epsilon = 0.0001
dt = 1
max_time = 36
max_iterations = 5000

# === Stockage des données ===
time_vals, G_vals, PC_vals, C_vals = [], [], [], []
x_vals, y_vals, state_vals = [], [], []
state_trajectories = {(0, 0): ([], [], []), (0, 1): ([], [], []),
                      (1, 0): ([], [], []), (1, 1): ([], [], [])}

# === Boucle de simulation ===
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

    # Saturation des bords
    x = max(0, min(x, 1))
    y = max(0, min(y, 1))

    print(f"[t={t*dt:.1f}h] G={G}, PC={PC}, x={x:.3f}, y={y:.3f}")

    # Transitions d'état
    if G == 1 and PC == 1:
        if x <= epsilon:
            G, PC = 0, 1
            x = 1
        elif y >= 1 - epsilon:
            y = 1

    elif G == 0 and PC == 1:
        if y <= epsilon:
            G, PC = 0, 0
            y = 1
        elif x <= epsilon:
            G, PC = 0, 0
            x = 0

    elif G == 0 and PC == 0:
        if x >= 1 - epsilon:
            G, PC = 1, 0
            x = 0
        elif y <= epsilon:
            G, PC = 1, 0
            y = 0

    elif G == 1 and PC == 0:
        if y >= 1 - epsilon:
            G, PC = 1, 1
            y = 0
        elif x >= 1 - epsilon:
            x = 1

    print(f"✅ Nouveau état: (G={G}, PC={PC}) à t={t*dt:.1f}h, x={x:.3f}, y={y:.3f}\n")
    t += 1

if t >= max_iterations:
    print("max iterations atteint, simulation terminée.")

# === Positions sur le plan (G, PC) → (ligne, colonne) ===
state_positions = {(1, 1): (0, 1), (0, 1): (0, 0), (1, 0): (1, 1), (0, 0): (1, 0)}

# === Affichage : scatter par état ===
fig, axes = plt.subplots(2, 2, figsize=(10, 10))
for state, (row, col) in state_positions.items():
    ax = axes[row][col]
    x_list, y_list, times = state_trajectories[state]
    colors = ['blue' if t < 24 else 'red' for t in times]
    ax.scatter(x_list, y_list, c=colors, label=f"État {state}")
    ax.set_xlim(0, 1)
    ax.set_ylim(0, 1)
    ax.set_title(f"État (G={state[0]}, PC={state[1]})")
    ax.set_xlabel("x")
    ax.set_ylabel("y")
    ax.legend()
    ax.grid()

plt.suptitle("Trajectoires (scatter)")
plt.tight_layout(rect=[0, 0, 1, 0.96])
plt.show()

# === Affichage : courbes continues ===
fig, axes = plt.subplots(2, 2, figsize=(10, 10))
for state, (row, col) in state_positions.items():
    ax = axes[row][col]
    x_list, y_list = state_trajectories[state][0], state_trajectories[state][1]
    ax.plot(x_list, y_list, marker='o', linestyle='-')
    ax.set_xlim(0, 1)
    ax.set_ylim(0, 1)
    ax.set_title(f"État (G={state[0]}, PC={state[1]})")
    ax.set_xlabel("x")
    ax.set_ylabel("y")
    ax.grid()

plt.suptitle("Trajectoires continues")
plt.tight_layout(rect=[0, 0, 1, 0.96])
plt.show()

# === Dynamique temporelle des états ===
plt.figure(figsize=(10, 5))
plt.plot(time_vals, G_vals, label="G (PER/CRY libre)", linestyle='-', marker='o')
plt.plot(time_vals, PC_vals, label="PC (Complexe PER/CRY)", linestyle='-', marker='s')
plt.plot(time_vals, C_vals, label="Cortisol", linestyle='-', marker='x')
plt.xlabel("Temps simulé (h)")
plt.ylabel("État binaire")
plt.title("Dynamique des états")
plt.legend()
plt.grid()
plt.show()

# === Courbes discontinues avec couleurs par segments ===
fig, axes = plt.subplots(2, 2, figsize=(10, 10))
color_cycle = cm.get_cmap('tab20')  # 20 couleurs cycliques

for state, (row, col) in state_positions.items():
    ax = axes[row][col]
    x_list, y_list = state_trajectories[state][0], state_trajectories[state][1]

    if not x_list:
        continue

    # Segmentation par discontinuité (écart > 0.1)
    segments = []
    current_segment = ([x_list[0]], [y_list[0]])

    for i in range(1, len(x_list)):
        dx = x_list[i] - x_list[i - 1]
        dy = y_list[i] - y_list[i - 1]
        if dx ** 2 + dy ** 2 > 0.1:
            segments.append(current_segment)
            current_segment = ([x_list[i]], [y_list[i]])
        else:
            current_segment[0].append(x_list[i])
            current_segment[1].append(y_list[i])
    segments.append(current_segment)

    # Tracé des segments avec couleurs différentes
    for j, (seg_x, seg_y) in enumerate(segments):
        ax.plot(seg_x, seg_y, marker='o', linestyle='-', color=color_cycle(j % 20))

    # Point initial rouge
    if state == (0, 0):
        ax.plot(x_vals[0], y_vals[0], 'ro', markersize=12, label='Départ')

    ax.set_xlim(0, 1)
    ax.set_ylim(0, 1)
    ax.set_title(f"État (G={state[0]}, PC={state[1]})")
    ax.set_xlabel("x")
    ax.set_ylabel("y")
    ax.grid()
    ax.legend()

plt.suptitle("Trajectoires discontinues (couleur = phase)")
plt.tight_layout(rect=[0, 0, 1, 0.96])
plt.show()

# Affichage : scatter avec changement de couleur à chaque transition et point rouge initial
fig, axes = plt.subplots(2, 2, figsize=(10, 10))
state_positions = {(1, 1): (0, 1), (0, 1): (0, 0), (1, 0): (1, 1), (0, 0): (1, 0)}

# Colormap pour les transitions d'états
color_cycle = cm.get_cmap('tab20')

for (state, ax_pos) in state_positions.items():
    ax = axes[ax_pos[0], ax_pos[1]]
    x_list = state_trajectories[state][0]
    y_list = state_trajectories[state][1]
    t_list = state_trajectories[state][2]

    if len(x_list) == 0:
        continue

    # Segmenter les points si saut dans le temps (changement d’état)
    segments = []
    current_segment = [[x_list[0]], [y_list[0]]]

    for i in range(1, len(x_list)):
        if abs(t_list[i] - t_list[i - 1]) > 1.1:  # Si saut dans le temps, c'est qu'on a changé d'état
            segments.append(current_segment)
            current_segment = [[x_list[i]], [y_list[i]]]
        else:
            current_segment[0].append(x_list[i])
            current_segment[1].append(y_list[i])
    segments.append(current_segment)

    # Affichage par segment de couleur différente
    for j, segment in enumerate(segments):
        color = color_cycle(j % 20)
        ax.scatter(segment[0], segment[1], c=[color], s=30)

    # Ajouter le gros point rouge au départ uniquement sur (0,0)
    if state == (0, 0):
        ax.plot(x_vals[0], y_vals[0], 'ro', markersize=12, label='Départ')

    ax.set_xlim(0, 1)
    ax.set_ylim(0, 1)
    ax.set_title(f"État (G={state[0]}, PC={state[1]})")
    ax.set_xlabel("x (Position interne)")
    ax.set_ylabel("y (Position interne)")
    ax.grid()
    ax.legend()

plt.suptitle("Trajectoire du système dans les états de René Thomas (scatter avec transitions)")
plt.tight_layout(rect=[0, 0, 1, 0.96])
plt.show()

import matplotlib.pyplot as plt

fig, axes = plt.subplots(2, 2, figsize=(12, 12))
state_positions = {(1, 1): (0, 1), (0, 1): (0, 0), (1, 0): (1, 1), (0, 0): (1, 0)}

# Attribuer une couleur fixe par état
state_colors = {
    (1, 1): 'blue',
    (0, 1): 'green',
    (0, 0): 'orange',
    (1, 0): 'purple'
}

for state, ax_pos in state_positions.items():
    ax = axes[ax_pos[0], ax_pos[1]]
    x_list, y_list, t_list = state_trajectories[state]

    if len(x_list) == 0:
        continue

    # Découper en sous-passages en fonction du temps (passages discontinus dans un même état)
    passages = []
    current_passage = [[x_list[0]], [y_list[0]], [t_list[0]]]

    for i in range(1, len(x_list)):
        if t_list[i] != t_list[i-1] + 1:  # nouveau passage si discontinuité temporelle
            passages.append(current_passage)
            current_passage = [[x_list[i]], [y_list[i]], [t_list[i]]]
        else:
            current_passage[0].append(x_list[i])
            current_passage[1].append(y_list[i])
            current_passage[2].append(t_list[i])

    passages.append(current_passage)  # ajouter le dernier

    color = state_colors[state]

    for idx, (x_pass, y_pass, t_pass) in enumerate(passages):
        alpha_val = 1.0 if idx == 0 else 0.4  # transparent à partir du 2e passage
        ax.scatter(x_pass, y_pass, c=color, alpha=alpha_val, s=50,
                   label=f"État (G={state[0]}, PC={state[1]})" if idx == 0 else None)

        for x, y, t in zip(x_pass, y_pass, t_pass):
            ax.text(x + 0.01, y + 0.01, f"t={int(t)}", fontsize=8, color='black')

    # Marquer le départ
    if state == (0, 0):
        ax.plot(x_list[0], y_list[0], 'ro', markersize=12, label='Départ')

    ax.set_xlim(0, 1)
    ax.set_ylim(0, 1)
    ax.set_title(f"État (G={state[0]}, PC={state[1]})")
    ax.set_xlabel("x")
    ax.set_ylabel("y")
    ax.grid()
    ax.legend()

plt.suptitle("Trajectoires du formalisme de René Thomas dynamique (Horloges circadiennes)", fontsize=16)
plt.tight_layout(rect=[0, 0, 1, 0.96])
plt.show()


