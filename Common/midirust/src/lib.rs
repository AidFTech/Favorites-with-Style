use std::{thread::{self, sleep}, time::Duration};

use jni::{EnvUnowned, JValue, errors::{Error, ThrowRuntimeExAndDefault}, jni_sig, jni_str, objects::{JByteArray, JClass, JObject, JObjectArray, JString}};
use midir::{MidiInput, MidiOutput};

use midi_device::JMidiDevice;

use crate::{fws_event::JFWSNoteEvent, fws_sequence::JFwsSequence, fws_song::JFWSSong, fws_style::JFwsStyle, fws_voice::JVoice, midi_manager::JMidiManager, midi_player_options::JExportOptions};

extern crate jni;
extern crate midir;

mod fws_sequence;
mod midi_device;
mod midi_manager;
mod midi_player_options;
mod fws_event;
mod fws_style;
mod fws_song;
mod fws_voice;

///Get the input device list.
#[no_mangle]
#[allow(non_snake_case)]
pub fn Java_controllers_MIDIManager_getMidiInputDeviceList<'local>(mut env: EnvUnowned<'local>, _class: JClass<'local>) -> JObjectArray<'local, JMidiDevice<'local>> {
	let ret = env.with_env(|mut env| -> Result<JObjectArray<'local, JMidiDevice<'local>>, Error> {
		let mut dev_list = Vec::new();
		let mut device_count = 0;

		let midi_in = MidiInput::new("FWS").unwrap();
		for (_n, dev) in midi_in.ports().iter().enumerate() {
			let mut r_desc = midi_in.port_name(dev).unwrap();
			match r_desc.find(&dev.id()) {
				Some(index) => {
					let cut = &r_desc[0..index];
					r_desc = cut.to_string();
				}
				None => {
				}
			}

			match r_desc.find(":") {
				Some(index) => {
					let cut = &r_desc[index+1..];
					r_desc = cut.to_string();
				}
				None => {
				}
			}

			let name = JString::from_str(&mut env, dev.id()).unwrap();
			let vendor = JString::from_str(&mut env, "").unwrap();
			let description = JString::from_str(&mut env, r_desc).unwrap();
			let version = JString::from_str(&mut env, "").unwrap();
			dev_list.push(JMidiDevice::new(&mut env, name, vendor, description, version).unwrap());
			device_count += 1;
		}
		
		let info_arr = JObjectArray::<JMidiDevice>::new(&mut env, device_count, &dev_list[0]).unwrap();
		for i in 0..device_count {
			let _ = info_arr.set_element(&mut env, i, &dev_list[i]);
		}

		Ok(info_arr)
	});

	ret.resolve::<ThrowRuntimeExAndDefault>()
}

///Get the output device list.
#[no_mangle]
#[allow(non_snake_case)]
pub fn Java_controllers_MIDIManager_getMidiOutputDeviceList<'local>(mut env: EnvUnowned<'local>, _class: JClass<'local>) -> JObjectArray<'local, JMidiDevice<'local>> {
	let ret = env.with_env(|mut env| -> Result<JObjectArray<'local, JMidiDevice<'local>>, Error> {
		let mut dev_list = Vec::new();
		let mut device_count = 0;

		let midi_out = MidiOutput::new("FWS").unwrap();
		for (_n, dev) in midi_out.ports().iter().enumerate() {
			let mut r_desc = midi_out.port_name(dev).unwrap();
			match r_desc.find(&dev.id()) {
				Some(index) => {
					let cut = &r_desc[0..index];
					r_desc = cut.to_string();
				}
				None => {
				}
			}

			match r_desc.find(":") {
				Some(index) => {
					let cut = &r_desc[index+1..];
					r_desc = cut.to_string();
				}
				None => {
				}
			}

			let name = JString::from_str(&mut env, dev.id()).unwrap();
			let vendor = JString::from_str(&mut env, "").unwrap();
			let description = JString::from_str(&mut env, r_desc).unwrap();
			let version = JString::from_str(&mut env, "").unwrap();
			dev_list.push(JMidiDevice::new(&mut env, name, vendor, description, version).unwrap());
			device_count += 1;
		}
		
		let info_arr = JObjectArray::<JMidiDevice>::new(&mut env, device_count, &dev_list[0]).unwrap();
		for i in 0..device_count {
			let _ = info_arr.set_element(&mut env, i, &dev_list[i]);
		}

		Ok(info_arr)
	});

	ret.resolve::<ThrowRuntimeExAndDefault>()
}

///Play a placed note.
#[no_mangle]
#[allow(non_snake_case)]
pub fn Java_controllers_MIDIManager_playPlacedNote<'local>(mut unowned_env: EnvUnowned<'local>, this: JMidiManager, note: JFWSNoteEvent, voice: JVoice) {
	let _ = unowned_env.with_env(|env| -> Result<_, Error> {
		let id_name = match this.get_output_id(env) {
			Ok(id_name) => id_name.to_string(),
			Err(_) => {
				return Ok(JValue::Void);
			}
		};

		let midi_out = MidiOutput::new("FWS").unwrap();
		let port = match midi_out.find_port_by_id(id_name) {
			Some(port) => port,
			None => {
				return Ok(JValue::Void);
			}
		};

		let mut connection = match midi_out.connect(&port, &port.id()) {
			Ok(connection) => connection,
			Err(e) => {
				println!("{}", e);
				return Ok(JValue::Void);
			}
		};

		let note_note = note.note(env)? as u8;
		let note_vel = note.velocity(env)? as u8;
		let note_chan = note.channel(env)? as u8;

		let voice_prog = voice.voice(env)? as u8;
		let voice_lsb = voice.lsb(env)? as u8;
		let voice_msb = voice.msb(env)? as u8;

		let voice_msg = &[0xC0 | note_chan, voice_prog, 0];
		let lsb_msg = &[0xB0 | note_chan, 32, voice_lsb];
		let msb_msg = &[0xB0 | note_chan, 0, voice_msb];

		let _ = connection.send(msb_msg);
		let _ = connection.send(lsb_msg);
		let _ = connection.send(voice_msg);

		thread::spawn( move || {
			let note_on_msg = &[0x90 | note_chan, note_note, note_vel];
			let note_off_msg = &[0x80 | note_chan, note_note, note_vel];

			let _ = connection.send(note_on_msg);
			sleep(Duration::from_millis(500));
			let _ = connection.send(note_off_msg);

			connection.close();
		});

		Ok(JValue::Void)
	});
}

///Play a MIDI sequence.
#[no_mangle]
#[allow(non_snake_case)]
pub fn Java_controllers_MIDIManager_playSequence<'local>(mut env: EnvUnowned<'local>, this: JMidiManager<'local>, sequence: JFwsSequence<'local>) {
	let _ = env.with_env(|env| -> Result<_, Error> {
		this.play_sequence(env, sequence, Vec::new());
		Ok(JValue::Void)
	});
}

///Start recording a sequence.
#[no_mangle]
#[allow(non_snake_case)]
pub fn Java_controllers_MIDIManager_recordStart<'local>(mut env: EnvUnowned<'local>, this: JMidiManager<'local>) {
	let _ = env.with_env(|env| -> Result<_, Error> {
		this.record_start(env);
		Ok(JValue::Void)
	});
}

///Play a style.
#[no_mangle]
#[allow(non_snake_case)]
pub fn Java_controllers_MIDIManager_playStyle<'local>(mut env: EnvUnowned<'local>, this: JMidiManager<'local>, style: JFwsStyle) {
	let _ = env.with_env(|env| -> Result<_, Error> {
		this.play_style(env, style);
		Ok(JValue::Void)
	});
}

///Play a song.
#[no_mangle]
#[allow(non_snake_case)]
pub fn Java_controllers_MIDIManager_playSong<'local>(mut env: EnvUnowned<'local>, this: JMidiManager<'local>, song: JFWSSong<'local>) {
	let _ = env.with_env(|env| -> Result<_, Error> {
		this.play_song(env, song);
		Ok(JValue::Void)
	});
}

///Get MIDI events from a song.
#[no_mangle]
#[allow(non_snake_case)]
pub fn Java_controllers_MIDIManager_calculateSongMidiEvents<'local>(mut env: EnvUnowned<'local>, this: JMidiManager<'local>, song: JFWSSong<'local>, export_options: JExportOptions<'local>, midi_events: JObject, midi_ticks: JObject) {
	let _ = env.with_env(|env| -> Result<_, Error> {
		let sequence = song.get_sequence(env);
		let styles = song.get_styles(env);
		let r_midi_events = this.get_midi_events(env, export_options, sequence, styles);

		for ev in r_midi_events {
			let j_event = JByteArray::new(env, ev.0.len()).unwrap();
			let mut s_event = Vec::new();
			for d in ev.0 {
				s_event.push(d as i8);
			}

			let _ = j_event.set_region(env, 0, &s_event);
			let long = env.find_class(jni_str!("java/lang/Long")).unwrap();
			let j_tick = env.call_static_method(long, jni_str!("valueOf"), jni_sig!((jlong) -> java.lang.Long), &[JValue::Long(ev.1 as i64)]).unwrap().l().unwrap();

			let _ = env.call_method(&midi_events, jni_str!("add"), jni_sig!((JObject) -> jboolean), &[JValue::Object(&j_event)]);
			let _ = env.call_method(&midi_ticks, jni_str!("add"), jni_sig!((JObject) -> jboolean), &[JValue::Object(&j_tick)]);
		}

		Ok(JValue::Void)
	});
}