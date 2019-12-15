package entity;

public class OrderIndexRecordBean  {
	
	private int orderIdInIB;
	private String setting;
	private int indexInDailySignInTableMap;
	private int indexInDailySignMap;
	private int indexInCurrentOrderMap;
	public int getOrderIdInIB() {
		return orderIdInIB;
	}
	public void setOrderIdInIB(int orderIdInIB) {
		this.orderIdInIB = orderIdInIB;
	}
	public String getSetting() {
		return setting;
	}
	public void setSetting(String setting) {
		this.setting = setting;
	}
	
	public int getIndexInDailySignInTableMap() {
		return indexInDailySignInTableMap;
	}
	public void setIndexInDailySignInTableMap(int indexInDailySignInTableMap) {
		this.indexInDailySignInTableMap = indexInDailySignInTableMap;
	}
	public int getIndexInDailySignMap() {
		return indexInDailySignMap;
	}
	public void setIndexInDailySignMap(int indexInDailySignMap) {
		this.indexInDailySignMap = indexInDailySignMap;
	}
	public int getIndexInCurrentOrderMap() {
		return indexInCurrentOrderMap;
	}
	public void setIndexInCurrentOrderMap(int indexInCurrentOrderMap) {
		this.indexInCurrentOrderMap = indexInCurrentOrderMap;
	}
	@Override
	public String toString() {
		return "OrderIndexRecordBean [orderIdInIB=" + orderIdInIB + ", setting=" + setting
				+ ", indexInDailySignInTableMap=" + indexInDailySignInTableMap + ", indexInDailySignMap="
				+ indexInDailySignMap + ", indexInCurrentOrderMap=" + indexInCurrentOrderMap + "]";
	}
	

}
