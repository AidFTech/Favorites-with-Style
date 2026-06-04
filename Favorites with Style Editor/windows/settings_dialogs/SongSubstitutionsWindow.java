package settings_dialogs;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;

import main_window.FWSEditorMainWindow;
import song.FWSSong;
import voices.InstrumentProfile;
import voices.SequenceSubstitution;
import voices.Voice;

import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import controllers.FWSEditor;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JComboBox;
import javax.swing.JButton;

public class SongSubstitutionsWindow extends JDialog {
	private static final long serialVersionUID = -488745901831987121L;

	private ArrayList<String> profiles = new ArrayList<>();
	private ArrayList<Voice> song_voices = new ArrayList<>();

	private Map<String, SequenceSubstitution[]> active_profile = new LinkedHashMap<>();
	private SequenceSubstitution active_sub;

	private FWSEditor controller;

	public SongSubstitutionsWindow(FWSEditorMainWindow main_window, SongPropertiesWindow parent, FWSSong song) {
		super(main_window, true);

		controller = main_window.getController();

		this.setTitle("Song Substitutions");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(648, 550));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent != null ? parent : main_window);
		getContentPane().setLayout(null);
		
		JLabel label_profile = new JLabel("Profile");
		label_profile.setFont(new Font("Dialog", Font.BOLD, 16));
		label_profile.setHorizontalAlignment(SwingConstants.CENTER);
		label_profile.setBounds(12, 12, 200, 35);
		getContentPane().add(label_profile);

		JLabel label_songvoice = new JLabel("Song Voices");
		label_songvoice.setFont(new Font("Dialog", Font.BOLD, 16));
		label_songvoice.setHorizontalAlignment(SwingConstants.CENTER);
		label_songvoice.setBounds(224, 12, 200, 35);
		getContentPane().add(label_songvoice);

		JLabel label_subvoice = new JLabel("Alternate Voices");
		label_subvoice.setFont(new Font("Dialog", Font.BOLD, 16));
		label_subvoice.setHorizontalAlignment(SwingConstants.CENTER);
		label_subvoice.setBounds(436, 12, 200, 35);
		getContentPane().add(label_subvoice);

		JScrollPane scrollpane_profile = new JScrollPane();
		scrollpane_profile.setBounds(12, 61, 200, 309);
		getContentPane().add(scrollpane_profile);
		
		JList<String> list_profile = new JList<>();
		scrollpane_profile.setViewportView(list_profile);
		
		JScrollPane scrollpane_songvoice = new JScrollPane();
		scrollpane_songvoice.setBounds(224, 61, 200, 309);
		getContentPane().add(scrollpane_songvoice);
		
		JList<String> list_songvoice = new JList<>();
		scrollpane_songvoice.setViewportView(list_songvoice);
		
		JScrollPane scrollpane_subvoice = new JScrollPane();
		scrollpane_subvoice.setBounds(436, 61, 200, 309);
		getContentPane().add(scrollpane_subvoice);
		
		JList<String> list_subvoice = new JList<String>();
		scrollpane_subvoice.setViewportView(list_subvoice);

		{
			String[] profiles = song.getSubstitutionProfileList();
			for(String profile: profiles)
				this.profiles.add(profile);
		}

		for(String profile: this.profiles) {
			SequenceSubstitution[] song_subs = song.getSubstitutions(profile);
			if(song_subs == null)
				continue;
			
			SequenceSubstitution[] new_subs = new SequenceSubstitution[song_subs.length];

			for(int i=0;i<song_subs.length;i+=1) {
				Voice[] alt_voices = song_subs[i].getAlternates();

				Voice[] new_voices = new Voice[alt_voices.length];
				for(int v=0;v<alt_voices.length;v+=1)
					new_voices[v] = new Voice(alt_voices[v]);

				new_subs[i] = new SequenceSubstitution(song_subs[i].getVoice(), new_voices);
			}

			active_profile.put(profile, new_subs);
		}

		populateProfileList(this.profiles, list_profile);
		populateOriginalVoiceList(controller, song, song_voices, list_songvoice);
		
		JComboBox<String> dropdown_profile = new JComboBox<String>();
		dropdown_profile.setToolTipText("Select a profile to add to the list.");
		dropdown_profile.setBounds(12, 382, 136, 35);
		getContentPane().add(dropdown_profile);

		{
			String[] all_profiles = controller.getInstrumentProfileList();
			for(String profile: all_profiles)
				dropdown_profile.addItem(profile);
		}

		JButton button_profile_add = new JButton("Add");
		button_profile_add.setBounds(160, 382, 70, 35);
		button_profile_add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) { 
				final String selected_profile = (String)dropdown_profile.getSelectedItem();
				if(selected_profile == null)
					return;

				for(String profile: profiles) {
					if(profile.equalsIgnoreCase(selected_profile))
						return;
				}

				profiles.add(selected_profile);
				populateProfileList(profiles, list_profile);
			}
		});
		getContentPane().add(button_profile_add);
		
		JLabel label_instrument = new JLabel("Instrument");
		label_instrument.setHorizontalAlignment(SwingConstants.RIGHT);
		label_instrument.setBounds(12, 429, 94, 35);
		getContentPane().add(label_instrument);
		
		JComboBox<String> dropdown_instrument = new JComboBox<>();
		dropdown_instrument.setToolTipText("Select the instrument to select substitute voices from.");
		dropdown_instrument.setBounds(124, 429, 136, 35);
		getContentPane().add(dropdown_instrument);

		populateInstrumentDropdown(controller, dropdown_instrument, (String)dropdown_profile.getSelectedItem());
		
		JComboBox<String> dropdown_voice = new JComboBox<>();
		dropdown_voice.setToolTipText("Select an alternate voice to add.");
		dropdown_voice.setBounds(418, 382, 136, 35);
		getContentPane().add(dropdown_voice);
		
		SongSubstitutionsWindow self = this;
		
		JButton button_voice_add = new JButton("Add");
		button_voice_add.setBounds(566, 382, 70, 35);
		button_voice_add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(active_sub == null) {
					if(list_songvoice.getSelectedValue() == null || list_songvoice.getSelectedValue().isEmpty())
						return;
					
					if(list_profile.getSelectedValue() == null || list_profile.getSelectedValue().isEmpty())
						return;
					
					Voice[] song_voices = new Voice[self.song_voices.size()];
					for(int i=0;i<self.song_voices.size();i+=1)
						song_voices[i] = self.song_voices.get(i);
					
					Voice new_voice = Voice.matchVoice(song_voices, list_songvoice.getSelectedValue());

					active_sub = new SequenceSubstitution(new_voice);
					
					final String profile = list_profile.getSelectedValue();
					active_profile.put(profile, new SequenceSubstitution[] {active_sub});
				}

				final String voice = (String)dropdown_voice.getSelectedItem();
				if(voice == null)
					return;

				Voice[] alternates = active_sub.getAlternates();
				if(Voice.matchVoice(alternates, voice) != null)
					return;

				ArrayList<Voice> voice_vec = new ArrayList<>();
				for(Voice alt: alternates)
					voice_vec.add(alt);

				final String profile = list_profile.getSelectedValue(), instrument = (String)dropdown_instrument.getSelectedItem();

				InstrumentProfile profile_obj = controller.getInstrumentProfile(profile);
				Voice[] instrument_voices = profile_obj.getVoiceList(instrument);

				Voice match = Voice.matchVoice(instrument_voices, voice);
				voice_vec.add(match);

				active_sub.setAlternates(voice_vec);
				
				populateAlternateVoiceList(active_sub, list_subvoice);
			}
		});
		getContentPane().add(button_voice_add);

		JButton button_voice_remove = new JButton("Remove");
		button_voice_remove.setBounds(542, 429, 94, 35);
		button_voice_remove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final String selected = list_subvoice.getSelectedValue();
				if(selected == null)
					return;

				Voice[] alts = active_sub.getAlternates();
				Voice match = Voice.matchVoice(alts, selected);

				if(match == null)
					return;

				ArrayList<Voice> new_alts = new ArrayList<>();
				for(Voice alt: alts)
					new_alts.add(alt);

				new_alts.remove(match);
				active_sub.setAlternates(new_alts);

				populateAlternateVoiceList(active_sub, list_subvoice);
			}
		});
		getContentPane().add(button_voice_remove);

		JButton button_up = new JButton("▲");
		button_up.setBounds(432, 429, 43, 35);
		button_up.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final String selected = list_subvoice.getSelectedValue();
				if(selected == null)
					return;

				Voice[] alts = active_sub.getAlternates();
				Voice match = Voice.matchVoice(alts, selected);

				if(match == null)
					return;

				int index = -1;
				for(int i=0;i<alts.length;i+=1) {
					if(alts[i] == match) {
						index = i;
						break;
					}
				}

				if(index < 1)
					return;

				Voice swap = alts[index-1];
				alts[index-1] = match;
				alts[index] = swap;

				populateAlternateVoiceList(active_sub, list_subvoice);
			}
		});
		getContentPane().add(button_up);
		
		JButton button_down = new JButton("▼");
		button_down.setBounds(487, 429, 43, 35);
		button_down.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final String selected = list_subvoice.getSelectedValue();
				if(selected == null)
					return;

				Voice[] alts = active_sub.getAlternates();
				Voice match = Voice.matchVoice(alts, selected);

				if(match == null)
					return;

				int index = -1;
				for(int i=0;i<alts.length;i+=1) {
					if(alts[i] == match) {
						index = i;
						break;
					}
				}

				if(index < 0 || index >= alts.length-1)
					return;

				Voice swap = alts[index+1];
				alts[index+1] = match;
				alts[index] = swap;

				populateAlternateVoiceList(active_sub, list_subvoice);
			}
		});
		getContentPane().add(button_down);

		list_songvoice.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(list_profile.getSelectedValue() == null)
					return;

				final String voice = list_songvoice.getSelectedValue(), profile = list_profile.getSelectedValue();

				if(voice == null || profile == null)
					return;

				if(!setActiveSub(voice, profile)) {
					Voice[] song_voices = new Voice[self.song_voices.size()];
					for(int i=0;i<self.song_voices.size();i+=1)
						song_voices[i] = self.song_voices.get(i);
					
					Voice new_voice = Voice.matchVoice(song_voices, list_songvoice.getSelectedValue());

					active_sub = new SequenceSubstitution(new_voice);

					if(active_profile.get(profile) == null)
						active_profile.put(profile, new SequenceSubstitution[] {active_sub});
					else {
						SequenceSubstitution[] subs = active_profile.get(profile);
						ArrayList<SequenceSubstitution> subs_vec = new ArrayList<>();
						
						for(SequenceSubstitution sub: subs)
							subs_vec.add(sub);

						subs_vec.add(active_sub);

						SequenceSubstitution[] new_subs = new SequenceSubstitution[subs_vec.size()];
						subs_vec.toArray(new_subs);

						active_profile.put(profile, new_subs);
					}
				}
				populateAlternateVoiceList(active_sub, list_subvoice);
			}
		});

		list_profile.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(list_profile.getSelectedValue() == null) {
					populateInstrumentDropdown(controller, dropdown_instrument, "");
					populateVoiceDropdown(dropdown_voice, null, "");
					return;
				}

				final String profile = list_profile.getSelectedValue();
				populateInstrumentDropdown(controller, dropdown_instrument, profile);

				InstrumentProfile profile_obj = controller.getInstrumentProfile(profile);
				populateVoiceDropdown(dropdown_voice, profile_obj, (String)dropdown_instrument.getSelectedItem());

				list_songvoice.clearSelection();
				populateAlternateVoiceList(null, list_subvoice);
			}
		});

		dropdown_instrument.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final String profile = list_profile.getSelectedValue();
				InstrumentProfile profile_obj = controller.getInstrumentProfile(profile);

				populateVoiceDropdown(dropdown_voice, profile_obj, (String)dropdown_instrument.getSelectedItem());
			}
		});

		JButton button_cancel = new JButton("Cancel");
		button_cancel.setBounds(414, 503, 105, 35);
		button_cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_cancel);

		JButton button_apply = new JButton("Apply");
		button_apply.setBounds(531, 503, 105, 35);
		button_apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String[] profiles = song.getSubstitutionProfileList();
				for(String profile: profiles)
					song.removeSubstitutionList(profile);

				ArrayList<String> name_list = new ArrayList<String>();
				for(Entry<String, SequenceSubstitution[]> name: active_profile.entrySet())
					name_list.add(name.getKey());

				for(String profile: name_list) {
					SequenceSubstitution[] sub_list = active_profile.get(profile);
					if(sub_list.length <= 0)
						continue;
					
					ArrayList<SequenceSubstitution> new_list_vec = new ArrayList<>();
					for(SequenceSubstitution sub: sub_list) {
						if(sub.getAlternates().length <= 0)
							continue;

						new_list_vec.add(sub);
					}

					SequenceSubstitution[] new_list = new SequenceSubstitution[new_list_vec.size()];
					new_list_vec.toArray(new_list);

					song.addSubstitutionList(profile, new_list);
				}

				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_apply);

		this.setVisible(true);
	}

	/** Set the active sub voice. */
	private boolean setActiveSub(final String voice_name, final String profile) {
		if(voice_name == null) {
			active_sub = null;
			return false;
		}

		Voice[] song_voices = new Voice[this.song_voices.size()];
		for(int i=0;i<this.song_voices.size();i+=1)
			song_voices[i] = this.song_voices.get(i);
		
		Voice match = Voice.matchVoice(song_voices, voice_name);
		if(match == null)
			return false;

		SequenceSubstitution[] substitutions = active_profile.get(profile);
		if(substitutions == null)
			return false;

		ArrayList<Voice[]> profile_voices = new ArrayList<>();
		InstrumentProfile profile_obj = controller.getInstrumentProfile(profile);
		String[] instruments = profile_obj.getInstrumentNames();
		for(String instrument: instruments)
			profile_voices.add(profile_obj.getVoiceList(instrument));

		for(SequenceSubstitution sub: substitutions) {
			if(sub.getVoice().match(match)) {
				active_sub = sub;

				Voice[] alternates = sub.getAlternates();
				for(int i=0;i<alternates.length;i+=1) {
					Voice alt = alternates[i];
					if(!alt.name.isEmpty())
						continue;
					String name = "";

					for(Voice[] voice_list: profile_voices) {
						for(Voice instrument_voice: voice_list) {
							if(alt.match(instrument_voice)) {
								name = instrument_voice.name;
								break;
							}
						}

						if(!name.isEmpty())
							break;
					}

					if(name.isEmpty())
						name = Integer.toString(alt.msb) + ' ' + alt.lsb + ' ' + alt.voice;

					alternates[i] = new Voice(name, alt.voice, alt.lsb, alt.msb);
				}

				return true;
			}
		}

		return false;
	}

	/** Populate the profile list. */
	private static void populateProfileList(ArrayList<String> profiles, JList<String> list) {
		DefaultListModel<String> model = new DefaultListModel<String>();

		for(String profile: profiles)
			model.addElement(profile);

		list.setModel(model);
	}

	/** Populate the instrument list dropdown. */
	private static void populateInstrumentDropdown(FWSEditor controller, JComboBox<String> dropdown, String profile) {
		InstrumentProfile profile_obj = controller.getInstrumentProfile(profile);
		if(profile_obj == null)
			return;

		dropdown.removeAllItems();

		String[] instruments = profile_obj.getInstrumentNames();

		for(String instrument: instruments)
			dropdown.addItem(instrument);

		dropdown.setSelectedIndex(0);
	}

	/** Populate the voice dropdown. */
	private static void populateVoiceDropdown(JComboBox<String> dropdown, InstrumentProfile profile, final String instrument) {
		dropdown.removeAllItems();

		if(profile == null)
			return;
		
		Voice[] voices = profile.getVoiceList(instrument);
		if(voices == null)
			return;

		for(Voice voice: voices)
			dropdown.addItem(!voice.name.isEmpty() ? voice.name : voice.msb + " " + voice.lsb + " " + voice.voice);
	}

	/** Populate the original voice list. */
	private static void populateOriginalVoiceList(FWSEditor controller, FWSSong song, ArrayList<Voice> matched_voices, JList<String> list) {
		Voice[] song_voices = song.getAllVoices();
		
		final String profile_name = song.getTargetProfile(), instrument = song.getTargetInstrument();
		InstrumentProfile profile = controller.getInstrumentProfile(profile_name);
		Voice[] instrument_voices = profile.getVoiceList(instrument);

		DefaultListModel<String> model = new DefaultListModel<>();

		for(Voice voice: song_voices) {
			Voice match = Voice.matchVoice(instrument_voices, voice.voice, voice.lsb, voice.msb);
			if(match == null)
				continue;

			model.addElement(match.name);
			
			matched_voices.add(new Voice(match));
		}

		list.setModel(model);
	}

	/** Populate the alternate voice list. */
	private static void populateAlternateVoiceList(SequenceSubstitution substitution, JList<String> list) {
		if(substitution == null) {
			DefaultListModel<String> model = new DefaultListModel<String>();
			list.setModel(model);
			return;
		}

		DefaultListModel<String> model = new DefaultListModel<String>();

		Voice[] voices = substitution.getAlternates();
		for(Voice voice: voices)
			model.addElement(voice.name);

		list.setModel(model);
	}
}
