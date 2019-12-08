package service;



import java.io.File;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.ib.client.EReader;
import com.ib.client.EReaderSignal;
import com.ib.client.Order;

import ib.MyEWrapperImpl;
import ib.MyEWrapperImplCallbackInterface;
import samples.testbed.orders.AvailableAlgoParams;
import config.SystemConfig;
import entity.CreatedOrder;
import entity.IBApiConfig;
import entity.IBServerConfig;
import entity.StockConfig;
import service.SettingService;
import systemenum.SystemEnum;


public class IBService implements MyEWrapperImplCallbackInterface {
	private volatile static IBService instance; 
	
	private volatile static MyEWrapperImpl wrapper;
	private EClientSocket m_client;
	private EReaderSignal m_signal;
	private volatile static EReader reader;
	
	private IBApiConfig ibApiConfig;
	private IBServerConfig ibServerConfig;
	private StockConfig stockConfig;
	
	private SettingService settingServiceObj;
	private int currentOrderId;
	
	private IBService ()  {
		
		ibApiConfig = new IBApiConfig();
		ibServerConfig = new IBServerConfig();
		stockConfig = new StockConfig();
		initConfigs();
    }

	private void initConfigs() {
		
		try {

			SAXBuilder builder = new SAXBuilder(); 
			Document doc = (Document) builder.build(new File(SystemConfig.DOC_PATH + "//" + SystemConfig.IB_CONFIG_NAME));
			Element foo = doc.getRootElement(); //get <IBTradeConfig>
			Element ibApiConf = foo.getChild("IBApiConfig");  //get <IBApiConfig>
			Element ibServerConf = foo.getChild("IBServerConfig");  //get <IBServerConfig>
			Element stockConf = foo.getChild("StockConfig");  //get <StockConfig>
			
			String actStr = ibApiConf.getChild("active").getText();
			if (actStr.equals("1")) {
				ibApiConfig.setActive(true);
			} else {
				ibApiConfig.setActive(false);
			}
			String accTypeStr = ibApiConf.getChild("accounttype").getText();
			ibApiConfig.setAccType(accTypeStr.equalsIgnoreCase("paper")?SystemEnum.IbAccountType.Paper:SystemEnum.IbAccountType.Live);
			
			ibServerConfig.setLocalHost(ibServerConf.getChild("localhost").getText());
			ibServerConfig.setClientId(Integer.valueOf(ibServerConf.getChild("clientid").getText()));
			ibServerConfig.setMaxTryTimes(Integer.valueOf(ibServerConf.getChild("maxtrytimes").getText()));
			if(ibApiConfig.getAccType() == SystemEnum.IbAccountType.Paper) {
				ibServerConfig.setAccount(ibServerConf.getChild("paperaccount").getText());
				ibServerConfig.setPort(Integer.valueOf(ibServerConf.getChild("paperport").getText()));
			} else {
				ibServerConfig.setAccount(ibServerConf.getChild("liveaccount").getText());
				ibServerConfig.setPort(Integer.valueOf(ibServerConf.getChild("liveport").getText()));
			}
			
//			stockConfig.setStockSymbol(stockConf.getChild("stocksymbol").getText());
//			stockConfig.setOrderType(stockConf.getChild("ordertype").getText());
//			stockConfig.setSecurityType(stockConf.getChild("securitytype").getText());
//			stockConfig.setStockExchange(stockConf.getChild("stockexchange").getText());
//			stockConfig.setPrimaryExchange(stockConf.getChild("primaryexchange").getText());
//			stockConfig.setStockCurrency(stockConf.getChild("stockcurrency").getText());
//			stockConfig.setStockExpiry(Double.valueOf(stockConf.getChild("stockexpiry").getText()));
//			stockConfig.setOrderQuantity(Integer.valueOf(stockConf.getChild("orderquantity").getText()));
//			stockConfig.setFirstOrderQuantityPercent(Double.valueOf(stockConf.getChild("firstorderquantitypercent").getText())); 
			
		} catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	private void sendOrderToIB(String action, int quantity) {
		
		Contract contract = new Contract();
//		contract.symbol(stockConfig.getStockSymbol());
//		contract.secType(stockConfig.getSecurityType());
//		contract.currency(stockConfig.getStockCurrency());
//		contract.exchange(stockConfig.getStockExchange());
//		stock.primaryExch(stockConfig.getPrimaryExchange());	
		
		//指数
//		contract.symbol = "DAX";
//		contract.secType = "IND";
//		contract.currency = "EUR";
//		contract.exchange = "DTB";
		
		//期货 期货合约需要给出到期日期和标的物的代码。
//		contract.symbol = "ES";
//		contract.secType = "FUT";
//		contract.exchange = "GLOBEX";
//		contract.currency = "USD";
//		contract.lastTradeDateOrContractMonth = "201612";
		
		//
//		.order.limit(action, quantity, price, transmitOrder)
//		.order.market(action, quantity, transmitOrder, goodAfterTime, goodTillDate)
//		.order.marketClose(action, quantity, price, transmitOrder)
//		.order.stop(action, quantity, price, transmitOrder, parentId, tif)
//		.order.stopLimit(action, quantity, limitPrice, stopPrice, transmitOrder, parentId, tif)
//		.order.trailingStop(action, quantity, auxPrice, tif, transmitOrder, parentId)
		
		Order order = new Order();
		order.action(action);
		order.orderType("MKT");
		order.totalQuantity(quantity);
		
		AvailableAlgoParams.FillAdaptiveParams(order, "Normal");
		
//		order.account(ibServerConfig.getAccount());
		m_client.placeOrder(getCurrentOrderId(), contract, order);
		setCurrentOrderId(getCurrentOrderId()+1);
	}
	
	public void ibConnect() {
		
		if (ibServerConfig.getLocalHost().length() == 0 || ibServerConfig.getPort() == 0) return;
		
		wrapper = new MyEWrapperImpl();
		wrapper.setIbServiceInstance(instance);
		
        m_client = wrapper.getClient();
        m_signal = wrapper.getSignal();
        
        m_client.eConnect(ibServerConfig.getLocalHost(), ibServerConfig.getPort(), ibServerConfig.getClientId());
        
        reader = new EReader(m_client, m_signal);
        reader.start();
        
        new Thread() {
            public void run() {
                while (m_client.isConnected()) {
                    m_signal.waitForSignal();
                    try {
                        reader.processMsgs();
                        
                    } catch (Exception e) {
                    	e.printStackTrace();
                    }
                }
            }
        }.start();
        try {
            Thread.sleep(2000);
            setCurrentOrderId(wrapper.getCurrentOrderId());
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	public void ibDisConnect() {
		
		m_client.eDisconnect();
		
		reader = null;
		wrapper = null;
	}
	/*
	public void searchContractByDetail() {
		
		Contract contract = new Contract();
		contract.symbol(stockConfig.getStockSymbol());
		contract.secType(stockConfig.getSecurityType());
		contract.currency(stockConfig.getStockCurrency());
		contract.exchange(stockConfig.getStockExchange());
		contract.primaryExch(stockConfig.getPrimaryExchange());

		m_client.reqContractDetails(222,contract);
	}
	
	public void searchAllContractBySymbol() {
		
		m_client.reqMatchingSymbols(211, stockConfig.getStockSymbol());
	}
	*/
	public boolean isIBConnecting() {
		
		return m_client.isConnected();
	}
	
	public void placeOrder(Enum<SystemEnum.OrderAction> newAction, double limitPrice, double stopPrice, double tick) {
		
		if(newAction == SystemEnum.OrderAction.Default) return;
		
		String actionStr = null;
		if(newAction == SystemEnum.OrderAction.Buy) {
			actionStr = "BUY";
		} else {
			actionStr = "SELL";
		}
		
		sendOrderToIB(actionStr, quantity);
	}
	
	public void closeTodayTrade(String scenario, String time) {
		
		
		
		sendOrderToIB(actionStr, preOrderQuantityIncrease);
	}
	
	public void stopOrderWithMarketPrice(CreatedOrder order) {
		//todo
	}
	
	public static IBService getInstance() {  
		if (instance == null) {  
			synchronized (IBService.class) {  
				if (instance == null) {  
					instance = new IBService();  
				}	  
			}  
		}  
		return instance;  
	}

	@Override
	public void updateTradePrice(double price) {
		
		System.out.println("from ib updateTradePrice:"+price);
		if(getPreOrderScenario() == null) return;
		
		if (getSettingServiceObj() != null) { 
			getSettingServiceObj().updateTradePrice(price, getPreOrderScenario(), getPreOrderTime(), preOrderQuantity);
		}
		setPreOrderScenario(null);
		setPreOrderTime(null);
	}
	
	public int getCurrentOrderId() {
		return currentOrderId;
	}

	public void setCurrentOrderId(int currentOrderId) {
		this.currentOrderId = currentOrderId;
	}
	
	public IBApiConfig getIbApiConfig() {
		return ibApiConfig;
	}

	public SettingService getSettingServiceObj() {
		return settingServiceObj;
	}

	public void setSettingServiceObj(SettingService settingServiceObj) {
		this.settingServiceObj = settingServiceObj;
	}

}
