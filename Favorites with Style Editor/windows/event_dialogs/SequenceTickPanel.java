package event_dialogs;

import fwsevents.FWSSequence;
import main_window.FWSEditorMainWindow;

public class SequenceTickPanel extends TickPanel {
	private static final long serialVersionUID = -3581697773557827245L;

	public SequenceTickPanel(FWSEditorMainWindow parent, final long initial_tick, final int x, final int y, final int w, final int h) {
		super(parent, initial_tick, x, y, w, h);
	}

	public SequenceTickPanel(FWSEditorMainWindow parent, FWSSequence parent_sequence, final long initial_tick, final int x, final int y, final int w, final int h) {
		super(parent, parent_sequence, initial_tick, x, y, w, h);
	}
}
