package view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class PlotMakerZoom extends Application {

	private static final int NUM_DATA_POINTS = 1000;

	private String filename = "C:\\Users\\adrie\\Documents\\These\\molecules\\resultats\\rectangles\\plot_times_10_lines.txt";
	@SuppressWarnings("rawtypes")
	private ArrayList<Series> series;
	private double xMin = Double.MAX_VALUE, yMin = Double.MAX_VALUE;
	private double xMax = 0, yMax = 0;
	private String xName, yName;
	private String title;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void readFile() throws IOException {

		series = new ArrayList<Series>();

		BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
		ArrayList<String> lines = new ArrayList<String>();
		String line;

		while ((line = reader.readLine()) != null) {
			lines.add(line);
		}

		reader.close();

		title = lines.get(0);
		xName = lines.get(1);
		yName = lines.get(2);

		Series sery = new Series();
		sery.setName(lines.get(3));

		int index = 4;
		while (index < lines.size()) {

			line = lines.get(index);

			if (line.equals("")) {

				series.add(sery);
				sery = new Series();
				index++;
				sery.setName(lines.get(index));
			} else {

				String[] splittedLine = line.split(" ");
				Double x = Double.parseDouble(splittedLine[0]);
				Double y = Double.parseDouble(splittedLine[1]);

				sery.getData().add(new XYChart.Data(x, y));

				if (x < xMin)
					xMin = x;

				if (x > xMax)
					xMax = x;

				if (y < yMin)
					yMin = y;

				if (y > yMax)
					yMax = y;
			}

			index++;
		}

		series.add(sery);
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		final LineChart<Number, Number> chart = createChart();

		final StackPane chartContainer = new StackPane();
		chartContainer.getChildren().add(chart);

		final Rectangle zoomRect = new Rectangle();
		zoomRect.setManaged(false);
		zoomRect.setFill(Color.LIGHTSEAGREEN.deriveColor(0, 1, 1, 0.5));
		chartContainer.getChildren().add(zoomRect);

		setUpZooming(zoomRect, chart);

		final HBox controls = new HBox(10);
		controls.setPadding(new Insets(10));
		controls.setAlignment(Pos.CENTER);

		final Button zoomButton = new Button("Zoom");
		final Button resetButton = new Button("Reset");
		zoomButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				doZoom(zoomRect, chart);
			}
		});
		resetButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				final NumberAxis xAxis = (NumberAxis) chart.getXAxis();
				xAxis.setLowerBound(0);
				xAxis.setUpperBound(1000);
				final NumberAxis yAxis = (NumberAxis) chart.getYAxis();
				yAxis.setLowerBound(0);
				yAxis.setUpperBound(1000);

				zoomRect.setWidth(0);
				zoomRect.setHeight(0);
			}
		});
		final BooleanBinding disableControls = zoomRect.widthProperty().lessThan(5)
				.or(zoomRect.heightProperty().lessThan(5));
		zoomButton.disableProperty().bind(disableControls);
		controls.getChildren().addAll(zoomButton, resetButton);

		final BorderPane root = new BorderPane();
		root.setCenter(chartContainer);
		root.setBottom(controls);

		final Scene scene = new Scene(root, 600, 400);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private LineChart<Number, Number> createChart() throws IOException {

		readFile();

		// stage.setTitle(title);

		// defining the axes
		final NumberAxis xAxis = new NumberAxis();
		final NumberAxis yAxis = new NumberAxis();
		xAxis.setLabel(xName);
		yAxis.setLabel(yName);

		// creating the chart
		final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis);

		lineChart.setTitle(title);

		xAxis.setLowerBound(xMin);
		xAxis.setUpperBound(xMax);
		xAxis.setAutoRanging(false);

		for (Series sery : series) {
			lineChart.getData().add(sery);
		}

		return lineChart;
	}

	private void setUpZooming(final Rectangle rect, final Node zoomingNode) {
		final ObjectProperty<Point2D> mouseAnchor = new SimpleObjectProperty<>();
		zoomingNode.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				mouseAnchor.set(new Point2D(event.getX(), event.getY()));
				rect.setWidth(0);
				rect.setHeight(0);
			}
		});
		zoomingNode.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				double x = event.getX();
				double y = event.getY();
				rect.setX(Math.min(x, mouseAnchor.get().getX()));
				rect.setY(Math.min(y, mouseAnchor.get().getY()));
				rect.setWidth(Math.abs(x - mouseAnchor.get().getX()));
				rect.setHeight(Math.abs(y - mouseAnchor.get().getY()));
			}
		});
	}

	private void doZoom(Rectangle zoomRect, LineChart<Number, Number> chart) {
		Point2D zoomTopLeft = new Point2D(zoomRect.getX(), zoomRect.getY());
		Point2D zoomBottomRight = new Point2D(zoomRect.getX() + zoomRect.getWidth(),
				zoomRect.getY() + zoomRect.getHeight());
		final NumberAxis yAxis = (NumberAxis) chart.getYAxis();
		Point2D yAxisInScene = yAxis.localToScene(0, 0);
		final NumberAxis xAxis = (NumberAxis) chart.getXAxis();
		Point2D xAxisInScene = xAxis.localToScene(0, 0);
		double xOffset = zoomTopLeft.getX() - yAxisInScene.getX();
		double yOffset = zoomBottomRight.getY() - xAxisInScene.getY();
		double xAxisScale = xAxis.getScale();
		double yAxisScale = yAxis.getScale();
		xAxis.setLowerBound(xAxis.getLowerBound() + xOffset / xAxisScale);
		xAxis.setUpperBound(xAxis.getLowerBound() + zoomRect.getWidth() / xAxisScale);
		yAxis.setLowerBound(yAxis.getLowerBound() + yOffset / yAxisScale);
		yAxis.setUpperBound(yAxis.getLowerBound() - zoomRect.getHeight() / yAxisScale);
		System.out.println(yAxis.getLowerBound() + " " + yAxis.getUpperBound());
		zoomRect.setWidth(0);
		zoomRect.setHeight(0);
	}

	public static void main(String[] args) {
		launch(args);
	}
}