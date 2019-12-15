package service;

public interface IBServiceCallbackInterface {

	void updateTradePrice(double price, String preOrderScenario, String preOrderTime, int preQuantity);
	void responseWhenOrderActive(int orderId, String orderState);
	void responseFuturePriceWhenOrderStop(int orderid, double limitPrice, double stopPrice, String orderState);
//	void ibLogouted();
}
