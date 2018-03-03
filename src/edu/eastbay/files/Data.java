package edu.eastbay.files;

import java.util.Map;

import edu.eastbay.other.Constants;
import edu.eastbay.sector.Sector;

public class Data implements Block{

	private byte userData[];
	
	public Data() {
		userData = new byte[Constants.MAX_NO_OF_BYTES];
		for(int i=0; i< Constants.MAX_NO_OF_BYTES; i++) {
			userData[i] = 3;//3 is ASCII value for END of Text
		}
		setUserData(userData);
	}
	
	public byte[] getUserData() {
		return userData;
	}
	public void setUserData(byte[] userData) {
		this.userData = userData;
	}
	
	private int back;
	private int frwd;
	
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

	
	public String writeUserData(int bytes, String data) {
		StringBuffer sb = new StringBuffer(data);
		int remainingBytes = remainingBytes();
		int filledBytes = filledBytes();
		int i=0, j=0;
		
		if(bytes < remainingBytes) {
			for(i=0,j=filledBytes; j<bytes && i<bytes; j++, i++) {
				if(i<sb.length()) 
					userData[j] = (byte)sb.charAt(i);
				else 
					userData[j] = (byte)' ';
			}
			return ""+filledBytes();
		} else if(bytes >= remainingBytes) {
			for(j=filledBytes, i=0; j<remainingBytes && i<remainingBytes; j++, i++) {
				if(i<sb.length())
					userData[j] = (byte)sb.charAt(i);
				else 
					userData[j] = (byte)' ';
			}
			if(remainingBytes > sb.length())
				return Constants.REMAINING_STRING+(bytes-remainingBytes)+"_";//As number of bytes is less than length of input string hence we are filling ' ' 
			else
				return Constants.REMAINING_STRING+(bytes-remainingBytes)+"_"+sb.substring(i);
		}
		
		return Constants.SUCCESS;
	}
	
	public int remainingBytes() {
		return(Constants.MAX_NO_OF_BYTES - filledBytes());
	}
	
	public int filledBytes() {
		int filled=Constants.MAX_NO_OF_BYTES;
		for(int i=0; i<Constants.MAX_NO_OF_BYTES; i++) {
			if(userData[i] == 3) {
				filled = i;
				break;
			}
		}
		return filled;
	}
	
	public String getUserDataString() {
		StringBuffer sb = new StringBuffer("");
		int filledBytes = filledBytes();
		for(int i =0; i< filledBytes ; i++) {
			sb.append((char)userData[i]);
		}
		
		return sb.toString();
	}
	
	public String toString() {
		return "Back : "+getBack()  + "  Forward: "+getFrwd();
	}
}
