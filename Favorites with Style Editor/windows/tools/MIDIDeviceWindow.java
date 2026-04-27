package tools;

import javax.swing.JDialog;
import javax.swing.JFrame;

import controllers.MIDIManager;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.sound.midi.MidiDevice.Info;
import javax.swing.JComboBox;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.JButton;

public class MIDIDeviceWindow extends JDialog {
	private static final long serialVersionUID = -2938285236253003955L;

	private boolean dropdown_listen = true;
	private Info[] inputs, outputs;

	public MIDIDeviceWindow(JFrame parent, MIDIManager mcontroller) {
		super(parent, true);

		this.setTitle("Configure MIDI Devices");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(590, 235));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);
		
		JLabel label_input = new JLabel("Input Device");
		label_input.setHorizontalAlignment(SwingConstants.RIGHT);
		label_input.setBounds(12, 12, 95, 35);
		getContentPane().add(label_input);
		
		JComboBox<String> dropdown_input = new JComboBox<String>();
		dropdown_input.setBounds(125, 12, 419, 35);
		dropdown_input.setToolTipText("Select the MIDI input device.");
		inputs = mcontroller.populateInputDropdown(dropdown_input);
		getContentPane().add(dropdown_input);
		
		JLabel label_output = new JLabel("Output Device");
		label_output.setHorizontalAlignment(SwingConstants.RIGHT);
		label_output.setBounds(12, 59, 95, 35);
		getContentPane().add(label_output);
		
		JComboBox<String> dropdown_output = new JComboBox<String>();
		dropdown_output.setToolTipText("Select the MIDI output device.");
		dropdown_output.setBounds(125, 59, 419, 35);
		outputs = mcontroller.populateOutputDropdown(dropdown_output);
		getContentPane().add(dropdown_output);

		dropdown_input.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(dropdown_listen)
					mcontroller.setInputDevice(inputs[dropdown_input.getSelectedIndex()]);
			}
		});

		dropdown_output.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(dropdown_listen)
					mcontroller.setOutputDevice(outputs[dropdown_output.getSelectedIndex()]);
			}
		});

		JButton button_refresh = new JButton("Refresh");
		button_refresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dropdown_listen = false;
				inputs = mcontroller.populateInputDropdown(dropdown_input);
				outputs = mcontroller.populateOutputDropdown(dropdown_output);
				dropdown_listen = true;
			}
		});
		button_refresh.setBounds(12, 151, 105, 35);
		getContentPane().add(button_refresh);

		this.setVisible(true);
	}
}
