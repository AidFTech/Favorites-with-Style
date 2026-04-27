package fwsevents;

import java.util.ArrayList;

public class FWSStyleChangeEvent extends FWSEvent {
	public String style_name = "", section_name = "";
	public long style_tick = -1;

	public boolean sub_rhythm = true, rhythm = true, bass = true, chord1 = true, chord2 = true, pad = true, phrase1 = true, phrase2 = true;

	public FWSStyleChangeEvent() {

	}

	public FWSStyleChangeEvent(FWSStyleChangeEvent c) {
		super(c);
		this.style_name = c.style_name;
		this.section_name = c.section_name;
		this.style_tick = c.style_tick;

		this.sub_rhythm = c.sub_rhythm;
		this.rhythm = c.rhythm;
		this.bass = c.bass;
		this.chord1 = c.chord1;
		this.chord2 = c.chord2;
		this.pad = c.pad;
		this.phrase1 = c.phrase1;
		this.phrase2 = c.phrase2;
	}

	@Override
	public String toString() {
		if(style_name.length() > 0) {
			String desc = style_name;
			if(section_name.length() > 0)
				desc += ": " + section_name;
			desc += '\n';
			
			if(style_tick >= 0)
				desc += "Tick: " + style_tick;
			else
				desc += "Retain Tick";
			desc += '\n';

			ArrayList<String> parts = new ArrayList<>();
			if(sub_rhythm)
				parts.add("Sub-Rhythm");
			if(rhythm)
				parts.add("Main Rhythm");
			if(bass)
				parts.add("Bass");
			if(chord1)
				parts.add("Chord 1");
			if(chord2)
				parts.add("Chord 2");
			if(pad)
				parts.add("Pad");
			if(phrase1)
				parts.add("Phrase 1");
			if(phrase2)
				parts.add("Phrase 2");

			for(int i=0;i<parts.size();i+=1) {
				desc += parts.get(i);
				if(i<parts.size()-1)
					desc += ", ";
			}

			return desc;
		} else return "Accompaniment Off";
	}

	/** Get the sections represented as a byte. */
	public byte getSections() {
		byte section_byte = 0;

		section_byte |= sub_rhythm ? (byte)0x80 : 0;
		section_byte |= rhythm ? (byte)0x40 : 0;
		section_byte |= bass ? (byte)0x20 : 0;
		section_byte |= chord1 ? (byte)0x10 : 0;
		section_byte |= chord2 ? (byte)0x8 : 0;
		section_byte |= pad ? (byte)0x4 : 0;
		section_byte |= phrase1 ? (byte)0x2 : 0;
		section_byte |= phrase2 ? (byte)0x1 : 0;

		return section_byte;
	}

	/** Set the sections from a byte. */
	public void setSections(final byte sections) {
		sub_rhythm = ((sections&0xFF)&0x80) != 0;
		rhythm = ((sections&0xFF)&0x40) != 0;
		bass = ((sections&0xFF)&0x20) != 0;
		chord1 = ((sections&0xFF)&0x10) != 0;
		chord2 = ((sections&0xFF)&0x8) != 0;
		pad = ((sections&0xFF)&0x4) != 0;
		phrase1 = ((sections&0xFF)&0x2) != 0;
		phrase2 = ((sections&0xFF)&0x1) != 0;
	}
}
