package controllers;

import java.io.File;

import javax.swing.JFrame;

import options.GlobalOptions;
import song.FWSSong;
import voices.Voice;

public abstract class FWS {
	public static final int event_height = 15; //The height of each event in piano roll view.
	public static final float std_tempo = 120; //The reference tempo.
	public static final int default_tpq = 192;
	public static final int max_byte_size = 128;

	protected SaveLoadController save_load_controller;
	protected FWSSong loaded_song;
	protected File loaded_song_file = null;

	private Voice[] active_voice_list;

	protected MIDIManager midi_manager;

	protected GlobalOptions global_options = new GlobalOptions();

	public FWS() {
		active_voice_list = Voice.getGMVoices();
		midi_manager = new MIDIManager(this);
	}

	/** Load a song. */
	public void loadSong() {
		FWSSong new_song = save_load_controller.loadSong();
		if(new_song == null)
			return;

		loadSong(new_song);
	}

	/** Load a song. */
	protected void loadSong(FWSSong song) {
		//Called by children.
	}

	/** Generic main window getter. */
	public JFrame getMainWindow() {
		return null;
	}

	/** Get the MIDI manager. */
	public MIDIManager getMidiManager() {
		return this.midi_manager;
	}

	/** Get the loaded song file reference. */
	public File getLoadedSongFile() {
		return this.loaded_song_file;
	}

	/** Set the loaded song file reference. */
	public void setLoadedSongFile(File loaded_song_file) {
		this.loaded_song_file = loaded_song_file;
	}

	/** Get the active voice list. */
	public Voice[] getVoiceList() {
		return this.active_voice_list;
	}

	/** Get the global option list. */
	public GlobalOptions getGlobalOptions() {
		return this.global_options;
	}

	/** Handle a list of recorded events. */
	protected void handleRecordedEvents(byte[][] midi_data, long[] timestamps) {
		//Called by children.
	}
}