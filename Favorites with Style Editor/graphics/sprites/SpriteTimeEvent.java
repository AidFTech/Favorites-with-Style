package sprites;

import java.awt.Color;
import java.awt.Graphics;

import event_dialogs.TimeEventDialog;
import fwsevents.FWSTimeSignatureEvent;

public class SpriteTimeEvent extends Sprite {
	private static final long serialVersionUID = -3031533341247926630L;

	public SpriteTimeEvent(final int x, final int y, final int w, final int h, FWSTimeSignatureEvent parent) {
		super(x, y, w, h);
		this.affected_event = parent;

		if(affected_event.tick == 0)
			locked_x = true;
	}

	public void paintComponent(Graphics g) {
		FWSTimeSignatureEvent affected_event = (FWSTimeSignatureEvent)this.affected_event;

		super.paintComponent(g);
		
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width-1, height-1);
		if(isselected == false)
			g.setColor(Color.BLACK);
		else
			g.setColor(Color.RED);
		g.drawRect(0, 0, width-1, height-1);
		
		g.setColor(Color.BLACK);
		g.drawString(Integer.toString(affected_event.num) + "/" + Integer.toString((int)Math.pow(2,affected_event.den)),2,(int) (this.height*0.75));

		this.setToolTipText(Integer.toString(affected_event.num) + "/" + Integer.toString((int)Math.pow(2,affected_event.den)));
	}
	
	@Override
	public void createDialog() {
		new TimeEventDialog(getViewport().getMainWindow(), (FWSTimeSignatureEvent)affected_event);
	}
}
