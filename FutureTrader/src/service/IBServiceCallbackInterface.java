package service;

public interface IBServiceCallbackInterface {

//	void updateTradePrice();
	void responseWhenOrderActive(Integer orderId, String orderState);
	void responseFuturePriceWhenOrderStop(Integer orderid, double limitPrice, double stopPrice, String orderState);
//	void ibLogouted();
}
