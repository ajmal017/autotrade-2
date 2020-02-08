package service;

public interface IBServiceCallbackInterface {

//	void updateTradePrice();
	void responseWhenParentOrderSubmitted(Integer parentOrderId, String orderStatus);
	void responseWhenParentOrderFilled(Integer parentOrderId, String orderStatus, double filledPrice);
	void responseWhenProfitLimitOrderFilled(Integer parentOrderId, String orderStatus, double filledPrice);
	
	void responseWhenParentOrderCancelled(Integer parentOrderId, String orderStatus);
	
	void responseCurrentPrice(double price);
//	void ibLogouted();
}
