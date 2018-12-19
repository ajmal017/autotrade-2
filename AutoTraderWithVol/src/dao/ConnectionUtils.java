package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import config.SystemConfig;

public class ConnectionUtils {

	
	private static ThreadLocal<Connection> tl = new ThreadLocal<Connection>();
	
    private static String driver = "org.sqlite.JDBC";
    
    private static String url = "jdbc:sqlite://" + SystemConfig.DOC_PATH + "//" + SystemConfig.DB_NAME;

    static {

    	try {
    		Class.forName(driver);
    	} catch (ClassNotFoundException e) {
    		e.printStackTrace();
        }
    	
    }
    
    public static Connection getConnection() throws SQLException {
        Connection con = tl.get();
        if (con == null) {
            con = DriverManager.getConnection(url);
            tl.set(con);
        }
        return con;
    }

    public static void closeConnection() {
        Connection con = tl.get();
        try {
            if (con != null) {
                con.close();
                tl.set(null);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void closeStatement(Statement stmt) {
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void closeResultSet(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void closeAll(Statement stmt, ResultSet rs){
        closeResultSet(rs);
        closeStatement(stmt);
        closeConnection();
    }
    

    public static void main(String[] args) throws Exception{
        System.out.println(ConnectionUtils.getConnection());
    }
}
