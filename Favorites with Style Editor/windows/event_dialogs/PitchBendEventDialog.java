package event_dialogs;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import fwsevents.FWSSequence;
import fwsevents.FWSShortEvent;
import main_window.FWSEditorMainWindow;
import javax.swing.JSlider;
import javax.swing.event.ChangeListener;

import canvas.SongViewPort;

import javax.swing.event.ChangeEvent;

public class PitchBendEventDialog extends JDialog {
	private static final long serialVersionUID = 3001011695817082327L;
	private boolean slider_listen = false;

	public PitchBendEventDialog(FWSEditorMainWindow parent, FWSShortEvent fws_event) {
		super(parent, true);

		this.setTitle("Pitch Bend Event");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(365, 310));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);

		JLabel label_channel = new JLabel("Channel");
		label_channel.setHorizontalAlignment(SwingConstants.RIGHT);
		label_channel.setBounds(12, 12, 60, 35);
		getContentPane().add(label_channel);
		
		JSpinner spinner_channel = new JSpinner();
		spinner_channel.setModel(new SpinnerNumberModel(fws_event.channel + 1, 1, 16, 1));
		spinner_channel.setBounds(89, 12, 73, 35);
		spinner_channel.setToolTipText("Select the event channel.");
		getContentPane().add(spinner_channel);
		
		JLabel label_pitch_bend = new JLabel("Pitch Bend");
		label_pitch_bend.setHorizontalAlignment(SwingConstants.RIGHT);
		label_pitch_bend.setBounds(12, 59, 79, 35);
		getContentPane().add(label_pitch_bend);

		final int pb = fws_event.data2*0x80 + fws_event.data1 - 8192;
		
		JSpinner spinner_pitch_bend = new JSpinner();
		spinner_pitch_bend.setModel(new SpinnerNumberModel(pb, -8192, 8191, 1));
		spinner_pitch_bend.setToolTipText("Set the pitch bend.");
		spinner_pitch_bend.setBounds(225, 59, 73, 35);
		getContentPane().add(spinner_pitch_bend);
		
		JSlider slider_bend = new JSlider();
		slider_bend.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				slider_listen = false;
				spinner_pitch_bend.setValue(slider_bend.getValue());
				slider_listen = true;
			}
		});
		slider_bend.setMaximum(8191);
		slider_bend.setMinimum(-8192);
		slider_bend.setValue(pb);
		slider_bend.setToolTipText("Adjust the pitch bend.");
		slider_bend.setBounds(22, 106, 200, 16);
		getContentPane().add(slider_bend);
		
		spinner_pitch_bend.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(slider_listen)
					slider_bend.setValue((Integer)spinner_pitch_bend.getValue());
			}
		});

		SequenceTickPanel tick_panel = new SequenceTickPanel(parent, fws_event.tick, 12, 141, 326, 100);
		tick_panel.setBounds(12, 141, 326, 100);
		getContentPane().add(tick_panel);

		PitchBendEventDialog self = this;

		JButton button_cancel = new JButton("Cancel");
		button_cancel.setBounds(131, 263, 105, 35);
		button_cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_cancel);
		
		JButton button_apply = new JButton("Apply");
		button_apply.setBounds(248, 263, 105, 35);
		button_apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				final long new_tick = tick_panel.getSetTick();

				SongViewPort vp = parent.getViewPort();
				FWSSequence sequence = vp.getActiveSequence();

				final int new_pb = (Integer)spinner_pitch_bend.getValue() + 8192;
				
				fws_event.data1 = (byte) (new_pb%0x80);
				fws_event.data2 = (byte)(new_pb/0x80);
				fws_event.channel = (byte)((Integer)spinner_channel.getValue() - 1);

				fws_event.tick = new_tick;
				if(sequence.getEvent(fws_event))
					sequence.refreshEvent(fws_event);
				else
					sequence.addEvent(fws_event);

				vp.refreshSprite(fws_event);
				vp.refresh();
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_apply);
		
		slider_listen = true;
		this.setVisible(true);
	}
}
