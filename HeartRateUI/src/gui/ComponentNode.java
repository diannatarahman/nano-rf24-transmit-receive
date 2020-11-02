package gui;

import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Spring;
import javax.swing.SpringLayout;

public abstract class ComponentNode {
	
	public static final class UnaryBuilder {
		private double xa, ya, wa, ha;
		private int xb, yb, wb, hb;
		
		private UnaryBuilder() {
			
		}
		
		private UnaryBuilder(UnaryBuilder ub) {
			xa = ub.xa;
			ya = ub.ya;
			wa = ub.wa;
			ha = ub.ha;
			xb = ub.xb;
			yb = ub.yb;
		}
		
		public UnaryBuilder setX(double scale, int offset) {
			xa = scale;
			xb = offset;
			return this;
		}
		
		public UnaryBuilder setY(double scale, int offset) {
			ya = scale;
			yb = offset;
			return this;
		}
		
		public UnaryBuilder setWidth(double scale, int offset) {
			wa = scale;
			wb = offset;
			return this;
		}
		
		public UnaryBuilder setHeight(double scale, int offset) {
			ha = scale;
			hb = offset;
			return this;
		}
		
		public ComponentNode build(JComponent component) {
			return new Unary(component, xa, ya, wa, ha, xb, yb, wb, hb);
		}
	}
	
	public static final class BinaryBuilder {
		private double pa, qa, m1a, m2a, da, na;
		private double q1a1, q1a2, n1a, q2a1, q2a2, n2a;
		private int dMin, dPref, dMax;
		private int pb, qb, m1b, m2b, db, nb;
		
		private BinaryBuilder(BinaryBuilder bb) {
			pa = bb.pa;
			qa = bb.qa;
			m1a = bb.m1a;
			m2a = bb.m2a;
			da = bb.da;
			na = bb.na;
			q1a1 = bb.q1a1;
			q1a2 = bb.q1a2;
			n1a = bb.n1a;
			q2a1 = bb.q2a1;
			q2a2 = bb.q2a2;
			n2a = bb.n2a;
			dMin = bb.dMin;
			dPref = bb.dPref;
			dMax = bb.dMax;
			pb = bb.pb;
			qb = bb.qb;
			m1b = bb.m1b;
			m2b = bb.m2b;
			db = bb.db;
			nb = bb.nb;
		}
		
		private BinaryBuilder() {
			
		}
		
		public BinaryBuilder setPosition0deg(double scale, int offset) {
			pa = scale;
			pb = offset;
			return this;
		}
		
		public BinaryBuilder setPosition90deg(double scale, int offset) {
			qa = scale;
			qb = offset;
			return this;
		}
		
		public BinaryBuilder setSize0deg1(double scale, int offset) {
			m1a = scale;
			m1b = offset;
			return this;
		}
		
		public BinaryBuilder setSize0deg2(double scale, int offset) {
			m2a = scale;
			m2b = offset;
			return this;
		}
		
		public BinaryBuilder setSizePad(double scale, int offset) {
			da = scale;
			db = offset;
			return this;
		}
		
		public BinaryBuilder setSize90deg(double scale, int offset) {
			na = scale;
			nb = offset;
			return this;
		}
		
		public BinaryBuilder setInnerBounds1(double scalePosition, double align, double scaleSize) {
			q1a1 = scalePosition;
			q1a2 = align;
			n1a = scaleSize;
			return this;
		}
		
		public BinaryBuilder setInnerBounds2(double scalePosition, double align, double scaleSize) {
			q2a1 = scalePosition;
			q2a2 = align;
			n2a = scaleSize;
			return this;
		}
		
		public BinaryBuilder setPad(int min, int pref, int max) {
			dMin = min;
			dPref = pref;
			dMax = max;
			return this;
		}
		
		public ComponentNode build(ComponentNode node1, ComponentNode node2, boolean horizontal) {
			return new Binary(node1, node2, pa, qa, m1a, m2a, da, na, q1a1, q1a2, n1a, q2a1,
					q2a2, n2a, dMin, dPref, dMax, pb, qb, m1b, m2b, db, nb, horizontal);
		}
	}
	
	public static final UnaryBuilder getUnaryBuilder() {
		return new UnaryBuilder();
	}
	
	public static final BinaryBuilder getBinaryBuilder() {
		return new BinaryBuilder();
	}
	
	public static final UnaryBuilder getCopy(UnaryBuilder ub) {
		return new UnaryBuilder(ub);
	}
	
	public static final BinaryBuilder getCopy(BinaryBuilder bb) {
		return new BinaryBuilder(bb);
	}
	
	public final Dimension getMinimumSize() {
		return getMinSize();
	}

	public final Dimension getPreferredSize() {
		return getPrefSize();
	}

	public final Dimension getMaximumSize() {
		return getMaxSize();
	}
	
	abstract Dimension getMinSize();
	abstract Dimension getPrefSize();
	abstract Dimension getMaxSize();
	abstract void addComponentToPanel(JPanel panel);
	abstract void refreshLayout(SpringLayout layout, JPanel panel, Spring x, Spring y,
			Spring w, Spring h);
	
	private static final class Unary extends ComponentNode {
		private final JComponent component;
		private final double xa, ya, wa, ha;
		private final int xb, yb, wb, hb;
		
		public Unary(JComponent component, double xa, double ya, double wa, double ha,
				int xb, int yb, int wb, int hb) {
			if (xa+wa < 0.0 || xa+wa > 1.0) {
				throw new IllegalArgumentException("Horizontal scale outside range 0 <= x <= 1");
			}
			if (ya+ha < 0.0 || ya+ha > 1.0) {
				throw new IllegalArgumentException("Vertical scale outside range 0 <= y <= 1");
			}
			this.xa = xa;
			this.ya = ya;
			this.wa = wa;
			this.ha = ha;
			this.xb = xb;
			this.yb = yb;
			this.wb = wb;
			this.hb = hb;
			this.component = component;
		}
		
		private Dimension calculateSize(Dimension componentSize) {
			Dimension dimension = Dimensions.addOffset(componentSize, -wb, -hb);
			return dimension;
		}

		@Override
		public Dimension getMinSize() {
			return calculateSize(component.getMinimumSize());
		}

		@Override
		public Dimension getPrefSize() {
			return calculateSize(component.getPreferredSize());
		}

		@Override
		public Dimension getMaxSize() {
			return calculateSize(component.getMaximumSize());
		}
		
		@Override
		void refreshLayout(SpringLayout layout, JPanel panel, Spring x, Spring y,
				Spring w, Spring h) {
			Dimension min = component.getMinimumSize(),
					pref = component.getPreferredSize(),
					max = component.getMaximumSize();
			Spring cw0 = Spring.constant(min.width, pref.width, max.width);
			Spring ch0 = Spring.constant(min.height, pref.height, max.height);
			min = getMinimumSize();
			pref = getPreferredSize();
			max = getMaximumSize();
			Spring cw = Spring.constant(min.width, pref.width, max.width);
			Spring ch = Spring.constant(min.height, pref.height, max.height);
			Spring deltaW = Spring.sum(w, Spring.minus(cw));
			Spring deltaH = Spring.sum(h, Spring.minus(ch));
			
			layout.getConstraints(component).setX(Spring.sum(x,
					Spring.sum(Spring.scale(deltaW, (float) xa), Spring.constant(xb))));
			layout.getConstraints(component).setY(Spring.sum(y,
					Spring.sum(Spring.scale(deltaH, (float) ya), Spring.constant(yb))));
			layout.getConstraints(component).setWidth(Spring.sum(Spring.scale(deltaW,
					(float) wa), cw0));
			layout.getConstraints(component).setHeight(Spring.sum(Spring.scale(deltaH,
					(float) ha), ch0));
		}

		@Override
		void addComponentToPanel(JPanel panel) {
			panel.add(component);
		}
		
		@Override
		public String toString() {
			return "node of {" + component.getClass().getSimpleName() + "}";
		}
	}
	
	private static final class Binary extends ComponentNode {
		private final ComponentNode node1, node2;
		private final double pa, qa, m1a, m2a, da, na;
		private final double q1a1, q1a2, n1a, q2a1, q2a2, n2a;
		private final int dMin, dPref, dMax;
		private final int pb, qb, m1b, m2b, db, nb;
		private final boolean horizontal;
		
		public Binary(ComponentNode node1, ComponentNode node2, double pa, double qa,
				double m1a, double m2a, double da, double na, double q1a1, double q1a2,
				double n1a, double q2a1, double q2a2, double n2a, int dMin, int dPref,
				int dMax, int pb, int qb, int m1b, int m2b, int db, int nb, boolean horizontal) {
			if (pa+m1a+m2a+da < 0.0 || pa+m1a+m2a+da > 1.0) {
				if (horizontal)
					throw new IllegalArgumentException("Horizontal scale outside range 0 <= x <= 1");
				else
					throw new IllegalArgumentException("Vertical scale outside range 0 <= y <= 1");
			}
			if (qa+na < 0.0 || qa+na > 1.0) {
				if (horizontal)
					throw new IllegalArgumentException("Vertical scale outside range 0 <= y <= 1");
				else
					throw new IllegalArgumentException("Horizontal scale outside range 0 <= x <= 1");
			}
			this.pa = pa;
			this.qa = qa;
			this.m1a = m1a;
			this.m2a = m2a;
			this.da = da;
			this.na = na;
			this.q1a1 = q1a1;
			this.q1a2 = q1a2;
			this.n1a = n1a;
			this.q2a1 = q2a1;
			this.q2a2 = q2a2;
			this.n2a = n2a;
			this.dMin = dMin;
			this.dPref = dPref;
			this.dMax = dMax;
			this.pb = pb;
			this.qb = qb;
			this.m1b = m1b;
			this.m2b = m2b;
			this.db = db;
			this.nb = nb;
			this.node1 = node1;
			this.node2 = node2;
			this.horizontal = horizontal;
		}
		
		private Dimension calculateSize(Dimension node1Size, Dimension node2Size, int d) {
			Dimension dimension;
			int m = -(m1b+m2b+db), n = -nb;
			if (horizontal) {
				dimension = Dimensions.combine(node1Size, node2Size, horizontal, d, m, n);
			}
			else {
				dimension = Dimensions.combine(node1Size, node2Size, horizontal, d, n, m);
			}
			return dimension;
		}

		@Override
		public Dimension getMinSize() {
			return calculateSize(node1.getMinimumSize(), node2.getMinimumSize(), dMin);
		}

		@Override
		public Dimension getPrefSize() {
			return calculateSize(node1.getPreferredSize(), node2.getPreferredSize(), dPref);
		}

		@Override
		public Dimension getMaxSize() {
			return calculateSize(node1.getMaximumSize(), node2.getMaximumSize(), dMax);
		}
		
		@Override
		void refreshLayout(SpringLayout layout, JPanel panel, Spring x, Spring y,
				Spring w, Spring h) {
			Spring cm, cm1, cm2, cd, cn, cn0, cn1, cn2;
			Dimension min = node1.getMinimumSize(),
					pref = node1.getPreferredSize(),
					max = node1.getMaximumSize();
			if (horizontal) {
				cm1 = Spring.constant(min.width, pref.width, max.width);
				cn1 = Spring.constant(min.height, pref.height, max.height);
			}
			else {
				cm1 = Spring.constant(min.height, pref.height, max.height);
				cn1 = Spring.constant(min.width, pref.width, max.width);
			}
			min = node2.getMinimumSize();
			pref = node2.getPreferredSize();
			max = node2.getMaximumSize();
			if (horizontal) {
				cm2 = Spring.constant(min.width, pref.width, max.width);
				cn2 = Spring.constant(min.height, pref.height, max.height);
			}
			else {
				cm2 = Spring.constant(min.height, pref.height, max.height);
				cn2 = Spring.constant(min.width, pref.width, max.width);
			}
			cn0 = Spring.max(cn1, cn2);
			cd = Spring.constant(dMin, dPref, dMax);
			min = getMinimumSize();
			pref = getPreferredSize();
			max = getMaximumSize();
			if (horizontal) {
				cm = Spring.constant(min.width, pref.width, max.width);
				cn = Spring.constant(min.height, pref.height, max.height);
			}
			else {
				cm = Spring.constant(min.height, pref.height, max.height);
				cn = Spring.constant(min.width, pref.width, max.width);
			}
			Spring p, q, m, n;
			if (horizontal) {
				p = x;
				q = y;
				m = w;
				n = h;
			}
			else {
				p = y;
				q = x;
				m = h;
				n = w;
			}
			Spring deltaM = Spring.sum(m, Spring.minus(cm));
			Spring deltaN = Spring.sum(n, Spring.minus(cn));
			
			Spring p1 = Spring.sum(p, Spring.sum(Spring.scale(deltaM, (float) pa),
					Spring.constant(pb)));
			Spring q0 = Spring.sum(q, Spring.sum(Spring.scale(deltaN, (float) qa),
					Spring.constant(qb)));
			Spring n0 = Spring.sum(Spring.scale(deltaN, (float) na), cn0);
			Spring m1 = Spring.sum(Spring.scale(deltaM, (float) m1a), cm1);
			Spring m2 = Spring.sum(Spring.scale(deltaM, (float) m2a), cm2);
			Spring d = Spring.sum(Spring.scale(deltaM, (float) da), cd);
			Spring n1 = Spring.sum(Spring.scale(n0, (float) n1a),
					Spring.scale(cn1, (float) (1.0-n1a)));
			Spring n2 = Spring.sum(Spring.scale(n0, (float) n2a),
					Spring.scale(cn2, (float) (1.0-n2a)));
			Spring p2 = Spring.sum(p1, Spring.sum(m1, d));
			Spring q1 = Spring.sum(q0, Spring.sum(Spring.scale(cn1, (float) -q1a2),
					Spring.scale(n0, (float) q1a1)));
			Spring q2 = Spring.sum(q0, Spring.sum(Spring.scale(cn2, (float) -q2a2),
					Spring.scale(n0, (float) q2a1)));
			if (horizontal) {
				node1.refreshLayout(layout, panel, p1, q1, m1, n1);
				node2.refreshLayout(layout, panel, p2, q2, m2, n2);
			}
			else {
				node1.refreshLayout(layout, panel, q1, p1, n1, m1);
				node2.refreshLayout(layout, panel, q2, p2, n2, m2);
			}
		}

		@Override
		void addComponentToPanel(JPanel panel) {
			node1.addComponentToPanel(panel);
			node2.addComponentToPanel(panel);
		}
		
		@Override
		public String toString() {
			return "node of {" + node1 + ", " + node2 + "}";
		}
	}
	
	public void refreshLayout(JPanel panel) {
		SpringLayout layout = new SpringLayout();
		panel.setLayout(layout);
		Dimension min = getMinimumSize(), pref = getPreferredSize(), max = getMaximumSize();
		Insets insets = panel.getInsets();
		panel.setMinimumSize(Dimensions.addOffset(min, insets.left+insets.right,
				insets.top+insets.bottom));
		panel.setPreferredSize(Dimensions.addOffset(pref, insets.left+insets.right,
				insets.top+insets.bottom));
		panel.setMaximumSize(Dimensions.addOffset(max, insets.left+insets.right,
				insets.top+insets.bottom));
		refreshLayout(layout, panel, layout.getConstraint(SpringLayout.WEST, panel),
				layout.getConstraint(SpringLayout.NORTH, panel),
				layout.getConstraint(SpringLayout.WIDTH, panel),
				layout.getConstraint(SpringLayout.HEIGHT, panel));
	}
	
	public void addToPanel(JPanel panel) {
		addComponentToPanel(panel);
	}
}
