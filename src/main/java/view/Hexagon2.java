package view;

import java.util.ArrayList;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import utils.Couple;

public class Hexagon2 extends Polygon{

	private Couple<Integer, Integer> coords;
	private ArrayList<Double> points;
	
	public Hexagon2(Couple<Integer, Integer> coords, ArrayList<Double> points) {
		super();
		this.coords = coords;
		this.points = points;	
		initialize();
	}
	
	private void initialize() {
		
		this.getPoints().addAll(points);
		this.setFill(Color.WHITE);
		this.setStroke(Color.BLACK);	
	}
	
	public Couple<Integer, Integer> getCoords() {
		return coords;
	}
}
