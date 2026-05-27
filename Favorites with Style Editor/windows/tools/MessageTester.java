package tools;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JDialog;

import main_window.FWSEditorMainWindow;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import controllers.MIDIManager;

import javax.swing.JTextField;
import java.awt.event.ActionEvent;

public class MessageTester extends JDialog {
	private static final long serialVersionUID = 5121979644872328384L;

	private ArrayList<byte[]> message_list_vec = new ArrayList<>();

	public MessageTester(FWSEditorMainWindow parent) {
		super(parent, false);
		
		MIDIManager midi_manager = parent.getController().getMidiManager();

		this.setTitle("Export MIDI Sequence");
		this.setType(Type.NORMAL);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(600, 420));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);
		
		JScrollPane list_panel = new JScrollPane();
		list_panel.setBounds(12, 12, 384, 325);
		getContentPane().add(list_panel);
		
		JList<String> message_list = new JList<String>();
		list_panel.setViewportView(message_list);
		DefaultListModel<String> list_model = new DefaultListModel<>();
		refreshJList(list_model);
		message_list.setModel(list_model);
		
		JButton button_add_message = new JButton("Add Message");
		button_add_message.setToolTipText("Add the message in the message field to the list.");
		button_add_message.setBounds(408, 12, 180, 35);
		getContentPane().add(button_add_message);
		
		JButton button_remove_message = new JButton("Remove Message");
		button_remove_message.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final int index = message_list.getSelectedIndex();
				if(index < 0 || index >= message_list_vec.size())
					return;

				message_list_vec.remove(index);

				refreshJList(list_model);
				message_list.setModel(list_model);
			}
		});
		button_remove_message.setToolTipText("Remove the selected message.");
		button_remove_message.setBounds(408, 59, 180, 35);
		getContentPane().add(button_remove_message);

		JButton button_change_message = new JButton("Modify Selected");
		button_change_message.setToolTipText("Replace the selected message with that entered in the message field.");
		button_change_message.setBounds(408, 106, 180, 35);
		getContentPane().add(button_change_message);
		
		JButton button_send_messages = new JButton("Send All");
		button_send_messages.setToolTipText("Send all listed messages.");
		button_send_messages.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				byte[][] messages = new byte[message_list_vec.size()][];
				message_list_vec.toArray(messages);
				midi_manager.sendMIDIList(messages);
			}
		});
		button_send_messages.setBounds(408, 153, 180, 35);
		getContentPane().add(button_send_messages);
		
		JLabel label_new_message = new JLabel("New Message");
		label_new_message.setHorizontalAlignment(SwingConstants.RIGHT);
		label_new_message.setBounds(12, 349, 118, 35);
		getContentPane().add(label_new_message);

		MessageTester self = this;
		
		JTextField field_new_message = new JTextField();
		field_new_message.setToolTipText("Enter the desired message here. Click Add Message to add the message to the list. Start sysex messages with F0.");
		field_new_message.setBounds(148, 356, 248, 35);
		getContentPane().add(field_new_message);
		field_new_message.setColumns(10);

		button_add_message.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				byte[] data = new byte[0];
				try {
					data = getBytesFromString(field_new_message.getText());
				} catch(NumberFormatException e1) {
					JOptionPane.showMessageDialog(self, "A number entry is formatted incorrectly. Please check the numbers and try again.", "Number Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				message_list_vec.add(data);

				refreshJList(list_model);
				message_list.setModel(list_model);

				field_new_message.setText("");
				message_list.setSelectedIndex(-1);
			}
		});

		button_change_message.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final int selected = message_list.getSelectedIndex();
				if(selected < 0 || selected >= message_list_vec.size())
					return;

				byte[] data = new byte[0];

				try {
					data = getBytesFromString(field_new_message.getText());
				} catch(NumberFormatException e1) {
					JOptionPane.showMessageDialog(self, "A number entry is formatted incorrectly. Please check the numbers and try again.", "Number Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				message_list_vec.set(selected, data);

				refreshJList(list_model);
				message_list.setModel(list_model);

				field_new_message.setText("");
				message_list.setSelectedIndex(selected);
			}
		});

		message_list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				if(message_list.getSelectedIndex() >= 0 && message_list.getSelectedIndex() < message_list_vec.size()) {
					String data_string = "";
					byte[] message = message_list_vec.get(message_list.getSelectedIndex());
					for(int i=0;i<message.length;i+=1)
						data_string += Integer.toHexString(message[i]&0xFF).toUpperCase() + (i<message.length-1 ? " " : "");

					field_new_message.setText(data_string);
				} else {
					field_new_message.setText("");
				}
			}
		});

		this.setVisible(true);
	}

	/** Refresh a list. */
	private void refreshJList(DefaultListModel<String> list_model) {
		list_model.clear();
		for(int m=0;m<message_list_vec.size();m+=1) {
			String data_string = "";
			byte[] message = message_list_vec.get(m);
			for(int i=0;i<message.length;i+=1)
				data_string += Integer.toHexString(message[i]&0xFF).toUpperCase() + (i<message.length-1 ? " " : "");
			
			list_model.addElement(data_string);
		}
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

	/** Get an array of hex bytes. */
	private static byte[] getBytesFromString(String b) throws NumberFormatException {
		ArrayList<Byte> data_vec = new ArrayList<Byte>(0);
		String[] data_strings = b.split(" ");
		for(int i=0;i<data_strings.length;i+=1) {
			if(data_strings[i].length() <= 0)
				continue;

			final byte d = getHexByte(data_strings[i]);
			data_vec.add(Byte.valueOf(d));
		}

		if(data_vec.size() <= 0)
			return new byte[0];

		byte[] data = new byte[data_vec.size()];
		for(int i=0;i<data.length;i+=1)
			data[i] = data_vec.get(i);

		return data;
	}
}
