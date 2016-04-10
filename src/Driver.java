import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

import network.Server;
import transaction.TransactionInterface;
import transaction.Transaction;


public class Driver {
	public static void main(String[] args) throws SQLException, IOException {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception err) {
			
		}
		Scanner sc = new Scanner(System.in);
		Server server = null;
		
		System.out.println("[1]Central, [2]palawan or [3]marinduque?");
		int choice =  sc.nextInt();
		
		switch(choice){
		case 1:
			server = new Server("db_hpq", choice);
			break;
		case 2:
//			System.out.print("Input Central IP:");
			server = new Server("db_hpq_palawan", choice);
//			trans = new TransactionManager(TransactionInterface.DB_NAME, statements.toArray(new String[0]), tables.toArray(new String[0]), 
//					Connection.TRANSACTION_READ_UNCOMMITTED, ip);
			break;
		case 3:
//			System.out.print("Input Central IP:");
			server = new Server("db_hpq_marinduque", choice);
//			trans = new TransactionManager(TransactionInterface.DB_NAME, statements.toArray(new String[0]), tables.toArray(new String[0]), 
//					Connection.TRANSACTION_READ_UNCOMMITTED, ip);
			break;
		}
		
		
		/*updates.add("SELECT * FROM hpq_mem WHERE ISNULL(mocrim);");
		updates.add("UPDATE hpq_mem SET mocrimind = 2 WHERE ISNULL(mocrim);");
		
		ReadTransaction trans = new ReadTransaction(Transaction.DB_NAME, statements.toArray(new String[0]), tables.toArray(new String[0]));
		WriteTransaction write = new WriteTransaction(Transaction.DB_NAME, updates.toArray(new String[0]), tables.toArray(new String[0]));
		
		write.start();
		trans.start();*/
		//Client client = new Client();
//		Server server = new Server("db_hpq");
	//	client.start();
		
		server.start();
		//trans.start();
		
		
	}
	
}
 