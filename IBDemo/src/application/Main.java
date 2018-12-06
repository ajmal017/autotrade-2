package application;
	
import javafx.application.Application;
import javafx.stage.Stage;
import service.IBService;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;



public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			BorderPane root = new BorderPane();
			Scene scene = new Scene(root,400,400);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			
			IBService ibService = IBService.getInstance();
			ibService.ibConnect();
			Thread.sleep(2000);
			
			
			
			
			Thread.sleep(10000);
			ibService.ibDisConnect();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
