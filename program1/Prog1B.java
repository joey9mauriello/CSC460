/*
 * Author: Joey Mauriello
 * Course: 460
 * Assignment: Program 1B
 * Instructor: McCann, Musa, Cox
 * Due Date: Jan 24th
 * 
 * Description:
 * This Java program reads in a binary file containing record. It then prints the first, middle, and last 3
 * crater names, diameters, depths, and ages. Then it prints the number of records in the bin file. Next, it
 * prints the information of the top 10 crater depths. Lastly, it takes input from the user and searches the bin
 * file for the name given, then it prints the information of the crater.
 * 
 * Language: Java 17.0
 * Input: /home/cs460/spring24/lunarcraters.bin
 */

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

public class Prog1B {

	
	public static void main(String[] args) throws IOException {
		String fileName = args[0]; // File path for bin input
		RandomAccessFile binFile = null; // Initialize the bin file
		
		binFile = readBin(fileName);
		
		int max1 = binFile.readInt(); // The max length of the first field
		int max2 = binFile.readInt(); // The max length of the second field

		int start = 8; // Starting place of the first record
		
		int recordLength = max1 + max2 + (8*8); // The byte length of each record
		long numberOfRecords = binFile.length() / recordLength;	 // The number of records in bin file
		
		// Check for empty bin file
		if (numberOfRecords == 0) {
			System.out.println("No Records!");
			return;
		}
	
		// Print first 3 records
		System.out.println("First Records:");
		for (int i = start; i < (recordLength*3)+8; i+= recordLength) {
			printRecord(binFile, i, recordLength);
		}
		
		// Print middle 3 or 4 records
		System.out.println("Middle Records:");
		// Check for even or odd number of records
		if (numberOfRecords % 2 == 0) {
			int middleStart = (int) ((numberOfRecords/2)-2) * recordLength + 8; // Starting byte address for the middle starting place
			
			// Check for invalid middleStart
			if (middleStart < 0) {
				middleStart = 8;
			}
			
			// Print middle 4 records
			for (int i = middleStart; i < middleStart + (4*recordLength); i += recordLength) {
				printRecord(binFile, i, recordLength);
			}
			
		}
		else {
			int middleStart = (int) (((numberOfRecords/2)-1) * recordLength + 8); // Starting byte address for the middle starting place
			
			// Check for invalid middleStart
			if (middleStart < 8) {
				middleStart = 8;
			}
			
			// Check middle 3 records
			for (int i = middleStart; i < middleStart + (3*recordLength); i += recordLength) {
				printRecord(binFile, i, recordLength);
			}
		}
		
		// Print last 3 records
		System.out.println("End Records:");
		for (int i = (int) (numberOfRecords*recordLength+8) - (recordLength*3); i < numberOfRecords*recordLength; i += recordLength) {
			if (i >= 0) {
				printRecord(binFile, i, recordLength);
			}
		}
		
		System.out.println("Number of records: " + numberOfRecords);
		
		
		ArrayList<Double> topTenDepth = new ArrayList<Double>(Collections.nCopies(20, Double.MIN_VALUE)); // List for top 10 depths
		ArrayList<Integer> topTenIndex = new ArrayList<Integer>(Collections.nCopies(20, -1)); // List for top 10 depth indexes

		getTopTen(topTenDepth, topTenIndex, binFile, numberOfRecords, recordLength);
		
		System.out.println("Top Ten Crater Depths:");
		
		int i; // Initialize index for top ten list printing
		// Print top 10 depths
		for (i = 0; i < 10 && i < numberOfRecords; i++) {
			printRecord(binFile, topTenIndex.get(i), recordLength);
		}
		// Print any ties
		while (Double.compare(topTenDepth.get(i-1),topTenDepth.get(i)) == 0) {
			printRecord(binFile, topTenIndex.get(i), recordLength);
			i++;
		}
		
		String name; // Initialize name variable for input
		int index = 0; // Initialize index variable for check
		Scanner scan = new Scanner(System.in); // Create scanner object for input
		while (true) {
			System.out.print("Enter a name (type 'e' or 'E' to exit): ");
			name = scan.nextLine();
			
			// Check for input to end
			if ("e".equalsIgnoreCase(name)) {
				System.out.println("Exiting");
				break;
			}
			
			index = printName(binFile, name, recordLength, numberOfRecords, max1);
			
			// Check for invalid search
			if (index == -1) {
				System.out.println("Name not in records!");
			}
			else {
				printRecord(binFile, index*recordLength + 8, recordLength);
			}
		}
		
		binFile.close();
	}

	/*
	 * printName(RandomAccessFile binFile, String name, int recordLength, long numberOfRecords, int max1) -- Stage 1 of the
	 * exponential binary search algorithm. Probes indexes to check whether to do binary search or not.
	 * 
	 * Pre-condition: The given binFile exists and the recordLength and numberOfRecords is accurately reflected. max1 is the maximum
	 * length of the first field.
	 * 
	 * Post-condition: Either the name was found and the index is returned or -1 is returned if the name can't be found.
	 * 
	 * Parameters: 
	 *  	binFile -- The binary file
	 *  	name -- The queried name
	 *  	recordLength -- The byte length of each record
	 *  	numberOfRecords -- The number of records in the bin file
	 *  	max1 -- The maximum length of the first field
	 *  
	 * Returns: An integer of either the index of the crater to be printed or -1
	 */
	private static int printName(RandomAccessFile binFile, String name, int recordLength, long numberOfRecords, int max1) throws IOException {
		
		// Check for and empty bin file
		if (numberOfRecords == 0) {
			System.out.println("No records available!");
			return -1;
		}
		
		// Probe each record index for a valid binary search option
		for (int i = 0; i < numberOfRecords; i++) {
			int newIndex = (int) (2*(Math.pow(2, i) - 1)); // Get the exponential index to check for a binary search
			
			// Check for a invalid exponential index
			if (newIndex >= numberOfRecords) {
				newIndex = (int) (numberOfRecords - 1);
			}
			
			// Read the name at the exponential index
			binFile.seek(newIndex * recordLength + 8);
			byte[] nameQuery = new byte[max1];
			binFile.readFully(nameQuery);
			
			// Check for a valid binary search
			if (newIndex >= numberOfRecords || new String(nameQuery).trim().compareTo(name) >= 0) {
				if (new String(nameQuery).trim().compareTo(name) == 0) {
					return newIndex;
				}
				return stage2(binFile, name, i, max1, recordLength, numberOfRecords);
			}
		}
		
		
		return -1;
	}

	/*
	 * stage2(RandomAccessFile binFile, String name, int i, int max1, int recordLength, long numberOfRecords) -- Stage 2 of the
	 * exponential binary search algorithm. Does binary search on the bin file based on the exponential index.
	 * 
	 * Pre-condition: The exponential index is valid and max1, recordLength, and numberOfRecords are accurate.
	 * 
	 * Post-condition: Either the name was found and the index is returned or -1 is returned if the name can't be found.
	 * 
	 * Parameters:
	 *  	binFile -- The binary file
	 *  	name -- The queried name
	 *  	i -- The exponential index
	 *  	max1 -- The maximum length of the first field
	 *  	recordLength -- The byte length of each record
	 *  	numberOfRecords -- The number of records in the bin file
	 *  
	 *  Returns: An integer of either the index of the crater to be printed or -1
	 */
	private static int stage2(RandomAccessFile binFile, String name, int i, int max1, int recordLength, long numberOfRecords) throws IOException {
		int start = (int) (2 * (Math.pow(2, i-1)-1))+1; // The starting place for binary search
		int end = (int) Math.min((2 * (Math.pow(2, i) - 1))-1, numberOfRecords-1); // The ending place for binary search
		
		while (start <= end) {
			int mid = start + (end - start)/2; // The middle point of start and end

			// Read the name at the index mid
			binFile.seek(mid * recordLength + 8);
			byte[] query = new byte[max1];
			binFile.readFully(query);
			String strQuery = new String(query).trim();
			
			// Check whether to go lower than the middle, higher than the middle, or return the correct index
			if (strQuery.compareTo(name) == 0) {
				return mid;
			}
			else if (strQuery.compareTo(name) < 0) {
				start = mid + 1;
			}
			else {
				end = mid - 1;
			}
		}
		
		return -1;
	}

	/*
	 * getTopTen(ArrayList<Double> topTenDepth, ArrayList<Integer> topTenIndex, RandomAccessFile binFile, long numberOfRecords, int recordLength) --
	 * Fills the topTenIndex and topTenDepth lists with the correct depths and indexes.
	 * 
	 * Pre-condition: The 2 array lists are filled with 0's and numberOfRecords and recordLength are acurate
	 * 
	 * Post-condition: The 2 array lists are filled with the accurate depths and indexes
	 * 
	 * Parameters:
	 *  	topTenDepth -- The list for the top 10 depths
	 *  	topTenIndex -- The list for the indexes of the top 10 depths
	 *  	binFile -- The binary file
	 *  	numberOfRecords -- The number of records in the bin file
	 *  	recordLength -- The byte length of each record
	 *  
	 * Returns: None
	 */
	private static void getTopTen(ArrayList<Double> topTenDepth, ArrayList<Integer> topTenIndex, RandomAccessFile binFile, long numberOfRecords, int recordLength) throws IOException {
		for (int i = 8; i < (numberOfRecords*recordLength)+8; i += recordLength) {
			binFile.seek(0);
			int max1 = binFile.readInt(); // Maximum length of the first field
			int max2 = binFile.readInt(); // Maximum length of the last field
			int depthOffset = max1 + (8*5); // The offset to add to reach the depth field
			double currDepth = readDepth(binFile, i, depthOffset); // The current depth at record index i
			
			// Check if current depth is in the top 10
			for (int j = 0; j < 20; j++) {
				if (currDepth > topTenDepth.get(j)) {
					topTenDepth.add(j, currDepth);
					topTenDepth.remove(20);
					
					topTenIndex.add(j, i);
					topTenIndex.remove(20);
					break;
				}
			}
		}
	}

	/*
	 * readDepth(RandomAccessFile binFile, int currOffset, int depthOffset) -- Reads the depth of the record at the currOffset
	 * and returns it.
	 * 
	 * Pre-condition: The depth offset correctly goes to the depth field and the currOffset starts at the beginning of a record
	 * 
	 * Post-condition: The depth has been read and returned
	 * 
	 * Parameters:
	 *  	binFile -- The binary file
	 *  	currOffset -- The starting position of the record to be read
	 *  	depthOffset -- The amount of bytes from the start of the record to the depth field
	 *  
	 *  Returns: A double of the depth read
	 */
	private static double readDepth(RandomAccessFile binFile, int currOffset, int depthOffset) throws IOException {
		binFile.seek(currOffset + depthOffset);
		return binFile.readDouble();
	}

	/*
	 * printRecord(RandomAccessFile binFile, int i, int recordLength) -- This method prints the name,
	 * diameter, depth, and age of a record at a given index.
	 * 
	 * Pre-condition: i is the correct byte location for the start of a record, and the recordLength is accurate
	 * 
	 * Post-condition: The record has been printed.
	 * 
	 * Parameters:
	 *  	binFile -- The binary file
	 *  	i -- The starting byte position of the record to be printed
	 *  	recordLength -- The byte length of each record
	 *  
	 * Returns: None
	 */
	private static void printRecord(RandomAccessFile binFile, int i, int recordLength) throws IOException {
		if (i >= binFile.length()) {
			return;
		}
		binFile.seek(0);
		int max1 = binFile.readInt(); // Maximum length of first field
		int max2 = binFile.readInt(); // Maximum length of last field
		
		byte[] name = new byte[max1]; // Byte for the crater name
		byte[] age = new byte[max2]; // Byte for the crater age
		
		binFile.seek(i);	
		binFile.readFully(name);
		String strName = new String(name); // A string for the name
		
		Double diameter = binFile.readDouble(); // Crater diameter
		
		binFile.seek(binFile.getFilePointer() + (4*8));
		Double depth = binFile.readDouble(); // Crater depth
		
		binFile.seek(binFile.getFilePointer() + (2*8));
		binFile.readFully(age);
		String strAge = new String(age); // A string for the age
		
		// Print the record
		if (strAge.trim() == "") {
			System.out.println("[" + strName.trim() + "][" + diameter + "][" + depth + "][null]");
		}
		else {
			System.out.println("[" + strName.trim() + "][" + diameter + "][" + depth + "][" + strAge.trim() + "]");
		}	
	}
	
	/*
	 * readBin(fileNme) -- This function reads the binary file to be looped through
	 * 
	 * Pre-condition: The file name exists as a binary file in the working directory
	 * 
	 * Post-condition: The file has been opened
	 * 
	 * Parameters:
	 *  	fileName -- The name of the binary file
	 *  
	 * Returns: None
	 */
	private static RandomAccessFile readBin(String fileName) throws FileNotFoundException {
		RandomAccessFile binFile = null;

		binFile = new RandomAccessFile(fileName, "r");
		return binFile;
	}
}
