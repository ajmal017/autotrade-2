package application;
	
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.java4less.ocr.utils.a;

import config.SystemConfig;
import dao.CommonDAOFactory;
import entity.ColorCount;
import entity.CreatedOrder;
import entity.DailySettingRefresh;
import entity.OrderSign;
import entity.Setting;
import entity.SignTableItem;
//import entity.ScenarioTrend;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import samples.testbed.orders.OrderSamples;
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
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


public class FutureTrader extends Application implements SettingServiceCallbackInterface {
	
	public static int timerRefreshMSec = 1000; //ms
	
	private Timer secTimer;
    private int ibDisConnectAlertTimerCount = 0;
    private int ibDisConnectAlertTimerCountMax = 5;
    
    private int refreshTbTimerCount = 0;
    private int refreshTbTimerCountMax = 5;
    
//    private final Label startTimeLbl = new Label();
//	  private final Label endTimeLbl = new Label();

	private Hashtable<String,Object> tableDataHash;
	
	private boolean wantCloseApp = false;
	
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
	
	private void calledBySecondTimer() {
		
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

			//close timer
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
			
			boolean orderCountMoreThanOne = false;
			for(String setting : settingService.getActiveSettingList()) {
				ArrayList<CreatedOrder> orders = settingService.getCurrentOrderMap().get(setting);
				if (orders.size() > 0) {
					orderCountMoreThanOne = true;
					break;
				}
			}
			
			//now is a refresh time
			if(isSettingRefreshTime()) {
				settingService.updateSettingListByRefreshPlan();
			}
			
			if (settingService.getPassedSettingRefreshPlanCount() == 0) {
				return;
			}
			
			//update closeZone color
			colorInfoService.updateCloseMonitorZoneColorByTimer();
			//check closeZone color
			ColorCount cc = colorInfoService.getColorCountByCloseZoneList();
			if (cc.getGreen() > 0 || cc.getRed() > 0) {
				//close all order
				if (orderCountMoreThanOne) {
					settingService.closeAllSetting();
				}
				//todo
				//stop timer and app if law need
			} else {
				//if market is open and every working setting need open first order 
				if (orderCountMoreThanOne) {
					settingService.closeUnWorkingSettingOrder();
				} else {
					if(settingService.getDailyFirstPrice() == 0) {
						settingService.getCurrentPrice();
					}
				}
			}
			
			refreshTbTimerCount ++;
			if (refreshTbTimerCount == refreshTbTimerCountMax) {
				refreshTbTimerCount = 0;
				for (int i = 0; i < settingService.getActiveSettingList().size(); i++) {

					String setting = settingService.getActiveSettingList().get(i);

					@SuppressWarnings("unchecked")
					ObservableList<SignTableItem> signData = (ObservableList<SignTableItem>) tableDataHash.get(setting);
					
					//get new data from db
					ArrayList<OrderSign> newestList = CommonDAOFactory.getCommonDAO().getNewestSignListByDate(new Date(),setting,signData.size());
					
					for(OrderSign os : newestList) {
						//insert into table
						SignTableItem signItem = new SignTableItem(
								Util.getDateStringByDateAndFormatter(os.getTime(), "HH:mm:ss"),
								""+os.getParentOrderIdInIB(),
								""+os.getOrderStatus(), 
								setting,Util.getActionTextByEnum(os.getOrderAction()), 
								""+os.getLimitPrice(), 
								""+os.getTick(), 
								""+os.getProfitLimitPrice(), 
								""+os.getTickProfit(), 
								""+os.getLimitFilledPrice(), 
								""+os.getProfitLimitFilledPrice());

						signData.add(signItem);
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
		
		//UI
		try {
			final HBox hb1 = new HBox();
	        hb1.setPrefSize(300, 15);
	        
	        Button btn1 = new Button();
	        btn1.setText("Only Close Order");
	        btn1.setPrefSize(50, 15);
	        btn1.setOnAction(new EventHandler<ActionEvent>() {
	            @Override
	            public void handle(ActionEvent event) {
	                
	                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,"Close?");
	                Optional<ButtonType> result = confirmation.showAndWait();
	                if (result.isPresent() && result.get() == ButtonType.OK) {
	                	closeAllOrder(); 
	                }
	            }
	        });
	        
	        Button btn2 = new Button();
	        btn2.setText("Close Order And App");
	        btn2.setPrefSize(50, 15);
	        btn2.setOnAction(new EventHandler<ActionEvent>() {
	            @Override
	            public void handle(ActionEvent event) {
	                
	                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,"Close?");
	                Optional<ButtonType> result = confirmation.showAndWait();
	                if (result.isPresent() && result.get() == ButtonType.OK) {
	                	closeApplication(); 
	                }
	            }
	        });
	        
	        hb1.getChildren().addAll(btn1,btn2);
	        hb1.setSpacing(3);
	        
	        final VBox vbox = new VBox();
	        vbox.setMinSize(450, 400);
	        vbox.setSpacing(5);
	        vbox.setPadding(new Insets(10, 0, 0, 10));
	        vbox.getChildren().addAll(hb1);
			
	        for (String setting : settingService.getActiveSettingList()) {
	        	
	        	TableView<SignTableItem> trendTable = new TableView<>();
	        	trendTable.setEditable(false);
	        	trendTable.setMaxHeight(250);
	        	trendTable.setFixedCellSize(22);
	        	ObservableList<SignTableItem> trendData =
		                FXCollections.observableArrayList();
	        	trendTable.setItems(trendData);
	        	
	        	
	        	String[] columTitle = {"Time","IBOrderId","OrderStatus","Setting","Action","LimitPrice","Tick","ProfitLimitPrice","TickProfit","OpenFilledPrice","CloseFilledPrice"};
	        	String[] valueName = {"time","ibOrderId","orderStatus","setting","action","limitPrice","tick","profitLimitPrice","tickProfit","openFilledPrice","closeFilledPrice"};
	        	
	        	for (int i = 0; i < columTitle.length; i++) {
	        		TableColumn tc = new TableColumn(columTitle[i]);
	        		tc.setPrefWidth(65);
	        		tc.setCellValueFactory(
		                new PropertyValueFactory<>(valueName[i]));
		        	trendTable.getColumns().add(tc);
	        	}
	        	
	        	vbox.getChildren().addAll(trendTable);
	        	tableDataHash.put(setting, trendData);
	        }
	        
	        BorderPane root = new BorderPane();
			root.setMinSize(450, 400);
			root.setTop(vbox);
			
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			    @Override
			    public void handle(WindowEvent t) {
			    	
			    	t.consume();
			    	Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,"Directly Close App Without Closing Orders?");
	                Optional<ButtonType> result = confirmation.showAndWait();
	                if (result.isPresent() && result.get() == ButtonType.OK) {
	                	Platform.exit();
				        System.exit(0);
	                }
			    }
			});
			primaryStage.show();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		//create daily path
		String dataStr = Util.getDateStringByDateAndFormatter(new Date(), "yyyyMMdd");
		//screenshot
		String ssPath = SystemConfig.DOC_PATH + "//screenshot";
		Util.createDir(ssPath);
		String dssPath = ssPath + "//" + dataStr;
		Util.createDir(dssPath);
		for (String s : settingService.getActiveSettingList()) {
			Util.createDir(dssPath + "//" + s);
		}
		//trend profit
		Util.createDir(SystemConfig.DOC_PATH + "//trendprofit");
		
		if(settingService.getSettingRefreshPlan().size() == 0) {
			//none scenario plan
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Warning");
			alert.setHeaderText(null);
			alert.setContentText("none setting plan");
			alert.showAndWait();
			return;
		}
		
		if (settingService.getPassedSettingRefreshPlanCount() == settingService.getSettingRefreshPlan().size()) {
			//every plan passed, including the close time
			//todo set tomorrow timer for 24
			/*
			secTimer = new Timer();
			Calendar c = Calendar.getInstance();
			c.setTime(finalStartTime);
			c.add(Calendar.DATE, +1); //tomorrow
//			c.add(Calendar.SECOND, +1); //delay 1 sec for swim's refresh
			finalStartTime = c.getTime();
			secTimer.scheduleAtFixedRate(new TimerTask() {
		        public void run() {
		        	calledBySecondTimer();
		        }
			}, finalStartTime, timerRefreshMSec);
			*/
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Warning");
			alert.setHeaderText(null);
			alert.setContentText("every setting is end");
			alert.showAndWait();
			return;
		} else if (settingService.getPassedSettingRefreshPlanCount() == 0) { 
			
			secTimer = new Timer ();
			secTimer.scheduleAtFixedRate(new TimerTask() {
		        public void run() {
		        	calledBySecondTimer();
		        	
		        }
			}, 1, timerRefreshMSec); //start soon
			
		}  else {
			
			//todo
			//load history order/setting and start trading?
			
			
			
//			secTimer = new Timer ();
//			secTimer.scheduleAtFixedRate(new TimerTask() {
//		        public void run() {
//		        	calledBySecondTimer();
//		        	
//		        }
//			}, 1, timerRefreshMSec);
			
			
		}
	}
	
	private void closeAllOrder() {
		boolean orderCountMoreThanOne = false;
		for(String setting : SettingService.getInstance().getActiveSettingList()) {
			ArrayList<CreatedOrder> orders = SettingService.getInstance().getCurrentOrderMap().get(setting);
			if (orders.size() > 0) {
				orderCountMoreThanOne = true;
				break;
			}
		}
		if (orderCountMoreThanOne) {
			SettingService.getInstance().closeAllSetting();
		}
	}
	
	private void closeApplication() {
		
		try {
			if (secTimer != null) secTimer.cancel();
        } catch (Exception e) {
        	e.printStackTrace();
        }
		
		//close order
		boolean orderCountMoreThanOne = false;
		for(String setting : SettingService.getInstance().getActiveSettingList()) {
			ArrayList<CreatedOrder> orders = SettingService.getInstance().getCurrentOrderMap().get(setting);
			if (orders.size() > 0) {
				orderCountMoreThanOne = true;
				break;
			}
		}
		if (orderCountMoreThanOne) {
			wantCloseApp = true;
			SettingService.getInstance().closeAllSetting();
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
	
	public void noticeGotDailyPrice() {
		
		SettingService settingService = SettingService.getInstance();
		for(Setting setting : settingService.getWorkingSettingList()) {
			settingService.openDailyFirstOrder(setting.getSetting(), setting.getOrderSettingList().get(0));
		}
	}
	
	@Override
	public void allOrderFilled() {
		
		//export xls
		SettingService.getInstance().exportTodayOrderProfit();
		try {
			Thread.sleep(2000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(wantCloseApp) {
		
			wantCloseApp = false;
			
			if(IBService.getInstance().getIbApiConfig().isActive() && IBService.getInstance().isIBConnecting()) {
				IBService.getInstance().ibDisConnect();
			}
			
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
	
}
