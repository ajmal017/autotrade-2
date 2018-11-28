package category;

import java.awt.Color;

public class MyColor extends Color {

	private int pixel;
	
	private static final long serialVersionUID = 99999L;

	public MyColor(int pixel) {
		super(pixel);
		setPixel(pixel);
	}

	public int alpha() {
		return pixel >>> 24;
	}

	public int red() {
		return (pixel & 0xff0000) >> 16;
	}

	public int green() {
		return (pixel & 0xff00) >> 8;
	}

	public int blue() {
		return (pixel & 0xff);
	}
	
	public void setPixel (int pixel) {
		this.pixel = pixel;
	}
	
	public int getPixel() {
		return pixel;
	}
}
