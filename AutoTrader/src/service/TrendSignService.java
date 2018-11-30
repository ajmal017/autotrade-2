package service;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import DAO.TrendSignDAO;
import DAO.TrendSignDAOFactory;
import DAO.ZoneDAOFactory;
import config.SystemConfig;
import entity.*;
import systemenum.SystemEnum;
import tool.Util;

public class TrendSignService {

	private volatile static TrendSignService instance;
	private ArrayList<TrendSign> dailySignList;
	private double closePriceSwim;
	private double closePriceIB;
	
	//初始化函数
    private TrendSignService ()  {
    	
    	this.dailySignList = new ArrayList<TrendSign>();
    } 
	
    public void checkScenarioTrend() {
		
    	ScenarioService scenarioService = ScenarioService.getInstance();
    	ZoneColorInfoService colorService = ZoneColorInfoService.getInstance();
    	
    	for (Scenario s : scenarioService.getWorkingScenarioList()) {
    		
    		int scenarioGreen = 0;
    		int scenarioRed = 0;
    		boolean trendAppear = true;
    		
			for (Area a : s.getAreaList()) {
				int areaGreen = 0;
				int areaRed = 0;
				for (String zone : a.getZoneList()) {
					Enum<SystemEnum.Color> c = colorService.getColorByZone(zone);
					if (c == SystemEnum.Color.Green) {scenarioGreen++; areaGreen++;}
					if (c == SystemEnum.Color.Red) {scenarioRed++; areaRed++;}
				}
				if ((areaGreen > areaRed && areaRed <= a.getPercent()) ||
				    (areaRed > areaGreen && areaGreen <= a.getPercent())) {
					trendAppear = trendAppear & true; 
				} else {
					trendAppear = trendAppear & false;
				}
			}
			
			if (trendAppear) {
				if(scenarioGreen > scenarioRed && s.getTrend() != SystemEnum.Trend.Up) {
					s.setTrend(SystemEnum.Trend.Up);
					pushNewTrendSign(s.getScenario(),s.getTrend(),scenarioGreen,scenarioRed);
				}
				if(scenarioGreen < scenarioRed && s.getTrend() != SystemEnum.Trend.Down) {
					s.setTrend(SystemEnum.Trend.Down);
					pushNewTrendSign(s.getScenario(),s.getTrend(),scenarioGreen,scenarioRed);
				}
			}
		}
	}
    
    public void pushNewTrendSign (String scenario, Enum<SystemEnum.Trend> trend, int green, int red) {
    	
    	Rectangle rect = ZoneDAOFactory.getZoneDAO().getRectByName("swim_price");
    	Util.createScreenShotByRect(rect,
    			SystemConfig.DOC_PATH + "//" + SystemConfig.PRICE_IMG_NAME,
    			"png");
    	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    	Rectangle screenRectangle = new Rectangle(screenSize);
    	String shotPath = SystemConfig.DOC_PATH + "//screenshot//" + 
    					  Util.getDateStringByDateAndFormatter(new Date(), "yyyyMMdd") + "//"+ 
    					  scenario + "//" + 
    					  scenario + "_" + Util.getDateStringByDateAndFormatter(new Date(), "HHmmss") + ".png";
    	Util.createScreenShotByRect(screenRectangle, shotPath, "png");
    	String swimPriceStr = Util.getStringByScreenShotPng(SystemConfig.DOC_PATH,SystemConfig.PRICE_IMG_NAME);
    	double priceSwim = Util.getPriceByString(swimPriceStr);
    	
    	double priceIB = 110.11; //todo
    	
    	TrendSign newSign = new TrendSign(new Date(), scenario, trend, green, red, priceSwim, priceIB, "", 0, 0);

    	getDailySignList().add(newSign);
    	TrendSignDAOFactory.getTrendSignDAO().insertNewTrendSign(newSign);
    }
    
    public void todayScenarioIsFinished () {
    	
    	Rectangle rect = ZoneDAOFactory.getZoneDAO().getRectByName("swim_price");
    	Util.createScreenShotByRect(rect,
    			SystemConfig.DOC_PATH + "//" + SystemConfig.PRICE_IMG_NAME,
    			"png");
    	String swimPriceStr = Util.getStringByScreenShotPng(SystemConfig.DOC_PATH,SystemConfig.PRICE_IMG_NAME);
    	closePriceSwim = Util.getPriceByString(swimPriceStr);
    	//TODO
    	closePriceIB = 111.01;
    	
    	//checkout sign records
    	Map<String, List<String>> recordMap = getTrendRecordWithProfit();
        String[] strArray = excelTitle();
        Util.createExcel(recordMap, strArray);
        
    	getDailySignList().clear(); //清空当日记录
    	closePriceSwim = 0;
    	closePriceIB = 0;
    }
    
    private Map<String, List<String>> getTrendRecordWithProfit() {
    	
    	//根据scenario分组重排
    	ArrayList<TrendSign> outputList = new ArrayList<TrendSign>();
    	ArrayList<String> scenarios = ScenarioService.getInstance().getActiveScenarioList();
    	TrendSignDAO tsDao = TrendSignDAOFactory.getTrendSignDAO();
    	for (String s : scenarios) {
    		double scenarioProfitSwim = 0;
    		double scenarioProfitIB = 0;
    		//计算每个信号的收益
    		ArrayList<TrendSign> tsList = tsDao.getTrendSignListByDate(new Date(), s);
    		for (int i = 0; i < tsList.size(); i ++) {
    			TrendSign tSign = tsList.get(i);
    			double newProfitSwim = 0;
    			double newProfitIB = 0;
    			//只有第2个信号出现，才算有第1比收益。
    			if (i > 0) { 
    				newProfitSwim = Util.getProfit(tsList.get(i-1).getPriceSwim(), tSign.getPriceSwim(), tsList.get(i-1).getTrend());
    				newProfitIB = Util.getProfit(tsList.get(i-1).getPriceIB(), tSign.getPriceIB(), tsList.get(i-1).getTrend());
				}
				scenarioProfitSwim += newProfitSwim;
				scenarioProfitIB += newProfitIB;
				tSign.setProfitSwim(newProfitSwim);
				tSign.setProfitIB(newProfitSwim);
    			outputList.add(tSign);
			}
    		//计算休市价和最后一个信号间的收益。
    		scenarioProfitSwim += Util.getProfit(tsList.get(tsList.size()-1).getPriceSwim(), closePriceSwim, tsList.get(tsList.size()-1).getTrend());
    		scenarioProfitIB+= Util.getProfit(tsList.get(tsList.size()-1).getPriceIB(), closePriceIB, tsList.get(tsList.size()-1).getTrend());
    		//添加收益汇总行
    		TrendSign profitLine = new TrendSign();
    		profitLine.setTime(new Date());
    		profitLine.setScenario(s);
    		profitLine.setTrend(SystemEnum.Trend.Default);
    		profitLine.setTrendText("close"); //休市
    		profitLine.setPriceSwim(closePriceSwim); //休市价
    		profitLine.setPriceIB(closePriceIB); //休市价
    		profitLine.setProfitSwim(scenarioProfitSwim); //总收益
    		profitLine.setProfitIB(scenarioProfitIB); //总收益
    		profitLine.setDesc("Daily Total Profit");
    		outputList.add(profitLine);
    	}

        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        for (int i = 0; i < outputList.size(); i++) {
        	TrendSign sign = outputList.get(i);
		    ArrayList<String> params = new ArrayList<String>();
		    //no
		    params.add((i+1) + "");
		    //time
		    params.add(df.format(sign.getTime()));
		    //scenario
		    params.add(sign.getScenario());
		    //trend
		    params.add(sign.getTrendText());
		    //count
		    params.add(sign.getGreenCount()+"");
		    params.add(sign.getRedCount()+"");
		    //price 
		    params.add(sign.getPriceSwim()+"");
		    params.add(sign.getPriceIB()+"");
		    //profit
		    params.add(sign.getProfitSwim()+"");
		    params.add(sign.getProfitIB()+"");
		    //desc
		    params.add(sign.getDesc());
		    //map key
		    map.put((i+1) + "", params);
		}
		return map;
    }
    
    private String[] excelTitle() {
        String[] strArray = { "no", "time", "scenario", "trend", "green", "red", "price_swim", "price_ib", "profit_swim", "profit_ib", "desc"};
        return strArray;
    }
    
    public Enum<SystemEnum.Trend> getTodayLastTrendByScenario(String scenario) {
    	TrendSignDAO tsDao = TrendSignDAOFactory.getTrendSignDAO();
    	return tsDao.getLastTrendByScenario(new Date(), scenario);
    }
    
	//单例函数 //IMPORT can not init before ScenarioService
    public static TrendSignService getInstance() {  
    	if (instance == null) {  
    		synchronized (TrendSignService.class) {  
    			if (instance == null) {  
    				instance = new TrendSignService();  
    			}	  
    		}  
    	}  
    	return instance;  
    }
    
	public ArrayList<TrendSign> getDailySignList() {
		return dailySignList;
	}

	public void setDailySignList(ArrayList<TrendSign> dailySignList) {
		this.dailySignList = dailySignList;
	}

	public double getClosePriceSwim() {
		return closePriceSwim;
	}

	public void setClosePriceSwim(double closePriceSwim) {
		this.closePriceSwim = closePriceSwim;
	}

	public double getClosePriceIB() {
		return closePriceIB;
	}

	public void setClosePriceIB(double closePriceIB) {
		this.closePriceIB = closePriceIB;
	} 
	
}
