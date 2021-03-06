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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import DAO.ScenarioDAOFactory;
import DAO.TrendSignDAO;
import DAO.TrendSignDAOFactory;
import DAO.ZoneDAO;
import DAO.ZoneDAOFactory;
import config.SystemConfig;
import entity.*;
import systemenum.SystemEnum;
import tool.Util;

public class TrendSignService {

	private volatile static TrendSignService instance;
	private ArrayList<TrendSign> dailySignList;
	
	//初始化函数
    private TrendSignService ()  {
    	
    	this.dailySignList = new ArrayList<TrendSign>();
    } 
	
    private Map<String, List<String>> getTrendRecordWithProfit(String scenario) {
    	
        Map<String, List<String>> map = new HashMap<String, List<String>>();
    	ArrayList<TrendSign> tsList = TrendSignDAOFactory.getTrendSignDAO().getTrendSignListByDate(new Date(), scenario);
        if(tsList.size() == 0) return map;
        
//    	ArrayList<TrendSign> outputList = new ArrayList<TrendSign>();
    	double scenarioProfitIB = 0;
    	for (int i = 1; i < tsList.size(); i ++) {
    		TrendSign tSign = tsList.get(i); //second trend
    		double newProfitIB = Util.getProfit(tsList.get(i-1).getPriceIB(), tSign.getPriceIB(), tsList.get(i-1).getTrend());
			scenarioProfitIB += newProfitIB;
			tSign.setProfitIB(newProfitIB);
		}
    	/*
    	//add total line
    	TrendSign profitLine = new TrendSign();
    	profitLine.setTime(new Date());
    	profitLine.setTrend(SystemEnum.Trend.Default);
    	profitLine.setTrendText(Util.getTrendTextByEnum(SystemEnum.Trend.Default)); //休市
    	profitLine.setProfitIB(scenarioProfitIB); //总收益
    	outputList.add(profitLine);
    	*/
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
    	

        for (int i = 0; i < tsList.size(); i++) {
        	TrendSign sign = tsList.get(i);
		    ArrayList<String> params = new ArrayList<String>();
		    
		    //time
		    params.add(df.format(sign.getTime()));
		    //scenario
		    params.add(sign.getScenario());
		    //trend
		    params.add(sign.getTrendText());
		    //count
		    if(sign.getGreenCount()>0) {
		    	params.add(sign.getGreenCount()+"");
		    } else {
		    	params.add("0");
		    }
		    if(sign.getRedCount()>0) {
		    	params.add(sign.getRedCount()+"");
		    } else {
		    	params.add("0");
		    }
		    //price
		    if(sign.getPriceSwim()!=0) {
		    	params.add(sign.getPriceSwim()+"");
		    } else {
		    	params.add("0");
		    }
		    if(sign.getPriceIB()!=0) {
		    	params.add(sign.getPriceIB()+"");
		    } else {
		    	params.add("0");
		    }
		    
		    //profit
		    if(sign.getProfitSwim()!=0) {
		    	params.add(sign.getProfitSwim()+"");
		    } else {
		    	params.add("0");
		    }
		    if(sign.getProfitIB()!=0) {
		    	params.add(sign.getProfitIB()+"");
		    } else {
		    	params.add("0");
		    }
		    //desc
		    params.add(sign.getDesc());
		    //map key
		    map.put((i+1) + "", params);
		}
		return map;
    }
    
    private String[] excelTitle() {
        String[] strArray = { "time", "scenario", "trend", "green", "red", "price_swim", "price_ib", "profit_swim", "profit_ib", "desc"};
        return strArray;
    }
    
    private int getGreenCountByZoneList(ArrayList<Zone> zoneList) {

    	int g = 0;
    	ZoneColorInfoService colorService = ZoneColorInfoService.getInstance();
    	for(Zone zone : zoneList) {
    		Enum<SystemEnum.Color> c = colorService.getColorByZone(zone.getZone());
    		if (c == SystemEnum.Color.Green) {g++;}
    	}
    	return g;
    }
    
    private int getRedCountByZoneList(ArrayList<Zone> zoneList) {

    	int r = 0;
    	ZoneColorInfoService colorService = ZoneColorInfoService.getInstance();
    	for(Zone zone : zoneList) {
    		Enum<SystemEnum.Color> c = colorService.getColorByZone(zone.getZone());
    		if (c == SystemEnum.Color.Red) {r++;}
    	}
    	return r;
    }
    
    public void checkScenarioTrend() {
		
    	ScenarioService scenarioService = ScenarioService.getInstance();
    	ZoneColorInfoService colorService = ZoneColorInfoService.getInstance();
    	ZoneDAO zoneDao = ZoneDAOFactory.getZoneDAO();
    	
    	for (Scenario scenario : scenarioService.getWorkingScenarioList()) {
    		
    		boolean trendAppear = true;
    		
    		Enum<SystemEnum.Color> preColor = SystemEnum.Color.Default;
    		Enum<SystemEnum.Color> thisColor = SystemEnum.Color.Default;
			for (Area area : scenario.getAreaList()) {
				int areaGreen = 0;
				int areaRed = 0;
				for (String zone : area.getZoneList()) {
					Enum<SystemEnum.Color> c = colorService.getColorByZone(zone);
					if (c == SystemEnum.Color.Green) {areaGreen++;}			
					if (c == SystemEnum.Color.Red) {areaRed++;}
				}
				if ((areaGreen + areaRed == area.getZoneList().size() && areaGreen > areaRed && areaRed <= area.getPercent()) ||
				    (areaGreen + areaRed == area.getZoneList().size() && areaRed > areaGreen && areaGreen <= area.getPercent())) {
					
					if(preColor != SystemEnum.Color.Default) {
						
						if(areaGreen > areaRed) {
							thisColor = SystemEnum.Color.Green;
						} else {
							thisColor = SystemEnum.Color.Red;
						}
						if(thisColor != preColor) {
							trendAppear = trendAppear & false;
						} else {
							trendAppear = trendAppear & true; 
						}
					} else {
						trendAppear = trendAppear & true; 
					}
					
				} else {
					trendAppear = trendAppear & false;
				}
				
				if(areaGreen > areaRed) {
					preColor = SystemEnum.Color.Green;
				} else {
					preColor = SystemEnum.Color.Red;
				}
				
			}
			if (trendAppear) {
				ArrayList<Scenario> ss = new ArrayList<Scenario>();
				ss.add(scenario);
				ArrayList<Zone> zones = zoneDao.getRelatedZoneListByScenarioList(ss);
				int scenarioGreen = getGreenCountByZoneList(zones);
				int scenarioRed = getRedCountByZoneList(zones);
				if(scenarioGreen > scenarioRed && scenario.getTrend() != SystemEnum.Trend.Up) {
					scenario.setTrend(SystemEnum.Trend.Up);
					pushNewTrendSign(scenario.getScenario(),scenario.getTrend(),scenarioGreen,scenarioRed);
				}
				if(scenarioGreen < scenarioRed && scenario.getTrend() != SystemEnum.Trend.Down) {
					scenario.setTrend(SystemEnum.Trend.Down);
					pushNewTrendSign(scenario.getScenario(),scenario.getTrend(),scenarioGreen,scenarioRed);
				}
			}
		}
	}
    
    public void pushNewTrendSign (String scenario, Enum<SystemEnum.Trend> trend, int green, int red) {
    	
    	//swim price
    	Rectangle rect = ZoneDAOFactory.getZoneDAO().getRectByName("swim_price");
    	Util.createScreenShotByRect(rect,
    			SystemConfig.DOC_PATH + "//" + SystemConfig.PRICE_IMG_NAME,
    			"png");
    	String swimPriceStr = Util.getStringByScreenShotPng(SystemConfig.DOC_PATH,SystemConfig.PRICE_IMG_NAME);
    	System.out.println("swimPriceStr:"+swimPriceStr);
    	double priceSwim = 0.0;
    	if(swimPriceStr != null && swimPriceStr.length() > 0) {
    		priceSwim = Util.getPriceByString(swimPriceStr);
    		System.out.println("priceSwim:"+priceSwim);
    	}
    	
    	TrendSign newSign = new TrendSign(new Date(), scenario, trend, green, red, priceSwim, 0, "", 0, 0);
    	getDailySignList().add(newSign);
    	TrendSignDAOFactory.getTrendSignDAO().insertNewTrendSign(newSign);
    	
    	//ib trade
    	//todo
    	
    	
    	ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
	    cachedThreadPool.execute(new Runnable() {
	  
	        @Override
	        public void run() {
	        	//screen shot
            	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            	Rectangle screenRectangle = new Rectangle(screenSize);
            	String shotPath = SystemConfig.DOC_PATH + "//screenshot//" + 
            					  Util.getDateStringByDateAndFormatter(new Date(), "yyyyMMdd") + "//"+ 
            					  scenario + "//" + 
            					  scenario + "_" + Util.getDateStringByDateAndFormatter(new Date(), "HHmmss") + ".png";
            	Util.createScreenShotByRect(screenRectangle, shotPath, "png");
	        }
	    });
    }
    
    public void exportTodayTrendProfit() {
    	
    	//checkout sign records
    	ArrayList<String> sheetList = ScenarioDAOFactory.getScenarioDAO().getAllActiveScenarioName();
    	ArrayList<Map<String, List<String>>> mapList = new ArrayList<Map<String, List<String>>>();
    	int trendCount = 0;
    	for (String s : sheetList) {
    		Map<String, List<String>> recordMap = getTrendRecordWithProfit(s);
    		mapList.add(recordMap);
    		trendCount += recordMap.size();
    	}
    	
    	if(trendCount > 0) {
    	
    		String path = SystemConfig.DOC_PATH + 
        		"//trendprofit//" +
        		Util.getDateStringByDateAndFormatter(new Date(), "yyyyMMdd") +  
        		".xls";
    		Util.createExcel(sheetList, mapList, excelTitle(), path);
    	}
        
    	getDailySignList().clear();
    }
    
    public Enum<SystemEnum.Trend> getTodayLastTrendByScenario(String scenario) {
    	return TrendSignDAOFactory.getTrendSignDAO().getLastTrendByScenario(new Date(), scenario);
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
	
}
