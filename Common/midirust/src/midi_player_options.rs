use jni::bind_java_type;

extern crate jni;

bind_java_type! {
	rust_type = pub JMidiPlayerOptions,
	java_type = options.MIDIPlayerOptions,

	fields {
		pub play: jboolean,

		pub song_melody_rh {
			sig = jbyte,
			name = "song_melody_rh",
		},

		pub song_melody_lh {
			sig = jbyte,
			name = "song_melody_lh",
		},

		pub instrument_melody_rh {
			sig = jbyte,
			name = "instrument_melody_rh",
		},

		pub instrument_melody_lh {
			sig = jbyte,
			name = "instrument_melody_lh",
		},

		pub info_display {
			sig = infobox.InfoBox,
			name = "info_display",
		},

		pub style_section {
			sig = JString,
			name = "active_section",
		},

		pub active_chord_root {
			sig = jbyte,
			name = "active_chord_root",
		},

		pub active_chord_type {
			sig = jbyte,
			name = "active_chord_type",
		},

		pub start_tick {
			sig = jlong,
			name = "start_tick",
		},

		pub current_tick {
			sig = jlong,
			name = "current_tick",
		},
	},
}

bind_java_type! {
	rust_type = pub JExportOptions,
	java_type = options.MIDIExportOptions,

	fields {
		pub truncate: jboolean,

		pub export_melody_rh {
			sig = jbyte,
			name = "export_melody_rh",
		},

		pub export_melody_lh {
			sig = jbyte,
			name = "export_melody_lh",
		},
	},
}