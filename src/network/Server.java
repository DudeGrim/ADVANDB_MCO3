package network;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.ResultSet;
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
import view.ResultFrame;

public class Server extends Thread{
	public static final int PORT = 1996;
	private TransactionManager tm;
	private ServerSocket srvr;
	private java.sql.Connection db;
	private Vector<AcceptClient> list;
	ResultSet rs;
	private int serverNode;
	Server thisServer;
	ArrayList<String> ips;
	static final int CENTRAL_SERVER = 1;
	static final int PALAWAN_SERVER = 2;
	static final int MARINDUQUE_SERVER = 3;
	TransactionInput ti = new TransactionInput();
	private HashMap<String, AcceptClient> connected;
	
	private Transaction localTrans;
	private String ack;
	
	private boolean sending;
	
	public Server(String db_name, int serverNode) throws IOException, SQLException {
		this.srvr = new ServerSocket(PORT);
		this.serverNode = serverNode;
		tm = TransactionManager.getInstance(db_name, this);
		thisServer = this;
		this.sending = false;
		connected = new HashMap<>();
		list = new Vector<>();
		
		/*if(serverNode != 1)*/
			ti.start();
		
		db = tm.getInstance(db_name, this).getDBConnection();	
	}
	
	//gets a local transactions
	public class TransactionInput extends Thread{
		Scanner sc = new Scanner(System.in);
		String statements;
		ArrayList<String> tables = new ArrayList<>();
		int choice, isoLevel;
		String ip;
		Socket socket;
		public void run(){
			choice = 1;
			if(serverNode != 1){
				System.out.println("Enter IP: ");
				ip = sc.nextLine();
				try {
					socket = new Socket(ip, PORT);
					AcceptClient cl = new AcceptClient(socket);
					cl.start();
					connected.put(ip, cl);
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}	 
			}else{
				System.out.println("YOU ARE CENTRAL");
				ip = "localhost";
				/*try {
					socket = new Socket(ips.get(0), PORT);
					in2 = new DataInputStream(socket.getInputStream());
			        out2 = new DataOutputStream(socket.getOutputStream());
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}	*/
			}
			           
	           
			while(choice!=0){
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
	
	public void run() { 
		while(true) {
			try {
				
				Socket rcv = srvr.accept();
				String ip = rcv.getInetAddress().getHostAddress();
				
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
	
	public class AcceptClient extends Thread {
		Socket clientSocket;
		
		boolean sqlQuery = false;
		String query = null;
		boolean go = true;
		
		DataInputStream in;
		DataOutputStream out;
		
		public AcceptClient(Socket s) throws IOException {
			this.clientSocket = s;
			in = new DataInputStream(clientSocket.getInputStream());
			out = new DataOutputStream(clientSocket.getOutputStream());
			
			/*if(serverNode == 1){
				Socket newSocket = new Socket(clientSocket.getInetAddress(), PORT);
				in2 = new DataInputStream(newSocket.getInputStream());
				out2 = new DataOutputStream(newSocket.getOutputStream());
			}*/
		}
		
		public void run() {
			//System.out.println(clientSocket.getInetAddress());
			ArrayList<String> statements = new ArrayList<>();
			while(true) { // stop when transaction is done?
				try{
		            String message = in.readUTF();
		            System.out.println(message);
					StringTokenizer st = new StringTokenizer(message, ":");
					String protocol = st.nextToken();
					
					switch(protocol){
					case "LOCK":
						lock(st.nextToken());break;	
					case "SQL":
						doSQL(st.nextToken());break;
					case "READY":
						ready(); break;
					case "UNLOCK TABLES":
						unlockTables();break;
					case "COMMIT":
						commit();break;
					case "END":
						end();break;
					default: setAcknowledgement(message);
					}
				} catch (IOException err) {
					//err.printStackTrace();
					list.remove(this);
					connected.remove(this.getClientIP());
					
					try {
						in.close();
						out.close();
						clientSocket.close();
					} catch(IOException e) {}
					
				}	
			}
			
			/*try {
				clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Socket closed.");*/
		}
		
		private void end() {
			System.out.println("END CHECK");
			try {
				new ResultFrame(query, String.valueOf(0), rs);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			go = false;
			localTrans = null;
		}

		private void commit() {
			try {
				System.out.println("COMMIT CHECK");
				db.commit();
//				db.setAutoCommit(true);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private void unlockTables() {
			try {
				System.out.println("UNLOCK CHECK");
				db.prepareStatement("UNLOCK TABLES").executeQuery();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private synchronized void ready() throws IOException {
						
			//out.writeUTF("READY");
			System.out.print("pls");
			setAcknowledgement("READY");
			System.out.println("set");
		}

		private synchronized void doSQL(String parameter) throws IOException {
			System.out.println("Executing SQL query");
			try {
				query = parameter;
				rs = db.prepareStatement(parameter).executeQuery();
				sqlQuery = true;
				out.writeUTF("READY");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
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
				// TODO Auto-generated catch block
				out.writeUTF("LOCKED FAILED");
				e.printStackTrace();
			}
			
		}
		
		public String getClientIP(){
			return clientSocket.getInetAddress().toString().substring(1);
		}
		
		public void send(String msg) {
			try {
				out.writeUTF(msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public synchronized void setAcknowledgement(String msg) {
		
		this.ack = msg;
		if(localTrans != null)
		localTrans.notifyTrans();
	}

	public String getAcknowledgement() {
		// TODO Auto-generated method stub
		return ack;
		
		
	}
}

