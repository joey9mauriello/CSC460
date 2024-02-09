/*
 * Author: Joey Mauriello
 * Course: CSC460
 * Assignment: Program 1A
 * Instructor: McCann, Musa, Cox
 * Due Date: Jan 17th
 * 
 * Description: 
 * This Java program reads a CSV file containing records, converts each record to binary format,
 * and creates a .bin file containing all records. The binary file stores records with specific
 * formatting for string and double values.
 * 
 * Language: Java 17.0
 * Input: /home/cs460/spring24/lunarcraters.csv
 */

import java.io.*;
import java.util.*;

public class Prog1A {
	
	public static void main(String[] args) {
		String fileName = args[0]; // File path for csv input
		ArrayList<List<String>> csv = null;	// List to hold all records from csv
		csv = readCsv(fileName);
		createBinFile(csv, fileName);
		//readBinFile(fileName);
		System.out.println("Done!");
	}

	/*
	 * readBinFile(String fileName) -- A testing method that reads the bin file, and prints the first name and number.
	 * 
	 * Pre-condition: The given fileName is just the name and exists in the current directory. The bin file
	 * is formatted correctly
	 * 
	 * Post-condition: The bin file is closed and the first name and number are correctly printed.
	 * 
	 * Paramters: fileName -- The name of the bin file.
	 * 
	 * Returns: None
	 */
	private static void readBinFile(String fileName) {
		RandomAccessFile binFile = null;	// Makes sure the random access file exists
		
		try {
			binFile = new RandomAccessFile(fileName.substring(0, fileName.length() - 5)+".bin","r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			int max1 = binFile.readInt();	// The max length of the first field
			int max2 = binFile.readInt();	// The max length of the last field
			byte[] name = new byte[max1];	// Byte array to contain the name
			binFile.readFully(name);
			System.out.println(new String(name));
			System.out.println(binFile.readDouble());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			binFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	/*
	 * createBinFile(ArrayList<List<String>> csv, String fileName) -- This method creates a bin file and writes the information
	 * in the csv list to the bin file.
	 * 
	 * Pre-condition: Csv contains records with 10 fields each, the file exists, and the first and last 
	 * field of each record is a string while the rest are doubles.
	 * 
	 * Post-condition: A bin file with the same name as fileName has been created and populated. Each
	 * record is the same byte length and the file is in the current directory.
	 * 
	 * Parameters:
	 *   - csv: The ArrayList of records from the CSV file.
     *   - fileName: The name of the file to be created.
	 * 
	 * Returns: None
	 */
	private static void createBinFile(ArrayList<List<String>> csv, String fileName) {
		int maxString1 = getMaxLen(csv, 0); 	// The maximum string length in the first field
		int maxString2 = getMaxLen(csv, 9); 	// The maximum string length in the last field
		
		RandomAccessFile binFile = null; 	// Allows the random access file to exist
		File fileRef = null;   // Allows the file reference to exist
		
		fileRef = new File(fileName.substring(0, fileName.length()-4) + ".bin");
		if (fileRef.exists()) {
            fileRef.delete();
        }
		
		try {
			binFile = new RandomAccessFile(fileRef,"rw");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			binFile.writeInt(maxString1);
			binFile.writeInt(maxString2);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		for (List<String> record : csv) {
			for (int i = 0; i < record.size(); i++) {
				if (i == 0) {
					stringToByte(record.get(i), binFile, maxString1);
				}
				else if (i == 9) {
					stringToByte(record.get(i), binFile, maxString2);
				}
				else {
					doubleToByte(Double.valueOf(record.get(i)), binFile);
				}
			}
		}
		
		
		

		try {
			binFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/*
	 * doubleToByte(Double valueOf, RandomAccessFile binFile) -- This method writes valueOf to the binFile
	 * 
	 * Pre-condition: The binFile is opened in write mode and valueOf is a double
	 * 
	 * Post-condition: valueOf has been written to the binFile
	 * 
	 * Parameters:
	 *  	- valueOf: The double to be written.
	 *  	- binFile: The bin file to be written to.
	 *  
	 * Returns: None
	 */
	private static void doubleToByte(Double valueOf, RandomAccessFile binFile) {
		try {
			binFile.writeDouble(valueOf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/*
	 * stringToByte(String string, RandomAccessFile binFile, int maxString) -- This method writes the string to the binFile
	 * 
	 * Pre-condition: The binFile is opened in write mode and maxString is the longest possible string
	 * 
	 * Post-condition: The string has been written to the binFile
	 * 
	 * Parameters:
	 *  	- string: The string to be written
	 *  	- binFile: The bin file to be written to
	 *  	- maxString: The maximum possible length of string the method will recieve
	 *  
	 * Returns: None
	 * 
	 */
	private static void stringToByte(String string, RandomAccessFile binFile, int maxString) {
		StringBuffer str = new StringBuffer(string); 	// String buffer to hold the string
		str.setLength(maxString);
		
		try {
			binFile.writeBytes(str.toString());
		} catch (IOException e) { 
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/*
	 * getMaxLen(ArrayList<List<String>> csv, int i) -- This method gets the maximum length string in the csv list
	 * in the ith field
	 * 
	 * Pre-condition: i is a valid index in the csv list and contains strings
	 * 
	 * Post-condition: The return value was the longest length of a string in the ith index
	 * 
	 * Parameters:
	 *  	- csv: The list containing records of strings
	 *  	- i: The index to get the longest string length from
	 *  
	 * Returns: An integer that is the longest string length
	 */
	private static int getMaxLen(ArrayList<List<String>> csv, int i) {
		int max = Integer.MIN_VALUE; 	// Place holder for the maximum length
		for (List<String> record : csv) {
			if (record.get(i).length() > max) {
				max = record.get(i).length();
			}
		}
		
		
		return max;
	}

	/*
	 * readCsv(String fileName) -- This method gets the records in the csv with the fileName
	 * 
	 * Pre-condition: The fileName exists as a csv file in the working directory
	 * 
	 * Post-condition: The csv file has been read into an array list
	 * 
	 * Parameters:
	 *  	- fileName: The name of the csv file to be read
	 * 
	 * Returns: An array list of lists containing records from the csv file
	 */
	private static ArrayList<List<String>> readCsv(String fileName) {
		ArrayList<List<String>> csv = new ArrayList<List<String>>(); 	// An array list to hold the records
		try {
			Scanner scan = new Scanner(new File(fileName)); 	// A scanner object to read the csv file
			scan.useDelimiter("\n");
			scan.next();
			while (scan.hasNext()) {
				ArrayList<String> record =  new ArrayList<>(Arrays.asList(scan.next().split(","))); 	// An array list to hold each field in a record
				if (record.size() == 9) {
					record.add("");
				}
				csv.add(record);
			}
		} catch (FileNotFoundException e) {
			System.out.println("Error: Couldn't find csv file.");
		}
		
		
		return csv;
	}
	
}