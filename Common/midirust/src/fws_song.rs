use jni::bind_java_type;
use jni::Env;
use jni::objects::JString;

use crate::fws_sequence::JFwsSequence;
use crate::fws_style::FwsStyle;
use crate::fws_style::JFwsStyle;

bind_java_type! {
	rust_type = pub JFWSSong,
	java_type = song.FWSSong,

	fields {
		priv song_sequence {
			sig = fwsevents.FWSSequence,
			name = "song_sequence",
		}
	},

	methods {
		pub fn get_style_names() -> JString[],
		pub fn get_style(JString) -> style.Style,
	},
}

impl<'local> JFWSSong<'local> {
	///Get all styles from the song.
	pub fn get_styles(&self, env: &mut Env<'_>) -> Vec<FwsStyle> {
		let j_style_names = self.get_style_names(env).unwrap();
		let mut style_names = Vec::new();

		for i in 0..j_style_names.len(env).unwrap() {
			let j_style_name = j_style_names.get_element(env, i).unwrap();
			let style_name = j_style_name.to_string();
			style_names.push(style_name);
		}

		let mut styles = Vec::new();
		for s in style_names {
			let j_style_name = JString::new(env, &s).unwrap();
			let j_style_obj = self.get_style(env, j_style_name).unwrap();
			let j_style = env.cast_local::<JFwsStyle>(j_style_obj).unwrap();
			styles.push(FwsStyle::get(env, j_style));
		}

		return styles;
	}

	///Get the song sequence.
	pub fn get_sequence(&self, env: &mut Env<'local>) -> JFwsSequence<'local> {
		let j_sequence_obj = self.song_sequence(env).unwrap();
		let j_sequence = env.cast_local::<JFwsSequence>(j_sequence_obj).unwrap();

		return j_sequence;
	}
}