import java.io.*;
import java.nio.*;
import java.sql.*;
import java.util.*;


public class CreateTables {

	public static void main(String[] args) throws SQLException, FileNotFoundException, IOException {
		String oracleUrl = "jdbc:oracle:thin:@aloe.cs.arizona.edu:1521:oracle";
		String username = "jmauriello";
		String password = "a8434";
		
		
		
		
		try {
			Class.forName("oracle.jdbc.OracleDriver");
		} catch (ClassNotFoundException e) {
			System.err.println("*** ClassNotFoundException:  "
                    + "Error loading Oracle JDBC driver.  \n"
                    + "\tPerhaps the driver is not on the Classpath?");
                System.exit(-1);
		}
		
		
		
		Connection dbConnect = null;
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
		
		String[] tableNames = new String[] {"ghgp_2010", "ghgp_2014", "ghgp_2018", "ghgp_2022"};
		String[] fieldNames = new String[] {"fac_id", "fac_name", "city", "state", "zip", "address", "latitude", "longitude", "total_emissions", "co2_emissions", "ch4_emissions", "n2o_emissions", "combustion", "electricity"};
		String[] fieldTypes = new String[] {"integer", "varchar2 (200)", "varchar2 (200)", "char (2)", "number (5,0)", "varchar2 (200)", "number (10,5)", "number (10,5)", "number (10,2)", "number (10,2)", "number (10,2)", "number (10,2)", "number (10,2)", "number (10,2)"};
		
		clear(dbConnect);
		start(dbConnect, tableNames, fieldNames, fieldTypes);
		
		




		dbConnect.close();
		
		//test
		
		
	}
	
	private static void clear(Connection dbConnect) throws SQLException {
		checkTables(dbConnect, "drop table ghgp_2010");
		checkTables(dbConnect, "drop table ghgp_2014");
		checkTables(dbConnect, "drop table ghgp_2018");
		checkTables(dbConnect, "drop table ghgp_2022");
	}

	
	private static void start(Connection dbConnect, String[] tableNames, String[] fieldNames, String[] fieldTypes) throws SQLException, FileNotFoundException, IOException {
		createAll(dbConnect, tableNames, fieldNames, fieldTypes);
		
		for (String table : tableNames) {
			fillTable(dbConnect, table, fieldNames);
		}
		
	
	}
	
	private static void fillTable(Connection dbConnect, String table, String[] fieldNames) throws FileNotFoundException, SQLException, IOException {
		String fileName = table + ".csv";
		Scanner scan = new Scanner(new File(fileName));
		scan.useDelimiter("\n");
		scan.nextLine();
		while (scan.hasNextLine()) {
			String line = scan.nextLine();
			String[] record = line.concat(" ").split("\\|");
			String[] newRecord = editRecord(record);
			if (newRecord[0] != "") {
				String msg = getMsg(newRecord, table);
				
				checkTables(dbConnect, msg);
			}
		}
		scan.close();
	}
	
	private static String[] editRecord(String[] record) {
		String[] newRecord = new String[14];
		for (int i = 0; i < newRecord.length; i++) {
			newRecord[i] = record[i];
		}
		return newRecord;
	}
	
	private static String getMsg(String[] record, String table) {
		StringBuilder msg = new StringBuilder("insert into " + "jmauriello." + table + " values (");
		
		for (int i = 0; i < record.length; i++) {
			if (i != 0) {
				msg.append(", ");
			}
			//System.out.println(record[i].getClass());
			if (i == 1 || i == 2 || i == 3 || i == 5) {
				if (record[i].trim() == "") {
					msg.append("NULL");
				}
				else {
					if (record[i].contains("'")) {
						msg.append("'" + record[i].replace("'", "''") + "'");
					}
					else {
						msg.append("'" + record[i] + "'");
					}
					
				}
			}
			else {
				if (record[i].trim() == "") {
					msg.append("NULL");
				}
				else {
					msg.append(record[i]);
				}
			}
			
			//System.out.println(msg);

		}
		msg.append(")");
		return msg.toString();
		
	}

	private static void createAll(Connection dbConnect, String[] tableNames, String[] fieldNames, String[] fieldTypes) {
		
		try {
			for (String table : tableNames) {
				String sqlStmt = CreateTable(dbConnect, table, fieldNames, fieldTypes);
				
				
				
				Statement createStmt = dbConnect.createStatement();
					
				ResultSet createAnswer = createStmt.executeQuery(sqlStmt);
					
					
				if (createAnswer != null) {
					ResultSetMetaData answermetadata = createAnswer.getMetaData();
	
		            for (int i = 1; i <= answermetadata.getColumnCount(); i++) {
		            	System.out.print(answermetadata.getColumnName(i) + "\t");
		            }
		            System.out.println();
		                
		            while (createAnswer.next()) {
		                System.out.println(createAnswer.getString("TABLE_NAME"));
		            }
						
						
				}
				System.out.println();
					
				createStmt.close();
			}
				
			
		} catch (SQLException e) {
			System.err.println("*** SQLException:  "
	                   + "Could not fetch query results.");
	        System.err.println("\tMessage:   " + e.getMessage());
	        System.err.println("\tSQLState:  " + e.getSQLState());
	        System.err.println("\tErrorCode: " + e.getErrorCode());
	        System.exit(-1);
		}
			
		
	}

	private static void checkTables(Connection dbConnect, String query) throws SQLException {
		Statement stmt = dbConnect.createStatement();
		System.out.println(query);
		ResultSet answer = stmt.executeQuery(query);
//		ResultSetMetaData answermetadata = answer.getMetaData();
//		
//        for (int i = 1; i <= answermetadata.getColumnCount(); i++) {
//        	System.out.print(answermetadata.getColumnName(i) + "\t");
//        }
//        System.out.println();
//            
//        while (answer.next()) {
//            System.out.println(answer.getString("TABLE_NAME"));
//        }
        
        stmt.close();
	}

	private static String CreateTable(Connection dbConnect, String tableName, String[] fieldNames, String[] fieldTypes) {
		String sql = "create table " + "jmauriello." + tableName + " (";
		for (int i = 0; i < fieldNames.length; i++) {
			sql = sql + fieldNames[i] + " " + fieldTypes[i];
			if (i != fieldNames.length-1) {
				sql = sql + ", ";
			}
		}
		
		sql = sql + ")";
		
		return sql;
	}

}
