package midicontrol;

public enum MIDIControl {
	VOICE(0, "Voice"),
	MODULATION_WHEEL(1, "Modulation Wheel"),
	BREATH_CONTROL(2, "Breath Control"),
	FOOT_CONTROL(4, "Foot Control"),
	PORTAMENTO_TIME(5, "Portamento"),
	DATA_ENTRY(6, "Data Entry"),
	CHANNEL_VOLUME(7, "Channel Volume"),
	BALANCE(8, "Balance"),
	PAN(0xA, "Pan"),
	EXPRESSION(0xB, "Expression Controller"),
	EFFECT_1(0xC, "Effect Controller 1"),
	EFFECT_2(0xD, "Effect Controller 2"),
	GP_1(0x10, "General Purpose Controller 1"),
	GP_2(0x11, "General Purpose Controller 2"),
	GP_3(0x12, "General Purpose Controller 3"),
	GP_4(0x13, "General Purpose Controller 4"),
	SUSTAIN_PEDAL(0x40, "Sustain Pedal On/Off"),
	PORTAMENTO(0x41, "Portamento On/Off"),
	SOSTENUTO(0x42, "Sostenuto On/Off"),
	SOFT(0x43, "Soft Pedal On/Off"),
	LEGATO(0x44, "Legato Footswitch"),
	HOLD_2(0x45, "Hold 2"),
	SOUND_VARIATION(0x46, "Sound Variation"),
	TIMBRE(0x47, "Harmonic Intensity"),
	RELEASE_TIME(0x48, "Release Time"),
	ATTACK_TIME(0x49, "Attack Time"),
	BRIGHTNESS(0x4A, "Brightness"),
	DECAY_TIME(0x4B, "Decay Time"),
	VIBRATO_RATE(0x4C, "Vibrato Rate"),
	VIBRATO_DEPTH(0x4D, "Vibrato Depth"),
	VIBRATO_DELAY(0x4E, "Vibrato Delay"),
	GP_5(0x50, "General Purpose Controller 5"),
	GP_6(0x51, "General Purpose Controller 6"),
	GP_7(0x52, "General Purpose Controller 7"),
	GP_8(0x53, "General Purpose Controller 8"),
	PORTAMENTO_CONTROL(0x54, "Portamento Control"),
	HIGH_RESOLUTION_VELOCITY(0x58, "High Resolution Velocity Prefix"),

	ALL_SOUND_OFF(0x78, "All Sound Off"),
	RESET_CONTROLLERS(0x79, "Reset All Controllers"),
	LOCAL_CONTROL(0x7A, "Local Control"),
	ALL_NOTES_OFF(0x7B, "All Notes Off"),
	OMNI_OFF(0x7C, "Omni Mode Off"),
	OMNI_ON(0x7D, "Omni Mode On"),
	MONO_ON(0x7E, "Mono Mode On"),
	POLY_ON(0x7F, "Poly Mode On");
	
	private int value;
	private String name;

	private MIDIControl(final int value, String name) {
		this.value = value;
		this.name = name;
	}

	private MIDIControl(MIDIControl c) {
		this.value = c.value;
		this.name = c.name;
	}

	/** Get the value. */
	public int getValue() {
		return this.value;
	}

	/** Get the LSB value. */
	public int getLSBValue() {
		if(value < 0x20)
			return value + 0x20;
		else
			return value;
	}

	/** Get the name. */
	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return this.name;
	}

	/** Get a MIDI control from the provided value. */
	public static MIDIControl getControl(final int value) {
		for(MIDIControl c : MIDIControl.values()) {
			if(c.value == value)
				return c;
			else if(value >= 0x20 && value < 0x40 && c.value == value - 0x20)
				return c;
		}

		return null;
	}
}
