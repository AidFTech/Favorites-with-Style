use jni::bind_java_type;

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