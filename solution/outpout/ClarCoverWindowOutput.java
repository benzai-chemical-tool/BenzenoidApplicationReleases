package solution.outpout;

import javafx.scene.Group;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import solution.BenzenoidSolution;
import solution.ClarCoverSolution;

public class ClarCoverWindowOutput extends SolutionWindowOutput {

	public ClarCoverWindowOutput(FlowPane pane) {
		super(pane);
	}

	public void output(BenzenoidSolution solution) {
		super.output(solution);
		
		float nbCouronnes = solution.getNbCouronnes();
		float dimension = (float) (nbCouronnes * 2.0 - 1.0);
		int size = 100;
		for(int rond:((ClarCoverSolution)solution).getRonds()) {
			float y = (rond / (int)dimension);
			float x = (float) (rond % dimension + (nbCouronnes - y / 2.));
			//System.out.println(x + "," + y);
			addCircleToDrawing(getDrawing(), x * size, (float) (y * size * Math.sqrt(3.)/2.), size / 3);
		}
		for(int bound:((ClarCoverSolution)solution).getDoublesLiaisons()) {
			float y = (bound / 6 / (int)dimension);
			float x = (float) ((bound / 6) % dimension + (nbCouronnes - y / 2.));
			//System.out.println(x + "," + y);
			addBondToDrawing(getDrawing(), x * size, (float) (y * size * Math.sqrt(3.)/2.), size, bound % 6);
		}
		
		for(int carbone:((ClarCoverSolution)solution).getCelibataires()) {
			float y = (carbone / 6 / (int)dimension);
			float x = (float) ((carbone / 6) % dimension + (nbCouronnes - y / 2.));
			//System.out.println(x + "," + y);
			addCelibataireToDrawing(getDrawing(), x * size, (float) (y * size * Math.sqrt(3.)/2.), size, carbone % 6);
		}
	}
	
	private void addCircleToDrawing(Group drawing, float xcenter, float ycenter, float radius) {
		Circle circle = new Circle(xcenter, ycenter, radius);
		circle.setFill(Color.TRANSPARENT);
		circle.setStroke(Color.BLACK);
		drawing.getChildren().add(circle);
	}
	
	private void addBondToDrawing(Group drawing, float xcenter, float ycenter, float size, int side) {
		float r = (float) (size / Math.sqrt(3));
		float c = r * (float) (Math.sqrt((double)3)/2);
		Line ligne; 
		switch(side) {
			case 0 : ligne = new Line(xcenter, ycenter - r * 0.9 , xcenter + c * 0.9 , ycenter - r / 2 * 0.9); break;
			case 1 : ligne = new Line(xcenter + c * 0.9, ycenter - r / 2 * 0.9, xcenter + c * 0.9, ycenter + r / 2 * 0.9); break;
			case 2 : ligne = new Line(xcenter + c * 0.9, ycenter + r / 2 * 0.9, xcenter, ycenter + r * 0.9 ); break;
			case 3 : ligne = new Line(xcenter, ycenter + r * 0.9, xcenter - c * 0.9, ycenter + r / 2 * 0.9); break;
			case 4 : ligne = new Line(xcenter - c * 0.9, ycenter + r / 2 * 0.9, xcenter - c * 0.9, ycenter - r / 2 * 0.9); break;
			case 5 : ligne = new Line(xcenter - c * 0.9, ycenter - r / 2 * 0.9, xcenter, ycenter - r * 0.9); break;
			default : ligne = null;
		}
		drawing.getChildren().add(ligne);
	}
	
	private void addCelibataireToDrawing(Group drawing, float xcenter, float ycenter, float size, int carbonePosition) {
		float r = (float) (size / Math.sqrt(3));
		float c = r * (float) (Math.sqrt((double)3)/2);
		Circle circle; 
		switch(carbonePosition) {
			case 0 : circle = new Circle(xcenter, ycenter - r , size / 10); break;
			case 1 : circle = new Circle(xcenter + c, ycenter - r / 2, size / 10); break;
			case 2 : circle = new Circle(xcenter + c, ycenter + r / 2, size / 10); break;
			case 3 : circle = new Circle(xcenter, ycenter + r, size / 10); break;
			case 4 : circle = new Circle(xcenter - c, ycenter + r / 2, size / 10); break;
			case 5 : circle = new Circle(xcenter - c, ycenter - r / 2, size / 10); break;
			default : circle = null;
		}
		circle.setFill(Color.TRANSPARENT);
		circle.setStroke(Color.BLACK);
		drawing.getChildren().add(circle);
	}
}
