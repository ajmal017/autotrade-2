package entity;

import java.util.ArrayList;
import entity.Area;
import systemenum.SystemEnum;

public class Scenario {
	
	//base info
	private String scenario;
//	private String startTime;
//	private String endTime;
	
	//sign and matrix
	private Enum<SystemEnum.Trend> trend;
	private ArrayList<Area> areaList;
	
	public Scenario () {
		
//		super();
		this.setAreaList(new ArrayList<Area>());
	}
	
	public Scenario (String scenario, String starTime, String endTime) {
		
//		super();
		this.setAreaList(new ArrayList<Area>());
		
		this.scenario = scenario;
//		this.startTime = starTime;
//		this.endTime = endTime;
	}
	
	public String getScenario() {
		return scenario;
	}
	
	public void setScenario(String scenario) {
		this.scenario = scenario;
	}	

	public ArrayList<Area> getAreaList() {
		return areaList;
	}

	public void setAreaList(ArrayList<Area> areaList) {
		this.areaList = areaList;
	}

	public Enum<SystemEnum.Trend> getTrend() {
		return trend;
	}

	public void setTrend(Enum<SystemEnum.Trend> trend) {
		this.trend = trend;
	}


}
