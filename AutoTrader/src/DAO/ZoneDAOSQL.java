package DAO;

import java.awt.Rectangle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import entity.Scenario;
import entity.Zone;

public class ZoneDAOSQL implements ZoneDAO {

	@Override

	public ArrayList<Zone> getRelatedZoneListByScenario(ArrayList<Scenario> scenarioList) {
		
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append("select * from xy_coords where zone in (SELECT distinct(zone) FROM area_zone where ");
		for (int i = 0; i < scenarioList.size(); i++) {
			Scenario s = scenarioList.get(i);
			sBuilder.append("(scenario = '" + s.getScenario() +  "' and start_time = '" + s.getStartTime() + "' and active = 1)");
			if(i + 1 < scenarioList.size()) {
				sBuilder.append(" or ");
			}
		}
		sBuilder.append(")");
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
	public Rectangle getRectByName(String name) {
		
		final String sqlString = "select origin_x,origin_y,width,height from my_frame where name = ?";
		Rectangle rect = new Rectangle();

		Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
		try {
			
			conn = ConnectionUtils.getConnection();
			stmt = conn.prepareStatement(sqlString);
			stmt.setString(1, name);
			rs = stmt.executeQuery();
	        if(rs.next()){
	        	
	        	rect.setBounds(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4));
	        }
			return rect;
			
		} catch (SQLException e) {
			e.printStackTrace(); 
		} finally {
	        ConnectionUtils.closeAll(stmt, rs);
		}
		return rect;
	}

	@Override
	public void cleanAreaZone() {
		final String sqlString = "delete from area_zone";
		
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
	public void cleanXYCoords() {
		final String sqlString = "delete from xy_coords";
		
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
	public void cleanMyFrame() {
		final String sqlString = "delete from my_frame";
		
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
	public void insertAreaZone(String scenario, String starttime, String area, String zone, int active) {
		final String sqlString = "insert into area_zone values (?,?,?,?,?)";
		
		Connection conn = null;
        PreparedStatement stmt = null;
		try {
			
			conn = ConnectionUtils.getConnection();
			stmt = conn.prepareStatement(sqlString);
			stmt.setString(1,scenario);
			stmt.setString(2,starttime);
			stmt.setString(3,area);
			stmt.setString(4,zone);
			stmt.setInt(5,active);
			stmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace(); 
		} finally {
	        ConnectionUtils.closeAll(stmt,null);
		}
	}

	@Override
	public void insertXYCoords(String zone, int x, int y) {
		final String sqlString = "insert into xy_coords values (?,?,?)";
		
		Connection conn = null;
        PreparedStatement stmt = null;
		try {
			
			conn = ConnectionUtils.getConnection();
			stmt = conn.prepareStatement(sqlString);
			stmt.setString(1,zone);
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
	public void insertMyFrame(String name, int x, int y, int width, int height) {
		final String sqlString = "insert into my_frame values (?,?,?,?,?)";
		
		Connection conn = null;
        PreparedStatement stmt = null;
		try {
			
			conn = ConnectionUtils.getConnection();
			stmt = conn.prepareStatement(sqlString);
			stmt.setString(1,name);
			stmt.setInt(2,x);
			stmt.setInt(3,y);
			stmt.setInt(4,width);
			stmt.setInt(5,height);
			stmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace(); 
		} finally {
	        ConnectionUtils.closeAll(stmt,null);
		}
	}

	

}
