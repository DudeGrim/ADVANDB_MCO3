package view;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JTable;

@SuppressWarnings("serial")
/**
 * 
 * @author Miko Garcia
 *
 * SQLJTable simply transforms a ResultSet into a JTable
 * where the headers contain the column names of the ResultSet
 * and the rows contain the rows in the resultSet
 */
public class SQLJTable extends JTable {
	
	/**
	 * 
	 * @param set			: The ResultSet containing the data to be used in the table.
	 * @throws SQLException	
	 */
	public SQLJTable(ResultSet set) throws SQLException {
		super(getRowData(set), getColumnNames(set));
	}
	
	/**
	 * 
	 * @param set			: The ResultSet containing the data to be used in the table.
	 * @param limit			: The limit of rows to be shown
	 * @throws SQLException
	 */
	public SQLJTable(ResultSet set, int limit) throws SQLException {
		super(getRowData(set, limit), getColumnNames(set));
	}
	
	/**
	 * 
	 * @param set			: The ResultSet containing the data to be used in the table.
	 * @return				: Returns the column names in the table
	 * @throws SQLException
	 * 
	 * This method simply gets the column names of each column in the ResultSet's Metadata
	 */
	private static String[] getColumnNames(ResultSet set) throws SQLException {
		int count = getColumnCount(set);
		String[] columns = new String[count];
		
		for(int i = 0; i < count; i++) {
			columns[i] = set.getMetaData().getColumnLabel(i + 1);
		}
		
		return columns;
	}
	
	/**
	 * 
	 * @param set			: The ResultSet containing the data to be used in the table.
	 * @return				: Returns the rows in the ResultSet as a 2D String Array
	 * @throws SQLException
	 */
	private static String[][] getRowData(ResultSet set) throws SQLException {
		int row_count = getRowCount(set);
		int col_count = getColumnCount(set);
		String[][] rowData = new String[row_count][col_count];
		
		if(set.first()) {
			for(int i = 0; i < row_count && !set.isAfterLast(); i++, set.next()) {
				for(int j = 0; j < col_count; j++) {
					rowData[i][j] = set.getString(j + 1);
				}
			}
		}
		
		return rowData;
	}
	
	/**
	 * 
	 * @param set			: The ResultSet containing the data to be used in the table.
	 * @param limit			: The limit of rows to be shown
	 * @return				: Returns the rows in the ResultSet as a 2D String Array
	 * @throws SQLException
	 */
	private static String[][] getRowData(ResultSet set, int limit) throws SQLException {
		int row_count = getRowCount(set);
		int col_count = getColumnCount(set);
		String[][] rowData = new String[row_count][col_count];
		
		if(set.first()) {
			for(int i = 0; i < limit && i < row_count && !set.isAfterLast(); i++, set.next()) {
				for(int j = 0; j < col_count; j++) {
					rowData[i][j] = set.getString(j + 1);
				}
			}
		}
		
		return rowData;
	}
	
	private static int getColumnCount(ResultSet set) throws SQLException {
		return set.getMetaData().getColumnCount();
	}
	
	private static int getRowCount(ResultSet set) throws SQLException {
		int row_count = 0;
		
		if(set.last()) {
			row_count = set.getRow();
			set.beforeFirst();
		}
		
		return row_count;
	}

}
