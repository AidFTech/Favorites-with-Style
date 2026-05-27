package tools;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;

import main_window.FWSEditorMainWindow;
import voices.InstrumentProfile;

import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.SwingConstants;

import controllers.FWSEditor;

public class OutputProfileWindow extends JDialog {
	private static final long serialVersionUID = 3994316436801156489L;

	public OutputProfileWindow(FWSEditorMainWindow parent) {
		super(parent, true);

		this.setTitle("Output Voice Profile");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(440, 250));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);

		FWSEditor controller = parent.getController();
		
		JLabel label_header = new JLabel("<html>Set the output MIDI profile for the connected instrument, e.g. if creating a song meant for a different instrument.</html>");
		label_header.setBounds(12, 12, 416, 39);
		getContentPane().add(label_header);
		
		JComboBox<String> dropdown_profile = new JComboBox<>();
		dropdown_profile.setToolTipText("Select the connected instrument family. To use the same instrument as is selected for the voice list, select \"Same as Voice List.\"");
		dropdown_profile.setBounds(126, 63, 277, 35);
		getContentPane().add(dropdown_profile);
		
		JLabel label_family = new JLabel("Family");
		label_family.setHorizontalAlignment(SwingConstants.RIGHT);
		label_family.setBounds(48, 63, 60, 35);
		getContentPane().add(label_family);
		
		JComboBox<String> dropdown_instrument = new JComboBox<>();
		dropdown_instrument.setToolTipText("Select the connected instrument.");
		dropdown_instrument.setBounds(126, 108, 277, 35);
		getContentPane().add(dropdown_instrument);
		
		JLabel label_instrument = new JLabel("Instrument");
		label_instrument.setHorizontalAlignment(SwingConstants.RIGHT);
		label_instrument.setBounds(23, 110, 85, 33);
		getContentPane().add(label_instrument);

		dropdown_profile.addItem("Same as Voice List");

		String[] profiles = controller.getInstrumentProfileList();
		for(String profile: profiles) {
			dropdown_profile.addItem(profile);
		}
		
		dropdown_profile.setSelectedIndex(0);
		if(!controller.getOutputProfileName().trim().isEmpty())
			dropdown_profile.setSelectedItem(controller.getOutputProfileName());

		refreshInstrumentMenu(dropdown_instrument, controller.getOutputProfile());

		dropdown_profile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(dropdown_profile.getSelectedIndex() <= 0)
					refreshInstrumentMenu(dropdown_instrument, null);
				else
					refreshInstrumentMenu(dropdown_instrument, controller.getInstrumentProfile((String)dropdown_profile.getSelectedItem()));
			}
		});
		
		dropdown_instrument.setSelectedIndex(0);
		if(!controller.getOutputInstrumentName().trim().isEmpty())
			dropdown_instrument.setSelectedItem(controller.getOutputInstrumentName());
		
		OutputProfileWindow self = this;

		JButton button_cancel = new JButton("Cancel");
		button_cancel.setBounds(206, 203, 105, 35);
		button_cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_cancel);

		JButton button_apply = new JButton("Apply");
		button_apply.setBounds(323, 203, 105, 35);
		button_apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(dropdown_profile.getSelectedIndex() > 0)
					controller.setOutputFamily((String)dropdown_profile.getSelectedItem());
				else
					controller.setOutputFamily("");

				if(dropdown_instrument.getSelectedIndex() > 0)
					controller.setOutputInstrument((String)dropdown_instrument.getSelectedItem());
				else
					controller.setOutputInstrument("");

				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_apply);
		
		this.setVisible(true);
	}

	/** Refresh the instrument menu. */
	private void refreshInstrumentMenu(JComboBox<String> instrument_dropdown, InstrumentProfile target_profile) {
		instrument_dropdown.removeAllItems();

		instrument_dropdown.addItem("");
		if(target_profile == null)
			return;

		String[] instruments = target_profile.getInstrumentNames();
		for(String instrument: instruments) {
			instrument_dropdown.addItem(instrument);
		}
	}
}
