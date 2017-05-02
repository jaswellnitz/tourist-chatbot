package data_access;

import java.sql.*;

public class DatabaseAccess {
	private Connection conn;
	private Statement statement;

	public DatabaseAccess(String database, String user, String pw) {
		try {
			// TODO adapt for heroku
			Class.forName("org.postgresql.Driver");
			String url = "jdbc:postgresql://localhost:5432/" + database;
			conn = DriverManager.getConnection(url, user, pw);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public ResultSet sendQuery(String query) {
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