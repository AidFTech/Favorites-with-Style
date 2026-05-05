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
import song.FWSSong;
import song.FWSSongMetadata;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;

import controllers.SaveLoadController;

public class MIDIExportWindow extends JDialog {
	private static final long serialVersionUID = -6963167790070753652L;

	private File export_file = null;
	private boolean exported = false;
	
	public MIDIExportWindow(FWSEditorMainWindow parent, FWSSong song, FWSSequence sequence, MIDIExportOptions export_options, SaveLoadController save_load_controller) {
		super(parent, true);

		this.setTitle("Export MIDI Sequence");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(440, 720));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);
		
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
		
		JLabel lblLeft = new JLabel("Left");
		lblLeft.setBounds(12, 113, 60, 35);
		getContentPane().add(lblLeft);
		
		JComboBox<String> dropdown_rh = new JComboBox<>();
		dropdown_rh.setToolTipText("Select the channel to export right hand events to.");
		dropdown_rh.setBounds(66, 160, 161, 35);
		for(int i=0;i<16;i+=1)
			dropdown_rh.addItem("Channel " + (i+1));
		dropdown_rh.setSelectedIndex(export_options.export_melody_rh);
		getContentPane().add(dropdown_rh);

		JLabel lblRight = new JLabel("Right");
		lblRight.setBounds(12, 160, 60, 35);
		getContentPane().add(lblRight);

		JComboBox<String> dropdown_lh = new JComboBox<>();
		dropdown_lh.setToolTipText("Select the channel to export left hand events to.");
		dropdown_lh.setBounds(66, 113, 161, 35);
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

		MIDIExportWindow self = this;
		
		JButton button_cancel = new JButton("Cancel");
		button_cancel.setBounds(206, 673, 105, 35);
		button_cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_cancel);
		
		JButton button_export = new JButton("Export");
		button_export.setBounds(323, 673, 105, 35);
		button_export.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exported = true;

				export_options.export_melody_rh = (byte)dropdown_rh.getSelectedIndex();
				export_options.export_melody_lh = (byte)dropdown_lh.getSelectedIndex();
				
				export_options.truncate = checkbox_truncate.isSelected();
				export_options.invert = checkbox_invert_chords.isSelected();

				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_export);

		this.setVisible(true);
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
