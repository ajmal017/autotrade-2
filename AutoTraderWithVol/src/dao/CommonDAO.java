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
	
	
	void cleanScenarioActiveData();
	void cleanScenarioData();
	void cleanAreaZone();
	void cleanMyFrame();
	void cleanVolumeZone();
	void cleanVolume();
	
	void insertScenarioActive(String scenario, 
			  int active);
	void insertScenario(String scenario, 
		String starttime, 
		String endtime, 
		String area, 
		int percent);
	void insertAreaZone(String scenario, 
			String starttime, 
			String area,
			String zone,
			int active);
	void insertMyFrame(String name, int x, int y, int width, int height);
	void insertVolumeZone(String zone);
	void insertVolume(String scenario, 
			String starttime, 
			String endtime, 
			int column, 
			int percent, 
			String rows);
}
