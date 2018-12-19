package entity;

import java.util.ArrayList;

public class Area {
	
	private String area;
	private String scenario;
	private String startTime;
	private String endTime;
	private int percent;
	private ArrayList<String> zoneList;
	
	public Area() {
		
//		super();
		this.setZoneList(new ArrayList<String>());
	}
	
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	public ArrayList<String> getZoneList() {
		return zoneList;
	}
	public void setZoneList(ArrayList<String> zoneList) {
		this.zoneList = zoneList;
	}
	public int getPercent() {
		return percent;
	}
	public void setPercent(int percent) {
		this.percent = percent;
	}

	public String getScenario() {
		return scenario;
	}

	public void setScenario(String scenario) {
		this.scenario = scenario;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	
}
