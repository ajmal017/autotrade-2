package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import entity.Area;
import entity.Scenario;
import systemenum.SystemEnum;

public class ScenarioDAOSQL implements ScenarioDAO {
	
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
	public void cleanScenarioActiveData() {
		final String sqlString = "delete from scenario_active";
		
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
	public void cleanScenarioData() {
		final String sqlString = "delete from scenario";
		
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
	public void insertScenarioActive(String scenario, int active) {
		final String sqlString = "insert into scenario_active values (?,?)";
		
		Connection conn = null;
        PreparedStatement stmt = null;
		try {
			
			conn = ConnectionUtils.getConnection();
			stmt = conn.prepareStatement(sqlString);
			stmt.setString(1,scenario);
			stmt.setInt(2, active);
			stmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace(); 
		} finally {
	        ConnectionUtils.closeAll(stmt,null);
		}
		
	}

	@Override
	public void insertScenario(String scenario, String starttime, String endtime, String area, int percent) {
		final String sqlString = "insert into scenario values (?,?,?,?,?)";
		
		Connection conn = null;
        PreparedStatement stmt = null;
		try {
			
			conn = ConnectionUtils.getConnection();
			stmt = conn.prepareStatement(sqlString);
			stmt.setString(1,scenario);
			stmt.setString(2,starttime);
			stmt.setString(3,endtime);
			stmt.setString(4,area);
			stmt.setInt(5, percent);
			stmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace(); 
		} finally {
	        ConnectionUtils.closeAll(stmt,null);
		}
	}
}
