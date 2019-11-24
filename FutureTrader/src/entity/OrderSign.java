package entity;

import java.util.Date;

import systemenum.SystemEnum;
import tool.Util;

public class OrderSign {

	private Date time;
	private String setting;
	private Enum<SystemEnum.OrderAction> orderAction;
	private String actionText;

	private double limitPrice; //限价（开单）
	private double tick;
	private double stopPrice;  //止盈（关单）
	private double tickProfit; // tick * price difference(limitPrice,stopPrice)
	
	public OrderSign(Date time, 
			String setting, 
			Enum<SystemEnum.OrderAction> orderAction, 
			double limitPrice,  
			double tick,
			double stopPrice,
			double tickProfit) {
        setTime(time);
        setSetting(setting);
        setOrderAction(orderAction);
        setActionText(Util.getActionTextByEnum(orderAction));
        setLimitPrice(limitPrice);
        setTick(tick);
        setStopPrice(stopPrice);
        setTickProfit(tickProfit);
    }
	
	public OrderSign() {
		
//		super();
		orderAction = SystemEnum.OrderAction.Default;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}

	public String getSetting() {
		return setting;
	}

	public void setSetting(String setting) {
		this.setting = setting;
	}

	public Enum<SystemEnum.OrderAction> getOrderAction() {
		return orderAction;
	}

	public void setOrderAction(Enum<SystemEnum.OrderAction> orderAction) {
		this.orderAction = orderAction;
	}

	public String getActionText() {
		return actionText;
	}

	public void setActionText(String actionText) {
		this.actionText = actionText;
	}

	public double getLimitPrice() {
		return limitPrice;
	}

	public void setLimitPrice(double limitPrice) {
		this.limitPrice = limitPrice;
	}
	
	public double getStopPrice() {
		return stopPrice;
	}

	public void setStopPrice(double stopPrice) {
		this.stopPrice = stopPrice;
	}

	public double getTick() {
		return tick;
	}

	public void setTick(double tick) {
		this.tick = tick;
	}

	public double getTickProfit() {
		return tickProfit;
	}

	public void setTickProfit(double tickProfit) {
		this.tickProfit = tickProfit;
	}
	
	
}
