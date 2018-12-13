package tool;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import systemenum.SystemEnum;
import systemenum.SystemEnum.Trend;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import config.SystemConfig;
import entity.Zone;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;


public class Util {
	
	private static BufferedReader bReader;

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
	
	public static Enum<SystemEnum.Color> getColorEnumByColorRGB(int red, int green, int blue) {
		
		/*
		 * green(G):	0,102,102     --->  for stock up
     		red(R):	50,0,0        --->  for stock down
     		yellow(Y): 153,102,0     --->  for volume_1
     		white(W):  102,102,102   --->  for volume_2
     		black(B):	0,0,0         --->  for volume_3
		*/
		if (red == 0 && green == 102 && blue == 102) {
			return SystemEnum.Color.Green;
		} else if (red == 50 && green == 0 && blue == 0) {
			return SystemEnum.Color.Red;
		} else if (red == 153 && green == 102 && blue == 0) {
			return SystemEnum.Color.Yellow;
		} else if (red == 102 && green == 102 && blue == 102) {
			return SystemEnum.Color.White;
		} else if (red == 0 && green == 0 && blue == 0) {
			return SystemEnum.Color.Black;
		} else {
			return SystemEnum.Color.Default;
		}
	}
	
	public static void getImagePixel(String image) {
		int[] rgb = new int[3];
		File file = new File(image);
		BufferedImage bi = null;
		try {
			bi = ImageIO.read(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		int width = bi.getWidth();
		int height = bi.getHeight();
		int minx = bi.getMinX();
		int miny = bi.getMinY();
		for (int i = minx; i < width; i++) {
			for (int j = miny; j < height; j++) {
				int pixel = bi.getRGB(i, j); // 下面三行代码将一个数字转换为RGB数字
				rgb[0] = (pixel & 0xff0000) >> 16;
				rgb[1] = (pixel & 0xff00) >> 8;
				rgb[2] = (pixel & 0xff);
				System.out.println("i=" + i + ",j=" + j + ":(" + rgb[0] + ","
						+ rgb[1] + "," + rgb[2] + ")");
			}
		}
	}

	
	public static int getScreenPixel(int x, int y)  { // 函数返回值为颜色的RGB值(负值)。
		
		try {

			Robot rb = null; // java.awt.image包中的类，可以用来抓取屏幕，即截屏。
			rb = new Robot();
			Toolkit tk = Toolkit.getDefaultToolkit(); // 获取缺省工具包
			Dimension di = tk.getScreenSize(); // 屏幕尺寸规格
			System.out.println(di.width);
			System.out.println(di.height);
			Rectangle rec = new Rectangle(0, 0, di.width, di.height);
			BufferedImage bi = rb.createScreenCapture(rec);
			int pixelColor = bi.getRGB(x, y);
	 
			return pixelColor;
			//return 16777216 + pixelColor; // pixelColor的值为负，经过实践得出：加上颜色最大值就是实际颜色值。
		} catch (AWTException e) {
			e.printStackTrace();
		}
		
		return 0;
	}
	
	public static void createExcel(ArrayList<String> sheetList, ArrayList<Map<String, List<String>>> mapList, String[] strArray, String path) {
        // 第一步，创建一个webbook，对应一个Excel文件
        HSSFWorkbook wb = new HSSFWorkbook();
        // 第二步，在webbook中添加一个sheet,对应Excel文件中的sheet
        
        for (int sheetIndex = 0; sheetIndex < sheetList.size(); sheetIndex++) {
        
        	Map<String, List<String>> map = mapList.get(sheetIndex);
        	if(map.isEmpty()) continue;

        	String sheetName = sheetList.get(sheetIndex);
        	HSSFSheet sheet = wb.createSheet(sheetName);
        	sheet.setDefaultColumnWidth(15);// 默认列宽
        	// 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
        	HSSFRow row = sheet.createRow((int) 0);
        	// 第四步，创建单元格，并设置值表头 设置表头居中
        	HSSFCellStyle style = wb.createCellStyle();
        	// 创建一个居中格式
        	//     style.setAlignment(HSSFCellStyle.ALIGN_CENTER);

        	// 添加excel title
        	HSSFCell cell = null;
        	for (int i = 0; i < strArray.length; i++) {
        		cell = row.createCell((short) i);
        		cell.setCellValue(strArray[i]);
        		cell.setCellStyle(style);
        	}

        	// 第五步，写入实体数据 实际应用中这些数据从数据库得到,list中字符串的顺序必须和数组strArray中的顺序一致
        	int i = 0;
        	for (String str : map.keySet()) {
        		row = sheet.createRow((int) i + 1);
        		List<String> list = map.get(str);

        		// 第四步，创建单元格，并设置值
        		for (int j = 0; j < strArray.length; j++) {
        			row.createCell((short) j).setCellValue(list.get(j));
        		}

        		i++;
        	}
        }

		// 第六步，将文件存到指定位置
		try {
			FileOutputStream fout = new FileOutputStream(SystemConfig.DOC_PATH + 
        		"//trendprofit//" +
        		Util.getDateStringByDateAndFormatter(new Date(), "yyyyMMdd") +  
        		".xls");
			wb.write(fout);
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	
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

	public static void createScreenShotByRect(Rectangle rect, String filepath, String filetype) {
		
		try {
			BufferedImage image = new Robot().createScreenCapture(rect);
			ImageIO.write(image, filetype, new File(filepath));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    public static String getStringByScreenShotPng(String docpath, String filename) {
    	/*
    	 //zoom on
        try {
        	BufferedImage src = ImageIO.read(new File(docpath+"//"+filename)); // 读入文件
            int width = src.getWidth(); // 得到源图宽
            int height = src.getHeight(); // 得到源图长
            // 放大
            
            width = width / 2;
            height = height / 2;
            Image image = src.getScaledInstance(width, height, Image.SCALE_DEFAULT);
            BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics g = tag.getGraphics();
            g.drawImage(image, 0, 0, null); // 绘制缩小后的图
            g.dispose();
            ImageIO.write(tag, "PNG", new File(docpath+"//"+filename));// 输出到文件流
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
        ITesseract instance = new Tesseract();
        File directory = new File(docpath);
        String courseFile = null;

        try {
            courseFile = directory.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        instance.setDatapath(courseFile + "//tessdata");
        instance.setLanguage("eng");

        String result = null;
        try {

        	File file = new File(docpath + "//" + filename);
            result =  instance.doOCR(file);
            return result;
        } catch (TesseractException e) {
            e.printStackTrace();
        }
        return result;
    }
    
    public static double getPriceByString(String str) {
    	
    	if(str == null || str.length() == 0) return 0;
    	
    	try {

        	str = str.replace("\n","");
        	str = str.replace(" ","");
        	if (str == null || str.length() == 0) return 0;
        	if (str.contains(",")) { 
        		str = str.replace(",", ".");
        	}
        	if (str.contains(".")) {
        		String[] sourceStrArray = str.split("\\.");
        		StringBuilder sBuilder = new StringBuilder(sourceStrArray[0]);
        		if(sourceStrArray.length>1) {
        			sBuilder.append("."+sourceStrArray[1]);
        		}
        		return Double.valueOf(sBuilder.toString()).doubleValue();
        	} else {
        		StringBuilder sb = new StringBuilder(str);
        		sb.insert(str.length()-2,".");
        		str = sb.toString();
        	}
        	
        	return 0;
        	
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	return 0;
    }
    
    public static ArrayList<String[]> readCSVFile(String filename) {

    	ArrayList<String[]> resultList = new ArrayList<String[]>();
    	
    	String path = SystemConfig.DOC_PATH + 
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
    
    public static boolean createFile(String destFileName) {
        File file = new File(destFileName);
        if(file.exists()) {
            System.out.println("创建单个文件" + destFileName + "失败，目标文件已存在！");
            return false;
        }
        if (destFileName.endsWith(File.separator)) {
            System.out.println("创建单个文件" + destFileName + "失败，目标文件不能为目录！");
            return false;
        }
        //判断目标文件所在的目录是否存在
        if(!file.getParentFile().exists()) {
            //如果目标文件所在的目录不存在，则创建父目录
            System.out.println("目标文件所在目录不存在，准备创建它！");
            if(!file.getParentFile().mkdirs()) {
                System.out.println("创建目标文件所在目录失败！");
                return false;
            }
        }
        //创建目标文件
        try {
            if (file.createNewFile()) {
                System.out.println("创建单个文件" + destFileName + "成功！");
                return true;
            } else {
                System.out.println("创建单个文件" + destFileName + "失败！");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("创建单个文件" + destFileName + "失败！" + e.getMessage());
            return false;
        }
    }
   
   
    public static boolean createDir(String destDirName) {
        File dir = new File(destDirName);
        if (dir.exists()) {
            System.out.println("创建目录" + destDirName + "失败，目标目录已经存在");
            return false;
        }
        if (!destDirName.endsWith(File.separator)) {
            destDirName = destDirName + File.separator;
        }
        //创建目录
        if (dir.mkdirs()) {
            System.out.println("创建目录" + destDirName + "成功！");
            return true;
        } else {
            System.out.println("创建目录" + destDirName + "失败！");
            return false;
        }
    }
    
    public static Zone getZoneByZoneName(String name) {
    	
    	if(name == null || name.length() < 3) return null;

    	Zone z = new Zone();
    	z.setZone(name);
    	
    	String zArea = Character.toString(name.charAt(0));
    	int zXIndex = Integer.valueOf(Character.toString(name.charAt(2)))-1;
    	int zYIndex = Integer.valueOf(Character.toString(name.charAt(1)))-1;
    	
    	switch(zArea) {
    		case "A" :
    			z.setxCoord(SystemConfig.A_X[zXIndex]);
    			break;
    		case "B" :
    			z.setxCoord(SystemConfig.B_X[zXIndex]);
    			break;
    		case "C" :
    			z.setxCoord(SystemConfig.C_X[zXIndex]);
    			break;
    		case "D" :
    			z.setxCoord(SystemConfig.D_X[zXIndex]);
    			break;
    		case "E" :
    			z.setxCoord(SystemConfig.E_X[zXIndex]);
    			break;
    		case "F" :
    			z.setxCoord(SystemConfig.F_X[zXIndex]);
    			break;
    		default:
    			z.setxCoord(SystemConfig.G_X[zXIndex]);
    			break;
    	}
    	z.setyCoord(SystemConfig.ZONE_Y[zYIndex]);
    	
    	return z;
    }
}
