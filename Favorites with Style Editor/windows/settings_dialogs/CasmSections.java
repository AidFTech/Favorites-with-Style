package settings_dialogs;

import javax.swing.JCheckBox;
import javax.swing.JDialog;

import style.Casm.CasmSect;
import style.Style;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JPanel;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;

public class CasmSections extends JDialog {

	private static final long serialVersionUID = 6542374607382879922L;
	
	private static final int check_height = 30;

	public CasmSections(CasmEditor parent, Style style, int CSEG) {
		super(parent, true);
		
		this.setTitle("CSEG Section Options");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(366,452));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);
		
		JLabel lblCsegSections = new JLabel("CSEG " + CSEG + " Sections");
		lblCsegSections.setHorizontalAlignment(SwingConstants.CENTER);
		lblCsegSections.setBounds(12, 12, 332, 28);
		getContentPane().add(lblCsegSections);
		
		JPanel panelChecks = new JPanel();
		panelChecks.setBounds(12, 42, 332, 330);
		getContentPane().add(panelChecks);
		panelChecks.setLayout(new GridLayout(0, 4, 0, 0));
		
		JPanel panelCancelSave = new JPanel();
		panelCancelSave.setBounds(0, 374, 356, 42);
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
		
		String[] section_names = style.getSectionNames();

		final int number_of_checks = section_names.length;
		JCheckBox[] checks = new JCheckBox[number_of_checks];
		for(int i=0;i<checks.length;i+=1) {
			checks[i] = new JCheckBox(section_names[i]);
			checks[i].setToolTipText("Check to add " + section_names[i] + " to CSEG " + CSEG + ", uncheck to remove.");
			panelChecks.add(checks[i]);
			
			for(int j=0;j<style.getCasm().sections.size();j+=1) {
				if(style.getCasm().sections.get(j).name.equals(section_names[i])) {
					//Depending on whether CSEG is the same or different, we do one of two things here.
					if(style.getCasm().sections.get(j).CSEG == CSEG)
						checks[i].setSelected(true);
					else
						checks[i].setEnabled(false);
					break;
				}
			}
		}
		
		panelChecks.setSize(new Dimension(panelChecks.getSize().width, check_height*(number_of_checks/4 + 1)));
		
		panelCancelSave.setBounds(panelCancelSave.getX(), panelChecks.getY() + panelChecks.getHeight(), panelCancelSave.getWidth(), panelCancelSave.getHeight());
		
		this.setSize(new Dimension(this.getWidth(), panelCancelSave.getY() + 2*panelCancelSave.getHeight()));
		
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				saveChanges(style, CSEG, checks);
			}
		});
		
		this.setVisible(true);
	}
	
	/** Close the window. */
	private void closeDialogBox() {
		this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}
	
	/** Save changes to the style. */
	private void saveChanges(Style style, int CSEG, JCheckBox[] checks) {
		if(style.getSectionNames().length != checks.length)
			return;
		
		for(int i=0;i<style.getCasm().sections.size();i+=1) {
			if(style.getCasm().sections.get(i).CSEG == CSEG) {
				style.getCasm().sections.remove(i);
				i -= 1;
			}
		}
		
		for(int i=0;i<checks.length;i+=1) {
			if(checks[i].isSelected()) {
				style.getCasm().sections.add(new CasmSect(style.getSectionNames()[i],CSEG));
			}
		}
		
		this.closeDialogBox();
	}
}
