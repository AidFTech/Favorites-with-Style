package settings_dialogs;

import javax.swing.JDialog;

import main_window.FWSEditorMainWindow;
import options.MIDIPlayerOptions;
import song.FWSSongMetadata;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import controllers.MIDIManager;

import javax.swing.JTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;

public class SongPropertiesWindow extends JDialog {
	private static final long serialVersionUID = -3952506437951462952L;

	public SongPropertiesWindow(FWSEditorMainWindow parent, FWSSongMetadata song_metadata) {
		super(parent, true);

		this.setTitle("Song Properties");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(500, 400));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);
		
		JLabel label_long_title = new JLabel("Long Title");
		label_long_title.setHorizontalAlignment(SwingConstants.RIGHT);
		label_long_title.setBounds(12, 12, 100, 35);
		getContentPane().add(label_long_title);
		
		JTextField field_long_title = new JTextField();
		field_long_title.setColumns(10);
		field_long_title.setToolTipText("The long title of the song.");
		field_long_title.setBounds(130, 12, 344, 35);
		field_long_title.setText(song_metadata.long_title);
		getContentPane().add(field_long_title);
		
		JLabel label_short_title = new JLabel("Short Title");
		label_short_title.setHorizontalAlignment(SwingConstants.RIGHT);
		label_short_title.setBounds(12, 59, 100, 35);
		getContentPane().add(label_short_title);
		
		JTextField field_short_title = new JTextField();
		field_short_title.setToolTipText("The shorter title of the song to be displayed on the keyboard LCD.");
		field_short_title.setColumns(10);
		field_short_title.setBounds(130, 59, 170, 35);
		field_short_title.setText(song_metadata.short_title);
		getContentPane().add(field_short_title);
		
		JLabel label_composer = new JLabel("Composer");
		label_composer.setHorizontalAlignment(SwingConstants.RIGHT);
		label_composer.setBounds(12, 106, 100, 35);
		getContentPane().add(label_composer);
		
		JTextField field_composer = new JTextField();
		field_composer.setToolTipText("The song composer.");
		field_composer.setColumns(10);
		field_composer.setBounds(130, 106, 344, 35);
		field_composer.setText(song_metadata.composer);
		getContentPane().add(field_composer);
		
		JLabel label_split = new JLabel("Split Point");
		label_split.setHorizontalAlignment(SwingConstants.RIGHT);
		label_split.setBounds(270, 153, 89, 35);
		getContentPane().add(label_split);
		
		JSpinner spinner_split = new JSpinner();
		spinner_split.setModel(new SpinnerNumberModel(song_metadata.split_point, 0, 127, 1));
		spinner_split.setToolTipText("Set the chord/melody split point.");
		spinner_split.setBounds(377, 153, 66, 35);
		getContentPane().add(spinner_split);
		
		JLabel label_chord_channel = new JLabel("Chord Channel");
		label_chord_channel.setHorizontalAlignment(SwingConstants.RIGHT);
		label_chord_channel.setBounds(12, 153, 100, 35);
		getContentPane().add(label_chord_channel);
		
		JComboBox<String> dropdown_chord_channel = new JComboBox<String>();
		dropdown_chord_channel.setBounds(130, 153, 122, 35);
		dropdown_chord_channel.setToolTipText("Set the channel on which to write auto accompaniment chord events.");
		dropdown_chord_channel.addItem("Chords Off");
		for(int i=0;i<16;i+=1)
			dropdown_chord_channel.addItem("Channel " + (i+1));
		dropdown_chord_channel.setSelectedIndex(song_metadata.chord_channel >= -1 ? song_metadata.chord_channel + 1 : -1);
		getContentPane().add(dropdown_chord_channel);
		
		JLabel label_rh_melody_channel = new JLabel("RH Melody Channel");
		label_rh_melody_channel.setHorizontalAlignment(SwingConstants.RIGHT);
		label_rh_melody_channel.setBounds(12, 200, 132, 35);
		getContentPane().add(label_rh_melody_channel);
		
		JComboBox<String> dropdown_rh_melody_channel = new JComboBox<String>();
		dropdown_rh_melody_channel.setToolTipText("Set the right-hand melody channel.");
		dropdown_rh_melody_channel.setBounds(162, 200, 122, 35);
		getContentPane().add(dropdown_rh_melody_channel);
		
		JLabel label_lh_melody_channel = new JLabel("LH Melody Channel");
		label_lh_melody_channel.setHorizontalAlignment(SwingConstants.RIGHT);
		label_lh_melody_channel.setBounds(12, 247, 132, 35);
		getContentPane().add(label_lh_melody_channel);
		
		JComboBox<String> dropdown_lh_melody_channel = new JComboBox<String>();
		dropdown_lh_melody_channel.setToolTipText("Set the left-hand melody channel.");
		dropdown_lh_melody_channel.setBounds(162, 247, 122, 35);
		getContentPane().add(dropdown_lh_melody_channel);

		dropdown_rh_melody_channel.addItem("RH Melody Off");
		dropdown_lh_melody_channel.addItem("LH Melody Off");

		for(int i=0;i<16;i+=1) {
			dropdown_rh_melody_channel.addItem("Channel " + (i+1));
			dropdown_lh_melody_channel.addItem("Channel " + (i+1));
		}
		dropdown_rh_melody_channel.setSelectedIndex(song_metadata.melody_rh_channel >= -1 ? song_metadata.melody_rh_channel + 1 : -1);
		dropdown_lh_melody_channel.setSelectedIndex(song_metadata.melody_lh_channel >= -1 ? song_metadata.melody_lh_channel + 1 : -1);

		SongPropertiesWindow self = this;

		JButton button_cancel = new JButton("Cancel");
		button_cancel.setBounds(254, 316, 105, 35);
		button_cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_cancel);

		JButton button_apply = new JButton("Apply");
		button_apply.setBounds(369, 316, 105, 35);
		button_apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				song_metadata.long_title = field_long_title.getText();
				song_metadata.short_title = field_short_title.getText();
				song_metadata.composer = field_composer.getText();
				song_metadata.split_point = ((Integer)spinner_split.getValue()).byteValue();
				
				final int chord_index = dropdown_chord_channel.getSelectedIndex(), rh_index = dropdown_rh_melody_channel.getSelectedIndex(), lh_index = dropdown_lh_melody_channel.getSelectedIndex();

				song_metadata.chord_channel = chord_index >= 0 ? (byte)(chord_index-1) : -1;
				song_metadata.melody_rh_channel = (byte)(rh_index - 1);
				song_metadata.melody_lh_channel = (byte)(lh_index - 1);

				MIDIManager mcontroller =  parent.getController().getMidiManager();
				MIDIPlayerOptions moptions = mcontroller.getPlayerOptions();
				
				moptions.song_melody_rh = song_metadata.melody_rh_channel;
				moptions.song_melody_lh = song_metadata.melody_lh_channel;

				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_apply);

		this.setVisible(true);
	}
}
