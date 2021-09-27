package solveur;

import molecules.Molecule;

public class Aromaticity {

	public enum RIType {
		NORMAL, OPTIMIZED
	};

	private final int MAX_CYCLE_SIZE = 20;

	private double[] Ri;
	private double[] optimizedRi = new double[] { 0.869, 0.246, 0.100, 0.041 };

	private Molecule molecule;

	private int[][] localCircuits;
	private int[] globalCircuits;

	private double[] localAromaticity;
	private double globalAromaticity;

	public Aromaticity(Molecule molecule, int[][] localCircuits, RIType type) {

		initializeRi();

		this.molecule = molecule;
		this.localCircuits = localCircuits;

		computeGlobalCircuits();
		computeAromaticity(type);
	}

	private void initializeRi() {

		Ri = new double[MAX_CYCLE_SIZE];

		for (int i = 0; i < MAX_CYCLE_SIZE; i++) {
			Ri[i] = 1.0 / ((double) (i + 1) * (double) (i + 1));
		}
	}

	private void computeGlobalCircuits() {

		globalCircuits = new int[localCircuits[0].length];

		for (int i = 0; i < localCircuits.length; i++) {
			for (int j = 0; j < localCircuits[i].length; j++) {
				globalCircuits[j] += localCircuits[i][j];
			}
		}
	}

	private void computeAromaticity(RIType type) {

		double[] chosenRi = null;

		switch (type) {
		case NORMAL:
			chosenRi = Ri;
			break;

		case OPTIMIZED:
			chosenRi = optimizedRi;
			break;
		}

		localAromaticity = new double[localCircuits.length];
		globalAromaticity = 0.0;

		for (int i = 0; i < localCircuits.length; i++) {
			for (int j = 0; j < localCircuits[i].length; j++) {
				localAromaticity[i] += localCircuits[i][j] * chosenRi[j];
				globalAromaticity += localCircuits[i][j] * chosenRi[j];
			}
		}
	}

	public Molecule getMolecule() {
		return molecule;
	}

	public void setMolecule(Molecule molecule) {
		this.molecule = molecule;
	}

	public int[][] getLocalCircuits() {
		return localCircuits;
	}

	public int[] getGlobalCircuits() {
		return globalCircuits;
	}

	public double[] getLocalAromaticity() {
		return localAromaticity;
	}

	public double getGlobalAromaticity() {
		return globalAromaticity;
	}

}
