from cobra import Model, Reaction, Metabolite

# 🔹 Création du modèle
model = Model("hepatic_glucose_storage")

# 🔹 Métabolites (tous dans le cytoplasme)
glucose = Metabolite('glucose', compartment='c')
pyruvate = Metabolite('pyruvate', compartment='c')
acetyl_coa = Metabolite('acetyl_coa', compartment='c')
glycogene = Metabolite('glycogene', compartment='c')
ag = Metabolite('ag', compartment='c')
atp = Metabolite('atp', compartment='c')

# 🔹 Réactions

# Import de glucose
r_glucose_import = Reaction('glucose_import')
r_glucose_import.lower_bound = 0  # entrée autorisée
r_glucose_import.upper_bound = 100
r_glucose_import.add_metabolites({glucose: 1})

# Glycolyse : glucose -> 2 pyruvate + 2 ATP
r_glycolyse = Reaction('glycolyse')
r_glycolyse.lower_bound = 0
r_glycolyse.upper_bound = 30
r_glycolyse.add_metabolites({glucose: -1, pyruvate: 2, atp: 2})

# Pyruvate -> Acetyl-CoA
r_pyr_to_acoa = Reaction('pyr_to_acoa')
r_pyr_to_acoa.lower_bound = 0
r_pyr_to_acoa.upper_bound = 10
r_pyr_to_acoa.add_metabolites({pyruvate: -1, acetyl_coa: 1})

# Glycogénogenèse : glucose -> glycogène + ATP consommé
r_glycogenese = Reaction('glycogenese')
r_glycogenese.lower_bound = 0
r_glycogenese.upper_bound = 12
r_glycogenese.add_metabolites({glucose: -1, glycogene: 1, atp: -1})

# Cycle de Krebs : Acetyl-CoA -> ATP
r_Krebs = Reaction('krebs_cycle')
r_Krebs.lower_bound = 0
r_Krebs.upper_bound = 8
r_Krebs.add_metabolites({acetyl_coa: -1, atp: 10})

# Lipogenèse : 8 Acetyl-CoA + 42 ATP -> AG
r_lipogenese = Reaction('lipogenese')
r_lipogenese.lower_bound = 0
r_lipogenese.upper_bound = 4
r_lipogenese.add_metabolites({acetyl_coa: -8, atp: -42, ag: 1})

# Sink pour AG : permet l'export ou utilisation des AG produits
r_ag_sink = Reaction('sink_ag')
r_ag_sink.lower_bound = 0
r_ag_sink.upper_bound = 1000
r_ag_sink.add_metabolites({ag: -1})

r_glyco_sink = Reaction('sink_glycogene')
r_glyco_sink.lower_bound = 0
r_glyco_sink.upper_bound = 1000
r_glyco_sink.add_metabolites({glycogene: -1})

# 🔹 Ajout des réactions
model.add_reactions([
    r_glucose_import,
    r_glycolyse,
    r_pyr_to_acoa,
    r_glycogenese,
    r_Krebs,
    r_lipogenese,
    r_ag_sink,
    r_glyco_sink
])

from optlang import Constraint

epsilon = 0.25  # petite marge

v_lipo = model.reactions.get_by_id('lipogenese').flux_expression
v_glyco = model.reactions.get_by_id('glycogenese').flux_expression

# v_lipo ≈ 0.25 * v_glyco
# --> v_lipo - 0.25 * v_glyco ∈ [-ε, +ε]
constraint_lo = Constraint(v_lipo - 0.25 * v_glyco, ub=epsilon)
constraint_hi = Constraint(v_lipo - 0.25 * v_glyco, lb=-epsilon)

model.solver.add(constraint_lo)
model.solver.add(constraint_hi)


# Objectif : maximiser stockage total
model.objective = model.problem.Objective(
    v_lipo + v_glyco, direction='max'
)


# Optimisation
solution = model.optimize()

# Résultats
print("Objectif (stockage total AG + glycogène) :", solution.objective_value)
print("Flux par réaction :")
for rxn in model.reactions:
    print(f"{rxn.id}: {solution.fluxes[rxn.id]}")
