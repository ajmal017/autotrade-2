package dao;

import java.util.ArrayList;
import java.util.Date;

import entity.Area;
import entity.Scenario;
import entity.Zone;

public interface CommonDAO {

	ArrayList<String> getAllActiveScenarioName();

	ArrayList<String> getAllDistinctScenarioStartTimeAndEndTime();
	
	ArrayList<String> getAllDistinctVolumeStartTimeAndEndTime();
	
	ArrayList<Scenario> getAllWorkingScenarioAtTime(Date time);
	
	ArrayList<Area> getAreaListWithoutZoneByScenario(String scenario,Date time);
	
	ArrayList<String> getOnlyActiveZoneListByScenarioArea(String scenario, String startTime, String area);
	
	ArrayList<Zone> getRelatedZoneListByScenarioList(ArrayList<Scenario> scenarioList);
	
	ArrayList<Zone> getVolumeZoneList();
}
