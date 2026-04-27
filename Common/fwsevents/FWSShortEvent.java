package fwsevents;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;

public class FWSShortEvent extends FWSEvent {
	public byte channel, command, data1, data2;

	public FWSShortEvent() {

	}

	public FWSShortEvent(final FWSShortEvent c) {
		super(c);
		this.channel = c.channel;
		this.command = c.command;
		this.data1 = c.data1;
		this.data2 = c.data2;
	}

	@Override
	public String toString() {
		String desc_string = "Channel " + (channel + 1) + ": ";
		desc_string += Integer.toHexString(command&0xFF).toUpperCase() + " ";
		desc_string += "[" + data1 + ", ";
		desc_string += data2 + "]";
		
		return desc_string;
	}

	public byte[] getMIDIBytes() {
		return new byte[] {(byte)(((int)this.command&0xF0) | this.channel), this.data1, this.data2};
	}

	/** Get the true MIDI command from this event. */
	public MidiEvent[] getMIDIEvents() {
		try {
			ShortMessage short_message = new ShortMessage(command&0xFF, channel&0xFF, data1&0xFF, data2&0xFF);
			return new MidiEvent[] {new MidiEvent(short_message, tick)};
		} catch (InvalidMidiDataException e) {
			return new MidiEvent[0];
		}
	}

	/** Compare short events. May still return true if the tick is different. */
	public boolean equals(FWSShortEvent c) {
		return (this.channel == c.channel &&
				this.command == c.command &&
				this.data1 == c.data1 &&
				this.data2 == c.data2);
	}
}
