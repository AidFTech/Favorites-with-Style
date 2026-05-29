package tools;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JDialog;

import dialogpanels.EventDialogSignature;
import fwsevents.FWSChordEvent;
import fwsevents.FWSEvent;
import fwsevents.FWSKeySignatureEvent;
import fwsevents.FWSNoteEvent;
import fwsevents.FWSSequence;
import main_window.FWSEditorMainWindow;
import style.ChordBody;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import canvas.SongViewPort;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class TransposeWindow extends JDialog {
	private static final long serialVersionUID = -1759437513833632185L;

	public TransposeWindow(FWSEditorMainWindow parent, FWSSequence sequence, final ArrayList<FWSEvent> events, final boolean key_signature) {
		super(parent, true);

		this.setTitle("Transpose");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(460, 220));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);
		
		JLabel label_key = new JLabel("Key");
		label_key.setHorizontalAlignment(SwingConstants.RIGHT);
		label_key.setBounds(12, 12, 60, 35);
		if(key_signature)
			getContentPane().add(label_key);
		
		JComboBox<String> dropdown_key = new JComboBox<>();
		dropdown_key.setToolTipText("Select the desired key to transpose to.");
		dropdown_key.setBounds(90, 12, 177, 35);
		if(key_signature)
			getContentPane().add(dropdown_key);

		EventDialogSignature signature = new EventDialogSignature();
		signature.setBounds(288, 12, signature.getWidth(), signature.getHeight());
		if(key_signature)
			this.getContentPane().add(signature);

		byte original_accidental_count = 0;
		boolean major = false;

		FWSKeySignatureEvent key_event = sequence.getKeySignatureAt(0);
		if(key_event != null) {
			signature.setAccidentalCount(key_event.accidental_count);
			original_accidental_count = key_event.accidental_count;
			fillKeyComboBox(dropdown_key, key_event.major);
			dropdown_key.setSelectedIndex(key_event.accidental_count + 7);

			major = key_event.major;
		}

		final byte set_accidental_count = original_accidental_count;
		final byte set_key = FWSKeySignatureEvent.getKey(set_accidental_count, major);
		final boolean set_major = major;

		dropdown_key.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				signature.setAccidentalCount(dropdown_key.getSelectedIndex() - 7);
			}
		});

		JRadioButton radiobutton_transpose_key = new JRadioButton("Transpose by Key");
		radiobutton_transpose_key.setToolTipText("Transpose by key signature.");
		radiobutton_transpose_key.setSelected(true);
		radiobutton_transpose_key.setBounds(12, 73, 160, 35);
		if(key_signature)
			getContentPane().add(radiobutton_transpose_key);
		
		JRadioButton radiobutton_transpose_steps = new JRadioButton("Transpose by Steps");
		radiobutton_transpose_steps.setToolTipText("Transpose by half steps.");
		radiobutton_transpose_steps.setBounds(12, 112, 160, 35);
		if(key_signature)
			getContentPane().add(radiobutton_transpose_steps);

		ButtonGroup transpose_group = new ButtonGroup();
		transpose_group.add(radiobutton_transpose_key);
		transpose_group.add(radiobutton_transpose_steps);

		JLabel label_style = new JLabel("Style events cannot be transposed by key.");
		label_style.setBounds(24, 12, 270, 35);
		if(!key_signature)
			getContentPane().add(label_style);

		JLabel label_steps = new JLabel("Steps");
		label_steps.setHorizontalAlignment(SwingConstants.RIGHT);
		label_steps.setBounds(12, 112, 160, 35);
		if(!key_signature)
			getContentPane().add(label_steps);
		
		JSpinner spinner_steps = new JSpinner();
		spinner_steps.setEnabled(!key_signature);
		spinner_steps.setToolTipText("Define the number of steps to transpose by.");
		spinner_steps.setModel(new SpinnerNumberModel(0, -12, 12, 1));
		spinner_steps.setBounds(180, 112, 60, 35);
		getContentPane().add(spinner_steps);

		radiobutton_transpose_key.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(radiobutton_transpose_key.isSelected()) {
					final boolean was_selected = dropdown_key.isEnabled();
					dropdown_key.setEnabled(true);
					signature.setVisible(true);

					spinner_steps.setEnabled(false);

					if(!was_selected)
						dropdown_key.setSelectedIndex(set_accidental_count + 7);
				}
			}
		});

		radiobutton_transpose_steps.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dropdown_key.setEnabled(false);
				signature.setVisible(false);

				spinner_steps.setEnabled(true);
				spinner_steps.setValue(0);
			}
		});

		TransposeWindow self = this;

		JButton button_cancel = new JButton("Cancel");
		button_cancel.setBounds(226, 173, 105, 35);
		button_cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_cancel);

		JButton button_apply = new JButton("Apply");
		button_apply.setBounds(343, 173, 105, 35);
		button_apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ArrayList<FWSEvent> sequence_events;
				
				if(events == null)
					sequence_events = sequence.getAllEvents();
				else
					sequence_events = events;

				int steps = 0, new_accidental_count = 0;

				if(radiobutton_transpose_key.isSelected() && key_signature) { //Transpose by key.
					new_accidental_count = dropdown_key.getSelectedIndex() - 7;
					final byte new_key = FWSKeySignatureEvent.getKey((byte)new_accidental_count, set_major);

					int key_up = 0, key_down = 0;
					for(int i=0;i<12;i+=1) {
						if((set_key + i)%12 == new_key) {
							key_up = i;
							break;
						}
					}
					for(int i=0;i<12;i+=1) {
						int test_key = set_key - i;
						while(test_key < 0)
							test_key += 12;

						if(test_key%12 == new_key) {
							key_down = i;
							break;
						}
					}

					if(Math.abs(key_up) <= Math.abs(key_down))
						steps = key_up;
					else
						steps = -key_down;
				} else {
					steps = (Integer)spinner_steps.getValue();

					int effective_key = set_key + steps;
					while(effective_key < 0)
						effective_key += 12;
					effective_key %= 12;

					new_accidental_count = FWSKeySignatureEvent.getAccidentalCount((byte)effective_key, set_major);
				}

				SongViewPort viewport = parent.getViewPort();

				for(int i=0;i<sequence_events.size();i+=1) {
					if(sequence_events.get(i) instanceof FWSNoteEvent) {
						FWSNoteEvent note = (FWSNoteEvent)sequence_events.get(i);
						note.note += steps;

						if(note.note < 0)
							note.note = 0;
						if(note.note >= 128)
							note.note = 127;

						viewport.refreshSprite(note);
					} else if(sequence_events.get(i) instanceof FWSChordEvent) {
						FWSChordEvent chord_event = (FWSChordEvent)sequence_events.get(i);
						ChordBody main_chord = chord_event.main_chord, bass_chord = chord_event.bass_chord;

						byte main_root = (byte)(main_chord.getRoot() + steps);
						while(main_root < 0)
							main_root += 12;
						main_root %= 12;

						final boolean sharp = sequence.getNoteNameAt((byte)(main_root + FWSNoteEvent.middle_c), chord_event.tick, false).contains("♯");

						chord_event.main_chord = new ChordBody(main_root, main_chord.getChord(), sharp);

						if(!bass_chord.getNoChord()) {
							byte bass_root = (byte)(bass_chord.getRoot() + steps);
							while(bass_root < 0)
								bass_root += 12;
							bass_root %= 12;

							chord_event.bass_chord = new ChordBody(bass_root, bass_chord.getChord(), sharp);
						}

						viewport.refreshSprite(chord_event);
					} else if(sequence_events.get(i) instanceof FWSKeySignatureEvent) {
						FWSKeySignatureEvent key_event = (FWSKeySignatureEvent)sequence_events.get(i);
						final byte start_key = FWSKeySignatureEvent.getKey(key_event.accidental_count, key_event.major);
						int accidental_count = FWSKeySignatureEvent.getAccidentalCount((byte)(start_key + steps), key_event.major);
						while(accidental_count < -7)
							accidental_count += 12;
						while(accidental_count > 7)
							accidental_count -= 12;

						key_event.accidental_count = (byte)accidental_count;

						viewport.refreshSprite(key_event);
					}
				}

				viewport.refreshFull();

				dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		getContentPane().add(button_apply);

		this.setVisible(true);
	}

	/** Fill the key signature combo box. */
	private void fillKeyComboBox(JComboBox<String> key_menu, final boolean major) {
		final int sel = key_menu.getSelectedIndex();
		key_menu.removeAllItems();

		if(major) {
			key_menu.addItem("C♭ major");
			key_menu.addItem("G♭ major");
			key_menu.addItem("D♭ major");
			key_menu.addItem("A♭ major");
			key_menu.addItem("E♭ major");
			key_menu.addItem("B♭ major");
			key_menu.addItem("F major");
			key_menu.addItem("C major");
			key_menu.addItem("G major");
			key_menu.addItem("D major");
			key_menu.addItem("A major");
			key_menu.addItem("E major");
			key_menu.addItem("B major");
			key_menu.addItem("F♯ major");
			key_menu.addItem("C♯ major");
		} else {
			key_menu.addItem("A♭ minor");
			key_menu.addItem("E♭ minor");
			key_menu.addItem("B♭ minor");
			key_menu.addItem("F minor");
			key_menu.addItem("C minor");
			key_menu.addItem("G minor");
			key_menu.addItem("D minor");
			key_menu.addItem("A minor");
			key_menu.addItem("E minor");
			key_menu.addItem("B minor");
			key_menu.addItem("F♯ minor");
			key_menu.addItem("C♯ minor");
			key_menu.addItem("G♯ minor");
			key_menu.addItem("D♯ minor");
			key_menu.addItem("A♯ minor");
		}
		key_menu.setSelectedIndex(sel);
	}
}
