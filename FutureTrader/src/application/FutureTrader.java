package application;
	
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.java4less.ocr.utils.a;

import config.SystemConfig;
import entity.ColorCount;
import entity.DailySettingRefresh;
import entity.OrderSign;
import entity.Setting;
import entity.SignTableItem;
//import entity.ScenarioTrend;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import service.IBService;
import service.MainService;
import service.SettingService;
import service.SettingServiceCallbackInterface;
import service.ZoneColorInfoService;
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
	
	private boolean isSettingRefreshTime() {

		SettingService sService = SettingService.getInstance();
		if (sService.getSettingRefreshPlan().size() == 0 || sService.getSettingRefreshPlan().size() == sService.getPassedSettingRefreshPlanCount()) {
			return false;
		}
		String nowTimeString = Util.getDateStringByDateAndFormatter(new Date(), "HH:mm:ss");
		DailySettingRefresh nextFresh = sService.getSettingRefreshPlan().get(sService.getPassedSettingRefreshPlanCount());
		if (nowTimeString.equals(nextFresh.getRefreshTime())) {
			return true;
		} else {
			return false;
		}
	}
	
	private void calledBySecondTimer () {
		
		SettingService settingService = SettingService.getInstance();
		ZoneColorInfoService colorInfoService = ZoneColorInfoService.getInstance();
		
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

			try {
				if (secTimer != null) secTimer.cancel();
	        } catch (Exception e) {
	        	e.printStackTrace();
	        }
			//export profit report
			settingService.exportTodayOrderProfit(); 
			
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Warning");
			alert.setHeaderText(null);
			alert.setContentText("every setting is end");
			alert.showAndWait();
			
		} else {
			
			//update closeZone color
			colorInfoService.updateCloseMonitorZoneColorByTimer();
			//check closeZone color
			ColorCount cc = colorInfoService.getColorCountByCloseZoneList();
			if ((cc.getGreen() > 0 || cc.getRed() > 0) && settingService.getDailySignCount() > 0) {
				//close all order
				settingService.closeAllOrderIfNeed();
			} else {
				
				//now is a refresh time
				if(isSettingRefreshTime()) {
					settingService.updateSettingListByRefreshPlan();
				}
				
				//if market is open and every working setting need open first order 
				if (settingService.getPassedSettingRefreshPlanCount() > 0) {
					
					settingService.openFirstOrderIfNeed();
				}
			}
			
			for (int i = 0; i < settingService.getActiveSettingList().size(); i++) {

				String setting = settingService.getActiveSettingList().get(i);

				ArrayList<OrderSign> shownList = settingService.getDailySignShownInTable().get(setting);
				ArrayList<OrderSign> newestList = settingService.getDailySignMap().get(setting);
				if(shownList.size() != newestList.size()) {

					int shownCount = shownList.size();
					for(int j = 0; j < newestList.size() - shownCount; j++) {

						OrderSign newSign = newestList.get(shownCount+j);
						shownList.add(newSign);
						//insert into table
						SignTableItem signItem = new SignTableItem(
								Util.getDateStringByDateAndFormatter(newSign.getTime(), "HH:mm:ss"),
								setting,
								Util.getActionTextByEnum(newSign.getOrderAction()),
								""+newSign.getLimitPrice(),
								""+newSign.getTick(),
								""+newSign.getStopPrice(),
								""+newSign.getTickProfit());
						@SuppressWarnings("unchecked")
						ObservableList<SignTableItem> signData = (ObservableList<SignTableItem>) tableDataHash.get(setting);
						signData.add(signItem);
						playSignAlertMusic();
					}

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
		MainService mainService = MainService.getInstance();
		mainService.refreshDBdataFromCSV();
		
		SettingService settingService = SettingService.getInstance();
		settingService.setTradeObj(this);
		
		if(settingService.getActiveSettingList().size() == 0) {
			//none active scenario
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Warning");
			alert.setHeaderText(null);
			alert.setContentText("none active setting");
			alert.showAndWait();
			return;
		}
		
		
		
		
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
		
		try {
            Thread.sleep(2000);
        } catch (Exception e) {
        	e.printStackTrace();
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
            Thread.sleep(2000);
        } catch (Exception e) {
        	e.printStackTrace();
        }
		//shutdown
		Platform.exit();
        System.exit(0);
	}
}
