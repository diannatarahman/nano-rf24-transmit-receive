package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;

import processing.HeartSignalData;

public final class HeartSignalChartPanel extends JPanel implements MouseListener,
		MouseMotionListener, FocusListener, KeyListener {
	private static final long serialVersionUID = -1030602565128462163L;
	private HeartSignalData data;
	private int tagNumber;
	private long lastTime = System.currentTimeMillis();
	private double sampleCount = 1000.0;
	private double millisecondFraction = 0.0;
	private int mouseX = Integer.MIN_VALUE, button = MouseEvent.NOBUTTON;
	private int sampleIndex = 0;
	private double secondSample = Double.NaN;
	private int selectData1 = HeartSignalData.SIGNAL;
	private int selectData2 = HeartSignalData.HEART_RATE;
	private int lastCountShow = -1;
	private double lastRange = 1.0;
	private double d = 0.0;
	private int from = 0;
	private boolean chartHighlight;
	private boolean finishButtonHighlight;
	private static final int X1 = 70, Y1 = 55, X2 = 70, Y2 = 35;
	private static final int BASELINE = 0;
	private static final int LEFT = 0;
	private static final int TOP = 1;
	private static final int RIGHT = 1;
	private static final int CENTER = 2;
	private static final Font FONT_PLAIN_TEXT = new Font("Tahoma", Font.PLAIN, 10);
	private static final Font FONT_BOLD_TEXT = new Font("Tahoma", Font.BOLD, 10);
	private static final Color SWAP_COLOR = new Color(0, 0, 128);
	private static final Color SEEK_COLOR = new Color(240, 240, 0);
	private static final Stroke CHART_BORDER_STROKE = new BasicStroke(3.0f);
	private static final Stroke CHART_STROKE = new BasicStroke(1.0f,
			BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f);
	private static final Stroke AXIS_STROKE = new BasicStroke(1.0f,
			BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f,
			new float[]{5.0f, 5.0f}, 0.0f);

	public HeartSignalChartPanel() {
		super();
		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(150+X1+X2, 100+Y1+Y2));
		setMinimumSize(new Dimension(150+X1+X2, 100+Y1+Y2));
		addMouseListener(this);
		addMouseMotionListener(this);
		addFocusListener(this);
		addKeyListener(this);
	}
	
	public void setSampleCount(int sampleCount) {
		this.sampleCount = sampleCount;
	}
	
	public int getSampleCount() {
		return (int) sampleCount;
	}
	
	public void setData(HeartSignalData data) {
		lastCountShow = -1;
		this.data = data;
		secondSample = Double.NaN;
		sampleCount = 1000.0;
		selectData1 = HeartSignalData.SIGNAL;
		selectData2 = HeartSignalData.HEART_RATE;
		if (data.isFinished()) {
			lastTime = data.getFinishTime();
			millisecondFraction = 0.0;
		}
	}
	
	public HeartSignalData getData() {
		return data;
	}

	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}
	
	public long getLastTime() {
		return lastTime;
	}

	public void setTagNumber(int tagNumber) {
		this.tagNumber = tagNumber;
	}
	
	public int getTagNumber() {
		return tagNumber;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		int w = getWidth(), h = getHeight();
		int wb = -X1-X2, hb = -Y1-Y2;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		drawGraph(g2, X1, Y1, w+wb, h+hb);
		g2.setColor(Color.BLACK);
		drawString(g2, FONT_BOLD_TEXT, X1+(w+wb)*0.5, h-5.0, CENTER, BASELINE, "Waktu (ms)");
		g2.rotate(-Math.PI/2.0);
		drawString(g2, FONT_BOLD_TEXT, -(Y1+(h+hb)*0.5), 5.0, CENTER, TOP,
				HeartSignalData.getSampleName(selectData1) +
				" (" + HeartSignalData.getSampleUnit(selectData1) + ")");
		g2.rotate(Math.PI/2.0);
		String name = "", rank = "", unit = "", category = "", age = "";
		if (data != null) {
			name = data.getPerson().getName();
			rank = data.getPerson().getRank();
			unit = data.getPerson().getUnit();
			category = data.getPerson().getCategory();
			age = data.getPerson().getAge() + " Tahun";
			if (data.isStarted()) {
				g2.setColor(Color.GRAY);
				g2.fillRect(w-X2+10, h-Y2-30, X2-20, 30);
				if (finishButtonHighlight) {
					g2.setColor(Color.RED);
				}
				else {
					g2.setColor(Color.BLACK);
				}
				g2.drawRect(w-X2+10, h-Y2-30, X2-20, 30);
				g2.setColor(Color.BLACK);
				if (data.isFinished()) {
					secondSample = data.getSample(sampleIndex, selectData2);
					g2.setColor(SWAP_COLOR);
					drawString(g2, FONT_BOLD_TEXT, w-X2*0.5, h-Y2-15, CENTER, CENTER, "Swap");
				}
				else {
					int count = (int)(lastTime-data.getStartTime())/500;
					if (count != lastCountShow) {
						secondSample = data.average(lastTime,
								500/data.getSamplingPeriod(), selectData2);
						lastCountShow = count;
					}
					drawString(g2, FONT_BOLD_TEXT, w-X2*0.5, h-Y2-15, CENTER, CENTER, "Stop");
				}
			}
		}
		g2.setColor(Color.BLACK);
		drawString(g2, FONT_PLAIN_TEXT, 5.0, 5.0, LEFT, TOP, 0.0, 5.0, 3, 2, "Nama",
				" : " + name, "Pangkat", " : " + rank, "Satuan", " : " + unit);
		drawString(g2, FONT_PLAIN_TEXT, w*0.5+5.0, 5.0, LEFT, TOP, 0.0, 5.0, 2, 2, "Kategori",
				" : " + category, "Umur", " : " + age);
		String[] s = HeartSignalData.getSampleName(selectData2).split(" ");
		drawString(g2, FONT_BOLD_TEXT, w-X2*0.5, 20.0, CENTER, TOP, 0.0, 5.0, s.length, 1, s);
		if (Double.isNaN(secondSample)) {
			drawString(g2, FONT_BOLD_TEXT, w-X2*0.5, Y1, CENTER, TOP, 0.0, 5.0, 3, 1,
					"Tidak", "ada", "data");
		}
		else {
			drawString(g2, FONT_BOLD_TEXT, w-X2*0.5, Y1, CENTER, TOP, 0.0, 5.0, 2, 1,
					String.format("%.3g", secondSample),
					HeartSignalData.getSampleUnit(selectData2));
		}
		drawString(g2, FONT_BOLD_TEXT, w-10.0, h-10.0, RIGHT, BASELINE, Integer.toString(tagNumber));
        g2.setStroke(CHART_BORDER_STROKE);
        if (isFocusOwner()) {
    		g2.setColor(Color.RED);
        }
        else {
    		g2.setColor(Color.BLACK);
        }
		g2.drawRect(0, 0, w-1, h-1);
	}

	private void drawGraph(Graphics2D g2, int x, int y, int w, int h) {
		d = (sampleCount%(int)sampleCount);
		int lastLocation = 0;
		if (data != null) {
			lastLocation = data.getLocation(lastTime);
			d -= (millisecondFraction+data.getLocationRemainder(lastTime))
					/data.getSamplingPeriod();
		}
		if (lastLocation+1 <= sampleCount) {
			d = 0.0;
		}
		int length = (int)sampleCount;
		if (d < 0.0) {
			d++;
		}
		else {
			length++;
		}
		from = 0;
		double yM = Double.NaN, ym = Double.NaN;
		if (data != null) {
			from = data.getInitIndex(lastTime, length);
			yM = data.max(lastTime, length, selectData1, true, true);
			ym = data.max(lastTime, length, selectData1, false, true);
		}
		if (yM-ym == 0.0) {
			yM += lastRange/2.0;
			ym -= lastRange/2.0;
		}
		else if (!Double.isNaN(yM-ym)) {
			lastRange = yM-ym;
		}
		double xs = w/sampleCount, ys = h/((yM - ym)*1.2);
		int[] dx = step(xs), dy = step(ys);
		double ey = 0.1*(yM - ym);
		AffineTransform at = AffineTransform.getTranslateInstance(x, y+h);
		at.scale(xs, -ys);
		at.translate(0.0, -ym+ey);
		g2.setColor(Color.LIGHT_GRAY);
		g2.fillRect(x, y, w, h);
		Stroke curveStroke = new BasicStroke(Math.max(Math.min(3.0f, (float)(xs/10.0)), 1.0f),
				BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f);
		List<Path2D> pathList = new ArrayList<Path2D>();
		Path2D singlePath = new Path2D.Double();
		boolean b = false, signalDetected = false;
		int lastIndex = 0;
		double yStart = Double.NaN, yEnd = Double.NaN;
		double lastX = d-1.0, lastY = Double.NaN;
		if (data != null) {
			int temp = from > 0 ? from-1 : 0;
			yStart = data.getSample(temp, selectData1);
			lastY = yStart;
			lastX = temp-from+d;
			yEnd = data.getSample(lastLocation+1, selectData1);
		}
		for (int i = 0; i < length; i++) {
			double di = i+d;
			double value = Double.NaN;
			if (data != null) {
				value = data.getSample(from+i, selectData1);
			}
			boolean b1 = !Double.isNaN(value);
			if (b1) {
				lastY = value;
				lastX = di;
			}
			if (i <= lastLocation-from) {
				if (b) {
					if (b1) {
						pathList.get(lastIndex).lineTo(di, value);
					}
					else {
						lastIndex++;
						b = false;
					}
				}
				else {
					if (b1) {
						Path2D path = new Path2D.Double();
						pathList.add(path);
						if (i == 0 && !Double.isNaN(yStart)) {
							path.moveTo(0, yStart+(value-yStart)*(1.0-di));
							path.lineTo(di, value);
						}
						else {
							path.moveTo(di, value);
						}
						b = true;
					}
				}
				if (b1) {
					if (signalDetected) {
						singlePath.lineTo(di, value);
					}
					else {
						if (!Double.isNaN(yStart)) {
							singlePath.moveTo(0, yStart+(value-yStart)*(-d+1.0)/(i+1));
						}
						else {
							singlePath.moveTo(0, value);
						}
						singlePath.lineTo(di, value);
						signalDetected = true;
					}
				}
				if (i == length-1 && (signalDetected ||
						(!Double.isNaN(yEnd) && !Double.isNaN(yStart)))) {
					if (!signalDetected) {
						singlePath.moveTo(0, yStart+(yEnd-yStart)*(-d+1.0)/(length+1));
						signalDetected = true;
					}
					if (Double.isNaN(yEnd)) {
						singlePath.lineTo(sampleCount, lastY);
					}
					else {
						singlePath.lineTo(sampleCount,
								lastY+(yEnd-lastY)*(sampleCount-lastX)/(di+1.0-lastX));
						if (b && b1) {
							pathList.get(lastIndex).lineTo(sampleCount,
									value+(yEnd-value)*(sampleCount-di));
						}
					}
				}
			}
			int t1 = from+i, t2 = dx[0]*5, t3 = dx[0], pd;
			double xx = di*xs+x;
			if (dx[1] < 0) {
				pd = (int) Math.pow(10.0, -dx[1]);
				t1 *= pd;
			}
			else {
				pd = (int) Math.pow(10.0, dx[1]);
				t2 *= pd;
				t3 *= pd;
			}
			if (t1 % t2 == 0) {
		        g2.setStroke(AXIS_STROKE);
				g2.setColor(Color.DARK_GRAY);
				g2.draw(new Line2D.Double(xx, y+h, xx, y));
		        g2.setStroke(CHART_STROKE);
		        g2.setColor(Color.BLACK);
				g2.draw(new Line2D.Double(xx, y+h, xx, y+h+6.0));
				int samplingPeriod = 1;
				if (data != null) {
					samplingPeriod = data.getSamplingPeriod();
				}
				String s = Integer.toString((from+i)*samplingPeriod);
				int charCount = s.length();
				int size = charCount <= 9 ? 10 : (int) (90.0/charCount);
				Font f = new Font("Tahoma", Font.PLAIN, size);
				drawString(g2, f, xx, y+h+8.0, CENTER, TOP, s);
			}
			else if (t1 % t3 == 0) {
		        g2.setStroke(CHART_STROKE);
		        g2.setColor(Color.BLACK);
				g2.draw(new Line2D.Double(xx, y+h, xx, y+h+2.0));
			}
		}
		double dyp = dy[0]*Math.pow(10.0, dy[1]);
		int y1 = (int) Math.ceil((ym-ey)/dyp),
				y2 = (int) Math.floor((yM+ey)/dyp);
		for (int i = 0; i <= y2-y1; i++) {
			double yy = y+h-(((i+y1)*dyp-ym+ey)*ys);
			if ((i+y1) % 5 == 0) {
		        g2.setStroke(AXIS_STROKE);
				g2.setColor(Color.DARK_GRAY);
				g2.draw(new Line2D.Double(x, yy, x+w, yy));
		        g2.setStroke(CHART_STROKE);
		        g2.setColor(Color.BLACK);
				g2.draw(new Line2D.Double(x, yy, x-6.0, yy));
				String s = String.format("%.3g", (i+y1)*dyp);
				int charCount = s.length();
				int size = charCount <= 9 ? 10 : (int) (90.0/charCount);
				Font f = new Font("Tahoma", Font.PLAIN, size);
				drawString(g2, f, x-8.0, yy, RIGHT, CENTER, s);
			}
			else {
		        g2.setStroke(CHART_STROKE);
		        g2.setColor(Color.BLACK);
				g2.draw(new Line2D.Double(x, yy, x-2.0, yy));
			}
		}
        g2.setStroke(CHART_STROKE);
		g2.setColor(Color.BLUE);
		singlePath.transform(at);
		g2.draw(singlePath);
        g2.setStroke(curveStroke);
		g2.setColor(Color.RED);
		for (Path2D path : pathList) {
			path.transform(at);
			g2.draw(path);
		}
		if (sampleIndex >= from && sampleIndex < from+length) {
			g2.setColor(SEEK_COLOR);
			double value = Double.NaN;
			if (data != null) {
				value = data.getSample(sampleIndex, selectData1);
			}
			double xx = (sampleIndex-from+d)*xs+x;
			double yy = y+h-((value-ym+ey)*ys);
			double temp = Math.max(Math.min(3.0, xs/10.0), 1.0);
			if (Double.isNaN(yy)) {
				g2.draw(new Line2D.Double(xx, y+h, xx, y));
			}
			else {
				g2.draw(new Line2D.Double(xx, y+h, xx, yy+temp));
				g2.draw(new Line2D.Double(xx, yy-temp, xx, y));
			}
			g2.draw(new Ellipse2D.Double(xx-temp, yy-temp, temp*2, temp*2));
			int samplingPeriod = 1;
			if (data != null) {
				samplingPeriod = data.getSamplingPeriod();
			}
			String s1 = Integer.toString(sampleIndex*samplingPeriod);
			String s2 = String.format("%.3g", value);
			int charCount = s1.length();
			int temp2 = s2.length();
			if (s1.length() < temp2) {
				charCount = temp2;
			}
			int size = charCount <= 9 ? 10 : (int) (90.0/charCount);
			s1 += " ms";
			s2 += " " + HeartSignalData.getSampleUnit(selectData1);
			Font f = new Font("Tahoma", Font.PLAIN, size);
			FontMetrics fm = g2.getFontMetrics(f);
			int strWidth = fm.stringWidth(s1);
			temp2 = fm.stringWidth(s2);
			if (strWidth < temp2) {
				strWidth = temp2;
			}
			if (xx < x+w/2.0) {
				g2.fillRect((int) xx, y, strWidth+4, 25);
				g2.setColor(Color.BLACK);
				drawString(g2, f, xx+2.0, y+2.0, LEFT, TOP, 0.0, 5.0, 2, 1, s1, s2);
			}
			else {
				g2.fillRect((int) xx-(strWidth+4), y, strWidth+4, 25);
				g2.setColor(Color.BLACK);
				drawString(g2, f, xx-2.0, y+2.0, RIGHT, TOP, 0.0, 5.0, 2, 1, s1, s2);
			}
		}
        g2.setStroke(CHART_STROKE);
		if (!signalDetected) {
			g2.setColor(Color.BLUE);
			drawString(g2, FONT_BOLD_TEXT, x+w/2, y+h/2, CENTER, CENTER, "Tidak ada sinyal");
		}
		if (chartHighlight) {
			g2.setColor(Color.RED);
		}
		else {
			g2.setColor(Color.BLACK);
		}
		g2.drawRect(x, y, w, h);
	}
	
	private static double[] drawString(Graphics2D g2, Font f, double x, double y,
			int xAlign, int yAlign, double xPad, double yPad, int row, int col, boolean singleH,
			String... strings) {
		TextLayout[] drawStringTL = new TextLayout[strings.length];
		for (int i = 0; i < drawStringTL.length; i++) {
			drawStringTL[i] = new TextLayout(strings[i], f, g2.getFontRenderContext());
		}
		double[] wArray = new double[col], hArray = new double[row];
		Arrays.fill(wArray, 0.0);
		Arrays.fill(hArray, 0.0);
		loop: for (int i = 0; i < row; i++) {
			for (int j = 0; j < col; j++) {
				int index = i*col+j;
				if (index >= strings.length)
					break loop;
				Rectangle2D bounds = drawStringTL[index].getBounds();
				double wTemp = bounds.getWidth(), hTemp = bounds.getHeight();
				if (Double.compare(wTemp, wArray[j]) > 0)
					wArray[j] = wTemp;
				if (singleH) {
					if (Double.compare(hTemp, hArray[0]) > 0)
						hArray[0] = hTemp;
				}
				else {
					if (Double.compare(hTemp, hArray[i]) > 0)
						hArray[i] = hTemp;
				}
			}
		}
		double ht = 0.0, wt = 0.0;
		for (int i = 0; i < row; i++) {
			wt = 0.0;
			for (int j = 0; j < col; j++) {
				int index = i*col+j;
				if (index >= strings.length) {
					wt += wArray[j];
					continue;
				}
				Rectangle2D bounds = drawStringTL[index].getBounds();
				double wTemp = bounds.getWidth(), hTemp = bounds.getHeight();
				double dx, dy;
				switch (xAlign) {
				case CENTER:
					dx = wTemp*0.5;
					break;
				case RIGHT:
					dx = wArray[j];
					break;
				default:
					dx = 0.0;
					break;
				}
				switch (yAlign) {
				case CENTER:
					dy = hTemp*0.4;
					break;
				case TOP:
					dy = singleH ? hArray[0] : hArray[i];
					break;
				default:
					dy = 0.0;
					break;
				}
				double drawStringX = x-dx+(xPad*j)+wt, drawStringY = y+dy+(yPad*i)+ht;
				drawStringTL[index].draw(g2, (float) drawStringX, (float) drawStringY);
				wt += wArray[j];
			}
			ht += singleH ? hArray[0] : hArray[i];
		}
		return new double[]{(xPad*(col-1))+wt, (yPad*(row-1))+ht};
	}
	
	private static double[] drawString(Graphics2D g2, Font f, double x, double y,
			int xAlign, int yAlign, double xPad, double yPad, int row, int col,
			String... strings) {
		return drawString(g2, f, x, y, xAlign, yAlign, xPad, yPad, row, col, true, strings);
	}
	
	private static double[] drawString(Graphics2D g2, Font f, double x, double y,
			int xAlign, int yAlign, String s) {
		return drawString(g2, f, x, y, xAlign, yAlign, 0.0, 0.0, 1, 1, true, s);
	}
	
	private static int[] step(double scale) {
		double d = 10.0/scale;
		int zeros = (int) Math.floor(Math.log10(d));
		d /= Math.pow(10.0, zeros);
		int step;
		if (d <= 1.0)
			step = 1;
		else if (d <= 2.0)
			step = 2;
		else if (d <= 5.0)
			step = 5;
		else {
			step = 1;
			zeros++;
		}
		return new int[]{step, zeros};
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int x = e.getX(), y = e.getY();
		int w = getWidth(), h = getHeight();
		int l1 = X1, r1 = w-X2, t1 = Y1, b1 = h-Y2;
		boolean finished = data.isFinished();
		boolean condition1 = (x >= l1 && x < r1 && y >= t1 && y < b1 && finished);
		if (condition1) {
			sampleIndex = (int)((x-X1)*sampleCount/(w-X1-X2)-d+from);
		}
		repaint();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (data == null)
			return;
		int x = e.getX(), y = e.getY();
		button = e.getButton();
		int w = getWidth(), h = getHeight();
		int l1 = X1, r1 = w-X2, t1 = Y1, b1 = h-Y2;
		int l2 = w-X2+10, r2 = w-10, t2 = h-Y2-30, b2 = h-Y2;
		boolean started = data.isStarted(), finished = data.isFinished();
		boolean condition1 = (x >= l1 && x < r1 && y >= t1 && y < b1 && finished);
		if (button == MouseEvent.BUTTON1) {
			if (condition1) {
				mouseX = x;
			}
			else if (x >= l2 && x < r2 && y >= t2 && y < b2 && started) {
				if (finished) {
					selectData1 = HeartSignalData.nextSample(selectData1);
					selectData2 = HeartSignalData.nextSample(selectData2);
				}
				else {
					data.finishReceivingData();
				}
			}
		}
		else if (button == MouseEvent.BUTTON3) {
			if (condition1) {
				mouseX = x;
			}
		}
		if (!isFocusOwner()) {
			requestFocusInWindow();
		}
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mouseX = Integer.MIN_VALUE;
		button = MouseEvent.NOBUTTON;
		checkHighlight(e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
		checkHighlight(e);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (data == null)
			return;
		int x = e.getX();
		int dx = x-mouseX;
		boolean condition1 = (mouseX != Integer.MIN_VALUE && dx != 0);
		int w = getWidth()-X1-X2;
		int samplingPeriod = data.getSamplingPeriod();
		if (button == MouseEvent.BUTTON1) {
			if (condition1) {
				double dt = dx*sampleCount/w*samplingPeriod;
				lastTime -= (int) dt;
				if (Math.abs(dt) >= 1.0) {
					millisecondFraction -= dt % (int) dt;
				}
				else {
					millisecondFraction -= dt;
				}
				if (millisecondFraction < 0.0) {
					int m = -(int)millisecondFraction+1;
					millisecondFraction += m;
					lastTime -= m;
				}
				else if (millisecondFraction >= 1.0) {
					int m = (int)millisecondFraction;
					millisecondFraction -= m;
					lastTime += m;
				}
				long startTime = data.getStartTime(), finishTime = data.getFinishTime();
				double t = (lastTime-startTime) + millisecondFraction;
				if (t < sampleCount*samplingPeriod) {
					double s = sampleCount*samplingPeriod;
					lastTime = startTime + (int) s;
					millisecondFraction = s % (int) s;
				}
				else if (lastTime >= finishTime) {
					lastTime = finishTime;
					millisecondFraction = 0.0;
				}
				mouseX = x;
			}
		}
		else if (button == MouseEvent.BUTTON3) {
			if (condition1) {
				double dl = dx*sampleCount/w;
				long startTime = data.getStartTime(), finishTime = data.getFinishTime();
				double tt = finishTime-startTime;
				double t = (lastTime-startTime) + millisecondFraction;
				sampleCount += dl;
				if (sampleCount < 1.0) {
					sampleCount = 1.0;
				}
				else if (sampleCount > tt/samplingPeriod) {
					sampleCount = tt/samplingPeriod;
				}
				if (t < sampleCount*samplingPeriod) {
					double s = sampleCount*samplingPeriod;
					lastTime = startTime + (int) s;
					millisecondFraction = s % (int) s;
				}
				mouseX = x;
			}
		}
		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		checkHighlight(e);
	}
	
	private void checkHighlight(MouseEvent e) {
		if (data == null)
			return;
		int x = e.getX(), y = e.getY();
		int w = getWidth(), h = getHeight();
		int l1 = X1, r1 = w-X2, t1 = Y1, b1 = h-Y2;
		int l2 = w-X2+10, r2 = w-10, t2 = h-Y2-30, b2 = h-Y2;
		boolean started = data.isStarted(), finished = data.isFinished();
		if (x >= l1 && x < r1 && y >= t1 && y < b1 && finished) {
			chartHighlight = true;
		}
		else if (x >= l2 && x < r2 && y >= t2 && y < b2 && started && !finished) {
			finishButtonHighlight = true;
		}
		else {
			if (button == MouseEvent.NOBUTTON) {
				chartHighlight = false;
			}
			finishButtonHighlight = false;
		}
		repaint();
	}

	@Override
	public void focusGained(FocusEvent e) {
		repaint();
	}

	@Override
	public void focusLost(FocusEvent e) {
		repaint();
	}

	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (data == null)
			return;
		int key = e.getKeyCode();
		if (data.isFinished()) {
			if ((key == KeyEvent.VK_LEFT || key == KeyEvent.VK_UP) && sampleIndex > 0) {
				sampleIndex--;
			}
			else if ((key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_DOWN) &&
					sampleIndex < data.getLocation(data.getFinishTime())) {
				sampleIndex++;
			}
		}
		repaint();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		
	}

}
