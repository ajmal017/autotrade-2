package entity;

import java.util.Date;

import systemenum.SystemEnum;
import tool.Util;

public class OrderSign {

	private String setting;
	
	private Integer parentOrderIdInIB;
	private Integer profitLimitOrderIdInIB;
	private String orderState;
	private Date time;
	private Enum<SystemEnum.OrderAction> orderAction;
	private String actionText;

	private double limitPrice; //限价（开单）
	private double tick;
	private double profitLimitPrice;  //止盈（关单）
	
	private double limitFilledPrice;
	private double profitLimitFilledPrice;
	private double tickProfit; // tick * price difference(limitPrice,stopPrice)
	
	public OrderSign(
			Integer parentOrderIdInIB, 
			Integer profitLimitOrderIdInIB,
			String orderState,
			Date time, 
			String setting, 
			Enum<SystemEnum.OrderAction> orderAction, 
			double limitPrice,  
			double tick,
			double profitLimitPrice,
			double limitFilledPrice,
			double profitLimitFilledPrice,
			double tickProfit) {
		setParentOrderIdInIB(parentOrderIdInIB);
		setProfitLimitOrderIdInIB(profitLimitOrderIdInIB);
		setOrderState(orderState);
        setTime(time);
        setSetting(setting);
        setOrderAction(orderAction);
        setActionText(Util.getActionTextByEnum(orderAction));
        setLimitPrice(limitPrice);
        setTick(tick);
        setLimitFilledPrice(limitFilledPrice);
        setProfitLimitFilledPrice(profitLimitFilledPrice);
        setProfitLimitPrice(profitLimitPrice);
        
        setTickProfit(tickProfit);
    }
	
	public OrderSign() {
		
//		super();
		orderAction = SystemEnum.OrderAction.Default;
	}
	
	

	public Integer getParentOrderIdInIB() {
		return parentOrderIdInIB;
	}

	public void setParentOrderIdInIB(Integer parentOrderIdInIB) {
		this.parentOrderIdInIB = parentOrderIdInIB;
	}

	public Integer getProfitLimitOrderIdInIB() {
		return profitLimitOrderIdInIB;
	}

	public void setProfitLimitOrderIdInIB(Integer profitLimitOrderIdInIB) {
		this.profitLimitOrderIdInIB = profitLimitOrderIdInIB;
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
	
	public double getLimitFilledPrice() {
		return limitFilledPrice;
	}

	public void setLimitFilledPrice(double limitFilledPrice) {
		this.limitFilledPrice = limitFilledPrice;
	}

	public double getProfitLimitFilledPrice() {
		return profitLimitFilledPrice;
	}

	public void setProfitLimitFilledPrice(double profitLimitFilledPrice) {
		this.profitLimitFilledPrice = profitLimitFilledPrice;
	}

	@Override
	public String toString() {
		return "OrderSign [setting=" + setting + ", parentOrderIdInIB=" + parentOrderIdInIB
				+ ", profitLimitOrderIdInIB=" + profitLimitOrderIdInIB + ", orderState=" + orderState + ", time=" + time
				+ ", orderAction=" + orderAction + ", actionText=" + actionText + ", limitPrice=" + limitPrice
				+ ", tick=" + tick + ", profitLimitPrice=" + profitLimitPrice + ", limitFilledPrice=" + limitFilledPrice
				+ ", profitLimitFilledPrice=" + profitLimitFilledPrice + ", tickProfit=" + tickProfit + "]";
	}
	
	
}
