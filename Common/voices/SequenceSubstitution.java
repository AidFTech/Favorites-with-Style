package voices;

import java.util.ArrayList;

public class SequenceSubstitution {
	private final Voice original;
	private Voice[] alternates = new Voice[0];

	public SequenceSubstitution(Voice original) {
		this.original = original;
	}

	public SequenceSubstitution(Voice original, Voice alternate) {
		this(original);
		Voice[] alternates = new Voice[] {alternate};
		this.setAlternates(alternates);
	}

	public SequenceSubstitution(Voice original, Voice[] alternates) {
		this(original);
		setAlternates(alternates);
	}

	public SequenceSubstitution(Voice original, ArrayList<Voice> alternates) {
		this(original);
		setAlternates(alternates);
	}

	/** Set the alternate voice list. Lower numbers take priority. */
	public void setAlternates(ArrayList<Voice> alternates) {
		Voice[] alternate_array = new Voice[alternates.size()];
		alternates.toArray(alternate_array);

		setAlternates(alternate_array);
	}

	/** Set the alternate voice list. Lower numbers take priority. */
	public void setAlternates(Voice[] alternates) {
		this.alternates = new Voice[alternates.length];
		for(int i=0;i<alternates.length;i+=1)
			this.alternates[i] = alternates[i];
	}

	/** Get the voice to be substituted. */
	public Voice getVoice() {
		return this.original;
	}

	/** Get the alternate list. */
	public Voice[] getAlternates() {
		return this.alternates;
	}
}
