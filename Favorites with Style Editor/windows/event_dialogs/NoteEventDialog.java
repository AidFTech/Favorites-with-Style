package event_dialogs;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import canvas.SongViewPort;
import fwsevents.FWSNoteEvent;
import fwsevents.FWSSequence;
import main_window.FWSEditorMainWindow;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;

public class NoteEventDialog extends JDialog {
	private static final long serialVersionUID = -4341561949723885843L;
	
	private boolean set_dropdown = true;

	public NoteEventDialog(FWSEditorMainWindow parent, FWSNoteEvent fws_event) {
		super(parent, true);

		this.setTitle("Note Event");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(385, 440));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);

		JLabel label_channel = new JLabel("Channel");
		label_channel.setHorizontalAlignment(SwingConstants.RIGHT);
		label_channel.setBounds(12, 12, 60, 35);
		getContentPane().add(label_channel);
		
		JSpinner spinner_channel = new JSpinner();
		spinner_channel.setModel(new SpinnerNumberModel(fws_event.channel + 1, 1, 16, 1));
		spinner_channel.setBounds(89, 12, 73, 35);
		spinner_channel.setToolTipText("Select the note channel.");
		getContentPane().add(spinner_channel);
		
		JLabel label_note = new JLabel("Note");
		label_note.setHorizontalAlignment(SwingConstants.RIGHT);
		label_note.setBounds(12, 59, 60, 35);
		getContentPane().add(label_note);
		
		JSpinner spinner_note = new JSpinner();
		spinner_note.setModel(new SpinnerNumberModel(fws_event.note, 0, 127, 1));
		spinner_note.setToolTipText("Set the note.");
		spinner_note.setBounds(89, 59, 73, 35);
		getContentPane().add(spinner_note);
		
		SongViewPort vp = parent.getViewPort();
		FWSSequence sequence = vp.getActiveSequence();

		JLabel label_note_name = new JLabel(sequence.getNoteNameAt(fws_event.note, fws_event.tick, true));
		label_note_name.setHorizontalAlignment(SwingConstants.CENTER);
		label_note_name.setFont(new Font("default",Font.PLAIN,26));
		label_note_name.setBounds(102, 106, 60, 35);
		getContentPane().add(label_note_name);
		
		spinner_note.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				label_note_name.setText(sequence.getNoteNameAt(((Integer)spinner_note.getValue()).byteValue(), fws_event.tick, true));
			}
		});
		
		JLabel label_velocity = new JLabel("Velocity");
		label_velocity.setHorizontalAlignment(SwingConstants.RIGHT);
		label_velocity.setBounds(12, 153, 60, 35);
		getContentPane().add(label_velocity);
		
		JSpinner spinner_velocity = new JSpinner();
		spinner_velocity.setModel(new SpinnerNumberModel(fws_event.velocity, 0, 127, 1));
		spinner_velocity.setToolTipText("Set the note velocity (volume).");
		spinner_velocity.setBounds(89, 153, 73, 35);
		getContentPane().add(spinner_velocity);
		
		JLabel label_duration = new JLabel("Duration");
		label_duration.setHorizontalAlignment(SwingConstants.RIGHT);
		label_duration.setBounds(12, 200, 60, 35);
		getContentPane().add(label_duration);
		
		JSpinner spinner_duration = new JSpinner();
		spinner_duration.setModel(new SpinnerNumberModel(fws_event.duration, Long.valueOf(0), null, Long.valueOf(1)));
		spinner_duration.setToolTipText("Set the note duration in ticks.");
		spinner_duration.setBounds(89, 200, 73, 35);
		getContentPane().add(spinner_duration);
		
		JComboBox<String> dropdown_duration = new JComboBox<String>();
		dropdown_duration.setToolTipText("Set a fixed note duration.");
		dropdown_duration.setModel(new DefaultComboBoxModel<String>(new String[] {"1/64th", "1/32nd", "1/16th", "1/8th", "Quarter", "Half", "Whole"}));
		dropdown_duration.setBounds(174, 200, 172, 35);
		dropdown_duration.setSelectedIndex(-1);
		getContentPane().add(dropdown_duration);
		
		JCheckBox checkbox_dot = new JCheckBox("Dot");
		checkbox_dot.setToolTipText("Check to add a dot to the fixed duration.");
		checkbox_dot.setEnabled(false);
		checkbox_dot.setBounds(174, 243, 114, 35);
		getContentPane().add(checkbox_dot);

		dropdown_duration.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(dropdown_duration.getSelectedIndex() < 0)
					return;
				
				set_dropdown = false;
				final int tpq = sequence.getTPQ(), power = dropdown_duration.getSelectedIndex() - 4;
				final long duration = (long) (tpq*Math.pow(2.0, power));
				spinner_duration.setValue(Long.valueOf(duration));
				checkbox_dot.setEnabled(true);
				checkbox_dot.setSelected(false);
				set_dropdown = true;
			}
		});
		
		checkbox_dot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final long current_duration = (Long)spinner_duration.getValue();
				set_dropdown = false;
				if(checkbox_dot.isSelected())
					spinner_duration.setValue(Long.valueOf(current_duration*3/2));
				else
					spinner_duration.setValue(Long.valueOf(current_duration*2/3));
				set_dropdown = true;
			}
		});
		
		spinner_duration.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(set_dropdown) {
					checkbox_dot.setEnabled(false);
					dropdown_duration.setSelectedIndex(-1);
				}
			}
		});

		SequenceTickPanel tick_panel = new SequenceTickPanel(parent, fws_event.tick, 12, 280, 326, 100);
		tick_panel.setBounds(12, 280, 326, 100);
		getContentPane().add(tick_panel);

		NoteEventDialog self = this;

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
				final long new_tick = tick_panel.getSetTick();

				SongViewPort vp = parent.getViewPort();
				FWSSequence sequence = vp.getActiveSequence();

				final boolean replace = sequence.getEvent(fws_event);
								
				fws_event.note = ((Integer)spinner_note.getValue()).byteValue();
				fws_event.velocity = ((Integer)spinner_velocity.getValue()).byteValue();
				fws_event.duration = (Long)spinner_duration.getValue();
				fws_event.channel = (byte)((Integer)spinner_channel.getValue() - 1);

				fws_event.tick = new_tick;
				if(replace)
					sequence.refreshEvent(fws_event);
				else
					sequence.addEvent(fws_event);				

				vp.refreshSprite(fws_event);
				vp.refresh();
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_apply);
		
		this.setVisible(true);
	}
}
