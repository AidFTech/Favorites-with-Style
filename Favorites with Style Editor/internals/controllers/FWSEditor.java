package controllers;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import fwsevents.FWSSequence;
import main_window.FWSEditorMainWindow;
import main_window.FWSEditorMainWindow.DisplayMode;
import options.MIDIExportOptions;
import options.RecordLoadOptions;
import song.FWSSong;
import style.Style;
import tools.MIDIExportWindow;
import tools.RecordLoadWindow;

public class FWSEditor extends FWS {
	private FWSEditorMainWindow main_window;

	private FWSSequence active_sequence, scratch_sequence = new FWSSequence();
	private Style active_style = null;

	private Color[] channel_colors;

	public static void main(String args[]) {
		try {
			try {
				if (!System.getProperty("os.name").equalsIgnoreCase("LINUX")) {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				}	
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e1) {
				e1.printStackTrace();
			}
		} catch (UnsupportedLookAndFeelException e3) {
			try {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			} catch (InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException | ClassNotFoundException e2) {
				e2.printStackTrace();
			}
		}

		new FWSEditor();
	}

	public FWSEditor() {
		this.save_load_controller = new SaveLoadController(this);

		this.loaded_song = new FWSSong();
		this.active_sequence = loaded_song.getSongSequence();

		this.scratch_sequence.init();

		this.channel_colors = new Color[] {
											new Color(255, 255, 0),
      										new Color(0, 0, 255),
											new Color(255, 0, 0),
      										new Color(0, 255, 0),
     										new Color(255, 200, 0),
     										new Color(200, 0, 200),
     										new Color(255, 200, 255),
      										new Color(200, 200, 255),
      										new Color(255, 200, 200),
      										new Color(100, 100, 100),
      										new Color(100, 100, 10),
      										new Color(10, 10, 100),
      										new Color(100, 10, 40),
      										new Color(90, 0, 90),
      										new Color(0, 196, 91),
      										new Color(12, 182, 254),
										};

		this.main_window = new FWSEditorMainWindow(this);
	}

	/** Get the main window. */
	public JFrame getMainWindow() {
		return this.main_window;
	}

	/** Get the loaded song. */
	public FWSSong getLoadedSong() {
		return this.loaded_song;
	}

	/** Get the active sequence. */
	public FWSSequence getActiveSequence() {
		return this.active_sequence;
	}

	/** Get the "scratch" sequence. */
	public FWSSequence getScratchSequence() {
		return this.scratch_sequence;
	}

	/** Load a song. */
	protected void loadSong(FWSSong song) {
		this.loaded_song = song;
		this.active_sequence = loaded_song.getSongSequence();
		main_window.setDisplayMode(DisplayMode.DISPLAY_MODE_SONG);
		main_window.getViewPort().fill(this.active_sequence, true);
		main_window.refreshViewport();
	}

	/** Set the loaded song file reference. */
	public void setLoadedSongFile(File loaded_song_file) {
		super.setLoadedSongFile(loaded_song_file);
		if(loaded_song_file != null)
			main_window.setSongTitle(loaded_song_file.getName());
		else
			main_window.setSongTitle("Untitled Song");
	}

	/** Add a new style. */
	public void addStyle(Style style) {
		this.loaded_song.addStyle(style.long_name, style);
	}

	/** Load a style into the song viewport. */
	public void setStyleViewMode(Style style) {
		if(style != null) {
			this.active_sequence = style.getFullSequence();
			this.active_style = style;
			main_window.setDisplayMode(DisplayMode.DISPLAY_MODE_STYLE);
			main_window.refreshStyleDropdown(style);
			main_window.getViewPort().fill(this.active_sequence, true);
		} else {
			this.active_style = null;
			this.active_sequence = loaded_song.getSongSequence();
			main_window.setDisplayMode(DisplayMode.DISPLAY_MODE_SONG);
			main_window.getViewPort().fill(this.active_sequence, true);
		}
	}

	/** Set the view mode in the main window. */
	public void setViewMode(DisplayMode view_mode) {
		if(view_mode == DisplayMode.DISPLAY_MODE_STYLE) //This should be called from setStyleViewMode.
			return;

		if(view_mode == DisplayMode.DISPLAY_MODE_SONG)
			this.active_sequence = loaded_song.getSongSequence();
		else if(view_mode == DisplayMode.DISPLAY_MODE_MIDI)
			this.active_sequence = scratch_sequence;

		main_window.setDisplayMode(view_mode);
		main_window.getViewPort().fill(this.active_sequence, true);
	}

	/** Get the active style. */
	public Style getActiveStyle() {
		return this.active_style;
	}
	
	/** Save the active style. */
	public void saveActiveStyle() {
		if(active_sequence != null)
			this.active_style.getFromSequence(active_sequence);
	}

	/** Get the assigned channel colors. */
	public Color[] getChannelColors() {
		return this.channel_colors;
	}

	/** Handle a list of recorded events. */
	protected void handleRecordedEvents(byte[][] midi_data, long[] timestamps) {
		RecordLoadOptions options = new RecordLoadOptions();
		boolean[] channels = new boolean[16];
		for(int b=0;b<midi_data.length;b+=1) {
			if(midi_data[b].length <= 0)
				continue;

			if((midi_data[b][0]&0xF0) == 0xF0)
				continue;

			final byte channel = (byte)(midi_data[b][0]&0xF);
			if(channel >= 0 && channel < channels.length)
				channels[channel] = true;
		}

		for(int i=0;i<channels.length;i+=1) {
			if(channels[i])
				options.channel_map[i] = (byte)i;
			else
				options.channel_map[i] = -1;
		}

		if(main_window.getDisplayMode() == DisplayMode.DISPLAY_MODE_STYLE) {
			RecordLoadWindow rec_window = new RecordLoadWindow(main_window, active_sequence, options, active_style);
			if(!rec_window.getSaved())
				return;

			if(options.section.isEmpty()) {
				active_sequence.addEventsFromInput(midi_data, timestamps, options);
				main_window.getViewPort().fill(active_sequence, true);
			} else {
				Style temp_style = new Style();
				temp_style.getFromSequence(active_sequence);
				
				FWSSequence section = temp_style.getSection(options.section);
				if(section == null)
					return;

				section.addEventsFromInput(midi_data, timestamps, options);

				active_sequence = temp_style.getFullSequence();
				main_window.getViewPort().fill(active_sequence, true);
			}
		} else {
			RecordLoadWindow rec_window = new RecordLoadWindow(main_window, active_sequence, options, null);
			if(rec_window.getSaved()) {
				active_sequence.addEventsFromInput(midi_data, timestamps, options);
				main_window.getViewPort().fill(this.active_sequence, true);
			}
		}
	}
	
	/** Import MIDI data into a song. */
	public void importMidi() {
		File midi_file = save_load_controller.openMidiImportChooser();
		if(midi_file == null)
			return;

		Sequence midi_sequence = null;
		try {
			midi_sequence = MidiSystem.getSequence(midi_file);
		} catch (InvalidMidiDataException | IOException e) {
			//Error message.
			return;
		}

		ArrayList<byte[]> midi_data_v = new ArrayList<>();
		ArrayList<Long> timestamps_v = new ArrayList<>();

		MIDIManager.getMidiBytes(midi_sequence, midi_data_v, timestamps_v);

		if(midi_data_v.size() != timestamps_v.size())
			return;

		byte[][] midi_data = new byte[midi_data_v.size()][];
		long[] timestamps = new long[timestamps_v.size()];

		final int seq_tpq = midi_sequence.getResolution(), tpq = active_sequence.getTPQ();

		midi_data_v.toArray(midi_data);
		for(int i=0;i<timestamps_v.size();i+=1)
			timestamps[i] = timestamps_v.get(i).longValue()*tpq/seq_tpq;

		RecordLoadOptions options = new RecordLoadOptions();
		options.live_import = false;
		options.midi_clock = false;
		boolean[] channels = new boolean[16];
		for(int b=0;b<midi_data.length;b+=1) {
			if(midi_data[b].length <= 0)
				continue;

			if((midi_data[b][0]&0xF0) == 0xF0)
				continue;

			final byte channel = (byte)(midi_data[b][0]&0xF);
			if(channel >= 0 && channel < channels.length)
				channels[channel] = true;
		}

		for(int i=0;i<channels.length;i+=1) {
			if(channels[i])
				options.channel_map[i] = (byte)i;
			else
				options.channel_map[i] = -1;
		}

		if(main_window.getDisplayMode() == DisplayMode.DISPLAY_MODE_STYLE) {
			RecordLoadWindow rec_window = new RecordLoadWindow(main_window, active_sequence, options, active_style);
			if(!rec_window.getSaved())
				return;

			if(options.section.isEmpty()) {
				active_sequence.addEventsFromInput(midi_data, timestamps, options);
				main_window.getViewPort().fill(active_sequence, true);
			} else {
				Style temp_style = new Style();
				temp_style.getFromSequence(active_sequence);
				
				FWSSequence section = temp_style.getSection(options.section);
				if(section == null)
					return;

				section.addEventsFromInput(midi_data, timestamps, options);

				active_sequence = temp_style.getFullSequence();
				main_window.getViewPort().fill(active_sequence, true);
			}
		} else {
			RecordLoadWindow rec_window = new RecordLoadWindow(main_window, active_sequence, options, null);
			if(rec_window.getSaved()) {
				active_sequence.addEventsFromInput(midi_data, timestamps, options);
				main_window.getViewPort().fill(this.active_sequence, true);
			}
		}
	}

	/** Export MIDI data from a song. */
	public void exportMidi() {
		if(main_window.getDisplayMode() == DisplayMode.DISPLAY_MODE_STYLE)
			return;

		if(main_window.getDisplayMode() == DisplayMode.DISPLAY_MODE_SONG) {
			try {
				MIDIExportOptions export_options = new MIDIExportOptions();
				export_options.export_melody_lh = midi_manager.getPlayerOptions().export_melody_lh;
				export_options.export_melody_rh = midi_manager.getPlayerOptions().export_melody_rh;

				byte[] default_black_chords = midi_manager.getPlayerOptions().black_chord_display, song_black_chords = export_options.black_chord_display;
				for(int i=0;i<default_black_chords.length;i+=1)
					song_black_chords[i] = default_black_chords[i];

				MIDIExportWindow export_window = new MIDIExportWindow(main_window, loaded_song, null, export_options, save_load_controller);
				if(!export_window.getExported())
					return;
				if(export_window.getExportFile() == null)
					return;

				Sequence midi_sequence = midi_manager.getSongMIDISequence(loaded_song, export_options);
				MidiSystem.write(midi_sequence, 0, export_window.getExportFile());
			} catch (InvalidMidiDataException e) {
				
			} catch (IOException e) {
				
			}
		}
	}

	/** Import legacy FWS data. */
	public void importLegacyFWS() {
		File fws_file = save_load_controller.openLegacyFWSImportChooser();
		if(fws_file == null)
			return;

		FWSSong song = FWSSong.loadFromLegacyFWS(fws_file);
		if(song == null)
			return;

		setViewMode(DisplayMode.DISPLAY_MODE_SONG);
		this.loaded_song = song;
		this.active_sequence = song.getSongSequence();

		this.setLoadedSongFile(null);

		main_window.refreshViewport();
	}

	/** Save a song file. */
	public void saveSong() {
		if(loaded_song_file != null)
			save_load_controller.saveSong(loaded_song_file, loaded_song);
		else
			save_load_controller.saveSong(loaded_song);
	}

	/** Save a song file. */
	public void saveSongAs() {
		save_load_controller.saveSong(loaded_song);
	}

	/** Save a style file. */
	public void saveStyle(Style style) {
		save_load_controller.openStyleSaveChooser(style.short_name, style.getStyleFileBinary());
	}

	/** Load a style into a song. Returns whether successful. */
	public boolean loadStyle() {
		File style_file = save_load_controller.openStyleLoadChooser();
		if(style_file == null)
			return false;

		Style new_style = Style.getStyleFromFile(style_file, main_window);
		if(new_style != null) {
			loaded_song.addStyle(new_style.long_name, new_style);
			return true;
		}

		return false;
	}
}
