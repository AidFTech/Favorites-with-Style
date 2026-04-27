package fwsevents;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import options.RecordLoadOptions;

public class FWSSequence {
	private int tpq = 192;
	private float div_type = Sequence.PPQ;

	@SuppressWarnings("unchecked")
	private ArrayList<FWSEvent>[] channel_events = new ArrayList[16];
	private ArrayList<FWSEvent> common_events = new ArrayList<FWSEvent>(0);

	public FWSSequence() {
		for(int i=0;i<channel_events.length;i+=1)
			channel_events[i] = new ArrayList<FWSEvent>(0);
	}

	/** Initialize the sequence. */
	public void init() {
		this.common_events.clear();
		for(int i=0;i<channel_events.length;i+=1)
			channel_events[i].clear();

		addEvent(new FWSTimeSignatureEvent());
		addEvent(new FWSKeySignatureEvent());
		addEvent(new FWSTempoEvent());

		setEndEvent(tpq*4*10);
	}

	/** Get the ticks per quarter note. */
	public int getTPQ() {
		return this.tpq;
	}

	/** Set the ticks per quarter note. */
	public void setTPQ(final int tpq) {
		final int last_tpq = this.tpq;
		this.tpq = tpq;

		//Adjust event timing.
		for(int i=0;i<common_events.size();i+=1)
			common_events.get(i).tick = common_events.get(i).tick*tpq/last_tpq;

		for(int c=0;c<channel_events.length;c+=1) {
			for(int i=0;i<channel_events[c].size();i+=1) {
				FWSEvent channel_event = channel_events[c].get(i);
				channel_event.tick = channel_event.tick*tpq/last_tpq;
				if(channel_event instanceof FWSNoteEvent)
					((FWSNoteEvent)channel_event).duration = ((FWSNoteEvent)channel_event).duration*tpq/last_tpq;
			}
		}
	}

	/** Get the division type. */
	public float getDivType() {
		return this.div_type;
	}

	/** Set the division type. */
	public void setDivType(final float div_type) {
		this.div_type = div_type;
	}

	/** Return whether the sequence is empty. */
	public boolean seqEmpty() {
		if(common_events.size() > 0)
			return false;
		else {
			for(int i=0;i<channel_events.length;i+=1) {
				if(channel_events[i].size() > 0)
					return false;
			}
		}
		return true;
	}

	/** Get the control value at the specified tick. */
	public byte getControlValueAt(final long tick, final byte channel, final byte controller) {
		if(channel < 0 || channel >= channel_events.length)
			return -1;

		FWSShortEvent apt_event = getControlEventAt(tick, channel, controller);
		if(apt_event == null)
			return -1;
		else
			return apt_event.data2;
	}

	/** Get the value-setting event at the specified tick. */
	public FWSShortEvent getControlEventAt(final long tick, final byte channel, final byte controller) {
		if(channel < 0 || channel >= channel_events.length)
			return null;

		ArrayList<FWSEvent> channel_events = this.channel_events[channel];
		int index = -1, last_index = -1;
		for(int i=0;i<channel_events.size();i+=1) {
			FWSEvent test_event = channel_events.get(i);
			if(test_event instanceof FWSShortEvent) {
				FWSShortEvent short_test = (FWSShortEvent)test_event;
				if((short_test.command&0xFF) == ShortMessage.CONTROL_CHANGE && short_test.data1 == controller) {
					if(short_test.tick < tick) {
						last_index = i;
					} else if(short_test.tick == tick) {
						index = i;
						break;
					} else if(short_test.tick > tick) {
						index = last_index;
						break;
					}
				}
			}
		}

		if(index < 0) {
			if(last_index < 0)
				return null;
			else
				return (FWSShortEvent)channel_events.get(last_index);
		} else
			return (FWSShortEvent)channel_events.get(index);
	}

	/** Get the voice event at the specified tick. */
	public FWSVoiceEvent getVoiceAt(final long tick, final byte channel) {
		if(channel < 0 || channel >= channel_events.length)
			return null;

		ArrayList<FWSEvent> channel_events = this.channel_events[channel];
		int index = -1, last_index = -1;
		for(int i=0;i<channel_events.size();i+=1) {
			FWSEvent test_event = channel_events.get(i);
			if(test_event instanceof FWSVoiceEvent) {
				if(test_event.tick > tick) {
					index = last_index;
					break;
				} else if(test_event.tick == tick) {
					index = i;
					break;
				} else if(test_event.tick < tick) {
					last_index = i;
				}
			}
		}

		if(index < 0) {
			if(last_index < 0)
				return null;
			else
				return (FWSVoiceEvent)channel_events.get(last_index);
		} else
			return (FWSVoiceEvent)channel_events.get(index);
	}

	/** Get the MIDI control setting at the specified tick. */
	public FWSShortEvent getShortSettingAt(final long tick, final byte channel, final byte control, final byte command) {
		if(channel < 0 || channel >= channel_events.length)
			return null;

		ArrayList<FWSEvent> channel_events = this.channel_events[channel];
		int index = -1, last_index = -1;
		for(int i=0;i<channel_events.size();i+=1) {
			FWSEvent test_event = channel_events.get(i);
			if(test_event instanceof FWSShortEvent) {
				FWSShortEvent short_test = (FWSShortEvent)test_event;

				if(short_test.channel != channel || (short_test.data1 != control && short_test.data1 >= 0) || short_test.command != command)
					continue;

				if(test_event.tick > tick) {
					index = last_index;
					break;
				} else if(test_event.tick == tick) {
					index = i;
					break;
				} else if(test_event.tick < tick) {
					last_index = i;
				}
			}
		}

		if(index < 0) {
			if(last_index < 0)
				return null;
			else
				return (FWSShortEvent)channel_events.get(last_index);
		} else
			return (FWSShortEvent)channel_events.get(index);
	}

	/** Get the full list of control events set at the specified tick. */
	public ArrayList<FWSEvent> getAllShortEventsAt(final long tick) {
		ArrayList<FWSEvent> short_list = new ArrayList<FWSEvent>(0);

		for(int c=0;c<16;c+=1) {
			FWSVoiceEvent voice_test = getVoiceAt(tick, (byte)c);
			if(voice_test != null)
				FWSEvent.insertEvent(short_list, voice_test);

			for(int i=0;i<128;i+=1) {
				FWSShortEvent control_test = getShortSettingAt(tick, (byte)c, (byte)i, (byte)ShortMessage.CONTROL_CHANGE);
				if(control_test != null)
					FWSEvent.insertEvent(short_list, control_test);
			}

			FWSShortEvent pressure_test = getShortSettingAt(tick, (byte)c, (byte)-1, (byte)ShortMessage.CHANNEL_PRESSURE);
			if(pressure_test != null)
				FWSEvent.insertEvent(short_list, pressure_test);

			for(int i=0;i<128;i+=1) {
				FWSShortEvent poly_test = getShortSettingAt(tick, (byte)c, (byte)i, (byte)ShortMessage.POLY_PRESSURE);
				if(poly_test != null)
					FWSEvent.insertEvent(short_list, poly_test);
			}

			FWSShortEvent pitch_test = getShortSettingAt(tick, (byte)c, (byte)-1, (byte)ShortMessage.PITCH_BEND);
			if(pitch_test != null)
				FWSEvent.insertEvent(short_list, pitch_test);
		}

		return short_list;
	}

	/** Get the time signature event at the specified tick. */
	public FWSTimeSignatureEvent getTimeSignatureAt(final long tick) {
		int index = -1, last_index = -1;
		for(int i=0;i<common_events.size();i+=1) {
			FWSEvent test_event = common_events.get(i);
			if(test_event instanceof FWSTimeSignatureEvent) {
				if(test_event.tick > tick) {
					index = last_index;
					break;
				} else if(test_event.tick == tick) {
					index = i;
					break;
				} else if(test_event.tick < tick) {
					last_index = i;
				}
			}
		}

		if(index < 0) {
			if(last_index < 0)
				return null;
			else
				return (FWSTimeSignatureEvent)common_events.get(last_index);
		} else
			return (FWSTimeSignatureEvent)common_events.get(index);
	}

	/** Get the key signature event at the specified tick. */
	public FWSKeySignatureEvent getKeySignatureAt(final long tick) {
		int index = -1, last_index = -1;
		for(int i=0;i<common_events.size();i+=1) {
			FWSEvent test_event = common_events.get(i);
			if(test_event instanceof FWSKeySignatureEvent) {
				if(test_event.tick > tick) {
					index = last_index;
					break;
				} else if(test_event.tick == tick) {
					index = i;
					break;
				} else if(test_event.tick < tick) {
					last_index = i;
				}
			}
		}

		if(index < 0) {
			if(last_index < 0)
				return null;
			else
				return (FWSKeySignatureEvent)common_events.get(last_index);
		} else
			return (FWSKeySignatureEvent)common_events.get(index);
	}

	/** Get the tempo event at the specified tick. */
	public FWSTempoEvent getTempoAt(final long tick) {
		int index = -1, last_index = -1;
		for(int i=0;i<common_events.size();i+=1) {
			FWSEvent test_event = common_events.get(i);
			if(test_event instanceof FWSTempoEvent) {
				if(test_event.tick > tick) {
					index = last_index;
					break;
				} else if(test_event.tick == tick) {
					index = i;
					break;
				} else if(test_event.tick < tick) {
					last_index = i;
				}
			}
		}

		if(index < 0) {
			if(last_index < 0)
				return null;
			else
				return (FWSTempoEvent)common_events.get(last_index);
		} else
			return (FWSTempoEvent)common_events.get(index);
	}

	/** Get the style at the specified tick. */
	public FWSStyleChangeEvent getStyleAt(final long tick) {
		int index = -1, last_index = -1;
		for(int i=0;i<common_events.size();i+=1) {
			FWSEvent test_event = common_events.get(i);
			if(test_event instanceof FWSStyleChangeEvent) {
				if(test_event.tick > tick) {
					index = last_index;
					break;
				} else if(test_event.tick == tick) {
					index = i;
					break;
				} else if(test_event.tick < tick) {
					last_index = i;
				}
			}
		}

		if(index < 0) {
			if(last_index < 0)
				return null;
			else
				return (FWSStyleChangeEvent)common_events.get(last_index);
		} else
			return (FWSStyleChangeEvent)common_events.get(index);
	}

	/** Get the measure points. */
	public long[] getMeasureTicks() {
		ArrayList<Long> measure_ticks = new ArrayList<Long>(0);
		long last_measure = 0;
		byte n = 4, d = 2;

		for(int e=0;e<common_events.size();e+=1) {
			if(common_events.get(e) instanceof FWSTimeSignatureEvent) {
				FWSTimeSignatureEvent time_change = (FWSTimeSignatureEvent)common_events.get(e);
				final int tpm = (int)(tpq/(Math.pow(2.0, d)/4.0))*n;
				for(long t=last_measure;t<time_change.tick;t+=tpm)
					measure_ticks.add(Long.valueOf(t));

				last_measure = time_change.tick;
				n = time_change.num;
				d = time_change.den;
			}
		}

		final int tpm = (int)(tpq/(Math.pow(2.0, d)/4.0))*n;
		final long final_tick = getEndEvent().tick;
		for(long t=last_measure;t<final_tick;t+=tpm)
			measure_ticks.add(Long.valueOf(t));

		long[] measure_ticks_array = new long[measure_ticks.size()];
		for(int i=0;i<measure_ticks.size();i+=1)
			measure_ticks_array[i] = (long)measure_ticks.get(i);

		return measure_ticks_array;
	}

	/** Get the beat points. */
	public long[] getBeatTicks() {
		ArrayList<Long> beat_ticks = new ArrayList<Long>(0);
		long last_measure = 0;
		byte d = 2;

		for(int e=0;e<common_events.size();e+=1) {
			if(common_events.get(e) instanceof FWSTimeSignatureEvent) {
				FWSTimeSignatureEvent time_change = (FWSTimeSignatureEvent)common_events.get(e);
				final int tpb = (int)(tpq/(Math.pow(2.0, d)/4.0));
				for(long t=last_measure;t<time_change.tick;t+=tpb)
					beat_ticks.add(Long.valueOf(t));

				last_measure = time_change.tick;
				d = time_change.den;
			}
		}

		final int tpb = (int)(tpq/(Math.pow(2.0, d)/4.0));
		final long final_tick = getEndEvent().tick;
		for(long t=last_measure;t<final_tick;t+=tpb)
			beat_ticks.add(Long.valueOf(t));

		long[] beat_ticks_array = new long[beat_ticks.size()];
		for(int i=0;i<beat_ticks.size();i+=1)
			beat_ticks_array[i] = (long)beat_ticks.get(i);

		return beat_ticks_array;
	}

	/** Get the measure number at the specified tick. */
	public int getMeasureAt(final long tick) {
		long[] ticks = getMeasureTicks();

		if(ticks.length == 0)
			return (int)(tick/tpq/4);

		for(int i=0;i<ticks.length;i+=1) {
			if(ticks[i] > tick)
				return i > 0 ? (i-1) : 0;
			else if(ticks[i] == tick)
				return i;
		}
		
		if(ticks.length > 0 && tick > ticks[ticks.length-1])
			return ticks.length - 1;

		return 0;
	}

	/** Get the modulo beat number at the specified tick. */
	public int getBeatAt(final long tick) {
		long[] ticks = getMeasureTicks(), beats = getBeatTicks();

		if(ticks.length == 0 || beats.length == 0)
			return (int)((tick/tpq)%4);

		final int m = getMeasureAt(tick);
		final long m_tick = ticks[m];

		int b = 0;
		for(int i=0;i<beats.length;i+=1) {
			if(beats[i] >= m_tick) {
				b = i;
				break;
			}
		}

		for(int i=b;i<beats.length;i+=1) {
			if(beats[i] == tick)
				return i - b;
			else if(beats[i] > tick)
				return (i > 0 ? (i-1) : 0) - b;
		}
		
		if(beats.length > 0 && tick > beats[beats.length-1])
			return beats.length - 1 - b;
		
		return 0;
	}

	/** Get the modulo tick number at the specified tick. */
	public long getTickAt(final long tick) {
		long[] beats = getBeatTicks();

		if(beats.length == 0)
			return tick%tpq;
		
		if(beats.length > 0 && tick > beats[beats.length - 1])
			return tick - beats[beats.length - 1];
		
		int b = 0;
		for(int i=0;i<beats.length;i+=1) {
			if(beats[i] == tick) {
				b = i;
				break;
			} else if(beats[i] > tick) {
				b = i > 0 ? (i-1) : 0;
				break;
			}
		}
		
		return tick - beats[b];
	}

	/** Get the note name at a particular tick. */
	public String getNoteNameAt(final byte note, final long tick, final boolean number) {
		final byte ks = getKeySignatureAt(tick) != null ? getKeySignatureAt(tick).accidental_count : 0;
		
		String note_name = "";

		switch(note%12) {
		case 0:
		case 2:
		case 4:
		case 5:
		case 7:
		case 9:
		case 11:
			note_name = FWSNoteEvent.note_map[note%12];
			break;
		case 1:
			if (ks < 0)
				note_name = "D♭";
			else
				note_name = "C♯";
			break;
		case 3:
			if (ks < 2)
				note_name = "E♭";
			else
				note_name = "D♯";
			break;
		case 6:
			if (ks < -1)
				note_name = "G♭";
			else
				note_name = "F♯";
			break;
		case 8:
			if (ks < 1)
				note_name = "A♭";
			else
				note_name = "G♯";
			break;
		case 10:
			if (ks < 3)
				note_name = "B♭";
			else
				note_name = "A♯";
			break;
		}

		if(number)
			note_name += Integer.toString(note/12 - 1);

		return note_name;
	}

	/** Add an event to the sequence. */
	public void addEvent(FWSEvent event) {
		//See if this is an initial event we need to remove.
		if(event.tick == 0) {
			if(event instanceof FWSTimeSignatureEvent) {
				FWSTimeSignatureEvent init_ts = getTimeSignatureAt(0);
				if(init_ts != null && init_ts.tick == 0)
					removeEvent(init_ts);
			} else if(event instanceof FWSTempoEvent) {
				FWSTempoEvent init_tempo = getTempoAt(0);
				if(init_tempo != null && init_tempo.tick == 0)
					removeEvent(init_tempo);
			} else if(event instanceof FWSKeySignatureEvent) {
				FWSKeySignatureEvent init_ks = getKeySignatureAt(0);
				if(init_ks != null && init_ks.tick == 0)
					removeEvent(init_ks);
			}
		}

		if(event instanceof FWSNoteEvent) {
			final byte channel = ((FWSNoteEvent)event).channel;
			FWSEvent.insertEvent(channel_events[channel], event);
		} else if(event instanceof FWSShortEvent) {
			final byte channel = ((FWSShortEvent)event).channel;
			FWSEvent.insertEvent(channel_events[channel], event);
		} else if(event instanceof FWSVoiceEvent) {
			final byte channel = ((FWSVoiceEvent)event).channel;
			FWSEvent.insertEvent(channel_events[channel], event);
		} else {
			FWSEvent.insertEvent(common_events, event);
		}

		FWSEvent end = getEndEvent();
		if(end != null) {
			if(event instanceof FWSNoteEvent) {
				if(end.tick < event.tick + ((FWSNoteEvent)event).duration)
					setEndEvent(event.tick + ((FWSNoteEvent)event).duration);
			} else {
				if(end.tick < event.tick)
					setEndEvent(event.tick);
			}
		}
	}

	/** Remove an event from the sequence. Returns whether successful. */
	public boolean removeEvent(FWSEvent event) {
		if(event instanceof FWSNoteEvent || event instanceof FWSShortEvent || event instanceof FWSVoiceEvent) {
			for(int c=0;c<channel_events.length;c+=1) {
				if(channel_events[c].contains(event))
					return channel_events[c].remove(event);
			}
			return common_events.remove(event);
		} else {
			return common_events.remove(event);
		}
	}

	/** Returns whether an event is present in the sequence. */
	public boolean getEvent(FWSEvent event) {
		for(int c=0;c<channel_events.length;c+=1) {
			ArrayList<FWSEvent> channel_events = this.channel_events[c];
			for(int i=0;i<channel_events.size();i+=1) {
				if(channel_events.get(i) == event)
					return true;
			}
		}

		for(int i=0;i<common_events.size();i+=1) {
			if(common_events.get(i) == event)
				return true;
		}

		return false;
	}

	/** Refresh the event position in the sequence. */
	public void refreshEvent(FWSEvent event) {
		if(!getEvent(event))
			return;
		
		removeEvent(event);
		addEvent(event);
	}

	/** Refresh a time signature event. */
	public void refreshTimeSignatures(FWSTimeSignatureEvent new_event, FWSTimeSignatureEvent original, final boolean shift_by_measure) {
		ArrayList<FWSEvent> time_signatures = new ArrayList<FWSEvent>(0);
		for(int i=0;i<common_events.size();i+=1) {
			if(common_events.get(i) instanceof FWSTimeSignatureEvent)
				time_signatures.add(common_events.get(i));
		}

		long[] all_measures = getMeasureTicks();
		int[] time_measures = new int[time_signatures.size()];

		for(int t=0;t<time_measures.length;t+=1) {
			for(int i=0;i<all_measures.length;i+=1) {
				if(time_signatures.get(t).tick == all_measures[i]) {
					time_measures[t] = i;
					break;
				}
			}
		}
		
		if(original != null)
			removeEvent(original);

		FWSTimeSignatureEvent current_event = null;
		if(new_event != null) {
			current_event = getTimeSignatureAt(new_event.tick);
			if(current_event != null && (current_event.tick == new_event.tick) && current_event != original)
				removeEvent(current_event);

			long[] new_measures = getMeasureTicks();
			for(int i=0;i<new_measures.length;i+=1) {
				if(new_measures[i] == new_event.tick)
					break;
				else if(new_measures[i] > new_event.tick && i > 0) {
					final long next_diff = new_measures[i] - new_event.tick, prev_diff = new_measures[i-1] - new_event.tick;
					if(next_diff < prev_diff)
						new_event.tick = new_measures[i];
					else
						new_event.tick = new_measures[i-1];
					break;
				}
			}

			addEvent(new_event);
		}

		if(shift_by_measure) {
			long[] new_measures = getMeasureTicks();
			for(int i=0;i<time_signatures.size();i+=1) {
				if(time_signatures.get(i) == current_event || time_signatures.get(i) == null)
					continue;

				if(time_measures[i] >= new_measures.length) {
					removeEvent(time_signatures.get(i));
				} else if(new_measures[time_measures[i]] != time_signatures.get(i).tick) {
					time_signatures.get(i).tick = new_measures[time_measures[i]];
					refreshEvent(time_signatures.get(i));
					refreshTimeSignatures(null, null, shift_by_measure); //Recursive fix.
					return;
				}
			}
		} else { //Round each signature to its nearest measure.
			for(int i=0;i<time_signatures.size();i+=1) {
				if(time_signatures.get(i) == current_event || time_signatures.get(i) == original || time_signatures.get(i) == null)
					continue;

				FWSTimeSignatureEvent ts = (FWSTimeSignatureEvent)time_signatures.get(i);
				removeEvent(time_signatures.get(i));

				long[] new_measures = getMeasureTicks();
				for(int m=0;m<new_measures.length;m+=1) {
					if(ts.tick < new_measures[m]) {
						ts.tick = new_measures[m > 0 ? (m-1) : m];
						addEvent(ts);
						break;
					} else if(time_signatures.get(i).tick == new_measures[m]) {
						addEvent(ts);
						break;
					}
				}
			}
		}
	}

	/** Get the end of track event. */
	public FWSEvent getEndEvent() {
		for(int c=0;c<channel_events.length;c+=1) {
			for(int e=0;e<channel_events[c].size();e+=1) {
				FWSEvent test_event = channel_events[c].get(e);
				if(test_event instanceof FWSMiscMIDIEvent) {
					FWSMiscMIDIEvent misc_event = (FWSMiscMIDIEvent)test_event;
					if(misc_event.type == 0x2F && misc_event.data.length == 0)
						return test_event;
				}
			}
		}

		for(int e=0;e<common_events.size();e+=1) {
			FWSEvent test_event = common_events.get(e);
			if(test_event instanceof FWSMiscMIDIEvent) {
				FWSMiscMIDIEvent misc_event = (FWSMiscMIDIEvent)test_event;
				if(misc_event.type == 0x2F && misc_event.data.length == 0)
					return test_event;
			}
		}

		return null;
	}

	/** Set the end of track event. */
	public void setEndEvent(final long end) {
		FWSEvent current_end = getEndEvent();
		if(current_end != null)
			removeEvent(current_end);

		FWSMiscMIDIEvent end_event = new FWSMiscMIDIEvent();
		end_event.tick = end;
		end_event.data = new byte[0];
		end_event.type = 0x2F;

		addEvent(end_event);
	}

	/** Get the list of channel events. */
	public ArrayList<FWSEvent> getChannelEvents(final int channel) {
		if(channel >= 0 && channel < channel_events.length)
			return channel_events[channel];
		else
			return new ArrayList<FWSEvent>(0);
	}

	/** Get the list of all channel events. */
	public ArrayList<FWSEvent> getChannelEvents() {
		ArrayList<FWSEvent> all_events = new ArrayList<FWSEvent>(0);

		for(int c=0;c<channel_events.length;c+=1) {
			ArrayList<FWSEvent> channel_events = this.channel_events[c];
			for(int i=0;i<channel_events.size();i+=1)
				FWSEvent.insertEvent(all_events, channel_events.get(i));
		}

		return all_events;
	}

	/** Get the list of common events. */
	public ArrayList<FWSEvent> getCommonEvents() {
		return common_events;
	}

	/** Return all events in the sequence. */
	public ArrayList<FWSEvent> getAllEvents() {
		ArrayList<FWSEvent> events = getChannelEvents();

		for(int i=0;i<common_events.size();i+=1)
			FWSEvent.insertEvent(events, common_events.get(i));

		return events;
	}

	/** Get the sequence length in ticks. */
	public long getSequenceLength() {
		long l = 0;
		for(int c=0;c<channel_events.length;c+=1) {
			for(int e=0;e<channel_events[c].size();e+=1) {
				FWSEvent test_event = channel_events[c].get(e);
				if(test_event instanceof FWSNoteEvent) {
					FWSNoteEvent test_note = (FWSNoteEvent)test_event;
					if(test_note.tick + test_note.duration > l)
						l = test_note.tick + test_note.duration;
				} else if(test_event.tick > l)
					l = test_event.tick;
			}
		}

		for(int e=0;e<common_events.size();e+=1) {
			FWSEvent test_event = common_events.get(e);
			if(test_event instanceof FWSNoteEvent) {
				FWSNoteEvent test_note = (FWSNoteEvent)test_event;
				if(test_note.tick + test_note.duration > l)
					l = test_note.tick + test_note.duration;
			} else if(test_event.tick > l)
				l = test_event.tick;
		}

		long align_tick = 0;
		while(align_tick < l)
			align_tick += tpq;

		return align_tick;
	}
	
	/** Get the tick of the final event in the sequence. */
	public long getSequenceLengthStrict() {
		long l = 0;
		for(int c=0;c<channel_events.length;c+=1) {
			for(int e=0;e<channel_events[c].size();e+=1) {
				FWSEvent test_event = channel_events[c].get(e);
				if(test_event.tick > l)
					l = test_event.tick;
			}
		}

		for(int e=0;e<common_events.size();e+=1) {
			FWSEvent test_event = common_events.get(e);
			if(test_event.tick > l)
				l = test_event.tick;
		}

		long align_tick = 0;
		while(align_tick < l)
			align_tick += tpq;

		return align_tick;
	}

	/** Get the piano-roll X position of a tick, with default pixels per quarter note defined as ppq. */
	public int getXPosition(final long tick, final int ppq) {
		int x_pos = 0;
		long last_tick = 0;
		double last_tempo = 120.0;
		for(int i=0;i<common_events.size();i+=1) {
			if(common_events.get(i) instanceof FWSTempoEvent) {
				FWSTempoEvent tempo_event = (FWSTempoEvent)common_events.get(i);
				if(tempo_event.tick > tick) {
					x_pos += (tick - last_tick)*120.0/last_tempo*ppq/tpq;
					return x_pos;
				}

				x_pos += (tempo_event.tick - last_tick)*120.0/last_tempo*ppq/tpq;

				last_tempo = tempo_event.tempo;
				if(last_tempo == 0)
					last_tempo = 1;

				last_tick = tempo_event.tick;
			}
		}
		
		if(tick > last_tick) {
			x_pos += (tick - last_tick)*120.0/last_tempo*ppq/tpq;
			return x_pos;
		}

		return x_pos;
	}

	/** Get the tick from a piano-roll X position. */
	public long getXTime(final int x, final int ppq) {
		int x_pos = 0;
		long last_tick = 0;
		int last_tempo = 120;
		for(int i=0;i<common_events.size();i+=1) {
			if(common_events.get(i) instanceof FWSTempoEvent) {
				FWSTempoEvent tempo_event = (FWSTempoEvent)common_events.get(i);
				if(getXPosition(tempo_event.tick, ppq) > x) {
					return (long)(last_tick + (x-x_pos)*tpq/getAbsoluteWidthAt(last_tick, ppq));
				}

				x_pos += (tempo_event.tick - last_tick)*120/last_tempo*ppq/tpq;

				last_tempo = tempo_event.tempo;
				if(last_tempo == 0)
					last_tempo = 1;

				last_tick = tempo_event.tick;
			}
		}

		if(x > x_pos) {
			return (long)((x-x_pos)*tpq/getAbsoluteWidthAt(last_tick, ppq) + last_tick);
		} else
			return last_tick;
	}

	/** Get an event graphic width at a certain tick. */
	public int getWidthAt(final long tick, final int ppq) {
		return (int)getAbsoluteWidthAt(tick, ppq);
	}

	/** Get an event graphic width at a certain tick. */
	private double getAbsoluteWidthAt(final long tick, final int ppq) {
		FWSTempoEvent tempo_at = getTempoAt(tick);
		if(tempo_at == null)
			return ppq;
		
		if(ppq*120/tempo_at.tempo <= 2)
			return 2;

		return ppq*120.0/tempo_at.tempo;
	}

	/** Load events from another FWS squeence. */
	public void addEventsFromFWSSequence(FWSSequence new_sequence, final boolean extend) {
		ArrayList<FWSEvent> sequence_events = new_sequence.getAllEvents();
		if(new_sequence.tpq != this.tpq) {
			sequence_events = FWSEvent.createCopy(sequence_events);
			for(int i=0;i<sequence_events.size();i+=1) {
				sequence_events.get(i).tick = sequence_events.get(i).tick*this.tpq/new_sequence.tpq;
				if(sequence_events.get(i) instanceof FWSNoteEvent) {
					FWSNoteEvent note_event = (FWSNoteEvent)sequence_events.get(i);
					note_event.duration = note_event.duration*this.tpq/new_sequence.tpq;
				}
			}
		}

		long new_len = 0;
		for(int i=0;i<sequence_events.size();i+=1) {
			this.addEvent(sequence_events.get(i));
			if(sequence_events.get(i).tick > new_len)
				new_len = sequence_events.get(i).tick;
			if(sequence_events.get(i) instanceof FWSNoteEvent) {
				FWSNoteEvent note_event = (FWSNoteEvent)sequence_events.get(i);
				if(note_event.tick + note_event.duration > new_len)
					new_len = note_event.tick + note_event.duration;
			}
		}

		if(extend)
			this.setEndEvent(new_len);
	}

	/** Load events into the sequence from recorded bytes. */
	public void addEventsFromInput(byte[][] midi_data, long[] timestamps, RecordLoadOptions options) {
		if(midi_data.length != timestamps.length)
			return;

		boolean notes_on[][] = new boolean[128][16];
		FWSNoteEvent cache_on[][] = new FWSNoteEvent[128][16];

		for(int j=0;j<16;j+=1)
		{
			for(int i=0;i<128;i+=1) {
				notes_on[i][j] = false;
				cache_on[i][j] = null;
			}
		}

		ArrayList<FWSNoteEvent> note_cache = new ArrayList<>(0);
		ArrayList<FWSVoiceEvent> voice_cache = new ArrayList<FWSVoiceEvent>(0);
		
		ArrayList<FWSEvent> event_list = new ArrayList<>(0);

		long first_tick = timestamps.length > 0 ? timestamps[0] : 0;
		for(int i=0;i<timestamps.length;i+=1) {
			if(timestamps[i] < first_tick)
				first_tick = timestamps[i];
		}

		long[] ticks = new long[timestamps.length];
		
		ArrayList<Long> upt_stamps = new ArrayList<>();
		ArrayList<Long> tick_stamps = new ArrayList<>();
		ArrayList<Double> upt_values = new ArrayList<>();

		{
			long last_time = 0, last_tick = 0;
			double last_upt = 60000000.0/120/tpq;

			for(int i=0;i<common_events.size();i+=1) {
				if(common_events.get(i) instanceof FWSTempoEvent) {
					FWSTempoEvent tempo = (FWSTempoEvent)common_events.get(i);
					last_time += (tempo.tick - last_tick)*last_upt;

					upt_values.add(Double.valueOf(60000000.0/tempo.tempo/tpq));
					last_tick = tempo.tick;
					last_upt = 60000000.0/tempo.tempo/tpq;

					upt_stamps.add(last_time);
					tick_stamps.add(tempo.tick);
				}
			}
		}

		/*if(init_upt <= 0) {
			long next_stamp = upt_stamps.size() > 0 ? upt_stamps.get(0) : 0, last_stamp = next_stamp;
			long last_tick = tick_stamps.size() > 0 ? tick_stamps.get(0) : 0, next_tick = last_tick;
			double upt = upt_values.size() > 0 ? upt_values.get(0) : 60000000.0/120/tpq, next_upt = upt;
			for(int t=0;t<timestamps.length;t+=1) {
				final long stamp = timestamps[t] - first_tick;

				ticks[t] = (long)((stamp-last_stamp)/upt) + last_tick;

				if(next_stamp >= 0 && stamp > next_stamp) {
					last_stamp = next_stamp;
					last_tick = next_tick;
					upt = next_upt;
					next_stamp = -1;
					for(int i=0;i<upt_stamps.size();i+=1) {
						if(upt_stamps.get(i) > stamp) {
							next_stamp = upt_stamps.get(i);
							next_upt = upt_values.get(i);
							next_tick = tick_stamps.get(i);
							break;
						}
					}
				}
			}
		} else {*/
		if(options.midi_clock) {
			long current_fws_tick = 0;//, next_tick = tick_stamps.size() > 0 ? tick_stamps.get(0) : 0;
			//double upt = upt_values.size() > 0 ? upt_values.get(0) : 60000000.0/120/tpq;
			for(int t=0;t<timestamps.length;t+=1) {
				current_fws_tick = (long)((timestamps[t] - first_tick)/24.0*tpq);
				ticks[t] = current_fws_tick;
			}
				/*if(current_fws_tick > next_tick && next_tick >= 0) {
					boolean next_tick_found = false;
					for(int i=0;i<tick_stamps.size();i+=1) {
						if(tick_stamps.get(i) > next_tick) {
							next_tick = tick_stamps.get(i);
							upt = upt_values.get(i);
							next_tick_found = true;
							break;
						}
					}
					if(!next_tick_found)
						next_tick = -1;
				}*/
		} else {
			for(int t=0;t<timestamps.length;t+=1)
				ticks[t] = timestamps[t];
		}
		//}
	
		for(int b=0;b<midi_data.length && b<ticks.length; b+=1) {
			byte[] d = midi_data[b];
			
			if(d.length == 3 && (d[0]&0xE0) == 0x80) { //Note event.
				if((d[0]&0xF0) == 0x90 && d[2] > 0) { //Note on.
					final byte note = d[1], channel = (byte)(d[0]&0xF), velocity = d[2];
					if(notes_on[note][channel]) {
						notes_on[note][channel] = false;

						FWSNoteEvent test_note = new FWSNoteEvent();
						test_note.channel = channel;
						test_note.note = note;
						
						FWSNoteEvent note_ptr = cache_on[note][channel];
						test_note.velocity = note_ptr.velocity;

						test_note.tick = note_ptr.tick;
						test_note.duration = ticks[b] - note_ptr.tick;

						cache_on[note][channel] = null;
						note_cache.remove(note_ptr);
						event_list.add(test_note);
					}

					FWSNoteEvent test_note = new FWSNoteEvent();
					test_note.note = note;
					test_note.channel = channel;
					test_note.velocity = velocity;
					test_note.tick = ticks[b];

					notes_on[note][channel] = true;
					note_cache.add(test_note);
					cache_on[note][channel] = test_note;
				} else { //Note off.
					final byte note = d[1], channel = (byte) (d[0]&0xF);
					notes_on[note][channel] = false;

					FWSNoteEvent test_note = new FWSNoteEvent();
					test_note.channel = channel;
					test_note.note = note;
					
					FWSNoteEvent note_ptr = cache_on[note][channel];
					if(note_ptr == null)
						continue;
					
					test_note.velocity = note_ptr.velocity;

					test_note.tick = note_ptr.tick;
					test_note.duration = ticks[b] - note_ptr.tick;

					cache_on[note][channel] = null;
					note_cache.remove(note_ptr);
					event_list.add(test_note);
				}
			} else if((d[0]&0xF0) != 0xF0) { //Short event.
				if((d[0]&0xF0) == 0xC0 && d.length >=2) { //Voice.
					FWSVoiceEvent voice_event = new FWSVoiceEvent();
					voice_event.channel = (byte)(d[0]&0xF);
					voice_event.voice = d[1];
					voice_event.tick = ticks[b];

					FWSShortEvent voice_msb = null, voice_lsb = null;
					for(int i=event_list.size()-1;i>=0;i-=1) {
						if(event_list.get(i) instanceof FWSShortEvent) {
							FWSShortEvent test_event = (FWSShortEvent)event_list.get(i);
							if(test_event.channel == voice_event.channel && test_event.command == (byte)0xB0 && test_event.data1 == 0x0 && voice_msb == null)
								voice_msb = test_event;
							else if(test_event.channel == voice_event.channel && test_event.command == (byte)0xB0 && test_event.data1 == 0x20 && voice_lsb == null)
								voice_lsb = test_event;

							if(voice_msb != null && voice_lsb != null)
								break;
						}
					}

					if(voice_msb != null && voice_lsb != null) {
						event_list.remove(voice_msb);
						event_list.remove(voice_lsb);

						voice_event.voice_msb = voice_msb.data2;
						voice_event.voice_lsb = voice_lsb.data2;

						event_list.add(voice_event);
					} else {
						voice_event.voice_msb = -1;
						voice_event.voice_lsb = -1;
						if(voice_msb != null) {
							voice_event.voice_msb = voice_msb.data2;
							event_list.remove(voice_msb);
						}
						if(voice_lsb != null) {
							voice_event.voice_lsb = voice_lsb.data2;
							event_list.remove(voice_lsb);
						}
						voice_cache.add(voice_event);
					}
				} else { //Other short event.
					if((d[0]&0xF0) == 0xB0 && d.length >=3) { //Control. Possible voice.
						final byte channel = (byte)(d[0]&0xF);
						boolean add_event = true;
						if(d[1] == 0x0 || d[1] == 0x20) { //Voice. Assess.
							for(int i=voice_cache.size()-1;i>=0;i-=1) {
								if(voice_cache.get(i).channel == channel && voice_cache.get(i).tick > ticks[b]-tpq/64) {
									FWSVoiceEvent voice_event = voice_cache.get(i);
									add_event = false;
									if(d[1] == 0x0)
										voice_event.voice_msb = d[2];
									else if(d[1] == 0x20)
										voice_event.voice_lsb = d[2];

									if(voice_event.voice_lsb >= 0 && voice_event.voice_msb >= 0) {
										voice_cache.remove(voice_event);
										event_list.add(voice_event);
									}
									break;
								}
							}
						}

						if(add_event) {
							FWSShortEvent short_event = new FWSShortEvent();
							short_event.tick = ticks[b];
							short_event.channel = channel;
							short_event.command = (byte)0xB0;
							short_event.data1 = d[1];
							short_event.data2 = d[2];

							event_list.add(short_event);
						}
					} else {
						FWSShortEvent short_event = new FWSShortEvent();
						short_event.tick = ticks[b];
						short_event.channel = (byte)(d[0]&0xF);
						short_event.command = (byte)(d[0]&0xF0);
						short_event.data1 = d[1];
						if(d.length >= 3)
							short_event.data2 = d[2];

						event_list.add(short_event);
					}
				}
			} else {
				if(d.length > 0 && d[0] == (byte)0xF0) { //Sysex.
					byte[] data = new byte[d.length-1];
					for(int i=0;i<d.length-1;i+=1)
						data[i] = d[i+1];

					FWSSysexEvent sysex_event = new FWSSysexEvent();
					sysex_event.tick = ticks[b];
					sysex_event.data = data;
					event_list.add(sysex_event);
				} else if(d.length > 2 && d[0] == (byte)0xFF) { //Meta.
					final byte type = d[1];
					if(type == 0x51 && d.length >= 6) { //Tempo.
						final float new_tempo = (float)(1.0/(((d[3]&0xFF)*0x10000 +
														(d[4]&0xFF)*0x100 +
														d[5])/1E6/60));

						FWSTempoEvent tempo_event = new FWSTempoEvent();
						tempo_event.tempo = (int)new_tempo;
						tempo_event.tick = ticks[b];
						event_list.add(tempo_event);
					} else if(type == 0x58 && d.length >= 7) { //Time signature.
						final byte num = d[3], den = d[4];
						FWSTimeSignatureEvent time_event = new FWSTimeSignatureEvent();
						time_event.num = num;
						time_event.den = den;
						time_event.tick = ticks[b];
						event_list.add(time_event);
					} else if(type == 0x59 && d.length >= 5) { //Key signature.
						final byte accidental_count = d[3];
						final boolean major = d[4] == 0;

						FWSKeySignatureEvent key_event = new FWSKeySignatureEvent();
						key_event.accidental_count = accidental_count;
						key_event.major = major;
						key_event.tick = ticks[b];
						event_list.add(key_event);
					} else {
						FWSMiscMIDIEvent new_event = new FWSMiscMIDIEvent();
						new_event.tick = ticks[b];
						new_event.type = type;
						new_event.data = new byte[d.length - 3];
						for(int i=0;i<new_event.data.length;i+=1)
							new_event.data[i] = d[i+3];

						event_list.add(new_event);
					}
				}
			}
		}

		for(int i=0;i<voice_cache.size();i+=1) {
			FWSVoiceEvent voice = voice_cache.get(i);
			if(voice.voice_lsb < 0)
				voice.voice_lsb = 0;
			if(voice.voice_msb < 0)
				voice.voice_msb = 0;

			event_list.add(voice);
		}

		long first_note = 0;
		for(int i=0;i<event_list.size();i+=1) {
			if(event_list.get(i) instanceof FWSNoteEvent) {
				final byte channel = ((FWSNoteEvent)event_list.get(i)).channel;
				if(options.channel_map[channel] < 0)
					continue;

				first_note = event_list.get(i).tick;
				break;
			}
		}
		
		for(int i=0;i<event_list.size();i+=1) {
			if(event_list.get(i) instanceof FWSNoteEvent && event_list.get(i).tick < first_note) {
				final byte channel = ((FWSNoteEvent)event_list.get(i)).channel;
				if(options.channel_map[channel] < 0)
					continue;

				first_note = event_list.get(i).tick;
			}
		}
		
		boolean first_note_triggered = false;
		final long org_len = getSequenceLengthStrict();

		for(int e=0;e<event_list.size();e+=1) {
			if(event_list.get(e) instanceof FWSNoteEvent && !options.import_notes)
				continue;
			else if(event_list.get(e) instanceof FWSVoiceEvent && !options.import_voice)
				continue;
			else if(event_list.get(e) instanceof FWSShortEvent && !options.import_short)
				continue;
			else if(event_list.get(e) instanceof FWSTempoEvent && !options.import_tempo)
				continue;
			else if(event_list.get(e) instanceof FWSTimeSignatureEvent && !options.import_time)
				continue;
			else if(event_list.get(e) instanceof FWSKeySignatureEvent && !options.import_key)
				continue;
			else if(event_list.get(e) instanceof FWSSysexEvent && !options.import_sysex)
				continue;
			else if(event_list.get(e) instanceof FWSMiscMIDIEvent && !options.import_other)
				continue;

			FWSEvent event = event_list.get(e);
			if(event instanceof FWSNoteEvent) {
				first_note_triggered = true;
				FWSNoteEvent note_event = (FWSNoteEvent)event;
				if(options.channel_map[note_event.channel] < 0)
					continue;
				else
					note_event.channel = options.channel_map[note_event.channel];
			} else if(event instanceof FWSShortEvent) {
				FWSShortEvent short_event = (FWSShortEvent)event;
				if(options.channel_map[short_event.channel] < 0)
					continue;
				else
					short_event.channel = options.channel_map[short_event.channel];
			} else if(event instanceof FWSVoiceEvent) {
				FWSVoiceEvent voice_event = (FWSVoiceEvent)event;
				if(options.channel_map[voice_event.channel] < 0)
					continue;
				else
					voice_event.channel = options.channel_map[voice_event.channel];
			}

			if(options.start_notes_at_zero)
				event.tick += -first_note + options.timeshift;
			
			if(!first_note_triggered && options.add_shorts_to_beginning && !(event instanceof FWSNoteEvent) && event.tick < first_note)
				event.tick = 0;
			
			if(event.tick < 0) {
				if(options.extend)
					event.tick = 0;
				else
					continue;
			}

			if(!options.extend && event.tick >= org_len)
				continue;

			if(event instanceof FWSNoteEvent) {
				FWSNoteEvent note_event = (FWSNoteEvent)event;
				if(!options.extend && note_event.tick + note_event.duration >= org_len) {
					note_event.duration = org_len - note_event.tick - 1;
					if(note_event.duration < 0)
						continue;
				}
			}

			this.addEvent(event_list.get(e));
		}

		if(options.extend)
			setEndEvent(getSequenceLength());
	}

	/** Get a sequence of all MIDI events in this FWS sequence. */
	public Sequence getMidiSequence() {
		try {
			Sequence new_sequence = new Sequence(div_type, tpq);
			getMidiSequence(new_sequence);

			return new_sequence;
		} catch (InvalidMidiDataException e) {
			return null;
		}
	}

	/** Add all events to a true MIDI sequence. */
	public void getMidiSequence(Sequence sequence) {
		Track seq_track;
		Track[] tracks = sequence.getTracks();
		if(tracks.length <= 0)
			seq_track = sequence.createTrack();
		else
			seq_track = tracks[0];

		for(int i=0;i<this.common_events.size();i+=1) {
			MidiEvent[] event_events = common_events.get(i).getMIDIEvents();
			for(int e=0;e<event_events.length;e+=1)
				seq_track.add(event_events[e]);
		}

		for(int c=0;c<this.channel_events.length;c+=1) {
			ArrayList<FWSEvent> channel_events = this.channel_events[c];
			for(int i=0;i<channel_events.size();i+=1) {
				MidiEvent[] event_events = channel_events.get(i).getMIDIEvents();
				for(int e=0;e<event_events.length;e+=1)
					seq_track.add(event_events[e]);
			}
		}
	}

	/** Convert MIDI data to FWS format. */
	public static FWSSequence getFWSSequencefromSequence(File song_file, JFrame main_window) {
		//To keep track of which notes are "pressed" and which aren't.
		boolean notes_on[][] = new boolean[128][16];
		FWSNoteEvent cache_on[][] = new FWSNoteEvent[128][16];
	
		ArrayList<FWSNoteEvent> note_cache = new ArrayList<FWSNoteEvent>(0);
		ArrayList<FWSVoiceEvent> voice_cache = new ArrayList<FWSVoiceEvent>(0);
		FWSSequence midi_sequence = new FWSSequence();
		
		for(int j=0;j<16;j+=1)
		{
			for(int i=0;i<128;i+=1) {
				notes_on[i][j] = false;
				cache_on[i][j] = null;
			}
		}
	
		try {
			Sequence loaded_sequence = MidiSystem.getSequence(song_file);
			Track[] tracks = loaded_sequence.getTracks();
	
			midi_sequence.setTPQ(loaded_sequence.getResolution());
			midi_sequence.setDivType(loaded_sequence.getDivisionType());
			
			for(int t=0;t<tracks.length;t+=1) {
				for(int e=0;e<tracks[t].size();e+=1) {
					MidiEvent ev = tracks[t].get(e);
					MidiMessage msg = ev.getMessage();
	
					if(msg instanceof ShortMessage) {
						ShortMessage short_msg = (ShortMessage)msg;
						if(short_msg.getCommand() == ShortMessage.NOTE_ON && short_msg.getData2() > 0) { //Note depressed.
							final byte note = (byte)short_msg.getData1(), channel = (byte)short_msg.getChannel(), velocity = (byte)short_msg.getData2();
							
							if(notes_on[note][channel]) {
								notes_on[note][channel] = false;
	
								FWSNoteEvent test_note = new FWSNoteEvent();
								test_note.channel = channel;
								test_note.note = note;
								
								FWSNoteEvent note_ptr = cache_on[note][channel];
								test_note.velocity = note_ptr.velocity;
	
								test_note.tick = note_ptr.tick;
								test_note.duration = ev.getTick() - note_ptr.tick;
	
								cache_on[note][channel] = null;
								note_cache.remove(note_ptr);
								midi_sequence.addEvent(test_note);
							}
	
							FWSNoteEvent test_note = new FWSNoteEvent();
							test_note.note = note;
							test_note.channel = channel;
							test_note.velocity = velocity;
							test_note.tick = ev.getTick();
	
							notes_on[note][channel] = true;
							note_cache.add(test_note);
							cache_on[note][channel] = test_note;
						} else if(short_msg.getCommand() == ShortMessage.NOTE_OFF || (short_msg.getCommand() == ShortMessage.NOTE_ON && short_msg.getData2() <= 0)) { //Note released.
							final byte note = (byte)short_msg.getData1(), channel = (byte)short_msg.getChannel();
							
							if(notes_on[note][channel]) {
								notes_on[note][channel] = false;
	
								FWSNoteEvent test_note = new FWSNoteEvent();
								test_note.channel = channel;
								test_note.note = note;
								
								FWSNoteEvent note_ptr = cache_on[note][channel];
								test_note.velocity = note_ptr.velocity;
	
								test_note.tick = note_ptr.tick;
								test_note.duration = ev.getTick() - note_ptr.tick;
	
								cache_on[note][channel] = null;
								note_cache.remove(note_ptr);
								midi_sequence.addEvent(test_note);
							}
						} else if(short_msg.getCommand() == ShortMessage.PROGRAM_CHANGE) { //Voice change.
							FWSVoiceEvent voice_event = new FWSVoiceEvent();
							voice_event.channel = (byte)short_msg.getChannel();
							voice_event.voice = (byte)short_msg.getData1();
							voice_event.tick = ev.getTick();
	
							FWSShortEvent voice_msb = midi_sequence.getControlEventAt(ev.getTick(), (byte)short_msg.getChannel(), (byte)0),
											voice_lsb = midi_sequence.getControlEventAt(ev.getTick(), (byte)short_msg.getChannel(), (byte)32);
	
							if(voice_msb != null && voice_lsb != null) {
								voice_event.voice_lsb = voice_lsb.data2;
								voice_event.voice_msb = voice_msb.data2;
								midi_sequence.removeEvent(voice_lsb);
								midi_sequence.removeEvent(voice_msb);
								midi_sequence.addEvent(voice_event);
							} else {
								if(voice_lsb != null) {
									voice_event.voice_lsb = voice_lsb.data2;
									midi_sequence.removeEvent(voice_lsb);
								} else
									voice_event.voice_lsb = -1;
	
								if(voice_msb != null) {
									voice_event.voice_msb = voice_msb.data2;
									midi_sequence.removeEvent(voice_msb);
								} else
									voice_event.voice_msb = -1;
	
								voice_cache.add(voice_event);
							}
	
						} else { //Other short message.
							FWSShortEvent short_event = new FWSShortEvent();
							short_event.channel = (byte)short_msg.getChannel();
							short_event.command = (byte)short_msg.getCommand();
							short_event.data1 = (byte)short_msg.getData1();
							short_event.data2 = (byte)short_msg.getData2();
							short_event.tick = ev.getTick();
	
							FWSVoiceEvent voice_match = null;
							if(short_msg.getCommand() == ShortMessage.CONTROL_CHANGE && short_msg.getData1() == 0 || short_msg.getData1() == 32) { //Voice change. Match if possible.
								for(int i=0;i<voice_cache.size();i+=1) {
									if(voice_cache.get(i).channel == short_event.channel && voice_cache.get(i).tick == short_event.tick) {
										voice_match = voice_cache.get(i);
									}
								}
							}
	
							if(voice_match != null) {
								if(short_msg.getData1() == 0)
									voice_match.voice_msb = (byte)short_msg.getData2();
								else if(short_msg.getData1() == 32)
									voice_match.voice_lsb = (byte)short_msg.getData2();
	
								if(voice_match.voice_lsb >= 0 && voice_match.voice_msb >= 0) {
									midi_sequence.addEvent(voice_match);
									voice_cache.remove(voice_match);
								}
							} else 
								midi_sequence.addEvent(short_event);
						}
					} else if(msg instanceof MetaMessage) {
						MetaMessage meta_msg = (MetaMessage)msg;
						byte[] meta_data = meta_msg.getData();
						if(meta_msg.getType() == 0x58) { //Time signature.
							FWSTimeSignatureEvent time_signature_event = new FWSTimeSignatureEvent();
							time_signature_event.num = meta_data[0];
							time_signature_event.den = meta_data[1];
							time_signature_event.tick = ev.getTick();
	
							{
								FWSTimeSignatureEvent test_event = midi_sequence.getTimeSignatureAt(time_signature_event.tick);
								if(test_event != null && test_event.tick == time_signature_event.tick)
									midi_sequence.removeEvent(test_event);
							}
							
							midi_sequence.addEvent(time_signature_event);
						} else if(meta_msg.getType() == 0x59) { //Key signature.
							FWSKeySignatureEvent key_signature_event = new FWSKeySignatureEvent();
							key_signature_event.accidental_count = meta_data[0];
							key_signature_event.major = (meta_data[1]&0b1) != 1;
							key_signature_event.tick = ev.getTick();
	
							{
								FWSKeySignatureEvent test_event = midi_sequence.getKeySignatureAt(key_signature_event.tick);
								if(test_event != null && test_event.tick == key_signature_event.tick)
									midi_sequence.removeEvent(test_event);
							}
	
							midi_sequence.addEvent(key_signature_event);
						} else if(meta_msg.getType() == 0x51) { //Tempo.
							final float new_tempo = (float)(1.0/(((meta_data[0]&0xFF)*0x10000 +
									(meta_data[1]&0xFF)*0x100 +
									meta_data[2])/1E6/60));
							
							FWSTempoEvent tempo_event = new FWSTempoEvent();
							tempo_event.tempo = (int)(new_tempo + 0.5);
							tempo_event.tick = ev.getTick();
	
							{
								FWSTempoEvent test_event = midi_sequence.getTempoAt(tempo_event.tick);
								if(test_event != null && test_event.tick == tempo_event.tick)
									midi_sequence.removeEvent(test_event);
							}
	
							midi_sequence.addEvent(tempo_event);
						} else { //Misc MIDI.
							FWSMiscMIDIEvent midi_event = new FWSMiscMIDIEvent();
	
							midi_event.type = meta_msg.getType();
	
							midi_event.data = meta_data;
							midi_event.tick = ev.getTick();
							
							if(meta_data.length == 0 && meta_msg.getType() == 0x2F) { //End event.
								FWSEvent end_event = midi_sequence.getEndEvent();
								if(end_event != null)
									midi_sequence.removeEvent(end_event);
							}
	
							midi_sequence.addEvent(midi_event);
						}
					} else if(msg instanceof SysexMessage) {
						SysexMessage sysex_msg = (SysexMessage)msg;
	
						FWSSysexEvent sysex_event = new FWSSysexEvent();
						sysex_event.data = sysex_msg.getData();
						sysex_event.status = sysex_msg.getStatus();
	
						sysex_event.tick = ev.getTick();
	
						midi_sequence.addEvent(sysex_event);
					}
				}
			}
	
			for(int i=0;i<voice_cache.size();i+=1) {
				FWSVoiceEvent voice_event = voice_cache.get(i);
				
				if(voice_event.voice_lsb < 0)
					voice_event.voice_lsb = 0;
				if(voice_event.voice_msb < 0)
					voice_event.voice_msb = (byte)(voice_event.channel == 9 ? 127 : 0);
	
				midi_sequence.addEvent(voice_event);
			}
	
			if(midi_sequence.getEndEvent() == null) {
				midi_sequence.setEndEvent(midi_sequence.getSequenceLength());
			} else {
				if(midi_sequence.getEndEvent().tick < midi_sequence.getSequenceLength())
					midi_sequence.setEndEvent(midi_sequence.getSequenceLength());
			}
	
		} catch (InvalidMidiDataException e) {
			JOptionPane.showMessageDialog(main_window, "Invalid or corrupted MIDI data!", "Error", JOptionPane.ERROR_MESSAGE);
		} catch(IOException e) {
	
		}
		
		return midi_sequence;
	}
}
