package song;

import fwsevents.FWSNoteEvent;

public class FWSSongMetadata {
	public String long_title = "", short_title = "", composer = "";

	public byte split_point = FWSNoteEvent.casio_split, chord_channel = 1;
	public byte melody_rh_channel = 0, melody_lh_channel = 1;
}