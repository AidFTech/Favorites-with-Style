package voices;

public class ProfileSubstitution {
	private final Voice original, alternate; //The original voice from the sequence instrument profile and alternate voice for the target instrument.
	private final String instrument_profile;

	public ProfileSubstitution(Voice original, Voice alternate, String profile) {
		this.original = original;
		this.alternate = alternate;
		this.instrument_profile = profile;
	}

	/** Get the original voice. */
	public Voice getOriginal() {
		return original;
	}

	/** Get the alternate voice. */
	public Voice getAlternate() {
		return alternate;
	}

	/** Get the instrument profile. */
	public String getProfile() {
		return instrument_profile;
	}
}
