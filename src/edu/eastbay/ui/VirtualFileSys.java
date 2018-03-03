package edu.eastbay.ui;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import edu.eastbay.files.Block;
import edu.eastbay.files.Data;
import edu.eastbay.files.Directory;
import edu.eastbay.other.Constants;
import edu.eastbay.other.ErrorType;
import edu.eastbay.sector.Sector;

public class VirtualFileSys {
	//Scanner to read input from console
	Scanner scan = null;
	String command = null;
	Sector sector = new Sector();
	boolean callFromCreate = false;
	int currindex;
	
	public static void main(String[] args) {
		VirtualFileSys vfs = new VirtualFileSys();
		vfs.runCommand();
		
	}
	
	public void runCommand() {
		while(true) {
			System.out.println("Please enter the command : CREATE or OPEN or DELETE -- EXIT to exit application");
			scan =  new Scanner(System.in);
			command = scan.next();
			
			if(command != null) {
				switch(command.toUpperCase()) {
					case "CREATE" : 
						System.out.println("Entered : CREATE");
						createCommand();
						break;
					case "OPEN":
						System.out.println("Entered : OPEN");
						openCommand(null);
						break;
					case "DELETE":
						System.out.println("Entered : DELETE");
						deleteCommand();
						break;
					case "EXIT" : 
						System.out.println("Entered : EXIT");
						System.exit(0);
					 default:
						 System.out.println("Not a valid command");
						 break;
				}
			}
			
			command = null;//Reset the command
			
		}
	}
	
	public void createCommand() {
		String type, name;
		System.out.println("Please enter type of file to be created: U or D");
		type = scan.next();
		type = type.toUpperCase();
		
		System.out.println("Please enter name of the file to be created");
		name = scan.next();
		String verify = verifyName(name);
		if(Constants.SUCCESS.equals(verify))
			createFile(type, name);
		else {
			System.out.println("Max length of any file name string is nine charactes ");
		}
			
	}
	
	private String verifyName(String fileName) {
		String first = null;
		String second = null;
		int slashIndex = fileName != null ? fileName.indexOf("/") : Constants.NEGATIVE;
		if(Constants.NEGATIVE == slashIndex) {
			return (fileName.length()>9 ? Constants.ERROR : Constants.SUCCESS);
		} else {
			second = fileName.substring(slashIndex+1);
			first = fileName.substring(0, slashIndex);
			if(first.length() > 9) 
				return Constants.ERROR;
			else {
				return verifyName(second);
			}
		}
	}

	public void deleteCommand() {
		System.out.println("Enter the file or directory to be deleted:\n");
		String name = scan.next();
		deleteFile(name);
		
	}
	
	public void createFile(String type, String name) {
			int status = sector.createEntry(sector.getRootDirectory(), name, type, 0);//Root directory index is zero hence I am hardCoding it here
			//System.out.println(status);
			if(status < 0)
				printErrorIfAny(status);
			afterCreateTestClass();
			
			if(Constants.USER_DATA_FILE.equals(type)) {
				callFromCreate = true;
				openFile(name, Constants.OUTPUT_MODE);
			}
				
	}
	
	public void deleteFile(String name) {
		int status = sector.deleteBlock(sector.getRootDirectory(), name);
		printErrorIfAny(status);
		afterCreateTestClass();
	}
	
	/**
	 * Prints error message if any
	 * @param status
	 */
	private void printErrorIfAny(int status) {
		if(status != ErrorType.SUCCESS.getErrorId()) {
			System.out.println(ErrorType.getValue(status).getDetails());
		}
	}

	//TODO: Remove test class
	private void afterCreateTestClass() {
		Map<Integer, Block> sectorMap = sector.getSector();
		Iterator<Entry<Integer, Block>> it = sectorMap.entrySet().iterator();
		System.out.println("");
		System.out.println("-------------------------------------------------------------------");
		int i=0;
		while(it.hasNext()) {
			Map.Entry<Integer, Block> entry = it.next();
			if(entry.getValue() != null) {
				System.out.println("Sector  " + entry.getKey()+ ": " );
				if(entry.getValue() instanceof Directory) {
					Directory dir = (Directory) entry.getValue();
					System.out.println(dir);
				} else {
					Data df = (Data) entry.getValue();
					System.out.println(df);
				}
			}
			i++;
		}
		System.out.println("-------------------------------------------------------------------");
		System.out.println("");
	}
	
	public void openCommand(String fileName) {
			String mode=null, name=null;
			System.out.println("Enter mode : I-Input, O-Output, U-Update :");
			mode = scan.next();
			System.out.println("Enter name of the file: ");
			name = scan.next();
			 openFile(name, mode);
	}
	
	public void openFile(String fileName, String mode) {
		boolean writeComplete = false;
		String input;
		while(true) {
			if(callFromCreate) {
				System.out.println("Open command with 'O' output mode auto selected");
				input = "2";
			} else {
				System.out.println("Please choose the option \n 1 for Read  2 for Write  3 for Seek 4 for Close");
				input = scan.next();
			}
			
			if("4".equals(input)) {
				return;
			}
			
			if(Constants.INPUT_MODE.equals(mode)) {
				if("1".equals(input)) {
					System.out.println("Enter number of bytes to read : ");
					int numberOfBytes = Integer.parseInt(scan.next());
					readFile(fileName, numberOfBytes);
				} else if("3".equals(input)) {
					int base,offset;
					System.out.println("Enter base : ");
					base = Integer.parseInt(scan.next());
					System.out.println("Enter offset : ");
					offset = Integer.parseInt(scan.next());
					seek(fileName, base, offset);
				} else {
					System.out.println("Only 1. Read and 3. Seek are premitted in Input mode");
				}
			} else if(Constants.OUTPUT_MODE.equals(mode)) {
				if("2".equals(input)) {
					writeCommand(fileName);
					writeComplete = true;
					
					System.out.println("Would you like to close the file : Y or N ");
					input = scan.next();
					if("Y".equals(input)) {
						callFromCreate = false;
						return;
					}
					else {
						System.out.println("Would you like to write somemore to file : Y or N ");
						return;
					}
						
				} else {
					System.out.println("Only Write mode is allowed");
				}
			} else if(Constants.UPDATE_MODE.equals(mode)) {
				if("4".equals(input)) {
					System.out.println("Close is not allowed - Only 1 for Read  2 for Write  3 for Seek are allowed");
				}
			}  else {
				System.out.println("Invalid option chosen");
			}
			
		}
		
	}

	private void writeCommand(String fileName) {
		System.out.println("Write command: Please enter number of bytes followed by text : syntax is  n 'data' ");
		int bytes =  Integer.parseInt(scan.next());
		String data = scan.nextLine();
		data = data.substring(data.indexOf('\'')+1, data.lastIndexOf('\''));
		
		sector.writeData(fileName, bytes, data);
		System.out.println("Write complete");
		
		afterCreateTestClass();
	}

	private void seek(String fileName, int base, int offset) {
		if (base == -1)
		{
			currindex=0;
	
			String readfile1;
			Map<Integer, String> returnData = sector.readData(fileName, offset);
			readfile1 = returnData.get(0)+(returnData.get(1)!=null ? returnData.get(1) : "");
			currindex =currindex+ readfile1.length();
			System.out.println("currindex is: " +currindex+"   "+readfile1.length());
			System.out.println(readfile1);
			
		}
		else if(base == 1)
		{
			currindex= 0;
			Map<Integer, String> returnData = sector.readData(fileName, 50);
			String seekfile;
			int endindex,seekindex;
			//System.out.println("Data length : "+ returnData.get(0).length());
			seekfile = returnData.get(0)+(returnData.get(1)!=null ? returnData.get(1) : "");
			endindex = seekfile.indexOf("-");
			seekfile = seekfile.substring(0,endindex);
			//System.out.println(seekfile);
			seekindex = seekfile.length() -offset;
			seekfile = seekfile.substring(seekindex,endindex);
			System.out.println(seekfile);
			currindex = currindex+seekfile.length();
			System.out.println("currindex is: " +currindex);
		}
		else 
		{
			if (offset >= 0)
			{
				String readfile0;
				int newoffset = currindex+offset;
				Map<Integer, String> returnData = sector.readData(fileName, newoffset);
				readfile0 = returnData.get(0)+(returnData.get(1)!=null ? returnData.get(1) : "");
				//System.out.println("old:  "+readfile0);
				System.out.println("currindex ::"+currindex);
				System.out.println("offset ::"+offset);
				readfile0= readfile0.substring(currindex,currindex+offset);
				System.out.println("new:  "+readfile0);
				currindex =currindex+readfile0.length();
				
			}
			else
			{
				String readfileneg;
				int newoffset = currindex+offset;
				Map<Integer, String> returnData = sector.readData(fileName, currindex);
				readfileneg = returnData.get(0)+(returnData.get(1)!=null ? returnData.get(1) : "");
				//System.out.println("old:  "+readfileneg);
				System.out.println("currindex ::"+currindex);
				System.out.println("offset ::"+offset);
				readfileneg= readfileneg.substring(newoffset,currindex);
				System.out.println("new:  "+readfileneg);
				currindex =currindex+readfileneg.length();
			}
			
		}
		
		
		
	}

	private void readFile(String fileName, int numberOfBytes) {
		String readfile,readfilenew;
		int readindex;
		currindex=0;
		Map<Integer, String> returnData = sector.readData(fileName, numberOfBytes);
		System.out.println("Data length : "+ returnData.get(0).length());
		System.out.println(returnData.get(0)+(returnData.get(1)!=null ? returnData.get(1) : ""));
		readfile = returnData.get(0)+(returnData.get(1)!=null ? returnData.get(1) : "");
		if(numberOfBytes> readfile.length())
		{
		readindex = readfile.indexOf("-");
		readfilenew = readfile.substring(0,readindex);
		currindex = currindex+readfilenew.length();
		}
		else
		{
		currindex = currindex+readfile.length();
		}
		System.out.println("currindex is: " +currindex);
		
	}
}
