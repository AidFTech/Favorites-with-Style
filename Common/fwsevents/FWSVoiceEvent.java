package fwsevents;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;

public class FWSVoiceEvent extends FWSEvent {
	public byte voice, voice_lsb, voice_msb, channel;

	public FWSVoiceEvent() {

	}

	public FWSVoiceEvent(final FWSVoiceEvent c) {
		super(c);
		this.voice = c.voice;
		this.voice_lsb = c.voice_lsb;
		this.voice_msb = c.voice_msb;
		this.channel = c.channel;
	}

	@Override
	public String toString() {
		String desc = "Channel " + (channel + 1) + " Voice: ";
		desc += "Program " + voice + ", LSB " + voice_lsb + ", MSB " + voice_msb;
		return desc;
	}

	public byte[] getMIDIBytes() {
		return new byte[] {(byte)(0xC0 | channel), voice, 0};
	}

	/** Get the true MIDI command from this event. */
	public MidiEvent[] getMIDIEvents() {
		try {
			ShortMessage voice_message = new ShortMessage(ShortMessage.PROGRAM_CHANGE, channel&0xFF, voice&0xFF, 0);
			ShortMessage lsb_message = new ShortMessage(ShortMessage.CONTROL_CHANGE, channel&0xFF, 32, voice_lsb&0xFF);
			ShortMessage msb_message = new ShortMessage(ShortMessage.CONTROL_CHANGE, channel&0xFF, 0, voice_msb&0xFF);

			return new MidiEvent[] {new MidiEvent(msb_message, tick), new MidiEvent(lsb_message, tick), new MidiEvent(voice_message, tick)};
		} catch (InvalidMidiDataException e) {
			return new MidiEvent[0];
		}
	}

	/** Compare voice events. May still return true if the tick is different. */
	public boolean equals(FWSVoiceEvent c) {
		return this.voice == c.voice &&
				this.voice_lsb == c.voice_lsb &&
				this.voice_msb == c.voice_msb;
	}
}
