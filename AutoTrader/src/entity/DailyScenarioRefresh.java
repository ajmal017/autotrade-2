package entity;

public class DailyScenarioRefresh {
		
	private String refreshTime;
	private boolean passed;
	
	public DailyScenarioRefresh() {
//		super();
	}
	

	public boolean isPassed() {
		return passed;
	}

	public void setPassed(boolean passed) {
		this.passed = passed;
	}


	public String getRefreshTime() {
		return refreshTime;
	}


	public void setRefreshTime(String refreshTime) {
		this.refreshTime = refreshTime;
	}
}
