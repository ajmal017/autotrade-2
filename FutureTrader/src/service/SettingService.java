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

import org.apache.poi.ss.formula.ptg.Deleted3DPxg;

import application.FutureTrader;
import config.SystemConfig;
import dao.CommonDAO;
import dao.CommonDAOFactory;
import entity.ColorCount;
import entity.DailySettingRefresh;
import entity.CreatedOrder;
import entity.Setting;
import entity.OrderSign;
import entity.Zone;
import systemenum.SystemEnum;
import tool.Util;


public class SettingService implements IBServiceCallbackInterface {
	
	private volatile static SettingService instance;
	
	private ArrayList<String> activeSettingList;
	private ArrayList<Setting> workingSettingList; //must be active, and will refresh
	
	private ArrayList<DailySettingRefresh> settingRefreshPlan;
	private int passedSettingRefreshPlanCount = 0;

	private Map<String,ArrayList<OrderSign>> dailySignShownInTable; // <setting, signList>
	private Map<String,ArrayList<OrderSign>> dailySignMap; //all today's sign <setting, signList>
	private Map<String,ArrayList<CreatedOrder>> currentOrderMap; //current trend's orders <setting, orderList>. if stop, order will be clear
	private HashMap<String, String> orderIsSettingMap; //<orderId, setting>

	private int dailySignCount = 0;

	private boolean needCloseApp;
	private FutureTrader tradeObj;

	private int wantCloseOrderCount;
	
	private String LOGIC_MODE = "1";
	
	private SettingService ()  {
    	
		this.activeSettingList = new ArrayList<String>();
    	this.workingSettingList = new ArrayList<Setting>();
		this.settingRefreshPlan = new ArrayList<DailySettingRefresh>();
		this.dailySignShownInTable = new HashMap<String, ArrayList<OrderSign>>();
    	this.dailySignMap = new HashMap<String, ArrayList<OrderSign>>();
    	this.currentOrderMap = new HashMap<String, ArrayList<CreatedOrder>>();
    	
    	initAllSettingData();
    }
	
	private void initSettingRefreshPlan() {
    	
		CommonDAO commonDao = CommonDAOFactory.getCommonDAO();
    	ArrayList<String> times = commonDao.getAllSettingDistinctStartTimeAndEndTime();
    	if (times.size() == 0) {
			
    		return;
		}
    	
    	for (String d : times) {
    		
    		DailySettingRefresh refresh = new DailySettingRefresh();
    		refresh.setRefreshTime(d);
    		StringBuilder str = new StringBuilder(Util.getDateStringByDateAndFormatter(new Date(), "yyyyMMdd"));
    		str.append(d);
    		Date dDate = Util.getDateByStringAndFormatter(str.toString(), "yyyyMMddHH:mm:ss");
    		if (dDate.before(new Date())) {
    			refresh.setPassed(true); 
    			int passed = getPassedSettingRefreshPlanCount() + 1;
    			setPassedSettingRefreshPlanCount(passed);
			} else {
				refresh.setPassed(false);
			}
    		getSettingRefreshPlan().add(refresh);	
		}
	}
	
	private void initAllSettingData() {
		

    	CommonDAO commonDao = CommonDAOFactory.getCommonDAO();
    	
    	//get all scenario names and set active = true;
    	ArrayList<String> settings = commonDao.getAllActiveSettingName();
    	if (settings.size() == 0) {
			//none active setting
    		return;
		}
    	
    	for (String nameString : settings) {
        	getActiveSettingList().add(nameString);
        	getDailySignShownInTable().put(nameString, new ArrayList<OrderSign>());
        	getDailySignMap().put(nameString, new ArrayList<OrderSign>());
        	getCurrentOrderMap().put(nameString, new ArrayList<CreatedOrder>());
		}
    	
		initSettingRefreshPlan();
		ZoneColorInfoService.getInstance().loadCloseMonitorZoneListWithDefaultColor(commonDao.getAllCloseMonitorZone());
	}
	
	private void newSettingPlanRefreshed() {
    	
    	DailySettingRefresh refresh = getSettingRefreshPlan().get(getPassedSettingRefreshPlanCount());
		refresh.setPassed(true);
		setPassedSettingRefreshPlanCount(getPassedSettingRefreshPlanCount()+1);
    }
	
	private Map<String, List<String>> getOrderRecordWithSetting(String setting) {
    	
        Map<String, List<String>> map = new HashMap<String, List<String>>();
    	ArrayList<OrderSign> tsList = CommonDAOFactory.getCommonDAO().getOrderSignListByDate(new Date(), setting);
        if(tsList.size() == 0) return map;
        
    	for (int i = 0; i < tsList.size(); i ++) {
    		double profit = Util.getProfit(tsList.get(i).getLimitPrice(), tsList.get(i).getStopPrice(), tsList.get(i).getOrderAction());
    		tsList.get(i).setTickProfit(profit);
		}
    	
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
    	

        for (int i = 0; i < tsList.size(); i++) {
        	OrderSign sign = tsList.get(i);
		    ArrayList<String> params = new ArrayList<String>();
		    
		    //time
		    params.add(df.format(sign.getTime()));
		    //setting
		    params.add(sign.getSetting());
		    //action
		    params.add(sign.getActionText());
		    //limit price
		    if(sign.getLimitPrice() > 0) {
		    	params.add(sign.getLimitPrice()+"");
		    } else {
		    	params.add("0");
		    }
		    //tick
		    if(sign.getTick() > 0) {
		    	params.add(sign.getTick()+"");
		    } else {
		    	params.add("0");
		    }
		    //stop price
		    if(sign.getStopPrice() > 0) {
		    	params.add(sign.getStopPrice()+"");
		    } else {
		    	params.add("0");
		    }
		    //profit
		    if(sign.getTickProfit()!=0) {
		    	params.add(sign.getTickProfit()+"");
		    } else {
		    	params.add("0");
		    }
		    
		    //map key
		    map.put((i+1) + "", params);
		}
		return map;
    }
	
	private String[] excelTitle() {
        String[] strArray = { "time", "setting", "action", "limit price", "tick", "stop price", "profit"};
        return strArray;
    }
	    
    private ColorCount getColorCountByCloseZoneList() {

    	ColorCount count =  new ColorCount();
    	ZoneColorInfoService colorService = ZoneColorInfoService.getInstance();
    	for (Zone zone : colorService.getCloseMonitorZoneList()) {
    		if (zone.getColor() == SystemEnum.Color.Green) {
    			count.setGreen(count.getGreen()+1);
    		} else if (zone.getColor() == SystemEnum.Color.Red) {
    			count.setRed(count.getRed()+1);
    		} else if (zone.getColor() == SystemEnum.Color.White) {
    			count.setWhite(count.getWhite()+1);
    		} else if (zone.getColor() == SystemEnum.Color.Yellow) {
    			count.setYellow(count.getYellow()+1);
    		} else {
    			count.setOther(count.getOther()+1);
    		}
    	}
    	return count;
    }
	
	
	//update when timer called
    public void updateSettingListByRefreshPlan() {
    	
    	if (getActiveSettingList().size() == 0) {
    		
    		return;
    	}
    	
    	CommonDAO commonDao = CommonDAOFactory.getCommonDAO();
    	
    	//get new working setting
    	ArrayList<Setting> newSettingList = commonDao.getAllWorkingSettingAtTime(new Date());
    	if (newSettingList.size() == 0) {
    		
    		workingSettingList.clear();
    		
		} else {
			
			//get active working setting
	    	ArrayList<Setting> tempSettingList = new ArrayList<>();
	    	for (Setting newS : newSettingList) {
	    		for (String activeS : getActiveSettingList()) {
	    			if (newS.getSetting().equals(activeS)) {
	    				tempSettingList.add(newS);
	    				break;
					}
	    		}
			}
	    	if (tempSettingList.size() == 0) {
	    		//none new setting
	    		workingSettingList.clear();
	    		
			} else {
				
		    	//update working setting
		    	workingSettingList.clear();
		    	for (Setting workingS : tempSettingList) {
		    		workingSettingList.add(workingS);
				}
			}
	    	
		}
		newSettingPlanRefreshed();
    	
		closeStopWorkingSettingOrder();
		
    }
    
    public void exportTodayOrderProfit() {
    	
    	//checkout sign records
    	ArrayList<String> sheetList = new ArrayList<String>();
    	ArrayList<Map<String, List<String>>> mapList = new ArrayList<Map<String, List<String>>>();
    	int trendCount = 0;
    	for (String setting : activeSettingList) {
    		sheetList.add(setting);
    		Map<String, List<String>> recordMap = getOrderRecordWithSetting(setting);
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
    
    public void closeAllSettingWhenAppWantClose() {
    	
    	wantCloseOrderCount = 0;
    	ArrayList<String> settings = new ArrayList<String>();
    	for(String setting : getActiveSettingList()) {
    		if(currentOrderMap.get(setting).size() > 0) {
    	    	wantCloseOrderCount += currentOrderMap.get(setting).size();
    		}
    	}
    	
    	if(wantCloseOrderCount > 0) {
    		setNeedCloseApp(true);
    		closeAllOrderIfNeed();
    	} else {
    		if(getTradeObj() != null) {
    			getTradeObj().closeAppAfterPriceUpdate();
    	    }
    	}
    }
    
    public void deleteAnotherActionOrder() {
    	
    	//todo
    }
    
    private void createScreenShot(String setting, Enum<SystemEnum.OrderAction> action, double limitPrice) {
    	
    	ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
	    cachedThreadPool.execute(new Runnable() {
	  
	        @Override
	        public void run() {
	        	//screen shot
            	String shotPath = SystemConfig.DOC_PATH + "//screenshot//" + 
            					  Util.getDateStringByDateAndFormatter(new Date(), "yyyyMMdd") + "//"+ 
            					  setting + "//" + 
            					  setting + "_" + 
            					  Util.getDateStringByDateAndFormatter(new Date(), "HHmmss") + "_" +
            					  Util.getActionTextByEnum(action) + "_" +
            					  limitPrice + ".png";
            	File newFile = new File(shotPath);
    			if(!newFile.exists()) {
                	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                	Rectangle screenRectangle = new Rectangle(screenSize);
    				Util.createScreenShotByRect(screenRectangle, shotPath, "png");
    			}
	        }
	    });
    }
    
    
    private void createNewOrder (String setting, Enum<SystemEnum.OrderAction> action, double limitPrice, double stopPrice) {
    	
    	CommonDAO commonDao = CommonDAOFactory.getCommonDAO();
    	
    	Date now = new Date();
    	String nowTimeStr = Util.getDateStringByDateAndFormatter(now, "HH:mm:ss");
    	
    	OrderSign newSign = new OrderSign(); //todo
    	ArrayList<OrderSign> dailySignList = getDailySignMap().get(setting);
    	dailySignList.add(newSign);
    	currentOrderMap.get(setting).add(new CreatedOrder()); //todo
    	commonDao.insertNewOrderSign(newSign);
    	
    	IBService ibService = IBService.getInstance();
    	if(ibService.getIbApiConfig().isActive() && ibService.isIBConnecting()) {
    		
    		
    		ibService.placeOrder(action, setting);
    		
    	}
    	
    	createScreenShot();
    	
    }
    
	public static SettingService getInstance() {  
		if (instance == null) {  
			synchronized (SettingService.class) {  
				if (instance == null) {  
					instance = new SettingService();
					IBService.getInstance().setSettingServiceObj(instance);
				}	  
			}  
		}  
		return instance;  
	}
	

	@Override
	public void updateTradePrice(double price, String preOrderScenario, String preOrderTime, int preQuantity) {
		System.out.println("Setting service updateTradePrice:"+price+" preOrderScenario:"+preOrderScenario+" preOrderTime:"+preOrderTime);
		//todo
		CommonDAOFactory.getCommonDAO().updateLastOrderSignIBPrice(preOrderScenario, preOrderTime, price, preQuantity);
		if(isNeedCloseApp() && wantCloseOrderCount > 0) wantCloseOrderCount--;
		if(isNeedCloseApp() && getTradeObj() != null && wantCloseOrderCount == 0) {
			setNeedCloseApp(false);
			getTradeObj().closeAppAfterPriceUpdate();
	    }
	}
	
	@Override
	public void orderStop(int orderId) {
	
		//cancel no-active order
		
		//create new other action order
	}
	
	@Override
	public void orderActive(int orderId) {
		
		//if market first order, cancel another action order
		
		//change all pre-order's stop price
		
		//create new same action order
	}
	
	public boolean isNeedCloseApp() {
		return needCloseApp;
	}

	public void setNeedCloseApp(boolean needCloseApp) {
		this.needCloseApp = needCloseApp;
	}
	
	public FutureTrader getTradeObj() {
		return tradeObj;
	}

	public void setTradeObj(FutureTrader tradeObj) {
		this.tradeObj = tradeObj;
	}
	
	public Map<String, ArrayList<OrderSign>> getDailySignShownInTable() {
		return dailySignShownInTable;
	}

	public void setDailySignShownInTable(Map<String, ArrayList<OrderSign>> dailySignShownInTable) {
		this.dailySignShownInTable = dailySignShownInTable;
	}

	public Map<String, ArrayList<OrderSign>> getDailySignMap() {
		return dailySignMap;
	}

	public void setDailySignMap(Map<String, ArrayList<OrderSign>> dailySignMap) {
		this.dailySignMap = dailySignMap;
	}

	public Map<String, ArrayList<CreatedOrder>> getCurrentOrderMap() {
		return currentOrderMap;
	}

	public void setCurrentOrderMap(Map<String, ArrayList<CreatedOrder>> currentOrderMap) {
		this.currentOrderMap = currentOrderMap;
	}
	
	public ArrayList<String> getActiveSettingList() {
		return activeSettingList;
	}

	public void setActiveSettingList(ArrayList<String> activeSettingList) {
		this.activeSettingList = activeSettingList;
	}
	
	public ArrayList<Setting> getWorkingSettingList() {
		return workingSettingList;
	}

	public void setWorkingSettingList(ArrayList<Setting> workingSettingList) {
		this.workingSettingList = workingSettingList;
	}

	public int getPassedSettingRefreshPlanCount() {
		return passedSettingRefreshPlanCount;
	}

	public void setPassedSettingRefreshPlanCount(int passedSettingRefreshPlanCount) {
		this.passedSettingRefreshPlanCount = passedSettingRefreshPlanCount;
	}
	
	public ArrayList<DailySettingRefresh> getSettingRefreshPlan() {
		return settingRefreshPlan;
	}
	
	public void setSettingRefreshPlan(ArrayList<DailySettingRefresh> settingRefreshPlan) {
		this.settingRefreshPlan = settingRefreshPlan;
	}
	
	public int getDailySignCount() {
		return dailySignCount;
	}

	public void setDailySignCount(int dailySignCount) {
		this.dailySignCount = dailySignCount;
	}
	
	public HashMap<String, String> getOrderIsSettingMap() {
		return orderIsSettingMap;
	}

	public void setOrderIsSettingMap(HashMap<String, String> orderIsSettingMap) {
		this.orderIsSettingMap = orderIsSettingMap;
	}
}
