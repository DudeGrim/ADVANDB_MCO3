package transaction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import network.Server;


public class TransactionManager {
	private static TransactionManager instance = null;
	private Connection db;
	private Server s;
	
	private TransactionManager (String db_name, Server s) throws SQLException{
		this.s = s;
		db = DriverManager.getConnection(TransactionInterface.URL + db_name, 
				TransactionInterface.USERNAME, TransactionInterface.PASSWORD);
		db.setAutoCommit(false);
	}
	
	synchronized void doTransaction(Transaction trans) {
		trans.begin();
		//s.sendToAll(trans);
	}
	
	public static TransactionManager getInstance(String db_name, Server s) throws SQLException{
		if(instance == null)
			instance = new TransactionManager(db_name, s);
		return instance;
		
	}
	
	public Connection getDBConnection(){
		return db;
		
	}
	

	
	
}
