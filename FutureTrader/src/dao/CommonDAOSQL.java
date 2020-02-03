package dao;

import java.awt.Rectangle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import dao.ConnectionUtils;
//import entity.Area;
//import entity.Scenario;
import entity.OrderSign;
import entity.Setting;
import entity.SingleOrderSetting;
//import entity.Volume;
import entity.Zone;
import systemenum.SystemEnum;
import systemenum.SystemEnum.OrderAction;
import tool.Util;

public class CommonDAOSQL implements CommonDAO {


	@Override
	public ArrayList<OrderSign> getOrderSignListByDate(Date date, String setting) {
		final String sqlString = "select * from order_sign where date = ? and setting = ?";
		ArrayList<OrderSign> list = new ArrayList<>();

		Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
		try {
			
			conn = ConnectionUtils.getConnection();
			stmt = conn.prepareStatement(sqlString);
			stmt.setString(1, Util.getDateStringByDateAndFormatter(date, "yyyy/MM/dd"));
			stmt.setString(2, setting);
			rs = stmt.executeQuery();
	        while(rs.next()){

	        	StringBuilder dateStr = new StringBuilder(rs.getString(1));
	        	dateStr.append(" ");
	        	dateStr.append(rs.getString(2));
	        	OrderSign sign = new OrderSign();
	        	sign.setTime(Util.getDateByStringAndFormatter(dateStr.toString(), "yyyy/MM/dd HH:mm:ss"));
	        	sign.setParentOrderIdInIB(rs.getInt(3));
	        	sign.setSetting(rs.getString(4));
	        	sign.setActionText(rs.getString(5));
	        	sign.setOrderAction(Util.getOrderActionEnumByText(sign.getActionText()));
	        	sign.setLimitPrice(rs.getDouble(6));
	        	sign.setTick(rs.getDouble(7));
	        	sign.setProfitLimitPrice(rs.getDouble(8));
	        	sign.setTickProfit(rs.getDouble(9));
	        	
	        	list.add(sign);
	        }
			return list;
			
		} catch (SQLException e) {
			e.printStackTrace(); 
		} finally {
	        ConnectionUtils.closeAll(stmt, rs);
		}
		return list;
	}

	@Override
	public void insertNewOrderSign(OrderSign sign) {
		final String sqlString = "insert into order_sign (date,time,orderidinib,setting,action,limit_price,tick,profit_limit_price,tick_profit) values (?,?,?,?,?,?,?,?,?)";

		Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
		try {
			
			conn = ConnectionUtils.getConnection();
			stmt = conn.prepareStatement(sqlString);
			stmt.setString(1, Util.getDateStringByDateAndFormatter(sign.getTime(),"yyyy/MM/dd"));
			stmt.setString(2, Util.getDateStringByDateAndFormatter(sign.getTime(),"HH:mm:ss"));
			stmt.setInt(3, sign.getParentOrderIdInIB());
			stmt.setString(4, sign.getSetting());
			stmt.setString(5, sign.getActionText());
			stmt.setDouble(6, sign.getLimitPrice());
			stmt.setDouble(7, sign.getTick());
			stmt.setDouble(8, sign.getProfitLimitPrice());
			stmt.setDouble(9, sign.getTickProfit());
			int i = stmt.executeUpdate();
	        if (i == 0) {
				//False
	        	System.out.print("insertNewTrendSign fail");
			}
			
		} catch (SQLException e) {
			e.printStackTrace(); 
		} finally {
	        ConnectionUtils.closeAll(stmt, rs);
		}
	}
	
	@Override
	public void updateOrderProfitLimitPrice(Integer orderId, double newProfitLimitPrice) {
		final String sqlString = "update order_sign set profit_limit_price = ? where orderidinib = ?";
		
		Connection conn = null;
        PreparedStatement stmt = null;
		try {
			
			conn = ConnectionUtils.getConnection();
			stmt = conn.prepareStatement(sqlString);
			stmt.setDouble(1,newProfitLimitPrice);
			stmt.setInt(2, orderId.intValue());
			stmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace(); 
		} finally {
	        ConnectionUtils.closeAll(stmt,null);
		}
		
	}
	
	@Override
	public void updateOrderLimitFilledInfo(Integer orderId, String orderState, double limitFilledPrice) {
		final String sqlString = "update order_sign set limit_price = ? where orderidinib = ?";
		
		Connection conn = null;
        PreparedStatement stmt = null;
		try {
			
			conn = ConnectionUtils.getConnection();
			stmt = conn.prepareStatement(sqlString);
			stmt.setDouble(1,limitFilledPrice);
			stmt.setInt(2, orderId.intValue());
			stmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace(); 
		} finally {
	        ConnectionUtils.closeAll(stmt,null);
		}
		
	}
	
	@Override
	public void updateOrderProfitLimitFilledInfo(Integer orderId, String orderState, double profitLimitFilledPrice, double tickProfit) {
		final String sqlString = "update order_sign set profit_limit_price = ?, tick_profit = ? where orderidinib = ?";
		
		Connection conn = null;
        PreparedStatement stmt = null;
		try {
			
			conn = ConnectionUtils.getConnection();
			stmt = conn.prepareStatement(sqlString);
			stmt.setDouble(1,profitLimitFilledPrice);
			stmt.setDouble(2,tickProfit);
			stmt.setInt(3, orderId.intValue());
			stmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace(); 
		} finally {
	        ConnectionUtils.closeAll(stmt,null);
		}
		
	}
/*
	@Override
	public void updateOrderInfo(Integer orderId, String setting, Date time, double limitPrice, double stopPrice, double tickProfit) {
		final String sqlString = "update order_sign set limit_price = ?, stop_price = ?, tick_profit = ? where orderidinib = ? and setting = ? and time = ? and date = ?";
		
		Connection conn = null;
        PreparedStatement stmt = null;
		try {
			
			conn = ConnectionUtils.getConnection();
			stmt = conn.prepareStatement(sqlString);
			stmt.setDouble(1,limitPrice);
			stmt.setDouble(2,stopPrice);
			stmt.setDouble(3,tickProfit);
			stmt.setInt(4, orderId.intValue());
			stmt.setString(5,setting);
			stmt.setString(6,Util.getDateStringByDateAndFormatter(time, "HH:mm:ss"));
			stmt.setString(7,Util.getDateStringByDateAndFormatter(time, "yyyy/MM/dd"));
			stmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace(); 
		} finally {
	        ConnectionUtils.closeAll(stmt,null);
		}
		
	}
	*/
	@Override
	public ArrayList<String>getAllActiveSettingName()  {
		
		final String sqlString = "select setting from setting_active where active = ?";
		ArrayList<String> list = new ArrayList<>();

		Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
		try {
			
			conn = ConnectionUtils.getConnection();
			stmt = conn.prepareStatement(sqlString);
			stmt.setInt(1, 1);
			rs = stmt.executeQuery();
	        while(rs.next()){
	        	list.add(rs.getString(1));
	        }
			return list;
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
	        ConnectionUtils.closeAll(stmt, rs);
		}
		return list;
	}
	
	@Override
	public ArrayList<String> getAllSettingDistinctStartTimeAndEndTime() {
		
		final String sqlString = "SELECT start_time FROM setting UNION SELECT end_time FROM setting";
		ArrayList<String> list = new ArrayList<>();

		Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
		try {
			
			conn = ConnectionUtils.getConnection();
			stmt = conn.prepareStatement(sqlString);
			rs = stmt.executeQuery();
	        while(rs.next()){
	        	list.add(rs.getString(1));
	        }
			return list;
			
		} catch (SQLException e) {
			e.printStackTrace(); 
		} finally {
	        ConnectionUtils.closeAll(stmt, rs);
		}
		return list;
	}
	
	@Override
	public ArrayList<Setting> getAllWorkingSettingAtTime(Date time) {
		
		final String sqlString = "select (setting,limit_change,tick,profit_limit_change) from setting where start_time <= ? and end_time > ?";
		ArrayList<Setting> list = new ArrayList<>();

		Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
		try {
			
			conn = ConnectionUtils.getConnection();
			stmt = conn.prepareStatement(sqlString);
	    	String timeStr = tool.Util.getDateStringByDateAndFormatter(time, "HH:mm:ss");
	    	
			stmt.setString(1, timeStr);
			stmt.setString(2, timeStr);
			rs = stmt.executeQuery();
	        while(rs.next()){
	        	
	        	String settingName = rs.getString(1);
	        	if (list.size() == 0 || !list.get(list.size()-1).getSetting().equals(settingName)) {
	        		Setting setting = new Setting();
		        	setting.setSetting(rs.getString(1));
		        	list.add(setting);
				}
	        	
	        	SingleOrderSetting sos = new SingleOrderSetting();
	        	sos.setLimitChange(rs.getDouble(2));
	        	sos.setTick(rs.getDouble(3));
	        	sos.setProfitLimitChange(rs.getDouble(4));
	        }
			return list;
			
		} catch (SQLException e) {
			e.printStackTrace(); 
		} finally {
	        ConnectionUtils.closeAll(stmt, rs);
		}
		return list;
	}
	
	@Override
	public ArrayList<Zone> getAllCloseMonitorZone() {
		
		final String sqlString = "SELECT * FROM close_zone where active = 1";
		ArrayList<Zone> list = new ArrayList<>();

		Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
		try {
			
			conn = ConnectionUtils.getConnection();
			stmt = conn.prepareStatement(sqlString);
			rs = stmt.executeQuery();
	        while(rs.next()){
	        	Zone zone = new Zone();
	        	zone.setZone(rs.getString(1));
	        	zone.setxCoord(rs.getInt(2));
	        	zone.setyCoord(rs.getInt(3));
	        	list.add(zone);
	        }
			return list;
			
		} catch (SQLException e) {
			e.printStackTrace(); 
		} finally {
	        ConnectionUtils.closeAll(stmt, rs);
		}
		return list;
	}
	
	@Override
	public void insertSettingActive(String seting, int active) {
		final String sqlString = "insert into setting_active values (?,?)";
		
		Connection conn = null;
        PreparedStatement stmt = null;
		try {
			
			conn = ConnectionUtils.getConnection();
			stmt = conn.prepareStatement(sqlString);
			stmt.setString(1,seting);
			stmt.setInt(2, active);
			stmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace(); 
		} finally {
	        ConnectionUtils.closeAll(stmt,null);
		}
		
	}

	@Override
	public void insertSetting(String setting, 
			String startTime, 
			String endTime, 
			int orderIndex,
			double limitChange,
			double tick,
			double stopChange) {
		final String sqlString = "insert into setting values (?,?,?,?,?,?,?)";
		
		Connection conn = null;
        PreparedStatement stmt = null;
		try {
			
			conn = ConnectionUtils.getConnection();
			stmt = conn.prepareStatement(sqlString);
			stmt.setString(1, setting);
			stmt.setString(2,startTime);
			stmt.setString(3,endTime);
			stmt.setInt(4,orderIndex);
			stmt.setDouble(5, limitChange);
			stmt.setDouble(6, tick);
			stmt.setDouble(7, stopChange);
			stmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace(); 
		} finally {
	        ConnectionUtils.closeAll(stmt,null);
		}
	}

	@Override
	public void insertCloseZone(String name, int x, int y) {
		final String sqlString = "insert into close_zone values (?,?,?)";
		
		Connection conn = null;
        PreparedStatement stmt = null;
		try {
			
			conn = ConnectionUtils.getConnection();
			stmt = conn.prepareStatement(sqlString);
			stmt.setString(1,name);
			stmt.setInt(2,x);
			stmt.setInt(3,y);
			stmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace(); 
		} finally {
	        ConnectionUtils.closeAll(stmt,null);
		}
	}
	
	@Override
	public void cleanSettingActive() {
		final String sqlString = "delete from setting_active";
		
		Connection conn = null;
        PreparedStatement stmt = null;
		try {
			
			conn = ConnectionUtils.getConnection();
			stmt = conn.prepareStatement(sqlString);
			stmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace(); 
		} finally {
	        ConnectionUtils.closeAll(stmt,null);
		}
	}

	@Override
	public void cleanSetting() {
		final String sqlString = "delete from setting";
		
		Connection conn = null;
        PreparedStatement stmt = null;
		try {
			
			conn = ConnectionUtils.getConnection();
			stmt = conn.prepareStatement(sqlString);
			stmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace(); 
		} finally {
	        ConnectionUtils.closeAll(stmt,null);
		}	
	}
	
	@Override
	public void cleanCloseZone() {
		final String sqlString = "delete from close_zone";
		
		Connection conn = null;
        PreparedStatement stmt = null;
		try {
			
			conn = ConnectionUtils.getConnection();
			stmt = conn.prepareStatement(sqlString);
			stmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace(); 
		} finally {
	        ConnectionUtils.closeAll(stmt,null);
		}
	}
	
	
}
