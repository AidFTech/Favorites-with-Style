package fwsevents;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.SysexMessage;

public class FWSSysexEvent extends FWSEvent {
	public int status;
	public byte[] data = new byte[0];

	public FWSSysexEvent() {

	}

	public FWSSysexEvent(final FWSSysexEvent c) {
		super(c);
		this.status = c.status;
		this.data = new byte[c.data.length];

		for(int i=0;i<c.data.length;i+=1)
			this.data[i] = c.data[i];
	}

	@Override
	public String toString() {
		String desc = "Sysex: [";

		for(int i=0;i<data.length;i+=1)
			desc += Integer.toHexString(data[i]&0xFF).toUpperCase() + (i < data.length - 1 ? " " : "");

		desc += "]";

		return desc;
	}

	public byte[] getMIDIBytes() {
		byte[] bytes;
		if(data.length > 0 && (data[0] == 0xF7 || data[0] == 0xF0)) {
			bytes = new byte[data.length];
			for(int i=0;i<data.length;i+=1)
				bytes[i] = data[i];
		} else {
			bytes = new byte[data.length + 1];
			bytes[0] = (byte)0xF7;
			for(int i=0;i<data.length;i+=1)
				bytes[i+1] = data[i];
		}
		return bytes;
	}

	/** Get the true MIDI sysex events from this event. */
	public MidiEvent[] getMIDIEvents() {
		try {
			SysexMessage sysex_message = new SysexMessage(data, data.length);
			return new MidiEvent[] {new MidiEvent(sysex_message, tick)};
		} catch (InvalidMidiDataException e) {
			return new MidiEvent[0];
		}
	}
}
