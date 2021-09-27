package view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.chocosolver.solver.Solution;

import generator.ClarCoverSolver;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import molecules.Molecule;
import problems.ClarCoverProblem;
import problems.ExtendedClarStructureProblem;
import problems.KekuleStructureProblem;
import solution.BenzenoidSolution;
import solution.ClarCoverSolution;
import solution.SolutionBuilder;
import solution.StatistiqueStructure;
import solution.outpout.ClarCoverWindowOutput;
import solution.outpout.RBOWindowOutput;
import solution.outpout.SolutionOutput;
import solution.outpout.SolutionWindowOutput;
import solveur.Approximation;
import solveur.Aromaticity;
import solveur.KekuleStructureSolver;
import solveur.LinSolver;

public class MoleculePane extends GridPane {

	private ArrayList<Molecule> molecules;

	private Molecule selectedMolecule;
	private String selectedName;
	private int index;

	private Group moleculeGroup;

	private Group[] linGroups;
	private Group[] linFanGroups;
	private Group[] clarGroups;
	private Group[] rboGroups;

	private Label nameLabel;

	private Label hexagonInformations;

	ArrayList<BenzenoidSolution> benzenoidSolutions;

	private ArrayList<int[]> allHexagonsCorrespondances;

	public MoleculePane(ArrayList<Molecule> molecules, ArrayList<BenzenoidSolution> benzenoidSolutions,
			ArrayList<int []> allHexagonsCorrespondances) {
		super();

		linGroups = new Group[molecules.size()];
		linFanGroups = new Group[molecules.size()];
		clarGroups = new Group[molecules.size()];
		rboGroups = new Group[molecules.size()];

		this.molecules = molecules;
		index = 0;
		selectedMolecule = molecules.get(0);
		selectedName = molecules.get(0).toString();
		moleculeGroup = new MoleculeGroup(selectedMolecule);
		this.benzenoidSolutions = benzenoidSolutions;
		this.allHexagonsCorrespondances = allHexagonsCorrespondances;
		initialize();
	}

	private void initialize() {

		this.setPadding(new Insets(20));
		this.setHgap(10);
		this.setVgap(10);

		this.add(moleculeGroup, 0, 0, 1, 5);

		nameLabel = new Label(selectedName);
		this.add(nameLabel, 1, 0);

		GridPane.setHalignment(nameLabel, HPos.CENTER);

		Button reLinButton = new Button("Resonance Energy (Lin CP)");
		Button reLinFanButton = new Button("Resonance Energy (Lin & Fan CP)");
		Button clarCoverButton = new Button("Clar cover");
		Button RBOButton = new Button("Ring Bound Order");

		reLinButton.setMaxWidth(Double.MAX_VALUE);
		reLinFanButton.setMaxWidth(Double.MAX_VALUE);
		clarCoverButton.setMaxWidth(Double.MAX_VALUE);
		RBOButton.setMaxWidth(Double.MAX_VALUE);

		reLinButton.setOnAction(e -> {
			System.out.println("RE Lin CP");
			try {

				if (linGroups[index] == null) {

					Aromaticity aromaticity = Approximation.solve(selectedMolecule);

					int[][] circuits = aromaticity.getLocalCircuits();

					for (int i = 0; i < circuits.length; i++) {
						System.out.print("H" + i + " : ");
						for (int j = 0; j < circuits[i].length; j++) {
							System.out.print(circuits[i][j] + " ");
						}
						System.out.println("");
					}

					this.getChildren().remove(moleculeGroup);
					moleculeGroup = new AromaticityGroup(this, selectedMolecule, aromaticity);
					this.add(moleculeGroup, 0, 0, 1, 5);

					linGroups[index] = moleculeGroup;
				}

				else {

					this.getChildren().remove(moleculeGroup);
					moleculeGroup = linGroups[index];
					this.add(moleculeGroup, 0, 0, 1, 5);
				}

			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});

		reLinFanButton.setOnAction(e -> {

			try {

				if (linFanGroups[index] == null) {

					Aromaticity aromaticity = LinSolver.computeEnergy(selectedMolecule);

					int[][] circuits = aromaticity.getLocalCircuits();

					for (int i = 0; i < circuits.length; i++) {
						System.out.print("H" + i + " : ");
						for (int j = 0; j < circuits[i].length; j++) {
							System.out.print(circuits[i][j] + " ");
						}
						System.out.println("");
					}

					this.getChildren().remove(moleculeGroup);
					moleculeGroup = new AromaticityGroup(this, selectedMolecule, aromaticity);
					this.add(moleculeGroup, 0, 0, 1, 5);

					linFanGroups[index] = moleculeGroup;
				}

				else {

					this.getChildren().remove(moleculeGroup);
					moleculeGroup = linFanGroups[index];
					this.add(moleculeGroup, 0, 0, 1, 5);
				}

			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
		});

		clarCoverButton.setOnAction(e -> {

			if (clarGroups[index] == null) {

				BenzenoidSolution benzenoidSolution = benzenoidSolutions.get(index);
				int [] hexagonsCorrespondances = allHexagonsCorrespondances.get(index);
				
				if (benzenoidSolution != null) {
					ExtendedClarStructureProblem clarProblem = new ExtendedClarStructureProblem(benzenoidSolution);
				
					ClarCoverSolver clarSolver = new ClarCoverSolver(clarProblem);
					Solution solution = clarSolver.solve();

					ClarCoverSolution clarCover = new ClarCoverSolution(benzenoidSolution, solution.toString(),
						hexagonsCorrespondances);

					SolutionOutput clarCoverSolutionOutput = new ClarCoverWindowOutput(new FlowPane());
				
					clarCoverSolutionOutput.output(clarCover);

					this.getChildren().remove(moleculeGroup);
					moleculeGroup = ((SolutionWindowOutput) clarCoverSolutionOutput).getDrawing();
					this.add(moleculeGroup, 0, 0, 1, 5);

					clarGroups[index] = moleculeGroup;
				}
				
				else {
					Molecule molecule = molecules.get(index);
					benzenoidSolution = SolutionBuilder.buildBenzenoidSolution(molecule);
					
					hexagonsCorrespondances = benzenoidSolution.getHexagonsCorrespondances();
					
					ExtendedClarStructureProblem clarProblem = new ExtendedClarStructureProblem(benzenoidSolution);
					
					ClarCoverSolver clarSolver = new ClarCoverSolver(clarProblem);
					Solution solution = clarSolver.solve();

					ClarCoverSolution clarCover = new ClarCoverSolution(benzenoidSolution, solution.toString(),
						hexagonsCorrespondances);

					SolutionOutput clarCoverSolutionOutput = new ClarCoverWindowOutput(new FlowPane());
				
					clarCoverSolutionOutput.output(clarCover);

					this.getChildren().remove(moleculeGroup);
					moleculeGroup = ((SolutionWindowOutput) clarCoverSolutionOutput).getDrawing();
					this.add(moleculeGroup, 0, 0, 1, 5);

					clarGroups[index] = moleculeGroup;
					
				}
			}

			else {

				this.getChildren().remove(moleculeGroup);
				moleculeGroup = clarGroups[index];
				this.add(moleculeGroup, 0, 0, 1, 5);
			}

		});

		RBOButton.setOnAction(e -> {

			if (rboGroups[index] == null) {
				
				BenzenoidSolution benzenoidSolution = benzenoidSolutions.get(index);
				int [] hexagonsCorrespondances = allHexagonsCorrespondances.get(index);
				
				if (benzenoidSolution != null) {
				
					ClarCoverProblem clarPb = new ClarCoverProblem(benzenoidSolution);
					ClarCoverSolver clarSolve = new ClarCoverSolver(clarPb);
					List<Solution> coverSolutions = clarSolve.solveAll();
					List<ClarCoverSolution> covers = new ArrayList<ClarCoverSolution>();
					for (Solution coverSolution : coverSolutions) {
						ClarCoverSolution cover = new ClarCoverSolution(benzenoidSolution, coverSolution.toString(),
								hexagonsCorrespondances);
						covers.add(cover);
					}

					KekuleStructureProblem pb = new KekuleStructureProblem(benzenoidSolution);
					KekuleStructureSolver sol = new KekuleStructureSolver(pb);
					List<Solution> kekuleSolutions = sol.solveAll();
					List<ClarCoverSolution> kekuleCovers = new ArrayList<ClarCoverSolution>();
					for (Solution kekuleSolution : kekuleSolutions) {
						ClarCoverSolution kekuleCover = new ClarCoverSolution(benzenoidSolution, kekuleSolution.toString(),
								hexagonsCorrespondances);
						kekuleCovers.add(kekuleCover);
					}

					StatistiqueStructure stats = new StatistiqueStructure(benzenoidSolution, kekuleCovers, covers);
					stats.getStats();

					SolutionOutput solutionOutput = new RBOWindowOutput(new FlowPane(), stats);
					solutionOutput.output(benzenoidSolution);

					this.getChildren().remove(moleculeGroup);
					moleculeGroup = ((SolutionWindowOutput) solutionOutput).getDrawing();
					this.add(moleculeGroup, 0, 0, 1, 5);

					rboGroups[index] = moleculeGroup;
				
				}
				
				else {
					
					Molecule molecule = molecules.get(index);
					benzenoidSolution = SolutionBuilder.buildBenzenoidSolution(molecule);
					
					hexagonsCorrespondances = benzenoidSolution.getHexagonsCorrespondances();
					
					ClarCoverProblem clarPb = new ClarCoverProblem(benzenoidSolution);
					ClarCoverSolver clarSolve = new ClarCoverSolver(clarPb);
					List<Solution> coverSolutions = clarSolve.solveAll();
					List<ClarCoverSolution> covers = new ArrayList<ClarCoverSolution>();
					for (Solution coverSolution : coverSolutions) {
						ClarCoverSolution cover = new ClarCoverSolution(benzenoidSolution, coverSolution.toString(),
								hexagonsCorrespondances);
						covers.add(cover);
					}

					KekuleStructureProblem pb = new KekuleStructureProblem(benzenoidSolution);
					KekuleStructureSolver sol = new KekuleStructureSolver(pb);
					List<Solution> kekuleSolutions = sol.solveAll();
					List<ClarCoverSolution> kekuleCovers = new ArrayList<ClarCoverSolution>();
					for (Solution kekuleSolution : kekuleSolutions) {
						ClarCoverSolution kekuleCover = new ClarCoverSolution(benzenoidSolution, kekuleSolution.toString(),
								hexagonsCorrespondances);
						kekuleCovers.add(kekuleCover);
					}

					StatistiqueStructure stats = new StatistiqueStructure(benzenoidSolution, kekuleCovers, covers);
					stats.getStats();

					SolutionOutput solutionOutput = new RBOWindowOutput(new FlowPane(), stats);
					solutionOutput.output(benzenoidSolution);

					this.getChildren().remove(moleculeGroup);
					moleculeGroup = ((SolutionWindowOutput) solutionOutput).getDrawing();
					this.add(moleculeGroup, 0, 0, 1, 5);

					rboGroups[index] = moleculeGroup;
				}
			}

			else {

				this.getChildren().remove(moleculeGroup);
				moleculeGroup = rboGroups[index];
				this.add(moleculeGroup, 0, 0, 1, 5);
			}
		});

		GridPane.setFillWidth(reLinButton, true);
		GridPane.setFillWidth(reLinFanButton, true);
		GridPane.setFillWidth(clarCoverButton, true);
		GridPane.setFillWidth(RBOButton, true);

		this.add(reLinButton, 1, 1);
		this.add(reLinFanButton, 1, 2);
		this.add(clarCoverButton, 1, 3);
		this.add(RBOButton, 1, 4);

		hexagonInformations = new Label();
		this.add(hexagonInformations, 1, 5);

		hexagonInformations.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		GridPane.setFillWidth(hexagonInformations, true);
		GridPane.setFillHeight(hexagonInformations, true);

		Button beginButton = new Button("|<");
		Button prevButton = new Button("<");
		Button nextButton = new Button(">");
		Button endButton = new Button(">|");

		beginButton.setOnAction(e -> {
			index = 0;
			updateSelectedMolecule();
		});

		prevButton.setOnAction(e -> {
			index = (index - 1) % molecules.size();
			if (index == -1)
				index = molecules.size() - 1;

			updateSelectedMolecule();
		});

		nextButton.setOnAction(e -> {
			index = (index + 1) % molecules.size();
			updateSelectedMolecule();
		});

		endButton.setOnAction(e -> {
			index = molecules.size() - 1;
			updateSelectedMolecule();
		});

		HBox buttonsHBox = new HBox(5);
		buttonsHBox.getChildren().addAll(beginButton, prevButton, nextButton, endButton);
		buttonsHBox.setAlignment(Pos.CENTER);

		this.add(buttonsHBox, 0, 5);

		moleculeGroup.setStyle("-fx-border-color: black;" + "-fx-border-width: 4;" + "-fx-border-radius: 10px;");

		this.autosize();
	}

	private void updateSelectedMolecule() {
		selectedMolecule = molecules.get(index);
		// selectedName = names.get(index);

		selectedName = molecules.get(index).toString();

		this.getChildren().remove(moleculeGroup);
		moleculeGroup = new MoleculeGroup(selectedMolecule);
		this.add(moleculeGroup, 0, 0, 1, 5);

		nameLabel.setText(selectedName);
		hexagonInformations.setText("");
	}

	public void setHexagonInformations(String informations) {
		hexagonInformations.setText(informations);
	}
}
