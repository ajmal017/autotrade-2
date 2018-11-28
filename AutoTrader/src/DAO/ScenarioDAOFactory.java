package DAO;

public class ScenarioDAOFactory {

	public static ScenarioDAO getScenarioDAO () {
		return new ScenarioDAOSQL();
	}
}
