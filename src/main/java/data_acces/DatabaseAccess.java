package data_acces;

import java.sql.*;


public class DatabaseAccess {
	private Connection conn;
	private Statement statement;

	public DatabaseAccess(String database, String user, String pw) {
		try {
			Class.forName("org.postgresql.Driver");
			String url = "jdbc:postgresql://localhost:5432/" + database;
			conn = DriverManager.getConnection(url, user, pw);
			statement = conn.createStatement();
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
	
	public ResultSet sendQuery(String query){
		ResultSet set = null;
		try {
			set = statement.executeQuery(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return set;
	}
	
	public void close(){
		try {
			conn.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}