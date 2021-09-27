package solveur;

import java.util.List;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;

import problems.KekuleStructureProblem;

public class KekuleStructureSolver {
	private KekuleStructureProblem kekuleStructureProblem;

	public KekuleStructureSolver(KekuleStructureProblem kekuleStructureProblem) {
		super();
		this.kekuleStructureProblem = kekuleStructureProblem;
	}

	public Solution solve() {
		Solver solver = kekuleStructureProblem.getModel().getSolver();

		boolean found = false;
		Solution kekuleSolution = new Solution(kekuleStructureProblem.getModel());

		while (solver.solve()) {
			found = true;
			kekuleSolution.record();
		}

		if (found) {
			System.out.println(kekuleSolution);
			// new ClarCoverSolution(clarSolution.toString());
		} else
			System.out.println("Pas de structure de Kekule");
		return kekuleSolution;
	}

	public List<Solution> solveAll() {

		Model model = kekuleStructureProblem.getModel();
		Solver solver = model.getSolver();
		solver.reset();

		List<Solution> solutions = solver.findAllOptimalSolutions(kekuleStructureProblem.getNbCelibataires(),
				Model.MINIMIZE);
		// clarSolver.solve();
		return solutions;
	}

}
