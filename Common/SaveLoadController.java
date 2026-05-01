package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.ini4j.Ini;

import fwsevents.FWSChordEvent;
import fwsevents.FWSEvent;
import fwsevents.FWSSequence;
import fwsevents.FWSStyleChangeEvent;
import song.FWSSong;
import song.FWSSongMetadata;
import style.ChordBody;
import style.Style;
import voices.InstrumentProfile;

public class SaveLoadController {
	private FWS controller;
	private String last_filepath = "", data_path = "";

	private static final String DEFAULTS = "FWSDefaults.ini", DEFAULTS_HEADER = "FWSDefaults";
	private static final String KEY_LAST_PATH = "LastPath", KEY_DATA_PATH = "DataPath";
	private static final String KEY_FAMILY = "InstrumentFamily", KEY_VOICE_LIST = "InstrumentVoices";

	public SaveLoadController(FWS controller) {
		this.controller = controller;
	}

	/** Load in initial default settings. */
	protected void initFWS() {
		boolean rewrite_init = false;

		try {
			Ini main_ini = new Ini(new FileReader(DEFAULTS));

			final String last_path = main_ini.get(DEFAULTS_HEADER, KEY_LAST_PATH);
			if(last_path != null)
				this.last_filepath = last_path;
			else
				rewrite_init = true;

			final String data_path = main_ini.get(DEFAULTS_HEADER, KEY_DATA_PATH);
			if(data_path != null) {
				this.data_path = data_path;
				getInstrumentProfiles(controller.instrument_profiles, new File(data_path));
			} else
				rewrite_init = true;

			final String family = main_ini.get(DEFAULTS_HEADER, KEY_FAMILY);
			if(family != null)
				controller.setInstrumentFamily(family);
			else
				rewrite_init = true;

			final String instrument = main_ini.get(DEFAULTS_HEADER, KEY_VOICE_LIST);
			if(instrument != null)
				controller.setVoiceList(instrument);
			else
				rewrite_init = true;
		} catch(FileNotFoundException e) {
			saveDefaults();
		} catch(IOException e) {

		}

		if(rewrite_init)
			saveDefaults();
	}

	/** Save initial default settings. */
	protected void saveDefaults() {
		Ini main_ini = null;
		try {
			File defaults_file = new File(DEFAULTS);
			main_ini = new Ini(defaults_file);
		} catch (IOException e) {
			File defaults_file = new File(DEFAULTS);

			try {
				defaults_file.createNewFile();
				main_ini = new Ini(defaults_file);
			} catch(IOException e1) {
				return;
			}
		}
		
		if(data_path.isBlank()) {
			try {
				final String home_dir = System.getProperty("user.home");
				
				Files.createDirectories(Paths.get(home_dir + "/FWS"));
				data_path = home_dir + "/FWS";
			} catch (IOException e) {
				return;
			}
		}

		main_ini.put(DEFAULTS_HEADER, KEY_LAST_PATH, last_filepath);
		main_ini.put(DEFAULTS_HEADER, KEY_DATA_PATH, data_path);
		if(controller.active_profile != null)
			main_ini.put(DEFAULTS_HEADER, KEY_FAMILY, controller.active_profile.getInstrumentFamily());
		else
			main_ini.put(DEFAULTS_HEADER, KEY_FAMILY, "");
		main_ini.put(DEFAULTS_HEADER, KEY_VOICE_LIST, controller.active_instrument);

		try {
			main_ini.store();
		} catch (IOException e) {
			
		}

	}

	/** Open the file chooser to import MIDI data. */
	protected File openMidiImportChooser() {
		JFileChooser file_chooser = new JFileChooser(last_filepath);

		FileNameExtensionFilter name_extension = new FileNameExtensionFilter("MIDI File (*.midi)", "mid");
		file_chooser.addChoosableFileFilter(name_extension);

		file_chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		file_chooser.setAcceptAllFileFilterUsed(true);

		final int return_val = file_chooser.showOpenDialog(controller.getMainWindow());
		if(return_val == JFileChooser.APPROVE_OPTION) {
			last_filepath = file_chooser.getSelectedFile().getParentFile().getPath();
			return file_chooser.getSelectedFile();
		}
		return null;
	}

	/** Open the file chooser to export MIDI data. */
	public File openMidiExportChooser() {
		JFileChooser file_chooser = new JFileChooser(last_filepath);

		FileNameExtensionFilter name_extension = new FileNameExtensionFilter("MIDI File (*.midi)", "mid");
		file_chooser.addChoosableFileFilter(name_extension);
		file_chooser.setFileFilter(name_extension);

		file_chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		file_chooser.setAcceptAllFileFilterUsed(true);

		final int return_val = file_chooser.showSaveDialog(controller.getMainWindow());
		if(return_val != JFileChooser.APPROVE_OPTION)
			return null;

		File output = file_chooser.getSelectedFile();
		String ext = "";

		final String des = file_chooser.getFileFilter().getDescription();
		if(des.toUpperCase().contains("MID"))
			ext = "mid";
		else {
			int period = output.getName().indexOf(".");
			if(period<0) {
				JOptionPane.showMessageDialog(controller.getMainWindow(), "Please specify an extension and try again.", "Error", JOptionPane.ERROR_MESSAGE);
				return null;
			} else {
				ext = output.getName().substring(period+1, output.getName().length());
			}
		}

		if(!file_chooser.getFileFilter().accept(output)) {
			String fpath = output.getAbsolutePath() + "." + ext;
			output = new File(fpath);
		}

		if(output.exists()) {
			final int answer = JOptionPane.showConfirmDialog(controller.getMainWindow(), output.getName() + " already exists. Overwrite it?", "Save", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if(answer == JOptionPane.NO_OPTION)
				return null;
		}

		last_filepath = file_chooser.getSelectedFile().getPath();
		return output;
	}

	/** Open the file chooser to import a legacy file. */
	protected File openLegacyFWSImportChooser() {
		JFileChooser file_chooser = new JFileChooser(last_filepath);

		FileNameExtensionFilter name_extension = new FileNameExtensionFilter("FWS File (*.fws)", "fws");
		file_chooser.addChoosableFileFilter(name_extension);

		file_chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		file_chooser.setAcceptAllFileFilterUsed(false);

		final int return_val = file_chooser.showOpenDialog(controller.getMainWindow());
		if(return_val == JFileChooser.APPROVE_OPTION) {
			last_filepath = file_chooser.getSelectedFile().getParentFile().getPath();
			return file_chooser.getSelectedFile();
		}
		return null;
	}

	/** Save a song file. */
	protected void saveSong(FWSSong song) {
		File song_file = openSongSaveChooser();
		if(song_file == null)
			return;

		controller.setLoadedSongFile(song_file);

		saveSong(song_file, song);
	}

	/** Save a song file. */
	protected void saveSong(File song_file, FWSSong song) {
		FWSSequence song_sequence = song.getSongSequence();
		Sequence song_midi_sequence = song_sequence.getMidiSequence();
		ArrayList<FWSEvent> song_common_events = song_sequence.getCommonEvents();

		final String tmp_dir = "./.fwstmp", style_dir = "/styles";
		Path style_path = Paths.get(tmp_dir + style_dir);

		File melody_file = new File(tmp_dir + "/song.mid"); //For melody and harmony notes.
		
		String[] style_names = song.getStyleNames();
		File[] style_files = new File[style_names.length];

		try {
			Files.createDirectories(style_path);
		
			MidiSystem.write(song_midi_sequence, 0, melody_file);
			
			for(int i=0;i<style_files.length;i+=1) {
				Style style = song.getStyle(style_names[i]);
				byte[] style_binary = style.getStyleFileBinary();

				style_files[i] = new File(tmp_dir + style_dir + '/' + style.short_name + ".sty");
				writeBytesToFile(style_files[i], style_binary);
			}

			FileOutputStream chord_file = new FileOutputStream(tmp_dir + "/acc.xml");
			getChordXML(song_common_events, chord_file);

			FileOutputStream meta_file = new FileOutputStream(tmp_dir + "/song.xml");
			getSongInfoXML(song, meta_file);

			//Create the ZIP:
			FileOutputStream song_file_output = new FileOutputStream(song_file);
			ZipOutputStream zip_out = new ZipOutputStream(song_file_output);

			File dir_file = new File(tmp_dir);
			zipFile(dir_file, zip_out);
			
			zip_out.close();
			deleteDirectory(new File(tmp_dir));
			
		} catch(IOException e) {
			return;
		} catch (ParserConfigurationException e) {
			
		} catch (TransformerException e) {
			
		}
	}

	/** Load a song file. */
	protected FWSSong loadSong() {
		File song_file = openSongLoadChooser();
		if(song_file == null)
			return null;
		
		FWSSong new_song = new FWSSong();
		FWSSequence melody_sequence = new_song.getSongSequence();
		{
			ArrayList<FWSEvent> start_events = melody_sequence.getCommonEvents();
			start_events.clear();
		}

		final String tmp_dir = "./.fwstmp";
		File tmp_dest = new File(tmp_dir);

		try {
			File[] song_files = unzipFile(song_file, tmp_dest);
			if(song_files == null) {
				deleteDirectory(tmp_dest);
				return null;
			}

			loadSongEvents("", song_files, new_song);
		} catch (IOException | ParserConfigurationException | SAXException e) {
			deleteDirectory(tmp_dest);
			return null;
		}


		deleteDirectory(tmp_dest);
		controller.setLoadedSongFile(song_file);
		return new_song;
	}

	/** Load song events recursively. */
	private void loadSongEvents(String directory, File[] song_files, FWSSong song) throws ParserConfigurationException, SAXException, IOException {
		FWSSequence melody_sequence = song.getSongSequence();

		for(int i=0;i<song_files.length;i+=1) {
			if(song_files[i].isDirectory()) {
				File[] new_files = song_files[i].listFiles();
				loadSongEvents(song_files[i].getName(), new_files, song);
			} else if(directory.equalsIgnoreCase("STYLES") && song_files[i].getName().toUpperCase().endsWith(".STY")) {
				Style new_style = Style.getStyleFromFile(song_files[i], controller.getMainWindow());
				song.addStyle(new_style.long_name, new_style);
			} else if(song_files[i].getName().equalsIgnoreCase("SONG.MID")) { //Main song file.
				FWSSequence new_melody = FWSSequence.getFWSSequencefromSequence(song_files[i], controller.getMainWindow());
				melody_sequence.addEventsFromFWSSequence(new_melody, true);
			} else if(song_files[i].getName().equalsIgnoreCase("SONG.XML")) { //Song metadata.
				loadSongInfoXML(song, song_files[i]);
			} else if(song_files[i].getName().equalsIgnoreCase("ACC.XML")) { //Chords and style changes.
				loadChordInfoXML(song, song_files[i]);
			}
		}
	}

	/** Add the current list of instrument profiles to the list. */
	protected void getInstrumentProfiles(Map<String, InstrumentProfile> profile_list, File directory) {
		if(!directory.isDirectory())
			return;

		File[] file_list = directory.listFiles();
		if(file_list == null)
			return;

		profile_list.clear();
		for(File f : file_list) {
			if(f.getName().toUpperCase().endsWith("FWSDAT")) { //Instrument profile!
				InstrumentProfile profile = loadInstrumentProfile(f);
				profile_list.put(profile.getInstrumentFamily(), profile);
			}
		}
	}

	/** Load an instrument profile. */
	private static InstrumentProfile loadInstrumentProfile(File profile_file) {
		final String tmp_dir = "./.fwstmp";
		File tmp_dest = new File(tmp_dir);

		InstrumentProfile profile = new InstrumentProfile();

		try {
			File[] profile_files = unzipFile(profile_file, tmp_dest);

			if(profile_files == null) {
				deleteDirectory(tmp_dest);
				return null;
			}

			loadInstrumentParameters("", profile_files, profile);
		} catch (IOException e) {
			deleteDirectory(tmp_dest);
			return null;
		}

		deleteDirectory(tmp_dest);
		return profile;
	}

	/** Load instrument profile parameters recursively. */
	private static void loadInstrumentParameters(String directory, File[] profile_files, InstrumentProfile profile) {
		for(File f : profile_files) {
			if(f.isDirectory()) {
				File[] new_files = f.listFiles();
				loadInstrumentParameters(f.getName(), new_files, profile);
			} else if(directory.equalsIgnoreCase("VOICES") && f.getName().toUpperCase().endsWith("CSV")) { //Scan in voices.
				String instrument_name = f.getName();
				int index = -1;
				if((index = instrument_name.lastIndexOf(".")) >= 0)
					instrument_name = instrument_name.substring(0, index);

				profile.addVoiceList(instrument_name, f);
			} else if(f.getName().equalsIgnoreCase("MAIN.INI")) { //General profile info.
				try {
					Ini main_ini = new Ini(new FileReader(f));
					
					String instrument_family = main_ini.get("InstrumentFamily", "Family");
					profile.setInstrumentFamily(instrument_family);

					String percussion = main_ini.get("InstrumentFamily", "Percussion");
					profile.setPercussionHeader(Integer.parseInt(percussion));

					String melody_stream_rh = main_ini.get("InstrumentFamily", "StreamRH"), melody_stream_lh = main_ini.get("InstrumentFamily", "StreamLH");
					String melody_file_rh = main_ini.get("InstrumentFamily", "FileRH"), melody_file_lh = main_ini.get("InstrumentFamily", "FileLH");

					final byte stream_rh = Byte.parseByte(melody_stream_rh), stream_lh = Byte.parseByte(melody_stream_lh);
					final byte file_rh = Byte.parseByte(melody_file_rh), file_lh = Byte.parseByte(melody_file_lh);
					profile.setMelodyChannels(stream_rh, stream_lh, file_rh, file_lh);
				} catch (IOException | NumberFormatException e) {
					continue;
				}
			}
		}
	}

	/** Unzip a file. */
	private static File[] unzipFile(File z_file, File tmp_dest) throws IOException {
		Files.createDirectories(tmp_dest.toPath());

		byte[] zip_buffer = new byte[1024];
		ZipInputStream zip_input = new ZipInputStream(new FileInputStream(z_file));
		ZipEntry zip_entry = zip_input.getNextEntry();

		while(zip_entry != null) {
			File new_file = new File(tmp_dest, zip_entry.getName());
			if(zip_entry.isDirectory()) {
				if(!new_file.isDirectory())
					new_file.mkdirs();
			} else {
				File parent = new_file.getParentFile();
				if(!parent.isDirectory())
					parent.mkdirs();

				FileOutputStream output_stream = new FileOutputStream(new_file);
				int length;
				while((length = zip_input.read(zip_buffer)) > 0) {
					output_stream.write(zip_buffer, 0, length);
				}
				output_stream.close();
			}

			zip_entry = zip_input.getNextEntry();
		}

		zip_input.closeEntry();
		zip_input.close();

		File[] files = tmp_dest.listFiles();
		if(files == null)
			return null;

		return files;
	}

	/** Zip a file. */
	private static void zipFile(File file_to_zip, ZipOutputStream zip_out) throws IOException {
		zipFile(file_to_zip, file_to_zip.getName(), zip_out);
	}

	/** Zip a file.
	 *  Thanks to https://www.baeldung.com/java-compress-and-uncompress
	 */
	private static void zipFile(File file_to_zip, String filename, ZipOutputStream zip_out) throws IOException {
		if(filename.startsWith(".fwstmp")) {
			final int slash = filename.indexOf("/");
			if(slash > 0)
				filename = filename.substring(slash + 1);
			else
				filename = "";
		}

		if(file_to_zip.isDirectory()) {
			if(filename.endsWith("/")) {
				zip_out.putNextEntry(new ZipEntry(filename));
				zip_out.closeEntry();
			} else {
				zip_out.putNextEntry(new ZipEntry(filename + "/"));
				zip_out.closeEntry();
			}
			File[] subfiles = file_to_zip.listFiles();
			for(int i=0;i<subfiles.length;i+=1)
				zipFile(subfiles[i], filename + "/" + subfiles[i].getName(), zip_out);
			return;
		}
		FileInputStream input_stream = new FileInputStream(file_to_zip);
		ZipEntry zip_entry = new ZipEntry(filename);
		zip_out.putNextEntry(zip_entry);
		
		byte[] b = new byte[1024];
		int length = 0;
		
		while((length = input_stream.read(b)) >= 0) {
			byte[] bz = new byte[length];
			for(int i=0;i<bz.length && i < b.length;i+=1)
				bz[i] = b[i];

			zip_out.write(bz);
		}

		input_stream.close();
	}

	/** Delete a directory. */
	private static boolean deleteDirectory(File directory) {
		if(!directory.isDirectory())
			return directory.delete();

		File[] files = directory.listFiles();
		for(int i=0;i<files.length;i+=1)
			deleteDirectory(files[i]);

		return directory.delete();
	}

	/** Open the file chooser to save a song file. */
	protected File openSongSaveChooser() {
		JFileChooser file_chooser = new JFileChooser(last_filepath);

		FileNameExtensionFilter name_extension = new FileNameExtensionFilter("Favorites with Style Song (*.fwsz)", "fwsz");
		file_chooser.addChoosableFileFilter(name_extension);
		file_chooser.setFileFilter(name_extension);

		file_chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		file_chooser.setAcceptAllFileFilterUsed(true);

		final int return_val = file_chooser.showSaveDialog(controller.getMainWindow());
		if(return_val != JFileChooser.APPROVE_OPTION)
			return null;

		File output = file_chooser.getSelectedFile();
		String ext = "";

		final String des = file_chooser.getFileFilter().getDescription();
		if(des.toUpperCase().contains("SONG"))
			ext = "fwsz";
		else {
			int period = output.getName().indexOf(".");
			if(period<0) {
				JOptionPane.showMessageDialog(controller.getMainWindow(), "Please specify an extension and try again.", "Error", JOptionPane.ERROR_MESSAGE);
				return null;
			} else {
				ext = output.getName().substring(period+1, output.getName().length());
			}
		}

		if(!file_chooser.getFileFilter().accept(output)) {
			String fpath = output.getAbsolutePath() + "." + ext;
			output = new File(fpath);
		}

		if(output.exists()) {
			final int answer = JOptionPane.showConfirmDialog(controller.getMainWindow(), output.getName() + " already exists. Overwrite it?", "Save", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if(answer == JOptionPane.NO_OPTION)
				return null;
		}

		last_filepath = file_chooser.getSelectedFile().getParentFile().getPath();
		return output;
	}

	/** Open the file chooser to load a song file. */
	protected File openSongLoadChooser() {
		JFileChooser file_chooser = new JFileChooser(last_filepath);

		FileNameExtensionFilter name_extension = new FileNameExtensionFilter("Favorites with Style Song (*.fwsz)", "fwsz");
		file_chooser.addChoosableFileFilter(name_extension);

		file_chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		file_chooser.setAcceptAllFileFilterUsed(false);

		final int return_val = file_chooser.showOpenDialog(controller.getMainWindow());
		if(return_val == JFileChooser.APPROVE_OPTION) {
			last_filepath = file_chooser.getSelectedFile().getParentFile().getPath();
			return file_chooser.getSelectedFile();
		}
		return null;
	}

	/** Open the file chooser to save style data. */
	protected void openStyleSaveChooser(String short_name, byte[] file_binary) {
		JFileChooser file_chooser = new JFileChooser(last_filepath);

		FileNameExtensionFilter name_extension = new FileNameExtensionFilter("Yamaha Style File (*.sty)", "sty");
		file_chooser.addChoosableFileFilter(name_extension);
		file_chooser.setFileFilter(name_extension);

		file_chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		file_chooser.setAcceptAllFileFilterUsed(true);

		file_chooser.setSelectedFile(new File(short_name + ".sty"));

		final int return_val = file_chooser.showSaveDialog(controller.getMainWindow());
		if(return_val != JFileChooser.APPROVE_OPTION)
			return;

		File output = file_chooser.getSelectedFile();
		String ext = "";

		final String des = file_chooser.getFileFilter().getDescription();
		if(des.toUpperCase().contains("STYLE"))
			ext = "sty";
		else {
			int period = output.getName().indexOf(".");
			if(period<0) {
				JOptionPane.showMessageDialog(controller.getMainWindow(), "Please specify an extension and try again.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			} else {
				ext = output.getName().substring(period+1, output.getName().length());
			}
		}

		if(!file_chooser.getFileFilter().accept(output)) {
			String fpath = output.getAbsolutePath() + "." + ext;
			output = new File(fpath);
		}

		if(output.exists()) {
			final int answer = JOptionPane.showConfirmDialog(controller.getMainWindow(), output.getName() + " already exists. Overwrite it?", "Save", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if(answer == JOptionPane.NO_OPTION)
				return;
		}

		last_filepath = file_chooser.getSelectedFile().getParentFile().getPath();
		writeBytesToFile(output, file_binary);
	}

	/** Open the file chooser to load a style data. */
	protected File openStyleLoadChooser() {
		JFileChooser file_chooser = new JFileChooser(last_filepath);

		FileNameExtensionFilter name_extension = new FileNameExtensionFilter("Yamaha Style File (*.sty)", "sty");
		file_chooser.addChoosableFileFilter(name_extension);
		file_chooser.setFileFilter(name_extension);

		file_chooser.setAcceptAllFileFilterUsed(true);

		file_chooser.setDialogType(JFileChooser.OPEN_DIALOG);

		final int return_val = file_chooser.showOpenDialog(controller.getMainWindow());
		if(return_val == JFileChooser.APPROVE_OPTION) {
			last_filepath = file_chooser.getSelectedFile().getParentFile().getPath();
			return file_chooser.getSelectedFile();
		}
		return null;
	}

	/** Write bytes to a file. */
	private void writeBytesToFile(File save_file, byte[] file_binary) {
		if(save_file.exists()) {
			try {
				save_file.delete();
			} catch(SecurityException e) {
				JOptionPane.showMessageDialog(controller.getMainWindow(), "Error saving file.\nMessage: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		try {
			Files.write(save_file.toPath(), file_binary, StandardOpenOption.CREATE_NEW);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(controller.getMainWindow(), "Error saving file.\nMessage: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		String default_path = save_file.getParent();
		
		if(default_path != null)
			this.last_filepath = save_file.getParent();
	}

	/** Get an XML from chord and style events. */
	private static void getChordXML(ArrayList<FWSEvent> events, OutputStream output) throws ParserConfigurationException, TransformerException {
		DocumentBuilderFactory doc_factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder doc_builder = doc_factory.newDocumentBuilder();
		
		Document song_doc = doc_builder.newDocument();
		Element root_element = song_doc.createElement("AccSong");
		song_doc.appendChild(root_element);

		for(int i=0;i<events.size();i+=1) {
			if(events.get(i) instanceof FWSStyleChangeEvent) {
				FWSStyleChangeEvent style_event = (FWSStyleChangeEvent)events.get(i);
				Element style_element = song_doc.createElement("Style");

				if(!style_event.style_name.isEmpty()) {
					Element style_name = song_doc.createElement("Style");
					style_name.setTextContent(style_event.style_name);
					style_element.appendChild(style_name);

					Element section_name = song_doc.createElement("Section");
					section_name.setTextContent(style_event.section_name);
					style_element.appendChild(section_name);

					Element style_tick = song_doc.createElement("StyleTick");
					style_tick.setTextContent(Long.toString(style_event.style_tick));
					style_element.appendChild(style_tick);

					Element sections = song_doc.createElement("Sections");
					sections.setTextContent(Integer.toString(style_event.getSections()&0xFF));
					style_element.appendChild(sections);
				}

				Element tick = song_doc.createElement("Tick");
				tick.setTextContent(Long.toString(style_event.tick));
				style_element.appendChild(tick);

				root_element.appendChild(style_element);
			} else if(events.get(i) instanceof FWSChordEvent) {
				FWSChordEvent chord_event = (FWSChordEvent)events.get(i);
				Element chord_element = song_doc.createElement("Chord");

				Element main_root = song_doc.createElement("MainRoot");
				main_root.setTextContent(Integer.toString(chord_event.main_chord.getFullRoot()&0xFF));
				chord_element.appendChild(main_root);

				Element main_chord = song_doc.createElement("MainChord");
				main_chord.setTextContent(Integer.toString(chord_event.main_chord.getChord()&0xFF));
				chord_element.appendChild(main_chord);

				Element bass_root = song_doc.createElement("BassRoot");
				bass_root.setTextContent(Integer.toString(chord_event.bass_chord.getFullRoot()&0xFF));
				chord_element.appendChild(bass_root);

				Element bass_chord = song_doc.createElement("BassChord");
				bass_chord.setTextContent(Integer.toString(chord_event.bass_chord.getChord()&0xFF));
				chord_element.appendChild(bass_chord);

				Element inversion = song_doc.createElement("Inversion");
				inversion.setTextContent(Integer.toString(chord_event.inversion));
				chord_element.appendChild(inversion);

				Element tick = song_doc.createElement("Tick");
				tick.setTextContent(Long.toString(chord_event.tick));
				chord_element.appendChild(tick);

				root_element.appendChild(chord_element);
			}
		}

		TransformerFactory transformer_factory = TransformerFactory.newInstance();
		Transformer transformer = transformer_factory.newTransformer();

		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(song_doc);
		StreamResult result = new StreamResult(output);

		transformer.transform(source, result);
	}

	/** Get an XML from song info. */
	private static void getSongInfoXML(FWSSong song, OutputStream output) throws ParserConfigurationException, TransformerException {
		DocumentBuilderFactory doc_factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder doc_builder = doc_factory.newDocumentBuilder();

		FWSSongMetadata metadata = song.getSongMetadata();
		
		Document song_doc = doc_builder.newDocument();
		Element root_element = song_doc.createElement("AccSong");
		song_doc.appendChild(root_element);

		Element long_title = song_doc.createElement("LongTitle");
		long_title.appendChild(song_doc.createCDATASection(metadata.long_title));
		root_element.appendChild(long_title);

		Element short_title = song_doc.createElement("ShortTitle");
		short_title.appendChild(song_doc.createCDATASection(metadata.short_title));
		root_element.appendChild(short_title);

		Element composer = song_doc.createElement("Composer");
		composer.appendChild(song_doc.createCDATASection(metadata.composer));
		root_element.appendChild(composer);

		Element split_point = song_doc.createElement("SplitPoint");
		split_point.setTextContent(Byte.toString(metadata.split_point));
		root_element.appendChild(split_point);

		Element chord_channel = song_doc.createElement("AccChannel");
		chord_channel.setTextContent(Byte.toString(metadata.chord_channel));
		root_element.appendChild(chord_channel);

		Element rh_channel = song_doc.createElement("RightChannel");
		rh_channel.setTextContent(Byte.toString(metadata.melody_rh_channel));
		root_element.appendChild(rh_channel);

		Element lh_channel = song_doc.createElement("LeftChannel");
		lh_channel.setTextContent(Byte.toString(metadata.melody_lh_channel));
		root_element.appendChild(lh_channel);

		TransformerFactory transformer_factory = TransformerFactory.newInstance();
		Transformer transformer = transformer_factory.newTransformer();

		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(song_doc);
		StreamResult result = new StreamResult(output);

		transformer.transform(source, result);
	}

	/** Get song info from an XML. */
	private static void loadSongInfoXML(FWSSong song, File input) throws ParserConfigurationException, SAXException, IOException, NumberFormatException {
		DocumentBuilderFactory doc_factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder doc_builder = doc_factory.newDocumentBuilder();

		FWSSongMetadata metadata = song.getSongMetadata();

		Document meta_doc = doc_builder.parse(input);
		meta_doc.getDocumentElement().normalize();

		Element root = meta_doc.getDocumentElement();

		NodeList meta_nodes = root.getChildNodes();
		final int len = meta_nodes.getLength();
		
		for(int i=0;i<len;i+=1) {
			Node item = meta_nodes.item(i);
			if(item.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element)item;
				final String param = item.getNodeName();
				if(param.equalsIgnoreCase("LONGTITLE"))
					metadata.long_title = element.getTextContent();
				else if(param.equalsIgnoreCase("SHORTTITLE"))
					metadata.short_title = element.getTextContent();
				else if(param.equalsIgnoreCase("COMPOSER"))
					metadata.composer = element.getTextContent();
				else if(param.equalsIgnoreCase("SPLITPOINT"))
					metadata.split_point = Byte.parseByte(element.getTextContent());
				else if(param.equalsIgnoreCase("ACCCHANNEL"))
					metadata.chord_channel = Byte.parseByte(element.getTextContent());
				else if(param.equalsIgnoreCase("LeftChannel"))
					metadata.melody_lh_channel = Byte.parseByte(element.getTextContent());
				else if(param.equalsIgnoreCase("RightChannel"))
					metadata.melody_rh_channel = Byte.parseByte(element.getTextContent());
			}
		}
	}

	/** Get chords and style changes from an XML. */
	private static void loadChordInfoXML(FWSSong song, File input) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory doc_factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder doc_builder = doc_factory.newDocumentBuilder();

		Document chord_doc = doc_builder.parse(input);
		chord_doc.getDocumentElement().normalize();

		Element root = chord_doc.getDocumentElement();

		NodeList chord_nodes = root.getChildNodes();
		final int len = chord_nodes.getLength();

		byte last_sections = (byte)0xFF;

		ArrayList<FWSEvent> change_events = new ArrayList<>(0);

		for(int i=0;i<len;i+=1) {
			Node item = chord_nodes.item(i);
			if(item.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element)item;
				if(item.getNodeName().equalsIgnoreCase("STYLE")) { //Style change.
					NodeList style_event_nodes = element.getChildNodes();
					final int slen = style_event_nodes.getLength();

					FWSStyleChangeEvent style_event = new FWSStyleChangeEvent();
					style_event.setSections(last_sections);

					for(int j=0;j<slen;j+=1) {
						Node s_item = style_event_nodes.item(j);
						
						if(s_item == null)
							continue;
						
						if(s_item.getNodeType() == Node.ELEMENT_NODE) {
							Element s_element = (Element)s_item;
							
							if(s_item.getNodeName().equalsIgnoreCase("STYLE"))
								style_event.style_name = s_element.getTextContent();
							else if(s_item.getNodeName().equalsIgnoreCase("SECTION"))
								style_event.section_name = s_element.getTextContent();
							else if(s_item.getNodeName().equalsIgnoreCase("STYLETICK"))
								style_event.style_tick = Long.parseLong(s_element.getTextContent());
							else if(s_item.getNodeName().equalsIgnoreCase("SECTIONS")) {
								style_event.setSections((byte)Integer.parseInt(s_element.getTextContent()));
								last_sections = style_event.getSections();
							} else if(s_item.getNodeName().equalsIgnoreCase("TICK"))
								style_event.tick = Long.parseLong(s_element.getTextContent());
						}
					}

					change_events.add(style_event);
				} else if(item.getNodeName().equalsIgnoreCase("CHORD")) {
					NodeList chord_event_nodes = element.getChildNodes();
					final int clen = chord_event_nodes.getLength();

					FWSChordEvent chord = new FWSChordEvent();
					byte main_root = 0x7F, main_chord = 34, bass_root = 0x7F, bass_chord = 34;
					int inversion = 0;
					
					for(int j=0;j<clen;j+=1) {
						Node c_item = chord_event_nodes.item(j);
						
						if(c_item == null)
							continue;
						
						if(c_item.getNodeType() == Node.ELEMENT_NODE) {
							Element c_element = (Element)c_item;
							
							if(c_item.getNodeName().equalsIgnoreCase("MAINROOT"))
								main_root = (byte)Integer.parseInt(c_element.getTextContent());
							else if(c_item.getNodeName().equalsIgnoreCase("MAINCHORD"))
								main_chord = (byte)Integer.parseInt(c_element.getTextContent());
							else if(c_item.getNodeName().equalsIgnoreCase("BASSROOT"))
								bass_root = (byte)Integer.parseInt(c_element.getTextContent());
							else if(c_item.getNodeName().equalsIgnoreCase("BASSCHORD"))
								bass_chord = (byte)Integer.parseInt(c_element.getTextContent());
							else if(c_item.getNodeName().equalsIgnoreCase("INVERSION"))
								inversion = Integer.parseInt(c_element.getTextContent());
							else if(c_item.getNodeName().equalsIgnoreCase("TICK"))
								chord.tick = Long.parseLong(c_element.getTextContent());
						}
					}

					chord.inversion = inversion;
					chord.main_chord = new ChordBody(main_root, main_chord);
					chord.bass_chord = new ChordBody(bass_root, bass_chord);

					change_events.add(chord);
				}
			}
		}

		for(int i=0;i<change_events.size();i+=1)
			song.getSongSequence().addEvent(change_events.get(i));
	}
}
