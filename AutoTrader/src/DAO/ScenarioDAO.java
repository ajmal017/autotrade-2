package DAO;

import java.util.ArrayList;
import java.util.Date;

import entity.Area;
import entity.Scenario;

public interface ScenarioDAO {
	
	ArrayList<String> getAllActiveScenarioName();
	ArrayList<String> getAllDistinctScenarioStartTimeAndEndTime();
	ArrayList<Scenario> getAllWorkingScenarioAtTime(Date time);
	ArrayList<Area> getAreaListWithoutZoneByScenario(String scenario, String startTime);
}
