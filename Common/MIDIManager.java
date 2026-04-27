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
		if(song.getSongMetadata().chord_channel >= 0) {
			byte channel = song.getSongMetadata().chord_channel;
			if(channel == song.getSongMetadata().melody_rh_channel)
				channel = export_options.export_melody_rh;
			else if(channel == song.getSongMetadata().melody_lh_channel)
				channel = export_options.export_melody_lh;

			ShortMessage chord_volume_message = new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 0x7, 0);
			MidiEvent chord_volume_event = new MidiEvent(chord_volume_message, 0);
			song_track.add(chord_volume_event);

			ArrayList<FWSEvent> song_events = song.getSongSequence().getCommonEvents();
			for(int e=0;e<song_events.size();e+=1) {
				if(song_events.get(e) instanceof FWSChordEvent) {
					FWSChordEvent chord_event = (FWSChordEvent)song_events.get(e);
					ChordBody main_chord = chord_event.main_chord;
					final int inversion = export_options.invert ? chord_event.inversion : -1;

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
			}
		}

		return ret_sequence;
	}
}
