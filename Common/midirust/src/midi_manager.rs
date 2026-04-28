use std::sync::{Arc, Mutex};
use std::thread;
use std::time::Duration;

use jni::objects::{JByteArray, JObject, JObjectArray};
use jni::{Env, bind_java_type, errors, jni_sig, jni_str};
use jni::JValue::{Byte,Bool,Int};
use midir::{MidiInput, MidiOutput};

use crate::fws_event::GenericMIDIEvent;
use crate::fws_song::JFWSSong;
use crate::fws_style::{Chord, ChordBody, FwsStyle, JFwsStyle, Note, StyleManager};
use crate::midi_player_options::JExportOptions;
use crate::{fws_sequence::JFwsSequence, midi_player_options::JMidiPlayerOptions};

extern crate jni;

#[allow(unused_attributes)]
#[allow(dead_code)]
const NOTE_NAMES: [&'static str; 12] = ["C", "C♯", "D", "E♭", "E", "F", "F♯", "G", "A♭", "A", "B♭", "B"];

bind_java_type! {
	rust_type = pub JMidiManager,
	java_type = controllers.MIDIManager,

	methods {
		pub fn get_input_id {
			sig = () -> JString,
			name = "getInputID",
		},

		pub fn get_output_id {
			sig = () -> JString,
			name = "getOutputID",
		},

		priv fn load_recorded_data(jbyte[][], jlong[]),
	},

	fields {
		priv player_options {
			sig = options.MIDIPlayerOptions,
			name = "player_options",
		}, 
	},
}

impl<'local> JMidiManager<'local> {
	///Play and loop a style.
	pub fn play_style(self, env: &mut Env<'_>, j_style: JFwsStyle) {
		let j_options = self.player_options(env).unwrap();
		let g_options = env.new_global_ref(&j_options).unwrap();
		let options = env.cast_local::<JMidiPlayerOptions>(j_options).unwrap();
		
		let glob_arc = Arc::clone(&Arc::new(Mutex::new(g_options)));

		let _ = options.set_play(env, true);

		let id_name = match self.get_output_id(env) {
			Ok(id_name) => id_name.to_string(),
			Err(_) => {
				return;
			}
		};

		let input_id = match self.get_input_id(env) {
			Ok(id_name) => Some(id_name.to_string()),
			Err(_) => None,
		};

		let midi_out = MidiOutput::new("FWS").unwrap();
		let output_port = match midi_out.find_port_by_id(id_name) {
			Some(port) => port,
			None => {
				return;
			}
		};

		let mut output_connection = match midi_out.connect(&output_port, &output_port.id()) {
			Ok(connection) => connection,
			Err(e) => {
				println!("{}", e);
				return;
			}
		};


		let style = FwsStyle::get(env, j_style);
		let tpq = style.tpq;
		let style_name = style.long_name.clone();

		let mut style_manager = StyleManager::get(&[style], tpq);
		let mut section = "".to_string();

		let chord = Arc::new(Mutex::new(ChordBody::new()));
		let j_chord = Arc::clone(&chord);

		match glob_arc.lock() {
			Ok(g_options) => {
				let jt_options = unsafe { JObject::from_raw(env, g_options.to_owned()) };
				let t_options = env.cast_local::<JMidiPlayerOptions>(jt_options).unwrap();

				chord.lock().unwrap().main_root = Note::from_i8(-1);
				chord.lock().unwrap().main_chord = Chord::from_i8(-1);

				let _ = t_options.set_active_chord_root(env, -1);
				let _ = t_options.set_active_chord_type(env, -1);

				section = t_options.style_section(env).unwrap().to_string();
			}
			Err(_) => {

			}
		}

		let mut section_changed = true;
		let mut style_init = true;
		let chord_changed = Arc::new(Mutex::new(true));
		let chord_changed_clone = Arc::clone(&chord_changed);

		let jvm = env.get_java_vm().unwrap();
		thread::spawn( move || {
			let mut time_events = Vec::new();

			let mut time_ptr = 0;

			let mut current_tick = 0;
			let mut current_upt = 5000/tpq;

			let mut len = 0;

			let mut play = true;

			let mut chord_keys_down = Vec::new();

			let mut note_on = [[false; 128];16];

			let mut start_events = style_manager.set_style_full(0, &j_chord.lock().unwrap(), &style_name, &section, 0, [true; 8]);

			let midi_in = MidiInput::new("FWS").unwrap();
			let input_port = match input_id {
				Some(id_name) => {
					match midi_in.find_port_by_id(id_name.clone()) {
						Some(port) => {
							Some(port)
						}
						None => None,
					}
				}
				None => None,
			};

			let set_root = Arc::new(Mutex::new(Note::NoteNC));
			let set_chord = Arc::new(Mutex::new(Chord::ChordOff));

			let j_set_root = Arc::clone(&set_root);
			let j_set_chord = Arc::clone(&set_chord);

			let input_connection = match input_port {
				Some(input_port) => {
					let input_connection = midi_in.connect(&input_port, "FWS", move |_stamp, in_msg, _| {
						if in_msg.len() >= 3 && (in_msg[0]&0xF0) == 0x90 && in_msg[2] > 0 { //Note! Valid?
							let note = in_msg[1] as i8;
							if note <= 53 { //Chord split point.
								let mut note_index = -1;
								for i in 0..chord_keys_down.len() {
									if chord_keys_down[i] > note {
										note_index = i as isize;
										break;
									}
								}

								if note_index >= 0 {
									chord_keys_down.insert(note_index as usize, note);
								} else {
									chord_keys_down.push(note);
								}
							}

							let mut set_root = j_set_root.lock().unwrap();
							let mut set_chord = j_set_chord.lock().unwrap();

							(*set_root, *set_chord) = FwsStyle::get_chord(&chord_keys_down);
							if *set_root as i8 != chord.lock().unwrap().main_root as i8 || *set_chord as i8 != chord.lock().unwrap().main_chord as i8 {
								*chord_changed_clone.lock().unwrap() = true;
							}
						} else if in_msg.len() >= 3 && ((in_msg[0]&0xE0) == 0x80 || in_msg[2] == 0) {
							let note = in_msg[1] as i8;
							if note <= 53 { //Chord split point.
								for i in 0..chord_keys_down.len() {
									if chord_keys_down[i] == note {
										chord_keys_down.remove(i);
										break;
									}
								}
							}
						}
						
						thread::sleep(Duration::from_micros(current_upt*tpq/32));
					}, (), ).unwrap();
					Some(input_connection)
				}
				None => {None}
			};

			while play {
				let _ = jvm.attach_current_thread(|env| -> errors::Result<()> {
					match glob_arc.try_lock() {
						Ok(g_options) => {
							let jt_options = unsafe { JObject::from_raw(env, g_options.to_owned()) };
							let t_options = env.cast_local::<JMidiPlayerOptions>(jt_options).unwrap();

							play = t_options.play(env).unwrap();
							let _ = t_options.set_current_tick(env, current_tick as i64);

							if *chord_changed.lock().unwrap() {
								let set_root = *set_root.lock().unwrap();
								let set_chord = *set_chord.lock().unwrap();
								j_chord.lock().unwrap().main_root = set_root;
								j_chord.lock().unwrap().main_chord = set_chord;

								let root = match set_root as i8 {
									0 => 0x31,
									1 => 0x41,
									2 => 0x32,
									3 => 0x23,
									4 => 0x33,
									5 => 0x34,
									6 => 0x44,
									7 => 0x35,
									8 => 0x26,
									9 => 0x36,
									10 => 0x27,
									11 => 0x37,
									_ => 0,
								};

								let info_display = t_options.info_display(env).unwrap();
								let _ = env.call_method(info_display, jni_str!("refreshStyleChord"), jni_sig!((jbyte, jbyte)), &[Byte(root), Byte(set_chord as i8)]);
							}

							let sel_section_name = t_options.style_section(env).unwrap().to_string();

							if !sel_section_name.eq(&section) {
								section = sel_section_name;
								section_changed = true;
							}
						}
						Err(_) => {
						}
					}
					Ok(())
				});

				if !play {
					break;
				}

				let mut note_bytes = Vec::new();

				if start_events.len() > 0 {
					for ev in &start_events {
						note_bytes.push(ev.clone());
					}
					start_events.clear();
				}

				if section_changed {
					let start_bytes = if style_init {
						style_init = false;
						style_manager.set_style_full(current_tick, &j_chord.lock().unwrap(), &style_name, &section, 0, [true; 8])
					} else {
						style_manager.set_section(current_tick, &section, 0, false, [true; 8])
					};
					
					
					for ev in start_bytes {
						note_bytes.push(ev);
					}

					time_events = style_manager.get_style().time_sections[style_manager.get_section_number()].clone();
					len = style_manager.get_style().length_sections[style_manager.get_section_number()];

					current_tick = 0;

					time_ptr = 0;

					section_changed = false;
				}

				if *chord_changed.lock().unwrap() {
					let start_bytes = style_manager.set_chord(current_tick, *j_chord.lock().unwrap());
					*chord_changed.lock().unwrap() = false;

					for ev in start_bytes {
						note_bytes.push(ev);
					}
				}

				let mut new_tick_vec = Vec::new();
				let mut new_tick = current_tick+tpq/16;

				if time_ptr < time_events.len() {
					let mut p = time_ptr;
					for ev in time_ptr..time_events.len() {
						if time_events[ev].tick < current_tick {
							continue;
						} else if time_events[ev].tick == current_tick {
							current_upt = time_events[ev].us_per_tick;
							p += 1;
						} else if time_events[ev].tick > current_tick {
							let mut tick_exists = false;
							for t in &new_tick_vec {
								if *t == time_events[ev].tick {
									tick_exists = true;
									break;
								}
							}

							if !tick_exists {
								new_tick_vec.push(time_events[ev].tick);
							}

							break;
						}
					}
					if p > time_ptr {
						time_ptr = p;
					}
				}

				let(new_events, style_tick) = style_manager.play(current_tick);
				new_tick_vec.push(style_tick);

				for ev in new_events {
					note_bytes.push(ev);
				}
				
				if new_tick_vec.len() > 0 {
					new_tick = new_tick_vec[0];
					for t in new_tick_vec {
						if t < new_tick {
							new_tick = t;
						}
					}
				}

				let old_tick = current_tick;
				if new_tick - old_tick > tpq/16 {
					new_tick = old_tick + tpq/16;
				}
				current_tick = new_tick;

				if current_tick >= len {
					for ev in &style_manager.get_style().event_sections[style_manager.get_section_number()] {
						if ev.tick < len {
							continue;
						}

						note_bytes.push(ev.data.clone());
					}
				}

				for ev in note_bytes {
					if ev.len() >= 3 && (ev[0]&0xF0) == 0x90 && ev[2] > 0 {
						let channel = ev[0]&0xF;
						let note = ev[1];

						if note_on[channel as usize][note as usize] {
							continue;
						}

						note_on[channel as usize][note as usize] = true;
					} else if ev.len() >= 3 && (ev[0]&0xE0) == 0x80 {
						let channel = ev[0]&0xF;
						let note = ev[1];

						note_on[channel as usize][note as usize] = false;
					}

					let _ = output_connection.send(&ev);
				}

				if current_tick >= len {
					current_tick = 0;
					time_ptr = 0;
					new_tick = len;
				}

				thread::sleep(Duration::from_micros(current_upt*(new_tick-old_tick)));
			}

			match input_connection {
				Some(input_connection) => {
					let _ = input_connection.close();
				}
				None => {

				}
			}

			for c in 0..note_on.len() {
				for n in 0..note_on[c].len() {
					if note_on[c][n] {
						let _ = output_connection.send(&[0x80 | (c as u8), n as u8, 0]);
					}
				}
			}

			let _ = jvm.attach_current_thread(|env| -> errors::Result<()> {
				match glob_arc.lock() {
					Ok(g_options) => {
						let jt_options = unsafe { JObject::from_raw(env, g_options.to_owned()) };
						let t_options = env.cast_local::<JMidiPlayerOptions>(jt_options).unwrap();
						
						let _ = t_options.set_play(env, false);

						let info_display = t_options.info_display(env).unwrap();
						let _ = env.call_method(&info_display, jni_str!("clearChord"), jni_sig!(()), &[]);
					}
					Err(_) => {
					}
				}
				Ok(())
			});

			output_connection.close();
		});
	}

	///Start playing a sequence.
	pub fn play_sequence(self, env: &mut Env<'local>, sequence: JFwsSequence<'local>, styles: Vec<FwsStyle>) {
		let j_options = self.player_options(env).unwrap();
		let g_options = env.new_global_ref(&j_options).unwrap();
		let options = env.cast_local::<JMidiPlayerOptions>(j_options).unwrap();
		
		let glob_arc = Arc::clone(&Arc::new(Mutex::new(g_options)));

		let _ = options.set_play(env, true);
		let song_melody_rh = options.song_melody_rh(env).unwrap();
		let song_melody_lh = options.song_melody_lh(env).unwrap();
		let instrument_melody_rh = options.instrument_melody_rh(env).unwrap();
		let instrument_melody_lh = options.instrument_melody_lh(env).unwrap();

		let id_name = match self.get_output_id(env) {
			Ok(id_name) => id_name.to_string(),
			Err(_) => {
				return;
			}
		};

		let midi_out = MidiOutput::new("FWS").unwrap();
		let port = match midi_out.find_port_by_id(id_name) {
			Some(port) => port,
			None => {
				return;
			}
		};

		let mut connection = match midi_out.connect(&port, &port.id()) {
			Ok(connection) => connection,
			Err(e) => {
				println!("{}", e);
				return;
			}
		};

		let start_tick = options.start_tick(env).unwrap();

		let melody_rh = options.song_melody_rh(env).unwrap() as u8;
		let melody_lh = options.song_melody_lh(env).unwrap() as u8;

		let tpq = sequence.get_tpq(env).unwrap() as u64;
		let (time_events, note_events, key_events, chord_events, style_events) = sequence.get_all_events(env, &[melody_rh, melody_lh]);

		let mut style_manager = StyleManager::get(&styles, tpq);

		let jvm = env.get_java_vm().unwrap();
		thread::spawn( move || {
			let mut time_ptr = 0;
			let mut note_ptr = 0;
			let mut key_ptr = 0;
			let mut chord_ptr = 0;
			let mut style_ptr = 0;

			let mut current_tick = if start_tick >= 0 {start_tick as u64} else {0};
			let mut current_upt = 5000/tpq;

			let mut style = "".to_string();
			let mut section = "".to_string();
			let mut style_parts = [false; 8];
			let mut set_style_tick = 0;
			let mut style_retain = false;
			let mut next_style_tick = current_tick;

			let set_root = Arc::new(Mutex::new(Note::NoteNC));
			let set_chord = Arc::new(Mutex::new(Chord::ChordOff));
			let set_full_root = Arc::new(Mutex::new(-1));
			let set_bass_root = Arc::new(Mutex::new(Note::NoteNC));
			let set_bass_chord = Arc::new(Mutex::new(Chord::ChordOff));
			let set_full_bass_root = Arc::new(Mutex::new(-1));

			let set_inversion = Arc::new(Mutex::new(-1));

			let mut chord_changed = false;

			let mut play = true;

			let mut note_on = [[false; 128];16];

			let mut start_events = Vec::new();

			//Set the initial chord and style.
			{
				let mut set_style = "".to_string();
				let mut set_section = "".to_string();
				let mut set_set_style_tick = 0;
				let mut set_style_parts = [false; 8];

				let mut set_set_root = *set_root.lock().unwrap();
				let mut set_set_chord = *set_chord.lock().unwrap();
				let mut set_set_full_root = *set_full_root.lock().unwrap();
				let mut set_set_bass_root = *set_bass_root.lock().unwrap();
				let mut set_set_bass_chord = *set_bass_chord.lock().unwrap();
				let mut set_set_bass_full_root = *set_full_bass_root.lock().unwrap();
				let mut set_set_inversion = *set_inversion.lock().unwrap();
				
				let mut last_tick = 0;

				let mut set_key_pressure = [[-1; 128]; 16];
				let mut set_control = [[-1; 128]; 16];
				let mut set_voice = [-1; 16];
				let mut set_channel_pressure = [-1; 16];
				let mut set_pitch_bend = [-1; 16];

				//Get the style.
				for e in &style_events {
					if e.tick <= current_tick {
						if !e.retain {
							last_tick = e.tick;
						}
						set_style = e.style.clone();
						set_section = e.section.clone();
						if !e.retain {
							set_set_style_tick = e.style_tick;
						}
						set_style_parts = e.parts.clone();
						continue;
					} else {
						style = set_style;
						section = set_section;
						set_style_tick = set_set_style_tick;
						style_parts = set_style_parts.clone();

						break;
					}
				}

				//Get the chord.
				for e in &chord_events {
					if e.tick <= current_tick {
						set_set_root = e.main_root;
						set_set_chord = e.main_chord;
						set_set_full_root = e.main_full;
						set_set_bass_root = e.bass_root;
						set_set_bass_chord = e.bass_chord;
						set_set_bass_full_root = e.bass_full;
						set_set_inversion = e.inversion;
						continue;
					} else {
						*set_root.lock().unwrap() = set_set_root;
						*set_chord.lock().unwrap() = set_set_chord;
						*set_full_root.lock().unwrap() = set_set_full_root;
						*set_bass_root.lock().unwrap() = set_set_bass_root;
						*set_bass_chord.lock().unwrap() = set_set_bass_chord;
						*set_full_bass_root.lock().unwrap() = set_set_bass_full_root;
						*set_inversion.lock().unwrap() = set_set_inversion;

						break;
					}
				}

				//Get the usec per tick.
				for e in &time_events {
					if e.tick <= current_tick {
						current_upt = e.us_per_tick;
					} else {
						break;
					}
				}

				//Get the key signature.
				let mut set_key = 0;
				for e in &key_events {
					if e.tick <= current_tick {
						set_key = e.accidental_count;
						continue;
					} else {
						let _ = jvm.attach_current_thread(|env| -> errors::Result<()> {
							match glob_arc.try_lock() {
								Ok(g_options) => {
									let jt_options = unsafe { JObject::from_raw(env, g_options.to_owned()) };
									let t_options = env.cast_local::<JMidiPlayerOptions>(jt_options).unwrap();

									let info_display = t_options.info_display(env).unwrap();
									let _ = env.call_method(&info_display, jni_str!("refreshKeySig"), jni_sig!((jbyte)), &[Byte(set_key)]);
								}
								Err(_) => {
								}
							}
							Ok(())
						});

						break;
					}
				}

				//Get initial events.
				for e in &note_events {
					if e.tick <= current_tick {
						if e.data.len() >= 2 && (e.data[0]&0xF0) != 0xF0 && (e.data[0]&0xE0) != 0x80 {
							let channel = (e.data[0]&0xF) as usize;
							let data1 = e.data[1];
							let data2 = if e.data.len() >= 3 { e.data[2] } else { 0 };
							match e.data[0]&0xF0 {
								0xA0 => { //Polyphonic key pressure.
									set_key_pressure[channel][data1 as usize] = data2 as i8;
								}
								0xB0 => { //Control.
									set_control[channel][data1 as usize] = data2 as i8;
								}
								0xC0 => { //Voice.
									set_voice[channel] = data1 as i8;
								}
								0xD0 => { //Channel aftertouch.
									set_channel_pressure[channel] = data1 as i8;
								}
								0xE0 => { //Pitch bend.
									set_pitch_bend[channel] = ((data2<<7) | data1) as i16;
								}
								_ => {
									
								}
							}
						}
					}
				}


				//Set the style.
				let style_start_events = if (!style.is_empty() && !section.is_empty()) || last_tick > 0 {
						style_manager.set_style_full(last_tick, &ChordBody {
						main_root: set_set_root,
						main_chord: set_set_chord,
						bass_root: set_set_bass_root,
						bass_chord: set_set_bass_chord,
					}, &style, &section, set_set_style_tick, set_style_parts.clone())
				} else {
					let mut style_found = false;
					let mut new_style = "".to_string();
					let mut new_section = "".to_string();
					for s in &style_events  {
						if s.tick > 0 && !s.style.is_empty() && !s.section.is_empty() {
							style_found = true;
							new_style = s.style.clone();
							new_section = s.section.clone();

							break;
						}
					}

					if style_found {
						style_manager.set_style_full(last_tick, &ChordBody {
						main_root: set_set_root,
						main_chord: set_set_chord,
						bass_root: set_set_bass_root,
						bass_chord: set_set_bass_chord,
					}, &new_style, &new_section, 0, [true; 8])
					} else {
						Vec::new()
					}
				};
			
				//Set the initial events.
				for c in 0..set_key_pressure.len() {
					let channel = set_key_pressure[c];
					for k in 0..channel.len() {
						if channel[k] >= 0 {
							start_events.push([0xA0 | c as u8, k as u8, channel[k] as u8].to_vec());
						}
					}
				}

				//Set the short variables.
				for c in 0..set_control.len() {
					let channel = set_control[c];
					for k in 0..channel.len() {
						if channel[k] >= 0 {
							start_events.push([0xB0 | c as u8, k as u8, channel[k] as u8].to_vec());
						}
					}
				}

				for c in 0..set_voice.len() {
					if set_voice[c] >= 0 {
						start_events.push([0xC0 | c as u8, set_voice[c] as u8].to_vec());
					}
				}

				for c in 0..set_channel_pressure.len() {
					if set_channel_pressure[c] >= 0 {
						start_events.push([0xD0 | c as u8, set_channel_pressure[c] as u8].to_vec());
					}
				}

				for c in 0..set_pitch_bend.len() {
					if set_pitch_bend[c] >= 0 {
						let data1 = (set_pitch_bend[c]&0x7F) as u8;
						let data2 = (set_pitch_bend[c]>>7) as u8;
						start_events.push([0xE0 | c as u8, data1, data2].to_vec());
					}
				}

				for ev in style_start_events {
					if ev.len() > 0 && (ev[0]&0xE0) != 80 {
						start_events.push(ev.clone());
					}
				}
			
				//Set the starting melody/harmony notes.
				let notes_on = GenericMIDIEvent::get_notes_on_at(&note_events, current_tick);
				for ev in notes_on {
					if ev.tick >= current_tick {
						continue;
					}

					start_events.push(ev.data);
				}
			}

			while play {
				let _ = jvm.attach_current_thread(|env| -> errors::Result<()> {
					match glob_arc.try_lock() {
						Ok(g_options) => {
							let jt_options = unsafe { JObject::from_raw(env, g_options.to_owned()) };
							let t_options = env.cast_local::<JMidiPlayerOptions>(jt_options).unwrap();

							play = t_options.play(env).unwrap();
							let _ = t_options.set_current_tick(env, current_tick as i64);
						}
						Err(_) => {
						}
					}
					Ok(())
				});

				if !play {
					break;
				}

				let mut note_bytes = Vec::new();
				let mut new_tick_vec = Vec::new();
				let mut new_tick = current_tick;

				if start_events.len() > 0 {
					for ev in &start_events {
						note_bytes.push(ev.clone());
					}
					start_events.clear();
				}

				if time_ptr < time_events.len() {
					let mut i = time_ptr;
					for e in time_ptr..time_events.len() {
						let ev = &time_events[e];
						if ev.tick < current_tick {
							continue;
						}

						if ev.tick > current_tick {
							new_tick_vec.push(ev.tick);
							time_ptr = e;
							break;
						}

						current_upt = ev.us_per_tick;
						i += 1;
					}
					if i > time_ptr {
						time_ptr = i;
					}
				}

				let last_style = style.clone();
				let last_section = section.clone();
				let last_parts = style_parts.clone();
				if style_ptr < style_events.len() {
					let mut s = style_ptr;
					for e in style_ptr..style_events.len() {
						let ev = &style_events[e];
						if ev.tick < current_tick {
							continue;
						}

						if ev.tick > current_tick {
							new_tick_vec.push(ev.tick);
							style_ptr = e;
							break;
						}

						style = ev.style.clone();
						section = ev.section.clone();
						style_parts = ev.parts.clone();
						set_style_tick = ev.style_tick;
						style_retain = ev.retain;

						s += 1;
					}
					if s > style_ptr {
						style_ptr = s;
					}
				}

				if chord_ptr < chord_events.len() {
					let mut c = chord_ptr;
					for e in chord_ptr..chord_events.len() {
						let ev = &chord_events[e];
						if ev.tick < current_tick {
							continue;
						}

						if ev.tick > current_tick {
							new_tick_vec.push(ev.tick);
							chord_ptr = e;
							break;
						}

						*set_root.lock().unwrap() = ev.main_root;
						*set_chord.lock().unwrap() = ev.main_chord;
						*set_full_root.lock().unwrap() = ev.main_full;
						*set_bass_root.lock().unwrap() = ev.bass_root;
						*set_bass_chord.lock().unwrap() = ev.bass_chord;
						*set_full_bass_root.lock().unwrap() = ev.bass_full;
						*set_inversion.lock().unwrap() = ev.inversion;
						chord_changed = true;

						c += 1;
					}
					if c > chord_ptr {
						chord_ptr = c;
					}
				}

				let j_chord_changed = chord_changed;

				if !last_style.eq(&style) {
					next_style_tick = current_tick;
					let new_notes = if chord_changed {
						chord_changed = false;
						style_manager.set_style_full(current_tick, &ChordBody { 
																			main_root: *set_root.lock().unwrap(),
																			main_chord: *set_chord.lock().unwrap(),
																			bass_root: *set_bass_root.lock().unwrap(),
																			bass_chord: *set_bass_chord.lock().unwrap() }
																			, &style, &section, set_style_tick, style_parts)
					} else {
						style_manager.set_style(current_tick, &style, &section, set_style_tick, style_parts)
					};
					for n in new_notes {
						note_bytes.push(n);
					}
				} else if !last_section.eq(&section) || !last_parts.eq(&style_parts) {
					next_style_tick = current_tick;
					let new_notes = if chord_changed {
						chord_changed = false;
						style_manager.set_section_full(current_tick, &ChordBody { 
																			main_root: *set_root.lock().unwrap(),
																			main_chord: *set_chord.lock().unwrap(),
																			bass_root: *set_bass_root.lock().unwrap(),
																			bass_chord: *set_bass_chord.lock().unwrap() }
																			, &section, set_style_tick, style_retain, style_parts)
					} else {
						style_manager.set_section(current_tick, &section, set_style_tick, style_retain, style_parts)
					};
					for n in new_notes {
						note_bytes.push(n);
					}
				}

				if chord_changed {
					chord_changed = false;
					let new_notes = style_manager.set_chord(current_tick, ChordBody { 
																			main_root: *set_root.lock().unwrap(),
																			main_chord: *set_chord.lock().unwrap(),
																			bass_root: *set_bass_root.lock().unwrap(),
																			bass_chord: *set_bass_chord.lock().unwrap() });
					for n in new_notes {
						note_bytes.push(n);
					}
				}

				if !style.is_empty() && !section.is_empty() && next_style_tick == current_tick {
					let (style_notes, next_event) = style_manager.play(current_tick);
					for n in style_notes {
						note_bytes.push(n);
					}
					next_style_tick = next_event;
					new_tick_vec.push(next_style_tick);
				} else if !style.is_empty() && !section.is_empty() {
					new_tick_vec.push(next_style_tick);
				}

				if note_ptr < note_events.len() {
					let mut j = note_ptr;
					for e in note_ptr..note_events.len() {
						let ev = &note_events[e];
						if ev.tick < current_tick {
							continue;
						}

						if ev.tick > current_tick {
							new_tick_vec.push(ev.tick);
							note_ptr = e;
							break;
						}

						note_bytes.push(ev.data.clone());
						j += 1;
					}
					if j > note_ptr {
						note_ptr = j;
					}
				}

				let mut new_key = false;
				let mut change_key = 0;

				if key_ptr < key_events.len() {
					let mut k = key_ptr;
					for e in key_ptr..key_events.len() {
						let ev = &key_events[e];
						if ev.tick < current_tick {
							continue;
						}

						if ev.tick > current_tick {
							new_tick_vec.push(ev.tick);
							key_ptr = e;
							break;
						}

						new_key = true;
						change_key = ev.accidental_count;

						k += 1;
					}
					if k > key_ptr {
						key_ptr = k;
					}
				}

				if new_key || j_chord_changed {
					let _ = jvm.attach_current_thread(|env| -> errors::Result<()> {
						match glob_arc.try_lock() {
							Ok(g_options) => {
								let jt_options = unsafe { JObject::from_raw(env, g_options.to_owned()) };
								let t_options = env.cast_local::<JMidiPlayerOptions>(jt_options).unwrap();

								let info_display = t_options.info_display(env).unwrap();
								if new_key {
									let _ = env.call_method(&info_display, jni_str!("refreshKeySig"), jni_sig!((jbyte)), &[Byte(change_key)]);
								}
								if j_chord_changed {
									let main = *set_chord.lock().unwrap();
									let root_full = *set_full_root.lock().unwrap();
									let bass = *set_bass_chord.lock().unwrap();
									let bass_full = *set_full_bass_root.lock().unwrap();

									let _ = env.call_method(&info_display, jni_str!("refreshChord"), jni_sig!((jbyte, jbyte, jbyte, jbyte, jint, jbyte)), &[Byte(root_full), Byte(main as i8), Byte(bass_full), Byte(bass as i8), Int(*set_inversion.lock().unwrap() as i32), Byte(0)]);
								}
							}
							Err(_) => {
							}
						}
						Ok(())
					});
				}

				if new_tick_vec.len() > 0 {
					new_tick = new_tick_vec[0];
					for t in new_tick_vec {
						if t < new_tick {
							new_tick = t;
						}
					}
				}

				if new_tick > current_tick + tpq/16 {
					new_tick = current_tick + tpq/16;
				}

				let old_tick = current_tick;
				current_tick = new_tick;

				let mut new_notes_on = Vec::new();
				let mut new_notes_off = Vec::new();

				for mut ve in note_bytes {
					if ve.len() >= 1 && (ve[0]&0xF0) == 0xF0 {
						continue;
					}
					
					if ve.len() >= 1 && (ve[0]&0xF0) != 0xF0 { //Change channel as needed.
						let command = ve[0]&0xF0;
						let channel = (ve[0]&0xF) as i8;

						let mut change_allowed = true;
						if instrument_melody_lh == instrument_melody_rh && channel == song_melody_lh && command != 0x80 && command != 0x90 {
							change_allowed = false;
						}

						if change_allowed {
							if channel == instrument_melody_rh || channel == song_melody_rh {
								if channel == instrument_melody_rh && channel != song_melody_rh && song_melody_rh >= 0 {
									ve[0] = command | (song_melody_rh as u8);
								} else if channel != instrument_melody_rh && channel == song_melody_rh && instrument_melody_rh >= 0 {
									ve[0] = command | (instrument_melody_rh as u8);
								} 
							} else if channel == instrument_melody_lh || channel == song_melody_lh {
								if channel == instrument_melody_lh && channel != song_melody_lh && song_melody_lh >= 0 {
									ve[0] = command | (song_melody_lh as u8);
								} else if channel != instrument_melody_lh && channel == song_melody_lh && instrument_melody_lh >= 0 {
									ve[0] = command | (instrument_melody_lh as u8);
								}
							}
						}
					}

					if ve.len() >= 2 && (ve[0]&0xF0) == 0x90 { //Note on event.
						let channel = (ve[0]&0xF) as usize;
						let note = ve[1] as usize;

						let last_note_on = note_on[channel][note];
						note_on[channel][note] = true;
						if !last_note_on && (channel as i8 == instrument_melody_lh || channel as i8 == instrument_melody_rh) {
							new_notes_on.push(note as u8);
						}
					} else if ve.len() >=2 && (ve[0]&0xF0) == 0x80 { //Note off event.
						let channel = (ve[0]&0xF) as usize;
						let note = ve[1] as usize;

						let last_note_on = note_on[channel][note];
						note_on[channel][note] = false;
						if last_note_on && (channel as i8 == instrument_melody_lh || channel as i8 == instrument_melody_rh) {
							new_notes_off.push(note as u8);
						}
					}

					for n in &new_notes_on {
						let mut i = 0;
						while i < new_notes_off.len() {
							if new_notes_off[i] == *n {
								new_notes_off.remove(i);
							} else {
								i += 1;
							}
						}
					}

					if new_notes_off.len() > 0 || new_notes_on.len() > 0 {
						let _ = jvm.attach_current_thread(|env| -> errors::Result<()> {
							match glob_arc.try_lock() {
								Ok(g_options) => {
									let jt_options = unsafe { JObject::from_raw(env, g_options.to_owned()) };
									let t_options = env.cast_local::<JMidiPlayerOptions>(jt_options).unwrap();

									let info_display = t_options.info_display(env).unwrap();
									
									for n in &new_notes_on {
										let _ = env.call_method(&info_display, jni_str!("refreshMelody"), jni_sig!((jbyte, jboolean)), &[Byte(*n as i8), Bool(true)]);
									}

									for n in &new_notes_off {
										let _ = env.call_method(&info_display, jni_str!("refreshMelody"), jni_sig!((jbyte, jboolean)), &[Byte(*n as i8), Bool(false)]);
									}
								}
								Err(_) => {
								}
							}
							Ok(())
						});
					}

					let _ = connection.send(&ve);
				}

				if time_ptr >= time_events.len() - 1 && note_ptr >= note_events.len() - 1 {
					break;
				}

				thread::sleep(Duration::from_micros(current_upt*(new_tick - old_tick)));
			}

			for c in 0..note_on.len() {
				for n in 0..note_on[c].len() {
					if note_on[c][n] {
						let _ = connection.send(&[0x80 | (c as u8), n as u8, 0]);
					}
				}
			}

			connection.close();

			let mut start_key = 0;
			let start_tick = 0;
			for ev in key_events {
				if ev.tick == start_tick {
					start_key = ev.accidental_count;
					break;
				} else if ev.tick > start_tick {
					break;
				} else {
					start_key = ev.accidental_count;
				}
			}

			let _ = jvm.attach_current_thread(|env| -> errors::Result<()> {
				match glob_arc.try_lock() {
					Ok(g_options) => {
						let jt_options = unsafe { JObject::from_raw(env, g_options.to_owned()) };
						let t_options = env.cast_local::<JMidiPlayerOptions>(jt_options).unwrap();
						
						let _ = t_options.set_play(env, false);

						let info_display = t_options.info_display(env).unwrap();
						let _ = env.call_method(&info_display, jni_str!("clearMelody"), jni_sig!(()), &[]);
						let _ = env.call_method(&info_display, jni_str!("refreshKeySig"), jni_sig!((jbyte)), &[Byte(start_key)]);
						let _ = env.call_method(&info_display, jni_str!("clearChord"), jni_sig!(()), &[]);
					}
					Err(_) => {
					}
				}
				Ok(())
			});
		});
	}

	///Start playing a song.
	pub fn play_song(self, env: &mut Env<'local>, song: JFWSSong<'local>) {
		let sequence = song.get_sequence(env);
		let styles = song.get_styles(env);
		self.play_sequence(env, sequence, styles);
	}

	///Get a generic MIDI event list.
	pub fn get_midi_events(self, env: &mut Env<'local>, export_options: JExportOptions, sequence: JFwsSequence<'local>, styles: Vec<FwsStyle>) -> Vec<(Vec<u8>, u64)> {
		let j_options = self.player_options(env).unwrap();
		let options = env.cast_local::<JMidiPlayerOptions>(j_options).unwrap();

		let song_melody_rh = options.song_melody_rh(env).unwrap();
		let song_melody_lh = options.song_melody_lh(env).unwrap();

		let export_melody_rh = export_options.export_melody_rh(env).unwrap();
		let export_melody_lh = export_options.export_melody_lh(env).unwrap();

		let song_melody_rh_u = song_melody_rh as u8;
		let song_melody_lh_u = song_melody_lh as u8;

		let truncate = export_options.truncate(env).unwrap();
		let truncate_channels = if truncate {
			&[song_melody_rh_u, song_melody_lh_u]
		} else {
			[].as_slice()
		};

		let tpq = sequence.get_tpq(env).unwrap() as u64;
		let (_, note_events, _, chord_events, style_events) = sequence.get_all_events(env, truncate_channels);

		let mut current_tick = 0;

		let mut style_manager = StyleManager::get(&styles, tpq);

		let mut style = "".to_string();
		let mut section = "".to_string();
		let mut style_parts = [false; 8];
		let mut set_style_tick = 0;
		let mut style_retain = false;
		let mut next_style_tick = current_tick;

		let mut set_root = Note::NoteNC;
		let mut set_chord = Chord::ChordOff;
		let mut set_bass = Note::NoteNC;
		let mut set_bass_chord = Chord::ChordOff;

		let mut chord_changed = false;

		let mut note_on = [[false; 128];16];

		let mut note_ptr = 0;
		let mut chord_ptr = 0;
		let mut style_ptr = 0;

		let mut midi_events = Vec::new();

		//Get the initial style settings.
		{
			let mut style_found = false;
			let mut new_style = "".to_string();
			let mut new_section = "".to_string();
			for s in &style_events  {
				if s.tick > 0 && !s.style.is_empty() && !s.section.is_empty() {
					style_found = true;
					new_style = s.style.clone();
					new_section = s.section.clone();

					break;
				}
			}

			if style_found {
				let style_start = style_manager.set_style_full(0, &ChordBody {
				main_root: Note::NoteNC,
				main_chord: Chord::ChordOff,
				bass_root: Note::NoteNC,
				bass_chord: Chord::ChordOff,
				}, &new_style, &new_section, 0, [true; 8]);

				for e in style_start {
					midi_events.push((e.clone(), 0));
				}
			}
		}

		while note_ptr < note_events.len() ||
				chord_ptr < chord_events.len() ||
				style_ptr < style_events.len() {
			let mut note_bytes = Vec::new();
			let mut new_tick_vec = Vec::new();
			let mut new_tick = current_tick;

			let last_style = style.clone();
			let last_section = section.clone();
			let last_parts = style_parts.clone();
			if style_ptr < style_events.len() {
				let mut s = style_ptr;
				for e in style_ptr..style_events.len() {
					let ev = &style_events[e];
					if ev.tick < current_tick {
						continue;
					}

					if ev.tick > current_tick {
						new_tick_vec.push(ev.tick);
						style_ptr = e;
						break;
					}

					style = ev.style.clone();
					section = ev.section.clone();
					style_parts = ev.parts.clone();
					set_style_tick = ev.style_tick;
					style_retain = ev.retain;

					s += 1;
				}
				if s > style_ptr {
					style_ptr = s;
				}
			}

			if chord_ptr < chord_events.len() {
				let mut c = chord_ptr;
				for e in chord_ptr..chord_events.len() {
					let ev = &chord_events[e];
					if ev.tick < current_tick {
						continue;
					}

					if ev.tick > current_tick {
						new_tick_vec.push(ev.tick);
						chord_ptr = e;
						break;
					}

					set_root = ev.main_root;
					set_chord = ev.main_chord;
					set_bass = ev.bass_root;
					set_bass_chord = ev.bass_chord;
					chord_changed = true;

					c += 1;
				}
				if c > chord_ptr {
					chord_ptr = c;
				}
			}

			if !last_style.eq(&style) {
				next_style_tick = current_tick;
				let new_notes = if chord_changed {
					chord_changed = false;
					style_manager.set_style_full(current_tick, &ChordBody { 
																		main_root: set_root,
																		main_chord: set_chord,
																		bass_root: set_bass,
																		bass_chord: set_bass_chord }
																		, &style, &section, set_style_tick, style_parts)
				} else {
					style_manager.set_style(current_tick, &style, &section, set_style_tick, style_parts)
				};
				for n in new_notes {
					note_bytes.push(n);
				}
			} else if !last_section.eq(&section) || !last_parts.eq(&style_parts) {
				next_style_tick = current_tick;
				let new_notes = if chord_changed {
					chord_changed = false;
					style_manager.set_section_full(current_tick, &ChordBody { 
																		main_root: set_root,
																		main_chord: set_chord,
																		bass_root: set_bass,
																		bass_chord: set_bass_chord }
																		, &section, set_style_tick, style_retain, style_parts)
				} else {
					style_manager.set_section(current_tick, &section, set_style_tick, style_retain, style_parts)
				};
				for n in new_notes {
					note_bytes.push(n);
				}
			}

			if chord_changed {
				chord_changed = false;
				let new_notes = style_manager.set_chord(current_tick, ChordBody { 
																		main_root: set_root,
																		main_chord: set_chord,
																		bass_root: set_bass,
																		bass_chord: set_bass_chord });
				for n in new_notes {
					note_bytes.push(n);
				}
			}

			if !style.is_empty() && !section.is_empty() && next_style_tick == current_tick {
				let (style_notes, next_event) = style_manager.play(current_tick);
				for n in style_notes {
					note_bytes.push(n);
				}
				next_style_tick = next_event;
				new_tick_vec.push(next_style_tick);
			} else if !style.is_empty() && !section.is_empty() {
				new_tick_vec.push(next_style_tick);
			}

			if note_ptr < note_events.len() {
				let mut j = note_ptr;
				for e in note_ptr..note_events.len() {
					let ev = &note_events[e];
					if ev.tick < current_tick {
						continue;
					}

					if ev.tick > current_tick {
						new_tick_vec.push(ev.tick);
						note_ptr = e;
						break;
					}

					note_bytes.push(ev.data.clone());
					j += 1;
				}
				if j > note_ptr {
					note_ptr = j;
				}
			}

			if new_tick_vec.len() > 0 {
				new_tick = new_tick_vec[0];
				for t in new_tick_vec {
					if t < new_tick {
						new_tick = t;
					}
				}
			}

			let old_tick = current_tick;
			current_tick = new_tick;

			let mut new_notes_on = Vec::new();
			let mut new_notes_off = Vec::new();

			for mut ve in note_bytes {
				if ve.len() >= 1 && (ve[0]&0xF0) != 0xF0 { //Change channel as needed.
					let command = ve[0]&0xF0;
					let channel = (ve[0]&0xF) as i8;

					let mut change_allowed = true;
					if export_melody_lh == export_melody_rh && channel == song_melody_lh && command != 0x80 && command != 0x90 {
						change_allowed = false;
					}

					if change_allowed {
						if channel == export_melody_rh || channel == song_melody_rh {
							if channel == export_melody_rh && channel != song_melody_rh && song_melody_rh >= 0 {
								ve[0] = command | (song_melody_rh as u8);
							} else if channel != export_melody_rh && channel == song_melody_rh && export_melody_rh >= 0 {
								ve[0] = command | (export_melody_rh as u8);
							} 
						} else if channel == export_melody_lh || channel == song_melody_lh {
							if channel == export_melody_lh && channel != song_melody_lh && song_melody_lh >= 0 {
								ve[0] = command | (song_melody_lh as u8);
							} else if channel != export_melody_lh && channel == song_melody_lh && export_melody_lh >= 0 {
								ve[0] = command | (export_melody_lh as u8);
							}
						}
					}
				}

				if ve.len() >= 2 && (ve[0]&0xF0) == 0x90 { //Note on event.
					let channel = (ve[0]&0xF) as usize;
					let note = ve[1] as usize;

					let last_note_on = note_on[channel][note];
					note_on[channel][note] = true;
					if !last_note_on {
						new_notes_on.push(note as u8);
					}
				} else if ve.len() >=2 && (ve[0]&0xF0) == 0x80 { //Note off event.
					let channel = (ve[0]&0xF) as usize;
					let note = ve[1] as usize;

					let last_note_on = note_on[channel][note];
					note_on[channel][note] = false;
					if last_note_on {
						new_notes_off.push(note as u8);
					}
				}

				for n in &new_notes_on {
					let mut i = 0;
					while i < new_notes_off.len() {
						if new_notes_off[i] == *n {
							new_notes_off.remove(i);
						} else {
							i += 1;
						}
					}
				}
				
				midi_events.push((ve.clone(), old_tick));
			}
		}
		
		return midi_events;
	}

	///Get a MIDI event list from a song.
	pub fn get_song_midi_events(self, env: &mut Env<'local>, export_options: JExportOptions, song: JFWSSong <'local>) -> Vec<(Vec<u8>, u64)> {
		let sequence = song.get_sequence(env);
		let styles = song.get_styles(env);

		return self.get_midi_events(env, export_options, sequence, styles);
	}

	///Start recording a sequence.
	pub fn record_start(self, env: &mut Env<'_>) {
		let j_options = self.player_options(env).unwrap();
		let g_options = env.new_global_ref(&j_options).unwrap();
		let options = env.cast_local::<JMidiPlayerOptions>(j_options).unwrap();
		
		let glob_arc = Arc::clone(&Arc::new(Mutex::new(g_options)));

		let _ = options.set_play(env, true);

		let id_name = match self.get_input_id(env) {
			Ok(id_name) => id_name.to_string(),
			Err(_) => {
				return;
			}
		};

		let g_self = env.new_global_ref(self);
		let self_arc = Arc::clone(&Arc::new(Mutex::new(g_self)));

		let jvm = env.get_java_vm().unwrap();

		thread::spawn( move || {
			let mut play = true;

			let midi_in = MidiInput::new("FWS").unwrap();
			let port = match midi_in.find_port_by_id(id_name.clone()) {
				Some(port) => port,
				None => {
					return;
				}
			};
			
			let midi_data = Vec::new();
			
			let mut tick = 0 as i64;

			let connection = midi_in.connect(&port, "FWS", move |_stamp, message, m_midi_data| {
				if message.len() > 0 && message[0] == 0xF8 { //Tick.
					tick += 1;
				} else {
					m_midi_data.push((message.to_vec(), tick));
				}
			}, midi_data, ).unwrap();

			while play {
				let _ = jvm.attach_current_thread(|env| -> errors::Result<()> {
					match glob_arc.try_lock() {
						Ok(g_options) => {
							let jt_options = unsafe { JObject::from_raw(env, g_options.to_owned()) };
							let t_options = env.cast_local::<JMidiPlayerOptions>(jt_options).unwrap();

							play = t_options.play(env).unwrap();
						}
						Err(_) => {
						}
					}
					Ok(())
				});

				if !play {
					break;
				}
			}

			let(_, midi_data) = connection.close();

			let mut r_midi_bytes = Vec::new();
			let mut r_midi_ticks = Vec::new();
			for d in midi_data {
				let mut data = Vec::new();
				for i in d.0 {
					data.push(i as i8);
				}
				r_midi_bytes.push(data);
				r_midi_ticks.push(d.1 as i64);
			}

			let _ = jvm.attach_current_thread(|env| -> errors::Result<()> {
				let j_midi_bytes = JObjectArray::<JByteArray>::new(env, r_midi_bytes.len(), JByteArray::null()).unwrap();
				for i in 0..r_midi_bytes.len() {
					let d = &r_midi_bytes[i];
					let j_bytes = JByteArray::new(env, d.len()).unwrap();
					let _ = j_bytes.set_region(env, 0, &d);
					let _ = j_midi_bytes.set_element(env, i, j_bytes);
				}

				let j_midi_ticks = env.new_long_array(r_midi_ticks.len()).unwrap();
				let _ = j_midi_ticks.set_region(env, 0, &r_midi_ticks);

				match self_arc.try_lock() {
					Ok(mut g_midi_handler) => {
						let gu_midi_handler = g_midi_handler.as_mut().unwrap();
						let j_midi_handler = unsafe { JObject::from_raw(env, gu_midi_handler.to_owned()) };

						let midi_handler = env.cast_local::<JMidiManager>(j_midi_handler).unwrap();
						let _ = midi_handler.load_recorded_data(env, j_midi_bytes, j_midi_ticks);
					}
					Err(_) => {
					}
				}
				Ok(())
			});
		});
	}

}
