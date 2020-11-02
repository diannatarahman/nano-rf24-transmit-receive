package gui;

import java.awt.Dimension;

public final class Dimensions {

	private Dimensions() {
	}
	
	public static Dimension combine(Dimension d1, Dimension d2, boolean parallelH, int distance,
			int wOffset, int hOffset) {
		long w, h;
		if (parallelH) {
			w = d1.width + (long) d2.width + (long) distance;
			h = (d1.height > d2.height) ? d1.height : d2.height;
		}
		else {
			h = d1.height + (long) d2.height + (long) distance;
			w = (d1.width > d2.width) ? d1.width : d2.width;
		}
		w += (long) wOffset;
		h += (long) hOffset;
		if (w > Integer.MAX_VALUE)
			w = Integer.MAX_VALUE;
		if (h > Integer.MAX_VALUE)
			h = Integer.MAX_VALUE;
		return new Dimension((int) w, (int) h);
	}
	
	public static Dimension combine(Dimension d1, Dimension d2, boolean parallelH, int distance) {
		return combine(d1, d2, parallelH, distance, 0, 0);
	}
	
	public static Dimension combine(Dimension d1, Dimension d2, boolean parallelH) {
		return combine(d1, d2, parallelH, 0, 0, 0);
	}
	
	public static Dimension addOffset(Dimension d, int wOffset, int hOffset) {
		long w = d.width + (long) wOffset, h =  d.height + (long) hOffset;
		if (w > Integer.MAX_VALUE)
			w = Integer.MAX_VALUE;
		if (h > Integer.MAX_VALUE)
			h = Integer.MAX_VALUE;
		return new Dimension((int) w, (int) h);
	}
	
	public static Dimension max(Dimension d1, Dimension d2) {
		int w = (d1.width > d2.width) ? d1.width : d2.width,
				h = (d1.height > d2.height) ? d1.height : d2.height;
		return new Dimension(w, h);
	}
	
	public static Dimension scale(Dimension d, double ws, double hs) {
		return new Dimension((int) (d.width*ws), (int) (d.height*hs));
	}
	
	public static Dimension scale(Dimension d, double scale) {
		return scale(d, scale, scale);
	}
	
	public static double getFitScale(Dimension d, Dimension bounds) {
		double w = d.width, h = d.height;
		double ws = bounds.width/w, hs = bounds.height/h;
		return (ws < hs) ? ws : hs;
	}
	
	public static Dimension scaleFit(Dimension d, Dimension bounds) {
		double scale = getFitScale(d, bounds);
		return scale(d, scale);
	}
	
	public static Dimension add(Dimension d1, Dimension d2){
		long w = d1.width + (long) d2.width, h =  d1.height + (long) d2.height;
		if (w > Integer.MAX_VALUE)
			w = Integer.MAX_VALUE;
		if (h > Integer.MAX_VALUE)
			h = Integer.MAX_VALUE;
		return new Dimension((int) w, (int) h);
	}
	
	public static Dimension subtract(Dimension d1, Dimension d2){
		long w = d1.width - (long) d2.width, h =  d1.height - (long) d2.height;
		if (w > Integer.MAX_VALUE)
			w = Integer.MAX_VALUE;
		if (h > Integer.MAX_VALUE)
			h = Integer.MAX_VALUE;
		return new Dimension((int) w, (int) h);
	}

}
