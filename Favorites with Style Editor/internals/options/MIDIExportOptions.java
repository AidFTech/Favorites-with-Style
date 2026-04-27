package options;

public class MIDIExportOptions {
	public byte export_melody_lh = 1, export_melody_rh = 0;
	public boolean invert = true;

	public boolean truncate = true; //Truncate melody notes.

	public byte[] black_chord_display = {MIDIPlayerOptions.CHORD_AS_IS, MIDIPlayerOptions.CHORD_AS_IS, MIDIPlayerOptions.CHORD_AS_IS, MIDIPlayerOptions.CHORD_AS_IS, MIDIPlayerOptions.CHORD_AS_IS};
}
