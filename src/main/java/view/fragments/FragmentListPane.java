package view.fragments;

import java.util.ArrayList;

import generator.fragments.Fragment;
import generator.fragments.FragmentGenerationType;
import generator.fragments.FragmentResolutionInformations;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import utils.Utils;
import view.generator.GeneratorCriterion;
import view.generator.GeneratorCriterion.Operator;
import view.generator.GeneratorCriterion.Subject;

public class FragmentListPane extends GridPane {

	private GenerateFragmentsPane parent;

	private ListView<GridPane> listView = new ListView<GridPane>();

	private ArrayList<GridPane> boxItems = new ArrayList<GridPane>();

	public FragmentListPane(GenerateFragmentsPane parent) {
		this.parent = parent;
		initialize();
	}

	private void initialize() {

		this.setPadding(new Insets(20));
		this.setHgap(25);
		this.setVgap(15);

		this.add(listView, 0, 0, 1, 8);

		listView.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				GridPane selection = listView.getSelectionModel().getSelectedItem();
				if (selection != null) {
					CloseButton button = (CloseButton) selection.getChildren().get(1);
					parent.setSelectedDrawPane(button.getIndex());
				}
			}
		});

		ToggleGroup group1 = new ToggleGroup();
		ToggleGroup group2 = new ToggleGroup();

		RadioButton buttonModel1 = new RadioButton("Modelisation 1");
		RadioButton buttonModel2 = new RadioButton("Modelisation 2");
		RadioButton buttonModel3 = new RadioButton("Modelisation 3");

		buttonModel2.setOnAction(e -> {
			buttonModel2.setSelected(false);
			Utils.alert("A supprimer");
		});

		buttonModel3.setOnAction(e -> {
			buttonModel3.setSelected(false);
			Utils.alert("A supprimer");
		});

		buttonModel1.setToggleGroup(group1);
		buttonModel2.setToggleGroup(group1);
		buttonModel3.setToggleGroup(group1);

		RadioButton buttonNonDisjoint = new RadioButton("Undisjunct Fragments");
		RadioButton buttonDisjoint = new RadioButton("Disjunct Fragments");
		RadioButton buttonDisjointAU = new RadioButton("Disjunct on A/U hexagons");

		buttonNonDisjoint.setOnAction(e -> {
			buttonNonDisjoint.setSelected(false);
			Utils.alert("Coming soon ...");
		});

		buttonDisjointAU.setOnAction(e -> {
			buttonDisjointAU.setSelected(false);
			Utils.alert("Coming soon ...");
		});

		buttonNonDisjoint.setToggleGroup(group2);
		buttonDisjoint.setToggleGroup(group2);
		buttonDisjointAU.setToggleGroup(group2);

		RadioButton buttonForbiddenFragment = new RadioButton("Forbid a fragment");

		RadioButton buttonFragmentOccurences = new RadioButton("Occurences of a fragment : ");
		TextField occurencesField = new TextField();
		HBox boxOccurences = new HBox(buttonFragmentOccurences, occurencesField);
		boxOccurences.setSpacing(3.0);

		buttonFragmentOccurences.setOnAction(e -> {
			buttonFragmentOccurences.setSelected(false);
			Utils.alert("Coming soon ...");
		});
		occurencesField.setPrefWidth(32);

		buttonForbiddenFragment.setToggleGroup(group1);
		buttonFragmentOccurences.setToggleGroup(group1);

		VBox box1 = new VBox(buttonModel1, buttonModel2, buttonModel3);
		box1.setSpacing(3.0);

		VBox box2 = new VBox(buttonNonDisjoint, buttonDisjoint, buttonDisjointAU);
		box2.setSpacing(3.0);

		VBox box3 = new VBox(buttonForbiddenFragment, boxOccurences);
		box3.setSpacing(3.0);

		Label singleMultipleLabel = new Label("Classical fragments generation");
		singleMultipleLabel.setStyle("-fx-font-weight: bold");

		Label otherModelLabel = new Label("Other fragment generation");
		otherModelLabel.setStyle("-fx-font-weight: bold");

		Label multipleFragmentPropertyLabel = new Label("Multiple Fragments properties");
		multipleFragmentPropertyLabel.setStyle("-fx-font-weight: bold");

		this.add(singleMultipleLabel, 1, 0);
		this.add(box1, 1, 1);

		this.add(multipleFragmentPropertyLabel, 1, 2);
		this.add(box2, 1, 3);

		this.add(otherModelLabel, 1, 4);
		this.add(box3, 1, 5);

		Button addFragmentButton = new Button("Add fragment");

		addFragmentButton.setOnAction(e -> {
			addEntry();
		});

		GridPane.setFillWidth(addFragmentButton, true);
		addFragmentButton.setAlignment(Pos.CENTER);

		Button applyButton = new Button("Apply");

		applyButton.setOnAction(e -> {

			ArrayList<Fragment> fragments = new ArrayList<Fragment>();

			for (DrawFragmentPane pane : parent.getFragmentsPanes()) {

				Fragment fragment = pane.buildFragment();
				fragments.add(fragment);
			}

			FragmentGenerationType type = null;
			GeneratorCriterion criterion = null;

			if (boxItems.size() == 1) {

				if (buttonModel1.isSelected()) {
					type = FragmentGenerationType.SINGLE_FRAGMENT_1;
					criterion = new GeneratorCriterion(Subject.SINGLE_PATTERN, Operator.NONE, "");
				}

				else if (buttonModel2.isSelected())
					type = FragmentGenerationType.SINGLE_FRAGMENT_2;

				else if (buttonModel3.isSelected())
					type = FragmentGenerationType.SINGLE_FRAGMENT_3;

				else if (buttonForbiddenFragment.isSelected()) {
					type = FragmentGenerationType.FORBIDDEN_FRAGMENT;
					criterion = new GeneratorCriterion(Subject.FORBIDDEN_PATTERN, Operator.NONE, "");
				}

				else if (buttonFragmentOccurences.isSelected())
					type = FragmentGenerationType.FRAGMENT_OCCURENCES;
			}

			else {
				if (buttonModel1.isSelected()) {
					type = FragmentGenerationType.MULTIPLE_FRAGMENT_1;
					criterion = new GeneratorCriterion(Subject.MULTIPLE_PATTERNS, Operator.NONE, "");
				}

				else if (buttonModel2.isSelected())
					type = FragmentGenerationType.MULTIPLE_FRAGMENT_2;

				else if (buttonModel3.isSelected())
					type = FragmentGenerationType.MULTIPLE_FRAGMENT_3;
			}

			FragmentResolutionInformations informations = new FragmentResolutionInformations(type, fragments);
			parent.setFragmentResolutionInformations(informations);
			parent.addEntry(criterion);

			parent.showFragmentCheckbox();

			parent.hide();
		});

		HBox box4 = new HBox(addFragmentButton, applyButton);
		box4.setSpacing(10.0);

		this.add(box4, 1, 6);

		addEntry();
	}

	public void addEntry() {

		Label label = new Label("unknown_fragment");

		CloseButton button = new CloseButton(this, parent.getFragmentsPanes().size());

		GridPane pane = new GridPane();
		pane.setPadding(new Insets(1));

		pane.add(label, 0, 0);
		label.setAlignment(Pos.BASELINE_CENTER);

		pane.add(button, 1, 0);
		button.setAlignment(Pos.BASELINE_RIGHT);

		boxItems.add(pane);
		ObservableList<GridPane> items = FXCollections.observableArrayList(boxItems);
		listView.setItems(items);

		parent.getFragmentsPanes().add(new DrawFragmentPane(parent, this, button.getIndex()));

		// parent.setSelectedDrawPane(button.getIndex());
	}

	public void changeName(int index, String name) {
		Label label = new Label(name);
		boxItems.get(index).getChildren().set(0, label);
	}

	public void removeEntry(int index) {

		boxItems.remove(index);
		ObservableList<GridPane> items = FXCollections.observableArrayList(boxItems);
		listView.setItems(items);

		parent.removePane(index);

		for (int i = 0; i < boxItems.size(); i++) {
			boxItems.get(i).getChildren().remove(1);
			boxItems.get(i).add(new CloseButton(this, i), 1, 0);
		}
	}

	public int getNbItems() {
		return boxItems.size();
	}
}
