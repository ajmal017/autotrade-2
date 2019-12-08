package service;

public interface IBServiceCallbackInterface {

	void updateTradePrice(double price, String preOrderScenario, String preOrderTime, int preQuantity);
	void orderStop(int orderId);
	void orderActive(int orderId);
	
//	void ibLogouted();
}
