package fwsevents;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;

public class FWSKeySignatureEvent extends FWSEvent {
	public byte accidental_count = 0;
	public boolean major = true;

	public FWSKeySignatureEvent() {

	}

	public FWSKeySignatureEvent(final FWSKeySignatureEvent c) {
		super(c);
		this.accidental_count = c.accidental_count;
		this.major = c.major;
	}

	@Override
	public String toString() {
		String desc = "Key Signature: ";

		if(major) {
			switch(accidental_count) {
			case 0:
				desc += "C";
				break;
			case 1:
				desc += "G";
				break;
			case 2:
				desc += "D";
				break;
			case 3:
				desc += "A";
				break;
			case 4:
				desc += "E";
				break;
			case 5:
				desc += "B";
				break;
			case 6:
				desc += "F♯";
				break;
			case 7:
				desc += "C♯";
				break;
			case -1:
				desc += "F";
				break;
			case -2:
				desc += "B♭";
				break;
			case -3:
				desc += "E♭";
				break;
			case -4:
				desc += "A♭";
				break;
			case -5:
				desc += "D♭";
				break;
			case -6:
				desc += "G♭";
				break;
			case -7:
				desc += "C♭";
				break;
			default:
				desc += "?";
			}
		} else {
			switch(accidental_count) {
			case 0:
				desc += "Am";
				break;
			case 1:
				desc += "Em";
				break;
			case 2:
				desc += "Bm";
				break;
			case 3:
				desc += "F♯m";
				break;
			case 4:
				desc += "C♯m";
				break;
			case 5:
				desc += "G♯m";
				break;
			case 6:
				desc += "D♯m";
				break;
			case 7:
				desc += "A♯m";
				break;
			case -1:
				desc += "Dm";
				break;
			case -2:
				desc += "Gm";
				break;
			case -3:
				desc += "Cm";
				break;
			case -4:
				desc += "Fm";
				break;
			case -5:
				desc += "B♭m";
				break;
			case -6:
				desc += "E♭m";
				break;
			case -7:
				desc += "A♭m";
				break;
			default:
				desc += "?m";
			}
		}

		return desc;
	}

	public byte[] getMIDIBytes() {
		return new byte[] {(byte)0xFF, 0x59, (byte)2, accidental_count, (byte)(major ? 0 : 1)};
	}

	/** Get the true MIDI key signature event. */
	public MidiEvent[] getMIDIEvents() {
		try {
			byte[] key_signature_data =  new byte[] {accidental_count, (byte)(major ? 0 : 1)};
			MetaMessage key_signature_message = new MetaMessage(0x59, key_signature_data, key_signature_data.length);

			return new MidiEvent[] {new MidiEvent(key_signature_message, tick)};
		} catch (InvalidMidiDataException e) {
			return new MidiEvent[0];
		}
	}

	/** Get the accidental count of a key. */
	public static byte getAccidentalCount(final byte key, final boolean major) {
		int accidentals = 7;

		byte effective_key = (byte)(key%12);
		if(!major)
			effective_key = (byte)((effective_key+3)%12);

		if(effective_key%2 == 0) {
			accidentals = (7 + effective_key)%12;
		} else {
			accidentals = (1 + effective_key)%12;
		}

		return (byte)(accidentals - 7);
	}

	/** Get the key fron an accidental count. */
	public static byte getKey(final byte accidental_count, final boolean major) {
		final int effective_accidentals = accidental_count + 7;
		byte key = 0;

		if(effective_accidentals%2 == 1) {
			key = (byte)((effective_accidentals+5)%12);
		} else {
			key = (byte)((effective_accidentals+11)%12);
		}

		if(!major)
			key = (byte)((key + 9)%12);

		return key;
	}
}
