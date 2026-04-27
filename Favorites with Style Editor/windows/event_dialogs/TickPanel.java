package event_dialogs;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fwsevents.FWSSequence;
import fwsevents.FWSTimeSignatureEvent;
import main_window.FWSEditorMainWindow;

public abstract class TickPanel extends JPanel {
	private static final long serialVersionUID = -4462281577912783733L;

	private FWSEditorMainWindow fws_parent;
	protected FWSSequence parent_sequence;

	protected JLabel label_tick;
	protected JSpinner spinner_measure = new JSpinner(), spinner_beat = new JSpinner(), spinner_tick = new JSpinner();

	protected boolean split_spinners = false;

	protected long current_tick;
	
	public TickPanel(FWSEditorMainWindow parent, final long initial_tick, final int x, final int y, final int w, final int h) {
		this(parent, parent.getSequence(), initial_tick, x, y, w, h);
	}

	public TickPanel(FWSEditorMainWindow parent, FWSSequence parent_sequence, final long initial_tick, final int x, final int y, final int w, final int h) {
		this.setBounds(x, y, w, h);
		this.fws_parent = parent;
		this.parent_sequence = parent_sequence;

		this.current_tick = initial_tick;
		setLayout(null);

		if(parent_sequence == null) {
			JOptionPane.showMessageDialog(fws_parent, "There is no FWS event sequence loaded.", "Internal Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		JCheckBox checkbox_split = new JCheckBox("Split");
		checkbox_split.setBounds(12, 60, 100, 35);
		checkbox_split.setSelected(true);
		checkbox_split.setToolTipText("Check to define the tick as a function of measure, beat, and modulo tick.");
		this.add(checkbox_split);

		checkbox_split.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(checkbox_split.isSelected())
					createSplitSpinners();
				else
					createSingleSpinner();
			}
		});

		label_tick = new JLabel("Tick");
		label_tick.setBounds(12, 12, 42, 35);
		this.add(label_tick);

		createSplitSpinners();
	}

	/** Create the spinners for a split tick. */
	protected void createSplitSpinners() {
		final boolean last_split = split_spinners;
		split_spinners = true;
		
		boolean add_listeners = false;
		
		if(!last_split)
			add_listeners = true;

		if(add_listeners) {
			Component components[] = getComponents();
			for(int i=0;i<components.length;i+=1) {
				if(components[i] == spinner_measure)
					this.remove(spinner_measure);
				if(components[i] == spinner_beat)
					this.remove(spinner_beat);
				if(components[i] == spinner_tick)
					this.remove(spinner_tick);
			}
		}

		FWSSequence active_sequence = parent_sequence;
		long[] measures = active_sequence.getMeasureTicks();

		if(!last_split || spinner_measure == null)
			spinner_measure = new JSpinner();
		
		spinner_measure.setBounds(54, 12, 76, 37);
		spinner_measure.setModel(new SpinnerNumberModel(active_sequence.getMeasureAt(current_tick) + 1, 1, measures.length, 1));
		spinner_measure.setToolTipText("Set the measure at which the event occurs.");
		if(!last_split)
			this.add(spinner_measure);
		
		if(!last_split || spinner_beat == null)
			spinner_beat = new JSpinner();
		
		spinner_beat.setBounds(142, 12, 60, 37);
		FWSTimeSignatureEvent time_signature = active_sequence.getTimeSignatureAt(current_tick);
		if(time_signature != null)
			spinner_beat.setModel(new SpinnerNumberModel(active_sequence.getBeatAt(current_tick) + 1, 1, time_signature.num, 1));
		else
			spinner_beat.setModel(new SpinnerNumberModel(active_sequence.getBeatAt(current_tick) + 1, 1, 4, 1));
		
		spinner_beat.setToolTipText("Set the beat at which the event occurs.");
		if(!last_split)
			this.add(spinner_beat);
		
		if(!last_split || spinner_tick == null)
			spinner_tick = new JSpinner();
		
		spinner_tick.setBounds(214, 12, 90, 37);
		long tick_max = active_sequence.getTPQ();
		
		if(time_signature != null)
			tick_max = (int)(active_sequence.getTPQ()/(Math.pow(2.0, time_signature.den)/4.0));
		
		final long set_tick = active_sequence.getTickAt(current_tick) + 1;
		spinner_tick.setModel(new SpinnerNumberModel(Long.valueOf(set_tick <= tick_max ? set_tick : tick_max), Long.valueOf(1), Long.valueOf(tick_max), Long.valueOf(1)));
		spinner_tick.setToolTipText("Set the tick at which the event occurs, out of " + active_sequence.getTPQ() + ".");
		if(!last_split)
			this.add(spinner_tick);
		
		if(add_listeners) {
			spinner_measure.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					current_tick = calculateTick(active_sequence, (Integer)spinner_measure.getValue() - 1, (Integer)spinner_beat.getValue() - 1, (Long)spinner_tick.getValue() - 1);
					createSplitSpinners();
				}
			});

			spinner_beat.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					current_tick = calculateTick(active_sequence, (Integer)spinner_measure.getValue() - 1, (Integer)spinner_beat.getValue() - 1, (Long)spinner_tick.getValue() - 1);
				}
			});

			spinner_tick.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					current_tick = calculateTick(active_sequence, (Integer)spinner_measure.getValue() - 1, (Integer)spinner_beat.getValue() - 1, (Long)spinner_tick.getValue() - 1);
				}
			});
		}

		this.revalidate();
		this.repaint();
	}

	/** Create a single tick spinner. */
	protected void createSingleSpinner() {
		final boolean last_split = split_spinners;
		split_spinners = false;
		
		boolean add_listeners = false;

		if(last_split)
			add_listeners = true;

		if(add_listeners) {
			Component components[] = getComponents();
			for(int i=0;i<components.length;i+=1) {
				if(components[i] == spinner_measure)
					this.remove(spinner_measure);
				if(components[i] == spinner_beat)
					this.remove(spinner_beat);
				if(components[i] == spinner_tick)
					this.remove(spinner_tick);
			}
		}

		if(last_split || spinner_tick == null)
			spinner_tick = new JSpinner();
		
		spinner_tick.setBounds(54, 12, 100, 37);
		final long tick_max = parent_sequence.getSequenceLength();
			
		spinner_tick.setModel(new SpinnerNumberModel(Long.valueOf(current_tick), Long.valueOf(0), Long.valueOf(tick_max-1), Long.valueOf(1)));
		spinner_tick.setToolTipText("Set the tick at which the event occurs.");
		if(last_split)
			this.add(spinner_tick);

		if(add_listeners) {
			spinner_tick.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					current_tick = (Long)spinner_tick.getValue();
				}
			});
		}

		this.revalidate();
		this.repaint();
	}
	
	/** Get the current tick calculated from spinner values. */
	protected static long calculateTick(FWSSequence sequence, final int m, final int b, final long t) {
		long[] measures = sequence.getMeasureTicks();
		long new_tick = measures[m];
		
		FWSTimeSignatureEvent time_signature = sequence.getTimeSignatureAt(new_tick);
		if(time_signature != null)
			new_tick += sequence.getTPQ()*(b/(Math.pow(2,time_signature.den)/4.0));
		else
			new_tick += sequence.getTPQ()*b;
		
		new_tick += t;
		return new_tick;
	}

	/** Set the active sequence. */
	protected void setSequence(FWSSequence sequence) {
		final int old_tpq = parent_sequence.getTPQ();
		this.parent_sequence = sequence;

		final int tpq = sequence.getTPQ();

		this.current_tick = this.current_tick*tpq/old_tpq;

		final long length = sequence.getSequenceLengthStrict();
		if(current_tick > length)
			current_tick = 0;
		
		if(split_spinners)
			createSplitSpinners();
		else
			createSingleSpinner();
	}

	/**Set the tick. */
	public void setTick(final long tick) {
		this.current_tick = tick;
		if(split_spinners)
			createSplitSpinners();
		else
			createSingleSpinner();
	}

	/** Get the set tick. */
	public long getSetTick() {
		return this.current_tick;
	}
}
