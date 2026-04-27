package event_dialogs;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import canvas.SongViewPort;
import fwsevents.FWSSequence;
import fwsevents.FWSShortEvent;
import main_window.FWSEditorMainWindow;
import midicontrol.MIDIControl;
import javax.swing.JComboBox;

public class ControlEventDialog extends JDialog {
	private static final long serialVersionUID = -6062322518882349007L;

	private boolean spinner_listen = true, dropdown_listen = true;

	private class ControlPanel extends JPanel {
		private static final long serialVersionUID = -3290699215180201555L;
		private byte value = 0;
		private boolean adjust_spinner = true;

		private JSpinner spinner_value;

		private ControlPanel(final byte value) {
			super();
			this.value = value;
			this.setLayout(null);

			spinner_value = new JSpinner();
			spinner_value.setModel(new SpinnerNumberModel(this.value, 0, 127, 1));
			spinner_value.setVisible(true);
			spinner_value.setBounds(100, 12, 65, 35);

			ControlPanel self = this;
			spinner_value.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					self.value = ((Integer)spinner_value.getValue()).byteValue();

					if(adjust_spinner) {
						Component[] components = getComponents();
						for(int i=0;i<components.length;i+=1) {
							if(components[i] instanceof JSlider) {
								JSlider slider = (JSlider)components[i];
								slider.setValue((Integer)spinner_value.getValue());
								break;
							}
						}
					}
				}
			});

			this.add(spinner_value);
		}

		/** Create the objects to set the value. */
		private void createValueObjects(final byte control) {
			Component[] components = this.getComponents();
			for(int i=0;i<components.length;i+=1) {
				if(components[i] != spinner_value)
					this.remove(components[i]);
			}

			JLabel label_spinner = new JLabel();
			label_spinner.setHorizontalAlignment(SwingConstants.RIGHT);
			label_spinner.setBounds(12, 12, 80, 35);

			boolean add_spinner = true;

			if(MIDIControl.getControl(control) != null) {
				MIDIControl control_enum = MIDIControl.getControl(control);
				final boolean lsb = control_enum.getLSBValue() == control && control_enum.getValue() != control;
				switch(control_enum) {
				case CHANNEL_VOLUME:
					label_spinner.setText("Volume " + (lsb ? "LSB" : "MSB"));
					spinner_value.setToolTipText("Set the volume " + (lsb ? "LSB." : "MSB."));
					{
						JSlider volume_slider = new JSlider(0, 127);
						volume_slider.setValue(value);
						volume_slider.setBounds(185, 12, 130, 35);
						volume_slider.addChangeListener(new ChangeListener() {
							public void stateChanged(ChangeEvent e) {
								adjust_spinner = false;
								spinner_value.setValue(volume_slider.getValue());
								adjust_spinner = true;
							}
						});
						volume_slider.setToolTipText("Set the volume " + (lsb ? "LSB." : "MSB."));
						this.add(volume_slider);
					}
					break;
				case BALANCE:
				case PAN:
					label_spinner.setText(control_enum.getName() + " " + (lsb ? "LSB" : "MSB"));
					spinner_value.setToolTipText("Set the " + control_enum.getName().toLowerCase() + " " + (lsb ? "LSB." : "MSB."));
					{
						JSlider pan_slider = new JSlider(0, 127);
						pan_slider.setValue(value);
						pan_slider.setBounds(185, 12, 130, 35);
						pan_slider.addChangeListener(new ChangeListener() {
							public void stateChanged(ChangeEvent e) {
								adjust_spinner = false;
								spinner_value.setValue(pan_slider.getValue());
								adjust_spinner = true;
							}
						});
						pan_slider.setToolTipText("Set the " + control_enum.getName().toLowerCase() + " " + (lsb ? "LSB." : "MSB."));

						JLabel label_l = new JLabel("L"), label_r = new JLabel("R");
						label_l.setBounds(pan_slider.getX(), pan_slider.getY() + pan_slider.getHeight(), 25, 20);
						label_l.setHorizontalAlignment(SwingConstants.CENTER);
						label_l.setVerticalAlignment(SwingConstants.TOP);
						label_r.setBounds(pan_slider.getX() + pan_slider.getWidth() - 25, pan_slider.getY() + pan_slider.getHeight(), 25, 20);
						label_r.setHorizontalAlignment(SwingConstants.CENTER);
						label_r.setVerticalAlignment(SwingConstants.TOP);

						this.add(pan_slider);
						this.add(label_l);
						this.add(label_r);
					}
					break;
				case SUSTAIN_PEDAL:
				case SOFT:
				case SOSTENUTO:
				case PORTAMENTO:
				case LEGATO:
				case HOLD_2:
				case LOCAL_CONTROL:
					add_spinner = false;
					{
						final String setting = control_enum.getName().replace("On/Off","");
						JCheckBox pedal_checkbox = new JCheckBox(setting);
						pedal_checkbox.setBounds(60, 12, 200, 35);
						pedal_checkbox.setSelected(this.value >= 64);

						pedal_checkbox.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								if(pedal_checkbox.isSelected())
									value = 127;
								else
									value = 0;
							}
						});

						pedal_checkbox.setToolTipText("Check to enable " + setting + ", uncheck to disable.");
						this.add(pedal_checkbox);
					}
					break;
				case ALL_SOUND_OFF:
				case RESET_CONTROLLERS:
				case ALL_NOTES_OFF:
				case OMNI_OFF:
				case OMNI_ON:
				case MONO_ON:
				case POLY_ON:
					add_spinner = false;
					value = 0;
					break;
				default:
					label_spinner.setText("Value");
					spinner_value.setToolTipText("Set the control value.");
					break;
				}
			} else {
				label_spinner.setText("Value");
				spinner_value.setToolTipText("Set the control value.");
			}

			if(add_spinner) {
				this.add(label_spinner);
				spinner_value.setVisible(true);
			} else
				spinner_value.setVisible(false);
		}
	}

	public ControlEventDialog(FWSEditorMainWindow parent, FWSShortEvent fws_event) {
		super(parent, true);

		this.setTitle("Control Change Event");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(365, 345));
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
		
		JLabel label_control = new JLabel("Control");
		label_control.setHorizontalAlignment(SwingConstants.RIGHT);
		label_control.setBounds(12, 59, 60, 35);
		getContentPane().add(label_control);
		
		JSpinner spinner_control = new JSpinner();
		spinner_control.setModel(new SpinnerNumberModel(fws_event.data1, 0, 127, 1));
		spinner_control.setToolTipText("Select the control to change.");
		spinner_control.setBounds(89, 59, 73, 35);
		getContentPane().add(spinner_control);
		
		ControlPanel panel_value = new ControlPanel(fws_event.data2);
		panel_value.setBounds(12, 106, 326, 63);
		panel_value.createValueObjects(fws_event.data1);
		getContentPane().add(panel_value);
		
		JComboBox<String> dropdown_control = new JComboBox<String>();
		dropdown_control.setToolTipText("Select the control to change.");
		ArrayList<Integer> ctl_indices = new ArrayList<Integer>(0);
		for(int i=0;i<128;i+=1) {
			MIDIControl c = MIDIControl.getControl(i);
			if(c != null) {
				if(i>=0x20 && i < 0x40) {
					dropdown_control.addItem(c.getName() + " LSB");
					ctl_indices.add(Integer.valueOf(c.getLSBValue()));
				} else {
					dropdown_control.addItem(c.getName() + (i < 0x20 ? " MSB" : ""));
					ctl_indices.add(Integer.valueOf(c.getValue()));
				}
			}
		}

		boolean found_ctl = false;
		for(int i=0;i<ctl_indices.size();i+=1) {
			if(ctl_indices.get(i) == fws_event.data1) {
				dropdown_control.setSelectedIndex(i);
				found_ctl = true;
				break;
			}
		}

		if(!found_ctl)
			dropdown_control.setSelectedIndex(-1);

		dropdown_control.setBounds(174, 59, 164, 35);
		getContentPane().add(dropdown_control);

		ControlEventDialog self = this;

		dropdown_control.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(dropdown_control.getSelectedIndex() < 0 || dropdown_control.getSelectedIndex() >= ctl_indices.size())
					return;
				
				if(!dropdown_listen)
					return;
				
				spinner_listen = false;
				final int control = ctl_indices.get(dropdown_control.getSelectedIndex());
				spinner_control.setValue(Integer.valueOf(control));

				MIDIControl mc = MIDIControl.getControl(control);
				if(mc != null && mc == MIDIControl.VOICE)
					JOptionPane.showMessageDialog(self, "Bank/voice MSB and LSB can be set in a Voice event in Favorites with Style. It is not necessary to set these values with a separate control message.", "Bank Select", JOptionPane.INFORMATION_MESSAGE);

				spinner_listen = true;
			}
		});

		spinner_control.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(spinner_listen) {
					dropdown_listen = false;
					final int control = (Integer)spinner_control.getValue();
					boolean found_ctl = false;
					for(int i=0;i<ctl_indices.size();i+=1) {
						if(ctl_indices.get(i) == control) {
							dropdown_control.setSelectedIndex(i);
							found_ctl = true;
							break;
						}
					}

					if(!found_ctl)
						dropdown_control.setSelectedIndex(-1);

					dropdown_listen = true;
				}

				panel_value.createValueObjects(((Integer)spinner_control.getValue()).byteValue());
				panel_value.repaint();
			}
		});
		
		SequenceTickPanel tick_panel = new SequenceTickPanel(parent, fws_event.tick, 12, 181, 326, 100);
		tick_panel.setBounds(12, 181, 326, 100);
		getContentPane().add(tick_panel);

		JButton button_cancel = new JButton("Cancel");
		button_cancel.setBounds(131, 298, 105, 35);
		button_cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_cancel);
		
		JButton button_apply = new JButton("Apply");
		button_apply.setBounds(248, 298, 105, 35);
		button_apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				final long new_tick = tick_panel.getSetTick();

				SongViewPort vp = parent.getViewPort();
				FWSSequence sequence = vp.getActiveSequence();

				fws_event.data1 = ((Integer)spinner_control.getValue()).byteValue();
				fws_event.data2 = panel_value.value;
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
