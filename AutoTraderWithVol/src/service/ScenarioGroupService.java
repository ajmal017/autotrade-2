package service;

import java.util.ArrayList;
import java.util.Date;

import DAO.ScenarioDAO;
import DAO.ScenarioDAOFactory;
import DAO.ZoneDAO;
import DAO.ZoneDAOFactory;
import dao.CommonDAO;
import dao.CommonDAOFactory;
import entity.Area;
import entity.DailyScenarioRefresh;
import entity.Scenario;
import entity.Volume;
import entity.Zone;
import tool.Util;

public class ScenarioGroupService {
	
	private volatile static ScenarioGroupService instance;
	
	private ArrayList<String> activeScenarioGroupList; //T10 T11 T12
	
	private ArrayList<Scenario> workingScenarioList; //T10
	private ArrayList<Volume> workingVolumeList; //T10
	
	private ArrayList<Zone> volumeZoneList; // B71 B72 ....G77 G78
	
	private ArrayList<DailyScenarioRefresh> sceRefreshPlan;
	private int passedSceRefreshPlanCount = 0; 
	private ArrayList<DailyScenarioRefresh> volRefreshPlan;
	private int passedVolRefreshPlanCount = 0; 
	
	
	private ScenarioGroupService ()  {
    	
		this.activeScenarioGroupList = new ArrayList<String>();
    	this.workingScenarioList = new ArrayList<Scenario>();
    	this.workingVolumeList = new ArrayList<Volume>();
    	this.volumeZoneList = new ArrayList<Zone>();
    	this.sceRefreshPlan = new ArrayList<DailyScenarioRefresh>();
    	this.volRefreshPlan = new ArrayList<DailyScenarioRefresh>();
    	
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
    	
    	for (String nameString : sceNames) {
        	getActiveScenarioGroupList().add(nameString);
		}
		
    	ArrayList<Zone> volumeZoneList = commonDao.getVolumeZoneList();
    	if (volumeZoneList.size() == 0) {
			//none active scenario
    		return;
		}
    	for(Zone z : volumeZoneList) {
    		getVolumeZoneList().add(z);
    	}
    	
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
	
	public void updateWorkingVolumeListByRefreshPlan() {
		
		if (getActiveScenarioList().size() == 0) {
    		workingScenarioList.clear();
    		
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
    		//none new scenario
    		for (Scenario scenario : workingScenarioList) {
				closeOrderByScenario(scenario.getScenario());
			}
    		workingScenarioList.clear();
    		newPlanRefreshed();
    		return;
		} else {
			
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
    		for (String workingS : getActiveScenarioGroupList()) {
    			if (newS.getScenario().equals(workingS)) {
    				tempScenarioList.add(newS);
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
    			for (String z : zones) {
    				area.getZoneList().add(z);
    			}
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

	public ArrayList<String> getActiveScenarioGroupList() {
		return activeScenarioGroupList;
	}

	public void setActiveScenarioGroupList(ArrayList<String> activeScenarioGroupList) {
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

	public ArrayList<Zone> getVolumeZoneList() {
		return volumeZoneList;
	}

	public void setVolumeZoneList(ArrayList<Zone> volumeZoneList) {
		this.volumeZoneList = volumeZoneList;
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
	

}
