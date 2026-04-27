package options;

import infobox.InfoBox;

public class MIDIPlayerOptions {
	public static final byte CHORD_AS_IS = 0, CHORD_AS_FLAT = 1, CHORD_AS_SHARP = 2;

	public boolean play = false; //True if the sequence is playing.

	public byte song_melody_rh = 0, song_melody_lh = 1, instrument_melody_rh = 0, instrument_melody_lh = 0, export_melody_lh = 1, export_melody_rh = 0;

	public InfoBox info_display = null;

	public String active_section = "";
	public byte active_chord_root = -1, active_chord_type = 0;
	public long start_tick = 0, current_tick = 0;

	public byte[] black_chord_display = {CHORD_AS_IS, CHORD_AS_IS, CHORD_AS_IS, CHORD_AS_IS, CHORD_AS_IS};
}
