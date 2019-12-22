package dao;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

//import entity.Area;
//import entity.Scenario;
import entity.OrderSign;
import entity.Setting;
//import entity.Volume;
import entity.Zone;
import systemenum.SystemEnum;

public interface CommonDAO {

	ArrayList<String> getAllActiveSettingName();
	ArrayList<String> getAllSettingDistinctStartTimeAndEndTime();
	ArrayList<Setting> getAllWorkingSettingAtTime(Date time);
	ArrayList<Zone> getAllCloseMonitorZone();
	
	void insertNewOrderSign(OrderSign sign);
	void updateOrderInfo(Integer orderId, String setting, String time, double limitPrice, double closePrice, double tickProfit);
	ArrayList<OrderSign> getOrderSignListByDate(Date date, String setting);
	
	void cleanSettingActive();
	void cleanSetting();
	void cleanCloseZone();

	void insertSettingActive(String setting, 
			  int active);
	void insertSetting(String setting, 
		String startTime, 
		String endTime, 
		int orderIndex,
		double limitChange,
		double tick,
		double stopChange);
	void insertCloseZone(String zone, int x, int y);
}
