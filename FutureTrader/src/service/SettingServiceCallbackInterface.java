package service;

public interface SettingServiceCallbackInterface {

	void updateOrderInfoInTable(String setting, Integer orderId, double limitPrice, double stopPrice,
			String orderState);

	void closeAppAfterPriceUpdate();
}
