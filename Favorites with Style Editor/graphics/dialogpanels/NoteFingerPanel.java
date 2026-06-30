package dialogpanels;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class NoteFingerPanel extends JPanel {
	private static final long serialVersionUID = 5565671394070234495L;
	
	private Image br_image, finger_image;
	private boolean rh = false;

	private byte finger = 0;

	public NoteFingerPanel(final boolean rh) {
		this((byte)0, rh);
	}

	public NoteFingerPanel(final byte finger, final boolean rh) {
		this.finger = (byte) (finger&0x1F);
		this.rh= rh;
		try {
			br_image = ImageIO.read(getClass().getResource("/icons/" + (rh ? "Right Hand Canvas.png" : "Left Hand Canvas.png")));
			finger_image = ImageIO.read(getClass().getResource("/icons/" + (rh ? "Right Hand Finger.png" : "Left Hand Finger.png")));
		} catch (IOException e) {

		}

		if(br_image == null || finger_image == null)
			return;

		NoteFingerPanel self = this;

		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(finger_image == null)
					return;

				final int x = e.getX(), y = e.getY();
				byte new_finger = -1;

				for(byte i=1;i<=5;i+=1) {
					int[] test_pos = getFingerXY(i);
					final int test_x = test_pos[0], test_y = test_pos[1];

					if(x >= test_x && x < test_x + finger_image.getWidth(null) && y >= test_y && y < test_y + finger_image.getHeight(null)) {
						new_finger = i;
						break;
					}
				}

				if(new_finger < 0)
					return;

				if(new_finger == finger)
					new_finger = 0;
				
				self.finger = new_finger;
				repaint();
			}
		});
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		if(br_image != null)
			g.drawImage(br_image, 0, 0, br_image.getWidth(null), br_image.getHeight(null), null);

		int[] fingers = getFingerXY(finger);
		final int finger_x = fingers[0], finger_y = fingers[1];
		
		if(finger_image != null && finger_x >= 0 && finger_y >= 0)
			g.drawImage(finger_image, finger_x, finger_y, finger_image.getWidth(null), finger_image.getHeight(null), null);
	}

	/** Get the selected finger. */
	public byte getFinger() {
		return finger;
	}

	/** Get the width. */
	public int getWidth() {
		if(br_image != null)
			return br_image.getWidth(null);
		else
			return 0;
	}

	/** Get the width. */
	public int getHeight() {
		if(br_image != null)
			return br_image.getHeight(null);
		else
			return 0;
	}

	/** Get the X and Y coordinates of the specified finger. */
	protected int[] getFingerXY(final byte finger) {
		int finger_x = -1, finger_y = -1;

		if(rh) {
			switch(finger) {
			case 1:
				finger_x = 8;
				finger_y = 30;
				break;
			case 2:
				finger_x = 21;
				finger_y = 11;
				break;
			case 3:
				finger_x = 30;
				finger_y = 9;
				break;
			case 4:
				finger_x = 39;
				finger_y = 12;
				break;
			case 5:
				finger_x = 48;
				finger_y = 21;
				break;
			}
		} else {
			switch(finger) {
			case 1:
				finger_x = 53;
				finger_y = 30;
				break;
			case 2: 
				finger_x = 40;
				finger_y = 11;
				break;
			case 3:
				finger_x = 31;
				finger_y = 9;
				break;
			case 4:
				finger_x = 22;
				finger_y = 12;
				break;
			case 5:
				finger_x = 13;
				finger_y = 21;
				break;
			}
		}

		return new int[] {finger_x, finger_y};
	}
}
