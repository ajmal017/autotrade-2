package entity;

import java.util.Date;

import systemenum.SystemEnum;
import tool.Util;

public class OrderSign {

	private Integer orderIdInIB;
	private String orderState; //todo update in db
	private Date time;
	private String setting;
	private Enum<SystemEnum.OrderAction> orderAction;
	private String actionText;

	private double limitPrice; //限价（开单）
	private double tick;
	private double profitLimitPrice;  //止盈（关单）
	private double tickProfit; // tick * price difference(limitPrice,stopPrice)
	
	public OrderSign(
			Integer orderIdInIB, 
			String orderState,
			Date time, 
			String setting, 
			Enum<SystemEnum.OrderAction> orderAction, 
			double limitPrice,  
			double tick,
			double profitLimitPrice,
			double tickProfit) {
		setOrderIdInIB(orderIdInIB);
		setOrderState(orderState);
        setTime(time);
        setSetting(setting);
        setOrderAction(orderAction);
        setActionText(Util.getActionTextByEnum(orderAction));
        setLimitPrice(limitPrice);
        setTick(tick);
        setProfitLimitPrice(profitLimitPrice);
        setTickProfit(tickProfit);
    }
	
	public OrderSign() {
		
//		super();
		orderAction = SystemEnum.OrderAction.Default;
	}
	
	public Integer getOrderIdInIB() {
		return orderIdInIB;
	}

	public void setOrderIdInIB(Integer orderIdInIB) {
		this.orderIdInIB = orderIdInIB;
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
	
	public double getProfitLimitPrice() {
		return profitLimitPrice;
	}

	public void setProfitLimitPrice(double profitLimitPrice) {
		this.profitLimitPrice = profitLimitPrice;
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

	public String getOrderState() {
		return orderState;
	}

	public void setOrderState(String orderState) {
		this.orderState = orderState;
	}
	
	
}
