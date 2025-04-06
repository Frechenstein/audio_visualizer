package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

public class WindowPanel extends JPanel implements Runnable{

	int screenWidth = 1280;
	int screenHeight = 720;
	
	public WindowPanel() {
		
		this.setPreferredSize(new Dimension(screenWidth, screenHeight));
		this.setBackground(Color.black);
		this.setDoubleBuffered(true);
		this.setFocusable(true);
		
	}
	
	@Override
	public void run() {
		
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.white);
		g.fillRect(50, 50, 1, 1);
	}
	
}
