package view.fragments;

import java.util.ArrayList;

import javafx.animation.PauseTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;
import utils.Couple;

public class Hexagon extends Polygon{

	//private final Color [] COLORS = new Color[] {Color.WHITE, Color.LIGHTGRAY, Color.DARKGRAY, Color.BLACK};
	private final Color [] COLORS = new Color[] {Color.WHITE, Color.ORANGE, Color.RED, Color.BLACK};
	
	private FragmentGroup group;
	
	private Couple<Integer, Integer> coords;
	private ArrayList<Double> points;
	
	private int label;
	private boolean isCenter;
	
	public Hexagon(FragmentGroup group, Couple<Integer, Integer> coords, ArrayList<Double> points) {
		super();
		this.group = group;
		this.coords = coords;
		this.points = points;
		label = 0;
		isCenter = false;
		
		initialize();
	}
	
	private void initialize() {
		
		Duration maxTimeBetweenSequentialClicks = Duration.millis(200);

		 PauseTransition clickTimer = new PauseTransition(maxTimeBetweenSequentialClicks);
	        final IntegerProperty sequentialClickCount = new SimpleIntegerProperty(0);
	        clickTimer.setOnFinished(event -> {
	            int count = sequentialClickCount.get();
	            if (count == 1) 
	            	shiftLabel();
	            if (count == 2) {
	            	if (!isCenter) {
	            		this.toFront();
						this.setStroke(Color.RED);
						this.setStrokeWidth(this.getStrokeWidth() * 2);
						group.disableOtherCenter(this);
					}
					else {
						this.setStroke(Color.BLACK);
						this.setStrokeWidth(this.getStrokeWidth() / 2);
					}

					isCenter = !isCenter;
	            }
	            	
	            sequentialClickCount.set(0);
	        });
		
		
		this.getPoints().addAll(points);
		
		this.setFill(Color.WHITE);
		this.setStroke(Color.BLACK);
		
		this.setOnMouseClicked(e -> {
			System.out.println(coords.toString());
			sequentialClickCount.set(sequentialClickCount.get()+1);
            clickTimer.playFromStart();
		});
		
	}
	
	public int getLabel() {
		return label;
	}
	
	private void shiftLabel() {
		
		if (coords.getX() == 4 && coords.getY() == 2 && label == 1)
			System.out.print("");
		
		label = (label + 1) % 4;
		this.setFill(COLORS[label]);
		
	}
	
	public void setLabel(int label) {
		
		if (coords.getX() == 4 && coords.getY() == 2 && label == 1)
			System.out.print("");
		
		this.label = label % 4;
		this.setFill(COLORS[label]);
		
	}
	
	public void disableCenter() {
		isCenter = false;
		this.setStrokeWidth(this.getStrokeWidth() / 2);

	}
	
	public boolean isCenter() {
		return isCenter;
	}
	
	public Couple<Integer, Integer> getCoords() {
		return coords;
	}
	
	@Override
	public String toString() {
		return coords.toString();
	}
}
