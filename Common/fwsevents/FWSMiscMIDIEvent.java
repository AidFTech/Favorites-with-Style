package fwsevents;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;

public class FWSMiscMIDIEvent extends FWSEvent {
	public byte[] data = new byte[0];
	public int type;

	public static final String[] midi_text_str = {
													"Text",
													"Copyright",
													"Track",
													"Voice",
													"Lyric",
													"Marker",
													"Cue",
												};

	public FWSMiscMIDIEvent() {

	}

	public FWSMiscMIDIEvent(final FWSMiscMIDIEvent c) {
		super(c);
		this.type = c.type;
		this.data = new byte[c.data.length];

		for(int i=0;i<c.data.length;i+=1)
			this.data[i] = c.data[i];
	}

	@Override
	public String toString() {
		String desc = "Misc: [";

		for(int i=0;i<data.length;i+=1)
			desc += Integer.toHexString(data[i]&0xFF).toUpperCase() + (i < data.length - 1 ? " " : "");

		desc += "]";

		return desc;
	}

	public byte[] getMIDIBytes() {
		byte[] bytes = new byte[data.length + 3];
		bytes[0] = (byte)0xFF;
		bytes[1] = (byte)type;
		bytes[2] = (byte)data.length;
		for(int i=0;i<data.length;i+=1)
			bytes[i+3] = data[i];

		return bytes;
	}

	/** Get the true MIDI events from this event. */
	public MidiEvent[] getMIDIEvents() {
		try {
			MetaMessage midi_message = new MetaMessage(type, data, data.length);
			return new MidiEvent[] {new MidiEvent(midi_message, tick)};
		} catch (InvalidMidiDataException e) {
			return new MidiEvent[0];
		}
	}
}
