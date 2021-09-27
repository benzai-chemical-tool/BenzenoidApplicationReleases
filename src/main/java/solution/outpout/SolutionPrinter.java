package solution.outpout;

import solution.BenzenoidSolution;

public class SolutionPrinter implements SolutionOutput {

	@Override
	public void output(BenzenoidSolution benzenoidSolution) {
		System.out.println(benzenoidSolution.getHexagonGraph());
	}

}
