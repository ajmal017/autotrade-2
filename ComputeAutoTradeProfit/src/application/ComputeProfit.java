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
import tool.Util;


public class ComputeProfit {
	
	private static HSSFWorkbook work;
	
	private static String[] excelTitle() {
        String[] strArray = { 
        		"time", 
        		"scenario", 
        		"trend", 
        		"green", 
        		"red", 
        		"price_swim", 
        		"price_ib", 
        		"price_swim - price_ib", 
        		"profit_swim", 
        		"profit_ib",
        		"profit_swim - profit_ib", 
        		"desc"};
        return strArray;
    }
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String filePath = "c://autotradedoc//trendprofit//" +
    		Util.getDateStringByDateAndFormatter(new Date(), "yyyyMMdd") +  
    		".xls";
		
		try {
			work = new HSSFWorkbook(new FileInputStream(filePath));
			int sheetCount = work.getNumberOfSheets();
			
			ArrayList<String> sheetList = new ArrayList<String>();
	    	ArrayList<Map<String, List<String>>> mapList = new ArrayList<Map<String, List<String>>>();
			
			for(int sheetIndex = 0; sheetIndex < sheetCount; sheetIndex++) {
				
				HSSFSheet sheet = work.getSheetAt(sheetIndex);
				sheetList.add(sheet.getSheetName());
				
				double totalProfitSwim = 0;
				double totalProfitIB = 0;
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
					ts.setPriceSwim(Double.valueOf(Util.getStrValueByCell(cell)));
					
					cell = row.getCell((short)6);
					ts.setPriceIB(Double.valueOf(Util.getStrValueByCell(cell)));
					
					if(i > 1) {
						
						TrendSign preSign = newTrendList.get(i-2);
						ts.setProfitSwim(Util.getProfit(preSign.getPriceSwim(), ts.getPriceSwim(), preSign.getTrend()));
					}
					
					cell = row.getCell((short)8);
					ts.setProfitIB(Double.valueOf(Util.getStrValueByCell(cell)));
					
					cell = row.getCell((short)9);
					ts.setDesc(cell == null?"":cell.getStringCellValue());
					
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
				    //price
				    if(sign.getPriceSwim()!=0) {
				    	params.add(sign.getPriceSwim()+"");
				    } else {
				    	params.add("0");
				    }
				    if(sign.getPriceIB()!=0) {
				    	params.add(sign.getPriceIB()+"");
				    } else {
				    	params.add("0");
				    }
				    
				    double temp1 = sign.getPriceSwim() - sign.getPriceIB();
				    if(temp1!=0) {
				    	params.add(temp1+"");
				    } else {
				    	params.add("0");
				    }
				    
				    //profit
				    if(sign.getProfitSwim()!=0) {
				    	params.add(sign.getProfitSwim()+"");
				    } else {
				    	params.add("0");
				    }
				    if(sign.getProfitIB()!=0) {
				    	params.add(sign.getProfitIB()+"");
				    } else {
				    	params.add("0");
				    }
				    
				    double temp2 = sign.getProfitSwim() - sign.getProfitIB();
				    if(temp2!=0) {
				    	params.add(temp2+"");
				    } else {
				    	params.add("0");
				    }
				    
				    //desc
				    params.add(sign.getDesc());
				    //map key
				    map.put((i+1) + "", params);
				}
		        
		        mapList.add(map);
			}
			
			
			Util.createExcel(sheetList, mapList, excelTitle(), filePath);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
