package processing;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class HeartSignalData implements Serializable {
	private static final long serialVersionUID = -2086799758175804274L;
	public static final int UNKNOWN = -1;
	public static final int NOT_STARTED = 0;
	public static final int STARTED = 1;
	public static final int FINISHED = 2;
	public static final int SIGNAL = 0;
	public static final int HEART_RATE = 1;
	public static final String SIGNAL_NAME = "Sinyal";
	public static final String HEART_RATE_NAME = "Detak Jantung";
	public static final String SIGNAL_UNIT = "V";
	public static final String HEART_RATE_UNIT = "BPM";
	private final int samplingPeriod;
	private final Personal person;
	private transient double[] signalData;
	private transient double[] heartRateData;
	private long startTime, finishTime;
	private int state = NOT_STARTED;
	private transient int signalLastLocation = -1, heartRateLastLocation = -1, a2 = 1, b2 = 1;
	private transient double a1 = Double.NaN, b1 = Double.NaN;
	private static boolean startedAll;
	private static final List<HeartSignalData> currentDatas =
			Collections.synchronizedList(new ArrayList<HeartSignalData>());

	public HeartSignalData(int sampleCount, int samplingPeriod, Personal person) {
		this.samplingPeriod = samplingPeriod;
		this.person = person;
		signalData = new double[sampleCount];
		heartRateData = new double[sampleCount];
		Arrays.fill(signalData, Double.NaN);
		Arrays.fill(heartRateData, Double.NaN);
	}

	public static void register(HeartSignalData data) {
		currentDatas.add(data);
	}

	public static void unregister(HeartSignalData data) {
		currentDatas.remove(data);
	}

	public static void unregisterAll() {
		currentDatas.clear();
		startedAll = false;
	}

	public void startReceivingData() {
		if (isStarted())
			return;
		startTime = System.currentTimeMillis();
		state = STARTED;
	}

	public void finishReceivingData() {
		if (!isStarted() || isFinished())
			return;
		finishTime = System.currentTimeMillis();
		state = FINISHED;
	}

	public static long startAll(boolean override) {
		if (startedAll)
			return -1L;
		long startTime = System.currentTimeMillis();
		for (HeartSignalData data : currentDatas) {
			if (!override && data.isStarted())
				continue;
			data.startTime = startTime;
			data.state = STARTED;
		}
		startedAll = true;
		return startTime;
	}

	public static long finishAll(boolean override) {
		if (!startedAll)
			return -1L;
		long finishTime = System.currentTimeMillis();
		for (HeartSignalData data : currentDatas) {
			if (!override && (!data.isStarted() || data.isFinished()))
				continue;
			data.finishTime = finishTime;
			data.state = FINISHED;
		}
		unregisterAll();
		return finishTime;
	}

	public static void addSamples(double[] samples) {
		if (currentDatas.isEmpty())
			return;
		long thisTime = System.currentTimeMillis();
		for (int i = 0; i < currentDatas.size(); i++) {
			HeartSignalData data = currentDatas.get(i);
			if (!data.isStarted() || data.isFinished())
				continue;
			int location = (int)(thisTime-data.getStartTime())/data.samplingPeriod;
			boolean b = (location == data.signalLastLocation);
			if (i*2 >= samples.length)
				break;
			double d = samples[i*2];
			if (b) {
				if (Double.isNaN(data.a1)) {
					data.a2 = 1;
					data.a1 = d;
				}
				else if (!Double.isNaN(d)) {
					data.a2++;
					data.a1 += d;
				}
			}
			else {
				data.a2 = 1;
				data.a1 = d;
			}
			data.signalData[location] = data.a1/data.a2;
			data.signalLastLocation = location;
			location = (int)(thisTime-data.getStartTime())/data.samplingPeriod;
			b = (location == data.heartRateLastLocation);
			if (i*2+1 >= samples.length)
				break;
			d = samples[i*2+1];
			if (b) {
				if (Double.isNaN(data.b1)) {
					data.b2 = 1;
					data.b1 = d;
				}
				else if (!Double.isNaN(d)) {
					data.b2++;
					data.b1 += d;
				}
			}
			else {
				data.b2 = 1;
				data.b1 = d;
			}
			data.heartRateData[location] = data.b1/data.b2;
			data.heartRateLastLocation = location;
		}
	}

	public int getSamples(long lastTime, double[] sampleArray, int selectData, int sampleCount) {
		if (sampleArray.length < sampleCount)
			throw new IllegalArgumentException("array length = " + sampleArray.length
					+ ", sample count = " + sampleCount);
		Arrays.fill(sampleArray, Double.NaN);
		double[] samples;
		switch (selectData) {
		case SIGNAL:
			samples = signalData;
			break;
		default:
			samples = heartRateData;
			break;
		}
		int from = getInitIndex(lastTime, sampleCount);
		if (!isStarted())
			return from;
		for (int i = 0, j = from; i < sampleCount; i++, j++) {
			if (j >= 0 && j < samples.length) {
				sampleArray[i] = samples[j];
			}
		}
		return from;
	}
	
	public int getSamples(long lastTime, double[] sampleArray, int selectData) {
		return getSamples(lastTime, sampleArray, selectData, sampleArray.length);
	}

	public int getSamplingPeriod() {
		return samplingPeriod;
	}

	public int getLocation(long time) {
		if (!isStarted())
			return 0;
		return (int)(time-getStartTime())/samplingPeriod;
	}

	public int getLocationRemainder(long time) {
		if (!isStarted())
			return 0;
		return (int)(time-getStartTime())%samplingPeriod;
	}

	public long getFinishTime() {
		return finishTime;
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public boolean isStarted() {
		return state >= STARTED && state <= FINISHED;
	}
	
	public boolean isFinished() {
		if (state >= STARTED && state < FINISHED) {
			long thisTime = System.currentTimeMillis();
			int l = (int)(thisTime-getStartTime())/samplingPeriod;
			if (l >= signalData.length && l >= heartRateData.length) {
				finishTime = thisTime;
				state = FINISHED;
			}
		}
		return (state == FINISHED);
	}
	
	public int getState() {
		return state;
	}

	public Personal getPerson() {
		return person;
	}

	public int getSampleCount(int selectData) {
		double[] samples;
		switch (selectData) {
		case SIGNAL:
			samples = signalData;
			break;
		default:
			samples = heartRateData;
			break;
		}
		if (isFinished()) {
			return (int)(getFinishTime()-getStartTime())/samplingPeriod+1;
		}
		else {
			return samples.length;
		}
	}

	public long getTime(int location, int selectData) {
		if (!isStarted())
			return System.currentTimeMillis();
		return (long)location*samplingPeriod+getStartTime();
	}

	public double average(long lastTime, int sampleCount, int selectData) {
		double[] samples;
		switch (selectData) {
		case SIGNAL:
			samples = signalData;
			break;
		default:
			samples = heartRateData;
			break;
		}
		int from = getInitIndex(lastTime, sampleCount), to = from+sampleCount;
		return Statistics.average(samples, from, to);
	}

	public double max(long lastTime, int sampleCount, int selectData, boolean max,
			boolean extended) {
		double[] samples;
		switch (selectData) {
		case SIGNAL:
			samples = signalData;
			break;
		default:
			samples = heartRateData;
			break;
		}
		int from = getInitIndex(lastTime, sampleCount), to = from+sampleCount;
		if (extended) {
			if (from > 0) {
				from--;
			}
			to++;
		}
		int i = Statistics.maxIndex(samples, max, from, to);
		if (i == -1)
			return Double.NaN;
		return samples[i];
	}
	
	public int getInitIndex(long lastTime, int sampleCount) {
		if (!isStarted()) {
			return 0;
		}
		int from = (int)(lastTime-getStartTime())/samplingPeriod - sampleCount + 1;
		if (from < 0) {
			from = 0;
		}
		return from;
	}

	public double getSample(int i, int selectData) {
		double[] samples;
		switch (selectData) {
		case SIGNAL:
			samples = signalData;
			break;
		default:
			samples = heartRateData;
			break;
		}
		if (i < 0 || i >= samples.length)
			return Double.NaN;
		return samples[i];
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		double[] signalData = new double[this.signalData.length];
		double[] heartRateData = new double[this.heartRateData.length];
		int[] signalLengths = new int[this.signalData.length];
		int[] heartRateLengths = new int[this.heartRateData.length];
		Arrays.fill(signalData, Double.NaN);
		Arrays.fill(heartRateData, Double.NaN);
		boolean b = false;
		int dl = 0, il = 0;
		for (int i = 0, j = 0; ; i++, j++) {
			double d = this.signalData[i];
			boolean b1 = Double.isNaN(d);
			if (b1 == b) {
				b = !b;
				signalLengths[il++] = j;
				j = 0;
			}
			if (!b1) {
				signalData[dl++] = d;
			}
			if (i == this.signalData.length-1) {
				if (!b1) {
					signalLengths[il++] = j+1;
				}
				break;
			}
		}
		signalData = Arrays.copyOf(signalData, dl);
		signalLengths = Arrays.copyOf(signalLengths, il);
		b = false;
		dl = 0;
		il = 0;
		for (int i = 0, j = 0; ; i++, j++) {
			double d = this.heartRateData[i];
			boolean b1 = Double.isNaN(d);
			if (b1 == b) {
				b = !b;
				heartRateLengths[il++] = j;
				j = 0;
			}
			if (!b1) {
				heartRateData[dl++] = d;
			}
			if (i == this.heartRateData.length-1) {
				if (!b1) {
					heartRateLengths[il++] = j+1;
				}
				break;
			}
		}
		heartRateData = Arrays.copyOf(heartRateData, dl);
		heartRateLengths = Arrays.copyOf(heartRateLengths, il);
		out.writeObject(signalData);
		out.writeObject(signalLengths);
		out.writeObject(heartRateData);
		out.writeObject(heartRateLengths);
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		double[] signalData = (double[]) in.readObject();
		int[] signalLengths = (int[]) in.readObject();
		double[] heartRateData = (double[]) in.readObject();
		int[] heartRateLengths = (int[]) in.readObject();
		int i1 = 0, i2 = 0;
		for (int i : signalLengths) {
			if (i > 0) {
				i1 += i;
			}
		}
		this.signalData = new double[i1];
		Arrays.fill(this.signalData, Double.NaN);
		i1 = 0;
		loop: for (int i = 0; i < signalLengths.length; i++) {
			if ((i%2)==0) {
				if (signalLengths[i] > 0) {
					i1 += signalLengths[i];
				}
			}
			else {
				for (int j = 0; j < signalLengths[i]; j++, i1++, i2++) {
					if (i2 >= signalData.length) {
						break loop;
					}
					this.signalData[i1] = signalData[i2];
				}
			}
		}
		i1 = 0;
		i2 = 0;
		for (int i : heartRateLengths) {
			if (i > 0) {
				i1 += i;
			}
		}
		this.heartRateData = new double[i1];
		Arrays.fill(this.heartRateData, Double.NaN);
		i1 = 0;
		loop: for (int i = 0; i < heartRateLengths.length; i++) {
			if ((i%2)==0) {
				if (heartRateLengths[i] > 0) {
					i1 += heartRateLengths[i];
				}
			}
			else {
				for (int j = 0; j < heartRateLengths[i]; j++, i1++, i2++) {
					if (i2 >= heartRateData.length) {
						break loop;
					}
					this.heartRateData[i1] = heartRateData[i2];
				}
			}
		}
		
		long thisTime = System.currentTimeMillis();
		if (state < NOT_STARTED || state > FINISHED) {
			state = UNKNOWN;
		}
		else if (state >= STARTED) {
			if (thisTime < startTime) {
				state = UNKNOWN;
			}
			else if (state == FINISHED && thisTime < finishTime) {
				state = UNKNOWN;
			}
		}
		signalLastLocation = -1;
		heartRateLastLocation = -1;
		a2 = 1;
		b2 = 1;
		a1 = Double.NaN;
		b1 = Double.NaN;
	}

	public static String getSampleName(int selectData) {
		switch (selectData) {
		case SIGNAL:
			return SIGNAL_NAME;
		default:
			return HEART_RATE_NAME;
		}
	}

	public static String getSampleUnit(int selectData) {
		switch (selectData) {
		case SIGNAL:
			return SIGNAL_UNIT;
		default:
			return HEART_RATE_UNIT;
		}
	}

	public static int nextSample(int selectData) {
		switch (selectData) {
		case SIGNAL:
			return HEART_RATE;
		default:
			return SIGNAL;
		}
	}

}
