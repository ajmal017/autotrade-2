package service;

import java.util.ArrayList;
import java.util.Date;

import DAO.ScenarioDAO;
import DAO.ScenarioDAOFactory;
import DAO.ZoneDAO;
import DAO.ZoneDAOFactory;
import entity.DailyScenarioRefresh;
import tool.Util;
import entity.*;

public class ScenarioService {
	
    private volatile static ScenarioService instance;
    
    //active scenario names
    private ArrayList<String> activeScenarioList;
    //scenario refresh plan
    private ArrayList<DailyScenarioRefresh> sceRefreshPlan;
    private int passedRefreshPlanCount = 0; 
    //current working scenario list, refresh by sceRefreshPlan 
    private ArrayList<Scenario> workingScenarioList;
    
    
    //init 
    private ScenarioService ()  {
    	
    	this.activeScenarioList = new ArrayList<String>();
    	this.sceRefreshPlan = new ArrayList<DailyScenarioRefresh>();
    	this.workingScenarioList = new ArrayList<Scenario>();
    	initAllScenarioData();
    } 
    
    public static ScenarioService getInstance() {  
    	if (instance == null) {  
    		synchronized (ScenarioService.class) {  
    			if (instance == null) {  
    				instance = new ScenarioService();  
    			}	  
    		}  
    	}  
    	return instance;  
    } 
    
    //init active scenario and refresh plan
    private void initAllScenarioData() {
    	
    	ScenarioDAO scenarioDao = ScenarioDAOFactory.getScenarioDAO();
    	
    	//get all scenario names and set active = true;
    	ArrayList<String> sceNames = scenarioDao.getAllActiveScenarioName();
    	if (sceNames.size() == 0) {
			//none active scenario
    		return;
		}
    	
    	for (String nameString : sceNames) {
        	getActiveScenarioList().add(nameString);
		}
    	
    	//create scenario start time and end time
    	//create scenario  refresh plan
    	ArrayList<String> times = scenarioDao.getAllDistinctScenarioStartTimeAndEndTime();
    	if (times.size() == 0) {
			//当日没有任务。
    		return;
		}
    	
    	for (String d : times) {
    		
    		DailyScenarioRefresh refresh = new DailyScenarioRefresh();
    		refresh.setRefreshTime(d);
    		StringBuilder str = new StringBuilder(Util.getDateStringByDateAndFormatter(new Date(), "yyyyMMdd"));
    		str.append(d);
    		Date dDate = Util.getDateByStringAndFormatter(str.toString(), "yyyyMMddHH:mm");
    		if (dDate.before(new Date())) {
    			refresh.setPassed(true); //需要把已经过去的时间节点打上标记（如果在开盘后启动)
    			int passed = getPassedRefreshPlanCount() + 1;
    			setPassedRefreshPlanCount(passed);
			} else {
				refresh.setPassed(false);
			}
    		getSceRefreshPlan().add(refresh);	
		}
	}
    
    //update when timer called
    public void updateWorkingScenarioListByRefreshPlan() {
    	
    	if (getActiveScenarioList().size() == 0) {
    		workingScenarioList.clear();
    		newPlanRefreshed();
    		return;
    	}
    	
    	ScenarioDAO scenarioDao = ScenarioDAOFactory.getScenarioDAO();
    	ZoneDAO zoneDAO = ZoneDAOFactory.getZoneDAO();
    	
    	//get new working scenarios
    	ArrayList<Scenario> newScenarioList = scenarioDao.getAllWorkingScenarioAtTime(new Date());
    	if (newScenarioList.size() == 0) {
    		for (Scenario scenario : workingScenarioList) {
				closeOrderByScenario(scenario.getScenario());
			}
    		workingScenarioList.clear();
    		newPlanRefreshed();
    		return;
		}
    	
    	//get active working scenarios
    	ArrayList<Scenario> tempScenarioList = new ArrayList<>();
    	for (Scenario newS : newScenarioList) {
    		for (String workingS : getActiveScenarioList()) {
    			if (newS.getScenario().equals(workingS)) {
    				tempScenarioList.add(newS);
				}
    		}
		}
    	if (tempScenarioList.size() == 0) {
    		//none new scenario 关闭所有相应订单
    		for (Scenario scenario : workingScenarioList) {
				closeOrderByScenario(scenario.getScenario());
			}
    		workingScenarioList.clear();
    		newPlanRefreshed();
    		return;
		} else {
			//close old working的scenario的订单
			for (Scenario oldS : workingScenarioList) {
				boolean needClose = true;
				for (Scenario newS : tempScenarioList) {
					if (oldS.getScenario().equals(newS.getScenario())) {
						newS.setTrend(oldS.getTrend()); //save trend
						needClose = false;
						break;
					}
				}
				if(needClose) closeOrderByScenario(oldS.getScenario());
			}
		}
    	
    	//add area to new scenario
    	for (Scenario s : tempScenarioList) {
    		
    		ArrayList<Area> areaList  = 
    				scenarioDao.getAreaListWithoutZoneByScenario(s.getScenario(),new Date());
    		for (Area area : areaList) {
    			ArrayList<String> zones = zoneDAO.getOnlyActiveZoneListByScenarioArea(area.getScenario(),area.getStartTime(), area.getArea());
    			for (String z : zones) {
    				area.getZoneList().add(z);
    			}
			}
    		s.setAreaList(areaList);
		}
    	
    	//get new related zone by new scenario
    	ArrayList<Zone> relatedZones = zoneDAO.getRelatedZoneListByScenarioList(tempScenarioList);
    	
    	//update working scenario memory
    	workingScenarioList.clear();
    	ZoneColorInfoService zcService = ZoneColorInfoService.getInstance();
    	zcService.reloadZoneColorsByNewZoneListWithDefaultColor(relatedZones);
    	for (Scenario workingScenario : tempScenarioList) {
			workingScenarioList.add(workingScenario);
		}
		newPlanRefreshed();
    }
    
    //update flag in refresh plan
    private void newPlanRefreshed() {
    	
    	DailyScenarioRefresh refresh = getSceRefreshPlan().get(getPassedRefreshPlanCount());
		refresh.setPassed(true);
		setPassedRefreshPlanCount(getPassedRefreshPlanCount()+1);
    }
    
    //maybe update everyday
    public void reloadAllScenarioIfNeeded() {
    	
    	getActiveScenarioList().clear();
    	getSceRefreshPlan().clear();
    	setPassedRefreshPlanCount(0);
    	getWorkingScenarioList().clear();
    	initAllScenarioData();
    }
    
    public boolean activeScenarioDidChanged() {
    	
    	ArrayList<String> oldS = getActiveScenarioList();

    	ScenarioDAO scenarioDao = ScenarioDAOFactory.getScenarioDAO();
    	ArrayList<String> newS = scenarioDao.getAllActiveScenarioName();
    	
    	if (oldS.size() != newS.size()) {
			return true;
		} else {
			if (oldS.size() == 0) {
				return false;
			} else {
				for (int i = 0; i < oldS.size(); i++) {
					if (!oldS.get(i).equals(newS.get(i))) {
						return true;
					}
				}
				return false;
			}
		}
    }
    
    private void closeOrderByScenario(String scenario) {
    	
    	//todo
    }
    
    /* getter setter */

	public ArrayList<DailyScenarioRefresh> getSceRefreshPlan() {
		return sceRefreshPlan;
	}

	public void setSceRefreshPlan(ArrayList<DailyScenarioRefresh> sceRefreshPlan) {
		this.sceRefreshPlan = sceRefreshPlan;
	}

	public ArrayList<Scenario> getWorkingScenarioList() {
		return workingScenarioList;
	}

	public void setWorkingScenarioList(ArrayList<Scenario> workingScenarioList) {
		this.workingScenarioList = workingScenarioList;
	}

	public int getPassedRefreshPlanCount() {
		return passedRefreshPlanCount;
	}

	public void setPassedRefreshPlanCount(int passedRefreshPlanCount) {
		this.passedRefreshPlanCount = passedRefreshPlanCount;
	}

	public ArrayList<String> getActiveScenarioList() {
		return activeScenarioList;
	}

	public void setActiveScenarioList(ArrayList<String> activeScenarioList) {
		this.activeScenarioList = activeScenarioList;
	}

}
