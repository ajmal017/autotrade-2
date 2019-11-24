package application;
	
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import config.SystemConfig;
import entity.DailySettingRefresh;
import entity.OrderSign;
import entity.ScenarioTrend;
import entity.Setting;
import entity.TrendSign;
import entity.TrendTableItem;
//import entity.ScenarioTrend;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import service.IBService;
import service.MainService;
import service.ScenarioGroupService;
import service.SettingService;
import service.SettingServiceCallbackInterface;
import systemenum.SystemEnum;
//import service.IBService;
import tool.MP3Player;
import tool.Util;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;


public class FutureTrader extends Application implements SettingServiceCallbackInterface {
	
	public static int timerRefreshMSec = 1000; //ms
	
	private Timer secTimer;
    private int ibDisConnectAlertTimerCount = 0;
    private int ibDisConnectAlertTimerCountMax = 5;
    
    private final Label startTimeLbl = new Label();
	private final Label endTimeLbl = new Label();
	
	private Hashtable<String,Object> tableDataHash;
	
	private boolean wantCloseApp;
	
	private boolean isSettingRefreshTime() {

		SettingService sService = SettingService.getInstance();
		if (sService.getSettingRefreshPlan().size() == 0 || sService.getSceRefreshPlan().size() == sService.getPassedSceRefreshPlanCount()) {
			return false;
		}
		String nowTimeString = Util.getDateStringByDateAndFormatter(new Date(), "HH:mm:ss");
		DailyScenarioRefresh nextFresh = sService.getSceRefreshPlan().get(sService.getPassedSceRefreshPlanCount());
		if (nowTimeString.equals(nextFresh.getRefreshTime())) {
			return true;
		} else {
			return false;
		}
	}
	
	private void calledBySecondTimer () {
		
		SettingService settingService = SettingService.getInstance();
		
		if (settingService.getActiveSettingList().size() == 0) {
			
			return;
		}
		
		ibDisConnectAlertTimerCount ++;
		if(ibDisConnectAlertTimerCount == ibDisConnectAlertTimerCountMax) {
			IBService ibService = IBService.getInstance();
			if(ibService.getIbApiConfig().isActive()) {
				if(ibService.isIBConnecting()) {
					//System.out.println("IB connected.");
				} else {
					playSignAlertMusic();
				}
			}
			ibDisConnectAlertTimerCount = 0;
		}
		
		//every plan passed
		if (settingService.getPassedSettingRefreshPlanCount() == settingService.getSettingRefreshPlan().size()) {

			//todo set tomorrow timer for 24
			/*
			StringBuilder str = new StringBuilder(Util.getDateStringByDateAndFormatter(new Date(), "yyyyMMdd"));
    		str.append(startTimeLbl.getText());
    		Date finalStartTime = Util.getDateByStringAndFormatter(str.toString(), "yyyyMMddHH:mm:ss");
			secTimer = new Timer ();
			Calendar c = Calendar.getInstance();
			c.setTime(finalStartTime);
			c.add(Calendar.DATE, +1);
//			c.add(Calendar.SECOND, +1);
			finalStartTime = c.getTime();
			secTimer.scheduleAtFixedRate(new TimerTask() {
		        public void run() {
		        	calledBySecondTimer();
		        	
		        }
			}, finalStartTime, timerRefreshMSec);
			*/

			try {
				if (secTimer != null) secTimer.cancel();
	        } catch (Exception e) {
	        	e.printStackTrace();
	        }
			settingService.exportTodayOrderProfit(); //export ºexcel
			
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Warning");
			alert.setHeaderText(null);
			alert.setContentText("every setting is end");
			alert.showAndWait();
			
		} else {
			//now is a refresh time
			if(isSettingRefreshTime()) {
				settingService.updateSettingListByRefreshPlan();
			}
			
			for (int i = 0; i < sceTrendList.size(); i++) {
				ScenarioTrend oldTrend = sceTrendList.get(i);
				ScenarioTrend scenario = scenarioService.getActiveScenarioGroupList().get(i);
				if(oldTrend.getTrend() != scenario.getTrend()) {
					//update trend
					oldTrend.setTrend(scenario.getTrend());
					//get last sign
					ArrayList<TrendSign> signList = scenarioService.getDailySignMap().get(scenario.getScenario());
					TrendSign lastSign = signList.get(signList.size()-1); 
					//insert into table
					TrendTableItem trendItem = new TrendTableItem(
							Util.getDateStringByDateAndFormatter(lastSign.getTime(), "HH:mm:ss"),
							lastSign.getScenario(),
							Util.getTrendTextByEnum(lastSign.getTrend()),
							""+lastSign.getGreenCount(), 
							""+lastSign.getRedCount(), 
							""+lastSign.getWhiteCount(),
							""+lastSign.getPriceSwim(), 
							""+lastSign.getPriceIB());
					ObservableList<TrendTableItem> trendData = (ObservableList<TrendTableItem>) tbDataHash.get(lastSign.getScenario());
					trendData.add(trendItem);
					playSignAlertMusic();
				}
			}
		}
		
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
		
		tableDataHash = new Hashtable<String,Object>();
		
		//connect to ib
		IBService ibService = IBService.getInstance();
		if(ibService.getIbApiConfig().isActive()) {
			ibService.ibConnect();
			
			if (!ibService.isIBConnecting()) {
				//none active scenario
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Warning");
				alert.setHeaderText(null);
				alert.setContentText("IB connected FAIL!!!");
				alert.showAndWait();
				return;
			} else {
				System.out.println("IB connect started");
			}
		}
		
		//init data from CSV file
		SettingService settingService = SettingService.getInstance();
		settingService.refreshDBdataFromCSV();
		
		
		try {
			BorderPane root = new BorderPane();
			Scene scene = new Scene(root,400,400);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void closeApplication() {
		
		try {
			if (secTimer != null) secTimer.cancel();
        } catch (Exception e) {
        	e.printStackTrace();
        }
		
		//close order
		if(SettingService.getInstance().getCurrentOrderMap().size() > 0) {
			SettingService.getInstance().closeAllSettingWhenAppWantClose();
			return;
		}
		
		
		if(IBService.getInstance().getIbApiConfig().isActive() && IBService.getInstance().isIBConnecting()) {
			IBService.getInstance().ibDisConnect();
		}
		
		try {
            Thread.sleep(2000);
        } catch (Exception e) {
        	e.printStackTrace();
        }
		//export xls
		SettingService.getInstance().exportTodayOrderProfit();
		try {
            Thread.sleep(1000);
        } catch (Exception e) {
        	e.printStackTrace();
        }
		//shutdown
		Platform.exit();
        System.exit(0);
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void closeAppAfterPriceUpdate() {
		
		if(IBService.getInstance().getIbApiConfig().isActive() && IBService.getInstance().isIBConnecting()) {
			IBService.getInstance().ibDisConnect();
		}
		
		try {
            Thread.sleep(2000);
        } catch (Exception e) {
        	e.printStackTrace();
        }
		//export xls
		SettingService.getInstance().exportTodayOrderProfit();
		try {
            Thread.sleep(2000);
        } catch (Exception e) {
        	e.printStackTrace();
        }
		//shutdown
		Platform.exit();
        System.exit(0);
	}
}
