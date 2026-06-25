package controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiDevice.Info;
import javax.swing.JComboBox;

import org.python.core.PyByteArray;
import org.python.core.PyException;
import org.python.core.PyFunction;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyTuple;
import org.python.util.PythonInterpreter;

import fwsevents.FWSChordEvent;
import fwsevents.FWSEvent;
import fwsevents.FWSNoteEvent;
import fwsevents.FWSSequence;
import fwsevents.FWSStyleChangeEvent;
import options.MIDIExportOptions;
import options.MIDIPlayerOptions;
import options.MIDIStartOptions;
import song.FWSSong;
import song.FWSSongMetadata;
import style.ChordBody;
import style.Style;
import voices.InstrumentProfile;
import voices.ProfileSubstitution;
import voices.SequenceSubstitution;
import voices.Voice;

public class MIDIManager {
	private FWS controller;
	private MIDIPlayerOptions player_options = new MIDIPlayerOptions();

	private Info input_device, output_device;

	/** Get the device list. */
	private static native Info[] getMidiInputDeviceList();

	/** Get the output device list. */
	private static native Info[] getMidiOutputDeviceList();

	/** Play a placed note. */
	public native void playPlacedNote(FWSNoteEvent note, Voice voice);

	/** Send a list of messages to the instrument. */
	private native void sendMIDIListJNI(byte[][] messages);

	/** Play a sequence. */
	private native void playSequenceJNI(FWSSequence sequence, MIDIStartOptions start_options);

	/** Play a song with chord and style changes. */
	private native void playSongJNI(FWSSong song, MIDIStartOptions start_options);

	/** Start recording. */
	public native void recordStart();

	/** Start playing a style. */
	private native void playStyleJNI(Style style, MIDIStartOptions start_options);

	/** Get MIDI events from a song */
	private native void calculateSongMidiEvents(FWSSong song, MIDIExportOptions export_options, MIDIStartOptions start_options, ArrayList<byte[]> midi_events, ArrayList<Long> midi_ticks);

	static {
		final String os = System.getProperty("os.name").toUpperCase();
		boolean use_windows = false;

		String library_path = "", file_name = "";
		
		if(os.contains("WIN")) {
			use_windows = true;
			library_path = "Windows_x86_64";
			file_name = "midirust.dll";
		} else if(os.contains("MAC")) {
			library_path = "Mac_x86_64";
			file_name = "libmidirust.jnilib";
		} else if(os.contains("LINUX") || os.contains("UNIX")) {
			library_path = "Linux_x86_64";
			file_name = "libmidirust.so";
		}

		final String full_path = "lib/" + library_path + "/" + file_name;
		final String temp_dir = System.getProperty("java.io.tmpdir");

		try {
			File temp_library = new File(temp_dir + "/" + file_name);
			File parent = temp_library.getParentFile();
			parent.mkdirs();
			parent.setReadable(true, false);
			parent.setWritable(true, false);
			parent.setExecutable(true, false);

			InputStream library_file = MIDIManager.class.getResourceAsStream("/" + full_path);
			FileOutputStream temp_library_stream = new FileOutputStream(temp_library);

			byte[] data = new byte[4096];
			int length = 0;
			while((length = library_file.read(data)) > 0)
				temp_library_stream.write(data, 0, length);

			temp_library_stream.close();

			if(!use_windows)
				System.load(temp_library.getAbsolutePath());
			else {
				final String path = temp_library.getAbsolutePath().replace('/', '\\');
				System.load(path);
			}
		} catch(IOException e) {

		}
	}

	public MIDIManager(FWS controller) {
		this.controller = controller;
	}

	/** Populate a combobox with input MIDI devices. */
	public Info[] populateInputDropdown(JComboBox<String> input_dropdown) {
		Info[] input_info = getMidiInputDeviceList();
		input_dropdown.removeAllItems();

		int index = -1;

		for(int i=0;i<input_info.length;i+=1) {
			input_dropdown.addItem(input_info[i].getDescription());
			if(input_device != null && input_info[i].getName().equals(input_device.getName()))
				index = i;
		}

		input_dropdown.setSelectedIndex(index);
		return input_info;
	}

	/** Set the input device. */
	public void setInputDevice(Info input_device) {
		this.input_device = input_device;
	}

	/** Get the input ID. */
	public String getInputID() {
		if(input_device != null)
			return input_device.getName();
		else
			return "";
	}

	/** Populate a combobox with output MIDI devices. */
	public Info[] populateOutputDropdown(JComboBox<String> output_dropdown) {
		Info[] output_info = getMidiOutputDeviceList();
		output_dropdown.removeAllItems();

		int index = -1;

		for(int i=0;i<output_info.length;i+=1) {
			output_dropdown.addItem(output_info[i].getDescription());
			if(output_device != null && output_info[i].getName().equals(output_device.getName()))
				index = i;
		}

		output_dropdown.setSelectedIndex(index);
		return output_info;
	}

	/** Set the output device. */
	public void setOutputDevice(Info output_device) {
		this.output_device = output_device;
	}

	/** Get the output ID. */
	public String getOutputID() {
		if(output_device != null)
			return output_device.getName();
		else
			return "";
	}

	/** Get the player options. */
	public MIDIPlayerOptions getPlayerOptions() {
		return this.player_options;
	}

	/** Load the recorded data into the active sequence. */
	private void loadRecordedData(byte[][] data, long[] us_ticks) {
		if(data.length != us_ticks.length)
			return;

		controller.handleRecordedEvents(data, us_ticks);
	}

	/** Send a list of messages to the instrument. */
	public void sendMIDIList(byte[][] messages) {
		this.sendMIDIListJNI(messages);
	}

	/** Send MIDI panic data. */
	public void sendMIDIPanic() {
		byte[][] panic_messages = new byte[16][];	
		for(int c=0;c<16;c+=1) {
			byte[] panic_msg = {(byte)(0xB0 | c), 120, 0};
			panic_messages[c] = panic_msg;
		}

		sendMIDIListJNI(panic_messages);
	}

	/** Play a song with chord and style changes. */
	public void playSong(FWSSong song) {
		InstrumentProfile active_profile = controller.output_profile;
		if(active_profile == null)
			active_profile = controller.active_profile;

		this.player_options.rec_accompaniment_volume = song.getSongMetadata().record_accompaniment_vol;
		if(active_profile != null)
			this.player_options.play_accompaniment_volume = active_profile.getAccompanimentVolume();
		else 
			this.player_options.play_accompaniment_volume = this.player_options.rec_accompaniment_volume;

		FWSSongMetadata meta = song.getSongMetadata();
		player_options.split_point = meta.split_point;
		player_options.chord_part = meta.chord_channel == meta.melody_lh_channel || meta.chord_channel == meta.melody_rh_channel;
		MIDIStartOptions start_options = new MIDIStartOptions();
		
		if(active_profile != null && active_profile.getReset())
			start_options.addResetMessages();

		String output_profile = controller.getOutputProfileName();
		if(output_profile == null || output_profile.isEmpty())
			output_profile = controller.getInstrumentProfileName();
		
		start_options.substitutions = getSubstitutionMap(song.getAllVoices(), song.getSubstitutions(output_profile), song.getTargetProfile(), song.getTargetInstrument());

		//Get the interpreter.
		PythonInterpreter interpreter = null;
		InstrumentProfile profile = controller.output_profile;

		if(profile == null)
			profile = controller.active_profile;

		if(profile != null) {
			interpreter = new PythonInterpreter();
			String script = profile.getScript();

			try {
				interpreter.exec(script);
			} catch(PyException e) {
				interpreter = null;
				//Error message?
			}
		}

		//Play events.
		if(interpreter != null) {
			String[] start_arg_names = JythonHandler.getRequiredArgs(interpreter, "on_song_play_stream");
			PyObject[] song_args = new PyObject[start_arg_names.length];

			for(int i=0;i<song_args.length;i+=1) {
				Object set_obj = null;
				switch(start_arg_names[i]) {
					case JythonHandler.ARG_SONG_META:
						set_obj = song.getSongMetadata();
						break;
					case JythonHandler.ARG_INSTRUMENT:
						set_obj = controller.output_instrument != null ? controller.output_instrument : controller.active_instrument;
						break;
					case JythonHandler.ARG_SONG_TITLE:
						set_obj = song.getSongMetadata().long_title;
						break;
					case JythonHandler.ARG_SONG_SHORT_TITLE:
						set_obj = song.getSongMetadata().short_title;
						break;
					case JythonHandler.ARG_SONG_TPQ:
						set_obj = Integer.valueOf(song.getSongSequence().getTPQ());
						break;
					case JythonHandler.ARG_CURRENT_TICK:
						set_obj = Long.valueOf(player_options.current_tick);
						break;
					case JythonHandler.ARG_CHORD_EVENTS:
						{
							ArrayList<FWSEvent> events = song.getSongSequence().getCommonEvents();
							ArrayList<FWSChordEvent> chord_events = new ArrayList<>();
							for(FWSEvent event: events) {
								if(event instanceof FWSChordEvent)
									chord_events.add((FWSChordEvent)event);
							}

							set_obj = chord_events;
						}
						break;
					case JythonHandler.ARG_SONG_EVENTS_MELODY:
						set_obj = song.getSongSequence().getAllEvents();
						break;
				}

				interpreter.set("set_obj", set_obj);
				song_args[i] = interpreter.get("set_obj");
			}

			PyFunction song_function = interpreter.get("on_song_play_stream", PyFunction.class);

			if(song_function != null) {
				PyList song_list = null;

				{
					PyObject p_song_obj = song_function.__call__(song_args);
					if(p_song_obj instanceof PyList)
						song_list = (PyList)p_song_obj;
					else if(p_song_obj instanceof PyByteArray)
						song_list = new PyList(p_song_obj);
				}

				if(song_list != null) {
					PyObject[] song_tuple_array = song_list.getArray();
					for(PyObject msg: song_tuple_array) {
						if(!(msg instanceof PyTuple))
							continue;
	
						PyTuple msg_tuple = (PyTuple)msg;
						PyObject[] msg_tuple_array = msg_tuple.getArray();

						if(msg_tuple_array.length != 2)
							continue;

						if(!(msg_tuple_array[0] instanceof PyByteArray))
							continue;

						if(!(msg_tuple_array[1] instanceof PyLong) && !(msg_tuple_array[1] instanceof PyInteger))
							continue;

						PyByteArray p_msg_bytes = (PyByteArray)msg_tuple_array[0];
						PyInteger[] p_msg_int = new PyInteger[p_msg_bytes.size()];
						p_msg_bytes.toArray(p_msg_int);
	
						byte[] msg_bytes = new byte[p_msg_int.length];
						for(int i=0;i<msg_bytes.length;i+=1)
							msg_bytes[i] = (byte)p_msg_int[i].getValue();

						long tick = 0;
						if(msg_tuple_array[1] instanceof PyLong) {
							PyLong p_tick_long = (PyLong)msg_tuple_array[1];
							tick = p_tick_long.getValue().longValue();
						} else if(msg_tuple_array[1] instanceof PyInteger) {
							PyInteger p_tick_long = (PyInteger)msg_tuple_array[1];
							tick = p_tick_long.getValue();
						} else continue;

						start_options.addSysexMessages(new byte[][] {msg_bytes}, new long[] {tick});
					}
				}
			}
		}

		this.playSongJNI(song, start_options);
	}

	/** Play a sequence. */
	public void playSequence(FWSSequence sequence) {
		MIDIStartOptions start_options = new MIDIStartOptions();

		FWSSong song = controller.loaded_song;

		InstrumentProfile active_profile = controller.output_profile;
		if(active_profile == null)
			active_profile = controller.active_profile;

		this.player_options.rec_accompaniment_volume = song.getSongMetadata().record_accompaniment_vol;
		if(active_profile != null)
			this.player_options.play_accompaniment_volume = active_profile.getAccompanimentVolume();
		else 
			this.player_options.play_accompaniment_volume = this.player_options.rec_accompaniment_volume;
		
		if(active_profile != null && active_profile.getReset())
			start_options.addResetMessages();

		player_options.chord_part = false;

		String output_profile = controller.getOutputProfileName();
		if(output_profile == null || output_profile.isEmpty())
			output_profile = controller.getInstrumentProfileName();

		start_options.substitutions = getSubstitutionMap(sequence.getAllVoices(), song.getSubstitutions(output_profile), song.getTargetProfile(), song.getTargetInstrument());

		this.playSequenceJNI(sequence, start_options);
	}

	/** Play a style. */
	public void playStyle(Style style) {
		MIDIStartOptions start_options = new MIDIStartOptions();

		FWSSong song = controller.loaded_song;

		InstrumentProfile active_profile = controller.output_profile;
		if(active_profile == null)
			active_profile = controller.active_profile;

		this.player_options.rec_accompaniment_volume = song.getSongMetadata().record_accompaniment_vol;
		if(active_profile != null)
			this.player_options.play_accompaniment_volume = active_profile.getAccompanimentVolume();
		else 
			this.player_options.play_accompaniment_volume = this.player_options.rec_accompaniment_volume;
		
		if(active_profile != null && active_profile.getReset())
			start_options.addResetMessages();

		String output_profile = controller.getOutputProfileName();
		if(output_profile == null || output_profile.isEmpty())
			output_profile = controller.getInstrumentProfileName();
		
		player_options.chord_part = false;
		start_options.substitutions = getSubstitutionMap(style.getFullSequence().getAllVoices(), song.getSubstitutions(output_profile), song.getTargetProfile(), song.getTargetInstrument());

		this.playStyleJNI(style, start_options);
	}

	/** Get a substitution map. */
	private SequenceSubstitution[] getSubstitutionMap(Voice[] sequence_voices, SequenceSubstitution[] sequence_substitutions, String sequence_profile, String sequence_instrument) {
		InstrumentProfile active_profile = controller.output_profile;
		if(active_profile == null)
			active_profile = controller.active_profile;
		
		if(active_profile == null)
			return new SequenceSubstitution[0];

		ArrayList<SequenceSubstitution> substitutions_vec = new ArrayList<>(0);

		final String target_profile = active_profile.getInstrumentFamily(), target_instrument = controller.output_profile != null ? controller.output_instrument : controller.getInstrumentName();

		if(isSameInstrumentProfile(target_profile, target_instrument, sequence_profile, sequence_instrument))
			return new SequenceSubstitution[0];

		final byte target_percussion_msb = (byte) (active_profile.getPercussionHeader()>>7), target_percussion_lsb = (byte) (active_profile.getPercussionHeader()&0x7F);
		Voice[] instrument_voices = active_profile.getVoiceList(target_instrument);

		ProfileSubstitution[] profile_substitution_list = active_profile.getSubstitutionList();
		ArrayList<ProfileSubstitution> profile_substitutions = new ArrayList<>(0);

		for(ProfileSubstitution substitution: profile_substitution_list) {
			if(substitution.getProfile().equalsIgnoreCase(sequence_profile))
				profile_substitutions.add(substitution);
		}

		for(Voice voice : sequence_voices) {
			boolean subbed = false;

			//Check the song/sequence.
			for(SequenceSubstitution substitution : sequence_substitutions) {
				if(substitution.getVoice().match(voice)) {
					for(Voice alternate : substitution.getAlternates()) {
						if(Voice.matchVoice(instrument_voices, alternate.voice, alternate.lsb, alternate.msb) != null) {
							substitutions_vec.add(new SequenceSubstitution(voice, alternate));
							subbed = true;
							break;
						}
					}
				}
				if(subbed)
					break;
			}

			if(subbed)
				continue;

			//Nothing from the song. Check profile substitutions.
			for(ProfileSubstitution substitution: profile_substitutions) {
				if(substitution.getOriginal().match(voice)) {
					if((substitution.getProfile().isEmpty() || sequence_profile.isEmpty()) || substitution.getProfile().equalsIgnoreCase(sequence_profile)) {
						substitutions_vec.add(new SequenceSubstitution(voice, substitution.getAlternate()));
						subbed = true;
						break;
					}
				}
			}

			if(subbed)
				continue;

			//Does the voice exist in the target list?
			if(Voice.matchVoice(instrument_voices, voice.voice, voice.lsb, voice.msb) != null)
				continue; //Substitution not needed.

			boolean is_percussion = false;

			//Does this voice exist anywhere in the controller libraries?
			InstrumentProfile sequence_profile_obj = controller.getInstrumentProfile(sequence_profile);
			if(sequence_profile_obj != null) {
				ProfileSubstitution[] target_substitution_list = sequence_profile_obj.getSubstitutionList();

				final byte sequence_percussion_msb = (byte)(sequence_profile_obj.getPercussionHeader()>>7), sequence_percussion_lsb = (byte)(sequence_profile_obj.getPercussionHeader()&0x7F);

				if(!target_instrument.isEmpty() && target_substitution_list != null) {
					for(ProfileSubstitution substitution: target_substitution_list) {
						if(substitution.getAlternate().match(voice)) {
							substitutions_vec.add(new SequenceSubstitution(voice, substitution.getOriginal()));
							subbed = true;
							break;
						}
					}
				}

				if(!subbed) {
					final boolean sequence_percussion = (voice.msb == sequence_percussion_msb && voice.lsb == sequence_percussion_lsb);
					is_percussion = sequence_percussion;
					Voice[] sequence_voice_list = sequence_profile_obj.getVoiceList(sequence_instrument);

					if(sequence_voice_list == null) {
						InstrumentProfile test_profile_obj = controller.instrument_profiles.get(sequence_profile);
						String[] test_instruments = test_profile_obj.getInstrumentNames();
						for(String test_instrument: test_instruments) {
							if(isSameInstrumentProfile(sequence_profile, sequence_instrument, sequence_profile, test_instrument)) {
								sequence_voice_list = sequence_profile_obj.getVoiceList(test_instrument);
								break;
							}
						}
					}

					if(!sequence_instrument.isEmpty() && sequence_voice_list != null) {
						Voice match = Voice.matchVoice(sequence_voice_list, voice.voice, voice.lsb, voice.msb);
						if(match != null) {
							ArrayList<Voice> gm_matches = new ArrayList<>(0);
							for(Voice instrument_voice: instrument_voices) {
								if((sequence_percussion && !(instrument_voice.msb == target_percussion_msb && instrument_voice.lsb == target_percussion_lsb)) ||
									!sequence_percussion && instrument_voice.msb == target_percussion_msb && instrument_voice.lsb == target_percussion_lsb)
									continue;

								if(instrument_voice.voice == voice.voice || sequence_percussion)
									gm_matches.add(instrument_voice);
							}

							int[] keyword_count = new int[gm_matches.size()];
							float[] keyword_match = new float[gm_matches.size()];
							for(int v=0;v<gm_matches.size();v+=1) {
								String voice_name = gm_matches.get(v).name;
								if(voice_name.contains(".")) {
									final int index_dot = voice_name.indexOf(".");
									String num = voice_name.substring(0, index_dot);
									try {
										Integer.parseInt(num);
										voice_name = voice_name.substring(index_dot + 1).trim();
									} catch(NumberFormatException e) {

									}
								}
								
								String[] voice_name_split = voice_name.split(" ");
								
								int matches = 0;
								int voice_name_len = voice_name_split.length;
								for(String split_name: voice_name_split) {
									if(split_name.contains(".")) {
										final int index_dot = split_name.indexOf(".");
										String num = split_name.substring(0, index_dot);
										try {
											Integer.parseInt(num);
											if(voice_name_len >= 1)
												voice_name_len -= 1;
											continue;
										} catch(NumberFormatException e) {

										}
									}

									if(match.name.toUpperCase().contains(split_name.toUpperCase()))
										matches += 1;
								}

								keyword_count[v] = matches;
								keyword_match[v] = (float)matches/((float)voice_name_len);
							}

							if(keyword_count.length > 0) {
								int max_index = 0;
								float max = 0;
								for(int i=0;i<keyword_match.length;i+=1) {
									if(keyword_match[i] > max) {
										max = keyword_match[i];
										max_index = i;
									}
								}

								substitutions_vec.add(new SequenceSubstitution(voice, gm_matches.get(max_index)));
								subbed = true;
							}
						}
					}
				}
			}

			if(subbed)
				continue;
			
			//Swap percussion or GM.
			if(is_percussion) {
				if(Voice.matchVoice(instrument_voices, voice.voice, target_percussion_lsb, target_percussion_msb) != null) {
					final Voice percussion_voice = new Voice("", voice.voice, target_percussion_lsb, target_percussion_msb);
					substitutions_vec.add(new SequenceSubstitution(voice, percussion_voice));
				} else {
					final Voice percussion_voice = new Voice("", (byte)0, target_percussion_lsb, target_percussion_msb);
					substitutions_vec.add(new SequenceSubstitution(voice, percussion_voice));
				}
			} else {
				final Voice gm_voice = new Voice("", voice.voice, (byte)0, (byte)0);
				substitutions_vec.add(new SequenceSubstitution(voice, gm_voice));
			}

		}

		SequenceSubstitution[] substitutions = new SequenceSubstitution[substitutions_vec.size()];
		substitutions_vec.toArray(substitutions);
		return substitutions;
	}

	/** Get a substitution voice. */
	public Voice getSubstitutionVoice(Voice original) {
		Voice[] original_list = new Voice[] {original};
		FWSSong song = controller.loaded_song;

		SequenceSubstitution[] substitutions = getSubstitutionMap(original_list, new SequenceSubstitution[0], song.getTargetProfile(), song.getTargetInstrument());

		for(SequenceSubstitution sub: substitutions) {
			if(sub.getVoice().match(original)) {
				if(sub.getAlternates().length > 0)
					return sub.getAlternates()[0];
			}
		}
		
		return original;
	}

	/** Return whether the instrument and profile are the same. */
	private static boolean isSameInstrumentProfile(String target_profile, String target_instrument, String sequence_profile, String sequence_instrument) {
		if(!target_profile.equalsIgnoreCase(sequence_profile))
			return false;

		String[] target_instruments = target_instrument.toUpperCase().split(",");
		for(String test_instrument: target_instruments) {
			if(sequence_instrument.toUpperCase().contains(test_instrument.trim()))
				return true;
		}

		String[] sequence_instruments = sequence_instrument.toUpperCase().split(",");
		for(String test_instrument: sequence_instruments) {
			if(target_instrument.toUpperCase().contains(test_instrument.trim()))
				return true;
		}

		return false;
	}

	/** Get bytes from a MIDI sequence. */
	protected static void getMidiBytes(Sequence midi_sequence, ArrayList<byte[]> midi_data, ArrayList<Long> timestamps) {
		if(midi_sequence == null)
			return;
		
		Track[] tracks = midi_sequence.getTracks();
		for(int t=0;t<tracks.length;t+=1) {
			Track track = tracks[t];
			final int len = track.size();
			for(int e=0;e<len;e+=1) {
				MidiEvent ev = track.get(e);
				midi_data.add(ev.getMessage().getMessage());
				timestamps.add(Long.valueOf(ev.getTick()));
			}
		}
	}

	/** Get a MIDI sequence from a song. */
	protected Sequence getSongMIDISequence(FWSSong song, MIDIExportOptions export_options) throws InvalidMidiDataException {
		ArrayList<byte[]> events = new ArrayList<>(0);
		ArrayList<Long> ticks = new ArrayList<>(0);

		String output_profile = controller.getOutputProfileName();
		if(output_profile == null)
			output_profile = controller.getInstrumentProfileName();

		MIDIStartOptions start_options = new MIDIStartOptions();
		start_options.substitutions = getSubstitutionMap(song.getAllVoices(), song.getSubstitutions(output_profile), song.getTargetProfile(), song.getTargetInstrument());

		calculateSongMidiEvents(song, export_options, start_options, events, ticks);

		if(events.size() != ticks.size())
			return null;

		Sequence ret_sequence = new Sequence(Sequence.PPQ, song.getSongSequence().getTPQ());
		Track song_track = ret_sequence.createTrack();

		//Get the interpreter.
		PythonInterpreter interpreter = null;
		InstrumentProfile profile = controller.output_profile;

		if(profile == null)
			profile = controller.active_profile;

		if(profile != null) {
			interpreter = new PythonInterpreter();
			String script = profile.getScript();

			try {
				interpreter.exec(script);
			} catch(PyException e) {
				interpreter = null;
				//Error message?
			}
		}

		//Start of song events.
		if(interpreter != null) {
			String[] start_arg_names = JythonHandler.getRequiredArgs(interpreter, "on_song_start_file");
			PyObject[] start_args = new PyObject[start_arg_names.length];

			for(int i=0;i<start_arg_names.length;i+=1) {
				Object set_obj = null;
				switch(start_arg_names[i]) {
					case JythonHandler.ARG_SONG_META:
						set_obj = song.getSongMetadata();
						break;
					case JythonHandler.ARG_EXPORT_OPTIONS:
						set_obj = export_options;
						break;
					case JythonHandler.ARG_INSTRUMENT:
						set_obj = controller.output_instrument != null ? controller.output_instrument : controller.active_instrument;
						break;
					case JythonHandler.ARG_SONG_TITLE:
						set_obj = song.getSongMetadata().long_title;
						break;
					case JythonHandler.ARG_SONG_SHORT_TITLE:
						set_obj = song.getSongMetadata().short_title;
						break;
					case JythonHandler.ARG_SONG_TPQ:
						set_obj = Integer.valueOf(song.getSongSequence().getTPQ());
						break;
					case JythonHandler.ARG_FILEPATH:
						set_obj = new PyString(controller.save_load_controller.getLastFilepath());
						break;
				}

				interpreter.set("set_obj", set_obj);
				start_args[i] = interpreter.get("set_obj");
			}

			PyFunction start_function = interpreter.get("on_song_start_file", PyFunction.class);

			if(start_function != null) {
				PyTuple start_tuple = null;

				{
					PyObject p_start_obj = start_function.__call__(start_args);
					if(p_start_obj instanceof PyTuple)
						start_tuple = (PyTuple)p_start_obj;
					else if(p_start_obj instanceof PyByteArray)
						start_tuple = new PyTuple(p_start_obj);
				}
				
				if(start_tuple != null) {
					PyObject[] start_tuple_array = start_tuple.getArray();
					for(PyObject msg : start_tuple_array) {
						if(!(msg instanceof PyByteArray))
							continue;
	
						PyByteArray p_msg_bytes = (PyByteArray)msg;
						PyInteger[] p_msg_int = new PyInteger[p_msg_bytes.size()];
						p_msg_bytes.toArray(p_msg_int);
	
						byte[] msg_bytes = new byte[p_msg_int.length];
						for(int i=0;i<msg_bytes.length;i+=1)
							msg_bytes[i] = (byte)p_msg_int[i].getValue();
	
						if(msg_bytes.length > 0 && (msg_bytes[0]&0xFF) == 0xF0) {
							SysexMessage start_message = new SysexMessage(msg_bytes, msg_bytes.length);
							MidiEvent start_event = new MidiEvent(start_message, 0);
							song_track.add(start_event);
						} else if(msg_bytes.length >= 3 && (msg_bytes[0]&0xFF) == 0xFF) {
							final int type = msg_bytes[1];
	
							if(msg_bytes.length <= 3)
								continue;
	
							byte[] meta_bytes = new byte[msg_bytes.length - 3];
							for(int i=0;i<meta_bytes.length;i+=1)
								meta_bytes[i] = msg_bytes[i+3];
	
							MetaMessage start_message = new MetaMessage(type, meta_bytes, meta_bytes.length);
							MidiEvent start_event = new MidiEvent(start_message, 0);
							song_track.add(start_event);
						}
					}
				}
			}
		}

		//Notes.
		for(int e=0;e<events.size() && e<ticks.size();e+=1) {
			if(events.get(e).length <= 0)
				continue;

			byte[] ev = events.get(e);
			if(ev.length >= 2 && (ev[0]&0xF0) != 0xF0) { //Short event.
				ShortMessage short_msg = new ShortMessage((ev[0]&0xF0)&0xFF, ev[0]&0xF, ev[1], ev.length >= 3 ? ev[2] : 0);
				MidiEvent short_event = new MidiEvent(short_msg, ticks.get(e));
				song_track.add(short_event);
			} else if(ev.length >= 3 && (ev[0]&0xFF) == 0xFF) { //Meta event.
				final int l = ev[2], type = ev[1];
				byte[] data = new byte[l];
				for(int i=0;i<data.length && i+3<ev.length;i+=1)
					data[i] = ev[i+3];

				MetaMessage meta_msg = new MetaMessage(type, data, l);
				MidiEvent meta_event = new MidiEvent(meta_msg, ticks.get(e));
				song_track.add(meta_event);
			} else if((ev[0]&0xFF) == 0xF0) { //Sysex event.
				SysexMessage sysex_msg = new SysexMessage(ev, ev.length);
				MidiEvent sysex_event = new MidiEvent(sysex_msg, ticks.get(e));
				song_track.add(sysex_event);
			}
		}

		//Export chords.
		if(export_options.export_chords || song.getSongMetadata().chord_channel >= 0) {
			final boolean print_notes = song.getSongMetadata().chord_channel >= 0;

			byte channel = song.getSongMetadata().chord_channel;
			if(channel == song.getSongMetadata().melody_rh_channel)
				channel = export_options.export_melody_rh;
			else if(channel == song.getSongMetadata().melody_lh_channel)
				channel = export_options.export_melody_lh;

			if(print_notes) {
				ShortMessage chord_volume_message = new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 0x7, 0);
				MidiEvent chord_volume_event = new MidiEvent(chord_volume_message, 0);
				song_track.add(chord_volume_event);
			}

			ArrayList<FWSEvent> song_events = song.getSongSequence().getCommonEvents();
			for(int e=0;e<song_events.size();e+=1) {
				if(song_events.get(e) instanceof FWSChordEvent) {
					FWSChordEvent chord_event = (FWSChordEvent)song_events.get(e);
					ChordBody main_chord = chord_event.main_chord, bass_chord = chord_event.bass_chord;
					final int inversion = export_options.invert ? chord_event.inversion : -1;

					if(print_notes) {
						byte[] chord_notes = main_chord.getInversion(inversion, song.getSongMetadata().split_point);
						long end_tick = song.getSongLength();

						for(int r=e+1;r<song_events.size();r+=1) {
							if(song_events.get(r) instanceof FWSChordEvent) {
								end_tick = song_events.get(r).tick;
								break;
							} else if(song_events.get(r) instanceof FWSStyleChangeEvent) {
								FWSStyleChangeEvent style = (FWSStyleChangeEvent) song_events.get(r);
								if(style.style_name.isEmpty() || style.section_name.isEmpty()) {
									end_tick = style.tick;
									break;
								}
							}
						}

						for(int i=0;i<chord_notes.length;i+=1) {
							ShortMessage chord_start_message = new ShortMessage(ShortMessage.NOTE_ON, channel, chord_notes[i], 1);
							ShortMessage chord_end_message = new ShortMessage(ShortMessage.NOTE_OFF, channel, chord_notes[i], 0);

							MidiEvent chord_start_event = new MidiEvent(chord_start_message, chord_event.tick);
							MidiEvent chord_end_event = new MidiEvent(chord_end_message, end_tick);

							song_track.add(chord_start_event);
							song_track.add(chord_end_event);
						}
					}

					if(interpreter != null && export_options.export_chords) {
						//Adjust the chord as needed.
						ChordBody main_chord_adj = main_chord, bass_chord_adj = bass_chord;
						int chord_index = -1;
						byte rule = MIDIPlayerOptions.CHORD_AS_IS;

						switch(main_chord.getRoot()) {
						case 1:
							chord_index = 0;
							break;
						case 3:
							chord_index = 1;
							break;
						case 6:
							chord_index = 2;
							break;
						case 8:
							chord_index = 3;
							break;
						case 10:
							chord_index = 4;
							break;
						}

						if(chord_index >= 0) {
							rule = export_options.black_chord_display[chord_index];
							byte new_root = main_chord.getFullRoot();
							switch(chord_index) {
							case 0:
								if(rule == MIDIPlayerOptions.CHORD_AS_SHARP)
									new_root = 0x41;
								else if(rule == MIDIPlayerOptions.CHORD_AS_FLAT)
									new_root = 0x22;
								break;
							case 1:
								if(rule == MIDIPlayerOptions.CHORD_AS_SHARP)
									new_root = 0x42;
								else if(rule == MIDIPlayerOptions.CHORD_AS_FLAT)
									new_root = 0x23;
								break;
							case 2:
								if(rule == MIDIPlayerOptions.CHORD_AS_SHARP)
									new_root = 0x44;
								else if(rule == MIDIPlayerOptions.CHORD_AS_FLAT)
									new_root = 0x25;
								break;
							case 3:
								if(rule == MIDIPlayerOptions.CHORD_AS_SHARP)
									new_root = 0x45;
								else if(rule == MIDIPlayerOptions.CHORD_AS_FLAT)
									new_root = 0x26;
								break;
							case 4:
								if(rule == MIDIPlayerOptions.CHORD_AS_SHARP)
									new_root = 0x46;
								else if(rule == MIDIPlayerOptions.CHORD_AS_FLAT)
									new_root = 0x27;
								break;
							}

							main_chord_adj = new ChordBody(new_root, main_chord.getChord());
						}

						//Bass:
						chord_index = -1;
						rule = MIDIPlayerOptions.CHORD_AS_IS;
						switch(bass_chord.getRoot()) {
						case 1:
							chord_index = 0;
							break;
						case 3:
							chord_index = 1;
							break;
						case 6:
							chord_index = 2;
							break;
						case 8:
							chord_index = 3;
							break;
						case 10:
							chord_index = 4;
							break;
						}

						if(chord_index >= 0) {
							rule = export_options.black_chord_display[chord_index];
							byte new_root = bass_chord.getFullRoot();
							switch(chord_index) {
							case 0:
								if(rule == MIDIPlayerOptions.CHORD_AS_SHARP)
									new_root = 0x41;
								else if(rule == MIDIPlayerOptions.CHORD_AS_FLAT)
									new_root = 0x22;
								break;
							case 1:
								if(rule == MIDIPlayerOptions.CHORD_AS_SHARP)
									new_root = 0x42;
								else if(rule == MIDIPlayerOptions.CHORD_AS_FLAT)
									new_root = 0x23;
								break;
							case 2:
								if(rule == MIDIPlayerOptions.CHORD_AS_SHARP)
									new_root = 0x44;
								else if(rule == MIDIPlayerOptions.CHORD_AS_FLAT)
									new_root = 0x25;
								break;
							case 3:
								if(rule == MIDIPlayerOptions.CHORD_AS_SHARP)
									new_root = 0x45;
								else if(rule == MIDIPlayerOptions.CHORD_AS_FLAT)
									new_root = 0x26;
								break;
							case 4:
								if(rule == MIDIPlayerOptions.CHORD_AS_SHARP)
									new_root = 0x46;
								else if(rule == MIDIPlayerOptions.CHORD_AS_FLAT)
									new_root = 0x27;
								break;
							}

							bass_chord_adj = new ChordBody(new_root, bass_chord.getChord());
						}

						interpreter.set("main_chord", main_chord_adj);
						interpreter.set("bass_chord", bass_chord_adj);
						PyObject p_chord_main = interpreter.get("main_chord"), p_chord_bass = interpreter.get("bass_chord");
						PyFunction chord_function = (PyFunction)interpreter.get("on_chord_file", PyFunction.class);

						if(chord_function != null) {
							PyTuple chord_sysex = null;

							{
								PyObject chord_obj = chord_function.__call__(p_chord_main, p_chord_bass, new PyString(controller.getInstrumentName()));
								if(chord_obj instanceof PyTuple)
									chord_sysex = (PyTuple)chord_obj;
								else if(chord_obj instanceof PyByteArray)
									chord_sysex = new PyTuple(chord_obj);
							}

							if(chord_sysex != null) {
								PyObject[] chord_sysex_array = chord_sysex.getArray();
								for(PyObject chord_obj: chord_sysex_array) {
									if(!(chord_obj instanceof PyByteArray))
										continue;

									PyByteArray chord_bytes_obj = (PyByteArray)chord_obj;

									PyInteger[] chord_sysex_py_bytes = new PyInteger[chord_bytes_obj.size()];
									chord_bytes_obj.toArray(chord_sysex_py_bytes);

									byte[] chord_sysex_bytes = new byte[chord_sysex_py_bytes.length];
									for(int i=0;i<chord_sysex_py_bytes.length;i+=1)
										chord_sysex_bytes[i] = (byte)chord_sysex_py_bytes[i].getValue();
									
									if(chord_sysex_bytes.length > 0 && (chord_sysex_bytes[0]&0xFF) == 0xF0) {
										byte[] msg_bytes = new byte[chord_sysex_bytes.length];
										for(int i=0;i<chord_sysex_bytes.length;i+=1)
											msg_bytes[i] = chord_sysex_bytes[i];

										SysexMessage chord_message = new SysexMessage(msg_bytes, msg_bytes.length);
										MidiEvent midi_chord_event = new MidiEvent(chord_message, chord_event.tick);
										song_track.add(midi_chord_event);
									} else if(chord_sysex_bytes.length >= 3 && (chord_sysex_bytes[0]&0xFF) == 0xFF) {
										final int type = chord_sysex_bytes[1];
										byte[] msg_bytes = new byte[chord_sysex_bytes.length - 3];
										for(int i=3;i<chord_sysex_bytes.length;i+=1)
											msg_bytes[i-3] = chord_sysex_bytes[i];

										MetaMessage chord_message = new MetaMessage(type, msg_bytes, msg_bytes.length);
										MidiEvent midi_chord_event = new MidiEvent(chord_message, chord_event.tick);
										song_track.add(midi_chord_event);
									}
								}
							}
						}
					}
				}
			}
		}

		//End of song events.
		if(interpreter != null) {
			String[] end_arg_names = JythonHandler.getRequiredArgs(interpreter, "on_song_play_file");
			PyObject[] end_args = new PyObject[end_arg_names.length];

			for(int i=0;i<end_arg_names.length;i+=1) {
				Object set_obj = null;
				switch(end_arg_names[i]) {
					case JythonHandler.ARG_SONG_META:
						set_obj = song.getSongMetadata();
						break;
					case JythonHandler.ARG_EXPORT_OPTIONS:
						set_obj = export_options;
						break;
					case JythonHandler.ARG_INSTRUMENT:
						set_obj = controller.output_instrument != null ? controller.output_instrument : controller.active_instrument;
						break;
					case JythonHandler.ARG_SONG_TITLE:
						set_obj = song.getSongMetadata().long_title;
						break;
					case JythonHandler.ARG_SONG_SHORT_TITLE:
						set_obj = song.getSongMetadata().short_title;
						break;
					case JythonHandler.ARG_SONG_TPQ:
						set_obj = Integer.valueOf(song.getSongSequence().getTPQ());
						break;
					case JythonHandler.ARG_CHORD_EVENTS:
						{
							ArrayList<FWSEvent> common_events = song.getSongSequence().getCommonEvents();
							ArrayList<FWSEvent> chord_events = new ArrayList<>();
							for(FWSEvent event: common_events) {
								if(event instanceof FWSChordEvent)
									chord_events.add((FWSChordEvent)event);
							}

							set_obj = FWSEvent.createCopy(chord_events);
						}
						break;
					case JythonHandler.ARG_SONG_EVENTS_MELODY:
						set_obj = FWSEvent.createCopy(song.getSongSequence().getAllEvents());
						break;
					case JythonHandler.ARG_SONG_EVENTS_ALL:
						{
							FWSSequence full_sequence = FWSSequence.getFWSSequencefromSequence(ret_sequence);

							ArrayList<FWSEvent> common_events = FWSEvent.createCopy(song.getSongSequence().getCommonEvents());
							for(FWSEvent event: common_events) {
								if(event instanceof FWSChordEvent || event instanceof FWSStyleChangeEvent)
									full_sequence.addEvent(event);
							}

							set_obj = full_sequence.getAllEvents();
						}
						break;
					case JythonHandler.ARG_FILEPATH:
						set_obj = new PyString(controller.save_load_controller.getLastFilepath());
						break;
				}

				interpreter.set("set_obj", set_obj);
				end_args[i] = interpreter.get("set_obj");
			}

			PyFunction end_function = interpreter.get("on_song_play_file", PyFunction.class);

			if(end_function != null) {
				PyList song_list = null;

				{
					PyObject p_song_obj = end_function.__call__(end_args);
					if(p_song_obj instanceof PyList)
						song_list = (PyList)p_song_obj;
					else if(p_song_obj instanceof PyByteArray)
						song_list = new PyList(p_song_obj);
				}

				if(song_list != null) {
					PyObject[] song_tuple_array = song_list.getArray();
					for(PyObject msg: song_tuple_array) {
						if(!(msg instanceof PyTuple))
							continue;
	
						PyTuple msg_tuple = (PyTuple)msg;
						PyObject[] msg_tuple_array = msg_tuple.getArray();

						if(msg_tuple_array.length != 2)
							continue;

						if(!(msg_tuple_array[0] instanceof PyByteArray))
							continue;

						if(!(msg_tuple_array[1] instanceof PyLong) && !(msg_tuple_array[1] instanceof PyInteger))
							continue;

						PyByteArray p_msg_bytes = (PyByteArray)msg_tuple_array[0];
						PyInteger[] p_msg_int = new PyInteger[p_msg_bytes.size()];
						p_msg_bytes.toArray(p_msg_int);
	
						byte[] msg_bytes = new byte[p_msg_int.length];
						for(int i=0;i<msg_bytes.length;i+=1)
							msg_bytes[i] = (byte)p_msg_int[i].getValue();

						long tick = 0;
						if(msg_tuple_array[1] instanceof PyLong) {
							PyLong p_tick_long = (PyLong)msg_tuple_array[1];
							tick = p_tick_long.getValue().longValue();
						} else if(msg_tuple_array[1] instanceof PyInteger) {
							PyInteger p_tick_long = (PyInteger)msg_tuple_array[1];
							tick = p_tick_long.getValue();
						} else continue;

						MidiMessage message = null;
						if(msg_bytes.length >= 2 && (msg_bytes[0]&0xF0) != 0xF0) {
							ShortMessage short_msg = new ShortMessage(msg_bytes[0]&0xF0, msg_bytes[0]&0xF, msg_bytes[1], msg_bytes.length >= 3 ? msg_bytes[2] : 0);
							message = short_msg;
						} else if(msg_bytes.length >= 3 && (msg_bytes[0]&0xFF) == 0xFF) {
							final int l = msg_bytes[2], type = msg_bytes[1];
							byte[] data = new byte[l];
							for(int i=0;i<data.length && i+3<msg_bytes.length;i+=1)
								data[i] = msg_bytes[i+3];

							MetaMessage meta_msg = new MetaMessage(type, data, l);
							message = meta_msg;
						} else if(msg_bytes.length >= 2 && (msg_bytes[0]&0xF0) == 0xF0) {
							SysexMessage sysex_msg = new SysexMessage(msg_bytes, msg_bytes.length);
							message = sysex_msg;
						}

						if(message != null)
							song_track.add(new MidiEvent(message, tick));
					}
				}
			}
		}

		return ret_sequence;
	}
}
