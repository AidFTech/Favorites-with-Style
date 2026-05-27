package fwsevents;

import java.util.ArrayList;

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
		ArrayList<Byte> data_vec = new ArrayList<>(0);
		for(int i=0;i<data.length;i+=1)
			data_vec.add(Byte.valueOf(data[i]));

		if(data_vec.size() > 0 && (data_vec.get(0)&0xFF) != 0xF0)
			data_vec.add(0, (byte)0xF0);
		else if(data_vec.size() <= 0)
			data_vec.add((byte)0xF0);

		if(data_vec.size() > 0 && (data_vec.get(data_vec.size()-1)&0xFF) == 0xF7) {

		} else
			data_vec.add((byte)0xF7);

		byte[] bytes = new byte[data_vec.size()];

		for(int i=0;i<bytes.length;i+=1)
			bytes[i] = data_vec.get(i);
		
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
