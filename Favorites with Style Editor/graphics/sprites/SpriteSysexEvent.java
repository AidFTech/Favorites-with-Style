package sprites;

import java.awt.Color;
import java.awt.Graphics;

import event_dialogs.SysexEventDialog;
import fwsevents.FWSSysexEvent;

public class SpriteSysexEvent extends Sprite {
	private static final long serialVersionUID = -1887701883699491163L;

	public SpriteSysexEvent (final int x, final int y, final int w, final int h, FWSSysexEvent parent) {
		super(x, y, w, h);
		this.affected_event = parent;
	}

	public void paintComponent(Graphics g) {
		FWSSysexEvent affected_event = (FWSSysexEvent)this.affected_event;

		super.paintComponent(g);
		
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width-1, height-1);
		if(isselected == false)
			g.setColor(Color.BLACK);
		else
			g.setColor(Color.RED);
		g.drawRect(0, 0, width-1, height-1);
		
		g.setColor(Color.BLACK);
		String sysex_text = "";
		for(int i=0;i<affected_event.data.length;i+=1)
			sysex_text += Integer.toHexString(affected_event.data[i]&0xFF) + (i<affected_event.data.length - 1 ? " " : "");

		g.drawString(sysex_text, 2, (int)(this.height*0.75));
		setToolTipText("Sysex: " + sysex_text);
	}

	@Override
	public void createDialog() {
		new SysexEventDialog(getViewport().getMainWindow(), (FWSSysexEvent)affected_event);
	}
}
