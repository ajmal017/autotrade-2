package service;

public class IBService implements IBCallBackInterface {

	private volatile static IBService instance; 
	
	//初始化函数
	private IBService ()  {
    	
    }

	//单例函数
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

	public void updateResult () {
		IBTest test = new IBTest();
		test.getIBResult(instance);
	}
	
	@Override
	public void IBCallBack(int result) {
		// TODO Auto-generated method stub
		System.out.print(result);
	}
	
}
