package sprites;

import java.awt.Color;
import java.awt.Graphics;

import event_dialogs.SectionNameEventDialog;
import fwsevents.FWSSectionNameEvent;

public class SpriteSectionNameEvent extends Sprite {
	private static final long serialVersionUID = 5826947441692528411L;

	public SpriteSectionNameEvent(final int x, final int y, final int w, final int h, FWSSectionNameEvent parent) {
		super(x, y, w, h);
		this.affected_event = parent;
	}

	public void paintComponent(Graphics g) {
		FWSSectionNameEvent affected_event = (FWSSectionNameEvent)this.affected_event;

		super.paintComponent(g);

		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width-1, height-1);
		if(isselected == false)
			g.setColor(Color.BLACK);
		else
			g.setColor(Color.RED);
		g.drawRect(0, 0, width-1, height-1);
		
		g.setColor(Color.BLACK);

		g.drawString(affected_event.section_name, 2, (int)(this.height*0.75));
		setToolTipText("Section: " + affected_event.section_name);
	}

	@Override
	public void createDialog() {
		new SectionNameEventDialog(getViewport().getMainWindow(), (FWSSectionNameEvent)affected_event);
	}
}
