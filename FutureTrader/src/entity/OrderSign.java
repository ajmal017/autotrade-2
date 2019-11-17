package entity;

import java.util.Date;

import systemenum.SystemEnum;
import tool.Util;

public class OrderSign {

	private Date time;
	private String setting;
	private Enum<SystemEnum.OrderAction> orderAction;
	private String actionText;
	
	private double priceSwim;
	private double priceIB;
	private int tick;
	private double 
	private String desc;
	
	//xls
	private double tickProfit; // tick * price difference
	
	public OrderSign(Date time, 
			String scenario, 
			Enum<SystemEnum.Trend> trend, 
			int green, 
			int red, 
			int white,
			double priceSwim, 
			double priceIB,
			int quantity,
			String desc,
			double profitSwim,
			double profitIB) {
        setTime(time);
        setScenario(scenario);
        setTrend(trend);
        setTrendText(Util.getTrendTextByEnum(trend));
        setGreenCount(green);
        setRedCount(red);
        setWhiteCount(white);
        setPriceSwim(priceSwim);
        setPriceIB(priceIB);
        setQuantity(quantity);
        setDesc(desc);
        setProfitSwim(priceSwim);
        setProfitIB(priceIB);
    }
	
	public OrderSign() {
		
//		super();
		trend = SystemEnum.Trend.Default;
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

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public int getWhiteCount() {
		return whiteCount;
	}

	public void setWhiteCount(int whiteCount) {
		this.whiteCount = whiteCount;
	}
	
}
