package entity;

public class StockConfig {

	private String stockSymbol; //AAPL
	private String orderType; //MKT
	private String securityType; //STK
	private String stockExchange; //SMART
	private String primaryExchange; //ISLAND
	private String stockCurrency;//USD
	private double stockExpiry; //0.0
	private int orderQuantity; //100
	private double firstOrderQuantityPercent;
	
	public StockConfig () {
		
	}
	
	public String getStockSymbol() {
		return stockSymbol;
	}
	public void setStockSymbol(String stockSymbol) {
		this.stockSymbol = stockSymbol;
	}
	public String getOrderType() {
		return orderType;
	}
	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}
	public String getSecurityType() {
		return securityType;
	}
	public void setSecurityType(String securityType) {
		this.securityType = securityType;
	}
	public String getStockExchange() {
		return stockExchange;
	}
	public void setStockExchange(String stockExchange) {
		this.stockExchange = stockExchange;
	}
	public String getPrimaryExchange() {
		return primaryExchange;
	}
	public void setPrimaryExchange(String primaryExchange) {
		this.primaryExchange = primaryExchange;
	}
	public String getStockCurrency() {
		return stockCurrency;
	}
	public void setStockCurrency(String stockCurrency) {
		this.stockCurrency = stockCurrency;
	}
	public double getStockExpiry() {
		return stockExpiry;
	}
	public void setStockExpiry(double stockExpiry) {
		this.stockExpiry = stockExpiry;
	}
	public int getOrderQuantity() {
		return orderQuantity;
	}
	public void setOrderQuantity(int orderQuantity) {
		this.orderQuantity = orderQuantity;
	}

	public double getFirstOrderQuantityPercent() {
		return firstOrderQuantityPercent;
	}

	public void setFirstOrderQuantityPercent(double firstOrderQuantityPercent) {
		this.firstOrderQuantityPercent = firstOrderQuantityPercent;
	}
}
