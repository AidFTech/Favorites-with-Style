package canvas;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;

import javax.sound.midi.ShortMessage;
import javax.swing.AbstractButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.Timer;

import controllers.FWS;
import controllers.FWSEditor;
import controllers.MIDIManager;
import event_dialogs.MultiEventDialog;
import event_dialogs.MultiNoteEventDialog;
import event_dialogs.MultiShortEventDialog;
import fwsevents.FWSChordEvent;
import fwsevents.FWSEvent;
import fwsevents.FWSKeySignatureEvent;
import fwsevents.FWSMiscMIDIEvent;
import fwsevents.FWSNoteEvent;
import fwsevents.FWSSectionNameEvent;
import fwsevents.FWSSequence;
import fwsevents.FWSShortEvent;
import fwsevents.FWSStyleChangeEvent;
import fwsevents.FWSSysexEvent;
import fwsevents.FWSTempoEvent;
import fwsevents.FWSTimeSignatureEvent;
import fwsevents.FWSVoiceEvent;
import infobox.InfoBox;
import main_window.FWSEditorMainWindow;
import main_window.NoteToggleButton;
import main_window.FWSEditorMainWindow.DisplayMode;
import options.MIDIPlayerOptions;
import options.SelectionOptions;
import sprites.Sprite;
import sprites.SpriteChordEvent;
import sprites.SpriteKeyEvent;
import sprites.SpriteMiscMIDIEvent;
import sprites.SpriteNoteEvent;
import sprites.SpriteSectionNameEvent;
import sprites.SpriteShortEvent;
import sprites.SpriteStyleEvent;
import sprites.SpriteSysexEvent;
import sprites.SpriteTempoEvent;
import sprites.SpriteTextEvent;
import sprites.SpriteTimeEvent;
import sprites.SpriteVoiceEvent;
import style.Style;
import tools.PasteTickWindow;
import voices.Voice;

public class SongViewPort extends JScrollPane {
	private static final long serialVersionUID = -3090378454256440114L;

	private FWSEditor controller;
	private FWSEditorMainWindow main_window;
	private FWSSequence active_sequence;
	protected InfoBox info_box;

	private boolean sprites_locked = false, playing = false;

	private int channel_selected = 0, snap;
	
	private SongPanelStyleHeader style_header;
	private SongPanelPianoHeader piano_header;
	private SongPanelPianoRoll piano_roll;

	private JPopupMenu popup_menu;
	private Component last_popup; //The last component to trigger the popup menu.

	private int last_popup_x = -1;

	private ArrayList<FWSEvent> clipboard = new ArrayList<>();
	private int clipboard_tpq = 192;

	private CanvasOptionGroup canvas_options;
	private SelectionOptions selection_options = new SelectionOptions();

	public SongViewPort(FWSEditor controller, FWSEditorMainWindow main_window, int x, int y, Dimension d) {
		super();
		
		this.controller = controller;
		this.main_window = main_window;

		this.setPreferredSize(d);
		this.setSize(d);
		this.setBounds(x, y, d.width, d.height);
		
		this.init();

		this.active_sequence = controller.getActiveSequence();
		if(this.active_sequence != null)
			this.snap = active_sequence.getTPQ();

		Timer refresh_timer = new Timer(40, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MIDIPlayerOptions player_options = controller.getMidiManager().getPlayerOptions();
				
				final boolean last_playing = playing;

				sprites_locked = player_options.play;
				playing = player_options.play;

				if(playing != last_playing) {
					Sprite[] sprites = getSprites();
					for(Sprite sprite: sprites)
						sprite.deselect();
				}
				
				if(player_options.play && main_window.getDisplayMode() != DisplayMode.DISPLAY_MODE_STYLE) {
					final long tick = player_options.current_tick, length = controller.getActiveSequence().getSequenceLength();
					JScrollBar scrh = getHorizontalScrollBar();

					final int maximum = scrh.getMaximum(), minimum = scrh.getMinimum(), range = maximum-minimum;
					final double ratio = (double)tick/length;

					scrh.setValue(minimum + (int)(range*ratio) - getWidth()/2);
				}
			}
		});
		refresh_timer.start();
	}

	/** Initialize the viewport. */
	private void init() {
		JViewport style_header_vp = new JViewport();
		style_header = new SongPanelStyleHeader(controller, this);
		style_header_vp.setView(style_header);
		this.setColumnHeader(style_header_vp);
		
		JViewport piano_header_vp = new JViewport();
		piano_header = new SongPanelPianoHeader();
		piano_header_vp.setView(piano_header);
		this.setRowHeader(piano_header_vp);
		
		piano_roll = new SongPanelPianoRoll(controller, this);
		piano_roll.setHead(piano_header);
		JViewport main_vp = new JViewport();
		main_vp.setView(piano_roll);
		this.setViewport(main_vp);

		//Popup menu:
		popup_menu = new JPopupMenu();

		JMenuItem menu_item_cut = new JMenuItem("Cut");
		menu_item_cut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copySelected();
				deleteSelectedSprites();
				refresh();
			}
		});
		popup_menu.add(menu_item_cut);

		JMenuItem menu_item_copy = new JMenuItem("Copy");
		menu_item_copy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copySelected();
			}
		});
		popup_menu.add(menu_item_copy);

		JMenuItem menu_item_paste = new JMenuItem("Paste");
		popup_menu.add(menu_item_paste);
		menu_item_paste.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pasteClipboard();
			}
		});
		popup_menu.addSeparator();

		JMenuItem menu_item_delete = new JMenuItem("Delete");
		menu_item_delete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteSelectedSprites();
				refresh();
			}
		});
		popup_menu.add(menu_item_delete);

		popup_menu.addSeparator();

		JMenuItem menu_item_properties = new JMenuItem("Properties");
		menu_item_properties.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openProperties(true);
			}
		});
		popup_menu.add(menu_item_properties);
	}

	/** Set the information display. */
	public void setInfoBox(InfoBox infobox) {
		this.info_box = infobox;
	}

	/** Set the option objects. */
	public void setOptionGroup(CanvasOptionGroup canvas_options) {
		this.canvas_options = canvas_options;

		for(Enumeration<AbstractButton> en = canvas_options.note_place_group.getElements(); en.hasMoreElements();) {
			en.nextElement().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final boolean last_lock = sprites_locked;
					sprites_locked = canvas_options.note_place_group.getSelectedToggle() != null;

					if(!last_lock && sprites_locked) {
						Sprite[] sprites = getSprites();
						for(int i=0;i<sprites.length;i+=1) {
							if(sprites[i].getSelected())
								sprites[i].deselect();
						}
					}
					
					main_window.requestFocus();
				}
			});
		}
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		this.setBackground(Color.WHITE);
		
		g.setFont(new Font("default",Font.PLAIN,12));
		g.setColor(Color.BLACK);
		g.drawLine(0, style_header.getHeight() - 1, piano_header.getWidth() - 1, style_header.getHeight() - 1);
		g.drawLine(piano_header.getWidth() - 1, 0, piano_header.getWidth() - 1, style_header.getHeight() - 1);
		g.setColor(Color.RED);
		if(main_window.getDisplayMode() == DisplayMode.DISPLAY_MODE_SONG) {
			g.drawString("Chord", 5, (int)(FWS.event_height*0.75));
			g.drawString("Style", 5, (int)(FWS.event_height*0.75) + FWS.event_height*1);
		} else if(main_window.getDisplayMode() == DisplayMode.DISPLAY_MODE_STYLE) {
			g.drawString("Section", 5, (int)(FWS.event_height*0.75));
		}
		g.setColor(Color.BLACK);
		g.drawString("Tempo", 5, (int)(FWS.event_height*0.75) + FWS.event_height*3);
		g.drawString("Time Sig.", 5, (int)(FWS.event_height*0.75) + FWS.event_height*4);
		g.drawString("Key Sig.", 5, (int)(FWS.event_height*0.75) + FWS.event_height*5);
		g.drawString("MIDI Text", 5, (int)(FWS.event_height*0.75) + FWS.event_height*6);
		g.drawString("SysEx", 5, (int)(FWS.event_height*0.75) + FWS.event_height*7);
		g.drawString("Other", 5, (int)(FWS.event_height*0.75) + FWS.event_height*8);
		g.drawString("Measure", 5, (int)(FWS.event_height*0.75) + FWS.event_height*9);
	}
	
	/** Change the size of the roll. */
	public void resize(int x, int y, Dimension d) {
		this.setPreferredSize(d);
		this.setSize(d);
		this.setBounds(x, y, d.width, d.height);
		this.revalidate();
	}

	/** Copy the selected events to the clipboard. */
	public void copySelected() {
		copySelected(true);
	}

	/** Copy the selected events to the clipboard. */
	public void copySelected(final boolean include_last_selected) {
		clipboard.clear();

		this.clipboard_tpq = active_sequence.getTPQ();

		Sprite[] sprites = getSprites();
		ArrayList<Sprite> selected = new ArrayList<>();
		
		for(int i=0;i<sprites.length;i+=1) {
			if(sprites[i].getSelected())
				selected.add(sprites[i]);
		}

		if(selected.size() <= 0 && include_last_selected) {
			if(last_popup instanceof Sprite)
				selected.add((Sprite)last_popup);
		}

		for(Sprite sprite: selected) {
			FWSEvent affected_event = sprite.getEvent();
			clipboard.add(affected_event);
		}
	}

	/** Paste the copied events with a window to specify tick. */
	public void pasteClipboard() {
		long start_tick = 0;
		if(last_popup instanceof Sprite)
			start_tick = ((Sprite)last_popup).getEvent().tick;
		else if(last_popup_x >= 0) {
			final int ppq = controller.getGlobalOptions().ppq;
			start_tick = active_sequence.getXTime(last_popup_x, ppq)/snap*snap;
		}

		PasteTickWindow paste_window = new PasteTickWindow(main_window, start_tick);
		if(paste_window.getSetTick() >= 0)
			pasteClipboard(paste_window.getSetTick());
	}

	/** Paste the copied events at the specified tick. */
	public void pasteClipboard(final long start_tick) {
		if(clipboard.size() <= 0)
			return;

		long event_start = clipboard.get(0).tick;
		for(FWSEvent event: clipboard) {
			if(event.tick < event_start)
				event_start = event.tick;
		}

		event_start = event_start*active_sequence.getTPQ()/clipboard_tpq;
		ArrayList<FWSEvent> new_clipboard = FWSEvent.createCopy(clipboard);

		for(FWSEvent event: new_clipboard) {
			event.tick = event.tick*active_sequence.getTPQ()/clipboard_tpq;
			
			if(event instanceof FWSNoteEvent) {
				FWSNoteEvent note_event = (FWSNoteEvent)event;
				note_event.duration = note_event.duration*active_sequence.getTPQ()/clipboard_tpq;
			}

			event.tick -= event_start;
			event.tick += start_tick;
		}

		for(FWSEvent event: new_clipboard) {
			active_sequence.addEvent(event);
			this.addSprite(event);
		}
	}

	/** Open the Properties window. */
	public void openProperties(final boolean include_last_selected) {
		Sprite[] sprites = getSprites();
		ArrayList<Sprite> selected = new ArrayList<>();
		
		for(int i=0;i<sprites.length;i+=1) {
			if(sprites[i].getSelected())
				selected.add(sprites[i]);
		}

		if(selected.size() <= 0 && include_last_selected) {
			if(last_popup instanceof Sprite)
				selected.add((Sprite)last_popup);
		}

		if(selected.size() == 1) {
			Sprite sprite = selected.get(0);
			sprite.createDialog();
		} else if(selected.size() > 1) {
			Class<?> selection_class = selected.get(0).getClass();
			boolean generic = false, channeled = true;;

			for(int i=0;i<selected.size();i+=1) {
				if(selected.get(i).getClass() != selection_class) {
					generic = true;
					break;
				}
			}

			for(int i=0;i<selected.size();i+=1) {
				if(!(selected.get(i) instanceof SpriteNoteEvent) && !(selected.get(i) instanceof SpriteShortEvent) && !(selected.get(i) instanceof SpriteVoiceEvent)) {
					channeled = false;
					break;
				}
			}

			if(!generic) {
				if(selection_class == SpriteNoteEvent.class) {
					FWSNoteEvent[] notes = new FWSNoteEvent[selected.size()];
					for(int i=0;i<notes.length;i+=1) {
						FWSNoteEvent note = (FWSNoteEvent)selected.get(i).getEvent();
						notes[i] = note;
					}
					new MultiNoteEventDialog(main_window, notes);
				} else if(selection_class == SpriteShortEvent.class) {
					FWSEvent[] shorts = new FWSEvent[selected.size()];
					for(int i=0;i<shorts.length;i+=1)
						shorts[i] = selected.get(i).getEvent();

					new MultiShortEventDialog(main_window, shorts);
				} else if(selection_class == SpriteVoiceEvent.class) {
					FWSEvent[] shorts = new FWSEvent[selected.size()];
					for(int i=0;i<shorts.length;i+=1)
						shorts[i] = selected.get(i).getEvent();

					new MultiShortEventDialog(main_window, shorts);
				} else {
					FWSEvent[] events = new FWSEvent[selected.size()];
					for(int i=0;i<events.length;i+=1)
						events[i] = selected.get(i).getEvent();

					new MultiEventDialog(main_window, events);
				}
			} else if(channeled) {
				FWSEvent[] shorts = new FWSEvent[selected.size()];
				for(int i=0;i<shorts.length;i+=1)
					shorts[i] = selected.get(i).getEvent();

				new MultiShortEventDialog(main_window, shorts);
			} else {
				FWSEvent[] events = new FWSEvent[selected.size()];
				for(int i=0;i<events.length;i+=1)
					events[i] = selected.get(i).getEvent();

				new MultiEventDialog(main_window, events);
			}
		}
	}

	/** Get the selection options. */
	public SelectionOptions getSelectionOptions() {
		return this.selection_options;
	}

	/** Select events from a click and drag. */
	public void selectDraggedChannelEvents(final int start_y, final int h, final long start_tick, final long end_tick) {
		if(sprites_locked)
			return;

		ArrayList<FWSEvent> sequence_events = active_sequence.getChannelEvents();

		boolean[] allowed_channels = Arrays.copyOf(selection_options.allowed_channels, selection_options.allowed_channels.length);
		if(selection_options.allowed_active) {
			for(int i=0;i<allowed_channels.length;i+=1)
				allowed_channels[i] = false;

			allowed_channels[channel_selected] = true;
		}

		if(start_y < SongPanelPianoRoll.voice_y) { //Note selection.
			for(FWSEvent event: sequence_events) {
				if(!(event instanceof FWSNoteEvent))
					continue;

				FWSNoteEvent note_event = (FWSNoteEvent)event;

				final byte upper_note = (byte)(127-start_y/FWS.event_height), lower_note = (byte)(128-(start_y+h)/FWS.event_height);

				if(note_event.note < lower_note || note_event.note > upper_note)
					continue;

				if(note_event.channel >= 0 && note_event.channel < allowed_channels.length && !allowed_channels[note_event.channel])
					continue;

				if(note_event.tick < start_tick)
					continue;

				if(note_event.tick >= end_tick)
					continue;

				getEventSprite(note_event).select();
			}
		} else { //Short selection.
			for(FWSEvent event: sequence_events) {
				if(!(event instanceof FWSShortEvent) && !(event instanceof FWSVoiceEvent))
					continue;

				if(event instanceof FWSShortEvent) {
					if(start_y + h < SongPanelPianoRoll.voice_y)
						continue;

					FWSShortEvent short_event = (FWSShortEvent)event;
					if(short_event.channel >= 0 && short_event.channel < allowed_channels.length && !allowed_channels[short_event.channel])
						continue;
				} else if(event instanceof FWSVoiceEvent) {
					if(start_y > SongPanelPianoRoll.voice_y)
						continue;

					FWSVoiceEvent voice_event = (FWSVoiceEvent)event;
					if(voice_event.channel >= 0 && voice_event.channel < allowed_channels.length && !allowed_channels[voice_event.channel])
						continue;
				}

				Sprite event_sprite = getEventSprite(event);
				final int sprite_y = event_sprite.getY();
				if(sprite_y < start_y || sprite_y > start_y + h)
					continue;

				if(event.tick < start_tick)
					continue;

				if(event.tick >= end_tick)
					continue;

				event_sprite.select();
			}
		}

		bringToFront();
	}

	/** Select all events. If filter is true, select only events in the selection filter. If non_channel is true, select non-channel events. */
	public void selectAll(final boolean filter, final boolean non_channel) {
		boolean[] allowed_channels = Arrays.copyOf(selection_options.allowed_channels, selection_options.allowed_channels.length);
		if(selection_options.allowed_active) {
			for(int i=0;i<allowed_channels.length;i+=1)
				allowed_channels[i] = false;

			allowed_channels[channel_selected] = true;
		}

		ArrayList<FWSEvent> sequence_events = active_sequence.getChannelEvents();
		for(FWSEvent event: sequence_events) {
			if(!non_channel && !(event instanceof FWSNoteEvent) && !(event instanceof FWSShortEvent) && !(event instanceof FWSVoiceEvent))
				continue;

			if(event instanceof FWSNoteEvent) {
				FWSNoteEvent note_event = (FWSNoteEvent)event;
				if(filter && note_event.channel >= 0 && note_event.channel < allowed_channels.length && !allowed_channels[note_event.channel])
					continue;
			} else if(event instanceof FWSShortEvent) {
				FWSShortEvent short_event = (FWSShortEvent)event;
				if(filter && short_event.channel >= 0 && short_event.channel < allowed_channels.length && !allowed_channels[short_event.channel])
					continue;
			} else if(event instanceof FWSVoiceEvent) {
				FWSVoiceEvent voice_event = (FWSVoiceEvent)event;
				if(filter && voice_event.channel >= 0 && voice_event.channel < allowed_channels.length && !allowed_channels[voice_event.channel])
					continue;
			}

			Sprite event_sprite = getEventSprite(event);
			event_sprite.select();
		}
	}

	/** Set the selected channel. */
	public void setSelectedChannel(final int channel) {
		this.channel_selected = channel;
		this.bringToFront();
	}

	/** Get the selected channel. */
	public int getSelectedChannel() {
		return this.channel_selected;
	}

	/** Get the active sequence. */
	public FWSSequence getActiveSequence() {
		return this.active_sequence;
	}

	/** Refresh the viewport. */
	public void refresh() {
		final int ppq = controller.getGlobalOptions().ppq;
		int w = ppq*40;

		FWSSequence active_sequence = this.active_sequence;
		if(active_sequence != null)
			w = active_sequence.getXPosition(active_sequence.getSequenceLength(), ppq);

		piano_roll.setPreferredSize(new Dimension(w, piano_roll.getHeight()));
		style_header.setPreferredSize(new Dimension(w, style_header.getHeight()));

		style_header.repaint();
		piano_header.repaint();
		piano_roll.repaint();
		repaint();

		if(active_sequence != null && info_box != null) {
			FWSKeySignatureEvent key_signature = active_sequence.getKeySignatureAt(0);
			if(key_signature != null)
				info_box.refreshKeySig(key_signature.accidental_count);
		}
	}

	/** Arrange all components of the panel as needed. */
	public void bringToFront() {
		Component[] parts = getComponents();

		for(int i=0;i<parts.length;i+=1) {
			if (parts[i] instanceof JViewport && ((JViewport)parts[i]).getView() instanceof SongPanelPianoRoll) {
				SongPanelPianoRoll piano_roll = (SongPanelPianoRoll)((JViewport)parts[i]).getView();
				parts = piano_roll.getComponents();
				break;
			}
		}

		for(int i=0;i<parts.length;i+=1) {
			boolean active = false;

			if(parts[i] instanceof SpriteNoteEvent) {
				SpriteNoteEvent note_event = (SpriteNoteEvent)parts[i];
				active = ((FWSNoteEvent)note_event.getEvent()).channel == channel_selected || note_event.getSelected();
			} else if(parts[i] instanceof SpriteVoiceEvent) {
				SpriteVoiceEvent voice_event = (SpriteVoiceEvent)parts[i];
				active = ((FWSVoiceEvent)voice_event.getEvent()).channel == channel_selected || voice_event.getSelected();
			} else if(parts[i] instanceof SpriteShortEvent) {
				SpriteShortEvent short_event = (SpriteShortEvent)parts[i];
				active = ((FWSShortEvent)short_event.getEvent()).channel == channel_selected || short_event.getSelected();
			}

			if(parts[i] instanceof Sprite) {
				Sprite sprite = (Sprite)parts[i];
				if(sprite.getLocked() == active) {
					sprite.setLocked(!active);
					piano_roll.setLayer(sprite, active ? 2 : 1);
				}
			}
		}
	}

	/** Add a new note event to the sequence from the canvas. */
	public void addNoteEvent(final long tick, final byte note, final long duration) {
		FWSSequence sequence = active_sequence;

		if(sequence == null)
			return;

		FWSNoteEvent new_note = new FWSNoteEvent();
		new_note.tick = tick;
		new_note.duration = duration;
		new_note.note = note;
		new_note.channel = (byte)channel_selected;
		
		if(canvas_options.spinner_velocity != null)
			new_note.velocity = ((Integer)canvas_options.spinner_velocity.getValue()).byteValue();
		else
			new_note.velocity = 100;

		sequence.addEvent(new_note);
		addSprite(new_note);

		if(sequence.getEvent(new_note) && canvas_options.checkbox_play_note != null && canvas_options.checkbox_play_note.isSelected()) {
			FWSVoiceEvent voice_event = sequence.getVoiceAt(tick, (byte)channel_selected);
			Voice note_voice = new Voice("", (byte)0);
			if(voice_event != null) {
				note_voice = new Voice("", voice_event.voice, voice_event.voice_lsb, voice_event.voice_msb);
				note_voice = controller.getMidiManager().getSubstitutionVoice(note_voice);
			}

			MIDIManager manager = controller.getMidiManager();
			manager.playPlacedNote(new_note, note_voice);
		}
	}

	/** Add a sprite from an event. */
	public void addSprite(FWSEvent event) {
		FWSSequence sequence = active_sequence;
		final int ppq = controller.getGlobalOptions().ppq;

		if(event instanceof FWSTempoEvent)
			style_header.add(new SpriteTempoEvent(sequence.getXPosition(event.tick, ppq), SongPanelStyleHeader.tempo_y, sequence.getWidthAt(event.tick, ppq), FWS.event_height, (FWSTempoEvent)event));
		else if(event instanceof FWSTimeSignatureEvent)
			style_header.add(new SpriteTimeEvent(sequence.getXPosition(event.tick, ppq), SongPanelStyleHeader.time_y, sequence.getWidthAt(event.tick, ppq), FWS.event_height, (FWSTimeSignatureEvent)event));
		else if(event instanceof FWSKeySignatureEvent)
			style_header.add(new SpriteKeyEvent(sequence.getXPosition(event.tick, ppq), SongPanelStyleHeader.key_y, sequence.getWidthAt(event.tick, ppq), FWS.event_height, (FWSKeySignatureEvent)event));
		else if(event instanceof FWSSysexEvent)
			style_header.add(new SpriteSysexEvent(sequence.getXPosition(event.tick, ppq), SongPanelStyleHeader.sysex_y, sequence.getWidthAt(event.tick, ppq), FWS.event_height, (FWSSysexEvent)event));
		else if(event instanceof FWSMiscMIDIEvent) {
			FWSMiscMIDIEvent misc_event = (FWSMiscMIDIEvent)event;
			if(misc_event.type > 0 && misc_event.type <= 7)
				style_header.add(new SpriteTextEvent(sequence.getXPosition(event.tick, ppq), SongPanelStyleHeader.text_y, sequence.getWidthAt(event.tick, ppq), FWS.event_height, misc_event));
			else if(misc_event.type != 0x2F)
				style_header.add(new SpriteMiscMIDIEvent(sequence.getXPosition(event.tick, ppq), SongPanelStyleHeader.other_y, sequence.getWidthAt(event.tick, ppq), FWS.event_height, misc_event));
		} else if(event instanceof FWSSectionNameEvent && main_window.getDisplayMode() == DisplayMode.DISPLAY_MODE_STYLE)
			style_header.add(new SpriteSectionNameEvent(sequence.getXPosition(event.tick, ppq), SongPanelStyleHeader.chord_y, sequence.getWidthAt(event.tick, ppq), FWS.event_height, (FWSSectionNameEvent)event));	
		else if(event instanceof FWSStyleChangeEvent && main_window.getDisplayMode() == DisplayMode.DISPLAY_MODE_SONG)
			style_header.add(new SpriteStyleEvent(sequence.getXPosition(event.tick, ppq), SongPanelStyleHeader.style_y, sequence.getWidthAt(event.tick, ppq), FWS.event_height*2, (FWSStyleChangeEvent)event));
		else if(event instanceof FWSChordEvent && main_window.getDisplayMode() == DisplayMode.DISPLAY_MODE_SONG)
			style_header.add(new SpriteChordEvent(sequence.getXPosition(event.tick, ppq), SongPanelStyleHeader.chord_y, sequence.getWidthAt(event.tick, ppq), FWS.event_height, (FWSChordEvent)event));
		else if(event instanceof FWSNoteEvent) {
			FWSNoteEvent note_event = (FWSNoteEvent)event;
			SpriteNoteEvent note_sprite = new SpriteNoteEvent(sequence.getXPosition(event.tick, ppq), (127 - note_event.note)*FWS.event_height, sequence.getWidthAt(note_event.tick, (int)(ppq*note_event.duration/sequence.getTPQ())), FWS.event_height, controller.getChannelColors()[note_event.channel], note_event);
			piano_roll.add(note_sprite);
			final boolean active = note_event.channel == channel_selected;
			note_sprite.setLocked(!active);
			piano_roll.setLayer(note_sprite, active ? 2 : 1);
		} else if(event instanceof FWSVoiceEvent) {
			FWSVoiceEvent voice_event = (FWSVoiceEvent)event;
			SpriteVoiceEvent voice_sprite = new SpriteVoiceEvent(sequence.getXPosition(event.tick, ppq), SongPanelPianoRoll.voice_y, sequence.getWidthAt(event.tick, ppq), FWS.event_height, controller.getChannelColors()[((FWSVoiceEvent)event).channel], (FWSVoiceEvent)event);
			piano_roll.add(voice_sprite);
			final boolean active = voice_event.channel == channel_selected;
			voice_sprite.setLocked(!active);
			piano_roll.setLayer(voice_sprite, active ? 2 : 1);
		} else if(event instanceof FWSShortEvent) {
			int y = SongPanelPianoRoll.control_y;
			FWSShortEvent short_event = (FWSShortEvent)event;
			switch(short_event.command&0xFF) {
			case ShortMessage.CHANNEL_PRESSURE:
				y = SongPanelPianoRoll.channel_p_y;
				break;
			case ShortMessage.PITCH_BEND:
				y = SongPanelPianoRoll.pitch_bend_y;
				break;
			case ShortMessage.POLY_PRESSURE:
				y = SongPanelPianoRoll.key_p_y;
				break;
			}

			SpriteShortEvent short_sprite = new SpriteShortEvent(sequence.getXPosition(event.tick, ppq), y, sequence.getWidthAt(event.tick, ppq), FWS.event_height, controller.getChannelColors()[short_event.channel], short_event);
			piano_roll.add(short_sprite);
			final boolean active = short_event.channel == channel_selected;
			short_sprite.setLocked(!active);
			piano_roll.setLayer(short_sprite, active ? 2 : 1);
		}
	}

	/** Fill the viewport with events. */
	public void fill(FWSSequence sequence, final boolean clear) {
		if(clear) {
			{
				Component[] parts = piano_roll.getComponents();
				for(int i=0;i<parts.length;i+=1) {
					if(parts[i] instanceof Sprite) {
						piano_roll.remove(parts[i]);
					}
				}
			}
			{
				Component[] parts = style_header.getComponents();
				for(int i=0;i<parts.length;i+=1) {
					if(parts[i] instanceof Sprite) {
						style_header.remove(parts[i]);
					}
				}
			}

			if(info_box != null)
				info_box.refreshKeySig((byte)0);
		}

		this.active_sequence = sequence;
		this.snap = active_sequence.getTPQ();

		ArrayList<FWSEvent> common_events = sequence.getCommonEvents();
		for(int i=0;i<common_events.size();i+=1) {
			FWSEvent event = common_events.get(i);
			addSprite(event);
		}
		
		for(int c=0;c<16;c+=1) {
			ArrayList<FWSEvent> channel_events = sequence.getChannelEvents(c);
			for(int i=0;i<channel_events.size();i+=1) {
				FWSEvent event = channel_events.get(i);
				addSprite(event);
			}
		}

		refresh();
	}

	/** Get whether the canvas sprites are locked. */
	public boolean getSpritesLocked() {
		return this.sprites_locked;
	}

	/** Lock or unlock the canvas. */
	public void setSpritesLocked(final boolean locked) {
		this.sprites_locked = locked;
		if(locked) {
			Sprite[] sprites = getSprites();
			for(int i=0;i<sprites.length;i+=1)
				sprites[i].deselect();
		}
	}

	/** Get the snap. */
	public int getSnap() {
		return this.snap;
	}

	/** Set the snap. */
	public void setSnap(final int snap) {
		this.snap = snap;
	}

	/** Get the placement length. */
	public NoteToggleButton.NoteToggle getPlacementLength() {
		if(canvas_options.note_place_group != null)
			return canvas_options.note_place_group.getSelectedToggle();
		else
			return null;
	}

	/** Get whether the dot is selected. */
	public boolean getDot() {
		if(canvas_options.togglebutton_dot != null)
			return canvas_options.togglebutton_dot.isSelected();
		else
			return false;
	}

	/** Get the set custom tick. */
	public long getCustomTick() {
		if(canvas_options.spinner_note_len != null)
			return (Long)canvas_options.spinner_note_len.getValue();
		else
			return active_sequence.getTPQ();
	}

	/** Get the sprite associated with this event. */
	public Sprite getEventSprite(FWSEvent event) {
		Component[] piano_roll_components = piano_roll.getComponents();
		for(int i=0;i<piano_roll_components.length;i+=1) {
			if(piano_roll_components[i] instanceof Sprite) {
				Sprite sprite = (Sprite)piano_roll_components[i];
				if(sprite.getEvent() == event)
					return sprite;
			}
		}

		Component[] common_components = style_header.getComponents();
		for(int i=0;i<common_components.length;i+=1) {
			if(common_components[i] instanceof Sprite) {
				Sprite sprite = (Sprite)common_components[i];
				if(sprite.getEvent() == event)
					return sprite;
			}
		}

		return null;
	}

	/** Remove the sprite for the specified event. */
	public void removeSprite(FWSEvent event) {
		Sprite sprite = getEventSprite(event);
		if(sprite != null)
			removeSprite(sprite);
	}

	/** Remove the specified sprite. */
	public void removeSprite(Sprite sprite) {
		sprite.deselect();

		Component[] piano_roll_components = piano_roll.getComponents();
		for(int i=0;i<piano_roll_components.length;i+=1) {
			if(piano_roll_components[i] == sprite) {
				piano_roll.remove(sprite);
				return;
			}
		}

		Component[] common_components = style_header.getComponents();
		for(int i=0;i<common_components.length;i+=1) {
			if(common_components[i] == sprite) {
				style_header.remove(sprite);
				break;
			}
		}

		if(sprite instanceof SpriteTempoEvent || sprite instanceof SpriteTimeEvent || sprite instanceof SpriteSectionNameEvent)
			refreshFull();
	}

	/** Refresh the sprite associated with the event. */
	public void refreshSprite(FWSEvent event) {
		Sprite sprite = getEventSprite(event);
		if(sprite == null) {
			addSprite(event);
			return;
		}

		removeSprite(sprite);
		addSprite(event);
	}
	
	/** Refresh the viewport and all sprites. */
	public void refreshFull() {
		Sprite[] sprites = getSprites();
		final int ppq = controller.getGlobalOptions().ppq;
		
		for(int i=0;i<sprites.length;i+=1) {
			FWSEvent event = sprites[i].getEvent();
			if(sprites[i] instanceof SpriteNoteEvent) {
				final long duration = ((FWSNoteEvent)event).duration;
				final int w = active_sequence.getWidthAt(event.tick, (int)(ppq*duration/active_sequence.getTPQ()));
				sprites[i].setBounds(active_sequence.getXPosition(event.tick, ppq), sprites[i].getY(), w, FWS.event_height);
			} else if(sprites[i] instanceof SpriteStyleEvent) 
				sprites[i].setBounds(active_sequence.getXPosition(event.tick, ppq), sprites[i].getY(), active_sequence.getWidthAt(event.tick, ppq), 2*FWS.event_height);
			else
				sprites[i].setBounds(active_sequence.getXPosition(event.tick, ppq), sprites[i].getY(), active_sequence.getWidthAt(event.tick, ppq), FWS.event_height);
			
			sprites[i].revalidate();
			sprites[i].repaint();
		}

		if(main_window.getDisplayMode() == DisplayMode.DISPLAY_MODE_STYLE) {
			Style ref_style = new Style();
			ref_style.getFromSequence(active_sequence);
			main_window.refreshStyleDropdown(ref_style);
		}

		refresh();
	}

	/** Get the sprites in the viewport. */
	public Sprite[] getSprites() {
		ArrayList<Sprite> sprite_list = new ArrayList<Sprite>(0);
		
		Component[] piano_roll_components = piano_roll.getComponents();
		for(int i=0;i<piano_roll_components.length;i+=1) {
			if(piano_roll_components[i] instanceof Sprite)
				sprite_list.add((Sprite)piano_roll_components[i]);
		}

		Component[] common_components = style_header.getComponents();
		for(int i=0;i<common_components.length;i+=1) {
			if(common_components[i] instanceof Sprite)
				sprite_list.add((Sprite)common_components[i]);
		}

		Sprite[] sprite_array = new Sprite[sprite_list.size()];
		sprite_list.toArray(sprite_array);

		return sprite_array;
	}

	/** Delete selected sprites. */
	public void deleteSelectedSprites() {
		deleteSelectedSprites(true);
	}

	/** Delete selected sprites. */
	public void deleteSelectedSprites(final boolean include_last_selected) {
		Sprite[] sprites = getSprites();

		int deletion_count = 0;

		for(int i=0;i<sprites.length;i+=1) {
			if(sprites[i].getSelected() && !sprites[i].getLocked()) {
				active_sequence.removeEvent(sprites[i].getEvent());
				removeSprite(sprites[i]);
				deletion_count += 1;
			}
		}

		if(deletion_count <= 0 && include_last_selected) {
			if(this.last_popup instanceof Sprite) {
				Sprite last_sprite = (Sprite)last_popup;
				active_sequence.removeEvent(last_sprite.getEvent());
				removeSprite(last_sprite);
			}
		}
	}

	/** Get the controller. */
	public FWSEditor getController() {
		return this.controller;
	}

	/** Return the piano roll. */
	public SongPanelPianoRoll getPianoRoll() {
		return this.piano_roll;
	}
	
	/** Get the main window. */
	public FWSEditorMainWindow getMainWindow() {
		return this.main_window;
	}

	/** Set the horizontal scrollbar to the tick position. */
	public void setHScroll() {
		MIDIPlayerOptions player_options = controller.getMidiManager().getPlayerOptions();
		final long tick = player_options.start_tick, length = controller.getActiveSequence().getSequenceLength();
		JScrollBar scrh = getHorizontalScrollBar();

		final int maximum = scrh.getMaximum(), minimum = scrh.getMinimum(), range = maximum-minimum;
		final double ratio = (double)tick/length;

		scrh.setValue(minimum + (int)(range*ratio) - getWidth()/2);
	}

	/** Reset the scrollbars. */
	public void resetScrollBars() {
		JScrollBar scrh = this.getHorizontalScrollBar(), scrv = this.getVerticalScrollBar();
		
		final double height_ratio = (double)FWS.event_height*68/this.piano_roll.getHeight() - (double)this.getHeight()/2/scrv.getMaximum();

		controller.getMidiManager().getPlayerOptions().start_tick = 0;
		
		scrh.setValue(scrh.getMinimum());
		scrv.setValue((int) ((scrv.getMaximum() - scrv.getMinimum())*height_ratio));
	}

	/** Show the right-click popup. */
	public void showPopup(Component caller, MouseEvent e) {
		if(this.sprites_locked)
			return;

		last_popup = caller;
		last_popup_x = e.getX();

		popup_menu.show(caller, e.getX(), e.getY());
	}
}
