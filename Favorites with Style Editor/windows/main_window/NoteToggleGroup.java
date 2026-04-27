package main_window;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;

public class NoteToggleGroup extends ButtonGroup {
	private static final long serialVersionUID = -9093417698535941789L;
	
	private ButtonModel previous;

	@Override
	public void setSelected(ButtonModel m, boolean b) {
		if(m.equals(previous))
			clearSelection();
		else 
			super.setSelected(m, b);
		previous = getSelection();
	}

	/** Get the enumerated toggle of the selected button. */
	public NoteToggleButton.NoteToggle getSelectedToggle() {
		for(int i=0;i<this.buttons.size();i+=1) {
			if(buttons.get(i) instanceof NoteToggleButton && buttons.get(i).isSelected()) {
				return ((NoteToggleButton)buttons.get(i)).getToggle();
			}
		}

		return null;
	}
}
