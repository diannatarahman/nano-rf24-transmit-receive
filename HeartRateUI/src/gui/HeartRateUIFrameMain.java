package gui;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import com.fazecast.jSerialComm.SerialPort;

import processing.HeartSignalData;
import processing.Personal;
import processing.SerialReceiver;

import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

public final class HeartRateUIFrameMain extends JFrame {
	private static final long serialVersionUID = -8870206574901610056L;
	private JPanel contentPane;
	private final JButton editParticipantsButton;
	private final Personal[] participants;
	private int numberOfParticipants;
	private final ParticipantEditorPanel editParticipantsPanel;
	private final InstantiatePersonalPanel observerPanel;
	private final SerialConnectionPanel serialPanel;
	private long startTime = -1, finishTime = -1;
	private int duration = MAX_DURATION_MINUTE*60000, timePassed = 0;
	private final JSpinner durationSpinner;
	private final Timer timer;
	private final JLabel stopWatchLabel;
	private final JButton startButton;
	private final JButton openButton;
	private final JButton saveButton;
	private final JPanel displayPanel;
	private final ComponentNode node;
	private final HeartSignalChartPanel[] chartPanels;
	private Report report;
	private boolean alreadyInvoked;
	private boolean dataInitialized;
	private static final Calendar calendar = Calendar.getInstance();
	private static final int MAX_DURATION_MINUTE = 30;
	private static final FileFilter DAT_FILTER = new FileFilter() {
		
		@Override
		public String getDescription() {
			return "File Data (*.dat)";
		}

		@Override
		public boolean accept(File file) {
			if (file.isDirectory())
				return true;

			String s = file.getName();
			int i = s.lastIndexOf('.');
			if (i < 0 || i >= s.length()-1)
				return false;
			s = s.substring(i+1).toLowerCase();
			return s.equals("dat");
		}
	};
	private static final FileFilter TXT_FILTER = new FileFilter() {
		
		@Override
		public String getDescription() {
			return "File Teks (*.txt)";
		}

		@Override
		public boolean accept(File file) {
			if (file.isDirectory())
				return true;

			String s = file.getName();
			int i = s.lastIndexOf('.');
			if (i < 0 || i >= s.length()-1)
				return false;
			s = s.substring(i+1).toLowerCase();
			return s.equals("txt");
		}
	};
	private static final int LEFT = 0;
	private static final int MIDDLE = 1;
	private static final int RIGHT = 2;
	private static final int REFRESH_PERIOD = 15;

	private static final class Report implements Serializable {
		private static final long serialVersionUID = 7851859518385220129L;
		private final Personal observer;
		private final String event, place;
		private final HeartSignalData[] participantsData;
		private final long startTime, finishTime;

		public Report(Personal observer, String event, String place,
				HeartSignalData[] participantsData, long startTime, long finishTime) {
			this.observer = observer;
			this.event = event;
			this.place = place;
			this.participantsData = participantsData;
			this.startTime = startTime;
			this.finishTime = finishTime;
		}
	}

	public HeartRateUIFrameMain() {
		super();
		setName("heartRateUIFrameMain");
		setExtendedState(Frame.MAXIMIZED_BOTH);
		setTitle("Tim Monitoring");
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("AppIcon.png")));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setName("contentPane");
		setContentPane(contentPane);

		JPanel controlPanel = new JPanel();
		controlPanel.setName("controlPanel");

		displayPanel = new JPanel();
		displayPanel.setName("displayPanel");

		observerPanel = new InstantiatePersonalPanel(false, "Acara", "Tempat", "Mulai", "Selesai");
		observerPanel.setEditable(false, 2);
		observerPanel.setEditable(false, 3);
		observerPanel.setName("observerPanel");
		observerPanel.setBorder(new TitledBorder(null, "Pengawas",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		observerPanel.refreshLayout();

		serialPanel = new SerialConnectionPanel();
		serialPanel.setName("serialPanel");
		serialPanel.setBorder(new TitledBorder(null, "Serial Connection",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		serialPanel.refreshLayout();

		editParticipantsPanel = new ParticipantEditorPanel();
		editParticipantsPanel.setName("editParticipantsPanel");
		WrapperDialog editParticipantsDialog = new WrapperDialog(editParticipantsPanel, this,
				"Edit Peserta", true);
		editParticipantsDialog.setName("addParticipantDialog");
		participants = new Personal[ParticipantEditorPanel.MAX_PARTICIPANTS];
		chartPanels = new HeartSignalChartPanel[ParticipantEditorPanel.MAX_PARTICIPANTS];
		for (int i = 0; i < chartPanels.length; i++) {
			chartPanels[i] = new HeartSignalChartPanel();
			chartPanels[i].setTagNumber(i+1);
			chartPanels[i].setName("chartPanels[" + i + "]");
		}
		updateParticipants();
		editParticipantsDialog.addActionListener((event) -> {
			if (event.getActionCommand().equals("OK")) {
				updateParticipants();
				updateDisplayPanel();
			}
			editParticipantsDialog.setVisible(false);
		});
		SerialReceiver.addListener(new SerialReceiver.Listener() {

			@Override
			public void connectionOpened(SerialPort port) {
				editParticipantsButton.setEnabled(false);
				editParticipantsDialog.setVisible(false);
				startButton.setEnabled(true);
			}

			@Override
			public void connectionClosed(SerialPort port) {
				startButton.setEnabled(false);
				if (timer.isRunning()) {
					stopCollectData();
				}
				editParticipantsButton.setEnabled(true);
			}
		});

		editParticipantsButton = new JButton("Edit Peserta");
		editParticipantsButton.setName("editParticipantsButton");
		editParticipantsButton.addActionListener((ae) -> {
			editParticipantsDialog.setVisible(true);
		});

		stopWatchLabel = new JLabel("00:00.000");
		stopWatchLabel.setName("stopWatchLabel");
		stopWatchLabel.setFont(new Font("Arial", Font.BOLD, 30));

		timer = new Timer(1, (ae) -> {
			long thisTime = System.currentTimeMillis();
			int timePassed = (int) (thisTime - startTime);
			this.timePassed++;
			stopWatchLabel.setText(getStopWatchText(timePassed));
			if ((this.timePassed % REFRESH_PERIOD) == 0) {
				boolean finishedAll = true;
				for (int i = 0; i < numberOfParticipants; i++) {
					if (!chartPanels[i].getData().isFinished()) {
						finishedAll = false;
						chartPanels[i].setLastTime(thisTime);
						chartPanels[i].repaint();
					}
				}
				if (finishedAll || timePassed >= duration || this.timePassed >= duration) {
					stopCollectData();
				}
			}
		});

		startButton = new JButton("Selesai");
		startButton.setName("startButton");
		startButton.setActionCommand("Start");
		startButton.setEnabled(false);
		startButton.addActionListener((ae) -> {
			String command = ae.getActionCommand();
			if (command.equals("Start")) {
				startCollectData();
			}
			else if (command.equals("Finish")) {
				stopCollectData();
			}
		});

		JLabel durationLabel = new JLabel("Selesai dalam:");
		durationLabel.setName("durationLabel");
		durationLabel.setHorizontalAlignment(SwingConstants.TRAILING);

		JLabel minuteLabel = new JLabel("menit");
		minuteLabel.setName("minuteLabel");

		durationSpinner = new JSpinner(new SpinnerNumberModel(MAX_DURATION_MINUTE, 1,
				MAX_DURATION_MINUTE, 1));
		durationSpinner.setName("durationSpinner");
		durationLabel.setLabelFor(durationSpinner);
		minuteLabel.setLabelFor(durationSpinner);
		durationSpinner.addChangeListener((ce) -> {
			duration = (int) durationSpinner.getValue()*60000;
		});

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(DAT_FILTER);

		openButton = new JButton("Buka...");
		openButton.setName("openButton");
		openButton.addActionListener((ae) -> {
			fileChooser.removeChoosableFileFilter(TXT_FILTER);
			fileChooser.setDialogTitle("Buka File");
			int option = fileChooser.showOpenDialog(this);
			if (option != JFileChooser.APPROVE_OPTION)
				return;
			File selectedFile = fileChooser.getSelectedFile();
			try {
				FileInputStream fis = new FileInputStream(selectedFile);
				ObjectInputStream in = new ObjectInputStream(fis);
				report = (Report) in.readObject();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			observerPanel.setInstance(report.observer);
			observerPanel.setField(report.event, 0);
			observerPanel.setField(report.place, 1);
			startTime = report.startTime;
			finishTime = report.finishTime;
			observerPanel.setField(getDateString(startTime), 2);
			observerPanel.setField(getDateString(finishTime), 3);
			numberOfParticipants = report.participantsData.length;
			if (numberOfParticipants > ParticipantEditorPanel.MAX_PARTICIPANTS) {
				numberOfParticipants = ParticipantEditorPanel.MAX_PARTICIPANTS;
			}
			for (int i = 0; i < numberOfParticipants; i++) {
				participants[i] = report.participantsData[i].getPerson();
				chartPanels[i].setData(report.participantsData[i]);
				chartPanels[i].repaint();
			}
			editParticipantsPanel.setParticipants(Arrays.copyOf(participants,
					numberOfParticipants));
			int timePassed = (int) (finishTime - startTime);
			if (timePassed < 0) {
				timePassed = 0;
			}
			stopWatchLabel.setText(getStopWatchText(timePassed));
			dataInitialized = true;
			updateDisplayPanel();
			dataInitialized = false;
		});

		saveButton = new JButton("Simpan...");
		saveButton.setName("saveButton");
		saveButton.addActionListener((ae) -> {
			fileChooser.addChoosableFileFilter(TXT_FILTER);
			fileChooser.setDialogTitle("Simpan File");
			File selectedFile;
			FileFilter fileFilter;
			boolean b;
			while (true) {
				int option = fileChooser.showSaveDialog(this);
				if (option != JFileChooser.APPROVE_OPTION)
					return;
				selectedFile = fileChooser.getSelectedFile();
				fileFilter = fileChooser.getFileFilter();
				String s = selectedFile.getAbsolutePath();
				b = fileFilter.equals(TXT_FILTER);
				if (b) {
					if (!s.endsWith(".txt")) {
						s += ".txt";
					}
				}
				else {
					if (!s.endsWith(".dat")) {
						s += ".dat";
					}
				}
				selectedFile = new File(s);
				if (selectedFile.exists()) {
					int response = JOptionPane.showConfirmDialog(this,
							"File \"" + selectedFile
							+ "\" sudah ada.\nApakah anda ingin menggantinya?",
							"Konfirmasi Simpan File", JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE);
					if (response == JOptionPane.YES_OPTION)
						break;
				}
				else {
					break;
				}
			}
			try {
				FileOutputStream fos = new FileOutputStream(selectedFile);
				if (b) {
					PrintWriter pw = new PrintWriter(fos);
					String s = "TIM JASMANI MILITER\n";
					s += "TIM MONITORING\n \n";
					String[] strings = fitToColumns(s, 0, 0, 80, MIDDLE);
					for (String s2 : strings) {
						pw.println(s2);
					}
					s = "Pengawas\n";
					s += "Nama        : " + report.observer.getName() + "\n";
					s += "Pangkat     : " + report.observer.getRank() + "\n";
					s += "Satuan      : " + report.observer.getUnit() + "\n \n";
					s += "Acara       : " + report.event + "\n";
					s += "Tempat      : " + report.place + "\n";
					s += "Dimulai pada: " + getDateString(report.startTime) + "\n";
					s += "Selesai pada: " + getDateString(report.finishTime) + "\n \n";
					strings = fitToColumns(s, 0, 14, 80, LEFT);
					for (String s2 : strings) {
						pw.println(s2);
					}
					for (int i = 0; i < report.participantsData.length; i++) {
						HeartSignalData data = report.participantsData[i];
						s = "Peserta " + (i+1) + "\n";
						s += "Nama    : " + data.getPerson().getName() + "\n";
						s += "Pangkat : " + data.getPerson().getRank() + "\n";
						s += "Satuan  : " + data.getPerson().getUnit() + "\n";
						s += "Kategori: " + data.getPerson().getCategory() + "\n";
						s += "Umur    : " + data.getPerson().getAge() + " Tahun\n \n";
						strings = fitToColumns(s, 0, 9, 80, LEFT);
						for (String s2 : strings) {
							pw.println(s2);
						}
						s = "";
						int sp = data.getSamplingPeriod();
						int t = (int) (report.finishTime - report.startTime)/sp;
						for (int j = 0; j < t; j++) {
							double d1 = data.getSample(j, HeartSignalData.SIGNAL),
									d2 = data.getSample(j, HeartSignalData.HEART_RATE);
							if (!Double.isNaN(d1) || !Double.isNaN(d2)) {
								s += getStopWatchText(j*sp) + " " + String.format("%.3g", d1)
								+ " V, " + String.format("%.3g", d2) + " BPM\n";
							}
						}
						if (s.equals("")) {
							s = "TIDAK ADA DATA\n";
						}
						s += " \n";
						strings = fitToColumns(s, 0, 0, 80, LEFT);
						for (String s2 : strings) {
							pw.println(s2);
						}
					}
					pw.close();
				}
				else {
					ObjectOutputStream out = new ObjectOutputStream(fos);
					out.writeObject(report);
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		initializeData();
		createReport();

		ComponentNode.UnaryBuilder ub1 = ComponentNode.getUnaryBuilder().setWidth(1.0, 0).
				setHeight(1.0, 0);
		ComponentNode.BinaryBuilder bb1 = ComponentNode.getBinaryBuilder().
				setPosition0deg(0.5, 0).setPosition90deg(0.5, 0).setPad(5, 5, 5).
				setInnerBounds1(0.5, 0.5, 0.0).setInnerBounds2(0.5, 0.5, 0.0);
		ComponentNode.BinaryBuilder bb2 = ComponentNode.getBinaryBuilder().
				setSize0deg1(0.5, 0).setSize0deg2(0.5, 0).setPad(5, 5, 5).
				setInnerBounds1(0.0, 0.0, 1.0).setInnerBounds2(0.0, 0.0, 1.0);
		ComponentNode.BinaryBuilder bb3 = ComponentNode.getBinaryBuilder().setSize0deg1(0.0, 0).
				setSize0deg2(1.0, -20).setSize90deg(1.0, -20).setPad(5, 5, 5).
				setInnerBounds1(0.0, 0.0, 1.0).setInnerBounds2(0.0, 0.0, 1.0).
				setPosition0deg(0, 10).setPosition90deg(0, 10);

		ComponentNode c1 = bb1.build(ub1.build(observerPanel), ub1.build(serialPanel), true);
		ComponentNode c2 = bb1.build(ub1.build(durationSpinner), ub1.build(minuteLabel), true);
		c2 = bb1.build(ub1.build(durationLabel), c2, true);
		c2 = bb1.build(ub1.build(editParticipantsButton), c2, false);
		c2 = bb1.build(c2, ub1.build(stopWatchLabel), false);
		c2 = bb1.build(c2, ub1.build(startButton), false);
		c1 = bb1.build(c1, c2, true);
		c2 = bb2.build(ub1.build(openButton), ub1.build(saveButton), false);
		c1 = bb1.build(c2, c1, true);
		c1.addToPanel(controlPanel);
		c1.refreshLayout(controlPanel);

		c1 = bb3.build(ub1.build(controlPanel), ub1.build(displayPanel), false);
		c1.addToPanel(contentPane);
		node = c1;

		updateDisplayPanel();
		node.refreshLayout(contentPane);
		pack();
		setPreferredSize(getSize());
		setMinimumSize(Dimensions.subtract(getSize(),
				Dimensions.subtract(getContentPane().getPreferredSize(),
						getContentPane().getMinimumSize())));
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width - getWidth()) / 2,
				(screenSize.height - getHeight()) / 2);
		setVisible(true);
		startButton.setText("Mulai");
	}

	private static String[] fitToColumns(String s, int off1, int off2, int columns, int pos) {
		String[] strings = s.split("\n");
		int length = 0;
		for (int i = 0; i < strings.length; i++) {
			int c = strings[i].length()+off1;
			if (c < columns) {
				length++;
				continue;
			}
			c -= columns;
			length++;
			int l = c / (columns-off2);
			if (c % (columns-off2) > 0)
				l++;
			length += l;
		}
		String[] strings2 = new String[length];
		for (int i = 0, j = 0, k = 0, c1 = columns-off1, c2 = off1; i < length; i++) {
			boolean b = k+c1 < strings[j].length();
			if (b) {
				strings2[i] = strings[j].substring(k, k+c1);
			}
			else {
				strings2[i] = strings[j].substring(k);
			}
			int off = c2;
			switch (pos) {
			case LEFT:
				break;
			case MIDDLE:
				off = (columns-strings2[i].length())/2;
				break;
			case RIGHT:
				off = columns-strings2[i].length();
				break;
			default:
				break;
			}
			String s2 = "";
			for (int a = 0; a < off; a++) {
				s2 = " " + s2;
			}
			strings2[i] = s2 + strings2[i];
			if (b) {
				k += c1;
				c1 = columns-off2;
				c2 = off2;
			}
			else {
				k = 0;
				j++;
				c1 = columns-off1;
				c2 = off1;
			}
		}
		return strings2;
	}

	private void startCollectData() {
		for (Component c : observerPanel.getComponents()) {
			if (!(c instanceof JLabel))
				c.setEnabled(false);
		}
		serialPanel.setEnabledButton(false);
		openButton.setEnabled(false);
		saveButton.setEnabled(false);
		durationSpinner.setEnabled(false);
		initializeData();
		timer.start();
		startTime = HeartSignalData.startAll(true);
		observerPanel.setField(getDateString(startTime), 2);
		observerPanel.setField("", 3);
		startButton.setText("Selesai");
		startButton.setActionCommand("Finish");
	}

	private void stopCollectData() {
		finishTime = HeartSignalData.finishAll(false);
		timer.stop();
		timePassed = 0;
		stopWatchLabel.setText(getStopWatchText((int) (finishTime - startTime)));
		createReport();
		dataInitialized = false;
		for (Component c : observerPanel.getComponents()) {
			if (!(c instanceof JLabel))
				c.setEnabled(true);
		}
		serialPanel.setEnabledButton(true);
		openButton.setEnabled(true);
		saveButton.setEnabled(true);
		durationSpinner.setEnabled(true);
		observerPanel.setField(getDateString(finishTime), 3);
		startButton.setText("Mulai");
		startButton.setActionCommand("Start");
	}
	
	private void createReport() {
		HeartSignalData[] participantsData = new HeartSignalData[numberOfParticipants];
		for (int i = 0; i < numberOfParticipants; i++) {
			participantsData[i] = chartPanels[i].getData();
		}
		report = new Report(observerPanel.getInstance(), observerPanel.getField(0),
				observerPanel.getField(1), participantsData, startTime, finishTime);
	}

	private static String getDateString(long time) {
		if (time < 0) {
			return "Tidak diketahui";
		}
		calendar.setTimeInMillis(time);
		String day;
		switch (calendar.get(Calendar.DAY_OF_WEEK)) {
		case Calendar.MONDAY:
			day = "Senin";
			break;
		case Calendar.TUESDAY:
			day = "Selasa";
			break;
		case Calendar.WEDNESDAY:
			day = "Rabu";
			break;
		case Calendar.THURSDAY:
			day = "Kamis";
			break;
		case Calendar.FRIDAY:
			day = "Jumat";
			break;
		case Calendar.SATURDAY:
			day = "Sabtu";
			break;
		case Calendar.SUNDAY:
			day = "Minggu";
			break;
		default:
			day = "~";
			break;
		}
		int date = calendar.get(Calendar.DATE);
		String month;
		switch (calendar.get(Calendar.MONTH)) {
		case Calendar.JANUARY:
			month = "Januari";
			break;
		case Calendar.FEBRUARY:
			month = "Februari";
			break;
		case Calendar.MARCH:
			month = "Maret";
			break;
		case Calendar.APRIL:
			month = "April";
			break;
		case Calendar.MAY:
			month = "Mei";
			break;
		case Calendar.JUNE:
			month = "Juni";
			break;
		case Calendar.JULY:
			month = "Juli";
			break;
		case Calendar.AUGUST:
			month = "Agustus";
			break;
		case Calendar.SEPTEMBER:
			month = "September";
			break;
		case Calendar.OCTOBER:
			month = "Oktober";
			break;
		case Calendar.NOVEMBER:
			month = "November";
			break;
		case Calendar.DECEMBER:
			month = "Desember";
			break;
		default:
			month = "~";
			break;
		}
		int year = calendar.get(Calendar.YEAR);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		int millisecond = calendar.get(Calendar.MILLISECOND);
		return day + ", " + date + " " + month + " " + year + " "
				+ String.format("%d%d:%d%d:%d%d.%d%d%d",
				hour/10, hour%10, minute/10, minute%10, second/10, second%10,
				millisecond/100, millisecond/10%10, millisecond%10);
	}
	
//	private void updateStopWatchLabel(int timePassed) {
//		int seconds = timePassed/1000;
//		updateStopWatchLabel(seconds/60%60, seconds%60, timePassed%1000);
//	}
//
//	private void updateStopWatchLabel(int minutes, int seconds, int milliseconds) {
//		stopWatchLabel.setText(String.format("%d%d:%d%d.%d%d%d",
//				minutes/10, minutes%10, seconds/10, seconds%10,
//				milliseconds/100, milliseconds/10%10, milliseconds%10));
//	}
	
	private static String getStopWatchText(int timePassed) {
		int seconds = timePassed/1000;
		return getStopWatchText(seconds/60%60, seconds%60, timePassed%1000);
	}
	
	private static String getStopWatchText(int minutes, int seconds, int milliseconds) {
		return String.format("%d%d:%d%d.%d%d%d",
				minutes/10, minutes%10, seconds/10, seconds%10,
				milliseconds/100, milliseconds/10%10, milliseconds%10);
	}

	private void updateParticipants() {
		Personal[] participants = editParticipantsPanel.getParticipants();
		for (int i = 0; i < participants.length; i++) {
			this.participants[i] = participants[i];
		}
		numberOfParticipants = participants.length;
		dataInitialized = false;
	}

	private void updateDisplayPanel() {
		displayPanel.removeAll();
		initializeData();
		int col = (int) Math.ceil(Math.sqrt(numberOfParticipants));
		int row = (int) Math.ceil(numberOfParticipants/(double)col);
		ComponentNode.UnaryBuilder ub1 = ComponentNode.getUnaryBuilder().setWidth(1.0, 0).
				setHeight(1.0, 0);
		ComponentNode.BinaryBuilder bb1 = ComponentNode.getBinaryBuilder().setSizePad(0, 0).
				setSize90deg(1.0, 0).setPad(5, 5, 5).setInnerBounds1(0.0, 0.0, 1.0).
				setInnerBounds2(0.0, 0.0, 1.0);
		ComponentNode c1 = null;
		for (int i = 0; i < row; i++) {
			ComponentNode c2 = null;
			for (int j = 0; j < col; j++) {
				int index = i*col+j;
				if (index >= numberOfParticipants)
					break;
				if (j == 0) {
					c2 = ub1.build(chartPanels[index]);
				}
				else {
					bb1.setSize0deg1(j/(double) (j+1), 0).setSize0deg2(1/(double) (j+1), 0);
					c2 = bb1.build(c2, ub1.build(chartPanels[index]), true);
				}
			}
			if (i == 0) {
				c1 = c2;
			}
			else {
				bb1.setSize0deg1(i/(double) (i+1), 0).setSize0deg2(1/(double) (i+1), 0);
				c1 = bb1.build(c1, c2, false);
			}
		}
		c1.addToPanel(displayPanel);
		c1.refreshLayout(displayPanel);
		if (alreadyInvoked) {
			node.refreshLayout(contentPane);
			setVisible(true);
		}
		else {
			alreadyInvoked = true;
		}
	}

	private void initializeData() {
		if (!dataInitialized) {
			HeartSignalData.unregisterAll();
		}
		for (int i = 0; i < numberOfParticipants; i++) {
			if (!dataInitialized) {
				HeartSignalData data = new HeartSignalData(duration/10, 10, participants[i]);
				HeartSignalData.register(data);
				chartPanels[i].setData(data);
			}
			chartPanels[i].repaint();
		}
		dataInitialized = true;
	}
}
