package settings_dialogs;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;

import main_window.FWSEditorMainWindow;
import options.SelectionOptions;

public class SelectionOptionsWindow extends JDialog {
	private static final long serialVersionUID = -8475548912388257860L;

	public SelectionOptionsWindow(FWSEditorMainWindow parent, SelectionOptions options) {
		super(parent, true);

		this.setTitle("Selection Filter");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(440, 720));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);

		JCheckBox checkbox_active_channel = new JCheckBox("Use Active Channel");
		checkbox_active_channel.setBounds(218, 17, 140, 32);
		checkbox_active_channel.setToolTipText("Check to filter channel number selection from the active channel selected in the main window.");
		checkbox_active_channel.setSelected(options.allowed_active);
		getContentPane().add(checkbox_active_channel);

		JCheckBox[] checkbox_channels = new JCheckBox[options.allowed_channels.length];

		for(int i=0;i<checkbox_channels.length;i+=1) {
			JCheckBox checkbox_channel = new JCheckBox("Channel " + (i+1));
			checkbox_channel.setToolTipText("Check to allow the selection of events from channel " + (i+1) + ".");
			checkbox_channel.setBounds(218, 52 + 35*i, 114, 32);
			checkbox_channel.setSelected(options.allowed_channels[i]);
			getContentPane().add(checkbox_channel);

			checkbox_channel.setEnabled(!checkbox_active_channel.isSelected());
			checkbox_channels[i] = checkbox_channel;
		}

		checkbox_active_channel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for(JCheckBox checkbox_channel: checkbox_channels)
					checkbox_channel.setEnabled(!checkbox_active_channel.isSelected());
			}
		});

		SelectionOptionsWindow self = this;

		JButton button_apply = new JButton("Apply");
		button_apply.setBounds(323, 673, 105, 35);
		button_apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				options.allowed_active = checkbox_active_channel.isSelected();
				
				for(int i=0;i<options.allowed_channels.length;i+=1)
					options.allowed_channels[i] = checkbox_channels[i].isSelected();

				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_apply);

		JButton button_cancel = new JButton("Cancel");
		button_cancel.setBounds(206, 673, 105, 35);
		button_cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_cancel);

		this.setVisible(true);
	}
}
