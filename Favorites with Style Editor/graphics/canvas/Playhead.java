package canvas;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.Timer;

import fwsevents.FWSSequence;
import options.MIDIPlayerOptions;

public class Playhead extends JPanel {
	private static final long serialVersionUID = 1674446904478034988L;
	
	private MIDIPlayerOptions options;
	private SongViewPort parent;
	private Container container;
	
	private long current_tick;

	public Playhead(SongViewPort parent, Container container, MIDIPlayerOptions options) {
		this.parent = parent;
		this.options = options;
		this.container = container;

		this.current_tick = options.play ? options.current_tick : options.start_tick;
		
		this.refresh();

		Timer refresh_timer = new Timer(40, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final long tick = options.play ? options.current_tick : options.start_tick;
				if(tick != current_tick) {
					current_tick = tick;
					refresh();
				}
			}
		});
		refresh_timer.start();
	}

	/** Refresh the playhead. */
	private void refresh() {
		FWSSequence sequence = parent.getActiveSequence();
		if(sequence == null)
			return;

		final long tick = options.play ? options.current_tick : options.start_tick;
		final int x = sequence.getXPosition(tick, parent.getController().getGlobalOptions().ppq);

		this.setBounds(x, 0, 1, container.getHeight());
		this.repaint();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		g.setColor(Color.RED);
		g.drawLine(0, 0, 0, container.getHeight());
	}
}
