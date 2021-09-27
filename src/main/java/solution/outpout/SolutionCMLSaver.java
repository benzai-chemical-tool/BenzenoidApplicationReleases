package solution.outpout;

import solution.BenzenoidSolution;

public class SolutionCMLSaver implements SolutionOutput {

	@Override
	public void output(BenzenoidSolution benzenoidSolution) {
		benzenoidSolution.saveCML(0, "/Users/nicolasprcovic/Misc/csp/Benzenoides/cml/");
	}

}
