package event_dialogs;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import canvas.SongViewPort;
import fwsevents.FWSSectionNameEvent;
import fwsevents.FWSSequence;
import main_window.FWSEditorMainWindow;
import style.Style;

public class SectionNameEventDialog extends JDialog {
	private static final long serialVersionUID = -5480169553744246864L;

	public SectionNameEventDialog(FWSEditorMainWindow parent, FWSSectionNameEvent fws_event) {
		super(parent, true);
		
		this.setTitle("Text Event");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(385, 200));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);

		JLabel label_data = new JLabel("Section Name");
		label_data.setHorizontalAlignment(SwingConstants.RIGHT);
		label_data.setBounds(12, 12, 96, 35);
		getContentPane().add(label_data);

		JTextField field_data = new JTextField();
		field_data.setToolTipText("Enter the section name.");
		field_data.setBounds(126, 12, 226, 35);
		getContentPane().add(field_data);
		field_data.setColumns(10);

		field_data.setText(fws_event.section_name);

		JLabel label_measure = new JLabel("Measure");
		label_measure.setHorizontalAlignment(SwingConstants.RIGHT);
		label_measure.setBounds(12, 76, 110, 35);
		getContentPane().add(label_measure);

		long[] measures = parent.getSequence().getMeasureTicks();
		final int m = parent.getSequence().getMeasureAt(fws_event.tick);
		
		JSpinner spinner_measure = new JSpinner();
		spinner_measure.setBounds(140, 76, 81, 35);
		spinner_measure.setToolTipText("Set the measure at which the event occurs.");
		spinner_measure.setModel(new SpinnerNumberModel(m + 1, 1, measures.length, 1));
		getContentPane().add(spinner_measure);

		SectionNameEventDialog self = this;

		JButton button_cancel = new JButton("Cancel");
		button_cancel.setBounds(151, 153, 105, 35);
		button_cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_cancel);
		
		JButton button_apply = new JButton("Apply");
		button_apply.setBounds(268, 153, 105, 35);
		button_apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				final long new_tick = measures[(Integer)spinner_measure.getValue() - 1];

				SongViewPort vp = parent.getViewPort();
				FWSSequence sequence = vp.getActiveSequence();

				fws_event.tick = new_tick;
				fws_event.section_name = field_data.getText();
				if(sequence.getEvent(fws_event))
					sequence.refreshEvent(fws_event);
				else
					sequence.addEvent(fws_event);

				vp.refreshSprite(fws_event);
				vp.refresh();

				{
					Style test_style = new Style();
					test_style.getFromSequence(sequence);
					parent.refreshStyleDropdown(test_style);
				}
				
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_apply);

		this.setVisible(true);
	}
}
