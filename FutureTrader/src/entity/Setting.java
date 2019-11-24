package entity;

import java.util.ArrayList;

import systemenum.SystemEnum;

public class Setting {
	
	private String setting;
	
	private ArrayList<SingleOrderSetting> orderSettingList;

	public Setting () {
		
//		super();
		this.setOrderSettingList(new ArrayList<SingleOrderSetting>());
	}
	
	public String getSetting() {
		return setting;
	}

	public void setSetting(String setting) {
		this.setting = setting;
	}

	public ArrayList<SingleOrderSetting> getOrderSettingList() {
		return orderSettingList;
	}

	public void setOrderSettingList(ArrayList<SingleOrderSetting> orderSettingList) {
		this.orderSettingList = orderSettingList;
	}
	
	

}
