package event_dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import main_window.FWSEditorMainWindow;

public class MultiTickPanel extends TickPanel {
	private static final long serialVersionUID = -6901475542285934321L;

	public MultiTickPanel(FWSEditorMainWindow parent, final long initial_tick, final int x, final int y, final int w, final int h, final boolean relative) {
		super(parent, initial_tick, x, y, w, h, relative);

		JCheckBox checkbox_relative = new JCheckBox("Relative");
		checkbox_relative.setBounds(130, 60, 100, 35);
		checkbox_relative.setToolTipText("Check shift the tick by the specified amount. Uncheck to specify the exact tick.");
		this.add(checkbox_relative);

		MultiTickPanel self = this;

		checkbox_relative.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				self.relative = checkbox_relative.isSelected();

				if(split_spinners)
					createSplitSpinners();
				else
					createSingleSpinner();
			}
		});

		checkbox_relative.setSelected(relative);
	}
}
