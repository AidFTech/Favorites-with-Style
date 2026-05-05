package voices;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class InstrumentProfile {
	private Map<String, Voice[]> voice_list = new HashMap<>();
	private String instrument_family = ""; //E.g. A^2, HL, AHL, PSR-E, DGX, Tyros, etc.

	private int percussion_header = 127<<7; //The percussion LSB/MSB combo.
	private String script = "";

	private byte stream_melody_lh = 0, stream_melody_rh = 0, file_melody_lh = 1, file_melody_rh = 0; //Melody channels.

	/** Get a voice list. */
	public Voice[] getVoiceList(String instrument_name) {
		return voice_list.get(instrument_name);
	}

	/** Add a voice list. Return whether successful. */
	public boolean addVoiceList(String instrument_name, Voice[] voices) {
		if(voice_list.get(instrument_name) != null) //Instrument data already exists.
			return false;

		Voice[] test_list = new Voice[voices.length];
		for(int i=0;i<test_list.length;i+=1)
			test_list[i] = new Voice(voices[i]);

		voice_list.put(instrument_name, test_list);
		return true;
	}

	/** Add a voice list. Return whether successful. */
	public boolean addVoiceList(String instrument_name, File voice_file) {
		if(voice_list.get(instrument_name) != null) //Instrument data already exists.
			return false;

		try {
			Voice[] test_list = getVoiceListFromFile(voice_file);
			this.voice_list.put(instrument_name, test_list);
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/** Get the list of instruments. */
	public String[] getInstrumentNames() {
		ArrayList<String> instrument_names = new ArrayList<>();
		for(Entry<String, Voice[]>instrument : voice_list.entrySet()) {
			instrument_names.add(instrument.getKey());
		}

		String[] instrument_name_array = new String[instrument_names.size()];
		instrument_names.toArray(instrument_name_array);
		return instrument_name_array;
	}

	/** Get the instrument family name. */
	public String getInstrumentFamily() {
		return this.instrument_family;
	}

	/** Set the instrument family name. */
	public void setInstrumentFamily(final String instrument_family) {
		this.instrument_family = instrument_family;
	}

	/** Get the percussion header. */
	public int getPercussionHeader() {
		return this.percussion_header;
	}

	/** Set the percussion header. */
	public void setPercussionHeader(final int percussion_header) {
		this.percussion_header = percussion_header;
	}

	/** Set the melody channel bytes. */
	public void setMelodyChannels(final byte stream_rh, final byte stream_lh, final byte file_rh, final byte file_lh) {
		stream_melody_rh = stream_rh;
		stream_melody_lh = stream_lh;
		file_melody_rh = file_rh;
		file_melody_lh = file_lh;
	}

	/** Get the streaming melody LH channel. */
	public byte getStreamMelodyLH() {
		return this.stream_melody_lh;
	}

	/** Get the streaming melody RH channel. */
	public byte getStreamMelodyRH() {
		return this.stream_melody_rh;
	}

	/** Get the file melody LH channel. */
	public byte getFileMelodyLH() {
		return this.file_melody_lh;
	}

	/** Get the file melody RH channel. */
	public byte getFileMelodyRH() {
		return this.file_melody_rh;
	}

	/** Get the Python script. */
	public String getScript() {
		return this.script;
	}

	/** Set the Python script. */
	public void setScript(String script) {
		this.script = script;
	}

	/** Set the Python script. */
	public void setScript(File script) {
		try {
			BufferedReader file_reader = new BufferedReader(new FileReader(script));
			String line = "", str_script = "";

			while((line = file_reader.readLine()) != null)
				str_script += line + '\n';

			file_reader.close();
			setScript(str_script);
		} catch (IOException e) {
			
		}
	}

	/** Get the voice list from an external file. */
	public static Voice[] getVoiceListFromFile(File voice_list_file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(voice_list_file));
		ArrayList<Voice> voice_list = new ArrayList<>();

		int index = -1, msb = -1, lsb = -1, voice = -1, voice_name = -1;
		int index_offset = 0, msb_offset = 0, lsb_offset = 0, voice_offset = 0;

		String current_line = "";
		boolean first_line = true;
		while((current_line = reader.readLine()) != null) {
			String[] values = current_line.split(",");
			if(first_line) {
				first_line = false;
				for(int i=0;i<values.length;i+=1) {
					values[i] = values[i].trim();
					if(values[i].toUpperCase().contains("INDEX")) {
						index = i;
						if(values[i].contains("=")) {
							String[] offset = values[i].split("=");
							try {
								index_offset = Integer.parseInt(offset[1]);
							} catch(NumberFormatException | ArrayIndexOutOfBoundsException e) {

							}
						}
					} else if(values[i].toUpperCase().contains("MSB")) {
						msb = i;
						if(values[i].contains("=")) {
							String[] offset = values[i].split("=");
							try {
								msb_offset = Integer.parseInt(offset[1]);
							} catch(NumberFormatException | ArrayIndexOutOfBoundsException e) {

							}
						}
					} else if(values[i].toUpperCase().contains("LSB")) {
						lsb = i;
						if(values[i].contains("=")) {
							String[] offset = values[i].split("=");
							try {
								lsb_offset = Integer.parseInt(offset[1]);
							} catch(NumberFormatException | ArrayIndexOutOfBoundsException e) {

							}
						}
					} else if(values[i].toUpperCase().contains("VOICE")) {
						voice = i;
						if(values[i].contains("=")) {
							String[] offset = values[i].split("=");
							try {
								voice_offset = Integer.parseInt(offset[1]);
							} catch(NumberFormatException | ArrayIndexOutOfBoundsException e) {

							}
						}
					} else if(values[i].equalsIgnoreCase("NAME"))
						voice_name = i;
				}
			} else {
				String name = "";
				int voice_index = -1;
				byte voice_msb = 0, voice_lsb = 0, voice_prog = 0;

				//Read the voice data.
				try {
					if(msb >= 0)
						voice_msb = (byte)(Integer.parseInt(values[msb]) - msb_offset);

					if(lsb >= 0)
						voice_lsb = (byte)(Integer.parseInt(values[lsb]) - lsb_offset);

					if(voice >= 0)
						voice_prog = (byte)(Integer.parseInt(values[voice]) - voice_offset);

					if(index >= 0)
						voice_index = Integer.parseInt(values[index]) - index_offset;

					if(voice_name >= 0)
						name = values[voice_name];

					Voice new_voice = new Voice(voice_index >= 0 ? voice_index + ". " + name : name, voice_prog, voice_lsb, voice_msb);
					voice_list.add(new_voice);
				} catch(NumberFormatException | ArrayIndexOutOfBoundsException e) {
					
				}
			}
		}

		reader.close();

		Voice[] voices = new Voice[voice_list.size()];
		voice_list.toArray(voices);
		return voices;
	}
}
