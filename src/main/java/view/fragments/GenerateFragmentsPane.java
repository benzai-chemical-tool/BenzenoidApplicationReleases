package view.fragments;

import java.util.ArrayList;

import application.BenzenoidApplication;
import generator.fragments.FragmentResolutionInformations;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import view.generator.GeneratorCriterion;
import view.generator.GeneratorPane;

public class GenerateFragmentsPane extends GridPane {

	private GeneratorPane parent;

	private ArrayList<DrawFragmentPane> panes = new ArrayList<>();
	private FragmentListPane listPane;

	private DrawFragmentPane selectedDrawPane;

	public GenerateFragmentsPane(GeneratorPane parent) {
		this.parent = parent;
		listPane = new FragmentListPane(this);
		selectedDrawPane = panes.get(0);
		initialize();
	}

	private void initialize() {

		this.setPadding(new Insets(20));
		this.setHgap(25);
		this.setVgap(15);

		this.add(selectedDrawPane, 0, 0);
		this.add(listPane, 1, 0);
	}

	public BenzenoidApplication getApp() {
		return parent.getApp();
	}

	public ArrayList<DrawFragmentPane> getFragmentsPanes() {
		return panes;
	}

	public void setSelectedDrawPane(int index) {
		this.getChildren().remove(selectedDrawPane);
		selectedDrawPane = panes.get(index);
		refresh();
	}

	private void refresh() {
		this.add(selectedDrawPane, 0, 0);
	}

	public void addDrawPane(DrawFragmentPane pane) {
		panes.add(pane);
	}

	public void removePane(int index) {
		panes.remove(index);
	}

	public void setFragmentResolutionInformations(FragmentResolutionInformations fragmentsInformations) {
		parent.setFragmentResolutionInformations(fragmentsInformations);
	}

	public void hide() {
		parent.hideFragmentStage();
	}

	public void addEntry(GeneratorCriterion criterion) {
		parent.addEntry(criterion);
	}

	public void showFragmentCheckbox() {
		// parent.setFragmentCheckBoxVisible(true);
		// parent.selectFragmentCheckBox(true);
	}
}
