package entity;

import java.util.ArrayList;

public class Area {
	
	private String area;
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
	
}
