package DAO;

public class TrendSignDAOFactory {

	public static TrendSignDAO getTrendSignDAO () {
		return new TrendSignDAOSQL();
	}
}
