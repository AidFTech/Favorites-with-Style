package event_dialogs;

import javax.swing.JDialog;

import fwsevents.FWSKeySignatureEvent;
import fwsevents.FWSSequence;
import main_window.FWSEditorMainWindow;
import canvas.SongViewPort;
import dialogpanels.EventDialogSignature;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;

public class KeyEventDialog extends JDialog {
	private static final long serialVersionUID = -9162921291997475169L;

	public KeyEventDialog(FWSEditorMainWindow parent, FWSKeySignatureEvent fws_event) {
		super(parent, true);

		this.setTitle("Key Signature Event");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(375, 325));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);
		
		JLabel label_key_signature = new JLabel("Key");
		label_key_signature.setHorizontalAlignment(SwingConstants.RIGHT);
		label_key_signature.setBounds(12, 12, 110, 35);
		getContentPane().add(label_key_signature);
		
		JComboBox<String> combo_box_key = new JComboBox<String>();
		combo_box_key.setToolTipText("Select the key.");
		combo_box_key.setBounds(140, 12, 207, 35);
		getContentPane().add(combo_box_key);

		fillKeyComboBox(combo_box_key, fws_event.major);
		combo_box_key.setSelectedIndex(fws_event.accidental_count + 7);

		EventDialogSignature sig_fig = new EventDialogSignature();
		sig_fig.setLocation(12,59);
		sig_fig.setAccidentalCount(fws_event.accidental_count);
		sig_fig.setToolTipText("Staff-notation representation of the selected key signature.");
		
		getContentPane().add(sig_fig);
		
		combo_box_key.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sig_fig.setAccidentalCount(combo_box_key.getSelectedIndex() - 7);
			}
		});
		
		ButtonGroup major_group = new ButtonGroup();

		JRadioButton radio_button_major = new JRadioButton("Major");
		radio_button_major.setBounds(217, 59, 130, 35);
		radio_button_major.setToolTipText("Major key (WWHWWWH).");
		getContentPane().add(radio_button_major);
		major_group.add(radio_button_major);
		
		JRadioButton radio_button_minor = new JRadioButton("Minor");
		radio_button_minor.setBounds(217, 98, 130, 35);
		radio_button_minor.setToolTipText("Minor key (WHWWHWW).");
		getContentPane().add(radio_button_minor);
		major_group.add(radio_button_minor);
		
		radio_button_major.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fillKeyComboBox(combo_box_key, radio_button_major.isSelected());
			}
		});
		
		radio_button_minor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fillKeyComboBox(combo_box_key, radio_button_major.isSelected());
			}
		});

		if(fws_event.major)
			radio_button_major.setSelected(true);
		else
			radio_button_minor.setSelected(true);

		SequenceTickPanel tick_panel = new SequenceTickPanel(parent, fws_event.tick, 12, 141, 326, 100);
		tick_panel.setBounds(12, 141, 326, 100);
		if(fws_event.tick > 0)
			getContentPane().add(tick_panel);

		KeyEventDialog self = this;

		JButton button_cancel = new JButton("Cancel");
		button_cancel.setBounds(141, 278, 105, 35);
		button_cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_cancel);
		
		JButton button_apply = new JButton("Apply");
		button_apply.setBounds(258, 278, 105, 35);
		button_apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				final int new_accidental_count = combo_box_key.getSelectedIndex() - 7;
				final boolean major = radio_button_major.isSelected();
				final long new_tick = tick_panel.getSetTick();

				SongViewPort vp = parent.getViewPort();
				FWSSequence sequence = vp.getActiveSequence();
								
				fws_event.accidental_count = (byte)new_accidental_count;
				fws_event.major = major;

				if(new_tick == 0 && fws_event.tick != 0) {
					final int answer = JOptionPane.showConfirmDialog(self, "Replace the initial key signature event?", "Key Signature", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if(answer == JOptionPane.NO_OPTION)
						return;

					sequence.removeEvent(sequence.getKeySignatureAt(0));
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
				vp.refresh();
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_apply);

		this.setVisible(true);
	}
	
	/** Fill the key signature combo box. */
	private void fillKeyComboBox(JComboBox<String> key_menu, final boolean major) {
		final int sel = key_menu.getSelectedIndex();
		key_menu.removeAllItems();

		if(major) {
			key_menu.addItem("C♭ major");
			key_menu.addItem("G♭ major");
			key_menu.addItem("D♭ major");
			key_menu.addItem("A♭ major");
			key_menu.addItem("E♭ major");
			key_menu.addItem("B♭ major");
			key_menu.addItem("F major");
			key_menu.addItem("C major");
			key_menu.addItem("G major");
			key_menu.addItem("D major");
			key_menu.addItem("A major");
			key_menu.addItem("E major");
			key_menu.addItem("B major");
			key_menu.addItem("F♯ major");
			key_menu.addItem("C♯ major");
		} else {
			key_menu.addItem("A♭ minor");
			key_menu.addItem("E♭ minor");
			key_menu.addItem("B♭ minor");
			key_menu.addItem("F minor");
			key_menu.addItem("C minor");
			key_menu.addItem("G minor");
			key_menu.addItem("D minor");
			key_menu.addItem("A minor");
			key_menu.addItem("E minor");
			key_menu.addItem("B minor");
			key_menu.addItem("F♯ minor");
			key_menu.addItem("C♯ minor");
			key_menu.addItem("G♯ minor");
			key_menu.addItem("D♯ minor");
			key_menu.addItem("A♯ minor");
		}
		key_menu.setSelectedIndex(sel);
	}
}
