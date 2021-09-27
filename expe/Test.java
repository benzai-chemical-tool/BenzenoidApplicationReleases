package expe;

import java.util.ArrayList;
import java.util.HashMap;

import generator.GeneralModel;
import modules.RhombusModule;
import view.generator.GeneratorCriterion;
import view.generator.GeneratorCriterion.Operator;
import view.generator.GeneratorCriterion.Subject;

public class Test {

	// ArrayList<GeneratorCriterion> hexagonsCriterions,
	// ArrayList<GeneratorCriterion> criterions, Map<String,
	// ArrayList<GeneratorCriterion>> mapCriterions

	public static void main(String[] args) {

		HashMap<String, ArrayList<GeneratorCriterion>> map = new HashMap<>();

		ArrayList<GeneratorCriterion> hexagonsCriterions = new ArrayList<>();
		hexagonsCriterions.add(new GeneratorCriterion(Subject.NB_HEXAGONS, Operator.LEQ, "9"));

		ArrayList<GeneratorCriterion> holesCriterions = new ArrayList<>();
		// holesCriterions.add(new GeneratorCriterion(Subject.NB_HOLES, Operator.EQ,
		// "1"));

		map.put("hexagons", hexagonsCriterions);
		map.put("holes", holesCriterions);

		ArrayList<GeneratorCriterion> criterions = new ArrayList<>();
		criterions.addAll(hexagonsCriterions);
		criterions.addAll(holesCriterions);

		GeneralModel m = new GeneralModel(hexagonsCriterions, criterions, map);

		m.addModule(new RhombusModule(m, new ArrayList<>()));

		m.solve();
	}
}
