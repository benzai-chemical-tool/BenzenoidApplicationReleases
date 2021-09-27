package view.draw;

import java.io.File;
import java.io.IOException;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import molecules.Molecule;
import parsers.CMLConverter;
import parsers.ComConverter;
import parsers.ComConverter.ComType;
import view.generator.GeneratorPane;

public class DrawMoleculePane extends GridPane {

	private GeneratorPane parent;

	private MoleculeGroup moleculeGroup;
	private Stage stage;

	public DrawMoleculePane(Stage stage, GeneratorPane parent) {
		super();
		this.stage = stage;
		this.parent = parent;
		initialize();
	}

	@SuppressWarnings("unchecked")
	private void initialize() {

		moleculeGroup = new MoleculeGroup(3);
		moleculeGroup.resize(500, 500);

		this.setPadding(new Insets(20));
		this.setHgap(10);
		this.setVgap(10);

		this.setMinSize(750, 300);

		this.add(moleculeGroup, 0, 0, 1, 4);

		Label nbCrownsLabel = new Label("Number of crowns : ");
		TextField fieldNbCrowns = new TextField();
		Button plusButton = new Button("+");
		Button minusButton = new Button("-");

		fieldNbCrowns.setText("3");
		fieldNbCrowns.setEditable(false);

		plusButton.setOnAction(e -> {
			int nbCrowns = Integer.parseInt(fieldNbCrowns.getText());
			nbCrowns++;
			fieldNbCrowns.setText(Integer.toString(nbCrowns));

			this.getChildren().remove(moleculeGroup);
			moleculeGroup = new MoleculeGroup(nbCrowns);
			this.add(moleculeGroup, 0, 0, 1, 4);
		});

		minusButton.setOnAction(e -> {
			int nbCrowns = Integer.parseInt(fieldNbCrowns.getText());

			if (nbCrowns > 1) {

				nbCrowns--;
				fieldNbCrowns.setText(Integer.toString(nbCrowns));

				this.getChildren().remove(moleculeGroup);
				moleculeGroup = new MoleculeGroup(nbCrowns);
				this.add(moleculeGroup, 0, 0, 1, 4);
			}
		});

		HBox box = new HBox(5.0);
		box.getChildren().addAll(nbCrownsLabel, fieldNbCrowns, minusButton, plusButton);

		this.add(box, 1, 0);

		Label nameLabel = new Label("Name : ");
		TextField nameField = new TextField();

		HBox nameBox = new HBox(5.0);
		nameBox.getChildren().addAll(nameLabel, nameField);

		this.add(nameBox, 1, 1);

		Button clearButton = new Button("Clear");

		clearButton.setOnAction(e -> {
			for (Hexagon hexagon : moleculeGroup.getHexagons())
				hexagon.setLabel(0);
		});

		Button selectAllButton = new Button("Select all");

		selectAllButton.setOnAction(e -> {
			for (Hexagon hexagon : moleculeGroup.getHexagons())
				hexagon.setLabel(1);
		});

		HBox box2 = new HBox(5.0);
		box2.getChildren().addAll(clearButton, selectAllButton);

		this.add(box2, 1, 2);

		Button addButton = new Button("Add benzenoid");

		addButton.setOnAction(e -> {
			Molecule molecule = moleculeGroup.exportMolecule();
			molecule.setDescription(nameField.getText());
			parent.addDrawMolecule(molecule);
		});

		this.add(addButton, 1, 3);

		Button exportButton = new Button("Export Benzenoid");

		@SuppressWarnings("rawtypes")
		ChoiceBox formatChoiceBox = new ChoiceBox();
		formatChoiceBox.getItems().add(".graph");
		formatChoiceBox.getItems().add(".cml");
		formatChoiceBox.getItems().add(".com (ER)");
		formatChoiceBox.getItems().add(".com (IR)");

		HBox exportBox = new HBox(5.0);
		exportBox.getChildren().addAll(exportButton, formatChoiceBox);
		this.add(exportBox, 1, 4);

		exportButton.setOnAction(e -> {

			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Save");
			File file = fileChooser.showSaveDialog(stage);

			if (file != null) {

				Molecule molecule = moleculeGroup.exportMolecule();

				if (formatChoiceBox.getValue().toString().equals(".graph")) {
					try {
						molecule.exportToGraphFile(file);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}

				else if (formatChoiceBox.getValue().toString().equals(".com (ER)")) {
					try {
						ComConverter.generateComFile(molecule, file, 0, ComType.ER, "TODO: add title");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}

				else if (formatChoiceBox.getValue().toString().equals(".com (IR)")) {
					try {
						ComConverter.generateComFile(molecule, file, 0, ComType.IR, "TODO: add title");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}

				else if (formatChoiceBox.getValue().toString().equals(".cml")) {
					try {
						CMLConverter.generateCmlFile(molecule, file);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

	}

}
