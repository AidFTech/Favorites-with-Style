package event_dialogs;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;

import fwsevents.FWSSequence;
import fwsevents.FWSSysexEvent;
import main_window.FWSEditorMainWindow;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import canvas.SongViewPort;

import javax.swing.JTextField;

public class SysexEventDialog extends JDialog {
	private static final long serialVersionUID = 7883147245495097799L;

	public SysexEventDialog(FWSEditorMainWindow parent, FWSSysexEvent fws_event) {
		super(parent, true);

		this.setTitle("System Exclusive Event");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(385, 215));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);
		
		JLabel label_data = new JLabel("Data");
		label_data.setHorizontalAlignment(SwingConstants.RIGHT);
		label_data.setBounds(12, 12, 60, 35);
		getContentPane().add(label_data);
		
		JTextField field_data = new JTextField();
		field_data.setToolTipText("Enter the event data in hexadecmial.");
		field_data.setBounds(90, 12, 262, 35);
		getContentPane().add(field_data);
		field_data.setColumns(10);

		String data_string = "";
		for(int i=0;i<fws_event.data.length;i+=1)
			data_string += Integer.toHexString(fws_event.data[i]&0xFF).toUpperCase() + (i<fws_event.data.length-1 ? " " : "");
		field_data.setText(data_string);

		SequenceTickPanel tick_panel = new SequenceTickPanel(parent, fws_event.tick, 12, 141, 326, 100);
		tick_panel.setBounds(12, 50, 326, 100);
		getContentPane().add(tick_panel);

		SysexEventDialog self = this;

		JButton button_cancel = new JButton("Cancel");
		button_cancel.setBounds(151, 168, 105, 35);
		button_cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_cancel);
		
		JButton button_apply = new JButton("Apply");
		button_apply.setBounds(268, 168, 105, 35);
		button_apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				final long new_tick = tick_panel.getSetTick();

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
