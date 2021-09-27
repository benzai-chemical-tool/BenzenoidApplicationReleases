package generator;

import java.util.List;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

import problems.ClarCoverProblem;

public class ClarCoverSolver {
	private ClarCoverProblem clarCoverProblem;

	public ClarCoverSolver(ClarCoverProblem clarCoverProblem) {
		super();
		this.clarCoverProblem = clarCoverProblem;
	}

	public Solution solve() {
		Solver clarSolver = clarCoverProblem.getModel().getSolver();

		boolean found = false;
		Solution clarSolution = new Solution(clarCoverProblem.getModel());

		while (clarSolver.solve()) {
			found = true;
			clarSolution.record();
		}

		if (found) {
			System.out.println(clarSolution);
			// new ClarCoverSolution(clarSolution.toString());
		} else
			System.out.println("Pas de structure de Clar");
		return clarSolution;
	}

	public List<Solution> solveAll() {

		Model model = clarCoverProblem.getModel();
		Solver clarSolver = model.getSolver();
		clarSolver.reset();
		IntVar objectif = model.intVar("objectif", -200, 999);
		model.scalar(new IntVar[] { clarCoverProblem.getNbRonds(), clarCoverProblem.getNbCelibataires() },
				new int[] { 1, -100 }, "=", objectif).post();
		// Specify objective
		// model.setObjective(Model.MAXIMIZE, objectif);

		List<Solution> solutions = clarSolver.findAllOptimalSolutions(objectif, Model.MAXIMIZE);
		// clarSolver.solve();
		return solutions;
	}

}
