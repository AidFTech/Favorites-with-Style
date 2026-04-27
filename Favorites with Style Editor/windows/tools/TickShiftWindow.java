package tools;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JDialog;

import fwsevents.FWSEvent;
import fwsevents.FWSKeySignatureEvent;
import fwsevents.FWSSectionNameEvent;
import fwsevents.FWSSequence;
import fwsevents.FWSTempoEvent;
import fwsevents.FWSTimeSignatureEvent;
import fwsevents.FWSVoiceEvent;
import main_window.FWSEditorMainWindow;
import javax.swing.JLabel;

import event_dialogs.SequenceTickPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.JButton;

public class TickShiftWindow extends JDialog {
	private static final long serialVersionUID = 4478650767232142472L;

	public TickShiftWindow(FWSEditorMainWindow parent, FWSSequence sequence, final boolean style_shift) {
		super(parent, true);
		
		if(sequence.seqEmpty())
			return;

		this.setTitle("Tick Shift");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(380, 500));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);
		
		JLabel label_shift_from = new JLabel("Shift Events From");
		label_shift_from.setBounds(12, 12, 137, 32);
		getContentPane().add(label_shift_from);

		SequenceTickPanel start_tick_panel = new SequenceTickPanel(parent, sequence, 0, 12, 56, 326, 100);
		start_tick_panel.setBounds(12, 56, 326, 100);
		getContentPane().add(start_tick_panel);
		
		JLabel label_to = new JLabel("to");
		label_to.setBounds(12, 168, 60, 32);
		getContentPane().add(label_to);

		SequenceTickPanel end_tick_panel = new SequenceTickPanel(parent, sequence, sequence.getSequenceLength(), 12, 200, 326, 100);
		end_tick_panel.setBounds(12, 212, 326, 100);
		getContentPane().add(end_tick_panel);
		
		JLabel label_by = new JLabel("by");
		label_by.setBounds(12, 324, 60, 32);
		getContentPane().add(label_by);
		
		JSpinner spinner_measure = new JSpinner();
		spinner_measure.setBounds(12, 368, 76, 32);
		spinner_measure.setModel(new SpinnerNumberModel(Integer.valueOf(0), null, null, Integer.valueOf(1)));
		spinner_measure.setToolTipText("Set the number of measures to shift the events by.");
		getContentPane().add(spinner_measure);
		
		JSpinner spinner_tick = new JSpinner();
		spinner_tick.setModel(new SpinnerNumberModel(Long.valueOf(0), null, null, Long.valueOf(1)));
		spinner_tick.setToolTipText("Set the number of ticks to shift the events by.");
		spinner_tick.setBounds(100, 368, 76, 32);
		getContentPane().add(spinner_tick);
		
		JLabel label_measures = new JLabel("Measures");
		label_measures.setVerticalAlignment(SwingConstants.TOP);
		label_measures.setBounds(12, 412, 76, 32);
		getContentPane().add(label_measures);
		
		JLabel label_ticks = new JLabel("Ticks");
		label_ticks.setVerticalAlignment(SwingConstants.TOP);
		label_ticks.setBounds(100, 412, 76, 32);
		getContentPane().add(label_ticks);

		TickShiftWindow self = this;
		
		JButton button_cancel = new JButton("Cancel");
		button_cancel.setBounds(146, 453, 105, 35);
		button_cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_cancel);
		
		JButton button_apply = new JButton("Apply");
		button_apply.setBounds(263, 453, 105, 35);
		button_apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final long start_tick = start_tick_panel.getSetTick(), end_tick = end_tick_panel.getSetTick();
				final int m_shift = (Integer)spinner_measure.getValue();
				final long tick_shift = (Long)spinner_tick.getValue();

				long[] measures = sequence.getMeasureTicks();
				final int measure_count = measures.length;

				ArrayList<FWSEvent> events = sequence.getAllEvents();

				for(int ev=0;ev<events.size();ev+=1) {
					if(events.get(ev) instanceof FWSTimeSignatureEvent || events.get(ev) instanceof FWSSectionNameEvent)
						continue;

					FWSEvent event = events.get(ev);
					if(event.tick < start_tick || event.tick > end_tick)
						continue;

					int m = sequence.getMeasureAt(event.tick);
					final long init_measure_tick = measures[m]; 
					
					m += m_shift;
					if(m < 0)
						m = 0;
					else if(m >= measure_count)
						m = measure_count - 1;
					
					final long new_measure_tick = measures[m];
					event.tick = (event.tick - init_measure_tick) + new_measure_tick;

					long new_tick = event.tick + tick_shift;
					if(new_tick < 0)
						new_tick = 0;

					if((event instanceof FWSKeySignatureEvent || event instanceof FWSTempoEvent || event instanceof FWSVoiceEvent) && event.tick == 0)
						new_tick = 0;

					event.tick = new_tick;

					parent.getViewPort().refreshSprite(event);
				}

				sequence.setEndEvent(sequence.getSequenceLength());

				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_apply);

		this.setVisible(true);
	}
}
