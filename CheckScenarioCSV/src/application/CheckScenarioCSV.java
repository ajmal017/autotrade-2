package application;
	
import java.awt.Label;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;


public class CheckScenarioCSV extends Application {
	
	private static BufferedReader bReader;
	
	public ArrayList<String[]> readCSVFile(String filename) {

    	ArrayList<String[]> resultList = new ArrayList<String[]>();
    	
    	String path = "c://autotradedoc_vol//csv//" + 
				filename + 
				".csv";
    	File file = new File(path);
    	if (!file.exists()) return resultList;
    	
    	try {
    		bReader = new BufferedReader(new FileReader(path));
    		String line = null;
    		while((line=bReader.readLine())!=null && line.length() > 0){
    			String item[] = line.split(",");
    			resultList.add(item);              
    		}
        	return resultList;
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	return resultList;
    }
	
	
	@Override
	public void start(Stage primaryStage) {
		try {
			
			//get active scenario
			ArrayList<String[]> activeList = readCSVFile("scenario_active");
			ArrayList<String[]> volList = readCSVFile("volume");
			
    		StringBuilder sb = new StringBuilder();
	    	//scenario area list
	    	for (String[] activeItem : activeList) {
	    		String scenario = activeItem[0];
	    		int active = Integer.parseInt(activeItem[1]);
	    		if(active == 1) {
	    			
	    			ArrayList<String[]> scenarioList = readCSVFile(scenario + "_scenario");
	    			ArrayList<String[]> zoneList = readCSVFile(scenario + "_area_zone");
	            	if(scenarioList.size() == 0 || zoneList.size() == 0) continue;
	            	
	            	for (String[] scenarioItem : scenarioList) {
	            		
	            		String sName = scenarioItem[0];
	            		String startTime = scenarioItem[1];
	            		String endTime = scenarioItem[2];
	            		String areaName = scenarioItem[3];
	            		String percent = scenarioItem[4];
	            		sb.append("Time:"+sName+",");
	            		sb.append("start:"+startTime+",");
	            		sb.append("end:"+endTime+",");
	            		sb.append("area:"+areaName+",");
	            		sb.append("percent:"+percent+"/");
	            		
	            		int zoneCount = 0;
	            		for (String[] zoneItem : zoneList) {
	          
	                		int zoneActive = Integer.parseInt(zoneItem[4]);
	                		if (sName.equals(zoneItem[0]) &&
	                				startTime.equals(zoneItem[1]) &&
	                				areaName.equals(zoneItem[2]) &&
	                			zoneActive == 1) {
	                			zoneCount++;
	                		}
	            		}
	            		sb.append(zoneCount+"\n");
	            	}
	    			
	            	for(String[] vol : volList) {
	            			            		
	            		if(scenario.equals(vol[0])) {

		            		sb.append("Volume:" + vol[0]+",");
		            		sb.append("start:"+vol[1]+",");
		            		sb.append("end:"+vol[2]+",");
		            		sb.append("column:"+vol[3]+",");
		            		sb.append("percent:"+vol[4]+",");
		            		sb.append("rows:"+vol[5]+"\n");
	            		}
	            	}
	            	
	            	
	            	sb.append("\n");
	    		}
	    	}
			
	    	
	    	
	    		
	    		Text l1 = new Text();
	    		l1.setText(sb.toString());
	    		final VBox vbox = new VBox();
	    		vbox.setMinSize(380, 380);
	    		vbox.getChildren().addAll(l1);
	    	
	    		BorderPane root = new BorderPane();
	    		root.setMinSize(400, 400);
	    		root.setTop(l1);
	    		root.setPadding(new Insets(10, 10, 10, 10));
	    		Scene scene = new Scene(root);
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
}
