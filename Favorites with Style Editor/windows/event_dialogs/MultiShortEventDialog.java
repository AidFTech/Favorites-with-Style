package event_dialogs;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import canvas.SongViewPort;
import fwsevents.FWSEvent;
import fwsevents.FWSNoteEvent;
import fwsevents.FWSSequence;
import fwsevents.FWSShortEvent;
import fwsevents.FWSVoiceEvent;
import main_window.FWSEditorMainWindow;

public class MultiShortEventDialog extends JDialog {
	private static final long serialVersionUID = 7309390309223926412L;
	private JSpinner spinner_channel;

	private boolean refresh = false;

	public MultiShortEventDialog(FWSEditorMainWindow parent, FWSEvent[] fws_events) {
		super(parent, true);

		this.setTitle("Short Events");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(385, 230));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);

		if(fws_events.length <= 0)
			return;

		byte channel = -1;

		if(fws_events[0] instanceof FWSShortEvent)
			channel = ((FWSShortEvent)fws_events[0]).channel;
		else if(fws_events[0] instanceof FWSNoteEvent)
			channel = ((FWSNoteEvent)fws_events[0]).channel;
		else if(fws_events[0] instanceof FWSVoiceEvent)
			channel = ((FWSVoiceEvent)fws_events[0]).channel;

		final byte set_channel = channel;
		for(int i=0;i<fws_events.length;i+=1) {
			if(fws_events[i] instanceof FWSShortEvent) {
				if(((FWSShortEvent)fws_events[i]).channel != channel) {
					channel = -1; //Keep channel option.
					break;
				}
			} else if(fws_events[i] instanceof FWSNoteEvent) {
				if(((FWSNoteEvent)fws_events[i]).channel != channel) {
					channel = -1; //Keep channel option.
					break;
				}
			} else if(fws_events[i] instanceof FWSVoiceEvent) {
				if(((FWSVoiceEvent)fws_events[i]).channel != channel) {
					channel = -1; //Keep channel option.
					break;
				}
			}
		}

		spinner_channel = new JSpinner();
		spinner_channel.setBounds(89, 12, 73, 35);
		spinner_channel.setToolTipText("Select the event channel.");
		setChannelSpinner(getContentPane(), spinner_channel, channel);
		getContentPane().add(spinner_channel);
		
		JCheckBox checkbox_keep_channel = new JCheckBox("Keep Channel");
		checkbox_keep_channel.setToolTipText("Check to keep the channel of the selected events.");
		checkbox_keep_channel.setBounds(170, 12, 157, 35);
		getContentPane().add(checkbox_keep_channel);

		checkbox_keep_channel.setSelected(channel < 0);
		checkbox_keep_channel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(checkbox_keep_channel.isSelected())
					setChannelSpinner(getContentPane(), spinner_channel, (byte)-1);
				else
					setChannelSpinner(getContentPane(), spinner_channel, set_channel);
			}
		});

		long tick = fws_events[0].tick;
		final long set_tick = tick;
		for(int i=0;i<fws_events.length;i+=1) {
			if(fws_events[i].tick != tick) {
				tick = -1;
				break;
			}
		}

		MultiTickPanel tick_panel = new MultiTickPanel(parent, tick >= 0 ? tick : set_tick, 12, 280, 326, 100, tick < 0);
		tick_panel.setBounds(12, 71, 326, 100);
		getContentPane().add(tick_panel);

		MultiShortEventDialog self = this;

		JButton button_cancel = new JButton("Cancel");
		button_cancel.setBounds(151, 183, 105, 35);
		button_cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_cancel);

		JButton button_apply = new JButton("Apply");
		button_apply.setBounds(268, 183, 105, 35);
		button_apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SongViewPort vp = parent.getViewPort();
				FWSSequence sequence = vp.getActiveSequence();

				refresh = true;

				byte new_channel = -1;
				if(!checkbox_keep_channel.isSelected())
					new_channel = (byte)((Integer)spinner_channel.getValue() - 1);

				for(FWSEvent fws_event: fws_events) {
					if(fws_event instanceof FWSShortEvent) {
						FWSShortEvent short_event = (FWSShortEvent)fws_event;
						if(new_channel >= 0)
							short_event.channel = new_channel;
					} else if(fws_event instanceof FWSNoteEvent) {
						FWSNoteEvent note_event = (FWSNoteEvent)fws_event;
						if(new_channel >= 0)
							note_event.channel = new_channel;
					} else if(fws_event instanceof FWSVoiceEvent) {
						FWSVoiceEvent voice_event = (FWSVoiceEvent)fws_event;
						if(new_channel >= 0)
							voice_event.channel = new_channel;
					}

					if(tick_panel.relative) {
						long new_tick = fws_event.tick + tick_panel.getSetTick();
						if(new_tick < 0)
							new_tick = 0;
						if(new_tick >= sequence.getSequenceLength())
							new_tick = sequence.getSequenceLength();

						fws_event.tick = new_tick;
					} else {
						fws_event.tick = tick_panel.getSetTick();
					}

					sequence.refreshEvent(fws_event);
					vp.refreshSprite(fws_event);
				}
				
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_apply);
		
		this.setVisible(true);
	}

	/** Return whether "Apply" was clicked. */
	public boolean getRefresh() {
		return refresh;
	}

	/** Initialize the channel spinner. */
	private static void setChannelSpinner(Container parent, JSpinner spinner, final byte channel) {
		if(channel >= 0 && channel < 16) {
			spinner.setModel(new SpinnerNumberModel(channel + 1, 1, 16, 1));
			spinner.setVisible(true);
		} else {
			spinner.setVisible(false);
		}
	}
}
