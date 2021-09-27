package classifier;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import molecules.Molecule;

public class IrregularityClassifier extends Classifier {

	private double step;

	public IrregularityClassifier(HashMap<String, MoleculeInformation> moleculesInformations, double step)
			throws IOException {
		super(moleculesInformations);
		this.step = step;
	}

	public static Irregularity computeParameterOfIrregularity(Molecule molecule) {

		if (molecule.getNbHexagons() == 1)
			return null;

		int[] N = new int[4];
		int[] checkedNodes = new int[molecule.getNbNodes()];

		ArrayList<Integer> V = new ArrayList<Integer>();

		for (int u = 0; u < molecule.getNbNodes(); u++) {
			int degree = molecule.degree(u);
			if (degree == 2 && !V.contains(u)) {
				V.add(u);
				checkedNodes[u] = 0;
			}

			else if (degree != 2)
				checkedNodes[u] = -1;
		}

		ArrayList<Integer> candidats = new ArrayList<Integer>();

		while (true) {

			int firstVertice = -1;
			for (Integer u : V) {
				if (checkedNodes[u] == 0) {
					firstVertice = u;
					break;
				}
			}

			if (firstVertice == -1)
				break;

			candidats.add(firstVertice);
			checkedNodes[firstVertice] = 1;

			int nbNeighbors = 1;

			while (candidats.size() > 0) {

				int candidat = candidats.get(0);

				for (int i = 0; i < molecule.getNbNodes(); i++) {
					if (molecule.getAdjacencyMatrix()[candidat][i] == 1 && checkedNodes[i] == 0) {

						checkedNodes[i] = 1;
						nbNeighbors++;
						candidats.add(i);
					}
				}

				candidats.remove(candidats.get(0));
			}

			N[nbNeighbors - 1] += nbNeighbors;
		}

		double XI = ((double) N[2] + (double) N[3]) / ((double) N[0] + (double) N[1] + (double) N[2] + (double) N[3]);
		return new Irregularity(N, XI);
	}

	@Override
	public ArrayList<PAHClass> classify() {

		int nbClasses = (int) (1.0 / step);

		System.out.println("step = " + step);
		System.out.println("nbClasses = " + nbClasses);

		ArrayList<PAHClass> classes = new ArrayList<PAHClass>();

		double xiMin = 0;
		double xiMax = step;

		double[] steps = new double[nbClasses];

		for (int i = 0; i < nbClasses; i++) {

			BigDecimal bdXiMin = new BigDecimal(xiMin).setScale(1, RoundingMode.HALF_UP);
			BigDecimal bdXiMax = new BigDecimal(xiMax).setScale(1, RoundingMode.HALF_UP);

			String title;
			if (i == 0)
				title = "irregularity <= " + step;
			else
				title = "irregularity (> " + bdXiMin.doubleValue() + " AND <= " + bdXiMax.doubleValue() + ")";

			PAHClass PAHClass = new PAHClass(title, moleculesInformations);
			classes.add(PAHClass);

			// BigDecimal bd = new BigDecimal(xiMax).setScale(1, RoundingMode.HALF_UP);

			steps[i] = bdXiMax.doubleValue();

			xiMin += step;
			xiMax += step;
		}

		Iterator<Entry<String, MoleculeInformation>> it = moleculesInformations.entrySet().iterator();
		while (it.hasNext()) {

			@SuppressWarnings("rawtypes")
			Map.Entry pair = (Map.Entry) it.next();

			MoleculeInformation moleculeInformation = (MoleculeInformation) pair.getValue();

			String moleculeName = moleculeInformation.getMoleculeName();
			Molecule molecule = moleculeInformation.getMolecule();

			Irregularity irregularity = computeParameterOfIrregularity(molecule);

			if (irregularity != null) {

				double XI = irregularity.getXI();

				int index = 0;

				for (int j = 0; j < nbClasses; j++) {

					if (XI <= steps[j]) {
						index = j;
						classes.get(index).addMolecule(moleculeName);
						break;
					}
				}
			}
		}
		/*
		 * for (int i = 0 ; i < classes.size() ; i++) { if
		 * (classes.get(i).getMoleculesNames().size() == 0) { classes.remove(i); i --; }
		 * }
		 */
		return classes;
	}
}
