package event_dialogs;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;

import fwsevents.FWSSequence;
import fwsevents.FWSStyleChangeEvent;
import main_window.FWSEditorMainWindow;
import song.FWSSong;
import style.Style;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import canvas.SongViewPort;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JCheckBox;

public class StyleChangeEventDialog extends JDialog {
	private static final long serialVersionUID = 2100513605103956171L;
	private final JLabel label_style = new JLabel("Style");

	public StyleChangeEventDialog(FWSEditorMainWindow parent, FWSStyleChangeEvent fws_event) {
		super(parent, true);

		this.setTitle("Style Change Event");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(359, 600));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);
		label_style.setHorizontalAlignment(SwingConstants.RIGHT);
		label_style.setBounds(12, 12, 60, 33);
		getContentPane().add(label_style);
		
		JLabel label_section = new JLabel("Section");
		label_section.setHorizontalAlignment(SwingConstants.RIGHT);
		label_section.setBounds(12, 57, 60, 33);
		getContentPane().add(label_section);
		
		JComboBox<String> style_dropdown = new JComboBox<>();
		style_dropdown.setBounds(90, 12, 244, 35);
		getContentPane().add(style_dropdown);

		style_dropdown.addItem("Accompaniment Off");

		FWSSong song = parent.getController().getLoadedSong();

		String[] styles = song.getStyleNames();
		for(int i=0;i<styles.length;i+=1)
			style_dropdown.addItem(styles[i]);

		style_dropdown.setSelectedIndex(0);
		style_dropdown.setSelectedItem(fws_event.style_name);
		
		JComboBox<String> section_dropdown = new JComboBox<String>();
		section_dropdown.setToolTipText("Select the style section to change to.");
		section_dropdown.setBounds(90, 57, 244, 35);
		getContentPane().add(section_dropdown);
		
		JPanel panel_style_parts = new JPanel();
		panel_style_parts.setBounds(22, 102, 312, 208);
		getContentPane().add(panel_style_parts);
		panel_style_parts.setLayout(new GridLayout(4, 2, 0, 0));
		
		JCheckBox checkbox_subrhythm = new JCheckBox("Sub-Rhythm");
		checkbox_subrhythm.setToolTipText("Check to enable the sub-rhythm track with this style change.");
		checkbox_subrhythm.setSelected(fws_event.sub_rhythm);
		panel_style_parts.add(checkbox_subrhythm);
		
		JCheckBox checkbox_main_rhythm = new JCheckBox("Rhythm");
		checkbox_main_rhythm.setToolTipText("Check to enable the main rhythm track with this style change.");
		checkbox_main_rhythm.setSelected(fws_event.rhythm);
		panel_style_parts.add(checkbox_main_rhythm);
		
		JCheckBox checkbox_bass = new JCheckBox("Bass");
		checkbox_bass.setToolTipText("Check to enable the bass track with this style change.");
		checkbox_bass.setSelected(fws_event.bass);
		panel_style_parts.add(checkbox_bass);
		
		JCheckBox checkbox_chord1 = new JCheckBox("Chord 1");
		checkbox_chord1.setToolTipText("Check to enable the Chord 1 track with this style change.");
		checkbox_chord1.setSelected(fws_event.chord1);
		panel_style_parts.add(checkbox_chord1);
		
		JCheckBox checkbox_chord2 = new JCheckBox("Chord 2");
		checkbox_chord2.setToolTipText("Check to enable the Chord 2 track with this style change.");
		checkbox_chord2.setSelected(fws_event.chord2);
		panel_style_parts.add(checkbox_chord2);
		
		JCheckBox checkbox_pad = new JCheckBox("Pad");
		checkbox_pad.setToolTipText("Check to enable the pad track with this style change.");
		checkbox_pad.setSelected(fws_event.pad);
		panel_style_parts.add(checkbox_pad);
		
		JCheckBox checkbox_phrase1 = new JCheckBox("Phrase 1");
		checkbox_phrase1.setToolTipText("Check to enable the first phrase track with this style change.");
		checkbox_phrase1.setSelected(fws_event.phrase1);
		panel_style_parts.add(checkbox_phrase1);
		
		JCheckBox checkbox_phrase2 = new JCheckBox("Phrase 2");
		checkbox_phrase2.setToolTipText("Check to enable the second phrase track with this style change.");
		checkbox_phrase2.setSelected(fws_event.phrase2);
		panel_style_parts.add(checkbox_phrase2);

		if(style_dropdown.getSelectedIndex() > 0) {
			Style start_style = song.getStyle((String)style_dropdown.getSelectedItem());
			populateSections(start_style, section_dropdown);
		}

		section_dropdown.setSelectedItem(fws_event.section_name);

		style_dropdown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Style new_style = null;
				if(style_dropdown.getSelectedIndex() > 0)
					new_style = song.getStyle((String)style_dropdown.getSelectedItem());

				populateSections(new_style, section_dropdown);
			}
		});

		SequenceTickPanel tick_panel = new SequenceTickPanel(parent, fws_event.tick, 12, 322, 326, 100);
		tick_panel.setBounds(12, 322, 326, 100);
		getContentPane().add(tick_panel);

		FWSSequence style_sequence = null;
		{
			Style new_style = null;
			if(style_dropdown.getSelectedIndex() > 0)
				new_style = song.getStyle((String)style_dropdown.getSelectedItem());

			if(new_style != null)
				style_sequence = new_style.getSection((String)section_dropdown.getSelectedItem());
		}

		StyleTickPanel style_tick_panel = new StyleTickPanel(parent, style_sequence, fws_event.style_tick, 12, 432, 326, 100);
		style_tick_panel.setBounds(12, 434, 326, 100);
		getContentPane().add(style_tick_panel);

		section_dropdown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Style new_style = null;
				if(style_dropdown.getSelectedIndex() > 0)
					new_style = song.getStyle((String)style_dropdown.getSelectedItem());
				
				if(new_style == null)
					return;

				FWSSequence style_sequence = new_style.getSection((String)section_dropdown.getSelectedItem());

				if(style_sequence != null)
					style_tick_panel.setSequence(style_sequence);
			}
		});

		style_dropdown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Style new_style = null;
				if(style_dropdown.getSelectedIndex() > 0)
					new_style = song.getStyle((String)style_dropdown.getSelectedItem());
				
				if(new_style != null) {
					section_dropdown.setEnabled(true);

					populateSections(new_style, section_dropdown);

					String[] sections = new_style.getSectionNames();
					int main_section_index = 0;
					for(int i=0;i<sections.length;i+=1) {
						if(sections[i].toUpperCase().contains("MAIN")) {
							main_section_index = i;
							break;
						}
					}

					section_dropdown.setSelectedIndex(main_section_index);
				} else
					section_dropdown.setEnabled(false);
			}
		});

		StyleChangeEventDialog self = this;

		JButton button_cancel = new JButton("Cancel");
		button_cancel.setBounds(131, 553, 105, 35);
		button_cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_cancel);
		
		JButton button_apply = new JButton("Apply");
		button_apply.setBounds(248, 553, 105, 35);
		button_apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				final long new_tick = tick_panel.getSetTick();

				SongViewPort vp = parent.getViewPort();
				FWSSequence sequence = vp.getActiveSequence();

				if(style_dropdown.getSelectedIndex() > 0) {
					fws_event.style_name = (String)style_dropdown.getSelectedItem();
					fws_event.section_name = (String)section_dropdown.getSelectedItem();
				} else {
					fws_event.style_name = "";
					fws_event.section_name = "";
				}

				fws_event.style_tick = style_tick_panel.getSetTick();
				
				fws_event.sub_rhythm = checkbox_subrhythm.isSelected();
				fws_event.rhythm = checkbox_main_rhythm.isSelected();
				fws_event.bass = checkbox_bass.isSelected();
				fws_event.chord1 = checkbox_chord1.isSelected();
				fws_event.chord2 = checkbox_chord2.isSelected();
				fws_event.pad = checkbox_pad.isSelected();
				fws_event.phrase1 = checkbox_phrase1.isSelected();
				fws_event.phrase2 = checkbox_phrase2.isSelected();

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

	/** Populate the section dropdown. */
	private void populateSections(Style style, JComboBox<String> section_box) {
		section_box.removeAllItems();

		if(style == null)
			return;
		
		int main_index = -1;
		String[] sections = style.getSectionNames();
		for(int s=0;s<sections.length;s+=1) {
			section_box.addItem(sections[s]);
			if(sections[s].toUpperCase().contains("MAIN") && main_index < 0)
				main_index = s;
		}

		section_box.setSelectedIndex(main_index >= 0 ? main_index : 0);
	}
}
