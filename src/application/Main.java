package application;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.Bindings;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("MainScene.FXML"));
			// Determine initial window size from the primary screen (use full available bounds)
			Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
			double initW = Math.min(1920, bounds.getWidth());
			double initH = Math.min(1080, bounds.getHeight());

			Scene scene = new Scene(root, initW, initH);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			// Choose node to scale: if root is a ScrollPane, scale its content; otherwise scale root
			Node scaleTarget = root;
			if (root instanceof ScrollPane) {
				ScrollPane sp = (ScrollPane) root;
				if (sp.getContent() != null) scaleTarget = sp.getContent();
			}

			// Bind a uniform scale so the UI shrinks/grows to always fit within the window
			final Node target = scaleTarget;
			DoubleBinding scale = Bindings.createDoubleBinding(() ->
					Math.min(scene.getWidth() / 1920.0, scene.getHeight() / 1080.0),
					scene.widthProperty(), scene.heightProperty());
			target.scaleXProperty().bind(scale);
			target.scaleYProperty().bind(scale);

			primaryStage.setScene(scene);
			primaryStage.setTitle("Tomasulo Simulation");
			primaryStage.setMinWidth(800);
			primaryStage.setMinHeight(600);
			primaryStage.centerOnScreen();
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
