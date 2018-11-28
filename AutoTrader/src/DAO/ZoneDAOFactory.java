package DAO;

public class ZoneDAOFactory {
	
	public static ZoneDAO getZoneDAO () {
		return new ZoneDAOSQL();
	}
}
