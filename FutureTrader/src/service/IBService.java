package service;



import java.io.File;
import java.util.IntSummaryStatistics;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.ib.client.EReader;
import com.ib.client.EReaderSignal;
import com.ib.client.Order;

import ch.qos.logback.core.joran.conditional.IfAction;
import ib.MyEWrapperImpl;
import ib.MyEWrapperImplCallbackInterface;
import samples.testbed.orders.AvailableAlgoParams;
import samples.testbed.orders.OrderSamples;
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
	
	private boolean requestPrice = false;
	
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
	
	public void getCurrentPrice() {
		
		requestPrice = true;
		m_client.reqMktData(arg0, arg1, arg2, arg3, arg4, arg5);
	};
	
	public void createBracketOrder(CreatedOrder order) {
		
		placeBracketOrder(order.getOrderAction(),order.getLimitPrice(),order.getProfitLimitPrice(),order.getTick());
	}
	
	public void modifyOrderProfitLimitPrice(CreatedOrder order, double newProfitLimitPrice) {
		
		//todo
	}
	
	public void cancelOrder(int orderId) {
		
		//todo
	}
	
	public void stopOrderWithMarketPrice(CreatedOrder order) {
		
		
	}
	
	private void placeBracketOrder(Enum<SystemEnum.OrderAction> orderAction, double limitPrice, double profitLimitPrice, double tick) {
		
		if(orderAction == SystemEnum.OrderAction.Default) return;
		
		String actionStr = null;
		if(orderAction == SystemEnum.OrderAction.Buy) {
			actionStr = "BUY";
		} else {
			actionStr = "SELL";
		}
		
		Contract contract = new Contract();
		contract.symbol("ES");
		contract.secType("FUT");
		contract.exchange("GLOBEX");
		contract.currency("USD");
		contract.lastTradeDateOrContractMonth("202003");		
		
		List<Order> bracket = OrderSamples.BracketOrder(getCurrentOrderId(), 
				actionStr,
				tick, 
				limitPrice,profitLimitPrice, 0);
//		order.account(ibServerConfig.getAccount());
		bracket.remove(2);
		for(Order o : bracket) {
			 //AvailableAlgoParams.FillAdaptiveParams(o, "Normal");
			 o.transmit(true);
			 m_client.placeOrder(o.orderId(), contract, o);
		}

		setCurrentOrderId(getCurrentOrderId()+2);
		
		
		//order excample
		//open order: orderId=510 action=BUY quantity=1.0 cashQty= conid=346577697 symbol=ES secType=FUT lastTradeDate=20200320 strike=0.0 right=? multiplier=50 exchange=GLOBEX primaryExch=null currency=USD localSymbol=ESH0 tradingClass=ES type=LMT lmtPrice=3240.0 auxPrice=0.0 TIF=DAY localSymbol=ESH0 client Id=0 parent Id=0 permId=410647624 outsideRth=false hidden=false discretionaryAmt=0.0 displaySize=0 triggerMethod=0 goodAfterTime=null goodTillDate=null faGroup=null faMethod=null faPercentage=null faProfile=null shortSaleSlot=0 designatedLocation=null exemptCode=-1 ocaGroup=null ocaType=3 rule80A=null allOrNone=false minQty= percentOffset= eTradeOnly=false firmQuoteOnly=false nbboPriceCap= optOutSmartRouting=false auctionStrategy=0 startingPrice= stockRefPrice= delta= stockRangeLower= stockRangeUpper= volatility= volatilityType=0 deltaNeutralOrderType=None deltaNeutralAuxPrice= deltaNeutralConId=0 deltaNeutralSettlingFirm=null deltaNeutralClearingAccount=null deltaNeutralClearingIntent=null deltaNeutralOpenClose=? deltaNeutralShortSale=false deltaNeutralShortSaleSlot=0 deltaNeutralDesignatedLocation=null continuousUpdate=0 referencePriceType=0 trailStopPrice=3241.0 trailingPercent= scaleInitLevelSize= scaleSubsLevelSize= scalePriceIncrement= scalePriceAdjustValue= scalePriceAdjustInterval= scaleProfitOffset= scaleAutoReset=false scaleInitPosition= scaleInitFillQty= scaleRandomPercent=false hedgeType=null hedgeParam=null account=DU1300317 modelCode=null settlingFirm=null clearingAccount=null clearingIntent=IB notHeld=false whatIf=false solicited=false randomize size=false randomize price=false dontUseAutoPriceForHedge=true isOmsContainer=false discretionaryUpToLimitPrice=false status=Submitted initMarginBefore=null maintMarginBefore=null equityWithLoanBefore=null initMarginChange=null maintMarginChange=null equityWithLoanChange=null initMarginAfter=null maintMarginAfter=null equityWithLoanAfter=null commission= minCommission= maxCommission= commissionCurrency=null warningText=null
		// OrderStatus. Id: 510, Status: Submitted, Filled0.0, Remaining: 1.0, AvgFillPrice: 0.0, PermId: 410647624, ParentId: 0, LastFillPrice: 0.0, ClientId: 0, WhyHeld: null, MktCapPrice: 0.0
		//open order: orderId=511 action=SELL quantity=1.0 cashQty= conid=346577697 symbol=ES secType=FUT lastTradeDate=20200320 strike=0.0 right=? multiplier=50 exchange=GLOBEX primaryExch=null currency=USD localSymbol=ESH0 tradingClass=ES type=LMT lmtPrice=3241.25 auxPrice=0.0 TIF=DAY localSymbol=ESH0 client Id=0 parent Id=510 permId=410647625 outsideRth=false hidden=false discretionaryAmt=0.0 displaySize=0 triggerMethod=0 goodAfterTime=null goodTillDate=null faGroup=null faMethod=null faPercentage=null faProfile=null shortSaleSlot=0 designatedLocation=null exemptCode=-1 ocaGroup=410647624 ocaType=3 rule80A=null allOrNone=false minQty= percentOffset= eTradeOnly=false firmQuoteOnly=false nbboPriceCap= optOutSmartRouting=false auctionStrategy=0 startingPrice= stockRefPrice= delta= stockRangeLower= stockRangeUpper= volatility= volatilityType=0 deltaNeutralOrderType=None deltaNeutralAuxPrice= deltaNeutralConId=0 deltaNeutralSettlingFirm=null deltaNeutralClearingAccount=null deltaNeutralClearingIntent=null deltaNeutralOpenClose=? deltaNeutralShortSale=false deltaNeutralShortSaleSlot=0 deltaNeutralDesignatedLocation=null continuousUpdate=0 referencePriceType=0 trailStopPrice=3240.25 trailingPercent= scaleInitLevelSize= scaleSubsLevelSize= scalePriceIncrement= scalePriceAdjustValue= scalePriceAdjustInterval= scaleProfitOffset= scaleAutoReset=false scaleInitPosition= scaleInitFillQty= scaleRandomPercent=false hedgeType=null hedgeParam=null account=DU1300317 modelCode=null settlingFirm=null clearingAccount=null clearingIntent=IB notHeld=false whatIf=false solicited=false randomize size=false randomize price=false dontUseAutoPriceForHedge=true isOmsContainer=false discretionaryUpToLimitPrice=false status=PreSubmitted initMarginBefore=null maintMarginBefore=null equityWithLoanBefore=null initMarginChange=null maintMarginChange=null equityWithLoanChange=null initMarginAfter=null maintMarginAfter=null equityWithLoanAfter=null commission= minCommission= maxCommission= commissionCurrency=null warningText=null
		// OrderStatus. Id: 511, Status: PreSubmitted, Filled0.0, Remaining: 1.0, AvgFillPrice: 0.0, PermId: 410647625, ParentId: 510, LastFillPrice: 0.0, ClientId: 0, WhyHeld: child,locate, MktCapPrice: 0.0

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
	public void responseCurrentPrice(double price) {
		
		if (requestPrice) {
			
			requestPrice = false;
			if (settingServiceObj != null) {
				settingServiceObj.responseCurrentPrice(price);
			}
		}
		m_client.cancelMktData(arg0);
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
