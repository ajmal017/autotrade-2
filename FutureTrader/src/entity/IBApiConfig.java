package entity;

import systemenum.SystemEnum;

public class IBApiConfig {
	
	private boolean active;
	private Enum<SystemEnum.IbAccountType> accType;
	
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public Enum<SystemEnum.IbAccountType> getAccType() {
		return accType;
	}
	public void setAccType(Enum<SystemEnum.IbAccountType> accType) {
		this.accType = accType;
	}
}
