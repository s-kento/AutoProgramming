package search;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.codec.DecoderException;

import register.MyVisitor;
import suggestion.Util;
import suggestion.entity.Length;

/*
 * DBに接続するクラス
 * @author s-kento
 */
public class SQLite {

	Connection connection = null;
	Statement statement = null;
	String db = "autoprog.db";
	String table = "methods";
	String lengthTableName = "lengths";

	public SQLite(String db, String table) throws ClassNotFoundException, SQLException {// コンストラクタ
		if (db != null)
			this.db = db;
		if (table != null)
			this.table = table;

		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection("jdbc:sqlite:sqlite/" + this.db);
		statement = connection.createStatement();

		/* テーブルがなければ作成する */
		String sql = "create table if not exists " + this.table
				+ "(filepath text, classname text, methodname text, returntype text, parametertype text, projectname text, startline numeric, sourcecode text)";
		statement.executeUpdate(sql);
		sql="create index if not exists signature on "+this.table+"(returntype,parametertype)";
		statement.executeUpdate(sql);

		sql = "create table if not exists " + lengthTableName +
				"(length numeric, id text, methodA text, methodB text)";
		statement.executeUpdate(sql);
		sql="create index if not exists signature on "+lengthTableName+"(id)";
		statement.executeUpdate(sql);
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
		String sql = "insert into " + table + " values(\'" + visitor.getFilePath() + "\',\'" + visitor.getClassName() + "\',\'"
				+ visitor.getMethodName() + "\',\'" + visitor.getReturnType() + "\',\'";
		String params = Util.getParameter(parameterType.toArray(new String[parameterType.size()]));
		sql += params + "\',\'" + visitor.getProjectName() + "\',\'" + visitor.getStartLine() + "\',\'"
				+ visitor.getSourceCode() + "\')";
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
	 *
	 * @return methods MethodInfoクラスのリスト
	 */
	public List<MethodInfo> getMethodInfo(CommandLine cl)
			throws ClassNotFoundException, SQLException, IOException, DecoderException {
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
		if (cl.hasOption("r")) {
			sql += " and returntype=\'" + cl.getOptionValue("r") + "\'";
		}
		if (cl.hasOption("P")) {
			sql += " and projectname=\'" + cl.getOptionValue("P") + "\'";
		}
		if (cl.hasOption("p")) {
			sql += " and parametertype=\'";
			sql += Util.getParameter(cl.getOptionValues("p"));
			sql += "\'";
		}
		/* SQL文の作成・ここまで */

		ResultSet rs = statement.executeQuery(sql);// SQL文の実行&結果の取得

		/* MethodInfoクラスの生成 */
		List<MethodInfo> methods = new ArrayList<MethodInfo>();
		while (rs.next()) {
			MethodInfo method = new MethodInfo(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
					rs.getString(5), rs.getString(6), rs.getInt(7), rs.getString(8));
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

	/*
	 * クエリを満たすメソッド一覧を，MethodInfoクラスのリストとして返す
	 *
	 * @param cli クエリ
	 *
	 * @return methods MethodInfoクラスのリスト
	 */
	public List<MethodInfo> getMethodInfo(String parameterType, String returnType)
			throws ClassNotFoundException, SQLException, IOException, DecoderException {
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection("jdbc:sqlite:sqlite/" + db);
		statement = connection.createStatement();
		String sql = "select * from " + table + " where 1=1";
		if (parameterType != null) {
			sql += " and parametertype=\'" + parameterType + "\'";
		}
		if (returnType != null) {
			sql += " and returntype=\'" + returnType + "\'";
		}
		ResultSet rs = statement.executeQuery(sql);
		List<MethodInfo> methods = new ArrayList<MethodInfo>();
		while (rs.next()) {
			MethodInfo method = new MethodInfo(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
					rs.getString(5), rs.getString(6), rs.getInt(7), rs.getString(8));
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

	public void regist(MethodInfo methodA, MethodInfo methodB, Integer length) throws SQLException, ClassNotFoundException {
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection("jdbc:sqlite:sqlite/" + db);
		statement = connection.createStatement();

		String id = Util.getId(methodA, methodB);
		String sql = "insert into " + lengthTableName + " values(\'" + length + "\',\'" + id + "\',\'"
				+ methodA.getFQName() + "\',\'" + methodB.getFQName() + "\')";
		statement.executeUpdate(sql);

		System.out.println(id + ": " + length.toString());
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

	public Length getLength(MethodInfo methodA, MethodInfo methodB) throws ClassNotFoundException, SQLException, IOException, DecoderException {
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection("jdbc:sqlite:sqlite/" + db);
		statement = connection.createStatement();

		String sql = "select * from " + lengthTableName + " where 1=1";
		String id = Util.getId(methodA, methodB);
		sql += " and id=\'" + id + "\'";

		ResultSet rs = statement.executeQuery(sql);
		Length length = null;
		while (rs.next()) {
			length = new Length(methodA, methodB, rs.getInt(1), id);
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
		return length;
	}
}
