package application;

import java.awt.Rectangle;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import DAO.ZoneDAOFactory;
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
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import service.IBService;
import service.MainService;
import service.ScenarioService;
import service.TrendSignService;
import service.ZoneColorInfoService;
import systemenum.SystemEnum;
import tool.AePlayWave;
import tool.Util;


public class AutoTrade extends Application implements AutoTradeCallBackInterface  {
	
	public static int timerRefreshMSec = 1000; //ms
	
    private Timer secTimer;
    private ArrayList<ScenarioTrend> sceTrendList;
	
    private final Label startTimeLbl = new Label();
	private final Label endTimeLbl = new Label();
	private final Label greenCountLbl = new Label();
	private final Label redCountLbl = new Label();
	
	private final TableView<TrendTableItem> trendTable = new TableView<>();
    private final ObservableList<TrendTableItem> trendData =
            FXCollections.observableArrayList();
    
	@Override
	public void start(Stage primaryStage) {
		
		sceTrendList = new ArrayList<ScenarioTrend>();
		
		try {
			
			//UI
	        final HBox hb1 = new HBox();
	        hb1.setPrefSize(300, 15);
	        final Label sTitle = new Label("StartTime:");
	        final Label eTitle = new Label("EndTime:");
	        startTimeLbl.setPrefWidth(60);
	        endTimeLbl.setPrefWidth(60);
	        hb1.getChildren().addAll(sTitle, startTimeLbl, eTitle, endTimeLbl);
	        hb1.setSpacing(3);
	        
	        final HBox hb2 = new HBox();
	        hb2.setPrefSize(400, 15);
	        final Label gTitle = new Label("Green:");
	        final Label rTitle = new Label("Red:");
	        greenCountLbl.setPrefWidth(60);
	        greenCountLbl.setText("0");
	        redCountLbl.setPrefWidth(60);
	        redCountLbl.setText("0");
	        hb2.getChildren().addAll(gTitle, greenCountLbl, rTitle, redCountLbl);
	        hb2.setSpacing(3);

	        sTitle.setFont(new Font(12));
	        eTitle.setFont(new Font(12));
	        startTimeLbl.setFont(new Font(12));
	        endTimeLbl.setFont(new Font(12));
	        gTitle.setFont(new Font(12));
	        rTitle.setFont(new Font(12));
	        greenCountLbl.setFont(new Font(12));
	        redCountLbl.setFont(new Font(12));
	        
	        //table
	        trendTable.setEditable(false);
	        trendTable.setMinHeight(200);
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
	        
	        
	        TableColumn swimCol = new TableColumn("SwPr");
	        swimCol.setPrefWidth(60);
	        swimCol.setCellValueFactory(
	                new PropertyValueFactory<>("swimPrice"));
	        
	        TableColumn ibCol = new TableColumn("IBPr");
	        ibCol.setPrefWidth(60);
	        ibCol.setCellValueFactory(
	                new PropertyValueFactory<>("ibPrice"));
	        
	        trendTable.setItems(trendData);
	        trendTable.getColumns().addAll(timeCol, scenarioCol, trendCol,greenCol,redCol,swimCol,ibCol);
	        
	        final VBox vbox = new VBox();
	        vbox.setMaxSize(390, 200);
	        vbox.setSpacing(5);
	        vbox.setPadding(new Insets(10, 0, 0, 10));
	        vbox.getChildren().addAll(hb1,hb2,trendTable);
	        
			BorderPane root = new BorderPane();
			root.setMinSize(400, 400);
			root.setTop(vbox);
			
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		

		//test
		//code here for test
		
		
		
		//init data from CSV file
		MainService mainService = MainService.getInstance();
		mainService.refreshDBdataFromCSV();
		
		ScenarioService scenarioService = ScenarioService.getInstance();
		ArrayList<String> activeScenariolist = scenarioService.getActiveScenarioList();
		if(activeScenariolist.size() == 0) {
			//none active scenario
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Warning");
			alert.setHeaderText(null);
			alert.setContentText("none active scenario");
			alert.showAndWait();
			return;
		}
		//create daily path
		String dataStr = Util.getDateStringByDateAndFormatter(new Date(), "yyyyMMdd");
		//screenshot
		String ssPath = SystemConfig.DOC_PATH + "//screenshot";
		Util.createDir(ssPath);
		String dssPath = ssPath + "//" + dataStr;
		Util.createDir(dssPath);
		for (String s : activeScenariolist) {
			Util.createDir(dssPath + "//" + s);
		}
		//trendprofit
		Util.createDir(SystemConfig.DOC_PATH + "//trendprofit");
		
		//get all daily active scenario
		for (String s : activeScenariolist) {
			ScenarioTrend trend  =  new ScenarioTrend();
			trend.setScenario(s);
			getSceTrendList().add(trend);
		};
		
		if(scenarioService.getSceRefreshPlan().size() == 0) {
			//none scenario plan
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Warning");
			alert.setHeaderText(null);
			alert.setContentText("none scenario plan");
			alert.showAndWait();
			return;
		}
		
		//load start end time label
		ArrayList<DailyScenarioRefresh> plans = scenarioService.getSceRefreshPlan();    	
		startTimeLbl.setText(plans.get(0).getRefreshTime());
		endTimeLbl.setText(plans.get(plans.size()-1).getRefreshTime());
		
		int didPassedCount =  scenarioService.getPassedRefreshPlanCount();
		if (didPassedCount == scenarioService.getSceRefreshPlan().size()) {
			//every plan passed, including the close time
			//set tomorrow timer
			StringBuilder str = new StringBuilder(Util.getDateStringByDateAndFormatter(new Date(), "yyyyMMdd"));
    		str.append(plans.get(0).getRefreshTime());
    		Date startTime = Util.getDateByStringAndFormatter(str.toString(), "yyyyMMddHH:mm");
			secTimer = new Timer();
			Calendar c = Calendar.getInstance();
			c.setTime(startTime);
			c.add(Calendar.DATE, +1); //tomorrow
			c.add(Calendar.SECOND, +1); //delay 1 sec for swim's refresh
			startTime = c.getTime();
			secTimer.scheduleAtFixedRate(new TimerTask() {
		        public void run() {
		        	calledBySecondTimer();
		        }
			}, startTime, timerRefreshMSec);
			
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Warning");
			alert.setHeaderText(null);
			alert.setContentText("every scenario is end");
			alert.showAndWait();
			return;
		} else if (didPassedCount > 0) { 
			//scenario started
			
			//change last plan's passed state to NO, then run following logic
			DailyScenarioRefresh refresh  =  scenarioService.getSceRefreshPlan().get(didPassedCount-1);
			refresh.setPassed(false);
			scenarioService.setPassedRefreshPlanCount(didPassedCount-1);
			scenarioService.updateWorkingScenarioListByRefreshPlan();
			
			//load appeared trend (if app reload during market time)
			for(ScenarioTrend trend : getSceTrendList()) {
				trend.setTrend(TrendSignService.getInstance().getTodayLastTrendByScenario(trend.getScenario()));
			}
			
			secTimer = new Timer ();
			secTimer.scheduleAtFixedRate(new TimerTask() {
		        public void run() {
		        	calledBySecondTimer();
		        	
		        }
			}, 5, timerRefreshMSec);
			
		}  else {
			//scenario did not start
			StringBuilder str = new StringBuilder(Util.getDateStringByDateAndFormatter(new Date(), "yyyyMMdd"));
    		str.append(plans.get(0).getRefreshTime());
    		Date startTime = Util.getDateByStringAndFormatter(str.toString(), "yyyyMMddHH:mm");
			secTimer = new Timer ();
			Calendar c = Calendar.getInstance();
			c.setTime(startTime);
			c.add(Calendar.SECOND, +1);
			startTime = c.getTime();
			secTimer.scheduleAtFixedRate(new TimerTask() {
		        public void run() {
		        	calledBySecondTimer();
		        	
		        }
			}, startTime, timerRefreshMSec);
		}
		
	}
	
	private void calledBySecondTimer () {
		
		ScenarioService scenarioService = ScenarioService.getInstance();
		ZoneColorInfoService colorService = ZoneColorInfoService.getInstance();
		TrendSignService trendService = TrendSignService.getInstance(); 

		/* now update the CSV files and must restart the app again.
		 * 
		//check active scenario update(24h working, update scenario active in night)
		if(scenarioService.activeScenarioDidChanged()) {
			scenarioService.reloadAllScenarioIfNeeded();
			getSceTrendList().clear();
			for (String s : scenarioService.getActiveScenarioList()) {
				ScenarioTrend trend  =  new ScenarioTrend();
				trend.setScenario(s);
				getSceTrendList().add(trend);
			}
		}
		*/
		
		if (scenarioService.getActiveScenarioList().size() == 0 ||
				scenarioService.getSceRefreshPlan().size() == 0) {
			
			return;
		}
		
		//every plan passed
		if (scenarioService.getPassedRefreshPlanCount()  == scenarioService.getSceRefreshPlan().size()) {
			
			trendService.todayScenarioIsFinished(); //export ºexcel
			
			StringBuilder str = new StringBuilder(Util.getDateStringByDateAndFormatter(new Date(), "yyyyMMdd"));
    		str.append(scenarioService.getSceRefreshPlan().get(0).getRefreshTime());
    		Date startTime = Util.getDateByStringAndFormatter(str.toString(), "yyyyMMddHH:mm");
			secTimer = new Timer();
			Calendar c = Calendar.getInstance();
			c.setTime(startTime);
			c.add(Calendar.DATE, +1);
			c.add(Calendar.SECOND, +1);
			startTime = c.getTime();
			secTimer.scheduleAtFixedRate(new TimerTask() {
		        public void run() {
		        	calledBySecondTimer();
		        }
			}, startTime, timerRefreshMSec);
			
		} else {
			//now is a refresh time
			if(isRefreshTime()) {
				scenarioService.updateWorkingScenarioListByRefreshPlan();
			}
			//get screen color
			colorService.updateZoneColorByTimer(this);
			
			//check trend
			trendService.checkScenarioTrend();
			
			//table
			//check and update trend
			for (ScenarioTrend oldTrend : sceTrendList) {
				for (Scenario scenario : scenarioService.getWorkingScenarioList()) {
					
					if(oldTrend.getScenario().equals(scenario.getScenario()) && 
							oldTrend.getTrend() != scenario.getTrend()) {
						
						//update trend
						oldTrend.setTrend(scenario.getTrend());
						 //music alert
						playSignAlertMusic();
						//get last sign
						for (int i = trendService.getDailySignList().size()-1; i > -1; i--) {
							TrendSign lastSign = trendService.getDailySignList().get(i); 
							if (lastSign.getScenario().equals(oldTrend.getScenario())) {
								//insert into table
								TrendTableItem trendItem = new TrendTableItem(
										Util.getDateStringByDateAndFormatter(lastSign.getTime(), "HH:mm:ss"), 
										lastSign.getScenario(),
										Util.getTrendTextByEnum(lastSign.getTrend()),
										""+lastSign.getGreenCount(), 
										""+lastSign.getRedCount(), 
										""+lastSign.getPriceSwim(), 
										""+lastSign.getPriceIB());
								trendData.add(trendItem);
								
								break;
							}
						}
						
						
					}
				}
			}
		}
	}
	
	private boolean isRefreshTime() {
		
		String nowTimeString = Util.getDateStringByDateAndFormatter(new Date(), "HH:mm");
		ScenarioService sService = ScenarioService.getInstance();
		if (sService.getSceRefreshPlan().size() == 0) {
			return false;
		}
		DailyScenarioRefresh nextFresh = sService.getSceRefreshPlan().get(sService.getPassedRefreshPlanCount());
		if (nowTimeString.equals(nextFresh.getRefreshTime())) {
			return true;
		} else {
			return false;
		}
	}
	
	private void playSignAlertMusic () {
		
		new AePlayWave("resource/sign_bg.wav").start();
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
	
}
