/*
 * Author: Joey Mauriello
 * Course: CSC460
 * Assignment: Program 3
 * Instructor: McCann, Musa, Cox
 * Due Date: March 27th
 * 
 * Description: 
 * This Java program provides the user with 4 queries they can ask, then runs the desired query. It pulls from an oracle database
 * that was filled with data from 4 different csv files. Some queries require aditional information from the user and asks for it, 
 * then runs the query.
 * 
 * Language: Java 17.0
 */
import java.io.*;
import java.nio.*;
import java.sql.*;
import java.util.*;
import java.math.BigDecimal;

public class Prog3 {

	public static void main(String[] args) throws SQLException {
		Connection dbConnect = initOracle(); // Get the connection variable to create statements
		
		System.out.println("A: For each sequential pair of years (2010 and 2014, 2014 and 2018, and 2018 and 2022), by what percentage did the quantity of facilities increase or decrease?");
		System.out.println();
		System.out.println("B: Provide a year and 2 facility ids to compute the “great circle navigation” distance in nautical miles between their latitude/longitude coordinates");
		System.out.println();
		System.out.println("C: For each of the four years of data, produce a table of the top ten total reported direct emission facilities for that year, in descending order by total reported direct emissions.");
		System.out.println();
		System.out.println("D: Provide a facility id to compute the average total reported direct emissions over each of the four years of data");
		System.out.println();
		
		
		String[] tableNames = new String[] {"jmauriello.ghgp_2010", "jmauriello.ghgp_2014", "jmauriello.ghgp_2018", "jmauriello.ghgp_2022"}; // List of table names
		String[] fieldNames = new String[] {"fac_id", "fac_name", "city", "state", "zip", "address", "latitude", "longitude", "total_emissions", "co2_emissions", "ch4_emissions", "n2o_emissions", "combustion", "electricity"}; // List of field names in each table
		String[] fieldTypes = new String[] {"integer", "varchar2 (200)", "varchar2 (200)", "char (2)", "number (5,0)", "varchar2 (200)", "number (10,5)", "number (10,5)", "number (10,2)", "number (10,2)", "number (10,2)", "number (10,2)", "number (10,2)", "number (10,2)"}; // List of field types for each table
		
		Scanner scan = new Scanner(System.in); // Scanner to get user input
		String input; // Input string to hold user input
		while (true) {
			System.out.println("Enter a query option (type 'e' or 'E' to exit): ");
			input = scan.nextLine();
			
			
			if ("e".equalsIgnoreCase(input)) {
				System.out.println("Exiting");
				break;
			}
			
			if ("a".equalsIgnoreCase(input)) {
				queryA(dbConnect, tableNames);
			}
			else if ("b".equalsIgnoreCase(input)) {
				System.out.println("Please enter a year:");
				String year = scan.nextLine(); // Desired year
				System.out.println("Please enter first facility id:");
				String id1 = scan.nextLine(); // Desired first id
				System.out.println("Please enter second facility id:");
				String id2 = scan.nextLine(); // Desired second id
				queryB(dbConnect, tableNames, year, id1, id2);
			}
			else if ("c".equalsIgnoreCase(input)) {
				queryC(dbConnect, tableNames);
			}
			else if ("d".equalsIgnoreCase(input)) {
				System.out.println("Please enter a facility id:");
				String id = scan.nextLine(); // Desired id
				queryD(dbConnect, tableNames, id);
			}
			else {
				System.out.println("Not a valid option!");
			}
			
		}
		
		dbConnect.close();
		

	}
	
	/**
	 * queryD(Connection dbConnect, String[] tableNames, String id) -- This function finds the average total emissions of the
	 * desired facility based on the given facility id.
	 * 
	 * Pre-Condition: There is a valid connection.
	 * 
	 * Post-Condition: Either the average total emissions has been found and printed or an error message is printed if there is 
	 * an invalid id
	 * 
	 * Parameters:
	 *   - dbConnect: The oracle connection
	 *   - tableNames: The list of table names
	 *   - id: The desired facility id
	 *   
	 * Returns: None
	 * 
	 */
	private static void queryD(Connection dbConnect, String[] tableNames, String id) {
		BigDecimal total = new BigDecimal(0); // Counter for total years
		BigDecimal total_emissions = new BigDecimal(0); // Counter for total emissions
		for (String table : tableNames) {
			try {
				
				String countQuery = "select count(*) from " + table + " where fac_id = " + id; // Query to check if id is valid
				Statement stmt = dbConnect.createStatement(); // Statement to check if id is valid
				ResultSet answer = stmt.executeQuery(countQuery); // Answer to check if id is valid
				
				answer.next();
				int count = answer.getInt("COUNT(*)"); // Number of times the desired id occurs in the table
				
				if (count == 0) {
					System.out.println("Id not in " + table);
					continue;
				}
				
				total = total.add(new BigDecimal(1));
				
				stmt = dbConnect.createStatement();
				String query = "select total_emissions from " + table + " where fac_id = " + id; // Query to find total emissions
				answer = stmt.executeQuery(query);
				answer.next();
				BigDecimal emissions = new BigDecimal(answer.getString("total_emissions")); // Number of emissions for current table
				total_emissions = total_emissions.add(emissions);
				
				
				stmt.close();
				
			} catch (SQLException e) {
				System.err.println("*** SQLException:  "
		                   + "Could not fetch query results.");
		        System.err.println("\tMessage:   " + e.getMessage());
		        System.err.println("\tSQLState:  " + e.getSQLState());
		        System.err.println("\tErrorCode: " + e.getErrorCode());
		        System.exit(-1);
			}
		}
		
		BigDecimal average = total_emissions.divide(total); // Average emissions for given id
		System.out.println(average);
	}
	
	/**
	 * queryC(Connection dbConnect, String[] tableNames) -- This function finds and prints the tables of the facilities with the 
	 * top 10 total emissions for each of the 4 tables.
	 * 
	 * Pre-Condition: There is a valid connection.
	 * 
	 * Post-Condition: All 4 tables have been printed
	 * 
	 * Parameters:
	 *   - dbConnect: The oracle connection
	 *   - tableNames: The list of table names
	 *   
	 * Returns: None
	 */
	private static void queryC(Connection dbConnect, String[] tableNames) {
		for (String table : tableNames) {
			printTable(dbConnect, table);
			System.out.println();
		}
	}
	
	/**
	 * printTable(Connection dbConnect, String table) -- This function prints a table consisting of facility names, states, and total
	 * emissions for the facilities that have the top 10 highest total emissions in the given table.
	 * 
	 * Pre-Condition: There is a valid connection and the table exists in oracle.
	 * 
	 * Post-Condition: A table has been printed
	 * 
	 * Parameters:
	 *   - dbConnect: The oracle connection
	 *   - table: The desired table to be printed from
	 * 
	 * Returns: None
	 */
	private static void printTable(Connection dbConnect, String table) {
		String query = "select fac_name, state, total_emissions from " + table + " order by total_emissions desc"; // Query to get the facility info based on top 10 total emissions
		
		try {
			Statement stmt = dbConnect.createStatement(); // Statement to get top 10 table
			ResultSet answer = stmt.executeQuery(query); // Answer to get top 10 table
			
			ResultSetMetaData answermetadata = answer.getMetaData(); // Column info for top 10 table
			
	        for (int i = 1; i <= answermetadata.getColumnCount(); i++) {
	        	System.out.print(answermetadata.getColumnName(i) + "\t");
	        }
	        System.out.println();
	        
	        for (int i = 0; i < 10; i++) {
	        	answer.next();
	        	System.out.println(answer.getString("fac_name").trim() + "\t" + answer.getString("state") + "\t" + new BigDecimal(answer.getString("total_emissions")));
	        }
	        
	        stmt.close();
		} catch (SQLException e) {
			System.err.println("*** SQLException:  "
	                   + "Could not fetch query results.");
	        System.err.println("\tMessage:   " + e.getMessage());
	        System.err.println("\tSQLState:  " + e.getSQLState());
	        System.err.println("\tErrorCode: " + e.getErrorCode());
	        System.exit(-1);
		}
		
		
	}
	
	/*
	 * queryB(Connection dbConnect, String[] tableNames, String year, String id1, String id2) -- This function finds the great circle navigation
	 * distance in nautical miles between the 2 facilities with the given ids in a given year.
	 * 
	 * Pre-Condition: There is a valid connection.
	 * 
	 * Post-Condition: Either the great circle navigation distance is printed or an error is printed if an invalid year/id is given.
	 * 
	 * Parameters:
	 *   - dbConnect: The oracle connection
	 *   - tableNames: The list of table names
	 *   - year: The desired year
	 *   - id1: The id of the first facilty
	 *   - id2: The id of the second facility
	 */
	private static void queryB(Connection dbConnect, String[] tableNames, String year, String id1, String id2) {
		String table = null; // The name of the correct table based on given year
		for (String tbl : tableNames) {
			if (tbl.contains(year)) {
				table = tbl;
			}
		}
		if (table == null) {
			System.out.println("Invalid year!");
			return;
		}
		
		try {
			Statement stmt1 = dbConnect.createStatement(); // Statement to check the first id
			Statement stmt2 = dbConnect.createStatement(); // Statement to check the second id
			String count1Query = "select count(*) from " + table + " where fac_id = " + id1; // Query to check count of first id
			String count2Query = "select count(*) from " + table + " where fac_id = " + id2; // Query to check count of second id
			
			ResultSet count1Answer = stmt1.executeQuery(count1Query); // Answer to check count of first id
			ResultSet count2Answer = stmt2.executeQuery(count2Query); // Answer to check count of second id
			
			count1Answer.next();
			count2Answer.next();

			int count1 = count1Answer.getInt("COUNT(*)"); // Number of times first id occurs in desired table
			int count2 = count2Answer.getInt("COUNT(*)"); // Number of times second id occurs in desired table
			
			if (count1 == 0) {
				System.out.println("Invalid first id!");
				stmt1.close();
				stmt2.close();
				return;
			}
			else if (count2 == 0) {
				System.out.println("Invalid second id!");
				stmt1.close();
				stmt2.close();
				return;
			}
			
			
			
			double lat1 = getLoc(stmt1, table, id1, "latitude"); // Latitude of first facility
			double lon1 = Math.abs(getLoc(stmt1, table, id1, "longitude")); // Longitude of first facility
			double lat2 = getLoc(stmt2, table, id2, "latitude"); // Latitude of second facility
			double lon2 = Math.abs(getLoc(stmt2, table, id2, "longitude")); // Longitude of second facility
					
			double distance = (Math.acos(Math.sin(lat1)*Math.sin(lat2)+Math.cos(lat1)*Math.cos(lat2)*Math.cos(lon1-lon2)) * 60 ); // Distance between the two locations
			
			System.out.println("The great circle navigation distance in nautical miles is: "  + Double.valueOf(distance));
					
			stmt1.close();
			stmt2.close();
			
		} catch (SQLException e) {
			System.err.println("*** SQLException:  "
	                   + "Could not fetch query results.");
	        System.err.println("\tMessage:   " + e.getMessage());
	        System.err.println("\tSQLState:  " + e.getSQLState());
	        System.err.println("\tErrorCode: " + e.getErrorCode());
	        System.exit(-1);
		}
		
	}
	
	/*
	 * getLoc(Statement stmt, String table, String id, String location) -- This function gets either the latitude or longitude
	 * of the facility with the given id in the given table
	 * 
	 * Pre-Conditions: The id is valid and the locaion is either "latitude" or "longitude"
	 * 
	 * Post-Conditions: The desired location has been found
	 * 
	 * Parameters:
	 *   - stmt: The statement use to execute the query
	 *   - table: The desired table to grab from
	 *   - id: The location of the desired facility
	 *   - location: Either "latitude" or "longitude"
	 *   
	 * Returns: Either the latitiude or longitude
	 */
	private static double getLoc(Statement stmt, String table, String id, String location) {
		try {
			String query = "select " + location + " from " + table + " where fac_id = " + id; // Query to get location
			ResultSet answer = stmt.executeQuery(query); // Answer to get location
			
			answer.next();
			
			return answer.getDouble(location);
			
		} catch (SQLException e) {
			System.err.println("*** SQLException:  "
	                   + "Could not fetch query results.");
	        System.err.println("\tMessage:   " + e.getMessage());
	        System.err.println("\tSQLState:  " + e.getSQLState());
	        System.err.println("\tErrorCode: " + e.getErrorCode());
	        System.exit(-1);
		}
		
		return -1.0;
	}
	
	/*
	 * queryA(Connection dbConnect, String[] tableNames) -- This function prints the percent increase/decrease
	 * between all 4 years
	 * 
	 * Pre-Condition: There is a valid connection
	 * 
	 * Post-Condition: The percent change has been printed
	 * 
	 * Parameters:
	 *   - dbConnect: The oracle connection
	 *   - tableNames: The list of table names
	 *   
	 * Returns: None
	 */
	private static void queryA(Connection dbConnect, String[] tableNames) {
		for (int i = 0; i < tableNames.length-1; i++) {
			String table1Name = tableNames[i]; // Name of the first table
			String table2Name = tableNames[i+1]; // Name of the second table
			
			int table1Count = getCount(dbConnect, table1Name); // Number of facilities in first table
			int table2Count = getCount(dbConnect, table2Name); // Number of facilities in second table
			
			boolean increase = true; // Whether it is an increase from first to second table
			float percent; // What percent increase/decrease it is
			
			if (table1Count < table2Count) {
				int diff = table2Count - table1Count; // Difference between first and second table
				percent = (float) diff / table1Count;
			}
			else {
				increase = false;
				int diff = table1Count - table2Count; // Difference between first and second table
				percent = (float) diff / table1Count;
			}
			
			percent = (float) (Math.round(percent * 10000.00) / 100.00);
			
			System.out.println(percent + "% " + ((increase) ? "increase" : "decrease") + " from " + table1Name.substring(5) + " to " + table2Name.substring(5));
			
		}
	}
	
	/*
	 * getCount(Connection dbConnect, String tableName) -- This function gets the number of facilities in the given table
	 * 
	 * Pre-Conditions: There is a valid connection
	 * 
	 * Post-Conditions: The table count has been found
	 * 
	 * Parameters:
	 *   - dbConnect: The oracle connection
	 *   - tableName: The name of the desired table
	 *   
	 * Returns: An integer of the number of facilities in the table
	 */
	private static int getCount(Connection dbConnect, String tableName) {
		try {
			Statement stmt = dbConnect.createStatement(); // Statement to get count
			String query = "select count(*) from " + tableName; // Query to get count
			ResultSet answer = stmt.executeQuery(query); // Answer to get count
			
	        answer.next();
	        return answer.getInt("COUNT(*)");
		} catch (SQLException e) {
			System.err.println("*** SQLException:  "
	                   + "Could not fetch query results.");
	        System.err.println("\tMessage:   " + e.getMessage());
	        System.err.println("\tSQLState:  " + e.getSQLState());
	        System.err.println("\tErrorCode: " + e.getErrorCode());
	        System.exit(-1);
		}
		return -1;
	}
	
	/*
	 * initOracle() -- This funciton initiallizes a connection to oracle
	 * 
	 * Pre-Conditions: The oracle account is correct
	 * 
	 * Post-Conditions: Oracle has been connected to
	 * 
	 * Parameters: None
	 * 
	 * Returns: The connection to oracle
	 */
	private static Connection initOracle() {
		String oracleUrl = "jdbc:oracle:thin:@aloe.cs.arizona.edu:1521:oracle"; // The url to connect to oracle
		String username = "jmauriello"; // Oracle username
		String password = "a8434"; // Oracle password

		
		try {
			Class.forName("oracle.jdbc.OracleDriver");
		} catch (ClassNotFoundException e) {
			System.err.println("*** ClassNotFoundException:  "
                    + "Error loading Oracle JDBC driver.  \n"
                    + "\tPerhaps the driver is not on the Classpath?");
                System.exit(-1);
		}
		
		
		
		Connection dbConnect = null; // Connection to oracle
		try {
			dbConnect = DriverManager.getConnection(oracleUrl, username, password);
		} catch (SQLException e) {
			System.err.println("*** SQLException:  "
                    + "Could not open JDBC connection.");
                System.err.println("\tMessage:   " + e.getMessage());
                System.err.println("\tSQLState:  " + e.getSQLState());
                System.err.println("\tErrorCode: " + e.getErrorCode());
                System.exit(-1);
		}
		
		return dbConnect;
		
	}

}
