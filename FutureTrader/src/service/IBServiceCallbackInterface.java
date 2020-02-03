package service;

public interface IBServiceCallbackInterface {

//	void updateTradePrice();
	void responseWhenOrderSubmitted(Integer parentOrderId, Integer orderId, String orderState);
	void responseWhenParentOrderFilled(Integer orderId, String orderState, double filledPrice);
	void responseWhenProfitLimitOrderFilled(Integer parentOrderId, Integer orderid, double filledPrice, String orderState, double tick);
	
	void responseCurrentPrice(double price);
//	void ibLogouted();
}
