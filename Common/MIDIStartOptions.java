package options;

import voices.SequenceSubstitution;

public class MIDIStartOptions {
	public SequenceSubstitution[] substitutions = new SequenceSubstitution[0];

	//Messages calculated from instrument script.
	public byte[][] sysex_messages = new byte[0][];
	public long[] sysex_ticks = new long[0];
}
