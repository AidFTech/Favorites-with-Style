package event_dialogs;

import javax.swing.JDialog;

import fwsevents.FWSSequence;
import fwsevents.FWSVoiceEvent;
import main_window.FWSEditorMainWindow;
import voices.Voice;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import canvas.SongViewPort;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class VoiceEventDialog extends JDialog {
	private static final long serialVersionUID = 1950061056022305461L;

	public VoiceEventDialog(FWSEditorMainWindow parent, FWSVoiceEvent fws_event) {
		super(parent, true);

		this.setTitle("Voice Event");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(495, 310));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);
		
		Voice[] voice_list = parent.getController().getVoiceList();
		String[] voice_names = new String[voice_list.length];

		for(int i=0;i<voice_names.length;i+=1)
			voice_names[i] = voice_list[i].name;
		
		JLabel label_channel = new JLabel("Channel");
		label_channel.setHorizontalAlignment(SwingConstants.RIGHT);
		label_channel.setBounds(12, 12, 60, 35);
		getContentPane().add(label_channel);
		
		JSpinner spinner_channel = new JSpinner();
		spinner_channel.setModel(new SpinnerNumberModel(fws_event.channel + 1, 1, 16, 1));
		spinner_channel.setBounds(89, 12, 73, 35);
		spinner_channel.setToolTipText("Select the voice channel.");
		getContentPane().add(spinner_channel);
		
		JLabel label_voice = new JLabel("Voice");
		label_voice.setHorizontalAlignment(SwingConstants.RIGHT);
		label_voice.setBounds(12, 58, 60, 35);
		getContentPane().add(label_voice);
		
		JComboBox<String> voice_dropdown = new JComboBox<String>(voice_names);
		voice_dropdown.setToolTipText("Select the voice to change to.");
		voice_dropdown.setBounds(89, 58, 348, 35);
		getContentPane().add(voice_dropdown);
		matchVoice(voice_dropdown, voice_list, fws_event.voice, fws_event.voice_lsb, fws_event.voice_msb);
		
		JLabel label_msb = new JLabel("MSB");
		label_msb.setHorizontalAlignment(SwingConstants.RIGHT);
		label_msb.setBounds(12, 105, 60, 35);
		getContentPane().add(label_msb);
		
		JSpinner spinner_msb = new JSpinner();
		spinner_msb.setToolTipText("Set the voice MSB.");
		spinner_msb.setModel(new SpinnerNumberModel(fws_event.voice_msb, 0, 127, 1));
		spinner_msb.setBounds(89, 105, 60, 35);
		getContentPane().add(spinner_msb);
		
		JLabel label_lsb = new JLabel("LSB");
		label_lsb.setHorizontalAlignment(SwingConstants.RIGHT);
		label_lsb.setBounds(156, 105, 34, 35);
		getContentPane().add(label_lsb);
		
		JSpinner spinner_lsb = new JSpinner();
		spinner_lsb.setModel(new SpinnerNumberModel(fws_event.voice_lsb, 0, 127, 1));
		spinner_lsb.setToolTipText("Set the voice LSB.");
		spinner_lsb.setBounds(208, 105, 60, 35);
		getContentPane().add(spinner_lsb);
		
		JLabel label_program = new JLabel("Program");
		label_program.setHorizontalAlignment(SwingConstants.RIGHT);
		label_program.setBounds(286, 105, 73, 35);
		getContentPane().add(label_program);
		
		JSpinner spinner_program = new JSpinner();
		spinner_program.setModel(new SpinnerNumberModel(fws_event.voice, 0, 127, 1));
		spinner_program.setToolTipText("Set the voice program.");
		spinner_program.setBounds(377, 105, 60, 35);
		getContentPane().add(spinner_program);
		
		SequenceTickPanel tick_panel = new SequenceTickPanel(parent, fws_event.tick, 12, 141, 326, 100);
		tick_panel.setBounds(12, 152, 326, 100);
		getContentPane().add(tick_panel);

		voice_dropdown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final int index = voice_dropdown.getSelectedIndex();
				if(index >= 0 && index < voice_list.length) {
					Voice new_voice = voice_list[index];
					spinner_msb.setValue(Integer.valueOf(new_voice.msb));
					spinner_lsb.setValue(Integer.valueOf(new_voice.lsb));
					spinner_program.setValue(Integer.valueOf(new_voice.voice));
				}
			}
		});

		spinner_msb.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				final int msb = (Integer)spinner_msb.getValue(), lsb = (Integer)spinner_lsb.getValue(), prog = (Integer)spinner_program.getValue();
				matchVoice(voice_dropdown, voice_list, (byte)prog, (byte)lsb, (byte)msb);
			}
		});

		spinner_lsb.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				final int msb = (Integer)spinner_msb.getValue(), lsb = (Integer)spinner_lsb.getValue(), prog = (Integer)spinner_program.getValue();
				matchVoice(voice_dropdown, voice_list, (byte)prog, (byte)lsb, (byte)msb);
			}
		});

		spinner_program.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				final int msb = (Integer)spinner_msb.getValue(), lsb = (Integer)spinner_lsb.getValue(), prog = (Integer)spinner_program.getValue();
				matchVoice(voice_dropdown, voice_list, (byte)prog, (byte)lsb, (byte)msb);
			}
		});

		VoiceEventDialog self = this;

		JButton button_cancel = new JButton("Cancel");
		button_cancel.setBounds(261, 263, 105, 35);
		button_cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_cancel);
		
		JButton button_apply = new JButton("Apply");
		button_apply.setBounds(378, 263, 105, 35);
		button_apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				final long new_tick = tick_panel.getSetTick();

				SongViewPort vp = parent.getViewPort();
				FWSSequence sequence = vp.getActiveSequence();
								
				fws_event.voice = ((Integer)spinner_program.getValue()).byteValue();
				fws_event.voice_lsb = ((Integer)spinner_lsb.getValue()).byteValue();
				fws_event.voice_msb = ((Integer)spinner_msb.getValue()).byteValue();
				fws_event.channel = (byte)((Integer)spinner_channel.getValue() - 1);

				if(new_tick == 0 && fws_event.tick != 0) {
					final int answer = JOptionPane.showConfirmDialog(self, "Replace the initial voice event?", "Voice", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if(answer == JOptionPane.NO_OPTION)
						return;

					sequence.removeEvent(sequence.getVoiceAt(0, fws_event.channel));
					fws_event.tick = new_tick;

					if(sequence.getEvent(fws_event))
						sequence.removeEvent(fws_event);
					
					sequence.addEvent(fws_event);
				} else {
					fws_event.tick = new_tick;
					if(sequence.getEvent(fws_event))
						sequence.refreshEvent(fws_event);
					else
						sequence.addEvent(fws_event);
				}

				vp.refreshSprite(fws_event);
				vp.refresh();
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_apply);

		this.setVisible(true);
	}

	/** Match the voice combobox to one on the list. */
	private static void matchVoice(JComboBox<String> voice_dropdown, Voice[] voice_list, final byte prog, final byte lsb, final byte msb) {
		Voice start_voice = Voice.matchVoice(voice_list, prog, lsb, msb);
		if(start_voice != null) {
			for(int i=0;i<voice_list.length;i+=1) {
				if(voice_list[i] == start_voice) {
					voice_dropdown.setSelectedIndex(i);
					break;
				}
			}
		} else
			voice_dropdown.setSelectedIndex(-1);
	}
}
