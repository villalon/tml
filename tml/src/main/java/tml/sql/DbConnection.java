package tml.sql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;

import tml.Configuration;
import tml.storage.Repository;

/**
 * This class represents the metadata database. When annotations are obtained
 * for a document, those annotations are stored in the metadata database.
 * 
 * Annotations can be anything provided by an Annotator class, for example
 * the PennTree annotator parses a sentence and stores the penn string associated
 * with the parse.
 * 
 * @author Jorge Villalon
 *
 */
public class DbConnection {

	private static Logger logger = Logger.getLogger(DbConnection.class);
	private static Connection connection = null;
	private static String url;

	private Connection getConnection() {

		if(connection != null)
			return connection;

		try {
			Properties props = Configuration.getTmlProperties();
			connection = DriverManager.getConnection(
					url, 
					props.getProperty("tml.database.username"), 
					props.getProperty("tml.database.password"));
			return connection;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Couldn't connect to DB server. Check parameters!");
			return null;
		}
	}

	public void cleanMetaDataStorage() {
		logger.info("Cleaning meta data storage, all documents will be lost!");

		try {
			Connection conn = getConnection();
			String sql = "delete from tml_documents";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.executeUpdate();
			sql = "delete from tml_documents_annotations";
			statement = getConnection().prepareStatement(sql);
			statement.executeUpdate();
			sql = "delete from tml_documents_reviews";
			statement = getConnection().prepareStatement(sql);
			statement.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
	}

	public DbConnection() throws SQLException, IOException {
		Properties props =  Configuration.getTmlProperties();
		url = props.getProperty("tml.database.url.protocol")
				+ props.getProperty("tml.database.url.db");

		String driver = Configuration.getTmlProperties().getProperty("tml.database.driver");
		try {
			Class.forName(driver).newInstance();
		} catch (Exception e) {
			logger.error("Problem with database driver installation");
			e.printStackTrace();
			throw new IOException(e);
		}

		try {
			DriverManager.getConnection(
					url, 
					props.getProperty("tml.database.username"), 
					props.getProperty("tml.database.password"));
		} catch (SQLException e) {
			logger.error("Problems accessing the annotations database. " +
					"Please check for the database and the user defined in the tml.properties file. " +
					"Read the README.txt file for installation instructions.");
			logger.error("User: " + props.getProperty("tml.database.username") + " URL:" + url);
			logger.error(e.getLocalizedMessage());
			throw e;
		}

		logger.info("Metadata:\t\tStoring metadata info in DB " + driver + " at " + url);
	}

	public String[][] getDocuments() throws Exception {
		PreparedStatement statement = getConnection().prepareStatement("select document_externalid, document_annotated from tml_documents where document_type = 'document'");
		String[] cols = new String[] {"document_externalid","document_annotated"};
		return executeQuery(statement, cols);
	}

	public String[][] getSubDocuments(String externalId) throws Exception {
		PreparedStatement statement = getConnection().prepareStatement("select document_externalid, document_annotated from tml_documents where document_externalid like ?");
		statement.setString(1, "%" + externalId);
		String[] cols = new String[] {"document_externalid","document_annotated"};
		return executeQuery(statement, cols);
	}

	public void insertDocument(Repository repo, Document document) throws SQLException {
		Connection conn = getConnection();
		Date now = new Date();
		String sql = "delete from tml_documents where document_externalid = ?";
		PreparedStatement statement = conn.prepareStatement(sql);
		statement.setString(1, document.get(repo.getLuceneExternalIdField()));
		statement.executeUpdate();
		sql = "insert into tml_documents " +
				" (document_title, document_url, document_externalid, document_content, document_type, document_date, document_annotated) values (?,?,?,?,?,?,0)";
		statement = getConnection().prepareStatement(sql);
		statement.setString(1, document.get(repo.getLuceneTitleField()));
		statement.setString(2, document.get(repo.getLuceneUrlField()));
		statement.setString(3, document.get(repo.getLuceneExternalIdField()));
		statement.setString(4, document.get(repo.getLuceneContentField()));
		statement.setString(5, document.get(repo.getLuceneTypeField()));
		statement.setDate(6, new java.sql.Date(now.getTime()));
		statement.executeUpdate();
	}

	public void setAnnotation(String documentId, String field, String annotation) {
		try {
			Connection conn = getConnection();
			Date now = new Date();
			String sql = "delete from tml_documents_annotations where document_externalid = ? and annotation_field = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, documentId);
			statement.setString(2, field);
			statement.execute();
			sql = "insert into tml_documents_annotations (document_externalid, annotation_field, annotation_value, annotation_date) " +
					" values (?,?,?,?)";
			statement = getConnection().prepareStatement(sql);
			statement.setString(1, documentId);
			statement.setString(2, field);
			statement.setString(3, annotation);
			statement.setDate(4, new java.sql.Date(now.getTime()));
			statement.execute();
			sql = "update tml_documents set document_annotated = 1 "
					+ "where document_externalid = ?";
			statement = getConnection().prepareStatement(sql);
			statement.setString(1, documentId);
			statement.execute();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
	}

	public boolean isDirty(Date date) {
		Date maxAnnotation = new Date(0);
		Date maxDocument = new Date(0);
		try {
			Connection conn = getConnection();
			String sql = "select max(annotation_date) as date from tml_documents_annotations";
			PreparedStatement statement = conn.prepareStatement(sql);
			String[] columns = new String[]{"date"};
			String[][] m = executeQuery(statement,columns);
			if(m[0][0] != null)
				maxAnnotation = new Date(Long.parseLong(m[0][0]));
			sql = "select max(document_date) as date from tml_documents";
			statement = conn.prepareStatement(sql);
			m = executeQuery(statement,columns);
			if(m[0][0] != null)
				maxDocument = new Date(Long.parseLong(m[0][0]));
			logger.info(date);
			logger.info(maxAnnotation);
			logger.info(maxDocument);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		if(date.before(maxDocument) || date.before(maxAnnotation))
			return true;
		else
			return false;
	}

	public String getAnnotation(String documentId, String field) {
		String sql = "select annotation_value from tml_documents_annotations "
				+ "where document_externalid = ? and annotation_field = ?";
		String[] columns = new String[]{"annotation_value"};

		try {
			Connection conn = getConnection();
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, documentId);
			statement.setString(2, field);
			String[][] result = executeQuery(statement, columns);
			if(result.length == 0) {
				return null;
			}
			return result[0][0];
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		return null;
	}

	public String getReview(String documentId, String reviewName, String user) {
		String sql = "select review_value from tml_documents_reviews "
				+ "where document_externalid = ? and review_name = ? and review_author = ?";
		String[] columns = new String[]{"review_value"};

		try {
			Connection conn = getConnection();
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, documentId);
			statement.setString(2, reviewName);
			statement.setString(3, user);
			String[][] result = executeQuery(statement, columns);
			if(result.length == 0) {
				return null;
			}
			return result[0][0];
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		return null;
	}

	public void setReview(String documentId, String review_name, String review, String user) {
		try {
			Connection conn = getConnection();

			Date now = new Date();
			String sql = "delete from tml_documents_reviews where document_externalid = ? and review_name = ? and review_author = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, documentId);
			statement.setString(2, review_name);
			statement.setString(3, user);
			statement.execute();
			sql = "insert into tml_documents_reviews (document_externalid, review_name, review_author, review_value, review_date) " +
					" values (?,?,?,?,?)";
			statement = getConnection().prepareStatement(sql);
			statement.setString(1, documentId);
			statement.setString(2, review_name);
			statement.setString(3, user);
			statement.setString(4, review);
			statement.setDate(5, new java.sql.Date(now.getTime()));
			statement.execute();

		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
	}

	public String[][] getUnannotatedDocument() {
		try {
			Connection conn = getConnection();
			conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			String sql = "update tml_documents " +
					"set document_annotated = 2 where document_annotated = 0";

			PreparedStatement statement = conn.prepareStatement(sql);
			int rowcount = statement.executeUpdate();
			if(rowcount == 0) {
				return null;
			}

			sql = "select document_externalid, document_type from tml_documents " + 
					"where document_annotated = 2";
			String[] columns = new String[]{"document_externalid","document_type"};

			statement = conn.prepareStatement(sql);
			String[][] result = executeQuery(statement, columns);
			if(result.length == 0) {
				return null;
			}

			return result;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		return null;		
	}

	public String[][] executeQuery(PreparedStatement st, String[] columns) throws Exception {
		Hashtable<String, ArrayList<String>> results = new Hashtable<String, ArrayList<String>>();
		if(columns == null)
			throw new Exception("Columns is null");
		try {
			ResultSet resultSet = st.executeQuery();
			for(String column : columns) {
				results.put(column, new ArrayList<String>());
			}
			while(resultSet.next()) {
				for(String column : columns) {
					if(resultSet.getObject(column)!=null)
						results.get(column).add(resultSet.getObject(column).toString());
					else
						results.get(column).add(null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
		return getTableFromHash(results, columns);		
	}	

	private String[][] getTableFromHash(Hashtable<String, ArrayList<String>> h, String[] sortedKeys) throws Exception {
		if(h.keySet().size()==0)
			return null;
		@SuppressWarnings("unchecked")
		int rows = ((ArrayList<String>)h.values().toArray()[0]).size();
		String[][] output = new String[rows][h.keySet().size()];
		for(int i=0;i<rows;i++) {
			int j=0;
			for(String key : sortedKeys) {
				output[i][j] = h.get(key).get(i);
				j++;
			}
		}
		return output;
	}

	public void close() {
		if(connection != null)
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error("Couldn't close DB connection");
				connection = null;
			}
	}
}
