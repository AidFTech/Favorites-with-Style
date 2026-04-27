package fwsevents;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;

public class FWSTempoEvent extends FWSEvent {
	public int tempo = 120;

	public FWSTempoEvent() {

	}

	public FWSTempoEvent(final FWSTempoEvent c) {
		super(c);
		this.tempo = c.tempo;
	}

	@Override
	public String toString() {
		String desc = "Tempo: " + tempo + "bpm"; 
		return desc;
	}

	public byte[] getMIDIBytes() {
		final float tempo_f = (float)(60000000.0/tempo);
		byte[] tempo_data = {(byte) (tempo_f/0x10000), (byte)((tempo_f%0x10000)/0x100), (byte)(tempo_f%0x100)};

		byte[] bytes = new byte[] {(byte)0xFF, 0x51, 3, tempo_data[0], tempo_data[1], tempo_data[2]};
		return bytes;
	}

	/** Get the true MIDI events from this event. */
	public MidiEvent[] getMIDIEvents() {
		try {
			final float tempo_f = (float)(60000000.0/tempo);

			byte[] tempo_data = {(byte) (tempo_f/0x10000), (byte)((tempo_f%0x10000)/0x100), (byte)(tempo_f%0x100)};
			MetaMessage tempo_message = new MetaMessage(0x51, tempo_data, tempo_data.length);
			
			return new MidiEvent[] {new MidiEvent(tempo_message, tick)};
		} catch (InvalidMidiDataException e) {
			return new MidiEvent[0];
		}
	}
}
