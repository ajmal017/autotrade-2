package application;
	
import java.util.Optional;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import service.IBService;
import service.IBServiceCallbackInterface;
import systemenum.SystemEnum;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;



public class Main extends Application implements IBServiceCallbackInterface {
	@Override
	public void start(Stage primaryStage) {
		try {
			
			IBService ibService = IBService.getInstance();
			ibService.setMainObj(this);
			ibService.ibConnect();
			Thread.sleep(2000);
			
			final HBox hb1 = new HBox();
	        hb1.setPrefSize(300, 15);
			
	        Button btn1 = new Button();
	        btn1.setText("Buy");
	        btn1.setPrefSize(50, 15);
	        btn1.setOnAction(new EventHandler<ActionEvent>() {
	            @Override
	            public void handle(ActionEvent event) {
	            	IBService ibService = IBService.getInstance();
	            	ibService.placeOrder(SystemEnum.OrderAction.Buy, "T10", "10:00:00");
	            }
	        });
	        
	        Button btn2 = new Button();
	        btn2.setText("Sell");
	        btn2.setPrefSize(50, 15);
	        btn2.setOnAction(new EventHandler<ActionEvent>() {
	            @Override
	            public void handle(ActionEvent event) {
	            	IBService ibService = IBService.getInstance();
	            	ibService.placeOrder(SystemEnum.OrderAction.Sell, "T10", "10:00:00"); 
	            }
	        });
	        
	        Button btn3 = new Button();
	        btn3.setText("Close");
	        btn3.setPrefSize(50, 15);
	        btn3.setOnAction(new EventHandler<ActionEvent>() {
	            @Override
	            public void handle(ActionEvent event) {
	            	IBService ibService = IBService.getInstance();
	            	ibService.closeTodayTrade();
	                closeApplication(); 
	            }
	        });
	        
	        hb1.getChildren().addAll(btn1,btn2,btn3);
	        hb1.setSpacing(3);
			
	        final VBox vbox = new VBox();
	        vbox.setMinSize(400, 400);
	        vbox.setSpacing(5);
	        vbox.setPadding(new Insets(10, 0, 0, 10));
	        vbox.getChildren().addAll(hb1);
	        
	        BorderPane root = new BorderPane();
			root.setMinSize(400, 400);
			root.setTop(vbox);
			
			Scene scene = new Scene(root,400,400);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			
			
			
			
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}

	
	private void closeApplication() {
		
		try {
            Thread.sleep(2000);
        } catch (Exception e) {
        	e.printStackTrace();
        }
		
		IBService.getInstance().ibDisConnect();
		
		//shutdown
		Platform.exit();
		System.exit(0);
	}
	
	@Override
	public void updateTradePrice(double price, String preOrderScenario, String preOrderTime) {
		
		//todo
		System.out.println("updateTradePrice = " + price);
	}
}
