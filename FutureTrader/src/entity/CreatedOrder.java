package entity;

import java.util.Date;

import systemenum.SystemEnum;

public class CreatedOrder {
	
	private int orderIdInIB;
	private String orderState;
	private Date createTime;
	private Enum<SystemEnum.OrderAction> orderAction;
	private double limitPrice;
	private double tick;
	private double stopPrice;
	public int getOrderIdInIB() {
		return orderIdInIB;
	}
	public void setOrderIdInIB(int orderIdInIB) {
		this.orderIdInIB = orderIdInIB;
	}
	
	public String getOrderState() {
		return orderState;
	}
	public void setOrderState(String orderState) {
		this.orderState = orderState;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
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
		return "CreatedOrder [orderIdInIB=" + orderIdInIB + ", orderState=" + orderState + ", createTime=" + createTime
				+ ", orderAction=" + orderAction + ", limitPrice=" + limitPrice + ", tick=" + tick + ", stopPrice="
				+ stopPrice + "]";
	}
	
	
}
