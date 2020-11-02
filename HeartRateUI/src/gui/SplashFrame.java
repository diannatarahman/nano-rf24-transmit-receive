package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Cursor;
import java.awt.Dimension;

import javax.swing.JLabel;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.SwingConstants;

public final class SplashFrame extends JFrame {
	private static final long serialVersionUID = -9190802480504982927L;
	private JPanel contentPane;
	private BufferedImage image;

	public SplashFrame() {
		super();
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		setTitle("Welcome");
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("AppIcon.png")));
		setUndecorated(true);
		setFocusable(false);
		setResizable(false);
		setAlwaysOnTop(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		try {
			image = ImageIO.read(getClass().getClassLoader().getResource("SplashImage.JPG"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		contentPane = new JPanel() {
			private static final long serialVersionUID = 3506846648154919645L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				setBackground(Color.BLACK);
				
				if (image != null) {
					Graphics2D g2 = (Graphics2D) g;
					g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
					int iw = image.getWidth(), ih = image.getHeight();
					double w = getWidth(), h = getHeight();
					double iws = w/iw, ihs = h/ih;
					double scale = (iws < ihs) ? iws : ihs;
					iws = iw*scale;
					ihs = ih*scale;
					AffineTransform at = AffineTransform.getTranslateInstance((w - iws)/2.0, (h - ihs)/2.0);
					at.scale(scale, scale);
					g2.drawRenderedImage(image, at);
				}
			}
		};
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(1, 1));
		
		JLabel SplashLabel = new JLabel("TIM JASMANI MILITER");
		contentPane.add(SplashLabel);
		SplashLabel.setHorizontalAlignment(SwingConstants.CENTER);
		SplashLabel.setForeground(Color.ORANGE);
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double scale = Dimensions.getFitScale(new Dimension(image.getWidth(), image.getHeight()),
				Dimensions.scale(screenSize, 0.5));
		SplashLabel.setFont(new Font("Arial", Font.BOLD, (int) (60*scale)));
		Dimension size = Dimensions.scale(new Dimension(image.getWidth(), image.getHeight()), scale);
		contentPane.setMinimumSize(size);
		contentPane.setPreferredSize(size);
		pack();
		setPreferredSize(getSize());
		setMinimumSize(Dimensions.subtract(getSize(),
				Dimensions.subtract(getContentPane().getPreferredSize(),
						getContentPane().getMinimumSize())));
		setLocation((screenSize.width - getWidth()) / 2, (screenSize.height - getHeight()) / 2);
		setVisible(true);
	}

}
