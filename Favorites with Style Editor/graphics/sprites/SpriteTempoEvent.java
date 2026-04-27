package sprites;

import java.awt.Color;
import java.awt.Graphics;

import event_dialogs.TempoEventDialog;
import fwsevents.FWSTempoEvent;

public class SpriteTempoEvent extends Sprite {
	private static final long serialVersionUID = 7653910496677512853L;

	public SpriteTempoEvent(final int x, final int y, final int w, final int h, FWSTempoEvent parent) {
		super(x, y, w, h);
		this.affected_event = parent;

		if(affected_event.tick == 0)
			locked_x = true;
	}
	
	public void paintComponent(Graphics g) {
		FWSTempoEvent affected_event = (FWSTempoEvent)this.affected_event;
		super.paintComponent(g);
		
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width-1, height-1);
		if(isselected == false)
			g.setColor(Color.BLACK);
		else
			g.setColor(Color.RED);
		g.drawRect(0, 0, width-1, height-1);
		
		g.setColor(Color.BLACK);
		g.drawString(Integer.toString(affected_event.tempo), 2, (int)(this.height*0.75));
		
		this.setToolTipText(Integer.toString(affected_event.tempo) + " BPM");
	}

	@Override
	public void createDialog() {
		new TempoEventDialog(getViewport().getMainWindow(), (FWSTempoEvent)affected_event);
	}
}
