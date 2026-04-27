package tools;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.sound.midi.ShortMessage;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;

import fwsevents.FWSEvent;
import fwsevents.FWSNoteEvent;
import fwsevents.FWSSequence;
import fwsevents.FWSShortEvent;
import fwsevents.FWSVoiceEvent;
import main_window.FWSEditorMainWindow;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import event_dialogs.ChannelPressureEventDialog;
import event_dialogs.ControlEventDialog;
import event_dialogs.KeyPressureEventDialog;
import event_dialogs.MultiEventDialog;
import event_dialogs.NoteEventDialog;
import event_dialogs.PitchBendEventDialog;
import event_dialogs.VoiceEventDialog;

import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class EventListWindow extends JDialog {
	private static final long serialVersionUID = 7776560316483565493L;

	private ArrayList<FWSEvent> event_list;

	public EventListWindow(FWSEditorMainWindow parent, FWSSequence sequence) {
		super(parent, true);

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

		event_list = sequence.getAllEvents();
		DefaultListModel<String> event_list_model = new DefaultListModel<>();
		populateEventList(event_list, sequence, event_list_model);

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

					MultiEventDialog event_dialog = new MultiEventDialog(parent, events);
					if(event_dialog.getRefresh()) {
						event_list = sequence.getAllEvents();
						populateEventList(event_list, sequence, event_list_model);
						jlist_event_list.setModel(event_list_model);
					}
				} else if(indices.length == 1) {
					FWSEvent event = event_list.get(indices[0]);

					if(event instanceof FWSNoteEvent)
						new NoteEventDialog(parent, (FWSNoteEvent)event);
					else if(event instanceof FWSVoiceEvent)
						new VoiceEventDialog(parent, (FWSVoiceEvent)event);
					else if(event instanceof FWSShortEvent) {
						FWSShortEvent short_event = (FWSShortEvent)event;
						if((short_event.command&0xFF) == ShortMessage.CONTROL_CHANGE)
							new ControlEventDialog(parent, short_event);
						else if((short_event.command&0xFF) == ShortMessage.POLY_PRESSURE)
							new KeyPressureEventDialog(parent, short_event);
						else if((short_event.command&0xFF) == ShortMessage.CHANNEL_PRESSURE)
							new ChannelPressureEventDialog(parent, short_event);
						else if((short_event.command&0xFF) == ShortMessage.PITCH_BEND)
							new PitchBendEventDialog(parent, short_event);
					}

					event_list = sequence.getAllEvents();
					populateEventList(event_list, sequence, event_list_model);
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
					events[i] = event_list.get(indices[i]);

				for(int i=0;i<events.length;i+=1) {
					sequence.removeEvent(events[i]);
					parent.getViewPort().removeSprite(events[i]);
				}
				parent.getViewPort().refresh();

				event_list = sequence.getAllEvents();
				populateEventList(event_list, sequence, event_list_model);
				jlist_event_list.setModel(event_list_model);
			}
		});
		button_delete_selected.setToolTipText("Delete the selected events.");
		button_delete_selected.setBounds(354, 59, 157, 35);
		getContentPane().add(button_delete_selected);

		this.setVisible(true);
	}

	/** Populate/load an event list. */
	private static void populateEventList(ArrayList<FWSEvent> event_list, FWSSequence sequence, DefaultListModel<String> event_list_model) {
		event_list_model.clear();
		for(int i=0;i<event_list.size();i+=1) {
			final long full_tick = event_list.get(i).tick;
			final int m = sequence.getMeasureAt(full_tick) + 1, b = sequence.getBeatAt(full_tick) + 1;
			final long tick = sequence.getTickAt(full_tick);

			event_list_model.addElement(m + ":" + b + ":" + tick + ": " + event_list.get(i).toString());
		}
	}
}
