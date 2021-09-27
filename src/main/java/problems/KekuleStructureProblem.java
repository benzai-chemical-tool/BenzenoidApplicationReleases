package problems;

import solution.BenzenoidSolution;

public class KekuleStructureProblem extends ClarCoverProblem {

	public KekuleStructureProblem(BenzenoidSolution benzenoid) {
		super(benzenoid);
		this.getModel().arithm(this.getNbRonds(), "=", 0).post();
	}

}
