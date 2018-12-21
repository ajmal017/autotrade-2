package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import dao.ConnectionUtils;
import entity.Area;
import entity.Scenario;
import entity.Zone;
import tool.Util;

public class CommonDAOSQL implements CommonDAO {

	@Override
	public ArrayList<String>getAllActiveScenarioName()  {
		
		final String sqlString = " select scenario from scenario_active where active = ?";
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
	public ArrayList<String> getAllDistinctScenarioStartTimeAndEndTime() {
		final String sqlString = "SELECT start_time FROM scenario UNION SELECT end_time FROM scenario";
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
	public ArrayList<String> getAllDistinctVolumeStartTimeAndEndTime() {
		final String sqlString = "SELECT start_time FROM volume UNION SELECT end_time FROM volume";
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
	public ArrayList<Scenario> getAllWorkingScenarioAtTime(Date time) {
		
		final String sqlString = "select distinct(scenario) from scenario where start_time <= ? and end_time > ?";
		ArrayList<Scenario> list = new ArrayList<>();

		Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
		try {
			
			conn = ConnectionUtils.getConnection();
			stmt = conn.prepareStatement(sqlString);
	    	String timeStr = tool.Util.getDateStringByDateAndFormatter(time, "HH:mm");
	    	
			stmt.setString(1, timeStr);
			stmt.setString(2, timeStr);
			rs = stmt.executeQuery();
	        while(rs.next()){
	        	
	        	Scenario scenario = new Scenario();
	        	scenario.setScenario(rs.getString(1));
	        	list.add(scenario);
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
	public ArrayList<Area> getAreaListWithoutZoneByScenario(String scenario,Date time) {
		final String sqlString = "select start_time,end_time,area,percent from scenario where scenario = ? and start_time <= ? and end_time > ?";
		ArrayList<Area> list = new ArrayList<>();

		Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
		try {
			
			conn = ConnectionUtils.getConnection();
			stmt = conn.prepareStatement(sqlString);
			String timeStr = tool.Util.getDateStringByDateAndFormatter(time, "HH:mm");
			stmt.setString(1, scenario);
			stmt.setString(2, timeStr);
			stmt.setString(3, timeStr);
			rs = stmt.executeQuery();
	        while(rs.next()){
	        	
	        	Area area = new Area();
	        	area.setStartTime(rs.getString(1));
	        	area.setEndTime(rs.getString(2));
	        	area.setArea(rs.getString(3));
	        	area.setPercent(rs.getInt(4));
	        	area.setScenario(scenario);
	        	list.add(area);
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
	public ArrayList<String> getOnlyActiveZoneListByScenarioArea(String scenario, String startTime, String area) {
		final String sqlString = "select zone from area_zone where scenario = ? and start_time = ? and area = ? and active = 1";
		ArrayList<String> list = new ArrayList<>();

		Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
		try {
			
			conn = ConnectionUtils.getConnection();
			stmt = conn.prepareStatement(sqlString);
			stmt.setString(1, scenario);
			stmt.setString(2, startTime);
			stmt.setString(3, area);
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
	public ArrayList<Zone> getRelatedZoneListByScenarioList(ArrayList<Scenario> scenarioList) {
		
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append("SELECT distinct(zone) FROM area_zone where ");
		for (int i = 0; i < scenarioList.size(); i++) {
			
			Scenario s = scenarioList.get(i);
			
			for (int j = 0; j < s.getAreaList().size(); j++) {
				
				Area a = s.getAreaList().get(j);
				
				sBuilder.append("(scenario = '" + a.getScenario() + "' and start_time = '" + a.getStartTime() + "' and area = '" + a.getArea() + "' and active = 1)");
				if(j + 1 < s.getAreaList().size()) {
					sBuilder.append(" or ");
				}
			}
			if(i + 1 < scenarioList.size()) {
				sBuilder.append(" or ");
			}
		}
		
		ArrayList<Zone> list = new ArrayList<>();
		Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
		try {
			
			conn = ConnectionUtils.getConnection();
			stmt = conn.prepareStatement(sBuilder.toString());
			rs = stmt.executeQuery();
	        while(rs.next()){
	        	
	        	Zone zone =  new Zone();
	        	zone.setZone(rs.getString(1));
	        	Util.setZoneXYByZone(zone);
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
	public ArrayList<Zone> getVolumeZoneList() {

		final String sqlString = "select zone from volume_zone";

		ArrayList<Zone> list = new ArrayList<>();
		Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
		try {
			
			conn = ConnectionUtils.getConnection();
			stmt = conn.prepareStatement(sqlString);
			rs = stmt.executeQuery();
	        while(rs.next()){
	        	
	        	Zone zone =  new Zone();
	        	zone.setZone(rs.getString(1));
	        	Util.setZoneXYByZone(zone);
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
}
