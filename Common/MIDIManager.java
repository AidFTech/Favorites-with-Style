package controllers;

import java.io.File;
import java.util.ArrayList;

import javax.sound.midi.MidiEvent;
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
import song.FWSSong;
import style.ChordBody;
import style.Style;
import voices.InstrumentProfile;
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

	/** Play a sequence. */
	public native void playSequence(FWSSequence sequence);

	/** Play a song with chord and style changes. */
	public native void playSong(FWSSong song);

	/** Start recording. */
	public native void recordStart();

	/** Start playing a style. */
	public native void playStyle(Style style);

	/** Get MIDI events from a song */
	private native void calculateSongMidiEvents(FWSSong song, MIDIExportOptions export_options, ArrayList<byte[]> midi_events, ArrayList<Long> midi_ticks);

	static {
		File midirust = new File("midirust/target/debug/libmidirust.so");
		System.load(midirust.getAbsolutePath());
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
		calculateSongMidiEvents(song, export_options, events, ticks);

		if(events.size() != ticks.size())
			return null;

		Sequence ret_sequence = new Sequence(Sequence.PPQ, song.getSongSequence().getTPQ());
		Track song_track = ret_sequence.createTrack();

		//Get the interpreter.
		PythonInterpreter interpreter = null;
		InstrumentProfile profile = controller.getInstrumentProfile(controller.getInstrumentProfileName());
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
			interpreter.set("song_metadata", song.getSongMetadata());
			interpreter.set("export_options", export_options);

			PyObject song_metadata_p = interpreter.get("song_metadata"), export_options_p = interpreter.get("export_options");
			PyFunction start_function = interpreter.get("on_song_start_file", PyFunction.class);

			PyTuple start_tuple = (PyTuple)start_function.__call__(song_metadata_p, export_options_p);
			
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
						interpreter.set("main_chord", main_chord);
						interpreter.set("bass_chord", bass_chord);
						PyObject p_chord_main = interpreter.get("main_chord"), p_chord_bass = interpreter.get("bass_chord");
						PyFunction chord_function = (PyFunction)interpreter.get("on_chord_file", PyFunction.class);

						if(chord_function != null) {
							PyByteArray chord_sysex = (PyByteArray)chord_function.__call__(p_chord_main, p_chord_bass, new PyString(controller.getInstrumentName()));

							if(chord_sysex != null) {
								PyInteger[] chord_sysex_py_bytes = new PyInteger[chord_sysex.size()];
								chord_sysex.toArray(chord_sysex_py_bytes);

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

		return ret_sequence;
	}
}
