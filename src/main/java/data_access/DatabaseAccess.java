package data_access;

import java.sql.*;

public class DatabaseAccess {
	private Connection conn;
	private Statement statement;

	public DatabaseAccess(String url) {
		try {
			conn = DriverManager.getConnection(url);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public int executeUpdate(String query){
		int rowCount = -1;
		try {
			statement = conn.createStatement();
			rowCount = statement.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rowCount;
	}
	
	public ResultSet executeQuery(String query) {
		ResultSet set = null;
		try {
			statement = conn.createStatement();
			set = statement.executeQuery(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return set;
	}

	public void close() {
		try {
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void tearDown() {
		try {
			statement.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}