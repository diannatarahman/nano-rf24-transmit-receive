package processing;

import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fazecast.jSerialComm.SerialPort;

public final class SerialReceiver {
	private static final Integer[] BAUD_RATES = {9600, 19200, 38400, 57600, 115200, 230400, 460800,
			921600, 1000000, 1500000, 2000000, 3000000};
	private static final int NUM_OF_TRIALS = 3;
	private static SerialPort port;
	private static final AtomicBoolean active = new AtomicBoolean(false);
	private static Thread thread;
	private static final List<Listener> listeners = new ArrayList<Listener>();

	private SerialReceiver() {

	}

	public static interface Listener {
		public void connectionOpened(SerialPort port);
		public void connectionClosed(SerialPort port);
	}

	public static void addListener(Listener listener) {
		listeners.add(listener);
	}

	private static void connectionOpened() {
		for (Listener l : listeners) {
			l.connectionOpened(port);
		}
	}

	private static void connectionClosed() {
		for (Listener l : listeners) {
			l.connectionClosed(port);
		}
	}

	public static void connect(String portName, int baudRate) {
		if (active.get()) {
			throw new IllegalStateException("SerialReceiver is still active, disconnect() it first");
		}
		else if (port != null) {
			throw new IllegalStateException("SerialReceiver port != null");
		}
		else if (thread != null && thread.isAlive()) {
			throw new IllegalStateException("SerialReceiver thread alive");
		}

		port = SerialPort.getCommPort(portName);
		port.setBaudRate(baudRate);
		if (portName.equals("Test")) {
			active.set(true);

			thread = new Thread(() -> {

				while(true) {
					if(!active.get())
						break;

					double[] samples = new double[12];
					int length = -1;
					for (int i = 0; i < samples.length; i++) {
						samples[i] = randomSample();
						String s = String.format("%.3g", samples[i]);
						samples[i] = Double.parseDouble(s);
						length += s.length()+1;
					}
					length *= 8;
					double millis = 1000.0/baudRate*length;
					int nanos = (int) ((millis-(int)millis)*1000000.0);
					try {
						Thread.sleep((int) millis, nanos);
					} catch (Exception e) {
						e.printStackTrace();
					}
					HeartSignalData.addSamples(samples);
				}
				port = null;
				active.set(false);
				connectionClosed();
			});
		}
		else {
			port.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
			for (int i = 0; ; i++) {
				if (port.openPort()) {
					active.set(true);
					break;
				}
				else if (i == NUM_OF_TRIALS) {
					port = null;
					connectionClosed();
					return;
				}
			}

			thread = new Thread(() -> {
				Scanner scanner = new Scanner(port.getInputStream());

				while(scanner.hasNextLine()) {
					if(!active.get())
						break;

					String line = scanner.nextLine();
					String[] tokens = line.split(",");
					double[] samples = new double[tokens.length];
					for(int i = 0; i < tokens.length; i++) {
						try {
							samples[i] = Double.parseDouble(tokens[i]);
						} catch (Exception e) {
							samples[i] = Double.NaN;
						}
						if ((i%2)==0) {
							samples[i] *= 5.0/1023.0;
						}
					}
					HeartSignalData.addSamples(samples);
				}
				scanner.close();
				port.closePort();
				port = null;
				active.set(false);
				connectionClosed();
			});
		}
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.setName("Serial Port Receiver");
		thread.start();
		connectionOpened();
	}

	private static double randomSample() {
		return Math.random()*(((System.currentTimeMillis() / 1032 * (int) (Math.random()*256)
				+ 12945) * System.nanoTime()) & 255);
	}

	public static void disconnect() {
		active.set(false);
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (!thread.isAlive()) {
			thread = null;
		}
	}

	public static boolean isActive() {
		return active.get();
	}

	public static String[] getPortNames() {
		SerialPort[] ports = SerialPort.getCommPorts();
		String[] names = new String[ports.length + 1];
		for(int i = 0; i < ports.length; i++)
			names[i] = ports[i].getSystemPortName();
		names[names.length - 1] = "Test";
		return names;
	}
	
	public static Integer[] getBaudRates() {
		return Arrays.copyOf(BAUD_RATES, BAUD_RATES.length);
	}

}
