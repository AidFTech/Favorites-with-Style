package sprites;

import java.awt.Color;
import java.awt.Graphics;

import event_dialogs.StyleChangeEventDialog;
import fwsevents.FWSStyleChangeEvent;

public class SpriteStyleEvent extends Sprite {
	private static final long serialVersionUID = -3293283589003139241L;

	public SpriteStyleEvent(final int x, final int y, final int w, final int h, FWSStyleChangeEvent parent) {
		super(x, y, w, h);
		this.affected_event = parent;
	}

	public void paintComponent(Graphics g) {
		FWSStyleChangeEvent affected_event = (FWSStyleChangeEvent)this.affected_event;

		super.paintComponent(g);
		
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width-1, height-1);
		if(isselected == false)
			g.setColor(Color.BLACK);
		else
			g.setColor(Color.RED);
		g.drawRect(0, 0, width-1, height-1);
		
		g.setColor(Color.BLACK);

		if(!affected_event.style_name.isEmpty()) {
			g.drawString(affected_event.style_name, 2, (int) (this.height*0.4));
			g.drawString(affected_event.section_name, 2, (int) (this.height*0.9));
		} else
			g.drawString("Accompaniment Off", 2, (int) (this.height*0.75));

		String tooltip_text = "<html>" + affected_event.toString() + "</html>";
		tooltip_text = tooltip_text.replace("\n", "<br/>");
		setToolTipText(tooltip_text);
	}

	@Override
	public void createDialog() {
		new StyleChangeEventDialog(getViewport().getMainWindow(), (FWSStyleChangeEvent)affected_event);
	}
}
