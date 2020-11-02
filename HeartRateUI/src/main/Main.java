package main;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import gui.HeartRateUIFrameMain;
import gui.SplashFrame;

public final class Main {
	private static SplashFrame splashWindow;
	
	private Main() {
		
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				e.printStackTrace();
			}
			splashWindow = new SplashFrame();
		});
		try {
			Thread.sleep(3000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		SwingUtilities.invokeLater(() -> {
			splashWindow.dispose();
			new HeartRateUIFrameMain();
		});
	}

}
