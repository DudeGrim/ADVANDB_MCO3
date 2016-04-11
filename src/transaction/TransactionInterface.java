package transaction;

import java.sql.SQLException;

public interface TransactionInterface {
	public static final String DB_NAME = "db_hpq";
	public static final String USERNAME = "root";
	public static final String PASSWORD = "1234";
	public static final String URL = "jdbc:mysql://localhost:3306/";
	
	
	
	public void begin();
	//public String next();
	public void commit();
	public void rollback() throws SQLException, InterruptedException;
}
