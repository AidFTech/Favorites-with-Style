package event_dialogs;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import fwsevents.FWSSequence;
import fwsevents.FWSTimeSignatureEvent;
import main_window.FWSEditorMainWindow;
import javax.swing.SwingConstants;

import canvas.SongViewPort;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JComboBox;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;

public class TimeEventDialog extends JDialog {
	private static final long serialVersionUID = 6629254055928628791L;

	public TimeEventDialog(FWSEditorMainWindow parent, FWSTimeSignatureEvent fws_event) {
		super(parent, true);

		this.setTitle("Time Signature Event");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(365, 220));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);

		JLabel label_time_signature = new JLabel("Time Signature");
		label_time_signature.setHorizontalAlignment(SwingConstants.RIGHT);
		label_time_signature.setBounds(12, 12, 110, 35);
		getContentPane().add(label_time_signature);
		
		JSpinner spinner_num = new JSpinner();
		spinner_num.setModel(new SpinnerNumberModel(fws_event.num, 1, 60, 1));
		spinner_num.setBounds(140, 12, 61, 35);
		spinner_num.setToolTipText("Set the event time signature upper number.");
		getContentPane().add(spinner_num);
		
		JComboBox<Integer> combobox_den = new JComboBox<Integer>();
		combobox_den.setModel(new DefaultComboBoxModel<Integer>(new Integer[] {1, 2, 4, 8, 16, 32, 64}));
		combobox_den.setSelectedIndex(fws_event.den);
		combobox_den.setBounds(213, 12, 61, 35);
		combobox_den.setToolTipText("Set the event time signature lower number.");
		getContentPane().add(combobox_den);

		JLabel label_measure = new JLabel("Measure");
		label_measure.setHorizontalAlignment(SwingConstants.RIGHT);
		label_measure.setBounds(12, 76, 110, 35);
		if(fws_event.tick != 0)
			getContentPane().add(label_measure);
		
		long[] measures = parent.getSequence().getMeasureTicks();
		final int m = parent.getSequence().getMeasureAt(fws_event.tick);
		
		JSpinner spinner_measure = new JSpinner();
		spinner_measure.setBounds(140, 76, 81, 35);
		spinner_measure.setToolTipText("Set the measure at which the event occurs.");
		spinner_measure.setModel(new SpinnerNumberModel(m + 1, 1, measures.length, 1));
		if(fws_event.tick != 0)
			getContentPane().add(spinner_measure);
		
		JCheckBox checkbox_retain_existing = new JCheckBox("Retain Existing Time Signature Measures");
		checkbox_retain_existing.setToolTipText("For time signatures in the sequence, retain the existing measure. Uncheck to round to the nearest measure.");
		checkbox_retain_existing.setBounds(22, 119, 302, 25);
		getContentPane().add(checkbox_retain_existing);

		TimeEventDialog self = this;

		JButton button_cancel = new JButton("Cancel");
		button_cancel.setBounds(131, 173, 105, 35);
		button_cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_cancel);
		
		JButton button_apply = new JButton("Apply");
		button_apply.setBounds(248, 173, 105, 35);
		button_apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				final int new_num = (Integer)spinner_num.getValue(), new_den = combobox_den.getSelectedIndex();
				final long new_tick = measures[(Integer)spinner_measure.getValue() - 1];

				FWSTimeSignatureEvent new_event = new FWSTimeSignatureEvent(fws_event);

				new_event.num = (byte)new_num;
				new_event.den = (byte)new_den;
				new_event.tick = new_tick;

				SongViewPort vp = parent.getViewPort();
				FWSSequence sequence = vp.getActiveSequence();

				if(new_tick == 0 && fws_event.tick != 0) {
					final int answer = JOptionPane.showConfirmDialog(self, "Replace the initial time signature event?", "Time Signature", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if(answer == JOptionPane.NO_OPTION)
						return;
				}

				sequence.refreshTimeSignatures(new_event, fws_event, checkbox_retain_existing.isSelected());

				vp.removeSprite(vp.getEventSprite(fws_event));
				vp.addSprite(new_event);
				vp.refreshFull();
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_apply);

		this.setVisible(true);
	}
}