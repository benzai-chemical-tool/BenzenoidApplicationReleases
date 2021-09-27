package view;

import java.io.*;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.stage.Stage;
 
 
public class PlotMaker extends Application {
	
	private String filename = "C:\\Users\\adrie\\Documents\\These\\molecules\\resultats\\molecules_CP\\rapports_max_min.txt";
	@SuppressWarnings("rawtypes")
	private ArrayList<Series> series;
	private double xMin = Double.MAX_VALUE, yMin = Double.MAX_VALUE;
	private double xMax = 0, yMax = 0;
	private String xName, yName;
	private String title;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void readFile() throws IOException{
		
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
		while(index < lines.size()) {
			
			line = lines.get(index);
			
			if (line.equals("")) {
				
				series.add(sery);
				sery = new Series();
				index ++;
				if (index < lines.size())
					sery.setName(lines.get(index));
			}
			else {
				
				String [] splittedLine = line.split(" ");
				Double x = Double.parseDouble(splittedLine[0]);
				Double y = Double.parseDouble(splittedLine[1]);
				
				sery.getData().add(new XYChart.Data(x, y));
				System.out.println("(" + x + ", " + y + ")");
				
				if (x < xMin)
					xMin = x;
				
				if (x > xMax)
					xMax = x;
				
				if (y < yMin)
					yMin = y;
				
				if (y > yMax)
					yMax = y;
			}
			
			index ++;
		}

		series.add(sery);
	}

	
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Override 
    public void start(Stage stage) throws IOException {
    	
    	readFile();
    	
        stage.setTitle(title);
        
        //defining the axes
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(xName);
        yAxis.setLabel(yName);
        
        //creating the chart
        final LineChart<Number,Number> lineChart = 
                new LineChart<Number,Number>(xAxis,yAxis);
                
        lineChart.setTitle(title);
        
        
        xAxis.setLowerBound(xMin);
        xAxis.setUpperBound(xMax);  
        yAxis.setLowerBound(yMin);
        yAxis.setUpperBound(yMax + (yMax * 0.1));
        
        /*
        xAxis.setLowerBound(1);
        xAxis.setUpperBound(5);
        yAxis.setLowerBound(yMin);
        yAxis.setUpperBound(60000);
        */
        
        xAxis.setAutoRanging(false);
        yAxis.setAutoRanging(false);
        
        Scene scene  = new Scene(lineChart,500,400);
       
        for (Series sery : series) {
        	lineChart.getData().add(sery);
        }
        
        stage.setScene(scene);
        stage.show();
    }
 
    public static void main(String[] args) {
        launch(args);
    }
    
}