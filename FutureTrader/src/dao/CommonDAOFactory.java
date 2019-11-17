package dao;

public class CommonDAOFactory {

	public static CommonDAO getCommonDAO () {
		return new CommonDAOSQL();
	}
}
