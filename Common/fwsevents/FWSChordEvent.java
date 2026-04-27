package fwsevents;

import style.ChordBody;

public class FWSChordEvent extends FWSEvent {
	public ChordBody main_chord = new ChordBody(), bass_chord = new ChordBody();
	public int inversion = 0;

	public FWSChordEvent() {

	}

	public FWSChordEvent(FWSChordEvent c) {
		super(c);
	}
}
