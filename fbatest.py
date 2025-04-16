import cobra
from cobra.io import load_model

model = load_model("textbook")
solution = model.optimize()
print(solution)
print(cobra.util.solver.solvers)