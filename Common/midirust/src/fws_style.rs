use jni::{Env, bind_java_type, jni_sig, jni_str, objects::JString};
use jni::JValue::Int;

use strum::IntoEnumIterator;
use strum_macros::EnumIter;


use crate::{fws_event::{GenericMIDIEvent, TimeChangeEvent}, fws_sequence::JFwsSequence};

const ROOT_FIXED_TABLE: [(i8, [i8; 12]); 4] = [(0, [0, 1, 2, 10, 11, 0, 1, 11, 0, 1, 2, 11]),
												(4, [4, 5, 6, 2, 3, 4, 5, 2, 3, 4, 5, 3]),
												(7, [7, 8, 9, 7, 8, 9, 10, 6, 7, 8, 9, 6]),
												(11, [11, 0, 1, 10, 11, 0, 1, 11, 0, 1, 2, 10])];

const CHORD_TABLE_MELODY: [(i8, [i8; 38]); 6] = [(0, [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0]),
												(2, [2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 0, 2, 2, 0, 0, 2, 0, 0, 2, 0, 0, 2, 0, 0, 1, 0, 3, 0, 0, 0, 0, 0, 2, -1, 0, 2, 0]),
												(4, [4, 4, 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 5, 4, 4, 4, 4, 4, 4, 4, 4, 4, 0, 0, 5, 2, -1, 4, 4, 3]),
												(7, [7, 7, 7, 7, 7, 7, 7, 8, 7, 9, 7, 6, 7, 10, 5, 7, 11, 6, 6, 7, 7, 6, 10, 6, 9, 10, 8, 10, 8, 8, 12, 7, 7, 7, -1, 6, 6, 6]),
												(9, [9, 9, 9, 9, 9, 9, 9, 10, 7, 9, 10, 6, 7, 10, 10, 7, 7, 6, 8, 10, 10, 10, 10, 10, 10, 10, 10, 10, 8, 8, 12, -1, 7, 7, -1, 6, 9, 6]),
												(11, [12, 12, 11, 11, 11, 11, 14, 12, 12, 12, 10, 10, 14, 14, 10, 11, 14, 12, 9, 10, 10, 10, 14, 10, 10, 13, 10, 15, 11, 10, 12, 12, 12, 12, -1, 11, 12, 11])];

const CHORD_TABLE_CHORD: [(i8, [i8; 38]); 4] = [(0, [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0]),
												(4, [4, 4, 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 5, 4, 4, 4, 4, 4, 4, 4, 4, 4, 0, 0, 5, 2, -1, 4, 4, 3]),
												(7, [7, 9, 7, 6, 7, 11, 9, 8, 7, 9, 7, 6, 7, 10, 5, 7, 11, 6, 6, 7, 7, 6, 10, 6, 9, 10, 8, 10, 8, 8, 12, 7, 7, 7, -1, 6, 6, 6]),
												(11, [12, 12, 11, 11, 14, 14, 14, 12, 12, 12, 10, 10, 14, 14, 10, 11, 14, 12, 9, 10, 10, 10, 14, 10, 10, 13, 10, 15, 11, 10, 12, 12, 12, 12, -1, 11, 12, 11])];

const CHORD_FINGERING: [[i8; 5]; 38] = [[0, 4, 7, -1, -1],
										[0, 4, 7, 9, -1],
										[0, 4, 7, 11, -1],
										[0, 4, 6, 7, 11],
										[0, 2, 4, 7, -1],
										[0, 2, 4, 7, 11],
										[0, 2, 4, 7, 9],
										[0, 4, 8, -1, -1],
										[0, 3, 7, -1, -1],
										[0, 3, 7, 9, -1],
										[0, 3, 7, 10, -1],
										[0, 3, 6, 10, -1],
										[0, 2, 3, 7, -1],
										[0, 2, 3, 7, 10],
										[0, 3, 5, 7, 10],
										[0, 3, 7, 11, -1],
										[0, 2, 3, 7, 11],
										[0, 3, 6, -1, -1],
										[0, 3, 6, 9, -1],
										[0, 4, 7, 10, -1],
										[0, 5, 7, 10, -1],
										[0, 4, 6, 10, -1],
										[0, 2, 4, 7, 10],
										[0, 4, 6, 7, 10],
										[0, 4, 7, 9, 10],
										[0, 1, 4, 7, 10],
										[0, 4, 7, 8, 10],
										[0, 3, 4, 7, 10],
										[0, 4, 8, 11, -1],
										[0, 4, 8, 10, -1],
										[0, 12, -1, -1, -1],
										[0, 7, -1, -1, -1],
										[0, 5, 7, -1, -1],
										[0, 2, 7, -1, -1],
										[-1, -1, -1, -1, -1],
										[0, 4, 6, 11, -1],
										[0, 4, 6, -1, -1],
										[0, 3, 6, 11, -1]];

#[allow(unused_attributes)]
#[allow(dead_code)]
#[derive(EnumIter, Debug, PartialEq, Clone, Copy)]
#[repr(i8)]
pub enum NTT {
	NTTBypass,
	NTTMelody,
	NTTChord,
	NTTBass,
	NTTMelodic,
	NTTMelodic5,
	NTTHarmonic,
}

#[allow(unused_attributes)]
#[allow(dead_code)]
#[derive(EnumIter, Debug, PartialEq, Clone, Copy)]
#[repr(i8)]
pub enum RTR {
	RTRStop,
	RTRPitchShift,
	RTRPitchShiftRoot,
	RTRRetrigger,
	RTRRetriggerRoot,
}

#[allow(unused_attributes)]
#[allow(clippy::all)]
#[derive(EnumIter, Debug, PartialEq, Clone, Copy)]
#[repr(i8)]
pub enum Note {
	NoteNC = -1,
	NoteC,
	NoteCs,
	NoteD,
	NoteEf,
	NoteE,
	NoteF,
	NoteFs,
	NoteG,
	NoteAf,
	NoteA,
	NoteBf,
	NoteB
}

impl Note {
	pub fn from_i8(n: i8) -> Self {
		for note in Note::iter() {
			if note as i8 == n {
				return note;
			}
		}
		return Self::NoteNC;
	}
}

#[allow(unused_attributes)]
#[allow(dead_code)]
#[derive(EnumIter, Debug, PartialEq, Clone, Copy)]
#[repr(i8)]
pub enum Chord {
	ChordMaj,
	ChordMaj6,
	ChordMaj7,
	ChordMaj7s11,
	ChordMaj9,
	ChordMaj79,
	ChordMaj69,
	ChordAug,
	ChordMin,
	ChordMin6,
	ChordMin7,
	ChordMin7f5,
	ChordMin9,
	ChordMin79,
	ChordMin711,
	ChordMinMaj7,
	ChordMinMaj79,
	ChordDim,
	ChordDim7,
	Chord7,
	Chord7sus,
	Chord7f5,
	Chord79,
	Chord7s11,
	Chord713,
	Chord7f9,
	Chord7f13,
	Chord7s9,
	ChordMaj7Aug,
	Chord7Aug,
	Chord8,
	Chord5,
	Chordsus4,
	Chordsus2,
	ChordOff,
	ChordMaj7f5,
	Chordf5,
	ChordMinMaj7f5
}

impl Chord {
	pub fn from_i8(c: i8) -> Self {
		for chord in Chord::iter() {
			if chord as i8 == c {
				return chord;
			}
		}
		return Self::ChordOff;
	}
}

bind_java_type! {
	rust_type = pub JFwsStyle,
	java_type = style.Style,

	fields {
		pub long_name {
			sig = JString,
			name = "long_name",
		}
	},

	methods {
		pub fn get_tpq {
			sig = () -> jint,
			name = "getTPQ",
		},

		priv fn j_get_section {
			sig = (JString) -> fwsevents.FWSSequence,
			name = "getSection",
		},

		priv fn j_get_casm {
			sig = () -> style.Casm,
			name = "getCasm",
		},

		pub fn get_section_names() -> JString[],
	},
}

impl<'local> JFwsStyle<'local> {
	///Get a section from the sequence.
	pub fn get_section(&self, env: &mut Env<'_>, section_name: &str) -> (Vec<TimeChangeEvent>, Vec<GenericMIDIEvent>) {
		let j_section_name = JString::from_str(env, section_name).unwrap();
		let j_sequence = match self.j_get_section(env, j_section_name) {
			Ok(sequence) => sequence,
			Err(e) => {
				println!{"Error: {}", e};
				return (Vec::new(), Vec::new());
			}
		};

		let sequence = env.cast_local::<JFwsSequence>(j_sequence).unwrap();
		let (time_events, events, _, _, _) = sequence.get_all_events(env, &[]);
		
		return(time_events, events);
	}

	///Get the length of a section.
	pub fn get_section_length(&self, env: &mut Env<'_>, section_name: &str) -> u64 {
		let j_section_name = JString::from_str(env, section_name).unwrap();
		let j_sequence = match self.j_get_section(env, j_section_name) {
			Ok(sequence) => sequence,
			Err(e) => {
				println!{"Error: {}", e};
				return 0;
			}
		};

		let sequence = env.cast_local::<JFwsSequence>(j_sequence).unwrap();
		return sequence.get_sequence_length_strict(env).unwrap() as u64;
	}
}

#[derive(Clone)]
pub struct FwsStyle {
	pub long_name: String,

	pub tpq: u64,

	pub event_sections: Vec<Vec<GenericMIDIEvent>>,
	pub time_sections: Vec<Vec<TimeChangeEvent>>,
	pub length_sections: Vec<u64>,

	drum_sections: Vec<bool>,
	drum_channels: Vec<Vec<i8>>,

	pub sections: Vec<String>,

	casm: Option<Casm>,
}

impl <'local> FwsStyle {
	///Get a Rust style from a Java style.
	pub fn get(env: &mut Env<'_>, j_style: JFwsStyle<'local>) -> Self {
		let j_names = j_style.get_section_names(env).unwrap();
		let name_count = j_names.len(env).unwrap();

		let mut section_names = Vec::new();
		for i in 0..name_count {
			let j_name = j_names.get_element(env, i).unwrap();
			let name = j_name.to_string();
			section_names.push(name);
		}

		let mut event_sections = Vec::new();
		let mut time_sections = Vec::new();
		let mut length_sections = Vec::new();

		for s in &section_names {
			let(times, events) = j_style.get_section(env, s);

			event_sections.push(events);
			time_sections.push(times);

			let l = j_style.get_section_length(env, s);
			length_sections.push(l);
		}

		let tpq = j_style.get_tpq(env).unwrap() as u64;

		let mut style = FwsStyle {
			long_name: j_style.long_name(env).unwrap().to_string(),

			sections: section_names,
			event_sections: Vec::new(),
			time_sections,
			length_sections,
			drum_sections: Vec::new(),
			drum_channels: Vec::new(),
			tpq,

			casm: None,
		};

		let casm = Casm::get(&style, j_style, env);

		match &casm {
			Some(casm) => {
				for es in event_sections {
					let mut drum_channels = Vec::new();

					let mut cseg = 0;
					for i in 0..casm.sections.len() {
						let cs = &casm.sections[i];
						if cs.section == i {
							cseg = cs.cseg;
							break;
						}
					}

					for i in 0..casm.parts.len() {
						if casm.parts[i].cseg == cseg && casm.parts[i].autostart && casm.parts[i].ntr && casm.parts[i].ntt == NTT::NTTBypass as i8 { 
							drum_channels.push(casm.parts[i].channel);
						}
					}

					let mut drum_track = true;
					for ev in &es {
						if ev.data.len() <= 0 {
							continue;
						}

						if (ev.data[0]&0xF0) == 0x90 {
							let channel = ev.data[0]&0xF;
							let mut drum_found = false;
							for i in &drum_channels {
								if *i == channel as i8 {
									drum_found = true;
									break;
								}
							}

							if !drum_found {
								drum_track = false;
								break;
							}
						}
					}

					style.event_sections.push(es);
					style.drum_channels.push(drum_channels);
					style.drum_sections.push(drum_track);
				}
			}
			None => {
				for es in event_sections {
					style.event_sections.push(es);
					style.drum_channels.push([8, 9].to_vec());
					style.drum_sections.push(false);
				}
			}
		}

		style.casm = casm;

		return style;
	}

	///Get the number of the provided section.
	pub fn get_section_number(&self, section: &str) -> Option<usize> {
		for i in 0..self.sections.len() {
			if self.sections[i].eq_ignore_ascii_case(section) {
				return Some(i);
			}
		}
		return None;
	}

	///Get the starting notes present in a section for each channel.
	fn get_section_notes(&self, section: usize) -> [Vec<u8>; 16] {
		let mut notes = [const { Vec::new() }; 16];

		if section > self.event_sections.len() {
			return notes;
		}

		for ev in &self.event_sections[section] {
			if ev.data.len() >= 2 && (ev.data[0]&0xF0 == 0x90) { //Note start.
				let note = ev.data[1];
				let c = ev.data[0]&0xF;

				let mut note_exists = false;
				for i in &notes[c as usize] {
					if *i == note {
						note_exists = true;
						break;
					}
				}
				if note_exists {
					continue;
				}

				notes[c as usize].push(note);
			}
		}

		return notes;
	}

	///Get the note map for the specified chord.
	pub fn get_chord_map(&self, section: usize, chord: &ChordBody) -> [Vec<(i8, i8)>; 16] {
		let start_notes = &self.get_section_notes(section);
		let mut note_map = [const { Vec::new() }; 16];

		let casm = match &self.casm {
			Some(casm) => casm,
			None => {
				return note_map;
			}
		};
		
		let mut cseg = 0;
		for cs in &casm.sections {
			if cs.section == section {
				cseg = cs.cseg;
				break;
			}
		}

		for c in 0..start_notes.len() {
			let mut casm_index = 0;
			let mut casm_found = false;
			for i in 0..casm.parts.len() {
				if casm.parts[i].source_channel == c as i8 && casm.parts[i].cseg == cseg {
					casm_index = i;
					casm_found = true;
					break;
				}
			}

			if !casm_found {
				continue;
			}

			let casm_part = &casm.parts[casm_index];
			let channel_start_notes = &start_notes[c];
			let mut channel_notes = channel_start_notes.clone();

			let mut skip = Vec::new(); //Elements of channel_start_notes to skip.

			for i in 0..channel_start_notes.len() {
				//Normalize to C.
				let mut n = Self::revert_to_c_maj7(channel_start_notes[i] as i8, casm_part);
				if n < 0 {
					skip.push(i);
					continue;
				}

				n = Self::calculate_chord(n, casm_part, chord);
				if n < 0 {
					skip.push(i);
					continue;
				}

				channel_notes[i] = n as u8;
			}

			for i in 0..channel_start_notes.len() {
				let mut skip_pass = false;
				for s in &skip {
					if *s == i {
						skip_pass = true;
						break;
					}
				}

				if skip_pass {
					continue;
				}

				note_map[c].push((channel_start_notes[i] as i8, channel_notes[i] as i8));
			}
		}

		return note_map;
	}

	///Get the channel map for the specified chord and section.
	pub fn get_channel_map(&self, section: usize) -> [i8; 16] {
		let mut channel_map = [-1; 16];

		let casm = match &self.casm {
			Some(casm) => casm,
			None => {
				return channel_map;
			}
		};
		
		let mut cseg_found = false;
		let mut cseg = 0;
		for cs in &casm.sections {
			if cs.section == section {
				cseg_found = true;
				cseg = cs.cseg;
				break;
			}
		}

		if !cseg_found {
			return [-1; 16];
		}

		for c in 0..channel_map.len() {
			let mut casm_index = 0;
			let mut casm_found = false;
			for i in 0..casm.parts.len() {
				if casm.parts[i].source_channel == c as i8 && casm.parts[i].cseg == cseg {
					casm_found = true;
					casm_index = i;
					break;
				}
			}

			if casm_found {
				channel_map[c] = casm.parts[casm_index].channel;
			}
		}

		return channel_map;
	}

	///Get the NTT rules.
	pub fn get_ntt(&self, section: usize) -> [NTT; 16] {
		let mut ntt_list = [const {NTT::NTTBypass}; 16];

		let casm = match &self.casm {
			Some(casm) => casm,
			None => {
				return ntt_list;
			}
		};
		
		let mut cseg_found = false;
		let mut cseg = 0;
		for cs in &casm.sections {
			if cs.section == section {
				cseg_found = true;
				cseg = cs.cseg;
				break;
			}
		}

		if !cseg_found {
			return ntt_list;
		}

		for c in 0..ntt_list.len() {
			let mut casm_index = 0;
			let mut casm_found = false;
			for i in 0..casm.parts.len() {
				if casm.parts[i].source_channel == c as i8 && casm.parts[i].cseg == cseg {
					casm_found = true;
					casm_index = i;
					break;
				}
			}

			if !casm_found {
				continue;
			}

			for r in NTT::iter() {
				if r as i8 == casm.parts[casm_index].ntt {
					ntt_list[c] = r;
					break;
				}
			}
		}

		return ntt_list;
	}

	///Get the retrigger rule.
	pub fn get_rtr(&self, section: usize) -> [RTR; 16] {
		let mut rtr_list = [const { RTR::RTRPitchShift }; 16];

		let casm = match &self.casm {
			Some(casm) => casm,
			None => {
				return rtr_list;
			}
		};
		
		let mut cseg_found = false;
		let mut cseg = 0;
		for cs in &casm.sections {
			if cs.section == section {
				cseg_found = true;
				cseg = cs.cseg;
				break;
			}
		}

		if !cseg_found {
			return rtr_list;
		}

		for c in 0..rtr_list.len() {
			let mut casm_index = 0;
			let mut casm_found = false;
			for i in 0..casm.parts.len() {
				if casm.parts[i].source_channel == c as i8 && casm.parts[i].cseg == cseg {
					casm_found = true;
					casm_index = i;
					break;
				}
			}

			if !casm_found {
				continue;
			}

			for r in RTR::iter() {
				if r as i8 == casm.parts[casm_index].rtr {
					rtr_list[c] = r;
					break;
				}
			}
		}

		return rtr_list;
	}

	///Get a chord from notes input on the keyboard.
	pub fn get_chord(notes: &[i8]) -> (Note, Chord) {
		let note_count = notes.len();

		//Find the base note.
		for b in 0..note_count{
			let test_base = notes[b];
			let mut test_notes = Vec::new();

			for i in 0..note_count {
				test_notes.push((notes[(b+i)%note_count]-test_base+12)%12);
			}

			for c in 0..CHORD_FINGERING.len() {
				let test_chord_static = CHORD_FINGERING[c];
				let mut chord_found = true;

				let mut test_chord = Vec::new();
				for n in test_chord_static {
					if n >= 0 {
						test_chord.push(n);
					}
				}

				if test_notes.len() != test_chord.len() {
					continue;
				}

				for i in 0..test_notes.len() {
					if i >= test_chord.len() || test_chord[i] < 0 {
						continue;
					}

					if test_notes[i] != test_chord[i] {
						chord_found = false;
						break;
					}
				}

				if chord_found {
					return (Note::from_i8(test_base%12), Chord::from_i8(c as i8));
				}
			}
		}

		return (Note::NoteNC, Chord::ChordOff);
	}

	///Revert a translated note to C M7.
	fn revert_to_c_maj7(note: i8, casm: &CasmPart) -> i8 {
		let casm_root = casm.source_chord;
		let casm_chord = casm.source_chord_type;
		let note_root = note%12;
		let mut new_note = note;

		//NTT...
		if casm.ntt != NTT::NTTBypass as i8 {
			let mut matched = false;

			let table = if casm.ntt == NTT::NTTChord as i8 {
				CHORD_TABLE_CHORD.as_slice()
			} else {
				CHORD_TABLE_MELODY.as_slice()
			};

			for k in table {
				if k.1[casm_chord as usize]%12 == note_root {
					let octave = note/12;
					new_note = k.0 + octave*12;
					matched = true;
					break;
				}
			}

			if !matched {
				new_note = -1;
			}
		}

		if new_note < 0 {
			return -1;
		}

		//Now for NTR.
		if casm.ntr { //Root fixed.
			if casm.ntt == NTT::NTTBypass as i8 {
				return note;
			}

			let mut matched = false;
			for k in ROOT_FIXED_TABLE {
				if k.1[casm_root as usize] == note_root {
					let octave = note/12;
					new_note = k.0 + octave*12;
					matched = true;
					break;
				}
			}

			if !matched {
				new_note = -1;
			}
		} else { //Root transposition. Easier.
			let difference = if casm_root > casm.high_key {
				12-casm_root
			} else {
				-casm_root
			};
			new_note += difference;
		}

		if new_note < 0 {
			return -1;
		}

		new_note = Self::normalize_note(new_note, casm);

		return new_note;
	}

	///Calculate the chord for a note.
	fn calculate_chord(note: i8, casm: &CasmPart, chord: &ChordBody) -> i8 {
		let chord_root = if casm.ntt == NTT::NTTBass as i8 {
			if chord.bass_root as i8 >= 0 {
				&chord.bass_root
			} else {
				&chord.main_root
			}
		} else {
			&chord.main_root
		};

		let chord_type = if casm.ntt == NTT::NTTBass as i8 {
			if chord.bass_chord as i8 >= 0 && chord.bass_chord != Chord::ChordOff {
				&chord.bass_chord
			} else {
				&chord.main_chord
			}
		} else {
			&chord.main_chord
		};

		if !casm.autostart && (chord_root == &Note::NoteNC || chord_type == &Chord::ChordOff) {
			return -1;
		}

		let mut new_note = note;
		let note_root = note%12;
		let chord_root_num = if chord_root != &Note::NoteNC {
			*chord_root as usize
		} else {
			casm.source_chord as usize
		};
		let chord_type_num = *chord_type as usize;

		if !casm.noteplay[chord_root_num] {
			return -1;
		}
		if !casm.chordplay[chord_type_num] {
			return -1;
		}

		//NTR.
		if casm.ntr { //Root fixed.
			if casm.ntt == NTT::NTTBypass as i8 {
				return note;
			}

			let mut note_found = false;
			for k in ROOT_FIXED_TABLE {
				if k.0 == note_root {
					note_found = true;
					let target_note = k.1[chord_root_num];

					let mut u = new_note;
					let mut d = new_note;

					for _i in 0..12 {
						if u%12 == target_note {
							break;
						}

						if u >= 127 {
							u -= 12;
						}
						u += 1;
					}
					for _i in 0..12 {
						if d%12 == target_note {
							break;
						}
						d -= 1;

						if d < 0 {
							d += 12
						}
					}

					if (new_note-u).abs() < (new_note-d).abs() {
						new_note = u;
					} else {
						new_note = d;
					}

					break;
				}
			}

			if !note_found {
				return -1;
			}
		} else { //Root transposition.
			let root_num = *chord_root as i8;
			if root_num > casm.high_key {
				new_note -= 12-root_num;
			} else {
				new_note += root_num;
			}
		}

		//NTT.
		if casm.ntt == NTT::NTTBypass as i8 {
			return Self::normalize_note(new_note, casm);
		}

		let norm_note = ((new_note-(chord_root_num as i8)) + 12)%12;
		let mut chord_note_found = false;

		let table = if casm.ntt == NTT::NTTChord as i8 {
			CHORD_TABLE_CHORD.as_slice()
		} else {
			CHORD_TABLE_MELODY.as_slice()
		};

		for c in table {
			if c.0 == norm_note {
				if c.1[chord_type_num] < 0 {
					return -1;
				}

				let target_note = c.1[chord_type_num]%12;
				let mut u = new_note;
				let mut d = new_note;

				for _i in 0..12 {
					if (u - (chord_root_num as i8))%12 == target_note {
						break;
					}

					if u >= 127 {
						u -= 12;
					}
					u += 1;
				}
				for _i in 0..12 {
					if (d - (chord_root_num as i8))%12 == target_note {
						break;
					}
					d -= 1;

					if d < 0 {
						d += 12
					}
				}

				if (new_note-u).abs() < (new_note-d).abs() {
					new_note = u;
				} else {
					new_note = d;
				}
				
				chord_note_found = true;
				break;
			}
		}

		if !chord_note_found {
			return -1;
		}

		new_note = Self::normalize_note(new_note, casm);

		return new_note;
	}

	///Ensure a note is within its allowed range.
	fn normalize_note(note: i8, casm: &CasmPart) -> i8 {
		let high_limit = casm.high_limit;
		let low_limit = casm.low_limit;

		let mut new_note = note;
		while new_note < low_limit {
			new_note += 12;
		}
		while new_note > high_limit {
			new_note -= 12;
		}

		return new_note;
	}
}

bind_java_type! {
	rust_type = pub JCasmPart,
	java_type = style.Casm::CasmPart,

	fields {
		pub name: JString,
		
		pub source_channel {
			sig = jbyte,
			name = "source_channel",
		},

		pub channel: jbyte,
		pub sourcechord: jbyte,

		pub sourcechord_type {
			sig = jbyte,
			name = "sourcechord_type",
		},

		pub ntt {
			sig = jbyte,
			name = "NTT",
		},

		pub high_key {
			sig = jbyte,
			name = "high_key",
		},

		pub low_limit {
			sig = jbyte,
			name = "low_limit",
		},

		pub high_limit {
			sig = jbyte,
			name = "high_limit",
		},

		pub rtr {
			sig = jbyte,
			name = "RTR",
		},

		pub editable: jboolean,
		pub noteplay: jboolean[],
		pub chordplay: jboolean[],

		pub ntr {
			sig = jboolean,
			name = "NTR",
		},

		pub autostart: jboolean,

		pub cseg {
			sig = jint,
			name = "CSEG",
		},
	},
}

bind_java_type! {
	rust_type = pub JCasmSect,
	java_type = style.Casm::CasmSect,

	fields {
		pub name: JString,

		pub cseg {
			sig = jint,
			name = "CSEG",
		}
	},
}

#[derive(Clone)]
pub struct CasmPart {
	source_channel: i8,
	channel: i8,
	source_chord: i8,
	source_chord_type: i8,
	ntt: i8,
	high_key: i8,
	low_limit: i8,
	high_limit: i8,
	rtr: i8,

	noteplay: [bool; 12],
	chordplay: [bool; 38],
	ntr: bool,
	autostart: bool,
	cseg: usize,
}

impl CasmPart {
	///Get a CASM part.
	fn get(jpart: JCasmPart, env: &mut Env<'_>) -> Self {
		let source_channel = jpart.source_channel(env).unwrap();
		let channel = jpart.channel(env).unwrap();
		let source_chord = jpart.sourcechord(env).unwrap();
		let source_chord_type = jpart.sourcechord_type(env).unwrap();
		let ntt = jpart.ntt(env).unwrap();
		let high_key = jpart.high_key(env).unwrap();
		let low_limit = jpart.low_limit(env).unwrap();
		let high_limit = jpart.high_limit(env).unwrap();
		let rtr = jpart.rtr(env).unwrap();

		let mut noteplay = [true; 12];
		let _ = jpart.noteplay(env).unwrap().get_region(env, 0, &mut noteplay);

		let mut chordplay = [true; 38];
		let _ = jpart.chordplay(env).unwrap().get_region(env, 0, &mut chordplay);

		let ntr = jpart.ntr(env).unwrap();
		let autostart = jpart.autostart(env).unwrap();

		let cseg = jpart.cseg(env).unwrap() as usize;

		return CasmPart {
			source_channel,
			channel,
			source_chord,
			source_chord_type,
			ntt,
			high_key,
			low_limit,
			high_limit,
			rtr,
			noteplay,
			chordplay,
			ntr,
			autostart,
			cseg
		};
	}
}

#[derive(Clone)]
pub struct CasmSect {
	section: usize,
	cseg: usize,
}

impl CasmSect {
	///Get a CASM section.
	fn get (jsect: JCasmSect, env: &mut Env<'_>, sections: &[String]) -> Self {
		let cseg = jsect.cseg(env).unwrap() as usize;
		let sect_name = jsect.name(env).unwrap().to_string();
		let mut section = 0;

		for i in 0..sections.len() {
			let s = &sections[i];
			if s.eq_ignore_ascii_case(&sect_name) {
				section = i;
				break;
			}
		}

		return CasmSect {
			section,
			cseg
		};
	}
}

#[derive(Clone)]
pub struct Casm {
	sections: Vec<CasmSect>,
	parts: Vec<CasmPart>,
}

impl Casm {
	fn get(style: &FwsStyle, j_style: JFwsStyle, env: &mut Env<'_>) -> Option<Self> {
		let mut sections = Vec::new();
		let mut parts = Vec::new();

		let jcasm = j_style.j_get_casm(env).unwrap();
		
		let jparts = env.get_field(&jcasm, jni_str!("parts"), jni_sig!(java.util.ArrayList)).unwrap().l().unwrap();
		let part_len = env.call_method(&jparts, jni_str!("size"), jni_sig!(() -> jint), &[]).unwrap().i().unwrap();

		let jsections = env.get_field(&jcasm, jni_str!("sections"), jni_sig!(java.util.ArrayList)).unwrap().l().unwrap();
		let section_len = env.call_method(&jsections, jni_str!("size"), jni_sig!(() -> jint), &[]).unwrap().i().unwrap();

		for i in 0..part_len {
			let jopart = env.call_method(&jparts, jni_str!("get"), jni_sig!((jint) -> JObject), &[Int(i)]).unwrap().l().unwrap();
			let jpart = env.cast_local::<JCasmPart>(jopart).unwrap();
			let part = CasmPart::get(jpart, env);
			parts.push(part);
		}

		for i in 0..section_len {
			let josect = env.call_method(&jsections, jni_str!("get"), jni_sig!((jint) -> JObject), &[Int(i)]).unwrap().l().unwrap();
			let jsect = env.cast_local::<JCasmSect>(josect).unwrap();
			let sect = CasmSect::get(jsect, env, &style.sections);
			sections.push(sect);
		}

		return Some(Casm {
			sections,
			parts,
		});
	}
}

#[derive(Clone, Copy)]
pub struct ChordBody {
	pub main_root: Note,
	pub main_chord: Chord,
	pub bass_root: Note,
	pub bass_chord: Chord,
}

impl ChordBody {
	pub fn new() -> Self {
		return ChordBody {
			main_root: Note::NoteNC,
			main_chord: Chord::ChordOff,
			bass_root: Note::NoteNC,
			bass_chord: Chord::ChordOff,
		}
	}
}

pub struct StyleManager {
	styles: Vec<FwsStyle>,
	style: usize,
	sections: Vec<Vec<String>>,
	section: usize,
	chromatic_section: usize,
	section_set: bool,

	drum_section: bool,

	chord_map: [Vec<(i8, i8)>; 16],
	channel_map: [i8; 16],
	parts_on: [bool; 8],
	retriggered_notes: Vec<(i8, u8)>,
	chord: ChordBody,

	looped_events: Vec<GenericMIDIEvent>,
	future_events: Vec<(GenericMIDIEvent, bool)>, //.1 is true if a chord shift should be performed.

	last_style_change_tick: i128,
	style_tpq: u64,
	song_tpq: u64,

	note_ptr: usize,
	style_len: u64,

	note_on: [[bool; 128]; 16],

	poly_aftertouch_setting: [[i8; 128]; 16],
	ctl_setting: [[i8; 128]; 16],
	voice_setting: [i8; 16],
	channel_aftertouch_setting: [i8; 16],
	pitch_bend_setting: [i16; 16],
}

impl StyleManager {
	///Get a new style manager.
	pub fn get(styles: &[FwsStyle], song_tpq: u64) -> Self {
		let mut style_manager = StyleManager {
			styles: Vec::new(),
			style: 0,
			sections: Vec::new(),
			section: 0,
			chromatic_section: 0,
			section_set: false,
			drum_section: false,

			chord_map: [const {Vec::new()}; 16],
			channel_map: [-1; 16],
			parts_on: [true; 8],
			chord: ChordBody{main_root: Note::NoteNC, main_chord: Chord::ChordOff, bass_root: Note::NoteNC, bass_chord: Chord::ChordOff},

			looped_events: Vec::new(),
			future_events: Vec::new(),
			retriggered_notes: Vec::new(),

			last_style_change_tick: 0,
			style_tpq: 192,
			song_tpq,

			note_ptr: 0,
			style_len: 0,

			note_on: [[false; 128]; 16],

			poly_aftertouch_setting: [[-1; 128]; 16],
			ctl_setting: [[-1; 128]; 16],
			voice_setting: [-1; 16],
			channel_aftertouch_setting: [-1; 16],
			pitch_bend_setting: [-1; 16],
		};

		for style in styles {
			style_manager.styles.push(style.clone());

			let mut sect_list = Vec::new();
			for sect in &style.sections {
				sect_list.push(sect.clone());
			}

			style_manager.sections.push(sect_list);
		}

		if style_manager.styles.len() > 0 && style_manager.styles[0].sections.len() > 0 {
			style_manager.looped_events = style_manager.styles[0].event_sections[0].clone();
			style_manager.channel_map = style_manager.styles[0].get_channel_map(0);
			style_manager.chord_map = style_manager.styles[0].get_chord_map(0, &ChordBody::new());

			style_manager.style_tpq = styles[0].tpq;
		}

		return style_manager;
	}

	///Get all style events at the current tick plus the next song tick at which a style change occurs.
	pub fn play(&mut self, song_tick: u64) -> (Vec<Vec<u8>>, u64) {
		let mut note_bytes = Vec::new();

		let style_tick = self.get_style_tick(song_tick);

		let mut new_tick_vec = Vec::new();
		let mut new_tick = style_tick+self.style_tpq/16;

		if self.note_ptr < self.looped_events.len() {
			let mut p = self.note_ptr;
			for ev in self.note_ptr..self.looped_events.len() {
				let effective_tick = self.looped_events[ev].tick*self.song_tpq/self.style_tpq*self.style_tpq/self.song_tpq;
				if effective_tick < style_tick {
					continue;
				} else if effective_tick == style_tick {
					note_bytes.push(self.looped_events[ev].data.clone());
					p += 1;
				} else if effective_tick > style_tick {
					let mut tick_exists = false;
					for t in &new_tick_vec {
						if *t == effective_tick {
							tick_exists = true;
							break;
						}
					}

					if !tick_exists {
						new_tick_vec.push(effective_tick);
					}
					break;
				}
			}

			if p > self.note_ptr {
				self.note_ptr = p;
			}
		}

		let mut return_bytes = Vec::new();

		let mut future_clear = true;
		if self.future_events.len() > 0 {
			for i in 0..self.future_events.len() {
				let ev = &self.future_events[i];
				let effective_tick = self.future_events[i].0.tick*self.song_tpq/self.style_tpq*self.style_tpq/self.song_tpq;
				if effective_tick == style_tick {
					if ev.1 {
						note_bytes.push(ev.0.data.clone());
					} else {
						return_bytes.push(ev.0.data.clone());
					}
				} else if effective_tick > style_tick {
					new_tick_vec.push(effective_tick);
					future_clear = false;
				}
			}
		}
		if future_clear {
			self.future_events.clear();
		}
		
		if new_tick_vec.len() > 0 {
			new_tick = new_tick_vec[0];
			for t in new_tick_vec {
				if t < new_tick {
					new_tick = t;
				}
			}
		}

		if self.note_ptr >= self.looped_events.len() && new_tick < self.style_len {
			new_tick = self.style_len;
		}

		let old_tick = style_tick;
		if new_tick - old_tick > self.style_tpq/16 {
			new_tick = old_tick + self.style_tpq/16;
		}

		if new_tick >= self.style_len {
			for ev in &self.looped_events {
				if ev.tick < self.style_len {
					continue;
				}

				note_bytes.push(ev.data.clone());
			}
		}

		let mut voice_changed = [false; 16];

		//Check for voice changes.
		for ve in &note_bytes {
			if ve.len() < 2 {
				continue;
			}

			let new_channel = if self.channel_map[(ve[0]&0xF) as usize] >= 0 {
				self.channel_map[(ve[0]&0xF) as usize] as usize
			} else {
				continue;
			};

			if (ve[0]&0xF0) == 0xC0 {
				if ve[1] as i8 != self.voice_setting[new_channel] {
					voice_changed[new_channel] = true;
				}
			} else if (ve[0]&0xF0) == 0xB0 && (ve[1] == 0x0 || ve[1] == 0x20) && ve.len() >= 3 {
				if ve[2] as i8 != self.ctl_setting[new_channel][ve[1] as usize] {
					voice_changed[new_channel] = true;
				}
			}
		}

		for mut ve in note_bytes {
			if ve.len() < 2 {
				continue;
			}

			if (ve[0]&0xF0) == 0xF0 {
				continue;
			}

			let org_channel = ve[0]&0xF;

			{
				let channel = (ve[0]&0xF) as usize;

				if ve[0]&0xE0 == 0x80 {
					let note = ve[1] as i8;

					let mut note_found = false;

					for n in &self.chord_map[channel] {
						if n.0 == note {
							note_found = true;
							ve[1] = n.1 as u8;
							break;
						}
					}

					if !note_found {
						continue;
					}
				}

				if self.channel_map[channel] < 0 {
					continue;
				}

				ve[0] &= 0xF0;
				ve[0] |= (self.channel_map[channel] as u8)&0xF;
			}

			if ve.len() >= 3 && (ve[0]&0xF0) == 0x90 && ve[2] > 0 { //Note on event.
				let channel = (ve[0]&0xF) as usize;
				let note = ve[1] as usize;

				let mut retriggered = false;
				for n in &self.retriggered_notes {
					if n.0 == note as i8 && n.1 == org_channel {
						retriggered = true;
						break;
					}
				}

				if retriggered { //Don't play.
					continue;
				}

				if self.note_on[channel][note] {
					continue;
				}

				let part_channel = channel as i8 - 8;
				if part_channel >= 0 && part_channel < self.parts_on.len() as i8 && !self.parts_on[part_channel as usize] {
					continue;
				}

				self.note_on[channel][note] = true;
			} else if ve.len() >=3 && ((ve[0]&0xF0) == 0x80 || ((ve[0]&0xF0) == 0x90 && ve[2] == 0)) { //Note off event.
				let channel = (ve[0]&0xF) as usize;
				let note = ve[1] as usize;

				self.note_on[channel][note] = false;
			} else if ve.len() > 0 && (ve[0]&0xE0) != 0x80 {
				if !self.set_short_settings(ve.clone()) {
					let command = ve[0]&0xF0;
					let channel = (ve[0]&0xF) as usize;

					let mut voice_event = false;

					if command == 0xC0 {
						voice_event = true;
					} else if command == 0xB0 {
						if ve.len() >= 2 && (ve[1] == 0x0 || ve[1] == 0x20) {
							voice_event = true;
						}
					}

					if !(voice_event && voice_changed[channel]) {
						continue;
					}
				}
			}

			return_bytes.push(ve);
		}

		if new_tick >= self.style_len {
			self.note_ptr = 0;
			new_tick = self.style_len;
		}

		self.retriggered_notes.clear();

		if (new_tick-old_tick)*self.song_tpq/self.style_tpq + song_tick <= song_tick {
			return (return_bytes, song_tick + 1);
		}

		return (return_bytes, (new_tick-old_tick)*self.song_tpq/self.style_tpq + song_tick);
	}

	///Set the style short-message settings to keep track of. Return whether the short was set, if false, the short was already set to the expected value.
	fn set_short_settings(&mut self, msg: Vec<u8>) -> bool {
		if msg.len() <= 0 {
			return true;
		}

		let channel = (msg[0]&0xF) as usize;

		match msg[0]&0xF0 {
			0xA0 => { //Polyphonic aftertouch.
				if self.poly_aftertouch_setting[channel][msg[1] as usize] == msg[2] as i8 {
					return false;
				}
				
				if msg.len() >= 3 {
					self.poly_aftertouch_setting[channel][msg[1] as usize] = msg[2] as i8;
				}
			}
			0xB0 => { //Control.
				if self.ctl_setting[channel][msg[1] as usize] == msg[2] as i8 {
					return false;
				}

				if msg.len() >= 3 {
					self.ctl_setting[channel][msg[1] as usize] = msg[2] as i8;
				}
			}
			0xC0 => { //Voice.
				if self.voice_setting[channel] == msg[1] as i8 {
					return false;
				}
				
				if msg.len() >= 2 {
					self.voice_setting[channel] = msg[1] as i8;
				}
			}
			0xD0 => { //Channel aftertouch.
				if self.channel_aftertouch_setting[channel] == msg[1] as i8 {
					return false;
				}

				if msg.len() >= 2 {
					self.channel_aftertouch_setting[channel] = msg[1] as i8;
				}
			}
			0xE0 => { //Pitch bend.
				if self.pitch_bend_setting[channel] == (msg[1] as i16) | ((msg[2]<<7) as i16) {
					return false;
				}
				
				if msg.len() >= 3 {
					self.pitch_bend_setting[channel] = (msg[1] as i16) | ((msg[2]<<7) as i16);
				}
			}
			_ => {}
		}

		return true;
	}

	///Set the current chord. Return any events that need to be sent.
	pub fn set_chord(&mut self, song_tick: u64, chord: ChordBody) -> Vec<Vec<u8>> {
		let style = &self.styles[self.style];

		let style_tick;
		if self.style_len > 0 {
			style_tick = self.get_style_tick(song_tick);
		} else {
			return Vec::new();
		}

		let last_chord = self.chord;
		self.chord = chord.clone();
		
		let chord_changed = last_chord.main_root != self.chord.main_root ||
							last_chord.main_chord != self.chord.main_chord ||
							last_chord.bass_root != self.chord.bass_root ||
							last_chord.bass_chord != self.chord.bass_chord;

		if !self.drum_section {
			self.chord_map = style.get_chord_map(self.section, &chord);
			self.channel_map = style.get_channel_map(self.section);
		} else {
			self.chord_map = style.get_chord_map(self.chromatic_section, &chord);
			let new_map = style.get_chord_map(self.section, &chord);

			for c in 0..new_map.len() {
				let mut drum_channel = false;
				for i in &style.drum_channels[self.section] {
					if *i == c as i8 {
						drum_channel = true;
						break;
					}
				}

				if !drum_channel {
					continue;
				}

				self.chord_map[c].clear();
				for ev in &new_map[c] {
					self.chord_map[c].push(*ev);
				}
			}
		}

		let mut new_notes_on = [[0; 128]; 16];

		let rtr_map = if !self.drum_section {
			style.get_rtr(self.section)
		} else {
			style.get_rtr(self.chromatic_section)
		};

		let ntt_map = if !self.drum_section {
			style.get_ntt(self.section)
		} else {
			style.get_ntt(self.chromatic_section)
		};

		let notes_on = GenericMIDIEvent::get_notes_on_at(&self.looped_events, style_tick);
	
		for n in notes_on {
			if n.data.len() < 3 {
				continue;
			}

			if (n.data[0]&0xF0) != 0x90 {
				continue;
			}

			let channel = n.data[0]&0xF;
			let c = channel as usize;

			if n.tick == style_tick && ((rtr_map[c] != RTR::RTRPitchShiftRoot && rtr_map[c] != RTR::RTRRetriggerRoot) || n.data[1]%12 == 0) {
				continue;
			}

			let note = n.data[1];
			let velocity = n.data[2];

			let mut new_note = -1;
			for m in &self.chord_map[c] {
				if m.0 == note as i8 {
					new_note = m.1;
					break;
				}
			}

			let rtr = rtr_map[c];

			if new_note >= 0 {
				let ac = self.channel_map[c];
				if ac < 0 {
					continue;
				}

				if ac-8 < 8 && ac-8 >= 0 && !self.parts_on[(ac-8) as usize] {
					continue;
				}

				/*let mut drum_channel = false;
				for i in &style.drum_channels[self.section] {
					if *i == c as i8 {
						drum_channel = true;
						break;
					}
				}
				if drum_channel {
					continue;
				}*/

				if rtr == RTR::RTRPitchShiftRoot || rtr == RTR::RTRRetriggerRoot {
					let root = if ntt_map[c] == NTT::NTTBass && self.chord.bass_root as i8 >= 0 {
						self.chord.bass_root
					} else {
						self.chord.main_root
					};

					let last_root = if ntt_map[c] == NTT::NTTBass && self.chord.bass_root as i8 >= 0 {
						last_chord.bass_root
					} else {
						last_chord.main_root
					};

					if root != last_root {
						let mut trans = new_note;

						let mut u = trans;
						let mut d = trans;

						for _i in 0..12 {
							if u%12 == root as i8 {
								break;
							}

							if u >= 127 {
								u -= 12;
							}
							u += 1;
						}
						for _i in 0..12 {
							if d%12 == root as i8 {
								break;
							}
							d -= 1;

							if d < 0 {
								d += 12
							}
						}

						if (trans-u).abs() < (trans-d).abs() {
							trans = u;
						} else {
							trans = d;
						}

						let orig_note = new_note;
						new_note = trans;
						let off_tick = self.get_off_time(channel, note as i8, style_tick);
						let new_channel = self.channel_map[channel as usize];

						if new_channel < 0 {
							continue;
						}

						self.retriggered_notes.push((orig_note, channel));

						if new_note != orig_note {
							self.future_events.push((GenericMIDIEvent {
								data: [0x80 | (new_channel as u8), new_note as u8, 0].to_vec(),
								tick: off_tick,
							}, false));
						}
					}
				}
				new_notes_on[ac as usize][new_note as usize] = velocity;
			}
		}

		let mut return_events = Vec::new();

		for c in 0..new_notes_on.len() {
			let rtr = rtr_map[c];
			for n in 0..new_notes_on[c].len() {
				if self.note_on[c][n] && new_notes_on[c][n] == 0 { //Turn note off.
					if rtr == RTR::RTRPitchShift || rtr == RTR::RTRPitchShiftRoot {
						return_events.push([0xB0 | (c as u8), 84, n as u8].to_vec());
						self.ctl_setting[c][84] = n as i8;
					}

					self.note_on[c][n] = false;
					return_events.push([0x80 | (c as u8), n as u8, 0].to_vec());
				} else if !self.note_on[c][n] && new_notes_on[c][n] > 0 { //Turn note on.
					if rtr != RTR::RTRStop {
						if rtr == RTR::RTRPitchShift || rtr == RTR::RTRPitchShiftRoot {
							/*let attack_time = if self.ctl_setting[c][0x49] >= 0 { self.ctl_setting[c][0x49] as u8} else {64};
							return_events.push([0xB0 | (c as u8), 0x49, if attack_time + 6 < 128 { attack_time + 6 } else {127}].to_vec());

							self.future_events.push((GenericMIDIEvent {
								data: [0xB0 | (c as u8), 0x49, attack_time].to_vec(),
								tick: song_tick + 2,
							}, true));*/
						}

						self.note_on[c][n] = true;
						return_events.push([0x90 | (c as u8), n as u8, new_notes_on[c][n]].to_vec());
					}
				} else if self.note_on[c][n] && new_notes_on[c][n] > 0 { //Retrigger?
					if rtr == RTR::RTRStop && chord_changed {
						self.note_on[c][n] = false;
						return_events.push([0x80 | (c as u8), n as u8, 0].to_vec());
					} else if (rtr == RTR::RTRRetrigger || rtr == RTR::RTRRetriggerRoot) && chord_changed {
						return_events.push([0x80 | (c as u8), n as u8, 0].to_vec());
						return_events.push([0x90 | (c as u8), n as u8, new_notes_on[c][n]].to_vec());
					} else {
						continue;
					}
				}
			}
		}

		return return_events;
	}

	///Set the style section.
	pub fn set_section(&mut self, song_tick: u64, section_name: &str, section_tick: u64, retain: bool, parts: [bool; 8]) -> Vec<Vec<u8>> {
		let chord = self.chord.clone();
		return self.set_section_full(song_tick, &chord, section_name, section_tick, retain, parts);
	}

	///Set the style section and chord.
	pub fn set_section_full(&mut self, song_tick: u64, new_chord: &ChordBody, section_name: &str, section_tick: u64, retain: bool, parts: [bool; 8]) -> Vec<Vec<u8>> {
		let last_parts = self.parts_on.clone();
		self.parts_on = parts.clone();

		let style = &self.styles[self.style];
		let last_section = self.section;
		let last_len = self.style_len;

		let last_chord = self.chord;
		let mut refresh_chord = false;

		let mut return_events = Vec::new();

		match style.get_section_number(section_name) {
			Some(section) => {
				self.section = section;
				self.style_len = style.length_sections[section];
			}
			None => {
				println!("Section {} not found...", section_name);
				return Vec::new();
			}
		}

		let prev_style_change_tick = self.last_style_change_tick;
		if !retain {
			self.last_style_change_tick = song_tick as i128 - (section_tick*self.song_tpq/self.style_tpq) as i128;
		}
		self.note_ptr = 0;
		self.chord = *new_chord;

		self.drum_section = style.drum_sections[self.section];

		if (self.section != last_section || !self.section_set) ||
			(last_chord.main_root != new_chord.main_root ||
			last_chord.main_chord != new_chord.main_chord ||
			last_chord.bass_chord != new_chord.bass_chord ||
			last_chord.bass_root != new_chord.bass_root) {
				refresh_chord = true;
		}

		let last_section_set = self.section_set;
		if self.section != last_section || !self.section_set || self.drum_section {
			self.section_set = true;

			if !style.drum_sections[self.section] { //Replace all events.
				self.chord_map = style.get_chord_map(self.section, new_chord);
				self.channel_map = style.get_channel_map(self.section);

				self.looped_events = style.event_sections[self.section].clone();

				let shorts = GenericMIDIEvent::separate_shorts(&self.looped_events, &[0xC0, 0xB0, 0xD0, 0xE0, 0xA0], false);

				let mut poly_aftertouch_set = [[false; 128]; 16];
				let mut control_set = [[false; 128]; 16];
				let mut voice_set = [false; 16];
				let mut channel_aftertouch_set = [false; 16];
				let mut pitch_bend_set = [false; 16];

				let mut voice_changed = [false; 16];

				for ev in &shorts {
					if ev.data.len() < 1 {
						continue;
					}

					let c = (ev.data[0]&0xF) as usize;
					if self.channel_map[c] < 0 {
						continue;
					}

					if ev.data.len() >= 3 && (ev.data[0]&0xF0) == 0xA0 { //Polyphonic aftertouch.
						let note = ev.data[1] as usize;
						poly_aftertouch_set[c][note] = true;
					} else if ev.data.len() >= 3 && (ev.data[0]&0xF0) == 0xB0 { //Control change.
						let ctl = ev.data[1] as usize;
						control_set[c][ctl] = true;

						if (ctl == 0 || ctl == 0x20) && self.ctl_setting[c][ctl] != ev.data[2] as i8 {
							voice_changed[c] = true;
						}
					} else if ev.data.len() >= 2 && (ev.data[0]&0xF0) == 0xC0 { //Voice change.
						voice_set[c] = true;

						if ev.data[1] as i8 != self.voice_setting[c] {
							voice_changed[c] = true;
						}
					} else if ev.data.len() >= 2 && (ev.data[0]&0xF0) == 0xD0 { //Channel aftertouch.
						channel_aftertouch_set[c] = true;
					} else if ev.data.len() >= 3 && (ev.data[0]&0xF0) == 0xE0 { //Pitch bend.
						pitch_bend_set[c] = true;
					}
				}

				for mut ev in shorts {
					if ev.data.len() < 2 {
						continue;
					}

					if ev.tick == 0 {
						let c = (ev.data[0]&0xF) as usize;
						if self.channel_map[c] < 0 {
							continue;
						}

						ev.data[0] = (ev.data[0]&0xF0) | (self.channel_map[c] as u8);

						if ev.data.len() >= 3 && (ev.data[0]&0xF0) == 0xA0 { //Polyphonic aftertouch.
							let value = ev.data[2] as i8;
							let note = ev.data[1] as usize;
							if self.poly_aftertouch_setting[self.channel_map[c] as usize][note] == value {
								continue;
							}

							if self.channel_map[c] as usize != c && poly_aftertouch_set[self.channel_map[c] as usize][note] {
								continue;
							}

							self.poly_aftertouch_setting[self.channel_map[c] as usize][note] = value;
						} else if ev.data.len() >= 3 && (ev.data[0]&0xF0) == 0xB0 { //Control change.
							let value = ev.data[2] as i8;
							let ctl = ev.data[1] as usize;
							if self.ctl_setting[self.channel_map[c] as usize][ctl] == value {
								if !((ctl == 0x0 || ctl == 0x20) && voice_changed[self.channel_map[c] as usize]) {
									continue;
								}
							}

							if self.channel_map[c] as usize != c && control_set[self.channel_map[c] as usize][ctl] {
								continue;
							}

							self.ctl_setting[self.channel_map[c] as usize][ctl] = value;
						} else if ev.data.len() >= 2 && (ev.data[0]&0xF0) == 0xC0 { //Voice change.
							let voice = ev.data[1];
							if self.voice_setting[self.channel_map[c] as usize] == voice as i8 && !voice_changed[self.channel_map[c] as usize] {
								continue;
							}

							if self.channel_map[c] as usize != c && voice_set[self.channel_map[c] as usize] {
								continue;
							}

							self.voice_setting[self.channel_map[c] as usize] = voice as i8;
						} else if ev.data.len() >= 2 && (ev.data[0]&0xF0) == 0xD0 { //Channel aftertouch.
							let at = ev.data[1] as i8;
							if self.channel_aftertouch_setting[self.channel_map[c] as usize] == at {
								continue;
							}

							if self.channel_map[c] as usize != c && channel_aftertouch_set[self.channel_map[c] as usize] {
								continue;
							}

							self.channel_aftertouch_setting[self.channel_map[c] as usize] = at;
						} else if ev.data.len() >= 3 && (ev.data[0]&0xF0) == 0xE0 { //Pitch bend.
							let pb = (ev.data[1] | (ev.data[2] << 7)) as i16;
							if self.pitch_bend_setting[self.channel_map[c] as usize] == pb {
								continue;
							}

							if self.channel_map[c] as usize != c && pitch_bend_set[self.channel_map[c] as usize] {
								continue;
							}

							self.pitch_bend_setting[self.channel_map[c] as usize] = pb;
						}

						return_events.push(ev.data);
					}
				}

				for c in 0..control_set.len() {
					let set = control_set[c];
					if set[100] || set[101] {
						return_events.push([0xB0 | (c as u8), 101, 127].to_vec());
						return_events.push([0xB0 | (c as u8), 100, 127].to_vec());
					}

					if set[98] || set[99] {
						return_events.push([0xB0 | (c as u8), 99, 127].to_vec());
						return_events.push([0xB0 | (c as u8), 98, 127].to_vec());
					}
				}

				for c in 0..self.note_on.len() {
					for n in 0..self.note_on[c].len() {
						if self.note_on[c][n] {
							return_events.push([0x80 | (c as u8), n as u8, 0].to_vec());
							self.note_on[c][n] = false;
						}
					}
				}
			} else { //Only replace drum events.
				if !retain {
					let new_section_measure = (song_tick as i128 - prev_style_change_tick)/((self.style_len*self.song_tpq/self.style_tpq) as i128)*((self.style_len*self.song_tpq/self.style_tpq) as i128);
					
					self.last_style_change_tick -= new_section_measure;
					while self.last_style_change_tick < 0 {
						self.last_style_change_tick += (self.style_len*self.song_tpq/self.style_tpq) as i128;
					}
				}

				if last_len > 0 {
					self.style_len = last_len;
				}

				if !style.drum_sections[last_section] {
					self.chromatic_section = last_section;
				}
				
				let mut i = 0;
				while i < self.looped_events.len() {
					let ev = &self.looped_events[i];
					
					if ev.data.len() <= 0 || (ev.data[0]&0xF0) == 0xF0 {
						i += 1;
						continue;
					}

					let channel = ev.data[0]&0xF;
					let mut drum_channel = false;
					for dc in &style.drum_channels[self.section] {
						if channel as i8 == *dc {
							drum_channel = true;
							break;
						}
					}

					if drum_channel {
						self.looped_events.remove(i);
						continue;
					}

					i += 1;
				}

				for c in 0..self.note_on.len() {
					for n in 0..self.note_on[c].len() {
						let mut drum_found = false;
						for dc in &style.drum_channels[self.section] {
							if *dc == c as i8 {
								drum_found = true;
								break;
							}
						}

						if !drum_found {
							continue;
						}

						if self.note_on[c][n] {
							return_events.push([0x80 | (c as u8), n as u8, 0].to_vec());
							self.note_on[c][n] = false;
						}
					}
				}

				let drum_events = &style.event_sections[self.section];
				let drum_section_len = style.length_sections[self.section];

				let section_ratio = if self.style_len > drum_section_len {
					self.style_len/drum_section_len
				} else {
					1
				};

				for ev in drum_events {
					for i in 0..section_ratio {
						let new_ev = GenericMIDIEvent {
							data: ev.data.clone(),
							tick: ev.tick + drum_section_len*i,
						};

						let mut index_found = false;
						for j in 0..self.looped_events.len() {
							let le = &self.looped_events[j];
							if le.tick > new_ev.tick {
								self.looped_events.insert(j, new_ev.clone());
								index_found = true;
								break;
							}
						}

						if !index_found {
							self.looped_events.push(new_ev.clone());
						}
					}
				}
			}
		} else if last_parts != self.parts_on && !refresh_chord { //Parts changed.
			refresh_chord = true;
		}

		if refresh_chord {
			if self.section != last_section || !last_section_set || style.drum_sections[self.section] {
				self.chord = last_chord;
			}

			let new_notes = self.set_chord(song_tick, *new_chord);
			for n in new_notes {
				return_events.push(n);
			}
		}

		return return_events;
	}

	///Get the current style.
	pub fn get_style(&self) -> &FwsStyle {
		return &self.styles[self.style];
	}

	///Get the current section number.
	pub fn get_section_number(&self) -> usize {
		return self.section;
	}

	///Set the style.
	pub fn set_style(&mut self, song_tick: u64, style_name: &str, section_name: &str, section_tick: u64, parts: [bool; 8]) -> Vec<Vec<u8>> {
		let chord = self.chord.clone();
		return self.set_style_full(song_tick, &chord, style_name, section_name, section_tick, parts);
	}

	///Set the style.
	pub fn set_style_full(&mut self, song_tick: u64, new_chord: &ChordBody, style_name: &str, section_name: &str, section_tick: u64, parts: [bool; 8]) -> Vec<Vec<u8>> {
		if style_name.is_empty() {
			let mut note_off = Vec::new();
			
			for c in 0..self.note_on.len() {
				for n in 0..self.note_on[c].len() {
					if self.note_on[c][n] {
						self.note_on[c][n] = false;
						note_off.push([0x80 | (c as u8), n as u8, 0].to_vec());
					}
				}
			}

			return note_off;
		}
		
		let mut found_style = false;
		for i in 0..self.styles.len() {
			let s = &self.styles[i];
			if s.long_name.eq_ignore_ascii_case(style_name) {
				found_style = true;
				self.style = i;
				break;
			}
		}

		if found_style {
			self.section_set = false;
			return self.set_section_full(song_tick, new_chord, section_name, section_tick, false, parts);
		} else {
			return Vec::new();
		}
	}

	///Get the effective style tick.
	fn get_style_tick(&self, song_tick: u64) -> u64 {
		if song_tick as i128 - self.last_style_change_tick >= 0 {
			return (((song_tick as i128 - self.last_style_change_tick) as u64)*self.style_tpq/self.song_tpq)%self.style_len;
		} else {
			let mut difference = song_tick as i128 - self.last_style_change_tick;
			while difference < 0 {
				difference += self.style_len as i128;
			}

			return ((difference as u64)*self.style_tpq/self.song_tpq)%self.style_len;
		}
	}

	///Get the off time for a note.
	fn get_off_time(&self, channel: u8, note: i8, tick: u64) -> u64 {
		for n in &self.looped_events {
			if n.data.len() > 0 && ((n.data[0]&0xF) != channel || (n.data[0]&0xE0) != 0x80) {
				continue;
			}

			if n.tick <= tick {
				continue;
			}

			if n.data.len() >= 3 && n.data[1] as i8 == note {
				return n.tick;
			}
		}

		return tick;
	}
}
