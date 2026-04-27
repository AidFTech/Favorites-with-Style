package main_window;

import javax.swing.JToggleButton;

public class NoteToggleButton extends JToggleButton {
	private static final long serialVersionUID = -5339614318736682905L;

	public enum NoteToggle {
		NOTE_TOGGLE_CTL,
		NOTE_TOGGLE_WHOLE,
		NOTE_TOGGLE_HALF,
		NOTE_TOGGLE_QUARTER,
		NOTE_TOGGLE_EIGHTH,
		NOTE_TOGGLE_SIXTEENTH,
		NOTE_TOGGLE_32,
		NOTE_TOGGLE_CUSTOM,
		NOTE_TOGGLE_TUPLET,
		NOTE_TOGGLE_DRAG
	}

	private NoteToggle toggle;
	
	protected NoteToggleButton(NoteToggle toggle) {
		super();
		this.toggle = toggle;
	}

	/** Get the defined toggle. */
	public NoteToggle getToggle() {
		return this.toggle;
	}
}
