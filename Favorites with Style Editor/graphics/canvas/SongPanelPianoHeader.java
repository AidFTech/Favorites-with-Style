package canvas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JPanel;

import controllers.FWS;

public class SongPanelPianoHeader extends JPanel {
	private static final long serialVersionUID = -7232739552286252395L;
	
	private int note_highlighted = -1;

	public SongPanelPianoHeader() {
		this.setBackground(Color.WHITE);
		this.setLayout(null);
		
		this.setPreferredSize(new Dimension(80,FWS.event_height*133));
		this.setSize(new Dimension(80,FWS.event_height*133));
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Color selection;
		
		for(int i=0;i<128;i+=1) {
			if(127-i==note_highlighted)
				selection = Color.RED;
			else
				selection = Color.WHITE;
			switch(i%12) {
			case 0: //C
			case 5: //F
				g.setColor(selection);
				g.fillRect(0, (127-i)*FWS.event_height - FWS.event_height/2, this.getWidth(), FWS.event_height + FWS.event_height/2);
				g.setColor(Color.BLACK);
				g.drawRect(0, (127-i)*FWS.event_height - FWS.event_height/2, this.getWidth(), FWS.event_height + FWS.event_height/2);
				break;
			case 2: //D
			case 7: //G
			case 9: //A
				g.setColor(selection);
				g.fillRect(0, (127-i)*FWS.event_height - FWS.event_height/2, this.getWidth(), FWS.event_height + FWS.event_height);
				g.setColor(Color.BLACK);
				g.drawRect(0, (127-i)*FWS.event_height - FWS.event_height/2, this.getWidth(), FWS.event_height + FWS.event_height);
				break;
			case 4: //E
			case 11: //B
				g.setColor(selection);
				g.fillRect(0, (127-i)*FWS.event_height, this.getWidth(), FWS.event_height + FWS.event_height/2 + 1);
				g.setColor(Color.BLACK);
				g.drawRect(0, (127-i)*FWS.event_height, this.getWidth(), FWS.event_height + FWS.event_height/2 + 1);
				break;
			}
			if(i%12 == 0)
			{
				g.setColor(Color.BLACK);
				if(i/12 == 5)
					g.setFont(new Font("default",Font.BOLD,11));
				else
					g.setFont(new Font("default",Font.PLAIN,11));
				g.drawString("C" + (i/12 - 1), 60, (127-i)*FWS.event_height + FWS.event_height/2);
			}
		}

		for(int i=0;i<128;i+=1) {
			if(127-i==note_highlighted)
				selection = Color.RED;
			else
				selection = Color.BLACK;

			
			switch(i%12) {
			case 1:
			case 3:
			case 6:
			case 8:
			case 10:
				g.setColor(selection);
				g.fillRect(0, (127-i)*FWS.event_height, (int)(this.getWidth()*0.6), FWS.event_height);
				g.setColor(Color.BLACK);
				g.drawRect(0, (127-i)*FWS.event_height, (int)(this.getWidth()*0.6), FWS.event_height);
				break;
			}
		}
		
		g.setFont(new Font("default",Font.PLAIN,12));
		g.setColor(Color.BLACK);
		g.drawString("Voice", 5, (int)(FWS.event_height*0.75) + FWS.event_height*128);
		g.drawString("Control", 5, (int)(FWS.event_height*0.75) + FWS.event_height*129);
		g.drawString("Key Press.", 5, (int)(FWS.event_height*0.75) + FWS.event_height*130);
		g.drawString("Ch. Press.", 5, (int)(FWS.event_height*0.75) + FWS.event_height*131);
		g.drawString("Pitch Bend", 5, (int)(FWS.event_height*0.75) + FWS.event_height*132);
		
		g.setColor(Color.BLACK);
		g.drawLine(this.getWidth() - 1, 0, this.getWidth() - 1, this.getHeight() - 1);
	}

	/** Set the highlighted key. */
	public void recolor(int note) {
		note_highlighted = note;
		this.repaint();
	}
}
