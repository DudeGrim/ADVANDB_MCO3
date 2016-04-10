package transaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;

import network.Server;
import view.ResultFrame;

public class Transaction extends Thread implements TransactionInterface{
	private Connection db;
	private String statements;
	private Savepoint start;
	
	private boolean rollbacked;
	private int currentStep;
	private String[] tables;
	
	String name;
	String ipadd;
	final int PORT = 1996;
    InputStreamReader input;
    BufferedReader inFromServer;
    
    
    String message = "initial";
   // Socket socket;
    ResultSet rs;
    Server server;
//    BufferedReader bufferedReaderFromClient;
//    PrintWriter printWriter;
//    BufferedReader bufferedReaderFromCommandPrompt;
//    String readerInput, ip;
//    Thread MST,MRT;
//    Thread main;
    
//    DataOutputStream out;
//    DataInputStream in;
	public Transaction(String statements, String[] tables, int isoLevel, Connection db, Server server) throws SQLException {
		
		this.db = db;
		this.server = server;
		try {
			db.setAutoCommit(false);
			db.setTransactionIsolation(isoLevel);
			
			
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
		 
	     /*  try {
	          // socket = new Socket(ipadd, PORT);	           
	           in = new DataInputStream(socket.getInputStream());
	           out = new DataOutputStream(socket.getOutputStream());
	  
	       } catch (ConnectException connectException) {
	           System.out.println(connectException.getMessage());
	       } catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		
		begin();		
	}
	
	public void begin() {
		try {
			System.out.println("START TRANSACTION;");
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
						System.out.println("rollback");
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
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
	
	/*public boolean hasNext() {
		if(currentStep < statements.length) {
			return true;
		} 
		
		return false;
	}
	
	public String next() {
		String ret = "";
		
		try {
			ret =  statements[currentStep];
			db.prepareStatement(ret).execute();
			currentStep++;
			
		} catch(SQLException err) {
			err.printStackTrace();
			try {
				rollback();
			} catch (SQLException | InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return ret;
	}*/
	
	
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
			// TODO Auto-generated catch block
			locked = false;
			e.printStackTrace();
		}
		try {
			System.out.println("LOCK: " + lock);
			server.sendToAll("LOCK:" + lock);
		
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String k = server.getAcknowledgement();
			
//			System.out.println(k);
			if(k.equals("LOCKED SUCCESSFUL"))
				locked = true;
			else locked = false;			
			
        } catch (IOException e) {
			// TODO Auto-generated catch block
        	locked = false;
			e.printStackTrace();
		}
        return locked;

	}
	
	public void doSQL(String sql){
		//sql processing\
		try {
			System.out.println("SQL:" + sql);
			server.sendToAll("SQL:" + sql);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {	
			rs = db.prepareStatement(sql).executeQuery();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized boolean ready(){
		//send to other nodes that I am ready
		boolean ready;
		try {
//			System.out.println("READY");
			server.sendToAll("READY");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ready = false;
		}
		
		try {
			wait();
		} catch (InterruptedException er) {}
		
		String k = server.getAcknowledgement();
//		System.out.println(k);
		
		if(k.equals("READY"))
			ready = true;
		else ready = false;
	
		
		return ready;
	}
	
	public void unlock(){
		//Unlock all tables
		try {
			db.prepareStatement("UNLOCK TABLES").executeQuery();
			try {
				server.sendToAll("UNLOCK TABLES");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void end(){
		try {
			server.sendToAll("END");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			new ResultFrame(statements, String.valueOf(0), rs);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void commit() {
		try {
			try {
				System.out.println("COMMIT");
				server.sendToAll("COMMIT");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			db.commit();
			//db.prepareStatement("UNLOCK TABLES").executeQuery();
//			db.setAutoCommit(true);
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
	
	public synchronized void notifyTrans() {
		notify();
	}
	/*public boolean isWriteTransaction(){
		for(String s:statements){
			if(s.contains("UPDATE") || s.contains("DELETE") || s.contains("INSERT")){
				return true;
			} 
		}
		return false;
	}*/


	
	
	
	 
}
