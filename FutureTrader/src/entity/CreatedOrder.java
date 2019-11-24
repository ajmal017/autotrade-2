package entity;

import systemenum.SystemEnum;

public class CreatedOrder {
	
	private int orderIdInIB;
	private String createTime;
	private SystemEnum.OrderAction orderAction;
	private double limitPrice;
	private double tick;
	private double stopPrice;
	public int getOrderIdInIB() {
		return orderIdInIB;
	}
	public void setOrderIdInIB(int orderIdInIB) {
		this.orderIdInIB = orderIdInIB;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public SystemEnum.OrderAction getOrderAction() {
		return orderAction;
	}
	public void setOrderAction(SystemEnum.OrderAction orderAction) {
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
	
	
}
