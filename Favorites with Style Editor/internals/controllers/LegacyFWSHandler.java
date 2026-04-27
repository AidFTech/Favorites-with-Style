package controllers;

import java.util.ArrayList;
import java.util.Arrays;

import javax.sound.midi.ShortMessage;

import fwsevents.FWSChordEvent;
import fwsevents.FWSEvent;
import fwsevents.FWSKeySignatureEvent;
import fwsevents.FWSMiscMIDIEvent;
import fwsevents.FWSNoteEvent;
import fwsevents.FWSSectionNameEvent;
import fwsevents.FWSSequence;
import fwsevents.FWSShortEvent;
import fwsevents.FWSStyleChangeEvent;
import fwsevents.FWSSysexEvent;
import fwsevents.FWSTempoEvent;
import fwsevents.FWSTimeSignatureEvent;
import fwsevents.FWSVoiceEvent;
import style.Casm;
import style.ChordBody;
import style.Style;

public class LegacyFWSHandler {
	/** Get the long MIDI format of a set of bytes. */
	public static long longMidiFormat(byte[] data) {
		long r = 0;
		int i;
		
		for(i=0;i<data.length;i+=1) {
			r = r << 7;
			r += data[i]&0b1111111;
		}
		return r;
	}

	/** Grab legacy FWS events. */
	public static ArrayList<FWSEvent> grabEvents(byte[] data) {
		ArrayList<FWSEvent> events = new ArrayList<FWSEvent>(0);
		ArrayList<FWSVoiceEvent> voice_cache = new ArrayList<FWSVoiceEvent>(0);
		long tick = 0;

		int i=0;
		while(i<data.length) {
			if((data[i]&0xF0) == 0x90) { //New note event.
				final byte channel = (byte) (data[i]&0xF), note = data[i+1], velocity = data[i+2];
				i+=3;

				int j=i;
				while(i<data.length && (data[i]&0b10000000) != 0) {
					i += 1;
				}
				final long d = longMidiFormat(Arrays.copyOfRange(data, j, i+1));
				i+=1;
				j = i;
				while(i<data.length && (data[i]&0b10000000) != 0) {
					i += 1;
				}
				tick += longMidiFormat(Arrays.copyOfRange(data, j, i+1));

				FWSNoteEvent note_event = new FWSNoteEvent();
				note_event.tick = tick;
				note_event.channel = channel;
				note_event.note = note;
				note_event.velocity = velocity;
				note_event.duration = d;

				events.add(note_event);
			} else if((data[i]&0xF0) == 0xC0) { //Voice change.
				final byte channel = (byte) (data[i]&0xF), voice = data[i+1];
				i+=2;

				final int j=i;
				while(i<data.length && (data[i]&0b10000000) != 0) {
					i += 1;
				}
				tick += longMidiFormat(Arrays.copyOfRange(data, j, i+1));

				FWSVoiceEvent new_voice = new FWSVoiceEvent();
				new_voice.tick = tick;
				new_voice.voice = voice;
				new_voice.channel = channel;
				new_voice.voice_lsb = -1;
				new_voice.voice_msb = -1;

				for(int e=0;e<events.size();e+=1) {
					if(events.get(e) instanceof FWSShortEvent) {
						FWSShortEvent se = (FWSShortEvent)events.get(e);
						if(se.channel != channel)
							continue;

						if(se.tick > tick)
							continue;
						
						if((se.command&0xF0) != ShortMessage.CONTROL_CHANGE)
							continue;

						if(se.data1 != 0x0 && se.data1 != 0x20)
							continue;

						if(se.data1 == 0x0) { //MSB.
							new_voice.voice_msb = se.data2;
							events.remove(se);
							e -= 1;
						} else if(se.data1 == 0x20) { //LSB.
							new_voice.voice_lsb = se.data2;
							events.remove(se);
							e -= 1;
						}

						if(new_voice.voice_msb >= 0 && new_voice.voice_lsb >= 0) {
							events.add(new_voice);
							break;
						}
					}
				}
			
				if(new_voice.voice_lsb < 0 || new_voice.voice_msb < 0)
					voice_cache.add(new_voice);
			} else if((data[i]&0xF0) == 0xB0) { //Control change.
				final byte channel = (byte) (data[i]&0xF), control = data[i+1], value = data[i+2];
				i+=3;

				final int j=i;
				while(i<data.length && (data[i]&0b10000000) != 0) {
					i += 1;
				}
				tick += longMidiFormat(Arrays.copyOfRange(data, j, i+1));

				FWSShortEvent short_event = new FWSShortEvent();
				short_event.channel = channel;
				short_event.data1 = control;
				short_event.data2 = value;
				short_event.command = (byte)0xB0;
				short_event.tick = tick;

				boolean add = true;

				if(control == 0x0 || control == 0x20) { //Voice LSB or MSB.
					for(int e=0;e<voice_cache.size();e+=1) {
						if(voice_cache.get(e).channel != channel || voice_cache.get(e).tick != tick)
							continue;
						
						if(control == 0x0 && voice_cache.get(e).voice_msb >= 0)
							continue;
						else if(control == 0x20 && voice_cache.get(e).voice_lsb >= 0)
							continue;

						if(control == 0x0)
							voice_cache.get(e).voice_msb = value;
						else if(control == 0x20)
							voice_cache.get(e).voice_lsb = value;

						add = false;

						if(voice_cache.get(e).voice_lsb >= 0 && voice_cache.get(e).voice_msb >= 0) {
							events.add(voice_cache.get(e));
							voice_cache.remove(e);
						}

						break;
					}
				}

				if(add)
					events.add(short_event);
			} else if((data[i]&0xF0) == 0xA0 || (data[i]&0xF0) == 0xE0) { //Two-byte short.
				final byte channel = (byte) (data[i]&0xF), data1 = data[i+1], data2 = data[i+2];
				i+=3;

				final int j=i;
				while(i<data.length && (data[i]&0b10000000) != 0) {
					i += 1;
				}
				tick += longMidiFormat(Arrays.copyOfRange(data, j, i+1));

				FWSShortEvent short_event = new FWSShortEvent();
				short_event.channel = channel;
				short_event.data1 = data1;
				short_event.data2 = data2;
				short_event.command = (byte)(data[i]&0xF0);
				short_event.tick = tick;
				events.add(short_event);
			} else if((data[i]&0xF0) == 0xD0) { //One-byte short.
				final byte channel = (byte) (data[i]&0xF), data1 = data[i+1];
				i+=2;

				final int j=i;
				while(i<data.length && (data[i]&0b10000000) != 0) {
					i += 1;
				}
				tick += longMidiFormat(Arrays.copyOfRange(data, j, i+1));

				FWSShortEvent short_event = new FWSShortEvent();
				short_event.channel = channel;
				short_event.data1 = data1;
				short_event.data2 = 0;
				short_event.command = (byte)(data[i]&0xF0);
				short_event.tick = tick;
				events.add(short_event);
			} else if(data[i] == (byte)0xFF) { //Meta
				final byte type = data[i+1];
				if(type == (byte)0x58) { //Time signature.
					final byte n = data[i+3]; //Num.
					final byte d =  data[i+4]; //Den.
					i += 5;
					final int j=i;
					while(i<data.length && (data[i]&0b10000000) != 0) {
						i += 1;
					}
					tick += longMidiFormat(Arrays.copyOfRange(data, j, i+1));

					FWSTimeSignatureEvent time_event = new FWSTimeSignatureEvent();
					time_event.tick = tick;
					time_event.num = n;
					time_event.den = d;

					events.add(time_event);
				} else if(type == (byte)0x51) { //Tempo.
					final byte t1 = data[i+3];
					final byte t2 = data[i+4];
					final byte t3 = data[i+5];
					final float new_tempo = (float)(1.0/(((t1&0xFF)*0x10000 +
							(t2&0xFF)*0x100 +
							t3)/1E6/60));
					final int tempo = (int)(new_tempo + 0.5);
					i += 6;
					final int j=i;
					while(i<data.length && (data[i]&0b10000000) != 0) {
						i += 1;
					}
					tick += longMidiFormat(Arrays.copyOfRange(data, j, i+1));
					
					FWSTempoEvent tempo_event = new FWSTempoEvent();
					tempo_event.tick = tick;
					tempo_event.tempo = tempo;
					
					events.add(tempo_event);
				} else if(data[i+1] == (byte)0x59) { //Key signature.
					final byte acc = data[i+3]; //Accidental count.
					final boolean major = data[i+4] == 0; //Minor if 1.
					i += 5;
					final int j=i;
					while(i<data.length && (data[i]&0b10000000) != 0) {
						i += 1;
					}
					tick += longMidiFormat(Arrays.copyOfRange(data, j, i+1));
					
					FWSKeySignatureEvent key_event = new FWSKeySignatureEvent();
					key_event.tick = tick;
					key_event.accidental_count = acc;
					key_event.major = major;

					events.add(key_event);
				} else { //Other meta.
					final byte meta_type = data[i+1]; //Type.
					if(meta_type == 47) {
						i += 1;
						continue;
					}
					
					int j = i + 2;
					i += data[j] + 2;
					byte[] subdata = Arrays.copyOfRange(data, j, i);
					j=i;
					while(i<data.length && (data[i]&0b10000000) != 0) {
						i += 1;
					}

					tick += longMidiFormat(Arrays.copyOfRange(data, j, i+1));
					
					FWSMiscMIDIEvent misc_event = new FWSMiscMIDIEvent();
					misc_event.tick = tick;
					misc_event.type = meta_type;
					misc_event.data = subdata;

					events.add(misc_event);
				}
			} else if(data[i] == (byte)0xF0) { //Sysex
				i += 1;
				ArrayList<Byte> sysex_v = new ArrayList<Byte>(0);
				do {
					i += 1;
					sysex_v.add(data[i]);
				} while(i<data.length && data[i] != (byte)0xF7);
				byte[] sysex_d = new byte[sysex_v.size()];
				
				for(int j=0;j<sysex_d.length;j+=1)
					sysex_d[j] = sysex_v.get(j);
				
				i+=1;
				int j=i;
				while(i<data.length && (data[i]&0b10000000) != 0) {
					i += 1;
				}
				tick += longMidiFormat(Arrays.copyOfRange(data, j, i+1));

				FWSSysexEvent sysex_event = new FWSSysexEvent();
				sysex_event.data = sysex_d;
				sysex_event.status = 240;
				sysex_event.tick = tick;

				events.add(sysex_event);
			} else if(data[i] == (byte)0x88) { //Eight-character string.
				i += 1;
				String data_str = new String(Arrays.copyOfRange(data, i, i+8));
				if(data_str.substring(0, 3).equals("CH ")) {
					if(data_str.substring(4, 8).equals("END "))
						tick = 0;
				}
				i+=7;
			}
			i+=1;
		}

		for(int v=0;v<voice_cache.size();v+=1) {
			FWSVoiceEvent voice = voice_cache.get(v);
			if(voice.voice_lsb < 0)
				voice.voice_lsb = 0;
			if(voice.voice_msb < 0)
				voice.voice_msb = 0;

			events.add(voice);
		}

		return events;
	}

	/** Get a legacy style. */
	public static Style getStyle(byte[] data) {
		int e = 0;
		Style new_style = new Style();

		FWSSequence style_sequence = new FWSSequence();

		while(e < data.length) {
			try {
				if(data[e] == (byte)0xA9) { //Start of event list.
					boolean found = false;
					e += 1;
					final int s = e;
					while(e<data.length) {
						if(e <= data.length - 3 && data[e] == (byte)0xFF) {
							if(data[e+1] == (byte)0x2F && data[e+2] == (byte)0x00) {
								found = true;
								break;
							}
						}
						e += 1;
					}
					
					if(found) {
						byte[] sty_data = Arrays.copyOfRange(data, s, e+3);
						ArrayList<FWSEvent> events = grabEvents(sty_data);
						for(int i=0;i<events.size();i+=1) {
							if(events.get(i).tick == 0) {
								if(events.get(i) instanceof FWSTempoEvent) {
									if(style_sequence.getTempoAt(0) != null)
										style_sequence.removeEvent(style_sequence.getTempoAt(0));
								} else if(events.get(i) instanceof FWSTimeSignatureEvent) {
									if(style_sequence.getTimeSignatureAt(0) != null)
										style_sequence.removeEvent(style_sequence.getTimeSignatureAt(0));
								} else if(events.get(i) instanceof FWSKeySignatureEvent) {
									if(style_sequence.getKeySignatureAt(0) != null)
										style_sequence.removeEvent(style_sequence.getKeySignatureAt(0));
								}
							}

							style_sequence.addEvent(events.get(i));
						}
					}
				} else if(data[e] == (byte)0xA7) { //Section list.
					e += 1;
					long t = 0;
					while(e<data.length) {
						if(data[e] == (byte)0xF6) { //Section.
							FWSSectionNameEvent section = new FWSSectionNameEvent();

							e += 1; //Section number first.
							while(e<data.length && (data[e]&0b1000000) != 0) {
								e += 1;
							}
							e += 1; //Section name.
							int s = e;
							while(e<data.length && data[e] != 0) {
								e += 1;
							}
							section.section_name = (new String(Arrays.copyOfRange(data, s, e)));
							e += 1; //Section tick.
							s = e;
							while(e<data.length && (data[e]&0b10000000) != 0) {
								e += 1;
							}
							t += longMidiFormat(Arrays.copyOfRange(data, s, e+1));
							section.tick = t;

							style_sequence.addEvent(section);
						} else if(data[e] == (byte)0xFF) { //Section list ends with FF.
							break;
						} else 
							e+=1;
					}
				} else if(data[e] == (byte)0x88) { //Eight-character hex.
					e += 1;
					final String data_str = new String(Arrays.copyOfRange(data, e, e+8));
					if(data_str.equals("STYLEEND"))
						break; //End of style.
				} else if(data[e] == (byte)0x84) { //Text.
					e += 1;
					final String data_str = new String(Arrays.copyOfRange(data, e,e+4));
					if(data_str.equals("CASM")) {
						//This is the CASM data required.
						final int s = e;
						while(e + 8<data.length && !(new String(Arrays.copyOfRange(data, e, e+8))).equals("CASMEND ")) {
							e += 1;
						}
						Casm new_casm = Casm.createCasm(Arrays.copyOfRange(data, s, e-1));

						new_style.setCasm(new_casm);
					}
				} else if(data[e] == (byte)0xDB) { //Long name.
					e += 1;
					final int s = e;
					while(e<data.length && data[e] != (byte)00) {
						e += 1;
					}
					new_style.long_name = new String(Arrays.copyOfRange(data, s,e));
				} else if(data[e] == (byte)0xDA) { //Short name.
					e += 1;
					final int s = e;
					while(e<data.length && data[e] != (byte)00) {
						e += 1;
					}
					new_style.short_name = new String(Arrays.copyOfRange(data, s,e));
				} else if(data[e] == (byte)0xE5) { //Style TPQ. 
					e += 1;
					final int s = e;
					while(e<data.length && (data[e]&0b10000000) != 0) {
						e += 1;
					}
					style_sequence.setTPQ((int)longMidiFormat(Arrays.copyOfRange(data, s, e+1)));
				}
				
				e += 1;
			} catch(ArrayIndexOutOfBoundsException ex) {
				break;
			}
		}

		new_style.getFromSequence(style_sequence);
		return new_style;
	}

	public static ArrayList<FWSEvent> getControlEvents(byte[] data, String[] styles, String[][] sections) {
		ArrayList<FWSEvent> events = new ArrayList<FWSEvent>(0);

		String current_style = "", current_section = "";
		int current_style_number = -1;
		long tick = 0;
		for(int p=0;p<data.length;p+=1) {
			if(data[p] == (byte)0xF5 && p + 7 < data.length) { //Chord event
				ChordBody main = new ChordBody(data[p+2], data[p+3]), bass = new ChordBody(data[p+4], data[p+5]);
				final byte inv = (byte) ((data[p+6]&0b11111000)>>5);
				p += 7;

				final int j = p;
				while(p<data.length && (data[p]&0b10000000) != 0) {
					p += 1;
				}
				tick += longMidiFormat(Arrays.copyOfRange(data, j, p+1));

				FWSChordEvent chord_event = new FWSChordEvent();
				chord_event.bass_chord = bass;
				chord_event.main_chord = main;
				chord_event.inversion = inv;
				chord_event.tick = tick;

				events.add(chord_event);
			} else if(data[p] == (byte)0xF4 && p + 2 < data.length) { //Style change event
				if(data[p+1] == (byte)0x07) { //Accompaniment Off.
					p += 2;
					final int j=p;
					while(p<data.length && (data[p]&0b10000000) != 0) {
						p += 1;
					}
					tick += longMidiFormat(Arrays.copyOfRange(data, j, p+1));
					
					FWSStyleChangeEvent style_change_event = new FWSStyleChangeEvent();
					style_change_event.section_name = "";
					style_change_event.style_name = "";
					style_change_event.tick = tick;
					style_change_event.style_tick = 0;

					style_change_event.sub_rhythm = false;
					style_change_event.rhythm = false;
					style_change_event.bass = false;
					style_change_event.chord1 = false;
					style_change_event.chord2 = false;
					style_change_event.pad = false;
					style_change_event.phrase1 = false;
					style_change_event.phrase2 = false;

					events.add(style_change_event);
				} else {
					p += 2;
					int j = p;
					while(p<data.length && (data[p]&0b10000000) != 0) {
						p += 1;
					}
					final int style_number = (int)longMidiFormat(Arrays.copyOfRange(data, j, p+1));
					current_style_number = style_number;
					
					if(style_number >= 0 && style_number < styles.length)
						current_style = styles[style_number];
					else
						current_style = "";

					p+=1;
					j = p;
					while(p<data.length && (data[p]&0b10000000) != 0) {
						p += 1;
					}
					final int section_number = (int)longMidiFormat(Arrays.copyOfRange(data, j, p+1));

					if(section_number >= 0 && section_number < sections[current_style_number].length)
						current_section = sections[current_style_number][section_number];
					else
						current_section = "";

					p+=1;
					final byte current_parts = data[p];

					FWSStyleChangeEvent style_change_event = new FWSStyleChangeEvent();
					style_change_event.style_name = current_style;
					style_change_event.section_name = current_section;

					style_change_event.sub_rhythm = ((current_parts&0xFF)&0b10000000) != 0;
					style_change_event.rhythm = ((current_parts&0xFF)&0b1000000) != 0;
					style_change_event.bass = ((current_parts&0xFF)&0b100000) != 0;
					style_change_event.chord1 = ((current_parts&0xFF)&0b10000) != 0;
					style_change_event.chord2 = ((current_parts&0xFF)&0b1000) != 0;
					style_change_event.pad = ((current_parts&0xFF)&0b100) != 0;
					style_change_event.phrase1 = ((current_parts&0xFF)&0b10) != 0;
					style_change_event.phrase2 = ((current_parts&0xFF)&0b1) != 0;

					long style_in = -1;
					if(data[p+1] == (byte)0x01) {
						p += 2;
					} else {
						p += 2;
						j = p;
						while(p<data.length && (data[p]&0b10000000) != 0) {
							p += 1;
						}
						style_in = longMidiFormat(Arrays.copyOfRange(data, j, p+1));
						p += 1;
					}

					style_change_event.style_tick = style_in;

					j=p;
					while(p<data.length && (data[p]&0b10000000) != 0) {
						p += 1;
					}
					tick += longMidiFormat(Arrays.copyOfRange(data, j, p+1));

					style_change_event.tick = tick;

					events.add(style_change_event);
				}
			}
		}

		return events;
	}
}
