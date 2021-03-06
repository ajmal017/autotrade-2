package tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;

import systemenum.SystemEnum;
import systemenum.SystemEnum.Trend;

public class Util {

	private static BufferedReader bReader;
	
	public static String getTrendTextByEnum(Enum<SystemEnum.Trend> trend) {
		Trend t  = (Trend) trend;
		switch (t) {
		case Up:
			return "up";
		case Down:
			return "down";
		default:
			return "close";
		}
	}
	
	public static Enum<SystemEnum.Trend> getTrendEnumByText(String trend) {
		switch (trend) {
		case "up":
			return SystemEnum.Trend.Up;
		case "down":
			return SystemEnum.Trend.Down;
		default:
			return SystemEnum.Trend.Default;
		}
	}
	
	public static double getProfit(double prePrice, double newPrice, Enum<SystemEnum.Trend> preTrend) {
		
		double profit = 0;
		if (preTrend == SystemEnum.Trend.Up) {
			profit = newPrice - prePrice;
		} else if (preTrend == SystemEnum.Trend.Down) {
			profit = prePrice - newPrice;
		}
		return profit;
	}
	
	public static Date getDateByStringAndFormatter(String dateString, String formatter) {
		
		try {
			
            SimpleDateFormat sdf = new SimpleDateFormat(formatter);
            Date date = sdf.parse(dateString);
            sdf.setLenient(false);
            return date;
            
        } catch (ParseException e) {
            e.printStackTrace();
        }
		
		return null;
	}
	
	public static String getDateStringByDateAndFormatter(Date date, String formatter) {
		
		SimpleDateFormat sdf = new SimpleDateFormat(formatter);
		String str = sdf.format(date);
        return str;
	}
	
	public static String getStrValueByCell(HSSFCell cell) {
		
		CellType tCellType = cell.getCellType();
		String msg="";
		if(tCellType == CellType.NUMERIC){
		     msg =String.valueOf(cell.getNumericCellValue());
		}else{
		     msg = cell.getStringCellValue(); 
		}
		if(msg.equals("0.0")) msg = "0";
		return msg;
	}
	
	public static void createExcel(ArrayList<String> sheetList, ArrayList<Map<String, List<String>>> mapList, String[] strArray, String path) {
        
        HSSFWorkbook wb = new HSSFWorkbook();
        
        for (int sheetIndex = 0; sheetIndex < sheetList.size(); sheetIndex++) {
        
        	Map<String, List<String>> map = mapList.get(sheetIndex);
        	if(map.isEmpty()) continue;

        	String sheetName = sheetList.get(sheetIndex);
        	HSSFSheet sheet = wb.createSheet(sheetName);
        	sheet.setDefaultColumnWidth(10);
        	
        	HSSFRow row = sheet.createRow((int) 0);
        	
        	HSSFCellStyle style = wb.createCellStyle();
        	
        	HSSFCell cell = null;
        	for (int i = 0; i < strArray.length; i++) {
        		cell = row.createCell((short) i);
        		cell.setCellValue(strArray[i]);
        		cell.setCellStyle(style);
        	}

        	
        	for (int i = 0; i < map.size(); i++) {
        		row = sheet.createRow((int) i + 1);
        		List<String> list = map.get(String.valueOf(i+1));
        		
        		for (int j = 0; j < strArray.length; j++) {
        			row.createCell((short) j).setCellValue(list.get(j));
        			row.setRowStyle(style);
        		}
        	}
        }
        
		try {
			FileOutputStream fout = new FileOutputStream("c://autotradedoc_vol//trendprofit//" +
		    		Util.getDateStringByDateAndFormatter(new Date(), "yyyyMMdd") +  
		    		".xls");
			wb.write(fout);
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static ArrayList<String[]> readCSVFile(String filename) {

    	ArrayList<String[]> resultList = new ArrayList<String[]>();
    	
    	String path = "c://autotradedoc_vol" + 
				"//csv//" + 
				filename + 
				".csv";
    	File file = new File(path);
    	if (!file.exists()) return resultList;
    	
    	try {
    		bReader = new BufferedReader(new FileReader(path));
    		String line = null;
    		while((line=bReader.readLine())!=null && line.length() > 0){
    			String item[] = line.split(",");
    			resultList.add(item);              
    		}
        	return resultList;
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	return resultList;
    }
}
