package fwsevents;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;

public class FWSNoteEvent extends FWSEvent {
	public static final byte middle_c = 60, low_c = 36, yamaha_split = 54, casio_split = 53;

	public static final String[] note_map = {"C", "C♯", "D", "E♭", "E", "F", "F♯", "G", "A♭", "A", "B♭", "B"};

	public byte note = middle_c, channel = 0, velocity = 100;
	public long duration;

	public FWSNoteEvent() {

	}

	public FWSNoteEvent(final FWSNoteEvent c) {
		super(c);
		this.note = c.note;
		this.channel = c.channel;
		this.velocity = c.velocity;
		this.duration = c.duration;
	}

	public byte[] getMIDIBytes() {
		return new byte[] {(byte)(0x90 | channel), note, velocity};
	}

	/** Get the true MIDI note events from this note. */
	public MidiEvent[] getMIDIEvents() {
		try {
			ShortMessage note_on_msg = new ShortMessage();
			note_on_msg.setMessage(ShortMessage.NOTE_ON, channel&0xFF, note, velocity);
			ShortMessage note_off_msg = new ShortMessage();
			note_off_msg.setMessage(ShortMessage.NOTE_OFF, channel&0xFF, note, velocity);
			
			MidiEvent note_on_event = new MidiEvent(note_on_msg, tick), note_off_event = new MidiEvent(note_off_msg, tick + duration);
			
			return new MidiEvent[] {note_on_event, note_off_event};
		} catch (InvalidMidiDataException e) {
			return new MidiEvent[0];
		}
	}

	@Override
	public String toString() {
		String desc = "Channel " + channel + ": ";
		desc += note_map[note%12];
		desc += Integer.toString(note/12 - 1) + ", ";
		desc += "Vel. " + velocity + ", ";
		desc += "Duration " + duration;

		return desc;
	}
}
