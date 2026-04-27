package event_dialogs;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.nio.charset.StandardCharsets;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import fwsevents.FWSMiscMIDIEvent;
import fwsevents.FWSSequence;
import main_window.FWSEditorMainWindow;
import canvas.SongViewPort;

import javax.swing.JButton;
import javax.swing.JComboBox;

public class TextEventDialog extends JDialog {
	private static final long serialVersionUID = 8445020513099696625L;

	public TextEventDialog (FWSEditorMainWindow parent, FWSMiscMIDIEvent fws_event) {
		super(parent, true);
		
		this.setTitle("Text Event");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(385, 265));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);
		
		JLabel label_type = new JLabel("Type");
		label_type.setHorizontalAlignment(SwingConstants.RIGHT);
		label_type.setBounds(12, 12, 60, 35);
		getContentPane().add(label_type);
		
		JLabel label_data = new JLabel("Text");
		label_data.setHorizontalAlignment(SwingConstants.RIGHT);
		label_data.setBounds(12, 59, 60, 35);
		getContentPane().add(label_data);
		
		JComboBox<String> dropdown_type = new JComboBox<String>(FWSMiscMIDIEvent.midi_text_str);
		dropdown_type.setToolTipText("Set the text event type.");
		dropdown_type.setBounds(90, 12, 262, 35);
		dropdown_type.setSelectedIndex(fws_event.type - 1);
		getContentPane().add(dropdown_type);
		
		JTextField field_data = new JTextField();
		field_data.setToolTipText("Enter the event text.");
		field_data.setBounds(90, 59, 262, 35);
		getContentPane().add(field_data);
		field_data.setColumns(10);

		field_data.setText(new String(fws_event.data, StandardCharsets.ISO_8859_1));

		SequenceTickPanel tick_panel = new SequenceTickPanel(parent, fws_event.tick, 12, 100, 326, 100);
		tick_panel.setBounds(12, 100, 326, 100);
		getContentPane().add(tick_panel);

		TextEventDialog self = this;

		JButton button_cancel = new JButton("Cancel");
		button_cancel.setBounds(151, 218, 105, 35);
		button_cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_cancel);
		
		JButton button_apply = new JButton("Apply");
		button_apply.setBounds(268, 218, 105, 35);
		button_apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				final long new_tick = tick_panel.getSetTick();

				fws_event.type = dropdown_type.getSelectedIndex() + 1;
				fws_event.data = field_data.getText().getBytes(StandardCharsets.ISO_8859_1);

				SongViewPort vp = parent.getViewPort();
				FWSSequence sequence = vp.getActiveSequence();
								
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
