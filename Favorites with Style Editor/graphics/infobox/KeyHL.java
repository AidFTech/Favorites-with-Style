package infobox;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class KeyHL extends JPanel {
	private static final long serialVersionUID = 2217060499346078499L;

	private static Image key_hl_black, key_hl_white;
	private static boolean loaded = false;

	private Image key_hl;

	protected boolean depressed = false;

	protected KeyHL(final int x, final int y, final boolean black) {
		try {
			if(!loaded) {
				key_hl_white = ImageIO.read(getClass().getResource("/infobox/key_white_hl.png"));
				key_hl_black = ImageIO.read(getClass().getResource("/infobox/key_black_hl.png"));
				loaded = true;
			}
		} catch (IOException e) {
			//Something went very wrong.
		}

		key_hl = black ? key_hl_black : key_hl_white;

		this.setBounds(x, y, key_hl.getWidth(null), key_hl.getHeight(null));
		this.setOpaque(false);

		Timer refresh_timer = new Timer(20, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				repaint();
			}
		});
		refresh_timer.start();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		if(depressed && key_hl != null) {
			g.drawImage(key_hl, 0, 0, null);
		}
	}
}
