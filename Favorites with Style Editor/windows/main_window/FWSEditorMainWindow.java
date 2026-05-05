package main_window;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import controllers.FWSEditor;
import fwsevents.FWSSequence;
import infobox.InfoBox;
import main_window.NoteToggleButton.NoteToggle;
import options.MIDIPlayerOptions;
import settings_dialogs.FirstVoicesWindow;
import settings_dialogs.SequencePropertiesWindow;
import settings_dialogs.SongPropertiesWindow;
import settings_dialogs.StyleManagerWindow;
import style.Style;
import tools.EventListWindow;
import tools.MIDIDeviceWindow;
import tools.TickShiftWindow;
import voices.InstrumentProfile;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;

import canvas.CanvasOptionGroup;
import canvas.Playhead;
import canvas.SongViewPort;

import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.SwingConstants;

import javax.swing.JButton;

public class FWSEditorMainWindow extends JFrame {
	public enum DisplayMode {
		DISPLAY_MODE_SONG,
		DISPLAY_MODE_STYLE,
		DISPLAY_MODE_MIDI,
	}

	private static final long serialVersionUID = 9061546918408750173L;

	private static final int min_w = 964, min_h = 720;
	private static final int song_pane_x = 250, song_pane_y = 47, song_pane_to_edge_x = 270, song_pane_to_edge_y = 291;
	private static final int info_to_edge_x = 70, info_to_edge_y = 30;

	private SongViewPort song_viewport;

	private FWSEditor controller;

	private DisplayMode display_mode = DisplayMode.DISPLAY_MODE_SONG;
	private ArrayList<Component> style_mode_visible = new ArrayList<>(0), style_mode_locked = new ArrayList<>(0),
								song_mode_visible = new ArrayList<>(0), song_mode_locked = new ArrayList<>(0),
								midi_mode_visible = new ArrayList<>(0), midi_mode_locked = new ArrayList<>(0);

	private JComboBox<String> dropdown_section;
	private JRadioButtonMenuItem style_song_view_selector, midi_scratch_view_selector;
	private boolean view_selector_listen = true;

	private JMenu voice_menu = null;
	private JRadioButtonMenuItem gm_option = null;

	public FWSEditorMainWindow(FWSEditor controller) {
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(FWSEditorMainWindow.class.getResource("/controllers/FWS Icon.png")));

		this.setTitle("Favorites: with Style! - Untitled Song");
		this.setSize(min_w, min_h);
		this.setFocusable(true);
		this.requestFocus();
		this.setMinimumSize(new Dimension(min_w, min_h));

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.getContentPane().setLayout(null);
		this.setLocationRelativeTo(null);

		this.controller = controller;
		FWSEditorMainWindow self = this;
		
		//Add the main menu bar.
		JMenuBar main_menu_bar = new JMenuBar();
		setJMenuBar(main_menu_bar);
		
		JMenu menu_file = new JMenu("File");
		main_menu_bar.add(menu_file);

		JMenuItem menu_item_load = new JMenuItem("Open");
		menu_item_load.setIcon(new ImageIcon(FWSEditorMainWindow.class.getResource("/icons/menu_open.png")));
		menu_item_load.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK)); //TODO: Different accelerator for Mac?
		menu_item_load.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.loadSong();
			}
		});
		menu_file.add(menu_item_load);
		
		JMenuItem menu_item_save = new JMenuItem("Save");
		menu_item_save.setIcon(new ImageIcon(FWSEditorMainWindow.class.getResource("/icons/menu_save.png")));
		menu_item_save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK)); //TODO: Different accelerator for Mac?
		menu_item_save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.saveSong();
			}
		});
		menu_file.add(menu_item_save);

		JMenuItem menu_item_save_as = new JMenuItem("Save As");
		menu_item_save_as.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK)); //TODO: Different accelerator for Mac?
		menu_item_save_as.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.saveSongAs();
			}
		});
		menu_file.add(menu_item_save_as);
		
		menu_file.addSeparator();

		JMenuItem menu_item_import_fws = new JMenuItem("Import Legacy FWS");
		menu_item_import_fws.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				self.controller.importLegacyFWS();
			}
		});
		menu_file.add(menu_item_import_fws);

		JMenuItem menu_item_import_midi = new JMenuItem("Import MIDI Data");
		menu_item_import_midi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				self.controller.importMidi();
			}
		});
		menu_file.add(menu_item_import_midi);

		JMenuItem menu_item_export_midi = new JMenuItem("Export MIDI Data");
		menu_item_export_midi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				self.controller.exportMidi();
			}
		});
		menu_file.add(menu_item_export_midi);

		JMenu menu_song = new JMenu("Song/Sequence");
		main_menu_bar.add(menu_song);

		JMenuItem menu_item_initial_voices = new JMenuItem("Initial Voices");
		menu_song.add(menu_item_initial_voices);

		menu_song.addSeparator();

		JMenuItem menu_item_event_list = new JMenuItem("Event List");
		menu_song.add(menu_item_event_list);

		JMenuItem menu_item_shift_events = new JMenuItem("Shift Events in Range");
		menu_song.add(menu_item_shift_events);
		
		JMenuItem menu_item_clear_sequence = new JMenuItem("Clear Sequence");
		menu_song.add(menu_item_clear_sequence);

		menu_song.addSeparator();

		JMenuItem menu_item_song_properties = new JMenuItem("Song Properties");
		menu_item_song_properties.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new SongPropertiesWindow(self, controller.getLoadedSong().getSongMetadata());
			}
		});
		menu_song.add(menu_item_song_properties);
		song_mode_locked.add(menu_item_song_properties);

		JMenuItem menu_item_sequence_properties = new JMenuItem("Sequence Properties");
		menu_song.add(menu_item_sequence_properties);

		menu_song.addSeparator();

		JRadioButtonMenuItem menu_item_song_style_view = new JRadioButtonMenuItem("Song/Style View");
		menu_item_song_style_view.setSelected(true);
		menu_song.add(menu_item_song_style_view);

		JRadioButtonMenuItem menu_item_scratch_view = new JRadioButtonMenuItem("MIDI Scratch View");
		menu_song.add(menu_item_scratch_view);

		style_song_view_selector = menu_item_song_style_view;
		midi_scratch_view_selector = menu_item_scratch_view;

		ButtonGroup view_group = new ButtonGroup();
		view_group.add(menu_item_song_style_view);
		view_group.add(menu_item_scratch_view);

		song_mode_locked.add(menu_item_song_style_view);
		song_mode_locked.add(menu_item_scratch_view);
		midi_mode_locked.add(menu_item_song_style_view);
		midi_mode_locked.add(menu_item_scratch_view);

		menu_item_scratch_view.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!view_selector_listen)
					return;

				if(display_mode == DisplayMode.DISPLAY_MODE_MIDI)
					return;

				view_selector_listen = false;
				controller.setViewMode(DisplayMode.DISPLAY_MODE_MIDI);
				view_selector_listen = true;
			}
		});

		menu_item_song_style_view.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!view_selector_listen)
					return;

				if(display_mode != DisplayMode.DISPLAY_MODE_MIDI)
					return;

				view_selector_listen = false;
				controller.setViewMode(DisplayMode.DISPLAY_MODE_SONG);
				view_selector_listen = true;
			}
		});

		//Style menu:
		JMenu menu_style = new JMenu("Style");
		main_menu_bar.add(menu_style);

		JMenuItem menu_item_style_manager = new JMenuItem("Style Manager");
		menu_item_style_manager.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new StyleManagerWindow(self, controller, self.display_mode == DisplayMode.DISPLAY_MODE_SONG);
			}
		});
		menu_style.add(menu_item_style_manager);
		song_mode_locked.add(menu_item_style_manager);
		midi_mode_locked.add(menu_item_style_manager);

		JMenu menu_midi = new JMenu("MIDI");
		main_menu_bar.add(menu_midi);
		
		JMenuItem menu_item_configure_devices = new JMenuItem("Configure Connected Devices");
		menu_item_configure_devices.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new MIDIDeviceWindow(self, self.controller.getMidiManager());
			}
		});
		menu_midi.add(menu_item_configure_devices);

		JMenuItem menu_item_configure_instrument = new JMenuItem("Configure Instrument");
		menu_item_configure_instrument.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		menu_midi.add(menu_item_configure_instrument);

		menu_midi.addSeparator();

		//Add the voice list.
		JMenu menu_voice_list = new JMenu("Voice List");
		this.voice_menu = menu_voice_list;
		menu_midi.add(menu_voice_list);

		JRadioButtonMenuItem menu_item_gm_list = new JRadioButtonMenuItem("General MIDI");
		this.gm_option = menu_item_gm_list;
		menu_item_gm_list.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.setInstrumentFamily("");
				controller.setVoiceList("");

				song_viewport.refreshFull();
			}
		});
		menu_voice_list.add(menu_item_gm_list);
		populateVoiceList();

		JMenuItem menu_item_refresh_voice_list = new JMenuItem("Refresh Voice List");
		menu_midi.add(menu_item_refresh_voice_list);
		menu_item_refresh_voice_list.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.refreshInstrumentProfiles();
				populateVoiceList();
			}
		});
		
		//Add transport controls.
		JButton button_stop = new JButton("");
		button_stop.setIcon(new ImageIcon(FWSEditorMainWindow.class.getResource("/icons/icon_stop.png")));
		button_stop.setBounds(16, 12, 35, 27);
		button_stop.setFocusable(false);
		getContentPane().add(button_stop);
		
		JButton button_play = new JButton("");
		button_play.setIcon(new ImageIcon(FWSEditorMainWindow.class.getResource("/icons/icon_play.png")));
		button_play.setBounds(51, 12, 35, 27);
		button_play.setFocusable(false);
		getContentPane().add(button_play);

		JButton button_prev = new JButton("");
		button_prev.setIcon(new ImageIcon(FWSEditorMainWindow.class.getResource("/icons/icon_back.png")));
		button_prev.setBounds(86, 12, 35, 27);
		button_prev.setFocusable(false);
		getContentPane().add(button_prev);

		JButton button_next = new JButton("");
		button_next.setIcon(new ImageIcon(FWSEditorMainWindow.class.getResource("/icons/icon_next.png")));
		button_next.setBounds(121, 12, 35, 27);
		button_next.setFocusable(false);
		getContentPane().add(button_next);

		song_mode_locked.add(button_next);
		song_mode_locked.add(button_prev);
		midi_mode_locked.add(button_next);
		midi_mode_locked.add(button_prev);

		JButton button_record = new JButton("");
		button_record.setIcon(new ImageIcon(FWSEditorMainWindow.class.getResource("/icons/icon_record.png")));
		button_record.setBounds(156, 12, 35, 27);
		button_record.setFocusable(false);
		getContentPane().add(button_record);
		
		//Add channel controls.
		JPanel panel_channel_list = new JPanel();
		panel_channel_list.setLayout(new BoxLayout(panel_channel_list, BoxLayout.Y_AXIS));

		ButtonGroup channel_group = new ButtonGroup();
		JRadioButton[] radiobutton_channel = new JRadioButton[16];

		for(int i=0;i<16;i+=1) {
			final int channel = i;
			JPanel panel_channel = new JPanel();
			panel_channel.setBounds(0, 0, panel_channel.getWidth(), 100);
			panel_channel.setFocusable(false);
			panel_channel.setLayout(new FlowLayout());
			panel_channel.setBackground(controller.getChannelColors()[channel]);
			panel_channel.repaint();
			panel_channel_list.add(panel_channel);

			JLabel channel_label = new JLabel("Channel " + (channel + 1));
			channel_label.setBounds(0,0,120,70);
			panel_channel.add(channel_label);

			JRadioButton radiobutton = new JRadioButton("Set Active");
			radiobutton.setBounds(0, 0, 80, 35);
			radiobutton.setOpaque(false);
			radiobutton.setFocusable(false);
			radiobutton_channel[channel] = radiobutton;
			channel_group.add(radiobutton);
			panel_channel.add(radiobutton);
			
			if(channel == 0)
				radiobutton.setSelected(true);

			radiobutton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(radiobutton.isSelected())
						song_viewport.setSelectedChannel(channel);
				}
				
			});
		}
		
		JScrollPane scroll_pane_channel = new JScrollPane(panel_channel_list);
		scroll_pane_channel.setBounds(12, 74, song_pane_x - 12*2, 321);

		getContentPane().add(scroll_pane_channel);
		panel_channel_list.repaint();
		scroll_pane_channel.repaint();

		//Add the toolbar.
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setBounds(250, 12, 694, 35);
		this.getContentPane().add(toolbar);

		NoteToggleButton togglebutton_place_control = new NoteToggleButton(NoteToggle.NOTE_TOGGLE_CTL);
		togglebutton_place_control.setFocusable(false);
		togglebutton_place_control.setToolTipText("Place any control event, including any standard Midi control event or FWS style control event.");
		togglebutton_place_control.setIcon(new ImageIcon(FWSEditorMainWindow.class.getResource("/icons/icon_ctl.png")));
		toolbar.add(togglebutton_place_control);

		NoteToggleButton togglebutton_place_whole = new NoteToggleButton(NoteToggle.NOTE_TOGGLE_WHOLE);
		togglebutton_place_whole.setFocusable(false);
		togglebutton_place_whole.setIcon(new ImageIcon(FWSEditorMainWindow.class.getResource("/icons/icon_whole.png")));
		togglebutton_place_whole.setToolTipText("Place a whole note.");
		toolbar.add(togglebutton_place_whole);

		NoteToggleButton togglebutton_place_half = new NoteToggleButton(NoteToggle.NOTE_TOGGLE_HALF);
		togglebutton_place_half.setFocusable(false);
		togglebutton_place_half.setIcon(new ImageIcon(FWSEditorMainWindow.class.getResource("/icons/icon_half.png")));
		togglebutton_place_half.setToolTipText("Place a half note.");
		toolbar.add(togglebutton_place_half);

		NoteToggleButton togglebutton_place_quarter = new NoteToggleButton(NoteToggle.NOTE_TOGGLE_QUARTER);
		togglebutton_place_quarter.setFocusable(false);
		togglebutton_place_quarter.setIcon(new ImageIcon(FWSEditorMainWindow.class.getResource("/icons/icon_quarter.png")));
		togglebutton_place_quarter.setToolTipText("Place a quarter note.");
		toolbar.add(togglebutton_place_quarter);

		NoteToggleButton togglebutton_place_eighth = new NoteToggleButton(NoteToggle.NOTE_TOGGLE_EIGHTH);
		togglebutton_place_eighth.setFocusable(false);
		togglebutton_place_eighth.setIcon(new ImageIcon(FWSEditorMainWindow.class.getResource("/icons/icon_eighth.png")));
		togglebutton_place_eighth.setToolTipText("Place an eighth note.");
		toolbar.add(togglebutton_place_eighth);

		NoteToggleButton togglebutton_place_sixteenth = new NoteToggleButton(NoteToggle.NOTE_TOGGLE_SIXTEENTH);
		togglebutton_place_sixteenth.setFocusable(false);
		togglebutton_place_sixteenth.setToolTipText("Place a sixteenth note.");
		togglebutton_place_sixteenth.setIcon(new ImageIcon(FWSEditorMainWindow.class.getResource("/icons/icon_16.png")));
		toolbar.add(togglebutton_place_sixteenth);

		NoteToggleButton togglebutton_place_thirtysecond = new NoteToggleButton(NoteToggle.NOTE_TOGGLE_32);
		togglebutton_place_thirtysecond.setFocusable(false);
		togglebutton_place_thirtysecond.setIcon(new ImageIcon(FWSEditorMainWindow.class.getResource("/icons/icon_32.png")));
		togglebutton_place_thirtysecond.setToolTipText("Place a thirty-second note.");
		toolbar.add(togglebutton_place_thirtysecond);

		JToggleButton togglebutton_dot = new JToggleButton("");
		togglebutton_dot.setFocusable(false);
		togglebutton_dot.setToolTipText("Increases the length of the note to be placed by half.");
		togglebutton_dot.setIcon(new ImageIcon(FWSEditorMainWindow.class.getResource("/icons/icon_dot.png")));
		toolbar.add(togglebutton_dot);

		NoteToggleButton togglebutton_place_tuplet = new NoteToggleButton(NoteToggle.NOTE_TOGGLE_TUPLET);
		togglebutton_place_tuplet.setFocusable(false);
		togglebutton_place_tuplet.setToolTipText("Place a tuplet note.");
		togglebutton_place_tuplet.setIcon(new ImageIcon(FWSEditorMainWindow.class.getResource("/icons/icon_tuplet.png")));
		toolbar.add(togglebutton_place_tuplet);

		NoteToggleButton togglebutton_place_custom = new NoteToggleButton(NoteToggle.NOTE_TOGGLE_CUSTOM);
		togglebutton_place_custom.setFocusable(false);
		togglebutton_place_custom.setIcon(new ImageIcon(FWSEditorMainWindow.class.getResource("/icons/icon_custom.png")));
		togglebutton_place_custom.setToolTipText("Place a note of custom length (in ticks).");
		toolbar.add(togglebutton_place_custom);

		//Add the song viewport.
		final int vp_w = self.getContentPane().getWidth() - song_pane_to_edge_x, vp_h = self.getContentPane().getHeight() - song_pane_to_edge_y;
		song_viewport = new SongViewPort(this.controller, this, song_pane_x, song_pane_y, new Dimension(vp_w, vp_h));
		song_viewport.setLocation(song_pane_x, song_pane_y);
		song_viewport.bringToFront();
		
		this.getContentPane().add(song_viewport);

		song_viewport.getPianoRoll().add(new Playhead(song_viewport, song_viewport.getPianoRoll(), controller.getMidiManager().getPlayerOptions()));
		
		song_viewport.fill(controller.getActiveSequence(), false);
		song_viewport.refresh();

		menu_item_initial_voices.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new FirstVoicesWindow(self);
			}
		});

		menu_item_event_list.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new EventListWindow(self, song_viewport.getActiveSequence());
			}
		});

		menu_item_sequence_properties.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new SequencePropertiesWindow(self, song_viewport.getActiveSequence());
			}
		});

		menu_item_shift_events.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new TickShiftWindow(self, song_viewport.getActiveSequence(), self.display_mode == DisplayMode.DISPLAY_MODE_STYLE);
			}
		});

		menu_item_clear_sequence.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final int answer = JOptionPane.showConfirmDialog(self, "This will clear all events in the sequence. This cannot be undone. Proceed?", "Clear Sequence", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if(answer == JOptionPane.NO_OPTION)
					return;

				song_viewport.getActiveSequence().init();
				song_viewport.fill(song_viewport.getActiveSequence(), true);
			}
		});

		//Back to the toolbar:
		JSpinner spinner_note_len = new JSpinner();
		spinner_note_len.setFocusable(false);
		spinner_note_len.setToolTipText("Set the desired custom note length to place.");
		spinner_note_len.setModel(new SpinnerNumberModel((long)song_viewport.getSnap(), 0L, (Comparable<Long>)null, 1L));
		spinner_note_len.setMinimumSize(new Dimension(70, 30));
		spinner_note_len.setMaximumSize(new Dimension(70, 30));
		spinner_note_len.setPreferredSize(new Dimension(70, 30));
		toolbar.add(spinner_note_len);

		NoteToggleButton togglebutton_place_drag = new NoteToggleButton(NoteToggle.NOTE_TOGGLE_DRAG);
		togglebutton_place_drag.setFocusable(false);
		togglebutton_place_drag.setIcon(new ImageIcon(FWSEditorMainWindow.class.getResource("/icons/icon_drag.png")));
		togglebutton_place_drag.setToolTipText("Drag a note event for custom length.");
		toolbar.add(togglebutton_place_drag);

		NoteToggleGroup note_place_group = new NoteToggleGroup();
		note_place_group.add(togglebutton_place_control);
		note_place_group.add(togglebutton_place_whole);
		note_place_group.add(togglebutton_place_half);
		note_place_group.add(togglebutton_place_quarter);
		note_place_group.add(togglebutton_place_eighth);
		note_place_group.add(togglebutton_place_sixteenth);
		note_place_group.add(togglebutton_place_thirtysecond);
		note_place_group.add(togglebutton_place_tuplet);
		note_place_group.add(togglebutton_place_custom);
		note_place_group.add(togglebutton_place_drag);

		toolbar.add(new JSeparator(1));

		//Snap menu:

		final JPopupMenu menu_snap = new JPopupMenu("");
		addPopup(this.getContentPane(), menu_snap);

		JRadioButtonMenuItem snap_button_free = new JRadioButtonMenuItem("Free (no snap)");
		snap_button_free.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				song_viewport.setSnap(1);
			}
		});
		menu_snap.add(snap_button_free);

		JRadioButtonMenuItem snap_button_quarter = new JRadioButtonMenuItem("Quarter");
		snap_button_quarter.setIcon(new ImageIcon(FWSEditorMainWindow.class.getResource("/icons/icon_quarter_16.png")));
		snap_button_quarter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FWSSequence sequence = song_viewport.getActiveSequence();
				if(sequence == null)
					return;

				song_viewport.setSnap(sequence.getTPQ());
			}
		});
		menu_snap.add(snap_button_quarter);

		JRadioButtonMenuItem snap_button_eighth = new JRadioButtonMenuItem("Eighth");
		snap_button_eighth.setIcon(new ImageIcon(FWSEditorMainWindow.class.getResource("/icons/icon_eighth_16.png")));
		snap_button_eighth.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FWSSequence sequence = song_viewport.getActiveSequence();
				if(sequence == null)
					return;

				song_viewport.setSnap(sequence.getTPQ()/2);
			}
		});
		menu_snap.add(snap_button_eighth);

		JRadioButtonMenuItem snap_button_sixteenth = new JRadioButtonMenuItem("Sixteenth");
		snap_button_sixteenth.setIcon(new ImageIcon(FWSEditorMainWindow.class.getResource("/icons/icon_16_16.png")));
		snap_button_sixteenth.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FWSSequence sequence = song_viewport.getActiveSequence();
				if(sequence == null)
					return;

				song_viewport.setSnap(sequence.getTPQ()/4);
			}
		});
		menu_snap.add(snap_button_sixteenth);

		JRadioButtonMenuItem snap_button_thirtysecond = new JRadioButtonMenuItem("Thirty-Second");
		snap_button_thirtysecond.setIcon(new ImageIcon(FWSEditorMainWindow.class.getResource("/icons/icon_32_16.png")));
		snap_button_thirtysecond.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FWSSequence sequence = song_viewport.getActiveSequence();
				if(sequence == null)
					return;

				song_viewport.setSnap(sequence.getTPQ()/8);
			}
		});
		menu_snap.add(snap_button_thirtysecond);

		ButtonGroup snap_group = new ButtonGroup();
		snap_group.add(snap_button_free);
		snap_group.add(snap_button_quarter);
		snap_group.add(snap_button_eighth);
		snap_group.add(snap_button_sixteenth);
		snap_group.add(snap_button_thirtysecond);

		//And back to the toolbar.
		JButton button_snap = new JButton("");
		button_snap.setFocusable(false);
		button_snap.setIcon(new ImageIcon(FWSEditorMainWindow.class.getResource("/icons/icon_snap.png")));
		button_snap.setToolTipText("Change the snap alignment of events to be placed.");
		button_snap.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FWSSequence sequence = song_viewport.getActiveSequence();
				if(sequence == null)
					return;

				final int tpq = sequence.getTPQ(), snap = song_viewport.getSnap();
				if(snap == tpq)
					snap_button_quarter.setSelected(true);
				else if(snap == tpq/2)
					snap_button_eighth.setSelected(true);
				else if(snap == tpq/4)
					snap_button_sixteenth.setSelected(true);
				else if(snap == tpq/8)
					snap_button_thirtysecond.setSelected(true);
				else if(snap == 1)
					snap_button_free.setSelected(true);

				menu_snap.show((Component)e.getSource(), 0, 0);
			}
		});
		toolbar.add(button_snap);
		
		JLabel label_velocity = new JLabel("Velocity");
		label_velocity.setHorizontalAlignment(SwingConstants.RIGHT);
		label_velocity.setBounds(22, 407, 69, 35);
		getContentPane().add(label_velocity);
		
		JSpinner spinner_velocity = new JSpinner();
		spinner_velocity.setToolTipText("Set the velocity of placed notes.");
		spinner_velocity.setFocusable(false);
		spinner_velocity.setModel(new SpinnerNumberModel(100, 0, 127, 1));
		spinner_velocity.setBounds(109, 407, 75, 35);
		getContentPane().add(spinner_velocity);
		
		JCheckBox checkbox_play_dropped_note = new JCheckBox("Play Dropped Note");
		checkbox_play_dropped_note.setFocusable(false);
		checkbox_play_dropped_note.setSelected(true);
		checkbox_play_dropped_note.setToolTipText("Check to play a note when dropped.");
		checkbox_play_dropped_note.setBounds(28, 459, 156, 35);
		getContentPane().add(checkbox_play_dropped_note);

		CanvasOptionGroup option_group = new CanvasOptionGroup();
		option_group.note_place_group = note_place_group;
		option_group.spinner_note_len = spinner_note_len;
		option_group.togglebutton_dot = togglebutton_dot;
		option_group.spinner_velocity = spinner_velocity;
		option_group.checkbox_play_note = checkbox_play_dropped_note;
		
		song_viewport.setOptionGroup(option_group);

		JButton button_return_to_song = new JButton("Return to Song");
		button_return_to_song.setFocusable(false);
		button_return_to_song.setVisible(false);
		button_return_to_song.setToolTipText("Return to the main song.");
		button_return_to_song.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final int answer = JOptionPane.showConfirmDialog(self, "Save the style before returning to the song?", "Save", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				if(answer == JOptionPane.CANCEL_OPTION)
					return;
				else if(answer == JOptionPane.YES_OPTION) {
					controller.saveActiveStyle();
					controller.setStyleViewMode(null);
				} else if(answer == JOptionPane.NO_OPTION) {
					controller.setStyleViewMode(null);
				}
			}
		});
		button_return_to_song.setBounds(28, 510, 150, 35);
		style_mode_visible.add(button_return_to_song);
		getContentPane().add(button_return_to_song);

		JLabel label_section = new JLabel("Section");
		label_section.setBounds(48, 560, 130, 30);
		label_section.setVisible(false);
		label_section.setHorizontalAlignment(SwingConstants.CENTER);
		style_mode_visible.add(label_section);
		getContentPane().add(label_section);

		dropdown_section = new JComboBox<>();
		dropdown_section.setFocusable(false);
		dropdown_section.setVisible(false);
		dropdown_section.setToolTipText("Select the section of the style to preview.");
		dropdown_section.setBounds(48, 590, 130, 35);
		dropdown_section.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.getMidiManager().getPlayerOptions().active_section = (String)dropdown_section.getSelectedItem();
			}
		});
		style_mode_visible.add(dropdown_section);
		getContentPane().add(dropdown_section);

		//Add the musical information display.
		InfoBox information_system = new InfoBox(controller);
		information_system.setBounds(419, 463, 500, 200);
		information_system.setFocusable(false);
		this.getContentPane().add(information_system);

		controller.getMidiManager().getPlayerOptions().info_display = information_system;
		song_viewport.setInfoBox(information_system);

		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent arg0) {
				song_viewport.resize(song_pane_x, song_pane_y, new Dimension(self.getContentPane().getWidth() - song_pane_to_edge_x, self.getContentPane().getHeight() - song_pane_to_edge_y));
				information_system.setBounds(getContentPane().getWidth() - information_system.getWidth() - info_to_edge_x,
											getContentPane().getHeight() - information_system.getHeight() - info_to_edge_y,
											information_system.getWidth(),
											information_system.getHeight());
			}
		});

		this.pack();
		this.setVisible(true);

		setDisplayMode(DisplayMode.DISPLAY_MODE_SONG);
		
		song_viewport.setSize(new Dimension(self.getContentPane().getWidth() - song_pane_to_edge_x, self.getContentPane().getHeight() - song_pane_to_edge_y));
		song_viewport.resetScrollBars();

		this.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_DELETE) {
					song_viewport.deleteSelectedSprites();
					song_viewport.refresh();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
		});
		
		button_play.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(controller.getMidiManager().getPlayerOptions().play)
					return;

				if(display_mode == DisplayMode.DISPLAY_MODE_SONG)
					controller.getMidiManager().playSong(controller.getLoadedSong());
				else if(display_mode == DisplayMode.DISPLAY_MODE_MIDI)
					controller.getMidiManager().playSequence(song_viewport.getActiveSequence());
				else if(display_mode == DisplayMode.DISPLAY_MODE_STYLE) {
					Style style = new Style();
					style.getFromSequence(song_viewport.getActiveSequence());
					style.long_name = controller.getActiveStyle().long_name;
					style.setCasm(controller.getActiveStyle().getCasm());

					controller.getMidiManager().playStyle(style);
				}

				controller.getMidiManager().getPlayerOptions().current_tick = controller.getMidiManager().getPlayerOptions().start_tick;
			}
		});

		button_stop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.getMidiManager().getPlayerOptions().play = false;
			}
		});

		button_record.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(controller.getMidiManager().getPlayerOptions().play)
					return;

				controller.getMidiManager().recordStart();
			}
		});

		button_prev.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(controller.getMidiManager().getPlayerOptions().play)
					return;

				FWSSequence seq = controller.getActiveSequence();
				if(seq == null)
					return;

				long[] m = seq.getMeasureTicks();

				if(m.length <= 0)
					return;

				MIDIPlayerOptions options = controller.getMidiManager().getPlayerOptions();
				final long current_tick = options.start_tick;
				long new_tick = m[m.length-1];

				for(int i=0;i<m.length;i+=1) {
					if(m[i] >= current_tick && i > 0) {
						new_tick = m[i-1];
						break;
					}
				}

				options.start_tick = new_tick;
			}
		});

		button_next.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(controller.getMidiManager().getPlayerOptions().play)
					return;

				FWSSequence seq = controller.getActiveSequence();
				if(seq == null)
					return;

				long[] m = seq.getMeasureTicks();

				if(m.length <= 0)
					return;

				MIDIPlayerOptions options = controller.getMidiManager().getPlayerOptions();
				final long current_tick = options.start_tick;
				long new_tick = m[m.length-1];

				for(int i=0;i<m.length;i+=1) {
					if(m[i] > current_tick) {
						new_tick = m[i];
						break;
					}
				}

				options.start_tick = new_tick;
			}
		});
	}

	/** Populate the voice list menu from the controller. */
	private void populateVoiceList() {
		Component[] existing_options = voice_menu.getMenuComponents();
		for(Component option : existing_options) {
			if(option != gm_option)
				voice_menu.remove(option);
		}

		ButtonGroup voice_button_group = new ButtonGroup();
		voice_button_group.add(gm_option);

		//Add the voice list from the controller.
		boolean voice_list_found = false;
		String[] families = controller.getInstrumentProfileList();
		for(String family : families) {
			InstrumentProfile profile = controller.getInstrumentProfile(family);
			if(profile == null)
				continue;

			String[] instruments = profile.getInstrumentNames();
			if(instruments == null)
				continue;

			JMenu profile_menu = new JMenu(family);
			voice_menu.add(profile_menu);

			for(String instrument : instruments) {
				if(instrument.isBlank())
					continue;

				final String instr_name = instrument;
				JRadioButtonMenuItem menu_item_instrument = new JRadioButtonMenuItem(instrument);
				profile_menu.add(menu_item_instrument);
				voice_button_group.add(menu_item_instrument);

				if(family.equalsIgnoreCase(controller.getInstrumentProfileName()) && instrument.equalsIgnoreCase(controller.getInstrumentName())) {
					menu_item_instrument.setSelected(true);
					voice_list_found = true;
				}

				menu_item_instrument.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						controller.setInstrumentFamily(family);
						controller.setVoiceList(instr_name);
					}
				});
			}
		}

		if(!voice_list_found)
			gm_option.setSelected(true);
	}

	/** Get the main controller. */
	public FWSEditor getController() {
		return this.controller;
	}

	/** Set the song title to be displayed at the top of the window. */
	public void setSongTitle(String title) {
		if(!title.isEmpty())
			this.setTitle("Favorites: with Style! - " + title);
		else
			this.setTitle("Favorites: with Style! - Untitled Song");
	}

	/** Get the song viewport. */
	public SongViewPort getViewPort() {
		return this.song_viewport;
	}

	/** Get the active sequence in the viewport. */
	public FWSSequence getSequence() {
		return this.song_viewport.getActiveSequence();
	}

	/** Set the display mode and adjust components as needed. */
	public void setDisplayMode(DisplayMode display_mode) {
		final DisplayMode last_display_mode = this.display_mode;

		if(last_display_mode == DisplayMode.DISPLAY_MODE_SONG) {
			for(int i=0;i<song_mode_locked.size();i+=1)
				song_mode_locked.get(i).setEnabled(false);
			for(int i=0;i<song_mode_visible.size();i+=1)
				song_mode_visible.get(i).setVisible(false);
		} else if(last_display_mode == DisplayMode.DISPLAY_MODE_STYLE) {
			for(int i=0;i<style_mode_locked.size();i+=1)
				style_mode_locked.get(i).setEnabled(false);
			for(int i=0;i<style_mode_visible.size();i+=1)
				style_mode_visible.get(i).setVisible(false);
		} else if(last_display_mode == DisplayMode.DISPLAY_MODE_MIDI) {
			for(int i=0;i<midi_mode_locked.size();i+=1)
				midi_mode_locked.get(i).setEnabled(false);
			for(int i=0;i<midi_mode_visible.size();i+=1)
				midi_mode_visible.get(i).setVisible(false);
		}

		this.display_mode = display_mode;

		if(display_mode == DisplayMode.DISPLAY_MODE_SONG) {
			view_selector_listen = false;
			style_song_view_selector.setSelected(true);
			view_selector_listen = true;

			for(int i=0;i<song_mode_locked.size();i+=1)
				song_mode_locked.get(i).setEnabled(true);
			for(int i=0;i<song_mode_visible.size();i+=1)
				song_mode_visible.get(i).setVisible(true);
		} else if(display_mode == DisplayMode.DISPLAY_MODE_STYLE) {
			view_selector_listen = false;
			style_song_view_selector.setSelected(true);
			view_selector_listen = true;

			for(int i=0;i<style_mode_locked.size();i+=1)
				style_mode_locked.get(i).setEnabled(true);
			for(int i=0;i<style_mode_visible.size();i+=1)
				style_mode_visible.get(i).setVisible(true);
		} else if(display_mode == DisplayMode.DISPLAY_MODE_MIDI) {
			view_selector_listen = false;
			midi_scratch_view_selector.setSelected(true);
			view_selector_listen = true;

			for(int i=0;i<midi_mode_locked.size();i+=1)
				midi_mode_locked.get(i).setEnabled(true);
			for(int i=0;i<midi_mode_visible.size();i+=1)
				midi_mode_visible.get(i).setVisible(true);
		}
	}

	/** Get the display mode. */
	public DisplayMode getDisplayMode() {
		return this.display_mode;
	}

	/** Refresh the song viewport. */
	public void refreshViewport() {
		song_viewport.fill(controller.getActiveSequence(), true);
		song_viewport.refresh();
		song_viewport.resetScrollBars();
	}

	/** Refresh the style dropdown. */
	public void refreshStyleDropdown(Style style) {
		dropdown_section.removeAllItems();

		int sel_index = -1;

		String[] sections = style.getSectionNames();
		for(int i=0;i<sections.length;i+=1) {
			dropdown_section.addItem(sections[i]);
			if(sel_index < 0 && !sections[i].toUpperCase().contains("INTRO"))
				sel_index = i;
		}

		if(sel_index < 0 && sections.length > 0)
			sel_index = 0;

		dropdown_section.setSelectedIndex(sel_index);
	}

	/** Add a popup menu. */
	private static void addPopup(Component component, final JPopupMenu popup) {
	component.addMouseListener(new MouseAdapter() {
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				this.showMenu(e);
			}

		}

		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				this.showMenu(e);
			}

		}

		private void showMenu(MouseEvent e) {
			popup.show(e.getComponent(), e.getX(), e.getY());
		}
	});
}
}
