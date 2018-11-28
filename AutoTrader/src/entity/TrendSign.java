package entity;

import java.util.Date;

import javafx.beans.property.SimpleStringProperty;
import systemenum.SystemEnum;
import tool.Util;

public class TrendSign {

	private Date time;
	private String scenario;
	private Enum<SystemEnum.Trend> trend; 
	private String trendText; //只用于 dao存储 和 xls导出 用
	private int greenCount;
	private int redCount;
	
	private double priceSwim; //swim price
	private double priceIB;
	private String desc; //备注，area zone等信息（later）
	
	//只用于xls导出用
	private double profitSwim;
	private double profitIB;
	
	public TrendSign(Date time, 
			String scenario, 
			Enum<SystemEnum.Trend> trend, 
			int green, 
			int red, 
			double priceSwim, 
			double priceIB,
			String desc,
			double profitSwim,
			double profitIB) {
        setTime(time);
        setScenario(scenario);
        setTrend(trend);
        setTrendText(Util.getTrendTextByEnum(trend));
        setGreenCount(green);
        setRedCount(red);
        setPriceSwim(priceSwim);
        setPriceIB(priceIB);
        setDesc(desc);
        setProfitSwim(priceSwim);
        setProfitIB(priceIB);
    }
	
	public TrendSign() {
		
//		super();
		
	}
	
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
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
	
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getTrendText() {
		return trendText;
	}

	public void setTrendText(String trendText) {
		this.trendText = trendText;
	}

	public double getPriceSwim() {
		return priceSwim;
	}

	public void setPriceSwim(double priceSwim) {
		this.priceSwim = priceSwim;
	}

	public double getPriceIB() {
		return priceIB;
	}

	public void setPriceIB(double priceIB) {
		this.priceIB = priceIB;
	}

	public double getProfitSwim() {
		return profitSwim;
	}

	public void setProfitSwim(double profitSwim) {
		this.profitSwim = profitSwim;
	}

	public double getProfitIB() {
		return profitIB;
	}

	public void setProfitIB(double profitIB) {
		this.profitIB = profitIB;
	}

	public int getGreenCount() {
		return greenCount;
	}

	public void setGreenCount(int greenCount) {
		this.greenCount = greenCount;
	}

	public int getRedCount() {
		return redCount;
	}

	public void setRedCount(int redCount) {
		this.redCount = redCount;
	}
	
}
