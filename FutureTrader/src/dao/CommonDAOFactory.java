package dao;

public class CommonDAOFactory {

	public static CommonDAO getCommonDAO () {
		return new CommonDAOSQL();
	}

	/*
	//table

	//order_sign
	---------------------------------------------------------------------------------------------------
	date   | time   | setting | orderidinib | order_state | action | limit_price | tick   | profit_limit_price | tick_profit | limit_filled_price | profit_limit_filled_price
	String | String | String  | int         | String      | String | double      | double | double             | double      | double             | double
	---------------------------------------------------------------------------------------------------
	yyyy/MM/dd, HH:mm:ss, 10, s1, BUY, 100.00, 2.0, 102.00, 4.00, 100.25, 102.25



	//setting_active
	---------------------------------------------------------------------------------------------------
	setting | active
	String  | int
	---------------------------------------------------------------------------------------------------
	s1, 1
	s2, 2



	//setting
	---------------------------------------------------------------------------------------------------
	setting | start_time| end_time | order_index | limit_change | tick   | profit_limit_change
	String  | String    | String   | int         | double       | double | double
	---------------------------------------------------------------------------------------------------



	//close_zone
	---------------------------------------------------------------------------------------------------
	zone    | x   | y   | active
	String  | int | int | int
	---------------------------------------------------------------------------------------------------
	z1, 300, 200, 1

	*/
}
