package service;



import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.ib.client.EReader;
import com.ib.client.EReaderSignal;

import IB.MyEWrapperImpl;
import entity.IBServerConfig;
import entity.StockConfig;


public class IBService {
	private volatile static IBService instance; 
	
	private MyEWrapperImpl wrapper;
	private EClientSocket m_client;
	private EReaderSignal m_signal;
	private EReader reader;
	
	private static final IBServerConfig ibConfig = new IBServerConfig();
	private static final StockConfig stockConfig = new StockConfig();
	
	private IBService ()  {
    	
		initConfigs();
    }

	private void initConfigs() {
		
		try {

			SAXBuilder builder = new SAXBuilder(); 
			Document doc = (Document) builder.build(new File("c://autotradedoc//ibtradeconfig.xml"));
			Element foo = doc.getRootElement(); //get <IBTradeConfig>
			Element ibConf = foo.getChild("IBServerConfig");  //get <IBServerConfig>
			Element stockConf = foo.getChild("StockConfig");  //get <StockConfig>
			
			ibConfig.setLocalHost(ibConf.getChild("localhost").getText());
			ibConfig.setPort(Integer.valueOf(ibConf.getChild("port").getText()));
			ibConfig.setClientId(Integer.valueOf(ibConf.getChild("clientid").getText()));
			ibConfig.setAccount(ibConf.getChild("account").getText());
			ibConfig.setMaxTryTimes(Integer.valueOf(ibConf.getChild("maxtrytimes").getText()));
			
			stockConfig.setStockSymbol(stockConf.getChild("stocksymbol").getText());
			stockConfig.setOrderType(stockConf.getChild("ordertype").getText());
			stockConfig.setSecurityType(stockConf.getChild("securitytype").getText());
			stockConfig.setStockExchange(stockConf.getChild("stockexchange").getText());
			stockConfig.setPrimaryExchange(stockConf.getChild("primaryexchange").getText());
			stockConfig.setStockCurrency(stockConf.getChild("stockcurrency").getText());
			stockConfig.setStockExpiry(Double.valueOf(stockConf.getChild("stockexpiry").getText()));
			stockConfig.setOrderQuantity(Integer.valueOf(stockConf.getChild("orderquantity").getText()));
			
		} catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	public void ibConnect() {
		
		if (ibConfig.getLocalHost().length() == 0) return;
		
		wrapper = new MyEWrapperImpl();
		
        m_client = wrapper.getClient();
        m_signal = wrapper.getSignal();
        
        m_client.eConnect(ibConfig.getLocalHost(), ibConfig.getPort(), ibConfig.getClientId());
        
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
            Thread.sleep(1000);
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	public void ibDisConnect() {
		
		m_client.eDisconnect();
		reader = null;
		wrapper = null;
	}
	
	public void searchContractByDetail() {
		
		Contract contract = new Contract();
		contract.symbol(stockConfig.getStockSymbol());
		contract.secType(stockConfig.getSecurityType());
		contract.currency(stockConfig.getStockCurrency());
		contract.exchange(stockConfig.getStockExchange());
		contract.primaryExch(stockConfig.getPrimaryExchange());

		m_client.reqContractDetails(17001,contract);
	}
	
	public void searchAllContractBySymbol() {
		
		m_client.reqMatchingSymbols(211, stockConfig.getStockSymbol());
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
	
}
