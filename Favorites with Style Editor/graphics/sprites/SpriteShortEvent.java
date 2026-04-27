package sprites;

import java.awt.Color;
import java.awt.Graphics;

import javax.sound.midi.ShortMessage;

import event_dialogs.ChannelPressureEventDialog;
import event_dialogs.ControlEventDialog;
import event_dialogs.KeyPressureEventDialog;
import event_dialogs.PitchBendEventDialog;
import fwsevents.FWSShortEvent;
import midicontrol.MIDIControl;

public class SpriteShortEvent extends Sprite {
	private static final long serialVersionUID = 4829770998721582429L;
	
	private Color color;

	public SpriteShortEvent(final int x, final int y, final int w, final int h, Color color, FWSShortEvent parent) {
		super(x, y, w, h);
		this.affected_event = parent;
		this.color = color;
	}

	public void paintComponent(Graphics g) {
		FWSShortEvent affected_event = (FWSShortEvent)this.affected_event;
		super.paintComponent(g);

		g.setColor(this.color);
		g.fillRect(0, 0, width-1, height-1);
		
		if(isselected == false)
			g.setColor(Color.BLACK);
		else
			g.setColor(Color.RED);
		g.drawRect(0, 0, width-1, height-1);

		String event_text = "";
		switch(affected_event.command&0xFF) {
		case ShortMessage.POLY_PRESSURE:
			event_text = "Note " + affected_event.data1 + ", Pressure " + affected_event.data2;
			break;
		case ShortMessage.CONTROL_CHANGE:
			{
				final MIDIControl ctl = MIDIControl.getControl(affected_event.data1);
				if(ctl != null) {
					final String ctl_str = ctl.getName().replace(" On/Off", "");
					event_text = ctl_str;
					if(affected_event.data1 == ctl.getValue() && ctl.getValue() != ctl.getLSBValue())
						event_text += " MSB";
					else if(affected_event.data1 == ctl.getLSBValue() && ctl.getValue() != ctl.getLSBValue())
						event_text += " LSB";

					switch(ctl) {
					case SUSTAIN_PEDAL:
					case SOFT:
					case SOSTENUTO:
					case PORTAMENTO:
					case LEGATO:
					case HOLD_2:
					case LOCAL_CONTROL:
						event_text += affected_event.data2 >= 0x64 ? " On" : " Off";
						break;
					case ALL_SOUND_OFF:
					case RESET_CONTROLLERS:
					case ALL_NOTES_OFF:
					case OMNI_OFF:
					case OMNI_ON:
					case MONO_ON:
					case POLY_ON:
						break;
					default:
						event_text +=  ": " + affected_event.data2;
						break;
					}
				} else
					event_text = "CTL " + affected_event.data1 + ": " + affected_event.data2;	
			}
			break;
		case ShortMessage.CHANNEL_PRESSURE:
			event_text = "Aftertouch: " + affected_event.data1;
			break;
		case ShortMessage.PITCH_BEND:
			{
				int pb = affected_event.data1 + affected_event.data2*0x80;
				pb -= 0x2000;
				event_text = Integer.toString(pb);
			}
			break;
		}

		g.setColor(Color.BLACK);
		g.drawString(event_text, 2,(int) (this.height*0.75));
		this.setToolTipText("Channel " + (affected_event.channel + 1) + ": " + event_text);
	}

	@Override
	public void createDialog() {
		FWSShortEvent affected_event = (FWSShortEvent)this.affected_event;
		switch(affected_event.command&0xFF) {
		case ShortMessage.PITCH_BEND:
			new PitchBendEventDialog(getViewport().getMainWindow(), affected_event);
			break;
		case ShortMessage.CHANNEL_PRESSURE:
			new ChannelPressureEventDialog(getViewport().getMainWindow(), affected_event);
			break;
		case ShortMessage.POLY_PRESSURE:
			new KeyPressureEventDialog(getViewport().getMainWindow(), affected_event);
			break;
		case ShortMessage.CONTROL_CHANGE:
			new ControlEventDialog(getViewport().getMainWindow(), affected_event);
			break;
		}
	}
}
