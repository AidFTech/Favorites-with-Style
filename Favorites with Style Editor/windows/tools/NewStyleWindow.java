package tools;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import controllers.FWSEditor;
import main_window.FWSEditorMainWindow;
import style.Style;

public class NewStyleWindow extends JDialog {
	private static final long serialVersionUID = 3082235616267850950L;

	private boolean refreshed = false;

	public NewStyleWindow(FWSEditorMainWindow parent, FWSEditor controller, Style style) {
		super(parent, true);

		this.setTitle("New Style");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(300,247));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);
		
		JLabel label_long_name = new JLabel("Long Name");
		label_long_name.setHorizontalAlignment(SwingConstants.RIGHT);
		label_long_name.setBounds(16, 12, 79, 35);
		getContentPane().add(label_long_name);
		
		JTextField field_long_name = new JTextField();
		field_long_name.setToolTipText("The full name of the new style.");
		field_long_name.setBounds(104, 12, 162, 35);
		getContentPane().add(field_long_name);
		field_long_name.setColumns(10);
		
		JLabel label_short_name = new JLabel("Short Name");
		label_short_name.setHorizontalAlignment(SwingConstants.RIGHT);
		label_short_name.setBounds(12, 59, 83, 35);
		getContentPane().add(label_short_name);
		
		JTextField field_short_name = new JTextField();
		field_short_name.setToolTipText("The eight-character short name of the new style as it would appear on the instrument's screen.");
		field_short_name.setColumns(10);
		field_short_name.setBounds(104, 59, 162, 35);
		getContentPane().add(field_short_name);
		
		JSpinner spinner_tpq = new JSpinner();
		spinner_tpq.setToolTipText("The ticks-per-quarter-note (TPQ) value of the new style.");
		spinner_tpq.setBounds(104, 106, 79, 35);
		spinner_tpq.setModel(new SpinnerNumberModel(Integer.valueOf(192),
				Integer.valueOf(64),
				Integer.valueOf(3840),
				Integer.valueOf(1)));
		getContentPane().add(spinner_tpq);
		
		JLabel label_tpq = new JLabel("TPQ");
		label_tpq.setHorizontalAlignment(SwingConstants.TRAILING);
		label_tpq.setBounds(25, 106, 70, 35);
		getContentPane().add(label_tpq);

		NewStyleWindow self = this;
		
		JButton button_cancel = new JButton("Cancel");
		button_cancel.setBounds(66, 200, 105, 35);
		button_cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_cancel);

		JButton button_create = new JButton("Create");
		button_create.setBounds(183, 200, 105, 35);
		button_create.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				style.init(field_long_name.getText(), field_short_name.getText(), (Integer)spinner_tpq.getValue());
				refreshed = true;
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_create);

		this.setVisible(true);
	}

	/** Get whether the style was successfully refreshed. */
	public boolean getRefreshed() {
		return this.refreshed;
	}
}
