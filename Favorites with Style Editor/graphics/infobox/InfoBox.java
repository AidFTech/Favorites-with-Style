package infobox;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;

import controllers.FWS;
import fwsevents.FWSNoteEvent;
import style.ChordBody;

public class InfoBox extends JPanel {
	private static final long serialVersionUID = 3299090665019526143L;

	private FWS controller;

	private Image backdrop_61 = null;
	private Image backdrop_76 = null;
	private Image[] chord_root = {null,null,null,null,null,null,null};
	private Image chord_root_sharp = null;
	private Image chord_root_flat = null;
	private Image[] bass_root = {null,null,null,null,null,null,null};
	private Image[] chord_name = new Image[40];
	private Image bass_root_sharp = null;
	private Image bass_root_flat = null;
	
	private Image staff_note = null;
	private Image staff_flat = null;
	private Image staff_sharp = null;
	private Image staff_natural = null;
	private Image staff_noteline = null;
	
	private Point chord_start;
	private NotePoint[] note_start;
	private int first_note_index = 0; //Lowest C on the keyboard. C1.
	
	private Point[] staff_start_treble;
	private Point[] staff_start_bass;
	private Point staff_middle_c;
	private int staff_treble_key = -1;
	private int staff_bass_key = -1;
	private int staff_dist = 0;
	
	private int chord_root_num = -1;
	private int chord_num = -1;
	private boolean chord_root_isflat = false;
	private boolean chord_root_issharp = false;

	private ChordBody main_ph = null;
	private ChordBody bass_ph = null;
	private int inversion = -1;
	private byte finger = 0;
	
	private int bass_root_num = -1;
	private boolean bass_root_isflat = false;
	private boolean bass_root_issharp = false;
	
	private byte key_sig = 0;
	
	private int b_width = 500, b_height = 200;
	
	private boolean[] notes_down_chord = new boolean[128];
	private boolean[] notes_down_melody = new boolean[128];

	private KeyHL[] keys;

	private boolean changed = false;

	public InfoBox(FWS controller) {
		this.controller = controller;
		this.setToolTipText("Musical Information System display.");

		this.setLayout(null);
		
		this.init(true);

		Timer refresh_timer = new Timer(40, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(changed) {
					repaint();
					changed = false;
				}
			}
		});
		refresh_timer.start();
	}

	/** Initialize the info box. */
	private void init(final boolean def) {
		for(int i=0;i<notes_down_chord.length;i+=1)
			notes_down_chord[i] = false;
		
		if(def) {
			try {
				this.backdrop_76 = ImageIO.read(getClass().getResource("/infobox/backdrop_76.png"));
				this.backdrop_61 = ImageIO.read(getClass().getResource("/infobox/backdrop_61.png"));
				
				this.chord_root[0] = ImageIO.read(getClass().getResource("/infobox/chord_root_c.png"));
				this.chord_root[1] = ImageIO.read(getClass().getResource("/infobox/chord_root_d.png"));
				this.chord_root[2] = ImageIO.read(getClass().getResource("/infobox/chord_root_e.png"));
				this.chord_root[3] = ImageIO.read(getClass().getResource("/infobox/chord_root_f.png"));
				this.chord_root[4] = ImageIO.read(getClass().getResource("/infobox/chord_root_g.png"));
				this.chord_root[5] = ImageIO.read(getClass().getResource("/infobox/chord_root_a.png"));
				this.chord_root[6] = ImageIO.read(getClass().getResource("/infobox/chord_root_b.png"));
				
				this.chord_root_sharp = ImageIO.read(getClass().getResource("/infobox/chord_root_sharp.png"));
				this.chord_root_flat = ImageIO.read(getClass().getResource("/infobox/chord_root_flat.png"));
				
				this.bass_root[0] = ImageIO.read(getClass().getResource("/infobox/chord_bass_c.png"));
				this.bass_root[1] = ImageIO.read(getClass().getResource("/infobox/chord_bass_d.png"));
				this.bass_root[2] = ImageIO.read(getClass().getResource("/infobox/chord_bass_e.png"));
				this.bass_root[3] = ImageIO.read(getClass().getResource("/infobox/chord_bass_f.png"));
				this.bass_root[4] = ImageIO.read(getClass().getResource("/infobox/chord_bass_g.png"));
				this.bass_root[5] = ImageIO.read(getClass().getResource("/infobox/chord_bass_a.png"));
				this.bass_root[6] = ImageIO.read(getClass().getResource("/infobox/chord_bass_b.png"));
				
				this.bass_root_sharp = ImageIO.read(getClass().getResource("/infobox/chord_bass_sharp.png"));
				this.bass_root_flat = ImageIO.read(getClass().getResource("/infobox/chord_bass_flat.png"));
				
				BufferedImage chord_start_img = ImageIO.read(getClass().getResource("/infobox/chord_map.png"));
				int w = chord_start_img.getWidth();
				int h = chord_start_img.getHeight();
				int cx = -1, cy = -1;
				for(int i=0;i<h;i+=1) {
					for(int j=0;j<w;j+=1) {
						int c = chord_start_img.getRGB(j, i);
						if((c&0xFFFFFF) == 0xFF0000) {
							cy = i;
							cx = j;
							break;
						}
					}
				}
				
				this.chord_start = new Point(cx, cy);
				
				BufferedImage note_start_img_76 = ImageIO.read(getClass().getResource("/infobox/key_map_76.png"));
				ArrayList<NotePoint> note_points = new ArrayList<NotePoint>(0);
				
				w = note_start_img_76.getWidth();
				h = note_start_img_76.getHeight();
				for(int i=0;i<h;i+=1) {
					for(int j=0;j<w;j+=1) {
						int c = note_start_img_76.getRGB(j, i);
						if((c&0xFFFFFF) == 0xFF0000) {
							note_points.add(new NotePoint(j, i, false));
						} else if((c&0xFFFFFF) == 0xFFFF00) {
							note_points.add(new NotePoint(j, i, false));
							first_note_index = note_points.size() - 1;
						} else if((c&0xFFFFFF) == 0x0000FF) {
							note_points.add(new NotePoint(j, i, true));
						}
					}
				}
				
				this.note_start = new NotePoint[note_points.size()];
				for(int i=0;i<note_points.size();i+=1)
					this.note_start[i] = note_points.get(i);
				
				BufferedImage staff_start_img = ImageIO.read(getClass().getResource("/infobox/staff_map.png"));
				ArrayList<Point> points = new ArrayList<Point>(0);
				
				w = staff_start_img.getWidth();
				h = staff_start_img.getHeight();
				for(int i=0;i<h;i+=1) {
					for(int j=0;j<w;j+=1) {
						int c = staff_start_img.getRGB(j, i);
						if((c&0xFFFFFF) == 0xFF0000) {
							points.add(new Point(j, i));
						} 
					}
				}
				
				if(points.size() != 5) {
					//TODO: Throw error, something is wrong!
				}
				
				this.staff_start_treble = new Point[points.size()];
				for(int i=0;i<this.staff_start_treble.length; i+=1)
					this.staff_start_treble[i] = points.get(i);
				
				points = new ArrayList<Point>(0);
				for(int i=0;i<h;i+=1) {
					for(int j=0;j<w;j+=1) {
						int c = staff_start_img.getRGB(j, i);
						if((c&0xFFFFFF) == 0xFF) {
							points.add(new Point(j, i));
						} 
					}
				}
				
				if(points.size() != 5) {
					//TODO: Throw error, something is wrong!
				}
				
				this.staff_start_bass = new Point[points.size()];
				for(int i=0;i<this.staff_start_bass.length; i+=1)
					this.staff_start_bass[i] = points.get(i);
				
				this.staff_middle_c = new Point(this.staff_start_bass[0].x, (this.staff_start_treble[this.staff_start_treble.length - 1].y + this.staff_start_bass[0].y)/2);
				
				this.staff_dist = this.staff_start_treble[0].y - this.staff_start_treble[1].y;
				
				BufferedImage staff_key_img = ImageIO.read(getClass().getResource("/infobox/staff_key_map.png"));
				w = staff_start_img.getWidth();
				h = staff_start_img.getHeight();
				for(int i=0;i<h;i+=1) {
					for(int j=0;j<w;j+=1) {
						int c = staff_key_img.getRGB(j, i);
						if((c&0xFFFFFF) == 0xFF0000) {
							this.staff_treble_key = j;
						} else if((c&0xFFFFFF) == 0xFF) {
							this.staff_bass_key = j;
						} 
					}
				}

				this.staff_note = ImageIO.read(getClass().getResource("/infobox/staff_notehead.png"));
				this.staff_flat = ImageIO.read(getClass().getResource("/infobox/staff_flat.png"));
				this.staff_sharp = ImageIO.read(getClass().getResource("/infobox/staff_sharp.png"));
				this.staff_natural = ImageIO.read(getClass().getResource("/infobox/staff_natural.png"));
				
				this.staff_noteline = ImageIO.read(getClass().getResource("/infobox/staff_noteline.png"));
				
			} catch (IOException e) {
				// Something is wrong...
			}
			
			for(int i=0;i<this.chord_name.length;i+=1) {
				this.chord_name[i] = null;
			}
			
			//Load chord name images.
			for(int i=0;i<this.chord_name.length;i+=1) {
				try {
					this.chord_name[i] = ImageIO.read(getClass().getResource("/infobox/chord_c" + Integer.toString(i) + ".png"));
				} catch (IOException e) {
					// Do nothing, this chord doesn't exist.
				} catch (IllegalArgumentException e) {
					// Ditto.
				}
			}
		}
		
		keys = new KeyHL[128];
		for(int i=0;i<keys.length;i+=1) {
			final int point_num = i-FWSNoteEvent.low_c + this.first_note_index;
				
			if(point_num>=0 && point_num<note_start.length) {
				keys[i] = new KeyHL(note_start[point_num].x, note_start[point_num].y, note_start[point_num].getBlack());
				this.add(keys[i]);
			}
		}
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Point spot;

		if(controller.getGlobalOptions().key76) {
			if(backdrop_76 != null)
				g.drawImage(backdrop_76,0,0,b_width,b_height,null);
		} else {
			if(backdrop_61 != null)
				g.drawImage(backdrop_61,0,0,b_width,b_height,null);
		}
		
		if(chord_root_num >= 0) {
			int h = chord_root_num;
			if(chord_root[h] != null) {
				g.drawImage(chord_root[h], chord_start.x, chord_start.y, chord_root[h].getWidth(null), chord_root[h].getHeight(null),null);
			}
		}
		if(chord_root_issharp) {
			if(chord_root_sharp != null)
				g.drawImage(chord_root_sharp, chord_start.x, chord_start.y, chord_root_sharp.getWidth(null), chord_root_sharp.getHeight(null),null);
		}
		if(chord_root_isflat) {
			if(chord_root_flat != null)
				g.drawImage(chord_root_flat, chord_start.x, chord_start.y, chord_root_flat.getWidth(null), chord_root_flat.getHeight(null),null);
		}
		
		if(bass_root_num >= 0) {
			int h = bass_root_num;
			if(bass_root[h] != null) {
				g.drawImage(bass_root[h], chord_start.x, chord_start.y, bass_root[h].getWidth(null), bass_root[h].getHeight(null),null);
			}
		}
		if(bass_root_issharp) {
			if(bass_root_sharp != null)
				g.drawImage(bass_root_sharp, chord_start.x, chord_start.y, bass_root_sharp.getWidth(null), bass_root_sharp.getHeight(null),null);
		}
		if(bass_root_isflat) {
			if(bass_root_flat != null)
				g.drawImage(bass_root_flat, chord_start.x, chord_start.y, bass_root_flat.getWidth(null), bass_root_flat.getHeight(null),null);
		}
		
		if(chord_num>=0 && chord_num<chord_name.length) {
			if(chord_name[chord_num] != null)
				g.drawImage(chord_name[chord_num], chord_start.x, chord_start.y, chord_name[chord_num].getWidth(null), chord_name[chord_num].getHeight(null),null);
		}
		
		if(this.staff_start_treble.length == 5 && this.staff_start_bass.length == 5) {
			int spacing = staff_sharp.getWidth(null)*7/10;
			
			if(this.key_sig > 0) { //Sharps
				if(this.key_sig >= 1) { //F#
					g.drawImage(staff_sharp, staff_treble_key, staff_start_treble[0].y - staff_sharp.getHeight(null)/2, staff_sharp.getWidth(null), staff_sharp.getHeight(null), null);
					g.drawImage(staff_sharp, staff_bass_key, staff_start_bass[1].y - staff_sharp.getHeight(null)/2, staff_sharp.getWidth(null), staff_sharp.getHeight(null), null);
				} if(this.key_sig >= 2) { //C#
					g.drawImage(staff_sharp, staff_treble_key + 1*spacing, (staff_start_treble[1].y + staff_start_treble[2].y)/2 - staff_sharp.getHeight(null)/2,
							staff_sharp.getWidth(null), staff_sharp.getHeight(null), null);
					g.drawImage(staff_sharp, staff_bass_key + 1*spacing, (staff_start_bass[2].y + staff_start_bass[3].y)/2 - staff_sharp.getHeight(null)/2,
							staff_sharp.getWidth(null), staff_sharp.getHeight(null), null);
				} if(this.key_sig >= 3) { //G#
					g.drawImage(staff_sharp, staff_treble_key + 2*spacing, (2*staff_start_treble[0].y + staff_dist)/2 - staff_sharp.getHeight(null)/2,
							staff_sharp.getWidth(null), staff_sharp.getHeight(null), null);
					g.drawImage(staff_sharp, staff_bass_key + 2*spacing, (staff_start_bass[0].y + staff_start_bass[1].y)/2 - staff_sharp.getHeight(null)/2,
							staff_sharp.getWidth(null), staff_sharp.getHeight(null), null);
				} if(this.key_sig >= 4) { //D#
					g.drawImage(staff_sharp, staff_treble_key + 3*spacing, staff_start_treble[1].y - staff_sharp.getHeight(null)/2,
							staff_sharp.getWidth(null), staff_sharp.getHeight(null), null);
					g.drawImage(staff_sharp, staff_bass_key + 3*spacing, staff_start_bass[2].y - staff_sharp.getHeight(null)/2,
							staff_sharp.getWidth(null), staff_sharp.getHeight(null), null);
				} if(this.key_sig >= 5) { //A#
					g.drawImage(staff_sharp, staff_treble_key + 4*spacing, (staff_start_treble[2].y + staff_start_treble[3].y)/2 - staff_sharp.getHeight(null)/2,
							staff_sharp.getWidth(null), staff_sharp.getHeight(null), null);
					g.drawImage(staff_sharp, staff_bass_key + 4*spacing, (staff_start_bass[3].y + staff_start_bass[4].y)/2 - staff_sharp.getHeight(null)/2,
							staff_sharp.getWidth(null), staff_sharp.getHeight(null), null);
				} if(this.key_sig >= 6) { //E#
					g.drawImage(staff_sharp, staff_treble_key + 5*spacing, (staff_start_treble[0].y + staff_start_treble[1].y)/2 - staff_sharp.getHeight(null)/2,
							staff_sharp.getWidth(null), staff_sharp.getHeight(null), null);
					g.drawImage(staff_sharp, staff_bass_key + 5*spacing, (staff_start_bass[1].y + staff_start_bass[2].y)/2 - staff_sharp.getHeight(null)/2,
							staff_sharp.getWidth(null), staff_sharp.getHeight(null), null);
				} if(this.key_sig >= 7) { //B#
					g.drawImage(staff_sharp, staff_treble_key + 6*spacing, staff_start_treble[2].y - staff_sharp.getHeight(null)/2,
							staff_sharp.getWidth(null), staff_sharp.getHeight(null), null);
					g.drawImage(staff_sharp, staff_bass_key + 6*spacing, staff_start_bass[3].y - staff_sharp.getHeight(null)/2,
							staff_sharp.getWidth(null), staff_sharp.getHeight(null), null);
				}
			} else if(this.key_sig < 0) { //Flats
				if(this.key_sig <= -1) { //Bb
					g.drawImage(staff_flat, staff_treble_key, staff_start_treble[2].y - staff_flat.getHeight(null)/2,
							staff_flat.getWidth(null), staff_flat.getHeight(null), null);
					g.drawImage(staff_flat, staff_bass_key, staff_start_bass[3].y - staff_flat.getHeight(null)/2,
							staff_flat.getWidth(null), staff_flat.getHeight(null), null);
				} if(this.key_sig <= -2) { //Eb
					g.drawImage(staff_flat, staff_treble_key + 1*spacing, (staff_start_treble[0].y + staff_start_treble[1].y)/2 - staff_flat.getHeight(null)/2,
							staff_flat.getWidth(null), staff_flat.getHeight(null), null);
					g.drawImage(staff_flat, staff_bass_key + 1*spacing, (staff_start_bass[1].y + staff_start_bass[2].y)/2 - staff_flat.getHeight(null)/2,
							staff_flat.getWidth(null), staff_flat.getHeight(null), null);
				} if(this.key_sig <= -3) { //Ab
					g.drawImage(staff_flat, staff_treble_key + 2*spacing, (staff_start_treble[2].y + staff_start_treble[3].y)/2 - staff_flat.getHeight(null)/2,
							staff_flat.getWidth(null), staff_flat.getHeight(null), null);
					g.drawImage(staff_flat, staff_bass_key + 2*spacing, (staff_start_bass[3].y + staff_start_bass[4].y)/2 - staff_flat.getHeight(null)/2,
							staff_flat.getWidth(null), staff_flat.getHeight(null), null);
				} if(this.key_sig <= -4) { //Db
					g.drawImage(staff_flat, staff_treble_key + 3*spacing, staff_start_treble[1].y - staff_flat.getHeight(null)/2,
							staff_flat.getWidth(null), staff_flat.getHeight(null), null);
					g.drawImage(staff_flat, staff_bass_key + 3*spacing, staff_start_bass[2].y - staff_flat.getHeight(null)/2,
							staff_flat.getWidth(null), staff_flat.getHeight(null), null);
				} if(this.key_sig <= -5) { //Gb
					g.drawImage(staff_flat, staff_treble_key + 4*spacing, staff_start_treble[3].y - staff_flat.getHeight(null)/2,
							staff_flat.getWidth(null), staff_flat.getHeight(null), null);
					g.drawImage(staff_flat, staff_bass_key + 4*spacing, staff_start_bass[4].y- staff_flat.getHeight(null)/2,
							staff_flat.getWidth(null), staff_flat.getHeight(null), null);
				} if(this.key_sig <= -6) { //Cb
					g.drawImage(staff_flat, staff_treble_key + 5*spacing, (staff_start_treble[1].y + staff_start_treble[2].y)/2 - staff_flat.getHeight(null)/2,
							staff_flat.getWidth(null), staff_flat.getHeight(null), null);
					g.drawImage(staff_flat, staff_bass_key + 5*spacing, (staff_start_bass[2].y + staff_start_bass[3].y)/2 - staff_flat.getHeight(null)/2,
							staff_flat.getWidth(null), staff_flat.getHeight(null), null);
				} if(this.key_sig <= -7) { //Fb
					g.drawImage(staff_flat, staff_treble_key + 6*spacing, (staff_start_treble[3].y + staff_start_treble[4].y)/2 - staff_flat.getHeight(null)/2,
							staff_flat.getWidth(null), staff_flat.getHeight(null), null);
					g.drawImage(staff_flat, staff_bass_key + 6*spacing, (2*staff_start_bass[4].y - staff_dist)/2 - staff_flat.getHeight(null)/2,
							staff_flat.getWidth(null), staff_flat.getHeight(null), null);
				}
			}
		}
		
		for(int i=0;i<notes_down_melody.length;i+=1) {
			if(notes_down_chord[i] || notes_down_melody[i]) {
				if(staff_note != null && staff_sharp != null && staff_flat != null && staff_natural != null) {
					int n = i;
					int spacing = staff_sharp.getWidth(null)*7/10;
					
					if(n==FWSNoteEvent.middle_c && this.key_sig < 6 && this.key_sig > -6 || (n==FWSNoteEvent.middle_c - 1 && this.key_sig <= -6) || (n==FWSNoteEvent.middle_c + 1 && this.key_sig > -1)) {
						g.drawImage(staff_note, staff_middle_c.x, staff_middle_c.y - staff_note.getHeight(null)/2, staff_note.getWidth(null), staff_note.getHeight(null), null);
						if(staff_noteline != null)
							g.drawImage(staff_noteline, staff_middle_c.x - (staff_noteline.getWidth(null) - staff_note.getWidth(null))/2,
									staff_middle_c.y - staff_noteline.getHeight(null)/2, staff_noteline.getWidth(null), staff_noteline.getHeight(null), null);
						if(n==FWSNoteEvent.middle_c && (this.key_sig >= 2 || this.key_sig <= -6)) { //Natural sign required.
							g.drawImage(staff_natural, staff_middle_c.x - staff_natural.getWidth(null), staff_middle_c.y - staff_natural.getHeight(null)/2, staff_natural.getWidth(null), staff_natural.getHeight(null), null);
						} else if(n==FWSNoteEvent.middle_c + 1 && (this.key_sig < 2)) { //Sharp sign required.
							g.drawImage(staff_sharp, staff_middle_c.x - staff_sharp.getWidth(null), staff_middle_c.y - staff_sharp.getHeight(null)/2, staff_sharp.getWidth(null), staff_sharp.getHeight(null), null);
						}
					} else if(n>=FWSNoteEvent.middle_c + 1) { //Treble clef.
						if(n<=FWSNoteEvent.middle_c + 24) { //Highest level we want to go before incurring 8va.
							if((n%12 == 3 && this.key_sig < 2) || n%12 == 4 && this.key_sig > -7 || (n%12 == 5 && this.key_sig >= 5)) { //E, Eb, or E#
								if((n-FWSNoteEvent.middle_c)/12 == 0) //Same octave as middle C.
									spot = new Point(this.staff_start_treble[4].x, this.staff_start_treble[4].y);
								else
									spot = new Point(this.staff_start_treble[0].x + this.staff_note.getWidth(null) + spacing, (this.staff_start_treble[0].y + this.staff_start_treble[1].y)/2);
								
								g.drawImage(staff_note, spot.x, spot.y - staff_note.getHeight(null)/2, staff_note.getWidth(null), staff_note.getHeight(null),null);
								if(n%12 == 4 && (this.key_sig <= -2 || this.key_sig >= 6)) { //Natural sign required.
									g.drawImage(staff_natural, spot.x - staff_natural.getWidth(null), spot.y - staff_natural.getHeight(null)/2, staff_natural.getWidth(null), staff_natural.getHeight(null), null);
								} else if(n%12 == 3 && this.key_sig > -2 && this.key_sig < 2) { //Flat sign required.
									g.drawImage(staff_flat, spot.x - staff_flat.getWidth(null), spot.y - staff_flat.getHeight(null)/2, staff_flat.getWidth(null), staff_flat.getHeight(null), null);
								} else if(n%12 == 5 && this.key_sig == 5) { //Sharp sign required. Only required if the key is B (otherwise will be displayed as F).
									g.drawImage(staff_sharp, spot.x - staff_sharp.getWidth(null), spot.y - staff_sharp.getHeight(null)/2, staff_sharp.getWidth(null), staff_sharp.getHeight(null), null);
								}
							} else if((n%12 == 6 && this.key_sig <= -2) || n%12 == 7 || (n%12 == 8 && this.key_sig >0)) { //G, Gb, or G#
								if((n-FWSNoteEvent.middle_c)/12 == 0) //Same octave as middle C.
									spot = new Point(this.staff_start_treble[3].x, this.staff_start_treble[3].y);
								else
									spot = new Point(this.staff_start_treble[0].x + this.staff_note.getWidth(null) + spacing, (this.staff_start_treble[0].y + staff_dist/2));
								
								g.drawImage(staff_note, spot.x, spot.y - staff_note.getHeight(null)/2, staff_note.getWidth(null), staff_note.getHeight(null),null);
								if(n%12 == 7 && (this.key_sig <= -5 || this.key_sig >= 3)) { //Natural sign required.
									g.drawImage(staff_natural, spot.x - staff_natural.getWidth(null), spot.y - staff_natural.getHeight(null)/2, staff_natural.getWidth(null), staff_natural.getHeight(null), null);
								} else if(n%12 == 6 && this.key_sig > -5 && this.key_sig < 0) { //Flat sign required.
									g.drawImage(staff_flat, spot.x - staff_flat.getWidth(null), spot.y - staff_flat.getHeight(null)/2, staff_flat.getWidth(null), staff_flat.getHeight(null), null);
								} else if(n%12 == 8 && this.key_sig < 3 && this.key_sig > 0) { //Sharp sign required.
									g.drawImage(staff_sharp, spot.x - staff_sharp.getWidth(null), spot.y - staff_sharp.getHeight(null)/2, staff_sharp.getWidth(null), staff_sharp.getHeight(null), null);
								}
							} else if((n%12 == 10 && this.key_sig < 3) || n%12 == 11 && this.key_sig > -6 || (n%12 == 0 && this.key_sig >=5)) { //B, Bb, or B# (C)
								if((n-1-FWSNoteEvent.middle_c)/12 == 0) //Same octave as middle C.
									spot = new Point(this.staff_start_treble[2].x, this.staff_start_treble[2].y);
								else {
									spot = new Point(this.staff_start_treble[0].x + this.staff_note.getWidth(null) + spacing, (this.staff_start_treble[0].y + staff_dist + staff_dist/2));
									if(staff_noteline != null)
										g.drawImage(staff_noteline, staff_start_treble[0].x + staff_note.getWidth(null)/2 - (staff_noteline.getWidth(null) - staff_note.getWidth(null))/2,
												staff_start_treble[0].y + staff_dist - staff_noteline.getHeight(null)/2,
												staff_noteline.getWidth(null)+ staff_note.getWidth(null), staff_noteline.getHeight(null), null);
								}
								
								g.drawImage(staff_note, spot.x, spot.y - staff_note.getHeight(null)/2, staff_note.getWidth(null), staff_note.getHeight(null),null);
								if(n%12 == 11 && (this.key_sig <= -1 || this.key_sig >= 7)) { //Natural sign required.
									g.drawImage(staff_natural, spot.x - staff_natural.getWidth(null), spot.y - staff_natural.getHeight(null)/2, staff_natural.getWidth(null), staff_natural.getHeight(null), null);
								} else if(n%12 == 10 && this.key_sig > -1 && this.key_sig < 7) { //Flat sign required.
									g.drawImage(staff_flat, spot.x - staff_flat.getWidth(null), spot.y - staff_flat.getHeight(null)/2, staff_flat.getWidth(null), staff_flat.getHeight(null), null);
								} else if(n%12 == 0 && this.key_sig < 7 && this.key_sig > 4) { //Sharp sign required.
									g.drawImage(staff_sharp, spot.x - staff_sharp.getWidth(null), spot.y - staff_sharp.getHeight(null)/2, staff_sharp.getWidth(null), staff_sharp.getHeight(null), null);
								}
							} else if((n%12 == 1 && this.key_sig < 0) || n%12 == 2 || (n%12 == 3 && this.key_sig >=2)) { //D, Db, or D#
								if((n-1-FWSNoteEvent.middle_c)/12 != 0) //Not same octave as middle C.
									spot = new Point(this.staff_start_treble[1].x, this.staff_start_treble[1].y);
								else
									spot = new Point(this.staff_start_treble[4].x + this.staff_note.getWidth(null) + spacing, (this.staff_start_treble[4].y - staff_dist/2));
								
								g.drawImage(staff_note, spot.x, spot.y - staff_note.getHeight(null)/2, staff_note.getWidth(null), staff_note.getHeight(null),null);
								if(n%12 == 2 && (this.key_sig <= -4 || this.key_sig >= 4)) { //Natural sign required.
									g.drawImage(staff_natural, spot.x - staff_natural.getWidth(null), spot.y - staff_natural.getHeight(null)/2, staff_natural.getWidth(null), staff_natural.getHeight(null), null);
								} else if(n%12 == 1 && this.key_sig > -4 && this.key_sig < 0) { //Flat sign required.
									g.drawImage(staff_flat, spot.x - staff_flat.getWidth(null), spot.y - staff_flat.getHeight(null)/2, staff_flat.getWidth(null), staff_flat.getHeight(null), null);
								} else if(n%12 == 3 && this.key_sig < 4 && this.key_sig > 1) { //Sharp sign required.
									g.drawImage(staff_sharp, spot.x - staff_sharp.getWidth(null), spot.y - staff_sharp.getHeight(null)/2, staff_sharp.getWidth(null), staff_sharp.getHeight(null), null);
								}
							} else if((n%12 == 4 && this.key_sig < -5) || n%12 == 5 || (n%12 == 6 && this.key_sig >=-1)) { //F, Fb (E), or F#
								if((n-FWSNoteEvent.middle_c)/12 != 0) //Not same octave as middle C.
									spot = new Point(this.staff_start_treble[0].x, this.staff_start_treble[0].y);
								else
									spot = new Point(this.staff_start_treble[3].x + this.staff_note.getWidth(null) + spacing, (this.staff_start_treble[3].y - staff_dist/2));
								
								g.drawImage(staff_note, spot.x, spot.y - staff_note.getHeight(null)/2, staff_note.getWidth(null), staff_note.getHeight(null),null);
								if(n%12 == 5 && (this.key_sig <= -7 || this.key_sig >= 1)) { //Natural sign required.
									g.drawImage(staff_natural, spot.x - staff_natural.getWidth(null), spot.y - staff_natural.getHeight(null)/2, staff_natural.getWidth(null), staff_natural.getHeight(null), null);
								} else if(n%12 == 4 && this.key_sig == -6) { //Flat sign required.
									g.drawImage(staff_flat, spot.x - staff_flat.getWidth(null), spot.y - staff_flat.getHeight(null)/2, staff_flat.getWidth(null), staff_flat.getHeight(null), null);
								} else if(n%12 == 6 && this.key_sig < 1 && this.key_sig > -6) { //Sharp sign required.
									g.drawImage(staff_sharp, spot.x - staff_sharp.getWidth(null), spot.y - staff_sharp.getHeight(null)/2, staff_sharp.getWidth(null), staff_sharp.getHeight(null), null);
								}
							} else if((n%12 == 11 && this.key_sig < 1) || n%12 == 0 || (n%12 == 1 && this.key_sig >= -1)) { //C, Cb (B), or C#
								if(n>FWSNoteEvent.middle_c + 13) { //Not same octave as middle C.
									spot = new Point(this.staff_start_treble[0].x, this.staff_start_treble[0].y + 2*staff_dist);
									if(staff_noteline != null) {
										g.drawImage(staff_noteline, staff_start_treble[0].x - (staff_noteline.getWidth(null) - staff_note.getWidth(null))/2,
												staff_start_treble[0].y + staff_dist - staff_noteline.getHeight(null)/2,
												staff_noteline.getWidth(null), staff_noteline.getHeight(null), null);
										g.drawImage(staff_noteline, staff_start_treble[0].x - (staff_noteline.getWidth(null) - staff_note.getWidth(null))/2,
												staff_start_treble[0].y + 2*staff_dist - staff_noteline.getHeight(null)/2,
												staff_noteline.getWidth(null), staff_noteline.getHeight(null), null);
									}
								}
								else
									spot = new Point(this.staff_start_treble[1].x + this.staff_note.getWidth(null) + spacing, (this.staff_start_treble[1].y - staff_dist/2));
								
								g.drawImage(staff_note, spot.x, spot.y - staff_note.getHeight(null)/2, staff_note.getWidth(null), staff_note.getHeight(null),null);
								if(n%12 == 0 && (this.key_sig <= -6 || this.key_sig >= 2)) { //Natural sign required.
									g.drawImage(staff_natural, spot.x - staff_natural.getWidth(null), spot.y - staff_natural.getHeight(null)/2, staff_natural.getWidth(null), staff_natural.getHeight(null), null);
								} else if(n%12 == 11 && this.key_sig > -7 && this.key_sig < -6) { //Flat sign required.
									g.drawImage(staff_flat, spot.x - staff_flat.getWidth(null), spot.y - staff_flat.getHeight(null)/2, staff_flat.getWidth(null), staff_flat.getHeight(null), null);
								} else if(n%12 == 1 && this.key_sig < 2 && this.key_sig > -1) { //Sharp sign required.
									g.drawImage(staff_sharp, spot.x - staff_sharp.getWidth(null), spot.y - staff_sharp.getHeight(null)/2, staff_sharp.getWidth(null), staff_sharp.getHeight(null), null);
								}
							} else if((n%12 == 8 && this.key_sig < 1) || n%12 == 9 || (n%12 == 10 && this.key_sig >= 3)) { //A, Ab, or A#
								if((n-FWSNoteEvent.middle_c)/12 != 0) { //Not same octave as middle C.
									spot = new Point(this.staff_start_treble[0].x, this.staff_start_treble[0].y + staff_dist);
									if(staff_noteline != null)
										g.drawImage(staff_noteline, staff_start_treble[0].x - (staff_noteline.getWidth(null) - staff_note.getWidth(null))/2,
												staff_start_treble[0].y + staff_dist - staff_noteline.getHeight(null)/2,
												staff_noteline.getWidth(null), staff_noteline.getHeight(null), null);
								}
								else
									spot = new Point(this.staff_start_treble[2].x + this.staff_note.getWidth(null) + spacing, (this.staff_start_treble[2].y - staff_dist/2));
								
								g.drawImage(staff_note, spot.x, spot.y - staff_note.getHeight(null)/2, staff_note.getWidth(null), staff_note.getHeight(null),null);
								if(n%12 == 9 && (this.key_sig <= -3 || this.key_sig >= 5)) { //Natural sign required.
									g.drawImage(staff_natural, spot.x - staff_natural.getWidth(null), spot.y - staff_natural.getHeight(null)/2, staff_natural.getWidth(null), staff_natural.getHeight(null), null);
								} else if(n%12 == 8 && this.key_sig > -3 && this.key_sig < 3) { //Flat sign required.
									g.drawImage(staff_flat, spot.x - staff_flat.getWidth(null), spot.y - staff_flat.getHeight(null)/2, staff_flat.getWidth(null), staff_flat.getHeight(null), null);
								} else if(n%12 == 10 && this.key_sig < 5 && this.key_sig > 2) { //Sharp sign required.
									g.drawImage(staff_sharp, spot.x - staff_sharp.getWidth(null), spot.y - staff_sharp.getHeight(null)/2, staff_sharp.getWidth(null), staff_sharp.getHeight(null), null);
								}
							}
						}
					} else if(n<=FWSNoteEvent.middle_c) { //Bass clef.
						if(n>=FWSNoteEvent.middle_c - 24) { //Lowest level we want to go before incurring 8vb.
							if((n%12 == 3 && this.key_sig < 2) || n%12 == 4 && this.key_sig > -7 || (n%12 == 5 && this.key_sig >= 5)) { //E, Eb, or E#
								if(n < FWSNoteEvent.middle_c - 12) { //Lower than low C.
									spot = new Point(this.staff_start_bass[4].x, this.staff_start_bass[4].y - this.staff_dist);
									if(staff_noteline != null)
										g.drawImage(staff_noteline, staff_start_bass[4].x - (staff_noteline.getWidth(null) - staff_note.getWidth(null))/2,
												staff_start_bass[4].y - staff_dist - staff_noteline.getHeight(null)/2,
												staff_noteline.getWidth(null), staff_noteline.getHeight(null), null);
								}
								else
									spot = new Point(this.staff_start_bass[1].x + this.staff_note.getWidth(null) + spacing, (this.staff_start_bass[1].y + this.staff_start_bass[2].y)/2);
								
								g.drawImage(staff_note, spot.x, spot.y - staff_note.getHeight(null)/2, staff_note.getWidth(null), staff_note.getHeight(null),null);
								if(n%12 == 4 && (this.key_sig <= -2 || this.key_sig >= 6)) { //Natural sign required.
									g.drawImage(staff_natural, spot.x - staff_natural.getWidth(null), spot.y - staff_natural.getHeight(null)/2, staff_natural.getWidth(null), staff_natural.getHeight(null), null);
								} else if(n%12 == 3 && this.key_sig > -2 && this.key_sig < 2) { //Flat sign required.
									g.drawImage(staff_flat, spot.x - staff_flat.getWidth(null), spot.y - staff_flat.getHeight(null)/2, staff_flat.getWidth(null), staff_flat.getHeight(null), null);
								} else if(n%12 == 5 && this.key_sig == 5) { //Sharp sign required. Only required if the key is B (otherwise will be displayed as F).
									g.drawImage(staff_sharp, spot.x - staff_sharp.getWidth(null), spot.y - staff_sharp.getHeight(null)/2, staff_sharp.getWidth(null), staff_sharp.getHeight(null), null);
								}
							} else if((n%12 == 6 && this.key_sig <= -2) || n%12 == 7 || (n%12 == 8 && this.key_sig >0)) { //G, Gb, or G#
								if(n < FWSNoteEvent.middle_c - 12) //Lower than low C.
									spot = new Point(this.staff_start_bass[4].x, this.staff_start_bass[4].y);
								else
									spot = new Point(this.staff_start_bass[0].x + this.staff_note.getWidth(null) + spacing, (this.staff_start_bass[0].y + this.staff_start_bass[1].y)/2);
								
								g.drawImage(staff_note, spot.x, spot.y - staff_note.getHeight(null)/2, staff_note.getWidth(null), staff_note.getHeight(null),null);
								if(n%12 == 7 && (this.key_sig <= -5 || this.key_sig >= 3)) { //Natural sign required.
									g.drawImage(staff_natural, spot.x - staff_natural.getWidth(null), spot.y - staff_natural.getHeight(null)/2, staff_natural.getWidth(null), staff_natural.getHeight(null), null);
								} else if(n%12 == 6 && this.key_sig > -5 && this.key_sig < 0) { //Flat sign required.
									g.drawImage(staff_flat, spot.x - staff_flat.getWidth(null), spot.y - staff_flat.getHeight(null)/2, staff_flat.getWidth(null), staff_flat.getHeight(null), null);
								} else if(n%12 == 8 && this.key_sig < 3 && this.key_sig > 0) { //Sharp sign required.
									g.drawImage(staff_sharp, spot.x - staff_sharp.getWidth(null), spot.y - staff_sharp.getHeight(null)/2, staff_sharp.getWidth(null), staff_sharp.getHeight(null), null);
								}
							} else if((n%12 == 10 && this.key_sig < 3) || n%12 == 11 && this.key_sig > -6 || (n%12 == 0 && this.key_sig >=5)) { //B, Bb, or B# (C)
								if(n-1 < FWSNoteEvent.middle_c - 12) //Lower than low C.
									spot = new Point(this.staff_start_bass[3].x, this.staff_start_bass[3].y);
								else 
									spot = new Point(this.staff_start_bass[0].x + this.staff_note.getWidth(null) + spacing, (this.staff_start_bass[0].y + staff_dist/2));
								
								g.drawImage(staff_note, spot.x, spot.y - staff_note.getHeight(null)/2, staff_note.getWidth(null), staff_note.getHeight(null),null);
								if(n%12 == 11 && (this.key_sig <= -1 || this.key_sig >= 7)) { //Natural sign required.
									g.drawImage(staff_natural, spot.x - staff_natural.getWidth(null), spot.y - staff_natural.getHeight(null)/2, staff_natural.getWidth(null), staff_natural.getHeight(null), null);
								} else if(n%12 == 10 && this.key_sig > -1 && this.key_sig < 7) { //Flat sign required.
									g.drawImage(staff_flat, spot.x - staff_flat.getWidth(null), spot.y - staff_flat.getHeight(null)/2, staff_flat.getWidth(null), staff_flat.getHeight(null), null);
								} else if(n%12 == 0 && this.key_sig < 7 && this.key_sig > 4) { //Sharp sign required.
									g.drawImage(staff_sharp, spot.x - staff_sharp.getWidth(null), spot.y - staff_sharp.getHeight(null)/2, staff_sharp.getWidth(null), staff_sharp.getHeight(null), null);
								}
							} else if((n%12 == 1 && this.key_sig < 0) || n%12 == 2 || (n%12 == 3 && this.key_sig >=2)) { //D, Db, or D#
								if(n > FWSNoteEvent.middle_c - 12) //Higher than low C.
									spot = new Point(this.staff_start_bass[2].x, this.staff_start_bass[2].y);
								else {
									spot = new Point(this.staff_start_bass[4].x + this.staff_note.getWidth(null) + spacing, (this.staff_start_bass[4].y - staff_dist - staff_dist/2));
									if(staff_noteline != null)
										g.drawImage(staff_noteline, staff_start_treble[0].x + staff_note.getWidth(null)/2 - (staff_noteline.getWidth(null) - staff_note.getWidth(null))/2,
												staff_start_bass[4].y - staff_dist - staff_noteline.getHeight(null)/2,
												staff_noteline.getWidth(null)+ staff_note.getWidth(null), staff_noteline.getHeight(null), null);
								}
								
								g.drawImage(staff_note, spot.x, spot.y - staff_note.getHeight(null)/2, staff_note.getWidth(null), staff_note.getHeight(null),null);
								if(n%12 == 2 && (this.key_sig <= -4 || this.key_sig >= 4)) { //Natural sign required.
									g.drawImage(staff_natural, spot.x - staff_natural.getWidth(null), spot.y - staff_natural.getHeight(null)/2, staff_natural.getWidth(null), staff_natural.getHeight(null), null);
								} else if(n%12 == 1 && this.key_sig > -4 && this.key_sig < 0) { //Flat sign required.
									g.drawImage(staff_flat, spot.x - staff_flat.getWidth(null), spot.y - staff_flat.getHeight(null)/2, staff_flat.getWidth(null), staff_flat.getHeight(null), null);
								} else if(n%12 == 3 && this.key_sig < 4 && this.key_sig > 1) { //Sharp sign required.
									g.drawImage(staff_sharp, spot.x - staff_sharp.getWidth(null), spot.y - staff_sharp.getHeight(null)/2, staff_sharp.getWidth(null), staff_sharp.getHeight(null), null);
								}
							} else if((n%12 == 4 && this.key_sig < -5) || n%12 == 5 || (n%12 == 6 && this.key_sig >=-1)) { //F, Fb (E), or F#
								if(n > FWSNoteEvent.middle_c - 12) //Higher than low C.
									spot = new Point(this.staff_start_bass[1].x, this.staff_start_bass[1].y);
								else
									spot = new Point(this.staff_start_bass[4].x + this.staff_note.getWidth(null) + spacing, (this.staff_start_bass[4].y - staff_dist/2));
								
								g.drawImage(staff_note, spot.x, spot.y - staff_note.getHeight(null)/2, staff_note.getWidth(null), staff_note.getHeight(null),null);
								if(n%12 == 5 && (this.key_sig <= -7 || this.key_sig >= 1)) { //Natural sign required.
									g.drawImage(staff_natural, spot.x - staff_natural.getWidth(null), spot.y - staff_natural.getHeight(null)/2, staff_natural.getWidth(null), staff_natural.getHeight(null), null);
								} else if(n%12 == 4 && this.key_sig == -6) { //Flat sign required.
									g.drawImage(staff_flat, spot.x - staff_flat.getWidth(null), spot.y - staff_flat.getHeight(null)/2, staff_flat.getWidth(null), staff_flat.getHeight(null), null);
								} else if(n%12 == 6 && this.key_sig < 1 && this.key_sig > -6) { //Sharp sign required.
									g.drawImage(staff_sharp, spot.x - staff_sharp.getWidth(null), spot.y - staff_sharp.getHeight(null)/2, staff_sharp.getWidth(null), staff_sharp.getHeight(null), null);
								}
							} else if((n%12 == 11 && this.key_sig < 1) || n%12 == 0 || (n%12 == 1 && this.key_sig >= -1)) { //C, Cb (B), or C#
								if(n+1<FWSNoteEvent.middle_c -12) { //Lower than low C.
									spot = new Point(this.staff_start_bass[4].x, this.staff_start_bass[4].y - 2*staff_dist);
									if(staff_noteline != null) {
										g.drawImage(staff_noteline, staff_start_bass[0].x - (staff_noteline.getWidth(null) - staff_note.getWidth(null))/2,
												staff_start_bass[4].y - staff_dist - staff_noteline.getHeight(null)/2,
												staff_noteline.getWidth(null), staff_noteline.getHeight(null), null);
										g.drawImage(staff_noteline, staff_start_bass[0].x - (staff_noteline.getWidth(null) - staff_note.getWidth(null))/2,
												staff_start_bass[4].y - 2*staff_dist - staff_noteline.getHeight(null)/2,
												staff_noteline.getWidth(null), staff_noteline.getHeight(null), null);
									}
								}
								else
									spot = new Point(this.staff_start_bass[2].x + this.staff_note.getWidth(null) + spacing, (this.staff_start_bass[2].y + this.staff_start_bass[3].y)/2);
								
								g.drawImage(staff_note, spot.x, spot.y - staff_note.getHeight(null)/2, staff_note.getWidth(null), staff_note.getHeight(null),null);
								if(n%12 == 0 && (this.key_sig <= -6 || this.key_sig >= 2)) { //Natural sign required.
									g.drawImage(staff_natural, spot.x - staff_natural.getWidth(null), spot.y - staff_natural.getHeight(null)/2, staff_natural.getWidth(null), staff_natural.getHeight(null), null);
								} else if(n%12 == 11 && this.key_sig > -7 && this.key_sig < -6) { //Flat sign required.
									g.drawImage(staff_flat, spot.x - staff_flat.getWidth(null), spot.y - staff_flat.getHeight(null)/2, staff_flat.getWidth(null), staff_flat.getHeight(null), null);
								} else if(n%12 == 1 && this.key_sig < 2 && this.key_sig > -1) { //Sharp sign required.
									g.drawImage(staff_sharp, spot.x - staff_sharp.getWidth(null), spot.y - staff_sharp.getHeight(null)/2, staff_sharp.getWidth(null), staff_sharp.getHeight(null), null);
								}
							} else if((n%12 == 8 && this.key_sig < 1) || n%12 == 9 || (n%12 == 10 && this.key_sig >= 3)) { //A, Ab, or A#
								if(n > FWSNoteEvent.middle_c - 12) //Higher than low C.
									spot = new Point(this.staff_start_bass[0].x, this.staff_start_bass[0].y);
								else
									spot = new Point(this.staff_start_bass[3].x + this.staff_note.getWidth(null) + spacing, (this.staff_start_bass[3].y + this.staff_start_bass[4].y)/2);
								
								g.drawImage(staff_note, spot.x, spot.y - staff_note.getHeight(null)/2, staff_note.getWidth(null), staff_note.getHeight(null),null);
								if(n%12 == 9 && (this.key_sig <= -3 || this.key_sig >= 5)) { //Natural sign required.
									g.drawImage(staff_natural, spot.x - staff_natural.getWidth(null), spot.y - staff_natural.getHeight(null)/2, staff_natural.getWidth(null), staff_natural.getHeight(null), null);
								} else if(n%12 == 8 && this.key_sig > -3 && this.key_sig < 3) { //Flat sign required.
									g.drawImage(staff_flat, spot.x - staff_flat.getWidth(null), spot.y - staff_flat.getHeight(null)/2, staff_flat.getWidth(null), staff_flat.getHeight(null), null);
								} else if(n%12 == 10 && this.key_sig < 5 && this.key_sig > 2) { //Sharp sign required.
									g.drawImage(staff_sharp, spot.x - staff_sharp.getWidth(null), spot.y - staff_sharp.getHeight(null)/2, staff_sharp.getWidth(null), staff_sharp.getHeight(null), null);
								}
							}
						}
					}
				}
			}
		}
	}

	/** Refresh a melody note. */
	public void refreshMelody(byte note, boolean on) {
		if(note >= 0 && note < notes_down_melody.length) {
			notes_down_melody[note] = on;
			if(keys[note] != null)
				keys[note].depressed = on | notes_down_chord[note];

			changed = true;
		}
	}
	
	/** Clear the melody notes. */
	public void clearMelody() {
		for(int i=0;i<notes_down_melody.length;i+=1)
			notes_down_melody[i] = false;
		
		for(int i=0;i<keys.length;i+=1) {
			if(keys[i] != null && !notes_down_chord[i])
				keys[i].depressed = false;
		}

		changed = true;
	}

	/** Refresh a chord from style play. */
	public void refreshStyleChord(final byte root, final byte chord) {
		ChordBody main = new ChordBody(root, chord);
		refreshChord(main, null, -1, (byte)0);
	}

	//Refresh a chord.
	public void refreshChord(final byte root, final byte chord, final byte bass_root, final byte bass_chord, final int inversion, final byte fingering) {
		ChordBody main = new ChordBody(root, chord);
		ChordBody bass = (bass_root&0xF0) >= 0x20 && (bass_root&0xF0) <= 0x40 && (bass_root&0xF) >= 1 && (bass_root&0xF) <= 7 && bass_chord != 34 ?
						new ChordBody(bass_root, bass_chord) : null;
		refreshChord(main, bass, inversion, fingering);
	}

	/** Refresh a chord. */
	public void refreshChord(ChordBody main, ChordBody bass, int inversion, byte fingering) {
		this.main_ph = main;
		this.bass_ph = bass;
		this.inversion = inversion;
		this.finger = fingering;

		for(int i=0;i<notes_down_chord.length;i+=1) {
			notes_down_chord[i] = false;
			if(keys[i] != null && !notes_down_melody[i])
				keys[i].depressed = false;
		}

		if(main_ph != null) {
			byte[] notes = main_ph.getInversion(this.inversion, FWSNoteEvent.yamaha_split);
			final byte full_root = main_ph.getFullRoot();
			

			for(int i=0;i<notes.length;i+=1) {
				notes_down_chord[notes[i]] = true;
				if(keys[notes[i]] != null)
					keys[notes[i]].depressed = true;
			}

			chord_root_num = (full_root&0xF) - 1;
			chord_num = main_ph.getChord();
			switch(full_root&0xF0) {
			case 0x20:
				chord_root_isflat = true;
				chord_root_issharp = false;
				break;
			case 0x40:
				chord_root_isflat = false;
				chord_root_issharp = true;
				break;
			default:
				chord_root_isflat = false;
				chord_root_issharp = false;
				break;
			}
		} else {
			this.chord_root_num = -1;
			this.chord_root_isflat = false;
			this.chord_root_issharp = false;
			
			this.chord_num = -1;
		}

		if(bass_ph != null) {
			final byte full_root = bass_ph.getFullRoot();
			bass_root_num = (full_root&0xF) - 1;
			switch(full_root&0xF0) {
			case 0x20:
				bass_root_isflat = true;
				bass_root_issharp = false;
				break;
			case 0x40:
				bass_root_isflat = false;
				bass_root_issharp = true;
				break;
			default:
				bass_root_isflat = false;
				bass_root_issharp = false;
				break;
			}
		} else {
			this.bass_root_num = -1;
			this.bass_root_isflat = false;
			this.bass_root_issharp = false;
		}
		
		changed = true;
	}

	/** Chord cleared. */
	public void clearChord() {
		this.chord_root_num = -1;
		this.bass_root_num = -1;
		this.chord_root_isflat = false;
		this.chord_root_issharp = false;
		this.bass_root_isflat = false;
		this.bass_root_issharp = false;
		
		this.chord_num = -1;
		
		this.main_ph = null;
		this.bass_ph = null;
		
		for(int i=0;i<notes_down_chord.length;i+=1) {
			notes_down_chord[i] = false;
			if(keys[i] != null && !notes_down_melody[i])
				keys[i].depressed = false;
		}
		
		changed = true;
	}

	/** Refresh the key signature. */
	public void refreshKeySig(final byte new_sig) {
		this.key_sig = new_sig;
		changed = true;
	}
}