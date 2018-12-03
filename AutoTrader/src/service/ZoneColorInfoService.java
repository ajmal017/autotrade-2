package service;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import application.AutoTradeCallBackInterface;
import entity.DailyScenarioRefresh;
import entity.Scenario;
import entity.Zone;
import systemenum.SystemEnum;

public class ZoneColorInfoService {
	
	private volatile static ZoneColorInfoService instance; 
	
	private Hashtable<String,Zone> zoneColors; //only working zone 

    //初始化函数
    private ZoneColorInfoService ()  {
    	
    	this.zoneColors = new Hashtable<String,Zone>();
    } 
    
    //单例函数
	public static ZoneColorInfoService getInstance() {  
    	if (instance == null) {  
    		synchronized (ZoneColorInfoService.class) {  
    			if (instance == null) {  
    				instance = new ZoneColorInfoService();  
    			}	  
    		}  
    	}  
    	return instance;  
    }

	public void reloadZoneColorsByNewZoneListWithDefaultColor(ArrayList<Zone> zoneList) {
		
		if (!getZoneColors().isEmpty()) {
			getZoneColors().clear();
		}
		
		for(Zone zone : zoneList) {
			getZoneColors().put(zone.getZone(),zone);
		}
	}
	
	public void updateZoneColorByTimer() {

		if (getZoneColors().isEmpty()) {
			return;
		}

    	int green = 0;
    	int red = 0;
        try {
        	
			Robot rb = null; // java.awt.image包中的类，可以用来抓取屏幕，即截屏。
			rb = new Robot();
			Toolkit tk = Toolkit.getDefaultToolkit(); // 获取缺省工具包
			Dimension di = tk.getScreenSize(); // 屏幕尺寸规格
			Rectangle rec = new Rectangle(0, 0, di.width, di.height);
			BufferedImage bi = rb.createScreenCapture(rec);

			int[] rgb = new int[3];
			//遍历value
			Enumeration<Zone> e = getZoneColors().elements();
			while( e. hasMoreElements() ){
				
				Zone zone = e.nextElement();
				
				int pixel = bi.getRGB(zone.getxCoord(), zone.getyCoord());
				rgb[0] = (pixel & 0xff0000) >> 16;
				rgb[1] = (pixel & 0xff00) >> 8;
				rgb[2] = (pixel & 0xff);
				
				zone.setColor(tool.Util.getColorEnumByColorRGB(rgb[0], rgb[1], rgb[2]));
				if (zone.getColor() == SystemEnum.Color.Green) {
					green ++;
				}
				if (zone.getColor() == SystemEnum.Color.Red) {
					red ++;
				}
			}
			
			return;
			
		} catch (AWTException e) {
			e.printStackTrace();
		}
        
	}
	
	public Enum<SystemEnum.Color> getColorByZone(String zone) {
		
		Zone z = zoneColors.get(zone);
		
		return z.getColor();
	}

	public Hashtable<String, Zone> getZoneColors() {
		return zoneColors;
	}
	
	public void setZoneColors(Hashtable<String, Zone> zoneColors) {
		this.zoneColors = zoneColors;
	} 
}
