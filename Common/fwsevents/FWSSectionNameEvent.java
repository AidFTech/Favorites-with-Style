package fwsevents;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;

public class FWSSectionNameEvent extends FWSEvent {
	public String section_name = "";

	public FWSSectionNameEvent() {
		
	}

	public FWSSectionNameEvent(FWSSectionNameEvent c) {
		super(c);
		this.section_name = c.section_name;
	}

	public byte[] getMIDIBytes() {
		byte[] data = section_name.getBytes();

		byte[] bytes = new byte[data.length + 3];
		bytes[0] = (byte)0xFF;
		bytes[1] = (byte)6;
		bytes[2] = (byte)data.length;
		for(int i=0;i<data.length;i+=1)
			bytes[i+3] = data[i];

		return bytes;
	}

	@Override
	public String toString() {
		return "Section " + section_name;
	}

	/** Get the true MIDI events from this event. */
	public MidiEvent[] getMIDIEvents() {
		try {
			byte[] data = section_name.getBytes();
			MetaMessage midi_message = new MetaMessage(6, data, data.length);
			return new MidiEvent[] {new MidiEvent(midi_message, tick)};
		} catch (InvalidMidiDataException e) {
			return new MidiEvent[0];
		}
	}
}
