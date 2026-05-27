package event_dialogs;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;

import canvas.SongViewPort;
import fwsevents.FWSEvent;
import fwsevents.FWSKeySignatureEvent;
import fwsevents.FWSSequence;
import fwsevents.FWSTempoEvent;
import fwsevents.FWSTimeSignatureEvent;
import main_window.FWSEditorMainWindow;

public class MultiEventDialog extends JDialog {
	private static final long serialVersionUID = -4753776659292284696L;
	
	private boolean refresh = false;

	public MultiEventDialog(FWSEditorMainWindow parent, FWSEvent[] events) {
		super(parent, true);

		if(events.length <= 0)
			return;

		long tick = events[0].tick;
		final long set_tick = tick;
		for(int i=0;i<events.length;i+=1) {
			if(events[i].tick != tick) {
				tick = -1;
				break;
			}
		}

		this.setTitle("Event Properties");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(385, 175));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);

		MultiTickPanel tick_panel = new MultiTickPanel(parent, set_tick, 12, 12, 326, 100, tick<0);
		tick_panel.setBounds(12, 12, 326, 100);
		getContentPane().add(tick_panel);

		MultiEventDialog self = this;

		JButton button_cancel = new JButton("Cancel");
		button_cancel.setBounds(151, 128, 105, 35);
		button_cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_cancel);

		JButton button_apply = new JButton("Apply");
		button_apply.setBounds(268, 128, 105, 35);
		button_apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				refresh = true;

				SongViewPort vp = parent.getViewPort();
				FWSSequence sequence = vp.getActiveSequence();

				for(int i=0;i<events.length;i+=1) {
					long new_tick = events[i].tick;

					if(new_tick == 0 && (events[i] instanceof FWSTimeSignatureEvent || events[i] instanceof FWSKeySignatureEvent || events[i] instanceof FWSTempoEvent))
						continue;

					if(tick_panel.relative) {
						new_tick = events[i].tick + tick_panel.getSetTick();
						if(new_tick < 0)
							new_tick = 0;
						if(new_tick >= sequence.getSequenceLength())
							new_tick = sequence.getSequenceLength() - 1;
					} else
						new_tick = tick_panel.getSetTick();

					if(events[i] instanceof FWSTimeSignatureEvent) {
						FWSTimeSignatureEvent original = new FWSTimeSignatureEvent((FWSTimeSignatureEvent)events[i]);
						final int new_measure = sequence.getMeasureAt(new_tick);

						long[] measures = sequence.getMeasureTicks();
						events[i].tick = measures[new_measure];
						sequence.refreshTimeSignatures((FWSTimeSignatureEvent)events[i], original, false);
					} else {
						events[i].tick = new_tick;
						sequence.refreshEvent(events[i]);
					}

					vp.refreshSprite(events[i]);
				}

				vp.refresh();
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		this.getContentPane().add(button_apply);

		this.setVisible(true);
	}

	/** Return whether "Apply" was clicked. */
	public boolean getRefresh() {
		return refresh;
	}
}
