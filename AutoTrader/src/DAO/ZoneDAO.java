package DAO;

import java.awt.Rectangle;
import java.util.ArrayList;

import entity.Area;
import entity.Scenario;
import entity.Zone;

public interface ZoneDAO {
	
	ArrayList<Zone> getRelatedZoneListByScenarioList(ArrayList<Scenario> scenarioList);
	ArrayList<String> getOnlyActiveZoneListByScenarioArea(String scenario, String startTime, String area);
	Rectangle getRectByName(String name);
	
	void cleanAreaZone();
	void cleanXYCoords();
	void cleanMyFrame();
	
	void insertAreaZone(String scenario, 
						String starttime, 
						String area,
						String zone,
						int active);
	void insertXYCoords(String zone, int x, int y);
	void insertMyFrame(String name, int x, int y, int width, int height);
}
