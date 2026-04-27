package settings_dialogs;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import controllers.FWSEditor;
import main_window.FWSEditorMainWindow;
import song.FWSSong;
import style.Style;
import tools.NewStyleWindow;

public class StyleManagerWindow extends JDialog {
	private static final long serialVersionUID = 145239366576793564L;

	public StyleManagerWindow(FWSEditorMainWindow parent, FWSEditor controller, final boolean allow_modify) {
		super(parent, true);

		this.setTitle("Style Manager");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(530,450));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);

		StyleManagerWindow self = this;

		DefaultListModel<String> style_list_model = new DefaultListModel<String>();
		FWSSong loaded_song = controller.getLoadedSong();
		populateStyleList(style_list_model, loaded_song);

		JLabel label_style_info = new JLabel("No Style Selected");
		label_style_info.setVerticalAlignment(SwingConstants.TOP);
		label_style_info.setBounds(180, 183, 329, 176);
		getContentPane().add(label_style_info);

		JList<String> style_list = new JList<String>(style_list_model);
		style_list.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		style_list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(style_list.getSelectedIndex() >= 0) {
					String style_name = style_list.getSelectedValue();
					label_style_info.setText("<html>" + 
										"Long Name: " + loaded_song.getStyle(style_name).long_name + "<br/>" +
										"Short Name: " + loaded_song.getStyle(style_name).short_name + "<br/>" +
										"Sections: " + loaded_song.getStyle(style_name).getSectionNames().length + "<br/>" +
										"CASM Sections: " + loaded_song.getStyle(style_name).getCasm().parts.size() + "<br/>" +
										"TPQ: " + loaded_song.getStyle(style_name).getTPQ() + "<br/>" +
										"</html>"
					);
				} else
					label_style_info.setText("No Style Selected");
			}
		});
		style_list.setBounds(12, 12, 497, 159);
		getContentPane().add(style_list);

		JPanel style_button_panel = new JPanel();
		style_button_panel.setBounds(22, 183, 146, 215);
		getContentPane().add(style_button_panel);
		style_button_panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JButton button_add_style = new JButton("Add New Style");
		button_add_style.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Style new_style = new Style();
				NewStyleWindow new_style_window = new NewStyleWindow(parent, controller, new_style);
				if(new_style_window.getRefreshed()) {
					controller.addStyle(new_style);
					controller.setStyleViewMode(new_style);
					self.dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
				}
			}
		});
		button_add_style.setToolTipText("Add a new empty style to the list.");
		style_button_panel.add(button_add_style);

		JButton button_load_style = new JButton("Load Style");
		button_load_style.setToolTipText("Load a previously saved style to the list.");
		button_load_style.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(controller.loadStyle()) {
					populateStyleList(style_list_model, loaded_song);
					style_list.setModel(style_list_model);
				}
			}
		});
		style_button_panel.add(button_load_style);

		JButton button_modify_style = new JButton("Modify Style");
		button_modify_style.setToolTipText("Edit the selected style.");
		button_modify_style.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(style_list.getSelectedIndex() < 0)
					return;

				Style style = loaded_song.getStyle(style_list.getSelectedValue());
				if(style == null)
					return;

				parent.getController().setStyleViewMode(style);
				self.dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		button_modify_style.setEnabled(allow_modify);
		style_button_panel.add(button_modify_style);

		JButton button_casm_editor = new JButton("CASM Editor");
		button_casm_editor.setToolTipText("Edit the CASM data for the selected style.");
		button_casm_editor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(style_list.getSelectedIndex() < 0)
					return;

				Style style = loaded_song.getStyle(style_list.getSelectedValue());
				if(style != null)
					new CasmEditor(controller, parent, style);
			}
		});
		style_button_panel.add(button_casm_editor);

		JButton button_save_style = new JButton("Save Style");
		button_save_style.setToolTipText("Export the selected style as a .sty file.");
		button_save_style.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(style_list.getSelectedIndex() < 0)
					return;

				Style style = loaded_song.getStyle(style_list.getSelectedValue());
				if(style == null)
					return;

				controller.saveStyle(style);
			}
		});
		style_button_panel.add(button_save_style);
		
		JButton button_delete_style = new JButton("Delete Style");
		button_delete_style.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(style_list.getSelectedIndex() < 0)
					return;

				Style style = loaded_song.getStyle(style_list.getSelectedValue());
				if(style == null)
					return;
				
				loaded_song.removeStyle(style.long_name);
				populateStyleList(style_list_model, loaded_song);
				style_list.setModel(style_list_model);
			}
		});
		button_delete_style.setToolTipText("Remove the selected style.");
		style_button_panel.add(button_delete_style);

		this.setVisible(true);
	}
	
	/** Load styles into a list model. */
	private static void populateStyleList(DefaultListModel<String> style_list_model, FWSSong song) {
		style_list_model.clear();

		String[] style_names = song.getStyleNames();
		for(int i=0;i<style_names.length;i+=1)
			style_list_model.addElement(style_names[i]);
	}
}
