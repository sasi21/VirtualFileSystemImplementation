package edu.eastbay.files;

import java.util.Arrays;

import edu.eastbay.other.Constants;

public class DirectoryEntry {
	private byte type = 'F'; // 'F' = Free, 'D' = Directory, 'U' = User data
	private byte name[] = {' ',' ',' ',' ',' ',' ',' ',' ',' '}; //File name, left-justified, blank filled
	private int link; //Block number of the first block of file
	private short size; // No of bytes used in the last block of the file
	
	public String getType() {
		Character tempType = (char)type;
		return tempType.toString();
	}
	public void setType(String type) {
		this.type = (byte)(type.toCharArray()[0]);
	}
	public String getName() {
		return new String(name).trim();
	}
	public void setName(String fileName) {
		byte tempName[] = fileName.getBytes();
		
		for(int i=0; i< Constants.MAX_FILE_NAME_LENGTH; i++) {
			if( i < fileName.length()) 
				name[i] = tempName[i];
			else 
				name[i] = (byte)' ';
		}
	}
	//Below method is used just for loggin purpose
	public String getNameWithOutTrim() {
		return new String(name);
	}
	
	public int getLink() {
		return link;
	}
	public void setLink(int link) {
		this.link = link;
	}
	public int getSize() {
		return (int)size;
	}
	public void setSize(int size) {
		this.size = (short)size;
	}
	
	public String toString() {
		return "Type : " +getType()+ "  Name : " +getNameWithOutTrim()+ "  Link : " +getLink()+ "  Size : "+getSize();
		
	}
	
}
