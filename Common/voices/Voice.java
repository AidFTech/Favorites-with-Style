package voices;

public class Voice {
	public byte voice = 0, lsb = 0, msb = 0;
	public String name = "";

	private static final String[] gm_names = {
									"Grand Piano",
									"Bright Piano",
									"Electric Grand Piano",
									"Honky-Tonk",
									"Electric Piano 1",
									"Electric Piano 2",
									"Harpsichord",
									"Clavichord",
									"Celesta",
									"Glockenspiel",
									"Music Box",
									"Vibraphone",
									"Marimba",
									"Xylophone",
									"Tubular Bell",
									"Dulcimer",
									"Drawbar Organ",
									"Percussive Organ",
									"Rock Organ",
									"Church Organ",
									"Reed Organ",
									"Accordion",
									"Harmonica",
									"Bandoneon/Tango Accordion",
									"Nylon Guitar",
									"Steel Guitar",
									"Jazz Guitar",
									"Clean Guitar",
									"Muted Guitar",
									"Overdriven Guitar",
									"Distortion Guitar",
									"Guitar Harmonics",
									"Acoustic Bass",
									"Finger Electric Bass",
									"Pick Electric Bass",
									"Fretless Bass",
									"Slap Bass 1",
									"Slap Bass 2",
									"Synth Bass 1",
									"Synth Bass 2",
									"Violin",
									"Viola",
									"Cello",
									"Contrabass",
									"Tremolo Strings",
									"Pizzicato Strings",
									"Harp",
									"Timpani",
									"Strings 1",
									"Strings 2",
									"Synth Strings 1",
									"Synth Strings 2",
									"Choir \"Aah\"",
									"Voice \"Ooh\"",
									"Synth Voice",
									"Orchestra Hit",
									"Trumpet",
									"Trombone",
									"Tuba",
									"Muted Trumpet",
									"French Horn",
									"Brass Ensemble",
									"Synth Brass 1",
									"Synth Brass 2",
									"Soprano Sax",
									"Alto Sax",
									"Tenor Sax",
									"Baritone Sax",
									"Oboe",
									"English Horn",
									"Bassoon",
									"Clarinet",
									"Piccolo",
									"Flute",
									"Recorder",
									"Pan Flute",
									"Blown Bottle",
									"Sakuhachi",
									"Whistle",
									"Ocarina",
									"Square Lead",
									"Sawtooth Lead",
									"Calliope Lead",
									"Chiff Lead",
									"Charang Lead",
									"Voice Lead",
									"Fifth Lead",
									"Bass Lead",
									"New Age Pad",
									"Warm Pad",
									"Polysynth Pad",
									"Choir Pad",
									"Bowed Pad",
									"Metallic Pad",
									"Halo Pad",
									"Sweep Pad",
									"Raindrop",
									"Soundtrack",
									"Crystal",
									"Atmosphere",
									"Brightness",
									"Goblins",
									"Echoes",
									"Sci-Fi",
									"Sitar",
									"Banjo",
									"Shamisen",
									"Koto",
									"Kalimba/Thumb Piano",
									"Bagpipes",
									"Fiddle",
									"Shenai",
									"Tinkle Bell",
									"Agogo Bell",
									"Steel Drums",
									"Wood Block",
									"Taiko Drum",
									"Melodic Tom",
									"Synth Drum",
									"Reverse Cymbal",
									"Fret Noise",
									"Breath Noise",
									"Seashore",
									"Bird Tweet",
									"Phone Ring",
									"Helicopter",
									"Applause",
									"Gunshot"
								};

	public Voice(String new_name, byte new_program) {
		name = new_name;
		voice = new_program;
		lsb = 0;
		msb = 0;
	}
	
	public Voice(String new_name, byte new_program, byte new_lsb, byte new_msb) {
		name = new_name;
		voice = new_program;
		lsb = new_lsb;
		msb = new_msb;
	}
	
	public Voice(Voice c) {
		this.name = c.name;
		this.voice = c.voice;
		this.lsb = c.lsb;
		this.msb = c.msb;
	}

	@Override
	public String toString() {
		return this.name;
	}

	/** Return whether this voice matches the supplied data. */
	public boolean match(byte msb, byte lsb, byte program) {
		if(this.msb == msb && this.lsb == lsb && this.voice == program)
			return true;
		else
			return false;
	}

	/** Get the GM voice list. */
	public static Voice[] getGMVoices() {
		Voice[] gm_voices = new Voice[gm_names.length];
		for(int i=0;i<gm_voices.length;i+=1)
			gm_voices[i] = new Voice(gm_names[i], (byte)i, (byte)0, (byte)0);

		return gm_voices;
	}

	/** Match a voice from a list. */
	public static Voice matchVoice(Voice[] voice_list, final byte program, final byte lsb, final byte msb) {
		for(int i=0;i<voice_list.length;i+=1) {
			if(voice_list[i].match(msb, lsb, program))
				return voice_list[i];
		}

		return null;
	}
}
