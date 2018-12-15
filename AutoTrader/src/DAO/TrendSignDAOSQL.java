package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import entity.TrendSign;
import systemenum.SystemEnum;
import systemenum.SystemEnum.Trend;
import tool.Util;

public class TrendSignDAOSQL implements TrendSignDAO {

	@Override 
	public void insertNewTrendSign(TrendSign sign) {
		final String sqlString = "insert into trend_sign (date,time,scenario,trend,green,red,price_swim,price_ib,desc) values (?,?,?,?,?,?,?,?,?)";

		Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
		try {
			
			conn = ConnectionUtils.getConnection();
			stmt = conn.prepareStatement(sqlString);
			stmt.setString(1, Util.getDateStringByDateAndFormatter(sign.getTime(),"yyyy/MM/dd"));
			stmt.setString(2, Util.getDateStringByDateAndFormatter(sign.getTime(),"HH:mm:ss"));
			stmt.setString(3, sign.getScenario());
			stmt.setString(4, sign.getTrendText());
			stmt.setInt(5, sign.getGreenCount());
			stmt.setInt(6, sign.getRedCount());
			stmt.setDouble(7, sign.getPriceSwim());
			stmt.setDouble(8, sign.getPriceIB());
			stmt.setString(9, sign.getDesc());
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
	public ArrayList<TrendSign> getTrendSignListByDate(Date date, String scenario) {
		final String sqlString = "select * from trend_sign where date = ? and scenario = ?";
		ArrayList<TrendSign> list = new ArrayList<>();

		Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
		try {
			
			conn = ConnectionUtils.getConnection();
			stmt = conn.prepareStatement(sqlString);
			stmt.setString(1, Util.getDateStringByDateAndFormatter(date, "yyyy/MM/dd"));
			stmt.setString(2, scenario);
			rs = stmt.executeQuery();
	        while(rs.next()){

	        	StringBuilder dateStr = new StringBuilder(rs.getString(1));
	        	dateStr.append(" ");
	        	dateStr.append(rs.getString(2));
	        	TrendSign sign = new TrendSign();
	        	sign.setTime(Util.getDateByStringAndFormatter(dateStr.toString(), "yyyy/MM/dd HH:mm:ss"));
	        	sign.setScenario(rs.getString(3));
	        	sign.setTrendText(rs.getString(4));
	        	sign.setTrend(Util.getTrendEnumByText(sign.getTrendText()));
	        	sign.setGreenCount(rs.getInt(5));
	        	sign.setRedCount(rs.getInt(6));
	        	sign.setPriceSwim(rs.getDouble(7));
	        	sign.setPriceIB(rs.getDouble(8));
	        	sign.setDesc(rs.getString(9));
	        	
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
	public Enum<Trend> getLastTrendByScenario(Date date, String scenario) {

		final String sqlString = "select trend from trend_sign where date = ? and scenario = ? order by time desc";
		
		Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
		try {
			
			conn = ConnectionUtils.getConnection();
			stmt = conn.prepareStatement(sqlString);
			stmt.setString(1, Util.getDateStringByDateAndFormatter(date, "yyyy/MM/dd"));
			stmt.setString(2, scenario);
			rs = stmt.executeQuery();
	        if(rs.next()){

	        	String trendTextString = rs.getString(1);
	        	return Util.getTrendEnumByText(trendTextString);
	        }
			
		} catch (SQLException e) {
			e.printStackTrace(); 
		} finally {
	        ConnectionUtils.closeAll(stmt, rs);
		}
		
		return SystemEnum.Trend.Default;
	}

}
