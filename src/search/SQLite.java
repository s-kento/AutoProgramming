package search;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;

import register.MyVisitor;

/*
 * DBに接続するクラス
 * @author s-kento
 */
public class SQLite {

	Connection connection = null;
	Statement statement = null;
	String db;
	String table;

	public SQLite(String db, String table) throws ClassNotFoundException, SQLException {// コンストラクタ
		this.db = db;
		this.table = table;
	}

	/*
	 * データベースにメソッド情報を登録する
	 *
	 * @param visitor
	 */
	public void register(MyVisitor visitor) throws SQLException, ClassNotFoundException {
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection("jdbc:sqlite:sqlite/" + db);
		statement = connection.createStatement();
		List<String> parameterType = visitor.getParameterType();

		/* SQL文の作成・ここから */
		String sql = "insert into " + table + " values(\'" + visitor.getFilePath() + "\',\'" + visitor.getClassName()
				+ "\',\'" + visitor.getMethodName() + "\',\'" + visitor.getReturnType() + "\',\'";
		String params = "";
		for (int i = 0; i < parameterType.size(); i++) {
			params += parameterType.get(i);
			if (i < parameterType.size() - 1) {
				params += ",";
			}
		}
		sql += params + "\',\'" + visitor.getProjectName() + "\')";
		/* SQL文の作成・ここまで */

		statement.executeUpdate(sql);// SQL文の実行

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

	/*
	 * クエリを満たすメソッド一覧を，MethodInfoクラスのリストとして返す
	 *
	 * @param cli クエリ
	 * @return methods MethodInfoクラスのリスト
	 */
	public List<MethodInfo> getMethodInfo(CommandLine cl) throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection("jdbc:sqlite:sqlite/" + db);
		statement = connection.createStatement();

		/* SQL文の作成・ここから */
		String sql = "select * from " + table + " where 1=1";
		if (cl.hasOption("f")) {
			sql += " and filepath=\'" + cl.getOptionValue("f") + "\'";
		}
		if (cl.hasOption("c")) {
			sql += " and classname=\'" + cl.getOptionValue("c") + "\'";
		}
		if (cl.hasOption("m")) {
			sql += " and methodname=\'" + cl.getOptionValue("m") + "\'";
		}
		if (cl.hasOption("r")) {
			sql += " and returntype=\'" + cl.getOptionValue("r") + "\'";
		}
		if (cl.hasOption("p")) {
			sql += " and parametertype=\'";
			String[] parameterType = cl.getOptionValues("p");
			Arrays.sort(parameterType);
			for (int i = 0; i < parameterType.length; i++) {
				sql += parameterType[i];
				if (i < parameterType.length - 1) {
					sql += ",";
				}
			}
			sql += "\'";
		}
		/* SQL文の作成・ここまで */

		ResultSet rs = statement.executeQuery(sql);// SQL文の実行&結果の取得

		/* MethodInfoクラスの生成 */
		List<MethodInfo> methods = new ArrayList<MethodInfo>();
		while (rs.next()) {
			MethodInfo method = new MethodInfo(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
					rs.getString(5), rs.getString(6));
			methods.add(method);
		}

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
		return methods;
	}

}
