package tools;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JDialog;

import fwsevents.FWSSequence;
import main_window.FWSEditorMainWindow;
import options.MIDIExportOptions;
import options.MIDIPlayerOptions;
import song.FWSSong;
import song.FWSSongMetadata;
import voices.InstrumentProfile;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;

import controllers.FWSEditor;
import controllers.SaveLoadController;
import javax.swing.SwingConstants;
import javax.swing.DefaultComboBoxModel;

public class MIDIExportWindow extends JDialog {
	private static final long serialVersionUID = -6963167790070753652L;

	private File export_file = null;
	private boolean exported = false;

	private boolean dropdown_listen = true;
	
	public MIDIExportWindow(FWSEditorMainWindow parent, FWSSong song, FWSSequence sequence, MIDIExportOptions export_options, SaveLoadController save_load_controller) {
		super(parent, true);

		this.setTitle("Export MIDI Sequence");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(490, 720));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);

		FWSEditor controller = parent.getController();
		
		JButton button_choose_file = new JButton("Choose File");
		button_choose_file.setBounds(12, 12, 110, 35);
		getContentPane().add(button_choose_file);
		
		JLabel label_filename = new JLabel("No File Selected");
		label_filename.setBounds(140, 12, 274, 35);
		getContentPane().add(label_filename);

		button_choose_file.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				export_file = save_load_controller.openMidiExportChooser();
				if(export_file == null)
					label_filename.setText("No File Selected");
				else
					label_filename.setText(export_file.getName());
			}
		});
		
		JLabel label_melody_channels = new JLabel("Melody Channels");
		label_melody_channels.setBounds(12, 72, 133, 35);
		getContentPane().add(label_melody_channels);
		
		JLabel label_left = new JLabel("Left");
		label_left.setBounds(12, 113, 60, 35);
		getContentPane().add(label_left);
		
		JComboBox<String> dropdown_rh = new JComboBox<>();
		dropdown_rh.setToolTipText("Select the channel to export right hand events to.");
		dropdown_rh.setBounds(66, 160, 133, 35);
		for(int i=0;i<16;i+=1)
			dropdown_rh.addItem("Channel " + (i+1));
		dropdown_rh.setSelectedIndex(export_options.export_melody_rh);
		getContentPane().add(dropdown_rh);

		JLabel label_right = new JLabel("Right");
		label_right.setBounds(12, 160, 60, 35);
		getContentPane().add(label_right);

		JComboBox<String> dropdown_lh = new JComboBox<>();
		dropdown_lh.setToolTipText("Select the channel to export left hand events to.");
		dropdown_lh.setBounds(66, 113, 133, 35);
		for(int i=0;i<16;i+=1)
			dropdown_lh.addItem("Channel " + (i+1));
		dropdown_lh.setSelectedIndex(export_options.export_melody_lh);
		getContentPane().add(dropdown_lh);
		
		JCheckBox checkbox_truncate = new JCheckBox("Truncate Melody Notes");
		checkbox_truncate.setToolTipText("Check to shorten melody notes to make keyboard display more natural.");
		checkbox_truncate.setBounds(12, 218, 180, 35);
		checkbox_truncate.setSelected(export_options.truncate);
		getContentPane().add(checkbox_truncate);
		
		JCheckBox checkbox_print_chords = new JCheckBox("Print Chords");
		checkbox_print_chords.setToolTipText("Check to print chords to the instrument display if supported.");
		checkbox_print_chords.setBounds(12, 250, 161, 35);
		checkbox_print_chords.setSelected(export_options.export_chords);

		if(song != null)
			getContentPane().add(checkbox_print_chords);

		JCheckBox checkbox_invert_chords = new JCheckBox("Invert Chords");
		checkbox_invert_chords.setToolTipText("Check to display chord inversions on the instrument display. Uncheck for the uninverted chord display of a Yamaha or Casio A² instrument.");
		checkbox_invert_chords.setBounds(12, 280, 161, 35);
		checkbox_invert_chords.setSelected(export_options.invert);
		
		if(song != null) {
			FWSSongMetadata metadata = song.getSongMetadata();

			if(metadata.chord_channel == metadata.melody_rh_channel || metadata.chord_channel == metadata.melody_lh_channel)
				getContentPane().add(checkbox_invert_chords);
		}
		
		JLabel label_chord_names = new JLabel("Chord Names");
		label_chord_names.setBounds(12, 348, 133, 35);
		getContentPane().add(label_chord_names);
		
		JRadioButton radiobutton_aswritten = new JRadioButton("As Written");
		radiobutton_aswritten.setToolTipText("Display the chord names as written in the song.");
		radiobutton_aswritten.setBounds(12, 391, 130, 35);
		getContentPane().add(radiobutton_aswritten);
		
		JRadioButton radiobutton_sharp = new JRadioButton("All Sharp");
		radiobutton_sharp.setToolTipText("Display the chord names as sharp.");
		radiobutton_sharp.setBounds(12, 430, 130, 35);
		getContentPane().add(radiobutton_sharp);
		
		JRadioButton radiobutton_flat = new JRadioButton("All Flat");
		radiobutton_flat.setToolTipText("Display the chord names as flat.");
		radiobutton_flat.setBounds(12, 469, 130, 35);
		getContentPane().add(radiobutton_flat);
		
		JRadioButton radiobutton_casio = new JRadioButton("Casio Convention");
		radiobutton_casio.setToolTipText("Display the chord names as they would be displayed on a Casio instrument.");
		radiobutton_casio.setBounds(12, 508, 161, 35);
		getContentPane().add(radiobutton_casio);
		
		JRadioButton radiobutton_yamaha = new JRadioButton("Yamaha Convention");
		radiobutton_yamaha.setToolTipText("Display the chord names as they would be displayed on a Yamaha instrument.");
		radiobutton_yamaha.setBounds(12, 547, 161, 35);
		getContentPane().add(radiobutton_yamaha);
		
		JRadioButton radiobutton_custom = new JRadioButton("Custom");
		radiobutton_custom.setToolTipText("Define custom chord display mapping.");
		radiobutton_custom.setBounds(12, 586, 161, 35);
		getContentPane().add(radiobutton_custom);

		ButtonGroup chord_display_group = new ButtonGroup();
		chord_display_group.add(radiobutton_aswritten);
		chord_display_group.add(radiobutton_casio);
		chord_display_group.add(radiobutton_custom);
		chord_display_group.add(radiobutton_flat);
		chord_display_group.add(radiobutton_sharp);
		chord_display_group.add(radiobutton_yamaha);

		JLabel label_instrument_profile = new JLabel("Instrument Profile");
		label_instrument_profile.setBounds(284, 72, 161, 35);
		getContentPane().add(label_instrument_profile);

		JComboBox<String> dropdown_profile = new JComboBox<>();
		dropdown_profile.setToolTipText("Select the family of the target instrument for the export.");
		dropdown_profile.setBounds(305, 113, 161, 35);
		getContentPane().add(dropdown_profile);
		
		JLabel label_family = new JLabel("Family");
		label_family.setHorizontalAlignment(SwingConstants.RIGHT);
		label_family.setBounds(217, 113, 70, 35);
		getContentPane().add(label_family);
		
		JComboBox<String> dropdown_instrument = new JComboBox<>();
		dropdown_instrument.setToolTipText("Select the model of the target instrument.");
		dropdown_instrument.setBounds(305, 160, 161, 35);
		getContentPane().add(dropdown_instrument);
		
		JLabel label_instrument = new JLabel("Instrument");
		label_instrument.setHorizontalAlignment(SwingConstants.RIGHT);
		label_instrument.setBounds(206, 160, 81, 35);
		getContentPane().add(label_instrument);

		dropdown_profile.addItem("Same as Voice List");

		String[] profiles = controller.getInstrumentProfileList();
		for(String profile: profiles) {
			dropdown_profile.addItem(profile);
		}
		
		dropdown_profile.setSelectedIndex(0);
		if(!controller.getOutputProfileName().trim().isEmpty())
			dropdown_profile.setSelectedItem(controller.getOutputProfileName());

		refreshInstrumentMenu(dropdown_instrument, controller.getOutputProfile());

		dropdown_profile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(dropdown_profile.getSelectedIndex() <= 0)
					refreshInstrumentMenu(dropdown_instrument, null);
				else
					refreshInstrumentMenu(dropdown_instrument, controller.getInstrumentProfile((String)dropdown_profile.getSelectedItem()));
			}
		});
		
		dropdown_instrument.setSelectedIndex(0);
		if(!controller.getOutputInstrumentName().trim().isEmpty())
			dropdown_instrument.setSelectedItem(controller.getOutputInstrumentName());

		JLabel label_cd = new JLabel("C♯/D♭");
		label_cd.setHorizontalAlignment(SwingConstants.RIGHT);
		label_cd.setBounds(227, 338, 60, 35);
		getContentPane().add(label_cd);
		
		JComboBox<String> dropdown_cd = new JComboBox<>();
		dropdown_cd.setModel(new DefaultComboBoxModel<String>(new String[] {"As Written", "D♭", "C♯"}));
		dropdown_cd.setToolTipText("Define how to display C♯/D♭ chords.");
		dropdown_cd.setSelectedIndex(export_options.black_chord_display[0]);
		dropdown_cd.setBounds(305, 338, 109, 35);
		getContentPane().add(dropdown_cd);

		JLabel label_de = new JLabel("D♯/E♭");
		label_de.setHorizontalAlignment(SwingConstants.RIGHT);
		label_de.setBounds(227, 385, 60, 35);
		getContentPane().add(label_de);
		
		JComboBox<String> dropdown_de = new JComboBox<>();
		dropdown_de.setModel(new DefaultComboBoxModel<String>(new String[] {"As Written", "E♭", "D♯"}));
		dropdown_de.setToolTipText("Define how to display D♯/E♭ chords.");
		dropdown_de.setSelectedIndex(export_options.black_chord_display[1]);
		dropdown_de.setBounds(305, 385, 109, 35);
		getContentPane().add(dropdown_de);

		JLabel label_fg = new JLabel("F♯/G♭");
		label_fg.setHorizontalAlignment(SwingConstants.RIGHT);
		label_fg.setBounds(227, 432, 60, 35);
		getContentPane().add(label_fg);
		
		JComboBox<String> dropdown_fg = new JComboBox<>();
		dropdown_fg.setModel(new DefaultComboBoxModel<String>(new String[] {"As Written", "G♭", "F♯"}));
		dropdown_fg.setToolTipText("Define how to display F♯/G♭ chords.");
		dropdown_fg.setSelectedIndex(export_options.black_chord_display[2]);
		dropdown_fg.setBounds(305, 432, 109, 35);
		getContentPane().add(dropdown_fg);

		JLabel label_ga = new JLabel("G♯/A♭");
		label_ga.setHorizontalAlignment(SwingConstants.RIGHT);
		label_ga.setBounds(227, 479, 60, 35);
		getContentPane().add(label_ga);
		
		JComboBox<String> dropdown_ga = new JComboBox<>();
		dropdown_ga.setModel(new DefaultComboBoxModel<String>(new String[] {"As Written", "A♭", "G♯"}));
		dropdown_ga.setToolTipText("Define how to display G♯/A♭ chords.");
		dropdown_ga.setSelectedIndex(export_options.black_chord_display[3]);
		dropdown_ga.setBounds(305, 479, 109, 35);
		getContentPane().add(dropdown_ga);

		JLabel label_ab = new JLabel("A♯/B♭");
		label_ab.setHorizontalAlignment(SwingConstants.RIGHT);
		label_ab.setBounds(227, 526, 60, 35);
		getContentPane().add(label_ab);
		
		JComboBox<String> dropdown_ab = new JComboBox<>();
		dropdown_ab.setModel(new DefaultComboBoxModel<String>(new String[] {"As Written", "B♭", "A♯"}));
		dropdown_ab.setToolTipText("Define how to display A♯/B♭ chords.");
		dropdown_ab.setSelectedIndex(export_options.black_chord_display[4]);
		dropdown_ab.setBounds(305, 526, 109, 35);
		getContentPane().add(dropdown_ab);

		//Determine how chords are configured. 
		do {
			//As written?
			byte[] black_keys = export_options.black_chord_display;
			boolean as_written = true;

			for(int i=0;i<black_keys.length;i+=1) {
				if(black_keys[i] != MIDIPlayerOptions.CHORD_AS_IS) {
					as_written = false;
					break;
				}
			}

			if(as_written) {
				radiobutton_aswritten.setSelected(true);
				break;
			}

			//All sharp?
			boolean sharp = true;
			for(int i=0;i<black_keys.length;i+=1) {
				if(black_keys[i] != MIDIPlayerOptions.CHORD_AS_SHARP) {
					sharp = false;
					break;
				}
			}

			if(sharp) {
				radiobutton_sharp.setSelected(true);
				break;
			}

			//All flat?
			boolean flat = true;
			for(int i=0;i<black_keys.length;i+=1) {
				if(black_keys[i] != MIDIPlayerOptions.CHORD_AS_FLAT) {
					flat = false;
					break;
				}
			}

			if(flat) {
				radiobutton_flat.setSelected(true);
				break;
			}

			//Casio?
			boolean casio = true;
			for(int i=0;i<black_keys.length;i+=1) {
				byte value = MIDIPlayerOptions.CHORD_AS_IS;
				switch(i) {
				case 0:
				case 2:
					value = MIDIPlayerOptions.CHORD_AS_SHARP;
					break;
				case 1:
				case 3:
				case 4:
					value = MIDIPlayerOptions.CHORD_AS_FLAT;
					break;
				default:
					continue;
				}

				if(black_keys[i] != value) {
					casio = false;
					break;
				}
			}

			if(casio) {
				radiobutton_casio.setSelected(true);
				break;
			}

			//Yamaha?
			boolean yamaha = true;
			for(int i=0;i<black_keys.length;i+=1) {
				byte value = MIDIPlayerOptions.CHORD_AS_IS;
				switch(i) {
				case 2:
				case 3:
					value = MIDIPlayerOptions.CHORD_AS_SHARP;
					break;
				case 0:
				case 1:
				case 4:
					value = MIDIPlayerOptions.CHORD_AS_FLAT;
					break;
				default:
					continue;
				}

				if(black_keys[i] != value) {
					yamaha = false;
					break;
				}
			}

			if(yamaha) {
				radiobutton_yamaha.setSelected(true);
				break;
			}

			//Something else.
			radiobutton_custom.setSelected(true);
		} while(false);

		//Set chords to as-written.
		radiobutton_aswritten.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dropdown_listen = false;
				dropdown_cd.setSelectedIndex(MIDIPlayerOptions.CHORD_AS_IS);
				dropdown_de.setSelectedIndex(MIDIPlayerOptions.CHORD_AS_IS);
				dropdown_fg.setSelectedIndex(MIDIPlayerOptions.CHORD_AS_IS);
				dropdown_ga.setSelectedIndex(MIDIPlayerOptions.CHORD_AS_IS);
				dropdown_ab.setSelectedIndex(MIDIPlayerOptions.CHORD_AS_IS);
				dropdown_listen = true;
			}
		});

		//Set chords to sharp.
		radiobutton_sharp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dropdown_listen = false;
				dropdown_cd.setSelectedIndex(MIDIPlayerOptions.CHORD_AS_SHARP);
				dropdown_de.setSelectedIndex(MIDIPlayerOptions.CHORD_AS_SHARP);
				dropdown_fg.setSelectedIndex(MIDIPlayerOptions.CHORD_AS_SHARP);
				dropdown_ga.setSelectedIndex(MIDIPlayerOptions.CHORD_AS_SHARP);
				dropdown_ab.setSelectedIndex(MIDIPlayerOptions.CHORD_AS_SHARP);
				dropdown_listen = true;
			}
		});

		//Set chords to flat.
		radiobutton_flat.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dropdown_listen = false;
				dropdown_cd.setSelectedIndex(MIDIPlayerOptions.CHORD_AS_FLAT);
				dropdown_de.setSelectedIndex(MIDIPlayerOptions.CHORD_AS_FLAT);
				dropdown_fg.setSelectedIndex(MIDIPlayerOptions.CHORD_AS_FLAT);
				dropdown_ga.setSelectedIndex(MIDIPlayerOptions.CHORD_AS_FLAT);
				dropdown_ab.setSelectedIndex(MIDIPlayerOptions.CHORD_AS_FLAT);
				dropdown_listen = true;
			}
		});

		//Set chords to Casio.
		radiobutton_casio.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dropdown_listen = false;
				dropdown_cd.setSelectedIndex(MIDIPlayerOptions.CHORD_AS_SHARP);
				dropdown_de.setSelectedIndex(MIDIPlayerOptions.CHORD_AS_FLAT);
				dropdown_fg.setSelectedIndex(MIDIPlayerOptions.CHORD_AS_SHARP);
				dropdown_ga.setSelectedIndex(MIDIPlayerOptions.CHORD_AS_FLAT);
				dropdown_ab.setSelectedIndex(MIDIPlayerOptions.CHORD_AS_FLAT);
				dropdown_listen = true;
			}
		});

		//Set chords to Yamaha.
		radiobutton_yamaha.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dropdown_listen = false;
				dropdown_cd.setSelectedIndex(MIDIPlayerOptions.CHORD_AS_FLAT);
				dropdown_de.setSelectedIndex(MIDIPlayerOptions.CHORD_AS_FLAT);
				dropdown_fg.setSelectedIndex(MIDIPlayerOptions.CHORD_AS_SHARP);
				dropdown_ga.setSelectedIndex(MIDIPlayerOptions.CHORD_AS_SHARP);
				dropdown_ab.setSelectedIndex(MIDIPlayerOptions.CHORD_AS_FLAT);
				dropdown_listen = true;
			}
		});

		//Change to custom.
		dropdown_cd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!dropdown_listen)
					return;

				radiobutton_custom.setSelected(true);
			}
		});

		dropdown_de.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!dropdown_listen)
					return;

				radiobutton_custom.setSelected(true);
			}
		});

		dropdown_fg.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!dropdown_listen)
					return;

				radiobutton_custom.setSelected(true);
			}
		});

		dropdown_ga.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!dropdown_listen)
					return;

				radiobutton_custom.setSelected(true);
			}
		});

		dropdown_ab.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!dropdown_listen)
					return;

				radiobutton_custom.setSelected(true);
			}
		});

		MIDIExportWindow self = this;
		
		JButton button_cancel = new JButton("Cancel");
		button_cancel.setBounds(256, 673, 105, 35);
		button_cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_cancel);
		
		JButton button_export = new JButton("Export");
		button_export.setBounds(373, 673, 105, 35);
		button_export.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exported = true;

				export_options.export_melody_rh = (byte)dropdown_rh.getSelectedIndex();
				export_options.export_melody_lh = (byte)dropdown_lh.getSelectedIndex();
				
				export_options.truncate = checkbox_truncate.isSelected();
				export_options.invert = checkbox_invert_chords.isSelected();

				if(dropdown_profile.getSelectedIndex() > 0)
					export_options.profile = (String)dropdown_profile.getSelectedItem();
				else
					export_options.profile = "";

				if(dropdown_instrument.getSelectedIndex() > 0)
					export_options.instrument = (String)dropdown_instrument.getSelectedItem();
				else
					export_options.instrument = "";

				export_options.black_chord_display[0] = (byte)dropdown_cd.getSelectedIndex();
				export_options.black_chord_display[1] = (byte)dropdown_de.getSelectedIndex();
				export_options.black_chord_display[2] = (byte)dropdown_fg.getSelectedIndex();
				export_options.black_chord_display[3] = (byte)dropdown_ga.getSelectedIndex();
				export_options.black_chord_display[4] = (byte)dropdown_ab.getSelectedIndex();

				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_export);

		this.setVisible(true);
	}

	/** Refresh the instrument menu. */
	private void refreshInstrumentMenu(JComboBox<String> instrument_dropdown, InstrumentProfile target_profile) {
		instrument_dropdown.removeAllItems();

		instrument_dropdown.addItem("");
		if(target_profile == null)
			return;

		String[] instruments = target_profile.getInstrumentNames();
		for(String instrument: instruments) {
			instrument_dropdown.addItem(instrument);
		}
	}

	/** Get the selected file to export. */
	public File getExportFile() {
		return this.export_file;
	}

	/** Get whether the user clicked Export. */
	public boolean getExported() {
		return exported;
	}
}
