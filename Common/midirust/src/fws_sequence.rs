use jni::objects::JByteArray;
use jni::{Env, bind_java_type, jni_sig, jni_str};
use jni::JValue::Int;

use crate::fws_event::{AccidentalChangeEvent, ChordChangeEvent, GenericMIDIEvent, JFWSChordEvent, JFWSKeySignatureEvent, JFWSNoteEvent, JFWSStyleEvent, JFWSTempoEvent, JFWSVoiceEvent, StyleChangeEvent, TimeChangeEvent};

extern crate jni;

bind_java_type! {
	rust_type = pub JFwsSequence,
	java_type = fwsevents.FWSSequence,

	methods {
		pub fn get_tpq {
			sig = () -> jint,
			name = "getTPQ",
		},

		pub fn get_div_type() -> jfloat,

		pub fn get_sequence_length() -> jlong,
		
		pub fn get_sequence_length_strict() -> jlong,

		pub fn get_all_channels {
			sig = () -> java.util.ArrayList,
			name = "getChannelEvents",
		},

		priv fn jget_all_events {
			sig = () -> java.util.ArrayList,
			name = "getAllEvents",
		},
	}
}

impl<'local> JFwsSequence<'local> {
	///Get all sequence events as Rust MIDI events. Truncate the listed channels.
	pub fn get_all_events(self, env: &mut Env<'_>, truncate: &[u8]) -> (Vec<TimeChangeEvent>, Vec<GenericMIDIEvent>, Vec<AccidentalChangeEvent>, Vec<ChordChangeEvent>, Vec<StyleChangeEvent>) {
		let common_events = env.call_method(&self, jni_str!("getCommonEvents"), jni_sig!(() -> java.util.ArrayList), &[]).unwrap().l().unwrap();
		let common_events_size = env.call_method(&common_events, jni_str!("size"), jni_sig!(() -> jint), &[]).unwrap().i().unwrap();

		let tpq = self.get_tpq(env).unwrap();

		let mut time_event_list = Vec::new();
		let mut key_event_list = Vec::new();

		let mut chord_event_list = Vec::new();
		let mut style_event_list = Vec::new();

		for e in 0..common_events_size {
			let j_event = env.call_method(&common_events, jni_str!("get"), jni_sig!((jint) -> JObject), &[Int(e)]).unwrap().l().unwrap();

			let tick = if env.is_instance_of(&j_event, jni_str!("fwsevents.FWSEvent")).unwrap() {
				env.get_field(&j_event, jni_str!("tick"), jni_sig!(jlong)).unwrap().j().unwrap() as u64
			} else {
				continue;
			};

			if env.is_instance_of(&j_event, jni_str!("fwsevents.FWSTempoEvent")).unwrap() {
				let tempo_event = env.cast_local::<JFWSTempoEvent>(j_event).unwrap();
				let us_per_beat = 60000000/tempo_event.tempo(env).unwrap();
				let us_per_tick = us_per_beat/tpq;

				time_event_list.push(TimeChangeEvent::new(us_per_tick as u64, tick));
			} else if env.is_instance_of(&j_event, jni_str!("fwsevents.FWSKeySignatureEvent")).unwrap() {
				let key_event = env.cast_local::<JFWSKeySignatureEvent>(j_event).unwrap();
				key_event_list.push(AccidentalChangeEvent::new(key_event.accidental_count(env).unwrap(), key_event.tick(env).unwrap() as u64))
			} else if env.is_instance_of(&j_event, jni_str!("fwsevents.FWSChordEvent")).unwrap() {
				let j_chord_event = env.cast_local::<JFWSChordEvent>(j_event).unwrap();
				chord_event_list.push(ChordChangeEvent::get(env, j_chord_event));
			} else if env.is_instance_of(&j_event, jni_str!("fwsevents.FWSStyleChangeEvent")).unwrap() {
				let style_event = env.cast_local::<JFWSStyleEvent>(j_event).unwrap();
				style_event_list.push(StyleChangeEvent::get(env, style_event));
			} 
		}
		
		let mut event_list: Vec<GenericMIDIEvent> = Vec::new();

		let j_event_list = self.jget_all_events(env).unwrap();
		let j_len = env.call_method(&j_event_list, jni_str!("size"), jni_sig!(() -> jint), &[]).unwrap().i().unwrap();

		for e in 0..j_len {
			let ev = env.call_method(&j_event_list, jni_str!("get"), jni_sig!((jint) -> JObject), &[Int(e)]).unwrap().l().unwrap();

			if !env.is_instance_of(&ev, jni_str!("fwsevents.FWSEvent")).unwrap() {
				continue;
			}

			let tick = env.get_field(&ev, jni_str!("tick"), jni_sig!(jlong)).unwrap().j().unwrap() as u64;

			let mut tick_index = -1;
			for i in 0..event_list.len() {
				if event_list[i].tick > tick {
					tick_index = i as isize;
					break;
				}
			}

			if env.is_instance_of(&ev, jni_str!("fwsevents.FWSNoteEvent")).unwrap() {
				let note_event = env.cast_local::<JFWSNoteEvent>(ev).unwrap();

				let note = note_event.note(env).unwrap() as u8;
				let channel = note_event.channel(env).unwrap() as u8;
				let velocity = note_event.velocity(env).unwrap() as u8;
				let mut duration = note_event.duration(env).unwrap() as u64;

				for c in truncate {
					if channel == *c {
						if duration > (tpq as u64)/16 {
							duration -= (tpq as u64)/16;
						} else if duration > 6 {
							duration = duration*15/16;
							if duration <= 0 {
								duration = 1;
							}
						}
					}
				}

				let note_on = GenericMIDIEvent::new([0x90 | channel, note, velocity].to_vec(), tick);
				let note_off = GenericMIDIEvent::new([0x80 | channel, note, velocity].to_vec(), tick + if duration > 0 { duration - 1 } else {duration});

				if tick_index >= 0 {
					event_list.insert(tick_index as usize, note_on);
				} else {
					event_list.push(note_on);
				}

				let mut off_tick_index = -1;
				for i in 0..event_list.len() {
					if event_list[i].tick > tick + if duration > 0 { duration - 1 } else {duration} {
						off_tick_index = i as isize;
						break;
					}
				}

				if off_tick_index >= 0 {
					event_list.insert(off_tick_index as usize, note_off);
				} else {
					event_list.push(note_off);
				}
			} else if env.is_instance_of(&ev, jni_str!("fwsevents.FWSVoiceEvent")).unwrap() {	
				let voice_event = env.cast_local::<JFWSVoiceEvent>(ev).unwrap();

				let voice_voice = voice_event.voice(env).unwrap() as u8;
				let voice_lsb = voice_event.voice_lsb(env).unwrap() as u8;
				let voice_msb = voice_event.voice_msb(env).unwrap() as u8;

				let voice_channel = voice_event.channel(env).unwrap() as u8;

				if tick_index >= 0 {
					event_list.insert(tick_index as usize, GenericMIDIEvent::new([0xC0 | voice_channel, voice_voice, 0].to_vec(), tick));
					event_list.insert(tick_index as usize, GenericMIDIEvent::new([0xB0 | voice_channel, 32, voice_lsb].to_vec(), tick));
					event_list.insert(tick_index as usize, GenericMIDIEvent::new([0xB0 | voice_channel, 0, voice_msb].to_vec(), tick));
				} else {
					event_list.push(GenericMIDIEvent::new([0xB0 | voice_channel, 0, voice_msb].to_vec(), tick));
					event_list.push(GenericMIDIEvent::new([0xB0 | voice_channel, 32, voice_lsb].to_vec(), tick));
					event_list.push(GenericMIDIEvent::new([0xC0 | voice_channel, voice_voice, 0].to_vec(), tick));
				}
			} else {
				let bytes_obj = env.call_method(&ev, jni_str!("getMIDIBytes"), jni_sig!(() -> jbyte[]), &[]).unwrap().l().unwrap();
				let jbytes = unsafe { JByteArray::from_raw(env, bytes_obj.as_raw()) };

				let mut bytes: Vec<i8> = vec![0; jbytes.len(env).unwrap()];

				let _ = jbytes.get_region(env, 0, &mut bytes);

				let mut midi_ev = GenericMIDIEvent::new(Vec::new(), tick);
				for i in bytes {
					midi_ev.data.push(i as u8);
				}

				if tick_index >= 0 {
					event_list.insert(tick_index as usize, midi_ev);
				} else {
					event_list.push(midi_ev);
				}
			}
		}

		return (time_event_list, event_list, key_event_list, chord_event_list, style_event_list);
	}
}