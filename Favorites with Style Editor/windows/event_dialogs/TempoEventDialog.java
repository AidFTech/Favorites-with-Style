package event_dialogs;

import javax.swing.JDialog;

import fwsevents.FWSSequence;
import fwsevents.FWSTempoEvent;
import main_window.FWSEditorMainWindow;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import canvas.SongViewPort;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.SwingConstants;

public class TempoEventDialog extends JDialog {
	private static final long serialVersionUID = 5182257518888343038L;

	public TempoEventDialog(FWSEditorMainWindow parent, FWSTempoEvent fws_event) {
		super(parent, true);

		this.setTitle("Tempo Event");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(365, 225));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);
		
		JLabel label_tempo = new JLabel("Tempo");
		label_tempo.setHorizontalAlignment(SwingConstants.RIGHT);
		label_tempo.setBounds(12, 12, 72, 35);
		getContentPane().add(label_tempo);
		
		JSpinner spinner_tempo = new JSpinner();
		spinner_tempo.setToolTipText("Set the event tempo.");
		spinner_tempo.setModel(new SpinnerNumberModel(fws_event.tempo, 11, 280, 1));
		spinner_tempo.setBounds(90, 12, 80, 35);
		getContentPane().add(spinner_tempo);
		
		SequenceTickPanel tick_panel = new SequenceTickPanel(parent, fws_event.tick, 12, 59, 326, 100);
		tick_panel.setBounds(12, 59, 326, 100);
		if(fws_event.tick > 0)
			getContentPane().add(tick_panel);

		TempoEventDialog self = this;
		
		JButton button_cancel = new JButton("Cancel");
		button_cancel.setBounds(131, 178, 105, 35);
		button_cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_cancel);
		
		JButton button_apply = new JButton("Apply");
		button_apply.setBounds(248, 178, 105, 35);
		button_apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				final int new_tempo = (Integer)spinner_tempo.getValue();
				final long new_tick = tick_panel.getSetTick();

				SongViewPort vp = parent.getViewPort();
				FWSSequence sequence = vp.getActiveSequence();
								
				fws_event.tempo = new_tempo;

				if(new_tick == 0 && fws_event.tick != 0) {
					final int answer = JOptionPane.showConfirmDialog(self, "Replace the initial tempo event?", "Tempo", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if(answer == JOptionPane.NO_OPTION)
						return;

					sequence.removeEvent(sequence.getTempoAt(0));
					fws_event.tick = new_tick;

					if(sequence.getEvent(fws_event))
						sequence.removeEvent(fws_event);
					
					sequence.addEvent(fws_event);
				} else {
					fws_event.tick = new_tick;
					if(sequence.getEvent(fws_event))
						sequence.refreshEvent(fws_event);
					else
						sequence.addEvent(fws_event);
				}

				vp.refreshSprite(fws_event);
				vp.refreshFull();
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_apply);
		
		this.setVisible(true);
	}
}
