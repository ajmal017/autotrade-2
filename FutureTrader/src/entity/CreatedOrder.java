package entity;

import java.util.Date;

import systemenum.SystemEnum;
import tool.Util;

public class CreatedOrder {
	
	private Integer orderIdInIB;
	private String orderState;
	private Date time;
	private Enum<SystemEnum.OrderAction> orderAction;
	private double limitPrice;
	private double tick;
	private double stopPrice;
	
	public CreatedOrder(
			Integer orderIdInIB, 
			String orderState,
			Date time, 
			Enum<SystemEnum.OrderAction> orderAction, 
			double limitPrice,  
			double tick,
			double stopPrice) {
		setOrderIdInIB(orderIdInIB);
		setOrderState(orderState);
        setTime(time);
        setOrderAction(orderAction);
        setLimitPrice(limitPrice);
        setTick(tick);
        setStopPrice(stopPrice);
    }
	
	public Integer getOrderIdInIB() {
		return orderIdInIB;
	}
	public void setOrderIdInIB(Integer orderIdInIB) {
		this.orderIdInIB = orderIdInIB;
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
	public double getStopPrice() {
		return stopPrice;
	}
	public void setStopPrice(double stopPrice) {
		this.stopPrice = stopPrice;
	}

	@Override
	public String toString() {
		return "CreatedOrder [orderIdInIB=" + orderIdInIB + ", orderState=" + orderState + ", time=" + time
				+ ", orderAction=" + orderAction + ", limitPrice=" + limitPrice + ", tick=" + tick + ", stopPrice="
				+ stopPrice + "]";
	}
}
