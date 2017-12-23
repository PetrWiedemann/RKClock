package net.pdynet.clock1j9;

import java.awt.BorderLayout;

import javax.swing.JFrame;

public class AnalogClock extends JFrame {
	private static final long serialVersionUID = 1L;

	public AnalogClock() {
		ClockPanel container = new ClockPanel();
		add(container, BorderLayout.CENTER);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("RK Clock");
		setResizable(false);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public static void main(String[] args) {
		new AnalogClock();
	}
}
