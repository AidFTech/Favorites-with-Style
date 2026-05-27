package event_dialogs;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import canvas.SongViewPort;
import fwsevents.FWSNoteEvent;
import fwsevents.FWSSequence;
import main_window.FWSEditorMainWindow;

public class MultiNoteEventDialog extends JDialog {
	private static final long serialVersionUID = -3320470611362922705L;

	private JSpinner spinner_channel, spinner_note, spinner_transpose, spinner_velocity, spinner_velocity_percentage, spinner_duration, spinner_duration_percentage;

	public MultiNoteEventDialog(FWSEditorMainWindow parent, FWSNoteEvent[] fws_events) {
		super(parent, true);

		this.setTitle("Note Events");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(385, 440));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);

		if(fws_events.length <= 0)
			return;

		byte channel = fws_events[0].channel;
		final byte set_channel = channel;
		for(int i=0;i<fws_events.length;i+=1) {
			if(fws_events[i].channel != channel) {
				channel = -1; //Keep channel option.
				break;
			}
		}

		byte note = fws_events[0].note;
		final byte set_note = note;
		for(int i=0;i<fws_events.length;i+=1) {
			if(fws_events[i].note != note) {
				note = -1;
				break;
			}
		}

		byte velocity = fws_events[0].velocity;
		final byte set_velocity = velocity;
		for(int i=0;i<fws_events.length;i+=1) {
			if(fws_events[i].velocity != velocity) {
				velocity = -1;
				break;
			}
		}

		long duration = fws_events[0].duration;
		final long set_duration = duration;
		for(int i=0;i<fws_events.length;i+=1) {
			if(fws_events[i].duration != duration) {
				duration = -1;
				break;
			}
		}

		long tick = fws_events[0].tick;
		final long set_tick = tick;
		for(int i=0;i<fws_events.length;i+=1) {
			if(fws_events[i].tick != tick) {
				tick = -1;
				break;
			}
		}

		JLabel label_channel = new JLabel("Channel");
		label_channel.setHorizontalAlignment(SwingConstants.RIGHT);
		label_channel.setBounds(12, 12, 60, 35);
		getContentPane().add(label_channel);
		
		spinner_channel = new JSpinner();
		spinner_channel.setBounds(89, 12, 73, 35);
		spinner_channel.setToolTipText("Select the note channel.");
		setChannelSpinner(getContentPane(), spinner_channel, channel);
		getContentPane().add(spinner_channel);
		
		JCheckBox checkbox_keep_channel = new JCheckBox("Keep Channel");
		checkbox_keep_channel.setToolTipText("Check to keep the channel of the selected notes.");
		checkbox_keep_channel.setBounds(170, 12, 157, 35);
		getContentPane().add(checkbox_keep_channel);

		checkbox_keep_channel.setSelected(channel < 0);
		checkbox_keep_channel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(checkbox_keep_channel.isSelected())
					setChannelSpinner(getContentPane(), spinner_channel, (byte)-1);
				else
					setChannelSpinner(getContentPane(), spinner_channel, set_channel);
			}
		});
		
		JLabel label_note = new JLabel("Note");
		label_note.setHorizontalAlignment(SwingConstants.RIGHT);
		label_note.setBounds(12, 59, 60, 35);
		getContentPane().add(label_note);
		
		spinner_note = new JSpinner();
		spinner_note.setToolTipText("Set the notes.");
		spinner_note.setBounds(89, 59, 73, 35);
		getContentPane().add(spinner_note);

		spinner_transpose = new JSpinner();
		spinner_transpose.setToolTipText("Transpose the note selection.");
		spinner_transpose.setBounds(89, 59, 73, 35);
		getContentPane().add(spinner_transpose);

		setNoteSpinner(getContentPane(), spinner_note, spinner_transpose, label_note, note);

		JCheckBox checkbox_transpose = new JCheckBox("Transpose");
		checkbox_transpose.setToolTipText("Check to transpose the note selection relative to the original notes. Uncheck to set all notes to the same tone.");
		checkbox_transpose.setBounds(170, 59, 157, 35);
		getContentPane().add(checkbox_transpose);

		checkbox_transpose.setSelected(note < 0);
		
		SongViewPort vp = parent.getViewPort();
		FWSSequence sequence = vp.getActiveSequence();

		JLabel label_note_name = new JLabel("");
		label_note_name.setHorizontalAlignment(SwingConstants.CENTER);
		label_note_name.setFont(new Font("default",Font.PLAIN,26));
		label_note_name.setBounds(22, 106, 305, 35);

		{
			byte[] note_range = getNoteRange(fws_events, 0);
			if(note_range.length == 2)
				setNoteNameDisplay(label_note_name, sequence, note_range[0], note_range[1], set_tick);
		}

		getContentPane().add(label_note_name);
		
		spinner_note.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setNoteNameDisplay(label_note_name, sequence, ((Integer)spinner_note.getValue()).byteValue(), set_tick);
			}
		});

		spinner_transpose.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				final int shift = (Integer)spinner_transpose.getValue();
				byte[] note_range = getNoteRange(fws_events, shift);

				if(note_range.length == 2)
					setNoteNameDisplay(label_note_name, sequence, note_range[0], note_range[1], set_tick);
			}
		});

		checkbox_transpose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(checkbox_transpose.isSelected()) {
					setNoteSpinner(getContentPane(), spinner_note, spinner_transpose, label_note, (byte)-1);
					final int shift = (Integer)spinner_transpose.getValue();
					byte[] note_range = getNoteRange(fws_events, shift);

					if(note_range.length == 2)
						setNoteNameDisplay(label_note_name, sequence, note_range[0], note_range[1], set_tick);
				} else {
					setNoteSpinner(getContentPane(), spinner_note, spinner_transpose, label_note, set_note);
					setNoteNameDisplay(label_note_name, sequence, set_note, set_tick);
				}
			}
		});
		
		JLabel label_velocity = new JLabel("Velocity");
		label_velocity.setHorizontalAlignment(SwingConstants.RIGHT);
		label_velocity.setBounds(12, 153, 60, 35);
		getContentPane().add(label_velocity);
		
		spinner_velocity = new JSpinner();
		spinner_velocity.setToolTipText("Set the note velocity (volume).");
		spinner_velocity.setBounds(89, 153, 73, 35);
		getContentPane().add(spinner_velocity);

		spinner_velocity_percentage = new JSpinner();
		spinner_velocity_percentage.setToolTipText("Set the note velocity percentage.");
		spinner_velocity_percentage.setBounds(89, 153, 73, 35);
		getContentPane().add(spinner_velocity_percentage);

		setVelocitySpinner(getContentPane(), spinner_velocity, spinner_velocity_percentage, velocity);

		JCheckBox checkbox_velocity_percentage = new JCheckBox("Percentage");
		checkbox_velocity_percentage.setToolTipText("Check to adjust the note velocity relative to the original notes. Uncheck to set all notes to the same velocity.");
		checkbox_velocity_percentage.setBounds(170, 153, 157, 35);
		getContentPane().add(checkbox_velocity_percentage);

		checkbox_velocity_percentage.setSelected(velocity < 0);
		checkbox_velocity_percentage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(checkbox_velocity_percentage.isSelected())
					setVelocitySpinner(getContentPane(), spinner_velocity, spinner_velocity_percentage, (byte)-1);
				else
					setVelocitySpinner(getContentPane(), spinner_velocity, spinner_velocity_percentage, set_velocity);
			}
		});
		
		JLabel label_duration = new JLabel("Duration");
		label_duration.setHorizontalAlignment(SwingConstants.RIGHT);
		label_duration.setBounds(12, 200, 60, 35);
		getContentPane().add(label_duration);
		
		spinner_duration = new JSpinner();
		spinner_duration.setToolTipText("Set the note duration in ticks.");
		spinner_duration.setBounds(89, 200, 73, 35);
		getContentPane().add(spinner_duration);
		
		spinner_duration_percentage = new JSpinner();
		spinner_duration_percentage.setToolTipText("Set the note duration in ticks.");
		spinner_duration_percentage.setBounds(89, 200, 73, 35);
		getContentPane().add(spinner_duration_percentage);

		setDurationSpinner(getContentPane(), spinner_duration, spinner_duration_percentage, duration);

		JCheckBox checkbox_duration_percentage = new JCheckBox("Percentage");
		checkbox_duration_percentage.setToolTipText("Check to adjust the note duration relative to the original notes. Uncheck to set all notes to the same velocity.");
		checkbox_duration_percentage.setBounds(170, 200, 157, 35);
		getContentPane().add(checkbox_duration_percentage);

		checkbox_duration_percentage.setSelected(duration < 0);
		checkbox_duration_percentage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(checkbox_duration_percentage.isSelected())
					setDurationSpinner(getContentPane(), spinner_duration, spinner_duration_percentage, -1);
				else
					setDurationSpinner(getContentPane(), spinner_duration, spinner_duration_percentage, set_duration);
			}
		});

		MultiTickPanel tick_panel = new MultiTickPanel(parent, tick >= 0 ? tick : set_tick, 12, 280, 326, 100, tick < 0);
		tick_panel.setBounds(12, 280, 326, 100);
		getContentPane().add(tick_panel);

		MultiNoteEventDialog self = this;

		JButton button_cancel = new JButton("Cancel");
		button_cancel.setBounds(151, 393, 105, 35);
		button_cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_cancel);

		JButton button_apply = new JButton("Apply");
		button_apply.setBounds(268, 393, 105, 35);
		button_apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SongViewPort vp = parent.getViewPort();
				FWSSequence sequence = vp.getActiveSequence();

				for(FWSNoteEvent fws_event: fws_events) {
					if(!checkbox_keep_channel.isSelected())
						fws_event.channel = (byte)((Integer)spinner_channel.getValue() - 1);

					if(checkbox_transpose.isSelected()) {
						final int new_note = fws_event.note + (Integer)spinner_transpose.getValue();
						if(new_note < 0)
							fws_event.note = 0;
						else if(new_note > 127)
							fws_event.note = 127;
						else
							fws_event.note = (byte)new_note;
					} else
						fws_event.note = ((Integer)spinner_note.getValue()).byteValue();

					if(checkbox_velocity_percentage.isSelected()) {
						final double adj = (Integer)spinner_velocity_percentage.getValue()/100.0;
						fws_event.velocity = (byte)(fws_event.velocity*adj);
					} else
						fws_event.velocity = ((Integer)spinner_velocity.getValue()).byteValue();

					if(checkbox_duration_percentage.isSelected()) {
						final double adj = (Integer)spinner_duration_percentage.getValue()/100.0;
						fws_event.duration = (long)(fws_event.duration*adj);
					} else
						fws_event.duration = ((Long)spinner_duration.getValue());

					if(tick_panel.relative) {
						long new_tick = fws_event.tick + tick_panel.getSetTick();
						if(new_tick < 0)
							new_tick = 0;
						if(new_tick >= sequence.getSequenceLength())
							new_tick = sequence.getSequenceLength();

						fws_event.tick = new_tick;
					} else {
						fws_event.tick = tick_panel.getSetTick();
					}

					sequence.refreshEvent(fws_event);
					vp.refreshSprite(fws_event);
				}
				vp.refresh();
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_apply);
		
		this.setVisible(true);
	}

	/** Initialize the channel spinner. */
	private static void setChannelSpinner(Container parent, JSpinner spinner, final byte channel) {
		if(channel >= 0 && channel < 16) {
			spinner.setModel(new SpinnerNumberModel(channel + 1, 1, 16, 1));
			spinner.setVisible(true);
		} else {
			spinner.setVisible(false);
		}
	}

	/** Initialize the note/transpose spinner. */
	private static void setNoteSpinner(Container parent, JSpinner note_spinner, JSpinner transpose_spinner, JLabel label, final byte note) {
		if(note >= 0) {
			note_spinner.setModel(new SpinnerNumberModel(note, 0, 127, 1));
			transpose_spinner.setVisible(false);
			note_spinner.setVisible(true);
			label.setText("Note");
			parent.repaint();
		} else {
			transpose_spinner.setModel(new SpinnerNumberModel(0, null, null, 1));
			note_spinner.setVisible(false);
			transpose_spinner.setVisible(true);
			label.setText("Adj.");
			parent.repaint();
		}
	}

	/** Initialize the velocity/percentage spinner. */
	private static void setVelocitySpinner(Container parent, JSpinner velocity_spinner, JSpinner percent_spinner, final byte velocity) {
		if(velocity >= 0) {
			velocity_spinner.setModel(new SpinnerNumberModel(velocity, 0, 127, 1));
			percent_spinner.setVisible(false);
			velocity_spinner.setVisible(true);
			parent.repaint();
		} else {
			percent_spinner.setModel(new SpinnerNumberModel(100, 0, null, 1));
			velocity_spinner.setVisible(false);
			percent_spinner.setVisible(true);
			parent.repaint();
		}
	}

	/** Initialize the duration/percentage spinner. */
	private static void setDurationSpinner(Container parent, JSpinner duration_spinner, JSpinner percent_spinner, final long duration) {
		if(duration >= 0) {
			duration_spinner.setModel(new SpinnerNumberModel(duration, Long.valueOf(0), null, Long.valueOf(1)));
			percent_spinner.setVisible(false);
			duration_spinner.setVisible(true);
			parent.repaint();
		} else {
			percent_spinner.setModel(new SpinnerNumberModel(100, 0, null, 1));
			duration_spinner.setVisible(false);
			percent_spinner.setVisible(true);
			parent.repaint();
		}
	}

	/** Get the range of selected notes. */
	private static byte[] getNoteRange(FWSNoteEvent[] notes, final int shift) {
		if(notes.length <= 0)
			return new byte[] {-1, -1};

		byte min_note = notes[0].note, max_note = notes[0].note;
		for(FWSNoteEvent n: notes) {
			if(n.note < min_note)
				min_note = n.note;
			if(n.note > max_note)
				max_note = n.note;
		}

		if(min_note + shift < 0)
			min_note = 0;
		else if(min_note + shift > 127)
			min_note = 127;
		else
			min_note += shift;

		if(max_note + shift < 0)
			max_note = 0;
		else if(max_note + shift > 127)
			max_note = 127;
		else
			max_note += shift;

		return new byte[] {min_note, max_note};
	}

	/** Set the note name display. */
	private static void setNoteNameDisplay(JLabel note_label, FWSSequence sequence, final byte note, final long tick) {
		setNoteNameDisplay(note_label, sequence, note, note, tick);
	}

	/** Set the note name display. */
	private static void setNoteNameDisplay(JLabel note_label, FWSSequence sequence, final byte min_note, final byte max_note, final long tick) {
		if(min_note == max_note)
			note_label.setText(sequence.getNoteNameAt(min_note, tick, true));
		else { //Display a range.
			final String min_note_str = sequence.getNoteNameAt(min_note, tick, true), max_note_str = sequence.getNoteNameAt(max_note, tick, true);
			note_label.setText(min_note_str + " - " + max_note_str);
		}
	}
}
