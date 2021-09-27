package solution.outpout;

import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import solution.BenzenoidSolution;

public class SolutionWindowOutput implements SolutionOutput {
	private FlowPane pane;
	private Group drawing;
	
	public SolutionWindowOutput(FlowPane pane) {
		super();
		this.pane = pane;
		pane.setHgap(20);
		pane.setVgap(20);
		pane.setPadding(new Insets(10));

	}


	@Override
	public void output(BenzenoidSolution benzenoidSolution) {
		Path path = benzenoidPath(benzenoidSolution, 100);
		drawing = new Group(path);
		VBox benzenoidBox = new VBox();
		benzenoidBox.getChildren().addAll(drawing, new Label(benzenoidSolution.getName()));
		pane.getChildren().add(benzenoidBox);
		//drawing.getChildren().add(new Circle(50, 50, 10));

	}
	private void addHexagonToPath(Path benzenoidPath, float xcenter, float ycenter, float width) {
		float r = (float) (width / Math.sqrt(3));
		float c = r * (float) (Math.sqrt((double)3)/2);
		benzenoidPath.getElements().addAll(new MoveTo(xcenter, ycenter - r),
				new LineTo(xcenter + c, ycenter - r / 2 ),
				new LineTo(xcenter + c, ycenter + r / 2 ),
				new LineTo(xcenter, ycenter + r ),
				new LineTo(xcenter - c, ycenter + r / 2 ),
				new LineTo(xcenter - c, ycenter - r / 2 ),
				new LineTo(xcenter, ycenter - r));
	}
	
	private Path benzenoidPath(BenzenoidSolution solution, float size) {
		Path benzenoid = new Path();
		float nbCouronnes = solution.getNbCouronnes();
		float dimension = (float) (nbCouronnes * 2.0 - 1.0);
		for(int n:solution.getHexagonGraph().vertexSet()) {
			float y = (n / (int)dimension);
			float x = (float) (n % dimension + (nbCouronnes - y / 2.));
			//System.out.print(n + " ");
			addHexagonToPath(benzenoid, x * size, (float) (y * size * Math.sqrt(3.)/2.), size);
		}
		return benzenoid;
	}

	public Group getDrawing() {
		return drawing;
	}


	public void setDrawing(Group drawing) {
		this.drawing = drawing;
	}

}
