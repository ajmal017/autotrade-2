package application;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.CellType;

import entity.TrendSign;
import javafx.application.Platform;
import tool.Util;


public class ComputeProfit {
	
	private static HSSFWorkbook work;
	
	private static String[] HALF_HOURS = {"07:00","07:30",
										  "08:00","08:30",
										  "09:00","09:30",
										  "10:00","10:30",
										  "11:00","11:30",
										  "12:00","12:30",
										  "13:00"};
	
	private static String[] excelTitle() {
        String[] strArray = { 
        		"time", 
        		"scenario", 
        		"trend", 
        		"green", 
        		"red", 
        		"white",
        		"price_swim", 
        		"price_ib", 
        		"price_swim - price_ib", 
        		"profit_swim", 
        		"profit_ib",
        		"profit_swim - profit_ib", 
        		"quantity",
        		"half_hour_profit_swim",
        		"half_hour_profit_ib",
        		"desc"};
        return strArray;
    }
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String filePath = "c://autotradedoc_vol//trendprofit//" +
    		Util.getDateStringByDateAndFormatter(new Date(), "yyyyMMdd") +  
    		".xls";
		
		try {
			work = new HSSFWorkbook(new FileInputStream(filePath));
			int sheetCount = work.getNumberOfSheets();
			
			ArrayList<String> sheetList = new ArrayList<String>();
	    	ArrayList<Map<String, List<String>>> mapList = new ArrayList<Map<String, List<String>>>();
			
			for (int sheetIndex = 0; sheetIndex < sheetCount; sheetIndex++) {
				
				int finishedHalfHourCount = 0;
				HSSFSheet sheet = work.getSheetAt(sheetIndex);
				sheetList.add(sheet.getSheetName());
				
				double totalProfitSwim = 0;
				double totalProfitIB = 0;
				double halfHourProfitSwim = 0;
				double halfHourProfitIB = 0;
				ArrayList<TrendSign> newTrendList = new ArrayList<TrendSign>();
				int rowNo = sheet.getLastRowNum()+1;
				TrendSign ts = new TrendSign();
				for (int i = 1; i < rowNo; i++) {
					ts = null;
					ts = new TrendSign();
					HSSFRow row = sheet.getRow(i);
					HSSFCell cell;
					cell = row.getCell((short)0);
					String timevalue;
//					System.out.println("sheetIndex: " + sheetIndex + " i: "+i);
					if(cell.getCellType() == CellType.STRING) {
						timevalue = cell.getStringCellValue();
					} else {
						SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		                Date time = HSSFDateUtil.getJavaDate(cell.getNumericCellValue());
		                timevalue = sdf.format(time);
					}
					
					String date = Util.getDateStringByDateAndFormatter(new Date(), "yyyyMMdd");
					ts.setTime(Util.getDateByStringAndFormatter(date+timevalue, "yyyyMMddHH:mm:ss"));
					
					cell = row.getCell((short)1);
					ts.setScenario(cell.getStringCellValue());
					
					cell = row.getCell((short)2);
					ts.setTrendText(cell.getStringCellValue());
					ts.setTrend(Util.getTrendEnumByText(ts.getTrendText()));
					
					cell = row.getCell((short)3);
					ts.setGreenCount(Integer.valueOf(Util.getStrValueByCell(cell)));
					
					cell = row.getCell((short)4);
					ts.setRedCount(Integer.valueOf(Util.getStrValueByCell(cell)));
					
					cell = row.getCell((short)5);
					ts.setWhiteCount(Integer.valueOf(Util.getStrValueByCell(cell)));
					
					cell = row.getCell((short)6);
					ts.setPriceSwim(Double.valueOf(Util.getStrValueByCell(cell)));
					
					cell = row.getCell((short)7);
					ts.setPriceIB(Double.valueOf(Util.getStrValueByCell(cell)));
					
					if(i > 1) {
						
						TrendSign preSign = newTrendList.get(i-2);
						ts.setProfitSwim(Util.getProfit(preSign.getPriceSwim(), ts.getPriceSwim(), preSign.getTrend()));
					}
					
					cell = row.getCell((short)9);
					ts.setProfitIB(Double.valueOf(Util.getStrValueByCell(cell)));
					
					cell = row.getCell((short)10);
					ts.setQuantity(Integer.valueOf(Util.getStrValueByCell(cell)));
					
					cell = row.getCell((short)11);
					ts.setDesc(cell == null?"":cell.getStringCellValue());
					
					StringBuilder str = new StringBuilder(Util.getDateStringByDateAndFormatter(new Date(), "yyyyMMdd"));
		    		str.append(HALF_HOURS[finishedHalfHourCount]);
					if(Util.getDateByStringAndFormatter(str.toString(), "yyyyMMddHH:mm").before(ts.getTime())) {
						TrendSign lastTS = newTrendList.get(newTrendList.size()-1);
						lastTS.setHalfHourProfitSwim(halfHourProfitSwim);
						lastTS.setHalfHourProfitIB(halfHourProfitIB);
						halfHourProfitSwim = 0;
						halfHourProfitIB = 0;
						finishedHalfHourCount++;
					}
					halfHourProfitSwim += ts.getProfitSwim();
					halfHourProfitIB += ts.getProfitIB();
					
					totalProfitSwim += ts.getProfitSwim();
					totalProfitIB += ts.getProfitIB();
					
					
					newTrendList.add(ts);
				}
				
				//add total line
				TrendSign lastts = new TrendSign();
				lastts.setTime(ts.getTime());
				lastts.setProfitSwim(totalProfitSwim);
				lastts.setProfitIB(totalProfitIB);
				lastts.setDesc("Total");
				newTrendList.add(lastts);
				
				//create map
				Map<String, List<String>> map = new HashMap<String, List<String>>();
				SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
		        for (int i = 0; i < newTrendList.size(); i++) {
		        	TrendSign sign = newTrendList.get(i);
				    ArrayList<String> params = new ArrayList<String>();
				    
				    //time
				    if(sign.getTime() != null) {
				    	params.add(df.format(sign.getTime()));
				    } else {
				    	params.add("");
				    }
				    //scenario
				    params.add(sign.getScenario());
				    //trend
				    params.add(sign.getTrendText());
				    //count
				    if(sign.getGreenCount()>0) {
				    	params.add(sign.getGreenCount()+"");
				    } else {
				    	params.add("0");
				    }
				    if(sign.getRedCount()>0) {
				    	params.add(sign.getRedCount()+"");
				    } else {
				    	params.add("0");
				    }
				    if(sign.getWhiteCount()>0) {
				    	params.add(sign.getWhiteCount()+"");
				    } else {
				    	params.add("0");
				    }
				    //price
				    if(sign.getPriceSwim()!=0) {
				    	params.add(String.format("%.2f",sign.getPriceSwim()));
				    } else {
				    	params.add("0");
				    }
				    if(sign.getPriceIB()!=0) {
				    	params.add(String.format("%.2f",sign.getPriceIB()));
				    } else {
				    	params.add("0");
				    }
				    
				    double temp1 = sign.getPriceSwim() - sign.getPriceIB();
				    if(temp1!=0) {
				    	params.add(String.format("%.2f",temp1));
				    } else {
				    	params.add("0");
				    }
				    
				    //profit
				    if(sign.getProfitSwim()!=0) {
				    	params.add(String.format("%.2f",sign.getProfitSwim()));
				    } else {
				    	params.add("0");
				    }
				    if(sign.getProfitIB()!=0) {
				    	params.add(String.format("%.2f",sign.getProfitIB()));
				    } else {
				    	params.add("0");
				    }
				    
				    double temp2 = sign.getProfitSwim() - sign.getProfitIB();
				    if(temp2!=0) {
				    	params.add(String.format("%.2f",temp2));
				    } else {
				    	params.add("0");
				    }
				    
				    if(sign.getQuantity()!=0) {
				    	params.add(sign.getQuantity()+"");
				    } else {
				    	params.add("0");
				    }
				    
				    if(sign.getHalfHourProfitSwim()!=0) {
				    	params.add(String.format("%.2f",sign.getHalfHourProfitSwim()));
				    } else {
				    	params.add("");
				    }
				    
				    if(sign.getHalfHourProfitIB()!=0) {
				    	params.add(String.format("%.2f",sign.getHalfHourProfitIB())+"");
				    } else {
				    	params.add("");
				    }
				    
				    
				    //desc
				    params.add(sign.getDesc());
				    //map key
				    map.put((i+1) + "", params);
				}
		        
		        mapList.add(map);
			}
			
			
			Util.createExcel(sheetList, mapList, excelTitle(), filePath);
			try {
	            Thread.sleep(1000);
	        } catch (Exception e) {
	        	e.printStackTrace();
	        }
			Platform.exit();
	        System.exit(0);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
