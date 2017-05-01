package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;

import analysis.MyVisitor;

/*
 * DBに接続するクラス
 * @author s-kento
 */
public class SQLite {

	Connection connection = null;
	Statement statement = null;
	String db;

	public SQLite(String db) throws ClassNotFoundException, SQLException {// コンストラクタ
		this.db = db;
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
		String sql = "insert into method values(\'" + visitor.getFilePath() + "\',\'" + visitor.getClassName() + "\',\'"
				+ visitor.getMethodName() + "\',\'" + visitor.getReturnType() + "\',\'";
		String params = "";
		for (int i = 0; i < parameterType.size(); i++) {
			params += parameterType.get(i);
			if (i < parameterType.size() - 1) {
				params += ",";
			}
		}
		sql += params + "\')";
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
	 * クエリを満たすテーブル一覧を出力
	 *
	 * @param cli クエリ
	 */
	public void showTable(CommandLine cl) throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection("jdbc:sqlite:sqlite/" + db);
		statement = connection.createStatement();

		String sql = "select * from method where 1=1";
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
		ResultSet rs = statement.executeQuery(sql);
		while (rs.next()) {
			for (int i = 1; i <= 5; i++) {
				System.out.print(rs.getString(i));
				if (i < 5)
					System.out.print(",");
			}
			System.out.println();
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
	}

}
