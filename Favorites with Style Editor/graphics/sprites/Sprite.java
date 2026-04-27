package sprites;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

import canvas.SongPanelPianoRoll;
import canvas.SongPanelStyleHeader;
import canvas.SongViewPort;
import controllers.FWS;
import fwsevents.FWSEvent;
import fwsevents.FWSNoteEvent;
import fwsevents.FWSSectionNameEvent;
import fwsevents.FWSSequence;
import fwsevents.FWSTimeSignatureEvent;

public abstract class Sprite extends JPanel {
	private static final long serialVersionUID = -9178657772123646464L;

	protected FWSEvent affected_event;
	
	protected int width;
	protected int height = FWS.event_height;
	
	protected boolean isselected = false, locked = false, locked_x = false, locked_y = true;

	private boolean dragged = false;
	private int dx, dy;

	protected Sprite(final int x, final int y, final int w, final int h) {
		this.width = w;
		this.height = h;
		this.setBounds(x,y,width,height);
		this.setPreferredSize(new Dimension(width, height));
		this.setLayout(null);

		this.setVisible(true);

		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				if(!locked && !getViewport().getSpritesLocked()) {
					if(arg0.getButton() == MouseEvent.BUTTON1) {
						if((arg0.getModifiersEx() & (InputEvent.BUTTON1_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK)) != InputEvent.SHIFT_DOWN_MASK + InputEvent.BUTTON1_DOWN_MASK) {
							Sprite[] all_sprites = getViewport().getSprites();
							for(int i=0;i<all_sprites.length;i+=1)
								all_sprites[i].deselect();
						}

						if(!isselected)
							select();
						
						if(arg0.getClickCount() == 2) {
							Sprite[] all_sprites = getViewport().getSprites();
							for(int i=0;i<all_sprites.length;i+=1)
								all_sprites[i].deselect();

							select();
							createDialog();
						}
					}
				} else {
					MouseListener ml[] = getParent().getMouseListeners();
					for(int i = 0;i<ml.length;i+=1)
						ml[i].mousePressed(arg0);
				}
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				if(!getViewport().getSpritesLocked() && !locked) {
					if(dragged)
						setDragTime(arg0.getComponent().getX() + dx, arg0.getComponent().getY() + dy);
					dragged = false;
				} else {
					MouseListener ml[] = getParent().getMouseListeners();
					for(int i = 0;i<ml.length;i+=1) {
						Component original = arg0.getComponent();
						arg0.setSource(getParent());
						arg0.translatePoint(original.getX(), original.getY());
						ml[i].mouseReleased(arg0);
					}
				}
			}
		});

		this.addMouseMotionListener(new MouseAdapter() {
			public void mouseMoved(MouseEvent arg0) {
				if(getParent() instanceof SongPanelPianoRoll) {
					SongPanelPianoRoll parent = (SongPanelPianoRoll)getParent();
					parent.refreshRoll(arg0.getComponent().getX() + arg0.getX(), arg0.getComponent().getY() + arg0.getY());
				} else if(getParent() instanceof SongPanelStyleHeader) {
					SongPanelStyleHeader parent = (SongPanelStyleHeader)getParent();
					parent.refreshHeader(arg0.getComponent().getX() + arg0.getX(), arg0.getComponent().getY() + arg0.getY());
				} else {
					MouseMotionListener ml[] = getParent().getMouseMotionListeners();
					for(int i=0;i<ml.length;i+=1) {
						Component original = arg0.getComponent();
						arg0.setSource(getParent());
						arg0.translatePoint(original.getX(), original.getY());
						ml[i].mouseMoved(arg0);
					}
				}
			}

			public void mouseDragged(MouseEvent arg0) {
				if(getViewport().getSpritesLocked() || locked) {
					MouseMotionListener ml[] = getParent().getMouseMotionListeners();
					for(int i = 0;i<ml.length;i+=1) {
						Component original = arg0.getComponent();
						arg0.setSource(getParent());
						arg0.translatePoint(original.getX(), original.getY());
						ml[i].mouseDragged(arg0);
					}
				} else {
					FWSSequence seq = getParentSequence();
					final int ppq = getViewport().getController().getGlobalOptions().ppq;
					final int snap = getViewport().getSnap();

					dragged = true;
					dx = arg0.getX();
					dy = arg0.getY();
					final int x = arg0.getComponent().getX(), y = arg0.getComponent().getY();
					
					int new_x = seq.getXPosition(seq.getXTime((x+dx), ppq)/snap*snap, ppq);
					if(affected_event instanceof FWSTimeSignatureEvent || affected_event instanceof FWSSectionNameEvent) {
						long[] measures = seq.getMeasureTicks();
						final long new_tick = seq.getXTime((x+dx), ppq);
						final int m = seq.getMeasureAt(new_tick);
						if(m >= 0 && m < measures.length)
							new_x = seq.getXPosition(measures[m]/snap*snap, ppq);
					}

					drag(!locked_x ? new_x : x, !locked_y ? ((y+dy)/FWS.event_height*FWS.event_height) : y);
				}
			}
		});
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		this.width = getWidth();
		this.height = getHeight();
	}
	
	/** Return whether the sprite is selected. */
	public boolean getSelected() {
		return isselected;
	}
	
	/** Deselect the sprite. */
	public void deselect() {
		isselected = false;
		this.repaint();
	}
	
	/** Select the sprite. */
	public void select() {
		isselected = true;
		this.repaint();
	}

	/** Set the lock state. */
	public void setLocked(final boolean locked) {
		this.locked = locked;
		if(locked && isselected)
			deselect();
	}

	/** Get the lock state. */
	public boolean getLocked() {
		return this.locked;
	}

	/** Create a dialog for the affected event. */
	public void createDialog() {
		//Call from children.
	}

	/** Get the parent piano roll. */
	protected SongPanelPianoRoll getParentPianoRoll() {
		if(this.getParent() instanceof SongPanelPianoRoll)
			return (SongPanelPianoRoll)this.getParent();
		else
			return null;
	}

	/** Get the parent style header. */
	protected SongPanelStyleHeader getParentHeader() {
		if(this.getParent() instanceof SongPanelStyleHeader)
			return (SongPanelStyleHeader)this.getParent();
		else
			return null;
	}

	/** Get the song panel viewport. */
	protected SongViewPort getViewport() {
		if(getParentPianoRoll() != null) {
			SongPanelPianoRoll parent = getParentPianoRoll();
			if(parent.getViewport() instanceof SongViewPort)
				return (SongViewPort)parent.getViewport();
			else
				return null;
		} else if(getParentHeader() != null) {
			SongPanelStyleHeader parent = getParentHeader();
			if(parent.getViewport() instanceof SongViewPort)
				return (SongViewPort)parent.getViewport();
			else
				return null;
		} else return null;
	}

	/** Drag the event. */
	protected void drag(int x, int y) {
		if(locked || getViewport().getSpritesLocked())
			return;

		FWSSequence seq = getParentSequence();
		if(x < 0)
			x = 0;
		else if(x >= seq.getXPosition(seq.getSequenceLength(), getViewport().getController().getGlobalOptions().ppq))
			x = seq.getXPosition(seq.getSequenceLength(), getViewport().getController().getGlobalOptions().ppq) - 1;

		if(y < 0)
			y = 0;
		else if(y >= 128*FWS.event_height)
			y = 127*FWS.event_height;

		this.setLocation(!locked_x ? x : getX(), !locked_y ? y : getY());
	}
	
	/** Set the event parameters after a drag. */
	protected void setDragTime(final int x, int y) {
		if(locked || getViewport().getSpritesLocked())
			return;

		FWSSequence seq = getParentSequence();
		final long new_tick = seq.getXTime(x, getViewport().getController().getGlobalOptions().ppq)/getViewport().getSnap()*getViewport().getSnap();
		if(!locked_x) {
			if(affected_event instanceof FWSTimeSignatureEvent) {
				FWSTimeSignatureEvent original = new FWSTimeSignatureEvent((FWSTimeSignatureEvent)affected_event);
				affected_event.tick = new_tick;
				seq.refreshTimeSignatures((FWSTimeSignatureEvent)affected_event, original, false);	
			} else if(affected_event instanceof FWSSectionNameEvent) {
				long[] measures = seq.getMeasureTicks();
				final int m = seq.getMeasureAt(new_tick);

				if(m>=0 || m < measures.length)
					affected_event.tick = measures[m];

				seq.refreshEvent(affected_event);
			} else {
				this.affected_event.tick = new_tick;
				seq.refreshEvent(affected_event);
			}
		}
		if(!locked_y && affected_event instanceof FWSNoteEvent) {
			FWSNoteEvent note_event = (FWSNoteEvent)affected_event;
			if(y < 0)
				y = 0;
			if(y >= 128*FWS.event_height)
				y = 127*FWS.event_height;
			note_event.note = (byte)(127 - y/FWS.event_height);
		}

		getViewport().refreshFull();
	}

	/** Get the affected event. */
	public FWSEvent getEvent() {
		return this.affected_event;
	}

	/** Get the parent sequence in which the affected event resides. */
	public FWSSequence getParentSequence() {
		SongViewPort vp = getViewport();
		if(vp != null)
			return vp.getActiveSequence();
		else
			return null;
	}
}
