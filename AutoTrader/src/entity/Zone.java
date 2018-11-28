package entity;


import systemenum.SystemEnum;

public class Zone {
	
	private String zone;
	private int xCoord;
	private int yCoord;
	private Enum<SystemEnum.Color> color;
	
	public Zone() {
//		super();
	}

	public String getZone() {
		return zone;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}

	public int getxCoord() {
		return xCoord;
	}

	public void setxCoord(int xCoord) {
		this.xCoord = xCoord;
	}

	public int getyCoord() {
		return yCoord;
	}

	public void setyCoord(int yCoord) {
		this.yCoord = yCoord;
	}

	public Enum<SystemEnum.Color> getColor() {
		return color;
	}

	public void setColor(Enum<SystemEnum.Color> color) {
		this.color = color;
	}
}
