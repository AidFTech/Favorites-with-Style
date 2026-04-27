package fwsevents;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;

public class FWSTimeSignatureEvent extends FWSEvent {
	public byte num = 4, den = 2;

	public FWSTimeSignatureEvent() {

	}

	public FWSTimeSignatureEvent(final FWSTimeSignatureEvent c) {
		super(c);
		this.num = c.num;
		this.den = c.den;
	}

	@Override
	public String toString() {
		String desc = "Time Signature: ";
		desc += Integer.toString(num) + '/' + Integer.toString((int)Math.pow(2,den));
		return desc;
	}

	public byte[] getMIDIBytes() {
		byte[] time_data = {num, den, 0x18, 0x8};
		byte[] bytes = {(byte)0xFF, 0x58, 4, time_data[0], time_data[1], time_data[2], time_data[3]};
		return bytes;
	}

	/** Get the true time signature event. */
	public MidiEvent[] getMIDIEvents() {
		try {
			byte[] time_data = {num, den, 0x18, 0x8};
			MetaMessage time_message = new MetaMessage(0x58, time_data, time_data.length);
			return new MidiEvent[] {new MidiEvent(time_message, tick)};
		} catch (InvalidMidiDataException e) {
			return new MidiEvent[0];
		}
	}
}
