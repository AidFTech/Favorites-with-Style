package sprites;

import java.awt.Color;
import java.awt.Graphics;
import java.nio.charset.StandardCharsets;

import event_dialogs.TextEventDialog;
import fwsevents.FWSMiscMIDIEvent;

public class SpriteTextEvent extends Sprite {
	private static final long serialVersionUID = -5675953681286647653L;

	public SpriteTextEvent(final int x, final int y, final int w, final int h, FWSMiscMIDIEvent parent) {
		super(x, y, w, h);
		this.affected_event = parent;
	}

	public void paintComponent(Graphics g) {
		FWSMiscMIDIEvent affected_event = (FWSMiscMIDIEvent)this.affected_event;

		super.paintComponent(g);
		
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width-1, height-1);
		if(isselected == false)
			g.setColor(Color.BLACK);
		else
			g.setColor(Color.RED);
		g.drawRect(0, 0, width-1, height-1);
		
		g.setColor(Color.BLACK);
		String midi_text = new String(affected_event.data, StandardCharsets.ISO_8859_1);
		String header = "", tooltip_header = "";

		switch(affected_event.type) {
		case 1:
			header = "Text: ";
			break;
		case 2:
			header = "Copyright: ";
			break;
		case 3:
			header = "Track: ";
			break;
		case 4:
			header = "Voice: ";
			break;
		case 7:
			header = "Cue: ";
			break;
		}

		switch(affected_event.type) {
		case 1:
		case 2:
		case 3:
		case 4:
		case 7:
			tooltip_header = header;
			break;
		case 5:
			tooltip_header = "Lyric: ";
			break;
		case 6:
			tooltip_header = "Marker: ";
			break;
		}

		g.drawString(header + midi_text, 2, (int)(this.height*0.75));
		setToolTipText(tooltip_header + midi_text);
	}

	@Override
	public void createDialog() {
		new TextEventDialog(getViewport().getMainWindow(), (FWSMiscMIDIEvent)affected_event);
	}
}
