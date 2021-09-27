package problems;

import solution.BenzenoidSolution;

public class ClarStructureProblem extends ExtendedClarStructureProblem {

	public ClarStructureProblem(BenzenoidSolution benzenoid) {
		super(benzenoid);
		this.getModel().arithm(this.getNbCelibataires(), "=", 0).post();
	}

}
