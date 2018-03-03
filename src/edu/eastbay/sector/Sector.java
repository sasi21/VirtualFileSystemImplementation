package edu.eastbay.sector;

import java.util.HashMap;
import java.util.Map;

import edu.eastbay.data.DirDetailsForWrite;
import edu.eastbay.data.DirEntryDetails;
import edu.eastbay.files.Block;
import edu.eastbay.files.Data;
import edu.eastbay.files.Directory;
import edu.eastbay.files.DirectoryEntry;
import edu.eastbay.other.Constants;
import edu.eastbay.other.ErrorType;

public class Sector {
	//private Block sector[];
	private static int MAX = 100;
	private Map<Integer, Block> sector;
	private Directory directory;
	
	public Sector() {
		sector = new HashMap<Integer,Block>();
		
		for(int i =0; i<MAX; i++) {
			if(i != 0)
				sector.put(i, null);
			else {
				directory = new Directory();
				directory.setBack(0);
				directory.setFrwd(0);
				directory.setFree((byte)1);
				sector.put(i, directory);
			}
		}
	}
	
	public Map<Integer, Block> getSector() {
		return sector;
	}
	
	
	/**
	 * @return Return the block that is free in the sector
	 */
	public int freeBlock() {
		for(int i=1 ; i<MAX; i++) {
			if(sector.get(i) == null) {
				return i;
			}
		}
		return Constants.NEGATIVE;
	}
	
/*	public int fileBlock(String fullFileName) {
		String name[];
		if(fullFileName != null) {
			name = fullFileName.split("/");
			if(name.length == 1) {
				return getRootDirectory().containDirEntry(name[0]);
			}  else if(name.length > 1) {
				
			}
		}
		return Constants.NEGATIVE;
	} */
	
	/**
	 * @return Return the Root-Directory Directory Object
	 */
	public Directory getRootDirectory() {
		return (Directory)sector.get(0);
	}
	
	public int createEntry(Directory root, String fileName, String type, int rootIndex) {
		int slashIndex = fileName != null ? fileName.indexOf("/") : Constants.NEGATIVE;
		String first = null;
		String second = null;
		
		if(slashIndex > 0) {
			second = fileName.substring(slashIndex+1);
			first = fileName.substring(0, slashIndex);
			
			//DirEntryDetails blockNum = root.containDirEntry(first, Constants.DIRECTORY_FILE);
			DirEntryDetails blockNum = containDirEntryInDirIncludingExtension(root, first, Constants.DIRECTORY_FILE);
			Directory newRoot = null;
			if(blockNum == null) {
				int index = addBlock(root,  first,  Constants.DIRECTORY_FILE, rootIndex);//Because files can only exist in directory hence I am hard-coding it here
				if(index > 0) {
					newRoot =(Directory) sector.get(index);
					rootIndex = index;
				} else {
					return index;
				}
			} else {
				newRoot =(Directory) sector.get(blockNum.getLink());
				rootIndex = blockNum.getLink();
			}
			return createEntry( newRoot,  second,  type, rootIndex);
			
		} else {
			second = null;
			first = fileName;
			
			DirEntryDetails blockNum = containDirEntryInDirIncludingExtension(root, first, type);
			if(blockNum != null) {
				if(blockNum.isDirectoryExtension()) {
					root = (Directory) sector.get(blockNum.getExtDirectoryIndex());
					rootIndex = blockNum.getExtDirectoryIndex();
				}
				deleteFile(root, blockNum, type);
				root.setDirWithDefaultValues(blockNum.getDirEntryIndex());
			}
			return addBlock(root, first, type, rootIndex);
		}
	}
	
	/**
	 * This method is written to search in directory and directory extension(i.e., In given directory all 31 Dir entries are exhausted and we extended with FRWD pointer) as well
	 * @param root
	 * @param fileName
	 * @param type
	 * @return
	 */
	private DirEntryDetails containDirEntryInDirIncludingExtension(Directory root, String fileName, String type) {
		DirEntryDetails blockNum = root.containDirEntry(fileName, Constants.DIRECTORY_FILE);
		int newRootIndex = 0;
		while(blockNum == null && root.getFrwd() != 0) {
			newRootIndex = root.getFrwd();
			root = (Directory) sector.get(newRootIndex);
			blockNum = root.containDirEntry(fileName, Constants.DIRECTORY_FILE);
			
			if(blockNum != null) {
				blockNum.setDirectoryExtension(true);
				blockNum.setExtDirectoryIndex(newRootIndex);
				
				return blockNum;
			}
		}
		return blockNum;
	}
	
	/**
	 * This method will check if empty blocks exist in sector. If there are empty blocks then a check is made to see if dir-listing of current directory is not exhausted.
	 * If above two conditions are true then a call is made to create file or directory and a call is made to make entry in directory listing of current directory.
	 * if dir-listing of current directory exhausted then new directory is created and FRWD and BACK pointers of both directories are updated
	 * @param root
	 * @param name
	 * @param type
	 * @return block id of sector where new file/directory is added
	 */
	public int addBlock(Directory root, String name, String type, int rootIndex) {
		int freeBlock = freeBlock(); // #1
		if(freeBlock != Constants.NEGATIVE) {
			int dirList = root.checkDirListing();
			if(dirList != Constants.NEGATIVE) {
				createFiOrDi(root, type, freeBlock);
				root.makeDirListEntry(type, name, freeBlock, Constants.EMPTY_FILE, dirList);
				return freeBlock;
			} else {//Current directory Directory_listing is exhausted so we need to create new directory and set OldDirectory-FRWD pointer to point to new Directory(if FRWD pointer of current directory is zero).
				if(root.getFrwd() != 0) {//Then check in the directory pointed by FRWD
					return addBlock((Directory)sector.get(root.getFrwd()), name, type, root.getFrwd());
				} else {
					sector.put(freeBlock, new Directory());
					Directory newRoot = (Directory) sector.get(freeBlock);
					root.setFrwd(freeBlock);
					newRoot.setBack(rootIndex);
					
					//As we have already used freeBlock got in #1 so we are going to get new freeBlock if any.
					freeBlock = freeBlock();
					dirList = newRoot.checkDirListing();
					if(freeBlock != Constants.NEGATIVE) {
						createFiOrDi(newRoot, type, freeBlock);
						newRoot.makeDirListEntry(type, name, freeBlock, Constants.EMPTY_FILE, dirList);
						return freeBlock;
					} else {
						return ErrorType.SECTOR_FULL.getErrorId();
					}
				}
				//return ErrorType.DIRECTORY_LISTING_FULL.getErrorId();
				
			}
		} else {
			return ErrorType.SECTOR_FULL.getErrorId();
		}
	}
	
	/**
	 * Create File / Directory and makes an entry in to Directory listing table of given root directory
	 * @param root
	 * @param type
	 * @param freeBlock
	 * @return block number of the newly created File / Directory
	 */
	private void createFiOrDi(Directory root, String type, int freeBlock) {
		if(Constants.DIRECTORY_FILE.equals(type)) {
			sector.put(freeBlock, new Directory());
		} else {
			sector.put(freeBlock, new Data());
		}
	}


	public int deleteBlock(Block root, String fileName) {
		
		int slashIndex = fileName != null ? fileName.indexOf("/") : Constants.NEGATIVE;
		String first = null;
		String second = null;
		DirEntryDetails dirEntryDetails = null;
		if(slashIndex > 0) {
			second = fileName.substring(slashIndex+1);
			first = fileName.substring(0, slashIndex);
			
			dirEntryDetails = ((Directory)root).containDirEntryByName(first);
			// Suppose file name is abcd/efgh. As abcd should contain efgh, abcd should definitely be directory.
			if(dirEntryDetails != null && (Constants.DIRECTORY_FILE.equals(dirEntryDetails.getType()))) {
				return deleteBlock((Directory) sector.get(dirEntryDetails.getLink()), second);
			} else {
				return ErrorType.NO_FILE_EXIST_WITH_GIVEN_NAME.getErrorId();
			}
			
		} else {
			dirEntryDetails = ((Directory)root).containDirEntryByName(fileName);
			if(dirEntryDetails != null) {
				if(Constants.USER_DATA_FILE.equals(dirEntryDetails.getType())) {
					recursiveDeleteUserFile(dirEntryDetails.getLink());
				} else {
					recursiveDeleteDirectory(root, dirEntryDetails.getLink(), dirEntryDetails);
				}
				((Directory)root).setDirWithDefaultValues(dirEntryDetails.getDirEntryIndex());
			} else {
				return ErrorType.NO_FILE_EXIST_WITH_GIVEN_NAME.getErrorId();
			}
		}
		return ErrorType.SUCCESS.getErrorId();
	}
	
	private void recursiveDeleteDirectory(Block root, int link, DirEntryDetails dirEntryDetails) {
		if(((Directory)sector.get(link)).isDirListingEmpty()) {
			sector.put(link, null);
			((Directory)root).setDirWithDefaultValues(dirEntryDetails.getDirEntryIndex());
		} else {
			for(int i=0; i< Constants.DIRECTORY_ENTIRES; i++) {
				DirectoryEntry dir  = ((Directory)sector.get(link)).getDir()[i];
				if(dir.getType() != null && Constants.USER_DATA_FILE.equals(dir.getType())) {
					recursiveDeleteUserFile(dir.getLink());
					((Directory)sector.get(link)).setDirWithDefaultValues(i);
				} else if(Constants.FREE.equals(dir.getType())) { 
					continue;
				} else {
					recursiveDeleteDirectory(sector.get(link), dir.getLink(), ((Directory)sector.get(link)).containDirEntry(dir.getName(), dir.getType()));
				}
			}
		}
		
	}

	private void recursiveDeleteUserFile(int fileIndex) {
		Data data = (Data) sector.get(fileIndex);
		if(data != null && data.getFrwd() != 0) 
			recursiveDeleteUserFile(data.getFrwd());
		
		sector.put(fileIndex, null);
	}

	private void deleteFile(Block file, DirEntryDetails blockNu, String type) {
		if(Constants.DIRECTORY_FILE.equals(type)) {
			recursiveDeleteDirectory(file, blockNu.getLink(), blockNu);
		} else {
			recursiveDeleteUserFile(blockNu.getLink());
			
		}
	}

	public ErrorType writeData(String fileName, int bytes, String data) {
		//Below variable will have sector index of directory which contain given file name in its directory listing entry
		DirDetailsForWrite dirDetailsForWrite = fileWithDirListingEntry(fileName, getRootDirectory(), 0); 
		String s= null;
		int lastBlockBytesUsed=0;
		int indexOfFile = dirDetailsForWrite.getDirEntryDetails().getLink();
		Data tempDataBlock = (((Data)sector.get(indexOfFile)));
		if(dirDetailsForWrite != null) {
			s= tempDataBlock.writeUserData(bytes, data);
		}
		
		while(true) {
			if(s!=null && (!Constants.SUCCESS.equals(s))) {
				int slashIndex = s != null ? s.indexOf("_") : Constants.NEGATIVE;
				String first = null;
				String remBytes = null;
				String remData = null;
				
				if(Constants.NEGATIVE != slashIndex) {
					System.out.println("s =  "+s + " Slash index : "+ slashIndex);
					first = s.substring(0, slashIndex+1);
					remBytes = s.substring(slashIndex+1);
				} else {
					first = s;
					remBytes = null;
				}
				
				if(first != null && !Constants.REMAINING_STRING.equals(first)) {
					lastBlockBytesUsed = Integer.parseInt(first);
					break; // break out of the loop as number of bytes mentioned is wrote already
				} else if(Constants.REMAINING_STRING.equals(first)) {
					slashIndex = remBytes != null ? remBytes.indexOf("_") : Constants.NEGATIVE;
					if(Constants.NEGATIVE != slashIndex) {
						remData = remBytes.substring(slashIndex+1);
						remBytes = remBytes.substring(0, slashIndex);
						
						int freeBlock = freeBlock();
						if(freeBlock != Constants.NEGATIVE) {
							sector.put(freeBlock, new Data());
							tempDataBlock.setFrwd(freeBlock);
							((Data)sector.get(freeBlock)).setBack(indexOfFile);
							s = ((Data)sector.get(freeBlock)).writeUserData(remBytes!=null?Integer.parseInt(remBytes):0, remData);
							//Setting tempDataBlock with the new Data object that is created now. This will be handy even we run out of new user block and we need another user block...
							tempDataBlock = ((Data)sector.get(freeBlock));
							indexOfFile = freeBlock;
						} else {
							return ErrorType.SECTOR_FULL;
						}
					}
				}
				
			}
		}
		
		DirEntryDetails dirEntryTemp = dirDetailsForWrite.getDirEntryDetails();
		//TODO: Update correct Free bytes value
		((Directory)sector.get(dirDetailsForWrite.getRootIndex())).setDirWithGivenSize(dirEntryTemp.getDirEntryIndex(), lastBlockBytesUsed);
		return ErrorType.SUCCESS;
	}
	
	/**
	 * If file name is given as abc/def then abc index value is returned
	 * If file name is given as abc/def/ijk then def index value is returned
	 * @param fileName
	 * @param root
	 * @param rootIndex
	 * @return
	 */
	private DirDetailsForWrite fileWithDirListingEntry(String fileName, Block root, int rootIndex) {
		int slashIndex = fileName != null ? fileName.indexOf("/") : Constants.NEGATIVE;
		String first = null;
		String second = null;
		DirEntryDetails dirEntryDetails = null;
		DirDetailsForWrite dirDetailsForWrite = null;
		
		if(slashIndex > 0) {
			second = fileName.substring(slashIndex+1);
			first = fileName.substring(0, slashIndex);
			
			dirEntryDetails = ((Directory)root).containDirEntryByName(first);
			//dirDetailsForWrite = new DirDetailsForWrite();
			//dirDetailsForWrite.setDirEntryDetails(dirEntryDetails);
			//dirDetailsForWrite.setRootIndex(rootIndex);
			
			return fileWithDirListingEntry(second, sector.get(dirEntryDetails.getLink()), dirEntryDetails.getLink());
		} else {
			dirEntryDetails = ((Directory)root).containDirEntryByName(fileName);
			
			dirDetailsForWrite = new DirDetailsForWrite();
			dirDetailsForWrite.setDirEntryDetails(dirEntryDetails);
			dirDetailsForWrite.setRootIndex(rootIndex);
			
			if(dirEntryDetails != null)
				return dirDetailsForWrite;
		}
		return null;
	}
	
	/**
	 * This method is invoked when a user issues a READ command on a file
	 * @param fileName
	 * @param numberOfBytes
	 * @return
	 */
	public Map<Integer, String> readData(String fileName, int numberOfBytes) {
		//Below variable will have sector index of directory which contain given file name in its directory listing entry
		DirDetailsForWrite dirDetailsForWrite = fileWithDirListingEntry(fileName, getRootDirectory(), 0); 
		DirEntryDetails dirEntryDetails = dirDetailsForWrite.getDirEntryDetails();
		
		Map<Integer, String> returnData = new HashMap<Integer, String>();
		
		String dataRead = recursiveReadUserFile(dirEntryDetails.getLink(), numberOfBytes, "");
		if(!dataRead.isEmpty()) {
			if(dataRead.length() < numberOfBytes) {
				returnData.put(0, dataRead.substring(0, dataRead.length()));
				returnData.put(1, Constants.END_OF_FILE_IS_REACHED);
			} else {
				returnData.put(0, dataRead.substring(0, numberOfBytes));
			}
			return returnData;
		}
		return null;
	}
	
	/**
	 * This method will read user file(s) recursively and display the data to user.
	 * @param fileIndex
	 * @param numberOfBytes
	 * @param dataRead
	 * @return
	 */
	private String recursiveReadUserFile(int fileIndex, int numberOfBytes, String dataRead) {
		Data data = (Data) sector.get(fileIndex);
		if(data != null && numberOfBytes > 0) {
			return data.getUserDataString() + (data.getFrwd() != 0 ? recursiveReadUserFile(data.getFrwd(), numberOfBytes-data.filledBytes(), dataRead) : "");
		} else {
			return "";
		}
	}
}

