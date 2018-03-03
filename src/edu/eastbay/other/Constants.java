package edu.eastbay.other;

public interface Constants {
	//CREATE TYPE
	String USER_DATA_FILE = "U";
	String DIRECTORY_FILE = "D";
	
	//OPEN MODE
	String INPUT_MODE = "I";
	String OUTPUT_MODE = "O";
	String UPDATE_MODE = "U";
	
	//SEEK base
	String BEGINNING = "-1";
	String CURRENT = "0";
	String END_OF_FILE = "+1";
	
	//Directory Listing
	int DIRECTORY_ENTIRES = 3;
	String FREE = "F";
	
	int NEGATIVE = -1;
	int EMPTY_FILE = 0;
	
	int MAX_FILE_NAME_LENGTH = 9;
	int MAX_NO_OF_BYTES = 504;
	
	String SUCCESS = "Success";
	String ERROR = "Error";
	//Below constant can be use if write operation filled current block and need to write remaining data to new block
	String REMAINING_STRING = "RemString_";
	
	String END_OF_FILE_IS_REACHED = "--End of File is Reached";
}
