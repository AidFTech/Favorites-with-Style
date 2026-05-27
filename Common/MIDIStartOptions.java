package options;

import java.util.ArrayList;

import voices.SequenceSubstitution;

public class MIDIStartOptions {
	public SequenceSubstitution[] substitutions = new SequenceSubstitution[0];

	//Messages calculated from instrument script.
	public byte[][] sysex_messages = new byte[0][];
	public long[] sysex_ticks = new long[0];

	//Messages to send only at the start of play, regardless of song position.
	public byte[][] start_messages = new byte[0][];

	/** Add "reset" messages to the start list. */
	public void addResetMessages() {
		ArrayList<byte[]> reset_message_vec = new ArrayList<>();

		for(int i=0;i<16;i+=1) {
			reset_message_vec.add(new byte[] {(byte)(0xB0 | (i&0xF)), 0x78, 0x0});
			reset_message_vec.add(new byte[] {(byte)(0xB0 | (i&0xF)), 0x79, 0x0});
		}

		addStartMessages(reset_message_vec);
	}

	/** Add messages to the start list. */
	public void addStartMessages(ArrayList<byte[]> messages) {
		byte[][] messages_ar = new byte[messages.size()][];
		messages.toArray(messages_ar);
		addStartMessages(messages_ar);
	}

	/** Add messages to the start list. */
	public void addStartMessages(byte[][] messages) {
		ArrayList<byte[]> current_start = new ArrayList<>();
		for(byte[] b: start_messages)
			current_start.add(b);

		for(byte[] b: messages)
			current_start.add(b);

		start_messages = new byte[current_start.size()][];
		for(int i=0;i<start_messages.length;i+=1)
			start_messages[i] = current_start.get(i);
	}

	/** Add messages to the sysex list. */
	public void addSysexMessages(byte[][] messages, long[] ticks) {
		ArrayList<byte[]> current_sysex = new ArrayList<>();
		for(byte[] b: sysex_messages)
			current_sysex.add(b);

		for(byte[] b: messages)
			current_sysex.add(b);

		sysex_messages = new byte[current_sysex.size()][];
		for(int i=0;i<sysex_messages.length;i+=1)
			sysex_messages[i] = current_sysex.get(i);
		
		ArrayList<Long> current_ticks = new ArrayList<>();
		for(long l: sysex_ticks)
			current_ticks.add(l);

		for(long l: ticks)
			current_ticks.add(l);

		sysex_ticks = new long[current_ticks.size()];
		for(int i=0;i<sysex_ticks.length;i+=1)
			sysex_ticks[i] = current_ticks.get(i);
	}
}
