package edu.eastbay.files;

import edu.eastbay.data.DirDetailsForWrite;
import edu.eastbay.data.DirEntryDetails;
import edu.eastbay.other.Constants;

public class Directory implements Block{
	//Block number of first unused block
	private byte free;
	private byte filler[] = new byte[4];
	//Directory Entries
	private DirectoryEntry dir[];
	
	private int back;
	private int frwd;
	
	public Directory() {
		dir = new DirectoryEntry[31];
		for(int i=0; i< Constants.DIRECTORY_ENTIRES; i++) {
			dir[i] = new DirectoryEntry();
		}
		setDir(dir);
		//Arrays.fill(dir, new DirectoryEntry());
	}
	
	public int getFree() {
		return (int)free;
	}
	public void setFree(int free) {
		this.free = (byte)free;
	}
	public byte[] getFiller() {
		return filler;
	}
	public void setFiller(byte[] filler) {
		this.filler = filler;
	}
	public DirectoryEntry[] getDir() {
		return dir;
	}
	public void setDir(DirectoryEntry[] dir) {
		this.dir = dir;
	}
	public int getBack() {
		return back;
	}
	public void setBack(int back) {
		this.back = back;
	}
	public int getFrwd() {
		return frwd;
	}
	public void setFrwd(int frwd) {
		this.frwd = frwd;
	}

	
	public void setDirWithDefaultValues(int index) {
		dir[index] = new DirectoryEntry();
	}
	
	public void setDirWithGivenSize(int index, int size) {
		DirectoryEntry dirEntry = dir[index];
		dirEntry.setSize(size);
		dir[index] = dirEntry;
	}
	
	/**
	 * This method checks if given fileName exist in given directory
	 * @param name
	 * @return Return the DirEntryDetails details object of given file/directory. If nothing found then null is returned.
	 */
	public DirEntryDetails containDirEntry(String name, String fileType) {
		String entryName;
		DirEntryDetails dirEntryDetails = null;
		for(int i=0; i< Constants.DIRECTORY_ENTIRES; i++) {
			entryName = dir[i].getName();
			if(entryName != null && entryName.equals(name) && dir[i].getType() != null && dir[i].getType().equals(fileType)) {
				dirEntryDetails = new DirEntryDetails();
				dirEntryDetails.setDirEntryIndex(i);
				dirEntryDetails.setLink(dir[i].getLink());
				dirEntryDetails.setType(dir[i].getType());
				return dirEntryDetails;
			}
		}
		return null;
	}
	
	/**
	 * This method checks if given fileName exist in given directory, given only fileName
	 * @param name
	 * @return Return a DirEntryDetails object. If no value is found then returns null
	 */
	public DirEntryDetails containDirEntryByName(String name) {
		String entryName;
		DirEntryDetails dirEntryDetails = null;
		for(int i=0; i< Constants.DIRECTORY_ENTIRES; i++) {
			entryName = dir[i].getName();
			if(entryName != null && entryName.equals(name)) {
				dirEntryDetails = new DirEntryDetails();
				dirEntryDetails.setDirEntryIndex(i);
				dirEntryDetails.setLink(dir[i].getLink());
				dirEntryDetails.setType(dir[i].getType());
				return dirEntryDetails;
			}
		}
		return null;
	}
	
	
	/**
	 * This method is used to make an entry in to directory listing
	 * @param type -- Type of file F-Free, D-Directory, U-UserData
	 * @param name -- File name, Left-Justified, Blank filled
	 * @param link -- Block number of first block of file
	 * @param size -- Number of bytes used in the last block of file
	 * @param index -- Index of DirListing array where entry should be made
	 */
	public void makeDirListEntry(String type, String name, int link, int size, int index) {
		dir[index].setType(type);
		dir[index].setName(name);
		dir[index].setLink(link);
		dir[index].setSize(size);
	}
	
	/**
	 * Check if directory listing entry of given root directory is exhausted 
	 * @return If the listing is not exhausted then return the index of first free entry
	 */
	public int checkDirListing() {
		for(int i=0; i< Constants.DIRECTORY_ENTIRES; i++) {
			if(Constants.FREE.equals(dir[i].getType()))
				return i;
		}
		return Constants.NEGATIVE;
	}
	
	public boolean isDirListingEmpty() {
		for(int i=0; i< Constants.DIRECTORY_ENTIRES; i++) {
			if(!Constants.FREE.equals(dir[i].getType()))
				return false;
		}
		return true;
	}
	
	
	public String toString() {
		String returnString = "Free: "+getFree() + "  Back: "+getBack() + "  Frwd : "+getFrwd();
		for(int i=0; i< Constants.DIRECTORY_ENTIRES; i++) {
			if(!Constants.FREE.equals(dir[i].getType())) {
				returnString = returnString + "\n" + dir[i];
			}
		}
		
		return returnString;
		
	}
	
}
