package style;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Casm {
	public static class CasmSect {
		public String name;
		public int CSEG;
		
		public CasmSect(String newname, final int newCSEG) {
			name = newname;
			CSEG = newCSEG;
		}

		public CasmSect(CasmSect c) {
			this.name = c.name;
			this.CSEG = c.CSEG;
		}
	}
	
	public static class CasmPart {
		public String name;
		public byte source_channel, channel, sourcechord, sourcechord_type, NTT, high_key, low_limit, high_limit, RTR;
		public boolean editable, noteplay[], chordplay[], NTR, autostart;
		public int CSEG;
		
		public CasmPart() {
		}
		
		public CasmPart(CasmPart c) {
			this.name = c.name;
			this.source_channel = c.source_channel;
			this.channel = c.channel;
			this.sourcechord = c.sourcechord;
			this.sourcechord_type = c.sourcechord_type;
			this.NTR = c.NTR;
			this.NTT = c.NTT;
			this.high_key = c.high_key;
			this.low_limit = c.low_limit;
			this.high_limit = c.high_limit;
			this.RTR = c.RTR;
			this.CSEG = c.CSEG;
			this.autostart = c.autostart;
			this.editable = c.editable;
			this.noteplay = Arrays.copyOf(c.noteplay, c.noteplay.length);
			this.chordplay = Arrays.copyOf(c.chordplay, c.chordplay.length);
		}
	}

	public ArrayList<CasmSect> sections;
	public ArrayList<CasmPart> parts;
	public boolean[] used;
	
	public byte[] casm_binary;
	
	public Casm() {
		this.parts = new ArrayList<CasmPart>(0);
		this.sections = new ArrayList<CasmSect>(0);
		this.casm_binary = new byte[0];
		this.used = new boolean[16];
	}
	
	public Casm(Casm c) {
		this.parts = new ArrayList<CasmPart>(0);
		this.sections = new ArrayList<CasmSect>(0);
		
		int i;
		
		this.casm_binary = new byte[c.casm_binary.length];
		for(i=0;i<this.casm_binary.length;i+=1)
			this.casm_binary[i] = c.casm_binary[i];
		
		for(i=0;i<c.parts.size();i+=1)
			this.parts.add(new CasmPart(c.parts.get(i)));
		for(i=0;i<c.sections.size();i+=1)
			this.sections.add(new CasmSect(c.sections.get(i)));
		
		this.used = new boolean[16];
		this.used = Arrays.copyOf(used, used.length);
	}

	/** Get the destination channel. */
	public byte getChannelMap(final byte source) {
		byte destination = source;
		
		for(int i=0;i<this.parts.size();i+=1) {
			if(this.parts.get(i).source_channel == source) {
				destination = this.parts.get(i).channel;
				break;
			}
		}
		
		return destination;
	}

	/** Get the CASM binary. */
	public byte[] getCasmBinary() {
		if(sections.size() <= 0 || parts.size() <= 0)
			return new byte[0];

		byte[] the_return = new byte[0];
		ArrayList<Byte> list_return = new ArrayList<Byte>(0);
		
		final String casm = "CASM";
		final String cseg = "CSEG";
		final String ctab = "Ctab";
		final String sdec = "Sdec";
		
		final Byte[] casm_b = byteCapitalize(casm.getBytes());
		final Byte[] cseg_b = byteCapitalize(cseg.getBytes());
		final Byte[] ctab_b = byteCapitalize(ctab.getBytes());
		final Byte[] sdec_b = byteCapitalize(sdec.getBytes());
		
		int i, j, k;
		byte ph;
		
		int main_data_length_index = casm_b.length;
		
		final Byte[] empty_four = new Byte[4];
		Byte[] four = new Byte[4];
		
		Collections.addAll(list_return, casm_b);
		Collections.addAll(list_return, empty_four);
		
		//Get the number of CSEGs we need to worry about.
		int cseg_count = 0;
		for(i=0;i<this.sections.size();i+=1) {
			if(this.sections.get(i).CSEG > cseg_count)
				cseg_count = this.sections.get(i).CSEG;
		}
		cseg_count += 1;
		
		//Add CSEG names.
		for(i=0;i<cseg_count;i+=1) {
			Collections.addAll(list_return, cseg_b);
			
			int cseg_length_index = list_return.size();
			Collections.addAll(list_return, empty_four);
			
			Collections.addAll(list_return, sdec_b);
			
			int sdec_length_index = list_return.size();
			Collections.addAll(list_return, empty_four);
			
			for(j=0;j<this.sections.size();j+=1) {
				if(this.sections.get(j).CSEG == i) {
					Collections.addAll(list_return, byteCapitalize(this.sections.get(j).name.getBytes()));
					Collections.addAll(list_return, byteCapitalize(new String(",").getBytes()));
				}
			}
			//Pop the last comma.
			list_return.remove(list_return.size()-1);
			
			//Add length.
			four = byteCapitalize(decToShort(list_return.size() - (sdec_length_index+4)));
			for(j=0;j<four.length;j+=1) {
				list_return.set(sdec_length_index + j, four[j]);
			}
			
			//Now for CTABs.
			for(j=0;j<this.parts.size();j+=1) {
				CasmPart part = this.parts.get(j);
				if(part.CSEG == i) {
					Collections.addAll(list_return, ctab_b);
					
					int ctab_length_index = list_return.size();
					Collections.addAll(list_return, empty_four);
					
					list_return.add(Byte.valueOf(part.source_channel));
					String name = part.name;
					if(name.length() != 8) {
						if(name.length() > 8) {
							name = name.substring(0, 8);
						} else { 
							while(name.length() < 8)
								name += " ";
						}
					}
					
					Collections.addAll(list_return, byteCapitalize(name.getBytes()));
					
					list_return.add(Byte.valueOf(part.channel));
					if(part.editable)
						list_return.add(Byte.valueOf((byte) 1));
					else
						list_return.add(Byte.valueOf((byte)0));
					
					//Note play.
					ph = 0;
					for(k=part.noteplay.length - 1;k>=0;k-=1) {
						if(part.noteplay[k]) {
							ph = (byte)(ph|(0b1<<(k%8)));
						}
						if(k%8 == 0) {
							list_return.add(Byte.valueOf(ph));
							ph = 0;
						}
					}
					
					//Chord play.
					ph = 0;
					if(part.autostart)
						ph = (byte)(ph|(0b100));
					
					for(k=part.chordplay.length - 1;k>=0;k-=1) {
						while(k>=34)
							k-=1;
						
						if(part.chordplay[k]) {
							ph = (byte)(ph|(0b1<<(k%8)));
						}
						if(k%8 == 0) {
							list_return.add(Byte.valueOf(ph));
							ph = 0;
						}
						
					}
					
					list_return.add(Byte.valueOf(part.sourcechord));
					list_return.add(Byte.valueOf(part.sourcechord_type));
					
					if(part.NTR)
						list_return.add(Byte.valueOf((byte)1));
					else
						list_return.add(Byte.valueOf((byte) 0));
					
					list_return.add(Byte.valueOf(part.NTT));
					list_return.add(Byte.valueOf(part.high_key));
					
					list_return.add(Byte.valueOf(part.low_limit));
					list_return.add(Byte.valueOf(part.high_limit));
					
					list_return.add(Byte.valueOf(part.RTR));
					
					list_return.add(Byte.valueOf((byte) 0));
					
					//Add length back.
					four = byteCapitalize(decToShort(list_return.size() - (ctab_length_index+4)));
					for(k=0;k<four.length;k+=1) {
						list_return.set(ctab_length_index + k, four[k]);
					}
				}
			}
			
			//Add length back.
			four = byteCapitalize(decToShort(list_return.size() - (cseg_length_index+4)));
			for(j=0;j<four.length;j+=1) {
				list_return.set(cseg_length_index + j, four[j]);
			}
			
		}
		
		//Add length.
		four = byteCapitalize(decToShort(list_return.size() - (main_data_length_index+4)));
		for(j=0;j<four.length;j+=1) {
			list_return.set(main_data_length_index + j, four[j]);
		}
		
		the_return = byteLowercase(list_return.toArray());
		
		return the_return;
	}

	/** Create a CASM from binary data. */
	public static Casm createCasm(byte[] data) {
		Casm CASM = new Casm();
		
		CASM.casm_binary = data;
		
		CASM.sections = new ArrayList<CasmSect>(0);
		CASM.parts = new ArrayList<CasmPart>(0);
		CASM.used = new boolean[16];
		
		String test_string;
		int cseg_start = 0, cseg_end = 0, cseg_count = -1;
		
		for(int i=0;i<data.length-4;i+=1) {
			if(new String(Arrays.copyOfRange(data,i,i+4)).equals("CSEG")) {
				cseg_count += 1;
				cseg_start = i+8;
				cseg_end = cseg_start + 8 + (data[cseg_start + 7]&0xFF) +
					0x100*(data[cseg_start+6]&0xFF) +
					0x10000*(data[cseg_start + 5]&0xFF) +
					0x1000000*(data[cseg_start+4]&0xFF);
				test_string = "";
				int j;
				for(j=cseg_start+8;j<cseg_end;j+=1) {
					if((char)data[j] != ',')
						test_string += Character.toString((char)data[j]);
					else {
						CASM.sections.add(new CasmSect(test_string,cseg_count));
						test_string = "";
					}
					
					if(j==cseg_end - 1) {
						CASM.sections.add(new CasmSect(test_string,cseg_count));
						test_string = "";
					}
				}
				i=j-1;
			} else if(new String(Arrays.copyOfRange(data,i,i+4)).equals("Ctab")) {
				test_string = "";
				cseg_start = i;
				/*cseg_end = cseg_start + data[cseg_start + 7] +
						0x100*data[cseg_start+6] +
						0x10000*data[cseg_start + 5] +
						0x1000000*data[cseg_start+4];*/
				CasmPart casm_data = new CasmPart();
				//Source Channel
				casm_data.source_channel = data[cseg_start+8];
				//Name
				for(int j=cseg_start + 9;j<cseg_start + 17; j+=1)
					test_string += Character.toString((char)data[j]);
				casm_data.name = test_string;
				//Destination Channel
				casm_data.channel = data[cseg_start + 17];
				casm_data.editable = data[cseg_start + 18]!=0;
				casm_data.noteplay = new boolean[12];
				for(int j=0;j<12;j+=1)
					casm_data.noteplay[j] = (((data[cseg_start+19]&0xFF)*0x100 + (data[cseg_start+20]&0xFF))&(0b1<<j))!=0;
				casm_data.chordplay = new boolean[38];
				for(int j=0;j<32;j+=1)
					casm_data.chordplay[j] = (((data[cseg_start+22]&0xFF)*0x1000000 +
							(data[cseg_start+23]&0xFF)*0x10000 +
							(data[cseg_start+24]&0xFF)*0x100 + 
							(data[cseg_start+25]&0xFF))&(0b1<<j))!=0;
				for(int j=0;j<2;j+=1)
					casm_data.chordplay[32+j] = ((data[cseg_start+21])&(0b1<<j))!=0;
				//for(int j=2;j<6;j+=1)
				//	casm_data.chordplay[32+j] = true; //What the hell?!? Where are the other chords?!
				casm_data.chordplay[34] = true;
				casm_data.chordplay[35] = casm_data.chordplay[0x15];
				casm_data.chordplay[36] = casm_data.chordplay[0x11];
				casm_data.chordplay[37] = casm_data.chordplay[0x0B];
				
				casm_data.autostart = ((data[cseg_start+21])&(0b100))!=0;
				casm_data.sourcechord = data[cseg_start + 26];
				casm_data.sourcechord_type = data[cseg_start + 27];
				casm_data.NTR = data[cseg_start+28]!=0;
				casm_data.NTT = data[cseg_start+29];
				casm_data.high_key = data[cseg_start+30];
				casm_data.low_limit = data[cseg_start+31];
				casm_data.high_limit = data[cseg_start+32];
				casm_data.RTR = data[cseg_start+33];
				casm_data.CSEG = cseg_count;
				
				CASM.parts.add(casm_data);
				CASM.used[casm_data.channel] = true;
				
				i = cseg_start + 33;
			} else if(new String(Arrays.copyOfRange(data,i,i+4)).equals("Ctb2")) {
				//CTS2, unsupported
				return new Casm();
			}
		}
		return CASM;
	}

	/** Create a CASM from a file. */
	public static Casm createCasmFromFile(File style_file) {
		Casm CASM = new Casm();
		
		CASM.sections = new ArrayList<CasmSect>(0);
		CASM.parts = new ArrayList<CasmPart>(0);
		CASM.used = new boolean[16];
		
		boolean casm_found = false;
		
		String test_string;
		int i, j, casm_start = 0, cseg_start = 0, cseg_end = 0, cseg_count = -1;
		for(i=0;i<16;i+=1) 
			CASM.used[i] = false;
		
		//Find the CASM data.
		try {
			RandomAccessFile style_RA = new RandomAccessFile(style_file.getAbsolutePath(),"r");
		
			byte[] data = new byte[(int)style_RA.length()];
			style_RA.read(data);
			
			for(i=0;i<data.length-4;i+=1)
			{
				test_string = new String(Arrays.copyOfRange(data,i,i+4));
				if(test_string.equals("CASM"))
				{
					if(i + 12 < style_RA.length())
					{
						if(new String(Arrays.copyOfRange(data,i+8,i+12)).equals("CSEG")) {
							casm_found = true;
							casm_start = i;
							break;
						}
					}
				}
			}
			
			if(casm_found == false) {
				style_RA.close();
				return new Casm();
			}
			
			CASM.casm_binary = Arrays.copyOfRange(data,casm_start,data.length);
			
			//CASM data has been found!
			//Now to scan until we reach the end.
			for(i=casm_start;i<data.length-4;i+=1) {
				if(new String(Arrays.copyOfRange(data,i,i+4)).equals("CSEG")) {
					cseg_count += 1;
					cseg_start = i+8;
					cseg_end = cseg_start + 8 + (data[cseg_start + 7]&0xFF) +
						0x100*(data[cseg_start+6]&0xFF) +
						0x10000*(data[cseg_start + 5]&0xFF) +
						0x1000000*(data[cseg_start+4]&0xFF);
					test_string = "";
					for(j=cseg_start+8;j<cseg_end;j+=1) {
						if((char)data[j] != ',')
							test_string += Character.toString((char)data[j]);
						else {
							CASM.sections.add(new CasmSect(test_string,cseg_count));
							test_string = "";
						}
						
						if(j==cseg_end - 1) {
							CASM.sections.add(new CasmSect(test_string,cseg_count));
							test_string = "";
						}
					}
					i=j-1;
				} else if(new String(Arrays.copyOfRange(data,i,i+4)).equals("Ctab")) {
					test_string = "";
					cseg_start = i;
					/*cseg_end = cseg_start + data[cseg_start + 7] +
							0x100*data[cseg_start+6] +
							0x10000*data[cseg_start + 5] +
							0x1000000*data[cseg_start+4];*/
					CasmPart casm_data = new CasmPart();
					//Source Channel
					casm_data.source_channel = data[cseg_start+8];
					//Name
					for(j=cseg_start + 9;j<cseg_start + 17; j+=1)
						test_string += Character.toString((char)data[j]);
					casm_data.name = test_string;
					//Destination Channel
					casm_data.channel = data[cseg_start + 17];
					casm_data.editable = data[cseg_start + 18]!=0;
					casm_data.noteplay = new boolean[12];
					for(j=0;j<12;j+=1)
						casm_data.noteplay[j] = (((data[cseg_start+19]&0xFF)*0x100 + (data[cseg_start+20]&0xFF))&(0b1<<j))!=0;
					casm_data.chordplay = new boolean[38];
					for(j=0;j<32;j+=1)
						casm_data.chordplay[j] = (((data[cseg_start+22]&0xFF)*0x1000000 +
								(data[cseg_start+23]&0xFF)*0x10000 +
								(data[cseg_start+24]&0xFF)*0x100 + 
								(data[cseg_start+25]&0xFF))&(0b1<<j))!=0;
					for(j=0;j<2;j+=1)
						casm_data.chordplay[32+j] = ((data[cseg_start+21])&(0b1<<j))!=0;
					//for(j=2;j<6;j+=1)
					//	casm_data.chordplay[32+j] = true; //What the hell?!? Where are the other chords?!
					casm_data.chordplay[34] = true;
					casm_data.chordplay[35] = casm_data.chordplay[0x15];
					casm_data.chordplay[36] = casm_data.chordplay[0x11];
					casm_data.chordplay[37] = casm_data.chordplay[0x0B];
					
					casm_data.autostart = ((data[cseg_start+21])&(0b100))!=0;
					casm_data.sourcechord = data[cseg_start + 26];
					casm_data.sourcechord_type = data[cseg_start + 27];
					casm_data.NTR = data[cseg_start+28]!=0;
					casm_data.NTT = data[cseg_start+29];
					casm_data.high_key = data[cseg_start+30];
					casm_data.low_limit = data[cseg_start+31];
					casm_data.high_limit = data[cseg_start+32];
					casm_data.RTR = data[cseg_start+33];
					casm_data.CSEG = cseg_count;
					
					CASM.parts.add(casm_data);
					CASM.used[casm_data.channel] = true;
					
					i = cseg_start + 33;
				} else if(new String(Arrays.copyOfRange(data,i,i+4)).equals("Ctb2")) {
					//CTS2, unsupported
					style_RA.close();
					return new Casm();
				}
			}
			
			style_RA.close();
		} catch (IOException | ArrayIndexOutOfBoundsException e) {
			//Something's happened...
		}
		
		return CASM;
	}

	/** Get an array of object pointer bytes. */
	public static Byte[] byteCapitalize(byte[] data) {
		Byte[] new_data = new Byte[data.length];
		
		for(int i=0;i<data.length;i+=1)
			new_data[i] = Byte.valueOf(data[i]);
		
		return new_data;
	}
	
	/** Get an array of primitive bytes. */
	public static byte[] byteLowercase(Object[] data) {
		byte[] new_data = new byte[data.length];
		
		for(int i=0;i<data.length;i+=1) {
			try {
				new_data[i] = (byte)data[i];
			} catch (NullPointerException|ClassCastException e) {
				new_data[i] = 0;
			}
		}
		
		return new_data;
	}
	
	/** Break an int into an array of bytes. */
	public static byte[] decToShort(int dec) {
		byte[] the_return = new byte[4];
		
		the_return[3] = (byte)(dec%0x100);
		the_return[2] = (byte)(dec%0x10000 / 0x100);
		the_return[1] = (byte)(dec%0x1000000 / 0x10000);
		the_return[0] = (byte)(dec / 0x1000000);
		
		return the_return;
	}
}
