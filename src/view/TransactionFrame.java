/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import transaction.Transaction;

public class TransactionFrame extends JFrame {
    
	private static final long serialVersionUID = 1L;
	                    
    private JScrollPane jScrollPane1;
    private JTextArea transText;
    private Transaction trans;
    
	public TransactionFrame(Transaction trans) {
		this.trans = trans;
		trans.setDisplay(this);
		
        initComponents();
        trans.start();
    }
                    
    private void initComponents() {

        jScrollPane1 = new JScrollPane();
        transText = new JTextArea();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        transText.setEditable(false);
        transText.setColumns(20);
        transText.setRows(5);
        transText.setBorder(BorderFactory.createTitledBorder("Transaction"));
        jScrollPane1.setViewportView(transText);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );

        pack();
    }     
    
    public void display(String msg) {
    	transText.append(msg);
		transText.setCaretPosition(transText.getText().length() - 1);
    }
}
