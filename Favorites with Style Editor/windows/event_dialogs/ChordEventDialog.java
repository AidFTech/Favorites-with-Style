package event_dialogs;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import canvas.SongViewPort;
import dialogpanels.ChordPiano;
import fwsevents.FWSChordEvent;
import fwsevents.FWSSequence;
import main_window.FWSEditorMainWindow;
import style.ChordBody;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class ChordEventDialog extends JDialog {
	private static final long serialVersionUID = -4538142478170051316L;

	private ChordBody set_main_chord, set_bass_chord;

	private boolean listen_dropdown = true, listen_checkbox = true;

	public ChordEventDialog(FWSEditorMainWindow parent, FWSChordEvent fws_event) {
		super(parent, true);
		
		this.setTitle("Chord Change Event");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(359, 550));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);

		set_main_chord = new ChordBody(fws_event.main_chord);
		set_bass_chord = new ChordBody(fws_event.bass_chord);
		
		JLabel label_enter_or_select = new JLabel("Enter or select the chord:");
		label_enter_or_select.setBounds(18, 11, 249, 16);
		getContentPane().add(label_enter_or_select);

		JTextField text_field_chord = new JFormattedTextField();
		text_field_chord.setPreferredSize(new Dimension(5, 35));
		text_field_chord.setToolTipText("Type the name of the chord you want.");
		text_field_chord.setBounds(35, 34, 299, 35);
		getContentPane().add(text_field_chord);

		JLabel label_main_chord = new JLabel("Main Chord");
		label_main_chord.setHorizontalAlignment(SwingConstants.TRAILING);
		label_main_chord.setBounds(12, 81, 79, 35);
		getContentPane().add(label_main_chord);
		
		JLabel label_bass_chord = new JLabel("Bass Chord");
		label_bass_chord.setHorizontalAlignment(SwingConstants.TRAILING);
		label_bass_chord.setBounds(12, 128, 81, 35);
		getContentPane().add(label_bass_chord);
		
		JComboBox<String> dropdown_main_chord_root = new JComboBox<String>();
		dropdown_main_chord_root.setToolTipText("Set the chord root.");
		dropdown_main_chord_root.setModel(new DefaultComboBoxModel<String>(new String[] {"N.C.", "C", "C\u266F", "D\u266D", "D", "D\u266F", "E\u266D", "E", "F", "F\u266F", "G\u266D", "G", "G\u266F", "A\u266D", "A", "A\u266F", "B\u266D", "B"}));
		dropdown_main_chord_root.setBounds(101, 81, 89, 35);
		getContentPane().add(dropdown_main_chord_root);
		
		JComboBox<String> dropdown_main_chord_type = new JComboBox<String>();
		dropdown_main_chord_type.setToolTipText("Set the chord type.");
		dropdown_main_chord_type.setModel(new DefaultComboBoxModel<String>(new String[] {"(major)", "6", "M7", "M7(\u266F11)", "(9)", "M7(9)", "6(9)", "aug", "m", "m6", "m7", "m7\u266D5", "m(9)", "m7(9)", "m7(11)", "mM7", "mM7(9)", "dim", "dim7", "7", "7sus4", "7\u266D5", "7(9)", "7(\u266F11)", "7(13)", "7(\u266D9)", "7(\u266D13)", "7(\u266F9)", "M7aug", "7aug", "1+8", "1+5", "sus4", "sus2", "(cancel)", "M7\u266D5", "\u266D5", "mM7\u266D5"}));
		dropdown_main_chord_type.setBounds(202, 81, 132, 35);
		getContentPane().add(dropdown_main_chord_type);
		
		JComboBox<String> dropdown_bass_chord_root = new JComboBox<String>();
		dropdown_bass_chord_root.setToolTipText("Choose the bass chord root.");
		dropdown_bass_chord_root.setModel(new DefaultComboBoxModel<String>(new String[] {"N.C.", "C", "C\u266F", "D\u266D", "D", "D\u266F", "E\u266D", "E", "F", "F\u266F", "G\u266D", "G", "G\u266F", "A\u266D", "A", "A\u266F", "B\u266D", "B"}));
		dropdown_bass_chord_root.setBounds(101, 128, 89, 35);
		getContentPane().add(dropdown_bass_chord_root);
		
		JComboBox<String> dropdown_bass_chord_type = new JComboBox<String>();
		dropdown_bass_chord_type.setToolTipText("Choose the bass chord type.");
		dropdown_bass_chord_type.setModel(new DefaultComboBoxModel<String>(new String[] {"(major)", "6", "M7", "M7(\u266F11)", "(9)", "M7(9)", "6(9)", "aug", "m", "m6", "m7", "m7\u266D5", "m(9)", "m7(9)", "m7(11)", "mM7", "mM7(9)", "dim", "dim7", "7", "7sus4", "7\u266D5", "7(9)", "7(\u266F11)", "7(13)", "7(\u266D9)", "7(\u266D13)", "7(\u266F9)", "M7aug", "7aug", "1+8", "1+5", "sus4", "sus2", "(cancel)", "M7\u266D5", "\u266D5", "mM7\u266D5"}));
		dropdown_bass_chord_type.setBounds(202, 128, 132, 35);
		getContentPane().add(dropdown_bass_chord_type);
		
		JCheckBox checkbox_bass = new JCheckBox("Bass");
		checkbox_bass.setToolTipText("Check if the bass of this chord is to be different from the root.");
		checkbox_bass.setBounds(101, 171, 79, 35);
		getContentPane().add(checkbox_bass);
		
		JLabel label_inversion = new JLabel("Inversion");
		label_inversion.setHorizontalAlignment(SwingConstants.RIGHT);
		label_inversion.setBounds(182, 175, 70, 31);
		getContentPane().add(label_inversion);
		
		JSpinner spinner_inversion = new JSpinner();
		spinner_inversion.setToolTipText("Set the chord inversion. This will not affect how the style is played, only fingering aesthetics.");
		spinner_inversion.setModel(new SpinnerNumberModel(fws_event.inversion, 0, set_main_chord.getInversionCount(), 1));
		spinner_inversion.setBounds(270, 175, 64, 35);
		getContentPane().add(spinner_inversion);

		JLabel label_chord_name = new JLabel("");
		label_chord_name.setFont(new Font("Arial Unicode MS", Font.PLAIN, 40));
		label_chord_name.setHorizontalAlignment(SwingConstants.CENTER);
		label_chord_name.setBounds(12, 219, 335, 65);
		getContentPane().add(label_chord_name);

		ChordPiano chord_piano = new ChordPiano();
		chord_piano.setBounds(12, 296, 335, 65);
		getContentPane().add(chord_piano);

		checkbox_bass.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!listen_checkbox)
					return;

				listen_dropdown = false;
				if(checkbox_bass.isSelected()) {
					dropdown_bass_chord_root.setSelectedIndex(dropdown_main_chord_root.getSelectedIndex());
					dropdown_bass_chord_type.setEnabled(true);
					dropdown_bass_chord_type.setSelectedIndex(dropdown_main_chord_type.getSelectedIndex());
				} else {
					dropdown_bass_chord_root.setSelectedIndex(0);
					dropdown_bass_chord_type.setEnabled(false);
					dropdown_bass_chord_type.setSelectedIndex(34);
				}
				listen_dropdown = true;
			}
		});

		dropdown_main_chord_root.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!listen_dropdown)
					return;

				set_main_chord.setRoot(getRoot(dropdown_main_chord_root.getSelectedIndex()));
				updateChordLabel(label_chord_name, chord_piano, (Integer)spinner_inversion.getValue(), set_main_chord, set_bass_chord);
			}
		});

		dropdown_bass_chord_root.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!listen_dropdown)
					return;

				set_bass_chord.setRoot(getRoot(dropdown_bass_chord_root.getSelectedIndex()));
				updateChordLabel(label_chord_name, chord_piano, (Integer)spinner_inversion.getValue(), set_main_chord, set_bass_chord);

				
				if(dropdown_bass_chord_root.getSelectedIndex() <= 0) {
					listen_checkbox = false;
					checkbox_bass.setSelected(false);
					listen_checkbox = true;
					dropdown_bass_chord_type.setSelectedIndex(34);
					dropdown_bass_chord_type.setEnabled(false);
				} else {
					listen_checkbox = false;
					checkbox_bass.setSelected(true);
					listen_checkbox = true;
					dropdown_bass_chord_type.setEnabled(true);
				}
			}
		});

		dropdown_main_chord_type.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!listen_dropdown)
					return;

				set_main_chord.setChord((byte)dropdown_main_chord_type.getSelectedIndex());
				spinner_inversion.setModel(new SpinnerNumberModel(0, 0, set_main_chord.getInversionCount(), 1));

				updateChordLabel(label_chord_name, chord_piano, (Integer)spinner_inversion.getValue(), set_main_chord, set_bass_chord);
			}
		});

		dropdown_bass_chord_type.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!listen_dropdown)
					return;

				set_bass_chord.setChord((byte)dropdown_bass_chord_type.getSelectedIndex());
				updateChordLabel(label_chord_name, chord_piano, (Integer)spinner_inversion.getValue(), set_main_chord, set_bass_chord);
			}
		});
		
		updateChordsDropdown(set_main_chord, set_bass_chord, dropdown_main_chord_root, dropdown_main_chord_type, dropdown_bass_chord_root, dropdown_bass_chord_type);

		text_field_chord.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateChordsText(text_field_chord.getText(), set_main_chord, set_bass_chord);
				updateChordsDropdown(set_main_chord, set_bass_chord, dropdown_main_chord_root, dropdown_main_chord_type, dropdown_bass_chord_root, dropdown_bass_chord_type);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateChordsText(text_field_chord.getText(), set_main_chord, set_bass_chord);
				updateChordsDropdown(set_main_chord, set_bass_chord, dropdown_main_chord_root, dropdown_main_chord_type, dropdown_bass_chord_root, dropdown_bass_chord_type);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateChordsText(text_field_chord.getText(), set_main_chord, set_bass_chord);
				updateChordsDropdown(set_main_chord, set_bass_chord, dropdown_main_chord_root, dropdown_main_chord_type, dropdown_bass_chord_root, dropdown_bass_chord_type);
			}
			
		});

		spinner_inversion.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				chord_piano.setHighlighted(set_main_chord, (Integer)spinner_inversion.getValue());	
			}
		});

		spinner_inversion.setValue(Integer.valueOf(fws_event.inversion));
		
		SequenceTickPanel tick_panel = new SequenceTickPanel(parent, fws_event.tick, 12, 370, 326, 100);
		tick_panel.setBounds(12, 373, 326, 100);
		getContentPane().add(tick_panel);

		ChordEventDialog self = this;

		JButton button_cancel = new JButton("Cancel");
		button_cancel.setBounds(125, 503, 105, 35);
		button_cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_cancel);

		JButton button_apply = new JButton("Apply");
		button_apply.setBounds(242, 503, 105, 35);
		button_apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				final long new_tick = tick_panel.getSetTick();

				SongViewPort vp = parent.getViewPort();
				FWSSequence sequence = vp.getActiveSequence();

				fws_event.inversion = (Integer)spinner_inversion.getValue();
				fws_event.main_chord = set_main_chord;
				fws_event.bass_chord = set_bass_chord;
				
				fws_event.tick = new_tick;
				if(sequence.getEvent(fws_event))
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

	/** Get a chord root from an index. */
	private byte getRoot(final int index) {
		switch(index) {
		case 1: //C
			return 0x31;
		case 2: //C#
			return 0x41;
		case 3: //Db
			return 0x22;
		case 4: //D
			return 0x32;
		case 5: //D#
			return 0x42;
		case 6: //Eb
			return 0x23;
		case 7: //E
			return 0x33;
		case 8: //F
			return 0x34;
		case 9: //F#
			return 0x44;
		case 10: //Gb
			return 0x25;
		case 11: //G
			return 0x35;
		case 12: //G#
			return 0x45;
		case 13: //Ab
			return 0x26;
		case 14: //A
			return 0x36;
		case 15: //A#
			return 0x46;
		case 16: //Bb
			return 0x27;
		case 17: //B
			return 0x37;
		default:
			return 0x7F;
		}
	}

	/** Get the dropdown index for the specified chord root. */
	private int getRootIndex(final byte root) {
		switch(root) {
		case 0x31: //C
		case 0x47:
			return 1;
		case 0x41: //C#
			return 2;
		case 0x22: //Db
			return 3;
		case 0x32: //D
			return 4;
		case 0x42: //D#
			return 5;
		case 0x23: //Eb
			return 6;
		case 0x33: //E
		case 0x24:
			return 7;
		case 0x34: //F
		case 0x43:
			return 8;
		case 0x44: //F#
			return 9;
		case 0x25: //Gb
			return 10;
		case 0x35: //G
			return 11;
		case 0x45: //G#
			return 12;
		case 0x26: //Ab
			return 13;
		case 0x36: //A
			return 14;
		case 0x46: //A#
			return 15;
		case 0x27: //Bb
			return 16;
		case 0x37: //B
		case 0x21:
			return 17;
		default:
			return 0;
		}
	}

	/** Update the chord dropdowns. */
	private void updateChordsDropdown(ChordBody main, ChordBody bass, JComboBox<String> dropdown_main, JComboBox<String> dropdown_main_chord, JComboBox<String> dropdown_bass, JComboBox<String> dropdown_bass_chord) {
		final byte main_root = main.getFullRoot(), main_chord = main.getChord(), bass_root = bass.getFullRoot(), bass_chord = bass.getChord();

		int main_root_sel = 0, main_chord_sel = 34, bass_root_sel = 0, bass_chord_sel = 34;
		main_root_sel = getRootIndex(main_root);

		if(main_chord >= 0 && main_chord < 38)
			main_chord_sel = main_chord;

		bass_root_sel = getRootIndex(bass_root);

		if(bass_chord >= 0 && bass_chord < 38)
			bass_chord_sel = bass_chord;

		dropdown_main.setSelectedIndex(main_root_sel);
		dropdown_main_chord.setSelectedIndex(main_chord_sel);
		dropdown_bass.setSelectedIndex(bass_root_sel);
		dropdown_bass_chord.setSelectedIndex(bass_chord_sel);
		
	}

	/** Interpret the desired chord from text. */
	private void updateChordsText(String text, ChordBody main, ChordBody bass) {
		int type_index = -1;
		String text_ns = text.replaceAll(" ","");
		if(text.contains("/")) {
			//Bass chord present. Split and use recursion.
			updateChordsText(text.substring(0,text.indexOf("/")),main,new ChordBody());
			updateChordsText(text.substring(text.indexOf("/")+1,text.length()),bass,new ChordBody());
		} else {
			if(text_ns.contains("6"))
			{
				if(text.toUpperCase().contains("MAJ6") || text.toUpperCase().contains("MAJ 6"))
				{
					if(text_ns.contains("9") && text.indexOf("6") < text.indexOf("9"))
						main.setChord((byte)0x6);
					else
						main.setChord((byte)0x1);
				}
				else if(text_ns.contains("m") && text.indexOf("m") < text.indexOf("6"))
					main.setChord((byte)0x9);
				else if(text_ns.contains("9") && text.indexOf("6") < text.indexOf("9"))
					main.setChord((byte)0x6);
				else
					main.setChord((byte)0x1);
				
				type_index = text.indexOf("6");
			} else if(text_ns.contains("m") && !text.toUpperCase().contains("DIM") && (!text.toUpperCase().contains("MAJ") || text.toUpperCase().contains("MMAJ"))) {
				if(text_ns.contains("M7") || text.toUpperCase().contains("MAJ7") || text.toUpperCase().contains("MAJ 7")) {
					if(text.contains("9") && text.indexOf("m") < text.indexOf("9"))
						main.setChord((byte)0x10);
					else if(text_ns.contains("b5") && text_ns.indexOf("7") < text_ns.indexOf("b5")
							|| text_ns.contains("\u266D5") && text_ns.indexOf("7") < text_ns.indexOf("\u266D5"))
						main.setChord((byte)0x25);
					else
						main.setChord((byte)0x0F);
				} else if(text.contains("7") && !text.toUpperCase().contains("DIM") && text.indexOf("m") < text.indexOf("7")) {
					if(text.contains("9") && text.indexOf("7") < text.indexOf("9"))
						main.setChord((byte)0x0D);
					else if(text_ns.contains("11") && text_ns.indexOf("7") < text_ns.indexOf("11"))
						main.setChord((byte)0x0E);
					else if(text_ns.contains("b5") && text_ns.indexOf("7") < text_ns.indexOf("b5")
							|| text_ns.contains("\u266D5") && text_ns.indexOf("7") < text_ns.indexOf("\u266D5"))
						main.setChord((byte)0x0B);
					else
						main.setChord((byte)0x0A);
				}
				else if(text.toUpperCase().contains("ADD9") && text.indexOf("m") < text.toUpperCase().indexOf("ADD9"))
					main.setChord((byte)0x0C);
				else if(text.contains("9") && text.indexOf("m") < text.indexOf("9"))
					main.setChord((byte)0x0C);
				else
					main.setChord((byte)0x08);
				
				type_index = text.indexOf("m");
			} else if((text_ns.contains("M7") || text.toUpperCase().contains("MAJ7") || text.toUpperCase().contains("MAJ 7")) && !text.toUpperCase().contains("DIM")) {
				if(text.contains("9") && text.indexOf("7") < text.indexOf("9"))
					main.setChord((byte)0x5);
				else if(text.toUpperCase().contains("AUG") && text.indexOf("7") < text.toUpperCase().indexOf("AUG"))
					main.setChord((byte)0x1C);
				else if((text.contains("#11") && text.indexOf("7") < text.indexOf("#11")) ||
						(text.contains("\u266F11") && text.indexOf("7") < text.indexOf("\u266F11")))
					main.setChord((byte)0x03);
				else if((text.contains("b5") && text.indexOf("7") < text.indexOf("b5")) ||
						(text.contains("\u266D5") && text.indexOf("7") < text.indexOf("\u266D5")))
					main.setChord((byte)0x23);
				else
					main.setChord((byte)0x02);
				
				if(text_ns.toUpperCase().contains("MAJ"))
					type_index = text.toUpperCase().indexOf("MAJ");
				else
					type_index = text.indexOf("M7");
			} else if(text_ns.contains("7") && !text.toUpperCase().contains("DIM")) {
				if((text.contains("#9") && text.indexOf("7") < text.indexOf("#9")) ||
						(text.contains("\u266F9") && text.indexOf("7") < text.indexOf("\u266F9")))
					main.setChord((byte)0x1B);
				else if(text.toUpperCase().contains("AUG") && text.indexOf("7") < text.toUpperCase().indexOf("AUG"))
					main.setChord((byte)0x1D);
				else if((text.contains("#11") && text.indexOf("7") < text.indexOf("#11")) ||
						(text.contains("\u266F11") && text.indexOf("7") < text.indexOf("\u266F11")))
					main.setChord((byte)0x17);
				else if((text.contains("b9") && text.indexOf("7") < text.indexOf("b9")) ||
						(text.contains("\u266D9") && text.indexOf("7") < text.indexOf("\u266D9")))
					main.setChord((byte)0x19);
				else if(text.contains("9") && text.indexOf("7") < text.indexOf("9"))
					main.setChord((byte)0x16);
				else if((text.contains("b5") && text.indexOf("7") < text.indexOf("b5")) ||
						(text.contains("\u266D5") && text.indexOf("7") < text.indexOf("\u266D5")))
					main.setChord((byte)0x15);
				else if((text.contains("b13") && text.indexOf("7") < text.indexOf("b13")) ||
						(text.contains("\u266D13") && text.indexOf("7") < text.indexOf("\u266D13")))
					main.setChord((byte)0x1A);
				else if(text.contains("13") && text.indexOf("7") < text.indexOf("13"))
					main.setChord((byte)0x18);
				else if(text.toUpperCase().contains("SUS"))
					main.setChord((byte)0x14);
				else
					main.setChord((byte)0x13);
				
				type_index = text.indexOf("7");
			} else if(text.toUpperCase().contains("ADD9")) {
				main.setChord((byte)0x4);
				type_index = text.toUpperCase().indexOf("ADD9");
			} else if(text.contains("9")) {
				main.setChord((byte)0x4);
				type_index = text.indexOf("9");
			} else if(text.toUpperCase().contains("DIM")) {
				if(text_ns.toUpperCase().contains("DIM7"))
					main.setChord((byte)0x12);
				else
					main.setChord((byte)0x11);
				
				type_index = text.toUpperCase().indexOf("DIM");
			} else if(text.toUpperCase().contains("AUG")) {
				main.setChord((byte)0x7);
				type_index = text.toUpperCase().indexOf("AUG");
			} else if(text.contains("+")) {
				main.setChord((byte)0x7);
				type_index = text.indexOf("+");
			} else if(text.toUpperCase().contains("SUS") && text_ns.toUpperCase().contains("SUS4") || text.contains("4")) {
				main.setChord((byte)0x20);
				type_index = text.toUpperCase().indexOf("SUS");
			} else if(text.toUpperCase().contains("SUS") && text_ns.toUpperCase().contains("SUS2") || text.contains("2")) {
				main.setChord((byte)0x21);
				type_index = text.toUpperCase().indexOf("SUS");
			} else if(text.contains("b5") || text.contains("\u266D5")) {
				main.setChord((byte)0x24);
				type_index = text.indexOf("b5");
			} else if(text.contains("8")) {
				main.setChord((byte)0x1E);
				type_index = text.indexOf("8");
			} else if(text.contains("5")) {
				main.setChord((byte)0x1F);
				type_index = text.indexOf("5");
			} else {
				main.setChord((byte)0x00);
			}
			
			if(type_index > 0) {
				text = text.substring(0, type_index);
				text_ns = text.replaceAll(" ", "");
			}
			
			if(text_ns.toUpperCase().contains("C")) {
				if(text_ns.toUpperCase().contains("C#") || text_ns.toUpperCase().contains("C\u266F"))
					main.setRoot((byte)0x41);
				else if(text_ns.contains("Cb") || text_ns.toUpperCase().contains("C\u266D"))
					main.setRoot((byte)0x37);
				else
					main.setRoot((byte)0x31);
			} else if(text_ns.toUpperCase().contains("D")) {
				if(text_ns.toUpperCase().contains("D#") || text_ns.toUpperCase().contains("D\u266F"))
					main.setRoot((byte)0x42);
				else if(text_ns.contains("Db") || text_ns.toUpperCase().contains("D\u266D"))
					main.setRoot((byte)0x22);
				else
					main.setRoot((byte)0x32);
			} else if(text_ns.toUpperCase().contains("E")) {
				if(text_ns.toUpperCase().contains("E#") || text_ns.toUpperCase().contains("E\u266F"))
					main.setRoot((byte)0x34);
				else if(text_ns.contains("Eb") || text_ns.toUpperCase().contains("E\u266D"))
					main.setRoot((byte)0x23);
				else
					main.setRoot((byte)0x33);
			} else if(text_ns.toUpperCase().contains("F")) {
				if(text_ns.toUpperCase().contains("F#") || text_ns.toUpperCase().contains("F\u266F"))
					main.setRoot((byte)0x44);
				else if(text_ns.contains("Fb") || text_ns.toUpperCase().contains("F\u266D"))
					main.setRoot((byte)0x33);
				else
					main.setRoot((byte)0x34);
			} else if(text_ns.toUpperCase().contains("G")) {
				if(text_ns.toUpperCase().contains("G#") || text_ns.toUpperCase().contains("G\u266F"))
					main.setRoot((byte)0x45);
				else if(text_ns.contains("Gb") || text_ns.toUpperCase().contains("G\u266D"))
					main.setRoot((byte)0x25);
				else
					main.setRoot((byte)0x35);
			} else if(text_ns.toUpperCase().contains("A")) {
				if(text_ns.toUpperCase().contains("A#") || text_ns.toUpperCase().contains("A\u266F"))
					main.setRoot((byte)0x46);
				else if(text_ns.contains("Ab") || text_ns.toUpperCase().contains("A\u266D"))
					main.setRoot((byte)0x26);
				else
					main.setRoot((byte)0x36);
			} else if(text_ns.toUpperCase().contains("B")) {
				if(text_ns.toUpperCase().contains("B#") || text_ns.toUpperCase().contains("B\u266F"))
					main.setRoot((byte)0x31);
				else if(text_ns.contains("Bb") || text_ns.toUpperCase().contains("B\u266D"))
					main.setRoot((byte)0x27);
				else
					main.setRoot((byte)0x37);
			} else {
				main.setRoot((byte)0x7F);
				main.setChord((byte)-1);
			}
			
		}
	}

	/** Update the chord label. */
	private void updateChordLabel(JLabel label, ChordPiano graphic, final int inversion, ChordBody main, ChordBody bass) {
		String chord_text = main.getName();

		if(!bass.getNoChord()) {
			chord_text += " / " + bass.getName();
		}

		label.setText(chord_text);
		graphic.setHighlighted(main, inversion);
	}
}
