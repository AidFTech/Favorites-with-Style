package event_dialogs;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import canvas.SongViewPort;
import fwsevents.FWSMiscMIDIEvent;
import fwsevents.FWSSequence;
import main_window.FWSEditorMainWindow;

public class MiscEventDialog extends JDialog {
	private static final long serialVersionUID = 1676925389572975435L;

	public MiscEventDialog(FWSEditorMainWindow parent, FWSMiscMIDIEvent fws_event) {
		super(parent, true);

		this.setTitle("Misc MIDI Event");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(385, 275));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);
		
		JLabel label_type = new JLabel("Type");
		label_type.setHorizontalAlignment(SwingConstants.RIGHT);
		label_type.setBounds(12, 12, 60, 35);
		getContentPane().add(label_type);
		
		JLabel label_data = new JLabel("Data");
		label_data.setHorizontalAlignment(SwingConstants.RIGHT);
		label_data.setBounds(12, 59, 60, 35);
		getContentPane().add(label_data);
		
		JTextField field_type = new JTextField();
		field_type.setBounds(90, 12, 105, 35);
		getContentPane().add(field_type);
		field_type.setToolTipText("Set the event type in hexadecimal. Note that types 1-7, 58, 59, 51, and 2F are reserved.");
		field_type.setColumns(10);
		
		field_type.setText(Integer.toHexString(fws_event.type).toUpperCase());
		
		JTextField field_data = new JTextField();
		field_data.setToolTipText("Enter the event data in hexadecmial.");
		field_data.setBounds(90, 59, 262, 35);
		getContentPane().add(field_data);
		field_data.setColumns(10);

		String data_string = "";
		for(int i=0;i<fws_event.data.length;i+=1)
			data_string += Integer.toHexString(fws_event.data[i]&0xFF).toUpperCase() + (i<fws_event.data.length-1 ? " " : "");
		field_data.setText(data_string);

		SequenceTickPanel tick_panel = new SequenceTickPanel(parent, fws_event.tick, 12, 100, 326, 100);
		tick_panel.setBounds(12, 100, 326, 100);
		getContentPane().add(tick_panel);

		MiscEventDialog self = this;

		JButton button_cancel = new JButton("Cancel");
		button_cancel.setBounds(151, 228, 105, 35);
		button_cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_cancel);
		
		JButton button_apply = new JButton("Apply");
		button_apply.setBounds(268, 228, 105, 35);
		button_apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				final long new_tick = tick_panel.getSetTick();

				int new_type = 0xFF;
				try {
					new_type = getHexByte(field_type.getText());
				} catch(NumberFormatException e) {
					JOptionPane.showMessageDialog(self, "A number entry is formatted incorrectly. Please check the numbers and try again.", "Number Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				if(new_type >= 1 && new_type <= 7) {
					JOptionPane.showMessageDialog(self, "The defined type is reserved for text messages, which are handled differently in Favorites with Style.", "Type Error", JOptionPane.ERROR_MESSAGE);
					return;
				} else {
					switch(new_type) {
					case 0x51:
					case 0x58:
					case 0x59:
					case 0x2F:
						JOptionPane.showMessageDialog(self, "The defined type is reserved.", "Type Error", JOptionPane.ERROR_MESSAGE);
						return;
					default:
						break;
					}
				}

				ArrayList<Byte> data = new ArrayList<Byte>(0);
				String[] data_strings = field_data.getText().split(" ");
				for(int i=0;i<data_strings.length;i+=1) {
					if(data_strings[i].length() <= 0)
						continue;

					try {
						final byte d = getHexByte(data_strings[i]);
						data.add(Byte.valueOf(d));
					} catch(NumberFormatException e) {
						JOptionPane.showMessageDialog(self, "A number entry is formatted incorrectly. Please check the numbers and try again.", "Number Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}

				fws_event.type = new_type;
				fws_event.data = new byte[data.size()];
				for(int i=0;i<data.size();i+=1)
					fws_event.data[i] = data.get(i).byteValue();

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

	/** Get a hex byte from a string. */
	private static byte getHexByte(String b) {
		try {
			if(!b.substring(0, 1).equals("#") && !b.substring(0, 2).toUpperCase().equals("0X"))
				b = "0x" + b;
		} catch(StringIndexOutOfBoundsException e) {
			b = "0x" + b;
		}
		
		int ab = Integer.decode(b);
		
		return (byte)ab;
	}
}