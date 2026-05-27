package tools;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;

import dialogpanels.EventDialogSignature;
import fwsevents.FWSKeySignatureEvent;
import fwsevents.FWSSequence;
import main_window.FWSEditorMainWindow;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class TransposeWindow extends JDialog {
	private static final long serialVersionUID = -1759437513833632185L;

	public TransposeWindow(FWSEditorMainWindow parent, FWSSequence sequence) {
		super(parent, true);

		this.setTitle("Transpose");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(460, 220));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);
		
		JLabel label_key = new JLabel("Key");
		label_key.setHorizontalAlignment(SwingConstants.RIGHT);
		label_key.setBounds(12, 12, 60, 35);
		getContentPane().add(label_key);
		
		JComboBox<String> dropdown_key = new JComboBox<>();
		dropdown_key.setToolTipText("Select the desired key to transpose to.");
		dropdown_key.setBounds(90, 12, 177, 35);
		getContentPane().add(dropdown_key);

		EventDialogSignature signature = new EventDialogSignature();
		signature.setBounds(288, 12, signature.getWidth(), signature.getHeight());
		this.getContentPane().add(signature);

		byte original_accidental_count = 0;

		FWSKeySignatureEvent key_event = sequence.getKeySignatureAt(0);
		if(key_event != null) {
			signature.setAccidentalCount(key_event.accidental_count);
			original_accidental_count = key_event.accidental_count;
			fillKeyComboBox(dropdown_key, key_event.major);
			dropdown_key.setSelectedIndex(key_event.accidental_count + 7);
		}

		final byte set_accidental_count = original_accidental_count;

		dropdown_key.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				signature.setAccidentalCount(dropdown_key.getSelectedIndex() - 7);
			}
		});

		JRadioButton radiobutton_transpose_key = new JRadioButton("Transpose by Key");
		radiobutton_transpose_key.setToolTipText("Transpose by key signature.");
		radiobutton_transpose_key.setSelected(true);
		radiobutton_transpose_key.setBounds(12, 73, 160, 35);
		getContentPane().add(radiobutton_transpose_key);
		
		JRadioButton radiobutton_transpose_steps = new JRadioButton("Transpose by Steps");
		radiobutton_transpose_steps.setToolTipText("Transpose by half steps.");
		radiobutton_transpose_steps.setBounds(12, 112, 160, 35);
		getContentPane().add(radiobutton_transpose_steps);

		ButtonGroup transpose_group = new ButtonGroup();
		transpose_group.add(radiobutton_transpose_key);
		transpose_group.add(radiobutton_transpose_steps);
		
		JSpinner spinner_steps = new JSpinner();
		spinner_steps.setEnabled(false);
		spinner_steps.setToolTipText("Define the number of steps to transpose by.");
		spinner_steps.setModel(new SpinnerNumberModel(0, -12, 12, 1));
		spinner_steps.setBounds(180, 112, 60, 35);
		getContentPane().add(spinner_steps);

		radiobutton_transpose_key.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(radiobutton_transpose_key.isSelected()) {
					final boolean was_selected = dropdown_key.isEnabled();
					dropdown_key.setEnabled(true);
					signature.setVisible(true);

					spinner_steps.setEnabled(false);

					if(!was_selected)
						dropdown_key.setSelectedIndex(set_accidental_count + 7);
				}
			}
		});

		radiobutton_transpose_steps.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dropdown_key.setEnabled(false);
				signature.setVisible(false);

				spinner_steps.setEnabled(true);
				spinner_steps.setValue(0);
			}
		});

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
