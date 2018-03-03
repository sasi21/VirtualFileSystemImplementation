package edu.eastbay.other;

public enum ErrorType {
	
	SECTOR_FULL(-101, "Secotr.full", "No empty blocks are available in sector"),
	DIRECTORY_LISTING_FULL(-102, "DirList.full", "Directory listing is full - Max 31 entires"),
	NO_FILE_EXIST_WITH_GIVEN_NAME(-103, "File.notFound", "Given file name does not exist"),
	SUCCESS(-10001, "Sucess" , "Success"),
	UNKNOWN(-10002, "Unkown error", "Unknown error");
	
    private int errorId;
    private String messageKey;
    private String details;
    
    ErrorType(int errorId, String messageKey, String details) {
        this.errorId = errorId;
        this.messageKey = messageKey;
        this.details = details;
    }
    
	public int getErrorId() {
		return errorId;
	}
	public String getMessageKey() {
		return messageKey;
	}
	public String getDetails() {
		return details;
	}
	
	ErrorType() {
	}
	
	public static ErrorType getValue(int errorId) {
		if(errorId == -101) return SECTOR_FULL;
		else if(errorId == -102) return DIRECTORY_LISTING_FULL;
		else if(errorId == -103) return NO_FILE_EXIST_WITH_GIVEN_NAME;
		else if(errorId == -10001) return SUCCESS;
		return UNKNOWN;
	}
    
}
