package settings_dialogs;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import controllers.FWSEditor;
import fwsevents.FWSEvent;
import fwsevents.FWSVoiceEvent;
import style.Casm.CasmPart;
import voices.Voice;
import style.Style;

import javax.swing.JComboBox;

import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;

import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;

public class CasmNewPart extends JDialog {

	private static final long serialVersionUID = -2332942492145480999L;
	
	private ArrayList<Integer> channel_indices = new ArrayList<Integer>(0);
	
	public CasmNewPart(CasmEditor parent, FWSEditor controller, int CSEG, Style style) {
		super(parent, true);
		
		this.setTitle("New CASM Part");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setPreferredSize(new Dimension(420,213));
		this.getContentPane().setSize(getContentPane().getPreferredSize());
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);
		
		JLabel lblSourceChannel = new JLabel("Source Channel");
		lblSourceChannel.setHorizontalAlignment(SwingConstants.TRAILING);
		lblSourceChannel.setBounds(12, 49, 145, 15);
		getContentPane().add(lblSourceChannel);
		
		JComboBox<String> comboBoxSource = new JComboBox<String>();
		comboBoxSource.setToolTipText("Select the channel from which to draw the new part's events from.");
		comboBoxSource.setSelectedIndex(-1);
		comboBoxSource.setEnabled(true);
		comboBoxSource.setBounds(175, 44, 212, 27);
		comboBoxSource.setSelectedIndex(-1);
		getContentPane().add(comboBoxSource);
		
		JLabel lblDestinationChannel = new JLabel("Destination Channel");
		lblDestinationChannel.setHorizontalAlignment(SwingConstants.TRAILING);
		lblDestinationChannel.setBounds(12, 91, 145, 15);
		getContentPane().add(lblDestinationChannel);
		
		JComboBox<String> comboBoxDestination = new JComboBox<String>();
		comboBoxDestination.setToolTipText("Select the channel to which the new part will record events on.");
		comboBoxDestination.setModel(new DefaultComboBoxModel<String>(new String[] {"Ch. 9: Sub-Rhythm", "Ch. 10: Rhythm", "Ch. 11: Bass", "Ch. 12: Chord 1", "Ch. 13: Chord 2", "Ch. 14: Pad", "Ch. 15: Phrase 1", "Ch. 16: Phrase 2"}));
		comboBoxDestination.setSelectedIndex(-1);
		comboBoxDestination.setEnabled(true);
		comboBoxDestination.setBounds(175, 86, 212, 27);
		comboBoxDestination.setSelectedIndex(-1);
		getContentPane().add(comboBoxDestination);
		
		this.fillSourceBox(style, CSEG, comboBoxSource, controller.getVoiceList());
		
		JPanel panelCancelSave = new JPanel();
		panelCancelSave.setBounds(0, 135, 410, 42);
		getContentPane().add(panelCancelSave);
		panelCancelSave.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JButton buttonCancel = new JButton("Cancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				closeDialogBox();
			}
		});
		buttonCancel.setToolTipText("Closes the window with no changes to the CASM data.");
		panelCancelSave.add(buttonCancel);
		
		JButton btnAddNewPart = new JButton("Add New Part");
		btnAddNewPart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(comboBoxDestination.getSelectedIndex() >= 0 && comboBoxSource.getSelectedIndex() >= 0) {
					newPart(channel_indices.get(comboBoxSource.getSelectedIndex()), comboBoxDestination.getSelectedIndex() + 8, CSEG, parent);
				}
			}
		});
		btnAddNewPart.setToolTipText("Closes the window and adds a new CASM part with the selected source and destination channels.");
		panelCancelSave.add(btnAddNewPart);
		
		this.setVisible(true);
	}
	
	/** Fill the source dropdown. */
	private void fillSourceBox(Style style, int CSEG, JComboBox<String> source_box, Voice[] voices) {
		ArrayList<String> channel_names = new ArrayList<String>(0);
		channel_indices = new ArrayList<Integer>(0);
		
		for(int i=0;i<16;i+=1) {
			ArrayList<CasmPart> parts = style.getCasm().parts;
			
			boolean exists = false;
			for(int j=0;j<parts.size();j+=1) {
				if(parts.get(j).source_channel == i && parts.get(j).CSEG == CSEG) {
					exists = true;
					break;
				}
			}
			if(!exists) {
				String voice = "";
				try {
					ArrayList<FWSEvent> start_events = style.getStartEvents();
					for(int e=0;e<start_events.size();e+=1) {
						if(start_events.get(e) instanceof FWSVoiceEvent) {
							FWSVoiceEvent voice_event = (FWSVoiceEvent)start_events.get(e);
							if(voice_event.channel != i)
								continue;

							voice = Voice.matchVoice(voices, voice_event.voice, voice_event.voice_lsb, voice_event.voice_msb).name;
						}
					}
				} catch(NullPointerException|IndexOutOfBoundsException e) {
					voice = "Unknown Voice";
				}
				
				channel_names.add("Ch. " + (i+1) + " (" + voice + ")");
				channel_indices.add(Integer.valueOf(i));
			}
		}
		
		String[] channel_names_final = new String[channel_names.size()];
		for(int i=0;i<channel_names_final.length;i+=1)
			channel_names_final[i] = channel_names.get(i);
		
		source_box.setModel(new DefaultComboBoxModel<String>(channel_names_final));
	}
	
	private void closeDialogBox() {
		this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}
	
	private void newPart(int source, int destination, int CSEG, CasmEditor parent) {
		parent.newPart(source, destination, CSEG);
		this.closeDialogBox();
	}
}
