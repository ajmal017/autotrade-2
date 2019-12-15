package service;

public interface SettingServiceCallbackInterface {
	
	void updateOrderInfoInTable(String setting, int orderId, double limitPrice, double stopPrice, String orderState);
	void closeAppAfterPriceUpdate();
}
