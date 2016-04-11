package view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import network.Server;

public class InitializeFrame extends JFrame {
	TitledBorder title;
	Border border;
	JPanel buttonPanel;
	ButtonGroup nodesGroup;
	JRadioButton palawanButton;
	JRadioButton centralButton;
	JRadioButton marinduqueButton;
	
	JPanel isolationPanel;
	ButtonGroup isolationGroup;
	JRadioButton readUncommittedButton;
	JRadioButton readCommittedButton;
	JRadioButton readRepeatableButton;
	JRadioButton serializableButton;
	
	JPanel ipPanel;
	JLabel centralIPLabel;
	JTextField centralIPTextField;
	
	JButton okButton;
	
	ButtonListener listener;
	
	public InitializeFrame(){
		initComponents();
	}
	
	private void initComponents(){
		listener = new ButtonListener();
		buttonPanel = new JPanel();
		border =BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		title = BorderFactory.createTitledBorder(border, "Select Node");
		buttonPanel.setBorder(title);
		palawanButton = new JRadioButton("Pawalan");
		centralButton = new JRadioButton("Central");
		marinduqueButton = new JRadioButton("Marinduque");
		nodesGroup = new ButtonGroup();
		nodesGroup.add(palawanButton);
		nodesGroup.add(centralButton);
		nodesGroup.add(marinduqueButton);
		buttonPanel.add(palawanButton);
		buttonPanel.add(centralButton);
		buttonPanel.add(marinduqueButton);
		
		readCommittedButton = new JRadioButton("Read Committed");
		readUncommittedButton = new JRadioButton("Read Uncommitted");
		readRepeatableButton = new JRadioButton("Read Repeatable");
		serializableButton = new JRadioButton("Serializable");
		isolationGroup = new ButtonGroup();
		isolationGroup.add(readCommittedButton);
		isolationGroup.add(readUncommittedButton);
		isolationGroup.add(readRepeatableButton);
		isolationGroup.add(serializableButton);
		isolationPanel = new JPanel();
		isolationPanel.add(readUncommittedButton);
		isolationPanel.add(readCommittedButton);
		isolationPanel.add(readRepeatableButton);
		isolationPanel.add(serializableButton);
		
		title = BorderFactory.createTitledBorder(border, "Select Isolation");
		isolationPanel.setBorder(title);
		
		this.add(isolationPanel);
		
		
		
		palawanButton.addActionListener(listener);
		centralButton.addActionListener(listener);
		marinduqueButton.addActionListener(listener);

			
		ipPanel = new JPanel();
		centralIPLabel  = new JLabel("Enter Central Node IP: ");
		centralIPTextField = new JTextField(); 
		centralIPTextField.setColumns(10);
		
		okButton = new JButton("OK");
		ipPanel.add(centralIPLabel);
		ipPanel.add(centralIPTextField);
		ipPanel.add(okButton);
		okButton.addActionListener(listener);
		
		
		
		
		this.add(buttonPanel);
		this.add(ipPanel);
		
		this.setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		this.pack();
		this.setTitle("MCO3 Initialization");
		this.setVisible(true);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(3);
	}
	
	public class ButtonListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			if(e.getSource() == palawanButton){
				centralIPTextField.setEnabled(true);
				
			}
			if(e.getSource() == centralButton){
				centralIPTextField.setEnabled(false);
			}
			if(e.getSource() == marinduqueButton){
				centralIPTextField.setEnabled(true);
				
			}
			if(e.getSource() == okButton){
				int nodeType = 0, isolationType = 0;
				String ip = "";
				if(palawanButton.isSelected()){
					nodeType = Server.PALAWAN_SERVER;
					ip = centralIPTextField.getText();
				}
				if(centralButton.isSelected()){
					nodeType = Server.CENTRAL_SERVER;
					ip= null;
				}
				
				if(marinduqueButton.isSelected()){
					nodeType = Server.MARINDUQUE_SERVER;
					ip = centralIPTextField.getText();
				}
				
				if(readCommittedButton.isSelected())
					isolationType = Connection.TRANSACTION_READ_COMMITTED;
				if(readUncommittedButton.isSelected())
					isolationType = Connection.TRANSACTION_READ_UNCOMMITTED;
				if(readRepeatableButton.isSelected())
					isolationType = Connection.TRANSACTION_REPEATABLE_READ;
				if(serializableButton.isSelected())
					isolationType = Connection.TRANSACTION_SERIALIZABLE;
				
					
				try {
					new SelectTransactionFrame(nodeType, isolationType, ip);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		
		
	} 
	
	
	
	
}
