package sprites;

import java.awt.Color;
import java.awt.Graphics;

import event_dialogs.ChordEventDialog;
import fwsevents.FWSChordEvent;

public class SpriteChordEvent extends Sprite {
	private static final long serialVersionUID = -2170907281285894024L;

	public SpriteChordEvent(final int x, final int y, final int w, final int h, FWSChordEvent parent) {
		super(x, y, w, h);
		this.affected_event = parent;
	}

	public void paintComponent(Graphics g) {
		FWSChordEvent affected_event = (FWSChordEvent)this.affected_event;

		super.paintComponent(g);
		
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width-1, height-1);
		if(isselected == false)
			g.setColor(Color.BLACK);
		else
			g.setColor(Color.RED);
		g.drawRect(0, 0, width-1, height-1);
		
		g.setColor(Color.BLACK);

		g.drawString(affected_event.main_chord.getName(), 2, (int) (this.height*0.75));

		if(affected_event.bass_chord != null && !affected_event.bass_chord.getNoChord())
			this.setToolTipText(affected_event.main_chord.getName() + " / " + affected_event.bass_chord.getName());
		else
			this.setToolTipText(affected_event.main_chord.getName());
	}

	@Override
	public void createDialog() {
		new ChordEventDialog(getViewport().getMainWindow(), (FWSChordEvent)affected_event);
	}
}
