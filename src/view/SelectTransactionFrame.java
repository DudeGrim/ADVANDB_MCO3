package view;

import java.io.IOException;
import java.sql.SQLException;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import network.Server;
import transaction.Transaction;

public class SelectTransactionFrame extends JFrame {
		String ip;
		int nodeType;
		Server server;
		
		TitledBorder title;
		Border border;
		
		JPanel queryPanel;
		JButton readButton;
		JButton writeButton;
		
		
		JPanel statusPanel;
		
		JPanel node1;
		JLabel nodeName1;
		JLabel nodeStatus1;
		
		JPanel node2;
		JLabel nodeName2;
		JLabel nodeStatus2;
		
		
		
	public SelectTransactionFrame(int nodeType, int isolationLevel, String ip) throws IOException, SQLException{
		this.nodeType = nodeType;
		this.ip = ip;
		server = new Server(nodeType, isolationLevel, ip);
		
		switch(nodeType){
		case Server.CENTRAL_SERVER:
			initCentralComponents(); break;
		case Server.MARINDUQUE_SERVER:
			initCentralComponents(); break;
		case Server.PALAWAN_SERVER:
			initPalawanComponents(); break;
		}
		
		
		
	}



	private void initPalawanComponents() {
		nodeName1 = new JLabel("Central: ");
		nodeName2 = new JLabel("Marinduque: ");
		nodeStatus1 = new JLabel("CONNECTED");
		nodeStatus2 = new JLabel("CONNECTED");
		
		
		
		
		
	}



	private void initCentralComponents() {
		// TODO Auto-generated method stub
		
	}
}
