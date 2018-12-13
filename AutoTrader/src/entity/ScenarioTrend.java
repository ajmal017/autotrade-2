package entity;

import systemenum.SystemEnum;

public class ScenarioTrend {

	private String scenario;
	private Enum<SystemEnum.Trend> trend;
	
	public ScenarioTrend() {
		
//		super();
		trend = SystemEnum.Trend.Default;
	}

	public String getScenario() {
		return scenario;
	}

	public void setScenario(String scenario) {
		this.scenario = scenario;
	}

	public Enum<SystemEnum.Trend> getTrend() {
		return trend;
	}

	public void setTrend(Enum<SystemEnum.Trend> trend) {
		this.trend = trend;
	}
}
