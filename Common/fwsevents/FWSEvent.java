package fwsevents;

import java.util.ArrayList;

import javax.sound.midi.MidiEvent;

public abstract class FWSEvent {
	public long tick = 0;

	public FWSEvent() {

	}

	public FWSEvent(final FWSEvent c) {
		this.tick = c.tick;
	}

	/** Get the MIDI bytes from this event. */
	public byte[] getMIDIBytes() {
		return new byte[0];
	}

	/** Get true MIDI events from this event. */
	public MidiEvent[] getMIDIEvents() {
		return new MidiEvent[0];
	}

	/** Return a copy of an event list. */
	public static FWSEvent[] createCopy(FWSEvent[] ev_list) {
		FWSEvent[] copies = new FWSEvent[ev_list.length];

		for(int i=0;i<ev_list.length;i+=1) {
			if(ev_list[i] instanceof FWSNoteEvent)
				copies[i] = new FWSNoteEvent((FWSNoteEvent)ev_list[i]);
			else if(ev_list[i] instanceof FWSChordEvent)
				copies[i] = new FWSChordEvent((FWSChordEvent)ev_list[i]);
			else if(ev_list[i] instanceof FWSKeySignatureEvent)
				copies[i] = new FWSKeySignatureEvent((FWSKeySignatureEvent)ev_list[i]);
			else if(ev_list[i] instanceof FWSMiscMIDIEvent)
				copies[i] = new FWSMiscMIDIEvent((FWSMiscMIDIEvent)ev_list[i]);
			else if(ev_list[i] instanceof FWSSectionNameEvent)
				copies[i] = new FWSSectionNameEvent((FWSSectionNameEvent)ev_list[i]);
			else if(ev_list[i] instanceof FWSStyleChangeEvent)
				copies[i] = new FWSStyleChangeEvent((FWSStyleChangeEvent)ev_list[i]);
			else if(ev_list[i] instanceof FWSShortEvent)
				copies[i] = new FWSShortEvent((FWSShortEvent)ev_list[i]);
			else if(ev_list[i] instanceof FWSSysexEvent)
				copies[i] = new FWSSysexEvent((FWSSysexEvent)ev_list[i]);
			else if(ev_list[i] instanceof FWSTempoEvent)
				copies[i] = new FWSTempoEvent((FWSTempoEvent)ev_list[i]);
			else if(ev_list[i] instanceof FWSTimeSignatureEvent)
				copies[i] = new FWSTimeSignatureEvent((FWSTimeSignatureEvent)ev_list[i]);
			else if(ev_list[i] instanceof FWSVoiceEvent)
				copies[i] = new FWSVoiceEvent((FWSVoiceEvent)ev_list[i]);
		}

		return copies;
	}

	/** Return a copy of an event list with a tick shift. */
	public static FWSEvent[] createCopy(FWSEvent[] ev_list, final long shift) {
		FWSEvent[] copies = createCopy(ev_list);
		for(int i=0;i<copies.length;i+=1)
			copies[i].tick += shift;
		return copies;
	}

	/** Return a copy of an event list. */
	public static ArrayList<FWSEvent> createCopy(ArrayList<FWSEvent> ev_list) {
		ArrayList<FWSEvent> copies = new ArrayList<FWSEvent>(0);

		for(int i=0;i<ev_list.size();i+=1) {
			if(ev_list.get(i) instanceof FWSNoteEvent)
				copies.add(new FWSNoteEvent((FWSNoteEvent)ev_list.get(i)));
			else if(ev_list.get(i) instanceof FWSKeySignatureEvent)
				copies.add(new FWSKeySignatureEvent((FWSKeySignatureEvent)ev_list.get(i)));
			else if(ev_list.get(i) instanceof FWSMiscMIDIEvent)
				copies.add(new FWSMiscMIDIEvent((FWSMiscMIDIEvent)ev_list.get(i)));
			else if(ev_list.get(i) instanceof FWSSectionNameEvent)
				copies.add(new FWSSectionNameEvent((FWSSectionNameEvent)ev_list.get(i)));
			else if(ev_list.get(i) instanceof FWSStyleChangeEvent)
				copies.add(new FWSStyleChangeEvent((FWSStyleChangeEvent)ev_list.get(i)));
			else if(ev_list.get(i) instanceof FWSChordEvent)
				copies.add(new FWSChordEvent((FWSChordEvent)ev_list.get(i)));
			else if(ev_list.get(i) instanceof FWSShortEvent)
				copies.add(new FWSShortEvent((FWSShortEvent)ev_list.get(i)));
			else if(ev_list.get(i) instanceof FWSSysexEvent)
				copies.add(new FWSSysexEvent((FWSSysexEvent)ev_list.get(i)));
			else if(ev_list.get(i) instanceof FWSTempoEvent)
				copies.add(new FWSTempoEvent((FWSTempoEvent)ev_list.get(i)));
			else if(ev_list.get(i) instanceof FWSTimeSignatureEvent)
				copies.add(new FWSTimeSignatureEvent((FWSTimeSignatureEvent)ev_list.get(i)));
			else if(ev_list.get(i) instanceof FWSVoiceEvent)
				copies.add(new FWSVoiceEvent((FWSVoiceEvent)ev_list.get(i)));
		}

		return copies;
	}

	/** Insert an event into a list, sorted by tick. */
	public static void insertEvent(ArrayList<FWSEvent> event_list, FWSEvent event) {
		int index = -1;
		for(int i=0;i<event_list.size();i+=1) {
			if(event_list.get(i).tick > event.tick) {
				index = i;
				break;
			}
		}

		if(index >= 0)
			event_list.add(index, event);
		else
			event_list.add(event);
	}
}
