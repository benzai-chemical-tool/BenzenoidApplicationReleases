package view.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import solution.BenzenoidSolution;

public class BenzenoidPane extends GridPane {

	private GeneratorPane parameterPane;

	@SuppressWarnings("unused")
	private int nbCrowns;
	
	@SuppressWarnings("unused")
	private String solution;
	
	private Group benzenoidDraw;
	private String description;

	private int index;

	private boolean isSelected;

	@SuppressWarnings("unused")
	private ArrayList<Integer> verticesSolution;

	private boolean isDrawMolecule;
	
	private BenzenoidSolution benzenoidSolution;
	private int [] hexagonsCorrespondances;
	
	public BenzenoidPane(GeneratorPane parameterPane, int nbCrowns, String solution, Group benzenoidDraw,
			String description, ArrayList<Integer> verticesSolution, int index, boolean isDrawMolecule) {

		super();

		this.parameterPane = parameterPane;

		this.nbCrowns = nbCrowns;
		this.solution = solution;
		this.benzenoidDraw = benzenoidDraw;
		this.description = description;

		this.index = index;

		isSelected = false;

		this.setStyle("-fx-border-color: black;" + "-fx-border-width: 4;" + "-fx-border-radius: 10px;");

		addItems();

		this.verticesSolution = verticesSolution;
		this.isDrawMolecule = isDrawMolecule;
	}

	public BenzenoidPane(GeneratorPane parameterPane, int nbCrowns, String solution, Group benzenoidDraw,
			String description, ArrayList<Integer> verticesSolution, int index, boolean isDrawMolecule, BenzenoidSolution benzenoidSolution, int [] hexagonsCorrespondances) {

		super();

		this.parameterPane = parameterPane;

		this.nbCrowns = nbCrowns;
		this.solution = solution;
		this.benzenoidDraw = benzenoidDraw;
		this.description = description;

		this.index = index;

		isSelected = false;

		this.setStyle("-fx-border-color: black;" + "-fx-border-width: 4;" + "-fx-border-radius: 10px;");

		addItems();

		this.verticesSolution = verticesSolution;
		this.isDrawMolecule = isDrawMolecule;
		
		this.benzenoidSolution = benzenoidSolution;
	}

	
	public int getIndex() {
		return index;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addItems() {

		this.setPadding(new Insets(5));
		this.setHgap(5);
		this.setVgap(5);

		this.add(benzenoidDraw, 0, 0, 2, 2);
		this.add(new Label(description), 2, 0, 2, 2);

		this.setOnMouseClicked(new EventHandler() {

			@Override
			public void handle(Event arg0) {

				if (!isSelected)
					select();

				else
					unselect();

			}

		});

		this.setMinSize(300, 200);

	}

	public void unselect() {

		isSelected = false;
		parameterPane.removeSelectedBenzenoid(this);
		this.setStyle("-fx-border-color: black;" + "-fx-border-width: 4;" + "-fx-border-radius: 10px;");
	}

	public void select() {

		System.out.println("Selecting : " + description);

		parameterPane.addSelectedBenzenoidPane(this);
		isSelected = true;
		setStyle("-fx-border-color: blue;" + "-fx-border-width: 4;" + "-fx-border-radius: 10px;");
	}

	public String getCSVLine(int index) {

		StringBuilder builder = new StringBuilder();

		builder.append(index + "\t");

		String[] splittedDescription = description.split(Pattern.quote("\n"));

		for (int i = 1; i < splittedDescription.length; i++) {

			String line = splittedDescription[i];
			String[] splittedLine = line.split(Pattern.quote(" = "));
			builder.append(splittedLine[1] + "\t");
		}

		return builder.toString();
	}

	public String getCSVHeader() {

		StringBuilder builder = new StringBuilder();

		builder.append("id_solution" + "\t");

		String[] splittedDescription = description.split(Pattern.quote("\n"));

		for (int i = 1; i < splittedDescription.length; i++) {

			String line = splittedDescription[i];
			String[] splittedLine = line.split(Pattern.quote(" = "));
			builder.append(splittedLine[0] + "\t");
		}

		return builder.toString();
	}

	public static void exportToCSV(ArrayList<ArrayList<BenzenoidPane>> allPanes, File file) {

		BufferedWriter writer;
		try {

			writer = new BufferedWriter(new FileWriter(file));

			for (ArrayList<BenzenoidPane> panes : allPanes) {
			
				String header = panes.get(0).getCSVHeader();
				writer.write(header + "\n");

				int index = 1;
				for (BenzenoidPane pane : panes) {

					String line = pane.getCSVLine(index);
					writer.write(line + "\n");

					index++;
				}

			}
			
			writer.close();

		} catch (IOException e1) {

			e1.printStackTrace();
		}

	}
	
	public BenzenoidSolution getBenzenoidSolution() {
		return benzenoidSolution;
	}
	
	public int [] getHexagonsCorrespondances() {
		return hexagonsCorrespondances;
	}
	
	public boolean isDrawMolecule() {
		return isDrawMolecule;
	}
	
	public boolean isSelected() {
		return isSelected;
	}
	
	public ArrayList<Integer> getVerticesSolution() {
		return verticesSolution;
	}
}
