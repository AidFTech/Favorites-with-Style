package controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JFrame;

import options.GlobalOptions;
import song.FWSSong;
import voices.InstrumentProfile;
import voices.Voice;

public abstract class FWS {
	public static final int event_height = 15; //The height of each event in piano roll view.
	public static final float std_tempo = 120; //The reference tempo.
	public static final int default_tpq = 192;
	public static final int max_byte_size = 128;

	protected SaveLoadController save_load_controller;
	protected FWSSong loaded_song;
	protected File loaded_song_file = null;
	protected MIDIManager midi_manager;
	protected GlobalOptions global_options = new GlobalOptions();

	protected Map<String, InstrumentProfile> instrument_profiles = new HashMap<>();
	protected InstrumentProfile active_profile = null;
	protected String active_instrument = "";

	private Voice[] active_voice_list;

	public FWS() {
		save_load_controller = new SaveLoadController(this);
		active_voice_list = Voice.getGMVoices();
		midi_manager = new MIDIManager(this);

		loaded_song = new FWSSong();

		save_load_controller.initFWS();
	}

	/** Load a song. */
	public void loadSong() {
		FWSSong new_song = save_load_controller.loadSong();
		if(new_song == null)
			return;

		midi_manager.getPlayerOptions().song_melody_rh = new_song.getSongMetadata().melody_rh_channel;
		midi_manager.getPlayerOptions().song_melody_lh = new_song.getSongMetadata().melody_lh_channel;

		loadSong(new_song);
	}

	/** Get the active instrument profile name. */
	public String getInstrumentProfileName() {
		if(this.active_profile != null)
			return this.active_profile.getInstrumentFamily();
		else
			return "";
	}

	/** Get the name of the selected instrument. */
	public String getInstrumentName() {
		return this.active_instrument;
	}

	/** Get the instrument profile. */
	public InstrumentProfile getInstrumentProfile(String family) {
		return this.instrument_profiles.get(family);
	}

	/** Get the list of instrument families. */
	public String[] getInstrumentProfileList() {
		ArrayList<String> instrument_names = new ArrayList<>();
		for(Entry<String, InstrumentProfile>instrument : instrument_profiles.entrySet()) {
			instrument_names.add(instrument.getKey());
		}

		String[] instrument_name_array = new String[instrument_names.size()];
		instrument_names.toArray(instrument_name_array);
		return instrument_name_array;
	}

	/** Set the instrument family. */
	public void setInstrumentFamily(final String family) {
		active_profile = this.instrument_profiles.get(family);
		active_voice_list = Voice.getGMVoices();
		active_instrument = "";
	}

	/** Set a new voice list from a specific instrument. If the string is blank, set to GM voices. */
	public void setVoiceList(final String instrument) {
		if(!instrument.isBlank()) {
			if(active_profile == null) {
				active_voice_list = Voice.getGMVoices();
				return;
			}

			Voice[] voice_list = active_profile.getVoiceList(instrument);
			if(voice_list == null) {
				active_voice_list = Voice.getGMVoices();
				return;
			} 

			active_voice_list = voice_list;
			active_instrument = instrument;
		} else
			active_voice_list = Voice.getGMVoices();
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