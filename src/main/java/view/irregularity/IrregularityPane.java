package view.irregularity;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import classifier.IrregularityClassifier;
import classifier.MoleculeInformation;
import classifier.PAHClass;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import molecules.Molecule;
import view.generator.GeneratorPane;

public class IrregularityPane extends GridPane {

	private GeneratorPane parent;

	private ArrayList<Molecule> molecules;
	private ArrayList<PAHClass> classes;
	@SuppressWarnings("rawtypes")
	private BarChart chart;

	private double step;

	private String[] categoriesNames;

	private int[][] allValues;

	public IrregularityPane(GeneratorPane parent, ArrayList<Molecule> molecules, double step) throws IOException {
		super();
		this.parent = parent;
		this.molecules = molecules;
		this.step = step;
		initialize();
	}

	private void initialize() throws IOException {
		buildClassifier();
		buildChart();
		buildPane();
	}

	private void buildClassifier() throws IOException {

		HashMap<String, MoleculeInformation> map = new HashMap<>();

		for (int i = 0; i < molecules.size(); i++) {
			MoleculeInformation info = new MoleculeInformation("solution_" + i, molecules.get(i));
			map.put("solution_" + i, info);
		}

		IrregularityClassifier classifier = new IrregularityClassifier(map, step);

		classes = classifier.classify();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void buildChart() {

		final List<BarChart.Series> seriesList = new LinkedList<>();

		int nbClasses = (int) (1.0 / step);
		categoriesNames = new String[nbClasses];

		double v = step;

		for (int i = 0; i < nbClasses; i++) {

			BigDecimal bd = new BigDecimal(v).setScale(1, RoundingMode.HALF_UP);
			categoriesNames[i] = Double.toString(bd.doubleValue());
			v += step;
		}

		final String[] seriesNames = { "Classes of irregularity" };

		allValues = new int[1][classes.size()];

		for (int i = 0; i < classes.size(); i++)
			allValues[0][i] = classes.get(i).size();

		final double minY = 0;
		double maxY = -Double.MAX_VALUE;
		for (int seriesIndex = 0; seriesIndex < seriesNames.length; seriesIndex++) {
			final BarChart.Series series = new BarChart.Series<>();
			series.setName(seriesNames[seriesIndex]);
			final int[] values = allValues[seriesIndex];
			for (int categoryIndex = 0; categoryIndex < categoriesNames.length; categoryIndex++) {
				final int value = values[categoryIndex];
				final String category = categoriesNames[categoryIndex];
				maxY = Math.max(maxY, value);
				final BarChart.Data data = new BarChart.Data(category, value);
				series.getData().add(data);
			}
			seriesList.add(series);
		}

		final CategoryAxis xAxis = new CategoryAxis();
		xAxis.getCategories().setAll(categoriesNames);
		xAxis.setLabel("Classes");
		final NumberAxis yAxis = new NumberAxis(minY, maxY, 50);
		yAxis.setLabel("");
		chart = new BarChart(xAxis, yAxis);
		chart.setTitle("Irregularity stats");
		chart.getData().setAll(seriesList);
	}

	private void buildPane() {

		this.add(chart, 0, 0, 1, 2);

		this.setPadding(new Insets(10));

		TextArea area = new TextArea();

		String[] titles = new String[categoriesNames.length];

		for (int i = 0; i < categoriesNames.length; i++) {
			titles[i] = "XI <= " + categoriesNames[i];
		}

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < classes.size(); i++) {
			builder.append(titles[i] + " : " + classes.get(i).size() + "\n");
		}

		area.setText(builder.toString());
		area.setEditable(false);

		ScrollPane scrollPane = new ScrollPane(area);

		this.add(scrollPane, 1, 0);

		Button exportCSVButton = new Button("Export to .csv");
		Button exportPNGButton = new Button("Export to .png");

		exportPNGButton.setOnAction(e -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Save");
			File file = fileChooser.showSaveDialog(parent.getApp().getStage());

			if (file != null) {
				exportAsPNG(file);
			}
		});

		exportCSVButton.setOnAction(e -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Save");
			File file = fileChooser.showSaveDialog(parent.getApp().getStage());

			if (file != null) {
				try {
					exportAsCSV(file);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		HBox buttonBox = new HBox(exportCSVButton, exportPNGButton);
		buttonBox.setSpacing(10);
		buttonBox.setPadding(new Insets(10));

		this.add(buttonBox, 1, 1);

		buttonBox.setAlignment(Pos.CENTER);
	}

	private void exportAsPNG(File file) {
		WritableImage wi = chart.snapshot(new SnapshotParameters(), new WritableImage(501, 408));
		BufferedImage awtImage = new BufferedImage((int) wi.getWidth(), (int) wi.getHeight(),
				BufferedImage.TYPE_INT_RGB);

		try {
			ImageIO.write(SwingFXUtils.fromFXImage(wi, awtImage), "png", file);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void exportAsCSV(File file) throws IOException {

		BufferedWriter writer = new BufferedWriter(new FileWriter(file));

		writer.write("class_name\tnb_molecules\n");

		for (int i = 0; i < categoriesNames.length; i++)
			writer.write(categoriesNames[i] + "\t" + allValues[0][i] + "\n");

		writer.close();
	}
}
