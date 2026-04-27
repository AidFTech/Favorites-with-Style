package options;

public class RecordLoadOptions {
	public byte[] channel_map = new byte[16];

	public boolean live_import = true; //True if the imported events were from a live recording.
	public boolean midi_clock = true; //True if the standard MIDI clock of 24 pulses per quarter note should be used.
	public boolean import_notes = true, import_voice = true, import_short = true, import_tempo = true, import_time = true, import_key = true, import_sysex = true, import_other = true;

	public long timeshift = 0;
	public boolean extend = true, start_notes_at_zero = true, add_shorts_to_beginning = true;
	
	public String section = "";
}
