package entity;

import javafx.beans.property.SimpleStringProperty;

public class TrendTableItem {

	private SimpleStringProperty time;
    private SimpleStringProperty scenario;
    private SimpleStringProperty trend;
    private SimpleStringProperty greenCount;
    private SimpleStringProperty redCount;
    private SimpleStringProperty whiteCount;
    private SimpleStringProperty swimPrice;
    private SimpleStringProperty ibPrice;

    public TrendTableItem(String time, String scenario, String trend, String green, String red, String white, String swimPrice, String ibPrice) {
        this.time = new SimpleStringProperty(time);
        this.scenario = new SimpleStringProperty(scenario);
        this.trend = new SimpleStringProperty(trend);
        this.greenCount = new SimpleStringProperty(green);
        this.redCount = new SimpleStringProperty(red);
        this.whiteCount = new SimpleStringProperty(swimPrice);
        this.swimPrice = new SimpleStringProperty(swimPrice);
        this.ibPrice = new SimpleStringProperty(ibPrice);
    }

    public String getTime() {
        return time.get();
    }

    public void setTime(String t) {
    	time.set(t);
    }

    public String getScenario() {
        return scenario.get();
    }

    public void setScenario(String s) {
    	scenario.set(s);
    }

    public String getTrend() {
        return trend.get();
    }

    public void setTrend(String t) {
    	trend.set(t);
    }

	public String getRedCount() {
		return redCount.get();
	}

	public void setRedCount(String r) {
		redCount.set(r);
	}

	public String getGreenCount() {
		return greenCount.get();
	}

	public void setGreenCount(String g) {
		greenCount.set(g);
	}

	public String getSwimPrice() {
		return swimPrice.get();
	}

	public void setSwimPrice(String s) {
		swimPrice.set(s);
	}

	public String getIbPrice() {
		return ibPrice.get();
	}

	public void setIbPrice(String i) {
		ibPrice.set(i);
	}

	public String getWhiteCount() {
		return whiteCount.get();
	}

	public void setWhiteCount(String w) {
		whiteCount.set(w);
	}

}
