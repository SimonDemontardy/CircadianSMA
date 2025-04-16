from cobra import Model, Reaction, Metabolite

model = Model("hepatic_atp_production")

# metabolites
ag = Metabolite('ag', compartment='c')
aa = Metabolite('aa', compartment='c')
glycogene = Metabolite('glycogene', compartment='c')
acetyl_coa = Metabolite('acetyl_coa', compartment='c')
pyruvate = Metabolite('pyruvate', compartment='c')
glucose = Metabolite('glucose', compartment='c')
atp = Metabolite('atp', compartment='c')


# imports
# acide gras
r_ag_import = Reaction('ag_import')
r_ag_import.lower_bound = 0  # entrée autorisée
r_ag_import.upper_bound = 10
r_ag_import.add_metabolites({ag: 1})
# acide aminé
#r_aa_import = Reaction('aa_import')
#r_aa_import.lower_bound = 0  # entrée autorisée
#r_aa_import.upper_bound = 5
#r_aa_import.add_metabolites({aa: 1})

# glycogène
r_glycogene_import = Reaction('glycogene_import')
r_glycogene_import.lower_bound = 0  # entrée autorisée
r_glycogene_import.upper_bound = 40
r_glycogene_import.add_metabolites({glycogene: 1})
# reactions interne
# beta-oxydation
r_beta_oxydation = Reaction('beta_oxydation')
r_beta_oxydation.lower_bound = 0
r_beta_oxydation.upper_bound = 15
r_beta_oxydation.add_metabolites({ag: -1, acetyl_coa: 8, atp: 28})

# transamination
#r_transamination = Reaction('transamination')
#r_transamination.lower_bound = 0
#r_transamination.upper_bound = 5
#r_transamination.add_metabolites({aa: -1, pyruvate: 1})

# glycogénolyse
r_glycogenolyse = Reaction('glycogenolyse')
r_glycogenolyse.lower_bound = 0
r_glycogenolyse.upper_bound = 20
r_glycogenolyse.add_metabolites({glycogene: -1, glucose: 1, atp: 1})

# glycolyse
r_glycolyse = Reaction('glycolyse')
r_glycolyse.lower_bound = 0
r_glycolyse.upper_bound = 20
r_glycolyse.add_metabolites({glucose: -1, pyruvate: 2, atp: 2})

# pyruvate -> acetyl-coa
r_pyr_to_acoa = Reaction('pyr_to_acoa')
r_pyr_to_acoa.lower_bound = 0
r_pyr_to_acoa.upper_bound = 10
r_pyr_to_acoa.add_metabolites({pyruvate: -1, acetyl_coa: 1})

# Krebs cycle
r_Krebs = Reaction('krebs_cycle')
r_Krebs.lower_bound = 0
r_Krebs.upper_bound = 40
r_Krebs.add_metabolites({acetyl_coa: -1, atp: 10})

# sink for atp
r_atp_sink = Reaction('sink_atp')
r_atp_sink.lower_bound = 0
r_atp_sink.upper_bound = 1000
r_atp_sink.add_metabolites({atp: -1})

model.add_reactions([
    r_ag_import,
    #r_aa_import,
    r_glycogene_import,
    r_beta_oxydation,
    #r_transamination,
    r_glycogenolyse,
    r_glycolyse,
    r_pyr_to_acoa,
    r_Krebs,
    r_atp_sink
])

from optlang import Constraint

epsilon = 0.05  # marge tolérée

v_ag = model.reactions.get_by_id('ag_import').flux_expression
v_glyco = model.reactions.get_by_id('glycogene_import').flux_expression

# v_ag - 0.25 * v_glyco ∈ [-epsilon, +epsilon]
constraint_lo = model.problem.Constraint(v_ag - 0.25 * v_glyco, ub=epsilon, name="ag_glyco_lo")
constraint_hi = model.problem.Constraint(v_ag - 0.25 * v_glyco, lb=-epsilon, name="ag_glyco_hi")

model.add_cons_vars([constraint_lo, constraint_hi])

# Set the objective function
model.objective = 'sink_atp'


# Solve the model
solution = model.optimize()
# Print the results
print("Solution status:", solution.status)
print("Objective value (maximum ATP production):", solution.objective_value)
print("Flux distribution:")
for rxn in model.reactions:
    print(f"{rxn.id}: {solution.fluxes[rxn.id]}")
