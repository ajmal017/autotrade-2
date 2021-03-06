package service;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import application.AutoTradeWithVol;
import config.SystemConfig;
import dao.CommonDAO;
import dao.CommonDAOFactory;
import entity.Area;
import entity.ColorCount;
import entity.DailyScenarioRefresh;
import entity.Scenario;
import entity.ScenarioTrend;
import entity.TrendSign;
import entity.Volume;
import entity.Zone;
import systemenum.SystemEnum;
import tool.Util;

public class ScenarioGroupService implements IBServiceCallbackInterface {
	
	private volatile static ScenarioGroupService instance;
	
	private ArrayList<ScenarioTrend> activeScenarioGroupList;

	private ArrayList<Volume> workingVolumeList;
	private ArrayList<Scenario> workingScenarioList;
	 
	private ArrayList<DailyScenarioRefresh> volRefreshPlan;
	private int passedVolRefreshPlanCount = 0;
	private ArrayList<DailyScenarioRefresh> sceRefreshPlan;
	private int passedSceRefreshPlanCount = 0; 
	private ArrayList<DailyScenarioRefresh> volZoneRefreshPlan;
	private int passedVolZoneRefreshPlanCount = 0; 
	
	private Map<String,ArrayList<TrendSign>> dailySignMap;
	
	private boolean needCloseApp;
	private AutoTradeWithVol autoTradeObj;
	
	private int yellowZoneCount;
	private int wantCloseOrderCount;
	
	private ScenarioGroupService ()  {
    	
		this.activeScenarioGroupList = new ArrayList<ScenarioTrend>();
		
    	this.workingScenarioList = new ArrayList<Scenario>();
    	this.workingVolumeList = new ArrayList<Volume>();
    	
    	this.sceRefreshPlan = new ArrayList<DailyScenarioRefresh>();
    	this.volRefreshPlan = new ArrayList<DailyScenarioRefresh>();
    	this.volZoneRefreshPlan = new ArrayList<DailyScenarioRefresh>();
    	
    	this.dailySignMap = new HashMap<String, ArrayList<TrendSign>>();
    	
    	initAllScenarioGroupData();
    }
	
	private void initAllVolumeZoneData() {
		
		CommonDAO commonDao = CommonDAOFactory.getCommonDAO();
		
		ArrayList<String> times = commonDao.getAllDistinctVolumeZoneStartTime();
    	if (times.size() == 0) {
			
    		return;
		}
    	
    	for (String d : times) {
    		
    		DailyScenarioRefresh refresh = new DailyScenarioRefresh();
    		refresh.setRefreshTime(d);
    		
    		StringBuilder str = new StringBuilder(Util.getDateStringByDateAndFormatter(new Date(), "yyyyMMdd"));
    		str.append(d);
    		Date dDate = Util.getDateByStringAndFormatter(str.toString(), "yyyyMMddHH:mm:ss");
    		
    		if (dDate.before(new Date())) {
    			refresh.setPassed(true); 
    			int passed = getPassedVolZoneRefreshPlanCount() + 1;
    			setPassedVolZoneRefreshPlanCount(passed);
			} else {
				refresh.setPassed(false);
			}
    		getVolZoneRefreshPlan().add(refresh);	
		}
		
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
    		Date dDate = Util.getDateByStringAndFormatter(str.toString(), "yyyyMMddHH:mm:ss");
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
    		Date dDate = Util.getDateByStringAndFormatter(str.toString(), "yyyyMMddHH:mm:ss");
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
    	
    	for (String nameString : sceNames) {
    		ScenarioTrend st = new ScenarioTrend(nameString);
    		Enum<SystemEnum.Trend> lastTrend = getTodayLastTrendByScenario(nameString);
        	st.setTrend(lastTrend);
        	getActiveScenarioGroupList().add(st);
        	getDailySignMap().put(nameString, new ArrayList<TrendSign>());
		}
    	
    	initAllVolumeZoneData();
		initAllVolumeData();
		initAllScenarioData();
	}
	
	private void newVolZonePlanRefreshed() {
    	
    	DailyScenarioRefresh refresh = getVolZoneRefreshPlan().get(getPassedVolZoneRefreshPlanCount());
		refresh.setPassed(true);
		setPassedVolZoneRefreshPlanCount(getPassedVolZoneRefreshPlanCount()+1);
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
		    if(sign.getWhiteCount()>0) {
		    	params.add(sign.getWhiteCount()+"");
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
		    if(sign.getQuantity()!=0) {
		    	params.add(sign.getQuantity()+"");
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
        String[] strArray = { "time", "scenario", "trend", "green", "red", "white", "price_swim", "price_ib", "profit_swim", "profit_ib", "quantity", "desc"};
        return strArray;
    }
	    
    private ColorCount getColorCountBySceZoneList(ArrayList<Zone> zoneList) {

    	ColorCount count =  new ColorCount();
    	ZoneColorInfoService colorService = ZoneColorInfoService.getInstance();
    	for (Zone zone : zoneList) {
    		Enum<SystemEnum.Color> c = colorService.getColorBySceZone(zone.getZone());
    		if (c == SystemEnum.Color.Green) {
    			count.setGreen(count.getGreen()+1);
    		} else if (c == SystemEnum.Color.Red) {
    			count.setRed(count.getRed()+1);
    		} else if (c == SystemEnum.Color.White) {
    			count.setWhite(count.getWhite()+1);
    		} else if (c == SystemEnum.Color.Yellow) {
    			count.setYellow(count.getYellow()+1);
    		} else {
    			count.setOther(count.getOther()+1);
    		}
    	}
    	return count;
    }
	
	public void closeOrderByScenario(String scenario) {
    	
    	pushNewTrendSign(scenario, SystemEnum.Trend.Default, 0, 0, 0);
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
    		
    		workingVolumeList.clear();
    		newVolPlanRefreshed();
    		return;
		} else {
			//close old working scenario order
			for (Volume oldV : workingVolumeList) {
				for (Volume newV : tempVolumeList) {
					if (oldV.getScenario().equals(newV.getScenario())) {
						newV.setTrend(oldV.getTrend()); //save trend
						break;
					}
				}
			}
		}
    	
    	//update working scenario memory
    	workingVolumeList.clear();
    	for (Volume workingVolume : tempVolumeList) {
			workingVolumeList.add(workingVolume);
		}
		newVolPlanRefreshed();
	}
	
	public void updateWorkingVolumeZoneListByRefreshPlan() {

    	if (getActiveScenarioGroupList().size() == 0) {
    		workingScenarioList.clear();
    		
    		return;
    	}
    	
    	CommonDAO commonDao = CommonDAOFactory.getCommonDAO();
    	
    	//get new working volumes
    	DailyScenarioRefresh refresh = getVolZoneRefreshPlan().get(passedVolZoneRefreshPlanCount);
    	ArrayList<Zone> newZones = commonDao.getVolumeZoneList(refresh.getRefreshTime());
    	if (newZones.size() == 0) {
    		
    		ZoneColorInfoService.getInstance().getVolumeZoneList().clear();
    		workingVolumeList.clear();
    		newVolPlanRefreshed();
    		return;
		}

    	ZoneColorInfoService.getInstance().loadVolumeBarZoneListWithDefaultColor(newZones);
		newVolZonePlanRefreshed();
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
    	
    	//new
    	int yellowCount = 0;
    	for (Zone vBar : zService.getVolumeZoneList()) {
    		if(vBar.getColor() == SystemEnum.Color.Yellow) {
    			yellowCount++;
    			for (int i = 0; i < SystemConfig.ZONE_Y.length;i++) {
    				Zone relatedZone = Util.getRelatedZoneWithVolBarAndRow(vBar.getZone(),String.valueOf(i+1),true);
    				zService.getVolZoneColors().put(relatedZone.getZone(), relatedZone);
    			}
    			
    		}
    	}
    	setYellowZoneCount(yellowCount);
    	//new end

    	/* 
    	//old
    	ArrayList<String> yellowZone = new ArrayList<String>();
    	for (Zone vBar : zService.getVolumeZoneList()) {
    		if(vBar.getColor() == SystemEnum.Color.Yellow) {
    			yellowZone.add(vBar.getZone());
    		}
    	}
    	setYellowZoneCount(yellowZone.size());
    	if(yellowZone.size() == 0) return;
    	
    	for (Volume vol : getWorkingVolumeList()) {
    		
    		if(vol.getColumn() > yellowZone.size()) {
    			System.out.println("updateRelatedVolZone vol.getColumn()" +vol.getColumn()+ " > yellowZone.size()"+yellowZone.size());
    			continue;
    		}
    		
    		int activeColume;
    		if (vol.getColumn() == 0) {
    			activeColume = yellowZone.size();
    		} else {
    			activeColume = vol.getColumn();
    		}
    		for (int i = 0; i < activeColume; i ++) {
    			
    			try {
    				String yz = yellowZone.get(yellowZone.size()-1-i);
    				for (String row : vol.getRows()) {
    					Zone relatedZone = Util.getRelatedZoneWithVolBarAndRow(yz,row,true);

    					if(!zService.getVolZoneColors().containsKey(relatedZone.getZone())) {
    						zService.getVolZoneColors().put(relatedZone.getZone(), relatedZone);
    					}
    				}
    			}  catch (Exception e) {
					System.out.println("Exception vol:"+vol.getScenario()+" activeColume:"+activeColume+" i:"+i);
					e.printStackTrace();
				}
    		}
    	}
    	//old end
    	*/
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
    
    public void closeAllSceWhenAppWantClose() {
    	
    	ArrayList<String> sces = new ArrayList<String>();
    	for(ScenarioTrend st : getActiveScenarioGroupList()) {
    		if(st.getTrend() != SystemEnum.Trend.Default) {
    			sces.add(st.getScenario());
    		}
    	}
    	
    	if(sces.size() > 0) {
    		setNeedCloseApp(true);
    		wantCloseOrderCount = sces.size();
    		for(String s : sces) {
    			closeOrderByScenario(s);
    		}
    	} else {
    		if(getAutoTradeObj() != null) {
    			getAutoTradeObj().closeAppAfterPriceUpdate();
    	    }
    	}
    }
    
    
    public void checkScenarioGroupTrend () {
    	
    	checkVolumeTrend();
    	checkScenarioTrend();
    	
    	for (ScenarioTrend groupTrend : getActiveScenarioGroupList()) {
    		
    		Enum<SystemEnum.Trend> volTrend = SystemEnum.Trend.Default;
    		boolean volWorking = false;
    		Volume matchVol = null;
    		for (Volume vol : getWorkingVolumeList()) {
    			
    			if(vol.getScenario().equals(groupTrend.getScenario())) {
    				matchVol = vol;
    				volWorking = true;
    				volTrend = vol.getTrend();
    				break;
    			}
    		}
    		
    		Enum<SystemEnum.Trend> sceTrend = SystemEnum.Trend.Default;
    		boolean sceWorking = false;
    		Scenario matchSce = null;
    		for (Scenario sce : getWorkingScenarioList()) {
    			
    			if(sce.getScenario().equals(groupTrend.getScenario())) {
    				matchSce = sce;
    				sceWorking = true;
    				sceTrend = sce.getTrend();
    				break;
    			}
    		}
    		
    		if(!volWorking && !sceWorking && groupTrend.getTrend() != SystemEnum.Trend.Default) {
    			groupTrend.setTrend(SystemEnum.Trend.Default);
    			closeOrderByScenario(groupTrend.getScenario());
    			continue;
    		}
    		
    		if(sceWorking) {
    			
    			if(volWorking) {
    				if (volTrend == sceTrend && 
        					volTrend != SystemEnum.Trend.Default &&
        					volTrend != groupTrend.getTrend()) {
    					//trend change
    					groupTrend.setTrend(volTrend);
    					
    					ArrayList<Scenario> ss = new ArrayList<Scenario>();
    					ss.add(matchSce);
    					ArrayList<Zone> zones = CommonDAOFactory.getCommonDAO().getRelatedZoneListByScenarioList(ss);
    					ColorCount cc = getColorCountBySceZoneList(zones);
    					pushNewTrendSign(groupTrend.getScenario(),
    							groupTrend.getTrend(),
    							cc.getGreen()+matchVol.getGreen(),
    							cc.getRed()+matchVol.getRed(),
    							cc.getWhite()+matchVol.getWhite());
    				}
    			} else {
    				
    				if(sceTrend != SystemEnum.Trend.Default && sceTrend != groupTrend.getTrend()) {
        				//trend change
        				groupTrend.setTrend(sceTrend);
        				
        				ArrayList<Scenario> ss = new ArrayList<Scenario>();
    					ss.add(matchSce);
    					ArrayList<Zone> zones = CommonDAOFactory.getCommonDAO().getRelatedZoneListByScenarioList(ss);
    					ColorCount cc = getColorCountBySceZoneList(zones);
        				pushNewTrendSign(groupTrend.getScenario(),groupTrend.getTrend(),cc.getGreen(),cc.getRed(),cc.getWhite());
        			}
    			}
    			
    		} else {
    			
    			if(volTrend != SystemEnum.Trend.Default && volTrend != groupTrend.getTrend()) {
    				//trend change
    				groupTrend.setTrend(volTrend);
    				pushNewTrendSign(groupTrend.getScenario(),groupTrend.getTrend(),matchVol.getGreen(),matchVol.getRed(),matchVol.getWhite());
    			}
    		}
    	} 
    }
    
    
    private void checkVolumeTrend() {
    	
    	if(getWorkingVolumeList().size() == 0) return;
    	
    	ZoneColorInfoService zService = ZoneColorInfoService.getInstance();
    	
    	ArrayList<String> yellowZone = new ArrayList<String>();
    	for (Zone vBar : zService.getVolumeZoneList()) {
    		if(vBar.getColor() == SystemEnum.Color.Yellow) {
    			yellowZone.add(vBar.getZone());
    		}
    	}
    	if(yellowZone.size() == 0) return;
    	
    	for (Volume vol : getWorkingVolumeList()) {
    		
    		if(vol.getColumn() > yellowZone.size()) {
    			System.out.println("checkVolumeTrend vol.getColumn()" +vol.getColumn()+ " > yellowZone.size()"+yellowZone.size());
    			continue;
    		}
    		
    		boolean trendAppear = false;
    		int volGreen = 0;
			int volRed = 0;
			int volWhite = 0;
			int activeColume;
    		if (vol.getColumn() == 0) {
    			activeColume = yellowZone.size();
    		} else {
    			activeColume = vol.getColumn();
    		}
    		
			for (int i = 0; i < activeColume; i ++) {
    			
				try {
					String yz = yellowZone.get(yellowZone.size()-1-i);
	    			for (String row : vol.getRows()) {
	    				Zone relatedZone = Util.getRelatedZoneWithVolBarAndRow(yz,row,false);
	    				Enum<SystemEnum.Color> c = zService.getColorByVolZone(relatedZone.getZone());
						if (c == SystemEnum.Color.Green) {volGreen++;}			
						if (c == SystemEnum.Color.Red) {volRed++;}
						if (c == SystemEnum.Color.White) {volWhite++;}
	    			}
				} catch (Exception e) {
					System.out.println("Exception vol:"+vol.getScenario()+" activeColume:"+activeColume+" i:"+i);
					e.printStackTrace();
				}
				
    		}
    		
			vol.setGreen(volGreen);
			vol.setRed(volRed);
			vol.setWhite(volWhite);
			
			Enum<SystemEnum.Color> newColor = SystemEnum.Color.Default;
			
    		if ((volGreen + volRed + volWhite == activeColume*vol.getRows().size() && volGreen > volRed && volRed <= vol.getPercent() && volWhite <= vol.getWhiteMax()) ||
				(volGreen + volRed + volWhite == activeColume*vol.getRows().size() && volRed > volGreen && volGreen <= vol.getPercent() && volWhite <= vol.getWhiteMax())) {
					
    			trendAppear = true;
    			if(volGreen > volRed) {
    				newColor = SystemEnum.Color.Green;
    			} else {
    				newColor = SystemEnum.Color.Red;
    			}
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
			} else {
				vol.setTrend(SystemEnum.Trend.Default);
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
				int areaWhite = 0;
				for (String zone : area.getZoneList()) {
					Enum<SystemEnum.Color> c = colorService.getColorBySceZone(zone);
					if (c == SystemEnum.Color.Green) {areaGreen++;}			
					if (c == SystemEnum.Color.Red) {areaRed++;}
					if (c == SystemEnum.Color.White) {areaWhite++;}
				}
				
				if(areaGreen + areaRed + areaWhite == area.getZoneList().size()) {
					
					if ((areaGreen > areaRed && areaGreen > area.getPercent()) || 
					    (areaRed > areaGreen && areaRed > area.getPercent()) ||
					    (areaGreen > areaRed && areaGreen == area.getPercent() && areaWhite >= area.getWhiteMin()) ||
					    (areaRed > areaGreen && areaRed == area.getPercent() && areaWhite >= area.getWhiteMin())) {
						
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
					
				} else {
					trendAppear = trendAppear & false;
				}
				
				
				/*
				if ((areaGreen + areaRed + areaWhite== area.getZoneList().size() && areaGreen > areaRed && areaGreen >= area.getPercent()) ||
				    (areaGreen + areaRed + areaWhite== area.getZoneList().size() && areaRed > areaGreen && areaRed >= area.getPercent())) {
					
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
				*/
				
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
			} else {
				scenario.setTrend(SystemEnum.Trend.Default);
			}
		}
	}
    
    public void pushNewTrendSign (String scenario, Enum<SystemEnum.Trend> trend, int green, int red, int white) {
    	
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
    	
    	Date now = new Date();
    	String nowTimeStr = Util.getDateStringByDateAndFormatter(now, "HH:mm:ss");
    	
    	TrendSign newSign = new TrendSign(now, scenario, trend, green, red, white, priceSwim, 0, 0, "", 0, 0);
    	ArrayList<TrendSign> dailySignList = getDailySignMap().get(scenario);
    	dailySignList.add(newSign);
    	commonDao.insertNewTrendSign(newSign);
    	
    	IBService ibService = IBService.getInstance();
    	if(ibService.getIbApiConfig().isActive() && ibService.isIBConnecting()) {
    		
    		if (trend == SystemEnum.Trend.Default) { //close
    			
    			ibService.closeTodayTrade(scenario, nowTimeStr);
    			
    			//test only T10
    			if(isNeedCloseApp() && wantCloseOrderCount > 0 && !scenario.equals(SystemConfig.TRADE_SCENARIO)) wantCloseOrderCount--;
    			
    		} else {
    			
    			Enum<SystemEnum.OrderAction> newAction = SystemEnum.OrderAction.Default;
    			if (trend == SystemEnum.Trend.Up) {
    				newAction = SystemEnum.OrderAction.Buy;
    			} else {
    				newAction = SystemEnum.OrderAction.Sell;
    			}
    			
    			ibService.placeOrder(newAction, scenario, nowTimeStr);
    		}
    		
    	} else {
    		
    		if (isNeedCloseApp() && wantCloseOrderCount > 0) wantCloseOrderCount--;
    	}
    	
    	ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
	    cachedThreadPool.execute(new Runnable() {
	  
	        @Override
	        public void run() {
	        	//screen shot
            	String shotPath = SystemConfig.DOC_PATH + "//screenshot//" + 
            					  Util.getDateStringByDateAndFormatter(new Date(), "yyyyMMdd") + "//"+ 
            					  scenario + "//" + 
            					  scenario + "_" + 
            					  Util.getDateStringByDateAndFormatter(new Date(), "HHmmss") + "_" +
            					  Util.getTrendTextByEnum(trend) +".png";
            	File newFile = new File(shotPath);
    			if(!newFile.exists()) {
                	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                	Rectangle screenRectangle = new Rectangle(screenSize);
    				Util.createScreenShotByRect(screenRectangle, shotPath, "png");
    			}
	        }
	    });
	    
	    if(wantCloseOrderCount == 0 && getAutoTradeObj() != null && isNeedCloseApp()) {

			setNeedCloseApp(false);
			getAutoTradeObj().closeAppAfterPriceUpdate();
	    }
    }
    
    public Enum<SystemEnum.Trend> getTodayLastTrendByScenario(String scenario) {
    	return CommonDAOFactory.getCommonDAO().getLastTrendByScenario(new Date(), scenario);
    }
    
	public static ScenarioGroupService getInstance() {  
		if (instance == null) {  
			synchronized (ScenarioGroupService.class) {  
				if (instance == null) {  
					instance = new ScenarioGroupService();
					IBService.getInstance().setGroupServiceObj(instance);
				}	  
			}  
		}  
		return instance;  
	}
	

	@Override
	public void updateTradePrice(double price, String preOrderScenario, String preOrderTime, int preQuantity) {
		System.out.println("Scenario group service updateTradePrice:"+price+" preOrderScenario:"+preOrderScenario+" preOrderTime:"+preOrderTime);
		CommonDAOFactory.getCommonDAO().updateLastTrendSignIBPrice(preOrderScenario, preOrderTime, price, preQuantity);
		if(isNeedCloseApp() && wantCloseOrderCount > 0) wantCloseOrderCount--;
		if(isNeedCloseApp() && getAutoTradeObj() != null && wantCloseOrderCount == 0) {
			setNeedCloseApp(false);
			getAutoTradeObj().closeAppAfterPriceUpdate();
	    }
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

	public boolean isNeedCloseApp() {
		return needCloseApp;
	}

	public void setNeedCloseApp(boolean needCloseApp) {
		this.needCloseApp = needCloseApp;
	}

	public AutoTradeWithVol getAutoTradeObj() {
		return autoTradeObj;
	}

	public void setAutoTradeObj(AutoTradeWithVol autoTradeObj) {
		this.autoTradeObj = autoTradeObj;
	}

	public int getYellowZoneCount() {
		return yellowZoneCount;
	}

	public void setYellowZoneCount(int yellowZoneCount) {
		this.yellowZoneCount = yellowZoneCount;
	}

	public ArrayList<DailyScenarioRefresh> getVolZoneRefreshPlan() {
		return volZoneRefreshPlan;
	}

	public void setVolZoneRefreshPlan(ArrayList<DailyScenarioRefresh> volZoneRefreshPlan) {
		this.volZoneRefreshPlan = volZoneRefreshPlan;
	}

	public int getPassedVolZoneRefreshPlanCount() {
		return passedVolZoneRefreshPlanCount;
	}

	public void setPassedVolZoneRefreshPlanCount(int passedVolZoneRefreshPlanCount) {
		this.passedVolZoneRefreshPlanCount = passedVolZoneRefreshPlanCount;
	}
	

}
