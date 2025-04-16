from cobra import Model, Reaction, Metabolite

# Création du modèle
model = Model('FBA_Consommation_Mode')

# Métabolites
glc = Metabolite('glucose', compartment='c')
glycogene = Metabolite('glycogene', compartment='c')
pyr = Metabolite('pyruvate', compartment='c')
accoa = Metabolite('acetyl_CoA', compartment='c')
oaa = Metabolite('oxaloacetate', compartment='c')
atp = Metabolite('ATP', compartment='c')
ag = Metabolite('fatty_acids', compartment='c')
hcoa = Metabolite('HsCoA', compartment='c')
aa = Metabolite('amino_acids', compartment='c')

# Réactions du graphe
r1 = Reaction('glycogenolysis')
r1.add_metabolites({glycogene: -1, glc: 1})

r2 = Reaction('glycolysis')
r2.add_metabolites({glc: -1, pyr: 1, atp: 2})

r3 = Reaction('transamination')
r3.add_metabolites({aa: -1, pyr: 1})

r4 = Reaction('pyr_to_accoa')
r4.add_metabolites({pyr: -1, accoa: 1, hcoa: -1})

r5 = Reaction('pyr_carboxylase')
r5.add_metabolites({pyr: -1, oaa: 1, atp: -1})

r6 = Reaction('krebs_cycle')
r6.add_metabolites({accoa: -1, oaa: -1, atp: 10, hcoa: 1})

r7 = Reaction('beta_ox')
r7.add_metabolites({ag: -1, accoa: 1, atp: 14})

# Ajout au modèle
model.add_reactions([r1, r2, r3, r4, r5, r6, r7])

# Définir l'objectif sur la production totale d'ATP
atp_production = model.problem.Objective(
    expression=r2.flux_expression * 2 + 
               r5.flux_expression * (-1) + 
               r6.flux_expression * 10 + 
               r7.flux_expression * 14,
    direction='max'
)
model.objective = atp_production

# Optimisation
solution = model.optimize()

# Affichage des résultats
print("Statut :", solution.status)
print("Flux objectif (ATP produit) :", solution.objective_value)
print("\nFlux par réaction :")
for rxn in model.reactions:
    print(f"{rxn.id:15s} : {solution.fluxes[rxn.id]:.3f}")
