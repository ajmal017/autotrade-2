package systemenum;

public interface SystemEnum {

	enum Color implements SystemEnum {
		
		Default,
		Green,
		Red,
		Yellow,
		White,
		Black
	}
	
	enum Active implements SystemEnum {
		
		Off,
		On
	}
	
	enum Trend implements SystemEnum {
		
		Default,
		Up,
		Down
	}
	
	enum DateType implements SystemEnum {
		
		String,
		Date
	}
	
	enum OrderAction implements SystemEnum {
		
		Default,
		Buy,
		Sell
	}
}
