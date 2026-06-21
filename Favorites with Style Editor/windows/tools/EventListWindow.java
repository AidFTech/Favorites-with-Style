package tools;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;

import fwsevents.FWSEvent;
import fwsevents.FWSNoteEvent;
import fwsevents.FWSSequence;
import fwsevents.FWSShortEvent;
import fwsevents.FWSStyleChangeEvent;
import fwsevents.FWSVoiceEvent;
import main_window.FWSEditorMainWindow;
import sprites.Sprite;
import voices.Voice;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import canvas.SongViewPort;
import controllers.FWSEditor;
import event_dialogs.MultiEventDialog;
import event_dialogs.MultiNoteEventDialog;
import event_dialogs.MultiShortEventDialog;

import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class EventListWindow extends JDialog {
	private static final long serialVersionUID = 7776560316483565493L;

	private ArrayList<FWSEvent> event_list;
	private FWSEditor controller;

	public EventListWindow(FWSEditorMainWindow parent, FWSSequence sequence) {
		this(parent, sequence, sequence.getAllEvents());
	}

	public EventListWindow(FWSEditorMainWindow parent, FWSSequence sequence, ArrayList<FWSEvent> event_list) {
		super(parent, true);
		
		controller = parent.getController();
		EventListWindow self = this;

		this.setTitle("Event List");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setSize(new Dimension(538, 600));
		//this.getContentPane().setSize(getContentPane().getPreferredSize());
		//this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);

		this.event_list = event_list;
		DefaultListModel<String> event_list_model = new DefaultListModel<>();
		populateEventList(this.event_list, sequence, event_list_model);

		JList<String> jlist_event_list = new JList<>(event_list_model);

		JScrollPane list_panel = new JScrollPane(jlist_event_list);
		list_panel.setBounds(12, 12, 330, 360);
		getContentPane().add(list_panel);
		
		JButton button_selected_properties = new JButton("Selected Properties");
		button_selected_properties.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] indices = jlist_event_list.getSelectedIndices();
				if(indices.length > 1) {
					FWSEvent[] events = new FWSEvent[indices.length];

					for(int i=0;i<indices.length;i+=1)
						events[i] = event_list.get(indices[i]);

					boolean all_notes = true, all_shorts = true;
					boolean refreshed = false;

					for(FWSEvent event: events) {
						if(!(event instanceof FWSNoteEvent))
							all_notes = false;
						if(!(event instanceof FWSShortEvent) && !(event instanceof FWSVoiceEvent))
							all_shorts = false;

						if(!all_notes && !all_shorts)
							break;
					}

					if(all_notes) {
						FWSNoteEvent[] note_events = new FWSNoteEvent[events.length];
						for(int i=0;i<events.length;i+=1)
							note_events[i] = (FWSNoteEvent)events[i];

						MultiNoteEventDialog event_dialog = new MultiNoteEventDialog(parent, note_events);
						refreshed = event_dialog.getRefresh();
					} else if(all_shorts) {
						MultiShortEventDialog event_dialog = new MultiShortEventDialog(parent, events);
						refreshed = event_dialog.getRefresh();
					} else {
						MultiEventDialog event_dialog = new MultiEventDialog(parent, events);
						refreshed = event_dialog.getRefresh();
					}
					if(refreshed) {
						sortEventList();
						populateEventList(self.event_list, sequence, event_list_model);
						jlist_event_list.setModel(event_list_model);
					}
				} else if(indices.length == 1) {
					FWSEvent event = self.event_list.get(indices[0]);
					SongViewPort viewport = parent.getViewPort();
					Sprite sprite = viewport.getEventSprite(event);

					if(sprite == null)
						return;

					sprite.createDialog();

					sortEventList();
					populateEventList(self.event_list, sequence, event_list_model);
					jlist_event_list.setModel(event_list_model);
				}
			}
		});
		button_selected_properties.setToolTipText("Edit the properties of the selected event(s).");
		button_selected_properties.setBounds(354, 12, 157, 35);
		getContentPane().add(button_selected_properties);
		
		JButton button_delete_selected = new JButton("Delete Selected");
		button_delete_selected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final int answer = JOptionPane.showConfirmDialog(self, "Delete the selected events?", "Delete Events", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if(answer == JOptionPane.NO_OPTION)
					return;

				int[] indices = jlist_event_list.getSelectedIndices();
				FWSEvent[] events = new FWSEvent[indices.length];

				for(int i=0;i<indices.length;i+=1)
					events[i] = self.event_list.get(indices[i]);

				for(int i=0;i<events.length;i+=1) {
					sequence.removeEvent(events[i]);
					parent.getViewPort().removeSprite(events[i]);
				}
				parent.getViewPort().refresh();

				sortEventList();
				populateEventList(self.event_list, sequence, event_list_model);
				jlist_event_list.setModel(event_list_model);
			}
		});
		button_delete_selected.setToolTipText("Delete the selected events.");
		button_delete_selected.setBounds(354, 59, 157, 35);
		getContentPane().add(button_delete_selected);

		this.setVisible(true);
	}

	/** Populate/load an event list. */
	private void populateEventList(ArrayList<FWSEvent> event_list, FWSSequence sequence, DefaultListModel<String> event_list_model) {
		event_list_model.clear();
		for(int i=0;i<event_list.size();i+=1) {
			final long full_tick = event_list.get(i).tick;
			final int m = sequence.getMeasureAt(full_tick) + 1, b = sequence.getBeatAt(full_tick) + 1;
			final long tick = sequence.getTickAt(full_tick);

			String event_string = event_list.get(i).toString();
			if(event_list.get(i) instanceof FWSVoiceEvent) {
				FWSVoiceEvent voice_event = (FWSVoiceEvent)event_list.get(i);
				Voice[] voice_names = controller.getVoiceList();
				Voice match = Voice.matchVoice(voice_names, voice_event.voice, voice_event.voice_lsb, voice_event.voice_msb);

				if(match != null)
					event_string = "Channel " + (voice_event.channel + 1) + ": " + match.name;

			} else if(event_list.get(i) instanceof FWSStyleChangeEvent) {
				FWSStyleChangeEvent style_event = (FWSStyleChangeEvent)event_list.get(i);
				if(!style_event.style_name.trim().isEmpty())
					event_string = "Style: " + style_event.style_name + ", " + style_event.section_name;
				else
					event_string = "Style Off";
			}

			event_list_model.addElement(m + ":" + b + ":" + tick + ": " + event_string);
		}
	}

	/** Sort the event list. */
	private void sortEventList() {
		ArrayList<FWSEvent> new_list = new ArrayList<>();
		
		for(FWSEvent event: event_list) {
			int tick_index = -1;
			for(int i=0;i<new_list.size();i+=1) {
				if(new_list.get(i).tick > event.tick) {
					tick_index = i;
					break;
				}
			}

			if(tick_index < 0)
				new_list.add(event);
			else
				new_list.add(tick_index, event);
		}

		event_list = new_list;
	}
}
