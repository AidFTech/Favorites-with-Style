package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;
import javax.swing.JOptionPane;

import org.python.core.PyException;
import org.python.core.PyFunction;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;

import fwsevents.FWSChordEvent;
import fwsevents.FWSEvent;
import fwsevents.FWSSequence;
import fwsevents.FWSStyleChangeEvent;
import options.MIDIExportOptions;
import song.FWSSong;

public class PluginController {
	private FWS controller;

	private Map<String, String> plugin_scripts = new LinkedHashMap<>();

	protected PluginController(FWS controller) {
		this.controller = controller;
		initPlugins();
	}

	/** Refresh the plugin list. */
	public void refresh() {
		initPlugins();
	}

	/** Load in provided plugins. */
	private void initPlugins() {
		plugin_scripts.clear();
		
		SaveLoadController save_load_controller = controller.save_load_controller;
		File main_directory = new File(save_load_controller.getDatapath());

		if(!main_directory.isDirectory())
			return;

		File[] main_file_list = main_directory.listFiles();
		if(main_file_list == null)
			return;

		for(File main_file: main_file_list) {
			if(main_file.isDirectory() && main_file.getName().equalsIgnoreCase("PLUGINS")) {
				File[] plugin_files = main_file.listFiles();

				//Get the interpreter.
				PythonInterpreter interpreter = new PythonInterpreter();

				for(File plugin_file: plugin_files) {
					if(!plugin_file.getName().toUpperCase().endsWith(".PY"))
						continue;

					if(!plugin_file.canRead())
						continue;

					try {
						BufferedReader file_reader = new BufferedReader(new FileReader(plugin_file));
						String line = "", str_script = "";

						while((line = file_reader.readLine()) != null)
							str_script += line + '\n';

						file_reader.close();
						
						try {
							interpreter.exec(str_script);
							PyString py_name = interpreter.get("plugin_name", PyString.class);
							final String name = py_name.asString();

							plugin_scripts.put(name, str_script);
						} catch(PyException e) {
							JOptionPane.showMessageDialog(controller.getMainWindow(), "Error initializing plugin " + plugin_file.getName() + ": " + e.getMessage(), "Plugin Error", JOptionPane.ERROR_MESSAGE);
							continue;
						}

					} catch (IOException e) {
						continue;
					}
				}
				
				interpreter.close();
				break;
			}
		}
	}

	/** Run the specified plugin. */
	public void runPlugin(final String plugin) {
		final String script = plugin_scripts.get(plugin);
		if(script == null)
			return;

		//Get the interpreter.
		PythonInterpreter interpreter = new PythonInterpreter();

		interpreter.exec(script);

		MIDIManager midi_manager = controller.getMidiManager();
		MIDIExportOptions export_options = new MIDIExportOptions();
		export_options.export_melody_lh = midi_manager.getPlayerOptions().export_melody_lh;
		export_options.export_melody_rh = midi_manager.getPlayerOptions().export_melody_rh;
		
		if(controller.output_profile != null)
			export_options.profile = controller.output_profile.getInstrumentFamily();
		else
			export_options.profile = "";

		export_options.instrument = "";
		if(!controller.getOutputInstrumentName().trim().isEmpty())
			export_options.instrument = controller.getOutputInstrumentName();

		byte[] default_black_chords = midi_manager.getPlayerOptions().black_chord_display, song_black_chords = export_options.black_chord_display;
		for(int i=0;i<default_black_chords.length;i+=1)
			song_black_chords[i] = default_black_chords[i];

		final String original_profile = controller.output_profile != null ? controller.output_profile.getInstrumentFamily() : "",
					original_instrument = controller.output_instrument;

		String py_function = "plugin_run";
		while(py_function != null && !py_function.isEmpty()) {
			py_function = callPluginFunction(interpreter, export_options, py_function);
		}

		controller.setOutputFamily(original_profile);
		controller.output_instrument = original_instrument;

		interpreter.close();
	}

	/** Call a plugin function. */
	private String callPluginFunction(PythonInterpreter interpreter, MIDIExportOptions export_options, final String function) {
		PyFunction plugin_function = interpreter.get(function, PyFunction.class);
		if(plugin_function == null)
			return null;

		controller.setOutputFamily(export_options.profile);
		controller.output_instrument = export_options.instrument;
		
		MIDIManager midi_manager = controller.getMidiManager();
		String[] variable_names = JythonHandler.getRequiredArgs(interpreter, function);
		PyObject[] variables = new PyObject[variable_names.length];

		FWSSequence sequence;
		if(controller instanceof FWSEditor) {
			FWSEditor editor_controller = (FWSEditor)controller;
			sequence = editor_controller.getActiveSequence();
		} else
			sequence = controller.getLoadedSong().getSongSequence();

		FWSSong song = controller.getLoadedSong();
		
		for(int i=0;i<variable_names.length;i+=1) {
			Object set_obj = null;
			switch(variable_names[i]) {
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
				case JythonHandler.ARG_CONTROLLER:
					set_obj = controller;
					break;
				case JythonHandler.ARG_MAIN_WINDOW:
					set_obj = this.controller.getMainWindow();
					break;
				case JythonHandler.ARG_CHORD_EVENTS:
					{
						ArrayList<FWSEvent> common_events = sequence.getCommonEvents();
						ArrayList<FWSEvent> chord_events = new ArrayList<>();
						for(FWSEvent event: common_events) {
							if(event instanceof FWSChordEvent)
								chord_events.add((FWSChordEvent)event);
						}

						set_obj = FWSEvent.createCopy(chord_events);
					}
					break;
				case JythonHandler.ARG_SONG_EVENTS_MELODY:
					set_obj = FWSEvent.createCopy(sequence.getAllEvents());
					break;
				case JythonHandler.ARG_SONG_EVENTS_ALL:
					{
						try {
							ArrayList<FWSEvent> common_events = sequence.getCommonEvents();
							ArrayList<FWSEvent> style_events = new ArrayList<>();
							for(FWSEvent event: common_events) {
								if(event instanceof FWSChordEvent || event instanceof FWSStyleChangeEvent)
									style_events.add(event);
							}
							style_events = FWSEvent.createCopy(style_events);

							Sequence midi_sequence = midi_manager.getSongMIDISequence(song, export_options);
							FWSSequence full_sequence = FWSSequence.getFWSSequencefromSequence(midi_sequence);
							
							ArrayList<FWSEvent> midi_events = full_sequence.getAllEvents();
							for(FWSEvent event: style_events)
								FWSEvent.insertEvent(midi_events, event);

							set_obj = midi_events;

						} catch (InvalidMidiDataException e) {
							set_obj = null;
							break;
						}
					}
					break;
				case JythonHandler.ARG_FILEPATH:
					set_obj = new PyString(controller.save_load_controller.getLastFilepath());
					break;
			}

			interpreter.set("set_obj", set_obj);
			variables[i] = interpreter.get("set_obj");
		}

		try {
			PyObject result = plugin_function.__call__(variables);
			if(result instanceof PyString)
				return ((PyString)result).asString();
		} catch(Error | PyException e) {
			JOptionPane.showMessageDialog(controller.getMainWindow(), e.getMessage(), "Plugin Error", JOptionPane.ERROR_MESSAGE);	
		}

		return null;
	}

	/** Get the plugin names. */
	public String[] getPluginNames() {
		ArrayList<String> plugin_names_vec = new ArrayList<>();

		for(Entry<String, String> plugin: plugin_scripts.entrySet())
			plugin_names_vec.add(plugin.getKey());

		String[] plugin_names = new String[plugin_names_vec.size()];
		plugin_names_vec.toArray(plugin_names);

		return plugin_names;
	}
}
