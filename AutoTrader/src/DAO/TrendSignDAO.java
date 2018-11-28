package DAO;

import java.util.ArrayList;
import java.util.Date;

import entity.TrendSign;
import systemenum.SystemEnum;

public interface TrendSignDAO {

	void insertNewTrendSign(TrendSign sign);
	ArrayList<TrendSign> getTrendSignListByDate(Date date, String scenario);
	Enum<SystemEnum.Trend> getLastTrendByScenario(Date date, String scenario);
}
