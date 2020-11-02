package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import processing.SerialReceiver;

import com.fazecast.jSerialComm.SerialPort;
import javax.swing.SwingConstants;
import java.awt.Color;

public final class SerialConnectionPanel extends JPanel implements ActionListener,
		SerialReceiver.Listener, UsingNodeLayout {
	private static final long serialVersionUID = -1943710150867802485L;
	private final JComboBox<String> portNamesComboBox;
	private final JComboBox<Integer> baudRatesComboBox;
	private final JLabel connectedLabel;
	private final JButton connectButton;
	private final AtomicBoolean waitingForSerialConnection = new AtomicBoolean(false);
	private final AtomicBoolean waitingForSerialDisconnection = new AtomicBoolean(false);
	private final ComponentNode node;

	public SerialConnectionPanel() {
		super();
		setName("serialConnectionPanel");

		connectButton = new JButton("Connect");
		connectButton.setName("connectButton");
		connectButton.setActionCommand("Connect");
		connectButton.addActionListener(this);
		
		connectedLabel = new JLabel("Disconnected");
		connectedLabel.setName("connectedLabel");
		connectedLabel.setForeground(Color.RED);
		connectedLabel.setHorizontalAlignment(SwingConstants.CENTER);
		
		JLabel portNamesLabel = new JLabel("Port:");
		portNamesLabel.setName("portNamesLabel");
		portNamesLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		
		JLabel baudRatesLabel = new JLabel("Baud Rate:");
		baudRatesLabel.setName("baudRatesLabel");
		baudRatesLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		
		portNamesComboBox = new JComboBox<String>(SerialReceiver.getPortNames());
		portNamesComboBox.setName("portNamesComboBox");
		portNamesLabel.setLabelFor(portNamesComboBox);
		portNamesComboBox.addPopupMenuListener(new PopupMenuListener() {
			
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				portNamesComboBox.removeAllItems();
				for (String s : SerialReceiver.getPortNames()) {
					portNamesComboBox.addItem(s);
				}
			}
			
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				
			}
			
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				
			}
		});
		
		baudRatesComboBox = new JComboBox<Integer>(SerialReceiver.getBaudRates());
		baudRatesComboBox.setName("baudRatesComboBox");
		baudRatesLabel.setLabelFor(baudRatesComboBox);
		
		ComponentNode.UnaryBuilder ub1 = ComponentNode.getUnaryBuilder().setWidth(1.0, 0).
				setHeight(1.0, 0);
		ComponentNode.BinaryBuilder bb1 = ComponentNode.getBinaryBuilder().setSize0deg1(0.5, 0).
				setSize0deg2(0.5, 0).setSizePad(0, 0).setSize90deg(1.0, 0).setPad(5, 5, 5).
				setInnerBounds1(0.0, 0.0, 1.0).setInnerBounds2(0.0, 0.0, 1.0);
		ComponentNode.BinaryBuilder bb2 = ComponentNode.getBinaryBuilder().setSize0deg1(0.0, 0).
				setSize0deg2(1.0, 0).setSizePad(0, 0).setSize90deg(1.0, 0).setPad(5, 5, 5).
				setInnerBounds1(0.0, 0.0, 1.0).setInnerBounds2(0.0, 0.0, 1.0);
		ComponentNode.BinaryBuilder bb3 = ComponentNode.getBinaryBuilder().setSize0deg1(1.0, 0).
				setSize0deg2(0.0, 0).setSizePad(0, 0).setSize90deg(1.0, 0).setPad(5, 5, 5).
				setInnerBounds1(0.0, 0.0, 1.0).setInnerBounds2(0.0, 0.0, 1.0);
		
		ComponentNode c1 = bb1.build(ub1.build(portNamesLabel),
				ub1.build(baudRatesLabel), false);
		ComponentNode c = bb1.build(ub1.build(portNamesComboBox),
				ub1.build(baudRatesComboBox), false);
		c = bb2.build(c1, c, true);
		c1 = bb1.build(ub1.build(connectedLabel), ub1.build(connectButton), false);
		c = bb3.build(c, c1, false);
		c.addToPanel(this);
		c.refreshLayout(this);
		node = c;
		
		SerialReceiver.addListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command.equals("Connect")) {
			portNamesComboBox.setEnabled(false);
			baudRatesComboBox.setEnabled(false);
			connectButton.setEnabled(false);
			connectButton.setText("Connecting...");
			waitingForSerialConnection.set(true);
			Thread t = new Thread(() -> {
				SerialReceiver.connect((String) portNamesComboBox.getSelectedItem(),
						(int) baudRatesComboBox.getSelectedItem());
			});
			t.start();
		}
		else if (command.equals("Disconnect")) {
			connectButton.setEnabled(false);
			connectButton.setText("Disconnecting...");
			waitingForSerialDisconnection.set(true);
			Thread t = new Thread(() -> {
				SerialReceiver.disconnect();
			});
			t.start();
		}
	}

	@Override
	public void connectionOpened(SerialPort port) {
		SwingUtilities.invokeLater(() -> {
			connectButton.setText("Disconnect");
			connectButton.setActionCommand("Disconnect");
			connectButton.setEnabled(true);
			waitingForSerialConnection.set(false);
			connectedLabel.setText("Connected");
			connectedLabel.setForeground(new Color(0, 128, 0));
		});
	}

	@Override
	public void connectionClosed(SerialPort port) {
		SwingUtilities.invokeLater(() -> {
			connectButton.setText("Connect");
			connectButton.setActionCommand("Connect");
			portNamesComboBox.setEnabled(true);
			baudRatesComboBox.setEnabled(true);
			connectButton.setEnabled(true);
			if (!waitingForSerialDisconnection.compareAndSet(true, false)) {
				JFrame parentWindow = (JFrame) SwingUtilities.windowForComponent(this);
				JOptionPane.showMessageDialog(parentWindow, "Serial connection lost.",
						"Serial Connection Lost", JOptionPane.WARNING_MESSAGE);
			}
			connectedLabel.setText("Disconnected");
			connectedLabel.setForeground(Color.RED);
		});
	}
	
	public void setEnabledButton(boolean b) {
		connectButton.setEnabled(b);
	}

	@Override
	public void refreshLayout() {
		node.refreshLayout(this);
	}

}
