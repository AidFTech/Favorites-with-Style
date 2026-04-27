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

import canvas.SongViewPort;
import fwsevents.FWSSequence;
import fwsevents.FWSShortEvent;
import main_window.FWSEditorMainWindow;

public class ChannelPressureEventDialog extends JDialog {
	private static final long serialVersionUID = -1572714061476477597L;

	public ChannelPressureEventDialog(FWSEditorMainWindow parent, FWSShortEvent fws_event) {
		super(parent, true);

		this.setTitle("Channel Pressure/Aftertouch Event");
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
		
		JSpinner spinner_pressure = new JSpinner();
		spinner_pressure.setModel(new SpinnerNumberModel(fws_event.data1, 0, 127, 1));
		spinner_pressure.setToolTipText("Set the pressure.");
		spinner_pressure.setBounds(89, 59, 73, 35);
		getContentPane().add(spinner_pressure);
		
		JLabel label_pressure = new JLabel("Pressure");
		label_pressure.setHorizontalAlignment(SwingConstants.RIGHT);
		label_pressure.setBounds(12, 59, 60, 35);
		getContentPane().add(label_pressure);
		
		SequenceTickPanel tick_panel = new SequenceTickPanel(parent, fws_event.tick, 12, 141, 326, 100);
		tick_panel.setBounds(12, 141, 326, 100);
		getContentPane().add(tick_panel);

		ChannelPressureEventDialog self = this;

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

				fws_event.data1 = ((Integer)spinner_pressure.getValue()).byteValue();
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

		this.setVisible(true);
	}
}
