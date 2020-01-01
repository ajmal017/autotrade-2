package service;

public interface IBServiceCallbackInterface {

//	void updateTradePrice();
	void responseWhenOrderSubmitted(Integer parentOrderId, Integer orderId, String orderState);
	void responseWhenParentOrderFilled(Integer orderId, String orderState);
	void responseWhenProfitLimitOrderFilled(Integer parentOrderId, Integer orderid, double limitPrice, double stopPrice, String orderState, double tick);
//	void ibLogouted();
}
