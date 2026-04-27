package settings_dialogs;

import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;

import style.Casm;
import style.Casm.CasmPart;
import voices.Voice;
import style.Style;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionListener;

import controllers.FWSEditor;
import fwsevents.FWSEvent;
import fwsevents.FWSNoteEvent;
import fwsevents.FWSVoiceEvent;
import main_window.FWSEditorMainWindow;

import javax.swing.event.ListSelectionEvent;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.JCheckBox;
import javax.swing.ListSelectionModel;

public class CasmEditor extends JDialog {

	private static final long serialVersionUID = 2877849995860336455L;

	private FWSEditor controller;

	private Style affected_style;
	private Casm affected_casm;
	
	private int cseg_count = 1;
	private ArrayList<boolean[]> cseg_sections;
	private ArrayList<Integer> cseg_indices;
	
	private JButton btnNotePlay;
	private JButton btnChordPlay;
	private JButton btnSavePart;
	
	private boolean[] note_play = new boolean[12];
	private boolean[] chord_play = new boolean[38];
	
	private String part_name = "";
	private JList<String> casmPartList;
	
	private int selected_part = -1;
	
	private JDialog casm_editor_frame;
	private JButton btnDeletePart;
	
	public CasmEditor(FWSEditor controller, FWSEditorMainWindow parent, Style style) {
		super(parent, true);
		
		this.affected_style = style;
		this.controller = controller;
		
		this.affected_casm = affected_style.getCasm();
		
		this.casm_editor_frame = this;
		
		this.setTitle("CASM Editor");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(602,530));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);
		
		int i, j;
		
		int highest_cseg = 0;
		for(i=0;i<affected_casm.sections.size();i+=1) {
			if(affected_casm.sections.get(i).CSEG > highest_cseg)
				highest_cseg = affected_casm.sections.get(i).CSEG;
		}
		for(i=0;i<affected_casm.parts.size();i+=1) {
			if(affected_casm.parts.get(i).CSEG > highest_cseg)
				highest_cseg = affected_casm.parts.get(i).CSEG;
		}
		this.cseg_count = highest_cseg + 1;
		
		cseg_sections = new ArrayList<boolean[]>(0);
		
		for(i=0;i<cseg_count;i+=1) {
			boolean[] active_sections = new boolean[affected_casm.sections.size()];
			
			for(j=0;j<active_sections.length;j+=1) {
				if(affected_casm.sections.get(j).CSEG == i)
					active_sections[j] = true;
				else
					active_sections[j] = false;
			}
			
			cseg_sections.add(Arrays.copyOf(active_sections, active_sections.length));
		}
		
		casmPartList = new JList<String>();
		casmPartList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		casmPartList.setToolTipText("List of CASM parts in the selected CSEG.");
		casmPartList.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		casmPartList.setBounds(12, 51, 175, 226);
		//getContentPane().add(casmPartList);
		
		JScrollPane listScrollPane = new JScrollPane();
		listScrollPane.setBounds(casmPartList.getBounds());
		listScrollPane.setViewportView(casmPartList);
		casmPartList.setLayoutOrientation(JList.VERTICAL);
		getContentPane().add(listScrollPane);
		
		JLabel lblCseg = new JLabel("CSEG");
		lblCseg.setBounds(32, 340, 36, 15);
		getContentPane().add(lblCseg);
		
		JSpinner spinnerCSEG = new JSpinner();
		spinnerCSEG.setToolTipText("Select the CSEG, or collection of CASM parts for a specific group of style sections.");
		spinnerCSEG.setBounds(75, 334, 72, 35);
		spinnerCSEG.setModel(new SpinnerNumberModel(Integer.valueOf(0),
				Integer.valueOf(0),
				Integer.valueOf(cseg_count-1),
				Integer.valueOf(1)));
		getContentPane().add(spinnerCSEG);
		
		refreshList(casmPartList, (int)spinnerCSEG.getValue());
		
		JLabel lblSourceChannel = new JLabel("Source Channel");
		lblSourceChannel.setBounds(205, 12, 375, 27);
		getContentPane().add(lblSourceChannel);
		
		JLabel lblName = new JLabel("Name");
		lblName.setHorizontalAlignment(SwingConstants.TRAILING);
		lblName.setBounds(205, 73, 47, 35);
		getContentPane().add(lblName);
		
		JTextField textFieldName = new JTextField();
		textFieldName.setToolTipText("Set the name of the CASM part.");
		textFieldName.setBounds(270, 73, 85, 35);
		getContentPane().add(textFieldName);
		textFieldName.setColumns(8);
		
		JCheckBox chckbxAutostart = new JCheckBox("Autostart");
		chckbxAutostart.setOpaque(false);
		chckbxAutostart.setToolTipText("When checked, the CASM part will start automatically as soon as the style starts playing. Use for percussion tracks.");
		chckbxAutostart.setBounds(378, 84, 93, 35);
		getContentPane().add(chckbxAutostart);
		
		JCheckBox chckbxEditable = new JCheckBox("Editable");
		chckbxEditable.setOpaque(false);
		chckbxEditable.setToolTipText("When checked, the CASM part will be able to be edited on a higher-end instrument.");
		chckbxEditable.setBounds(475, 84, 89, 35);
		getContentPane().add(chckbxEditable);
		
		JLabel lblDestinationChannel = new JLabel("Destination Channel");
		lblDestinationChannel.setHorizontalAlignment(SwingConstants.TRAILING);
		lblDestinationChannel.setBounds(205, 51, 145, 23);
		getContentPane().add(lblDestinationChannel);
		
		JComboBox<String> comboBoxDestination = new JComboBox<String>();
		comboBoxDestination.setToolTipText("Set the channel on which the selected CASM part will record its events during play.");
		comboBoxDestination.setModel(new DefaultComboBoxModel<String>(new String[] {"Ch. 9: Sub-Rhythm", "Ch. 10: Rhythm", "Ch. 11: Bass", "Ch. 12: Chord 1", "Ch. 13: Chord 2", "Ch. 14: Pad", "Ch. 15: Phrase 1", "Ch. 16: Phrase 2"}));
		comboBoxDestination.setBounds(368, 46, 212, 35);
		comboBoxDestination.setSelectedIndex(-1);
		getContentPane().add(comboBoxDestination);
		
		JLabel lblSourceChord = new JLabel("Source Chord");
		lblSourceChord.setHorizontalAlignment(SwingConstants.TRAILING);
		lblSourceChord.setBounds(205, 121, 105, 30);
		getContentPane().add(lblSourceChord);
		
		JComboBox<String> comboBoxRoot = new JComboBox<String>();
		comboBoxRoot.setToolTipText("Set the chord root on which the CASM part was recorded (C is recommended).");
		comboBoxRoot.setModel(new DefaultComboBoxModel<String>(new String[] {"C", "C♯/D♭", "D", "D♯/E♭", "E", "F", "F♯/G♭", "G", "G♯/A♭", "A", "A♯/B♭", "B"}));
		comboBoxRoot.setBounds(328, 116, 72, 35);
		comboBoxRoot.setSelectedIndex(-1);
		getContentPane().add(comboBoxRoot);
		
		JComboBox<String> comboBoxChord = new JComboBox<String>();
		comboBoxChord.setToolTipText("Set the chord on which the CASM part was recorded (Maj7 is recommended).");
		comboBoxChord.setModel(new DefaultComboBoxModel<String>(new String[] {"(major)", "6", "M7", "M7(♯11)", "(9)", "M7(9)", "6(9)", "aug", "m", "m6", "m7", "m7♭5", "m(9)", "m7(9)", "m7(11)", "mM7", "mM7(9)", "dim", "dim7", "7", "7sus4", "7♭5", "7(9)", "7(♯11)", "7(13)", "7(♭9)", "7(♭13)", "7(♯9)", "M7aug", "7aug", "1+8", "1+5", "sus4", "sus2", "(cancel)", "M7♭5", "♭5", "mM7♭5"}));
		comboBoxChord.setBounds(410, 116, 154, 35);
		comboBoxChord.setSelectedIndex(-1);
		getContentPane().add(comboBoxChord);
		
		JLabel lblNoteTranspositionRule = new JLabel("Note Transposition Rule");
		lblNoteTranspositionRule.setHorizontalAlignment(SwingConstants.TRAILING);
		lblNoteTranspositionRule.setBounds(205, 161, 171, 29);
		getContentPane().add(lblNoteTranspositionRule);
		
		JComboBox<String> comboBoxNTR = new JComboBox<String>();
		comboBoxNTR.setToolTipText("<html><p>Set the note transposition rule:<br>Root Transposition: All notes are transposed the same amount according to the chord root. Use for bass and melody tracks.<br>Root Fixed: Each note is transposed to the closest note of the chord root; e.g. if the reference notes are B3, E4, and G4 in a C M7 chord, they will be transposed to C4, E4, G4 for a C major chord, and C4, F4, A4 for an F major chord. Use for percussion, chord, and pad tracks.</p></html>");
		comboBoxNTR.setModel(new DefaultComboBoxModel<String>(new String[] {"Root Transposition", "Root Fixed"}));
		comboBoxNTR.setBounds(394, 155, 154, 35);
		comboBoxNTR.setSelectedIndex(-1);
		getContentPane().add(comboBoxNTR);
		
		JLabel lblNoteTranspositionTable = new JLabel("Note Transposition Table");
		lblNoteTranspositionTable.setHorizontalAlignment(SwingConstants.TRAILING);
		lblNoteTranspositionTable.setBounds(197, 197, 179, 33);
		getContentPane().add(lblNoteTranspositionTable);
		
		JComboBox<String> comboBoxNTT = new JComboBox<String>();
		comboBoxNTT.setToolTipText("<html><p>Set the note transposition table:<br><br>Bypass: The part is not transposed depending on chord type. Use for percussion (root fixed NTR) and intro/ending tracks (root transposition NTR)<br>Melody: The part is transposed based on main chord type. If the source chord is C M7, these parts may only contain the notes C, D, E, G, A, and B. Use for phrase with root transposition NTR. <br>Chord: The part is transposed based on chord type, with specific rules. If the source chord is C M7, these parts may only contain the notes C, E, G, and B. Use for chord and pad parts.<br>Bass: Transposed like melody but based on bass chord type. Use for bass with root transposition NTR.</p></html>");
		comboBoxNTT.setModel(new DefaultComboBoxModel<String>(new String[] {"Bypass", "Melody", "Chord", "Bass", "Melodic Minor", "Harmonic Minor"}));
		comboBoxNTT.setSelectedIndex(-1);
		comboBoxNTT.setBounds(394, 194, 154, 35);
		getContentPane().add(comboBoxNTT);
		
		JLabel lblRetriggerRule = new JLabel("Retrigger Rule");
		lblRetriggerRule.setHorizontalAlignment(SwingConstants.TRAILING);
		lblRetriggerRule.setBounds(271, 235, 105, 33);
		getContentPane().add(lblRetriggerRule);
		
		JComboBox<String> comboBoxRTR = new JComboBox<String>();
		comboBoxRTR.setToolTipText("Set the rule for notes held through chord changes.");
		comboBoxRTR.setModel(new DefaultComboBoxModel<String>(new String[] {"Stop", "Pitch Shift", "Pitch Shift to Root", "Retrigger", "Retrigger to Root", "Note Generator"}));
		comboBoxRTR.setSelectedIndex(-1);
		comboBoxRTR.setBounds(394, 233, 154, 35);
		getContentPane().add(comboBoxRTR);
		
		JLabel lblHighKey = new JLabel("High Key");
		lblHighKey.setHorizontalAlignment(SwingConstants.TRAILING);
		lblHighKey.setBounds(205, 268, 72, 35);
		getContentPane().add(lblHighKey);
		
		JComboBox<String> comboBoxHighKey = new JComboBox<String>();
		comboBoxHighKey.setToolTipText("Set the highest chord root for this part; if a higher chord root is played, it will be transposed down an octave. Only effective with note transposition NTR.");
		comboBoxHighKey.setModel(new DefaultComboBoxModel<String>(new String[] {"C", "C♯/D♭", "D", "D♯/E♭", "E", "F", "F♯/G♭", "G", "G♯/A♭", "A", "A♯/B♭", "B"}));
		comboBoxHighKey.setSelectedIndex(-1);
		comboBoxHighKey.setBounds(283, 268, 72, 35);
		getContentPane().add(comboBoxHighKey);
		
		JLabel lblLimits = new JLabel("Limits");
		lblLimits.setHorizontalAlignment(SwingConstants.TRAILING);
		lblLimits.setBounds(368, 280, 47, 15);
		getContentPane().add(lblLimits);
		
		JSpinner spinnerLowLimit = new JSpinner();
		spinnerLowLimit.setToolTipText("Set the lowest note that can be played in this CASM part.");
		spinnerLowLimit.setBounds(424, 272, 54, 35);
		spinnerLowLimit.setModel(new SpinnerNumberModel(Byte.valueOf((byte) 0),
				Byte.valueOf((byte) 0),
				Byte.valueOf((byte)127),
				Byte.valueOf((byte) 1)));
		getContentPane().add(spinnerLowLimit);
		
		JSpinner spinnerHighLimit = new JSpinner();
		spinnerHighLimit.setToolTipText("Set the highest note that can be played in this CASM part.");
		spinnerHighLimit.setBounds(490, 272, 54, 35);
		spinnerHighLimit.setModel(new SpinnerNumberModel(Byte.valueOf((byte) 127),
				Byte.valueOf((byte) 0),
				Byte.valueOf((byte)127),
				Byte.valueOf((byte) 1)));
		getContentPane().add(spinnerHighLimit);
		
		JLabel lblLowLimit = new JLabel("");
		lblLowLimit.setHorizontalAlignment(SwingConstants.CENTER);
		lblLowLimit.setBounds(424, 307, 54, 15);
		getContentPane().add(lblLowLimit);
		
		JLabel lblHighLimit = new JLabel("");
		lblHighLimit.setHorizontalAlignment(SwingConstants.CENTER);
		lblHighLimit.setBounds(490, 307, 54, 15);
		getContentPane().add(lblHighLimit);
		
		spinnerLowLimit.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if((byte)spinnerLowLimit.getValue() > (byte)((byte)spinnerHighLimit.getValue() - 11))
					spinnerLowLimit.setValue(Byte.valueOf((byte)((byte)spinnerHighLimit.getValue() - 11)));

				final byte note_value = ((Byte)spinnerLowLimit.getValue()).byteValue();
				
				lblLowLimit.setText(FWSNoteEvent.note_map[note_value%12] + (note_value/12 - 1));
			}
		});
		
		spinnerHighLimit.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if((byte)spinnerHighLimit.getValue() < (byte)((byte)spinnerLowLimit.getValue() + 11))
					spinnerHighLimit.setValue(Byte.valueOf((byte)((byte)spinnerLowLimit.getValue() + 11)));

				final byte note_value = ((Byte)spinnerHighLimit.getValue()).byteValue();
				
				lblHighLimit.setText(FWSNoteEvent.note_map[note_value%12] + (note_value/12 - 1));
			}
		});
		
		textFieldName.getDocument().addDocumentListener(new DocumentListener() {

			//TODO: This throws illegal state exceptions. Find out why?
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				//if(textFieldName.getText().length() > 8)
					//textFieldName.setText(textFieldName.getText().substring(0, 8));
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				//if(textFieldName.getText().length() > 8)
					//textFieldName.setText(textFieldName.getText().substring(0, 8));
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				//if(textFieldName.getText().length() > 8)
					//textFieldName.setText(textFieldName.getText().substring(0, 8));
			}
		});
		
		spinnerCSEG.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				refreshList(casmPartList, (int)spinnerCSEG.getValue());
				selectPart(-1, textFieldName, comboBoxDestination, comboBoxRoot, comboBoxChord, comboBoxNTR, comboBoxNTT,
						comboBoxRTR, comboBoxHighKey, spinnerLowLimit, spinnerHighLimit, chckbxAutostart, chckbxEditable, lblSourceChannel); 
			}
		});
		
		casmPartList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				if(casmPartList.getSelectedIndex() >= 0 && casmPartList.getSelectedIndex() < cseg_indices.size()) {
					selectPart(cseg_indices.get(casmPartList.getSelectedIndex()), textFieldName, comboBoxDestination, comboBoxRoot, comboBoxChord, comboBoxNTR, comboBoxNTT,
							comboBoxRTR, comboBoxHighKey, spinnerLowLimit, spinnerHighLimit, chckbxAutostart, chckbxEditable, lblSourceChannel); 
				} else {
					selectPart(-1, textFieldName, comboBoxDestination, comboBoxRoot, comboBoxChord, comboBoxNTR, comboBoxNTT,
							comboBoxRTR, comboBoxHighKey, spinnerLowLimit, spinnerHighLimit, chckbxAutostart, chckbxEditable, lblSourceChannel); 
				}
			}
		});
		
		final byte low_note_value = ((Byte)spinnerLowLimit.getValue()).byteValue(), high_note_value = ((Byte)spinnerHighLimit.getValue()).byteValue();

		lblLowLimit.setText(FWSNoteEvent.note_map[low_note_value%12] + (low_note_value/12 - 1));
		lblHighLimit.setText(FWSNoteEvent.note_map[high_note_value%12] + (high_note_value/12 - 1));
		
		btnNotePlay = new JButton("Note Play");
		btnNotePlay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				casmNoteWindow();
			}
		});
		btnNotePlay.setToolTipText("Select the chord root notes for which this CASM part will play.");
		btnNotePlay.setBounds(197, 334, 117, 35);
		getContentPane().add(btnNotePlay);
		
		btnChordPlay = new JButton("Chord Play");
		btnChordPlay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				casmChordWindow();
			}
		});
		btnChordPlay.setToolTipText("Select the chord types for which this CASM part will play.");
		btnChordPlay.setBounds(328, 334, 117, 35);
		getContentPane().add(btnChordPlay);
		
		btnSavePart = new JButton("Save Part");
		btnSavePart.setToolTipText("Saves the CASM part being edited.");
		btnSavePart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				savePart(cseg_indices.get(casmPartList.getSelectedIndex()), textFieldName, comboBoxDestination, comboBoxRoot, comboBoxChord, comboBoxNTR, comboBoxNTT,
						comboBoxRTR, comboBoxHighKey, spinnerLowLimit, spinnerHighLimit, chckbxAutostart, chckbxEditable);
				selectPart(-1, textFieldName, comboBoxDestination, comboBoxRoot, comboBoxChord, comboBoxNTR, comboBoxNTT,
						comboBoxRTR, comboBoxHighKey, spinnerLowLimit, spinnerHighLimit, chckbxAutostart, chckbxEditable, lblSourceChannel); 
			}
		});
		btnSavePart.setBounds(457, 334, 117, 35);
		getContentPane().add(btnSavePart);
		
		btnDeletePart = new JButton("Delete Part");
		btnDeletePart.setToolTipText("Deletes the CASM part being edited.");
		btnDeletePart.setBounds(457, 381, 117, 35);
		getContentPane().add(btnDeletePart);
		
		selectPart(-1, textFieldName, comboBoxDestination, comboBoxRoot, comboBoxChord, comboBoxNTR, comboBoxNTT,
				comboBoxRTR, comboBoxHighKey, spinnerLowLimit, spinnerHighLimit, chckbxAutostart, chckbxEditable, lblSourceChannel); 
		
		JButton btnNewPart = new JButton("New Part");
		btnNewPart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int CSEG = (int)spinnerCSEG.getValue();
				newPartWindow(CSEG);
			}
		});
		btnNewPart.setToolTipText("Creates a new CASM part in the current CSEG.");
		btnNewPart.setBounds(32, 289, 117, 35);
		getContentPane().add(btnNewPart);
		
		JButton btnAddCseg = new JButton("Add CSEG");
		btnAddCseg.setToolTipText("Adds a new CSEG, if unused sections exist.");
		btnAddCseg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(style.getCasm().sections.size() < style.getSectionNames().length) {
					int answer = JOptionPane.showConfirmDialog(casm_editor_frame, "Copy the current CSEG data into the new CSEG (e.g. if only one part is different)?", "New CSEG", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
					if(answer != JOptionPane.CANCEL_OPTION) {
						cseg_count += 1;
						if(answer == JOptionPane.YES_OPTION) {
							int current_cseg = (int)spinnerCSEG.getValue(), number_of_segs = style.getCasm().parts.size();
							for(int i=0;i<number_of_segs;i+=1) {
								if(style.getCasm().parts.get(i).CSEG == current_cseg) {
									CasmPart new_part = new CasmPart(style.getCasm().parts.get(i));
									new_part.CSEG = cseg_count - 1;
									style.getCasm().parts.add(new_part);
								}
							}
						}
						spinnerCSEG.setModel(new SpinnerNumberModel(Integer.valueOf(cseg_count-1),
								Integer.valueOf(0),
								Integer.valueOf(cseg_count-1),
								Integer.valueOf(1)));
						refreshList((int)spinnerCSEG.getValue());
					}
				}
			}
		});
		btnAddCseg.setBounds(30, 392, 117, 35);
		getContentPane().add(btnAddCseg);
		
		JButton btnCsegSections = new JButton("Sections");
		btnCsegSections.setToolTipText("Change the sections of the selected CSEG.");
		btnCsegSections.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new CasmSections((CasmEditor)casm_editor_frame, affected_style, (int)spinnerCSEG.getValue());
			}
		});
		btnCsegSections.setBounds(30, 429, 117, 35);
		getContentPane().add(btnCsegSections);
		
		JButton btnClose = new JButton("Close");
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				closeDialogBox();
			}
		});
		btnClose.setToolTipText("Closes the CASM editor window.");
		btnClose.setBounds(463, 428, 117, 36);
		getContentPane().add(btnClose);
		
		btnDeletePart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(selected_part >= 0 && selected_part < affected_style.getCasm().parts.size()) {
					int answer = JOptionPane.showConfirmDialog(casm_editor_frame, "Are you sure?", "Delete Part", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if(answer==JOptionPane.YES_OPTION) {
						deletePart(selected_part);
						selectPart(-1, textFieldName, comboBoxDestination, comboBoxRoot, comboBoxChord, comboBoxNTR, comboBoxNTT,
								comboBoxRTR, comboBoxHighKey, spinnerLowLimit, spinnerHighLimit, chckbxAutostart, chckbxEditable, lblSourceChannel); 
						refreshList((int)spinnerCSEG.getValue());
					}
				}
			}
		});
		
		this.setVisible(true);
	}
	
	/** Close the window. */
	private void closeDialogBox() {
		this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}
	
	/** Refresh the part list. */
	private void refreshList(int cseg) {
		this.refreshList(casmPartList, cseg);
	}
	
	/** Refresh the part list. */
	private void refreshList(JList<String> list, int cseg) {
		DefaultListModel<String> model = new DefaultListModel<String>();
		
		this.cseg_indices = new ArrayList<Integer>(0);
		
		for(int i=0;i<this.affected_casm.parts.size();i+=1) {
			if(this.affected_casm.parts.get(i).CSEG == cseg) {
				model.addElement(this.affected_casm.parts.get(i).name + "Source: Ch. " + (this.affected_casm.parts.get(i).source_channel+1));
				cseg_indices.add(Integer.valueOf(i));
			}
		}
		
		list.setModel(model);
	}
	
	/** Refresh everything when a new CASM part is selected.**/
	private void selectPart(int part_index, JTextField name, JComboBox<String> destination, JComboBox<String> source_root, JComboBox<String> source_chord,
							JComboBox<String> NTR, JComboBox<String> NTT, JComboBox<String> RTR, JComboBox<String> high_key, JSpinner low_limit, JSpinner high_limit,
							JCheckBox autostart, JCheckBox editable, JLabel source) {
		this.selected_part = part_index;
		
		if(part_index < 0 || part_index >= this.affected_casm.parts.size()) {
			part_name = "";
			name.setText("");
			name.setEnabled(false);
			destination.setSelectedIndex(-1);
			destination.setEnabled(false);
			source_root.setSelectedIndex(-1);
			source_root.setEnabled(false);
			source_chord.setSelectedIndex(-1);
			source_chord.setEnabled(false);
			NTR.setSelectedIndex(-1);
			NTR.setEnabled(false);
			NTT.setSelectedIndex(-1);
			NTT.setEnabled(false);
			RTR.setSelectedIndex(-1);
			RTR.setEnabled(false);
			high_key.setSelectedIndex(-1);
			high_key.setEnabled(false);
			low_limit.setValue(Byte.valueOf((byte)0));
			low_limit.setEnabled(false);
			high_limit.setValue(Byte.valueOf((byte)127));
			high_limit.setEnabled(false);
			autostart.setSelected(false);
			autostart.setEnabled(false);
			editable.setSelected(false);
			editable.setEnabled(false);
			
			btnNotePlay.setEnabled(false);
			btnChordPlay.setEnabled(false);
			btnSavePart.setEnabled(false);
			btnDeletePart.setEnabled(false);
			
			source.setText("Source Channel:");
			return;
		}
		
		CasmPart part = this.affected_casm.parts.get(part_index);
		
		part_name = part.name;
		
		String voice = "";
		try {
			ArrayList<FWSEvent> start_events = affected_style.getStartEvents();
			for(int i=0;i<start_events.size();i+=1) {
				if(start_events.get(i) instanceof FWSVoiceEvent) {
					FWSVoiceEvent voice_event = (FWSVoiceEvent)start_events.get(i);
					if(voice_event.channel != part.source_channel)
						continue;

					voice = Voice.matchVoice(controller.getVoiceList(), voice_event.voice, voice_event.voice_lsb, voice_event.voice_msb).name;
				}
			}
			
		} catch(NullPointerException|IndexOutOfBoundsException e) {
			voice = "Unknown Voice";
		}
		
		source.setText("Source Channel: " + (part.source_channel+1) + " (" + voice + ")");
		
		name.setEnabled(true);
		name.setText(part.name);
		
		destination.setEnabled(true);
		if(part.channel >=8 && part.channel < 16)
			destination.setSelectedIndex(part.channel - 8);
		else
			destination.setSelectedIndex(-1);
		
		source_root.setEnabled(true);
		source_root.setSelectedIndex(part.sourcechord);
		
		source_chord.setEnabled(true);
		source_chord.setSelectedIndex(part.sourcechord_type);
		
		NTR.setEnabled(true);
		if(part.NTR)
			NTR.setSelectedIndex(1);
		else
			NTR.setSelectedIndex(0);
		
		NTT.setEnabled(true);
		if(part.NTT > 5)
			NTT.setSelectedIndex(5);
		else
			NTT.setSelectedIndex(part.NTT);
		
		RTR.setEnabled(true);
		RTR.setSelectedIndex(part.RTR);
		
		high_key.setEnabled(true);
		high_key.setSelectedIndex(part.high_key);
		
		low_limit.setEnabled(true);
		low_limit.setValue(Byte.valueOf(part.low_limit));
		
		high_limit.setEnabled(true);
		high_limit.setValue(Byte.valueOf(part.high_limit));
		
		autostart.setEnabled(true);
		autostart.setSelected(part.autostart);
		
		editable.setEnabled(true);
		editable.setSelected(!part.editable);
		
		btnNotePlay.setEnabled(true);
		btnChordPlay.setEnabled(true);
		btnSavePart.setEnabled(true);
		btnDeletePart.setEnabled(true);
		
		this.note_play = Arrays.copyOf(part.noteplay, part.noteplay.length);
		this.chord_play = Arrays.copyOf(part.chordplay, part.chordplay.length);
	}
	
	private void savePart(int part_index, JTextField name, JComboBox<String> destination, JComboBox<String> source_root, JComboBox<String> source_chord,
			JComboBox<String> NTR, JComboBox<String> NTT, JComboBox<String> RTR, JComboBox<String> high_key, JSpinner low_limit, JSpinner high_limit,
			JCheckBox autostart, JCheckBox editable) {
		
		CasmPart part = this.affected_casm.parts.get(part_index);
		
		String name_text = name.getText();
		if(name_text.length() != 8) {
			if(name_text.length() > 8)
				name_text = name_text.substring(0,8);
			else {
				while(name_text.length() < 8)
					name_text += " ";
			}
		}
		part.name = name_text;
		
		if(destination.getSelectedIndex() >= 0)
			part.channel = (byte) (destination.getSelectedIndex() + 8);
		
		if(source_root.getSelectedIndex() >= 0)
			part.sourcechord = (byte) source_root.getSelectedIndex();
		
		if(source_chord.getSelectedIndex() >= 0)
			part.sourcechord_type = (byte) source_chord.getSelectedIndex();
		
		if(NTR.getSelectedIndex() >= 0)
			part.NTR = NTR.getSelectedIndex() != 0;
		
		if(NTT.getSelectedIndex() >= 0)
			part.NTT = (byte) NTT.getSelectedIndex();
		
		if(RTR.getSelectedIndex() >= 0)
			part.RTR = (byte) RTR.getSelectedIndex();
		
		if(high_key.getSelectedIndex() >= 0)
			part.high_key = (byte) high_key.getSelectedIndex();
		
		part.low_limit = (byte)low_limit.getValue();
		part.high_limit = (byte)high_limit.getValue();
		
		part.autostart = autostart.isSelected();
		part.editable = !editable.isSelected();
		
		part.noteplay = Arrays.copyOf(this.note_play, this.note_play.length);
		part.chordplay = Arrays.copyOf(this.chord_play, this.chord_play.length);
		
		this.affected_casm.parts.set(part_index, part);
	}	
	
	/** Create a note window. */
	private void casmNoteWindow() {
		new CasmNotes(this.part_name, this.note_play, this);
	}
	
	/** Create a chord window. */
	private void casmChordWindow() {
		new CasmChords(this.part_name, this.chord_play, this);
	}
	
	/** Create a new part window. */
	private void newPartWindow(int cseg) {
		new CasmNewPart(this, controller, cseg, this.affected_style);
	}
	
	/** Create a new part. */
	public void newPart(int source, int destination, int CSEG) {
		CasmPart part = new CasmPart();
		
		part.source_channel = (byte)source;
		part.channel = (byte)destination;
		part.CSEG = CSEG;
		
		part.sourcechord = 0;
		part.sourcechord_type = 2;
		
		part.editable = false;
		
		part.noteplay = new boolean[12];
		for(int i=0;i<part.noteplay.length;i+=1)
			part.noteplay[i] = true;
		
		part.chordplay = new boolean[38];
		for(int i=0;i<part.chordplay.length;i+=1)
			part.chordplay[i] = true;
		
		part.low_limit = 0;
		part.high_limit = 127;
		part.high_key = 6;
		
		if(destination == 8) {//Sub-rhythm
			part.name = "SubRhytm";
			part.autostart = true;
			part.NTR = true;
			part.NTT = (byte)0;
			
			part.RTR = (byte)1;
		} else if(destination == 9) {//Rhythm
			part.name = "Rhythm  ";
			part.autostart = true;
			part.NTR = true;
			part.NTT = (byte)0;
			
			part.RTR = (byte)1;
		} else if(destination == 10) {//Bass
			part.name = "Bass    ";
			part.autostart = false;
			part.NTR = false;
			part.NTT = (byte)3;
			
			part.RTR = (byte)2;
		} else if(destination == 11 || destination == 12) {//Chord
			part.name = "Chord   ";
			part.autostart = false;
			part.NTR = true;
			part.NTT = (byte)2;
			
			part.RTR = (byte)1;
		} else if(destination == 13) {//Pad
			part.name = "Pad     ";
			part.autostart = false;
			part.NTR = true;
			part.NTT = (byte)2;
			
			part.RTR = (byte)1;
		} else if(destination == 14 || destination == 15) {//Phrase
			part.name = "Phrase   ";
			part.autostart = false;
			part.NTR = false;
			part.NTT = (byte)1;
			
			part.RTR = (byte)3;
		} else
			return;
		
		affected_style.getCasm().parts.add(part);
		refreshList(CSEG);
	}
	
	/** Delete the part at index. */
	private void deletePart(final int index) {
		affected_style.getCasm().parts.remove(index);
	}
}
