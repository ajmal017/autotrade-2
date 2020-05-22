package entity;


public class IBMsgDelegateLog {
	
	private Integer orderId;
	private String orderStatus;
	private double remainingQuantity;
	
	public IBMsgDelegateLog(Integer orderId, String orderStatus, double remainingQuantity) {
		setOrderId(orderId);
		setOrderStatus(orderStatus);
		setRemainingQuantity(remainingQuantity);
	}
	
	public IBMsgDelegateLog() {
		
	}


	public Integer getOrderId() {
		return orderId;
	}


	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}


	public String getOrderStatus() {
		return orderStatus;
	}


	public void setOrderStatus(String orderStatus) {
		this.orderStatus = orderStatus;
	}


	public double getRemainingQuantity() {
		return remainingQuantity;
	}


	public void setRemainingQuantity(double remainingQuantity) {
		this.remainingQuantity = remainingQuantity;
	}

	
}
