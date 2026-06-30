package settings_dialogs;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;

import main_window.FWSEditorMainWindow;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;

public class TupletConfigurationWindow extends JDialog {
	private static final long serialVersionUID = 409976440230130620L;

	private int set_reference, set_div;
	private boolean applied = false;

	public TupletConfigurationWindow(FWSEditorMainWindow parent, final int init_reference, final int init_div) {
		super(parent, true);
		
		this.setTitle("Tuplet Configuration");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(360,160));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);

		set_reference = init_reference;
		set_div = init_div;
		
		JLabel label_tuplet_reference = new JLabel("Tuplet Reference");
		label_tuplet_reference.setHorizontalAlignment(SwingConstants.RIGHT);
		label_tuplet_reference.setBounds(12, 12, 121, 35);
		getContentPane().add(label_tuplet_reference);
		
		JComboBox<String> dropdown_reference = new JComboBox<>();
		dropdown_reference.setToolTipText("Select the reference note length to divide.");
		dropdown_reference.setModel(new DefaultComboBoxModel<>(new String[] {"Whole", "Half", "Quarter", "Eighth", "Sixteenth", "Thirty-Second"}));
		dropdown_reference.setBounds(151, 12, 181, 35);
		getContentPane().add(dropdown_reference);
		
		JLabel label_tuplet_div = new JLabel("Tuplet Division");
		label_tuplet_div.setHorizontalAlignment(SwingConstants.RIGHT);
		label_tuplet_div.setBounds(12, 59, 121, 35);
		getContentPane().add(label_tuplet_div);
		
		JComboBox<String> dropdown_div = new JComboBox<String>();
		dropdown_div.setToolTipText("Select the tuplet ratio.");
		dropdown_div.setBounds(151, 59, 181, 35);
		getContentPane().add(dropdown_div);

		for(int i=2;i<=7;i+=1)
			dropdown_div.addItem(Integer.toString(i));

		switch(init_reference) {
		case 32:
			dropdown_reference.setSelectedIndex(0);
			break;
		case 16:
			dropdown_reference.setSelectedIndex(1);
			break;
		case 8:
			dropdown_reference.setSelectedIndex(2);
			break;
		case 4:
			dropdown_reference.setSelectedIndex(3);
			break;
		case 2:
			dropdown_reference.setSelectedIndex(4);
			break;
		case 1:
			dropdown_reference.setSelectedIndex(5);
			break;
		default:
			dropdown_reference.setSelectedIndex(-1);
			break; 
		}

		if(init_div >= 2 && init_div <= 7)
			dropdown_div.setSelectedIndex(init_div - 2);
		else
			dropdown_div.setSelectedIndex(-1);

		TupletConfigurationWindow self = this;

		JButton button_cancel = new JButton("Cancel");
		button_cancel.setBounds(126, 113, 105, 35);
		button_cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_cancel);

		JButton button_apply = new JButton("Apply");
		button_apply.setBounds(243, 113, 105, 35);
		button_apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				applied = true;

				set_reference = (int)Math.pow(2, 5-dropdown_reference.getSelectedIndex());
				set_div = dropdown_div.getSelectedIndex() + 2;
				
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_apply);

		this.setVisible(true);
	}

	/** Get the values set in the window. */
	public int[] getSetValues() {
		return new int[] {this.set_reference, this.set_div};
	}

	/** Return whether Apply was clicked. */
	public boolean getApplied() {
		return this.applied;
	}
}
