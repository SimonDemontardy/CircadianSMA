from cobra import Model, Reaction, Metabolite
from optlang.symbolics import Zero

# Création du modèle
model = Model('Circadian_Glucose_Model')

# ----- Métabolites -----
glc = Metabolite('glucose', compartment='c')
pyr = Metabolite('pyruvate', compartment='c')
gly = Metabolite('glycogene', compartment='c')
accoa = Metabolite('acetyl_CoA', compartment='c')
fa = Metabolite('fatty_acids', compartment='c')
atp = Metabolite('atp', compartment='c')
hscoa = Metabolite('HSCoA', compartment='c')

# ----- Réactions -----
# 1. Glycolyse
glycolysis = Reaction('glycolysis')
glycolysis.add_metabolites({glc: -1, pyr: 1, atp: 2})

# 2. Glycogenese
glycogenesis = Reaction('glycogenesis')
glycogenesis.add_metabolites({glc: -1, gly: 1})

# 3. Glycogenolyse
glycogenolysis = Reaction('glycogenolysis')
glycogenolysis.add_metabolites({gly: -1, glc: 1})

# 4. Pyruvate -> AcCoA
pyr_to_accoa = Reaction('pyr_to_accoa')
pyr_to_accoa.add_metabolites({pyr: -1, accoa: 1, hscoa: -1})

# 5. Lipogenese
lipogenesis = Reaction('lipogenesis')
lipogenesis.add_metabolites({accoa: -1, fa: 1})

# 6. Beta-oxydation
beta_ox = Reaction('beta_ox')
beta_ox.add_metabolites({fa: -1, accoa: 1, atp: 14})

# 7. Neoglucogenese
neogluco = Reaction('neoglucogenesis')
neogluco.add_metabolites({pyr: -1, glc: 1})

# 8. Cycle de Krebs
krebs = Reaction('krebs')
krebs.add_metabolites({accoa: -1, atp: 10, hscoa: 1})

# ----- Import de glucose -----
glc_exchange = Reaction('EX_glucose')
glc_exchange.add_metabolites({glc: 1})
glc_exchange.lower_bound = -100  # entrée possible
glc_exchange.upper_bound = 0     # pas de sortie

# Ajout au modèle
model.add_reactions([glycolysis, glycogenesis, glycogenolysis,
                     pyr_to_accoa, lipogenesis, beta_ox,
                     neogluco, krebs, glc_exchange])

# ----- Contraintes de base -----
atp_seuil = 20
model.add_cons_vars([
    model.problem.Constraint(
        expression=2 * glycolysis.flux_expression +
                  14 * beta_ox.flux_expression +
                  10 * krebs.flux_expression,
        lb=atp_seuil,
        name='atp_min_requirement'
    )
])

# Optimisation
solution = model.optimize()

# Affichage des flux
print("\nFlux optimaux :")
for rxn in model.reactions:
    print(f"{rxn.id}: {rxn.flux:.2f}")
