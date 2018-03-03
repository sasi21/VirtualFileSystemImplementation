package edu.eastbay.data;

import edu.eastbay.other.Constants;

public class DirEntryDetails {
	private int dirEntryIndex; // Dir[31] array index where file details are present
	private String type; // F / D / U
	private int link; // Block number of first block of user file
	
	boolean isDirectoryExtension = false;
	private int extDirectoryIndex = Constants.NEGATIVE;
	/**
	 * Index of Directory Entry array of current block
	 * @return
	 */
	public int getDirEntryIndex() {
		return dirEntryIndex;
	}
	public void setDirEntryIndex(int dirEntryIndex) {
		this.dirEntryIndex = dirEntryIndex;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getLink() {
		return link;
	}
	public void setLink(int link) {
		this.link = link;
	}
	public boolean isDirectoryExtension() {
		return isDirectoryExtension;
	}
	public void setDirectoryExtension(boolean isDirectoryExtension) {
		this.isDirectoryExtension = isDirectoryExtension;
	}
	public int getExtDirectoryIndex() {
		return extDirectoryIndex;
	}
	public void setExtDirectoryIndex(int extDirectoryIndex) {
		this.extDirectoryIndex = extDirectoryIndex;
	}
	
	
}
