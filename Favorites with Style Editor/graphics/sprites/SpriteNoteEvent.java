package sprites;

import java.awt.Color;
import java.awt.Graphics;

import event_dialogs.NoteEventDialog;
import fwsevents.FWSNoteEvent;
import fwsevents.FWSSequence;

public class SpriteNoteEvent extends Sprite {
	private static final long serialVersionUID = -8720303813731603820L;

	private Color color;
	
	public SpriteNoteEvent(final int x, final int y, final int w, final int h, Color color, FWSNoteEvent parent) {
		super(x, y, w, h);
		this.affected_event = parent;
		this.color = color;

		locked_y = false;
	}

	public void paintComponent(Graphics g) {
		FWSNoteEvent affected_event = (FWSNoteEvent)this.affected_event;

		super.paintComponent(g);

		g.setColor(this.color);
		g.fillRect(0, 0, width-1, height-1);
		
		if(isselected == false)
			g.setColor(Color.BLACK);
		else
			g.setColor(Color.RED);
		g.drawRect(0, 0, width-1, height-1);

		FWSSequence active_sequence = getViewport().getActiveSequence();
		
		this.setToolTipText("<html>Channel " + (affected_event.channel+1) + ": " +
				"Note " + affected_event.note + " (" + active_sequence.getNoteNameAt(affected_event.note, affected_event.tick, true) + ")" + "<br/>"
				+ "Velocity: " + affected_event.velocity + "<br/>"
				+ "Duration: " + affected_event.duration
				+ "</html>");
	}

	/** Set the note color. */
	public void setColor(Color color) {
		this.color = color;
		this.repaint();
	}

	@Override
	public void createDialog() {
		new NoteEventDialog(getViewport().getMainWindow(), (FWSNoteEvent)affected_event);
	}
}
