package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import javafx.stage.Stage;
import view.catalog.CatalogPane;
import view.generator.GeneratorPane;

public class BenzenoidApplication extends Application {

	private Stage stage;

	private Region generatorPane;
	private Region catalogPane;
	
	private Scene generatorScene;

	private boolean generator = true;

	public Stage getStage() {
		return stage;
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			stage = primaryStage;
			catalogPane = new CatalogPane(this);
			
			BorderPane rootPane = buildRootPane();

			generatorScene = new Scene(rootPane);
			generatorScene.getStylesheets().add("application/application.css");

			initPrimaryStageProperties(primaryStage);
			primaryStage.setScene(generatorScene);
			primaryStage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/***
	 * 
	 * @return the root pane
	 */
	private BorderPane buildRootPane() {
		BorderPane rootPane = new BorderPane();
		
		generatorPane = new GeneratorPane(this);
		generatorPane.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.F2)
				switchMode(rootPane);
		});
		
		MenuBar menuBar = buildMenuBar(rootPane);
		
		rootPane.setTop(menuBar);
		rootPane.setCenter(generatorPane);
		return rootPane;
	}
	
	/***
	 * 
	 * @param rootPane
	 * @return the primaryStage menu bar
	 */
	private MenuBar buildMenuBar(BorderPane rootPane) {		
		final Menu fileMenu = new Menu("File");
		final Menu optionsMenu = new Menu("Options");
		final Menu helpMenu = new Menu("Help");
		
		Label menu4Label = new Label("Change mode");
		menu4Label.setOnMouseClicked(e -> {
			switchMode(rootPane);
		});
		final Menu changeModeMenu = new Menu();
		changeModeMenu.setGraphic(menu4Label);
		
		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().addAll(fileMenu, optionsMenu, helpMenu, changeModeMenu);	
		return menuBar;
	}
	
	/***
	 * set the primary stage title, width, length, ...
	 * @param primaryStage
	 */
	private void initPrimaryStageProperties(Stage primaryStage) {
		primaryStage.setTitle("Benzenoid Generator");

		primaryStage.setWidth(Screen.getPrimary().getBounds().getWidth() * 2 / 3);
		primaryStage.setHeight(Screen.getPrimary().getBounds().getHeight() * 2 / 3);

		primaryStage.centerOnScreen();

	}
	
	/***
	 * @param rootPane
	 * Switch between the generator Pane and the catalog Pane
	 */
	public void switchMode(BorderPane rootPane) {

		if (generator) {
			rootPane.getChildren().remove(generatorPane);
			rootPane.setCenter(catalogPane);
		}
		
		else {
			rootPane.getChildren().remove(catalogPane);
			rootPane.setCenter(generatorPane);
		}
		
		generator = !generator;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
