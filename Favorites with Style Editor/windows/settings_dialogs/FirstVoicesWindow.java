package settings_dialogs;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fwsevents.FWSSequence;
import fwsevents.FWSVoiceEvent;
import main_window.FWSEditorMainWindow;
import sprites.Sprite;
import voices.Voice;

public class FirstVoicesWindow extends JDialog {
	private static final long serialVersionUID = 8705810675670045472L;

	private JCheckBox[] checkbox_channel = new JCheckBox[16];
	private JSpinner[] spinner_msb = new JSpinner[16], spinner_lsb = new JSpinner[16], spinner_prog = new JSpinner[16];

	@SuppressWarnings("unchecked")
	private JComboBox<String>[] dropdown_voice = new JComboBox[16];

	private boolean spinner_listen = true, dropdown_listen = true;

	public FirstVoicesWindow(FWSEditorMainWindow parent) {
		super(parent, true);

		this.setTitle("Initial Voices");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(570, 690));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);

		Voice[] voices = parent.getController().getVoiceList();
		FWSSequence sequence = parent.getViewPort().getActiveSequence();

		JLabel label_voice = new JLabel("Voice");
		label_voice.setHorizontalAlignment(SwingConstants.CENTER);
		label_voice.setBounds(145, 6, 201, 15);
		getContentPane().add(label_voice);
		
		JLabel label_msb = new JLabel("MSB");
		label_msb.setHorizontalAlignment(SwingConstants.CENTER);
		label_msb.setBounds(358, 6, 55, 15);
		getContentPane().add(label_msb);
		
		JLabel label_lsb = new JLabel("LSB");
		label_lsb.setHorizontalAlignment(SwingConstants.CENTER);
		label_lsb.setBounds(414, 6, 55, 15);
		getContentPane().add(label_lsb);
		
		JLabel label_program = new JLabel("Program");
		label_program.setBounds(470, 6, 73, 15);
		getContentPane().add(label_program);

		for(int c=0;c<16;c+=1) {
			checkbox_channel[c] = new JCheckBox("Channel " + (c+1));
			checkbox_channel[c].setToolTipText("Channel " + (c+1) + " is given a voice at tick 0 if this is checked.");
			checkbox_channel[c].setHorizontalAlignment(SwingConstants.TRAILING);
			checkbox_channel[c].setBounds(8, 35 + c*35, 129, 35);
			getContentPane().add(checkbox_channel[c]);

			final int vc = c;

			spinner_msb[c] = new JSpinner();
			spinner_msb[c].setModel(new SpinnerNumberModel(0, 0, 127, 1));
			spinner_msb[c].setBounds(358, 35 + c*35, 55, 35);
			spinner_msb[c].setToolTipText("Select the MSB for the voice for Channel " + (c+1) + ".");
			getContentPane().add(spinner_msb[c]);

			spinner_lsb[c] = new JSpinner();
			spinner_lsb[c].setModel(new SpinnerNumberModel(0, 0, 127, 1));
			spinner_lsb[c].setBounds(414, 35 + c*35, 55, 35);
			spinner_lsb[c].setToolTipText("Select the LSB for the voice for Channel " + (c+1) + ".");
			getContentPane().add(spinner_lsb[c]);

			spinner_prog[c] = new JSpinner();
			spinner_prog[c].setModel(new SpinnerNumberModel(0, 0, 127, 1));
			spinner_prog[c].setBounds(470, 35 + c*35, 55, 35);
			spinner_prog[c].setToolTipText("Select the program for the voice for Channel " + (c+1) + ".");
			getContentPane().add(spinner_prog[c]);

			dropdown_voice[c] = new JComboBox<String>();
			dropdown_voice[c].setToolTipText("Select the first voice for Channel " + (c+1) + " from the loaded voice list.");
			for(int i=0;i<voices.length;i+=1)
				dropdown_voice[c].addItem(voices[i].name);
			dropdown_voice[c].setBounds(145, 35 + c*35, 201, 35);
			dropdown_voice[c].setSelectedIndex(-1);
			getContentPane().add(dropdown_voice[c]);

			FWSVoiceEvent voice_event = sequence.getVoiceAt(0, (byte)c);
			if(voice_event == null) {
				checkbox_channel[c].setSelected(false);
				spinner_msb[c].setEnabled(false);
				spinner_lsb[c].setEnabled(false);
				spinner_prog[c].setEnabled(false);
				dropdown_voice[c].setEnabled(false);
			} else {
				checkbox_channel[c].setSelected(true);
				spinner_msb[c].setValue(Integer.valueOf(voice_event.voice_msb));
				spinner_lsb[c].setValue(Integer.valueOf(voice_event.voice_lsb));
				spinner_prog[c].setValue(Integer.valueOf(voice_event.voice));

				Voice sel_voice = Voice.matchVoice(voices, voice_event.voice, voice_event.voice_lsb, voice_event.voice_msb);

				if(sel_voice != null) {
					for(int i=0;i<voices.length;i+=1) {
						if(voices[i].match(voice_event.voice_msb, voice_event.voice_lsb, voice_event.voice)) {
							dropdown_voice[c].setSelectedIndex(i);
							break;
						}
					}
				}
			}

			dropdown_voice[vc].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(dropdown_voice[vc].getSelectedIndex() < 0 || dropdown_voice[vc].getSelectedIndex() >= voices.length)
						return;

					if(!dropdown_listen)
						return;

					spinner_listen = false;
					Voice sel_voice = voices[dropdown_voice[vc].getSelectedIndex()];
					spinner_msb[vc].setValue(Integer.valueOf(sel_voice.msb));
					spinner_lsb[vc].setValue(Integer.valueOf(sel_voice.lsb));
					spinner_prog[vc].setValue(Integer.valueOf(sel_voice.voice));
					spinner_listen = true;
				}
			});

			spinner_msb[vc].addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					if(!spinner_listen)
						return;

					final byte msb = ((Integer)spinner_msb[vc].getValue()).byteValue(), lsb = ((Integer)spinner_lsb[vc].getValue()).byteValue(), voice = ((Integer)spinner_prog[vc].getValue()).byteValue();

					dropdown_listen = false;
					dropdown_voice[vc].setSelectedIndex(-1);
					for(int i=0;i<voices.length;i+=1) {
						if(voices[i].match(msb, lsb, voice)) {
							dropdown_voice[vc].setSelectedIndex(i);
							break;
						}
					}
					dropdown_listen = true;
				}
			});

			spinner_lsb[vc].addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					if(!spinner_listen)
						return;

					final byte msb = ((Integer)spinner_msb[vc].getValue()).byteValue(), lsb = ((Integer)spinner_lsb[vc].getValue()).byteValue(), voice = ((Integer)spinner_prog[vc].getValue()).byteValue();

					dropdown_listen = false;
					dropdown_voice[vc].setSelectedIndex(-1);
					for(int i=0;i<voices.length;i+=1) {
						if(voices[i].match(msb, lsb, voice)) {
							dropdown_voice[vc].setSelectedIndex(i);
							break;
						}
					}
					dropdown_listen = true;
				}
			});

			spinner_prog[vc].addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					if(!spinner_listen)
						return;

					final byte msb = ((Integer)spinner_msb[vc].getValue()).byteValue(), lsb = ((Integer)spinner_lsb[vc].getValue()).byteValue(), voice = ((Integer)spinner_prog[vc].getValue()).byteValue();

					dropdown_listen = false;
					dropdown_voice[vc].setSelectedIndex(-1);
					for(int i=0;i<voices.length;i+=1) {
						if(voices[i].match(msb, lsb, voice)) {
							dropdown_voice[vc].setSelectedIndex(i);
							break;
						}
					}
					dropdown_listen = true;
				}
			});

			checkbox_channel[vc].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(checkbox_channel[vc].isSelected()) {
						spinner_msb[vc].setEnabled(true);
						spinner_lsb[vc].setEnabled(true);
						spinner_prog[vc].setEnabled(true);
						dropdown_voice[vc].setEnabled(true);

						final byte msb = ((Integer)spinner_msb[vc].getValue()).byteValue(), lsb = ((Integer)spinner_lsb[vc].getValue()).byteValue(), voice = ((Integer)spinner_prog[vc].getValue()).byteValue();

						dropdown_listen = false;
						dropdown_voice[vc].setSelectedIndex(-1);
						for(int i=0;i<voices.length;i+=1) {
							if(voices[i].match(msb, lsb, voice)) {
								dropdown_voice[vc].setSelectedIndex(i);
								break;
							}
						}
						dropdown_listen = true;
					} else {
						spinner_msb[vc].setEnabled(false);
						spinner_lsb[vc].setEnabled(false);
						spinner_prog[vc].setEnabled(false);
						dropdown_voice[vc].setEnabled(false);
					}
				}
			});
		}

		FirstVoicesWindow self = this;

		JButton button_cancel = new JButton("Cancel");
		button_cancel.setBounds(309, 606, 105, 35);
		button_cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_cancel);

		JButton button_apply = new JButton("Apply");
		button_apply.setBounds(426, 606, 105, 35);
		button_apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for(int c=0;c<16;c+=1) {
					FWSVoiceEvent existing_voice = sequence.getVoiceAt(0, (byte)c);
					if(existing_voice != null) {
						sequence.removeEvent(existing_voice);
						Sprite sprite = parent.getViewPort().getEventSprite(existing_voice);
						if(sprite != null)
							parent.getViewPort().removeSprite(sprite);
					}

					if(checkbox_channel[c].isSelected()) {
						FWSVoiceEvent new_voice = new FWSVoiceEvent();
						new_voice.tick = 0;
						new_voice.voice_msb = ((Integer)spinner_msb[c].getValue()).byteValue();
						new_voice.voice_lsb = ((Integer)spinner_lsb[c].getValue()).byteValue();
						new_voice.voice = ((Integer)spinner_prog[c].getValue()).byteValue();
						new_voice.channel = (byte)c;

						sequence.addEvent(new_voice);
						parent.getViewPort().addSprite(new_voice);
					}
				}

				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_apply);

		this.setVisible(true);
	}
}
