package service;

import java.util.ArrayList;

import dao.CommonDAO;
import dao.CommonDAOFactory;
import entity.Zone;
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
    
   
    private void insertSettingActive() {
    	
    	ArrayList<String[]> resultList = Util.readCSVFile("setting_active");
    	if(resultList.size() == 0) return;
    	
    	CommonDAO commonDao = CommonDAOFactory.getCommonDAO();
    	for (String[] result : resultList) {
    		String setting = result[0];
    		int active = Integer.parseInt(result[1]);
    		commonDao.insertSettingActive(setting,active);
    	}
    }
    
    private void insertSetting() {
    	
    	CommonDAO commonDao = CommonDAOFactory.getCommonDAO();
    	
    		
    		ArrayList<String[]> resultList = Util.readCSVFile("setting");
        	if(resultList.size() == 0) return;
        	
        	for (String[] result : resultList) {
        		String sname = result[0];
        		String startTime = result[1];
        		String endTime = result[2];
        		int orderIndex = Integer.parseInt(result[3]);
        		double limitChange = Double.parseDouble(result[4]);
        		double tick = Double.parseDouble(result[5]);
        		double profitLimitChange = Double.parseDouble(result[6]);
        	
        		commonDao.insertSetting(sname,startTime,endTime,orderIndex,limitChange,tick,profitLimitChange);
        	}
    }
    
    private void insertCloseZone() {
	
    	ArrayList<String[]> resultList = Util.readCSVFile("close_zone");
    	if(resultList.size() == 0) return;
    	
    	CommonDAO commonDao = CommonDAOFactory.getCommonDAO();
    	for (String[] result : resultList) {
    		String zone = result[0];
    		int x = Integer.parseInt(result[1]);
    		int y = Integer.parseInt(result[2]);
    		commonDao.insertCloseZone(zone, x, y);
    	}
    }
    
    private void insertNewDataFromCSV() {
    	
    	insertSettingActive();
    	insertSetting();
    	insertCloseZone();
    }
    
    private void cleanOldDataInDB() {
    	
    	CommonDAO commonDao = CommonDAOFactory.getCommonDAO();
    	commonDao.cleanSettingActive();
    	commonDao.cleanSetting();
    	commonDao.cleanCloseZone();
    }
    
    public void refreshDBdataFromCSV() {
    	
    	cleanOldDataInDB();
    	insertNewDataFromCSV();
    }
}
