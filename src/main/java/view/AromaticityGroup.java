package view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javafx.scene.paint.Color;
import molecules.Molecule;
import solveur.Aromaticity;
import utils.HexagonAromaticity;

public class AromaticityGroup extends MoleculeGroup {

	private MoleculePane pane;
	
	private Color [] palette;
	private Aromaticity aromaticity;
	
	public AromaticityGroup(MoleculePane pane, Molecule molecule, Aromaticity aromaticity) throws IOException {
		
		super(molecule);
		this.pane = pane;
		this.aromaticity = aromaticity;
		buildPalette();
		coloringHexagons();
	}
	
	private void coloringHexagons() {
		
		double [] localAromaticity = aromaticity.getLocalAromaticity();
		ArrayList<HexagonAromaticity> aromaticities = new ArrayList<HexagonAromaticity>();
		
		for (int i = 0 ; i < localAromaticity.length ; i++)
			aromaticities.add(new HexagonAromaticity(i, localAromaticity[i]));
		
		Collections.sort(aromaticities);
		
		int nbColors = 0;
		double curentValue = -1.0;
		
		ArrayList<Double> values = new ArrayList<Double>();
		
		for (HexagonAromaticity aromaticity : aromaticities) {
			if (aromaticity.getAromaticity() != curentValue) {
				nbColors ++;
				curentValue = aromaticity.getAromaticity();
				values.add(curentValue);
			}
		}
		
		int scale = Math.floorDiv(palette.length, nbColors);
		
	
		int colorIndex = 0;
		for (int i = 0  ; i < nbColors ; i++) {
						
			Color color = palette[colorIndex];
			double value = values.get(i);
			
			for (HexagonAromaticity aromaticity : aromaticities) {
				if (aromaticity.getAromaticity() == value) {
					hexagons[aromaticity.getIndex()].setFill(color);
				}
			}
			
			colorIndex += scale;
		}
		
		for (int i = 0 ; i < hexagons.length ; i++) {
			
			final int index = i;
			final int [] localCircuits = aromaticity.getLocalCircuits()[i];
			
			Hexagon2 hexagon = hexagons[i];
			hexagon.setOnMouseClicked(e -> {
				
				StringBuilder informations = new StringBuilder();
				informations.append("H" + index + " : ");
				
				for (int j = 0 ; j < localCircuits.length ; j++) {
					informations.append(localCircuits[j] + " ");
				}
				
				informations.append("(" + aromaticity.getLocalAromaticity()[index] + ")");
				
				pane.setHexagonInformations(informations.toString());
			});
		}
	}
	
	private void buildPalette() throws IOException {
		
		BufferedReader reader = new BufferedReader(new FileReader(new File("palette.txt")));
		String line;
		ArrayList<String> lines = new ArrayList<String>();
		
		while ((line = reader.readLine()) != null)
			lines.add(line);
		
		Collections.reverse(lines);
		
		reader.close();
		
		palette = new Color[lines.size()];
		
		for (int i = 0 ; i < palette.length ; i++) {
			
			line = lines.get(i);
			String [] splittedLine = line.split(" ");
			
			int r = Integer.parseInt(splittedLine[0]);
			int g = Integer.parseInt(splittedLine[1]);
			int b = Integer.parseInt(splittedLine[2]);
			
			palette[i] = Color.rgb(r, g, b);
		}
	}
}
