package dao;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Date;

import entity.Area;
import entity.Scenario;
import entity.TrendSign;
import entity.Volume;
import entity.Zone;
import systemenum.SystemEnum;

public interface CommonDAO {

	ArrayList<String> getAllActiveScenarioName();

	ArrayList<String> getAllDistinctVolumeStartTimeAndEndTime();
	ArrayList<String> getAllDistinctScenarioStartTimeAndEndTime();
	
	ArrayList<Volume> getAllWorkingVolumeAtTime(Date time);
	
	ArrayList<Scenario> getAllWorkingScenarioAtTime(Date time);
	ArrayList<Area> getAreaListWithoutZoneByScenario(String scenario,Date time);
	ArrayList<String> getOnlyActiveZoneListByScenarioArea(String scenario, String startTime, String area);
	ArrayList<Zone> getRelatedZoneListByScenarioList(ArrayList<Scenario> scenarioList);
	
	ArrayList<Zone> getVolumeZoneList();
	Rectangle getRectByName(String name);
	
	ArrayList<TrendSign> getTrendSignListByDate(Date date, String scenario);
	Enum<SystemEnum.Trend> getLastTrendByScenario(Date date, String scenario);
	
	void insertNewTrendSign(TrendSign sign);
}
