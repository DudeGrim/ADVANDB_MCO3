package transaction;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

import network.Server;
import view.TransactionFrame;

public class Transaction extends Thread implements TransactionInterface{
	private Connection db;
	private String statements;
	private Savepoint start;
	
	private String[] tables;
	
	private TransactionFrame frame;
	private boolean hasFrame;
    
    private Server server;
    
	public Transaction(String statements, String[] tables, int isoLevel, Connection db, Server server) throws SQLException {
		this.hasFrame = false;
		this.db = db;
		this.server = server;
		
		try {
			db.setAutoCommit(false);
			db.setTransactionIsolation(isoLevel);
			
			this.statements = statements;
			this.tables = tables;
		} catch (SQLException err) {
			err.printStackTrace();
			db.setAutoCommit(true);
		}
	}

	public Transaction(String statements, String[] tables, int isoLevel, Connection db, Server server, TransactionFrame frame) throws SQLException {
		this.frame = frame;
		this.hasFrame = true;
		this.db = db;
		this.server = server;
		
		try {
			db.setAutoCommit(false);
			db.setTransactionIsolation(isoLevel);
			
			
			this.statements = statements;
			this.tables = tables;
		} catch (SQLException err) {
			err.printStackTrace();
			db.setAutoCommit(true);
		}
	}
	
	public void display(String msg) {
		if(hasFrame) {
			frame.display(msg + "\n");
		}
		
		System.out.println(msg);
	}

	
	public void run() {	
		begin();		
	}
	
	public void begin() {
		try {
			display("START TRANSACTION;");
			start = db.setSavepoint();
			db.prepareStatement("START TRANSACTION;").executeQuery();
			if(lock(tables, "READ") == true){
				doSQL(statements);
				if(ready()){
					unlock();
					commit();
					end();
				}else{
					try {
						rollback();
						display("ROLLBACK");
					} catch (InterruptedException e) {
					
						e.printStackTrace();
					}
				}
			}else{				
				System.out.println("FUCK");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			
			try {
				rollback();
			} catch (SQLException | InterruptedException er) {
				er.printStackTrace();
			}
		}
	}
	
	public synchronized boolean lock(String[] tables, String lockType){
		//requests for lock on the table 
		String lock = "LOCK TABLES ";
		boolean locked; 
		for(int i = 0; i < tables.length; i++) {
			if(i != 0) {
				lock += ", ";
			} 
			
			lock += tables[i];
		}
		
		lock += " " + lockType + ";";
		try {
			db.prepareStatement(lock).executeQuery();
		} catch (SQLException e) {
		
			locked = false;
			e.printStackTrace();
		}
		try {
			display("LOCK: " + lock);
			server.sendToAll("LOCK:" + lock);
		
			try {
				wait();
			} catch (InterruptedException e) {
			
				e.printStackTrace();
			}
			String k = server.getAcknowledgement();
			
			if(k.equals("LOCKED SUCCESSFUL"))
				locked = true;
			else locked = false;			
			
        } catch (IOException e) {
		
        	locked = false;
			e.printStackTrace();
		}
		
        return locked;

	}
	
	public void doSQL(String sql){
		try {
			display("SQL:" + sql);
			server.sendToAll("SQL:" + sql);
		} catch (IOException e1) {
		
			e1.printStackTrace();
		}
		
		try {	
			db.prepareStatement(sql).executeQuery();
		} catch (SQLException e) {
		
			e.printStackTrace();
		}
	}
	
	public synchronized boolean ready(){
		//send to other nodes that I am ready
		boolean ready;
		try {
			display("READY");
			server.sendToAll("READY");
		} catch (IOException e) {
			e.printStackTrace();
			ready = false;
		}
		
		try {
			wait();
		} catch (InterruptedException er) {}
		
		String k = server.getAcknowledgement();
		
		if(k.equals("READY"))
			ready = true;
		else ready = false;
		
		return ready;
	}
	
	public void unlock(){
		//Unlock all tables
		try {
			display("UNLOCK TABLES");
			db.prepareStatement("UNLOCK TABLES").executeQuery();
			try {
				server.sendToAll("UNLOCK TABLES");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void end(){
		try {
			display("END");
			server.sendToAll("END");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void commit() {
		try {
			try {
				display("COMMIT");
				server.sendToAll("COMMIT");
			} catch (IOException e) {
				e.printStackTrace();
			}
			db.commit();
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
		display("UNLOCK TABLES;");
		display("ROLLBACK;");
		
		db.prepareStatement("UNLOCK TABLES").executeQuery();
		db.rollback();
		db.setAutoCommit(true);	
		
		throw new InterruptedException();
	}
	
	public synchronized void notifyTrans() {
		notify();
	}

	public void setDisplay(TransactionFrame frame) {
		this.frame = frame;
		this.hasFrame = true;
		
	}
}
