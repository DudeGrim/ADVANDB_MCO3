package transaction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;

import view.ResultFrame;

public class ReadTransaction extends Thread implements TransactionInterface{
	private Connection db;
	private String[] statements;
	private Savepoint start;
	
	private boolean rollbacked;
	private int currentStep;
	private String[] tables;
	
	public ReadTransaction(String db_name, String[] statements, String[] tables) throws SQLException {
		db = DriverManager.getConnection(Transaction.URL + db_name, Transaction.USERNAME, Transaction.PASSWORD);
		
		try {
			db.setAutoCommit(false);
			db.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			
			this.statements = statements;
			this.tables = tables;
			this.currentStep = 0;
			this.rollbacked = false;
		} catch (SQLException err) {
			err.printStackTrace();
			db.setAutoCommit(true);
		}
	}
	
	public void run() {
		begin();
		
		while(hasNext() && !rollbacked) {
			System.out.println(next());
		}
		
		commit();
	}
	
	public void begin() {
		try {
			System.out.println("Starting Transaction:");
			System.out.println("START TRANSACTION;");
			start = db.setSavepoint();
			db.prepareStatement("START TRANSACTION;").executeQuery();
			
			String lock = "LOCK TABLES ";

			for(int i = 0; i < tables.length; i++) {
				if(i != 0) {
					lock += ", ";
				} 
				
				lock += tables[i];
			}
			
			lock += " READ;";
			
			System.out.println(lock);
			db.prepareStatement(lock).executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
			
			try {
				rollback();
			} catch (SQLException | InterruptedException er) {
				er.printStackTrace();
			}
		}
	}
	
	public boolean hasNext() {
		if(currentStep < statements.length) {
			return true;
		} 
		
		return false;
	}
	
	public String next() {
		String ret = "";
		
		try {
			ret =  statements[currentStep];
			ResultSet rs = db.prepareStatement(ret).executeQuery();
			currentStep++;
			
			new ResultFrame(ret, String.valueOf(0), rs);
			
		} catch(SQLException err) {
			err.printStackTrace();
			try {
				rollback();
			} catch (SQLException | InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return ret;
	}

	@Override
	public void commit() {
		try {
			System.out.println("COMMIT;");
			System.out.println("UNLOCK TABLES;");
			db.commit();
			db.prepareStatement("UNLOCK TABLES").executeQuery();
			db.setAutoCommit(true);
		} catch (SQLException err) {
			err.printStackTrace();
			try {
				rollback();
			} catch (SQLException er) {
				er.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void rollback() throws SQLException, InterruptedException {
		db.rollback(start);
		System.out.println("UNLOCK TABLES;");
		System.out.println("ROLLBACK;");
		
		db.prepareStatement("UNLOCK TABLES").executeQuery();
		db.setAutoCommit(true);	
		rollbacked = true;
		
		throw new InterruptedException();
	}

}
