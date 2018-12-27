package service;

import java.util.ArrayList;

import DAO.ScenarioDAO;
import DAO.ScenarioDAOFactory;
import DAO.ZoneDAO;
import DAO.ZoneDAOFactory;
import tool.Util;
import DAO.TrendSignDAO;
import DAO.TrendSignDAOFactory;

public class MainService {

	private volatile static MainService instance;
	
	//init 
    private MainService ()  {
    	
    } 
    
    public static MainService getInstance() {  
    	if (instance == null) {  
    		synchronized (MainService.class) {  
    			if (instance == null) {  
    				instance = new MainService();  
    			}	  
    		}  
    	}  
    	return instance;  
    }
    
    
    private void insertScenarioActive() {
    	
    	ArrayList<String[]> resultList = Util.readCSVFile("scenario_active");
    	if(resultList.size() == 0) return;
    	
    	ScenarioDAO scenarioDao = ScenarioDAOFactory.getScenarioDAO();
    	for(String[] result : resultList) {
    		String scenario = result[0];
    		int active = Integer.parseInt(result[1]);
    		scenarioDao.insertScenarioActive(scenario,active);
    	}
    }
    
    private void insertScenario() {
    	
    	ScenarioDAO scenarioDao = ScenarioDAOFactory.getScenarioDAO();
    	ArrayList<String> scenarioList = scenarioDao.getAllActiveScenarioName();
    	for (String as : scenarioList) {
    		
    		ArrayList<String[]> resultList = Util.readCSVFile(as + "_scenario");
        	if(resultList.size() == 0) continue;
        	
        	for(String[] result : resultList) {
        		String sname = result[0];
        		String starttime = result[1];
        		String endtime = result[2];
        		String area = result[3];
        		int percent = Integer.parseInt(result[4]);
        		scenarioDao.insertScenario(sname,starttime,endtime,area,percent);
        	}
    	}
    }
    
    private void insertAreaZone() {

    	ScenarioDAO scenarioDao = ScenarioDAOFactory.getScenarioDAO();
    	ArrayList<String> scenarioList = scenarioDao.getAllActiveScenarioName();
    	for (String as : scenarioList) {
    		
    		ArrayList<String[]> resultList = Util.readCSVFile(as + "_area_zone");
        	if(resultList.size() == 0) continue;

        	ZoneDAO zoneDao = ZoneDAOFactory.getZoneDAO();
        	for(String[] result : resultList) {
        		String sname = result[0];
        		String starttime = result[1];
        		String area = result[2];
        		String zone = result[3];
        		int active = Integer.parseInt(result[4]);
        		zoneDao.insertAreaZone(sname, starttime, area, zone, active);
        	}
    	}
    }

    private void insertMyFrame() {
	
    	ArrayList<String[]> resultList = Util.readCSVFile("my_frame");
    	if(resultList.size() == 0) return;
    	
    	ZoneDAO zoneDao = ZoneDAOFactory.getZoneDAO();
    	for(String[] result : resultList) {
    		String name = result[0];
    		int x = Integer.parseInt(result[1]);
    		int y = Integer.parseInt(result[2]);
    		int width = Integer.parseInt(result[3]);
    		int height = Integer.parseInt(result[4]);
    		zoneDao.insertMyFrame(name, x, y, width, height);
    	}
    }
    
    private void insertNewDataFromCSV() {
    	
    	insertScenarioActive();
    	insertScenario();
    	
    	insertAreaZone();
    	insertMyFrame();
    }
    
    private void cleanOldDataInDB() {
    	
    	ScenarioDAO scenarioDao = ScenarioDAOFactory.getScenarioDAO();
    	scenarioDao.cleanScenarioActiveData();
    	scenarioDao.cleanScenarioData();
    	
    	ZoneDAO zoneDao = ZoneDAOFactory.getZoneDAO();
    	zoneDao.cleanAreaZone();
    	zoneDao.cleanMyFrame();
    }
    
    public void refreshDBdataFromCSV() {
    	
    	cleanOldDataInDB();
    	insertNewDataFromCSV();
    }
}
