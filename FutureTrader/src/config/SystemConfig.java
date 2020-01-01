package config;

public class SystemConfig {
	public static String DOC_PATH = "c://futuretrader";
	
	public static String PRICE_IMG_NAME = "swim_price.png";
	public static String DB_NAME = "lttrade.db";
	public static String IB_CONFIG_NAME = "ibtradeconfig.xml";
	

	public static String LOGIC_MODE = "1";
	
	//PendingSubmit - ��ʾ���Ѿ������˶���������δ�ӵ����Զ���Ŀ�ĵؽ��յ�ȷ����Ϣ�� 
	//ע���������״̬������TWS���͵ģ�Ӧ����API���������Ϊ����������ʱ������
	public static String IB_ORDER_STATE_PendingSubmit = "PendingSubmit"; 
	
	//PendingCancel - ��ʾ���Ѿ�������ȡ�����������󣬵���δ�յ����Զ���Ŀ�ĵص�ȡ��ȷ�ϡ� 
	//��ʱ����Ķ���û�б�ȷ��ȡ���� �����ȡ������ȴ��ڼ䡣��Ķ������п��ܵõ�ִ�С� 
	//ע���������״̬������TWS���͵ģ�Ӧ����API���������Ϊ����������ʱ������
	public static String IB_ORDER_STATE_PendingCancel = "PendingCancel"; 
	
	//PreSubmitted - ��ʾģ�ⶨ�������Ѿ���IBϵͳ���գ�����δ��ѡ�С�
	//������������IBϵͳ��ֱ��ѡ�����������㡣 ��ʱ�������������͵�ָ���Ķ���Ŀ�ĵء�
	public static String IB_ORDER_STATE_PreSubmitted = "PreSubmitted"; 
	
	//Submitted - ��ʾ��Ķ����Ѿ�������Ŀ�ĵؽ��ܣ������ڹ����С�
	public static String IB_ORDER_STATE_Submitted = "Submitted"; 
	
	//Cancelled - ��ʾ�㶨����ʣ�ಿ���ѱ�IBϵͳȷ��ȡ���ˡ� ��Ҳ����IB��Ŀ�ĵؾܾ���Ķ���ʱ������
	public static String IB_ORDER_STATE_Cancelled = "Cancelled"; 
	
	//Filled - ��ʾ�����ѱ�ȫ��ִ�С�
	public static String IB_ORDER_STATE_Filled = "Filled"; 
	
	//Inactive - ��ʾ�����ѱ�ϵͳ���գ�ģ�ⶨ�������������գ����ض�������������ϵͳ��������������ԭ��Ŀǰ�������ڷǹ���״̬��
	public static String IB_ORDER_STATE_Inactive = "Inactive"; 
	
	
	//when parent order
	public static String IB_ORDER_STATE_Finished = "Finished";
}
