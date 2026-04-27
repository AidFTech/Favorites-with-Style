use jni::bind_java_type;

extern crate jni;

bind_java_type! {
	rust_type = pub JMidiDevice,
	java_type = "javax.sound.midi.MidiDevice$Info",

	constructors {
		pub fn new(name: JString, vendor: JString, description: JString, version: JString),
	},

	methods {
		pub fn get_name() -> JString,
		pub fn get_vendor() -> JString,
		pub fn get_description() -> JString,
		pub fn get_version() -> JString,
	},

	fields {
		name: JString,
		vendor: JString,
		description: JString,
		version: JString,
	},
}