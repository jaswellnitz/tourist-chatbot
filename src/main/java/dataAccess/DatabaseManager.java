package dataAccess;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.log4j.Logger;

/**
 * Manages the access to the PostgreSQL database via JDBC
 * @author Jasmin Wellnitz
 *
 */
public abstract class DatabaseManager {
	private Connection conn;
	private Statement statement;
	private DataSource connectionPool;
	protected Logger logger = Logger.getLogger(this.getClass());

	/***
	 * Creates a DatabaseManager by connecting to the given database using JDBC
	 * @param url The database access url
	 */
	protected DatabaseManager(String url) {
		try {
			URI dbUri = new URI(url);
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
		} catch (URISyntaxException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
		
	}

	/**
	 * Executes an update on the database
	 * @param query The PostgreSQL Query to be executed
	 * @return either (1) the row count for SQL Data Manipulation Language (DML) statements or (2) 0 for SQL statements that return nothing
	 */
	protected int executeUpdate(String query) {
		int rowCount = -1;
		try (Connection conn = connectionPool.getConnection()) {
			try (Statement statement = conn.createStatement()) {
				rowCount = statement.executeUpdate(query);
			}
		} catch (SQLException e) {
			logger.error(e);
		}
		return rowCount;
	}

	/**
	 * Executes a query on the database
	 * @param query The PostgreSQL Query to be executed
	 * @return a ResultSet object that contains the data produced by the given query
	 */
	protected ResultSet executeQuery(String query) {
		ResultSet set = null;
		try {
			conn = connectionPool.getConnection();
			statement = conn.createStatement();
			set = statement.executeQuery(query);
		} catch (SQLException e) {
			logger.error(e);
		}
		return set;
	}

	/**
	 * Closes the previously opened connection and statement
	 */
	protected void close() {
		try {
			statement.close();
			conn.close();
		} catch (SQLException e) {
			logger.error(e);
		}
	}

	/***
	 * Gets the database connection
	 * @return connectionPool
	 */
	public DataSource getDataSource() {
		return connectionPool;
	}

}