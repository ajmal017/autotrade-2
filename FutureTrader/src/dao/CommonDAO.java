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
	void deleteOrderSign(Integer orderId);
	void updateOrderSubmittedInfo(Integer orderId, String orderStatus);
	void updateOrderProfitLimitPrice(Integer orderId, double newProfitLimitPrice);
	void updateOrderLimitFilledInfo(Integer orderId, String orderStatus, double limitFilledPrice);
	void updateOrderProfitLimitFilledInfo(Integer orderId, String orderStatus, double profitLimitFilledPrice, double tickProfit);
	ArrayList<OrderSign> getOrderSignListByDate(Date date, String setting);
	ArrayList<OrderSign> getNewestSignListByDate(Date date, String setting, int oldCount);
	
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
		double profitLimitChange);
	void insertCloseZone(String zone, int x, int y, int active);
}
