package dialogpanels;

import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class EventDialogSignature extends JPanel {
	private static final long serialVersionUID = -932674320851796705L;
	
	private Image staff;
	private Image sharp, flat;
	private int acc_count = 0;
	
	private static final int staff_start_x = 30;
	
	public EventDialogSignature() {
		try {
			staff = ImageIO.read(getClass().getResource("/icons/Key Signature Canvas.png"));
			sharp = ImageIO.read(getClass().getResource("/icons/Sharp.png"));
			flat = ImageIO.read(getClass().getResource("/icons/Flat.png"));
		} catch (IOException e) {
			staff = null;
			sharp = null;
			flat = null;
			//Something went really wrong here...
		}
		
		this.setSize(160,52);
	}

	/** Set the accidental count. */
	public void setAccidentalCount(final int count) {
		this.acc_count = count;
		this.repaint();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		if(staff!=null)
			g.drawImage(staff, staff_start_x, 0, 104, 52, null);
		if(sharp!=null) {
			if(acc_count >= 1) //F#
				g.drawImage(sharp, 27 + staff_start_x, 3, 8, 20, null);
			if(acc_count >= 2) //C#
				g.drawImage(sharp, 35 + staff_start_x, 15, 8, 20, null);
			if(acc_count >= 3) //G#
				g.drawImage(sharp, 43 + staff_start_x, 1, 8, 20, null);
			if(acc_count >= 4) //D#
				g.drawImage(sharp, 51 + staff_start_x, 10, 8, 20, null);
			if(acc_count >= 5) //A#
				g.drawImage(sharp, 59 + staff_start_x, 19, 8, 20, null);
			if(acc_count >= 6) //E#
				g.drawImage(sharp, 67 + staff_start_x, 8, 8, 20, null);
			if(acc_count >= 7) //B#
				g.drawImage(sharp, 75 + staff_start_x, 17, 8, 20, null);
		} if(flat!=null) {
			if(acc_count <= -1) //Bb
				g.drawImage(flat, 29 + staff_start_x, 14, 10, 20, null);
			if(acc_count <= -2) //Eb
				g.drawImage(flat, 38 + staff_start_x, 4, 10, 20, null);
			if(acc_count <= -3) //Ab
				g.drawImage(flat, 47 + staff_start_x, 16, 10, 20, null);
			if(acc_count <= -4) //Db
				g.drawImage(flat, 56 + staff_start_x, 7, 10, 20, null);
			if(acc_count <= -5) //Gb
				g.drawImage(flat, 65 + staff_start_x, 20, 10, 20, null);
			if(acc_count <= -6) //Cb
				g.drawImage(flat, 74 + staff_start_x, 11, 10, 20, null);
			if(acc_count <= -7) //Fb
				g.drawImage(flat, 83 + staff_start_x, 23, 10, 20, null);
		}
	}
}
