package generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import generator.fragments.FragmentResolutionInformations;
import modules.CarbonsHydrogensModule;
import modules.CatacondensedModule;
import modules.CoronoidModule;
import modules.CoronoidModule2;
import modules.DiameterModule;
import modules.ForbiddenFragmentModule1;
import modules.IrregularityModule;
import modules.MultipleFragments1Module;
import modules.RectangleModule;
import modules.RhombusModule;
import modules.SingleFragment1Module;
import modules.SymmetriesModule;
import solving_modes.SymmetryType;
import view.generator.GeneratorCriterion;
import view.generator.GeneratorCriterion.Operator;
import view.generator.GeneratorCriterion.Subject;

public class ModelBuilder {

	public static GeneralModel buildModel(ArrayList<GeneratorCriterion> criterions,
			Map<String, ArrayList<GeneratorCriterion>> map, FragmentResolutionInformations patternsInformations) {

		GeneralModel model;

		if (GeneratorCriterion.containsSubject(criterions, Subject.SYMM_ROT_60) || GeneratorCriterion.containsSubject(criterions, Subject.ROT_60_MIRROR)) {

			int nbMaxHexagons = 0;

			for (GeneratorCriterion criterion : map.get("hexagons")) {

				Operator operator = criterion.getOperator();
				if (operator == Operator.EQ || operator == Operator.LT || operator == Operator.LEQ) {
					int value = Integer.parseInt(criterion.getValue());
					if (value > nbMaxHexagons)
						nbMaxHexagons = value;
				}
			}

			model = new GeneralModel(map.get("hexagons"), criterions, map, (nbMaxHexagons + 10) / 6);
		}

		else if (GeneratorCriterion.containsSubject(criterions, Subject.SYMM_ROT_120)
				|| GeneratorCriterion.containsSubject(criterions, Subject.SYMM_ROT_120_V)
				|| GeneratorCriterion.containsSubject(criterions, Subject.ROT_120_MIRROR_H)
				|| GeneratorCriterion.containsSubject(criterions, Subject.ROT_120_MIRROR_E) 
				|| GeneratorCriterion.containsSubject(criterions, Subject.ROT_120_VERTEX_MIRROR)) {

			int nbMaxHexagons = 0;

			for (GeneratorCriterion criterion : map.get("hexagons")) {

				Operator operator = criterion.getOperator();
				if (operator == Operator.EQ || operator == Operator.LT || operator == Operator.LEQ) {
					int value = Integer.parseInt(criterion.getValue());
					if (value > nbMaxHexagons)
						nbMaxHexagons = value;
				}
			}

			model = new GeneralModel(map.get("hexagons"), criterions, map, (nbMaxHexagons + 4) / 3);
		}

		else if (GeneratorCriterion.containsSubject(criterions, Subject.CORONOID) || 
				 GeneratorCriterion.containsSubject(criterions, Subject.CORONOID_2) || 
				 GeneratorCriterion.containsSubject(criterions, Subject.NB_HOLES)) {
			
			int nbMaxHexagons = 0;

			for (GeneratorCriterion criterion : map.get("hexagons")) {

				Operator operator = criterion.getOperator();
				if (operator == Operator.EQ || operator == Operator.LT || operator == Operator.LEQ) {
					int value = Integer.parseInt(criterion.getValue());
					if (value > nbMaxHexagons)
						nbMaxHexagons = value;
				}
			}
			
			int nbMaxHoles = 0;
			
			for (GeneratorCriterion criterion : map.get("coronoid2")) {

				if (criterion.getSubject() == Subject.NB_HOLES) {
				
					Operator operator = criterion.getOperator();
					if (operator == Operator.EQ || operator == Operator.LT || operator == Operator.LEQ) {
						int value = Integer.parseInt(criterion.getValue());
						if (value > nbMaxHoles)
							nbMaxHoles = value;
					}
				
				}
			}
			
			/*
			 * (holes > 0)
				nbCouronnes = nbHexagones > 4 * holes ? (nbHexagones + 2 - 4 * holes) / 2 : 1;
			else
				nbCouronnes = (nbHexagones + 2) / 2 < nbCouronnes || nbCouronnes == 0 ? (nbHexagones + 2) / 2 : nbCouronnes;
			 */
			/*
			if (nbMaxHoles == 0) {
				nbMaxHoles = ((nbMaxHexagons - 8) / 5) + 1;
			}
			
			int nbCrowns = nbMaxHexagons > 4 * nbMaxHoles ? (nbMaxHexagons + 2 - 4 * nbMaxHoles) / 2 : 1;
			model = new GeneralModel(map.get("hexagons"), criterions, map, nbCrowns);
			*/
			
			if (nbMaxHexagons >= 8 && nbMaxHexagons <= 12)
				nbMaxHoles = 1;
			
			int nbCrowns = (int) Math.floor((((double) ((double) nbMaxHexagons + 1)) / 2.0) + 1.0);;
			
			if (nbMaxHoles == 0) {
				nbCrowns = (nbCrowns + 2) / 2 < nbCrowns || nbCrowns == 0 ? (nbCrowns + 2) / 2 : nbCrowns;
			}
			
			else {
				nbCrowns = nbMaxHexagons > 4 * nbMaxHoles ? (nbMaxHexagons + 2 - 4 * nbMaxHoles) / 2 : 1;
			}
			
			model = new GeneralModel(map.get("hexagons"), criterions, map, nbCrowns);
			
		}
		
		else
			model = new GeneralModel(map.get("hexagons"), criterions, map);

		model.setPatternsInformations(patternsInformations);
		
		if (map.get("carbons_hydrogens").size() > 0)
			model.addModule(new CarbonsHydrogensModule(model, map.get("carbons_hydrogens")));

		if (GeneratorCriterion.containsSubject(criterions, Subject.VIEW_IRREG))
			model.addModule(new IrregularityModule(model, map.get("irregularity")));

		if (map.get("diameter").size() > 0)
			model.addModule(new DiameterModule(model, map.get("diameter")));

		if (GeneratorCriterion.containsSubject(criterions, Subject.RECTANGLE))
			model.addModule(new RectangleModule(model, map.get("rectangle")));

		if (GeneratorCriterion.containsSubject(criterions, Subject.RHOMBUS))
			model.addModule(new RhombusModule(model, map.get("rhombus")));
		
		if (GeneratorCriterion.containsSubject(criterions, Subject.CORONOID))
			model.addModule(new CoronoidModule(model));

		if (GeneratorCriterion.containsSubject(criterions, Subject.CORONOID_2))
			model.addModule(new CoronoidModule2(model, map.get("coronoid2")));
		
		if (GeneratorCriterion.containsSubject(criterions, Subject.CATACONDENSED))
			model.addModule(new CatacondensedModule(model));

		/*
		 * Symmetries
		 */

		if (GeneratorCriterion.containsSubject(criterions, Subject.ROT_60_MIRROR)) {
			model.addModule(new SymmetriesModule(model, SymmetryType.MIRROR));
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_60));
		}

		if (GeneratorCriterion.containsSubject(criterions, Subject.ROT_120_VERTEX_MIRROR)) {
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_120_VERTEX));
			model.addModule(new SymmetriesModule(model, SymmetryType.VERTICAL));
		}

		if (GeneratorCriterion.containsSubject(criterions, Subject.ROT_120_MIRROR_H)) {
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_120));
			model.addModule(new SymmetriesModule(model, SymmetryType.MIRROR));
		}

		if (GeneratorCriterion.containsSubject(criterions, Subject.ROT_120_MIRROR_E)) {
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_120));
			model.addModule(new SymmetriesModule(model, SymmetryType.VERTICAL));
		}

		if (GeneratorCriterion.containsSubject(criterions, Subject.ROT_180_EDGE_MIRROR)) {
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_180_EDGE));
			model.addModule(new SymmetriesModule(model, SymmetryType.VERTICAL));
		}

		if (GeneratorCriterion.containsSubject(criterions, Subject.ROT_180_MIRROR)) {
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_180));
			model.addModule(new SymmetriesModule(model, SymmetryType.MIRROR));
		}

		if (GeneratorCriterion.containsSubject(criterions, Subject.SYMM_MIRROR))
			model.addModule(new SymmetriesModule(model, SymmetryType.MIRROR));

		if (GeneratorCriterion.containsSubject(criterions, Subject.SYMM_ROT_60))
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_60));

		if (GeneratorCriterion.containsSubject(criterions, Subject.SYMM_ROT_120))
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_120));

		if (GeneratorCriterion.containsSubject(criterions, Subject.SYMM_ROT_180))
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_180));

		if (GeneratorCriterion.containsSubject(criterions, Subject.SYMM_VERTICAL))
			model.addModule(new SymmetriesModule(model, SymmetryType.VERTICAL));

		if (GeneratorCriterion.containsSubject(criterions, Subject.SYMM_ROT_120_V))
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_120_VERTEX));

		if (GeneratorCriterion.containsSubject(criterions, Subject.SYMM_ROT_180_E))
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_180_EDGE));

		/*
		 * Patterns
		 */

		if (GeneratorCriterion.containsSubject(map.get("patterns"), Subject.SINGLE_PATTERN))
			model.addModule(new SingleFragment1Module(model, patternsInformations.getFragments().get(0),
					VariableStrategy.FIRST_FAIL, ValueStrategy.INT_MAX, OrderStrategy.CHANNELING_FIRST));

		if (GeneratorCriterion.containsSubject(map.get("patterns"), Subject.MULTIPLE_PATTERNS))
			model.addModule(new MultipleFragments1Module(model, patternsInformations.getFragments(),
					VariableStrategy.FIRST_FAIL, ValueStrategy.INT_MAX, OrderStrategy.CHANNELING_FIRST));

		if(GeneratorCriterion.containsSubject(map.get("patterns"), Subject.FORBIDDEN_PATTERN))
			model.addModule(new ForbiddenFragmentModule1(model, patternsInformations.getFragments().get(0),
					VariableStrategy.FIRST_FAIL, ValueStrategy.INT_MAX, OrderStrategy.CHANNELING_FIRST));
		
		return model;
	}

	public static GeneralModel buildModel(ArrayList<GeneratorCriterion> criterions,
			Map<String, ArrayList<GeneratorCriterion>> map, int nbCrowns, FragmentResolutionInformations patternsInformations) {

		GeneralModel model;

		model = new GeneralModel(map.get("hexagons"), criterions, map, nbCrowns);

		if (map.get("carbons_hydrogens").size() > 0)
			model.addModule(new CarbonsHydrogensModule(model, map.get("carbons_hydrogens")));

		if (GeneratorCriterion.containsSubject(criterions, Subject.VIEW_IRREG))
			model.addModule(new IrregularityModule(model, map.get("irregularity")));

		if (map.get("diameter").size() > 0)
			model.addModule(new DiameterModule(model, map.get("diameter")));

		if (GeneratorCriterion.containsSubject(criterions, Subject.RECTANGLE))
			model.addModule(new RectangleModule(model, map.get("rectangle")));

		if (GeneratorCriterion.containsSubject(criterions, Subject.RHOMBUS))
			model.addModule(new RhombusModule(model, map.get("rhombus")));
		
		if (GeneratorCriterion.containsSubject(criterions, Subject.CORONOID))
			model.addModule(new CoronoidModule(model));

		if (GeneratorCriterion.containsSubject(criterions, Subject.CORONOID_2))
			model.addModule(new CoronoidModule2(model, map.get("coronoid2")));
		
		if (GeneratorCriterion.containsSubject(criterions, Subject.CATACONDENSED))
			model.addModule(new CatacondensedModule(model));

		/*
		 * Symmetries
		 */

		if (GeneratorCriterion.containsSubject(criterions, Subject.ROT_60_MIRROR)) {
			model.addModule(new SymmetriesModule(model, SymmetryType.MIRROR));
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_60));
		}

		if (GeneratorCriterion.containsSubject(criterions, Subject.ROT_120_VERTEX_MIRROR)) {
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_120_VERTEX));
			model.addModule(new SymmetriesModule(model, SymmetryType.VERTICAL));
		}

		if (GeneratorCriterion.containsSubject(criterions, Subject.ROT_120_MIRROR_H)) {
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_120));
			model.addModule(new SymmetriesModule(model, SymmetryType.MIRROR));
		}

		if (GeneratorCriterion.containsSubject(criterions, Subject.ROT_120_MIRROR_E)) {
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_120));
			model.addModule(new SymmetriesModule(model, SymmetryType.VERTICAL));
		}

		if (GeneratorCriterion.containsSubject(criterions, Subject.ROT_180_EDGE_MIRROR)) {
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_180_EDGE));
			model.addModule(new SymmetriesModule(model, SymmetryType.VERTICAL));
		}

		if (GeneratorCriterion.containsSubject(criterions, Subject.ROT_180_MIRROR)) {
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_180));
			model.addModule(new SymmetriesModule(model, SymmetryType.MIRROR));
		}

		if (GeneratorCriterion.containsSubject(criterions, Subject.SYMM_MIRROR))
			model.addModule(new SymmetriesModule(model, SymmetryType.MIRROR));

		if (GeneratorCriterion.containsSubject(criterions, Subject.SYMM_ROT_60))
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_60));

		if (GeneratorCriterion.containsSubject(criterions, Subject.SYMM_ROT_120))
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_120));

		if (GeneratorCriterion.containsSubject(criterions, Subject.SYMM_ROT_180))
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_180));

		if (GeneratorCriterion.containsSubject(criterions, Subject.SYMM_VERTICAL))
			model.addModule(new SymmetriesModule(model, SymmetryType.VERTICAL));

		if (GeneratorCriterion.containsSubject(criterions, Subject.SYMM_ROT_120_V))
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_120_VERTEX));

		if (GeneratorCriterion.containsSubject(criterions, Subject.SYMM_ROT_180_E))
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_180_EDGE));

		/*
		 * Patterns
		 */

		if (GeneratorCriterion.containsSubject(map.get("patterns"), Subject.SINGLE_PATTERN))
			model.addModule(new SingleFragment1Module(model, patternsInformations.getFragments().get(0),
					VariableStrategy.FIRST_FAIL, ValueStrategy.INT_MAX, OrderStrategy.CHANNELING_FIRST));

		if (GeneratorCriterion.containsSubject(map.get("patterns"), Subject.MULTIPLE_PATTERNS))
			model.addModule(new MultipleFragments1Module(model, patternsInformations.getFragments(),
					VariableStrategy.FIRST_FAIL, ValueStrategy.INT_MAX, OrderStrategy.CHANNELING_FIRST));

		if(GeneratorCriterion.containsSubject(map.get("patterns"), Subject.FORBIDDEN_PATTERN))
			model.addModule(new ForbiddenFragmentModule1(model, patternsInformations.getFragments().get(0),
					VariableStrategy.FIRST_FAIL, ValueStrategy.INT_MAX, OrderStrategy.CHANNELING_FIRST));
		
		return model;
	}
	
	public static GeneralModel buildModel(ArrayList<GeneratorCriterion> criterions,
			Map<String, ArrayList<GeneratorCriterion>> map, int nbCrowns) {

		GeneralModel model = new GeneralModel(map.get("hexagons"), criterions, map, nbCrowns);

		if (map.get("carbons_hydrogens").size() > 0)
			model.addModule(new CarbonsHydrogensModule(model, map.get("carbons_hydrogens")));

		if (GeneratorCriterion.containsSubject(criterions, Subject.VIEW_IRREG))
			model.addModule(new IrregularityModule(model, map.get("irregularity")));

		if (map.get("diameter").size() > 0)
			model.addModule(new DiameterModule(model, map.get("diameter")));

		if (GeneratorCriterion.containsSubject(criterions, Subject.RECTANGLE))
			model.addModule(new RectangleModule(model, map.get("rectangle")));

		if (GeneratorCriterion.containsSubject(criterions, Subject.CORONOID))
			model.addModule(new CoronoidModule(model));

		if (GeneratorCriterion.containsSubject(criterions, Subject.CATACONDENSED))
			model.addModule(new CatacondensedModule(model));

		/*
		 * Symmetries
		 */

		if (GeneratorCriterion.containsSubject(criterions, Subject.ROT_60_MIRROR)) {
			model.addModule(new SymmetriesModule(model, SymmetryType.MIRROR));
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_60));
		}

		if (GeneratorCriterion.containsSubject(criterions, Subject.ROT_120_VERTEX_MIRROR)) {
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_120_VERTEX));
			model.addModule(new SymmetriesModule(model, SymmetryType.VERTICAL));
		}

		if (GeneratorCriterion.containsSubject(criterions, Subject.ROT_120_MIRROR_H)) {
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_120));
			model.addModule(new SymmetriesModule(model, SymmetryType.MIRROR));
		}

		if (GeneratorCriterion.containsSubject(criterions, Subject.ROT_120_MIRROR_E)) {
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_120));
			model.addModule(new SymmetriesModule(model, SymmetryType.VERTICAL));
		}

		if (GeneratorCriterion.containsSubject(criterions, Subject.ROT_180_EDGE_MIRROR)) {
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_180_EDGE));
			model.addModule(new SymmetriesModule(model, SymmetryType.VERTICAL));
		}

		if (GeneratorCriterion.containsSubject(criterions, Subject.ROT_180_MIRROR)) {
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_180));
			model.addModule(new SymmetriesModule(model, SymmetryType.MIRROR));
		}

		if (GeneratorCriterion.containsSubject(criterions, Subject.SYMM_MIRROR))
			model.addModule(new SymmetriesModule(model, SymmetryType.MIRROR));

		if (GeneratorCriterion.containsSubject(criterions, Subject.SYMM_ROT_60))
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_60));

		if (GeneratorCriterion.containsSubject(criterions, Subject.SYMM_ROT_120))
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_120));

		if (GeneratorCriterion.containsSubject(criterions, Subject.SYMM_ROT_180))
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_180));

		if (GeneratorCriterion.containsSubject(criterions, Subject.SYMM_VERTICAL))
			model.addModule(new SymmetriesModule(model, SymmetryType.VERTICAL));

		if (GeneratorCriterion.containsSubject(criterions, Subject.SYMM_ROT_120_V))
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_120_VERTEX));

		if (GeneratorCriterion.containsSubject(criterions, Subject.SYMM_ROT_180_E))
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_180_EDGE));

		return model;
	}

	public static GeneralModel buildModel(ArrayList<GeneratorCriterion> criterions) {

		Map<String, ArrayList<GeneratorCriterion>> map = new HashMap<>();

		map.put("hexagons", new ArrayList<>());
		map.put("carbons_hydrogens", new ArrayList<>());
		map.put("irregularity", new ArrayList<>());
		map.put("diameter", new ArrayList<>());
		map.put("rectangle", new ArrayList<>());
		map.put("rhombus", new ArrayList<>());
		map.put("coronoid", new ArrayList<>());
		map.put("coronoid2", new ArrayList<>());
		map.put("catacondensed", new ArrayList<>());
		map.put("symmetries", new ArrayList<>());
		map.put("patterns", new ArrayList<>());

		for (GeneratorCriterion criterion : criterions) {

			Subject subject = criterion.getSubject();

			if (subject == Subject.NB_HEXAGONS)
				map.get("hexagons").add(criterion);

			else if (subject == Subject.NB_CARBONS || subject == Subject.NB_HYDROGENS)
				map.get("carbons_hydrogens").add(criterion);

			else if (subject == Subject.XI || subject == Subject.N0 || subject == Subject.N1 || subject == Subject.N2
					|| subject == Subject.N3 || subject == Subject.N4)
				map.get("irregularity").add(criterion);

			else if (subject == Subject.RECT_NB_LINES || subject == Subject.RECT_NB_COLUMNS)
				map.get("rectangle").add(criterion);

			else if (subject == Subject.SYMM_MIRROR || subject == Subject.SYMM_ROT_60 || subject == Subject.SYMM_ROT_120
					|| subject == Subject.SYMM_ROT_180 || subject == Subject.SYMM_VERTICAL
					|| subject == Subject.SYMM_ROT_120_V || subject == Subject.SYMM_ROT_180_E
					|| subject == Subject.ROT_60_MIRROR || subject == Subject.ROT_120_MIRROR_H
					|| subject == Subject.ROT_120_MIRROR_E || subject == Subject.ROT_120_VERTEX_MIRROR
					|| subject == Subject.ROT_180_EDGE_MIRROR || subject == Subject.ROT_180_MIRROR)

				map.get("symmetries").add(criterion);

			else if (subject == Subject.DIAMETER)
				map.get("diameter").add(criterion);

			else if (subject == Subject.CORONOID)
				map.get("coronoid").add(criterion);

			else if (subject == Subject.CORONOID_2 || subject == Subject.NB_HOLES)
				map.get("coronoid2").add(criterion);

			else if (subject == Subject.CATACONDENSED)
				map.get("catacondensed").add(criterion);

			else if (subject == Subject.RHOMBUS)
				map.get("rhombus").add(criterion);
		}

		GeneralModel model = new GeneralModel(map.get("hexagons"), criterions, map);

		if (map.get("carbons_hydrogens").size() > 0)
			model.addModule(new CarbonsHydrogensModule(model, map.get("carbons_hydrogens")));

		if (GeneratorCriterion.containsSubject(criterions, Subject.VIEW_IRREG))
			model.addModule(new IrregularityModule(model, map.get("irregularity")));

		if (map.get("diameter").size() > 0)
			model.addModule(new DiameterModule(model, map.get("diameter")));

		if (GeneratorCriterion.containsSubject(criterions, Subject.RECTANGLE))
			model.addModule(new RectangleModule(model, map.get("rectangle")));

		if (GeneratorCriterion.containsSubject(criterions, Subject.RHOMBUS))
			model.addModule(new RhombusModule(model, map.get("rhombus")));

		if (GeneratorCriterion.containsSubject(criterions, Subject.CORONOID))
			model.addModule(new CoronoidModule(model));

		if (GeneratorCriterion.containsSubject(criterions, Subject.CORONOID_2))
			model.addModule(new CoronoidModule2(model, map.get("coronoid2")));

		if (GeneratorCriterion.containsSubject(criterions, Subject.CATACONDENSED))
			model.addModule(new CatacondensedModule(model));

		/*
		 * Symmetries
		 */

		if (GeneratorCriterion.containsSubject(criterions, Subject.ROT_60_MIRROR)) {
			model.addModule(new SymmetriesModule(model, SymmetryType.MIRROR));
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_60));
		}

		if (GeneratorCriterion.containsSubject(criterions, Subject.ROT_120_VERTEX_MIRROR)) {
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_120_VERTEX));
			model.addModule(new SymmetriesModule(model, SymmetryType.VERTICAL));
		}

		if (GeneratorCriterion.containsSubject(criterions, Subject.ROT_120_MIRROR_H)) {
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_120));
			model.addModule(new SymmetriesModule(model, SymmetryType.MIRROR));
		}

		if (GeneratorCriterion.containsSubject(criterions, Subject.ROT_120_MIRROR_E)) {
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_120));
			model.addModule(new SymmetriesModule(model, SymmetryType.VERTICAL));
		}

		if (GeneratorCriterion.containsSubject(criterions, Subject.ROT_180_EDGE_MIRROR)) {
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_180_EDGE));
			model.addModule(new SymmetriesModule(model, SymmetryType.VERTICAL));
		}

		if (GeneratorCriterion.containsSubject(criterions, Subject.ROT_180_MIRROR)) {
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_180));
			model.addModule(new SymmetriesModule(model, SymmetryType.MIRROR));
		}

		if (GeneratorCriterion.containsSubject(criterions, Subject.SYMM_MIRROR))
			model.addModule(new SymmetriesModule(model, SymmetryType.MIRROR));

		if (GeneratorCriterion.containsSubject(criterions, Subject.SYMM_ROT_60))
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_60));

		if (GeneratorCriterion.containsSubject(criterions, Subject.SYMM_ROT_120))
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_120));

		if (GeneratorCriterion.containsSubject(criterions, Subject.SYMM_ROT_180))
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_180));

		if (GeneratorCriterion.containsSubject(criterions, Subject.SYMM_VERTICAL))
			model.addModule(new SymmetriesModule(model, SymmetryType.VERTICAL));

		if (GeneratorCriterion.containsSubject(criterions, Subject.SYMM_ROT_120_V))
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_120_VERTEX));

		if (GeneratorCriterion.containsSubject(criterions, Subject.SYMM_ROT_180_E))
			model.addModule(new SymmetriesModule(model, SymmetryType.ROT_180_EDGE));

		return model;
	}
}
