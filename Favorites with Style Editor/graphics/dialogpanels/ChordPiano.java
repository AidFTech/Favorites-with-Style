package dialogpanels;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Arrays;

import javax.swing.JPanel;

import style.ChordBody;

public class ChordPiano extends JPanel {
	private static final long serialVersionUID = 593426071733076140L;
	
	private static final int keywidth = 10, keyheight = 40;
	private boolean[] highlighted_keys = new boolean[36];

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		final int keystartx = this.getWidth()/2 - keywidth*14/2, keystarty = this.getHeight()/2 - keyheight/2;
		
		for(int i=0;i<14;i+=1) {
			switch(i%7) {
			case 0: //C
				if(highlighted_keys[0+12*(i/7)])
					g.setColor(Color.RED);
				else
					g.setColor(Color.WHITE);
				break;
			case 1: //D
				if(highlighted_keys[2+12*(i/7)])
					g.setColor(Color.RED);
				else
					g.setColor(Color.WHITE);
				break;
			case 2: //E
				if(highlighted_keys[4+12*(i/7)])
					g.setColor(Color.RED);
				else
					g.setColor(Color.WHITE);
				break;
			case 3: //F
				if(highlighted_keys[5+12*(i/7)])
					g.setColor(Color.RED);
				else
					g.setColor(Color.WHITE);
				break;
			case 4: //G
				if(highlighted_keys[7+12*(i/7)])
					g.setColor(Color.RED);
				else
					g.setColor(Color.WHITE);
				break;
			case 5: //A
				if(highlighted_keys[9+12*(i/7)])
					g.setColor(Color.RED);
				else
					g.setColor(Color.WHITE);
				break;
			case 6: //B
				if(highlighted_keys[11+12*(i/7)])
					g.setColor(Color.RED);
				else
					g.setColor(Color.WHITE);
				break;
			default:
				g.setColor(Color.WHITE);
				break;
			}
			g.fillRect(keystartx+i*keywidth, keystarty, keywidth, keyheight);
			g.setColor(Color.BLACK);
			g.drawRect(keystartx+i*keywidth, keystarty, keywidth, keyheight);
		}
		
		for(int i=0;i<14;i+=1) {
			if(i%7 == 0 || i%7 == 1 || i%7 == 3 || i%7 == 4 || i%7 == 5)
			{
				switch(i%7) {
				case 0: //C#
					if(highlighted_keys[1+12*(i/7)])
						g.setColor(Color.RED);
					else
						g.setColor(Color.BLACK);
					break;
				case 1: //Eb
					if(highlighted_keys[3+12*(i/7)])
						g.setColor(Color.RED);
					else
						g.setColor(Color.BLACK);
					break;
				case 3: //F#
					if(highlighted_keys[6+12*(i/7)])
						g.setColor(Color.RED);
					else
						g.setColor(Color.BLACK);
					break;
				case 4: //Ab
					if(highlighted_keys[8+12*(i/7)])
						g.setColor(Color.RED);
					else
						g.setColor(Color.BLACK);
					break;
				case 5: //Bb
					if(highlighted_keys[10+12*(i/7)])
						g.setColor(Color.RED);
					else
						g.setColor(Color.BLACK);
					break;
				default:
					g.setColor(Color.BLACK);
					break;
				}
				g.fillRect(keystartx+i*keywidth + (int)(0.8*keywidth), keystarty, (int)(keywidth*0.5), (int)(keyheight*0.6));
				g.setColor(Color.BLACK);
				g.drawRect(keystartx+i*keywidth + (int)(0.8*keywidth), keystarty, (int)(keywidth*0.5), (int)(keyheight*0.6));
			}
		}
	}

	/** Set the highlighted chord. */
	public void setHighlighted(ChordBody chord, int inversion) {
		for(int i=0;i<highlighted_keys.length;i+=1)
			highlighted_keys[i] = false;

		final byte root = chord.getRoot();
		byte[] notes = chord.getNotesDown();
		byte[] new_notes = Arrays.copyOf(notes, notes.length);

		int inv = 0, active_inv = 0;
		while(inv <= inversion && active_inv < notes.length) {
			new_notes = Arrays.copyOf(notes, notes.length);

			for(int i=0;i<active_inv && i<new_notes.length;i+=1)
				new_notes[i] += 12;

			new_notes = ChordBody.shift(new_notes, active_inv);

			boolean shift_down = true;
			for(int i=0;i<new_notes.length;i+=1) {
				if(root + new_notes[i] < 12) {
					shift_down = false;
					break;
				}
			}

			if(shift_down) {
				for(int i=0;i<new_notes.length;i+=1)
					new_notes[i] -= 12;
			}
			
			byte[] test_notes = Arrays.copyOf(new_notes, new_notes.length);
			for(int i=0;i<new_notes.length;i+=1)
				test_notes[i] += root;

			if(chord.getValid(test_notes)) {
				inv += 1;
			}
			active_inv += 1;
		}

		for(int i=0;i<notes.length;i+=1) {
			if(root + notes[i] >= 0 && root + new_notes[i] < highlighted_keys.length)
				highlighted_keys[root + new_notes[i]] = true;
		}

		this.repaint();
	}
}
