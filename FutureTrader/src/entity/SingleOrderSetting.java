package entity;

public class SingleOrderSetting {
	
	private double limitChange;
	private double tick;
	private double stopChange;
	
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

	public double getStopChange() {
		return stopChange;
	}

	public void setStopChange(double stopChange) {
		this.stopChange = stopChange;
	}
	

}
