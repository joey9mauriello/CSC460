/*
 * Author: Joey Mauriello
 * Course: CSC460
 * Assignment: Program 22
 * Instructor: McCann, Musa, Cox
 * Due Date: Feb 7th
 * 
 * Description:
 * This program reads in a bin file and an index file then queries the user for names.
 * It searches the index file to find the location of the record in the bin file and if it exists,
 * it prints information about that record from the bin file.
 * 
 * Language: Java 17.0
 * Input: lunarcraters.bin lhl.idx
 */

import java.io.*;
import java.util.*;

public class Prog22 {

	public static void main(String[] args) throws IOException {
		String indexName = args[0]; // File path for the index file
		String binName = args[1]; // File path for the bin file
		RandomAccessFile indexFile = new RandomAccessFile(indexName, "r"); // Reference to the index file
		RandomAccessFile binFile = new RandomAccessFile(binName, "r"); // Reference to the bin file
		
		int max1 = binFile.readInt(); // Maximum length of the first string in a record in bin file
		int max2 = binFile.readInt(); // Maximum length of the last string in a record in the bin file
		
		indexFile.seek(indexFile.length()-4);
		int h = indexFile.readInt(); // The h value that was used in the creation of the index file
		int numBuckets = (int) Math.pow(2, h+1); // The number of buckets in the index file
		
		String name; // Initialize name variable for input
		Scanner scan = new Scanner(System.in); // Create scanner object for input
		while (true) {
			System.out.print("Enter a name (type 'e' or 'E' to exit): ");
			name = scan.nextLine();
			
			// Check for input to end
			if ("e".equalsIgnoreCase(name)) {
				System.out.println("Exiting");
				break;
			}
			
			int hashCode = getHashCode(name, h); // The index of the bucket that the name should be in
			int binIndex = searchBucket(indexFile, name, hashCode, h, numBuckets, max1); // The location in the bin file of the desired record
			
			if (binIndex == -1) {
				System.out.println("The target value '" + name + "' was not found!");
			}
			else {
				printDetails(binFile, binIndex);
			}
		}

	}
	
	/*
	 * printDetails(RandomAccessFile binFile, int binIndex) -- This method prints the details of the record located at the binIndex.
	 * 
	 * Pre-condition: The binIndex is a valid location in the binFile
	 * 
	 * Post-condition: The details of the desired record have been printed
	 * 
	 * Parameters:
	 *  	binFile -- The binary file
	 *  	binIndex -- The location of the record in the bin file
	 *  
	 * Returns: None
	 */
	private static void printDetails(RandomAccessFile binFile, int binIndex) throws IOException {
		binFile.seek(0);
		int max1 = binFile.readInt(); // The maximum length of the first string in a record
		int max2 = binFile.readInt(); // The maximum length of the last string in a record
		
		binFile.seek(binIndex);
		
		byte[] name = new byte[max1]; // Byte for the crater name
		byte[] age = new byte[max2]; // Byte for the crater age
		
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
	 * searchBucket(RandomAccessFile indexFile, String name, int hashCode, int h, int numBuckets, int max1) -- This method searches the desired bucket
	 * for a name and determines if that name is in the index file or not.
	 * 
	 * Pre-condition: The hashCode is a valid location for the start of a bucket. The number of buckets is accurate.
	 * 
	 * Post-condition: The desired bucket has been fully searched for the name.
	 * 
	 * Parameters:
	 *  	indexFile -- The index file
	 *  	name -- The queried name
	 *  	hashCode -- The bucket index
	 *  	h -- The h value used in the equation to find the hash code
	 *  	numBuckets -- The number of buckets in the index file
	 *  	max1 -- The max length of the first string in a record
	 *  
	 * Returns: Either the location of the record in the bin file or -1 if the name was not found in the index file
	 */
	private static int searchBucket(RandomAccessFile indexFile, String name, int hashCode, int h, int numBuckets, int max1) throws IOException {
		int starting = hashCode * (25 * (4+max1)); // The starting location in the index file of the bucket to be searched
		for (int i = 0; i < 25; i++) {
			// For each entry in the bucket, check if the name is the same as the queried name
			int newIndex = starting + (i * (4+max1)); // The starting location in the index file of record i in the bucket
			indexFile.seek(newIndex);
			byte[] nameCheck = new byte[max1]; // Byte for the name to be read
			indexFile.readFully(nameCheck);
			String strNameCheck = new String(nameCheck); // String version of the name to be read
			if (strNameCheck.trim().equals(name)) {
				int binIndex = indexFile.readInt(); // The location in the bin file of the record
				return binIndex;
			}
			
		}
		return -1;
	}

	/*
	 * getHashCode(String strName, int h) -- This gets the hash code for the given string.
	 * 
	 * Pre-condition: The h value is accurate based on the number of buckets being used.
	 * 
	 * Post-condition: The hash code as been found
	 * 
	 * Parameters:
	 *  	strName -- The string of the name to be coded
	 *  	h -- The h value used for hash code equation
	 *  
	 * Returns: The hash code of the given string
	 */
	private static int getHashCode(String strName, int h) {
		int hash = (int) (Math.abs(strName.hashCode()) % (Math.pow(2, h+1)));
		return hash;
	}

}
