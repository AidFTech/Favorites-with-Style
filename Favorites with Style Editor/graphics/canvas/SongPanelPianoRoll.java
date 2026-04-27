package canvas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.sound.midi.ShortMessage;
import javax.swing.JLayeredPane;

import controllers.FWS;
import controllers.FWSEditor;
import event_dialogs.ChannelPressureEventDialog;
import event_dialogs.ControlEventDialog;
import event_dialogs.KeyPressureEventDialog;
import event_dialogs.PitchBendEventDialog;
import event_dialogs.VoiceEventDialog;
import fwsevents.FWSSequence;
import fwsevents.FWSShortEvent;
import fwsevents.FWSVoiceEvent;
import main_window.NoteToggleButton.NoteToggle;
import sprites.Sprite;

public class SongPanelPianoRoll extends JLayeredPane {
	private static final long serialVersionUID = -5440461985271190074L;

	protected static final int voice_y = FWS.event_height*128,
								control_y = FWS.event_height*(128 + 1),
								key_p_y = FWS.event_height*(128 + 2),
								channel_p_y = FWS.event_height*(128 + 3),
								pitch_bend_y = FWS.event_height*(128 + 4);

	private FWSEditor controller;
	private SongViewPort vp_parent;

	private int mx = -1, rx = -1, my = -1, dx = -1;
	private long mt = 0;
	private boolean dragged = false;
	
	private SongPanelPianoHeader head;

	public SongPanelPianoRoll(FWSEditor controller, SongViewPort parent) {
		this.controller = controller;
		this.vp_parent = parent;

		this.setBackground(Color.WHITE);
		this.setLayout(null);

		final int ppq = controller.getGlobalOptions().ppq;
		int w = ppq*40;

		FWSSequence active_sequence = parent.getActiveSequence();
		if(active_sequence != null)
			w = active_sequence.getXPosition(active_sequence.getSequenceLength(), ppq);
		
		this.setPreferredSize(new Dimension(w, FWS.event_height*133));
		this.setSize(new Dimension(w, FWS.event_height*133));

		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				Sprite[] sprites = vp_parent.getSprites();
				for(int i=0;i<sprites.length;i+=1)
					sprites[i].deselect();

				if(arg0.getButton() == MouseEvent.BUTTON1) {
					final NoteToggle placement_length = vp_parent.getPlacementLength();
					if(placement_length == null || placement_length == NoteToggle.NOTE_TOGGLE_DRAG)
						return;
					
					FWSSequence active_sequence = parent.getActiveSequence();
					if(active_sequence == null)
						return;

					if(my >= 0 && my < FWS.event_height*128 && placement_length != NoteToggle.NOTE_TOGGLE_CTL && placement_length != NoteToggle.NOTE_TOGGLE_DRAG) {
						final double ratio = getRatio(placement_length);
						if(ratio > 0) {
							final int snap = vp_parent.getSnap();
							final long tick = mt/snap*snap,
								duration = (placement_length != NoteToggle.NOTE_TOGGLE_CUSTOM) ?
									(long)(active_sequence.getTPQ()*ratio) : 
									vp_parent.getCustomTick();
							
							vp_parent.addNoteEvent(tick, (byte)(127-my/FWS.event_height), duration);
						}
					} else if(my >= FWS.event_height*128 && placement_length == NoteToggle.NOTE_TOGGLE_CTL) {
						final int snap = vp_parent.getSnap();
						final long tick = mt/snap*snap;
						final byte channel = (byte)vp_parent.getSelectedChannel();
						switch(my/FWS.event_height*FWS.event_height) {
						case voice_y: //Voice
							{
								FWSVoiceEvent new_voice;
								if(active_sequence.getVoiceAt(tick, (byte)vp_parent.getSelectedChannel()) != null)
									new_voice = new FWSVoiceEvent(active_sequence.getVoiceAt(tick, channel));
								else {
									new_voice = new FWSVoiceEvent();
									new_voice.channel = channel;
								}
								new_voice.tick = tick;
								new VoiceEventDialog(vp_parent.getMainWindow(), new_voice);
							}
							break;
						case control_y: //Control
							{
								FWSShortEvent new_ctl = new FWSShortEvent();
								new_ctl.channel = channel;
								new_ctl.tick = tick;
								new_ctl.command = (byte)ShortMessage.CONTROL_CHANGE; 
								new ControlEventDialog(vp_parent.getMainWindow(), new_ctl);
							}
							break;
						case key_p_y: //Key Pressure
							{
								FWSShortEvent new_polypressure = new FWSShortEvent();
								new_polypressure.channel = channel;
								new_polypressure.tick = tick;
								new_polypressure.command = (byte)ShortMessage.POLY_PRESSURE;
								new KeyPressureEventDialog(vp_parent.getMainWindow(), new_polypressure);
							}
							break;
						case channel_p_y: //Channel Pressure
							{
								FWSShortEvent new_channelpressure = new FWSShortEvent();
								new_channelpressure.channel = channel;
								new_channelpressure.tick = tick;
								new_channelpressure.command = (byte)ShortMessage.CHANNEL_PRESSURE;
								new ChannelPressureEventDialog(vp_parent.getMainWindow(), new_channelpressure);
							}
							break;
						case pitch_bend_y: //Pitch Bend
							{
								FWSShortEvent new_pitch = new FWSShortEvent();
								new_pitch.channel = channel;
								new_pitch.data1 = 0;
								new_pitch.data2 = 0x40;
								new_pitch.tick = tick;
								new_pitch.command = (byte)ShortMessage.PITCH_BEND;
								new PitchBendEventDialog(vp_parent.getMainWindow(), new_pitch);
							}
							break;
						}
					}
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				dragged = false;
				FWSSequence active_sequence = parent.getActiveSequence();
				if(active_sequence == null)
					return;

				if(vp_parent.getPlacementLength() == NoteToggle.NOTE_TOGGLE_DRAG && dx > 0 && my >= 0 && my < FWS.event_height*128) {
					final int snap = vp_parent.getSnap(), ppq = vp_parent.getController().getGlobalOptions().ppq;
					final long duration = (long) ((active_sequence.getXTime(rx+dx,ppq))/(snap)*snap) - (long)active_sequence.getXTime(rx,ppq)/snap*snap;
					vp_parent.addNoteEvent(active_sequence.getXTime(rx,ppq)/snap*snap, (byte)(127-my/FWS.event_height), duration);
					repaint();
				}
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				mx = -1;
				my = -1;
				if(head != null)
					head.recolor(-1);
				repaint();
			}
		});

		this.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent arg0) {
				FWSSequence active_sequence = parent.getActiveSequence();
				if(active_sequence != null) {
					mt = active_sequence.getXTime(arg0.getX(), ppq)/vp_parent.getSnap()*vp_parent.getSnap();
					mx = active_sequence.getXPosition(mt, ppq);
				}
				rx = arg0.getX();
				if(!dragged)
					my = arg0.getY()/FWS.event_height*FWS.event_height;
				if(head != null)
					head.recolor(my/FWS.event_height);
				repaint();
			}

			@Override
			public void mouseDragged(MouseEvent arg0) {
				if(vp_parent.getPlacementLength() == NoteToggle.NOTE_TOGGLE_DRAG) {
					dragged = true;
					dx = drag(arg0.getX());
					repaint();
				}
			}
		});
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		for(int i=0;i<128;i+=1) {
			if(i%12 == 1 || i%12 == 3 || i%12 == 6 || i%12 == 8 || i%12 == 10)
				g.setColor(new Color(220,220,220));
			else
				g.setColor(new Color(255,255,255));
			
			g.fillRect(0, (127-i)*FWS.event_height, this.getWidth() - 1, FWS.event_height);
			g.setColor(Color.DARK_GRAY);
			g.drawRect(0, (127-i)*FWS.event_height, this.getWidth() - 1, FWS.event_height);
			
			if(i%12 == 0 && i!=0) {
				g.setColor(new Color(0,0,150));
				//g.drawLine(0, (127-i+1)*FWS.event_height - 2, this.getWidth() - 1, (127-i+1)*FWS.event_height - 2);
				g.drawLine(0, (127-i+1)*FWS.event_height - 1, this.getWidth() - 1, (127-i+1)*FWS.event_height - 1);
				g.drawLine(0, (127-i+1)*FWS.event_height, this.getWidth() - 1, (127-i+1)*FWS.event_height);
				g.drawLine(0, (127-i+1)*FWS.event_height + 1, this.getWidth() - 1, (127-i+1)*FWS.event_height + 1);
				//g.drawLine(0, (127-i+1)*FWS.event_height + 2, this.getWidth() - 1, (127-i+1)*FWS.event_height + 2);
			}
		}

		FWSSequence active_sequence = vp_parent.getActiveSequence();
		if(active_sequence != null) {
			final int ppq = controller.getGlobalOptions().ppq;
			long[] m = active_sequence.getMeasureTicks();

			g.setColor(Color.LIGHT_GRAY);
			for(int i=0;i<m.length;i+=1) {
				g.drawLine(active_sequence.getXPosition(m[i], ppq), 0, active_sequence.getXPosition(m[i], ppq), this.getHeight() - 1);
			}
			
			if(mx < 0 || my < 0)
				return;

			//Selection line.
			if(vp_parent.getPlacementLength() != null) {
				g.setColor(Color.RED);
				final NoteToggle placement_length = vp_parent.getPlacementLength();
				if(my >= 0 && my < FWS.event_height*128) {
					final double ratio = getRatio(placement_length);
					if(ratio > 0) {
						final long tick = active_sequence.getXTime(mx, ppq);
						final int effective_ppq = active_sequence.getWidthAt(tick, ppq);

						final int w = (int)(effective_ppq*ratio);
						g.drawRect(mx, my, w, FWS.event_height);
					} else if(placement_length == NoteToggle.NOTE_TOGGLE_DRAG) {
						if(!dragged) {
							g.drawLine(mx, my, mx + ppq/2, my);
							g.drawLine(mx, my, mx, my + FWS.event_height);
							g.drawLine(mx, my + FWS.event_height, mx + ppq/2, my + FWS.event_height);
						} else {
							final int channel = vp_parent.getSelectedChannel();
							g.setColor(vp_parent.getController().getChannelColors()[channel]);
							g.fillRect(mx, my, dx-1, FWS.event_height);
							g.setColor(Color.BLACK);
							g.drawRect(mx, my, dx-1, FWS.event_height);
						}
					}
				} else if(placement_length == NoteToggle.NOTE_TOGGLE_CTL) {
					g.drawLine(mx, my, mx + ppq/2, my);
					g.drawLine(mx, my, mx, my + FWS.event_height);
					g.drawLine(mx, my + FWS.event_height, mx + ppq/2, my + FWS.event_height);
				}
			}
		}
	}

	/** Set the piano roll header. */
	public void setHead(SongPanelPianoHeader head) {
		this.head = head;
	}

	/** Refresh the piano roll. */
	public void refreshRoll(final int x, final int y) {
		FWSSequence active_sequence = vp_parent.getActiveSequence();
		if(active_sequence != null) {
			final int ppq = controller.getGlobalOptions().ppq;
			mx = active_sequence.getXPosition(active_sequence.getXTime(x, ppq)/vp_parent.getSnap()*vp_parent.getSnap(), ppq);
		}
		rx = x;
		my = y/FWS.event_height*FWS.event_height;
		if(head != null)
			head.recolor(my/FWS.event_height);
		repaint();
	}

	/** Get the viewport. */
	public SongViewPort getViewport() {
		return this.vp_parent;
	}

	/** Get the amount dragged. */
	private int drag(int x) {
		final int ppq = vp_parent.getController().getGlobalOptions().ppq;
		FWSSequence active_sequence = vp_parent.getActiveSequence();
		if(active_sequence == null)
			return x-mx;

		if(x < 0)
			x = 0;
		else if(x >= active_sequence.getXPosition(active_sequence.getSequenceLength(), ppq))
			x = active_sequence.getXPosition(active_sequence.getSequenceLength(), ppq) - 1;

		x = active_sequence.getXPosition(active_sequence.getXTime(x, ppq)/vp_parent.getSnap()*vp_parent.getSnap(), ppq);

		return x-mx;
	}

	/** Get a ratio from a note toggle option. */
	private double getRatio(NoteToggle placement_length) {
		double ratio = -1;
		switch(placement_length) {
		case NOTE_TOGGLE_WHOLE:
			ratio = 4;
			break;
		case NOTE_TOGGLE_HALF:
			ratio = 2;
			break;
		case NOTE_TOGGLE_QUARTER:
			ratio = 1;
			break;
		case NOTE_TOGGLE_EIGHTH:
			ratio = 0.5;
			break;
		case NOTE_TOGGLE_SIXTEENTH:
			ratio = 0.25;
			break;
		case NOTE_TOGGLE_32:
			ratio = 0.125;
			break;
		case NOTE_TOGGLE_TUPLET:
			ratio = 1.0/3.0; //TODO: Check the defined tuplet setting.
			break;
		case NOTE_TOGGLE_CUSTOM:
			ratio = (double)vp_parent.getCustomTick()/vp_parent.getActiveSequence().getTPQ();
			break;
		default:
			ratio = -1;
			break;
		}

		if(vp_parent.getDot() && placement_length != NoteToggle.NOTE_TOGGLE_CUSTOM && placement_length != NoteToggle.NOTE_TOGGLE_TUPLET)
			ratio *= 1.5;

		return ratio;
	}
}
