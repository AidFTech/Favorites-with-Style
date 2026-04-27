package song;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import controllers.LegacyFWSHandler;
import fwsevents.FWSEvent;
import fwsevents.FWSKeySignatureEvent;
import fwsevents.FWSSequence;
import fwsevents.FWSTempoEvent;
import fwsevents.FWSTimeSignatureEvent;
import style.Style;

public class FWSSong {
	private FWSSequence song_sequence;
	private FWSSongMetadata song_metadata;

	private Map<String, Style> styles = new LinkedHashMap<>();

	public FWSSong() {
		song_sequence = new FWSSequence();
		song_sequence.init();

		song_metadata = new FWSSongMetadata();
	}

	/** Get the song sequence. */
	public FWSSequence getSongSequence() {
		return this.song_sequence;
	}

	/** Get the song metadata. */
	public FWSSongMetadata getSongMetadata() {
		return this.song_metadata;
	}

	/** Add a style to the list. */
	public void addStyle(final String name, Style style) {
		styles.put(name, style);
	}

	/** Get a loaded style. */
	public Style getStyle(final String style) {
		return styles.get(style);
	}

	/** Remove a style from the list. */
	public void removeStyle(final String name) {
		styles.remove(name);
	}

	/** Get a list of available style names. */
	public String[] getStyleNames() {
		ArrayList<String> name_list = new ArrayList<String>();
		for(Entry<String, Style> name : styles.entrySet()) {
			name_list.add(name.getKey());
		}

		String[] names = new String[name_list.size()];
		name_list.toArray(names);
		return names;
	}

	/** Get the song length. */
	public long getSongLength() {
		FWSEvent end_event = song_sequence.getEndEvent();
		if(end_event != null)
			return end_event.tick;
		else
			return 0;
	}

	/** Add events from an FWS sequence to the song sequence. */
	public void addSequenceEvents(FWSSequence sequence) {
		ArrayList<FWSEvent> common_events = sequence.getCommonEvents();
		for(int i=0;i<common_events.size();i+=1)
			song_sequence.addEvent(common_events.get(i));

		for(int c=0;c<16;c+=1) {
			ArrayList<FWSEvent> channel_events = sequence.getChannelEvents(c);
			for(int i=0;i<channel_events.size();i+=1)
				song_sequence.addEvent(channel_events.get(i));
		}
	}

	/** Load a song from a legacy FWS file. */
	public static FWSSong loadFromLegacyFWS(File song_file) {
		FWSSong song = new FWSSong();
		byte[] data = new byte[0];

		ArrayList<String> style_list = new ArrayList<>();
		ArrayList<String[]> section_list = new ArrayList<>();

		try {
			RandomAccessFile data_buf = new RandomAccessFile(song_file.getAbsolutePath(),"r");
			data = new byte[(int)data_buf.length()];
			data_buf.read(data);
			data_buf.close();
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			//TODO: Show error message.
		}

		int e = 8;
		while(e<data.length) {
			if(data[e] == (byte)0x88) { //Eight-character hex.
				e += 1;
				String data_str = new String(Arrays.copyOfRange(data, e, e+8));

				if(data_str.equals("STYLE   ")) {
					boolean found = false;
					e += 8;
					final int s = e;
					while(e<data.length) {
						e += 1;
						if(e < data.length - 9 && data[e] == (byte)0x88) {
							final String check_str = new String(Arrays.copyOfRange(data, e+1, e+9));
							if(check_str.equals("STYLEEND")) {
								found = true;
								break;
							}
						}
					}
					if(found) {
						byte[] style_data = Arrays.copyOfRange(data, s, e+3);
						Style new_style = LegacyFWSHandler.getStyle(style_data);

						style_list.add(new_style.long_name);
						section_list.add(new_style.getOrderedSectionNames());

						song.addStyle(new_style.long_name, new_style);
					}
				} else if(data_str.equals("SONG    ")) {
					boolean found = false;
					e += 8;
					int s = e;
					while(e<data.length) {
						e += 1;
						if(data[e] == (byte)0xFF && e < data.length - 3) {
							if(data[e+1] == (byte)0x2F && data[e+2] == (byte)0x00) {
								found = true;
								break;
							}
						}
					}
					if(found) {
						byte[] song_data = Arrays.copyOfRange(data, s+1, e+3);
						ArrayList<FWSEvent> song_events = LegacyFWSHandler.grabEvents(song_data);
						for(int i=0;i<song_events.size();i+=1) {
							if(song_events.get(i).tick == 0) {
								if(song_events.get(i) instanceof FWSTempoEvent) {
									if(song.song_sequence.getTempoAt(0) != null)
										song.song_sequence.removeEvent(song.song_sequence.getTempoAt(0));
								} else if(song_events.get(i) instanceof FWSTimeSignatureEvent) {
									if(song.song_sequence.getTimeSignatureAt(0) != null)
										song.song_sequence.removeEvent(song.song_sequence.getTimeSignatureAt(0));
								} else if(song_events.get(i) instanceof FWSKeySignatureEvent) {
									if(song.song_sequence.getKeySignatureAt(0) != null)
										song.song_sequence.removeEvent(song.song_sequence.getKeySignatureAt(0));
								}
							}

							song.song_sequence.addEvent(song_events.get(i));
						}
					}
				} else if(data_str.equals("CTL     ")) {
					boolean found = false;
					e += 7;
					final int s = e;
					while(e<data.length - 8) {
						e+=1;
						data_str = new String(Arrays.copyOfRange(data, e, e+8));
						if(data_str.equals("CTLEND  ")) {
							found = true;
							break;
						}
					}
					if(found) {
						byte[] ctl_data = Arrays.copyOfRange(data, s+1, e+8);

						String[] styles = new String[style_list.size()];
						String[][] sections = new String[section_list.size()][];

						style_list.toArray(styles);
						section_list.toArray(sections);

						ArrayList<FWSEvent> control_events = LegacyFWSHandler.getControlEvents(ctl_data, styles, sections);
						for(int i=0;i<control_events.size();i+=1)
							song.song_sequence.addEvent(control_events.get(i));
					}
				} else if(data_str.equals("FWSEND  "))
					break; //End of song.
			} else if(data[e] == (byte)0xCA) { //Chord split note.
				e += 1;
				song.song_metadata.split_point = data[e];
			} else if(data[e] == (byte)0xCC) { //Chord channel.
				e += 1;
				song.song_metadata.chord_channel = data[e];
			} else if(data[e] == (byte)0xCE) { //Left hand channel.
				e += 1;
				song.song_metadata.melody_lh_channel = data[e];
			} else if(data[e] == (byte)0xCD) { //Right hand channel.
				e += 1;
				song.song_metadata.melody_rh_channel = data[e];
			} else if(data[e] == (byte)0xDB) { //Song name.
				e += 1;
				final int s = e;
				while(e<data.length && data[e] != (byte)0x00) {
					e += 1;
				}
				song.song_metadata.long_title = new String(Arrays.copyOfRange(data, s, e));
			} else if(data[e] == (byte)0xDA) { //Song short name.
				e += 1;
				final int s = e;
				while(e<data.length && data[e] != (byte)0x00) {
					e += 1;
				}
				song.song_metadata.short_title = new String(Arrays.copyOfRange(data, s, e));
			} else if(data[e] == (byte)0xDC) { //Composer.
				e += 1;
				final int s = e;
				while(e<data.length && data[e] != (byte)0x00) {
					e += 1;
				}
				song.song_metadata.composer = new String(Arrays.copyOfRange(data, s, e));
			} else if(data[e] == (byte)0xE5) { //TPQ.
				e += 1;
				int s = e;
				while(e<data.length && (data[e]&0b10000000) != 0) {
					e += 1;
				}
				song.song_sequence.setTPQ((int)LegacyFWSHandler.longMidiFormat(Arrays.copyOfRange(data, s, e+1)));
			} else if(data[e] == (byte) 0xE3) { //Song length.
				e += 1;
				final int s = e;
				while(e<data.length && (data[e]&0b10000000) != 0) {
					e += 1;
				}
				final long full_song_length = (LegacyFWSHandler.longMidiFormat(Arrays.copyOfRange(data, s, e+1)));
				song.song_sequence.setEndEvent(full_song_length);
			}
			
			e += 1;
		}
	
		return song;
	}
}