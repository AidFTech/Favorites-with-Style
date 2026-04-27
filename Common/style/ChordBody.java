package style;

import java.util.ArrayList;
import java.util.Arrays;

import fwsevents.FWSNoteEvent;

public class ChordBody {
	private String name;
	private byte root, root_note, chord;
	private byte[] notes = new byte[0];

	private final static String[] CHORD_TREE = {"", "6", " M7", " M7(♯11)", "(9)", " M7(9)", "6(9)", "aug", "m", "m6", "m7", "m7♭5",
										"m(9)", "m7(9)", "m7(11)", "mM7", "mM7(9)", "dim", "dim7", "7", "7sus4", "7♭5", "7(9)", "7(♯11)",
										"7(13)", "7(♭9)", "7(♭13)", "7(♯9)", " M7aug", "7aug", "(1+8)", "(1+5)" , "sus4", "sus2",
										"", " M7♭5", "♭5", "mM7♭5", "?"};

	public static final byte[][] CHORD_FINGERING = {{0, 4, 7},
													{0, 4, 7, 9},
													{0, 4, 7, 11},
													{0, 4, 6, 7, 11},
													{0, 2, 4, 7},
													{0, 2, 4, 7, 11},
													{0, 2, 4, 7, 9},
													{0, 4, 8},
													{0, 3, 7},
													{0, 3, 7, 9},
													{0, 3, 7, 10},
													{0, 3, 6, 10},
													{0, 2, 3, 7},
													{0, 2, 3, 7, 10},
													{0, 3, 5, 7, 10},
													{0, 3, 7, 11},
													{0, 2, 3, 7, 11},
													{0, 3, 6},
													{0, 3, 6, 9},
													{0, 4, 7, 10},
													{0, 5, 7, 10},
													{0, 4, 6, 10},
													{0, 2, 4, 7, 10},
													{0, 4, 6, 7, 10},
													{0, 4, 7, 9, 10},
													{0, 1, 4, 7, 10},
													{0, 4, 7, 8, 10},
													{0, 3, 4, 7, 10},
													{0, 4, 8, 11},
													{0, 4, 8, 10},
													{0, 12},
													{0, 7},
													{0, 5, 7},
													{0, 2, 7},
													{},
													{0, 4, 6, 11},
													{0, 4, 6},
													{0, 3, 6, 11}};

	public ChordBody() {
		root = (byte)0x7F;
		root_note = -1;
		chord = (byte)0x22;
		refresh();
	}

	public ChordBody(byte newroot, byte newchord) {
		this.root = newroot;
		this.chord = newchord;
		this.root_note = -1;
		refresh();
	}

	public ChordBody(ChordBody c) {
		this.root = c.root;
		this.root_note = c.root_note;
		this.chord = c.chord;
		refresh();
	}

	/** Refresh the chord params. */
	private void refresh() {
		name = "";

		if(root == 0x7F)
			return;

		switch(root&0xF) {
		case 1:
			name = "C";
			root_note = 0;
			break;
		case 2:
			name = "D";
			root_note = 2;
			break;
		case 3:
			name = "E";
			root_note = 4;
			break;
		case 4:
			name = "F";
			root_note = 5;
			break;
		case 5:
			name = "G";
			root_note = 7;
			break;
		case 6:
			name = "A";
			root_note = 9;
			break;
		case 7:
			name = "B";
			root_note = 11;
			break;
		default:
			name = "";
			root_note = -1;
			return;
		}
		
		if((root&0xF0)<0x30) {
			//Flat
			root_note -= (0x30-(root&0xF0))/0x10;
			for(int i=0;i<(0x30-(root&0xF0))/0x10;i+=1)
				name += '♭';
		} else if((root&0xF0)>0x30) {
			//Sharp
			root_note += ((root&0xF0)-0x30)/0x10;
			for(int i=0;i<((root&0xF0)-0x30)/0x10;i+=1)
				name += '♯';
		}
		
		root_note = (byte)((root_note&0xFF)%12);

		ArrayList<Byte> notes = new ArrayList<>();
		for(int n=0;n<CHORD_FINGERING[chord].length;n+=1) {
			if(CHORD_FINGERING[chord][n] >= 0)
				notes.add(CHORD_FINGERING[chord][n]);
		}

		this.notes = new byte[notes.size()];
		for(int i=0;i<this.notes.length;i+=1)
			this.notes[i] = notes.get(i);

		name += CHORD_TREE[chord];
	}

	/** Get the chord name. */
	public String getName() {
		return this.name;
	}

	/** Get the full root. */
	public byte getFullRoot() {
		return root;
	}

	/** Get the quantifiable root. */
	public byte getRoot() {
		return root_note;
	}

	/** Get the chord. */
	public byte getChord() {
		return chord;
	}

	/** Return whether the chord is null. */
	public boolean getNoChord() {
		return root == 0x7F;
	}

	/** Get the notes down. */
	public byte[] getNotesDown() {
		byte[] notes = new byte[this.notes.length];
		for(int i=0;i<notes.length;i+=1)
			notes[i] = this.notes[i];

		return notes;
	}

	/** Get the number of possible inversions in a chord. */
	public int getInversionCount() {
		int inv = 0;

		switch(this.chord) {
		case 0x1:
		case 0x6:
		case 0x7:
		case 0x9:
		case 0x12:
		case 0x20:
		case 0x21:
		case 0x1E:
			inv = 0;
			break;
		case 0x15:
			inv = 1;
			break;
		case 0x0A:
		case 0x0B:
			inv = 2;
			break;
		case 0x0E:
			inv = 3;
			break;
		default:
			inv = notes.length-1 >= 0 ? notes.length - 1 : 0;
			break;
		}

		return inv;
	}

	/** Return whether the provided notes are valid. */
	public boolean getValid(byte[] notes_in) {
		if(notes_in.length <= 0)
			return false;

		for(int s=0;s<notes_in.length;s+=1) {
			byte[] test_notes = shift(notes_in, s);
			final byte root = test_notes[0];

			for(int i=0;i<test_notes.length;i+=1)
				test_notes[i] = (byte)((test_notes[i]-root+12)%12);

			//Check if the inverted chord is valid for something else.
			for(int c=0;c<CHORD_FINGERING.length;c+=1) {
				if(CHORD_FINGERING[c].length != test_notes.length)
					continue;

				boolean chord_match = true;
				for(int n=0;n<CHORD_FINGERING[c].length;n+=1) {
					if(test_notes[n] != CHORD_FINGERING[c][n]) {
						chord_match = false;
						break;
					}
				}

				if(chord_match)
					return c == this.chord && root%12 == this.root_note;
			}
		}
		return false;
	}

	/** Shift the chord notes. */
	public static byte[] shift(byte[] x, final int conv) {
		final int l = x.length;
		int start = -conv;
		while(start < 0)
			start += l;
		start = start%l;

		byte[] ans = new byte[l];

		for(int i=0;i<l;i+=1) {
			final int j = (start + i)%l;
			ans[j] = x[i];
		}

		return ans;
	}

	/** Get the inversion notes. Use -1 as an argument for the default inversion. */
	public byte[] getInversion(final int inversion, final int split_point) {
		byte[] notes = this.getNotesDown();
		final byte root = root_note;

		boolean chord_set = false;

		final int start_note = FWSNoteEvent.low_c;

		if(inversion < 0) {
			for(int s=0;s<notes.length;s+=1) {
				byte[] notes_down = new byte[notes.length];
				for(int i=0;i<notes.length;i+=1)
					notes_down[i] = (byte)(notes[i]+root+start_note);

				byte[] test_notes = ChordBody.shift(notes_down, -s);
				for(int i=0;i<test_notes.length;i+=1) {
					if(test_notes[i] > root+start_note && i < s)
						test_notes[i] -= 12;
				}
				
				{
					boolean move_up = false;
					for(int i=0;i<test_notes.length;i+=1) {
						if(test_notes[i] < start_note) {
							move_up = true;
						}
					}
					
					if(move_up) {
						for(int i=0;i<test_notes.length;i+=1)
							test_notes[i] += 12;
					}
				}

				if(getValid(test_notes)) {
					boolean use = true;
					for(int i=0;i<test_notes.length;i+=1) {
						if(test_notes[i] > split_point) {
							use = false;
							break;
						}
					}

					if(use) {
						chord_set = true;
						for(int i=0;i<test_notes.length;i+=1)
							notes[i] = test_notes[i];
						break;
					}
				}
			}
		} else {
			final int s = inversion;
			byte[] notes_down = new byte[notes.length];
			for(int i=0;i<notes.length;i+=1)
				notes_down[i] = (byte)(notes[i]+root+start_note);

			int inv = 0, active_inv = inv;
			while(inv <= s) {
				byte[] test_notes = Arrays.copyOf(notes_down, notes_down.length);
				
				for(int i=0;i<test_notes.length && i < active_inv;i+=1)
					test_notes[i] += 12;
				
				test_notes = ChordBody.shift(test_notes, active_inv);
				
				{
					boolean move_down = true;
					for(int i=0;i<test_notes.length;i+=1) {
						if(root + (test_notes[i] - start_note) < 12) {
							move_down = false;
							break;
						}
					}
					
					if(move_down) {
						for(int i=0;i<test_notes.length;i+=1)
							test_notes[i] -= 12;
					}
				}

				if(getValid(test_notes))
					inv += 1;
				active_inv += 1;

				if(inv > s) {
					boolean shift_up = false;
					
					do {
						shift_up = false;
						for(int i=0;i<test_notes.length;i+=1) {
							if(test_notes[i] < start_note) {
								shift_up = true;
								break;
							}
						}

						if(shift_up) {
							for(int i=0;i<test_notes.length;i+=1)
								test_notes[i] += 12;
						}
					} while(shift_up);

					chord_set = true;
					for(int i=0;i<test_notes.length;i+=1)
						notes[i] = test_notes[i];
				}
			}
		}
		
		if(!chord_set) {
			for(int i=0;i<notes.length;i+=1)
				notes[i] += root + start_note;
		}

		return notes;
	}

	/** Set the root. */
	public void setRoot(final byte root) {
		this.root = root;
		refresh();
	}

	/** Set the chord. */
	public void setChord(final byte chord) {
		this.chord = chord;
	}
}
