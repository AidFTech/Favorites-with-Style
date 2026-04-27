package sprites;

import java.awt.Color;
import java.awt.Graphics;

import event_dialogs.MiscEventDialog;
import fwsevents.FWSMiscMIDIEvent;

public class SpriteMiscMIDIEvent extends Sprite {
	private static final long serialVersionUID = -8963630092077437629L;

	public SpriteMiscMIDIEvent(final int x, final int y, final int w, final int h, FWSMiscMIDIEvent parent) {
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
		String midi_text = "";
		for(int i=0;i<affected_event.data.length;i+=1)
			midi_text += Integer.toHexString(affected_event.data[i]&0xFF) + (i<affected_event.data.length - 1 ? " " : "");

		g.drawString(midi_text, 2, (int)(this.height*0.75));
		setToolTipText("Misc MIDI: " + midi_text);
	}
	
	@Override
	public void createDialog() {
		new MiscEventDialog(getViewport().getMainWindow(), (FWSMiscMIDIEvent)affected_event);
	}
}
