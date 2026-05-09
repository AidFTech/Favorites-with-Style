use jni::bind_java_type;
use jni::Env;

extern crate jni;

bind_java_type! {
	rust_type = pub JVoice,
	java_type = voices.Voice,

	fields {
		pub voice: jbyte,
		pub lsb: jbyte,
		pub msb: jbyte,
	},
}

bind_java_type! {
	rust_type = pub JSubstitution,
	java_type = voices.SequenceSubstitution,

	fields {
		priv original: voices.Voice,
		priv alternates: voices.Voice[],
	},
}

impl<'local> JSubstitution<'local> {
	///Get the numerical voice substitution. By this point in the playback process, the substitution to use has already been calculated, so the first alternate voice is used.
	pub fn get_substitution(self, env: &mut Env<'_>) -> ((u8, u8, u8), (u8, u8, u8)) {
		let j_original = self.original(env).unwrap();
		let original = env.cast_local::<JVoice>(j_original).unwrap();

		let original_voice = original.voice(env).unwrap() as u8;
		let original_lsb = original.lsb(env).unwrap() as u8;
		let original_msb = original.msb(env).unwrap() as u8;

		let alternates = self.alternates(env).unwrap();
		if alternates.len(env).unwrap() <= 0 {
			return ((original_msb, original_lsb, original_voice), (original_msb, original_lsb, original_voice));
		}

		let j_alternate = alternates.get_element(env, 0).unwrap();
		let alternate = env.cast_local::<JVoice>(j_alternate).unwrap();

		let alternate_voice = alternate.voice(env).unwrap() as u8;
		let alternate_lsb = alternate.lsb(env).unwrap() as u8;
		let alternate_msb = alternate.msb(env).unwrap() as u8;

		return ((original_msb, original_lsb, original_voice), (alternate_msb, alternate_lsb, alternate_voice));
	}
}