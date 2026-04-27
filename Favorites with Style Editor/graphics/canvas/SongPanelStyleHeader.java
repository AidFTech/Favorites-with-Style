package canvas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import controllers.FWS;
import controllers.FWSEditor;
import event_dialogs.ChordEventDialog;
import event_dialogs.KeyEventDialog;
import event_dialogs.MiscEventDialog;
import event_dialogs.SectionNameEventDialog;
import event_dialogs.StyleChangeEventDialog;
import event_dialogs.SysexEventDialog;
import event_dialogs.TempoEventDialog;
import event_dialogs.TextEventDialog;
import event_dialogs.TimeEventDialog;
import fwsevents.FWSChordEvent;
import fwsevents.FWSKeySignatureEvent;
import fwsevents.FWSMiscMIDIEvent;
import fwsevents.FWSSectionNameEvent;
import fwsevents.FWSSequence;
import fwsevents.FWSStyleChangeEvent;
import fwsevents.FWSSysexEvent;
import fwsevents.FWSTempoEvent;
import fwsevents.FWSTimeSignatureEvent;
import main_window.FWSEditorMainWindow.DisplayMode;
import main_window.NoteToggleButton.NoteToggle;
import sprites.Sprite;

public class SongPanelStyleHeader extends JPanel {
	private static final long serialVersionUID = 4281719430289712146L;

	protected static final int chord_y = 0,
								style_y = FWS.event_height*1,
								tempo_y = FWS.event_height*3,
								time_y = FWS.event_height*4,
								key_y = FWS.event_height*5,
								text_y = FWS.event_height*6,
								sysex_y = FWS.event_height*7,
								other_y = FWS.event_height*8;

	private FWSEditor controller;
	private SongViewPort vp_parent;
	
	private int mx = -1, my = -1;
	private long mt = 0;

	public SongPanelStyleHeader(FWSEditor controller, SongViewPort parent) {
		this.controller = controller;
		this.vp_parent = parent;

		this.setBackground(Color.WHITE);
		this.setLayout(null);
		
		final int ppq = controller.getGlobalOptions().ppq;
		int w = ppq*40;

		FWSSequence active_sequence = parent.getActiveSequence();
		if(active_sequence != null)
			w = active_sequence.getXPosition(active_sequence.getSequenceLength(), ppq);

		this.setPreferredSize(new Dimension(w,FWS.event_height*10));
		this.setSize(new Dimension(w,FWS.event_height*10));

		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				DisplayMode display_mode = vp_parent.getMainWindow().getDisplayMode();

				Sprite[] sprites = vp_parent.getSprites();
				for(int i=0;i<sprites.length;i+=1)
					sprites[i].deselect();

				if(vp_parent.getPlacementLength() == NoteToggle.NOTE_TOGGLE_CTL) {
					FWSSequence active_sequence = parent.getActiveSequence();
					if(active_sequence == null)
						return;
					final int snap = vp_parent.getSnap();
					final long tick = mt/snap*snap;

					switch(my/FWS.event_height*FWS.event_height) {
					case chord_y:
						if(display_mode == DisplayMode.DISPLAY_MODE_STYLE) { //Style section header.
							FWSSectionNameEvent new_section = new FWSSectionNameEvent();
							long[] measures = active_sequence.getMeasureTicks();
							for(int m=0;m<measures.length;m+=1) {
								if(measures[m] == tick) {
									new_section.tick = tick;
									break;
								} else if(measures[m] > tick) {
									new_section.tick = measures[m > 0 ? m-1 : 0];
									break;
								}
							}
							new SectionNameEventDialog(vp_parent.getMainWindow(), new_section);
						} else if(display_mode == DisplayMode.DISPLAY_MODE_SONG) { //Song header.
							FWSChordEvent new_chord = new FWSChordEvent();
							new_chord.tick = tick;

							new ChordEventDialog(vp_parent.getMainWindow(), new_chord);
						}
						break;
					case style_y:
					case style_y + FWS.event_height:
						if(display_mode == DisplayMode.DISPLAY_MODE_SONG) { //Song header.
							FWSStyleChangeEvent new_style;
							if(active_sequence.getStyleAt(tick) != null) {
								new_style = new FWSStyleChangeEvent(active_sequence.getStyleAt(tick));
								new_style.style_tick = -1;
							} else {
								new_style = new FWSStyleChangeEvent();
								new_style.style_tick = 0;
								
								if(controller.getLoadedSong().getStyleNames().length > 0)
									new_style.style_name = controller.getLoadedSong().getStyleNames()[0];
							}
							new_style.tick = tick;
							new StyleChangeEventDialog(vp_parent.getMainWindow(), new_style);
						}
						break;
					case tempo_y:
						{
							FWSTempoEvent new_tempo;
							if(active_sequence.getTempoAt(tick) != null)
								new_tempo = new FWSTempoEvent(active_sequence.getTempoAt(tick));
							else
								new_tempo = new FWSTempoEvent();
							new_tempo.tick = tick;
							new TempoEventDialog(vp_parent.getMainWindow(), new_tempo);
						}
						break;
					case time_y:
						if(display_mode != DisplayMode.DISPLAY_MODE_STYLE) {
							FWSTimeSignatureEvent new_time;
							if(active_sequence.getTimeSignatureAt(tick) != null)
								new_time = new FWSTimeSignatureEvent(active_sequence.getTimeSignatureAt(tick));
							else
								new_time = new FWSTimeSignatureEvent();
							new_time.tick = tick;
							long[] measures = active_sequence.getMeasureTicks();
							for(int m=0;m<measures.length;m+=1) {
								if(measures[m] == tick) {
									new_time.tick = tick;
									break;
								} else if(measures[m] > tick) {
									new_time.tick = measures[m > 0 ? m-1 : 0];
									break;
								}
							}

							new TimeEventDialog(vp_parent.getMainWindow(), new_time);
						} else {
							JOptionPane.showMessageDialog(vp_parent.getMainWindow(), "SFF1 styles cannot contain time signature change events.", "No Time Signatures", JOptionPane.WARNING_MESSAGE);
						}
						break;
					case key_y:
						{
							FWSKeySignatureEvent new_key;
							if(active_sequence.getKeySignatureAt(tick) != null)
								new_key = new FWSKeySignatureEvent(active_sequence.getKeySignatureAt(tick));
							else
								new_key = new FWSKeySignatureEvent();
							new_key.tick = tick;
							new KeyEventDialog(vp_parent.getMainWindow(), new_key);
						}
						break;
					case text_y:
						{
							FWSMiscMIDIEvent new_text = new FWSMiscMIDIEvent();
							new_text.tick = tick;
							new TextEventDialog(vp_parent.getMainWindow(), new_text);
						}
						break;
					case sysex_y:
						{
							FWSSysexEvent new_sysex = new FWSSysexEvent();
							new_sysex.tick = tick;
							new SysexEventDialog(vp_parent.getMainWindow(), new_sysex);
						}
						break;
					case other_y:
						{
							FWSMiscMIDIEvent new_other = new FWSMiscMIDIEvent();
							new_other.tick = tick;
							new MiscEventDialog(vp_parent.getMainWindow(), new_other);
						}
						break;
					}
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				mx = -1;
				my = -1;
				repaint();
			}
		});

		this.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				FWSSequence active_sequence = parent.getActiveSequence();
				if(active_sequence == null)
					return;
				final int snap = vp_parent.getSnap();
				mt = active_sequence.getXTime(e.getX(), ppq)/snap*snap;
				mx = active_sequence.getXPosition(mt, ppq);
				my = e.getY()/FWS.event_height*FWS.event_height;
				if(my == FWS.event_height*2)
					my = FWS.event_height;
				repaint();
			}
		});
	}

	/** Refresh the style header. */
	public void refreshHeader(final int x, final int y) {
		FWSSequence active_sequence = vp_parent.getActiveSequence();
		mx = x;
		if(active_sequence != null) {
			final int ppq = controller.getGlobalOptions().ppq;
			mx = active_sequence.getXPosition(active_sequence.getXTime(x, ppq)/vp_parent.getSnap()*vp_parent.getSnap(), ppq);
		}
		my = y/FWS.event_height*FWS.event_height;
		repaint();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		g.setColor(Color.BLACK);
		g.drawLine(0, this.getHeight() - 1, this.getWidth() - 1, this.getHeight() - 1);

		FWSSequence active_sequence = vp_parent.getActiveSequence();
		if(active_sequence != null) {
			final int ppq = controller.getGlobalOptions().ppq;
			long[] m = active_sequence.getMeasureTicks();

			for(int i=0;i<m.length;i+=1) {
				g.setColor(Color.BLACK);
				g.drawString("" + (i+1), active_sequence.getXPosition(m[i], ppq), this.getHeight() - 3);
				g.setColor(Color.LIGHT_GRAY);
				g.drawLine(active_sequence.getXPosition(m[i], ppq), 0, active_sequence.getXPosition(m[i], ppq), this.getHeight() - 1);
			}

			long[] b = active_sequence.getBeatTicks();

			g.setColor(new Color(180,180,220));
			for(int i=0;i<b.length;i+=1) {
				g.drawLine(active_sequence.getXPosition(b[i], ppq), this.getHeight() - 6, active_sequence.getXPosition(b[i], ppq), this.getHeight() - 1);
			}

			if(my >= 0 && my < FWS.event_height*9 && vp_parent.getPlacementLength() == NoteToggle.NOTE_TOGGLE_CTL) {
				g.setColor(Color.RED);

				if(my < FWS.event_height || my >= FWS.event_height*3) { //Non-style event.
					g.drawLine(mx, my, mx+15, my);
					g.drawLine(mx, my, mx, my+FWS.event_height-1);
					g.drawLine(mx, my+FWS.event_height-1, mx+15, my+FWS.event_height-1);
				} else { //Style event.
					g.drawLine(mx, my, mx+15, my);
					g.drawLine(mx, my, mx, my+FWS.event_height*2-1);
					g.drawLine(mx, my+FWS.event_height*2-1, mx+15, my+FWS.event_height*2-1);
				}
			}
		}
	}

	/** Get the viewport. */
	public SongViewPort getViewport() {
		return this.vp_parent;
	}
}
