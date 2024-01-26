package com.si.coredata.reportcontroller.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CoreDataProcessor {

	static final Logger LOGGER = Logger.getLogger(CoreDataProcessor.class.getName());

	@Value("${jdbc.driver.class}")
	String jdbcDriver;

	@Value("${jdbc.sql.url}")
	String jdbcURL;

	@Value("${data.normalisechars.tokeep}")
	String normaliseChars;

	@Value("${databasetype}")
	String databsetype;
	


	public String stackTrace(Exception e) {

		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String exceptionAsString = sw.toString();
		return exceptionAsString;
	}

	public static String escapeValueForDatabase(String component, String databaseType) {
		if (databaseType.equalsIgnoreCase("mysql")) {
			return "`" + component + "`";
		} else if (databaseType.equalsIgnoreCase("sqlserver")) {
			return "[" + component + "]";
		} else if (databaseType.equalsIgnoreCase("postgresql")) {
			return "\"" + component + "\"";
		} 
		else if (databaseType.equalsIgnoreCase("h2")) {
	        return "\"" + component + "\""; // H2 uses double quotes for identifier escaping
	    }else {
			return component;
		}
	}

	public void process(String[] dataLines, String tableName) {

		try {

			tableName = escapeValueForDatabase(tableName, databsetype);

			Class.forName(jdbcDriver).newInstance();
			LOGGER.info("JDBC driver loaded");

		} catch (Exception err) {
			LOGGER.severe(" Error while loding driver " + stackTrace(err));
		}

		Connection databaseConnection = null;
		try {
			// Connect to the database
			databaseConnection = DriverManager.getConnection(jdbcURL);

			LOGGER.info("Connected to the database");
		} catch (SQLException err) {
			LOGGER.severe("Error connecting to the database" + stackTrace(err));
		}

		try {

			String columns = dataLines[0];
			columns = columns.replaceAll("[^\\\\.A-Za-z0-9|]", "");
			columns = columns.replace(".", "_");
			columns = columns.replace("|", ",");
			int columnsCount = columns.split(",").length;
			columns = formatColumns(columns);

			if (createTable(databaseConnection, columns, columnsCount, tableName))
				formatAndInsertData(databaseConnection, columns, columnsCount, dataLines, tableName);

			// close the database connection
			databaseConnection.close();
		} catch (SQLException err) {
			LOGGER.severe(stackTrace(err));
		}
		LOGGER.info("Program finished");
	}

	public String formatColumns(String columnString) {
		List<String> columnNames = Arrays.asList(columnString.split(","));

		Map<String, Integer> columnCounts = new HashMap<>();
		List<String> renamedColumnNames = new ArrayList<>();

		for (String columnName : columnNames) {
			if (columnCounts.containsKey(columnName)) {
				int count = columnCounts.get(columnName);
				count++;
				columnCounts.put(columnName, count);
				columnName = columnName + count;
			} else {
				columnCounts.put(columnName, 1);
			}
			renamedColumnNames.add(columnName);
		}

		LOGGER.info("Original column names: " + columnNames);
		LOGGER.info("Renamed column names: " + renamedColumnNames);

		String commaSeparatedString = String.join(",", renamedColumnNames);

		LOGGER.info("Creating table with column names: " + commaSeparatedString);

		return commaSeparatedString;

	}

	boolean tableExists(Connection connection, String tableName) throws SQLException {
		DatabaseMetaData meta = connection.getMetaData();

		ResultSet resultSet = meta.getTables(null, null, unescapeTableNameForDatabase(tableName, databsetype),
				new String[] { "TABLE" });

		return resultSet.next();
	}

	private static String unescapeTableNameForDatabase(String tableName, String databaseType) {
		if (databaseType.equalsIgnoreCase("mysql")) {
			return tableName.replaceAll("`", "");
		} else if (databaseType.equalsIgnoreCase("sqlserver")) {
			return tableName.replaceAll("[\\[\\]]", "");
		} else if (databaseType.equalsIgnoreCase("postgresql")) {
			return tableName.replaceAll("\"", "");
		} else {
			return tableName;
		}
	}

	private boolean createTable(Connection connection, String columnNames, int columnsCount, String tableName) {
		try {

			Statement statement = connection.createStatement();

			if (tableExists(connection, tableName)) {
				LOGGER.info("table already exists " + tableName);
				// Truncate the table
				//String truncateQuery = "TRUNCATE TABLE " + tableName;
				//statement.executeUpdate(truncateQuery);

				//LOGGER.info("Table " + tableName + " truncated successfully.");
				return true;

			}
 
			String sql = "CREATE TABLE " + tableName + "( ";
			StringBuffer indicators = new StringBuffer();
			indicators.append(sql);
			String columnNamesArr[] = columnNames.split(",");

			for (int i = 0; i < columnNamesArr.length; i++) {

				if (columnNamesArr[i].trim().length() > 0) {
					String columnName = validateColumnname(columnNamesArr[i]);
					indicators.append(columnName + " TEXT,");
				}
			}
			indicators.deleteCharAt(indicators.length() - 1);
			indicators.append(");");

			statement.executeUpdate(indicators.toString());

			LOGGER.info("Successfully created " + tableName);
			return true;
		} catch (SQLException e) {

			LOGGER.severe(" Error while creating table" + tableName + stackTrace(e));
		}
		return false;
	}

	private String validateColumnname(String column) {

		LOGGER.info(column);
		if (Character.isDigit(column.charAt(0))) {
			String arr[] = column.split("_");
			if (arr.length > 1) {
				shuffleFirstAndLast(arr);
				column = String.join("_", arr);

			} else {
				column = "COLUMN_" + column;
			}

		}
		System.out.println("******************"+column);
		column = escapeValueForDatabase(column, databsetype);
		return column;
	}

	private void shuffleFirstAndLast(String[] array) {
		if (array.length < 2) {
			return;
		}
		String temp = array[0];
		array[0] = array[array.length - 1];
		array[array.length - 1] = temp;
	}

	private void formatAndInsertData(Connection con, String coulmnNames, int columnsCount, String[] dataLines,
			String tableName) {
		StringBuffer columnNamesWithCommaSeperated = new StringBuffer();

		String columnNamesArr[] = coulmnNames.split(",");

		for (int i = 0; i < columnNamesArr.length; i++) {

			String columnName = validateColumnname(columnNamesArr[i]);
			columnNamesWithCommaSeperated.append(columnName + ",");

		}
		columnNamesWithCommaSeperated.deleteCharAt(columnNamesWithCommaSeperated.length() - 1);
		String sql = "INSERT INTO " + tableName + " (" + columnNamesWithCommaSeperated.toString() + ")  VALUES (";
		StringBuffer indicators = new StringBuffer();
		indicators.append(sql);
		for (int i = 0; i < columnsCount; i++) {
			indicators.append("?,");
		}
		indicators.deleteCharAt(indicators.length() - 1);
		indicators.append(")");
		int totalRows = dataLines.length;

		String array[][] = new String[totalRows-1][columnsCount];
		for (int i = 1; i < totalRows; i++) {
			String row = (String) dataLines[i];
			
			
			 
			LOGGER.info(row);
			String coulmns[] = row.split("\\|");

			for (int j = 0; j < coulmns.length; j++) {
				String value = coulmns[j];

				String Normalizedvalue = value.replaceAll("[^\\\\.A-Za-z0-9 " + normaliseChars + "]", "");

				LOGGER.info("actual value " + value + " normalised value " + Normalizedvalue);
				array[i-1][j] = value;
			}
		}

		try {
			putBatchData(con, indicators.toString(), array);

		} catch (Exception e) {
			LOGGER.severe(stackTrace(e));
		}
	}

	public void putBatchData(Connection con, String sql, String args[][]) {
		try {
			PreparedStatement stmt = con.prepareStatement(sql);
			for (int i = 0; i < args.length; i++) {
				for (int j = 0; j < args[i].length; j++) {
					stmt.setString(j + 1, args[i][j]);
				}
				stmt.addBatch();
				stmt.executeBatch();
				stmt.clearParameters();
			}
			stmt.executeBatch();
			LOGGER.info("Success");
		} catch (Exception e) {
			LOGGER.severe(stackTrace(e));
			LOGGER.info("Failed");
		}
	}

}