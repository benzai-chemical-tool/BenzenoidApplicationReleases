package view.fragments;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import generator.fragments.Fragment;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import molecules.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class DrawFragmentPane extends GridPane{

	private GenerateFragmentsPane parent;
	private FragmentGroup group;
	private FragmentListPane fragmentListPane;
	
	private TextField nameField;
	
	private int index;
	
	private TextField degreeField;
	
	public DrawFragmentPane(GenerateFragmentsPane parent, FragmentListPane fragmentListPane, int index) {
		super();
		this.index = index;
		this.parent = parent;
		this.fragmentListPane = fragmentListPane;
		initialize();
	}
	
	private void initialize() {
		
		group = new FragmentGroup(3);
		
		group.resize(500, 500);
		
		this.setPadding(new Insets(20));
		this.setHgap(10);
		this.setVgap(10);
		
		this.setMinSize(750, 300);
		
		this.add(group, 0, 0, 1, 6);
		
		Label nameLabel = new Label("Name: ");
		nameField = new TextField();
		
		Label nbCrownsLabel = new Label("Number of crowns: ");
		
		TextField fieldNbCrowns = new TextField();
		fieldNbCrowns.setText("3");
		fieldNbCrowns.setEditable(false);
		
		Button plusButton = new Button("+");
		plusButton.setOnMouseClicked(e -> {
			int nbCrowns = Integer.parseInt(fieldNbCrowns.getText()) + 1;
			fieldNbCrowns.setText(Integer.toString(nbCrowns));
			refresh(nbCrowns);
		});
		
		plusButton.setStyle(
                "-fx-background-radius: 5em; " +
                "-fx-min-width: 32px; " +
                "-fx-min-height: 32px; " +
                "-fx-max-width: 32px; " +
                "-fx-max-height: 32px;"
        );
		
		Button minusButton = new Button("-");
		minusButton.setOnAction(e -> {
			int nbCrowns = Integer.parseInt(fieldNbCrowns.getText()) - 1;
			if (nbCrowns > 0) {
				fieldNbCrowns.setText(Integer.toString(nbCrowns));
				refresh(nbCrowns);
			}
		});
		
		minusButton.setStyle(
                "-fx-background-radius: 5em; " +
                "-fx-min-width: 32px; " +
                "-fx-min-height: 32px; " +
                "-fx-max-width: 32px; " +
                "-fx-max-height: 32px;"
        );
		
		this.add(nameLabel, 1, 0);
		this.add(nameField, 2, 0);
		
		nameField.setOnKeyPressed(e -> {
			fragmentListPane.changeName(index, nameField.getText());
		});
		
		nameField.setOnKeyReleased(e -> {
			fragmentListPane.changeName(index, nameField.getText());
		});
		
		HBox crownsBox = new HBox(fieldNbCrowns, minusButton, plusButton);
		crownsBox.setSpacing(3.0);
		
		this.add(nbCrownsLabel, 1, 1);
		this.add(crownsBox, 2, 1);
		
		Label degreeLabel = new Label("Order : ");
		degreeField = new TextField();
		
		this.add(degreeLabel, 1, 2);
		this.add(degreeField, 2, 2);
		
		Button allUnknown = new Button("All unknown");
		allUnknown.setOnAction(e -> {
			group.setAllLabels(1);
		});
		
		Button allPresent = new Button("All present");
		allPresent.setOnAction(e -> {
			group.setAllLabels(2);
		});
		
		Button allForbidden = new Button("All forbidden");
		allForbidden.setOnAction(e -> {
			group.setAllLabels(3);
		});
		
		Button allUnselect = new Button ("Clear");
		allUnselect.setOnAction(e -> {
			group.setAllLabels(0);
			group.removeCenter();
		});
		
		HBox allButtonsBox = new HBox(allUnknown, allPresent, allForbidden, allUnselect);
		allButtonsBox.setSpacing(5);
		this.add(allButtonsBox, 1, 4, 2, 1);
		
		Button exportButton = new Button("Export");
		
		exportButton.setOnAction(e -> {
			FileChooser fileChooser = new FileChooser();
		    fileChooser.setTitle("Save");
		    File file = fileChooser.showSaveDialog(parent.getApp().getStage());
		    
		    if (file != null) {
		    	
				int degree;
				
				try {
					degree = Integer.parseInt(degreeField.getText());
				}catch (NumberFormatException e1) {
					e1.printStackTrace();
					degree = 0;
				}
				
				group.setDegree(degree);
		    	
		    	Fragment fragment = group.exportFragment();
		    	try {
					fragment.export(file);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		    }
		});
		
		Button importButton = new Button("Import");
		
		importButton.setOnAction(e -> {
			FileChooser fileChooser = new FileChooser();
		    fileChooser.setTitle("Save");
		    File file = fileChooser.showOpenDialog(parent.getApp().getStage());
		    
		    if (file != null) {
		    	try {
					Fragment fragment = Fragment.importFragment(file);
					
					int yMax = -1;
					
					for (Node node : fragment.getNodesRefs())
						if (node.getY() > yMax) yMax = node.getX();
						
					int maxColumn = -1;
					
					for (int i = 0 ; i <= yMax ; i++) {
						int column = 0;
						for (Node node : fragment.getNodesRefs()) {
							if (node.getY() == i) {
								column ++;
							}
						}
						if (column > maxColumn)
							maxColumn = column;
					}
					
					fieldNbCrowns.setText(Integer.toString(maxColumn));
					refresh(maxColumn);
					
					group.importFragment(fragment);
					
					String filename = file.getName();
					nameField.setText(filename.split(Pattern.quote("."))[0]);
					
					degreeField.setText(Integer.toString(fragment.getOrder()));
					
					fragmentListPane.changeName(index, nameField.getText());
					
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		    }
		});
		
		HBox hBoxFinalButtons = new HBox(importButton, exportButton);
		hBoxFinalButtons.setSpacing(5);
		hBoxFinalButtons.setAlignment(Pos.CENTER);
		
		this.add(hBoxFinalButtons, 1, 5, 2, 1);
	}

	private void refresh(int nbCrowns) {
		this.getChildren().remove(group);
		group = new FragmentGroup(nbCrowns);
		this.add(group, 0, 0, 1, 6);
	}
	
	public String getName() {
		
		if (nameField.getText().equals(""))
			return "unknown_fragment";
		return nameField.getText();
	}

	public Fragment buildFragment() {
		Fragment fragment = group.exportFragment();
		
		try {
			fragment.setOrder(Integer.parseInt(degreeField.getText()));
		} 
		catch (NumberFormatException e) {
			fragment.setOrder(0);
		}
		
		return fragment;
		
	}
	
	public int getIndex() {
		return index;
	}
}
