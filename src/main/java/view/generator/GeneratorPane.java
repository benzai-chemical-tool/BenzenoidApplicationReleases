package view.generator;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import application.BenzenoidApplication;
import generator.GeneralModel;
import generator.ModelBuilder;
import generator.ResultSolver;
import generator.fragments.FragmentResolutionInformations;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import molecules.Molecule;
import parsers.CMLConverter;
import parsers.ComConverter;
import parsers.ComConverter.ComType;
import parsers.GraphCoordFileBuilder;
import parsers.GraphFileBuilder;
import parsers.GraphParser;
import solution.BenzenoidSolution;
import utils.Utils;
import view.MoleculeGroup;
import view.MoleculePane;
import view.draw.DrawMoleculePane;
import view.fragments.GenerateFragmentsPane;
import view.generator.GeneratorCriterion.Operator;
import view.generator.GeneratorCriterion.Subject;
import view.irregularity.IrregularityPane;

public class GeneratorPane extends GridPane {

	private BenzenoidApplication app;

	/*
	 * Solver's data
	 */

	// private ArrayList<Molecule> molecules = new ArrayList<>();
	private ArrayList<ArrayList<Molecule>> generatedMolecules = new ArrayList<>();
	private ArrayList<ResultSolver> resultsSolvers = new ArrayList<>();
	private ArrayList<Molecule> drawMolecules = new ArrayList<>();
	private ArrayList<Molecule> selectedMolecules;
	private ArrayList<String> names;

	/*
	 * JavaFX Components
	 */

	private FlowPane flowPane;
	private ScrollPane scrollPane = new ScrollPane();
	private ListView<GridPane> chosenParameterListView;
	private ArrayList<GridPane> boxItems;
	private ArrayList<GeneratorCriterion> criterions;
	private Button exportAllButton;

	private ArrayList<BenzenoidPane> selectedBenzenoidPanes = new ArrayList<>();
	private ArrayList<BenzenoidPane> drawBenzenoidPanes = new ArrayList<>();
	private ArrayList<ArrayList<BenzenoidPane>> generatedBenzenoidPanes = new ArrayList<>();

	/*
	 * Fragments
	 */

	private GenerateFragmentsPane fragmentPane;
	private Stage fragmentStage;
	private FragmentResolutionInformations fragmentsInformations;

	public GeneratorPane(BenzenoidApplication app) {
		super();
		this.app = app;
		initialize();
	}

	/***
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void initialize() {

		initGeneratorPaneProperties();

		criterions = new ArrayList<>();
		boxItems = new ArrayList<>();

		addNumericalParameterComponent("Nb hexagons", Subject.NB_HEXAGONS, 0);
		//		Label starLabel = new Label("*");
		//		starLabel.setTextFill(Color.RED);

		addNumericalParameterComponent("Nb carbons", Subject.NB_CARBONS, 1);
		addNumericalParameterComponent("Nb hydrogens", Subject.NB_HYDROGENS, 2);
		addIrregularityParameterComponent();
		addNumericalParameterComponent("Diameter", Subject.DIAMETER, 4);
		addRectangleParameterComponent();
		addCoronoidParameterComponent();
		addRhombusParameterComponent();
		addCatacondensedParameterComponent();
		addSymmetryParameterComponent();
		addPatternParameterComponent();

		addChosenParameterListView();

		addClearButton();
		HBox displayIrregularityStatBox = addDisplayIrregularityStatComponent();
		addGenerateButton(displayIrregularityStatBox);
		addChangeDisplayModeButton();
		addExportComponents();
		addSelectUnselectButtons();
		addDrawImportButtons();
		addLegende();

		this.add(scrollPane, 0, 13, 5, 1);
	}

	/***
	 * 
	 */
	private void initGeneratorPaneProperties() {
		scrollPane.setFitToHeight(true);
		scrollPane.setFitToWidth(true);

		this.setPrefWidth(1400);

		this.setPadding(new Insets(20));
		this.setHgap(25);
		this.setVgap(15);

		this.setPrefWidth(this.getPrefWidth());
	}

	/***
	 * 
	 * @param parameterName
	 * @param subject
	 * @param gridLine
	 * @param generatorPane
	 */
	private void addNumericalParameterComponent(String parameterName, Subject subject, int gridLine) {
		Label label = new Label(parameterName + " :");

		ChoiceBox choiceBox = addComparatorChoiceBox();

		TextField textField = new TextField();

		HBox hbox = new HBox();
		hbox.setSpacing(5.0);
		hbox.getChildren().addAll(choiceBox, textField);

		Button addButton = new Button("+");

		addButton.setOnAction(e -> {

			String value = textField.getText();

			if (choiceBox.getValue() != null && Utils.isNumber(value) && !choiceBox.getValue().toString().equals("")) {

				//Subject subject = Subject.NB_CARBONS;
				Operator operator = GeneratorCriterion.getOperator(choiceBox.getValue().toString());

				GeneratorCriterion criterion = new GeneratorCriterion(subject, operator, value);
				this.addEntry(criterion);
			}

			else
				Utils.alert("Invalid entry");
		});

		this.add(label, 0, gridLine);
		this.add(hbox, 1, gridLine);
		this.add(addButton, 2, gridLine);


	}
	/***
	 * 
	 * @return a choice box for any kind of numerical comparators
	 */
	private ChoiceBox addComparatorChoiceBox() {
		ChoiceBox choiceBox = new ChoiceBox();
		choiceBox.getItems().add("<=");
		choiceBox.getItems().add("<");
		choiceBox.getItems().add("=");
		choiceBox.getItems().add(">");
		choiceBox.getItems().add(">=");
		choiceBox.getItems().add("!=");
		return choiceBox;
	}

	/***
	 * 
	 */
	private void addIrregularityParameterComponent() {
		CheckBox irregularityChb = new CheckBox("Irregularity");

		irregularityChb.setOnAction(e -> {

			if (irregularityChb.isSelected())
				addEntry(new GeneratorCriterion(Subject.VIEW_IRREG, Operator.NONE, ""));

			else {

				for (int index = 0; index < criterions.size(); index++) {

					GeneratorCriterion criterion = criterions.get(index);
					if (criterion.getSubject().equals(Subject.VIEW_IRREG) || criterion.getSubject().equals(Subject.XI)
							|| criterion.getSubject().equals(Subject.N0) || criterion.getSubject().equals(Subject.N1)
							|| criterion.getSubject().equals(Subject.N2) || criterion.getSubject().equals(Subject.N3)
							|| criterion.getSubject().equals(Subject.N4)) {

						removeEntry(index);
						index--;
					}

				}
			}
		});

		ChoiceBox cbIrregularitySubject = new ChoiceBox();
		cbIrregularitySubject.getItems().add("XI");
		cbIrregularitySubject.getItems().add("N0");
		cbIrregularitySubject.getItems().add("N1");
		cbIrregularitySubject.getItems().add("N2");
		cbIrregularitySubject.getItems().add("N3");
		cbIrregularitySubject.getItems().add("N4");

		ChoiceBox cbIrregularityOperator = addComparatorChoiceBox();

		TextField irregularityField = new TextField();

		HBox boxIrregularity = new HBox();
		boxIrregularity.setSpacing(5.0);
		boxIrregularity.getChildren().addAll(cbIrregularitySubject, cbIrregularityOperator, irregularityField);

		Button irregularityAddButton = new Button("+");

		irregularityAddButton.setOnAction(e -> {

			if (cbIrregularitySubject.getValue() != null) {

				String subjectStr = cbIrregularitySubject.getValue().toString();
				String value = irregularityField.getText();

				if (Utils.isNumber(value)) {

					double doubleValue = Double.parseDouble(value);

					if (subjectStr.equals("XI") && (doubleValue > 1.0 || doubleValue < 0)) {
						Utils.alert("XI must be >= 0.0 and <= 1.0");
					}

					else {

						if (cbIrregularityOperator.getValue() != null && !subjectStr.equals("")
								&& Utils.isNumber(value)) {

							Subject subject;

							if (subjectStr.equals("XI"))
								subject = Subject.XI;

							else if (subjectStr.equals("N0"))
								subject = Subject.N0;

							else if (subjectStr.equals("N1"))
								subject = Subject.N1;

							else if (subjectStr.equals("N1"))
								subject = Subject.N1;

							else if (subjectStr.equals("N2"))
								subject = Subject.N2;

							else if (subjectStr.equals("N3"))
								subject = Subject.N3;

							else
								subject = Subject.N4;

							Operator operator = GeneratorCriterion
									.getOperator(cbIrregularityOperator.getValue().toString());
							GeneratorCriterion criterion = new GeneratorCriterion(subject, operator, value);
							addEntry(criterion);
						}

						else
							Utils.alert("Invalid fields");
					}
				}

			}
		});

		this.add(irregularityChb, 0, 3);
		this.add(boxIrregularity, 1, 3);
		this.add(irregularityAddButton, 2, 3);
	}

	/***
	 * 
	 */
	private void addRectangleParameterComponent(){
		CheckBox chbRectangle = new CheckBox("Rectangle");

		chbRectangle.setOnAction(e -> {

			if (chbRectangle.isSelected())
				addEntry(new GeneratorCriterion(Subject.RECTANGLE, Operator.NONE, ""));

			else {

				for (int index = 0; index < criterions.size(); index++) {

					GeneratorCriterion criterion = criterions.get(index);
					if (criterion.getSubject().equals(Subject.RECTANGLE)
							|| criterion.getSubject().equals(Subject.RECT_NB_LINES)
							|| criterion.getSubject().equals(Subject.RECT_NB_COLUMNS)) {

						removeEntry(index);
						index--;
					}
				}
			}
		});

		this.add(chbRectangle, 0, 5);

		// nb_lines

		Label linesLabel = new Label("Nb lines : ");

		ChoiceBox cbLines = addComparatorChoiceBox();

		TextField linesField = new TextField();

		HBox boxNbLines = new HBox();
		boxNbLines.setSpacing(5.0);
		boxNbLines.getChildren().addAll(linesLabel, cbLines, linesField);

		Button linesAddButton = new Button("+");

		linesAddButton.setOnAction(e -> {

			String value = linesField.getText();

			if (cbLines.getValue() != null && Utils.isNumber(value) && !cbLines.getValue().toString().equals("")) {

				Subject subject = Subject.RECT_NB_LINES;
				Operator operator = GeneratorCriterion.getOperator(cbLines.getValue().toString());

				GeneratorCriterion criterion = new GeneratorCriterion(subject, operator, value);
				addEntry(criterion);
			}

			else
				Utils.alert("Invalid entry");

		});

		this.add(boxNbLines, 1, 5);
		this.add(linesAddButton, 2, 5);

		// nb_columns

		Label columnsLabel = new Label("Nb columns : ");

		ChoiceBox cbColumns = addComparatorChoiceBox();

		TextField columnsField = new TextField();

		HBox boxNbColumns = new HBox();
		boxNbColumns.setSpacing(5.0);
		boxNbColumns.getChildren().addAll(columnsLabel, cbColumns, columnsField);

		Button columnsAddButton = new Button("+");

		columnsAddButton.setOnAction(e -> {

			String value = columnsField.getText();

			if (cbColumns.getValue() != null && Utils.isNumber(value) && !cbColumns.getValue().toString().equals("")) {

				Subject subject = Subject.RECT_NB_COLUMNS;
				Operator operator = GeneratorCriterion.getOperator(cbColumns.getValue().toString());

				GeneratorCriterion criterion = new GeneratorCriterion(subject, operator, value);
				addEntry(criterion);
			}

			else
				Utils.alert("Invalid entry");
		});

		this.add(boxNbColumns, 1, 6);
		this.add(columnsAddButton, 2, 6);

	}
	/***
	 * 
	 */
	private void addCoronoidParameterComponent() {
		CheckBox chbCoronoid = new CheckBox("Coronoid");
		CheckBox chbCoronoid2 = new CheckBox("Coronoid 2");
		Label nbHolesLabel = new Label("Nb Holes : ");

		ChoiceBox cbHoles = addComparatorChoiceBox();

		TextField nbHolesField = new TextField();
		Button nbHolesAddButton = new Button("+");

		HBox boxCoronoid = new HBox(5.0);
		boxCoronoid.getChildren().addAll(nbHolesLabel, cbHoles, nbHolesField);

		chbCoronoid.setOnAction(e -> {

			if (chbCoronoid.isSelected()) {
				addEntry(new GeneratorCriterion(Subject.CORONOID, Operator.NONE, ""));

				chbCoronoid2.setSelected(false);

				for (int index = 0 ; index < criterions.size() ; index ++) {

					GeneratorCriterion criterion = criterions.get(index);
					if (criterion.getSubject() == Subject.CORONOID_2) {
						removeEntry(index);
						index --;
					}
				}
			}

			else {
				for (int index = 0; index < criterions.size(); index++) {

					GeneratorCriterion criterion = criterions.get(index);
					if (criterion.getSubject() == Subject.CORONOID) {

						removeEntry(index);
						index--;
					}
				}
			}

		});

		chbCoronoid2.setOnAction(e -> {

			if (chbCoronoid2.isSelected()) {
				addEntry(new GeneratorCriterion(Subject.CORONOID_2, Operator.NONE, ""));

				chbCoronoid.setSelected(false);

				for (int index = 0 ; index < criterions.size() ; index ++) {

					GeneratorCriterion criterion = criterions.get(index);
					if (criterion.getSubject() == Subject.CORONOID) {
						removeEntry(index);
						index --;
					}
				}
			}

			else {
				for (int index = 0; index < criterions.size(); index++) {

					GeneratorCriterion criterion = criterions.get(index);
					if (criterion.getSubject() == Subject.CORONOID_2) {

						removeEntry(index);
						index--;
					}
				}
			}

		});

		nbHolesAddButton.setOnAction(e -> {

			String value = nbHolesField.getText();

			if (cbHoles.getValue() != null && Utils.isNumber(value)
					&& !cbHoles.getValue().toString().equals("")) {

				Subject subject = Subject.NB_HOLES;
				Operator operator = GeneratorCriterion.getOperator(cbHoles.getValue().toString());

				GeneratorCriterion criterion = new GeneratorCriterion(subject, operator, value);
				addEntry(criterion);
			}

			else
				Utils.alert("Invalid entry");

		});

		this.add(chbCoronoid, 0, 7);
		this.add(chbCoronoid2, 0, 8);
		this.add(boxCoronoid, 1, 8);
		this.add(nbHolesAddButton, 2, 8);
	}

	/***
	 * 
	 */
	private void addRhombusParameterComponent(){		
		CheckBox chbRhombus = new CheckBox("Rhombus");

		chbRhombus.setOnAction(e -> {
			addEntry(new GeneratorCriterion(Subject.RHOMBUS, Operator.NONE, ""));
		});

		this.add(chbRhombus, 1, 7);
	}	

	/***
	 * 
	 */
	private void addCatacondensedParameterComponent() {

		CheckBox chbCatacondensed = new CheckBox("Catacondensed");

		chbCatacondensed.setOnAction(e -> {
			if (chbCatacondensed.isSelected()) {
				addEntry(new GeneratorCriterion(Subject.CATACONDENSED, Operator.NONE, ""));
			}

			else {
				for (int index = 0; index < criterions.size(); index++) {

					GeneratorCriterion criterion = criterions.get(index);
					if (criterion.getSubject() == Subject.CATACONDENSED) {
						removeEntry(index);
						index--;
					}
				}
			}
		});

		this.add(chbCatacondensed, 0, 9);

	}

	/***
	 * 
	 */
	private void addSymmetryParameterComponent() {

		Label symmetriesLabel = new Label("Symmetries : ");

		ChoiceBox chbSymmetries = new ChoiceBox();
		chbSymmetries.getItems().add("None");
		chbSymmetries.getItems().add("Mirror symmetry");
		chbSymmetries.getItems().add("Rotation of 60�");
		chbSymmetries.getItems().add("Rotation of 120�");
		chbSymmetries.getItems().add("Rotation of 180�");
		chbSymmetries.getItems().add("Vertical symmetry");
		chbSymmetries.getItems().add("Rotation of 120� (vertex)");
		chbSymmetries.getItems().add("Rotation of 180� (edges)");

		chbSymmetries.getItems().add("Rotation of 60° + Mirror");
		chbSymmetries.getItems().add("Rotation of 120° (V) + Mirror");
		chbSymmetries.getItems().add("Rotation of 120° + Mirror (H)");
		chbSymmetries.getItems().add("Rotation of 120° + Mirror (E)");
		chbSymmetries.getItems().add("Rotation of 180° + Mirror (E)");
		chbSymmetries.getItems().add("Rotation of 180° + Mirror");

		Button symmetriesAddButton = new Button("+");

		symmetriesAddButton.setOnAction(e -> {

			if (chbSymmetries.getValue() != null && !chbSymmetries.getValue().equals("None")) {

				Subject subject;
				String symmetryStr = chbSymmetries.getValue().toString();

				if (symmetryStr.equals("Mirror symmetry"))
					subject = Subject.SYMM_MIRROR;

				else if (symmetryStr.equals("Rotation of 60�"))
					subject = Subject.SYMM_ROT_60;

				else if (symmetryStr.equals("Rotation of 120�"))
					subject = Subject.SYMM_ROT_120;

				else if (symmetryStr.equals("Rotation of 180�"))
					subject = Subject.SYMM_ROT_180;

				else if (symmetryStr.equals("Vertical symmetry"))
					subject = Subject.SYMM_VERTICAL;

				else if (symmetryStr.equals("Rotation of 120� (vertex)"))
					subject = Subject.SYMM_ROT_120_V;

				else if (symmetryStr.contentEquals("Rotation of 180� (edges)"))
					subject = Subject.SYMM_ROT_180_E;

				else if (symmetryStr.contentEquals("Rotation of 60° + Mirror"))
					subject = Subject.ROT_60_MIRROR;

				else if (symmetryStr.contentEquals("Rotation of 120° (V) + Mirror"))
					subject = Subject.ROT_120_VERTEX_MIRROR;

				else if (symmetryStr.contentEquals("Rotation of 120° + Mirror (H)"))
					subject = Subject.ROT_120_MIRROR_H;

				else if (symmetryStr.contentEquals("Rotation of 120° + Mirror (E)"))
					subject = Subject.ROT_120_MIRROR_E;

				else if (symmetryStr.contentEquals("Rotation of 180° + Mirror (E)"))
					subject = Subject.ROT_180_EDGE_MIRROR;

				else if (symmetryStr.contentEquals("Rotation of 180° + Mirror"))
					subject = Subject.ROT_180_MIRROR;

				else 
					subject = null;

				GeneratorCriterion criterion = new GeneratorCriterion(subject, Operator.NONE, "");
				addEntry(criterion);
			}

			else
				Utils.alert("Invalid entry");

		});

		this.add(symmetriesLabel, 0, 10);
		this.add(chbSymmetries, 1, 10);
		this.add(symmetriesAddButton, 2, 10);
	}

	/***
	 * 
	 */
	private void addPatternParameterComponent(){

		Button patternButton = new Button("Add patterns properties");

		patternButton.setOnAction(e -> {

			if (fragmentPane == null) {
				fragmentPane = new GenerateFragmentsPane(this);
				fragmentStage = new Stage();

				fragmentStage.setTitle("Fragments");

				Scene scene = new Scene(fragmentPane);
				scene.getStylesheets().add("application/application.css");

				fragmentStage.setScene(scene);
				fragmentStage.show();
			}

			fragmentStage.show();
		});

		patternButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		this.add(patternButton, 0, 11, 2, 1);
		GridPane.setFillWidth(patternButton, true);

	}
	/***
	 * 
	 */
	private void addChosenParameterListView() {
		chosenParameterListView = new ListView<GridPane>();
		this.add(chosenParameterListView, 3, 0, 1, 11);
	}

	/***
	 * 
	 */
	private void addClearButton() {
		Button clearButton = new Button("Clear");

		clearButton.setOnAction(e -> {

			boxItems.clear();
			ObservableList<GridPane> items = FXCollections.observableArrayList(boxItems);
			chosenParameterListView.setItems(items);

			criterions.clear();

			generatedMolecules.clear();
			resultsSolvers.clear();
			drawMolecules.clear();

			if (selectedMolecules != null)
				selectedMolecules.clear();

			if (names != null)
				names.clear();

			refresh();
		});

		clearButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		this.add(clearButton, 4, 0);
		GridPane.setFillWidth(clearButton, true);
	}

	/***
	 * 
	 */
	private HBox addDisplayIrregularityStatComponent() {
		CheckBox chbIrregularityStats = new CheckBox("Display irregularity statistics");
		Label stepLabel = new Label("Step : ");
		TextField fieldIrregularityStats = new TextField();

		HBox boxIrregularityStats = new HBox(5);
		boxIrregularityStats.getChildren().addAll(chbIrregularityStats, stepLabel, fieldIrregularityStats);

		this.add(boxIrregularityStats, 4, 2);
		return boxIrregularityStats;
	}

	/**
	 * @param displayIrregularityStatBox *
	 * 
	 */
	private void addGenerateButton(HBox displayIrregularityStatBox) {
		Button generateButton = new Button("Generate");

		generateButton.setOnAction(e -> {

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

				else if (subject == Subject.XI || subject == Subject.N0 || subject == Subject.N1
						|| subject == Subject.N2 || subject == Subject.N3 || subject == Subject.N4)
					map.get("irregularity").add(criterion);

				else if (subject == Subject.RECT_NB_LINES || subject == Subject.RECT_NB_COLUMNS)
					map.get("rectangle").add(criterion);

				else if (subject == Subject.RHOMBUS)
					map.get("rhombus").add(criterion);

				else if (subject == Subject.SYMM_MIRROR || subject == Subject.SYMM_ROT_60
						|| subject == Subject.SYMM_ROT_120 || subject == Subject.SYMM_ROT_180
						|| subject == Subject.SYMM_VERTICAL || subject == Subject.SYMM_ROT_120_V
						|| subject == Subject.SYMM_ROT_180_E)

					map.get("symmetries").add(criterion);

				else if (subject == Subject.DIAMETER)
					map.get("diameter").add(criterion);

				else if (subject == Subject.CORONOID)
					map.get("coronoid").add(criterion);

				else if (subject == Subject.CORONOID_2 || subject == Subject.NB_HOLES)
					map.get("coronoid2").add(criterion);

				else if (subject == Subject.CATACONDENSED)
					map.get("catacondensed").add(criterion);

				else if (subject == Subject.SINGLE_PATTERN || subject == Subject.MULTIPLE_PATTERNS
						|| subject == Subject.FORBIDDEN_PATTERN || subject == Subject.OCCURENCE_PATTERN)
					map.get("patterns").add(criterion);
			}

			Iterator it = map.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				System.out.println(pair.getKey() + " = " + pair.getValue().toString());
			}

			GeneralModel model = ModelBuilder.buildModel(criterions, map, fragmentsInformations);
			ResultSolver resultSolver = model.solve();

			if (resultSolver.size() > 0) {
				resultsSolvers.add(resultSolver);
				addMolecules(resultSolver);
				refresh();

				if (((CheckBox)(displayIrregularityStatBox.getChildren().get(0))).isSelected()) {

					double step = 0.1;

					try {
						step = Double.parseDouble(((TextField)(displayIrregularityStatBox.getChildren().get(2))).getText());
					} catch (NumberFormatException e1) {
						Utils.alert("Bad format for the step, using 0.1 as default");
						step = 0.1;
					}

					if (step < 0.1 || step > 1) {
						Utils.alert("Bad format for the step, using 0.1 as default");
						step = 0.1;
					}

					IrregularityPane root;
					try {
						root = new IrregularityPane(this, generatedMolecules.get(generatedMolecules.size() - 1), step);
						Stage stage = new Stage();
						stage.setTitle("Irregularity stats");

						Scene scene = new Scene(root);
						scene.getStylesheets().add("application/application.css");

						stage.setScene(scene);
						stage.show();
					} catch (IOException e1) {
						e1.printStackTrace();
					}

				}
			}

			else {
				Utils.alert("No solution found");
			}

		});

		generateButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		this.add(generateButton, 4, 1);
		GridPane.setFillWidth(generateButton, true);
	}

	/***
	 * 
	 */
	private void addChangeDisplayModeButton(){
		Button changeDisplayModeButton = new Button("Change display mode");

		changeDisplayModeButton.setOnAction(e -> {

			if (selectedBenzenoidPanes.size() > 0) {

				ArrayList<BenzenoidSolution> benzenoidSolutions = new ArrayList<>();
				ArrayList<int[]> allHexagonsCorrespondances = new ArrayList<>();

				for (BenzenoidPane pane : selectedBenzenoidPanes) {

					if (!pane.isDrawMolecule()) {
						benzenoidSolutions.add(pane.getBenzenoidSolution());
						allHexagonsCorrespondances.add(pane.getHexagonsCorrespondances());
					}
				}

				addSelectedMolecules();

				MoleculePane root = new MoleculePane(selectedMolecules, benzenoidSolutions, allHexagonsCorrespondances);

				Stage stage = new Stage();
				stage.setTitle("Solutions");

				Scene scene = new Scene(root);
				scene.getStylesheets().add("application/application.css");

				stage.setScene(scene);
				stage.show();
			}

			else
				Utils.alert("No selected solution");
		});

		changeDisplayModeButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		this.add(changeDisplayModeButton, 4, 3);
		GridPane.setFillWidth(changeDisplayModeButton, true);
	}

	/***
	 * 
	 */
	private void addExportComponents() {

		RadioButton csvButton = new RadioButton(".csv");
		RadioButton cmlButton = new RadioButton(".cml");
		RadioButton graphButton = new RadioButton(".graph");
		RadioButton ERButton = new RadioButton(".com(NICS)");
		RadioButton IRButton = new RadioButton(".com(IR)");

		ToggleGroup toggleGroup = new ToggleGroup();
		csvButton.setToggleGroup(toggleGroup);
		cmlButton.setToggleGroup(toggleGroup);
		graphButton.setToggleGroup(toggleGroup);
		ERButton.setToggleGroup(toggleGroup);
		IRButton.setToggleGroup(toggleGroup);

		HBox radioButtonBox = new HBox(5.0);
		radioButtonBox.getChildren().addAll(csvButton, cmlButton, graphButton, ERButton, IRButton);

		this.add(radioButtonBox, 4, 8);

		radioButtonBox.setAlignment(Pos.CENTER);

		csvButton.setSelected(true);

		Button exportButton = new Button("Export selected solutions");

		exportButton.setOnAction(e -> {

			addSelectedMolecules();

			if (selectedBenzenoidPanes.size() > 0) {

				DirectoryChooser directoryChooser = new DirectoryChooser();
				directoryChooser.setTitle("Select directory");
				File file = directoryChooser.showDialog(getApp().getStage());

				String directoryPath = file.getAbsolutePath();
				System.out.println(directoryPath);

				String separator;

				String[] split = directoryPath.split(Pattern.quote("/"));

				if (split.length > 1)
					separator = "/";

				else {
					split = directoryPath.split(Pattern.quote("\\"));
					separator = "\\";
				}

				if (file != null) {

					if (cmlButton.isSelected()) {

						for (int i = 0; i < selectedMolecules.size(); i++) {

							Molecule molecule = selectedMolecules.get(i);
							try {
								CMLConverter.generateCmlFile(molecule,
										new File(directoryPath + separator + "solution_" + (i + 1) + ".cml"));
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}

					}

					if (ERButton.isSelected()) {

						for (int i = 0; i < selectedMolecules.size(); i++) {

							Molecule molecule = selectedMolecules.get(i);
							try {

								File moleculeFile = new File(
										directoryPath + separator + "solution_" + (i + 1) + "_ER.com");

								ComConverter.generateComFile(molecule, moleculeFile, 0, ComType.ER,
										file.getAbsolutePath());

							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					}

					if (IRButton.isSelected()) {

						for (int i = 0; i < selectedMolecules.size(); i++) {

							Molecule molecule = selectedMolecules.get(i);
							try {

								File moleculeFile = new File(
										directoryPath + separator + "solution_" + (i + 1) + "_IR.com");

								ComConverter.generateComFile(molecule, moleculeFile, 0, ComType.IR,
										file.getAbsolutePath());

							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					}
					
					if (graphButton.isSelected()) {
						
						System.out.println("Hey ! Listen !");
						
						for (int i = 0 ; i < selectedMolecules.size() ; i++) {
							
							Molecule molecule = selectedMolecules.get(i);
							
							try {

								File moleculeFile = new File(
										directoryPath + separator + "solution_" + (i + 1) + ".graph_coord");

								molecule.exportToGraphFile(moleculeFile);

							} catch (IOException e1) {
								e1.printStackTrace();
							}
							
						}
					}
				}

			}

			else
				Utils.alert("No solution to export");
		});

		exportButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		this.add(exportButton, 4, 4);
		GridPane.setFillWidth(exportButton, true);

		exportAllButton = new Button("Export all solutions");

		exportAllButton.setOnAction(e -> {

			if (generatedBenzenoidPanes.size() > 0) {

				DirectoryChooser directoryChooser = new DirectoryChooser();
				directoryChooser.setTitle("Select directory");
				File file = directoryChooser.showDialog(getApp().getStage());

				String directoryPath = file.getAbsolutePath();
				System.out.println(directoryPath);

				String separator;

				String[] split = directoryPath.split(Pattern.quote("/"));

				if (split.length > 1)
					separator = "/";

				else {
					split = directoryPath.split(Pattern.quote("\\"));
					separator = "\\";
				}

				if (file != null) {

					if (cmlButton.isSelected()) {

						int i = 0;
						for (ArrayList<Molecule> molecules : generatedMolecules) {
							for (Molecule molecule : molecules) {

								try {
									CMLConverter.generateCmlFile(molecule,
											new File(directoryPath + separator + "solution_" + (i + 1) + ".cml"));
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								i++;
							}
						}
					}

					if (ERButton.isSelected()) {

						int i = 0;
						for (ArrayList<Molecule> molecules : generatedMolecules) {
							for (Molecule molecule : molecules) {

								try {
									
									File moleculeFile = new File(
											directoryPath + separator + "solution_" + (i + 1) + "_ER.com");
									
									ComConverter.generateComFile(molecule, moleculeFile, 0, ComType.ER,
											file.getAbsolutePath());
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								i++;
							}
						}
					}

					if (IRButton.isSelected()) {

						int i = 0;
						for (ArrayList<Molecule> molecules : generatedMolecules) {
							for (Molecule molecule : molecules) {

								try {
									
									File moleculeFile = new File(
											directoryPath + separator + "solution_" + (i + 1) + "_IR.com");
									
									ComConverter.generateComFile(molecule, moleculeFile, 0, ComType.IR,
											file.getAbsolutePath());
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								i++;
							}
						}
					}
					
					if (graphButton.isSelected()) {
						
						int i = 0;
						for (ArrayList<Molecule> molecules : generatedMolecules) {
							for (Molecule molecule : molecules) {

								try {
									
									File moleculeFile = new File(
											directoryPath + separator + "solution_" + (i + 1) + ".graph_coord");

									molecule.exportToGraphFile(moleculeFile);
									
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								i++;
							}
						}
					}
				}
			}

			else
				Utils.alert("No solution to export");
		});

		exportAllButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		this.add(exportAllButton, 4, 5);
		GridPane.setFillWidth(exportAllButton, true);
	}

	/***
	 * 
	 */
	private void addSelectUnselectButtons() {
		Button selectAllButton = new Button("Select all solutions");

		selectAllButton.setOnAction(e -> {

			for (ArrayList<BenzenoidPane> panes : generatedBenzenoidPanes)
				for (BenzenoidPane pane : panes)
					pane.select();
		});

		Button unselectAllButton = new Button("Unselect All");

		unselectAllButton.setOnAction(e -> {

			for (ArrayList<BenzenoidPane> panes : generatedBenzenoidPanes)
				for (BenzenoidPane pane : panes)
					pane.unselect();
		});

		HBox boxSelect = new HBox(5.0);
		boxSelect.getChildren().addAll(selectAllButton, unselectAllButton);

		this.add(boxSelect, 4, 6);
	}

	/***
	 * 
	 */
	private void addDrawImportButtons(){
		Button drawButton = new Button("Draw benzenoid");

		drawButton.setOnAction(e -> {
			Stage stage = new Stage();
			DrawMoleculePane pane = new DrawMoleculePane(stage, this);

			Scene scene = new Scene(pane);
			scene.getStylesheets().add("application/application.css");

			stage.setTitle("Draw Benzenoid");

			stage.setScene(scene);
			stage.show();
		});

		Button importButton = new Button("Import benzenoids");

		importButton.setOnAction(e -> {

			DirectoryChooser directoryChooser = new DirectoryChooser();
			directoryChooser.setTitle("Select directory");
			File file = directoryChooser.showDialog(getApp().getStage());

			if (file != null) {
				ArrayList<Molecule> importedMolecules = importFiles(file);
				drawMolecules.addAll(importedMolecules);
				refresh();
			}
		});

		HBox boxDrawImport = new HBox(5.0);
		boxDrawImport.getChildren().addAll(drawButton, importButton);

		this.add(boxDrawImport, 4, 7);

	}
	private void addLegende() {
		Label legend1 = new Label("* : required criterion");
		Label legend2 = new Label("Upper bound on hexagons is required");

		legend1.setTextFill(Color.RED);
		legend2.setTextFill(Color.RED);

		this.add(legend1, 4, 9);
		this.add(legend2, 4, 10);
	}
	/***
	 * 
	 * @param criterion
	 */
	public void addEntry(GeneratorCriterion criterion) {

		Label label = new Label(criterion.toString());
		DeleteButton button = new DeleteButton(this, criterions.size());

		criterions.add(criterion);

		GridPane pane = new GridPane();
		pane.setPadding(new Insets(1));

		pane.add(label, 0, 0);
		label.setAlignment(Pos.BASELINE_CENTER);

		pane.add(button, 1, 0);
		button.setAlignment(Pos.BASELINE_RIGHT);

		GridPane.setHalignment(button, HPos.RIGHT);

		boxItems.add(pane);
		ObservableList<GridPane> items = FXCollections.observableArrayList(boxItems);
		chosenParameterListView.setItems(items);
	}

	public void removeEntry(int index) {

		boxItems.remove(index);
		ObservableList<GridPane> items = FXCollections.observableArrayList(boxItems);
		chosenParameterListView.setItems(items);

		criterions.remove(index);

		for (int i = 0; i < boxItems.size(); i++) {
			boxItems.get(i).getChildren().remove(1);
			boxItems.get(i).add(new DeleteButton(this, i), 1, 0);
		}
	}

	private void addMolecules(ResultSolver resultSolver) {

		names = new ArrayList<String>();

		ArrayList<Molecule> molecules = new ArrayList<>();

		for (int i = 0; i < resultSolver.size(); i++) {

			ArrayList<Integer> verticesSolution = resultSolver.getVerticesSolution(i);
			try {

				String graphFilename = "tmp.graph";
				String graphCoordFilename = "tmp.graph_coord";

				GraphFileBuilder graphBuilder = new GraphFileBuilder(verticesSolution, graphFilename,
						resultSolver.getCrown(i));
				graphBuilder.buildGraphFile();

				GraphCoordFileBuilder graphCoordBuilder = new GraphCoordFileBuilder(graphFilename, graphCoordFilename);
				graphCoordBuilder.convertInstance();

				Molecule molecule = GraphParser.parseUndirectedGraph(graphCoordFilename, null, false);

				File file = new File("tmp.graph");
				file.delete();

				file = new File("tmp.graph_coord");
				file.delete();

				molecule.setVerticesSolutions(verticesSolution);

				molecules.add(molecule);
				names.add("solution_" + i);

			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		generatedMolecules.add(molecules);
	}

	private void addSelectedMolecules() {

		selectedMolecules = new ArrayList<Molecule>();
		names = new ArrayList<String>();

		for (int i = 0; i < generatedBenzenoidPanes.size(); i++) {

			ResultSolver resultSolver = resultsSolvers.get(i);
			ArrayList<BenzenoidPane> panes = generatedBenzenoidPanes.get(i);

			for (int j = 0; j < panes.size(); j++) {

				BenzenoidPane pane = panes.get(j);

				if (pane.isSelected()) {

					String graphFilename = "tmp.graph";
					String graphCoordFilename = "tmp.graph_coord";

					GraphFileBuilder graphBuilder = new GraphFileBuilder(pane.getVerticesSolution(), graphFilename,
							resultSolver.getCrown(j));

					try {
						graphBuilder.buildGraphFile();
					} catch (IOException e) {
						e.printStackTrace();
					}

					GraphCoordFileBuilder graphCoordBuilder = new GraphCoordFileBuilder(graphFilename,
							graphCoordFilename);
					graphCoordBuilder.convertInstance();

					Molecule molecule = GraphParser.parseUndirectedGraph(graphCoordFilename, null, false);

					File file = new File("tmp.graph");
					file.delete();

					file = new File("tmp.graph_coord");
					file.delete();

					selectedMolecules.add(molecule);
					names.add("solution_" + pane.getIndex());
				}
			}

		}

		for (int i = 0; i < drawBenzenoidPanes.size(); i++) {

			Molecule molecule = drawMolecules.get(i);
			selectedMolecules.add(molecule);
		}

	}

	public ArrayList<BenzenoidPane> getSelectedBenzenoidPanes() {
		return selectedBenzenoidPanes;
	}

	public void addSelectedBenzenoidPane(BenzenoidPane benzenoidPane) {
		selectedBenzenoidPanes.add(benzenoidPane);
	}

	public void removeSelectedBenzenoid(BenzenoidPane benzenoidPane) {
		selectedBenzenoidPanes.remove(benzenoidPane);
	}

	public BenzenoidApplication getApp() {
		return app;
	}

	public void setFragmentResolutionInformations(FragmentResolutionInformations fragmentsInformations) {
		this.fragmentsInformations = fragmentsInformations;
	}

	public void hideFragmentStage() {
		fragmentStage.hide();
	}

	public void addDrawMolecule(Molecule molecule) {
		drawMolecules.add(molecule);
		refresh();
	}

	private void refresh() {

		selectedBenzenoidPanes.clear();
		generatedBenzenoidPanes.clear();
		drawBenzenoidPanes.clear();

		int size = 0;

		for (ArrayList<Molecule> molecules : generatedMolecules)
			size += molecules.size();

		size += drawMolecules.size();

		exportAllButton.setText("Export all solutions (" + size + ")");

		this.getChildren().remove(scrollPane);

		scrollPane = new ScrollPane();

		scrollPane.setFitToHeight(true);
		scrollPane.setFitToWidth(true);

		flowPane = new FlowPane();

		flowPane.getChildren().clear();

		flowPane.setHgap(20);
		flowPane.setVgap(20);
		flowPane.setPadding(new Insets(10));

		int index = 0;

		for (int i = 0; i < generatedMolecules.size(); i++) {

			ResultSolver resultSolver = resultsSolvers.get(i);
			ArrayList<Molecule> molecules = generatedMolecules.get(i);
			ArrayList<BenzenoidPane> panes = new ArrayList<>();

			for (int j = 0; j < molecules.size(); j++) {

				Molecule molecule = molecules.get(j);
				MoleculeGroup benzenoidDraw = new MoleculeGroup(molecule);
				// String description = "Solution #" + index;
				String description = resultSolver.getDescriptions().get(j);

				BenzenoidSolution benzenoidSolution = resultSolver.getSolutions().get(j);

				BenzenoidPane benzenoidPane = new BenzenoidPane(this, -1, null, benzenoidDraw, description,
						molecule.getVerticesSolutions(), index, false, benzenoidSolution,
						resultSolver.getHexagonsCorrespondances());

				flowPane.getChildren().add(benzenoidPane);
				panes.add(benzenoidPane);

				index++;
			}

			generatedBenzenoidPanes.add(panes);
		}

		for (int i = 0; i < drawMolecules.size(); i++) {

			Molecule molecule = drawMolecules.get(i);
			MoleculeGroup benzenoidDraw = new MoleculeGroup(molecule);
			String description = molecule.getDescription();

			BenzenoidPane benzenoidPane = new BenzenoidPane(this, -1, null, benzenoidDraw, description,
					molecule.getVerticesSolutions(), index, false);

			flowPane.getChildren().add(benzenoidPane);
			drawBenzenoidPanes.add(benzenoidPane);
		}

		scrollPane.setContent(flowPane);

		this.add(scrollPane, 0, 12, 5, 1);
	}

	private ArrayList<Molecule> importFiles(File directory) {

		File[] files = directory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".graph_coord");
			}
		});

		ArrayList<Molecule> molecules = new ArrayList<>();

		for (File file : files) {

			Molecule molecule = GraphParser.parseUndirectedGraph(file.getAbsolutePath(), null, false);

			String filename = file.getAbsolutePath();
			String description;

			if (filename.split(Pattern.quote("/")).length > 1) {
				String[] split = filename.split(Pattern.quote("/"));
				description = split[split.length - 1];
			}

			else {
				String[] split = filename.split(Pattern.quote("\\"));
				description = split[split.length - 1];
			}

			molecule.setDescription(description);
			molecules.add(molecule);
		}

		return molecules;
	}
}
