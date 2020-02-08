package entity;

import javafx.beans.property.SimpleStringProperty;

public class SignTableItem {

	private SimpleStringProperty time;
	private SimpleStringProperty ibOrderId;
	private SimpleStringProperty orderStatus;
    private SimpleStringProperty setting;
    private SimpleStringProperty action;
    private SimpleStringProperty limitPrice;
    private SimpleStringProperty tick;
    private SimpleStringProperty profitLimitPrice;
    private SimpleStringProperty tickProfit;
    private SimpleStringProperty limitFilledPrice;
	private SimpleStringProperty profitLimitFilledPrice;

    public SignTableItem(String time, String ibOrderId, String orderStatus, String setting, 
    		String action, String limitPrice, String tick, String profitLimitPrice, 
    		String tickProfit, String limitFilledPrice, String profitLimitFilledPrice) {
        this.time = new SimpleStringProperty(time);
        this.ibOrderId = new SimpleStringProperty(ibOrderId);
        this.orderStatus = new SimpleStringProperty(orderStatus);
        this.setting = new SimpleStringProperty(setting);
        this.action = new SimpleStringProperty(action);
        this.limitPrice = new SimpleStringProperty(limitPrice);
        this.tick = new SimpleStringProperty(tick);
        this.profitLimitPrice = new SimpleStringProperty(profitLimitPrice);
        this.tickProfit = new SimpleStringProperty(tickProfit);
        this.limitFilledPrice = new SimpleStringProperty(limitFilledPrice);
        this.profitLimitFilledPrice = new SimpleStringProperty(profitLimitFilledPrice);
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
    
    public String getOrderStatus() {
        return orderStatus.get();
    }

    public void setOrderStatus(String o) {
    	orderStatus.set(o);
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


	public String getProfitLimitPrice() {
		return profitLimitPrice.get();
	}

	public void setProfitLimitPrice(String c) {
		profitLimitPrice.set(c);
	}

	public String getTickProfit() {
		return tickProfit.get();
	}

	public void setTickProfit(String t) {
		tickProfit.set(t);
	}
   
	public String getLimitFilledPrice() {
		return limitFilledPrice.get();
	}

	public void setLimitFilledPrice(String t) {
		limitFilledPrice.set(t);
	}
	
	public String getProfitLimitFilledPrice() {
		return profitLimitFilledPrice.get();
	}

	public void setProfitLimitFilledPrice(String t) {
		profitLimitFilledPrice.set(t);
	}
}
