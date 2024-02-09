/*
 * Author: Joey Mauriello
 * Course: CSC460
 * Assignment: Program 21
 * Instructor: McCann, Musa, Cox
 * Due Date: Feb 7th
 * 
 * Description:
 * This Java program reads in a bin file that contains records, it then creates an
 * index file with buckets of 25 records. The hashable index file contains a name and the
 * location of that record in the bin file.
 * 
 * Language: Java 17.0
 * Input: lunarcraters.bin
 */

import java.io.*;
import java.util.*;
import java.lang.*;

public class Prog21 {

	public static void main(String[] args) throws IOException {
		String fileName = args[0]; // File path for bin file
		RandomAccessFile binFile = new RandomAccessFile(fileName, "r"); // Get the binfile from the working directory
		RandomAccessFile indexFile = null;// Initialize the index file

		
		int max1 = binFile.readInt(); // The max length of the first string
		int max2 = binFile.readInt(); // The max length of the last string
		
		int numBuckets = 2; // The starting number of buckets in the index file
		int H = 0; // Starting H value for the hash equation
		indexFile = createIndex(indexFile, max1, numBuckets);
		
		int update = fillIndexFile(binFile, indexFile, max1, max2, H); // An integer determining if a bucket is full while trying to fill index file
		while (update == 1) {
			// Reaches here if a bucket is full and additional buckets needed
			H = H + 1;
			numBuckets = numBuckets*2;
			indexFile = createIndex(indexFile, max1, numBuckets);
			update = fillIndexFile(binFile, indexFile, max1, max2, H);
		}
		
		indexFile.seek(indexFile.length());
		indexFile.writeInt(H);

		
		System.out.println("Number of Buckets: " + numBuckets);
		printOthers(indexFile, numBuckets, max1);
		
		
		indexFile.close();
		binFile.close();
		
		
	}

	/*
	 * printOthers(RandomAccessFile indexFile, int numBuckets, int max1) -- Prints the lowest, highest, and average number
	 * of records in a bucket.
	 * 
	 * Pre-condition: The indexFile accurately has the same number of buckets as numBuckets. The index file contains entries.
	 * 
	 * Post-condition: The needed information has been printed.
	 * 
	 * Paramters:
	 *  	indexFile -- The index file
	 *  	numBuckets -- The number of buckets in the index file
	 *  	max1 -- The maximum length of the first string
	 *  
	 * Returns: None
	 */
	private static void printOthers(RandomAccessFile indexFile, int numBuckets, int max1) throws IOException {
		int min = Integer.MAX_VALUE; // Placeholder for minimun number of records in a bucket
		int max = Integer.MIN_VALUE; // Placeholder for maximum number of records in a bucket
		double total = 0; // Total number of records in index file
		
		// For each bucket, check if holding min or max records
		for (int bucket = 0; bucket < numBuckets; bucket++) {
			int starting = bucket * (25 * (max1+4)); // Starting location of bucket i
			int records = getRecordCount(indexFile, starting, max1); // Number of records in bucket i
			
			if (records > max) {
				max = records;
			}
			if (records < min) {
				min = records;
			}
			total += records;
		}
		
		System.out.println("Lowest number of records in a bucket: " + min);
		System.out.println("Highest number of records in a bucket: " + max);
		System.out.println("Average number of records in a bucket: " + total/numBuckets);
		
		
	}

	/*
	 * getRecordCount(RandomAccessFile indexFile, int starting, int max1) -- Gets the number of records in the bucket at the
	 * starting location.
	 * 
	 * Pre-condition: The index file has a bucket at the given location.
	 * 
	 * Post-condition: The number of records in the bucket has been found.
	 * 
	 * Parameters:
	 *  	indexFile -- The index file
	 *  	starting -- The starting location of the bucket to be searched
	 *  	max1 -- The maximum length of the first string
	 * Returns: An integer of how many records were in the bucket
	 */
	private static int getRecordCount(RandomAccessFile indexFile, int starting, int max1) throws IOException {
		int retval = 0; // Placeholder for record count
		indexFile.seek(starting);
		
		for(int i = 0; i < 25; i++) {
			indexFile.seek(starting + (i*(max1+4)));
			byte[] name = new byte[max1]; // Byte to read the name of the record
			indexFile.readFully(name);
			String strName = new String(name); // String version of the name in record
			
			// Check if string contains a name
			if (strName.trim() != "") {
				retval++;
			}
		}
		return retval;
	}

	/*
	 * fillIndexFile(RandomAccessFile binFile, RandomAccessFile indexFile, int max1, int max2, int h) -- This method fills
	 * the index file with each name and binFile location of each record in the bin file.
	 * 
	 * Pre-condition: The bin file exists and contains records to fill the index file with.
	 * 
	 * Post-condition: The index file is either filled with all of the records from the bin file, or 1 has been returned to indicate
	 * this method should be run again.
	 * 
	 * Parameters:
	 *  	binFile -- The binary file
	 *  	indexFile -- The index file
	 *  	max1 -- The max length of the first string in each record of the bin file
	 *  	max2 -- The max length of the last string in each record of the bin file
	 *  	h -- The h value to be used in the hashing equation
	 *  
	 * Returns: An integer indicating whether any bucket is completely full or not.
	 */
	private static int fillIndexFile(RandomAccessFile binFile, RandomAccessFile indexFile, int max1, int max2, int h) throws IOException {
		binFile.seek(0);
		max1 = binFile.readInt();
		max2 = binFile.readInt();
		
		
		
		int recordLength = max1 + max2 + (8*8); // The byte length of each record
		long numberOfRecords = binFile.length() / recordLength; // The number of records in the bin file
		
		for (int i = 0; i < numberOfRecords; i++) {
			int binIndex = 8 + (i*recordLength); // The location in the bin file of record i
			
			// Read the first string in record
			binFile.seek(binIndex);
			byte[] name = new byte[max1]; // Byte to read the name of the record
			binFile.readFully(name);
			String strName = new String(name); // String version of the name in record
			
			int hashIndex = getHashCode(strName.trim(), h); // The hashed index for which bucket record will go into
			int binHashIndex = hashIndex * (25*(4+max1)); // The location in the index file of the start of the desired bucket

			int update = writeToIndex(strName, binIndex, binHashIndex, indexFile, max1); // Indicate whether a bucket was completely filled up
			
			if (update == 1) {
				return 1;
			}
			
		}
		
		return 0;
	}

	/*
	 * writeToIndex(String strName, int binIndex, int binHashIndex, RandomAccessFile indexFile, int max1) -- This method writes a name and location to
	 * the index file in the desired bucket.
	 * 
	 * Pre-condition: The binHashIndex is for the correct bucket that goes with the string and the binIndex is accurate in the bin file.
	 * 
	 * Post-condition: Either the name and location have been written to the index file or 1 was returned to indicate a bucket overflow.
	 * 
	 * Parameters:
	 *  	strName -- The name to be written
	 *  	binIndex -- The location in the bin file of the name
	 *  	binHashIndex -- The location in the index file of the starting place of the desired bucket
	 *  	indexFile -- The index file
	 *  	max1 -- The max length of first string in the bin file
	 *  
	 * Returns: An integer indicating whether any bucket is overflowed
	 */
	private static int writeToIndex(String strName, int binIndex, int binHashIndex, RandomAccessFile indexFile, int max1) throws IOException {
		indexFile.seek(binHashIndex);
		
		
		int i; // Counting variable for each record in the bucket
		for (i = 0; i < 25; i++) {
			int currBinIndex = binHashIndex + (i*(4+max1)); // The current location within the index file of record i
			indexFile.seek(currBinIndex);
			byte[] nameCheck = new byte[max1]; // The byte to hold the name of record i
			indexFile.readFully(nameCheck);
			String strNameCheck = new String(nameCheck); // The string version of the name of record i
			
			if (strNameCheck.trim() == "") {
				break;
			}
			
		}
		
		// Check for overflow
		if (i == 25) {
			return 1;
		}
		
		// If no overflow, write to index file
		indexFile.seek(binHashIndex + (i*(4+max1)));
		
		StringBuffer name = new StringBuffer(strName); // A string buffer for the name to be written
		name.setLength(max1);
		indexFile.writeBytes(name.toString());
		indexFile.writeInt(binIndex);

		indexFile.seek(binHashIndex);
		
		return 0;
		
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
		int hash = (int) (Math.abs(strName.hashCode()) % (Math.pow(2, h+1))); // The hash code
		return hash;
	}

	/*
	 * createIndex(RandomAccessFile indexFile, int max1, int numBuckets) -- This creates an index file with the given amount of buckets.
	 * 
	 * Pre-condition: The number of buckets is accurate.
	 * 
	 * Post-condition: The index file is created with empty 25 empty records per bucket for as many buckets as desired.
	 * 
	 * Parameters:
	 *  	indexFile -- The index file
	 *  	max1 -- The max length of the first string in the bin file
	 *  	numBuckets -- The number of desired buckets to be put in the index file
	 *  
	 *  Returns: The random access file reference to the index file
	 */
	private static RandomAccessFile createIndex(RandomAccessFile indexFile, int max1, int numBuckets) throws IOException {

		File fileRef = null; // File reference for index file
		
		
		fileRef = new File("lhl.idx");
		if (fileRef.exists()) {
            fileRef.delete();
        }
		
		indexFile = new RandomAccessFile(fileRef, "rw");
		
		for (int i = 0; i < 25*numBuckets; i++) {
			StringBuffer name = new StringBuffer(); // String buffer for name to be written
			name.setLength(max1);
			indexFile.writeBytes(name.toString());
			
			indexFile.writeInt(-1);
		}
		
		return indexFile;
	}



}
