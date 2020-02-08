package ib;

public interface MyEWrapperImplCallbackInterface {

//	void updateTradePrice(double price);
//	void ibLogouted();
	
	void responseCurrentPrice(double price);
	void responseOrderStatusUpdate(int parentOrderId, int orderId, String orderStatus, double filledQuantity, double remainingQuantity, double avgFilledPrice);
}
