package controllers;

import java.util.ArrayList;

import org.python.core.PyException;
import org.python.core.PyInteger;
import org.python.core.PyTuple;
import org.python.util.PythonInterpreter;

public class JythonHandler {
	protected final static String ARG_SONG_META = "song_metadata", //Complete song metadata.
								ARG_EXPORT_OPTIONS = "export_options", //Export options.
								ARG_INSTRUMENT = "instrument", //Instrument name.

								ARG_SONG_SHORT_TITLE = "short_title", //Short song title.
								ARG_SONG_TITLE = "song_title", //Long song title.

								ARG_SONG_TPQ = "tpq", //TPQ.
								ARG_CURRENT_TICK = "current_tick", //The current song tick.

								ARG_CHORD_EVENTS = "chord_events", //Chord event list.
								ARG_SONG_EVENTS_MELODY = "melody_events", //All melody events.
								ARG_SONG_EVENTS_ALL = "song_events", //All song events, including calculated style events.

								//Only valid during calculatoin.
								ARG_CHORD_MAIN = "main_chord", //Main chord.
								ARG_CHORD_BASS = "bass_chord", //Bass chord.
								ARG_CHORD_INV = "chord_inv", //Chord inversion.
								ARG_CHORD_FINGERING = "chord_fingering", //Chord fingering.
								ARG_NOTE_FINGERING = "note_fingering"; //Note fingering.

	/** Get the required args for a Python function. */
	protected static String[] getRequiredArgs(PythonInterpreter interpreter, String function) {
		try {
			ArrayList<String> ret_string_vec = new ArrayList<>(0);

			interpreter.exec("var_code = " + function + ".__code__");
			interpreter.exec("argc = var_code.co_argcount");
			interpreter.exec("args = var_code.co_varnames");

			PyInteger argc_p = interpreter.get("argc", PyInteger.class);
			PyTuple args = interpreter.get("args", PyTuple.class);

			final int argc = argc_p.asInt();

			for(int i=0;i<argc;i+=1) {
				Object test_obj = args.get(i);
				if(test_obj instanceof String)
					ret_string_vec.add((String)test_obj);
			}

			String[] ret_strings = new String[ret_string_vec.size()];
			ret_string_vec.toArray(ret_strings);
			return ret_strings;
		} catch(PyException e) {
			return new String[0];
		}
	}
}
