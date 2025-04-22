import cobra
import matplotlib.pyplot as plt
import numpy as np

def create_metabolic_model():
    model = cobra.Model('Liver_Metabolism')
    
    # Métabolites
    glucose = cobra.Metabolite('glucose', compartment='c')
    glycogen = cobra.Metabolite('glycogen', compartment='c')
    atp = cobra.Metabolite('ATP', compartment='c')
    adp = cobra.Metabolite('ADP', compartment='c')
    acetyl_coa = cobra.Metabolite('acetyl_coa', compartment='c')
    pyruvate = cobra.Metabolite('pyruvate', compartment='c')
    ampk = cobra.Metabolite('AMPK', compartment='c')
    
    # Réactions
    glycolysis = cobra.Reaction('glycolysis')
    glycolysis.add_metabolites({glucose: -1, pyruvate: 2, atp: 2})
    glycolysis.lower_bound = -10  # Permettre un certain retour (réversible)
    
    glycogenesis = cobra.Reaction('glycogenesis')
    glycogenesis.add_metabolites({glucose: -1, glycogen: 1})
    glycogenesis.lower_bound = -5  # Permettre stockage/libération
    glycogenesis.upper_bound = 10  # Forcer stockage si ATP élevé
    
    glycogenolysis = cobra.Reaction('glycogenolysis')
    glycogenolysis.add_metabolites({glycogen: -1, glucose: 1})
    glycogenolysis.lower_bound = -5  # Réversible aussi
    glycogenolysis.upper_bound = 5
    
    gluconeogenesis = cobra.Reaction('gluconeogenesis')
    gluconeogenesis.add_metabolites({pyruvate: -2, glucose: 1})
    gluconeogenesis.lower_bound = -5  # Permet retour
    gluconeogenesis.upper_bound = 5
    
    pyruvate_to_acetylcoa = cobra.Reaction('pyruvate_to_acetylcoa')
    pyruvate_to_acetylcoa.add_metabolites({pyruvate: -1, acetyl_coa: 1})
    pyruvate_to_acetylcoa.lower_bound = 0
    
    krebs_cycle = cobra.Reaction('krebs_cycle')
    krebs_cycle.add_metabolites({acetyl_coa: -1, atp: 12})
    krebs_cycle.lower_bound = 0
    
    beta_oxidation = cobra.Reaction('beta_oxidation')
    beta_oxidation.add_metabolites({acetyl_coa: 1, atp: 8})
    beta_oxidation.lower_bound = 0
    
    ampk_activation = cobra.Reaction('ampk_activation')
    ampk_activation.add_metabolites({atp: -2, adp: 1, ampk: 1})
    ampk_activation.lower_bound = 0
    ampk_activation.upper_bound = 10  # Activation forcée à ATP bas
    
    # Ajout d'une source externe de glucose
    glucose_input = cobra.Reaction('glucose_input')
    glucose_input.add_metabolites({glucose: 1})
    glucose_input.lower_bound = 10  # On force un apport de glucose
    glucose_input.upper_bound = 100  # Maximum d'entrée
    
    # Ajout d'une consommation d'ATP plus réaliste
    atp_consumption = cobra.Reaction('atp_consumption')
    atp_consumption.add_metabolites({atp: -5})
    atp_consumption.lower_bound = 50  # Réduire consommation excessive
    atp_consumption.upper_bound = 200
    
    # Ajout des réactions au modèle
    model.add_reactions([glycolysis, glycogenesis, glycogenolysis,
                         gluconeogenesis, pyruvate_to_acetylcoa, krebs_cycle, 
                         beta_oxidation, ampk_activation,
                         glucose_input, atp_consumption])
    
    # Définition de la fonction objectif : Maximiser ATP
    model.objective = 'krebs_cycle'
    
    return model

def solve_fba(model):
    solution = model.optimize()
    return solution

def plot_fluxes(solution):
    reactions = solution.fluxes.index.tolist()
    flux_values = solution.fluxes.values
    
    plt.figure(figsize=(10, 5))
    plt.barh(reactions, flux_values, color='skyblue')
    plt.xlabel("Flux (mmol/h)")
    plt.ylabel("Réactions")
    plt.title("Flux Métaboliques à l'Équilibre (FBA)")
    plt.grid()
    plt.show()

# Exécution
model = create_metabolic_model()
solution = solve_fba(model)
print("\nFlux à l'équilibre:")
print(solution.fluxes)
plot_fluxes(solution)
