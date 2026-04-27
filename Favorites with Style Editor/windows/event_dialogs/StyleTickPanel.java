package event_dialogs;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import fwsevents.FWSSequence;
import main_window.FWSEditorMainWindow;

public class StyleTickPanel extends TickPanel {
	private static final long serialVersionUID = -7597168249236456186L;
	private long last_tick;

	public StyleTickPanel(FWSEditorMainWindow parent, final long initial_tick, final int x, final int y, final int w, final int h) {
		this(parent, parent.getSequence(), initial_tick, x, y, w, h);
	}

	public StyleTickPanel(FWSEditorMainWindow parent, FWSSequence parent_sequence, final long initial_tick, final int x, final int y, final int w, final int h) {
		super(parent, parent_sequence, initial_tick, x, y, w, h);
		last_tick = initial_tick >= 0 ? initial_tick : 0;

		label_tick.setText("<html>Style<br>Tick</html>");

		JCheckBox checkbox_current = new JCheckBox("Retain Tick");
		checkbox_current.setBounds(130, 60, 100, 35);
		checkbox_current.setSelected(true);
		checkbox_current.setToolTipText("Check to retain the style tick at the current position.");
		this.add(checkbox_current);

		checkbox_current.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(checkbox_current.isSelected()) {
					last_tick = current_tick;
					current_tick = -1;
				} else {
					current_tick = last_tick;
				}

				if(split_spinners)
					createSplitSpinners();
				else
					createSingleSpinner();
			}
		});

		if(this.current_tick < 0)
			checkbox_current.setSelected(true);
		else
			checkbox_current.setSelected(false);
	}

	/** Set the sequence. */
	public void setSequence(FWSSequence sequence) {
		final long length = sequence.getSequenceLengthStrict();

		if(this.current_tick > length)
			this.current_tick = this.current_tick%length;

		super.setSequence(sequence);
	}

	@Override
	protected void createSingleSpinner() {
		if(this.current_tick < 0) {
			Component components[] = getComponents();
			for(int i=0;i<components.length;i+=1) {
				if(components[i] == spinner_measure)
					this.remove(spinner_measure);
				if(components[i] == spinner_beat)
					this.remove(spinner_beat);
				if(components[i] == spinner_tick)
					this.remove(spinner_tick);
			}
			
			this.revalidate();
			this.repaint();
		} else {
			split_spinners = true;
			super.createSingleSpinner();
		}
	}

	@Override
	protected void createSplitSpinners() {
		if(this.current_tick < 0) {
			Component components[] = getComponents();
			for(int i=0;i<components.length;i+=1) {
				if(components[i] == spinner_measure)
					this.remove(spinner_measure);
				if(components[i] == spinner_beat)
					this.remove(spinner_beat);
				if(components[i] == spinner_tick)
					this.remove(spinner_tick);
			}
			this.revalidate();
			this.repaint();
		} else {
			split_spinners = false;
			super.createSplitSpinners();
		}
	}
}
