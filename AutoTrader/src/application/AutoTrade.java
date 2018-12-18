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
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import service.MainService;
import service.ScenarioService;
import service.TrendSignService;
import service.ZoneColorInfoService;
import systemenum.SystemEnum;
import tool.Util;
import tool.MP3Player;


public class AutoTrade extends Application {
	
	public static int timerRefreshMSec = 1000; //ms
	
    private Timer secTimer;
    private ArrayList<ScenarioTrend> sceTrendList;
	
    private final Label startTimeLbl = new Label();
	private final Label endTimeLbl = new Label();
	private final Label greenCountLbl = new Label();
	private final Label redCountLbl = new Label();
	
	private Hashtable<String,Object> tbDataHash;
	
    
	@Override
	public void start(Stage primaryStage) {
		
		sceTrendList = new ArrayList<ScenarioTrend>();
		tbDataHash = new Hashtable<String,Object>();
		
		

		//test
		//code here for test
		/*
		String swimPriceStr = Util.getStringByScreenShotPng(SystemConfig.DOC_PATH,SystemConfig.PRICE_IMG_NAME);
		System.out.println("swimPriceStr:"+swimPriceStr);
		double priceSwim = 0.0;
    	if(swimPriceStr != null && swimPriceStr.length() > 0) {
    		priceSwim = Util.getPriceByString(swimPriceStr);
    		System.out.println("priceSwim:"+priceSwim);
    	}
    	*/
		
		
//    	/*
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
	        
	        final VBox vbox = new VBox();
	        vbox.setMinSize(400, 400);
	        vbox.setSpacing(5);
	        vbox.setPadding(new Insets(10, 0, 0, 10));
	        vbox.getChildren().addAll(hb1,hb2);
	        
	        //table

	        for (String scenario : activeScenariolist) {
	        
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
	        	vbox.getChildren().addAll(trendTable);
	        	
	        	tbDataHash.put(scenario, trendData);
	        }
	        
			BorderPane root = new BorderPane();
			root.setMinSize(400, 400);
			root.setTop(vbox);
			
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			    @Override
			    public void handle(WindowEvent t) {
			        Platform.exit();
			        System.exit(0);
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
			}, 1, timerRefreshMSec);
			
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
//		*/
	}
	
	private int getGreen() {
		
		int green = 0;
		Enumeration<Zone> e = ZoneColorInfoService.getInstance().getZoneColors().elements();
		while( e. hasMoreElements() ){
			
			Zone zone = e.nextElement();
			if (zone.getColor() == SystemEnum.Color.Green) {green++;}
		}
		return green;
	}
	
	private int getRed() {
		
		int red = 0;
		Enumeration<Zone> e = ZoneColorInfoService.getInstance().getZoneColors().elements();
		while( e. hasMoreElements() ){
			
			Zone zone = e.nextElement();
			if (zone.getColor() == SystemEnum.Color.Red) {red++;}
		}
		return red;
	}
	
	private void calledBySecondTimer () {
		
		
		ScenarioService scenarioService = ScenarioService.getInstance();
		ZoneColorInfoService colorService = ZoneColorInfoService.getInstance();
		TrendSignService trendService = TrendSignService.getInstance();
		
		if (scenarioService.getActiveScenarioList().size() == 0 ||
				scenarioService.getSceRefreshPlan().size() == 0) {
			
			return;
		}
		
		//every plan passed
		if (scenarioService.getPassedRefreshPlanCount()  == scenarioService.getSceRefreshPlan().size()) {
			
			trendService.exportTodayTrendProfit(); //export ºexcel
			
			StringBuilder str = new StringBuilder(Util.getDateStringByDateAndFormatter(new Date(), "yyyyMMdd"));
    		str.append(scenarioService.getSceRefreshPlan().get(0).getRefreshTime());
    		Date startTime = Util.getDateByStringAndFormatter(str.toString(), "yyyyMMddHH:mm");
    		secTimer.cancel();
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
			
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Warning");
			alert.setHeaderText(null);
			alert.setContentText("every scenario is end");
			alert.showAndWait();
			
			playSignAlertMusic();
			
		} else {
			//now is a refresh time
			if(isRefreshTime()) {
				scenarioService.updateWorkingScenarioListByRefreshPlan();
			}
			//get screen color
			colorService.updateZoneColorByTimer();
			
			//check trend
			trendService.checkScenarioTrend();
			
			Platform.runLater(()->greenCountLbl.setText(""+getGreen()));
			Platform.runLater(()->redCountLbl.setText(""+getRed()));
			
			//check and update trend
			for (ScenarioTrend oldTrend : sceTrendList) {
				
				boolean sceWorking = false;
				for (Scenario scenario : scenarioService.getWorkingScenarioList()) {
					
					if(oldTrend.getScenario().equals(scenario.getScenario())) {sceWorking = true;}
					if(oldTrend.getScenario().equals(scenario.getScenario()) && 
							oldTrend.getTrend() != scenario.getTrend()) {
						//update trend
						oldTrend.setTrend(scenario.getTrend());
						//get last sign
						for (int i = trendService.getDailySignList().size()-1; i > -1; i--) {
							TrendSign lastSign = trendService.getDailySignList().get(i); 
							if (lastSign.getScenario().equals(oldTrend.getScenario())) {
								//insert into table
								TrendTableItem trendItem = new TrendTableItem(
										Util.getDateStringByDateAndFormatter(lastSign.getTime(), "HH:mm:ss"), lastSign.getScenario(),
										Util.getTrendTextByEnum(lastSign.getTrend()),""+lastSign.getGreenCount(), ""+lastSign.getRedCount(), ""+lastSign.getPriceSwim(), ""+lastSign.getPriceIB());
								ObservableList<TrendTableItem> trendData = (ObservableList<TrendTableItem>) tbDataHash.get(lastSign.getScenario());
								trendData.add(trendItem);
								break;
							}
						}
						playSignAlertMusic();
					}
				}
				if(!sceWorking) oldTrend.setTrend(SystemEnum.Trend.Default);
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
	
	private void playSignAlertMusic() {
		
		ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
	    cachedThreadPool.execute(new Runnable() {
	  
	        @Override
	        public void run() {
				 //music alert

	    		MP3Player mp3 = new MP3Player("c://autotradedoc//sign_bg.mp3");
	            mp3.play();
	        }
	    });
	    
	}
	
	private void closeApplication() {
		
		secTimer.cancel();
		
		//close order
		for (ScenarioTrend st : sceTrendList) {
			if(st.getTrend() != SystemEnum.Trend.Default) {
				
				ScenarioService.getInstance().closeOrderByScenario(st.getScenario());
			}
		}
		try {
            Thread.sleep(1000);
        } catch (Exception e) {
        	e.printStackTrace();
        }
		//export xls
		TrendSignService.getInstance().exportTodayTrendProfit();
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

	public ArrayList<ScenarioTrend> getSceTrendList() {
		return sceTrendList;
	}

	public void setSceTrendList(ArrayList<ScenarioTrend> sceTrendList) {
		this.sceTrendList = sceTrendList;
	}
	
}
