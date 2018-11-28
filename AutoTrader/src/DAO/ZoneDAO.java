package DAO;

import java.awt.Rectangle;
import java.util.ArrayList;

import entity.Scenario;
import entity.Zone;

public interface ZoneDAO {
	
	ArrayList<Zone> getRelatedZoneListByScenario(ArrayList<Scenario> scenarioList);
	ArrayList<String> getOnlyActiveZoneListByScenarioArea(String scenario, String startTime, String area);
	Rectangle getRectByName(String name);
}
