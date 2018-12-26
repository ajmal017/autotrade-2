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

import config.SystemConfig;
import dao.CommonDAO;
import dao.CommonDAOFactory;
import entity.Area;
import entity.DailyScenarioRefresh;
import entity.Scenario;
import entity.ScenarioTrend;
import entity.TrendSign;
import entity.Volume;
import entity.Zone;
import systemenum.SystemEnum;
import tool.Util;

public class ScenarioGroupService {
	
	private volatile static ScenarioGroupService instance;
	
	private ArrayList<ScenarioTrend> activeScenarioGroupList; //T10+trend T11 T12

	private ArrayList<Volume> workingVolumeList; //T10
	private ArrayList<Scenario> workingScenarioList; //T10
	 
	private ArrayList<DailyScenarioRefresh> volRefreshPlan;
	private int passedVolRefreshPlanCount = 0;
	private ArrayList<DailyScenarioRefresh> sceRefreshPlan;
	private int passedSceRefreshPlanCount = 0; 
	
	private Map<String,ArrayList<TrendSign>> dailySignMap; //T10,List
	
	private ScenarioGroupService ()  {
    	
		this.activeScenarioGroupList = new ArrayList<ScenarioTrend>();
		
    	this.workingScenarioList = new ArrayList<Scenario>();
    	this.workingVolumeList = new ArrayList<Volume>();
    	
    	this.sceRefreshPlan = new ArrayList<DailyScenarioRefresh>();
    	this.volRefreshPlan = new ArrayList<DailyScenarioRefresh>();
    	
    	this.dailySignMap = new HashMap<String, ArrayList<TrendSign>>();
    	
    	initAllScenarioGroupData();
    }
	
	private void initAllVolumeData() {
		
		CommonDAO commonDao = CommonDAOFactory.getCommonDAO();
		
		ArrayList<String> times = commonDao.getAllDistinctVolumeStartTimeAndEndTime();
    	if (times.size() == 0) {
			
    		return;
		}
    	
    	for (String d : times) {
    		
    		DailyScenarioRefresh refresh = new DailyScenarioRefresh();
    		refresh.setRefreshTime(d);
    		StringBuilder str = new StringBuilder(Util.getDateStringByDateAndFormatter(new Date(), "yyyyMMdd"));
    		str.append(d);
    		Date dDate = Util.getDateByStringAndFormatter(str.toString(), "yyyyMMddHH:mm");
    		if (dDate.before(new Date())) {
    			refresh.setPassed(true); 
    			int passed = getPassedVolRefreshPlanCount() + 1;
    			setPassedVolRefreshPlanCount(passed);
			} else {
				refresh.setPassed(false);
			}
    		getVolRefreshPlan().add(refresh);	
		}
    	
	}
	
	private void initAllScenarioData() {
    	
		CommonDAO commonDao = CommonDAOFactory.getCommonDAO();
    	//create scenario start time and end time
    	//create scenario  refresh plan
    	ArrayList<String> times = commonDao.getAllDistinctScenarioStartTimeAndEndTime();
    	if (times.size() == 0) {
			
    		return;
		}
    	
    	for (String d : times) {
    		
    		DailyScenarioRefresh refresh = new DailyScenarioRefresh();
    		refresh.setRefreshTime(d);
    		StringBuilder str = new StringBuilder(Util.getDateStringByDateAndFormatter(new Date(), "yyyyMMdd"));
    		str.append(d);
    		Date dDate = Util.getDateByStringAndFormatter(str.toString(), "yyyyMMddHH:mm");
    		if (dDate.before(new Date())) {
    			refresh.setPassed(true); 
    			int passed = getPassedSceRefreshPlanCount() + 1;
    			setPassedSceRefreshPlanCount(passed);
			} else {
				refresh.setPassed(false);
			}
    		getSceRefreshPlan().add(refresh);	
		}
	}
	
	private void initAllScenarioGroupData() {
		

    	CommonDAO commonDao = CommonDAOFactory.getCommonDAO();
    	
    	//get all scenario names and set active = true;
    	ArrayList<String> sceNames = commonDao.getAllActiveScenarioName();
    	if (sceNames.size() == 0) {
			//none active scenario
    		return;
		}
    	
    	ArrayList<Zone> volumeZoneList = commonDao.getVolumeZoneList();
    	if (volumeZoneList.size() == 0) {
			//none volume zone
    		return;
		}
    	
    	for (String nameString : sceNames) {
    		ScenarioTrend st = new ScenarioTrend(nameString);
        	st.setTrend(getTodayLastTrendByScenario(st.getScenario()));
        	getActiveScenarioGroupList().add(new ScenarioTrend(nameString));
        	getDailySignMap().put(nameString, new ArrayList<TrendSign>());
		}
		
    	ZoneColorInfoService.getInstance().loadVolumeBarZoneListWithDefaultColor(volumeZoneList);
    	
		initAllVolumeData();
		initAllScenarioData();
	}
	
	private void newVolPlanRefreshed() {
    	
    	DailyScenarioRefresh refresh = getVolRefreshPlan().get(getPassedVolRefreshPlanCount());
		refresh.setPassed(true);
		setPassedVolRefreshPlanCount(getPassedVolRefreshPlanCount()+1);
    }
	
	private void newScePlanRefreshed() {
    	
    	DailyScenarioRefresh refresh = getSceRefreshPlan().get(getPassedSceRefreshPlanCount());
		refresh.setPassed(true);
		setPassedSceRefreshPlanCount(getPassedSceRefreshPlanCount()+1);
    }
	
	private Map<String, List<String>> getTrendRecordWithProfit(String scenario) {
    	
        Map<String, List<String>> map = new HashMap<String, List<String>>();
    	ArrayList<TrendSign> tsList = CommonDAOFactory.getCommonDAO().getTrendSignListByDate(new Date(), scenario);
        if(tsList.size() == 0) return map;
        
    	for (int i = 1; i < tsList.size(); i ++) {
    		TrendSign tSign = tsList.get(i); //second trend
    		double newProfitIB = Util.getProfit(tsList.get(i-1).getPriceIB(), tSign.getPriceIB(), tsList.get(i-1).getTrend());
			tSign.setProfitIB(newProfitIB);
		}
    	
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
	
	private int getGreenCountBySceZoneList(ArrayList<Zone> zoneList) {

    	int g = 0;
    	ZoneColorInfoService colorService = ZoneColorInfoService.getInstance();
    	for(Zone zone : zoneList) {
    		Enum<SystemEnum.Color> c = colorService.getColorBySceZone(zone.getZone());
    		if (c == SystemEnum.Color.Green) {g++;}
    	}
    	return g;
    }
    
    private int getRedCountBySceZoneList(ArrayList<Zone> zoneList) {

    	int r = 0;
    	ZoneColorInfoService colorService = ZoneColorInfoService.getInstance();
    	for(Zone zone : zoneList) {
    		Enum<SystemEnum.Color> c = colorService.getColorBySceZone(zone.getZone());
    		if (c == SystemEnum.Color.Red) {r++;}
    	}
    	return r;
    }
	
	public void closeOrderByScenario(String scenario) {
    	
    	pushNewTrendSign(scenario, SystemEnum.Trend.Default, 0, 0);
    }
	
	public void updateWorkingVolumeListByRefreshPlan() {

    	if (getActiveScenarioGroupList().size() == 0) {
    		workingScenarioList.clear();
    		
    		return;
    	}
    	
    	CommonDAO commonDAO = CommonDAOFactory.getCommonDAO();
    	
    	//get new working volumes
    	ArrayList<Volume> newVolumeList = commonDAO.getAllWorkingVolumeAtTime(new Date());
    	if (newVolumeList.size() == 0) {
    		for (ScenarioTrend activeS : getActiveScenarioGroupList()) {
				if(activeS.getTrend() != SystemEnum.Trend.Default) closeOrderByScenario(activeS.getScenario());
			}
    		workingVolumeList.clear();
    		newVolPlanRefreshed();
    		return;
		}
    	
    	//get active working scenarios
    	ArrayList<Volume> tempVolumeList = new ArrayList<Volume>();
    	for (Volume newV : newVolumeList) {
    		for (ScenarioTrend activeS : getActiveScenarioGroupList()) {
    			if (newV.getScenario().equals(activeS.getScenario())) {
    				tempVolumeList.add(newV);
    				break;
				}
    		}
		}
    	if (tempVolumeList.size() == 0) {
    		//none new scenario
    		for (ScenarioTrend activeS : getActiveScenarioGroupList()) {
				if(activeS.getTrend() != SystemEnum.Trend.Default) closeOrderByScenario(activeS.getScenario());
			}
    		workingVolumeList.clear();
    		newVolPlanRefreshed();
    		return;
		} else {
			//close old working scenario order
			for (Volume oldV : workingVolumeList) {
				boolean needClose = true;
				for (Volume newV : tempVolumeList) {
					if (oldV.getScenario().equals(newV.getScenario())) {
						newV.setTrend(oldV.getTrend()); //save trend
						needClose = false;
						break;
					}
				}
				if(needClose) {
					for (ScenarioTrend activeS : getActiveScenarioGroupList()) {
						if(activeS.getScenario().equals(oldV.getScenario()) && 
								activeS.getTrend() != SystemEnum.Trend.Default) {
							closeOrderByScenario(oldV.getScenario());
							break;
						}
					}
				}
			}
		}
    	
    	//update working scenario memory
    	workingScenarioList.clear();
    	for (Volume workingVolume : tempVolumeList) {
			workingVolumeList.add(workingVolume);
		}
		newVolPlanRefreshed();
	}
	
	//update when timer called
    public void updateWorkingScenarioListByRefreshPlan() {
    	
    	if (getActiveScenarioGroupList().size() == 0) {
    		workingScenarioList.clear();
    		
    		return;
    	}
    	
    	CommonDAO commonDao = CommonDAOFactory.getCommonDAO();
    	
    	//get new working scenarios
    	ArrayList<Scenario> newScenarioList = commonDao.getAllWorkingScenarioAtTime(new Date());
    	if (newScenarioList.size() == 0) {
    		
    		workingScenarioList.clear();
    		newScePlanRefreshed();
    		return;
		}
    	
    	//get active working scenarios
    	ArrayList<Scenario> tempScenarioList = new ArrayList<>();
    	for (Scenario newS : newScenarioList) {
    		for (ScenarioTrend activeS : getActiveScenarioGroupList()) {
    			if (newS.getScenario().equals(activeS.getScenario())) {
    				tempScenarioList.add(newS);
    				break;
				}
    		}
		}
    	if (tempScenarioList.size() == 0) {
    		//none new scenario
    		workingScenarioList.clear();
    		newScePlanRefreshed();
    		return;
		} else {
			
			for (Scenario oldS : workingScenarioList) {
				
				for (Scenario newS : tempScenarioList) {
					if (oldS.getScenario().equals(newS.getScenario())) {
						newS.setTrend(oldS.getTrend()); //save trend
						break;
					}
				}
			}
		}
    	
    	//add area to new scenario
    	for (Scenario s : tempScenarioList) {
    		
    		ArrayList<Area> areaList  = 
    				commonDao.getAreaListWithoutZoneByScenario(s.getScenario(),new Date());
    		for (Area area : areaList) {
    			ArrayList<String> zones = commonDao.getOnlyActiveZoneListByScenarioArea(area.getScenario(),area.getStartTime(), area.getArea());
    			area.setZoneList(zones);
			}
    		s.setAreaList(areaList);
		}
    	
    	//get new related zone by new scenario
    	ArrayList<Zone> relatedZones = commonDao.getRelatedZoneListByScenarioList(tempScenarioList);
    	
    	//update working scenario memory
    	workingScenarioList.clear();
    	ZoneColorInfoService zcService = ZoneColorInfoService.getInstance();
    	zcService.reloadSceZoneColorsByNewZoneListWithDefaultColor(relatedZones);
    	for (Scenario workingScenario : tempScenarioList) {
			workingScenarioList.add(workingScenario);
		}
		newScePlanRefreshed();
    }
	
    public void updateRelatedVolZone() {
    	
    	ZoneColorInfoService zService = ZoneColorInfoService.getInstance();
    	if (!zService.getVolZoneColors().isEmpty()) {
    		zService.getVolZoneColors().clear();
		}
    	
    	ArrayList<String> yellowZone = new ArrayList<String>();
    	for(Zone vBar : zService.getVolumeZoneList()) {
    		if(vBar.getColor() == SystemEnum.Color.Yellow) {
    			yellowZone.add(vBar.getZone());
    		}
    	}
    	if(yellowZone.size() == 0) return;
    	
    	for(Volume vol : getWorkingVolumeList()) {
    		
    		int activeColume = (vol.getColumn() == 0 ? yellowZone.size():vol.getColumn());
    		for(int i = 0; i < activeColume; i ++) {
    			
    			String yz = yellowZone.get(yellowZone.size()-1-i);
    			for(String row : vol.getRows()) {
    				Zone relatedZone = Util.getRelatedZoneWithVolBarAndRow(yz,row,true);

    		    	if(!zService.getVolZoneColors().containsKey(relatedZone.getZone())) {
    		    		zService.getVolZoneColors().put(relatedZone.getZone(), relatedZone);
    		    	}
    			}
    		}
    	}
    	
    }
    
    public void exportTodayTrendProfit() {
    	
    	//checkout sign records
    	ArrayList<String> sheetList = new ArrayList<String>();
    	ArrayList<Map<String, List<String>>> mapList = new ArrayList<Map<String, List<String>>>();
    	int trendCount = 0;
    	for (ScenarioTrend s : getActiveScenarioGroupList()) {
    		sheetList.add(s.getScenario());
    		Map<String, List<String>> recordMap = getTrendRecordWithProfit(s.getScenario());
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
        
    }
    
    public void checkScenarioGroupTrend () {
    	
    	checkVolumeTrend();
    	checkScenarioTrend();
    	
    	for(ScenarioTrend groupTrend : getActiveScenarioGroupList()) {
    		
    		Enum<SystemEnum.Trend> volTrend = SystemEnum.Trend.Default;
    		boolean volWorking = false;
    		Volume matchVol = null;
    		for(Volume vol : getWorkingVolumeList()) {
    			
    			if(vol.getScenario().equals(groupTrend.getScenario())) {
    				matchVol = vol;
    				volWorking = true;
    				volTrend = vol.getTrend();
    				break;
    			}
    		}
    		
    		if(!volWorking) {
    			groupTrend.setTrend(SystemEnum.Trend.Default);
    			continue;
    		}
    		
    		Enum<SystemEnum.Trend> sceTrend = SystemEnum.Trend.Default;
    		boolean sceWorking = false;
    		Scenario matchSce = null;
    		for(Scenario sce : getWorkingScenarioList()) {
    			
    			if(sce.getScenario().equals(groupTrend.getScenario())) {
    				matchSce = sce;
    				sceWorking = true;
    				sceTrend = sce.getTrend();
    				break;
    			}
    		}
    		
    		if(sceWorking) {
    			
    			if(volTrend == sceTrend && volTrend != groupTrend.getTrend()) {
    				//trend change
    				groupTrend.setTrend(volTrend);
    				ArrayList<Scenario> ss = new ArrayList<Scenario>();
    				ss.add(matchSce);
    				ArrayList<Zone> zones = CommonDAOFactory.getCommonDAO().getRelatedZoneListByScenarioList(ss);
    				int scenarioGreen = getGreenCountBySceZoneList(zones);
    				int scenarioRed = getRedCountBySceZoneList(zones);
    				
    				System.out.println("volTrend:" + Util.getTrendTextByEnum(volTrend) +" sceTrend"+Util.getTrendTextByEnum(sceTrend) +" groupTrend:"+Util.getTrendTextByEnum(groupTrend.getTrend()));
    				
    				pushNewTrendSign(groupTrend.getScenario(),groupTrend.getTrend(),scenarioGreen+matchVol.getGreen(),scenarioRed+matchVol.getRed());
    			}
    			
    		} else {
    			
    			if(volTrend != groupTrend.getTrend()) {
    				//trend change
    				groupTrend.setTrend(volTrend);
    				System.out.println("volTrend:" + Util.getTrendTextByEnum(volTrend) +" sceTrend"+Util.getTrendTextByEnum(sceTrend) +" groupTrend:"+Util.getTrendTextByEnum(groupTrend.getTrend()));
    				
    				pushNewTrendSign(groupTrend.getScenario(),groupTrend.getTrend(),matchVol.getGreen(),matchVol.getRed());
    			}
    		}
    	} 
    }
    
    
    private void checkVolumeTrend() {
    	
    	ZoneColorInfoService zService = ZoneColorInfoService.getInstance();
    	
    	ArrayList<String> yellowZone = new ArrayList<String>();
    	for(Zone vBar : zService.getVolumeZoneList()) {
    		if(vBar.getColor() == SystemEnum.Color.Yellow) {
    			yellowZone.add(vBar.getZone());
    		}
    	}
    	if(yellowZone.size() == 0) return;
    	
    	for(Volume vol : getWorkingVolumeList()) {
    		
    		if(vol.getColumn() > yellowZone.size()) continue;
    		
    		boolean trendAppear = false;
    		int volGreen = 0;
			int volRed = 0;
			
			int activeColume = (vol.getColumn() == 0 ? yellowZone.size():vol.getColumn());
    		
			for(int i = 0; i < activeColume; i ++) {
    			
    			String yz = yellowZone.get(yellowZone.size()-1-i);
    			for(String row : vol.getRows()) {
    				Zone relatedZone = Util.getRelatedZoneWithVolBarAndRow(yz,row,false);
    				Enum<SystemEnum.Color> c = zService.getColorByVolZone(relatedZone.getZone());
					if (c == SystemEnum.Color.Green) {volGreen++;}			
					if (c == SystemEnum.Color.Red) {volRed++;}
    			}
    		}
    		
			vol.setGreen(volGreen);
			vol.setRed(volRed);
			
			Enum<SystemEnum.Color> newColor = SystemEnum.Color.Default;
			
    		if ((volGreen + volRed == activeColume*vol.getRows().size() && volGreen > volRed && volRed <= vol.getPercent()) ||
				    (volGreen + volRed == activeColume*vol.getRows().size() && volRed > volGreen && volGreen <= vol.getPercent())) {
					
    			trendAppear = true;
			}
				
			if(volGreen > volRed) {
				newColor = SystemEnum.Color.Green;
			} else {
				newColor = SystemEnum.Color.Red;
			}
    		
			if (trendAppear) {

				if(newColor == SystemEnum.Color.Green && vol.getTrend() != SystemEnum.Trend.Up) {
					vol.setTrend(SystemEnum.Trend.Up);
					System.out.println(vol.getScenario()+" vol trendAppear:"+Util.getTrendTextByEnum(vol.getTrend()) + " green:" + volGreen + " red:" + volRed);
				}
				if(newColor == SystemEnum.Color.Red && vol.getTrend() != SystemEnum.Trend.Down) {
					vol.setTrend(SystemEnum.Trend.Down);
					System.out.println(vol.getScenario()+" vol trendAppear:"+Util.getTrendTextByEnum(vol.getTrend()) + " green:" + volGreen + " red:" + volRed);
				}
			}
			
			
    	}
    }
    
    private void checkScenarioTrend() {
    	
    	ZoneColorInfoService colorService = ZoneColorInfoService.getInstance();
    	
    	for (Scenario scenario : getWorkingScenarioList()) {
    		
    		boolean trendAppear = true;
    		
    		Enum<SystemEnum.Color> preColor = SystemEnum.Color.Default;
    		Enum<SystemEnum.Color> thisColor = SystemEnum.Color.Default;
			for (Area area : scenario.getAreaList()) {
				int areaGreen = 0;
				int areaRed = 0;
				for (String zone : area.getZoneList()) {
					Enum<SystemEnum.Color> c = colorService.getColorBySceZone(zone);
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
				
				if(preColor == SystemEnum.Color.Green && scenario.getTrend() != SystemEnum.Trend.Up) {
					scenario.setTrend(SystemEnum.Trend.Up);
				}
				if(preColor == SystemEnum.Color.Red && scenario.getTrend() != SystemEnum.Trend.Down) {
					scenario.setTrend(SystemEnum.Trend.Down);
				}
			}
		}
	}
    
    public void pushNewTrendSign (String scenario, Enum<SystemEnum.Trend> trend, int green, int red) {
    	
    	CommonDAO commonDao = CommonDAOFactory.getCommonDAO();
    	
    	//swim price
    	Rectangle rect = commonDao.getRectByName("swim_price");
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
    	
    	ArrayList<TrendSign> dailySignList = getDailySignMap().get(scenario);
    	dailySignList.add(newSign);
    	
    	commonDao.insertNewTrendSign(newSign);
    	
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
    
    public Enum<SystemEnum.Trend> getTodayLastTrendByScenario(String scenario) {
    	return CommonDAOFactory.getCommonDAO().getLastTrendByScenario(new Date(), scenario);
    }
    
	public static ScenarioGroupService getInstance() {  
		if (instance == null) {  
			synchronized (ScenarioGroupService.class) {  
				if (instance == null) {  
					instance = new ScenarioGroupService();  
				}	  
			}  
		}  
		return instance;  
	}

	public ArrayList<ScenarioTrend> getActiveScenarioGroupList() {
		return activeScenarioGroupList;
	}

	public void setActiveScenarioGroupList(ArrayList<ScenarioTrend> activeScenarioGroupList) {
		this.activeScenarioGroupList = activeScenarioGroupList;
	}

	public ArrayList<Volume> getWorkingVolumeList() {
		return workingVolumeList;
	}

	public void setWorkingVolumeList(ArrayList<Volume> workingVolumeList) {
		this.workingVolumeList = workingVolumeList;
	}

	public ArrayList<Scenario> getWorkingScenarioList() {
		return workingScenarioList;
	}

	public void setWorkingScenarioList(ArrayList<Scenario> workingScenarioList) {
		this.workingScenarioList = workingScenarioList;
	}

	public ArrayList<DailyScenarioRefresh> getSceRefreshPlan() {
		return sceRefreshPlan;
	}

	public void setSceRefreshPlan(ArrayList<DailyScenarioRefresh> sceRefreshPlan) {
		this.sceRefreshPlan = sceRefreshPlan;
	}

	public int getPassedSceRefreshPlanCount() {
		return passedSceRefreshPlanCount;
	}

	public void setPassedSceRefreshPlanCount(int passedSceRefreshPlanCount) {
		this.passedSceRefreshPlanCount = passedSceRefreshPlanCount;
	}

	public ArrayList<DailyScenarioRefresh> getVolRefreshPlan() {
		return volRefreshPlan;
	}

	public void setVolRefreshPlan(ArrayList<DailyScenarioRefresh> volRefreshPlan) {
		this.volRefreshPlan = volRefreshPlan;
	}

	public int getPassedVolRefreshPlanCount() {
		return passedVolRefreshPlanCount;
	}

	public void setPassedVolRefreshPlanCount(int passedVolRefreshPlanCount) {
		this.passedVolRefreshPlanCount = passedVolRefreshPlanCount;
	}

	public Map<String,ArrayList<TrendSign>> getDailySignMap() {
		return dailySignMap;
	}

	public void setDailySignMap(Map<String,ArrayList<TrendSign>> dailySignMap) {
		this.dailySignMap = dailySignMap;
	}
	

}
