package settings_dialogs;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fwsevents.FWSSequence;
import fwsevents.FWSTimeSignatureEvent;
import main_window.FWSEditorMainWindow;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class SequencePropertiesWindow extends JDialog {
	private static final long serialVersionUID = 548478591040033438L;

	private boolean spinner_listen = true;

	public SequencePropertiesWindow(FWSEditorMainWindow parent, FWSSequence sequence) {
		super(parent, true);

		this.setTitle("Sequence Properties");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(392, 184));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);

		JLabel label_length = new JLabel("Sequence Length");
		label_length.setHorizontalAlignment(SwingConstants.RIGHT);
		label_length.setBounds(12, 12, 130, 35);
		getContentPane().add(label_length);
		
		JLabel label_measures = new JLabel("Measures");
		label_measures.setHorizontalAlignment(SwingConstants.CENTER);
		label_measures.setBounds(136, 47, 87, 29);
		getContentPane().add(label_measures);
		
		JSpinner spinner_measure = new JSpinner();
		spinner_measure.setToolTipText("Set the number of measures in the sequence.");
		spinner_measure.setModel(new SpinnerNumberModel(Integer.valueOf(sequence.getMeasureTicks().length), Integer.valueOf(0), null, Integer.valueOf(1)));
		spinner_measure.setBounds(160, 12, 62, 35);
		getContentPane().add(spinner_measure);
		
		JSpinner spinner_tick = new JSpinner();
		spinner_tick.setToolTipText("Set the number of ticks in the sequence.");
		spinner_tick.setModel(new SpinnerNumberModel(Long.valueOf(sequence.getSequenceLength()), Long.valueOf(0), null, Long.valueOf(1)));
		spinner_tick.setBounds(234, 12, 80, 35);
		getContentPane().add(spinner_tick);

		JLabel label_tpq = new JLabel("TPQ");
		label_tpq.setHorizontalAlignment(SwingConstants.RIGHT);
		label_tpq.setBounds(12, 90, 100, 35);
		getContentPane().add(label_tpq);
		
		JSpinner spinner_tpq = new JSpinner();
		spinner_tpq.setToolTipText("Set the sequence TPQ (ticks per quarter note).");
		spinner_tpq.setModel(new SpinnerNumberModel(sequence.getTPQ(), 64, 3840, 1));
		spinner_tpq.setBounds(130, 90, 87, 35);
		getContentPane().add(spinner_tpq);

		spinner_measure.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if(!spinner_listen)
					return;

				final int m = (Integer)spinner_measure.getValue(), tpq = sequence.getTPQ();
				long[] measures = sequence.getMeasureTicks();

				spinner_listen = false;
				if(m >= 0 && m < measures.length)
					spinner_tick.setValue(measures[m]);
				else if(measures.length > 0) {
					final long last_tick = measures[measures.length - 1];
					FWSTimeSignatureEvent time_sig = sequence.getTimeSignatureAt(last_tick);
					if(time_sig != null)
						spinner_tick.setValue((last_tick + (long)((m-(measures.length-1))*tpq*(time_sig.num/(Math.pow(2.0, time_sig.den)/4.0)))));
					else
						spinner_tick.setValue(last_tick + (long)((m-(measures.length-1))*tpq*4));
				} else {
					FWSTimeSignatureEvent time_sig = sequence.getTimeSignatureAt(0);
					if(time_sig != null)
						spinner_tick.setValue(((long)((m-(measures.length-1))*tpq*(time_sig.num/(Math.pow(2.0, time_sig.den)/4.0)))));
					else
						spinner_tick.setValue((long)((m-(measures.length-1))*tpq*4));
				}
				spinner_listen = true;
			}
		});

		spinner_tick.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if(!spinner_listen)
					return;

				spinner_listen = false;
				final long tick = (Long)spinner_tick.getValue(), old_length = sequence.getSequenceLength();
				final int tpq = sequence.getTPQ();
				
				if(tick < old_length) {
					final int m = sequence.getMeasureAt(tick);
					spinner_measure.setValue(Integer.valueOf(m));
				} else {
					final int measure_count = sequence.getMeasureTicks().length;
					FWSTimeSignatureEvent time_sig = sequence.getTimeSignatureAt(old_length);
					if(time_sig != null) {
						final int m = measure_count + (int)((tick - old_length)/tpq/(time_sig.num/(Math.pow(2.0, time_sig.den)/4.0)));
						spinner_measure.setValue(Integer.valueOf(m));
					} else {
						final int m = measure_count + (int)((tick - old_length)/(4.0));
						spinner_measure.setValue(Integer.valueOf(m));
					}
				}

				spinner_listen = true;
			}
		});

		SequencePropertiesWindow self = this;

		JButton button_cancel = new JButton("Cancel");
		button_cancel.setBounds(158, 137, 105, 35);
		button_cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_cancel);

		JButton button_apply = new JButton("Apply");
		button_apply.setBounds(275, 137, 105, 35);
		button_apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final int new_tpq = (Integer)spinner_tpq.getValue(), old_tpq = sequence.getTPQ();
				if(new_tpq < old_tpq) {
					final int answer = JOptionPane.showConfirmDialog(self, "The new TPQ value is lower than the old value. This will cause a loss of sequence resolution. All event ticks will be rounded. Proceed?", "TPQ", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if(answer == JOptionPane.NO_OPTION)
						return;
				}

				final long new_tick = (Long)spinner_tick.getValue(), old_tick = sequence.getSequenceLength();
				if(new_tick < old_tick) {
					final int answer = JOptionPane.showConfirmDialog(self, "The new sequence length is less than the old length. This will delete some events. Proceed?", "Song Length", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if(answer == JOptionPane.NO_OPTION)
						return;
				}

				sequence.setTPQ(new_tpq);
				sequence.setEndEvent(new_tick);
				parent.refreshViewport();

				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_apply);
		
		JLabel label_tick = new JLabel("Ticks");
		label_tick.setHorizontalAlignment(SwingConstants.CENTER);
		label_tick.setBounds(227, 47, 87, 29);
		getContentPane().add(label_tick);

		this.setVisible(true);
	}
}
