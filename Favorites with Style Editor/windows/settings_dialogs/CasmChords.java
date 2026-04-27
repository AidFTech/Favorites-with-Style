package settings_dialogs;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JPanel;

import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JButton;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;

public class CasmChords extends JDialog {

	private static final long serialVersionUID = 3056113861874624893L;
	
	private boolean[] chords;

	CasmChords(String part_name, boolean[] chords, CasmEditor parent) {
		super(parent, true);
		
		this.setTitle("Part " + part_name + " Chord Play");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(414,430));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);
		
		JLabel lblAllowedChords = new JLabel("Allowed Chords:");
		lblAllowedChords.setHorizontalAlignment(SwingConstants.CENTER);
		lblAllowedChords.setBounds(12, 12, 380, 25);
		getContentPane().add(lblAllowedChords);
		
		JPanel panelCheckBoxes = new JPanel();
		panelCheckBoxes.setBounds(12, 42, 380, 298);
		getContentPane().add(panelCheckBoxes);
		panelCheckBoxes.setLayout(new GridLayout(0, 4, 0, 0));
		
		JCheckBox[] all_checkbox_ref = new JCheckBox[0x22];
		
		JCheckBox chckbxMajor = new JCheckBox("Major");
		panelCheckBoxes.add(chckbxMajor);
		
		JCheckBox chckbxSixth = new JCheckBox("6");
		panelCheckBoxes.add(chckbxSixth);
		
		JCheckBox chckbxMaj7 = new JCheckBox("M7");
		panelCheckBoxes.add(chckbxMaj7);
		
		JCheckBox chckbxM7sh11 = new JCheckBox("M7(♯11)");
		panelCheckBoxes.add(chckbxM7sh11);
		
		JCheckBox chckbxNinth = new JCheckBox("(9)");
		panelCheckBoxes.add(chckbxNinth);
		
		JCheckBox chckbxMaj79 = new JCheckBox("M7(9)");
		panelCheckBoxes.add(chckbxMaj79);
		
		JCheckBox chckbxSixthNinth = new JCheckBox("6(9)");
		panelCheckBoxes.add(chckbxSixthNinth);
		
		JCheckBox chckbxAug = new JCheckBox("aug");
		panelCheckBoxes.add(chckbxAug);
		
		JCheckBox chckbxMinor = new JCheckBox("m");
		panelCheckBoxes.add(chckbxMinor);
		
		JCheckBox chckbxMinorSixth = new JCheckBox("m6");
		panelCheckBoxes.add(chckbxMinorSixth);
		
		JCheckBox chckbxMinorSeventh = new JCheckBox("m7");
		panelCheckBoxes.add(chckbxMinorSeventh);
		
		JCheckBox chckbxMinor7b5 = new JCheckBox("m7♭5");
		panelCheckBoxes.add(chckbxMinor7b5);
		
		JCheckBox chckbxMinorNinth = new JCheckBox("m(9)");
		panelCheckBoxes.add(chckbxMinorNinth);
		
		JCheckBox chckbxMinor79 = new JCheckBox("m7(9)");
		panelCheckBoxes.add(chckbxMinor79);
		
		JCheckBox chckbxMinor711 = new JCheckBox("m7(11)");
		panelCheckBoxes.add(chckbxMinor711);
		
		JCheckBox chckbxMinorMaj7 = new JCheckBox("mM7");
		panelCheckBoxes.add(chckbxMinorMaj7);
		
		JCheckBox chckbxMinorMaj79 = new JCheckBox("mM7(9)");
		panelCheckBoxes.add(chckbxMinorMaj79);
		
		JCheckBox chckbxDim = new JCheckBox("dim");
		panelCheckBoxes.add(chckbxDim);
		
		JCheckBox chckbxDim7 = new JCheckBox("dim7");
		panelCheckBoxes.add(chckbxDim7);
		
		JCheckBox chckbxSeventh = new JCheckBox("7");
		panelCheckBoxes.add(chckbxSeventh);
		
		JCheckBox chckbxSeventhSus = new JCheckBox("7sus4");
		panelCheckBoxes.add(chckbxSeventhSus);
		
		JCheckBox chckbxSeventhb5 = new JCheckBox("7♭5");
		panelCheckBoxes.add(chckbxSeventhb5);
		
		JCheckBox chckbxSeventh9 = new JCheckBox("7(9)");
		panelCheckBoxes.add(chckbxSeventh9);
		
		JCheckBox chckbx7sh11 = new JCheckBox("7(♯11)");
		panelCheckBoxes.add(chckbx7sh11);
		
		JCheckBox chckbx713 = new JCheckBox("7(13)");
		panelCheckBoxes.add(chckbx713);
		
		JCheckBox chckbx7b9 = new JCheckBox("7(♭9)");
		panelCheckBoxes.add(chckbx7b9);
		
		JCheckBox chckbx7b13 = new JCheckBox("7(♭13)");
		panelCheckBoxes.add(chckbx7b13);
		
		JCheckBox chckbx7sh9 = new JCheckBox("7(♯9)");
		panelCheckBoxes.add(chckbx7sh9);
		
		JCheckBox chckbxM7aug = new JCheckBox("M7aug");
		panelCheckBoxes.add(chckbxM7aug);
		
		JCheckBox chckbx7aug = new JCheckBox("7aug");
		panelCheckBoxes.add(chckbx7aug);
		
		JCheckBox chckbxOctave = new JCheckBox("(1+8)");
		panelCheckBoxes.add(chckbxOctave);
		
		JCheckBox chckbxFifth = new JCheckBox("(1+5)");
		panelCheckBoxes.add(chckbxFifth);
		
		JCheckBox chckbxSus4 = new JCheckBox("sus4");
		panelCheckBoxes.add(chckbxSus4);
		
		JCheckBox chckbxSus2 = new JCheckBox("sus2");
		panelCheckBoxes.add(chckbxSus2);
		
		all_checkbox_ref[0x00] = chckbxMajor;
		all_checkbox_ref[0x01] = chckbxSixth;
		all_checkbox_ref[0x02] = chckbxMaj7;
		all_checkbox_ref[0x03] = chckbxM7sh11;
		all_checkbox_ref[0x04] = chckbxNinth;
		all_checkbox_ref[0x05] = chckbxMaj79;
		all_checkbox_ref[0x06] = chckbxSixthNinth;
		all_checkbox_ref[0x07] = chckbxAug;
		all_checkbox_ref[0x08] = chckbxMinor;
		all_checkbox_ref[0x09] = chckbxMinorSixth;
		all_checkbox_ref[0x0A] = chckbxMinorSeventh;
		all_checkbox_ref[0x0B] = chckbxMinor7b5;
		all_checkbox_ref[0x0C] = chckbxMinorNinth;
		all_checkbox_ref[0x0D] = chckbxMinor79;
		all_checkbox_ref[0x0E] = chckbxMinor711;
		all_checkbox_ref[0x0F] = chckbxMinorMaj7;
		all_checkbox_ref[0x10] = chckbxMinorMaj79;
		all_checkbox_ref[0x11] = chckbxDim;
		all_checkbox_ref[0x12] = chckbxDim7;
		all_checkbox_ref[0x13] = chckbxSeventh;
		all_checkbox_ref[0x14] = chckbxSeventhSus;
		all_checkbox_ref[0x15] = chckbxSeventhb5;
		all_checkbox_ref[0x16] = chckbxSeventh9;
		all_checkbox_ref[0x17] = chckbx7sh11;
		all_checkbox_ref[0x18] = chckbx713;
		all_checkbox_ref[0x19] = chckbx7b9;
		all_checkbox_ref[0x1A] = chckbx7b13;
		all_checkbox_ref[0x1B] = chckbx7sh9;
		all_checkbox_ref[0x1C] = chckbxM7aug;
		all_checkbox_ref[0x1D] = chckbx7aug;
		all_checkbox_ref[0x1E] = chckbxOctave;
		all_checkbox_ref[0x1F] = chckbxFifth;
		all_checkbox_ref[0x20] = chckbxSus4;
		all_checkbox_ref[0x21] = chckbxSus2;
		
		for(int i=0;i<all_checkbox_ref.length;i+=1)
			all_checkbox_ref[i].setToolTipText("Check to allow the CASM part to play during all " + all_checkbox_ref[i].getText() + " chords, uncheck to disallow.");
		
		this.chords = chords;
		if(this.chords.length != 38)
			this.closeDialogBox();
		
		chckbxMajor.setSelected(this.chords[0x00]);
		chckbxSixth.setSelected(this.chords[0x01]);
		chckbxMaj7.setSelected(this.chords[0x02]);
		chckbxM7sh11.setSelected(this.chords[0x03]);
		chckbxNinth.setSelected(this.chords[0x04]);
		chckbxMaj79.setSelected(this.chords[0x05]);
		chckbxSixthNinth.setSelected(this.chords[0x06]);
		chckbxAug.setSelected(this.chords[0x07]);
		chckbxMinor.setSelected(this.chords[0x08]);
		chckbxMinorSixth.setSelected(this.chords[0x09]);
		chckbxMinorSeventh.setSelected(this.chords[0x0A]);
		chckbxMinor7b5.setSelected(this.chords[0x0B]);
		chckbxMinorNinth.setSelected(this.chords[0x0C]);
		chckbxMinor79.setSelected(this.chords[0x0D]);
		chckbxMinor711.setSelected(this.chords[0x0E]);
		chckbxMinorMaj7.setSelected(this.chords[0x0F]);
		chckbxMinorMaj79.setSelected(this.chords[0x10]);
		chckbxDim.setSelected(this.chords[0x11]);
		chckbxDim7.setSelected(this.chords[0x12]);
		chckbxSeventh.setSelected(this.chords[0x13]);
		chckbxSeventhSus.setSelected(this.chords[0x14]);
		chckbxSeventhb5.setSelected(this.chords[0x15]);
		chckbxSeventh9.setSelected(this.chords[0x16]);
		chckbx7sh11.setSelected(this.chords[0x17]);
		chckbx713.setSelected(this.chords[0x18]);
		chckbx7b9.setSelected(this.chords[0x19]);
		chckbx7b13.setSelected(this.chords[0x1A]);
		chckbx7sh9.setSelected(this.chords[0x1B]);
		chckbxM7aug.setSelected(this.chords[0x1C]);
		chckbx7aug.setSelected(this.chords[0x1D]);
		chckbxOctave.setSelected(this.chords[0x1E]);
		chckbxFifth.setSelected(this.chords[0x1F]);
		chckbxSus4.setSelected(this.chords[0x20]);
		chckbxSus2.setSelected(this.chords[0x21]);
		
		JPanel panelCancelSave = new JPanel();
		panelCancelSave.setBounds(0, 352, 404, 42);
		getContentPane().add(panelCancelSave);
		panelCancelSave.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JButton buttonCancel = new JButton("Cancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				closeDialogBox();
			}
		});
		buttonCancel.setToolTipText("Closes the window with no changes to the CASM data.");
		panelCancelSave.add(buttonCancel);
		
		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				saveChords(new JCheckBox[] {
						chckbxMajor, chckbxSixth, chckbxMaj7, chckbxM7sh11, chckbxNinth, chckbxMaj79, chckbxSixthNinth, chckbxAug, chckbxMinor, chckbxMinorSixth,
						chckbxMinorSeventh, chckbxMinor7b5, chckbxMinorNinth, chckbxMinor79, chckbxMinor711, chckbxMinorMaj7, chckbxMinorMaj79, chckbxDim, chckbxDim7,
						chckbxSeventh, chckbxSeventhSus, chckbxSeventhb5, chckbxSeventh9, chckbx7sh11, chckbx713, chckbx7b9, chckbx7b13, chckbx7sh9, chckbxM7aug, chckbx7aug,
						chckbxOctave, chckbxFifth, chckbxSus4, chckbxSus2
				});
			}
		});
		btnSave.setToolTipText("Saves the changes made to the CASM data and closes the window.");
		panelCancelSave.add(btnSave);
		
		this.setVisible(true);
	}
	
	private void closeDialogBox() {
		this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}
	
	private void saveChords(JCheckBox[] selected) {
		boolean[] old_chords = Arrays.copyOf(this.chords, this.chords.length);
		
		try {
			for(int i=0;i<selected.length;i+=1) {
				this.chords[i] = selected[i].isSelected();
			}
			
			this.chords[0x23] = selected[0x15].isSelected();
			this.chords[0x24] = selected[0x11].isSelected();
			this.chords[0x25] = selected[0x0B].isSelected();
		} catch (ArrayIndexOutOfBoundsException e) {
			this.chords = Arrays.copyOf(old_chords, old_chords.length);
		}
		
		closeDialogBox();
	}
}
