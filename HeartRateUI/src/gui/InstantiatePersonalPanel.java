package gui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import processing.Personal;
import javax.swing.SwingConstants;

public final class InstantiatePersonalPanel extends JPanel implements UsingNodeLayout {
	private static final long serialVersionUID = 8195058783022337011L;
	private final JTextField nameTextField;
	private final JTextField rankTextField;
	private final JTextField unitTextField;
	private final JTextField categoryTextField;
	private final JSpinner ageSpinner;
	private final JTextField[] additionalTextFields;
	private final ComponentNode node;
	
	public InstantiatePersonalPanel() {
		this(true, (String[]) null);
	}

	public InstantiatePersonalPanel(boolean b, String... fields) {
		super();
		setName("instantiatePersonalPanel");
		
		JLabel nameLabel = new JLabel("Nama:");
		nameLabel.setName("nameLabel");
		nameLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		
		JLabel rankLabel = new JLabel("Pangkat:");
		rankLabel.setName("rankLabel");
		rankLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		
		JLabel unitLabel = new JLabel("Satuan:");
		unitLabel.setName("unitLabel");
		unitLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		
		JLabel categoryLabel = new JLabel("Kategori:");
		categoryLabel.setName("categoryLabel");
		categoryLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		
		JLabel ageLabel = new JLabel("Umur:");
		ageLabel.setName("ageLabel");
		ageLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		
		nameTextField = new JTextField("", 40);
		nameTextField.setName("nameTextField");
		nameLabel.setLabelFor(nameTextField);
		
		rankTextField = new JTextField("", 40);
		rankTextField.setName("rankTextField");
		rankLabel.setLabelFor(rankTextField);
		
		unitTextField = new JTextField("", 40);
		unitTextField.setName("unitTextField");
		unitLabel.setLabelFor(unitTextField);
		
		categoryTextField = new JTextField("", 40);
		categoryTextField.setName("categoryTextField");
		categoryLabel.setLabelFor(categoryTextField);
		
		ageSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
		ageSpinner.setName("ageSpinner");
		ageLabel.setLabelFor(ageSpinner);
		
		JLabel[] additionalLabels;
		
		if (fields == null) {
			fields = new String[0];
		}
		
		additionalLabels = new JLabel[fields.length];
		additionalTextFields = new JTextField[fields.length];
		
		for (int i = 0; i < fields.length; i++) {
			additionalLabels[i] = new JLabel(fields[i] + ":");
			additionalLabels[i].setName("additionalLabels[" + i + "]");
			additionalLabels[i].setHorizontalAlignment(SwingConstants.TRAILING);
		}
		
		for (int i = 0; i < fields.length; i++) {
			additionalTextFields[i] = new JTextField("", 40);
			additionalTextFields[i].setName("additionalTextFields[" + i + "]");
			additionalLabels[i].setLabelFor(additionalTextFields[i]);
		}

		ComponentNode.UnaryBuilder ub1 = ComponentNode.getUnaryBuilder().setWidth(1.0, 0).
				setHeight(1.0, 0);
		ComponentNode.BinaryBuilder bb1 = ComponentNode.getBinaryBuilder().setSize0deg1(0.5, 0).
				setSize0deg2(0.5, 0).setSizePad(0, 0).setSize90deg(1.0, 0).setPad(5, 5, 5).
				setInnerBounds1(0.0, 0.0, 1.0).setInnerBounds2(0.0, 0.0, 1.0);
		ComponentNode.BinaryBuilder bb2 = ComponentNode.getBinaryBuilder().setSize0deg1(2/3.0, 0).
				setSize0deg2(1/3.0, 0).setSizePad(0, 0).setSize90deg(1.0, 0).setPad(5, 5, 5).
				setInnerBounds1(0.0, 0.0, 1.0).setInnerBounds2(0.0, 0.0, 1.0);
		ComponentNode.BinaryBuilder bb3 = ComponentNode.getCopy(bb2);
		ComponentNode.BinaryBuilder bb4 = ComponentNode.getBinaryBuilder().setSize0deg1(0.0, 0).
				setSize0deg2(1.0, 0).setSizePad(0, 0).setSize90deg(1.0, 0).setPad(5, 5, 5).
				setInnerBounds1(0.0, 0.0, 1.0).setInnerBounds2(0.0, 0.0, 1.0);
		
		ComponentNode c1 = bb1.build(ub1.build(nameLabel), ub1.build(rankLabel), false);
		if (b) {
			ComponentNode c2 = bb1.build(ub1.build(unitLabel), ub1.build(categoryLabel), false);
			c1 = bb1.build(c1, c2, false);
			bb2.setSize0deg1(0.8, 0).setSize0deg2(0.2, 0);
			c1 = bb2.build(c1, ub1.build(ageLabel), false);
			for (int i = 0; i < fields.length; i++) {
				bb3.setSize0deg1((i+5)/(double) (i+6), 0).setSize0deg2(1/(double) (i+6), 0);
				c1 = bb3.build(c1, ub1.build(additionalLabels[i]), false);
			}
		}
		else {
			c1 = bb2.build(c1, ub1.build(unitLabel), false);
			for (int i = 0; i < fields.length; i++) {
				bb3.setSize0deg1((i+3)/(double) (i+4), 0).setSize0deg2(1/(double) (i+4), 0);
				c1 = bb3.build(c1, ub1.build(additionalLabels[i]), false);
			}
		}
		ComponentNode c = bb1.build(ub1.build(nameTextField), ub1.build(rankTextField), false);
		if (b) {
			ComponentNode c2 = bb1.build(ub1.build(unitTextField),
					ub1.build(categoryTextField), false);
			c = bb2.build(c, c2, false);
			c = bb2.build(c, ub1.build(ageSpinner), false);
			for (int i = 0; i < fields.length; i++) {
				bb3.setSize0deg1((i+5)/(double) (i+6), 0).setSize0deg2(1/(double) (i+6), 0);
				c = bb3.build(c, ub1.build(additionalTextFields[i]), false);
			}
		}
		else {
			c = bb2.build(c, ub1.build(unitTextField), false);
			for (int i = 0; i < fields.length; i++) {
				bb3.setSize0deg1((i+3)/(double) (i+4), 0).setSize0deg2(1/(double) (i+4), 0);
				c = bb3.build(c, ub1.build(additionalTextFields[i]), false);
			}
		}
		c = bb4.build(c1, c, true);
		c.addToPanel(this);
		c.refreshLayout(this);
		node = c;
	}
	
	public Personal getInstance() {
		return new Personal(nameTextField.getText(),
				rankTextField.getText(), unitTextField.getText(), categoryTextField.getText(),
				(int) ageSpinner.getValue());
	}
	
	public String getField(int i) {
		return additionalTextFields[i].getText();
	}
	
	public void setInstance(Personal p) {
		nameTextField.setText(p.getName());
		rankTextField.setText(p.getRank());
		unitTextField.setText(p.getUnit());
		categoryTextField.setText(p.getCategory());
		ageSpinner.setValue(p.getAge());
	}
	
	public void setField(String s, int i) {
		additionalTextFields[i].setText(s);
	}
	
	public void setEditable(boolean b, int i) {
		additionalTextFields[i].setEditable(b);
	}

	@Override
	public void refreshLayout() {
		node.refreshLayout(this);
	}

}
