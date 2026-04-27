package style;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.swing.JFrame;

import fwsevents.FWSEvent;
import fwsevents.FWSKeySignatureEvent;
import fwsevents.FWSMiscMIDIEvent;
import fwsevents.FWSSectionNameEvent;
import fwsevents.FWSSequence;
import fwsevents.FWSShortEvent;
import fwsevents.FWSTempoEvent;
import fwsevents.FWSTimeSignatureEvent;
import fwsevents.FWSVoiceEvent;

public class Style {
	private Map<String, FWSSequence> sections = new LinkedHashMap<>();
	private Casm casm = new Casm();

	private ArrayList<FWSEvent> start_events = new ArrayList<>(0);

	private int tpq = 192;

	public String long_name = "", short_name = "";

	/** Get the named section. */
	public FWSSequence getSection(String section) {
		return sections.get(section);	
	}

	/** Get the style TPQ. */
	public int getTPQ() {
		int tpq = this.tpq;
		String[] sections = getSectionNames();
		for(int i=0;i<sections.length;i+=1) {
			FWSSequence section = this.sections.get(sections[i]);
			if(section.getTPQ() > tpq)
				tpq = section.getTPQ();
		}

		this.tpq = tpq;
		return this.tpq;
	}

	/** Initialize a style. */
	public void init(String long_name, String short_name, final int tpq) {
		sections.clear();
		start_events.clear();

		this.long_name = long_name;
		this.short_name = short_name;
		this.tpq = tpq;

		FWSSequence section = new FWSSequence();
		section.setTPQ(tpq);

		FWSTempoEvent tempo = new FWSTempoEvent();
		tempo.tick = 0;
		tempo.tempo = 120;

		FWSTimeSignatureEvent time_sig = new FWSTimeSignatureEvent();
		time_sig.tick = 0;
		time_sig.num = 4;
		time_sig.den = 2;

		start_events.add(tempo);
		start_events.add(time_sig);
		
		section.setEndEvent(tpq*4*4);

		sections.put("Main A", section);
	}

	/** Get the list of section names. */
	public String[] getSectionNames() {
		ArrayList<String> names_vec = new ArrayList<String>(0);
		for(Entry<String, FWSSequence> section : sections.entrySet()) {
			final String name = section.getKey();
			int index = -1;

			if(name.toUpperCase().contains("INTRO")) {
				for(int i=0;i<names_vec.size();i+=1) {
					if(!names_vec.get(i).toUpperCase().contains("INTRO")) {
						index = i;
						break;
					}
				}
			} else if(!name.toUpperCase().contains("ENDING")) {
				for(int i=0;i<names_vec.size();i+=1) {
					if(names_vec.get(i).toUpperCase().contains("ENDING")) {
						index = i;
						break;
					}
				}
			}

			if(index >= 0) 
				names_vec.add(index, name);
			else
				names_vec.add(name);
		}

		String[] names = new String[names_vec.size()];
		names_vec.toArray(names);
		return names;
	}

	/** Get the section names in the order in which they were loaded. */
	public String[] getOrderedSectionNames() {
		ArrayList<String> names_vec = new ArrayList<String>(0);
		for(Entry<String, FWSSequence> section : sections.entrySet()) {
			final String name = section.getKey();
			names_vec.add(name);
		}

		String[] names = new String[names_vec.size()];
		names_vec.toArray(names);
		return names;
	}
	
	/** Set the CASM. */
	public void setCasm(Casm casm) {
		this.casm = casm;
	}

	/** Get the CASM. */
	public Casm getCasm() {
		return this.casm;
	}

	/** Get the style start events. */
	public ArrayList<FWSEvent> getStartEvents() {
		return start_events;
	}
	
	/** Get a full, editable sequence from the style. */
	public FWSSequence getFullSequence() {
		FWSSequence full_sequence = new FWSSequence();
		full_sequence.setTPQ(this.tpq);

		String[] section_names = getSectionNames();

		ArrayList<FWSEvent> start_events = FWSEvent.createCopy(this.start_events);

		for(int i=0;i<start_events.size();i+=1)
			full_sequence.addEvent(start_events.get(i));

		full_sequence.setEndEvent(tpq*4*10);

		long[] measures = full_sequence.getMeasureTicks();
		if(measures.length <= 1)
			return null;
		
		long tick = measures[1];

		for(int s=0;s<section_names.length;s+=1) {
			FWSSequence section = sections.get(section_names[s]);
			ArrayList<FWSEvent> section_events_vec = section.getAllEvents();
			FWSEvent[] section_events_org = new FWSEvent[section_events_vec.size()];
			section_events_vec.toArray(section_events_org);

			FWSEvent[] section_events = FWSEvent.createCopy(section_events_org, tick);
			final long start_tick = tick;
			tick += section.getEndEvent().tick;
			full_sequence.setEndEvent(tick);

			for(int i=0;i<section_events.length;i+=1) {
				if(section_events[i].tick >= start_tick && section_events[i].tick < tick) {
					try {
						if(section_events[i] instanceof FWSTempoEvent && section_events[i].tick == start_tick && ((FWSTempoEvent)section_events[i]).tempo == full_sequence.getTempoAt(start_tick).tempo)
							continue;
						else if(section_events[i] instanceof FWSVoiceEvent && section_events[i].tick == start_tick) {
							FWSVoiceEvent voice_event = (FWSVoiceEvent)section_events[i];
							if(full_sequence.getVoiceAt(start_tick, voice_event.channel).equals(voice_event))
								continue;
						} else if(section_events[i] instanceof FWSShortEvent && section_events[i].tick == start_tick) {
							FWSShortEvent short_event = (FWSShortEvent)section_events[i];
							if(short_event.command == ShortMessage.PITCH_BEND || short_event.command == ShortMessage.CHANNEL_PRESSURE) {
								if(full_sequence.getShortSettingAt(start_tick, short_event.channel, (byte)-1, short_event.command).equals(short_event))
									continue;
							} else {
								if(full_sequence.getShortSettingAt(start_tick, short_event.channel, short_event.data1, short_event.command).equals(short_event))
									continue;
							}
						}
					} catch(NullPointerException e) {

					}

					if(section_events[i] instanceof FWSTimeSignatureEvent || section_events[i] instanceof FWSKeySignatureEvent)
						continue;

					full_sequence.addEvent(section_events[i]);
				}
			}

			FWSSectionNameEvent name_event = new FWSSectionNameEvent();
			name_event.tick = start_tick;
			name_event.section_name = section_names[s];

			full_sequence.addEvent(name_event);
		}

		return full_sequence;
	}

	/** Break a full FWS sequence into smaller sequences. */
	public void getFromSequence(FWSSequence full_sequence) {
		sections.clear();
		start_events.clear();
		tpq = full_sequence.getTPQ();

		ArrayList<FWSSectionNameEvent> section_names = new ArrayList<FWSSectionNameEvent>(0);

		ArrayList<FWSEvent> common_events = full_sequence.getCommonEvents();
		for(int i=0;i<common_events.size();i+=1) {
			if(common_events.get(i) instanceof FWSMiscMIDIEvent) {
				FWSMiscMIDIEvent misc_event = (FWSMiscMIDIEvent)common_events.get(i);
				if(misc_event.type == 6) { //Marker.
					final String text = new String(misc_event.data, StandardCharsets.ISO_8859_1);
					if(!text.toUpperCase().contains("SFF") && !text.equalsIgnoreCase("SInt")) {
						FWSSectionNameEvent section = new FWSSectionNameEvent();
						section.tick = misc_event.tick;
						section.section_name = text;
						section_names.add(section);
					}
				} else if(misc_event.type == 3) { //Long style name.
					final String text = new String(misc_event.data, StandardCharsets.ISO_8859_1);
					long_name = text;
				} else if(misc_event.tick == 0)
					start_events.add(misc_event);
			} else if(common_events.get(i) instanceof FWSSectionNameEvent) {
				section_names.add((FWSSectionNameEvent)common_events.get(i));
			} else if(common_events.get(i).tick == 0)
				start_events.add(common_events.get(i));
		}

		FWSTimeSignatureEvent time_sig = full_sequence.getTimeSignatureAt(0);
		FWSTempoEvent tempo = full_sequence.getTempoAt(0);

		FWSEvent[] seq_events;
		{
			ArrayList<FWSEvent> seq_events_vec = full_sequence.getAllEvents();

			for(int i=0;i<seq_events_vec.size();i+=1) {
				if((seq_events_vec.get(i) instanceof FWSVoiceEvent || seq_events_vec.get(i) instanceof FWSShortEvent) && seq_events_vec.get(i).tick == 0)
					start_events.add(seq_events_vec.get(i));
			}

			seq_events = new FWSEvent[seq_events_vec.size()];
			seq_events_vec.toArray(seq_events);
		}

		for(int s=0;s<section_names.size();s+=1) {
			FWSSectionNameEvent section_name = section_names.get(s);
			FWSSequence section = new FWSSequence();
			section.setTPQ(tpq);

			final long section_end = (s < section_names.size() - 1 ? section_names.get(s+1).tick : full_sequence.getSequenceLength()) - section_name.tick;

			FWSEvent[] section_event_pool = FWSEvent.createCopy(seq_events, -section_name.tick);
			for(int i=0;i<section_event_pool.length;i+=1) {
				if(section_event_pool[i].tick >= 0 && section_event_pool[i].tick < section_end) {
					if(section_event_pool[i] instanceof FWSMiscMIDIEvent) {
						final int type = ((FWSMiscMIDIEvent)section_event_pool[i]).type;
						if(type == 3 || type == 6)
							continue;
					} else if(section_event_pool[i] instanceof FWSSectionNameEvent)
						continue;
					section.addEvent(section_event_pool[i]);
				}
			}

			if(tempo != null)
				section.addEvent(new FWSTempoEvent(tempo));

			if(time_sig != null)
				section.addEvent(new FWSTimeSignatureEvent(time_sig));

			ArrayList<FWSEvent> section_start_events = FWSEvent.createCopy(full_sequence.getAllShortEventsAt(section_name.tick));
			for(int i=0;i<section_start_events.size();i+=1) {
				FWSEvent ev = section_start_events.get(i);
				ev.tick = 0;
				if(section_start_events.get(i) instanceof FWSVoiceEvent)
					section.addEvent((FWSVoiceEvent)ev);
				else if(section_start_events.get(i) instanceof FWSShortEvent)
					section.addEvent((FWSShortEvent)ev);
			}

			section.setEndEvent(section_end);

			sections.put(section_name.section_name, section);
		}
	}

	/** Get Yamaha style file binary from the style. */
	public byte[] getStyleFileBinary() {
		try {
			FWSSequence full_sequence = getFullSequence();
			Sequence midi_sequence = new Sequence(Sequence.PPQ, this.tpq);

			final String int_str = "SInt", sff_str = "SFF1";
			Track seq_track = midi_sequence.createTrack();
		
			seq_track.add(new MidiEvent(new MetaMessage(6, int_str.getBytes(), int_str.length()), 0));
			seq_track.add(new MidiEvent(new MetaMessage(6, sff_str.getBytes(), sff_str.length()), 0));

			seq_track.add(new MidiEvent(new MetaMessage(3, this.long_name.getBytes(), this.long_name.length()), 0));

			full_sequence.getMidiSequence(midi_sequence);

			ByteArrayOutputStream output_stream = new ByteArrayOutputStream();

			MidiSystem.write(midi_sequence, 0, output_stream);
			output_stream.write(casm.getCasmBinary());
			
			return output_stream.toByteArray();
		} catch (InvalidMidiDataException | IOException e) {
			return null;	
		}
	}

	/** Get a style from a .sty file. */
	public static Style getStyleFromFile(File style_file, JFrame main_window) {
		Style style = new Style();
		FWSSequence full_sequence = FWSSequence.getFWSSequencefromSequence(style_file, main_window);
		final int tpq = full_sequence.getTPQ();

		style.getFromSequence(full_sequence);

		style.tpq = tpq;
		style.casm = Casm.createCasmFromFile(style_file);

		final String filename = style_file.getName();
		final int ext = filename.toUpperCase().indexOf(".STY");
		if(ext >= 0)
			style.short_name = filename.substring(0, ext);
		else
			style.short_name = filename;

		return style;
	}
}
