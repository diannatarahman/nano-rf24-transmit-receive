package gui;

import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import processing.Personal;

public final class ParticipantEditorPanel extends JPanel {
	private static final long serialVersionUID = -4561387367411375826L;
	public static final int MAX_PARTICIPANTS = 6;
	private static final Personal EMPTY = new Personal("", "", "");
	private final InstantiatePersonalPanel participantPanel;
	private final Personal[] participants;
	private final JSpinner slotsSpinner;
	private final JSpinner selectedSlotSpinner;
	private final SpinnerNumberModel selectedSlotModel;
	private int numberOfSlots;
	private int selectedSlot;

	public ParticipantEditorPanel() {
		super();
		setName("participantEditorPanel");
		participantPanel = new InstantiatePersonalPanel();
		participantPanel.setName("participantPanel");
		participants = new Personal[MAX_PARTICIPANTS];
		for (int i = 0; i < MAX_PARTICIPANTS; i++) {
			participants[i] = EMPTY;
		}
		numberOfSlots = MAX_PARTICIPANTS;
		
		JLabel slotsLabel = new JLabel("Jumlah peserta:");
		slotsLabel.setName("slotsLabel");
		slotsLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		
		JLabel selectedSlotLabel = new JLabel("No. Urut:");
		selectedSlotLabel.setName("selectedslotLabel");
		selectedSlotLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		
		slotsSpinner = new JSpinner(new SpinnerNumberModel(MAX_PARTICIPANTS,
				1, MAX_PARTICIPANTS, 1));
		slotsSpinner.setName("slotsSpinner");
		slotsLabel.setLabelFor(slotsSpinner);
		
		selectedSlotModel = new SpinnerNumberModel(1, 1, MAX_PARTICIPANTS, 1);
		selectedSlotSpinner = new JSpinner(selectedSlotModel);
		selectedSlotSpinner.setName("selectedslotSpinner");
		selectedSlotLabel.setLabelFor(selectedSlotSpinner);
		slotsSpinner.addChangeListener((ce) -> {
			numberOfSlots = (int) slotsSpinner.getValue();
			selectedSlotModel.setMaximum(numberOfSlots);
			if (selectedSlot >= numberOfSlots) {
				selectedSlot = numberOfSlots-1;
				selectedSlotSpinner.setValue(numberOfSlots);
			}
		});
		selectedSlotSpinner.addChangeListener((ce) -> {
			selectedSlot = (int) selectedSlotSpinner.getValue()-1;
			participantPanel.setInstance(participants[selectedSlot]);
		});
		
		JButton setButton = new JButton("Set Peserta");
		setButton.setName("setButton");
		setButton.addActionListener((ae) -> {
			int selectedSlot = (int) selectedSlotSpinner.getValue() - 1;
			participants[selectedSlot] = participantPanel.getInstance();
		});
		
		ComponentNode.UnaryBuilder ub1 = ComponentNode.getUnaryBuilder().setWidth(1.0, 0).
				setHeight(1.0, 0);
		ComponentNode.BinaryBuilder bb1 = ComponentNode.getBinaryBuilder().setSize0deg1(0.5, 0).
				setSize0deg2(0.5, 0).setSizePad(0, 0).setSize90deg(1.0, 0).setPad(5, 5, 5).
				setInnerBounds1(0.0, 0.0, 1.0).setInnerBounds2(0.0, 0.0, 1.0);
		ComponentNode.BinaryBuilder bb2 = ComponentNode.getBinaryBuilder().
				setPosition0deg(0.5, 0).setPosition90deg(0.5, 0).setPad(5, 5, 5).
				setInnerBounds1(0.5, 0.5, 0.0).setInnerBounds2(0.5, 0.5, 0.0);
		ComponentNode.BinaryBuilder bb3 = ComponentNode.getBinaryBuilder().setSize0deg1(1.0, 0).
				setSize0deg2(0.0, 0).setSizePad(0, 0).setSize90deg(1.0, 0).setPad(5, 5, 5).
				setInnerBounds1(0.0, 0.0, 1.0).setInnerBounds2(0.5, 0.5, 0.0);
		ComponentNode.BinaryBuilder bb4 = ComponentNode.getBinaryBuilder().setSize0deg1(0.0, 0).
				setSize0deg2(1.0, -10).setSizePad(0, 0).setSize90deg(1.0, -10).setPad(5, 5, 5).
				setInnerBounds1(0.0, 0.0, 1.0).setInnerBounds2(0.0, 0.0, 1.0).
				setPosition0deg(0, 5).setPosition90deg(0, 5);
		
		ComponentNode c1 = bb1.build(ub1.build(slotsLabel),
				ub1.build(slotsSpinner), true);
		ComponentNode c = bb1.build(ub1.build(selectedSlotLabel),
				ub1.build(selectedSlotSpinner), true);
		c = bb2.build(c1, c, true);
		c1 = bb3.build(ub1.build(participantPanel), ub1.build(setButton), true);
		c = bb4.build(c, c1, false);
		c.addToPanel(this);
		c.refreshLayout(this);
	}
	
	public Personal[] getParticipants() {
		return Arrays.copyOf(participants, numberOfSlots);
	}

	public void setParticipants(Personal[] participants) {
		for (int i = 0; i < participants.length; i++) {
			this.participants[i] = participants[i];
		}
		numberOfSlots = participants.length;
		slotsSpinner.setValue(numberOfSlots);
		selectedSlotModel.setMaximum(numberOfSlots);
		if (selectedSlot >= numberOfSlots) {
			selectedSlot = numberOfSlots-1;
			selectedSlotSpinner.setValue(numberOfSlots);
		}
		participantPanel.setInstance(participants[selectedSlot]);
	}

}
