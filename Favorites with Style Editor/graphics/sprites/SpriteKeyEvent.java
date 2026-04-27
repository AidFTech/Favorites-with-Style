package sprites;

import java.awt.Color;
import java.awt.Graphics;

import event_dialogs.KeyEventDialog;
import fwsevents.FWSKeySignatureEvent;

public class SpriteKeyEvent extends Sprite {
	private static final long serialVersionUID = 595933784780725111L;

	public SpriteKeyEvent(final int x, final int y, final int w, final int h, FWSKeySignatureEvent parent) {
		super(x, y, w, h);
		this.affected_event = parent;

		if(affected_event.tick == 0)
			locked_x = true;
	}

	public void paintComponent(Graphics g) {
		FWSKeySignatureEvent affected_event = (FWSKeySignatureEvent)this.affected_event;

		String name = "", tooltip = "";
		super.paintComponent(g);
		
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width-1, height-1);
		if(isselected == false)
			g.setColor(Color.BLACK);
		else
			g.setColor(Color.RED);
		g.drawRect(0, 0, width-1, height-1);
		
		g.setColor(Color.BLACK);
		
		if(affected_event.major) {
			//Major key.
			switch(affected_event.accidental_count) {
			case 0:
				name = "C";
				break;
			case 1:
				name = "G";
				break;
			case 2:
				name = "D";
				break;
			case 3:
				name = "A";
				break;
			case 4:
				name = "E";
				break;
			case 5:
				name = "B";
				break;
			case 6:
				name = "F♯";
				break;
			case 7:
				name = "C♯";
				break;
			case -1:
				name = "F";
				break;
			case -2:
				name = "B♭";
				break;
			case -3:
				name = "E♭";
				break;
			case -4:
				name = "A♭";
				break;
			case -5:
				name = "D♭";
				break;
			case -6:
				name = "G♭";
				break;
			case -7:
				name = "C♭";
				break;
			}
			tooltip = name + " major";
		} else {
			//Minor key.
			switch(affected_event.accidental_count) {
			case 0:
				name = "A";
				break;
			case 1:
				name = "E";
				break;
			case 2:
				name = "B";
				break;
			case 3:
				name = "F♯";
				break;
			case 4:
				name = "C♯";
				break;
			case 5:
				name = "G♯";
				break;
			case 6:
				name = "D♯";
				break;
			case 7:
				name = "A♯";
				break;
			case -1:
				name = "D";
				break;
			case -2:
				name = "G";
				break;
			case -3:
				name = "C";
				break;
			case -4:
				name = "F";
				break;
			case -5:
				name = "B♭";
				break;
			case -6:
				name = "E♭";
				break;
			case -7:
				name = "A♭";
				break;
			}
			tooltip = name + " minor";
			name += "m";
		}
		
		g.drawString(name, 2, (int) (this.height*0.75));
		
		this.setToolTipText(tooltip);
	}
	
	@Override
	public void createDialog() {
		new KeyEventDialog(getViewport().getMainWindow(), (FWSKeySignatureEvent)affected_event);
	}
}
