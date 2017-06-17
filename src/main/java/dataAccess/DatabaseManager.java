package dataAccess;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;

// TODO close connections
public abstract class DatabaseManager {
	private Connection conn;
	private Statement statement;
	private DataSource connectionPool;

	// https://stackoverflow.com/questions/4938517/closing-jdbc-connections-in-pool
	protected DatabaseManager(String url) {
		URI dbUri = null;
		try {
			dbUri = new URI(url);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath();
		BasicDataSource basicDataSource = new BasicDataSource();

		if (dbUri.getUserInfo() != null) {
			basicDataSource.setUsername(dbUri.getUserInfo().split(":")[0]);
			basicDataSource.setPassword(dbUri.getUserInfo().split(":")[1]);
		}
		basicDataSource.setDriverClassName("org.postgresql.Driver");
		basicDataSource.setUrl(dbUrl);
		basicDataSource.setInitialSize(1);
		connectionPool = basicDataSource;
	}

	public int executeUpdate(String query) {
		int rowCount = -1;
		try (Connection conn = connectionPool.getConnection()) {
			try (Statement statement = conn.createStatement()) {
				rowCount = statement.executeUpdate(query);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rowCount;
	}

	public ResultSet executeQuery(String query) {
		ResultSet set = null;
		try {
			conn = connectionPool.getConnection();
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
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public DataSource getDataSource() {
		return connectionPool;
	}

}