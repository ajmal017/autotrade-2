package systemenum;

public interface SystemEnum {

	enum OrderAction implements SystemEnum {
		
		Default,
		Buy,
		Sell
	}
	
	enum IbAccountType implements SystemEnum {
		
		Live,
		Paper
	}
}
