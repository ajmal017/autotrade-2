package application;
	
import java.util.ArrayList;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import config.SystemConfig;
//import entity.ScenarioTrend;
import javafx.application.Application;
import javafx.stage.Stage;
//import service.IBService;
import tool.MP3Player;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;


public class FutureTrader extends Application {
	
	private Timer secTimer;
    private int ibDisConnectAlertTimerCount = 0;
    private int ibDisConnectAlertTimerCountMax = 5;
    
    private final Label startTimeLbl = new Label();
	private final Label endTimeLbl = new Label();
	
	
	
	
	private void calledBySecondTimer () {
		
//		ibDisConnectAlertTimerCount ++;
//		if(ibDisConnectAlertTimerCount == ibDisConnectAlertTimerCountMax) {
//			IBService ibService = IBService.getInstance();
//			if(ibService.getIbApiConfig().isActive()) {
//				if(ibService.isIBConnecting()) {
//					//System.out.println("IB connected.");
//				} else {
//					playSignAlertMusic();
//				}
//			}
//			ibDisConnectAlertTimerCount = 0;
//		}
	}
	
	private void playSignAlertMusic() {
		
		ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
	    cachedThreadPool.execute(new Runnable() {
	  
	        @Override
	        public void run() {
				 //music alert

	    		MP3Player mp3 = new MP3Player("resource/sign_bg.mp3");
	            mp3.play();
	        }
	    });
	    
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			BorderPane root = new BorderPane();
			Scene scene = new Scene(root,400,400);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			
			playSignAlertMusic();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
