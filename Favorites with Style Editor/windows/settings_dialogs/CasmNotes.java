package settings_dialogs;

import java.awt.event.WindowEvent;

import javax.swing.JPanel;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JCheckBox;
import javax.swing.JDialog;

import java.awt.GridLayout;

public class CasmNotes extends JDialog {

	private static final long serialVersionUID = 2569091701640774873L;
	
	private boolean[] notes;
	
	public CasmNotes(String part_name, boolean[] notes, CasmEditor parent) {
		super(parent, true);
		
		this.setTitle("Part " + part_name + " Note Play");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(270,335));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);
		
		JPanel panelCancelSave = new JPanel();
		panelCancelSave.setBounds(0, 257, 260, 42);
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
		btnSave.setToolTipText("Saves the changes made to the CASM data and closes the window.");
		panelCancelSave.add(btnSave);
		
		JLabel lblAllowedChords = new JLabel("Allowed Chord Notes:");
		lblAllowedChords.setHorizontalAlignment(SwingConstants.CENTER);
		lblAllowedChords.setBounds(12, 12, 236, 26);
		getContentPane().add(lblAllowedChords);
		
		JPanel panelCheckBoxes = new JPanel();
		panelCheckBoxes.setBounds(12, 37, 236, 208);
		getContentPane().add(panelCheckBoxes);
		panelCheckBoxes.setLayout(new GridLayout(6, 2, 0, 0));
		
		JCheckBox chckbxC = new JCheckBox("C");
		panelCheckBoxes.add(chckbxC);
		chckbxC.setToolTipText("Check to allow the CASM part to play in C chords, uncheck to disallow.");
		
		JCheckBox chckbxFg = new JCheckBox("F♯/G♭");
		panelCheckBoxes.add(chckbxFg);
		chckbxFg.setToolTipText("Check to allow the CASM part to play in F♯/G♭ chords, uncheck to disallow.");
		
		JCheckBox chckbxCd = new JCheckBox("C♯/D♭");
		panelCheckBoxes.add(chckbxCd);
		chckbxCd.setToolTipText("Check to allow the CASM part to play in C♯/D♭ chords, uncheck to disallow.");
		
		JCheckBox chckbxG = new JCheckBox("G");
		panelCheckBoxes.add(chckbxG);
		chckbxG.setToolTipText("Check to allow the CASM part to play in G chords, uncheck to disallow.");
		
		JCheckBox chckbxD = new JCheckBox("D");
		panelCheckBoxes.add(chckbxD);
		chckbxD.setToolTipText("Check to allow the CASM part to play in D chords, uncheck to disallow.");
		
		JCheckBox chckbxGa = new JCheckBox("G♯/A♭");
		panelCheckBoxes.add(chckbxGa);
		chckbxGa.setToolTipText("Check to allow the CASM part to play in G♯/A♭ chords, uncheck to disallow.");
		
		JCheckBox chckbxDe = new JCheckBox("D♯/E♭");
		panelCheckBoxes.add(chckbxDe);
		chckbxDe.setToolTipText("Check to allow the CASM part to play in D♯/E♭ chords, uncheck to disallow.");
		
		JCheckBox chckbxA = new JCheckBox("A");
		panelCheckBoxes.add(chckbxA);
		chckbxA.setToolTipText("Check to allow the CASM part to play in A chords, uncheck to disallow.");
		
		JCheckBox chckbxE = new JCheckBox("E");
		panelCheckBoxes.add(chckbxE);
		chckbxE.setToolTipText("Check to allow the CASM part to play in E chords, uncheck to disallow.");
		
		JCheckBox chckbxAb = new JCheckBox("A♯/B♭");
		panelCheckBoxes.add(chckbxAb);
		chckbxAb.setToolTipText("Check to allow the CASM part to play in A♯/B♭ chords, uncheck to disallow.");
		
		JCheckBox chckbxF = new JCheckBox("F");
		panelCheckBoxes.add(chckbxF);
		chckbxF.setToolTipText("Check to allow the CASM part to play in F chords, uncheck to disallow.");
		
		JCheckBox chckbxB = new JCheckBox("B");
		panelCheckBoxes.add(chckbxB);
		chckbxB.setToolTipText("Check to allow the CASM part to play in B chords, uncheck to disallow.");
		
		this.notes = notes;
		if(this.notes.length != 12)
			this.closeDialogBox();
		
		chckbxC.setSelected(this.notes[0]);
		chckbxCd.setSelected(this.notes[1]);
		chckbxD.setSelected(this.notes[2]);
		chckbxDe.setSelected(this.notes[3]);
		chckbxE.setSelected(this.notes[4]);
		chckbxF.setSelected(this.notes[5]);
		chckbxFg.setSelected(this.notes[6]);
		chckbxG.setSelected(this.notes[7]);
		chckbxGa.setSelected(this.notes[8]);
		chckbxA.setSelected(this.notes[9]);
		chckbxAb.setSelected(this.notes[10]);
		chckbxB.setSelected(this.notes[11]);
		
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				saveNotes(new JCheckBox[] {
					chckbxC, chckbxCd, chckbxD, chckbxDe, chckbxE, chckbxF, chckbxFg, chckbxG, chckbxGa, chckbxA, chckbxAb, chckbxB	
				});
			}
		});
		
		this.setVisible(true);
	}
	
	private void closeDialogBox() {
		this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}
	
	private void saveNotes(JCheckBox[] selected) {
		if(this.notes.length != selected.length)
			closeDialogBox();
		
		for(int i=0;i<selected.length;i+=1)
			this.notes[i] = selected[i].isSelected();
		
		closeDialogBox();
	}
}
