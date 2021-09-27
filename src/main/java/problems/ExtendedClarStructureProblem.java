package problems;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

import solution.BenzenoidSolution;

public class ExtendedClarStructureProblem extends ClarCoverProblem {

	public ExtendedClarStructureProblem(BenzenoidSolution benzenoid) {
		super(benzenoid);
		// Model objective function #ronds - 100 #celibataires
		Model clarModel = getModel();
		IntVar OBJ = clarModel.intVar("objectif", -200, 999);
		clarModel.scalar(new IntVar[]{getNbRonds(),getNbCelibataires()}, new int[]{1, -100}, "=", OBJ).post();
		// Specify objective
		clarModel.setObjective(Model.MAXIMIZE, OBJ);
	}

}
