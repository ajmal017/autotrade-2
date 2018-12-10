package DAO;

import java.util.ArrayList;
import java.util.Date;

import entity.Area;
import entity.Scenario;

public interface ScenarioDAO {
	
	ArrayList<String> getAllActiveScenarioName();
	ArrayList<String> getAllDistinctScenarioStartTimeAndEndTime();
	ArrayList<Scenario> getAllWorkingScenarioAtTime(Date time);
	ArrayList<Area> getAreaListWithoutZoneByScenario(String scenario, Date time);
	
	void cleanScenarioActiveData();
	void cleanScenarioData();
	
	void insertScenarioActive(String scenario, 
							  int active);
	void insertScenario(String scenario, 
						String starttime, 
						String endtime, 
						String area, 
						int percent);
}
