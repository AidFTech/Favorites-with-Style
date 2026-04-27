use jni::{Env, bind_java_type, jni_sig, jni_str};

use crate::fws_style::{Chord, Note};

extern crate jni;

#[derive(Clone)]
pub struct GenericMIDIEvent {
	pub data: Vec<u8>,
	pub tick: u64,
}

impl GenericMIDIEvent {
	pub fn new(data: Vec<u8>, tick: u64) -> Self {
		return GenericMIDIEvent {
			data,
			tick
		};
	}

	///Return a list of short events.
	pub fn separate_shorts(list: &Vec<GenericMIDIEvent>, command: &[u8], keep_long: bool) -> Vec<GenericMIDIEvent> {
		let mut ret_list = Vec::new();

		for ev in list.clone() {
			if ev.data.len() > 0 {
				let mut add = false;
				for i in command {
					if (ev.data[0]&0xF0) == *i {
						add = true;
						break;
					}
				}
				
				if add {
					ret_list.push(ev);
				}
			} else if ev.data.len() >0 && (ev.data[0]&0xF0) == 0xF0 && keep_long {
				ret_list.push(ev);
			}
		}

		return ret_list;
	}

	///Return a list of note events.
	pub fn separate_notes(list: &Vec<GenericMIDIEvent>) -> Vec<GenericMIDIEvent> {
		let mut ret_list = Vec::new();
		
		for ev in list.clone() {
			if ev.data.len() > 0 && (ev.data[0]&0xF0 == 0x80 || ev.data[0]&0xF0 == 0x90) {
				ret_list.push(ev);
			}
		}

		return ret_list;
	}

	///Return which notes are on at the specified tick.
	pub fn get_notes_on_at(list: &Vec<GenericMIDIEvent>, tick: u64) -> Vec<GenericMIDIEvent> {
		let mut note_vel = [[0; 128]; 16];
		let mut note_tick = [[0; 128]; 16];
		for ev in list {
			if ev.tick > tick {
				continue;
			}
			
			if ev.data.len() >= 3 && (ev.data[0]&0xE0) == 0x80 { //Note event.
				let channel = ev.data[0]&0xF;
				let note = ev.data[1];
				let vel = ev.data[2];
				if (ev.data[0]&0xF0) == 0x90 && ev.data[2] > 0 { //Note on.
					note_vel[channel as usize][note as usize] = vel;
					note_tick[channel as usize][note as usize] = ev.tick;
				} else { //Note off.
					note_vel[channel as usize][note as usize] = 0;
				}
			}
		}

		let mut new_list = Vec::new();
		for c in 0..note_vel.len() {
			for n in 0..note_vel[c].len() {
				if note_vel[c][n] > 0 {
					let b0 = 0x90 | (c as u8);
					new_list.push(GenericMIDIEvent::new((&[b0, n as u8, note_vel[c][n]]).to_vec(), note_tick[c][n]));
				}
			}
		}
		
		return new_list;
	}
}

#[derive(Clone)]
pub struct TimeChangeEvent {
	pub us_per_tick: u64,
	pub tick: u64,
}

impl TimeChangeEvent {
	pub fn new(us_per_tick: u64, tick: u64) -> Self {
		return TimeChangeEvent { us_per_tick, tick };
	}
}

pub struct AccidentalChangeEvent {
	pub accidental_count: i8,
	pub tick: u64,
}

impl AccidentalChangeEvent {
	pub fn new(accidental_count: i8, tick: u64) -> Self {
		return AccidentalChangeEvent { accidental_count, tick };
	}
}

bind_java_type! {
	rust_type = pub JFWSNoteEvent,
	java_type = fwsevents.FWSNoteEvent,

	fields {
		pub channel: jbyte,
		pub note: jbyte,
		pub velocity: jbyte,
		pub tick: jlong,
		pub duration: jlong,
	}
}

bind_java_type! {
	rust_type = pub JFWSVoiceEvent,
	java_type = fwsevents.FWSVoiceEvent,

	fields {
		pub voice: jbyte,
		pub voice_lsb {
			sig = jbyte,
			name = "voice_lsb",
		},
		pub voice_msb {
			sig = jbyte,
			name = "voice_msb",
		},
		pub channel: jbyte,
		pub tick: jlong,
	}
}

bind_java_type! {
	rust_type = pub JFWSShortEvent,
	java_type = fwsevents.FWSShortEvent,

	fields {
		pub channel: jbyte,
		pub command: jbyte,
		pub data1: jbyte,
		pub data2: jbyte,
		pub tick: jlong,
	}
}

bind_java_type! {
	rust_type = pub JFWSTempoEvent,
	java_type = fwsevents.FWSTempoEvent,

	fields {
		pub tempo: jint,
		pub tick: jlong,
	}
}

bind_java_type! {
	rust_type = pub JFWSTimeSignatureEvent,
	java_type = fwsevents.FWSTimeSignatureEvent,

	fields {
		pub num: jbyte,
		pub den: jbyte,
		pub tick: jlong,
	}
}

bind_java_type! {
	rust_type = pub JFWSKeySignatureEvent,
	java_type = fwsevents.FWSKeySignatureEvent,

	fields {
		pub accidental_count {
			sig = jbyte,
			name = "accidental_count",
		},
		pub major: jboolean,
		pub tick: jlong,
	}
}

bind_java_type! {
	rust_type = pub JFWSChordEvent,
	java_type = fwsevents.FWSChordEvent,

	fields {
		pub main_chord {
			sig = style.ChordBody,
			name = "main_chord",
		},

		pub bass_chord {
			sig = style.ChordBody,
			name = "bass_chord",
		},

		pub inversion: jint,
		pub tick: jlong,
	},
}

bind_java_type! {
	rust_type = pub JFWSStyleEvent,
	java_type = fwsevents.FWSStyleChangeEvent,

	fields {
		pub style_name {
			sig = JString,
			name = "style_name",
		},
		pub section_name {
			sig = JString,
			name = "section_name",
		},
		pub style_tick {
			sig = jlong,
			name = "style_tick",
		},

		pub tick: jlong,

		pub sub_rhythm {
			sig = jboolean,
			name = "sub_rhythm",
		},

		pub rhythm: jboolean,
		pub bass: jboolean,
		pub chord1: jboolean,
		pub chord2: jboolean,
		pub pad: jboolean,
		pub phrase1: jboolean,
		pub phrase2: jboolean,
	}
}

pub struct ChordChangeEvent {
	pub main_root: Note,
	pub main_full: i8,
	pub main_chord: Chord,
	pub bass_root: Note,
	pub bass_full: i8,
	pub bass_chord: Chord,

	pub inversion: i8,
	pub tick: u64,
}

impl ChordChangeEvent {
	///Get a Rust chord change event.
	pub fn get(env: &mut Env<'_>, j_event: JFWSChordEvent) -> Self {
		let main_chord = j_event.main_chord(env).unwrap();
		let bass_chord = j_event.bass_chord(env).unwrap();

		let main_root = Note::from_i8(env.call_method(&main_chord, jni_str!("getRoot"), jni_sig!(() -> jbyte), &[]).unwrap().b().unwrap());
		let main_full = env.call_method(&main_chord, jni_str!("getFullRoot"), jni_sig!(() -> jbyte), &[]).unwrap().b().unwrap();
		let main_chord = Chord::from_i8(env.call_method(&main_chord, jni_str!("getChord"), jni_sig!(() -> jbyte), &[]).unwrap().b().unwrap());

		let bass_root = Note::from_i8(env.call_method(&bass_chord, jni_str!("getRoot"), jni_sig!(() -> jbyte), &[]).unwrap().b().unwrap());
		let bass_full = env.call_method(&bass_chord, jni_str!("getFullRoot"), jni_sig!(() -> jbyte), &[]).unwrap().b().unwrap();
		let bass_chord = Chord::from_i8(env.call_method(&bass_chord, jni_str!("getChord"), jni_sig!(() -> jbyte), &[]).unwrap().b().unwrap());

		let inversion = j_event.inversion(env).unwrap() as i8;
		let tick = j_event.tick(env).unwrap() as u64;

		return ChordChangeEvent { main_root, main_full, main_chord, bass_root, bass_full, bass_chord, inversion, tick };
	}
}

pub struct StyleChangeEvent {
	pub style: String,
	pub section: String,

	pub parts: [bool; 8],

	pub tick: u64,
	pub style_tick: u64,
	pub retain: bool,
}

impl StyleChangeEvent {
	//Get a Rust style change event.
	pub fn get(env: &mut Env<'_>, j_event: JFWSStyleEvent) -> Self {
		let mut style_change_event = StyleChangeEvent{
			style: "".to_string(),
			section: "".to_string(),
			
			parts: [true; 8],

			tick: 0,
			style_tick: 0,
			retain: false,
		};

		style_change_event.style = j_event.style_name(env).unwrap().to_string();
		style_change_event.section = j_event.section_name(env).unwrap().to_string();

		style_change_event.parts[0] = j_event.sub_rhythm(env).unwrap();
		style_change_event.parts[1] = j_event.rhythm(env).unwrap();
		style_change_event.parts[2] = j_event.bass(env).unwrap();
		style_change_event.parts[3] = j_event.chord1(env).unwrap();
		style_change_event.parts[4] = j_event.chord2(env).unwrap();
		style_change_event.parts[5] = j_event.pad(env).unwrap();
		style_change_event.parts[6] = j_event.phrase1(env).unwrap();
		style_change_event.parts[7] = j_event.phrase2(env).unwrap();

		style_change_event.tick = j_event.tick(env).unwrap() as u64;

		let style_tick = j_event.style_tick(env).unwrap();
		if style_tick >= 0 {
			style_change_event.style_tick = style_tick as u64;
		} else {
			style_change_event.retain = true;
		}

		return style_change_event;
	}
}