package service;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import entity.ColorCount;
import entity.Zone;
import systemenum.SystemEnum;

public class ZoneColorInfoService {

private volatile static ZoneColorInfoService instance; 
	
	private BufferedImage bi;
	
	private ArrayList<Zone> closeMonitorZoneList;
	
	private ZoneColorInfoService ()  {

    	this.closeMonitorZoneList = new ArrayList<Zone>();
    } 
    
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

	public void loadCloseMonitorZoneListWithDefaultColor(ArrayList<Zone> zoneList) {
		
		if (closeMonitorZoneList.size() > 0) {
			closeMonitorZoneList.clear();
		}
		
		if(zoneList == null || zoneList.size() == 0) return;
		
		for (Zone zone : zoneList) {
			closeMonitorZoneList.add(zone);
		}
	}
	
	public void updateCloseMonitorZoneColorByTimer() {
		
        try {
        	
			Robot rb = null;
			rb = new Robot();
			Toolkit tk = Toolkit.getDefaultToolkit();
			Dimension di = tk.getScreenSize();
			Rectangle rec = new Rectangle(0, 0, di.width, di.height);
			setBi(rb.createScreenCapture(rec));

			int[] rgb = new int[3];
			
			for (Zone z: closeMonitorZoneList){
				
				int pixel = getBi().getRGB(z.getxCoord(), z.getyCoord());
				rgb[0] = (pixel & 0xff0000) >> 16;
				rgb[1] = (pixel & 0xff00) >> 8;
				rgb[2] = (pixel & 0xff);
				
				z.setColor(tool.Util.getColorEnumByColorRGB(rgb[0], rgb[1], rgb[2]));
				//test
//				z.setColor(SystemEnum.Color.Red);
			}
			
			return;
			
		} catch (AWTException e) {
			e.printStackTrace();
		}
        
	}
	
    public ColorCount getColorCountByCloseZoneList() {

    	ColorCount count =  new ColorCount();
    	ZoneColorInfoService colorService = ZoneColorInfoService.getInstance();
    	for (Zone zone : colorService.getCloseMonitorZoneList()) {
    		if (zone.getColor() == SystemEnum.Color.Green) {
    			count.setGreen(count.getGreen()+1);
    		} else if (zone.getColor() == SystemEnum.Color.Red) {
    			count.setRed(count.getRed()+1);
    		} else if (zone.getColor() == SystemEnum.Color.White) {
    			count.setWhite(count.getWhite()+1);
    		} else if (zone.getColor() == SystemEnum.Color.Yellow) {
    			count.setYellow(count.getYellow()+1);
    		} else {
    			count.setOther(count.getOther()+1);
    		}
    	}
    	return count;
    }
	
	public ArrayList<Zone> getCloseMonitorZoneList() {
		return closeMonitorZoneList;
	}

	public void setCloseMonitorZoneList(ArrayList<Zone> closeMonitorZoneList) {
		this.closeMonitorZoneList = closeMonitorZoneList;
	}

	
    public BufferedImage getBi() {
		return bi;
	}

	public void setBi(BufferedImage bi) {
		this.bi = bi;
	}


}
