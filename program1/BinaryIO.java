/*
 * BinaryIO.java -- A demonstration of binary data input and output
 * using the RandomAccessFile (RAF) class in java.io.  Unlike most of the
 * Java file classes, RAF includes both input and output methods.
 *
 * Accompanying this program is its input file, 'binaryIO.csv'.  This program
 * reads lines of data from that file, and, using RAF methods, creates a binary
 * data file named 'binaryIO.bin' that contains the same content as the CSV
 * file, and then reads and displays what it just wrote.
 *
 * An aside:  Java includes a nice pair of classes for doing I/O of objects
 * (ObjectInputStream and ObjectOutputStream).  Why didn't I use them?
 * Simple:  They have to store more than just the content of the fields
 * to completely store an object.  In a database, we generally prefer
 * files of binary data that can be 'randomly' accessed; the excess junk
 * complicates access and wastes secondary storage.
 *
 * Speaking of random (aka 'direct') access files:  In order for the
 * records of data in such files to be randomly accessed, we need to ensure
 * that each record in a file is of a single predetermined size.  With this
 * knowledge, we can compute the location (the first byte) of any record
 * we need.  Primitive types (int, double, etc.) are all fixed-size,
 * but Strings are not.  Before writing String values, we have to pad them
 * to a suitable maximum length.  This program also demonstrates one way
 * to do that.
 *
 * Author:  L. McCann
 *
 * First Version: 2001-09-07.
 * Second Version: 2022-06-21: Expanded to more closely mimic all of the
 *     file actions students need to perform for the initial pair of CSc 460
 *     assignments.
 */

import java.io.*;
import java.util.*;

/*+----------------------------------------------------------------------
 ||
 ||  Class DataRecord 
 ||
 ||         Author:  L. McCann
 ||
 ||        Purpose:  An object of this class holds the field values of one
 ||                  record of data.  There are three fields:  State Code,
 ||                  Place Code, and County Name.  These are motivated by
 ||                  the Federal Information Processing System (FIPS)
 ||                  geographic encoding system.
 ||
 ||  Inherits From:  None.
 ||
 ||     Interfaces:  None.
 ||
 |+-----------------------------------------------------------------------
 ||
 ||      Constants:  RECORD_LENGTH -- the # of bytes required to hold
 ||                      the field values of a single record.  Because the
 ||                      data is pre-supplied, we can pre-compute the total.
 ||                  COUNTY_NAME_LENGTH -- Each county name in the data can
 ||                      have a different length.  Again, as this is a sample
 ||                      program with fixed data, we predefine the max length.
 ||
 |+-----------------------------------------------------------------------
 ||
 ||   Constructors:  Just the default constructor; no arguments.
 ||
 ||  Class Methods:  None.
 ||
 ||  Inst. Methods:     int getStateCode()
 ||                     int getPlaceCode()
 ||                  String getCountyName()
 ||                    void setStateCode(int newCode)
 ||                    void setPlaceCode(int newCode)
 ||                    void setCountyName(String newName)
 ||
 ||                    void dumpObject(RandomAccessFile stream)
 ||                    void fetchObject(RandomAccessFile stream)
 ||
 ++-----------------------------------------------------------------------*/

class DataRecord
{
                    // Class Constants

    private static final int COUNTY_NAME_LENGTH = 22;  // max length allowed
    public  static final int RECORD_LENGTH = 30;       // ints+string = 2(4)+22

                    // The data fields that comprise a record of our file

    private    int stateCode;    // the U.S. FIPS code for states
    private    int placeCode;    // the FIPS code for places
    private String countyName;   // of a county the place occupies

                    // 'Getters' for the data field values

    public     int getStateCode() { return(stateCode); }
    public     int getPlaceCode() { return(placeCode); }
    public  String getCountyName() { return(countyName); }

                    // 'Setters' for the data field values

    public    void setStateCode(int newCode) { stateCode = newCode; }
    public    void setPlaceCode(int newCode) { placeCode = newCode; }
    public    void setCountyName(String newName) { countyName = newName; }

       /*---------------------------------------------------------------------
        |  Method dumpObject(stream)
        |
        |  Purpose:  Writes the content of the object's fields
        |            to the file represented by the given RandomAccessFile
        |            object reference.  Primitive types (e.g., int)
        |            are written directly.  Non-fixed-size values
        |            (e.g., strings) are converted to the maximum allowed
        |            size before being written.  The result is a file of
        |            uniformly-sized records.  Text (i.e., strings) is
        |            written with just one byte per character, meaning that
        |            general Unicode text is not supported.
        |
        |  Pre-condition:  Fields have been populated, stream is writeable,
        |                  file pointer is positioned to new data's location.
        |
        |  Post-condition: Stream contains field data in sequence, file pointer
        |                  is left at the end of the written data.
        |
        |  Parameters:
        |      stream -- This is the stream object representing the data file
        |                to which the data is being written.
        |
        |  Returns:  None.
        *-------------------------------------------------------------------*/

    public void dumpObject(RandomAccessFile stream)
    {
        StringBuffer name = new StringBuffer(countyName);  // paddable name str

        try {
            stream.writeInt(stateCode);
            stream.writeInt(placeCode);
            name.setLength(COUNTY_NAME_LENGTH);  // pads to right with nulls
            stream.writeBytes(name.toString());  // only ASCII, not UNICODE
        } catch (IOException e) {
           System.out.println("I/O ERROR: Couldn't write to the file;\n\t"
                            + "perhaps the file system is full?");
           System.exit(-1);
        }
    }

       /*---------------------------------------------------------------------
        |  Method fetchObject(stream)
        |
        |  Purpose:  Read the content of the object's fields from the file
        |            represented by the given RandomAccessFile object 
        |            reference ('stream'), starting at the current file
        |            position.  Primitive types (e.g., int) are read directly.
        |            To create Strings containing text, because the file
        |            records have text stored to a maximum length with one byte
        |            per character, we can read a text field into a predefined
        |            array of bytes and use that array as a parameter
        |            to a String constructor.
        |
        |  Pre-condition:  Stream is readable, file pointer is positioned
        |                  to the record's first field's first byte.
        |
        |  Post-condition: Object fields are populated, file pointer
        |                  is left at the end of the read data
        |
        |  Parameters:
        |      stream -- This is the stream object representing the data file
        |                from which the data is being read.
        |
        |  Returns:  None.
        *-------------------------------------------------------------------*/

    public void fetchObject(RandomAccessFile stream)
    {
        byte[] ctyName = new byte[COUNTY_NAME_LENGTH];  // ASCII, not UNICODE

        try {
            stateCode = stream.readInt();
            placeCode = stream.readInt();
            stream.readFully(ctyName);        // reads all the bytes we need...
            countyName = new String(ctyName); // ...& makes a String of them
       } catch (IOException e) {
           System.out.println("I/O ERROR: Couldn't read from the file;\n\t"
                            + "perhaps it doesn't have the expected content?");
           System.exit(-1);
        }
    }
}


public class BinaryIO
{

    private static final String fileName = "C:\\\\Users\\\\swimd\\\\OneDrive\\\\Documents\\\\U OF A\\\\Spring 24\\\\460\\\\program1\\\\BinaryIO";  // no typing for user!

       /*---------------------------------------------------------------------
        |  Method readCSVFile (String fileName)
        |
        |  Purpose:  Opens the fileName.csv file, reads the lines of
        |            data, converts them to DataRecord objects, stores
        |            them into a ArrayList, closes the file before returning
        |            the collection.
        |
        |  Pre-condition:  The given fileName string is just the filename, with
        |                  no extension.  The file is in the current directory
        |                  and is readable.  The CSV file's content is
        |                  correctly structured (int,int,String) on each line.
        |                  
        |
        |  Post-condition: The file is closed.  The returned ArrayList has the
        |                  file's content in the same order as it was
        |                  in the file.
        |
        |  Parameters:
        |      fileName -- Just the file name of the CSV file, not the 
        |                  file extension.
        |
        |  Returns: An ArrayList of DataRecord objects, one per line.  
        *-------------------------------------------------------------------*/

            // SPECIAL NOTE:  Storing an entire file's content into
            // memory isn't generally a good idea; a file could well be
            // larger than available memory.  I'm doing it here for
            // clarity -- this method reads the CSV file;
            // the next method writes the data to the binary file.
            // In practice, it's better to read one record, write it,
            // and repeat for the rest of the file's content.
 
    private static ArrayList<DataRecord> readCSVFile (String fileName)
    {
        File fileRef = null;                     // provides exists() method
        BufferedReader reader = null;            // provides buffered text I/O
        ArrayList<DataRecord> csvContent = null; // list of record contents
        DataRecord currentRecord = null;         // next object to add to list

                    // If the CSV file doesn't exist, we can't proceed.

        try {
            fileRef = new File(fileName + ".csv");
            if (!fileRef.exists()) {
                System.out.println("PROBLEM:  The input file `binaryIO.csv' "
                                 + "does not exist in the current directory.");
                System.out.println("          Create or copy the file to the "
                                 + "current directory and try again.");
                System.exit(-1);
            }
        } catch (Exception e) {
            System.out.println("I/O ERROR: Something went wrong with the "
                             + "detection of the CSV input file.");
            System.exit(-1);
        }

                    // Read the content of the CSV file into an ArrayList
                    // of DataRecord objects.

        try {

            reader = new BufferedReader(new FileReader(fileRef));
            csvContent = new ArrayList<DataRecord>();

            String line = null;  // content of one line/record of the CSV file
            while((line = reader.readLine()) != null) {
                String[] field = line.split(",");
                currentRecord = new DataRecord();
                currentRecord.setStateCode(Integer.parseInt(field[0]));
                currentRecord.setPlaceCode(Integer.parseInt(field[1]));
                currentRecord.setCountyName(field[2]);
                csvContent.add(currentRecord);
            }

        } catch (IOException e) {
            System.out.println("I/O ERROR: Couldn't open, or couldn't read "
                             + "from, the CSV file.");
            System.exit(-1);
        }

                    // We're done reading the CSV file, time to close it up.

        try {
            reader.close();
        } catch (IOException e) {
            System.out.println("VERY STRANGE I/O ERROR: Couldn't close "
                             + "the CSV file!");
            System.exit(-1);
        }

        return csvContent;

    }  // readCSVFile()


       /*---------------------------------------------------------------------
        |  Method writeBinaryFile (fileName, csvContent)
        |
        |  Purpose:  Create and populate a binary file named 'fileName.bin'
        |            that contains the data from the supplied csvContent
        |            list in the same order.  The integers are stored
        |            as 4-byte ints.  The strings are stored as 22-byte
        |            character sequences, padded on the right with null
        |            characters as necessary to reach the desired length.
        |
        |  Pre-condition:  The given fileName string is just the filename, with
        |                  no extension.  The file is in the current directory.
        |
        |  Post-condition: The file is created in the current directory.
        |                  The file has the same content as the csvContent list.
        |                  The records all have the same 30-byte length.
        |
        |  Parameters:
        |      fileName -- file name only of the binary file, no extension 
        |      csvContent -- An ArrayList of DataRecord objects, containing
        |                    the data from the given CSV file.
        |
        |  Returns: None.
        *-------------------------------------------------------------------*/

    private static void writeBinaryFile (String fileName,
                                         ArrayList<DataRecord> csvContent)
    {
        File fileRef = null;              // provides exists(), delete()
        RandomAccessFile binFile = null;  // RAF specializes in binary file I/O

                    // If an old version of this binary file exists, delete it.
                    // We can overwrite an old file, but it's safer to delete
                    // and start fresh.

        try {
            fileRef = new File(fileName + ".bin");
            if (fileRef.exists()) {
                fileRef.delete();
            }
        } catch (Exception e) {
            System.out.println("I/O ERROR: Something went wrong with the "
                             + "deletion of the previous binary file.");
            System.exit(-1);
        }

                    // (Re)Create the binary file.  The mode cannot be just
                    // "w"; that's not an acceptable option to Java.

        try {
            binFile = new RandomAccessFile(fileRef,"rw");
        } catch (IOException e) {
            System.out.println("I/O ERROR: Something went wrong with the "
                             + "creation of the RandomAccessFile object.");
            System.exit(-1);
        }

                    // Ask the DataRecord objects to write themselves to the
                    // binary file, in the same order in which they were read.

        for (int i = 0; i < csvContent.size(); i++) {
            DataRecord r = csvContent.get(i);
            r.dumpObject(binFile);
        }

                    // Wrtiting is complete; close the binary file.

        try {
            binFile.close();
        } catch (IOException e) {
            System.out.println("VERY STRANGE I/O ERROR: Couldn't close "
                             + "the binary file!");
            System.exit(-1);
        }

    }  // writeBinaryFile()


       /*---------------------------------------------------------------------
        |  Method readBinaryFile(fileName)
        |
        |  Purpose:  Opens and reads the content of fileName.bin, and returns
        |            that content to the caller in the form of an ArrayList
        |            of DataRecord objects.
        |
        |  Pre-condition:  The file fileName.bin exists in the current
        |                  directory, is properly structured, and is readable.
        |
        |  Post-condition: The returned collection of records contains the
        |                  same data as the file does, and in the same order.
        |
        |  Parameters:
        |      fileName -- Just the file name of the CSV file, not the 
        |                  file extension.
        |
        |  Returns: An ArrayList of DataRecord objects.
        *-------------------------------------------------------------------*/
 
    private static ArrayList<DataRecord> readBinaryFile (String fileName)
    {
        RandomAccessFile binFile = null;  // RAF specializes in binary file I/O

                    // Open the binary file of data for reading.

        try {
            binFile = new RandomAccessFile(fileName+".bin","r");
        } catch (IOException e) {
            System.out.println("I/O ERROR: Something went wrong with the "
                             + "opening of the RandomAccessFile object.");
            System.exit(-1);
        }

                    // Determine how many records are in the binary file,
                    // so that we know how many to read.

        long numberOfRecords = 0;  // Quantity of records in the binary file

        try {
            numberOfRecords = binFile.length() / DataRecord.RECORD_LENGTH;
        } catch (IOException e) {
            System.out.println("I/O ERROR: Couldn't get the file's length.");
            System.exit(-1);
        }

                    // Move the file pointer (which marks the byte with which
                    // the next access will begin) to the front of the
                    // file (that is, to byte 0).  NOTE:  This happens
                    // automatically when a file is opened for reading, but
                    // I wanted this program to demonstrate seek().

        try {
            binFile.seek(0);
        } catch (IOException e) {
            System.out.println("I/O ERROR: Seems we can't reset the file "
                             + "pointer to the start of the file.");
            System.exit(-1);
        }

                    // Read the records from the binary file into an
                    // in-memory data structure, for return to the caller.

                                                // to hold binary file records
        ArrayList<DataRecord> binContent = new ArrayList<DataRecord>();

        for (int i = 0; i < numberOfRecords; i++) {
            DataRecord rec = new DataRecord(); // create object to hold record
            rec.fetchObject(binFile);
            binContent.add(rec);
        }

                    // Reading is complte; close the binary file. 

        try {
            binFile.close();
        } catch (IOException e) {
            System.out.println("VERY STRANGE I/O ERROR: Couldn't close "
                             + "the binary file after reading!");
            System.exit(-1);
        }

        return binContent;

    }  // readBinaryFile()


    public static void main (String [] args)
    {
        ArrayList<DataRecord> csvContent = null,  // the objects to write/read
                              binContent = null;  // content of the binary file

                    // Read the CSV file's content into memory, write the
                    // data records to the binary file, and then read the
                    // binary file's content.

        csvContent = readCSVFile(fileName);

        writeBinaryFile(fileName, csvContent);

        binContent = readBinaryFile(fileName);

                    // Display the content of the records to stdout.

        System.out.println("\nThere are " + binContent.size()
                         + " records in the file.\n");

        for (int i = 0; i < binContent.size(); i++) {
            DataRecord rec = binContent.get(i);
            System.out.println(" State code: " + rec.getStateCode());
            System.out.println(" Place code: " + rec.getPlaceCode());
            System.out.println("County Name: " + rec.getCountyName());
            System.out.println();
        }

    }  // main()

} // class BinaryIO
