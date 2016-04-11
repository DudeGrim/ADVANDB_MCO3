package network;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import com.mysql.jdbc.Connection;

import transaction.Transaction;
import transaction.TransactionManager;

public class Server extends Thread{
	public static final int PORT = 1996;
	public static final int CENTRAL_SERVER = 1;
	public static final int PALAWAN_SERVER = 2;
	public static final int MARINDUQUE_SERVER = 3;
	
	private TransactionManager tm;
	private ServerSocket srvr;
	private java.sql.Connection db;
	private int serverNode;
	
	private Server thisServer;
	
	private TransactionInput ti;
	
	private HashMap<String, AcceptClient> connected;
	private Vector<AcceptClient> list;
	
	private Transaction localTrans;
	private String ack;
	
	private boolean sending;
	
	public Server(int serverNode, int isolationLevel, String ip) throws IOException, SQLException {
		this.srvr = new ServerSocket(PORT);
		this.serverNode = serverNode;
		switch(serverNode){
		case 1: 
			this.tm = TransactionManager.getInstance("db_hpq", this);
			break;
		case 2: 
			this.tm = TransactionManager.getInstance("db_hpq_palawan", this);
			break;
		case 3:
			this.tm = TransactionManager.getInstance("db_hpq_marinduque", this);
			break;
		}
		this.thisServer = this;
		this.sending = false;
		this.connected = new HashMap<>();
		this.list = new Vector<>();
		this.ti  = new TransactionInput();
		this.db = tm.getDBConnection();
		
		ti.start();		
	}
	
	public void display(String msg) {
		System.out.println(msg);
	}
	
	public void run() { 
		while(true) {
			try {
				Socket rcv = srvr.accept();
				String ip = rcv.getInetAddress().getHostAddress();
				
				//connect to what connected to you, if not connected yet
				if(!connected.containsKey(ip)) {
					AcceptClient cl = new AcceptClient(new Socket(ip, PORT));
					connected.put(ip, cl);
					cl.start();
				}
				
				AcceptClient client = new AcceptClient(rcv);
				
				list.add(client);
				client.start();
				System.out.println("Something connected.");
			} catch (IOException err) {
				err.printStackTrace();
			}
		}
	}
	
	public synchronized void sendToAll(String string) throws IOException {
		while(sending) {
			try {
				wait();
			} catch (InterruptedException e) {}
		}
		
		sending = true;
		Set<String> keys = connected.keySet();
		
		for(String key: keys) {
			connected.get(key).send(string);
		}
		
		sending = false;
		notify();
	}
	
	//gets a local transactions
	public class TransactionInput extends Thread{
		Scanner sc = new Scanner(System.in);
		
		String statements, ip;
		ArrayList<String> tables;
		int choice, isoLevel;
		Socket socket;
		
		public TransactionInput() {
			tables = new ArrayList<>();
		}
		
		public void run(){
			choice = 1;
			
			if(serverNode != CENTRAL_SERVER){
				try {
					socket = new Socket(ip, PORT);
					AcceptClient cl = new AcceptClient(socket);
					cl.start();
					connected.put(ip, cl);
				} catch (UnknownHostException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					//TODO central was down
					display("CENTRAL is down: " + e1);
				}	 
			}else{
				System.out.println("YOU ARE CENTRAL");
				ip = "localhost";
			}
	           
			while(choice != 0){
				System.out.println("Select isolation level:");
				System.out.println("[1]Read Committed"
						+ "\n[2]Read Uncommited "
						+ "\n[3]Repeatable Read "
						+ "\n[4]Serializable");
				choice = sc.nextInt();
				switch(choice){
				case 1: isoLevel = Connection.TRANSACTION_READ_COMMITTED; break;
				case 2: isoLevel = Connection.TRANSACTION_READ_UNCOMMITTED; break;
				case 3: isoLevel = Connection.TRANSACTION_REPEATABLE_READ; break;
				case 4: isoLevel = Connection.TRANSACTION_SERIALIZABLE; break;
				
				}
				
				System.out.println("Choose a transaction pls:");
				System.out.println("[1]Read: Select Everything from Crop Table"
						+ "\n[2]Write: "
						+ "\n[3]Read: "
						+ "\n[4]Write: ");
				choice = sc.nextInt();
				tables.clear();
				switch(choice) {
				case 1: 
					statements = "SELECT * FROM hpq_crop;";
					tables.add("hpq_crop");
					try {
						localTrans = new Transaction(statements, tables.toArray(new String[0]), isoLevel, db, thisServer);
						localTrans.start();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case 2: 	
					statements = "UPDATE hpq_crop SET croptype_o = trains WHERE hpq_hh_id = 11333;";
					tables.add("hpq_crop");
					try {
						localTrans = new Transaction(statements, tables.toArray(new String[0]), isoLevel, db, thisServer);
						localTrans.start();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}	
			}	
		}
	}
	
	
	
	public class AcceptClient extends Thread {
		Socket clientSocket;
		
		boolean sqlQuery = false;
		String query = null;
		boolean go = true;
		int nodeType;
		
		DataInputStream in;
		DataOutputStream out;
		
		public AcceptClient(Socket s) throws IOException {
			this.clientSocket = s;
			in = new DataInputStream(clientSocket.getInputStream());
			out = new DataOutputStream(clientSocket.getOutputStream());
			
			this.go = true;
			this.sqlQuery = false;
			//tell others what node you are when you connect
			out.write(serverNode);
			nodeType = in.read();
		}
		
		public void run() {
			while(true) { 
				try{
		            String message = in.readUTF();
		            System.out.println(message);
					StringTokenizer st = new StringTokenizer(message, ":");
					String protocol = st.nextToken();
					
					switch(protocol){
					case "LOCK":
						lock(st.nextToken()); break;	
					case "SQL":
						doSQL(st.nextToken()); break;
					case "READY":
						ready(); break;
					case "UNLOCK TABLES":
						unlockTables(); break;
					case "COMMIT":
						commit(); break;
					case "END":
						end(); break;
					default: setAcknowledgement(message);
					}
				} catch (IOException err) {
					//TODO Server ran into a problem, shutting down
					list.remove(this);
					connected.remove(getClientIP());
					break; //Break out the loop;
				}	
			}
			
			try {
				clientSocket.close();
				in.close();
				out.close();
			} catch (IOException e) {
				// If something went wrong here, nothing else I can do
				e.printStackTrace();
			}
			System.out.println("Socket closed.");
		}
		
		private void end() {
			System.out.println("END CHECK");
			
			go = false;
			localTrans = null;
		}

		private void commit() {
			try {
				System.out.println("COMMIT CHECK");
				db.commit();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		private void unlockTables() {
			try {
				System.out.println("UNLOCK CHECK");
				db.prepareStatement("UNLOCK TABLES").executeQuery();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		private synchronized void ready() throws IOException {
			setAcknowledgement("READY");
		}

		private synchronized void doSQL(String parameter) throws IOException {
			System.out.println("Executing SQL query");
			try {
				query = parameter;
				db.prepareStatement(parameter).executeQuery();
				sqlQuery = true;
				out.writeUTF("READY");
			} catch (SQLException e) {
				e.printStackTrace();
			}
			System.out.println("Executed SQL query");
		}

		private void lock(String parameter) throws IOException{
			try {
				System.out.println("Executing LOCK query");
				db.prepareStatement(parameter).executeQuery();
				System.out.println("Executed LOCK query");
				out.writeUTF("LOCKED SUCCESSFUL");
			} catch (SQLException e) {
				out.writeUTF("LOCKED FAILED");
				e.printStackTrace();
			}
			
		}
		
		public String getClientIP(){
			return clientSocket.getInetAddress().getHostAddress(); //guise pls...
		}
		
		public void send(String msg) {
			try {
				out.writeUTF(msg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public synchronized void setAcknowledgement(String msg) {
		this.ack = msg;

		//tell transaction a reply has came back
		if(localTrans != null) {
			localTrans.notifyTrans();
		}
	}

	public String getAcknowledgement() {
		return ack;
	}
}

