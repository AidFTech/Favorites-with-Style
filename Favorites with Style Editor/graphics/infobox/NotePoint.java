package infobox;

import java.awt.Point;

public class NotePoint extends Point {
	private static final long serialVersionUID = -5759698777901739461L;
	
	private boolean black;
	
	public NotePoint(int x, int y, boolean black) {
		super(x,y);
		this.black = black;
	}
	
	/** Return whether the note is a black key. */
	public boolean getBlack() {
		return this.black;
	}

}
