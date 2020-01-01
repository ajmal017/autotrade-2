package config;

public class SystemConfig {
	public static String DOC_PATH = "c://futuretrader";
	
	public static String PRICE_IMG_NAME = "swim_price.png";
	public static String DB_NAME = "lttrade.db";
	public static String IB_CONFIG_NAME = "ibtradeconfig.xml";
	

	public static String LOGIC_MODE = "1";
	
	//PendingSubmit - 表示你已经传递了定单，但还未接到来自定单目的地接收的确认信息。 
	//注：这个定单状态不是由TWS发送的，应该由API设计人设置为当定单发送时发出。
	public static String IB_ORDER_STATE_PendingSubmit = "PendingSubmit"; 
	
	//PendingCancel - 表示你已经发送了取消定单的请求，但还未收到来自定单目的地的取消确认。 
	//此时，你的定单没有被确认取消。 在你的取消请求等待期间。你的定单仍有可能得到执行。 
	//注：这个定单状态不是由TWS发送的，应该由API设计人设置为当定单发送时发出。
	public static String IB_ORDER_STATE_PendingCancel = "PendingCancel"; 
	
	//PreSubmitted - 表示模拟定单类型已经被IB系统接收，但还未被选中。
	//定单被保持在IB系统中直到选择条件被满足。 届时，定单将被发送到指定的定单目的地。
	public static String IB_ORDER_STATE_PreSubmitted = "PreSubmitted"; 
	
	//Submitted - 表示你的定单已经被定单目的地接受，并处于工作中。
	public static String IB_ORDER_STATE_Submitted = "Submitted"; 
	
	//Cancelled - 表示你定单的剩余部分已被IB系统确认取消了。 这也会在IB或目的地拒绝你的定单时发生。
	public static String IB_ORDER_STATE_Cancelled = "Cancelled"; 
	
	//Filled - 表示定单已被全部执行。
	public static String IB_ORDER_STATE_Filled = "Filled"; 
	
	//Inactive - 表示定单已被系统接收（模拟定单）或交易所接收（本地定单），但由于系统、交易所或其它原因，目前定单处于非工作状态。
	public static String IB_ORDER_STATE_Inactive = "Inactive"; 
	
	
	//when parent order
	public static String IB_ORDER_STATE_Finished = "Finished";
}
