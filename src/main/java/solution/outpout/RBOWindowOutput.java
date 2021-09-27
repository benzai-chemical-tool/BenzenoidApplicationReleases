package solution.outpout;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import solution.BenzenoidSolution;
import solution.StatistiqueStructure;

public class RBOWindowOutput extends SolutionWindowOutput {
	private StatistiqueStructure stats;

	public RBOWindowOutput(FlowPane pane, StatistiqueStructure stats) {
		super(pane);
		this.stats = stats;
	}

	public void output(BenzenoidSolution solution) {
		super.output(solution);

		float nbCouronnes = solution.getNbCouronnes();
		float dimension = (float) (nbCouronnes * 2.0 - 1.0);
		int size = 100;

		for (int hex = 0; hex < dimension * dimension; hex++) {
			float y = (hex / (int) dimension);
			float x = (float) (hex % dimension + (nbCouronnes - y / 2.));
			// System.out.println(hex + ":" + x + "," + y + "=" +
			// stats.getCompteursCycles()[hex]);
			int pourcentCycle = stats.getNbKekuleCovers() > 0
					? stats.getCompteursCycles()[hex] * 100 / stats.getNbKekuleCovers()
					: 0;
			int pourcentRBO = stats.getNbKekuleCovers() > 0
					? stats.getCompteursRBO()[hex] * 100 / stats.getNbKekuleCovers()
					: 0;
			if (stats.getCompteursCycles()[hex] > 0)
				addCycleTextToDrawing(getDrawing(), x * size, (float) (y * size * Math.sqrt(3.) / 2.), size / 3,
						"" + pourcentCycle);
			if (stats.getCompteursRBO()[hex] > 0)
				addRondTextToDrawing(getDrawing(), x * size, (float) (y * size * Math.sqrt(3.) / 2.), size / 3,
						"" + pourcentRBO / 100.);

		}

		for (int hex = 0; hex < dimension * dimension; hex++)
			for (int j = 0; j < 6; j++) {
				float y = (hex / (int) dimension);
				float x = (float) (hex % dimension + (nbCouronnes - y / 2.));
				// System.out.println(x + "," + y);
				if (stats.getCompteursLiaisons()[hex][j] > 0)
					addBondTextToDrawing(getDrawing(), x * size, (float) (y * size * Math.sqrt(3.) / 2.), size, j,
							"" + stats.getCompteursLiaisons()[hex][j] * 100 / stats.getNbKekuleCovers());
				if (stats.getCompteursCelibataires()[hex][j] > 0)
					addCelibataireTextToDrawing(getDrawing(), x * size, (float) (y * size * Math.sqrt(3.) / 2.), size,
							j, "" + stats.getCompteursCelibataires()[hex][j] * 100 / stats.getNbKekuleCovers());
			}

		Text nbKekule = new Text(0, 0, "" + stats.getNbKekuleCovers());
		nbKekule.setFont(new Font(20));
		nbKekule.setStroke(Color.DARKCYAN);
		getDrawing().getChildren().add(nbKekule);

		// saveToFile(getDrawing(), solution.getName());
	}

	private void addCircleToDrawing(Group drawing, float xcenter, float ycenter, float radius) {
		Circle circle = new Circle(xcenter, ycenter, radius);
		circle.setFill(Color.BLACK);
		circle.setStroke(Color.BLACK);
		drawing.getChildren().add(circle);
	}

	private void addSquareToDrawing(Group drawing, float xcenter, float ycenter, float size, Color squareColor) {
		Line line1 = new Line(0, 0, 0, size);
		line1.setStroke(squareColor);
		Line line2 = new Line(0, size, size, size);
		line2.setStroke(squareColor);
		Line line3 = new Line(size, size, size, 0);
		line3.setStroke(squareColor);
		Line line4 = new Line(size, 0, 0, 0);
		line4.setStroke(squareColor);
		drawing.getChildren().addAll(line1, line2, line3, line4);
	}

	private void addCycleTextToDrawing(Group drawing, float xcenter, float ycenter, float size, String texte) {
		Text text = new Text(xcenter - size / 2, ycenter, texte);
		// text.setFill(Color.TRANSPARENT);
		text.setStroke(Color.DARKCYAN);
		text.setTextAlignment(TextAlignment.CENTER);
		text.setFont(new Font(size));
		drawing.getChildren().add(text);
	}

	private void addBondTextToDrawing(Group drawing, float xcenter, float ycenter, float size, int side, String texte) {
		float r = (float) (size / Math.sqrt(3));
		float c = r * (float) (Math.sqrt((double) 3) / 2);
		Text text;
		switch (side) {
		case 0:
			text = new Text(xcenter + c * 0.5, ycenter - r * 0.75, texte);
			break;
		case 1:
			text = new Text(xcenter + c, ycenter, texte);
			break;
		case 2:
			text = new Text(xcenter + c * 0.5, ycenter + r * 0.75, texte);
			break;
		case 3:
			text = new Text(xcenter - c * 0.5, ycenter + r * 0.75, texte);
			break;
		case 4:
			text = new Text(xcenter - c, ycenter, texte);
			break;
		case 5:
			text = new Text(xcenter - c * 0.5, ycenter - r * 0.75, texte);
			break;
		default:
			text = null;
		}
		text.setStroke(Color.ORANGE);
		drawing.getChildren().add(text);
	}

	private void addCelibataireTextToDrawing(Group drawing, float xcenter, float ycenter, float size,
			int carbonePosition, String texte) {
		float r = (float) (size / Math.sqrt(3));
		float c = r * (float) (Math.sqrt((double) 3) / 2);
		Text text;
		switch (carbonePosition) {
		case 0:
			text = new Text(xcenter, ycenter - r, texte);
			break;
		case 1:
			text = new Text(xcenter + c, ycenter - r / 2, texte);
			break;
		case 2:
			text = new Text(xcenter + c, ycenter + r / 2, texte);
			break;
		case 3:
			text = new Text(xcenter, ycenter + r, texte);
			break;
		case 4:
			text = new Text(xcenter - c, ycenter + r / 2, texte);
			break;
		case 5:
			text = new Text(xcenter - c, ycenter - r / 2, texte);
			break;
		default:
			text = null;
		}
		text.setStroke(Color.RED);
		drawing.getChildren().add(text);
	}

	private void addRondTextToDrawing(Group drawing, float xcenter, float ycenter, float size, String texte) {
		Text text = new Text(xcenter - size / 2, ycenter + size * 0.8, texte);
		// text.setFill(Color.TRANSPARENT);
		text.setStroke(Color.YELLOW);
		text.setTextAlignment(TextAlignment.CENTER);
		text.setFont(new Font(size * 0.8));
		drawing.getChildren().add(text);
	}

	public static void saveToFile(Group group, String name) {
		WritableImage image = group.snapshot(null, null);
		File file = new File("/Users/nicolasprcovic/Misc/csp/Benzenoides/snapshots/" + name + ".png");
		try {
			ImageIO.write(SwingFXUtils.fromFXImage(image, null), "PNG", file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
