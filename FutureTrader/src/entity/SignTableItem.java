package entity;

import javafx.beans.property.SimpleStringProperty;

public class SignTableItem {

	private SimpleStringProperty time;
	private SimpleStringProperty ibOrderId;
	private SimpleStringProperty orderState;
    private SimpleStringProperty setting;
    private SimpleStringProperty action;
    private SimpleStringProperty limitPrice;
    private SimpleStringProperty tick;
    private SimpleStringProperty stopPrice;
    private SimpleStringProperty tickProfit;

    public SignTableItem(String time, String ibOrderId, String orderState, String setting, String action,  String limitPrice, String tick, String stopPrice, String tickProfit) {
        this.time = new SimpleStringProperty(time);
        this.ibOrderId = new SimpleStringProperty(ibOrderId);
        this.orderState = new SimpleStringProperty(orderState);
        this.setting = new SimpleStringProperty(setting);
        this.action = new SimpleStringProperty(action);
        this.limitPrice = new SimpleStringProperty(limitPrice);
        this.tick = new SimpleStringProperty(tick);
        this.stopPrice = new SimpleStringProperty(stopPrice);
        this.tickProfit = new SimpleStringProperty(tickProfit);
    }
    
    public String getTime() {
        return time.get();
    }

    public void setTime(String t) {
    	time.set(t);
    }
    
    public String getIbOrderId() {
        return ibOrderId.get();
    }

    public void setIbOrderId(String i) {
    	ibOrderId.set(i);
    }
    
    public String getOrderState() {
        return orderState.get();
    }

    public void setOrderState(String o) {
    	orderState.set(o);
    }

	public String getSetting() {
		return setting.get();
	}

	public void setSetting(String s) {
		setting.set(s);
	}

	public String getAction() {
		return action.get();
	}

	public void setAction(String a) {
		action.set(a);
	}

	public String getLimitPrice() {
		return limitPrice.get();
	}

	public void setLimitPrice(String l) {
		limitPrice.set(l);
	}
	
	public String getTick() {
		return tick.get();
	}

	public void setTick(String t) {
		tick.set(t);
	}


	public String getStopPrice() {
		return stopPrice.get();
	}

	public void setStopPrice(String c) {
		stopPrice.set(c);
	}

	public String getTickProfit() {
		return tickProfit.get();
	}

	public void setTickProfit(String t) {
		tickProfit.set(t);
	}
   
}
