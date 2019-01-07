package application;
	
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import config.SystemConfig;
import entity.DailyScenarioRefresh;
import entity.Scenario;
import entity.ScenarioTrend;
import entity.TrendSign;
import entity.TrendTableItem;
import entity.Zone;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import service.IBService;
import service.IBServiceCallbackInterface;
import service.MainService;
import service.ScenarioGroupService;
import service.ScenarioGroupServiceCallbackInterface;
import service.ZoneColorInfoService;
import systemenum.SystemEnum;
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
import javafx.scene.text.Font;


public class AutoTradeWithVol extends Application implements ScenarioGroupServiceCallbackInterface {
	
	public static int timerRefreshMSec = 700; //ms
	
    private Timer secTimer;
    private int ibTimerCount = 0; //5
    private int ibTimerCountMax = 10;
    private ArrayList<ScenarioTrend> sceTrendList;
	
    private final Label startTimeLbl = new Label();
	private final Label endTimeLbl = new Label();
	private final Label yellowCountLbl = new Label();
	
	private Hashtable<String,Object> tbDataHash;
	
	private boolean wantCloseApp;
	
	private boolean isSceRefreshTime() {

		ScenarioGroupService sService = ScenarioGroupService.getInstance();
		if (sService.getSceRefreshPlan().size() == 0 || sService.getSceRefreshPlan().size() == sService.getPassedSceRefreshPlanCount()) {
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
	
	private boolean isVolRefreshTime() {

		ScenarioGroupService sService = ScenarioGroupService.getInstance();
		if (sService.getVolRefreshPlan().size() == 0 || sService.getVolRefreshPlan().size() == sService.getPassedVolRefreshPlanCount()) {
			return false;
		}
		String nowTimeString = Util.getDateStringByDateAndFormatter(new Date(), "HH:mm:ss");
		DailyScenarioRefresh nextFresh = sService.getVolRefreshPlan().get(sService.getPassedVolRefreshPlanCount());
		if (nowTimeString.equals(nextFresh.getRefreshTime())) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isVolZoneRefreshTime() {

		ScenarioGroupService sService = ScenarioGroupService.getInstance();
		if (sService.getVolZoneRefreshPlan().size() == 0 || sService.getVolZoneRefreshPlan().size() == sService.getPassedVolZoneRefreshPlanCount()) {
			return false;
		}
		String nowTimeString = Util.getDateStringByDateAndFormatter(new Date(), "HH:mm:ss");
		DailyScenarioRefresh nextFresh = sService.getVolZoneRefreshPlan().get(sService.getPassedVolZoneRefreshPlanCount());
		if (nowTimeString.equals(nextFresh.getRefreshTime())) {
			return true;
		} else {
			return false;
		}
	}
	
	private void playSignAlertMusic() {
		
		ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
	    cachedThreadPool.execute(new Runnable() {
	  
	        @Override
	        public void run() {
				 //music alert

	    		MP3Player mp3 = new MP3Player(SystemConfig.DOC_PATH+"//sign_bg.mp3");
	            mp3.play();
	        }
	    });
	    
	}
	
	@Override
	public void start(Stage primaryStage) {
		
		setSceTrendList(new ArrayList<ScenarioTrend>());
		tbDataHash = new Hashtable<String,Object>();
		
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
		
		ScenarioGroupService scenarioService = ScenarioGroupService.getInstance();
		scenarioService.setAutoTradeObj(this);
		
		ArrayList<ScenarioTrend> activeScenariolist = scenarioService.getActiveScenarioGroupList();
		if(activeScenariolist.size() == 0) {
			//none active scenario
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Warning");
			alert.setHeaderText(null);
			alert.setContentText("none active scenario");
			alert.showAndWait();
			return;
		}
		
		try {
			//UI
	        final HBox hb1 = new HBox();
	        hb1.setPrefSize(300, 15);
	        final Label sTitle = new Label("StartTime:");
	        final Label eTitle = new Label("EndTime:");
	        startTimeLbl.setPrefWidth(60);
	        endTimeLbl.setPrefWidth(60);
	        
	        Button btn = new Button();
	        btn.setText("Close");
	        btn.setPrefSize(50, 15);
	        btn.setOnAction(new EventHandler<ActionEvent>() {
	            @Override
	            public void handle(ActionEvent event) {
	                
	                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,"Close?");
	                Optional<ButtonType> result = confirmation.showAndWait();
	                if (result.isPresent() && result.get() == ButtonType.OK) {
	                	closeApplication(); 
	                }
	            }
	        });
	        
	        hb1.getChildren().addAll(sTitle, startTimeLbl, eTitle, endTimeLbl,btn);
	        hb1.setSpacing(3);
	    
	        final HBox hb2 = new HBox();
	        hb2.setPrefSize(300, 15);
	        final Label yTitle = new Label("Yellow:");
	        yellowCountLbl.setPrefWidth(60);
	        yellowCountLbl.setText("0");
	        hb2.getChildren().addAll(yTitle, yellowCountLbl);
	        hb2.setSpacing(3);
	        
	        sTitle.setFont(new Font(12));
	        eTitle.setFont(new Font(12));
	        startTimeLbl.setFont(new Font(12));
	        endTimeLbl.setFont(new Font(12));
	        yTitle.setFont(new Font(12));
	        yellowCountLbl.setFont(new Font(12));
	        
	        final VBox vbox = new VBox();
	        vbox.setMinSize(450, 400);
	        vbox.setSpacing(5);
	        vbox.setPadding(new Insets(10, 0, 0, 10));
	        vbox.getChildren().addAll(hb1,hb2);
	        
	        //table

	        for (ScenarioTrend scenario : activeScenariolist) {
	        	
	        	TableView<TrendTableItem> trendTable = new TableView<>();
	        	ObservableList<TrendTableItem> trendData =
	                FXCollections.observableArrayList();
	        	
	        	trendTable.setEditable(false);
	        	trendTable.setMaxHeight(120);
	        	trendTable.setFixedCellSize(22);
	        
	        	TableColumn timeCol = new TableColumn("Time");
	        	timeCol.setPrefWidth(65);
	        	timeCol.setCellValueFactory(
	                new PropertyValueFactory<>("time"));

	        	TableColumn scenarioCol = new TableColumn("T");
	        	scenarioCol.setPrefWidth(40);
	        	scenarioCol.setCellValueFactory(
	                new PropertyValueFactory<>("scenario"));

	        	TableColumn trendCol = new TableColumn("Trend");
	        	trendCol.setPrefWidth(50);
	        	trendCol.setCellValueFactory(
	                new PropertyValueFactory<>("trend"));

	        	TableColumn greenCol = new TableColumn("Green");
	        	greenCol.setPrefWidth(50);
	        	greenCol.setCellValueFactory(
	                new PropertyValueFactory<>("greenCount"));
	        
	        	TableColumn redCol = new TableColumn("Red");
	        	redCol.setPrefWidth(50);
	        	redCol.setCellValueFactory(
	                new PropertyValueFactory<>("redCount"));
	        
	        	TableColumn whiteCol = new TableColumn("White");
	        	whiteCol.setPrefWidth(50);
	        	whiteCol.setCellValueFactory(
	                new PropertyValueFactory<>("whiteCount"));
	        
	        	TableColumn swimCol = new TableColumn("SwPr");
	        	swimCol.setPrefWidth(60);
	        	swimCol.setCellValueFactory(
	                new PropertyValueFactory<>("swimPrice"));
	        
	        	TableColumn ibCol = new TableColumn("IBPr");
	        	ibCol.setPrefWidth(60);
	        	ibCol.setCellValueFactory(
	                new PropertyValueFactory<>("ibPrice"));
	        
	        	trendTable.setItems(trendData);
	        	trendTable.getColumns().addAll(timeCol, scenarioCol, trendCol,greenCol,redCol,whiteCol,swimCol,ibCol);
	        	vbox.getChildren().addAll(trendTable);
	        	
	        	tbDataHash.put(scenario.getScenario(), trendData);
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
			    	Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,"Close?");
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
		for (ScenarioTrend s : activeScenariolist) {
			Util.createDir(dssPath + "//" + s.getScenario());
		}
		//trendprofit
		Util.createDir(SystemConfig.DOC_PATH + "//trendprofit");
		
		//get all daily active scenario
		for (ScenarioTrend s : activeScenariolist) {
			ScenarioTrend trend  =  new ScenarioTrend();
			trend.setScenario(s.getScenario());
			Enum<SystemEnum.Trend> lastTrend = scenarioService.getTodayLastTrendByScenario(trend.getScenario());
			trend.setTrend(lastTrend);
			getSceTrendList().add(trend);
		}

		ArrayList<DailyScenarioRefresh> volPlans = scenarioService.getVolRefreshPlan();
		ArrayList<DailyScenarioRefresh> scePlans = scenarioService.getSceRefreshPlan();
		if(volPlans.size() == 0 && scePlans.size() == 0) {
			//none scenario plan
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Warning");
			alert.setHeaderText(null);
			alert.setContentText("none scenario plan");
			alert.showAndWait();
			return;
		}
		
		//load start end time label
		Date volStartTime = null,volEndTime = null,sceStartTime = null,sceEndTime = null,finalStartTime = null,finalEndTime = null;
		if(volPlans.size() > 0) {
			
			StringBuilder str1 = new StringBuilder(Util.getDateStringByDateAndFormatter(new Date(), "yyyyMMdd"));
			str1.append(volPlans.get(0).getRefreshTime());
			volStartTime = Util.getDateByStringAndFormatter(str1.toString(), "yyyyMMddHH:mm:ss");
			
			StringBuilder str2 = new StringBuilder(Util.getDateStringByDateAndFormatter(new Date(), "yyyyMMdd"));
			str2.append(volPlans.get(volPlans.size()-1).getRefreshTime());
			volEndTime = Util.getDateByStringAndFormatter(str2.toString(),"yyyyMMddHH:mm:ss");
		}
		if(scePlans.size() > 0) {
			
			StringBuilder str3 = new StringBuilder(Util.getDateStringByDateAndFormatter(new Date(), "yyyyMMdd"));
			str3.append(scePlans.get(0).getRefreshTime());
			sceStartTime = Util.getDateByStringAndFormatter(str3.toString(),"yyyyMMddHH:mm:ss");
			
			StringBuilder str4 = new StringBuilder(Util.getDateStringByDateAndFormatter(new Date(), "yyyyMMdd"));
			str4.append(scePlans.get(scePlans.size()-1).getRefreshTime());
			sceEndTime = Util.getDateByStringAndFormatter(str4.toString(),"yyyyMMddHH:mm:ss");
		}
		
		if (volPlans.size() > 0 && scePlans.size() > 0) {
			
			if(sceStartTime.before(volStartTime)) {
				finalStartTime = sceStartTime;
			} else {
				finalStartTime = volStartTime;
			}
			if(sceEndTime.before(volEndTime)) {
				finalEndTime = volEndTime;
			} else {
				finalEndTime = sceEndTime;
			}
		} else if(volPlans.size() > 0) {
			finalStartTime = volStartTime;
			finalEndTime = volEndTime;
		} else {
			finalStartTime = sceStartTime;
			finalEndTime = sceEndTime;
		}
		
		startTimeLbl.setText(Util.getDateStringByDateAndFormatter(finalStartTime, "HH:mm:ss"));
		endTimeLbl.setText(Util.getDateStringByDateAndFormatter(finalEndTime, "HH:mm:ss"));
		
		int didPassedCountVol =  scenarioService.getPassedVolRefreshPlanCount();
		int didPassedCountSce =  scenarioService.getPassedSceRefreshPlanCount();
		
		if (didPassedCountVol == scenarioService.getVolRefreshPlan().size() && 
				didPassedCountSce == scenarioService.getSceRefreshPlan().size()) {
			//every plan passed, including the close time
			//set tomorrow timer
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
			
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Warning");
			alert.setHeaderText(null);
			alert.setContentText("every scenario is end");
			alert.showAndWait();
			return;
		} else if (didPassedCountVol == 0 && didPassedCountSce == 0) { 
			
			secTimer = new Timer ();
			secTimer.scheduleAtFixedRate(new TimerTask() {
		        public void run() {
		        	calledBySecondTimer();
		        	
		        }
			}, finalStartTime, timerRefreshMSec);
			
		}  else {
			
			if (didPassedCountSce > 0 && didPassedCountSce != scenarioService.getSceRefreshPlan().size()) {
				
				DailyScenarioRefresh sceRefresh = scenarioService.getSceRefreshPlan().get(didPassedCountSce-1);
				sceRefresh.setPassed(false);
				scenarioService.setPassedSceRefreshPlanCount(didPassedCountSce-1);
				scenarioService.updateWorkingScenarioListByRefreshPlan();
			}
			
			if (didPassedCountVol > 0 && didPassedCountVol != scenarioService.getVolRefreshPlan().size()) {
				
				DailyScenarioRefresh volRefresh = scenarioService.getVolRefreshPlan().get(didPassedCountVol-1);
				volRefresh.setPassed(false);
				scenarioService.setPassedVolRefreshPlanCount(didPassedCountVol-1);
				scenarioService.updateWorkingVolumeListByRefreshPlan();
			}
			secTimer = new Timer ();
			secTimer.scheduleAtFixedRate(new TimerTask() {
		        public void run() {
		        	calledBySecondTimer();
		        	
		        }
			}, 1, timerRefreshMSec);
		}
	}
	
	private void calledBySecondTimer () {
		
		
		ScenarioGroupService scenarioService = ScenarioGroupService.getInstance();
		
		if (scenarioService.getActiveScenarioGroupList().size() == 0) {
			
			return;
		}
		
		ibTimerCount ++;
		if(ibTimerCount == ibTimerCountMax) {
			IBService ibService = IBService.getInstance();
			if(ibService.getIbApiConfig().isActive()) {
				if(ibService.isIBConnecting()) {
					System.out.println("IB connected.");
				} else {
					playSignAlertMusic();
				}
			}
			ibTimerCount = 0;
		}
		
		ZoneColorInfoService colorService = ZoneColorInfoService.getInstance();
		//every plan passed
		if (scenarioService.getPassedVolRefreshPlanCount() == scenarioService.getVolRefreshPlan().size() &&
				scenarioService.getPassedSceRefreshPlanCount() == scenarioService.getSceRefreshPlan().size()) {
			
			scenarioService.exportTodayTrendProfit(); //export ºexcel
			
			StringBuilder str = new StringBuilder(Util.getDateStringByDateAndFormatter(new Date(), "yyyyMMdd"));
    		str.append(startTimeLbl.getText());
    		Date finalStarTime = Util.getDateByStringAndFormatter(str.toString(), "yyyyMMddHH:mm:ss");
			secTimer = new Timer ();
			Calendar c = Calendar.getInstance();
			c.setTime(finalStarTime);
			c.add(Calendar.DATE, +1);
//			c.add(Calendar.SECOND, +1);
			finalStarTime = c.getTime();
			secTimer.scheduleAtFixedRate(new TimerTask() {
		        public void run() {
		        	calledBySecondTimer();
		        	
		        }
			}, finalStarTime, timerRefreshMSec);
			
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Warning");
			alert.setHeaderText(null);
			alert.setContentText("every scenario is end");
			alert.showAndWait();
			
		} else {
			//now is a refresh time
			if(isSceRefreshTime()) {
				scenarioService.updateWorkingScenarioListByRefreshPlan();
			}
			if(isVolZoneRefreshTime()) {
				scenarioService.updateWorkingVolumeZoneListByRefreshPlan();
			}
			if(isVolRefreshTime()) {
				scenarioService.updateWorkingVolumeListByRefreshPlan();
			}
			
			//get screen color
			colorService.updateSceZoneAndVolumeBarZoneColorByTimer();
			scenarioService.updateRelatedVolZone();
			
			colorService.updateVolumeZoneColorAfterReloadVolBarByTimer();
			
			//check trend
			scenarioService.checkScenarioGroupTrend();
			
			Platform.runLater(()->yellowCountLbl.setText(""+scenarioService.getYellowZoneCount()));
			
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
	
	private void closeApplication() {
		
		secTimer.cancel();
		
		boolean needCloseOrder = false;
		//close order
		for (ScenarioTrend st : sceTrendList) {
			if(st.getTrend() != SystemEnum.Trend.Default) {
				ScenarioGroupService scenarioService =  ScenarioGroupService.getInstance();
				scenarioService.closeOrderByScenario(st.getScenario());
				scenarioService.setNeedCloseApp(true);
				needCloseOrder = true;
			}
		}
		
		if(needCloseOrder) return; //close app after price update
		
		if(IBService.getInstance().getIbApiConfig().isActive() && IBService.getInstance().isIBConnecting()) {
			IBService.getInstance().ibDisConnect();
		}
		
		try {
            Thread.sleep(2000);
        } catch (Exception e) {
        	e.printStackTrace();
        }
		//export xls
		ScenarioGroupService.getInstance().exportTodayTrendProfit();
		try {
            Thread.sleep(1000);
        } catch (Exception e) {
        	e.printStackTrace();
        }
		//shutdown
		Platform.exit();
        System.exit(0);
	}
	
	public void stop() {
		
	}
	
	public static void main(String[] args) {
		launch(args);
	}

	public ArrayList<ScenarioTrend> getSceTrendList() {
		return sceTrendList;
	}

	public void setSceTrendList(ArrayList<ScenarioTrend> sceTrendList) {
		this.sceTrendList = sceTrendList;
	}

	public boolean isWantCloseApp() {
		return wantCloseApp;
	}

	public void setWantCloseApp(boolean wantCloseApp) {
		this.wantCloseApp = wantCloseApp;
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
		ScenarioGroupService.getInstance().exportTodayTrendProfit();
		try {
            Thread.sleep(1000);
        } catch (Exception e) {
        	e.printStackTrace();
        }
		//shutdown
		Platform.exit();
        System.exit(0);
	}
}
