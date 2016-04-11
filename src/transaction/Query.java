package transaction;

public class Query {
	private String query;
	private String sql;
	
	public Query(String query, String sql) {
		this.query = query;
		this.sql = sql;
	}
	
	public String getQuery() {
		return query;
	}
	
	public String getSQL() {
		return sql;
	}
	
	public String toString() {
//		if(query.length() > 200) {
//			return query.substring(0, 197) + "...";
//		} else {
//			return query;
//		}
		
		return query;
	}
}
