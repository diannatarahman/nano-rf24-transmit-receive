package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import javax.swing.border.EmptyBorder;

public final class WrapperDialog extends JDialog {
	private static final long serialVersionUID = 8897829448498467233L;
	private final JButton okButton;
	private final JButton cancelButton;

	public WrapperDialog(JPanel contentPanel, Frame owner, String title, boolean modal) {
		super(owner, title, modal);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		okButton = new JButton("OK");
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);
		
		cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);
		
		Dimension d1 = contentPanel.getMinimumSize(), d2 = okButton.getMinimumSize(),
				d3 = cancelButton.getMinimumSize();
		Dimension d4 = Dimensions.combine(d2, d3, true, 5, 10, 10);
		buttonPane.setMinimumSize(d4);
		buttonPane.setPreferredSize(Dimensions.max(d4, buttonPane.getPreferredSize()));
		Dimension d = Dimensions.combine(Dimensions.addOffset(d1, 10, 10), d4, false);
		getContentPane().setMinimumSize(d);
		d1 = contentPanel.getPreferredSize();
		d2 = okButton.getPreferredSize();
		d3 = cancelButton.getPreferredSize();
		d4 = Dimensions.combine(d2, d3, true, 5, 10, 10);
		buttonPane.setPreferredSize(d4);
		d = Dimensions.combine(Dimensions.addOffset(d1, 10, 10), d4, false);
		getContentPane().setPreferredSize(d);
		pack();
		setPreferredSize(getSize());
		setMinimumSize(Dimensions.subtract(getSize(),
				Dimensions.subtract(getContentPane().getPreferredSize(),
						getContentPane().getMinimumSize())));
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width - getWidth()) / 2,
				(screenSize.height - getHeight()) / 2);
	}
	
	public void addActionListener(ActionListener l) {
		okButton.addActionListener(l);
		cancelButton.addActionListener(l);
	}

}
