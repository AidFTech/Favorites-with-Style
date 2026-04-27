package sprites;

import java.awt.Color;
import java.awt.Graphics;

import event_dialogs.VoiceEventDialog;
import fwsevents.FWSVoiceEvent;
import voices.Voice;

public class SpriteVoiceEvent extends Sprite {
	private static final long serialVersionUID = -1051421886076723325L;

	private Color color;

	public SpriteVoiceEvent(final int x, final int y, final int w, final int h, Color color, FWSVoiceEvent parent) {
		super(x, y, w, h);
		this.affected_event = parent;
		this.color = color;
	}

	public void paintComponent(Graphics g) {
		FWSVoiceEvent affected_event = (FWSVoiceEvent)this.affected_event;

		super.paintComponent(g);

		g.setColor(this.color);
		g.fillRect(0, 0, width-1, height-1);
		
		if(isselected == false)
			g.setColor(Color.BLACK);
		else
			g.setColor(Color.RED);
		g.drawRect(0, 0, width-1, height-1);

		Voice[] voice_list = getViewport().getController().getVoiceList();
		Voice voice = Voice.matchVoice(voice_list, affected_event.voice, affected_event.voice_lsb, affected_event.voice_msb);
		String voice_name = "";

		if(voice != null)
			voice_name = voice.name;
		else
			voice_name = affected_event.voice + ", " + affected_event.voice_lsb + ", " + affected_event.voice_msb;

		g.setColor(Color.BLACK);
		g.drawString(voice_name, 2,(int) (this.height*0.75));

		this.setToolTipText("Channel " + (affected_event.channel + 1) + ": " + voice_name);
	}

	/** Set the voice color. */
	public void setColor(Color color) {
		this.color = color;
		this.repaint();
	}

	@Override
	public void createDialog() {
		new VoiceEventDialog(getViewport().getMainWindow(), (FWSVoiceEvent)affected_event);
	}
}
