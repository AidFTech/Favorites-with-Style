package tools;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;

import event_dialogs.SequenceTickPanel;
import main_window.FWSEditorMainWindow;

public class PasteTickWindow extends JDialog {
	private long set_tick = -1;

	public PasteTickWindow(FWSEditorMainWindow parent, final long tick) {
		super(parent, true);

		this.setTitle("Event Properties");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(385, 175));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);

		SequenceTickPanel tick_panel = new SequenceTickPanel(parent, tick, 12, 12, 326, 100);
		tick_panel.setBounds(12, 12, 326, 100);
		getContentPane().add(tick_panel);

		PasteTickWindow self = this;

		JButton button_cancel = new JButton("Cancel");
		button_cancel.setBounds(151, 128, 105, 35);
		button_cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_cancel);

		JButton button_apply = new JButton("Paste");
		button_apply.setBounds(268, 128, 105, 35);
		button_apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				set_tick = tick_panel.getSetTick();
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_apply);

		this.setVisible(true);
	}

	/** Get the set tick. */
	public long getSetTick() {
		return this.set_tick;
	}
}
