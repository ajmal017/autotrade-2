package entity;

import java.util.ArrayList;

import systemenum.SystemEnum;

public class Volume {

	private String scenario;
	
	private int column;
	private int percent;
	private int whiteMax;
	private ArrayList<String> rows;
	
	private Enum<SystemEnum.Trend> trend;

	private int green;
	private int red;
	private int white;
	
	public Volume() {
		
		this.setTrend(SystemEnum.Trend.Default);
		this.setRows(new ArrayList<String>());
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

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	public int getPercent() {
		return percent;
	}

	public void setPercent(int percent) {
		this.percent = percent;
	}

	public ArrayList<String> getRows() {
		return rows;
	}

	public void setRows(ArrayList<String> rows) {
		this.rows = rows;
	}

	public int getGreen() {
		return green;
	}

	public void setGreen(int green) {
		this.green = green;
	}

	public int getRed() {
		return red;
	}

	public void setRed(int red) {
		this.red = red;
	}

	public int getWhiteMax() {
		return whiteMax;
	}

	public void setWhiteMax(int whiteMax) {
		this.whiteMax = whiteMax;
	}

	public int getWhite() {
		return white;
	}

	public void setWhite(int white) {
		this.white = white;
	}
}
