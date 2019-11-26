package dao;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Date;

//import entity.Area;
//import entity.Scenario;
import entity.OrderSign;
//import entity.Volume;
import entity.Zone;
import systemenum.SystemEnum;

public interface CommonDAO {

//	ArrayList<String> getAllActiveScenarioName();

//	ArrayList<String> getAllDistinctVolumeStartTimeAndEndTime();
//	ArrayList<String> getAllDistinctScenarioStartTimeAndEndTime();
//	ArrayList<String> getAllDistinctVolumeZoneStartTime();
	
//	ArrayList<Volume> getAllWorkingVolumeAtTime(Date time);
	
//	ArrayList<Scenario> getAllWorkingScenarioAtTime(Date time);
//	ArrayList<Area> getAreaListWithoutZoneByScenario(String scenario,Date time);
//	ArrayList<String> getOnlyActiveZoneListByScenarioArea(String scenario, String startTime, String area);
//	ArrayList<Zone> getRelatedZoneListByScenarioList(ArrayList<Scenario> scenarioList);
	
//	ArrayList<Zone> getVolumeZoneList(String time);
//	Rectangle getRectByName(String name);
	
	ArrayList<OrderSign> getOrderSignListByDate(Date date, String setting);
//	Enum<SystemEnum.OrderAction> getLastActionBySetting(Date date, String setting);
	
	void insertNewOrderSign(OrderSign sign);
	void updateOrderInfo(String setting, String time, double limitPrice, double closePrice, double tickProfit);
	
//	void cleanScenarioActiveData();
//	void cleanScenarioData();
//	void cleanAreaZone();
//	void cleanMyFrame();
//	void cleanVolumeZone();
//	void cleanVolume();
	
//	void insertScenarioActive(String scenario, 
//			  int active);
//	void insertScenario(String scenario, 
//		String starttime, 
//		String endtime, 
//		String area, 
//		int percent,
//		int whiteMin);
//	void insertAreaZone(String scenario, 
//			String starttime, 
//			String area,
//			String zone,
//			int active);
//	void insertMyFrame(String name, int x, int y, int width, int height);
//	void insertVolumeZone(String time, String zone);
//	void insertVolume(String scenario, 
//			String starttime, 
//			String endtime, 
//			int column, 
//			int percent, 
//			int whiteMax,
//			String rows);
}
