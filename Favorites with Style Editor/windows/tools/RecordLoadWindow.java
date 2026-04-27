package tools;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;

import main_window.FWSEditorMainWindow;
import options.RecordLoadOptions;
import style.Style;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fwsevents.FWSSequence;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JComboBox;
import javax.swing.JButton;

public class RecordLoadWindow extends JDialog {
	private static final long serialVersionUID = 7764885384815860718L;

	private boolean spinner_listen = true;
	private JComboBox<String> dropdown_section;

	private boolean save = false;

	public RecordLoadWindow(FWSEditorMainWindow parent, FWSSequence sequence, RecordLoadOptions options, Style style) {
		super(parent, true);

		this.setTitle(options.live_import ? "Record Events" : "Import Events");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(440, 720));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);
		
		JLabel label_import_events = new JLabel("Import Events");
		label_import_events.setBounds(12, 12, 116, 32);
		getContentPane().add(label_import_events);
		
		JCheckBox checkbox_note_events = new JCheckBox("Note Events");
		checkbox_note_events.setToolTipText("Check to import recorded note events.");
		checkbox_note_events.setBounds(12, 52, 150, 32);
		checkbox_note_events.setSelected(options.import_notes);
		getContentPane().add(checkbox_note_events);
		
		JCheckBox checkbox_voice_events = new JCheckBox("Voice Events");
		checkbox_voice_events.setToolTipText("Check to import recorded voice change (program) events.");
		checkbox_voice_events.setBounds(12, 88, 150, 32);
		checkbox_voice_events.setSelected(options.import_voice);
		getContentPane().add(checkbox_voice_events);
		
		JCheckBox checkbox_short_events = new JCheckBox("Short Events");
		checkbox_short_events.setToolTipText("Check to import recorded short events.");
		checkbox_short_events.setBounds(12, 124, 150, 32);
		checkbox_short_events.setSelected(options.import_short);
		getContentPane().add(checkbox_short_events);
		
		JCheckBox checkbox_tempo_events = new JCheckBox("Tempo Events");
		checkbox_tempo_events.setToolTipText("Check to import recorded tempo events.");
		checkbox_tempo_events.setBounds(12, 160, 150, 32);
		checkbox_tempo_events.setSelected(options.import_tempo);
		getContentPane().add(checkbox_tempo_events);
		
		JCheckBox checkbox_time_events = new JCheckBox("Time Events");
		checkbox_time_events.setToolTipText("Check to import recorded time signature events.");
		checkbox_time_events.setBounds(12, 196, 150, 32);
		checkbox_time_events.setSelected(options.import_time);
		getContentPane().add(checkbox_time_events);
		
		JCheckBox checkbox_key_events = new JCheckBox("Key Events");
		checkbox_key_events.setToolTipText("Check to import recorded key signature events.");
		checkbox_key_events.setBounds(12, 232, 150, 32);
		checkbox_key_events.setSelected(options.import_key);
		getContentPane().add(checkbox_key_events);
		
		JCheckBox checkbox_sysex_events = new JCheckBox("Sysex Events");
		checkbox_sysex_events.setToolTipText("Check to import recorded system exclusive events.");
		checkbox_sysex_events.setBounds(12, 268, 150, 32);
		checkbox_sysex_events.setSelected(options.import_sysex);
		getContentPane().add(checkbox_sysex_events);
		
		JCheckBox checkbox_other_events = new JCheckBox("Other Events");
		checkbox_other_events.setToolTipText("Check to import any other recorded MIDI events.");
		checkbox_other_events.setBounds(12, 304, 150, 32);
		checkbox_other_events.setSelected(options.import_other);
		getContentPane().add(checkbox_other_events);
		
		JLabel label_import_channels = new JLabel("Import Channels");
		label_import_channels.setHorizontalAlignment(SwingConstants.TRAILING);
		label_import_channels.setBounds(313, 12, 115, 32);
		getContentPane().add(label_import_channels);
		
		JLabel label_timeshift = new JLabel("Time Shift");
		label_timeshift.setBounds(12, 388, 116, 32);
		getContentPane().add(label_timeshift);
		
		JSpinner spinner_measure_shift = new JSpinner();
		spinner_measure_shift.setToolTipText("Shift the recorded data by a number of measures.");
		spinner_measure_shift.setModel(new SpinnerNumberModel(0, 0, sequence.getMeasureTicks().length - 1, 1));
		spinner_measure_shift.setBounds(12, 424, 74, 32);
		getContentPane().add(spinner_measure_shift);
		
		JSpinner spinner_tick_shift = new JSpinner();
		spinner_tick_shift.setToolTipText("Shift the recorded data by a number of ticks.");
		spinner_tick_shift.setModel(new SpinnerNumberModel(Long.valueOf(0), Long.valueOf(0), Long.valueOf(sequence.getSequenceLength()), Long.valueOf(1)));
		spinner_tick_shift.setBounds(98, 424, 74, 32);
		getContentPane().add(spinner_tick_shift);

		spinner_measure_shift.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if(!spinner_listen)
					return;

				spinner_listen = false;
				final int m = (Integer)spinner_measure_shift.getValue();
				long[] measures = sequence.getMeasureTicks();
				spinner_tick_shift.setValue(Long.valueOf(measures[m]));
				spinner_listen = true;
			}
		});

		spinner_tick_shift.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if(!spinner_listen)
					return;

				spinner_listen = false;
				final long tick = (Long)spinner_tick_shift.getValue();
				long[] measures = sequence.getMeasureTicks();
				int m = measures.length - 1;
				for(int i=0;i<measures.length;i+=1) {
					if(measures[i] > tick) {
						m = i > 0 ? i-1 : i;
						break;
					}
				}
				spinner_measure_shift.setValue(Integer.valueOf(m));
				spinner_listen = true;
			}
		});
		
		JLabel label_measures = new JLabel("Measures");
		label_measures.setBounds(12, 456, 74, 26);
		getContentPane().add(label_measures);
		
		JLabel label_ticks = new JLabel("Ticks");
		label_ticks.setBounds(98, 456, 74, 26);
		getContentPane().add(label_ticks);
		
		JCheckBox checkbox_first_note = new JCheckBox("First Note at t=0");
		checkbox_first_note.setSelected(options.start_notes_at_zero);
		checkbox_first_note.setToolTipText("Check to put the first note event at tick 0 (plus the time shift).");
		checkbox_first_note.setBounds(12, 490, 150, 32);
		getContentPane().add(checkbox_first_note);
		
		JCheckBox checkbox_add_shorts = new JCheckBox("Add Shorts to t=0");
		checkbox_add_shorts.setSelected(options.add_shorts_to_beginning);
		checkbox_add_shorts.setToolTipText("Check to add all pre-note short events to tick 0.");
		checkbox_add_shorts.setBounds(12, 526, 150, 32);
		getContentPane().add(checkbox_add_shorts);
		
		dropdown_section = new JComboBox<String>();
		dropdown_section.setToolTipText("Select the style section to add the events to.");
		dropdown_section.setBounds(12, 604, 137, 35);

		if(style != null) {
			JLabel label_section = new JLabel("Style Section");
			label_section.setBounds(12, 566, 116, 26);
			getContentPane().add(label_section);
		
			String[] style_sections = style.getSectionNames();
			dropdown_section.addItem("Fill Style");
			int s = -1;
			for(int i=0;i<style_sections.length;i+=1) {
				dropdown_section.addItem(style_sections[i]);
				if(s<0 && style_sections[i].toUpperCase().contains("MAIN"))
					s = i;
			}
			if(s<0)
				s = 0;
			dropdown_section.setSelectedIndex(s);

			getContentPane().add(dropdown_section);
		}

		JCheckBox[] checkbox_channels = new JCheckBox[16];
		JSpinner[] spinner_channels = new JSpinner[16];
		
		for(int i=0;i<checkbox_channels.length;i+=1) {
			JCheckBox checkbox_channel = new JCheckBox("Channel " + (i+1));
			checkbox_channel.setToolTipText("Check to import events recorded on channel " + (i+1) + ".");
			checkbox_channel.setBounds(218, 52 + 35*i, 114, 32);
			checkbox_channel.setSelected(options.channel_map[i] >= 0);
			getContentPane().add(checkbox_channel);

			checkbox_channels[i] = checkbox_channel;
			
			JSpinner spinner_channel = new JSpinner();
			spinner_channel.setToolTipText("Select the channel to copy events from channel " + (i+1) + " to.");
			spinner_channel.setModel(new SpinnerNumberModel(i+1, 1, 16, 1));
			spinner_channel.setBounds(340, 52 + 35*i, 62, 32);
			getContentPane().add(spinner_channel);

			spinner_channels[i] = spinner_channel;

			if(options.channel_map[i] < 0)
				spinner_channel.setEnabled(false);

			checkbox_channel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					spinner_channel.setEnabled(checkbox_channel.isSelected());
				}
			});
		}
		
		JCheckBox checkbox_extend = new JCheckBox("Extend Sequence Time");
		checkbox_extend.setToolTipText("Check to extend the sequence time to accommodate the imported events.");
		checkbox_extend.setSelected(options.extend);
		checkbox_extend.setBounds(12, 647, 181, 32);
		getContentPane().add(checkbox_extend);

		RecordLoadWindow self = this;

		JButton button_save = new JButton("Save");
		button_save.setBounds(323, 673, 105, 35);
		button_save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				save = true;

				options.import_notes = checkbox_note_events.isSelected();
				options.import_voice = checkbox_voice_events.isSelected();
				options.import_short = checkbox_short_events.isSelected();
				options.import_tempo = checkbox_tempo_events.isSelected();
				options.import_time = checkbox_time_events.isSelected();
				options.import_key = checkbox_key_events.isSelected();
				options.import_sysex = checkbox_sysex_events.isSelected();
				options.import_other = checkbox_other_events.isSelected();

				options.timeshift = (Long)spinner_tick_shift.getValue();
				options.start_notes_at_zero = checkbox_first_note.isSelected();
				options.add_shorts_to_beginning = checkbox_add_shorts.isSelected();
				options.extend = checkbox_extend.isSelected();

				for(int i=0;i<spinner_channels.length;i+=1) {
					if(checkbox_channels[i].isSelected())
						options.channel_map[i] = (byte)((Integer)spinner_channels[i].getValue() - 1);
					else
						options.channel_map[i] = -1;
				}

				if(style != null) {
					if(dropdown_section.getSelectedIndex() > 0) {
						options.section = (String)dropdown_section.getSelectedItem();
					}
				}

				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_save);
		
		JButton button_cancel = new JButton("Cancel");
		button_cancel.setBounds(206, 673, 105, 35);
		button_cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(options.live_import) {
					final int answer = JOptionPane.showConfirmDialog(self, "This will drop all recorded MIDI events. Are you sure you want to cancel?", "Cencel Recording", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if(answer == JOptionPane.NO_OPTION)
						return;
				}
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_cancel);
		
		this.setVisible(true);
	}

	/** Return whether the save button was clicked. */
	public boolean getSaved() {
		return this.save;
	}
}
