package service;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.File;
import java.io.ObjectInputStream.GetField;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.text.html.HTMLDocument.HTMLReader.PreAction;

import org.apache.poi.ss.formula.ptg.Deleted3DPxg;

import com.ib.client.Order;
import com.ib.client.TickAttrib;

import application.FutureTrader;
import config.SystemConfig;
import dao.CommonDAO;
import dao.CommonDAOFactory;
import entity.ColorCount;
import entity.DailySettingRefresh;
import entity.CreatedOrder;
import entity.Setting;
import entity.SingleOrderSetting;
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

//	private Map<String,ArrayList<OrderSign>> dailySignShownInTable; // <setting, signList>
	private Map<String,ArrayList<OrderSign>> dailySignMap; //all today's sign <setting, signList>
	private Map<String,ArrayList<CreatedOrder>> currentOrderMap; //current trend's parent orders <setting, orderList>. if stop, order will be clear
//	private Map<Integer,CreatedOrder> currentProfitLimitOrderMap; //parentOrderId,childProfitOrder
	
	private Map<Integer,String> parentOrderIdSettingMap; //<parentOrderId,setting>

	private int dailyOrderCount = 0;

	private boolean needCloseApp;
	private FutureTrader tradeObj;

	private int wantCloseOrderCount;
	
	private String LOGIC_MODE = "1"; 
	//1: create order a lot  
	//2:stop all pre-orders, when order count cover the max  
	//3:stop first pre-orders, when order count cover the max(keep max)
	
	private double dailyFirstPrice = 0;

	private SettingService ()  {
    	
		this.activeSettingList = new ArrayList<String>();
    	this.workingSettingList = new ArrayList<Setting>();
		this.settingRefreshPlan = new ArrayList<DailySettingRefresh>();
//		this.dailySignShownInTable = new HashMap<String, ArrayList<OrderSign>>();
    	this.dailySignMap = new HashMap<String, ArrayList<OrderSign>>();
    	this.currentOrderMap = new HashMap<String, ArrayList<CreatedOrder>>();
//    	this.currentProfitLimitOrderMap = new HashMap<Integer, CreatedOrder>();
    	this.parentOrderIdSettingMap = new HashMap<Integer,String>();
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
//        	getDailySignShownInTable().put(nameString, new ArrayList<OrderSign>());
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
    		double profit = Util.getProfit(tsList.get(i).getLimitPrice(), tsList.get(i).getProfitLimitPrice(), tsList.get(i).getOrderAction());
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
		    if(sign.getProfitLimitPrice() > 0) {
		    	params.add(sign.getProfitLimitPrice()+"");
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
	
	public void getCurrentPrice() {
		
		IBService.getInstance().getCurrentPrice();
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
    }
    
    
	public void closeAllOrder() {
		
		for(String settting : activeSettingList) {
			
			ArrayList<CreatedOrder> orders = currentOrderMap.get(settting);
			if (orders.size() == 0) {
				continue;
			} else {
				for(CreatedOrder order:orders) {
					if (order.getOrderState().equals(SystemConfig.IB_ORDER_STATE_Filled)) {
						IBService.getInstance().stopOrderWithMarketPrice(order);
					} else {
						IBService.getInstance().cancelOrder(order.getOrderIdInIB());
					}
				}
			}
		}
	}
    
    public void openDailyFirstOrder(String setting, SingleOrderSetting firstSetting) {
    	
    	//buy order
    	createNewBracketOrder(setting, 
    			SystemEnum.OrderAction.Buy, 
    			dailyFirstPrice - firstSetting.getLimitChange(), 
    			dailyFirstPrice - firstSetting.getLimitChange() + firstSetting.getProfitLimitChange(), 
    			firstSetting.getTick());
    	
    	
    	//sell order
    	createNewBracketOrder(setting, 
    			SystemEnum.OrderAction.Sell, 
    			dailyFirstPrice + firstSetting.getLimitChange(), 
    			dailyFirstPrice + firstSetting.getLimitChange() - firstSetting.getProfitLimitChange(), 
    			firstSetting.getTick());
    }
    
    public void closeUnWorkingSettingOrder() {
    	
    	for (String setting : getActiveSettingList()) {
			
    		boolean working = false;
    		for(Setting setting2 : getWorkingSettingList()) {
    			if (setting.equals(setting2.getSetting())) {
					working = true;
					break;
				}
    		}
			if (!working && currentOrderMap.get(setting).size() > 0) {
				
				for (CreatedOrder parentOrder : currentOrderMap.get(setting)) {
					
					if (!parentOrder.getOrderState().equals(SystemConfig.IB_ORDER_STATE_Filled)) {
						 //when cancel unfilled parent order, profit-limit order will be anti-canceled
						IBService.getInstance().cancelOrder(parentOrder.getOrderIdInIB());
					} else if (parentOrder.getOrderState().equals(SystemConfig.IB_ORDER_STATE_Filled)) {
						//when stop filled parent order, must hand-cancel profit-limit order first. then create a ANTI-action MKT order.  maybe WRONG
						//IBService.getInstance().cancelOrder(parentOrder.getProfitLimitOrderIdInIB());
						
						//when stop filled parent order, only need change profit-limit order��s ordertype to MKT.
						
						IBService.getInstance().stopOrderWithMarketPrice(parentOrder);
					}
	//				currentProfitLimitOrderMap.remove(parentOrder.getOrderIdInIB());
				}
				currentOrderMap.get(setting).clear();
			}
			
		}
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
    	for(String setting : getActiveSettingList()) {
    		if(currentOrderMap.get(setting).size() > 0) {
    	    	wantCloseOrderCount += currentOrderMap.get(setting).size();
    		}
    	}
    	
    	if(wantCloseOrderCount > 0) {
    		setNeedCloseApp(true);
    		closeAllOrder();
    	} else {
    		if(getTradeObj() != null) {
    			getTradeObj().closeAppAfterPriceUpdate();
    	    }
    	}
    }
    
    private void createScreenShot(String setting, Enum<SystemEnum.OrderAction> action, double limitPrice) {
    	
    	//todo when use this method
    	
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
    
    
    private void createNewBracketOrder (String setting, Enum<SystemEnum.OrderAction> action, double limitPrice, double stopPrice, double tick) {
    	
    	OrderSign newSign = new OrderSign(IBService.getInstance().getCurrentOrderId(), 
    			IBService.getInstance().getCurrentOrderId()+1, 
    			SystemConfig.IB_ORDER_STATE_Submitted, 
    			new Date(), 
    			setting, 
    			action, limitPrice, tick, stopPrice, 0, 0, 0);
    	CreatedOrder newOrder = new CreatedOrder(IBService.getInstance().getCurrentOrderId(), 
    			IBService.getInstance().getCurrentOrderId()+1, 
    			SystemConfig.IB_ORDER_STATE_Submitted, 
    			new Date(), 
    			action, limitPrice, tick, stopPrice);
    	
//    	getDailySignShownInTable().get(setting).add(newSign);
    	getDailySignMap().get(setting).add(newSign);
    	currentOrderMap.get(setting).add(newOrder);
    	CommonDAOFactory.getCommonDAO().insertNewOrderSign(newSign);
    	
    	IBService ibService = IBService.getInstance();
    	if(ibService.getIbApiConfig().isActive() && ibService.isIBConnecting()) {
    		
    		ibService.createBracketOrder(newOrder);
    	}
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
	public void responseWhenOrderSubmitted(Integer parentOrderId, Integer orderId, String orderState) {
		
		//todo
	}
	
	@Override
	public void responseWhenParentOrderFilled(Integer orderId, String orderState, double filledPrice) {
		
		String thisSetting = parentOrderIdSettingMap.get(orderId);
		for(OrderSign os : dailySignMap.get(thisSetting)) {
			if(os.getParentOrderIdInIB() == orderId) {
				os.setOrderState(orderState);
				os.setLimitFilledPrice(filledPrice);
				CommonDAOFactory.getCommonDAO().updateOrderLimitFilledInfo(os.getParentOrderIdInIB(), os.getOrderState(), os.getLimitFilledPrice());
				break;
			}
		}
		for(CreatedOrder os : currentOrderMap.get(thisSetting)) {
			if(os.getOrderIdInIB() == orderId) {
				os.setOrderState(orderState);
				break;
			}
		}
		
		//if market first order, cancel another action order
		if(currentOrderMap.get(thisSetting).size() == 2 && 
				currentOrderMap.get(thisSetting).get(0).getOrderAction() != 
				currentOrderMap.get(thisSetting).get(1).getOrderAction()) {

			int shouldCancelOrderId;
			if (currentOrderMap.get(thisSetting).get(0).getOrderIdInIB() == orderId) {
				//cancel another
				shouldCancelOrderId = currentOrderMap.get(thisSetting).get(1).getOrderIdInIB();
			} else {
				//cancel another
				shouldCancelOrderId = currentOrderMap.get(thisSetting).get(0).getOrderIdInIB();
			}
			for(OrderSign os : dailySignMap.get(thisSetting)) {
				if(os.getParentOrderIdInIB() == shouldCancelOrderId) {
					dailySignMap.get(thisSetting).remove(os);
					break;
				}
			}
			for(CreatedOrder os : currentOrderMap.get(thisSetting)) {
				if(os.getOrderIdInIB() == shouldCancelOrderId) {
					currentOrderMap.get(thisSetting).remove(os);
					break;
				}
			}
			IBService.getInstance().cancelOrder(shouldCancelOrderId);
		}
		

		//change all pre-order's profit limit price
		if (currentOrderMap.get(thisSetting).size() > 1) {
		
			CreatedOrder last = currentOrderMap.get(thisSetting).get(currentOrderMap.get(thisSetting).size()-1);
			for(int i = 0; i < currentOrderMap.get(thisSetting).size()-1;i++) {
				CreatedOrder preOrder = currentOrderMap.get(thisSetting).get(i);
				if (preOrder.getProfitLimitPrice() != last.getProfitLimitPrice()) {
					preOrder.setProfitLimitPrice(last.getProfitLimitPrice());
					
					for(OrderSign os : dailySignMap.get(thisSetting)) {
						if(os.getParentOrderIdInIB() == preOrder.getOrderIdInIB()) {
							os.setProfitLimitPrice(last.getProfitLimitPrice());
							break;
						}
					}
					CommonDAOFactory.getCommonDAO().updateOrderProfitLimitPrice(preOrder.getOrderIdInIB(), preOrder.getProfitLimitPrice());
					IBService.getInstance().modifyOrderProfitLimitPrice(preOrder, preOrder.getProfitLimitPrice());
				}
			}
		}
		
		//create new same action order
		ArrayList<SingleOrderSetting> orderSettingList = null;
		for(Setting setting : workingSettingList) {
			if (setting.getSetting().equals(thisSetting)) {
				orderSettingList = setting.getOrderSettingList();
				break;
			}
		}
		SingleOrderSetting newOrderSetting;
		if (currentOrderMap.get(thisSetting).size() == orderSettingList.size()) {
			//if order count = setting count, user last setting
			newOrderSetting = orderSettingList.get(orderSettingList.size() - 1);
		} else {
			newOrderSetting = orderSettingList.get(currentOrderMap.get(thisSetting).size());
		}
		
		CreatedOrder sameSettingPreOrder = currentOrderMap.get(thisSetting).get(currentOrderMap.get(thisSetting).size()-1);
		CreatedOrder sameSettingFirstOrder = currentOrderMap.get(thisSetting).get(0);
		double newLimitPrice;
		double newProfitLimitPrice;
		if (sameSettingPreOrder.getOrderAction() == SystemEnum.OrderAction.Buy) {
			if (sameSettingPreOrder.getLimitPrice() == sameSettingFirstOrder.getLimitPrice()) {
				//if only 1 order is in list, now create 2rd order
				newLimitPrice = sameSettingPreOrder.getLimitPrice() - newOrderSetting.getLimitChange();
				newProfitLimitPrice = sameSettingPreOrder.getLimitPrice() - newOrderSetting.getProfitLimitChange();
			} else {
				newLimitPrice = sameSettingPreOrder.getLimitPrice() - newOrderSetting.getLimitChange();
				newProfitLimitPrice = sameSettingPreOrder.getProfitLimitPrice() - newOrderSetting.getProfitLimitChange();
			}
		} else { //sell
			if (sameSettingPreOrder.getLimitPrice() == sameSettingFirstOrder.getLimitPrice()) {
				//if only 1 order is in list, now create 2rd order
				newLimitPrice = sameSettingPreOrder.getLimitPrice() + newOrderSetting.getLimitChange();
				newProfitLimitPrice = sameSettingPreOrder.getLimitPrice() + newOrderSetting.getProfitLimitChange();
			} else {
				newLimitPrice = sameSettingPreOrder.getLimitPrice() + newOrderSetting.getLimitChange();
				newProfitLimitPrice = sameSettingPreOrder.getProfitLimitPrice() + newOrderSetting.getProfitLimitChange();
			}
		}
		createNewBracketOrder(thisSetting, sameSettingPreOrder.getOrderAction(), newLimitPrice, newProfitLimitPrice, newOrderSetting.getTick());
	}
	
	@Override
	public void responseWhenProfitLimitOrderFilled(Integer parentOrderId, Integer ordeId, double filledPrice, String orderState, double tick) {
		
		String thisSetting = parentOrderIdSettingMap.get(parentOrderId);
		int index = 0;
		double profit = 0;
		
		for(OrderSign os : dailySignMap.get(thisSetting)) {
			index++;
			if(os.getParentOrderIdInIB() == parentOrderId) {
				os.setOrderState(orderState);
				os.setProfitLimitFilledPrice(filledPrice);
				profit = Util.getProfit(os.getLimitFilledPrice(), filledPrice, os.getOrderAction())*tick;
				os.setTickProfit(profit);
				CommonDAOFactory.getCommonDAO().updateOrderProfitLimitFilledInfo(parentOrderId, os.getOrderState(), os.getProfitLimitFilledPrice(), profit);
				break;
			}
		}
		
		for(CreatedOrder os : currentOrderMap.get(thisSetting)) {
			if(os.getOrderIdInIB() == parentOrderId) {
				currentOrderMap.get(thisSetting).remove(os);
				break;
			}
		}
		
		//cancel last same action no-active order and create new different action order (only do once time)
		if (currentOrderMap.get(thisSetting).size() == 1) { //all active order have stoped
			
//			dailySignShownInTable.get(thisSetting).remove(dailySignShownInTable.get(thisSetting).size()-1);
			dailySignMap.get(thisSetting).remove(dailySignMap.get(thisSetting).size()-1);
			currentOrderMap.get(thisSetting).remove(0);
		}
		
		
		//if need close app
		if(needCloseApp) {
			
			if(wantCloseOrderCount > 0) {
				wantCloseOrderCount --;
			} else {
				tradeObj.closeAppAfterPriceUpdate();
			}
			
		} else {

			//create new 1st different action order
			SystemEnum.OrderAction  newAction;
			OrderSign lastOrder = dailySignMap.get(thisSetting).get(dailySignMap.get(thisSetting).size()-1);
			if (lastOrder.getOrderAction() == SystemEnum.OrderAction.Buy) {
				newAction = SystemEnum.OrderAction.Sell;
			} else {
				newAction = SystemEnum.OrderAction.Buy;
			}
			
			ArrayList<SingleOrderSetting> orderSettingList = null;
			for(Setting setting : workingSettingList) {
				if (setting.getSetting().equals(thisSetting)) {
					orderSettingList = setting.getOrderSettingList();
					break;
				}
			}
			
			double newLimitPrice = lastOrder.getProfitLimitPrice();
			double newStopPrice;
			if (newAction == SystemEnum.OrderAction.Sell) {
				newStopPrice = newLimitPrice - orderSettingList.get(0).getProfitLimitChange();
			} else {
				newStopPrice = newLimitPrice + orderSettingList.get(0).getProfitLimitChange();
			}
			
			createNewBracketOrder(thisSetting, newAction, newLimitPrice, newStopPrice, orderSettingList.get(0).getTick());
		}
	}
	
	@Override
	public void responseCurrentPrice(double price) {
		setDailyFirstPrice(price);
		if (tradeObj != null) {
			tradeObj.noticeGotDailyPrice();
		}
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
	
//	public Map<String, ArrayList<OrderSign>> getDailySignShownInTable() {
//		return dailySignShownInTable;
//	}
//
//	public void setDailySignShownInTable(Map<String, ArrayList<OrderSign>> dailySignShownInTable) {
//		this.dailySignShownInTable = dailySignShownInTable;
//	}

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
	
//	public Map<Integer,CreatedOrder> getCurrentProfitLimitOrderMap() {
//		return currentProfitLimitOrderMap;
//	}
//
//	public void setCurrentProfitLimitOrderMap(Map<Integer,CreatedOrder> currentProfitLimitOrderMap) {
//		this.currentProfitLimitOrderMap = currentProfitLimitOrderMap;
//	}

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
	
	public int getDailyOrderCount() {
		return dailyOrderCount;
	}

	public void setDailyOrderCount(int dailyOrderCount) {
		this.dailyOrderCount = dailyOrderCount;
	}

	public Map<Integer, String> getParentOrderIdSettingMap() {
		return parentOrderIdSettingMap;
	}

	public void setParentOrderIdSettingMap(Map<Integer, String> parentOrderIdSettingMap) {
		this.parentOrderIdSettingMap = parentOrderIdSettingMap;
	}

	public double getDailyFirstPrice() {
		return dailyFirstPrice;
	}

	public void setDailyFirstPrice(double dailyFirstPrice) {
		this.dailyFirstPrice = dailyFirstPrice;
	}
	
	
}
