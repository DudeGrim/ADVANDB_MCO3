/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import java.sql.Connection;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;
import javax.swing.WindowConstants;

import transaction.Query;

/**
 *
 * @author Miko Garcia
 */
public class Dashboard extends JFrame {

	public static final String[] isolationLevels = new String[] {"READ COMMITED", "READ UNCOMMITED", "REPEATABLE", "SERIALIZABLE"};
	private Query[] queries;
	
    private JPanel NodePanel;
    private ButtonGroup buttonGroup1;
    private JRadioButton centralRadio;
    private JPanel dashboard;
    private JComboBox<String> isolationCombo;
    private JPanel isolationPanel;
    private JRadioButton marinduquePalawan;
    private JRadioButton palawanRadio;
    private JScrollPane scrollPane;
    private JButton submitBtn;
    private JComboBox<Query> transCombo;
    private JPanel transPanel;
    
	private static final long serialVersionUID = 1L;
	
    public Dashboard(Query[] queryList) {
    	this.queries = queryList;
        initComponents();
    }
               
    private void initComponents() {
        buttonGroup1 = new ButtonGroup();
        NodePanel = new JPanel();
        centralRadio = new JRadioButton();
        palawanRadio = new JRadioButton();
        marinduquePalawan = new JRadioButton();
        isolationPanel = new JPanel();
        isolationCombo = new JComboBox<>(isolationLevels);
        transPanel = new JPanel();
        transCombo = new JComboBox<>(queries);
        submitBtn = new JButton();
        scrollPane = new JScrollPane();
        dashboard = new JPanel();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        NodePanel.setBorder(BorderFactory.createTitledBorder("Node Selection"));

        buttonGroup1.add(centralRadio);
        centralRadio.setText("Central");

        buttonGroup1.add(palawanRadio);
        palawanRadio.setText("Palawan");

        buttonGroup1.add(marinduquePalawan);
        marinduquePalawan.setText("Marinduque");

        GroupLayout NodePanelLayout = new GroupLayout(NodePanel);
        NodePanel.setLayout(NodePanelLayout);
        NodePanelLayout.setHorizontalGroup(
            NodePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(NodePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(centralRadio)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(palawanRadio)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(marinduquePalawan)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        NodePanelLayout.setVerticalGroup(
            NodePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(NodePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(centralRadio)
                .addComponent(palawanRadio)
                .addComponent(marinduquePalawan))
        );

        isolationPanel.setBorder(BorderFactory.createTitledBorder("Isolation Level"));
        
        GroupLayout isolationPanelLayout = new GroupLayout(isolationPanel);
        isolationPanel.setLayout(isolationPanelLayout);
        isolationPanelLayout.setHorizontalGroup(
            isolationPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(isolationCombo, 0, 142, Short.MAX_VALUE)
        );
        isolationPanelLayout.setVerticalGroup(
            isolationPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(isolationCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );

        transPanel.setBorder(BorderFactory.createTitledBorder("Transaction Selection"));
        
        submitBtn.setText("Submit");

        GroupLayout transPanelLayout = new GroupLayout(transPanel);
        transPanel.setLayout(transPanelLayout);
        transPanelLayout.setHorizontalGroup(
            transPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(transPanelLayout.createSequentialGroup()
                .addComponent(transCombo, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(submitBtn))
        );
        transPanelLayout.setVerticalGroup(
            transPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(transPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(transCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(submitBtn))
        );

        scrollPane.setBorder(BorderFactory.createTitledBorder("Dashboard"));

        GroupLayout dashboardLayout = new GroupLayout(dashboard);
        dashboard.setLayout(dashboardLayout);
        dashboardLayout.setHorizontalGroup(
            dashboardLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 408, Short.MAX_VALUE)
        );
        dashboardLayout.setVerticalGroup(
            dashboardLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 356, Short.MAX_VALUE)
        );

        scrollPane.setViewportView(dashboard);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(NodePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(isolationPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE))
            .addComponent(transPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(NodePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(transPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(isolationPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE)))
        );

        pack();
    }                                                                               
    
    private void doTransaction() {
    	Query query = (Query) transCombo.getSelectedItem();
    	String isoLevel = (String) isolationCombo.getSelectedItem();
    	int iso;
    	
    	switch(isoLevel) {
    	case "READ COMMITED": iso = Connection.TRANSACTION_READ_COMMITTED;
    	case "READ UNCOMMITED": iso = Connection.TRANSACTION_READ_UNCOMMITTED;
    	case "REPEATABLE": iso = Connection.TRANSACTION_REPEATABLE_READ;
    	case "SERIALIZABLE": iso = Connection.TRANSACTION_SERIALIZABLE;
    	}
    }
}
