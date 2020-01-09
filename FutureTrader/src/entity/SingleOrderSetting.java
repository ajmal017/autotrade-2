package entity;

public class SingleOrderSetting {
	
	private double limitChange;
	private double tick;
	private double profitLimitChange;
	
	public double getTick() {
		return tick;
	}
	
	public void setTick(double tick) {
		this.tick = tick;
	}

	public double getLimitChange() {
		return limitChange;
	}

	public void setLimitChange(double limitChange) {
		this.limitChange = limitChange;
	}

	public double getProfitLimitChange() {
		return profitLimitChange;
	}

	public void setProfitLimitChange(double profitLimitChange) {
		this.profitLimitChange = profitLimitChange;
	}

	

}
