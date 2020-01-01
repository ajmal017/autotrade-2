package entity;

import java.util.Date;

import systemenum.SystemEnum;
import tool.Util;

public class CreatedOrder {
	
	private Integer orderIdInIB;
	private Integer profitLimitOrderIdInIB;
	private String orderState;
	private Date time;
	private Enum<SystemEnum.OrderAction> orderAction;
	private double limitPrice;
	private double tick;
	private double profitLimitPrice;
	
	public CreatedOrder(
			Integer orderIdInIB, 
			Integer profitLimitOrderIdInIB,
			String orderState,
			Date time, 
			Enum<SystemEnum.OrderAction> orderAction, 
			double limitPrice,  
			double tick,
			double profitLimitPrice) {
		setOrderIdInIB(orderIdInIB);
		setProfitLimitOrderIdInIB(profitLimitOrderIdInIB);
		setOrderState(orderState);
        setTime(time);
        setOrderAction(orderAction);
        setLimitPrice(limitPrice);
        setTick(tick);
        setProfitLimitPrice(profitLimitPrice);
    }
	
	public Integer getOrderIdInIB() {
		return orderIdInIB;
	}
	public void setOrderIdInIB(Integer orderIdInIB) {
		this.orderIdInIB = orderIdInIB;
	}
	
	
	
	public Integer getProfitLimitOrderIdInIB() {
		return profitLimitOrderIdInIB;
	}

	public void setProfitLimitOrderIdInIB(Integer profitLimitOrderIdInIB) {
		this.profitLimitOrderIdInIB = profitLimitOrderIdInIB;
	}

	public String getOrderState() {
		return orderState;
	}
	public void setOrderState(String orderState) {
		this.orderState = orderState;
	}
	
	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public Enum<SystemEnum.OrderAction> getOrderAction() {
		return orderAction;
	}
	public void setOrderAction(Enum<SystemEnum.OrderAction> orderAction) {
		this.orderAction = orderAction;
	}
	public double getLimitPrice() {
		return limitPrice;
	}
	public void setLimitPrice(double limitPrice) {
		this.limitPrice = limitPrice;
	}
	public double getTick() {
		return tick;
	}
	public void setTick(double tick) {
		this.tick = tick;
	}
	
	public double getProfitLimitPrice() {
		return profitLimitPrice;
	}

	public void setProfitLimitPrice(double profitLimitPrice) {
		this.profitLimitPrice = profitLimitPrice;
	}

	@Override
	public String toString() {
		return "CreatedOrder [orderIdInIB=" + orderIdInIB + ", profitLimitOrderIdInIB=" + profitLimitOrderIdInIB
				+ ", orderState=" + orderState + ", time=" + time + ", orderAction=" + orderAction + ", limitPrice="
				+ limitPrice + ", tick=" + tick + ", profitLimitPrice=" + profitLimitPrice + "]";
	}

	
}
