package modules;

import java.util.ArrayList;

import generator.GeneralModel;
import view.generator.GeneratorCriterion;

public class RhombusModule extends RectangleModule {

	public RhombusModule(GeneralModel generalModel, ArrayList<GeneratorCriterion> criterions) {
		super(generalModel, criterions);
	}

	@Override
	public void postConstraints() {

		super.postConstraints();
		generalModel.getProblem().arithm(rotation, "=", 1).post();
		generalModel.getProblem().arithm(xH, "=", xW).post();
	}
}
