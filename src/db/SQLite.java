package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import analysis.MyVisitor;

public class SQLite {

	Connection connection = null;
	Statement statement = null;
	String db;

	public SQLite(String db) throws ClassNotFoundException, SQLException {// コンストラクタ
		this.db=db;
	}

	/*
	 * データベースにメソッド情報を登録する
	 *
	 * @param visitor
	 */
	public void register(MyVisitor visitor) throws SQLException, ClassNotFoundException{
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection("jdbc:sqlite:sqlite/" + db);
		List<String> parameterType = visitor.getParameterType();
		statement = connection.createStatement();
		String sql="insert into method values(\'"+visitor.getFilePath()+"\',\'"+visitor.getClassName()
		+"\',\'"+visitor.getMethodName()+"\',\'"+visitor.getReturnType()+"\',\'";
		String params="";
		for (int i = 0; i <parameterType.size(); i++) {
			params+=parameterType.get(i);
			if (i < parameterType.size() - 1) {
				params+=",";
			}
		}
		sql+=params+"\')";
		statement.executeUpdate(sql);

		try {
			if (statement != null) {
				statement.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void test() {
		try {
			statement = connection.createStatement();
			String sql = "select * from fruits";
			ResultSet rs = statement.executeQuery(sql);
			while (rs.next()) {
				System.out.println(rs.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
