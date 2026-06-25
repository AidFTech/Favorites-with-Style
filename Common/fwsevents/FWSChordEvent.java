package fwsevents;

import style.ChordBody;

public class FWSChordEvent extends FWSEvent {
	public ChordBody main_chord = new ChordBody(), bass_chord = new ChordBody();
	public int inversion = 0;

	public FWSChordEvent() {

	}

	public FWSChordEvent(FWSChordEvent c) {
		super(c);
		main_chord = new ChordBody(c.main_chord);
		bass_chord = new ChordBody(c.bass_chord);
		inversion = c.inversion;
	}

	@Override
	public String toString() {
		return "Chord: " + main_chord.getName() + (!bass_chord.getNoChord() ? ("/" + bass_chord.getName()) : "");
	}
}
