package view;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
/**
 * 
 * @author Miko Garcia
 * 
 * This class shows the runtime and results of a query
 *
 */
public class ResultFrame extends JFrame {
	private JLabel queryLabel;
	private JLabel timeLabel;
	private JLabel rowCountLabel;
	private JTable resultTable;
	private JScrollPane scroll;
	
	/**
	 * 
	 * @param query			: query that was processed
	 * @param process_time	: time the query process finished
	 * @param results		: A ResultSet containing the results of the query
	 * @throws SQLException	
	 * 
	 */
	public ResultFrame(String query, String process_time, ResultSet results) throws SQLException {
		JPanel content = new JPanel();
		
		queryLabel = new JLabel("Query: " + query);
		timeLabel = new JLabel("Time in seconds: " + process_time);
		resultTable = new SQLJTable(results);
		rowCountLabel = new JLabel("Row(s) Returned: " + resultTable.getRowCount() + " row(s)");
		
		resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		scroll = new JScrollPane(resultTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		
		content.add(queryLabel);
		content.add(timeLabel);
		content.add(rowCountLabel);
		content.add(scroll);
		
		content.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		this.setContentPane(content);
		
		this.setTitle("Query Results for: " + query);
		this.pack();
		
		this.setVisible(true);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
	}
}
