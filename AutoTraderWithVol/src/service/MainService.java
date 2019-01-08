package service;

import java.util.ArrayList;

import dao.CommonDAO;
import dao.CommonDAOFactory;
import tool.Util;

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
    	
    	CommonDAO commonDao = CommonDAOFactory.getCommonDAO();
    	for (String[] result : resultList) {
    		String scenario = result[0];
    		int active = Integer.parseInt(result[1]);
    		commonDao.insertScenarioActive(scenario,active);
    	}
    }
    
    private void insertScenario() {
    	
    	CommonDAO commonDao = CommonDAOFactory.getCommonDAO();
    	ArrayList<String> scenarioList = commonDao.getAllActiveScenarioName();
    	for (String as : scenarioList) {
    		
    		ArrayList<String[]> resultList = Util.readCSVFile(as + "_scenario");
        	if(resultList.size() == 0) continue;
        	
        	for (String[] result : resultList) {
        		String sname = result[0];
        		String starttime = result[1];
        		String endtime = result[2];
        		String area = result[3];
        		int percent = Integer.parseInt(result[4]);
        		int white_min = Integer.parseInt(result[5]);
        		commonDao.insertScenario(sname,starttime,endtime,area,percent,white_min);
        	}
    	}
    }
    
    private void insertAreaZone() {

    	CommonDAO commonDao = CommonDAOFactory.getCommonDAO();
    	ArrayList<String> scenarioList = commonDao.getAllActiveScenarioName();
    	for (String as : scenarioList) {
    		
    		ArrayList<String[]> resultList = Util.readCSVFile(as + "_area_zone");
        	if(resultList.size() == 0) continue;

        	for (String[] result : resultList) {
        		String sname = result[0];
        		String starttime = result[1];
        		String area = result[2];
        		String zone = result[3];
        		int active = Integer.parseInt(result[4]);
        		commonDao.insertAreaZone(sname, starttime, area, zone, active);
        	}
    	}
    }

    private void insertMyFrame() {
	
    	ArrayList<String[]> resultList = Util.readCSVFile("my_frame");
    	if(resultList.size() == 0) return;
    	
    	CommonDAO commonDao = CommonDAOFactory.getCommonDAO();
    	for (String[] result : resultList) {
    		String name = result[0];
    		int x = Integer.parseInt(result[1]);
    		int y = Integer.parseInt(result[2]);
    		int width = Integer.parseInt(result[3]);
    		int height = Integer.parseInt(result[4]);
    		commonDao.insertMyFrame(name, x, y, width, height);
    	}
    }
    
    private void insertVolumeZone() {
    	
    	ArrayList<String[]> resultList = Util.readCSVFile("volume_zone");
    	if(resultList.size() == 0) return;
    	
    	CommonDAO commonDao = CommonDAOFactory.getCommonDAO();
    	for (String[] result : resultList) {
    		String time = result[0];
    		String zone = result[1];
    		commonDao.insertVolumeZone(time,zone);
    	}
    }
    
    private void insertVolume() {
    	
    	ArrayList<String[]> resultList = Util.readCSVFile("volume");
    	if(resultList.size() == 0) return;
    	
    	CommonDAO commonDao = CommonDAOFactory.getCommonDAO();
    	for (String[] result : resultList) {
    		String scenario = result[0];
    		String start = result[1];
    		String end = result[2];
    		int column = Integer.parseInt(result[3]);
    		int percent = Integer.parseInt(result[4]);
    		int whiteMax = Integer.parseInt(result[5]);
    		String rows = result[6];
    		commonDao.insertVolume(scenario,start,end,column,percent,whiteMax,rows);
    	}
    }
    
    private void insertNewDataFromCSV() {
    	
    	insertScenarioActive();
    	insertScenario();
    	insertAreaZone();
    	insertMyFrame();
    	insertVolumeZone();
    	insertVolume();
    }
    
    private void cleanOldDataInDB() {
    	
    	CommonDAO commonDao = CommonDAOFactory.getCommonDAO();
    	commonDao.cleanScenarioActiveData();
    	
    	commonDao.cleanScenarioData();
    	commonDao.cleanAreaZone();
    	
    	commonDao.cleanMyFrame();
    	
    	commonDao.cleanVolumeZone();
    	commonDao.cleanVolume();
    }
    
    public void refreshDBdataFromCSV() {
    	
    	cleanOldDataInDB();
    	insertNewDataFromCSV();
    }
}
